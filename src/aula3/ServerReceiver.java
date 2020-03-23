package aula3;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServerReceiver extends Thread{

	private Socket socket;
	private int id;
	private Server s = Server.getInstance();
	private Cipher c;
	private InputStream is = null;
	private static final String MODE = "AES/CBC/PKCS5Padding";
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
		byte[] numBlocks = {1};

		while (true){
			try{
				is.read(numBlocks);
				for(int i=0; i<numBlocks[0]; i++){
					String x = "";
					int l = is.read(cipheredLine);

					if(l > 0){

						if(numBlocks[0] == 1){
							//first is last
							byte[] decipheredLine = c.doFinal(cipheredLine);
							x = new String(decipheredLine, Charset.defaultCharset());
						}
						else if(i<numBlocks[0] - 1){
							//first isn't last or middle block
							byte[] decipheredLine = c.update(cipheredLine);
							x = new String(decipheredLine, Charset.defaultCharset());
						}
						else{
							//last block
							byte[] decipheredLine = c.doFinal(cipheredLine);
							x = new String(decipheredLine, Charset.defaultCharset());
						}

						System.out.println("-->"  + x);
					}
				}

			}
			catch(IOException i){
				i.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			}

		}

	}
	
	//cipher initialization
	private void initCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
		
		String key = "1234567890123456";
		SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
		this.c = Cipher.getInstance(MODE);
		while(true) {
			if(is.available() != 0) {
				is.read(iv);
				break;
			}
		}
		IvParameterSpec ivParams = new IvParameterSpec(iv);
		c.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
	}

}
