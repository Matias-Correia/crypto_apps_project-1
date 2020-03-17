package aula3;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.*;

public class Server extends Thread{
    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private ConnectionAccepter conAccepter;
    private static Server serverInstance = null;
    private ArrayList<ServerReceiver> clients = new ArrayList<>();
    private List<String> messagelist = Collections.synchronizedList(new ArrayList<String>()); 

    // constructor with port
    public Server(int port) {
        // starts server and waits for a connection
        try {
        	this.serverInstance = this;
        	server = new ServerSocket(port);
        	conAccepter = new ConnectionAccepter(server, this);
        	conAccepter.run();
        	this.run();
        }
        catch(IOException e) {
        	try {
				server.close();
			} catch (IOException l) {
				l.printStackTrace();
			}
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
    
    public static void main(String args[]) {
        Server server = new Server(5000);
    }

	
}
