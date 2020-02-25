import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Server{
  // A pre-allocated buffer for the received data
  static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );

  // Decoder for incoming text -- assume UTF-8
  static private final Charset charset = Charset.forName("UTF8");
  static private final CharsetDecoder decoder = charset.newDecoder();
  static private final CharsetEncoder encoder = charset.newEncoder();

  static private HashSet<SelectionKey> users = new HashSet<>();

  static private Selector selector;
  private static AtomicInteger userCount = new AtomicInteger(0);
  
  static public void main( String args[] ) throws Exception {
    // Parse port from command line
    int port = Integer.parseInt( "1234" );

    try {
      // Instead of creating a ServerSocket, create a ServerSocketChannel
      ServerSocketChannel ssc = ServerSocketChannel.open();

      // Set it to non-blocking, so we can use select
      ssc.configureBlocking( false );

      // Get the Socket connected to this channel, and bind it to the
      // listening port
      ServerSocket ss = ssc.socket();
      InetSocketAddress isa = new InetSocketAddress( port );
      ss.bind( isa );

      // Create a new Selector for selecting
      /*Selector*/ selector = Selector.open();

      // Register the ServerSocketChannel, so we can listen for incoming
      // connections
      ssc.register( selector, SelectionKey.OP_ACCEPT );
      //System.out.println( "Listening on port " + port );

      while (true) {
        // See if we've had any activity -- either an incoming connection,
        // or incoming data on an existing connection
        int num = selector.select();

        // If we don't have any activity, loop around and wait again
        if (num == 0) {
          continue;
        }

        // Get the keys corresponding to the activity that has been
        // detected, and process them one by one
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
          // Get a key representing one of bits of I/O activity
          SelectionKey key = it.next();

          // What kind of activity is it?
          if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

            // It's an incoming connection.  Register this socket with
            // the Selector so we can listen for input on it
            Socket s = ss.accept();
            //System.out.println( "Got connection from "+s );

            // Make sure to make it non-blocking, so we can use a selector
            // on it.
            SocketChannel sc = s.getChannel();
            sc.configureBlocking( false );

            // Register it with the selector, for reading
            sc.register( selector, SelectionKey.OP_READ );


          }
          else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {

            SocketChannel sc = null;

            try {

              // Registering a new user
              if(key.attachment() == null){
            	  int auxid = userCount.incrementAndGet();
            	  key.attach(new User(auxid));
	              users.add(key);
              }

              // It's incoming data on a connection -- process it
              boolean ok = processInput( key );
              sc = (SocketChannel) key.channel();

              // If the connection is dead, remove it from the selector
              // and close it
              if (!ok) {
                users.remove(key);
                key.cancel();
                
                Socket s = null;
                try {
                  s = sc.socket();
                  //System.out.println( "Closing connection to "+s );
                  s.close();
                } catch( IOException ie ) {
                  //System.err.println( "Error closing socket "+s+": "+ie );
                }
              }

            } catch( IOException ie ) {

              // On exception, remove this channel from the selector
              key.cancel();

              try {
                sc.close();
              } catch( IOException | NullPointerException ne ) { //JAVARDEIRA
            	  User user = (User) key.attachment();
                  System.out.println("USER " + user.getID() + " DISCONECTED");
              }

              //System.out.println( "Closed " + sc );
            }
          }
        }

        // We remove the selected keys, because we've dealt with them.
        keys.clear();
      }
    } catch( IOException ie ) {
      //System.err.println( ie );
    }
  }


  // Just read the message from the socket and send it to stdout
  static private boolean processInput(SelectionKey key) throws IOException {
    SocketChannel sc = (SocketChannel) key.channel();
    // Read the message to the buffer
    buffer.clear();
    sc.read( buffer );
    buffer.flip();

    // If no data, close the connection
    if (buffer.limit()==0) {
      return false;
    }

    String message = decoder.decode(buffer).toString();
    //System.out.println("RECEIVED MESSAGE: " + message);

    //checking if it's a \n or a Ctrl+D
    User auxUser = (User) key.attachment();
    message = auxUser.getMessage() + message; 
    message = message.replace("\n","");
    message(key, message);
   

    auxUser.cleanMessage();
    return true;
  }  


  static private void message(SelectionKey key, String message) throws IOException {
    User user = (User) key.attachment();
    if(message.charAt(0) == '/'){
      message = "/" + message;
    }
    message = "[" + user.getID() + "]"  + message;
    System.out.println(message);
  }

}
