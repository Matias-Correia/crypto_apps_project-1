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
    private static final String MODE    = "AES/CBC/PKCS5Padding";
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

                int numBlocks;
                if( inputString.length() % 16 == 0) numBlocks = (byte) (inputString.length() / 16);
                else                                numBlocks = (byte) (inputString.length() / 16 + 1);


                byte[] numBlocksBytes = toBytes(numBlocks);
                byte[] cipheredNumBlocks = new byte[16];
                cipheredNumBlocks = c.doFinal(numBlocksBytes);
                os.write(cipheredNumBlocks);

                //partitioning in blocks of size 16 bytes to cipher
                int i = 0;
                int lineSize = inputString.length();

                while(lineSize >= 16){
                    byte[] first16 = Arrays.copyOfRange(inputBytes, i * 16, (i + 1) * 16);
                    byte[] cipheredLine = c.update(first16);
                    os.write(cipheredLine);
                    i++;
                    lineSize = lineSize-16;
                }

                int remainder = inputBytes.length % 16;
                if(remainder == 0) remainder = 16;
                byte[] lastBlock = Arrays.copyOfRange(inputBytes, i*16, i*16 + remainder);
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
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        c = Cipher.getInstance(MODE);

        //iv generation
    	SecureRandom randomSecureRandom = new SecureRandom();
    	iv = new byte[c.getBlockSize()];
    	randomSecureRandom.nextBytes(iv);
    	IvParameterSpec ivParams = new IvParameterSpec(iv);

    	os.write(ivParams.getIV());
        c.init(c.ENCRYPT_MODE, secretKey, ivParams);
    }

    byte[] toBytes(int i) {
        byte[] result = new byte[16];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /* >> 0*/);

        return result;
    }

    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 5000);
    }
}