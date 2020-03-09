package aula3;

// A Java program for a Server
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;

public class Server extends Thread{
    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private ConnectionAccepter conAccepter;
    private static Server serverInstance = null;
    private ArrayList<ServerReceiver> clients = new ArrayList<>();
    private List<String> messagelist = Collections.synchronizedList(new ArrayList<String>()); 
    private static final String MODE = "AES";
    private Cipher c;
    
    // constructor with port
    public Server(int port) {
        // starts server and waits for a connection
        try {
            String key = "1234567890123456";
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), MODE);
        	this.c = Cipher.getInstance(MODE);
        	c.init(c.DECRYPT_MODE, secretKey);
        	this.serverInstance = this;
        	server = new ServerSocket(port);
        	conAccepter = new ConnectionAccepter(server, this);
        	conAccepter.run();
        	this.run();
        }
        catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException i) {
        	try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println(i);
        }
    }
    
    @Override
    public void run() {
    	
    }
    
    public static Server getInstance() {
    	return serverInstance;
    }

    public void addClient(ServerReceiver sr) {
    	clients.add(sr);
    	sr.run();
    }
    
    public void addMessage(char m, int id) {
    	synchronized(messagelist) {
    		messagelist.add("[" + id + "] " + m);
    	}
    }
    
    public Cipher getCipher() {
		return c;
	}
    
    public static void main(String args[]) {
        Server server = new Server(5000);
    }

	
}
