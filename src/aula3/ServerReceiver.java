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
	private Cipher c;

	ServerReceiver(Socket socket, int id){
		c = s.getCipher();
		this.socket = socket;
		this.id = id;
	}
	
	@Override
	public void run() {
		
		 // takes input from the client socket 
       try {
		   CipherInputStream cis = new CipherInputStream(new BufferedInputStream(socket.getInputStream()), c);
			int m;
			
			while (true){ 
                try{ 
                    m = cis.read();
					System.out.println("--> " + (char) m);
                    s.addMessage((char)m, id);
  
                } 
                catch(IOException i){ 
                	socket.close(); 
        		    cis.close();
					i.printStackTrace();
                }
            }
			
       } catch (IOException e) {
			e.printStackTrace();
			
       }
      
	}
	
	
}
