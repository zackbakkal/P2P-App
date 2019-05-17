package tme3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class DBInfoReader {
	
	private static final String URL = "jdbc:postgresql://";
	private String host;
	private int port;
	private String databaseName;
	private String username;
	private String password;
	
	public void readDBInfo() {
		Scanner input = new Scanner(System.in);
		// prompt for database info
    	
    	System.out.print("Enter Database hostname: ");
    	host = input.nextLine();
    	System.out.print("Enter hostname port: ");
    	try {
    		port = Integer.parseInt(input.nextLine());
    	} catch(NumberFormatException e) {
    		System.out.println("DBInfoReader: readDBInfo(): "
    				+ "Number Format Exception: " + e.getMessage());
    		System.exit(1);
    	}
    	System.out.print("Enter Database name: ");
    	databaseName = input.nextLine();
    	System.out.print("Enter username: ");
    	username = input.nextLine();
    	System.out.print("Enter password: ");
    	password = input.nextLine();
    	
    	input.close();
	}
	
	public void saveDBInfo() {
		File file;
		try {
			file = new File(
					new File(".").getCanonicalFile() + "\\dbinfo.txt");
			
			FileOutputStream fout = new FileOutputStream(file);
			
			StringBuilder dbinfo = new StringBuilder(URL);
			dbinfo.append(host)
					.append(":")
					.append(port)
					.append("/")
					.append(databaseName)
					.append(";")
					.append(username)
					.append(";")
					.append(password);
			
			byte[] bytes = dbinfo.toString().getBytes();
			fout.write(bytes);
			
			fout.close();
		} catch (IOException e) {
			System.out.println("DBInfoReader: saveDBInfo(): "
					+ "IO Exception: " + e.getMessage());
			System.exit(1);
		}
		
		
		
	}
	
	public static void main(String[] args) {
		DBInfoReader dbInfoReader = new DBInfoReader();
		dbInfoReader.readDBInfo();
		dbInfoReader.saveDBInfo();
		
		System.out.println();
		System.out.println("Database info succefully stored");
	}
}
