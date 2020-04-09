package aula5;

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
	private byte[] derivedCipherKey = new byte[16];
	private byte[] derivedMACKey = new byte[16];
	
	

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
		byte[] receivedMAC = new byte[32];
			
		while (true){
			try{
				
				int m = 0;
				m = is.read(receivedMAC);
				
				
				int l = 0;
				l = is.read(cipheredNumBlocks);
				if(l > 0) {
					byte[] numBlocksBytes = c.doFinal(cipheredNumBlocks);
					numBlocks = Integer.valueOf(bytetoString(numBlocksBytes));
				}
				byte[] decipheredLine = null;
				StringBuilder x = new StringBuilder("");
				for(int i=0; i<numBlocks; i++){
					
					l = is.read(cipheredLine);
					
					if(l > 0){

						if(numBlocks == 1){
							//first is last
							decipheredLine = c.doFinal(cipheredLine);
						}
						else if(i<numBlocks - 1){
							//first isn't last or middle block
							decipheredLine = c.update(cipheredLine);
						}else{
							//last block
							decipheredLine = c.doFinal(cipheredLine);
						}
						String aux = new String(decipheredLine, Charset.defaultCharset());
						x.append(aux);
					}					
				}
				String message = x.toString();
				byte[] messageb = message.getBytes();
				System.out.println("-->"  + x.toString());
				System.out.println(CheckMAC(messageb, receivedMAC));

			}
			catch(IOException | BadPaddingException | IllegalBlockSizeException i){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}
	
	//cipher initialization
	private void initCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
		String key = "1234567890123456";

		
		derivedCipherKey = getDerivedKey(key.getBytes(), "SHA-256", '1');
		derivedMACKey = getDerivedKey(key.getBytes(), "SHA-256", '2');


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

	private byte[] getDerivedKey(byte[] sessionKey, String mode, char c) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(mode);
		byte[] sessionAndChar = new byte[sessionKey.length + 1];
		for(int i=0; i<sessionKey.length; i++){
			sessionAndChar[i] = sessionKey[i];
		}
		//adding the char to the last position in the array
		sessionAndChar[sessionKey.length] = (byte) c;
		return  md.digest(sessionAndChar);
	}
	
	private boolean CheckMAC(byte[] message, byte[] receivedmac) {
    	byte[] hmacSha256 = null; 
    	try {
    		Mac mac = Mac.getInstance("HmacSHA256");
    	    SecretKeySpec secretKeySpec = new SecretKeySpec(derivedMACKey, "HmacSHA256");
    	    mac.init(secretKeySpec);
    	    hmacSha256 = mac.doFinal(message);
    	} catch (Exception e) {
    		throw new RuntimeException("Failed to calculate hmac-sha256", e);
    	}
    	if(Arrays.equals(receivedmac, hmacSha256)) {
    		return true;
    	}
    	return false;
    }

	private String bytetoString(byte[] bytes) {
		StringBuilder value = new StringBuilder();
		
		for(int i=0; i < bytes.length; i++) {
			value.append((char)bytes[i]);
		}
		
		return value.toString();
	}

}
