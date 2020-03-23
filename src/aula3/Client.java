package aula3;

import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

public class Client {
    // initialize socket and input output streams
    private Socket socket               = null;
    private Scanner  input              = null;
    private static final String MODE    = "AES/CBC/NoPadding";
    private Cipher c;
    private OutputStream os             = null;
    private byte[] iv = new byte[16];
    
    // constructor to put ip address and port
    public Client(String address, int port) {
        // establish a connection
        try {
            socket = new Socket(address, port);
            os = socket.getOutputStream();
            System.out.println("Connected");

            try {
				initCipher();
			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
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
                byte[] inputBytes = inputString.getBytes();
                byte[] numBlocks = new byte[1];

            	//adding numBlocks to the first byte
                int lineSize = inputBytes.length + 1;

                if( lineSize % 16 == 0) numBlocks[0] = (byte) (lineSize / 16);
                else                    numBlocks[0] = (byte) (lineSize / 16 + 1);

                byte[] line = Arrays.copyOf(numBlocks, numBlocks.length + inputBytes.length);
                System.arraycopy(inputBytes, 0, line, numBlocks.length, inputBytes.length);


                //partitioning in blocks of size 16 bytes to cipher
                int i = 0;
                while(lineSize >= 16){
                    byte[] first16 = Arrays.copyOfRange(numBlocks, i * 16, (i + 1) * 16);
                    byte[] cipheredLine = c.update(first16);
                    os.write(cipheredLine);
                    i++;
                    lineSize = lineSize-16;
                }

                int remainder = numBlocks.length % 16;
                if(remainder == 0) remainder = 16;
                byte[] lastBlock = Arrays.copyOfRange(numBlocks, i*16, i*16 + remainder);
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
    

    //cipher initialization
    public void initCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
    	String key = "1234567890123456";
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), MODE);
        c = Cipher.getInstance(MODE);
    	SecureRandom randomSecureRandom = new SecureRandom();
    	iv = new byte[c.getBlockSize()];
    	randomSecureRandom.nextBytes(iv);
    	IvParameterSpec ivParams = new IvParameterSpec(iv);
    	os.write(ivParams.getIV());
        c.init(c.ENCRYPT_MODE, secretKey, ivParams);
    }

    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 5000);
    }
}