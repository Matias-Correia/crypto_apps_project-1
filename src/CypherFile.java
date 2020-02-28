import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;


public class CypherFile {

	private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
	
    
    private static void doCrypto(int cipherMode, String key, InputStream inputFile,
            OutputStream outputFile) {
        try {
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);
             
            CipherInputStream inputStream = new CipherInputStream(inputFile, cipher);
            byte[] inputBytes = (inputFile).readAllBytes();
            inputStream.read(inputBytes);
             
            byte[] outputBytes = cipher.doFinal(inputBytes);
             
            CipherOutputStream outputStream = new CipherOutputStream(outputFile, cipher);
            outputStream.write(outputBytes);
             
            inputStream.close();
            outputStream.close();
             
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    
    public static void main(String[] args) throws FileNotFoundException {

        File initialFile = new File("src/CypherMe.txt");
        InputStream initialStream = new FileInputStream(initialFile);

        File finalFile = new File("src/DecypherMe.txt");
        OutputStream finalStream = new FileOutputStream(finalFile);

        doCrypto(Cipher.ENCRYPT_MODE, "qwertyuiopasdfgh", initialStream , finalStream);

        initialFile = new File("src/DecypherMe.txt");
        finalFile = new File("src/End.txt");

        initialStream = new FileInputStream(initialFile);
        finalStream = new FileOutputStream(finalFile);

        doCrypto(Cipher.DECRYPT_MODE, "qwertyuiopasdfgh", initialStream, finalStream);






    }
}
