package aula3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ConnectionAccepter extends Thread{

	private Socket          socket   = null;
	private ServerSocket    serverSocket   = null;
	private Server serverInstance;
	
	ConnectionAccepter(ServerSocket serverSocket, Server serverInstance){
		this.serverSocket = serverSocket;
		this.serverInstance = serverInstance;
	}
	
	@Override
	public void run() {
		try {
        	while(true) {
        		socket = serverSocket.accept();
        		System.out.println("Client accepted");
                ServerReceiver newClient = new ServerReceiver(socket);
                serverInstance.addClient(newClient);               
                socket.close();
        	}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
