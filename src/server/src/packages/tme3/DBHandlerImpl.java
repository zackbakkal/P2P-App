package tme3;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/*
* @WebService indicates that this is web service interface and the name
* indicates the web service name.
*/
@WebService(
		name = "DBHandlerImpl",
		serviceName = "DBHandlerImplService"
)

/*
* @SOAPBinding indicates binding information of soap messages. Here we have
* document-literal style of web service and the parameter style is wrapped.
*/
@SOAPBinding(
		style = SOAPBinding.Style.DOCUMENT,
		parameterStyle = SOAPBinding.ParameterStyle.WRAPPED
)

public class DBHandlerImpl implements DBHandler{
	
	private DBConnector dbConnector;
	
	public DBHandlerImpl() {
		dbConnector = new DBConnector();
	}

	@WebMethod(operationName = "addClient")
	@WebResult(name = "isClientAdded")
	public boolean addClient(
			@WebParam(name = "client_Mac") 
			String mac,
			@WebParam(name = "client_IPAddress") 
			String ipAddress,
			@WebParam(name = "port") 
			int port
			) 
	{
		
		if(dbConnector.addClient(mac, ipAddress, port)) return true;
		
		return false;
	}

	@WebMethod(operationName = "shareFile")
	@WebResult(name = "isFileShared")
	public boolean shareFile(
			@WebParam(name = "file_Owner_mac") 
			String mac,
			@WebParam(name = "file_Name") 
			String filename,
			@WebParam(name= "file_Size")
			String size) 
	{
		if(dbConnector.shareFile(mac, filename, size)) return true;
		
		return false;
	}

	@WebMethod(operationName = "deleteFile")
	@WebResult(name = "isFileDeleted")
	public boolean deleteFile(
			@WebParam(name = "client_mac") 
			String mac,
			@WebParam(name = "file_Name") 
			String filename) 
	{

		if(dbConnector.deleteFile(mac, filename)) return true;
		
		return false;
	}

	@WebMethod(operationName = "search")
	@WebResult(name = "searchResult")
	public String search(
			@WebParam(name = "filename") 
			String filename,
			@WebParam(name = "client_mac") 
			String mac) {

		String result = dbConnector.search(filename, mac);
		
		return result;
	}

	@WebMethod(operationName = "downloadFile")
	@WebResult(name = "downloadInfo")
	public String downloadFile(
			@WebParam(name = "owner_mac") 
			String mac,
			@WebParam(name = "filename") 
			String filename) 
	{

		String downloadInfo = dbConnector.downloadFile(mac, filename);
		
		return downloadInfo;
	}
	
	@WebMethod(operationName = "clientExist")
	@WebResult(name = "doesClientExist")
	public boolean clientExist(
			@WebParam(name = "client_mac")
			String mac)
	{
		return dbConnector.clientExist(mac);
	}
	
	@WebMethod(operationName = "isShared")
	@WebResult(name = "isFileShared")
	public boolean isShared(
			@WebParam(name = "filename")
			String filename,
			@WebParam(name = "client_mac")
			String mac)
	{
		return dbConnector.isShared(filename, mac);
	}
	
	@WebMethod(operationName = "fileExist")
	@WebResult(name = "doesFileExist")
	public boolean fileExist(
			@WebParam(name = "filename")
			String filename,
			@WebParam(name = "owner_mac")
			String mac)
	{
		return dbConnector.fileExist(filename, mac);
	}
	
}