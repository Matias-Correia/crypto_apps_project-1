package aula3;

import java.net.Socket;

public class ServerReceiver extends Thread{

	private Socket socket;
	
	ServerReceiver(Socket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
	}
	
	
}
