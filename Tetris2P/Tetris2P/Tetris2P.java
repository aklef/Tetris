package Tetris2P;

import java.io.*;
import javax.sound.sampled.*;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import javax.swing.JList;
import javax.swing.DefaultListModel;

import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import Tetris2P.Shape.Tetromino;
import Tetris2P.Tetris.HotBar.ShapeArea;
import Tetris2P.Board.*;
import 	ocsf.client.*;


import java.util.Queue;
import java.util.LinkedList;

/**
 * This class represents one complete instance of a game of  multiplayer tetris played by a single user.
 * Contains two instances of a {@code Tetris} game. One is fo rthe current user and the other is for the opponent.
 * 
 * 
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */
@SuppressWarnings("unused")
public class Tetris2P extends JFrame implements Runnable
{
    /**
     * Instance of a tetris game mapped to the local player.
     */
    private final Tetris localGame;
    /**
     * Instance of a tetris game mapped to a specific remote player during multiplayer sessions.
     */
    private final Tetris opponentGame;
    /**
     * Extends {@code JPanel} and implements {@code ChatIF}. Contains only one
     * {@code JTextArea} to show the contents of chat.
     */
    private final OutputBox outputBox;
    /**
     * Extends {@code JPanel} and implements {@code ActionListener}. Contains only one
     * JInputField to let the player type in the chat and input commands.
     */
    private final InputBox inputBox;
    /**
     * Contains icons that perform useful functions such as muting sounds.
     */
    private final ToolBar iconBar;
    /**
     * Static variable representing the background color of the board.
     */
    private static Color backgroundColor;
    /**
     * Variable that holds the GUI for the current connected users
     */
    private final PlayerList userList;
    /**
     * Label that displays server information when it is active.
     */
    private final JLabel serverInfo;
    /**
     * The default port to connect on.
     */
    final private static int DEFAULT_PORT = 1337;
    /**
     * The default hostname to connect with.
     */
    final private static String DEFAULT_HOST = "localhost";
    /**
     * Local client for the multiplayer Tetris game.
     */
    private final TetrisClient tetrisClient;
    /**
     * Boolean variable that determines if the game will make sounds..
     */
    public boolean isMusicOn = true;
    /**
     * Music soundtrack for the game
     */
    private Clip tetrisSoudtrack; 


    //*************************************CONSTRUCTOR*************************************//
    
    /**
     * Constructor for the Teris multiplayer game. UI dispatcher.
     */
    public Tetris2P()
    {
        // Must create OutputBox before setting L&F to nimbus or bad things happen.
        outputBox	 = new OutputBox();
        iconBar		 = new ToolBar();

        // Sets default UIManager values
        UIManager.put("nimbusBase", Color.BLACK);
        
        try// Attemps to set the Nimbus L&F
        {
        	for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
        	{
        		if ("Nimbus".equals(info.getName()))
        		{
        			UIManager.setLookAndFeel(info.getClassName());
        			break;
                }
            }
        }
        catch (Exception e)
        {
        	// If Nimbus is not available, the GUI can be set to another look and feel.
        }
        
        // Creating instances of elements
        tetrisClient = new TetrisClient (DEFAULT_HOST, DEFAULT_PORT, outputBox);
        localGame	 = new Tetris(tetrisClient, outputBox, iconBar);
        opponentGame = new Tetris(outputBox);
        userList	 = new PlayerList();
        serverInfo	 = new JLabel("TESTING");
        inputBox	 = new InputBox();        
        createAndShowGUI();
   }
    /** 
     * Create the GUI and show it.  For thread safety, 
     * this method should be invoked from the 
     * event-dispatching thread. 
     */  
    private void createAndShowGUI()
    {
        
        // Panel for the middle area
        JPanel middle		 = new JPanel( new GridLayout(1, 3, 30, 0) );
        JPanel bottom		 = new JPanel( new FlowLayout(FlowLayout.LEFT));
        JPanel socialArea	 = new JPanel( new GridLayout(2, 1) );
        
        // Default background color
        backgroundColor = new Color(16,16,32);
        
        // Setting the frame's colors
        getContentPane().setBackground(backgroundColor);
        
        // Setting each component's colors
        middle.setBackground(backgroundColor);
        bottom.setBackground(backgroundColor);
        socialArea.setBackground(backgroundColor);
        
        localGame.setBackground(backgroundColor);
        opponentGame.setBackground(backgroundColor);
        
        userList.setBackground(backgroundColor);
        
        serverInfo.setBackground(backgroundColor);
        serverInfo.setForeground(Color.LIGHT_GRAY);
        
        outputBox.setBackground(backgroundColor.brighter());
        inputBox.setForeground(Color.WHITE);
        
        iconBar.setBackground(backgroundColor);
        
        // Creating spacing
        socialArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        serverInfo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        inputBox.setPreferredSize(new Dimension(450, 30));
        
        bottom.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
                
        // Setting components as not focusable
        opponentGame.setFocusable(false);
        userList.setFocusable(false);
        
        // Setting the Label's properties
        serverInfo.setVisible(true);
        
        // Adding components to frame
        middle.add(localGame);
        middle.add(opponentGame);
        
        //socialArea.add(serverInfo, BorderLayout.NORTH);
        socialArea.add(userList, BorderLayout.CENTER);
        socialArea.add(outputBox, BorderLayout.SOUTH);
        
        middle.add(socialArea);
        
        bottom.add(inputBox);
        bottom.add(serverInfo);
        
        // Adding components to frame
        add(iconBar, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        
        // ABSOLUTELY REQUIRED - DO NOT FUCK WITH THE NUMBERS
        getContentPane().setPreferredSize(new Dimension(685,573));
        // Necessary
        pack();
        
        // Mute opponent game
        opponentGame.setAudioPlayback(false);
        
        setTitle("Tetris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        run();
    }

    //*************************************THREAD-LOGIC*************************************//
    
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
        
        // Shows the window
        setVisible(true);
        
        // Makes the frame steady
        //setResizable(false);
        
	}

    //*************************************TOGGLES*************************************//
	
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
	
    //*************************************SETTER/GETTER*************************************//
	
	/**
	 * Allows the tetris client to be accessed from outside Tetris2P in order to send messages to server
	 * @return tetrisClient
	 */
	
	public TetrisClient getTetrisClient(){
		return tetrisClient;
	}
    
    /**
     * Returns the background color of this panel.
     */
    @Override
    public Color getBackground() {
       return backgroundColor;
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
		private final JLabel label;
	   /**
	    * GUI componenent that displays list of users
	    **/
	    @SuppressWarnings("rawtypes")
		private final DefaultListModel userList;
		/**
		 * LinkedList to hold the list of players
		**/
	    private final LinkedList<String> users; 
	    /**
		 * {@code JScrollPane} to show the list of players
		**/
		private final  JScrollPane scrollPane; 
		/**
		 * constructor method for list.
		**/
	    @SuppressWarnings({ "rawtypes", "unchecked" })
		protected PlayerList()
	    {
	        setLayout(new BorderLayout());
	        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
	        
	        // TODO
	        users 	 = new LinkedList<String>();
	        userList = new DefaultListModel();
	        JList list 	 = new JList(userList);
	        
	        // XXX Remove! Test addition to the list
	        for(int i=0; i<10; i++){
	        	addUserToList("Dingletronic" + i);
	        }
	        // Attach a ScrollPane to the list to make it scrollable
	        scrollPane = new JScrollPane();
	        
	        list.setOpaque(true);
	        list.setCellRenderer(new CustomCellRenderer());
	        list.setForeground(Color.WHITE);
	        
	        // Adding the list to the scrollable area
	        scrollPane.getViewport().add(list);
	        
	        // Title of the playerlist
	        label = new JLabel(" Online Players :");
	        label.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
	        label.setForeground(Color.WHITE);
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
	    
	    private class CustomCellRenderer extends DefaultListCellRenderer
	    {
	        @SuppressWarnings("rawtypes")
			public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
	        {
	            Component c = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
	            
	            c.setBackground( backgroundColor.brighter() );
	            return c;  
	        }  
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
	protected class ToolBar extends JPanel implements ActionListener
	{
		/**
		 * The soundButton icon that can displays wether the game is muted and is able to toggle mute
		 */
		private final JButton soundButton;
		/**
		 * The play button icon that is able to toggle between pause and play states 
		 */
		private final JButton playPauseButton;
		/**
		 * Restart button icon that toggles restart on the game
		 */
		private final JButton restartButton;
		/**
		 * 
		 */
		private final ImageIcon soundOn;
		/**
		 * 
		 */
		private final ImageIcon soundOff;
		/**
		 * 
		 */
		private final ImageIcon play;
		/**
		 * 
		 */
		private final ImageIcon pause;
		/**
		 * 
		 */
		private final ImageIcon restart;
		
		/**
		 * Constructor method to create toolbar of icons
		 */
		protected ToolBar()
		{
	        setLayout( new FlowLayout(FlowLayout.CENTER));
			
			JPanel left = 	new JPanel( new FlowLayout(FlowLayout.LEFT));
	        JPanel right = 	new JPanel( new FlowLayout(FlowLayout.RIGHT));
	        
	        //icons declarations
	        soundOn = 	new ImageIcon(getClass().getResource("/Icons/soundOn.png"));
	        soundOff = 	new ImageIcon(getClass().getResource("/Icons/soundoff.png"));
	        
	        play = 		new ImageIcon(getClass().getResource("/Icons/play.png"));
	        pause = 	new ImageIcon(getClass().getResource("/Icons/pause.png"));
	        
	        restart = 	new ImageIcon(getClass().getResource("/Icons/restart.png"));
	        
	        // Defaults to the sound being on
	        soundButton 	= new JButton("sound Off", soundOn );
	        playPauseButton = new JButton("pause", play );
	        restartButton 	= new JButton("restart", restart );
	        
	        // Adding tooltips
	        soundButton.setToolTipText("sound Off");
	        playPauseButton.setToolTipText("pause");
	        restartButton.setToolTipText("restart");
	        
	        // Setting backround colors
	        setBackground(backgroundColor);
	        left.setBackground(backgroundColor);
	        right.setBackground(backgroundColor);
	        
	        soundButton.setOpaque(true);
	        playPauseButton.setOpaque(true);
	        restartButton.setOpaque(true);
	        
	        soundButton.setBackground(new Color(16,16,32).brighter().brighter());
	        playPauseButton.setBackground(new Color(16,16,32).brighter().brighter());
	        restartButton.setBackground(new Color(16,16,32).brighter().brighter());
	        
	        // Setting foreground colors
	        soundButton.setForeground(Color.LIGHT_GRAY);
	        playPauseButton.setForeground(Color.LIGHT_GRAY);
	        restartButton.setForeground(Color.LIGHT_GRAY);
	        
	        // Adding the action listeners to the buttons
	        soundButton.addActionListener(this);
	        playPauseButton.addActionListener(this);
	        restartButton.addActionListener(this);
	        
	        //adding the buttons to the JPanel and displaying to the UI
	        left.add(playPauseButton);
	        left.add(restartButton);
	        left.add(soundButton);
	        
	        add(left);
	        add(right);
	        
	        setFocusable(false);
	        left.setFocusable(false);
	        right.setFocusable(false);
	        
	        setVisible(true);
		}

		/**
		 * 
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton buttonPressed = (JButton) e.getSource();
			
			String id = buttonPressed.getText();
			
			
			switch (id)
			{
				case "sound On":
					soundButton.setIcon(soundOff);
					soundButton.setText("sound Off");
					soundButton.setToolTipText("sound Off");
					toggleMuteGame();
					break;
					
				case "sound Off":
					soundButton.setIcon(soundOn);
					soundButton.setText("sound On");
					soundButton.setToolTipText("sound On");
					toggleMuteGame();
					break;
					
				case "play":
					playPauseButton.setIcon(pause);
					localGame.getBoard().pause();
					playPauseButton.setText("pause");
					playPauseButton.setToolTipText("pause");
					break;
					
				case "pause":
					playPauseButton.setIcon(pause);
					localGame.getBoard().pause();
					playPauseButton.setText("play");
					playPauseButton.setToolTipText("play");
					break;
					
				case "restart":
					localGame.getBoard().restart();
					break;
					
				case "email":
					break;
					
				case "github":
					break;
			}
		}
		
		/**
	     * Gets the play/pause button so the game can be paused/activate from the Board
	     * 
	     */
	    public JButton getPlayPauseButton(){
	    	return playPauseButton;
	    }
	    
	    /**
	     * Gets the sound button so the sound can be toggled form the Board
	     * 
	     */
	    public JButton getSoundButton(){
	    	return soundButton;
	    }
		
	    /**
	     * Gets the restart button so the icon can 
	     * 
	     */
	    public JButton getRestartButton(){
	    	return restartButton;
	    }
	}
	
	//*************************************OUTPUTBOX*************************************//
	
	/**
	 * This is a nested class in Tetris2P.java that holds the chat content.
	 * It is displayed at the right of the main Tetris2P frame.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	protected class OutputBox extends JTextPane implements ChatIF
	{
		/**
		 * Default font
		 */
		private final Font defaultFont;
		
		/**
		 * Constructor method.
		 */
		private OutputBox()
		{
			super();
			
			defaultFont = getFont();
			
			setBorder(BorderFactory.createMatteBorder(0, 0, 0, 18, new Color (16,16,32)));
			
			setFocusable(false);
		}
		
		/**
		 * This method overrides the method in the ChatIF interface.
		 * It displays a message on the chatBox in the default color.
		 *
		 * @param message The string to be displayed.
		 */
		public void display(String message) 
		{
			display(message, Color.WHITE);
		}
		
		/**
		 * This method overrides the method in the ChatIF interface.
		 * It displays a message on the chatBox in the given {@code color}.
		 *
		 * @param message The string to be displayed.
		 * @param message The color in which the string will be displayed.
		 */
		public void display(String message, Color color, Font font)
		{
			// uses StyleContext
			StyleContext sc = StyleContext.getDefaultStyleContext();
			
			AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
			
			int len = getDocument().getLength(); // same value as getText().length();
			setCaretPosition(len); // place caret at the end (with no selection)
			
			setFont(font);
			setCharacterAttributes(aset, false);
			
			replaceSelection("\n"+message); // there is no selection, so inserts at caret
		}
		
		/**
		 * This method overrides the method in the ChatIF interface.
		 * It displays a message on the chatBox in the default color.
		 *
		 * @param message The string to be displayed.
		 */
		public void display(String message, Color color)
		{
			display(message, color, defaultFont);
		}
	}
	
	//*************************************INPUTBOX*************************************//
	
	/**
	 * This is a nested class in Tetris2P.java that is a JPanel.
	 * It is displayed at the bottom of the main Tetris2P frame. 
	 * Allows for user inputsuch as chat messages and commands.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	private class InputBox extends JTextField
	{
		/**
		 * Constructor method.
		 */
		private InputBox()
		{
			super(); 
			setBackground(backgroundColor);
			
			KeyAdapter keyListener = new KeyAdapter() {
				public void keyPressed(KeyEvent e)
				{
					// Command statement switch
					switch (e.getKeyCode()) {
						case KeyEvent.VK_ENTER:
							
							String msg = getText();
							
							tetrisClient.handleMessageFromClientUI(msg);
							
							setText(null);
							repaint();
							break;
					}
				}
			};
			
			addKeyListener(keyListener);
			
			setFocusable(true);
			
		}
		
		//***************************GRAPHICS***************************
		
		/**
		 * The paint method.
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
		OutputBox clientUI; 
		
	    /**
	     * Local server for the multiplayer Tetris game.
	     */
	    private TetrisServer tetrisServer;
	    
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
			this.clientUI = (OutputBox) clientUI;
		}

		//Instance methods ************************************************
		  
		/**
		 * This method handles all data that comes in from the server.
		 *
		 * @param msg The message from the server.
		 */
		public void handleMessageFromServer(Object msg) 
		{
			if ( msg instanceof Updater)
			{ //the updater was sent from the server to update the board of the opponent
				opponentGame.getBoard().updateBoard((Updater)msg);
			}
			else
			{
			//If the message was a command message, send the instruction for interpretation
			if(((String) msg).startsWith("/"))
				commandMessage(((String) msg).substring(1));
			}
		}

		/** This method handles all data coming from the UI
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
    			if(message.startsWith("/"))
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
			
			switch (instruction.toLowerCase())
			{
				//*******************************************************************//
				// Authentication Methods
				
				//Log the client back in if the client is not connected
				 case ("connect"):
					 connect();
				break;
				
				//Log off client but does not terminate
				case ("disconnect"):
					disconnect();
				break;
				
				//*******************************************************************//
				// Control methods
				
				//Terminates the client
				case ("start"):
					start();
				break;
				//Terminates the client
				case ("exit"): case ("quit"):
					quit();
				break;
				
				//The client won the match.
				case ("GameOver"):
					matchWon();
				break;
				//*******************************************************************//
				// Setter methods
				
				//Sets the host if client not connected
				case ("sethost"):
					if(this.isConnected())
						disconnect();
					setHost(operand);
					clientUI.display("The host has been set to: " + getHost(), Color.RED);
					connect();
				break;
				
				//Sets the port if client not connected
				case ("setport"):
					if(this.isConnected())
						disconnect();
					setPort(Integer.parseInt(operand));
					clientUI.display("Port set: " + getPort());
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
						sendToServer("#"+msg);
					}
					catch (IOException e)
					{
						System.out.println
							("Could not send command to server.");
					}
				break;
			}
		}
		
		/**
		 * Method informs the user server has been terminated and closes the client
		 */
		protected void connectionClosed(){
			clientUI.display("Disconnected from server. Terminating client.", Color.RED);
		}
		
		/**
		 * Method informs the user server has been terminated and closes the client
		 */
		protected void connectionException(Exception exception)
		{
			clientUI.display("Server closed. Abnormal termination of connection.", Color.ORANGE);
		}
		
		/**
		 * TODO
		 */
		protected void connectionEstablished()
		{
			clientUI.display("Connected to server.");
		}
		
		/**
		 * Returns the chat interface of the TetrisClient
		 * @return clientUI
		 */
		public ChatIF getClientUI(){
			return clientUI;
		}

		/**
		 * This method starts the server.
		 */
		public void start()
		{
			tetrisServer = new TetrisServer(getPort(), clientUI);
			
			try 
			{
				tetrisServer.listen(); //Start listening for connections
			}
			catch (Exception ex) 
			{
				clientUI.display("ERROR - Could not listen for clients!", Color.YELLOW);
				System.exit(0);
			}
			// connects with default parameters.
			connect();
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
		
		/**
		 * Resets this player's opponent ghost board and display a win message.
		 */
		private void matchWon()
		{
			clientUI.display("You won!", Color.BLUE, new Font("Malgun Gothic", Font.BOLD, 16));
			localGame.getBoard().restart();
			opponentGame.getBoard().restart();
		}
		
		/**
		 * This method connects the client to a server with default parameters.
		 */
		public void connect()
		{
			if(this.isConnected())
				clientUI.display("Client already connected.", Color.LIGHT_GRAY);
			else
			{
				try
				{
				openConnection();
				}
				catch(IOException e)
				{
					clientUI.display("Cannot open connection. Awaiting command.", Color.ORANGE);
				}
			}
		}
		
		/**
		 * This method disconnects the client from the server.
		 */
		public void disconnect()
		{
			try{
				closeConnection();
			}
			catch(IOException e) {
				clientUI.display("Could not disconnect.", Color.LIGHT_GRAY);
			}
		}
	}
}
