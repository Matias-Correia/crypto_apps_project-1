package aula3;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

public class ServerReceiver extends Thread{

	private Socket socket;
	private int id;
	private Server s = Server.getInstance();
	private Cipher c;
	private InputStream is = null;

	ServerReceiver(Socket socket, int id){
		c = s.getCipher();
		this.socket = socket;
		try {
			is = socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.id = id;
	}
	
	@Override
	public void run() {
		
		 //CipherInputStream cis = new CipherInputStream(socket.getInputStream(), c);
	byte[] m = new byte[200];
	
	while (true){ 
	    try{ 
	    	
	    	int l = is.read(m);
			//int l = socket.getInputStream().read(m);
			String x = new String(m, Charset.defaultCharset());
			System.out.println("--> " + x);
	        //s.addMessage((char) m, id);

	    } 
	    catch(IOException i){ 
			i.printStackTrace();
		   	return;
	    }
	}
      
	}
	
	
}
