package Tetris2P;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.awt.Color;
import java.io.*;

import ocsf.server.*;
import Tetris2P.Board.Updater;
import Tetris2P.Tetris2P.OutputBox;

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
  
    /**
     * The default port to listen on.
     */
    public final static int DEFAULT_PORT = 1337;
    /**
     * It will be used to pair up player and opponent
     */
    private LinkedList<ClientNode> 	clientList = new LinkedList<ClientNode>();
    /**
     * The interface type variable.  It allows the implementation of 
     * the display method in the client.
     */
    private final OutputBox serverOutput;
    
    
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
    	serverOutput = (OutputBox) serverText;
   }

   //*************************************MESSAGE-HANDLERS*************************************//
  
  /**
   * This method handles any messages send from the client to the server.
   * If an updater message is detected, it is sent to the opponent of the client.
   * If a string message is detected, it can be a command message or chat to be sent to all clients.
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient (Object msg, ConnectionToClient client)
  {
	// Object received is an Updater.
	if (msg instanceof Updater)
	{	
		//sending game command messages to the commandMessage method to be implemented
		if(((Updater) msg).getCommandMessage() != null)
		{
			try
			{
				commandMessage(((Updater) msg).getCommandMessage(), (ConnectionToTetrisClient) client);
			}
			catch(Exception e) { serverOutput.display("Cannot send command message"); }
		}
		
		performUpdate((Updater) msg, (ConnectionToTetrisClient) client);
		return;
	}
	// Continue assuming a string message has been sent to the server
    try
    {
        //If the message was a command message, send the instruction for interpretation
    	if(((String) msg).startsWith("#"))
        	commandMessage(((String) msg).substring(1), (ConnectionToTetrisClient) client);
      	else{
      	    serverOutput.display(("["+ client.getInfo("ID") + "] " + msg));
        	this.sendToAllClients(msg);
      	}
    }
    catch(Exception e)
    {
      serverOutput.display("Could not send message to clients. Terminating server.");
      quit();
    }
  }

  /** 
    * This method will determine the type of command that is received by the server admin.
    * 
    * @param msg The {@code String} message from the UI.
    * @param client The {@code ConnectionToTetrisclient} that this message came from.
    */ 
  private void commandMessage(String msg , ConnectionToTetrisClient client) throws IOException
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
		
		//TETRIS COMMAND MESSAGES
		
		//informs the opposing player of their victory
		case "GameOver":
			client.opponent.send("Congratulations! You have won!");
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
			sendToAllClients("WARNING - Server has stopped listening for connections.");
		break;
		
		// Causes the server to start listening for new clients.
		case "listen":
			try 
			{
				listen(); //Start listening for connections
			}
			catch (Exception ex) 
			{
				System.out.println("ERROR - Could not listen for clients!");
			}
			sendToAllClients("WARNING - Server now listening for connections.");
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
					("Server running. Close server to set port.");
			else{
				setPort(Integer.parseInt(operand));
				serverOutput.display
					("Port set: " + getPort());
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
				serverOutput.display("Server listening on port: " + getPort(), Color.BLUE);
			else
				serverOutput.display("Server closed. Current port: " + getPort());
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
				System.out.println
					("> Command Not Found.");
			}else
				client.send("Invalid Command.");
		break;
	}
  }

  /** 
   * This method will send an update package to a given client's opponent.
   * 
   * @param client The {@code ConnectionToTetrisclient} that this message originated from.
   * @param update The {@code Updater} object to be sent to the given client's opponent.
   */ 
  private void performUpdate(Updater update, ConnectionToTetrisClient client){
		try
		{
			findOpponent(client);
			client.opponent.send(update);
		}
		catch (IOException ex)
		{
			System.out.println("Could not send update package to the opponent of "+client.getName()+" at "+client.getInetAddress());
			ex.printStackTrace();
		}
  }
  
  //*************************************GAME-LOGIC*************************************//
  
  /**
   * This method searches the threadlist to find and set a client's opponent.
   * 
   * @param client the {@code ConnectionToTetrisClient} we're trying to associate an opponent to.
   * @throws NullPointerException if the client could not be given an opponent.
   */
  private void findOpponent( ConnectionToTetrisClient client) throws NullPointerException
  {
	int indexID = 0;
	Long opponentID = 0L; // Default value for a long

	Thread[] clientThreadList = getClientConnections(); //Obtain a list of connections
	
	//obtaining the ClientNode list index of the client
	for(int i=0; i<clientList.size(); i++)
	{
		if(clientList.get(i).playerID == client.getId())
		{
			indexID = i;
			opponentID = clientList.get(indexID).opponentID;
			break;
		}
	}
	
	// Set the client's opponent
	if(opponentID != 0L)
	{
		for (int i=0; i<clientThreadList.length; i++)
		{
			if((clientThreadList[i]).getId() == opponentID)
			{
				client.opponent = (ConnectionToTetrisClient) clientThreadList[i];
				break;
			}
		}
	} else
		throw new NullPointerException();
  }
  
  /**
   * This method searches the threadlist to find and remove the opponent of a disconnecting client as an opponent.
   * 
   * @param client the {@code ConnectionToTetrisClient} we're trying to associate an opponent to.
   * @throws NullPointerException if the client could not be given an opponent.
   */
    private void removeOpponent( ConnectionToTetrisClient client)
    {
    	int clientIndex = 0;
    	int opponentIndex = 0;
    	Long opponentID = 0L; // Default value for a long
    	
    	for(int i=0; i<clientList.size(); i++)
    	{
    		if(clientList.get(i).playerID == client.getId())
    		{
    			//obtaining the ClientNode list index of the client to be disconnected 
    			if(clientIndex != 0)
    			{
    				clientIndex = i;
    				opponentID = clientList.get(clientIndex).opponentID;
    				
    				try
    				{
    					client.send("You no longer have an opponent!");
    					client.opponent.send("You no longer have an opponent!");
    				}
    				catch (IOException e){}
    			}
    			
    			break;
    		}
    		
    	}
    	
    	if(opponentID != 0L)
    	{
    		//obtaining the ID of the opponent and setting them to have no opponent
    		for(int i=0; i<clientList.size(); i++)
    		{
    			if(clientList.get(i).playerID == opponentID)
    			{
    				opponentIndex = clientList.indexOf(i);
    				clientList.get(opponentIndex).opponentID = null;
    				break;
    			}
    		}
    	}
    	
    	// Remove the client's opponent
    	client.opponent = null;
    	
    	// Removing the user that disconnected from the clientList
    	clientList.remove(clientIndex);
    }
  
  //*************************************CONTROL*************************************//
  
  
  /**
   * This method causes the server to quit gracefully.
   */
  public void quit()
  {
    try
    {
    	System.out.println("***SERVER GOING OFFLINE***");
    	close();
    }
    catch(IOException e)
    {
    	System.out.println("Could not quit server. Aborting.");
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
    System.out.println("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println("Server no longer listening for connections.");
  }
  
  /**
   * This method overrides the one in the superclass.
   * Called when the server is closed.
   */
  protected void serverClosed()
  {
    System.out.println("Server closed.");
  }
  

  /**
   * This method attempts to match a newly connected client to a client already connected so they can play Tetris
   * @param ConnectionToTetrisclient client is a newly connected client
   */

 protected void clientConnected(ConnectionToTetrisClient client)
  {	
	try
	{
		//adding client to the client list and checking if it is possible to match them with an opponent
		ClientNode connectedClient = new ClientNode(client.getId());
		clientList.add(connectedClient);
		findOpponent(client);
		client.send("You have a new opponent!");
		client.opponent.send("You have a new opponent!");
		
		if(clientList.size() == 1)
			client.send("Server running!");
		else
			serverOutput.display("Client " + client.toString() + " connected.");
	}	catch (NullPointerException ex)
	{
		serverOutput.display("Could not find an opponent for client "+client.getName()+" at "+client.getInetAddress());
		ex.printStackTrace();
	}
	catch (IOException e)
	{
		serverOutput.display("Could not send message to the opponent of "+client.getName()+" at "+client.getInetAddress());
		e.printStackTrace();
	}
  }
  
  /**
   * This method removes a client from the list of connected clients and updates the status of the client's opponent
   * @param ConnectionToTetrisclient client is a client about to be disconnected
   */
  
  synchronized protected void clientDisconnected( ConnectionToTetrisClient client)
  {
	try
	{
		//removing the current client is an opponent of another client
		//this method will also disconnect the current client
		removeOpponent(client);
		
	}
	catch (NullPointerException ex)
	{
		System.out.println("Could not remove opponent for client "+client.getName()+" at "+client.getInetAddress());
		ex.printStackTrace();
	}
	
	// Notifying other clients
	System.out.println("Client " + client.getInfo("ID") + " disconnected.");
	sendToAllClients("Client " + client.getInfo("ID") + " left.");
  }
  
  synchronized protected void clientException(ConnectionToTetrisClient client, Throwable exception)
  {
	
	  clientDisconnected(client);
  }
  
  //*************************************CLIENTNODE*************************************//
  
    /**
     * This will group clients into a player and their respective opponent ID
     * 
     * @author Dmitry Anglinov
     * @author Andréas K.LeF.
    */
    private class ClientNode
    {
    	/**
    	 * TODO
    	 */
    	private Long playerID;
    	/**
    	 * TODO
    	 */
    	private Long opponentID;
    	 
    	/**
    	 * 
    	 * 
    	 * @param playerID
    	 */
		public ClientNode(Long playerID)
    	{
    		this.playerID = playerID;
    	}
    }
}
