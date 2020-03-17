package aula3;

// A Java program for a Client
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;

public class Client {
    // initialize socket and input output streams
    private Socket socket            = null;
    private Scanner  input   = null;
    private static final String MODE = "AES";
    private Cipher c;
    private OutputStream os = null;
    
    // constructor to put ip address and port
    public Client(String address, int port) {
        // establish a connection
        try {
            socket = new Socket(address, port);
            os = socket.getOutputStream();
            System.out.println("Connected");


            String key = "1234567890123456";
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), MODE);
            c = Cipher.getInstance(MODE);
            try {
                c.init(c.ENCRYPT_MODE, secretKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            // takes input from terminal
            input  = new Scanner(System.in);


        }
        catch(IOException | NoSuchAlgorithmException | NoSuchPaddingException u) {
        	u.printStackTrace();
        }
      

        // keep reading until "Over" is input
       while (true) {
            try {
                // string to read message from input
            	String inputString = input.nextLine();
                byte[] line = new byte[inputString.length()];                
                line = inputString.getBytes();                
                os.write(line);
            }
            catch(IOException i) {
            	try {
					input.close();
					os.close();
	                
				} catch (IOException e) {
					e.printStackTrace();
				}
                System.out.println(i);
            }
        }

       
       
    }

    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 5000);
    }
}