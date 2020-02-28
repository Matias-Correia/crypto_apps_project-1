import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


public class CypherFile {

	private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";





    private static void doCrypto(int cipherMode, String key, InputStream inputFile,
            OutputStream outputFile) {
        try {
            //TODO acabar de por o initialization vector no AES
            byte[] iv = new byte[16];
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(iv);

            SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);


            byte[] inputBytes = new byte[inputFile.available()];
            inputFile.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);
            outputFile.write(outputBytes);

        }
        catch(NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) throws IOException {

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
