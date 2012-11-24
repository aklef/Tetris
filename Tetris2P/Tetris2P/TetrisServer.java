package Tetris2P;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import ocsf.server.*;
import Tetris2P.Board.Updater;
import java.util.LinkedList;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class TetrisServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 1337;
  
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF serverText;
  
  /**
   * This data structures will store the identification of all clients connected
   * It will be used to pair up player and opponent
   */
  private LinkedList<ClientNode> clientList = new LinkedList<ClientNode>();
  
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public TetrisServer(int port, ChatIF serverText) 
  {
    super(port);
    this.serverText = serverText;
  }

  //Instance methods ************************************************
  
  /**
   * This method handles any string messages from the client
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient (Object msg, ConnectionToClient client)
  {	  
	  long clientID;
	  
	  if(msg instanceof Updater)
	  { //string input has been passed from the client
		clientID = client.getId();
	  }
	  
    try
    {
        //If the message was a command message, send the instruction for interpretation
    	if(((String) msg).startsWith("#") || ((String) msg).startsWith("/"))
        	commandMessage(((String) msg).substring(1), client);
      	else{
      	    System.out.println
      	    	("["+ client.getInfo("ID") + "] " + msg);
        	this.sendToAllClients(msg);
      	}
    }
    catch(Exception e)
    {
      System.out.println 
      	("Could not send message to clients. Terminating server.");
      quit();
    }
  }
  
  /**
   * This method attempts to match a newly connected client to a client already connected so they can play Tetris
   * @param ConnectionToClient c is a newly connected client
   */
  public void newClientConnected(ConnectionToClient c)
  {
	  int indexID;
	  
	  ClientNode newNode = new ClientNode(c.getId()); //setting the player
	  clientList.add(newNode);
	  indexID = clientList.indexOf(newNode); //obtaining position of the new element in the list
	  
	  //checking for opponent
	  if(indexID == 0){
		 clientList.get(indexID).opponentID = null; //if it is the first client connected, then he has no opponent
	  }
	  else
	  {
		 //If there is a client connected that doesn't have an opponent, match him with new connected player
		 if(clientList.get(indexID - 1).opponentID == null)
		 {
			clientList.get(indexID - 1).opponentID = clientList.get(indexID).playerID;
			clientList.get(indexID).opponentID = clientList.get(indexID - 1).opponentID;
		 }
		 else
		 {
			 //if the previously connected client has an opponent, then the current opponent is null
				clientList.get(indexID).opponentID = null;
		 }
	  }
	  
  }
  
  //Sends the message to the Server User.
  public void handleMessageFromServerUI (String msg){
	    try
	    {
	        //If the message was a command message, send the instruction for interpretation
	        if(msg.startsWith("#") || msg.startsWith("/"))
	        	commandMessage(msg.substring(1), null);
	      	else{
	      		this.sendToAllClients("SERVER MSG: " + msg );
	      	}
	    }
	    catch(Exception e)
	    {
	      System.out.println 
	      	("Could not send message to clients. Terminating server.");
	      quit();
	    }
	  
  }
 
  /* *****Changed for E50**** DA, akleff
   * This method will determine the type of command that was inputed by the server admin
   * @param message The message from the UI.
  */ 
  public void commandMessage(String msg , ConnectionToClient client){
	
	//initialize local variables
	String message[]   = msg.split(" ");
	String instruction = "";
	String operand     = "";
	
	boolean hasWhiteSpace = false;
	
	//Find if a split has happened
	if ( message.length != 1) hasWhiteSpace = true;
	
	//If there is a white space, we must load the instruction with its operand
	if(hasWhiteSpace) 
	{
		instruction = message[0];
		operand 	= message[1];
	}
	else //If there is no white space, then there is no operand and only load the instruction
	instruction = message[0];
	
	// ****************************************************************************************//
	// List of all server-side usable commands
	
	switch (instruction) {
		//*******************************************************************//
		// Server Control methods
		
		// Causes the server to quit gracefully.
		case "quit": case "exit":
			quit();
		break;
		
		// Causes the server to stop listening for new clients.
		case "stop":
			stopListening();
			sendToAllClients("WARNING - Server has stopped listening for connections.");
		break;
		
		// Causes the server to start listening for new clients.
		case "start":
			try 
			{
				listen(); //Start listening for connections
				sendToAllClients("WARNING - Server now listening for connections.");
			}
			catch (Exception ex) 
			{
				System.out.println
					("ERROR - Could not listen for clients!");
			}
		break;
		
		// Causes the server to not only stop listening for new clients,
		// but also to disconnect all existing clietns.
		case "close":
			closeServer();
		break;
				
		//*******************************************************************//
		// Setter methods
		
		// Sets the port if client not connected
		case "setport": case "setPort":
			if(this.isListening())
				System.out.println
					("Server running. Close server to set port.");
			else{
				setPort(Integer.parseInt(operand));
				System.out.println
					("Port set: " + getPort());
			}
		break;
		
		//*******************************************************************//
		// Getter methods
		
		// Get the port
		case "getport": case "getPort":
			System.out.println
				("The port is: " + getPort());
		break;
		
		//*******************************************************************//
		// Helper methods
		
		// Returns a list of available commands and their usage.
		case "help": case "Help":
			System.out.println
				("   —————————————————————————————— Help ——————————————————————————————"+
				 "\n/quit	: Quit gracefully"+
				 "\n/stop	: Stop listening for new clients"+
				 "\n/close	: Stop listening for new clients & disconnect all existing clietns"+
				 "\n/getport: Returns the listening port"+
				 "\n/setport: Sets the listening port"+
				 "\n/ping	: Pong!"+
				 "\n/pong	: Ping!"
				 );
			
		break;
		
		// Ping, Pong!
		case "ping": case "Ping":
			try
			{
				client.sendToClient("Pong!");
			}
			catch (IOException e){}
		break;
		
		// Pong, Ping!
		case "pong": case "Pong":
			try
			{
				client.sendToClient("Ping!");
			}
			catch (IOException e)
			{}
		break;
		
		// Pica, Boo!
		case "pica": case "Pica":
			try
			{
				client.sendToClient("Boo!");
			}
			catch (IOException e)
			{}
		break;
		
		// Pica, Boo!
		case "doodle": case "Doodle":
			try
			{
				client.sendToClient("Di-Dah!");
			}
			catch (IOException e)
			{}
		break;
		
		// Pica, Boo!
		case "Hello": case "hello":
			try
			{
				client.sendToClient("Yo.");
			}
			catch (IOException e)
			{}
		break;
		
		
		//*******************************************************************//
		// Operation not found
		default:
			if (client == null)
			{
				System.out.println
					("> Command Not Found.");
			}else{
				try
				{
					client.sendToClient("Invalid Command.");
				}
				catch (IOException e)
				{}
			}
		break;
	}
  }
  
  /**
   * This method causes the server to quit gracefully.
   */
  public void quit()
  {
    try
    {
    	System.out.println
    		("***SERVER GOING OFFLINE***");
    	close();
    }
    catch(IOException e)
    {
    	System.out.println
    		("Could not quit server. Aboritng.");
    }
    System.exit(0);
  }
  
  /**
   * Causes the server to not only stop listening for new clients,
   * but also to disconnect all existing clietns.
   */
  public void closeServer()
  {
	stopListening();
	
	try
	{
		close();
	}
	catch (IOException e)
	{
		System.out.println
			("Could not close server. Aboritng.");
	}
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
    	("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
    	("Server no longer listening for connections.");
  }
  
  /**
   * This method overrides the one in the superclass.
   * Called when the server is closed.
   */
  protected void serverClosed()
  {
    System.out.println
    	("WARNING - Server now closed.");
  }
  
  //*****Changed for E49**** DA, AKL
  protected void clientConnected(ConnectionToClient client)
  {
	System.out.println
		("Client " + client.toString() + " connected.");
  }
  
  //*****Changed for E49**** DA, AKL
  synchronized protected void clientDisconnected( ConnectionToClient client)
  {
	System.out.println
		("Client " + client.getInfo("ID") + " disconnected.");
	sendToAllClients
		("Client " + client.getInfo("ID") + " left.");
  }
  
  //*****Changed for E49**** DA, AKL
  synchronized protected void clientException(ConnectionToClient client, Throwable exception)
  {
	
	  clientDisconnected(client);
  }
  /**
   * This will group clients into a player and their respective opponent ID
   * 
   * @author Dmitry Anglinov
   * @author Andréas K.LeF.
   *
   */
  private class ClientNode
  {
	  private Long playerID;
	  private Long opponentID;
	  	
	  public ClientNode(Long playerID)
	  {
		  this.playerID = playerID;
	  }
	  
  }
}
//End of EchoServer class
