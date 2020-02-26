import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.charset.*;
import java.util.Scanner;

public class Client {


    // Se for necessario adicionar variaveis ao objecto ChatClient, devem
    // ser colocadas aqui
    private Socket clientSocket;
    private String server;
    private int port;
    private BufferedReader br;
    
    static private final Charset charset = Charset.forName("UTF-8");
  	static private final CharsetEncoder encoder = charset.newEncoder();

    // Metodo a usar para acrescentar uma string a caixa de texto
    // * NAO MODIFICAR *
    private void printMessage(String message) {
      message = message.replace("\n","");
      System.out.println("PRINTING: " + message);
      //chatArea.append(message + "\n");
    }

    // Construtor
    Client(String server, int port) throws IOException {

    	// Se for necessario adicionar codigo de inicializacao ao
        // construtor, deve ser colocado aqui
        this.server = (InetAddress.getByName(server)).getHostAddress();
        this.port = port;  	
    	this.br = new BufferedReader(new InputStreamReader(System.in)); 
        
    }

    // Metodo invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    private void newMessage(String message) throws IOException {
        // PREENCHER AQUI com codigo que envia a mensagem ao servidor
        message = message.trim(); //so it doesn't send extra spaces
        System.out.println("SENDING: " + message);
        DataOutputStream dataToServer = new DataOutputStream(clientSocket.getOutputStream());
        dataToServer.write((message + "\n").getBytes("UTF-8"));
    }

    // Metodo principal do objecto
    private void run() throws IOException {
        // PREENCHER AQUI
        clientSocket = new Socket(server, port);
        new Thread(new Listener()).start();
      //Interacao para Envio de mensagem
        System.out.println("Escreva a mensagem");
    	while(true) {
    		newMessage(br.readLine());    		
    	}    	
    }

    private class Listener implements Runnable {

      public void run(){
        try{
          BufferedReader dataFromServer;
          boolean alive = true;

          while(alive){
            dataFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = dataFromServer.readLine() + "\n"; //readLine removes \n
            System.out.println("RECEIVED: " + message);

            if(message.compareTo("BYE\n") == 0){
              alive = false;
            }

            printMessage(message);
          }
          clientSocket.close();
          System.exit(0); //to close the window
        } catch(Exception e){
          e.printStackTrace();
        }

      }

    }
    
    // Instancia o ChatClient e arranca-o invocando o seu metodo run()
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 1234);
        client.run();
    }

}
