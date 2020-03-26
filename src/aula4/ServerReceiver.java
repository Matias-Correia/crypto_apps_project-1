package aula4;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
		int numBlocks = 0;
		byte[] cipheredNumBlocks = new byte[16];
			
		while (true){
			try{
				
				int l = 0;
				l = is.read(cipheredNumBlocks);
				if(l > 0) {
					byte[] numBlocksBytes = c.doFinal(cipheredNumBlocks);
					numBlocks = Integer.valueOf(bytetoString(numBlocksBytes));
				}
				
				
				for(int i=0; i<numBlocks; i++){
					String x;
					l = is.read(cipheredLine);

					if(l > 0){

						if(numBlocks == 1){
							//first is last
							byte[] decipheredLine = c.doFinal(cipheredLine);
							x = new String(decipheredLine, Charset.defaultCharset());
						}
						else if(i<numBlocks - 1){
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
			catch(IOException | BadPaddingException | IllegalBlockSizeException i){
				i.printStackTrace();
			}

		}

	}
	
	//cipher initialization
	private void initCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
		String key = "1234567890123456";

		byte[] derivedCipherKey = new byte[16];
		try {
			derivedCipherKey = getDerivedKey(key.getBytes(), "SHA-256", 1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SecretKey secretKey = new SecretKeySpec(derivedCipherKey, "AES");
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

	private byte[] getDerivedKey(byte[] sessionKey, String mode, int part) throws Exception {
		MessageDigest md = MessageDigest.getInstance(mode);
		byte[] bothKeys = md.digest(sessionKey);
		switch (part) {
			case 1:
				return Arrays.copyOf(bothKeys, 16);
			case 2:
				return Arrays.copyOfRange(bothKeys, 17, 32);
			default:
				throw new Exception("illegal part");
		}
	}

	private String bytetoString(byte[] bytes) {
		StringBuilder value = new StringBuilder();
		
		for(int i=0; i < bytes.length; i++) {
			value.append((char)bytes[i]);
		}
		
		return value.toString();
	}

}
