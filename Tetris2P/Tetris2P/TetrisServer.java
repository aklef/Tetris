package Tetris2P;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;

import ocsf.server.*;
import Tetris2P.Board.Updater;

import java.util.ArrayList;

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
  
    /**
     * The default port to listen on.
     */
    public final static int DEFAULT_PORT = 1337;
    /**
     * It will be used to pair up player and opponent
     */
    private ArrayList<ClientNode> clientList;
    /**
     * The interface type variable.  It allows the implementation of 
     * the display method in the client.
     */
    private final ChatIF serverOutput;
    
    
    //*************************************CONSTRUCTOR*************************************//
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
   public TetrisServer(int port, ChatIF serverText) 
   {
    	// Calls constructor in parent
    	super(port);
    	serverOutput = serverText;
    	clientList = new ArrayList<ClientNode>();
   }

   //*************************************MESSAGE-HANDLERS*************************************//
  
  /**
   * This method handles any messages send from the client to the server.
   * If an updater message is detected, it is sent to the opponent of the client.
   * If a string message is detected, it can be a command message or chat to be sent to all clients.
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient (Object obj, ConnectionToClient client)
  {
	// Object received is an Updater.
	if ( obj instanceof Updater)
	{
		Updater update = (Updater) obj;
		String command = update.getCommandMessage();
		
		if ( command != "")
		{
			try
			{
				tetrisCommandMessage(command, client);
			}
			catch (IOException e) { serverOutput.display("[ERROR] Could not parse command message."); }
		}
		else
		{// Send game command messages to be executed
			try
			{
				commandMessage(update.getCommandMessage(), client);
			}
			catch(IOException e) { serverOutput.display("[ERROR] Could not parse client input"); }
		}
		
		performUpdate(update, client);
		return;
	}
	else 
	// Continue assuming a string message has been sent to the server
    try
    {
    	String command = (String) obj;
    	//If the message was a command message, send the instruction for interpretation
    	if(command.startsWith("#"))
        	commandMessage(command.substring(1), client);
      	else{
      	    serverOutput.display(("["+ client.getInfo("ID") + "] " + command));
        	sendToAllClients("["+client.getInfo("ID")+"] "+command);
      	}
    }
    catch(Exception e)
    {
      serverOutput.display("[ERROR] Could not send message to clients.");
      //serverOutput.display("[CRITICAL] Terminating server.");
      //quit();
    }
  }

  
  /**
   * Receives input from the Server User.
   * 
   * @param msg input from the server user.
   */
  public void handleMessageFromServerUI (String msg){
	    try
	    {
	        //If the message was a command message, send the instruction for interpretation
	        if(msg.startsWith("#") || msg.startsWith("/"))
	        	commandMessage(msg.substring(1), null);
	      	else{
	      		serverOutput.display(msg);
	      		sendToAllClients("[SERVER MSG] " + msg );
	      	}
	    }
	    catch(IOException e)
	    {
	    	serverOutput.display("[ERROR] Could not parse server input.");
	    	serverOutput.display("[CRITICAL] Terminating server.");
	    	
	    	quit();
	    }
	  
  }
  /**
	 * This method will determine the type of command that was sent by the server
	 * @param message The message from the server.
	 */
	@SuppressWarnings("unused")
	public void tetrisCommandMessage( String msg, ConnectionToClient client)  throws IOException
	{
		//initialize local variables
		String message[]   = msg.split(" ");
		String instruction = "";
		String operand     = "";
		
		boolean hasWhiteSpace = false;
		
		//Find if multipart message
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
		// List of all client-side usable commands
		
		switch (instruction.toLowerCase())
		{
			//*******************************************************************//
			// Control methods
			
			//The client won the match.
			case ("gameWon"):
				findOpponent(client).send("");
			break;
			
			//The client lost the match.
			case ("gameLost"):
				findOpponent(client).send("");
			break;
			
			//The match can start.
			case ("gameReady"):
				findOpponent(client).send("");
			break;
		}
	}
  
  /** 
    * This method will determine the type of command that is received by the server admin.
    * 
    * @param msg The {@code String} message from the UI.
    * @param client The {@code ConnectionToclient} that this message came from.
    */ 
  private void commandMessage(String msg, ConnectionToClient client) throws IOException
  {
	//initialize local variables
	String message[]   = msg.split(" ");
	String instruction = "";
	String operand     = "";
	
	boolean hasWhiteSpace = false;
	
	//Find if a split has happened
	if ( message.length != 1)
		hasWhiteSpace = true;
	
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
		// MULTIPLAYER TETRIS COMMAND MESSAGES
		
		//informs the opposing player of their victory
		case "GameOver":
			findOpponent(client).send("Congratulations! You have won!");
			break;
		
		//*******************************************************************//
		// SERVER CONTROL METHODS
	
		// Causes the server to quit gracefully.
		case "quit": case "exit":
			quit();
		break;
		
		// Causes the server to stop listening for new clients.
		case "stopListening":
			stopListening();
			sendToAllClients("[INFO] Server has stopped listening for connections.");
		break;
		
		// Causes the server to start listening for new clients.
		case "listen":
			try 
			{
				listen(); //Start listening for connections
			}
			catch (Exception ex) 
			{
				serverOutput.display("[ERROR] Could not listen for clients!");
			}
			sendToAllClients("[INFO] Server now listening for connections.");
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
				serverOutput.display
					("[INFO] Server is open. Close server to set port.");
			else{
				setPort(Integer.parseInt(operand));
				serverOutput.display
					("[INFO] Port set to "+getPort());
			}
		break;
		
		//*******************************************************************//
		// Getter methods
		
		// Get the port
		case "getport": case "getPort":
			serverOutput.display
				("The port is: " + getPort());
		break;
		
		//*******************************************************************//
		// Helper methods
		
		// Returns a list of available commands and their usage.
		case "help": case "Help":
			serverOutput.display
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
		
		case "status":
			if(this.isListening())
				serverOutput.display("[INFO] Server open on port: " + getPort());
			else
				serverOutput.display("[INFO] Server closed. Port set to: " + getPort());
		break;
		
		// Ping!
		case "Ping": case "ping":
			client.send("Pong.");
		break;
		// Pong!
		case "Pong": case "pong":
			client.send("Ping.");
		break;
		
		//*******************************************************************//
		// Operation not found
		default:
			if (client == null)
			{
				serverOutput.display("> Command Not Found.");
			}else
				client.send("Invalid Command.");
		break;
	}
  }

  /** 
   * This method will send an update package to a given client's opponent.
   * 
   * @param client The {@code ConnectionToclient} that this message originated from.
   * @param update The {@code Updater} object to be sent to the given client's opponent.
   */ 
  private void performUpdate(Updater update, ConnectionToClient client){
		try
		{
			findOpponent(client).send(update);
		}
		catch (IOException ex)
		{
			serverOutput.display("[CRITICAL] Could not send updater to the opponent of "+client.getInfo("ID")+" at "+client.getInetAddress());
			ex.printStackTrace();
		}
  }
  
  //*************************************GAME-LOGIC*************************************//

  /**
   * This method attempts to match a newly connected client to an already connected client.
   * 
   * @param ConnectionToclient client is a newly connected client
   */

 protected void clientConnected(ConnectionToClient client)
{
  	// Creating connection for new client
  	ClientNode newClient = new ClientNode(client.getId());
  	
  	// Adding client to the client list
  	clientList.add(newClient);
  	
  	try
	{
  		sendToAllClients(clientList);
	}
	catch (IOException ex)
	{
		serverOutput.display("[FAILED] Could not send clientList to client "+client.getInfo("ID"));
		ex.printStackTrace();
	}
  	
  	// console output
  	serverOutput.display("[INFO] Client " + client.toString() + " connected.");
  	
  	// Setting default user information.
  	client.setInfo("ID", newClient.name);
  	
  	// Checking if it is possible to match them with an opponent
  	try
  	{
  		ConnectionToClient opponent = findOpponent(client);
  		
  		opponent.send("You have a new opponent!");
  		opponent.send("/ready");
  		client.send("You have a new opponent!");
  		client.send("/ready");
  		
  		serverOutput.display("[INFO] Client " + client.toString() + " has the opponent "+opponent.toString());
  	}
  	catch (NullPointerException ex) // catching 
    {
		serverOutput.display("[FAILED] No opponent for client "+client.getInfo("ID")+" at "+client.getInetAddress());
		try
  		{
  			client.send("[INFO] Sorry, no opponent found.");
  		}
    		catch (IOException e) // catching 
  		{
  			serverOutput.display("[FAILED] Send message to opponent of "+client.getInfo("ID")+" at "+client.getInetAddress());
  		}
    }
  	catch (IOException e) // catching 
	{
		serverOutput.display("[FAILED] Send message to opponent of "+client.getInfo("ID")+" at "+client.getInetAddress());
	}
}
  
  /**
   * This method removes a client from the list of connected clients and updates the status of the client's opponent
   * @param ConnectionToclient client is a client about to be disconnected
   */
  synchronized protected void clientDisconnected( ConnectionToClient client )
  {
	try
	{
		// Current client is an opponent of another client
		// will also disconnect the current client
		removeAsOpponent(client);
		
		// Notifying other clients
		serverOutput.display("[INFO] Client " + client.getInfo("ID") + " disconnected.");
		sendToAllClients("[INFO] Client " + client.getInfo("ID") + " left.");
		
	}
	catch (IOException e)
	{
		serverOutput.display("[ERROR] Could not send confirmation of removal for client "+client.getInfo("ID"));
	}
  }
  
  /**
   * Fired off when a client unexpectedly quits the server.
   */
  synchronized protected void clientException(ConnectionToClient client, Throwable exception)
  {
  	serverOutput.display("[ERROR] Connection with client "+client.getInfo("ID")+" at "+client.getInetAddress()+" terminated abruptly.");
  	
  	exception.printStackTrace();
  	
  	clientDisconnected(client);
  }
  
  /**
   * This method searches the threadlist to find and set a client's opponent.
   * 
   * @param client the {@code ConnectionToClient} we're trying to associate an opponent to.
   * @throws NullPointerException if the client could not be given an opponent.
   */
  private ConnectionToClient findOpponent( ConnectionToClient client) throws NullPointerException
  {
	Long opponentIndex = -1L; // Default value for a long
	
	for( ClientNode opponentNode : clientList )
	{// Iterate through all clients connected in the list
		
		// If listClient different from clientConnected and that client has himself as opponent
		if( opponentNode.getPlayerID() != client.getId() && opponentNode.getPlayerID() == opponentNode.getOpponentID())
		{
			// Sets the client's opponent's opponent as himself
			opponentIndex = opponentNode.setOpponentID(client.getId());
	    	break;
		}
	}
	
	Thread[] 			clientThreadList 	= getClientConnections(); //Obtain a list of connections
	ConnectionToClient 	opponent 			= null;
	
	// Iterate through the connectiontoclients array
	if(opponentIndex != -1L)
	{
		for(Thread clientThread : clientThreadList)
		{
			if(clientThread.getId() == opponentIndex)
			{
				opponent = (ConnectionToClient) clientThread;
				break;
			}
		}
	}
	else
		throw new NullPointerException("Client has no opponent.");
	
	return opponent;
  }
  
  /**
   * This method is only called when a client disconnects. It removes that client as other clients' opponent.
   * 
   * @param client the {@code ConnectionToClient} we're trying to remove as other clients' opponent.
   */
    private void removeAsOpponent( ConnectionToClient client)
    
    {
    	Long clientID = -1L;
    	
    	// Find the client in the list
    	for( ClientNode clientNode : clientList )
    	{
    		clientID = clientNode.getPlayerID();// Try to match ID's
    		
    		if( clientID == client.getId())
    		{// If the node in the list and the client leaving are matched
    			
    			for( ClientNode opponentNode : clientList )
    			{// Find the client's opponent
    	    		
    				if( opponentNode.getOpponentID() == clientID)
    	    		{
    	    			// Sets the client's opponent's opponent as himself
    					opponentNode.setOpponentID(opponentNode.getPlayerID());
    	    			
    	    			try
    	    			{
    	    				client.send("[INFO] You no longer have an opponent!");
    	    				findOpponent(client).send("[INFO] You no longer have an opponent!");
    	    			}
    	    			catch (IOException e)
    	    			{
    	    				serverOutput.display("[IDIOT] The client in removeAsOpponent has already been disconnected.");
    	    			}
    	    			
    	    	    	// Removing the user that disconnected from the clientList
    	    	    	clientList.remove(clientNode);
    	    	    	
    	    	    	break;
    	    		}
    	    	}
    		}
    	}
    }
    
  //*************************************CONTROL*************************************//
  
  
  /**
   * This method causes the server to quit gracefully.
   */
  public void quit()
  {
    try
    {
    	serverOutput.display("[CRITICAL] SERVER GOING OFFLINE");
    	close();
    }
    catch(IOException e)
    {
    	serverOutput.display("[CRITICAL] Could not close server gracefully. Terminating.");
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
		System.out.println("Could not close server. Aboritng.");
	}
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println("[INFO] Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println("[INFO] Server no longer listening for connections.");
  }
  
  /**
   * This method overrides the one in the superclass.
   * Called when the server is closed.
   */
  protected void serverClosed()
  {
    System.out.println("Server closed.");
  }
  
}