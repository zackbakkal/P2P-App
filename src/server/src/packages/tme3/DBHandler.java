package tme3;

public interface DBHandler {
	
	public boolean addClient(String mac, String ipAddress, int port);
	
	public boolean shareFile(String mac, String filename, String size);
	
	public boolean deleteFile(String mac, String filename);
	
	public String search(String filename, String mac);
	
	public String downloadFile(String mac, String filename);
}
