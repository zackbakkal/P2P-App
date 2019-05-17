package tme3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {

	private final String DRIVER = "org.postgresql.Driver";
	private Connection connection;
	private boolean connected;
	
	public DBConnector() {		
		connection = null;
		connected = false;
	}
	
	public void connect() {
		try {
			// retrieve the file where the database info is stored
			File file = new File(
					new File(".").getCanonicalPath() + "\\dbinfo.txt");
			// holds database info
			StringBuilder dbinfo = new StringBuilder();
			// setup the stream to read the 
			FileInputStream fin = new FileInputStream(file);
			
			// read the file
			int c;
			while((c = fin.read()) != -1) {
				dbinfo.append((char) c);
			}
			
			// split the info
			String[] info = dbinfo.toString().split(";");
			// load the driver
        	Class.forName(DRIVER);
        	// establish database connection
			connection = DriverManager.getConnection(info[0], info[1], info[2]);
			connected = true;
			fin.close();
		} catch (FileNotFoundException e) {
			System.out.println("DBConnector: connect(): "
					+ "File Not Found Exception " + e.getMessage());
		} catch (IOException e) {
			System.out.println("DBConnector: connect(): "
					+ "IO Exception " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("DBConnector: connect(): "
					+ "SQL Exception " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("DBConnector: connect(): "
					+ "Class Not Found Exception " + e.getMessage());
		}
	}

	public boolean addClient(String mac, String ipAddress, int port) {
		if(!connected) connect();
		// check if the client already exists
		if(!clientExist(mac)) {
			// construct the query
			String query = "INSERT INTO User_Table VALUES "
					+ "('" + mac + "'"
					+ ",'" + ipAddress + "'"
					+ "," + port + ")";
					
			try {
				Statement statement = connection.createStatement();
				// execute the query
				statement.executeUpdate(query);
				return true;
			} catch (SQLException e) {
				System.out.println("DBHandler: addClient(): SQL Exception " + e.getMessage());
			}
		} else { // the client already exists
			// in case the ip address or port change
			if(updateIPAddress(mac, ipAddress) && updatePort(mac, port)) return true;
		}
		return false;
	}

	public boolean shareFile(String mac, String filename, String size) {
		if(!connected) connect();
		// check if the file exists
		if(!fileExist(filename, mac)) {
			String query = "INSERT INTO File_Table VALUES "
					+ "('" + mac + "'"
					+ ",'" + filename + "'"
					+ ",'s'"
					+ ",'" + size + "')";
					
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate(query);
				return true;
			} catch (SQLException e) {
				System.out.println("DBHandler: shareFile()-if: SQL Exception " + e.getMessage());
			}
		} else if(!isShared(filename, mac)){ // if it exits check if it is shared
			String query = "UPDATE File_Table "
					+ "SET fstatus = 's' "
					+ "WHERE mac = '" + mac + "' "
					+ "AND fname = '" + filename + "'";
					
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate(query);
				return true;
			} catch (SQLException e) {
				System.out.println("DBHandler: shareFile()-else: SQL Exception " + e.getMessage());
			}
		}
		
		return false;
	}

	public boolean deleteFile(String mac, String filename) {
		if(!connected) connect();
		// check if the file exists and shared
		if(fileExist(filename, mac) && isShared(filename, mac)) {
			String query = "UPDATE File_Table "
					+ "SET fstatus = 'n' "
					+ "WHERE mac = '" + mac + "' "
					+ "AND fname = '" + filename +"'";
			
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate(query);
				return true;
			} catch (SQLException e) {
				System.out.println("DBHandler: removeFile() : SQL Exception " + e.getMessage());
			}
		}
		
		return false;
	}

	public String search(String filename, String mac) {
		if(!connected) connect();
		String query = "SELECT fname, mac, size from File_Table "
				+ "WHERE fname = '" + filename + "' "
				+ "AND mac <> '" + mac + "' "
				+ "AND fstatus = 's'";
		
		try {
			Statement statement = connection.createStatement();
			ResultSet records = statement.executeQuery(query);
			// construct the search result in the format:
			// filename,filesize,mac;filename,filesize,mac;
			StringBuilder searchResult = new StringBuilder();
			while(records.next()) {
				searchResult.append(records.getString("fname"));
				searchResult.append(",");
				searchResult.append(records.getString("size"));
				searchResult.append(",");
				searchResult.append(records.getString("mac"));
				searchResult.append(";");
			}
			
			return searchResult.toString();
		} catch (SQLException e) {
			System.out.println("DBHandler: search(): SQL Exception " + e.getMessage());
		}
		
		return null;
	}

	public String downloadFile(String mac, String filename) {
		if(!connected) connect();
		String query = "SELECT uip_address, uport from User_Table, File_Table "
				+ "WHERE File_Table.fname = '" + filename + "' "
				+ "AND File_Table.mac = '" + mac + "' "
				+ "AND User_Table.mac = '" + mac + "'";
		
		try {
			Statement statement = connection.createStatement();
			ResultSet records = statement.executeQuery(query);
			// store ip address
			if(records.next()) {
				String downloadInfo = records.getString("uip_address") + ","
						+ records.getShort("uport");
				return downloadInfo;
			}
		} catch (SQLException e) {
			System.out.println("DBHandler: SQL Exception " + e.getMessage());
		}
		
		return null;
	}
	
	/* 
	 * check if the client exists in the DB.
	 * @return boolean
	 */
	public boolean clientExist(String mac) {
		// construct the query
		String query = "SELECT mac from User_Table "
				+ "WHERE mac = '" + mac + "'";
		
		try {
			Statement statement = connection.createStatement();
			// execute the query
			ResultSet records = statement.executeQuery(query);
			// if the client exists the condition is true
			if(records.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("DBHandler: clientExist(): SQL Exception " + e.getMessage());
		}
		
		return false;
	}

	/* 
	 * updates the client ip address
	 */
	private boolean updateIPAddress(String mac, String ipAddress) {
		
		String query = "UPDATE User_Table "
				+ "SET uip_address = '" + ipAddress + "' "
				+ "WHERE mac = '" + mac + "'";
			
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			System.out.println("DBHandler: updateIPAddress(): SQL Exception " + e.getMessage());
		}
		
		return false;
	}

	/* 
	 * updates the client port
	 */
	private boolean updatePort(String mac, int port) {
		
		String query = "UPDATE User_Table "
				+ "SET uport = " + port + " "
				+ "WHERE mac = '" + mac +"'";
			
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			System.out.println("DBHandler: updatePort(): SQL Exception " + e.getMessage());
		}
		
		return false;
	}
	
	/* 
	 * checks if the exists.
	 * @return boolean
	 */
	public boolean fileExist(String filename, String mac) {
		
		String query = "SELECT fname, mac from File_Table "
				+ "WHERE mac = '" + mac + "' "
				+ "AND fname = '" + filename + "'";
		
		try {
			Statement statement = connection.createStatement();
			ResultSet records = statement.executeQuery(query);
			if(records.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("DBHandler: fileExist():SQL Exception " + e.getMessage());
		}
		
		return false;
	}
	
	/* 
	 * checks if the file is shared associated with the mac address.
	 * @return boolean
	 */
	public boolean isShared(String filename, String mac) {
	
		String query = "SELECT fname from File_Table "
				+ "WHERE mac = '" + mac + "' "
				+ "AND fname = '" + filename + "' "
				+ "AND fstatus = 's'";
		
		try {
			Statement statement = connection.createStatement();
			ResultSet records = statement.executeQuery(query);
			if(records.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("DBHandler: isShared(): SQL Exception " + e.getMessage());
		}
		
		return false;
	}
}
