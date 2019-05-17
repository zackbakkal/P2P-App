package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.table.DefaultTableModel;
import javax.xml.rpc.ServiceException;
import javax.xml.ws.WebServiceRef;

import tme3.DBHandlerImpl;
import tme3.DBHandlerImplServiceLocator;

/**
* title: DBHandlerClient.java
* to compile: javac DBHandlerClient.java
* 
* description: This class represents a p2p application client.
* 
 * @author Zakaria Bakkal
 * @date August 25, 2018
 * @version 5
 */
public class DBHandlerClient {

    // used for client requests handled
    private final int MAX_REQ = 10;
    // the listening port of the client server
    private final int SERVER_PORT = 9000;
	
	// the web service that is consumed
	static DBHandlerImplServiceLocator service;
	// the port used to consume services
	private DBHandlerImpl port;
    // the MAC address of the machine the client resides on
    // it is used as the user id in the database
    private String mac;
    // the clients ip address used for uploading files
    private String myIP;
    // the ip address of the client that owns the required file to download
    private String remoteIP;
    // the port of the listening server of the client who will send the required file
    private int remotePort;
    // holds the path of the shared files folder
    private String sharedFolder ;
    // holds the path of the downloaded files folder
    private String downloads;
    
	/**
	 * Instantiates a new DB handler client. And initialize the service
	 * instance variable, also setup the client.
	 * 
	 * Calls: setupClient()
	 * 
	 */
	public DBHandlerClient() {
		service = new DBHandlerImplServiceLocator();
		setupClient();
	}
	
	/**
     * sets the local IP address, the MAC address of this client. Adds this
     * client to the database and loads the settings. The settings are the
     * download files folder if it was modified the last time the user
     * was using the application
     * 
     * calls: setLocalIP()
     *        setMAC()
     *        DBHandlerImpl.addClient(String, String, int)
     *        loadSettings()
     */
    private void setupClient() {
    	// sets the instance variable port that will be 
    	// used to consume the web service
    	setPort();
    	
    	// set the client identity
        setLocalIP();
        setMAC();
        
        // add the client to the database
        try {
			port.addClient(mac, myIP, SERVER_PORT);
		} catch (RemoteException e) {
			System.out.println("DBHandlerClient: setupClient(): "
					+ "RemoteException: " + e.getMessage());
			System.exit(1);
		}
        
        loadSettings();
    }
	
	/**
	 * Sets the instance variable port. 
	 */
	public void setPort() {
		try {
			port = (DBHandlerImpl) service.getDBHandlerImplPort();
		} catch (ServiceException e) {
			System.out.println("DBHandlerClient: setPort(): "
					+ "ServiceException: " + e.getMessage());
			System.exit(1);
		}
	}
    
    /**
     * sets up the shared folder to "p2psharedfolder" in the current directory,
     * and loads the folder path where the downloads are saved.
     */
    private void loadSettings() {
    	
    	// create necessary folders
    	// p2pdownloads
    	// p2psharedfolder
    	// settings
    	createNecessaryFolders();
    	
        try {
            // set the sharedFolder path
            sharedFolder = new File(".").getCanonicalPath() + "\\p2psharedfolder";
        } catch (IOException ex) {
            System.out.println("DBHandlerClient: loadSettings(): "
                        + "IO Exception " + ex.getMessage());
        }
        
        try {
            // read the downloads folder path from the downloadsFolderSettings
            // file.
            FileInputStream fis = new FileInputStream(
            		new File(".").getCanonicalPath()
                    + "\\settings\\dowlodsFolderSettings.txt");
            
            int c = 0;
            StringBuilder sb = new StringBuilder();
            while((c = fis.read()) != -1) {
                sb.append((char) c);
            }
            
            downloads = sb.toString();
            fis.close();
        } catch (FileNotFoundException ex) {
            try {
                downloads = new File(".").getCanonicalPath() + "\\p2pdownloads";
            } catch (IOException ex1) {
                System.out.println("DBHandlerClient: loadSettings(): "
                        + "IO Exception " + ex.getMessage());
            }
        } catch (IOException ex) {
            System.out.println("DBHandlerClient: loadSettings(): "
                        + "IO Exception " + ex.getMessage());
        }
        
        
    }
    
    private void createNecessaryFolders() {
    	
    	try {
			new File(new File(".").getCanonicalPath() + "\\p2pdownloads").mkdir();
			new File(new File(".").getCanonicalPath() + "\\p2psharedfolder").mkdir();
			new File(new File(".").getCanonicalPath() + "\\settings").mkdir();
		} catch (IOException e) {
			System.out.println("DBHandlerClient: createNeccessaryFolder(): "
                    + "IO Exception " + e.getMessage());
		}
    }
    /**
     * retrieves the machines mac address and stores it in the mac instance
     * variable.
     */
    private void setMAC() {
        NetworkInterface network = null;
        // holds the machine mac address
        byte[] mac = null;
        try {
            // scan the network interface
            network = NetworkInterface.getByInetAddress(setLocalIP());
            // retrieve the machine's mac address
            mac = network.getHardwareAddress();
        } catch (SocketException ex) {
            System.out.println("DBHandlerClient: setMAC(): Socket Exception "
                    + ex.getMessage());
        }
        
        // convert the mac array from byte to a string
        StringBuilder sb = new StringBuilder();
        if(mac != null) {
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
            }
            this.mac = sb.toString();
        }
    }
    
    /**
     * returns the machine's mac address
     * @return String
     */
    public String getMAC() {
        return mac;
    }
    
    /**
     * retrieves the host's IP address stores it in the myIP instance variable,
     * and returns the IP address.
     * @return InetAddress
     */
    private InetAddress setLocalIP() {
        // holds the client IP address
        InetAddress ip = null;
        try {
        	// retrieve the ip address
            ip = InetAddress.getLocalHost();
            String localIP = ip.toString();
            // store only the IP address which starts after the first "/"
            int index = localIP.indexOf("/") + 1;
            myIP = localIP.substring(index);
        } catch (UnknownHostException ex) {
            System.out.println("DBHandlerClient: setLocalIP(): Unknowns Host Exception "
                    + ex.getMessage());
        }
        
        return ip;
    }
    
    /**
     * returns the remote host IP address, where the file will be downloaded
     * from.
     * 
     * @return String
     */
    public String getRemoteIP() {
        return remoteIP;
    }
    
    /**
     * returns the remote host port number, where the file will be downloaded 
     * from
     * @return int
     */
    public int getRemotePort() {
        return remotePort;
    }
    
    /**
     * shares the file represented by the parameter filename.
     * calls: DBHandlerImpl.shareFile(String, String, String)
     * 
     * @param filename represents the file to be downloaded
     */
    public void shareFile(String filename, String size) {
        // share the file
        try {
			port.shareFile(mac, filename, size);
		} catch (RemoteException e) {
			System.out.println("DBHandlerClient: shareFile(): "
					+ "RemoteException: " + e.getMessage());
			System.exit(1);
		}
    }
    
    /**
     * calculates the file size and returns it in the format: x B, x KB, x MB and
     * x GB, where x is a number.
     * 
     * @param filename represented by the the parameter filename
     * @return String
     */
    public String getFileSize(String filename) {
        // create a file from the filename argument
        File file = new File(filename);
        // we use these to represent the units
        String[] units = {"B", "KB", "MB", "GB"};
        // used to locate which units is appropriate
        int index = 0;
        // check if the file exists
        if(file.exists()) {
            // retrieve the file size in bytes
            long size =  file.length();
            // file size is compared with 1024 because:
            // 1 KB = 1024 Bytes
            // 1 MB = 1024 KB
            // 1 GB = 1024 MG
            while(size > 1024 && index < 4) {
                size /= 1024;
                index++;
            }
            // round the result to 2 decimal places
            size = Math.round(size * 100) / 100;
            
            return size + " " + units[index];
        }
        
        return "0 B";
    }
    
    /**
     * sets the sharedFolder instance variable to the value of the parameter
     * sharedFolder.
     * 
     * @param sharedFolder
     */
    public void setSharedFolder(String sharedFolder) {
        this.sharedFolder = sharedFolder;
    }
    
    /**
     * returns the value of the sharedFolder instance variable
     * @return String
     */
    public String getSharedFolder() {
        return sharedFolder;
    }
    
    /**
     * sets the downloads instance variable to the value of the parameter.
     * 
     * @param downlaods
     */
    public void setDownloadsFolder(String downloads) {
        this.downloads = downloads;
    }
    
    /**
     * returns the value of the downloads instance variable
     * @return String
     */
    public String getDownloadsFolder() {
        return downloads;
    }
    
    /**
     * removes the file from being shared.
     * calls: DBHandlerImpl.removeFile(String, String)
     *
     * @param filename
     */
    public void removeFile(String filename) {
        try {
			port.deleteFile(mac, filename);
		} catch (RemoteException e) {
			System.out.println("DBHandlerClient: removeFile(): "
					+ "RemoteException: " + e.getMessage());
			System.exit(1);
		}
    }
    
    /**
     * searches the file represented by the parameter filename. The mac parameter
     * is passed so that files retrieved from the database are not shared by
     * the same host who is searching.
     * 
     * @param filename
     * @param mac
     * @return String[]
     */
    public String[] search(String filename, String mac) {
        // holds the search results
        String searchResult = null;
		try {
			// retrieve the search results
			searchResult = port.search(filename, mac);
		} catch (RemoteException e) {
			System.out.println("DBHandlerClient: search(): "
					+ "RemoteException: " + e.getMessage());
			System.exit(1);
		}
        // holds the records of the search result
        String[] records = null;
        // check if the search result has any records in it
        if(searchResult != null && searchResult.length() > 0) {
            // the search result are of the format:
            // filename,mac;filename,mac
            // so we split the records
            records = searchResult.split(";");
        }
        
        return records;
    }
    
    /**
     * retrieve the remote host info which is the IP and port number.
     * 
     * @param filename
     * @param mac
     */
    public void getRemoteHostInfo(String filename, String mac) {
        // store the remote host into in the format:
        // IP,port
        String downloadInfo = null;
		try {
			downloadInfo = port.downloadFile(mac, filename);
		} catch (RemoteException e) {
			System.out.println("DBHandlerClient: getRemoteHostInfo(): "
					+ "RemoteException: " + e.getMessage());
			System.exit(1);
		}
        // if the host exists
        if(downloadInfo != null) {
            // split the info
            String[] info = downloadInfo.split(",");
            // retrieve the IP
            remoteIP = info[0];
            // retrieve the port number
            remotePort = Integer.parseInt(info[1]);
        }
    }
    
    /**
     * downloads the requested file from the file owner.
     * 
     * Calls:
     * 		DownloadHandler.start()
     * 
     * @param clientInterface
     * @param filename
     * @param remoteIP
     * @param remotePort
     * @param downloadRow
     * @return DownloadHandler
     */
    public DownloadHandler downloadFile(DBHandlerClientInterface clientInterface, 
    		String filename, String remoteIP, int remotePort, int downloadRow) {
    	
    	DownloadHandler downloadHandler = 
    			new DownloadHandler(clientInterface, filename, 
    					remoteIP, remotePort, downloadRow);
    	
    	downloadHandler.start();
    	return downloadHandler;
    }
    
    /**
     * reads the expected file size from the file owner.
     * 
     * @param connection
     * @return int
     */
    public int readFileSize(Socket connection) {
    	
    	DataInputStream din = null;

        int fileSize = 0;
        // end of message here is the value -1
        int endOfMessage = -1;
        int c;
        try {
        	din = new DataInputStream(connection.getInputStream());
        	while((c = din.readInt()) != endOfMessage) {
        		fileSize = c;
        	}
        } catch (IOException ex) {
            System.out.println("DBHandlerClient: Handler: readFileSize(): "
                    + "IO exception " + ex.getMessage());
        }
        
        return fileSize;
    }
    
    /**
     * check if a file is shared
     *
     * @param filename
     * @return boolean
     */
    public boolean isShared(String filename) {
    	
            try {
				return port.isShared(filename, mac);
			} catch (RemoteException e) {
				System.out.println("DBHandlerClient: isShared(): "
						+ "RemoteException: " + e.getMessage());
				System.exit(1);
			}
            
            return false;
        }
    
    /**
     * starts the host's server to server other client's requests
     */
    public void startServer() {
        ExecutorService pool = Executors.newFixedThreadPool(10);
		// holds the InetAddress of the server
		InetAddress local = null;
        String localhost = myIP;
    	try {
            local = InetAddress.getByName(localhost);
        } catch (UnknownHostException ex) {
            System.out.println("Server: Unknown Host \"" + local + "\"");
        }
		
        try (ServerSocket server = new ServerSocket(SERVER_PORT, MAX_REQ, local)) {
            System.out.println("Server: " + server.getInetAddress() 
                    + "\tPort: " + server.getLocalPort());
            System.out.println("Accepting Connections...");
            
            while(true) {
				try {
		                    Socket connection = server.accept();
		                    System.out.println();
		                    System.out.println("Accepted connection...");
		                    System.out.println("Client: " + connection.getInetAddress()
		                            + "\tPort: " + connection.getPort());
		                    pool.submit(new UploadHandler(connection) {});
		                } catch (IOException ex) {
		                    System.out.println("Exception accepting connection" + ex);
				} catch (RuntimeException ex) {
		                    System.out.println("Unexpected error" + ex);
				}
            }
		} catch (IOException ex) {
	            System.out.println("DBHandlerClient: startServer(): "
	            		+ "Could not start server" + ex);
		}
    }
    
    
    /**
     * handles client's requests by sending wanted files to them.
     * @author Zakaria Bakkal
     */
    private class UploadHandler implements Callable<Void> {

        // the client's socket
        private final Socket connection;

        /**
         * Instantiate a new UploadHandler
         * 
         * @param connection to the remote client
         */
        public UploadHandler(Socket connection) {
        	this.connection = connection;
        }
	
        @Override
        public Void call() {
            
            // read the wanted file name from the client
            String filename = readRequestedFile();
            
            // send the requested file
            uploadFile(filename.toString());

            return null;
        }
        
        /**
         * reads the client requested file name and returns it.
         * 
         * @return String
         */
        public String readRequestedFile() {
            InputStream in = null;
            try {
                in = new BufferedInputStream(connection.getInputStream());
            } catch (IOException ex) {
                System.out.println("DBHandlerClient: Handler: call(): "
                        + "IO exception getting input stream" + ex.getMessage());
            }
            int c;
            StringBuilder filename = new StringBuilder();
            // end of message here is a * character
            int endOfMessage = 42;
            try {
                while((c = in.read()) != endOfMessage) {
                    filename.append((char) c);
                }
        
            } catch (IOException ex) {
                System.out.println("DBHandlerClient: Handler: readRequestedFile(): "
                        + "IO exception " + ex.getMessage());
            }
            
            return filename.toString();
        }
                
        /**
         * sends the file requested by a client
         * 
         * @param filename 
         */
        public void uploadFile(String filename) {
        	FileInputStream fis = null;
            BufferedInputStream bis = null;
            DataOutputStream dos = null;
            try {
            	dos = new DataOutputStream(connection.getOutputStream());
                // create the requested file
                File myFile = new File (sharedFolder + "\\" + filename.toString());
                byte [] mybytearray  = new byte [(int) myFile.length()];
                // first send the file size to the requester
                dos.writeInt((int) myFile.length());
                dos.writeInt(-1);
                dos.flush();
                // read the requested file from disk
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray, 0, mybytearray.length);
                // write the requested file to the socket
                dos.write(mybytearray,0,mybytearray.length);
                dos.flush();
                
                bis.close();
                dos.close();
            } catch (FileNotFoundException ex) {
                System.out.println("DBHandlerClient: Handler: "
                		+ " uploadFile: File not found exception " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("DBHandlerClient: Handler: "
                		+ "uploadFile: IO exception " + ex.getMessage());
            }
        }
    }

    /**
     * handles client's requests by downloading requested files
     * @author Zakaria Bakkal
     *
     */
    private class DownloadHandler extends Thread{

    	// The filename to download
    	private String filename;
    	// The remote IP to download from
    	private String remoteIP;
    	// The remote port of the remote host
    	private int remotePort;
    	// The client interface
    	private DBHandlerClientInterface clientInterface;
    	// The download row from the search table
    	private int downloadRow;
    	// the row which this thread is repsonsible for
    	private int myRow;
    	
    	/**
    	 * Instantiates a new downloader.
    	 *
    	 * @param clientInterface the client interface
    	 * @param filename the filename
    	 * @param remoteIP the remote IP
    	 * @param remotePort the remote port
    	 * @param downloadRow the download row
    	 */
    	public DownloadHandler(DBHandlerClientInterface clientInterface,
    			String filename, String remoteIP, int remotePort, int downloadRow) {
    		this.filename = filename;
    		this.remoteIP = remoteIP;
    		this.remotePort = remotePort;
    		this.clientInterface = clientInterface;
    		this.downloadRow = downloadRow;
    		this.myRow = clientInterface.getDownloadFilesTable().getRowCount() - 1;
    	}
    	
    	/* 
    	 * downloads the file, and updates the interface tables
    	 * 
    	 * Calls:
    	 * 		downloadFile()
    	 * 		updateTables(int, String, String)
         */
        public void run() {
        	downloadFile();
        	String size = clientInterface
        			.getClient().getFileSize(
        					clientInterface.getClient()
        					.getDownloadsFolder() + "\\" + filename);
        	updateTables(downloadRow, filename, size);
        }
    	
    	/**
    	 * downloads the file from the remote host.
    	 * 
    	 * Calls: requestFile()
    	 *        saveFile(Socket, String)
    	 *        
    	 *
    	 * @return Socket to the remote host
    	 */
        public Socket downloadFile() {
            // create a socket to the remote host
            Socket socket = requestFile();
            if(socket != null) {
            	int fileSize = readFileSize(socket);
    	        // save the file on disk
    	        saveFile(socket, fileSize);
            }
            
            return socket;
        }
        
        /**
         * creates a socket to the remote host, sends the filename that needs
         * to be downloaded to the remote host, and returns a socket to the 
         * remote host. 
         *
         * @return Socket
         */
        public Socket requestFile() {
            
            Socket socket = null;
            try {
                // create a socket to the remote host
                socket = new Socket(remoteIP, remotePort);
                
                // send the filename wanted
                OutputStream outStream = socket.getOutputStream();
                Writer output = new OutputStreamWriter(outStream);
                // the "*" represents end of message, assuming no file name has a "*"
                output.write(filename + "*");
                output.flush();
            } catch (UnknownHostException ex) {
                System.out.println("DBHandlerClient: requestFile(): "
                        + "Unknown Host Exception: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("DBHandlerClient: requestFile(): "
                        + "IO Exception: " + ex.getMessage());
            }
            
            return socket;
        }
        
        /**
         * Read file size.
         *
         * @param connection the connection
         * @return the int
         */
        public int readFileSize(Socket connection) {

        	InputStream in = null;
        	DataInputStream din = null;

            int fileSize = 0;
            // end of message here is the value -1
            int endOfMessage = -1;
            int c;
            
            try {
            	din = new DataInputStream(connection.getInputStream());
            	while((c = din.readInt()) != endOfMessage) {
            		fileSize = c;
            	}
            } catch (IOException ex) {
                System.out.println("DBHandlerClient: Handler: readFileSize(): "
                        + "IO exception reading filename" + ex.getMessage());
            }
            
            return fileSize;
        }
        
        /**
         * Saves the downloaded file into local disk.
         *
         * @param clientSock the client sock
         * @param fileSize the file size
         */
        private void saveFile(Socket clientSock, int fileSize) {
            
            DataInputStream dis = null;
            try {
                // represents the bytes read from the socket
                int bytesRead;
                // represents the current total bytes read from the socket
                int current = 0;
                // used to write to the file being saved on disk
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                Socket sock = clientSock;
                
                // receive file, and save it on disk
                // holds the bytes read from the socket. There is a MAX of bytes
                // this can be changed depending on the host's memory capacity.
                byte [] bytearray  = new byte [fileSize];
                
                // setup the streams
                dis = new DataInputStream(sock.getInputStream());
                fos = new FileOutputStream(
                		clientInterface.getClient()
                		.getDownloadsFolder() + "\\" + filename);
                bos = new BufferedOutputStream(fos);
                
                // read the file bytes to the array
                bytesRead = dis.read(bytearray,0,bytearray.length);
                
                current = bytesRead;
                // read the requested file
                do {
                    bytesRead =
                            dis.read(bytearray, current, (bytearray.length-current));
                    if(bytesRead > 0) {
                        current += bytesRead;
                    }
                    downloadProgress(current);
                } while(bytesRead > 0);
                
                // save the file into disk
                bos.write(bytearray, 0 , current);
                
                // close the streams
                bos.flush();
                fos.close();
                bos.close();
                sock.close();
            } catch (IOException ex) {
                System.out.println("DBHandlerClient: saveFile(): "
                        + "IO Exception Exception: " + ex.getMessage());
            } finally {
                try {
                    dis.close();
                } catch (IOException ex) {
                    System.out.println("DBHandlerClient: saveFile(): "
                        + "IO Exception Exception: " + ex.getMessage());
                }
            }
            
        }
        
        /**
         * Update the download and downloaded files tables.
         *
         * @param downloadRow the download row selected
         * @param filename the filename
         * @param size the file size
         */
        public void updateTables(int downloadRow, String filename, String size) {
        	// after the file is downloaded we move it from download table
            // to downloaded table
        	DefaultTableModel downloadedModel;
        	DefaultTableModel downloadModel;
        	
            if(filename != null && !filename.equals("No Match Found")) {
                downloadedModel = (DefaultTableModel) clientInterface.
                		getDownlowdedFilesTbale().getModel();
                
                downloadedModel.addRow(new Object[] {filename, size});
                downloadModel = (DefaultTableModel) clientInterface.
                		getDownloadFilesTable().getModel();
                
                downloadModel.removeRow(downloadRow);
            }
        }
        
        /**
         * updates the bytes read field in the downloading files table while
         * a file is being downloading.
         * 
         * @param current bytes read
         */
        public void downloadProgress(int current) {
        	// retrieve the table model
        	DefaultTableModel downloadModel = 
        			(DefaultTableModel) clientInterface.
        			getDownloadFilesTable().getModel();
        	
        	String size = "0 B";
        	
        	// formating the size depending on file size
        	if(current < 1024) {
        		size = current + " B";
        	}
        	
        	if(current >= 1024 && current < 1048576) {
        		current /= 1024;
        		size = current + " KB";
        	}
        	
        	if(current >= 1048576 && current < 1073741824) {
        		current /= 1048576;
        		size = current + " MB";
        	}
        	
        	if(current >= 1073741824) {
        		current /= 1073741824;
        		size = current + " GB";
        	}
        	
        	// update the bytes read field
        	downloadModel.setValueAt(size, myRow, 1);
        }
        
    }
}
