package aula3;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public class ServerReceiver extends Thread{

	private Socket socket;
	private int id;
	private Server s = Server.getInstance();
	private CipherInputStream cis;
	private Cipher c;
	private CipherInputStream in;
	
	ServerReceiver(Socket socket, int id){
		c = s.getCipher();
		this.socket = socket;
		this.id = id;
	}
	
	@Override
	public void run() {
		
		 // takes input from the client socket 
       try {
			in = new CipherInputStream(new BufferedInputStream(socket.getInputStream()),c);
			int m = 0;
			
			while (true){ 
                try{ 
                    m = in.read();
					System.out.println("--> " + (char) m);
                    s.addMessage((char)m, id);
  
                } 
                catch(IOException i){ 
                	socket.close(); 
        		    in.close(); 
                    System.out.println(i); 
                } 
            }
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} 
      
	}
	
	
}
