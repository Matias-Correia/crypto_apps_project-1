package aula6;

import javax.crypto.*;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class ServerReceiver extends Thread{

	private Socket socket;
	private int id;
	private Server s = Server.getInstance();
	private Cipher c;
	private InputStream is = null;
	private OutputStream os = null;
	private static final String MODE = "AES/CBC/PKCS5Padding";
	private byte[] iv = new byte[16];
	private byte[] derivedCipherKey = new byte[16];
	private byte[] derivedMACKey = new byte[16];
	private IvParameterSpec ivParams;
	private static final String hexp = "00a52fdac8f2c727faaea01987b5a981b5be0d2984171a86e81bc6914098e1c54edece428515283309e038261b9552d3f9caf580c73ecebd9736b17f331657a294add84bc1a621d87f4df88c4a70294c2f218766b559b4024103106c3054964f6337b3f8a63029e2abb89e13d52715f43725b69704337be73e9c671f85318981cb7b532b38e93d97a5e77d8ccf27c9d3cbf78d078ffa6b078b6d67be002916110ba87acb6b8a8bbecc66bb4739383105e445c8a22e83a3aa1c307389b84c6e3c6d300407e2683c54ec435f1aa32689b0d1c86ff471d8c881a1baeb3a107befbee44acaf0499f4751442c88d64b4057e50da1bb5850fbc9cd72db05de89cd0da7f2529dbaad9bb0671c4902949049d286c13ff792144800b7daaddcca56ff7d1da4d3a9919443f7072889a28c46bbf64ca94aa151192a7c20c1b1a582d73cc0009e2983cb5754bb18a5850d8e358f5e8fb95c20e09c871595e4522c259b446d6d4d72fff0d74a59da25ce429c8915e379421c99a909a04768528a9bcc329c88705536d06ee7c51725298cc9975d3084f52d233704cc93903eef16f86ca3ee6b81ddbbdcf6ec9de7bc17cb272d8f9b9d1a0f1eaaf31fa3469aa4994113a8f8f493cd39a2e0de8b933f50817929c3409442164a950bdf9e432230b6a7c60162d5ae4e982cd5a06273ea8603798a1b667b5d1f1f80a19a9e977392dfaf85c3d4280e9b";
	private static final String hexq = "008e0b9d881233cc376e7ae6423b08f26bc74ccfd0e8ae4cd96c8f67d9cc816af1";
	private static final String hexg = "0084ad301e3b077b5b3c067399e7050a17bb94044b9502acd7c70f8ce7060c1a069fe586dbd8eda1c313bce3003c20b565d024936d84c42f5021dd808d5f5903c172fceb111cfcd553d2d85892b53e376542cbbc3c50598874b506d222f10b7ec2417f075db389922ace033955b3b29df86e87e16097113ef823d6dac890ec925e0100f8a433ee2d676e270fb705170a022cebf9d43b1c22399cf0a901e4d5be952b2dbb3a7a29408f27c76f37c908f7acf5c5150adf1ff84adc9ae774ecc176513aeee50eebc761339c3088241ac2d7eec48ef4f0fa0249f7ea64fada6490cdac896a92e93d59e72ee39c5c4f5a97febe7f0dbeb6f32649bf2b266acaab165c8700ab5154337ce72089193953964cf403f1c0a34b67ee80e6229de15c570d6f3663a7803c21901552364c1f7028bafe43e1abea7775d79cdc338d11f102002115dff33b403d024a4b673b8db24c7d06338e0e1773b4f1df9f1022f285aa8f91f79d01219ca8f92a0e2628f6774d7236b8e0d460b973677d19257f69a85faebb8d86762e24e3a891fed0a37927828dee01fcf3dcef01f03846af0154d1a55462eca5e744eadc7b0f7e3da0228c2ab810ee745956fe9db9578817e18929e8c9f3854934022da5870b28034111946c5b989bd82111dcb87cf2014842414541bbd337d5e8d718eba2fa274c79c26c38a3998bdef4f70d2eb112a55d80bbcb4e4cc2d7";




	ServerReceiver(Socket socket, int id){
		// establish a connection
		this.socket = socket;
		this.id = id;

		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			ivReceiver();
			diffiehellman();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeySpecException | InvalidKeyException | NoSuchPaddingException | SignatureException e) {
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
	private void initCipherServer(byte[] secret) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {

		derivedCipherKey = getDerivedKey(secret, "SHA-256", '1');
		derivedMACKey = getDerivedKey(secret, "SHA-256", '2');


		SecretKey secretKey = new SecretKeySpec(derivedCipherKey, "AES");
		this.c = Cipher.getInstance(MODE);
		
	}
	
	private void ivReceiver() throws IOException {
		while(true) {
			if(is.available() != 0) {
				is.read(iv);
				break;
			}
		}
		ivParams = new IvParameterSpec(iv);
	}
	
	private void cipherModeChange(String encOrDec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
 	   SecretKey secretKey = new SecretKeySpec(derivedCipherKey, "AES");        
 	   if(encOrDec.compareTo("ENCRYPT") == 0){
           c.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
        }
        else if(encOrDec.compareTo("DECRYPT") == 0){
           c.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        }
        else throw new NoSuchAlgorithmException("Cipher cannot be initiatied with that mode");
 }

	private void diffiehellman() throws BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, SignatureException {

		// STEP 1 OF STATION-TO-STATION DIFFIE-HELLMAN
		BigInteger intP = new BigInteger(hexp, 16);
		BigInteger intG = new BigInteger(hexg, 16);
		DHParameterSpec dhParams = new DHParameterSpec(intP, intG);

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
		keyGen.initialize(dhParams, new SecureRandom());
		KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
		KeyPair bPair = keyGen.generateKeyPair();
		keyAgree.init(bPair.getPrivate());
		PublicKey bPublicKey = bPair.getPublic();

		byte[] apk = new byte[1583];
		int r = is.read(apk); //receiving g^x
		KeyFactory kf = KeyFactory.getInstance("DH");
		PublicKey aPublicKey = kf.generatePublic(new X509EncodedKeySpec(apk));

		keyAgree.doPhase(aPublicKey, true);
		byte[] secret = keyAgree.generateSecret(); //generating the agreed key

		// STEP 2 OF STATION-TO-STATION DIFFIE-HELLMAN
		initCipherServer(secret); //initCipher
		os.write(bPublicKey.getEncoded()); //sending g^y
		os.flush();

		byte[] privKey = readkey("ServerSK.key");
		PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privKey);
		KeyFactory privatekey = KeyFactory.getInstance("RSA");
		PrivateKey pvt = privatekey.generatePrivate(ks);

		//SigB(g^y, g^x)
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(pvt);
		signature.update(bPublicKey.getEncoded());
		signature.update(aPublicKey.getEncoded());
		byte[] sign = signature.sign();


		cipherModeChange("ENCRYPT");
		byte[] cipheredSignature = c.doFinal(sign);
		os.write(cipheredSignature); //sending Ek( SigB (g^y, g^x))
		os.flush();

		//STEP 3 OF STATION-TO-STATION DIFFIE-HELLMAN
		cipherModeChange("DECRYPT");
		byte[] step3 = new byte[272];

		int t = is.read(step3); //obter o Ek(SigA(g^x, g^y))
		byte[] decryptedstep3 = c.doFinal(step3);


		byte[] publiKey = readkey("ClientPK.key");
		/* Gerar public key. */
		X509EncodedKeySpec cs = new X509EncodedKeySpec(publiKey);
		KeyFactory cpk = KeyFactory.getInstance("RSA");
		PublicKey clientPublicKey = cpk.generatePublic(cs);

		signature.initVerify(clientPublicKey);
		signature.update(aPublicKey.getEncoded());
		signature.update(bPublicKey.getEncoded());
		System.out.println(signature.verify(decryptedstep3));

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
	
	private byte[] readkey(String privOrpub) throws IOException {

		Path path = Paths.get(privOrpub);
		byte[] bytes = Files.readAllBytes(path);
		
		return bytes;
	}

}
