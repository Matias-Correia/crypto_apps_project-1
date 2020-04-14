package aula6;

import javax.crypto.*;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    // initialize socket and input output streams
    private Socket socket               = null;
    private Scanner  input              = null;
    private static final String MODE    = "AES/CBC/PKCS5Padding";
    private Cipher c;
    private OutputStream os             = null;
    private InputStream is 				= null;
    private byte[] iv = new byte[16];
    private byte[] derivedCipherKey;
    private  byte[] derivedMACKey;
    private static final String hexp = "00a52fdac8f2c727faaea01987b5a981b5be0d2984171a86e81bc6914098e1c54edece428515283309e038261b9552d3f9caf580c73ecebd9736b17f331657a294add84bc1a621d87f4df88c4a70294c2f218766b559b4024103106c3054964f6337b3f8a63029e2abb89e13d52715f43725b69704337be73e9c671f85318981cb7b532b38e93d97a5e77d8ccf27c9d3cbf78d078ffa6b078b6d67be002916110ba87acb6b8a8bbecc66bb4739383105e445c8a22e83a3aa1c307389b84c6e3c6d300407e2683c54ec435f1aa32689b0d1c86ff471d8c881a1baeb3a107befbee44acaf0499f4751442c88d64b4057e50da1bb5850fbc9cd72db05de89cd0da7f2529dbaad9bb0671c4902949049d286c13ff792144800b7daaddcca56ff7d1da4d3a9919443f7072889a28c46bbf64ca94aa151192a7c20c1b1a582d73cc0009e2983cb5754bb18a5850d8e358f5e8fb95c20e09c871595e4522c259b446d6d4d72fff0d74a59da25ce429c8915e379421c99a909a04768528a9bcc329c88705536d06ee7c51725298cc9975d3084f52d233704cc93903eef16f86ca3ee6b81ddbbdcf6ec9de7bc17cb272d8f9b9d1a0f1eaaf31fa3469aa4994113a8f8f493cd39a2e0de8b933f50817929c3409442164a950bdf9e432230b6a7c60162d5ae4e982cd5a06273ea8603798a1b667b5d1f1f80a19a9e977392dfaf85c3d4280e9b";
    private static final String hexq = "008e0b9d881233cc376e7ae6423b08f26bc74ccfd0e8ae4cd96c8f67d9cc816af1";
    private static final String hexg = "0084ad301e3b077b5b3c067399e7050a17bb94044b9502acd7c70f8ce7060c1a069fe586dbd8eda1c313bce3003c20b565d024936d84c42f5021dd808d5f5903c172fceb111cfcd553d2d85892b53e376542cbbc3c50598874b506d222f10b7ec2417f075db389922ace033955b3b29df86e87e16097113ef823d6dac890ec925e0100f8a433ee2d676e270fb705170a022cebf9d43b1c22399cf0a901e4d5be952b2dbb3a7a29408f27c76f37c908f7acf5c5150adf1ff84adc9ae774ecc176513aeee50eebc761339c3088241ac2d7eec48ef4f0fa0249f7ea64fada6490cdac896a92e93d59e72ee39c5c4f5a97febe7f0dbeb6f32649bf2b266acaab165c8700ab5154337ce72089193953964cf403f1c0a34b67ee80e6229de15c570d6f3663a7803c21901552364c1f7028bafe43e1abea7775d79cdc338d11f102002115dff33b403d024a4b673b8db24c7d06338e0e1773b4f1df9f1022f285aa8f91f79d01219ca8f92a0e2628f6774d7236b8e0d460b973677d19257f69a85faebb8d86762e24e3a891fed0a37927828dee01fcf3dcef01f03846af0154d1a55462eca5e744eadc7b0f7e3da0228c2ab810ee745956fe9db9578817e18929e8c9f3854934022da5870b28034111946c5b989bd82111dcb87cf2014842414541bbd337d5e8d718eba2fa274c79c26c38a3998bdef4f70d2eb112a55d80bbcb4e4cc2d7";


    // constructor to put ip address and port
    public Client(String address, int port) {
        // establish a connection
        try {
            socket = new Socket(address, port);
            os = socket.getOutputStream();
            is = socket.getInputStream();
            System.out.println("Connected");

            try {

                diffieHellman();
            	
			} catch (InvalidKeyException | InvalidKeySpecException | InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
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
                byte[] mac = initMAC(inputBytes);
                
                //send HMAC
                os.write(mac);
                os.flush();
                
                //if numBlocks isn't a multiple of 16, there's an extra block for the remaining bytes
                //if numBlocks IS a multiple of 16, then there will be an extra block with just padding
                int numBlocks;
                numBlocks = (byte) (inputString.length() / 16 + 1);

                byte[] numBlocksBytes = String.valueOf(numBlocks).getBytes();
                byte[] cipheredNumBlocks = c.doFinal(numBlocksBytes);

                os.write(cipheredNumBlocks);
                os.flush();

                //partitioning in blocks of size 16 bytes to cipher
                int i = 0;
                int lineSize = inputString.length();

                while(lineSize > 16){
                    byte[] first16 = Arrays.copyOfRange(inputBytes, i * 16, (i + 1) * 16);
                    byte[] cipheredLine = c.update(first16);
                    os.write(cipheredLine);
                    os.flush();
                    i++;
                    lineSize = lineSize-16;
                }

                int remainder = inputBytes.length % 16;
                if(remainder == 0) remainder = 16;
                byte[] lastBlock = Arrays.copyOfRange(inputBytes, i*16, i*16 + remainder);
                byte[] cipheredLine = c.doFinal(lastBlock);

                os.write(cipheredLine);
                os.flush();

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
    private void initCipher(byte[] secret, String encOrDec) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {

        derivedCipherKey = getDerivedKey(secret, "SHA-256", '1');
        derivedMACKey = getDerivedKey(secret, "SHA-256", '2');
        SecretKey secretKey = new SecretKeySpec(derivedCipherKey, "AES");
        this.c = Cipher.getInstance(MODE);

        if(encOrDec.compareTo("ENCRYPT") == 0){
            //iv generation
            SecureRandom randomSecureRandom = new SecureRandom();
            iv = new byte[c.getBlockSize()];
            randomSecureRandom.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);

            os.write(ivParams.getIV());
            c.init(c.ENCRYPT_MODE, secretKey, ivParams);
        }
        else if(encOrDec.compareTo("DECRYPT") == 0){
            while(true) {
                if(is.available() != 0) {
                    is.read(iv);
                    break;
                }
            }
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            c.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        }
        else throw new NoSuchAlgorithmException("Cipher cannot be initiatied with that mode");
        return;
    }
    
    private void diffieHellman() throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, SignatureException {
        BigInteger intP = new BigInteger(hexp, 16);
        BigInteger intQ = new BigInteger(hexq, 16);
        BigInteger intG = new BigInteger(hexg, 16);
        DHParameterSpec dhParams = new DHParameterSpec(intP, intG);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        keyGen.initialize(dhParams, new SecureRandom());

        
        
        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
        KeyPair aPair = keyGen.generateKeyPair();

        keyAgree.init(aPair.getPrivate());
        PublicKey aPublicKey = aPair.getPublic();
   
        os.write(aPublicKey.getEncoded()); //envio de g^x
        os.flush();
        byte[] bPK = new byte[1583];
        
        int r = is.read(bPK);
       
        KeyFactory kf = KeyFactory.getInstance("DH");
        PublicKey bPublicKey = kf.generatePublic(new X509EncodedKeySpec(bPK));

        keyAgree.doPhase(bPublicKey, true);
        byte[] secret = keyAgree.generateSecret(); //obter o K (chave acordada via diffie-hellman)


        initCipher(secret, "DECRYPT");
        byte[] diffieResponse = new byte[128];
        r = is.read(diffieResponse); //obter o Ek( SigB (g^y, g^x))
        byte[] sign = c.doFinal(diffieResponse);

        /*
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(bPublicKey.getEncoded());
        signature.update(aPublicKey.getEncoded());
        System.out.println(signature.verify(sign));
        */



        //os.write(); //envio de Ek(SigA(g^x, g^y))
        os.flush();
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
    
    private byte[] initMAC(byte[] message) {
    	byte[] hmacSha256 = null; 
    	try {
    		Mac mac = Mac.getInstance("HmacSHA256");
    	    SecretKeySpec secretKeySpec = new SecretKeySpec(derivedMACKey, "HmacSHA256");
    	    mac.init(secretKeySpec);
    	    hmacSha256 = mac.doFinal(message);
    	} catch (Exception e) {
    		throw new RuntimeException("Failed to calculate hmac-sha256", e);
    	}
    	return hmacSha256;
    }

    private byte[] keyRead() throws NoSuchAlgorithmException {
        //KeyFactory kf = KeyFactory.getInstance("RSA");
        //AlgorithmParameterSpec params =
        //RSAKeyGenParameterSpec rsaKeyGen = new RSAKeyGenParameterSpec()
        DSAParameterSpec params = new DSAParameterSpec()
    }


    public static void main(String args[]) {
        Client client = new Client("127.0.0.1", 5000);
    }
}