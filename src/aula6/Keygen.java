package aula6;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Keygen {

	
	public static void main(String[] args) {
		Keygen kg = new Keygen();
		try {
			kg.doKg();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	private void doKg() throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Key pub = kp.getPublic();
		Key pvt = kp.getPrivate();
		
		FileOutputStream out;
		out = new FileOutputStream("ServerPK" + ".key");
		out.write(pub.getEncoded());
		out.close();
		 
		out = new FileOutputStream("ServerSK" + ".key");
		out.write(pvt.getEncoded());
		out.close();
		
	
	}
}
