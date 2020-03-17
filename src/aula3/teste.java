package aula3;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Scanner;

public class teste {

	Scanner input = null;
	
	void read() {
		
		input = new Scanner(System.in);
		while(true) {
		String aux = input.nextLine();
		System.out.println(aux);
		System.out.println(aux.length());
		}
	}
	
	public static void main(String[] args) {
		
		teste test = new teste();
		test.read();
	}
}
