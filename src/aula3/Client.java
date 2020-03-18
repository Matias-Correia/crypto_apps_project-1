package aula3;

import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

public class Client {
    // initialize socket and input output streams
    private Socket socket               = null;
    private Scanner  input              = null;
    private static final String MODE    = "AES";
    private Cipher c;
    private OutputStream os             = null;
    
    // constructor to put ip address and port
    public Client(String address, int port) {
        // establish a connection
        try {
            socket = new Socket(address, port);
            os = socket.getOutputStream();
            System.out.println("Connected");


            //cipher initialization
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
      

        // reading and sending input
       while (true) {
            try {

                // string to read message from input
            	String inputString = input.nextLine();
                byte[] newline = inputString.getBytes();
                byte[] line = new byte[newline.length + 1];

            	//adding numBlocks to the first byte
                int lineSize = line.length + 1;

                if( lineSize % 16 == 0) line[0] = (byte) (lineSize / 16);
                else                    line[0] = (byte) (lineSize / 16 + 1);

                for(int i=1; i<line.length; i++) { //copying the array to one that has the numBlocks in the first byte
                    line[i] = newline[i - 1];
                }

                //partitioning in blocks of size 16 bytes to cipher
                int i = 0;
                while(lineSize >= 16){
                    byte[] first16 = Arrays.copyOfRange(line, i * 16, (i + 1) * 16);
                    byte[] cipheredLine = c.update(first16);
                    os.write(cipheredLine);
                    i++;
                    lineSize = lineSize-16;
                }

                int remainder = line.length % 16;
                byte[] lastBlock = Arrays.copyOfRange(line, i*16, i*16 + remainder);
                byte[] cipheredLine = c.doFinal(lastBlock);
                os.write(cipheredLine);

            }


            catch(IOException i) {
            	try {
					input.close();
					os.close();
	                
				} catch (IOException e) {
					e.printStackTrace();
				}
                System.out.println(i);
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }



       }

       
       
    }

    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 5000);
    }
}