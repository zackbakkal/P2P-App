# P2P-SOAP

I.	Environment

1.	Database
DBMS used: Postgres
Version: "PostgreSQL 9.6.8 on x86_64-pc-mingw64, 64-bit"
SQL used: PostgreSQL
Tables:
File_Table
CREATE TABLE file_table
(
  	mac character varying(30) NOT NULL,
  	fname character varying(256) NOT NULL,
  	fstatus "char" NOT NULL DEFAULT 's'::"char",
 	size character varying(50),
  	CONSTRAINT file_table_pkey PRIMARY KEY (mac, fname)
);

User_Table
	CREATE TABLE user_table
(
  		mac character varying(30)  NOT NULL,
 	 	uip_address inet NOT NULL,
  		uport integer NOT NULL,
  		CONSTRAINT user_table_pkey PRIMARY KEY (mac)
);

2.	Eclipse:
Java EE IDE for Web Developers
Version: Oxygen.3a Release (4.7.3a)

3.	Jar files included:
Postgresql-42.2.4.jar
Javax.mail.jar

4.	Server
WildFly 8.2.1.Final


II.	Client & Server Classes & Interfaces

1.	Server Side

Interface:
	DBHandler

Classes:
DBHandlerImpl: Implements DBHandler. This class represents the web service.
	
Run:	Runs on a server

DBConnector: Helps DBHandler connect to the database and carry database queries execution.
DBInfoReader: Helps save database info in a file for later use by the DBConnector class. This class should be ran first before a connection to a database is made. Once the info are stored no need to run it again until a new database, different than the first one, is needed then this class could be ran again to store the new database info.
Run:	java DBInfoReader

2.	Client Side

Classes:
	DBHandlerClient: Represents the p2p application client.
	DBHandlerClientInterface: Represents the client interface.
	
Run:	java DBHandlerClientInterface


	     Folders:
		Three (3) folder are used for shared, downloaded and database settings:
			p2pdownloads: holds the downloaded files.
p2psharedfolder: holds the shared files. Note: shared folder may contain shared files and files to be shared.
			settings: holds the database connection info.
III.	Generating Client

1.	Run Server 

Run DBHandlerImpl class on the server WildFly 8.2.1.Final

2.	Locate the wsdl file in the server workspace:

workspacePath/standalone\data\wsdl\tme3.war/BHandlerImplService.wsdl

3.	Copy the wsdl file into the tme3 project source folder

4.	Deploy the client


While on eclipse
a.	Right click on the wsdl file.
b.	Click chose web services option.
c.	Click on generate client.
d.	Choose deploy client
e.	Choose your client project name
f.	Click finish.
Now, you should have a new project created for you with the name you chose in step (e).
5.	Create two packages

First package name it tme3, and the second package name it client.

In the tme3 package copy and paste the classes generated in step (4):
	BDHandlerImpl.java
	DBHandlerImplProxy.java
	DBHandlerImplServiceLocator.java
	DBHandlerImplServiceSoapBindingStub.java

In the client package copy the classes provided by me:
	DBHandlerClient.java
	DBHandlerClientInterface.java

6.	Install jar files

a.	In the client project:
Add the following jar file to the built path:
			Postgresql-42.2.4.jar
b.	In the client project:
Move the following jar files to :   WebContent/Meta-INF/lib/
	axis.jar
	commons-logginf.jar
	commons-discovery-0.2.jar
	jaxrpc.jar
	saaj.jar
wsdl4j.jar	
Also, you need to add the following jar file to the built path:
			Javax.mail.jar

IV.	Running the Server and Client

1.	Run the server

Server used: WildFly 8.2.1.Final

Run DBHandlerImpl class on the server.

2.	Run the client

Run DBHandlerClientInterface class.

