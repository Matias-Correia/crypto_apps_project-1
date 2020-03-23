package aula3;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServerReceiver extends Thread{

	private Socket socket;
	private int id;
	private Server s = Server.getInstance();
	private Cipher c;
	private InputStream is = null;
	private static final String MODE = "AES/CBC/NoPadding";
	private byte[] iv = new byte[16];

	ServerReceiver(Socket socket, int id){
		// establish a connection
		this.socket = socket;
		this.id = id;

		try {
			is = socket.getInputStream();
			initCipher();
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IOException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		//12345678901234567890
		byte[] cipheredLine = new byte[16];

		int numBlocks = 1;
		int counter = 0;
		boolean first = true;
			
		while (true){
			try{
				int l = is.read(cipheredLine);
				String x = "";
				if(first && l != 0) {
					first = false;
					byte[] decipheredLine = c.update(cipheredLine);
					numBlocks = decipheredLine[0];
					if(numBlocks == 1) {
						c.doFinal();
					}else {
						counter++;
					}
					
				}else if(l != 0 && counter != numBlocks) {
					byte[] decipheredLine = c.update(cipheredLine);
					counter ++;
				}else if(l != 0 && counter == numBlocks) {
					byte[] decipheredLine = c.doFinal(cipheredLine);
					counter = 0;
					numBlocks = 1; 
				}
				System.out.println("--> " + x);

			}
			catch(IOException i){
				i.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			}
			/*
			catch (BadPaddingException | IllegalBlockSizeException e) {
				e.printStackTrace();
			}

			 */

		}

	}
	
	//cipher initialization
	private void initCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String key = "1234567890123456";
		SecretKey secretKey = new SecretKeySpec(key.getBytes(), MODE);
		this.c = Cipher.getInstance(MODE);
		while(true) {
			if(is.available() != 0) {
				System.out.println("Aqui");
				is.read(iv);
				System.out.println(iv);
				break;
			}
		}
		IvParameterSpec ivParams = new IvParameterSpec(iv);
		c.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
	}
	
}
