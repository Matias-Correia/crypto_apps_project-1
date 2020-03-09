package aula3;

// A Java program for a Server
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Server extends Thread{
    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private ConnectionAccepter conAccepter;
    private static Server serverInstance = null;
    private ArrayList<ServerReceiver> clients = new ArrayList<>();
    
    
    // constructor with port
    public Server(int port) {
        // starts server and waits for a connection
        try {
        	this.serverInstance = this;
            server = new ServerSocket(port);
            conAccepter = new ConnectionAccepter(server, this);
            conAccepter.run();
           
        }
        catch(IOException i) {
        	try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            System.out.println(i);
        }
    }
    
    public Server getInstance() {
    	return serverInstance;
    }

    public void addClient(ServerReceiver sr) {
    	clients.add(sr);
    	sr.run();
    }
    
    public static void main(String args[]) {
        Server server = new Server(5000);
    }
}
