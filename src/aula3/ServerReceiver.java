package aula3;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class ServerReceiver extends Thread{

	private Socket socket;
	private int id;
	private Server s = Server.getInstance();
	private Cipher c;
	private InputStream is = null;
	private static final String MODE = "AES";


	ServerReceiver(Socket socket, int id){
		// establish a connection
		this.socket = socket;
		try {
			is = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.id = id;

		//cipher initialization
		String key = "1234567890123456";
		SecretKey secretKey = new SecretKeySpec(key.getBytes(), MODE);
		try {
			this.c = Cipher.getInstance(MODE);
			c.init(Cipher.DECRYPT_MODE, secretKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		//12345678901234567890
		byte[] cipheredLine = new byte[16];

		int numBlocks = 1;
		int counter = 0;

		while (true){
			try{
				//System.out.println("output size ===>" + c.getOutputSize(16));
				//System.out.println("block size  ===>" + c.getBlockSize());
				int l = is.read(cipheredLine);
				String x = "";
				if(counter == numBlocks){
					byte[] decipheredLine = c.doFinal(cipheredLine);
					x = new String(decipheredLine, Charset.defaultCharset());
					numBlocks = 1;
					counter = 0;
				}
				else if(counter == 0){
					//buscar o primeiro byte com o tamanho da lista de blocos
					byte[] decipheredLine = c.update(cipheredLine);
					numBlocks = decipheredLine[0];

					//remover o primeiro byte antes de imprimir
					decipheredLine = Arrays.copyOfRange(decipheredLine, 1, decipheredLine.length);
					x = new String(decipheredLine, Charset.defaultCharset());
					counter++;
				}
				else{
					byte[] decipheredLine = c.update(cipheredLine);
					x = new String(decipheredLine, Charset.defaultCharset());
					counter++;
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
	
	
}
