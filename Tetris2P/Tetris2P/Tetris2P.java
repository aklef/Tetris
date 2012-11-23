package Tetris2P;

import java.io.*;

import javax.sound.sampled.*;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JList;
import javax.swing.DefaultListModel;

import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import Tetris2P.Shape.Tetromino;
//import Tetris2P.Board.Updater;
import Tetris2P.Tetris.HotBar.ShapeArea;
import 	ocsf.client.*;


import java.util.Queue;
import java.util.LinkedList;

/**
 * TODO
 * 
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */
@SuppressWarnings("unused")
public class Tetris2P extends JFrame implements Runnable{
	
    /**
     * Instance of a tetris game mapped to the local player.
     */
    private final Tetris localGame;
    /**
     * Instance of a tetris game mapped to a specific remote player during multiplayer sessions.
     */
    private final Tetris opponentGame;
    
    /**
     * Static variable representing the background color of the board.
     */
    private static Color backgroundColor;
    /**
     * Variable that holds the GUI for the current connected users
     */
    private final PlayerList userList;
    /**
     * Holds the chat contents
     */
    private final JTextArea chatBox;
    /**
     * Input area for chat messages and commands.
     */
    private final JTextField inputBox;
    /**
     * Boolean variable that determines if the game will make sounds..
     */
    public boolean isMusicOn = true;
    /**
     * Music soundtrack for the game
     */
    private Clip tetrisSoudtrack; 
    
    /**
     * Constructor for the Teris multiplayer game.
     */
    public Tetris2P()
    {
        // Panel for the middle area
        JPanel middle = new JPanel();
        // GridLayout for middle area
        GridLayout myGrid = new GridLayout(1, 2, 30, 0);
        
        // Creating instances of emleents
        localGame	 = new Tetris();
        opponentGame = new Tetris();
        userList	 = new PlayerList();
        chatBox		 = new JTextArea();
        inputBox	 = new JTextField();
        
        // Default background color
        backgroundColor = new Color(13,13,13);
        
        // Setting the frame's background
        getContentPane().setBackground(backgroundColor);
        // Setting each component's background
        middle.setBackground(backgroundColor);
        localGame.setBackground(backgroundColor);
        opponentGame.setBackground(backgroundColor);
        userList.setBackground(backgroundColor);
        
        // Setting components' sizes.
        userList.setPreferredSize(new Dimension(100,200));

        // Setting components as not focusable
        opponentGame.setFocusable(false);
        userList.setFocusable(false);
        chatBox.setFocusable(false);
        
        
        // Setting the middle's layout manager
        middle.setLayout(myGrid);
        
        // Adding components to frame
        middle.add(localGame);
        middle.add(opponentGame);
        middle.add(userList);
        
        // Adding components to frame
        //add(toolBar, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(inputBox, BorderLayout.SOUTH);
        
        // ABSOLUTELY REQUIRED - DO NOT FUCK WITH THE NUMBERS
        getContentPane().setPreferredSize(new Dimension(600,473));
        // Necessary
        pack();
        
        // Not needed but works fine
        //revalidate();
        
        // mute opponent game
        opponentGame.setAudioPlayback(false);
        
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        run();
   }

    /**
     * Returns the background color of this panel.
     */
    @Override
    public Color getBackground() {
       return backgroundColor;
   }

    /**
     * Main method of the multiplayer Tetris game.
     */
    public static void main(String[] args) {
        new Tetris2P();
    }

    /**
     * TODO
     */
	public void run()
	{
        // Makes the window open in the center of the screen.
        setLocationRelativeTo(null);
        
        // Makes the frame steady
        //setResizable(false);
        
        // Shows the window
        setVisible(true);
        
	}

    /**
     * Toggles mute on the entire game when called
     */
	protected void toggleMuteGame(){
		if(localGame.isAudioPlaybackAllowed()){
			localGame.setAudioPlayback(false);
		}
		else
			localGame.setAudioPlayback(true);
	}

    //*************************************PLAYERLIST*************************************//

	/**
	 * TODO
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	protected class PlayerList extends JPanel
	{
		
	   /**
	    * This label will show the "Users Connected" title
	    **/
		private JLabel label;

	   /**
	    * GUI componenent that displays list of users
	    **/
	    private DefaultListModel userList;
	    
		/**
		 * LinkedList to hold the list of players
		**/
	    private LinkedList<String> users; 
	    
		/**
		 * constructor method for list.
		**/
	    @SuppressWarnings({ "rawtypes", "unchecked" })
		protected PlayerList()
	    {
	        setLayout(new BorderLayout());
	        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
	        
	        // TODO
	        users = new LinkedList<String>();
	        // TODO
	        userList = new DefaultListModel();
	        JList list = new JList(userList);
	        
	        // XXX Remove! Test addition to the list
	        for(int i=0; i<10; i++){
	        	addUserToList("Dingletronic" + i);
	        }
	        // Attach a ScrollPane to the list to make it scrollable
	        JScrollPane scrollPane = new JScrollPane();
	        
	        list.setPreferredSize(new Dimension(60, 50));
	        //list.setSize(100, 30);
	        scrollPane.getViewport().add(list);
	        
	        //scrollPane.setSize(new Dimension(100, 100));
	        
	        // Title of the playerlist
	        label = new JLabel(" Online Players :");
	        label.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
	        label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
	        
	        add(label, BorderLayout.NORTH);
	        add(scrollPane, BorderLayout.CENTER);
	    }

	    /**
	     * Adds the given player's name into the list
	     * 
	     * @param username {@code String} name of player.
	     */
	    @SuppressWarnings("unchecked")
		protected void addUserToList(String username)
	    {
	    	users.addLast(username); //add to end of list so the new user will be last in the queue to play
	    	userList.addElement(username); //adds a user to the list GUI
	    }

	    /**
	     * Removes the given player's name from the list
	     * 
	     * @param username {@code String} name of player.
	     */
	    protected void removeUserFromList(String username)
	    {
	    	users.remove(username); //removes the user from the list
	    	userList.removeElement(username); //adds a user to the list GUI
	    }
	    
		/**
		 * TODO
		 */
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
		}
	}
	
	//**************************************TOOLBAR*************************************//
	
	/**
	 * This is a nested class in Tetris2P.java that is a JPanel.
	 * It is displayed at the top of the main Tetris2P frame and allows 
	 * for user interaction with buttons.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	protected class ToolBar extends JPanel
	{
		/**
		 * TODO Icons.
		
		/**
		 * Constructor method.
		 */
		protected ToolBar()
		{
			/*
			this.setLayout(new BorderLayout());
			
			holdArea = new ShapeArea();
			add(holdArea, BorderLayout.WEST);
			
			previewNextPieceArea = new ShapeArea();
			
			previewNextPieceArea.setBackground(backgroundColor);
			previewNextPieceArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
			add(previewNextPieceArea, BorderLayout.EAST);
			*/
		}
		
		/**
		 * TODO
		 */
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
		}
	}
	
	//*************************************OUTPUTBOX*************************************//
	
	/**
	 * This is a nested class in Tetris2P.java that is a JPanel.
	 * It is displayed at the bottom of the main Tetris2P frame and allows for user input.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	public class OutputBox extends JPanel implements ChatIF
	{
		/**
		 * TODO Icons.
		
		/**
		 * Constructor method.
		 */
		public OutputBox(/*TetrisClient client*/)
		{
			/*
			this.setLayout(new BorderLayout());
			
			holdArea = new ShapeArea();
			add(holdArea, BorderLayout.WEST);
			
			previewNextPieceArea = new ShapeArea();
			
			previewNextPieceArea.setBackground(backgroundColor);
			previewNextPieceArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
			add(previewNextPieceArea, BorderLayout.EAST);
			*/
		}
		
		/**
		  * This method overrides the method in the ChatIF interface.  It
		 * displays a message onto the screen.
		 *
		 * @param message The string to be displayed.
		 */
		public void display(String message) 
		{
			System.out.println(message);
		}
		
		/**
		 * TODO
		 */
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
		}
	}
	
	//*************************************INPUTBOX*************************************//
	
	/**
	 * This is a nested class in Tetris2P.java that is a JPanel.
	 * It is displayed at the bottom of the main Tetris2P frame and allows for user input.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	private class InputBox extends JPanel
	{
		/**
		 * TODO Icons.
		
		/**
		 * Constructor method.
		 */
		public InputBox(/*TetrisClient client*/)
		{
			/*
			this.setLayout(new BorderLayout());
			
			holdArea = new ShapeArea();
			add(holdArea, BorderLayout.WEST);
			
			previewNextPieceArea = new ShapeArea();
			
			previewNextPieceArea.setBackground(backgroundColor);
			previewNextPieceArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
			add(previewNextPieceArea, BorderLayout.EAST);
			*/
		}

		/**
		 * TODO
		 */
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
		}
	}
	
	//*************************************TETRISCLIENT*************************************//
	
		/**
		 * This is a nested class in Tetris2P.java that is a JPanel.
		 * It is displayed at the bottom of the main Tetris2P frame and allows for user input.
		 * 
		 * @author Andréas K.LeF.
		 * @author Dmitry Anglinov
		 */
		protected class TetrisClient extends AbstractClient
		{
			/**
			 * The interface type variable.  It allows the implementation of 
			 * the display method in the client.
			 */
			ChatIF clientUI; 

			
			/**
			 * Constructs an instance of the Tetris client.
			 * Initially calls the Abstractclient constructor
			 *
			 * @param host The server to connect to.
			 * @param port The port number to connect on.
			 * @param clientUI The interface type variable.
			 */
			protected TetrisClient(String host, int port, ChatIF clientUI)
			{
				super(host, port); 
				this.clientUI = clientUI;
				try
				{
					openConnection();
				}
				catch (IOException e)
				{
					clientUI.display("Cannot open connection. Awaiting command.");
				}
			}

			//Instance methods ************************************************
			  
			/**
			 * This method handles all data that comes in from the server.
			 *
			 * @param msg The message from the server.
			 */
			public void handleMessageFromServer(Object msg) 
			{
				/*if ( msg instanceof Updater )
				{
					
				}*/
				//If the message was a command message, send the instruction for interpretation
				if(((String) msg).startsWith("/"))
					commandMessage(((String) msg).substring(1));
				else
					clientUI.display("> "+msg.toString());
			}

			/* ****Changed for E49**** DA, akleff
			 * This method handles all data coming from the UI            
			 * 
			 * @param message The message from the UI.    
			 */
			public void handleMessageFromClientUI(String message)
			{
    			try
        		{
        			// Idiot-proofing the input
        			if(message.equals(""))
        			return;
        			
        			//If the message was a command message, send the instruction for interpretation
        			if(message.startsWith("#") || message.startsWith("/"))
        			{
        				commandMessage(message.substring(1));
        			}
        			else
        			{
        			sendToServer(message);
        			}
        		}
        		catch(IOException e)
        		{
        			clientUI.display("Could not send message to server. Terminating client.");
        			quit();
        		}
			}
			
			/**
			 * This method will determine the type of command that was inputed by the user
			 * @param message The message from the UI.
			 */
			public void commandMessage( String msg )
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
				
				switch (instruction)
				{
					//*******************************************************************//
					// Authentication Methods
					
					//Log the client back in if the client is not connected
					 case ("connect"):
						if(this.isConnected())
							clientUI.display("Client already connected.");
						else{
							try{
							openConnection();
							}
							catch(IOException e) {
								clientUI.display("Could not connect.");
							}
						}
					break;
					
					//Log off client but does not terminate
					case ("disconnect"):
						try{
							closeConnection();
						}
						catch(IOException e) {
							clientUI.display("Could not disconnect.");
						}
					break;
					
					//*******************************************************************//
					// Client Control methods
					
					//Terminates the client
					case ("exit"): case ("quit"):
						quit();
					break;
					
					//*******************************************************************//
					// Setter methods
					
					//Sets the host if client not connected
					case ("sethost"): case ("setHost"):
						if(this.isConnected())
							clientUI.display("The client is connected. Please logoff to set host.");
						else{
							setHost(operand);
							clientUI.display("The host has been set to: " + getHost());
						}
					break;
					
					//Sets the port if client not connected
					case ("setport"): case ("setPort"):
						if(this.isConnected())
							clientUI.display("Client connected. Logoff to set port.");
						else{
							setPort(Integer.parseInt(operand));
							clientUI.display("Port set: " + getPort());
						}
					break;
					
					//*******************************************************************//
					// Getter methods
					
					//Get the host
					case ("gethost"):
						clientUI.display("The host is: " + getHost());
					break;
					
					//Get the port
					case ("getport"):
						clientUI.display("The port is: " + getPort());
					break;
					
					//*******************************************************************//
					// Operation not found
					default:
						System.out.println
							("> Command Not Found. Sending cmd to server.");
						try
						{
							sendToServer("/"+msg);
						}
						catch (IOException e) {}
					break;
				}
			}
			
			//*****Changed for E49**** DA, akleff
			//Method informs the user server has been terminated and closes the client
			protected void connectionClosed(){
				clientUI.display("Disconnected from server. Terminating client.");
			}
			
			/**
			 * Method informs the user server has been terminated and closes the client
			 */
			protected void connectionException(Exception exception)
			{
				clientUI.display("Server closed. Abnormal termination of connection.");
			}
			
			/**
			 * TODO
			 */
			protected void connectionEstablished()
			{
				clientUI.display("Connected to server.");
			}

			/**
			 * This method terminates the client.
			 */
			public void quit()
			{
				try
				{
					closeConnection();
				}
				catch(IOException e) {}
				System.exit(0);
			}
		}
}