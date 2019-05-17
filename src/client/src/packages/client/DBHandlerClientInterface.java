package client;

	import java.awt.event.KeyEvent;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.io.FileOutputStream;
	import java.io.IOException;

	import javax.swing.JFileChooser;
	import javax.swing.JFrame;
	import javax.swing.JTable;
	import javax.swing.table.DefaultTableModel;

	/*
	 * To change this license header, choose License Headers in Project Properties.
	 * To change this template file, choose Tools | Templates
	 * and open the template in the editor.
	 */

	/**
	* title: DBHandlerClientInterface.java
	* to compile: javac DBHandlerClientInterface.java
	* to run: java DBHandlerClientInterface
	* 
	* description: This class represents a p2p application client interface.
	* 
	 * @author Zakaria Bakkal
	 * @date August 28, 2018
	 * @version 2
	 */
	public class DBHandlerClientInterface extends JFrame {

		private static final long serialVersionUID = 1L;
		// p2p client
	    private DBHandlerClient client;
	    // holds the macs of the search result
	    private String[] macs;
	    // holds the row number of a selected row in search result table
	    private int searchResultSelectedRow;
	    // holds the row number of a selected row in shared files table
	    private int sharedFilesSelectedRow;
	    
	    /**
	     * Creates new form Client interface, instantiate a host and load
	     * shared files from the shared folder
	     */
	    public DBHandlerClientInterface() {
	        initComponents();
	        client = new DBHandlerClient();
	        searchResultSelectedRow = -1;
	        sharedFilesSelectedRow = -1;
	        loadSharedFiles();
	    }
	    
	    /**
	     * clears the search field
	     * @param evt 
	     */
	    private void searchFieldMouseClicked(java.awt.event.MouseEvent evt) {                                         
	        searchField.setText("");
	    }                                        

	    /**
	     * handles the event of clicking on the search button
	     * @param evt 
	     */
	    private void SearchButtonMouseClicked(java.awt.event.MouseEvent evt) {                                          
	        search();
	    }                                                                                 

	    /**
	     * in case the user presses enter while the cursor is in the search field.
	     * @param evt 
	     */
	    private void searchFieldKeyPressed(java.awt.event.KeyEvent evt) {                                       
	        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
	            search();
	        }
	    }                                      

	    /**
	     * search foe a file and add results to the result table
	     */
	    public void search() {
	        clearTable(resultTable);
	        // read the file name requested
	        String filename = searchField.getText();
	        // search for the file in the database
	        String[] searchResult = client.search(filename, client.getMAC());
	        
	        DefaultTableModel model = (DefaultTableModel) resultTable.getModel();
	        // if there is results add them to the result table
	        if(searchResult != null && searchResult.length > 0) {
	            macs = new String[searchResult.length];
	            for(int i = 0; i < searchResult.length; i++) {
	                String[] record = searchResult[i].split(",");
	                String name = record[0];
	                String size = record[1];
	                macs[i] = record[2];
	                
	                model.addRow(new Object[]{name, size});
	            }
	        } else {
	            model.addRow(new Object[]{"No Match Found"});
	        }
	    }
	    
	    /**
	     * register the row selected from the search result table
	     * @param evt 
	     */
	    private void resultTableMouseClicked(java.awt.event.MouseEvent evt) {                                         
	        searchResultSelectedRow = resultTable.getSelectedRow();
	    }
	    
	    /**
	     * downloads the file selected from the result table
	     * @param evt 
	     */
	    private void downloadFileMouseClicked(java.awt.event.MouseEvent evt) {                                          
	        int downloadRow = -1; 
	        String filename = null;
	        String mac = null;
	        
	        // get models of the result, download and downloaded tables
	        DefaultTableModel resultModel;
	        DefaultTableModel downloadModel = 
	        		(DefaultTableModel) downloadTable.getModel();
	        
	        // if a row is selected the value will be >= 0
	        if(searchResultSelectedRow >= 0) {
	            resultModel = (DefaultTableModel) resultTable.getModel();
	            // retrieve the filename to download
	            filename = (String) resultModel
	            		.getValueAt(searchResultSelectedRow, 0);
	            
	            // if there is records in the table
	            if(!filename.equals("No Match Found")) {
	                // retrieve the file owner's mac address
	                mac = macs[searchResultSelectedRow];

	                // add the file to the download table
	                downloadModel.addRow(new Object[]{filename, 0});
	                downloadRow = downloadModel.getRowCount() - 1;

	                // retrieve the remote host info, IP address that correspond
	                // to the mac provided
	                client.getRemoteHostInfo(filename, mac);
	                // download the file, each download is handled by a thread
	                client.downloadFile(this, filename, client.getRemoteIP(), 
	                		client.getRemotePort(), downloadRow);
	            }

	            searchResultSelectedRow = -1;
	        }
	    }                                         

	    /**
	     * register the selected row in the shared files table
	     * @param evt 
	     */
	    private void sharedFilesTableMouseClicked(java.awt.event.MouseEvent evt) {                                              
	        sharedFilesSelectedRow = sharedFilesTable.getSelectedRow();
	    }
	    
	    /**
	     * handles the event when a user click on the share a file button.
	     * @param evt 
	     */
	    private void shareFileMouseClicked(java.awt.event.MouseEvent evt) {                                       
	        // instantiate a file chooser
	        JFileChooser fileExplorer = new JFileChooser("Add File");
	        fileExplorer.setCurrentDirectory(
	        		new File(client.getSharedFolder()));
	        int returnValue = fileExplorer.showDialog(this, "ADD");
	        
	        if(returnValue == JFileChooser.APPROVE_OPTION) {
	            FileOutputStream fos = null;
	            FileInputStream in = null;
	            try {
	                // retrieve the source file from the file chooser
	                File file = fileExplorer.getSelectedFile();
	                
	                if(file.getParent().equals(client.getSharedFolder())) {
	                	// do nothing;
	                } else {
		                // create a destination file in the shared folder
		                File newFile = 
		                		new File(client.getSharedFolder() 
		                				+ "\\" + file.getName());
		                // read the source file
		                in = new FileInputStream(file.getAbsolutePath());
		                // write the destination file
		                fos = new FileOutputStream(newFile);
		                
		                // read from source and write to destination file
		                int c = 0;
		                try {
		                    while((c = in.read()) != -1) {
		                        fos.write(c);
		                    }
		                    fos.close();
		                } catch (IOException ex) {
		                    System.out.println("DBHandlerClientInterface: "
		                    		+ "addFileMouseClicked: "
		                        + "IO Exception " + ex.getMessage());
		                }
	                }
		                
	                // get the file size
					String size = client.getFileSize(file.getCanonicalPath());
	                // share the file
	                client.shareFile(file.getName(), size);
	                
	                // load shared files to the table
	                loadSharedFiles();
	            } catch (FileNotFoundException ex) {
	                System.out.println("DBHandlerClientInterface: "
	                		+ "addFileMouseClicked: "
	                        + "File not found Exception " + ex.getMessage());
	            } catch (IOException e) {
	            	System.out.println("DBHandlerClientInterface: "
                    		+ "shareFileMouseClicked: "
                        + "IO Exception " + e.getMessage());
				}
	        } else {
	            // action cancelled
	        }
	    }                                      

	    /**
	     * removes the file from being shared
	     * @param evt 
	     */
	    private void deleteFileMouseClicked(java.awt.event.MouseEvent evt) {           
	    	// check if a file is selected from the sheared files table
	        if(sharedFilesSelectedRow >= 0) {
	            DefaultTableModel model = 
	            		(DefaultTableModel) sharedFilesTable.getModel();
	            // read the file name
	            String filename = (String) model
	            		.getValueAt(sharedFilesSelectedRow, 0);
	            // remove the file from being shared
	            client.removeFile(filename);
	            
	            // reset the row selected on shared files table
	            sharedFilesSelectedRow = -1;
	            
	            // refresh the shared files table
	            loadSharedFiles();
	        }
	    }
	    
	    /**
	     * adds a file to the shared folder.
	     * @param evt 
	     */
	    private void addFileMouseClicked(java.awt.event.MouseEvent evt) {                                     
	        // instantiate a file chooser
	        JFileChooser fileExplorer = new JFileChooser("Add File");
	        int returnValue = fileExplorer.showDialog(this, "ADD");
	        
	        if(returnValue == JFileChooser.APPROVE_OPTION) {
	            FileOutputStream fos = null;
	            FileInputStream in = null;
	            try {
	                // retrieve the source file from the file chooser
	                File file = fileExplorer.getSelectedFile();
	                // create a destination file in the shared folder
	                File newFile = new File(client.getSharedFolder() 
	                				+ "\\" + file.getName());
	                // read the source file
	                in = new FileInputStream(file.getAbsolutePath());
	                // write the destination file
	                fos = new FileOutputStream(newFile);
	                
	                // read from source and write to destination file
	                int c = 0;
	                try {
	                    while((c = in.read()) != -1) {
	                        fos.write(c);
	                    }
	                } catch (IOException ex) {
	                    System.out.println("DBHandlerClientInterface: "
	                    		+ "addFileMouseClicked: "
	                        + "IO Exception " + ex.getMessage());
	                }
	                
	                // get the file size
	                String size = client.getFileSize(newFile.getCanonicalPath());
	                
	                // share the file
	                client.shareFile(newFile.getName(), size);
	                
	                // load shared files to the table
	                loadSharedFiles();
	            } catch (FileNotFoundException e) {
	                System.out.println("DBHandlerClientInterface: "
	                		+ "addFileMouseClicked: "
	                        + "File not found Exception " + e.getMessage());
	            } catch (IOException e) {
	            	System.out.println("DBHandlerClientInterface: "
	                		+ "addFileMouseClicked: "
	                        + "File not found Exception " + e.getMessage());
				} finally {
	                try {
	                    fos.close();
	                } catch (IOException ex) {
	                    //
	                }
	            }
	        } else {
	            // action cancelled
	        }
	    }                                    
                                             
	    /**
	     * clear the downloaded files table
	     * @param evt 
	     */
	    private void clearDownloadedFilesTableMouseClicked(java.awt.event.MouseEvent evt) {                                                       
	        clearTable(downloadedFilesTable);
	    }                                                      

	    /**
	     * clears the table passed as a parameter
	     * @param table 
	     */
	    private void clearTable(JTable table) {
	        DefaultTableModel dm = (DefaultTableModel) table.getModel();
	        while(dm.getRowCount() > 0) {
	            dm.removeRow(0);
	        }
	    }
	    
	    /**
	     * registers the download folder path in a file for later use.
	     * @param evt 
	     */
	    private void chooseDownloadsFolderMouseClicked(java.awt.event.MouseEvent evt) {                                                   
	        // instantiate a file chooser
	        JFileChooser fileExplorer = new JFileChooser(); 
	        // opens to the current directory
	        fileExplorer.setCurrentDirectory(new File("."));
	        // set the title
	        fileExplorer.setDialogTitle("Chose Downloads Directory");
	        // limit the option to folders only
	        fileExplorer.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        // remove the files option from the file chooser
	        fileExplorer.setAcceptAllFileFilterUsed(false);
	        
	        if (fileExplorer.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
	            try {
	                // set the downloads folder to the chosen folder
	                client.setDownloadsFolder(fileExplorer
	                		.getCurrentDirectory().getCanonicalPath()
	                        + "\\" + fileExplorer.getSelectedFile().getName());
	            } catch (IOException ex) {
	                System.out.println("DBHandlerClientInterface: "
	                		+ "chooseSharedFolderMouseClicked: "
	                        + "IO Exception " + ex.getMessage());
	            }
	            // set the downloads folder field to the new value
	            downloadsFolderPath.setText(client.getDownloadsFolder());
	            
	            // save downloads folder info for next start up
	            File file = new File(new File(".") 
	            		+ "\\settings\\downloadsFolderSettings.txt");
	            FileOutputStream fos = null;
	            try {
	                fos = new FileOutputStream(file);
	                byte[] b = downloadsFolderPath.getText().getBytes();
	                fos.write(b);
	            } catch (FileNotFoundException ex) {
	                System.out.println("DBHandlerClientInterface: "
	                		+ "chooseSharedFolderMouseClicked: "
	                        + "File not found Exception " + ex.getMessage());
	            } catch (IOException ex) {
	                System.out.println("DBHandlerClientInterface: "
	                		+ "chooseSharedFolderMouseClicked: "
	                        + "IO Exception " + ex.getMessage());
	            } finally {
	                try {
	                    fos.close();
	                } catch (IOException ex) {
	                    //
	                }
	            }
	        }
	        else {
	            // no selection made
	        }
	    }                                                  
                                           
	    /**
	     * shared all files present in the shared folder
	     * @param evt 
	     */
	    private void shareAllMouseClicked(java.awt.event.MouseEvent evt) {                                      
	        
	        // retrieve the shared folder
	        File folder = new File(client.getSharedFolder());
	        // retrieve the files in the shared folder
	        File[] listOfFiles = folder.listFiles();
	        
	        for (File file : listOfFiles) {
	            if (file.isFile()) {
	                String name = file.getName();
	             
	                String size;
					try {
						// get the file size
						size = client.getFileSize(file.getCanonicalPath());
						// share the file
		                client.shareFile(name, size);
					} catch (IOException e) {
						System.out.println("DBHandlerClientInterface: "
								+ "shareAllMousedClicked: " + e.getMessage());
					}
	                
	            }
	        }
	        
	        // load the shared files
	        loadSharedFiles();
	    }                                     

	    /**
	     * removes all the files from being shared
	     * @param evt 
	     */
	    private void deleteAllMouseClicked(java.awt.event.MouseEvent evt) {                                       
	        // retrieve the shared folder
	        File folder = new File(client.getSharedFolder());
	        // retrieve all the files in the shared folder
	        File[] listOfFiles = folder.listFiles();
	        
	        // delete the files
	        for (File file : listOfFiles) {
	            if (file.isFile()) {
	                String name = file.getName();
	                client.removeFile(name);
	            }
	        }
	        
	        clearTable(sharedFilesTable);
	    }                                                                                  

	    private void clearSearchResultMouseClicked(java.awt.event.MouseEvent evt) {                                               
	        clearTable(resultTable);
	        searchResultSelectedRow = -1;
	    }                                              

	    /**
	     * loads the shared files from the shared folder to the shared files table
	     */
	    private void loadSharedFiles() {
	        clearTable(sharedFilesTable);
	        // retrieve the shared files folder
	        File folder = new File(client.getSharedFolder());
	        // retrieve the files in the shared folder
	        File[] listOfFiles = folder.listFiles();
	        
	        DefaultTableModel model = 
	        		(DefaultTableModel) sharedFilesTable.getModel();
	        // if a file is shared we add it to the shared files table
	        for (File file : listOfFiles) {
	            if (file.isFile()) {
	                if(client.isShared(file.getName())) {
	                    String size = "0 B";
						try {
							size = client.getFileSize(file.getCanonicalPath());
						} catch (IOException e) {
							System.out.println("DBHandlerClient: loadSharedFiles(); "
									+ "IO Exception " + e.getMessage());
						}
	                    model.addRow(new Object[]{file.getName(), size});
	                }
	                
	            }
	        }
	    }
	    
	    /**
	     * returns the downloaded Files Table
	     * 
	     * @return JTable
	     */
	    public JTable getDownlowdedFilesTbale() {
	    	return downloadedFilesTable;
	    }
	    
	    /**
	     * returns the downloading files table
	     * @return JTable
	     */
	    public JTable getDownloadFilesTable() {
	    	return downloadTable;
	    }
	    
	    /**
	     * returns the local client object
	     * @return JTable
	     */
	    public DBHandlerClient getClient() {
	    	return client;
	    }
	    
	    /**
	     * @param args the command line arguments
	     */
	    public static void main(String args[]) {
	            
	    	DBHandlerClientInterface clientInterface = 
	    			new DBHandlerClientInterface();
	        clientInterface.setExtendedState(JFrame.MAXIMIZED_BOTH);
	        clientInterface.setVisible(true);
	        // start the client server to handle other client requests
	        clientInterface.client.startServer();

	    }

	    /**
	     * initialize swing components
	     */
	    private void initComponents() {

	        panel2 = new java.awt.Panel();
	        jScrollPane1 = new javax.swing.JScrollPane();
	        resultTable = new javax.swing.JTable();
	        searchResultLabel = new java.awt.Label();
	        searchField = new java.awt.TextField();
	        SearchButton = new java.awt.Button();
	        downloadFile = new java.awt.Button();
	        clearSearchResult = new java.awt.Button();
	        panel3 = new java.awt.Panel();
	        downloadsFolderPath = new java.awt.TextField();
	        chooseDownloadsFolder = new java.awt.Button();
	        downloadsFolderLabel = new java.awt.Label();
	        doenloadedFilesLabel = new java.awt.Label();
	        jScrollPane4 = new javax.swing.JScrollPane();
	        downloadedFilesTable = new javax.swing.JTable();
	        clearDownloadedFilesTable = new java.awt.Button();
	        panel4 = new java.awt.Panel();
	        downloadsLabel = new java.awt.Label();
	        jScrollPane2 = new javax.swing.JScrollPane();
	        downloadTable = new javax.swing.JTable();
	        jPanel1 = new javax.swing.JPanel();
	        panel1 = new java.awt.Panel();
	        jScrollPane3 = new javax.swing.JScrollPane();
	        sharedFilesTable = new javax.swing.JTable();
	        sharedFilesLabel = new java.awt.Label();
	        deleteAll = new java.awt.Button();
	        deleteFile = new java.awt.Button();
	        addFile = new java.awt.Button();
	        shareFile = new java.awt.Button();
	        shareAll = new java.awt.Button();

	        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	        setTitle("P2P APPLICATION");
	        setName("clientInterface"); // NOI18N

	        panel2.setPreferredSize(new java.awt.Dimension(430, 360));

	        resultTable.setModel(new javax.swing.table.DefaultTableModel(
	        		new Object [][] {}, new String [] {"Name", "Size"}) {

					private static final long serialVersionUID = 1L;
					boolean[] canEdit = new boolean [] {
	                false, false
	            };

	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        });
	        resultTable.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
	        resultTable.setRowHeight(20);
	        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                resultTableMouseClicked(evt);
	            }
	        });
	        jScrollPane1.setViewportView(resultTable);

	        searchResultLabel.setName("resultLabel"); // NOI18N
	        searchResultLabel.setText("Results");

	        searchField.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
	        searchField.setText("Enter File Name here");
	        searchField.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                searchFieldMouseClicked(evt);
	            }
	        });
	        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
	            public void keyPressed(java.awt.event.KeyEvent evt) {
	                searchFieldKeyPressed(evt);
	            }
	        });

	        SearchButton.setLabel("Search");
	        SearchButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                SearchButtonMouseClicked(evt);
	            }
	        });

	        downloadFile.setLabel("Download");
	        downloadFile.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                downloadFileMouseClicked(evt);
	            }
	        });

	        clearSearchResult.setLabel("Clear");
	        clearSearchResult.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                clearSearchResultMouseClicked(evt);
	            }
	        });

	        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
	        panel2.setLayout(panel2Layout);
	        panel2Layout.setHorizontalGroup(
	            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(panel2Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
	                    .addComponent(searchField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
	                        .addGap(0, 0, Short.MAX_VALUE)
	                        .addComponent(clearSearchResult, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(downloadFile, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
	                        .addComponent(searchResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(SearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                .addContainerGap())
	        );
	        panel2Layout.setVerticalGroup(
	            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(panel2Layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(panel2Layout.createSequentialGroup()
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(SearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(panel2Layout.createSequentialGroup()
	                        .addGap(33, 33, 33)
	                        .addComponent(searchResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(downloadFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(clearSearchResult, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(20, Short.MAX_VALUE))
	        );

	        panel3.setPreferredSize(new java.awt.Dimension(161, 360));

	        chooseDownloadsFolder.setLabel("Choose");
	        chooseDownloadsFolder.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                chooseDownloadsFolderMouseClicked(evt);
	            }
	        });

	        downloadsFolderLabel.setText("Downloads Folder");

	        doenloadedFilesLabel.setText("Dowloaded Files");

	        downloadedFilesTable.setModel(new javax.swing.table.DefaultTableModel(
	        		new Object [][] {}, new String [] {"Name", "Size"}) {

						private static final long serialVersionUID = 1L;
						boolean[] canEdit = new boolean [] {
								false, false
	            };

	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        });
	        jScrollPane4.setViewportView(downloadedFilesTable);

	        clearDownloadedFilesTable.setLabel("Clear");
	        clearDownloadedFilesTable.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                clearDownloadedFilesTableMouseClicked(evt);
	            }
	        });

	        javax.swing.GroupLayout panel3Layout = new javax.swing.GroupLayout(panel3);
	        panel3.setLayout(panel3Layout);
	        panel3Layout.setHorizontalGroup(
	            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                    .addGroup(panel3Layout.createSequentialGroup()
	                        .addComponent(downloadsFolderLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(downloadsFolderPath, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
	                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panel3Layout.createSequentialGroup()
	                        .addComponent(doenloadedFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGap(0, 0, Short.MAX_VALUE))
	                    .addGroup(panel3Layout.createSequentialGroup()
	                        .addGap(0, 0, Short.MAX_VALUE)
	                        .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
	                            .addComponent(clearDownloadedFilesTable, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addComponent(chooseDownloadsFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
	                .addGap(28, 28, 28))
	        );
	        panel3Layout.setVerticalGroup(
	            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(panel3Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(downloadsFolderPath, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(downloadsFolderLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(chooseDownloadsFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(5, 5, 5)
	                .addComponent(doenloadedFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(clearDownloadedFilesTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(15, Short.MAX_VALUE))
	        );

	        downloadsLabel.setName("downloadingLabel"); // NOI18N
	        downloadsLabel.setText("Downloading");

	        downloadTable.setModel(new javax.swing.table.DefaultTableModel(
	            new Object [][] {}, new String [] {"Name", "Bytes Downloaded"}) {

					private static final long serialVersionUID = 1L;
					boolean[] canEdit = new boolean [] {
	                false, false
	            };

	            public boolean isCellEditable(int rowIndex, int columnIndex) {
	                return canEdit [columnIndex];
	            }
	        });
	        downloadTable.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
	        downloadTable.setRowHeight(20);
	        jScrollPane2.setViewportView(downloadTable);

	        javax.swing.GroupLayout panel4Layout = new javax.swing.GroupLayout(panel4);
	        panel4.setLayout(panel4Layout);
	        panel4Layout.setHorizontalGroup(
	            panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(panel4Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(panel4Layout.createSequentialGroup()
	                        .addComponent(downloadsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                        .addGap(0, 0, Short.MAX_VALUE))
	                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
	                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
	                        .addGap(0, 0, Short.MAX_VALUE)))
	                .addContainerGap())
	        );
	        panel4Layout.setVerticalGroup(
	            panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(panel4Layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(downloadsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
	        jPanel1.setLayout(jPanel1Layout);
	        jPanel1Layout.setHorizontalGroup(
	            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGap(0, 0, Short.MAX_VALUE)
	        );
	        jPanel1Layout.setVerticalGroup(
	            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGap(0, 0, Short.MAX_VALUE)
	        );

	        panel1.setPreferredSize(new java.awt.Dimension(483, 242));

	        sharedFilesTable.setAutoCreateRowSorter(true);
	        sharedFilesTable.setModel(new javax.swing.table.DefaultTableModel(
	            new Object [][] {}, new String [] {"Name", "Size"}));
	        sharedFilesTable.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                sharedFilesTableMouseClicked(evt);
	            }
	        });
	        jScrollPane3.setViewportView(sharedFilesTable);

	        sharedFilesLabel.setText("Shared Files");

	        deleteAll.setLabel("Delete All");
	        deleteAll.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                deleteAllMouseClicked(evt);
	            }
	        });

	        deleteFile.setLabel("Delete");
	        deleteFile.setName(""); // NOI18N
	        deleteFile.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                deleteFileMouseClicked(evt);
	            }
	        });

	        addFile.setLabel("Add File");
	        addFile.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                addFileMouseClicked(evt);
	            }
	        });

	        shareFile.setLabel("Share File");
	        shareFile.setName("shareFile"); // NOI18N
	        shareFile.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                shareFileMouseClicked(evt);
	            }
	        });

	        shareAll.setLabel("Share All");
	        shareAll.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                shareAllMouseClicked(evt);
	            }
	        });

	        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
	        panel1.setLayout(panel1Layout);
	        panel1Layout.setHorizontalGroup(
	            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(panel1Layout.createSequentialGroup()
	                .addContainerGap()
	                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(sharedFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                        .addGroup(panel1Layout.createSequentialGroup()
	                            .addComponent(addFile, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                            .addComponent(shareFile, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                            .addComponent(shareAll, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(deleteFile, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
	                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                            .addComponent(deleteAll, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
	                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                .addGap(0, 21, Short.MAX_VALUE))
	        );
	        panel1Layout.setVerticalGroup(
	            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(sharedFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(deleteAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(deleteFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(shareAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(shareFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(addFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(11, Short.MAX_VALUE))
	        );

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(15, 15, 15)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(panel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                .addGap(29, 29, 29)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
	                    .addComponent(panel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                .addContainerGap())
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addGap(5, 5, 5)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(panel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(28, 28, 28)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	                    .addComponent(panel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(0, 16, Short.MAX_VALUE))
	        );

	        pack();
	    }// </editor-fold>
	    
	    // Variables declaration - do not modify                     
	    private java.awt.Button SearchButton;
	    private java.awt.Button addFile;
	    private java.awt.Button chooseDownloadsFolder;
	    private java.awt.Button clearDownloadedFilesTable;
	    private java.awt.Button clearSearchResult;
	    private java.awt.Button deleteAll;
	    private java.awt.Button deleteFile;
	    private java.awt.Label doenloadedFilesLabel;
	    private java.awt.Button downloadFile;
	    private javax.swing.JTable downloadTable;
	    private javax.swing.JTable downloadedFilesTable;
	    private java.awt.Label downloadsFolderLabel;
	    private java.awt.TextField downloadsFolderPath;
	    private java.awt.Label downloadsLabel;
	    private javax.swing.JPanel jPanel1;
	    private javax.swing.JScrollPane jScrollPane1;
	    private javax.swing.JScrollPane jScrollPane2;
	    private javax.swing.JScrollPane jScrollPane3;
	    private javax.swing.JScrollPane jScrollPane4;
	    private java.awt.Panel panel1;
	    private java.awt.Panel panel2;
	    private java.awt.Panel panel3;
	    private java.awt.Panel panel4;
	    private javax.swing.JTable resultTable;
	    private java.awt.TextField searchField;
	    private java.awt.Label searchResultLabel;
	    private java.awt.Button shareAll;
	    private java.awt.Button shareFile;
	    private java.awt.Label sharedFilesLabel;
	    private javax.swing.JTable sharedFilesTable;
	    // End of variables declaration                   
	}

