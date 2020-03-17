package aula3;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
		
		byte[] cipheredLine = new byte[16];

		while (true){
			try{

				int l = is.read(cipheredLine);
				byte[] decipheredLine = c.doFinal(cipheredLine);

				String x = new String(decipheredLine, Charset.defaultCharset());
				System.out.println("--> " + x);
				//s.addMessage((char) m, id);

			}
			catch(IOException i){
				i.printStackTrace();
			}
			catch (BadPaddingException | IllegalBlockSizeException e) {
				e.printStackTrace();
			}
		}

	}
	
	
}
