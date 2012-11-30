package Tetris2P;

import java.io.*;
import java.net.BindException;

import javax.sound.sampled.*;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.SwingUtilities;

import Tetris2P.Shape.Tetromino;
import Tetris2P.Tetris.HotBar.ShapeArea;
import Tetris2P.ClientNode;
import Tetris2P.Board.*;
import ocsf.client.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * This class represents one complete instance of a game of  multiplayer tetris played by a single user.
 * Contains two instances of a {@code Tetris} game. One is fo rthe current user and the other is for the opponent.
 * 
 * 
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */
@SuppressWarnings("unused")
public class Tetris2P extends JFrame implements Runnable, Serializable
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
    private final ToolBar toolBar;
    /**
     * Static variable representing the background color of the board.
     */
    private final static Color backgroundColor = new Color(16,16,32);
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
    private boolean isMusicOn = false;
    /**
     * Boolean variable that determines if the game is being played on a server.
     */
    private boolean isMultiplayerOn = false;
    /**
     * If the client is ready in multiplayer.
     */
    private boolean isMultiplayerReady = false;
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
        toolBar		 = new ToolBar();
        
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
        userList	 = new PlayerList();
        tetrisClient = new TetrisClient (DEFAULT_HOST, DEFAULT_PORT, outputBox, userList);
        
        localGame	 = new Tetris(outputBox, toolBar, tetrisClient);
        
        opponentGame = new Tetris();
        serverInfo	 = new JLabel("Single Player Mode");
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
        List<Image> icons = new ArrayList<Image>();
 //       icons.add((new ImageIcon(getClass().getResource("/Media/Love_for_Tetris256x256.jpg"))).getImage());
        setIconImages(icons);
        
        // Panel for the middle area
        JPanel middle		 = new JPanel( new GridLayout(1, 3, 30, 0) );
        JPanel bottom		 = new JPanel( new FlowLayout(FlowLayout.LEFT));
        JPanel socialArea	 = new JPanel( new GridLayout(2, 1) );
        
        JScrollPane scroll = new JScrollPane (outputBox);
        		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Setting the frame's colors
        getContentPane().setBackground(backgroundColor);
        
        // Setting each component's colors
        middle.setBackground(backgroundColor);
        bottom.setBackground(backgroundColor);
        socialArea.setBackground(backgroundColor);
        
        localGame.setBackground(backgroundColor);
        opponentGame.setBackground(backgroundColor);
        
        userList.setBackground(backgroundColor);
        
        outputBox.setBackground(backgroundColor.brighter());
        inputBox.setForeground(Color.WHITE);
        
        serverInfo.setBackground(backgroundColor);
        serverInfo.setForeground(Color.LIGHT_GRAY);
        
        toolBar.setBackground(backgroundColor);
        
        // Creating spacing and Borders
        outputBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        socialArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.DARK_GRAY));
        
        serverInfo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        bottom.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
        
        // Setting components' focus
        // Top
        toolBar.setFocusable(false);
        
        // Middle
        middle.setFocusable(true);
        localGame.setFocusable(true);
        opponentGame.setFocusable(false);
        
        middle.addKeyListener(localGame.getBoard().getKeyListeners()[0]);
        middle.addMouseListener(localGame.getBoard());
        
        //socialArea.setFocusable(false); does nothing
        //outputBox.setEditable(false); DO NOT SET FALSE
        
        outputBox.setFocusable(false); // important do not change
        userList.setFocusable(false);
        
        // Bottom
        //bottom.setFocusable(true); // important do not change
        inputBox.setFocusable(true); // important do not change
        
        // Setting the Label's properties
        serverInfo.setVisible(true);
        
        // Adding components to frame
        middle.add(localGame);
        middle.add(opponentGame);
        
        //socialArea.add(serverInfo, BorderLayout.NORTH);
        socialArea.add(userList, BorderLayout.CENTER);
        socialArea.add(scroll, BorderLayout.SOUTH);
        
        middle.add(socialArea);
        
        bottom.add(inputBox);
        bottom.add(serverInfo);
        
        // Adding components to frame
        add(toolBar, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        
        // ABSOLUTELY REQUIRED - DO NOT FUCK WITH THE NUMBERS
        getContentPane().setPreferredSize(new Dimension(668,573));
        // Necessary
        pack();
        
        revalidate();
        
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
    	Tetris2P Game = new Tetris2P();
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
        
        // Makes the frame steady DO NOT USE
        //setResizable(false);
        
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        
        while (true) {
            repaint();
            try {
               Thread.sleep(40);
            } catch (InterruptedException ie) {
            }
        }
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

    /**
     * Returns local Tetris game
     * 
     */
    public Tetris getLocalGame(){
    	return localGame;
    }
    
    /**
     * Returns the opponent Tetris game
     * 
     */
    public Tetris getOpponentGame(){
    	return opponentGame;
    }
    /**
     * 
     * @return instance of Toolar
     */
    
    public ToolBar getToolBar(){
    	return toolBar;
    }
    
    //*************************************PLAYERLIST*************************************//

	/**
	 * TODO
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	protected class PlayerList extends JPanel implements Serializable
	{
		
	   /**
	    * This label will show the "Users Connected" title
	    **/
		private final JLabel playerListTitle;
	   /**
	    * GUI componenent that displays list of users
	    **/
		private DefaultListModel<String> userList;
		/**
		 * ArrayList to hold the list of players
		**/
	    private ArrayList<String> users;
	    /**
		 * {@code JScrollPane} to show the list of players
		**/
		private JScrollPane scrollPane; 
		private JList<String> list; 
		/**
		 * Constructor method for list.
		**/
	    @SuppressWarnings({ "rawtypes", "unchecked" })
		protected PlayerList()
	    {
	        setLayout(new BorderLayout());
	        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
	        
	        // TODO
	        users 	 = new ArrayList<String>();
	        userList = new DefaultListModel<String>();
	        list 	 = new JList(userList);
	        
	        // Attach a ScrollPane to the list to make it scrollable
	        scrollPane = new JScrollPane();
	        
	        list.setOpaque(true);
	        list.setCellRenderer(new CustomCellRenderer());
	        list.setForeground(Color.WHITE);
	        list.setBackground(backgroundColor);
	        
	        // Adding the list to the scrollable area
	        scrollPane.getViewport().add(list);
	        
	        // Title of the playerlist
	        playerListTitle = new JLabel(" Online Players :");
	        playerListTitle.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
	        playerListTitle.setForeground(Color.WHITE);
	        playerListTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
	        
	        add(playerListTitle, BorderLayout.NORTH);
	        add(scrollPane, BorderLayout.CENTER);
	    }

	    /**
	     * Adds the given player's name into the list
	     * 
	     * @param newUser {@code String} name of player.
	     */
		private void addUserToList(ClientNode newUser)
	    {
	    	users.add(newUser.getName()); //add to end of list so the new user will be last in the queue to play
	    	userList.addElement(newUser.getName()); //adds a user to the list GUI
	    }
	    
	    /**
	     *  Updates the playerlist.
	     * 
	     * @param obj a {@code ArrayList} of {@code ClientNode} of players.
	     */
		private void updatePlayerList( String[] playerList)
	    {
			ArrayList<String> players = new ArrayList<String>();
			
			for(String name : playerList)
			{
				players.add(name);
			}
			
			// Take one user each time from local list
			// Remove missing players if found
			for ( String node :  players)
			{
				// If local player not in new list remove them.
				if (!users.contains(node))
				{
					users.add(node);
					userList.addElement(node);
				}
			}
			// Take one user each time from remote list
			// Add new players if found 
			for ( String node :  players)
			{
				if (!users.contains(node))
				{
					users.remove(node);
					userList.removeElement(node);
				}
			}
			
	    }

	    /**
	     * Removes the given player's name from the list
	     * 
	     */
	    protected void clearList()
	    {
	    	users 	 = new ArrayList<String>();
	        userList = new DefaultListModel<String>();
	        list.setListData(new String[0]);
	        
	        repaint();
	    }
	    
	    /**
	     * TODO
	     * 
	     * @author Andréas K.LeF.
	     */
	    private class CustomCellRenderer extends DefaultListCellRenderer implements Serializable
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
	public class ToolBar extends JPanel implements ActionListener, Serializable
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
	     * Label that displays game status information.
	     */
	    private final JLabel gameStatus;
	    /**
	     * Label that displays te number of lines removed druing this game.
	     */
	    private final JLabel linesRemoved;
		
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
	        
	        // Labels
	        gameStatus	 = new JLabel("");
	        linesRemoved = new JLabel("Lines Removed: 0");
	        
	        linesRemoved.setForeground(Color.green);
            
	        //gameStatus.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
	        //linesRemoved.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));
	        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 18));
	        
	        // Defaults to the sound being on
	        soundButton 	= new JButton("Sound Off", soundOff );
	        playPauseButton = new JButton("Play", pause );
	        restartButton 	= new JButton("Restart", restart );
	        
	        // Adding tooltips
	        soundButton.setToolTipText("Sound Off");
	        playPauseButton.setToolTipText("Play");
	        restartButton.setToolTipText("Restart");
	        
	        // Setting background colors
	        setBackground(backgroundColor);
	        left.setBackground(backgroundColor);
	        right.setBackground(backgroundColor);
	        
	        //setting button colors
	        soundButton.setBackground(backgroundColor.brighter().brighter());
	        playPauseButton.setBackground(backgroundColor.brighter().brighter());
	        restartButton.setBackground(backgroundColor.brighter().brighter());
	        
	        // Correcting button look and feel
	        soundButton.setOpaque(true);
	        playPauseButton.setOpaque(true);
	        restartButton.setOpaque(true);
	        
	        // Setting foreground colors
	        soundButton.setForeground(Color.LIGHT_GRAY);
	        playPauseButton.setForeground(Color.LIGHT_GRAY);
	        restartButton.setForeground(Color.LIGHT_GRAY);
	        
	        // Adding the action listeners to the buttons
	        soundButton.addActionListener(this);
	        playPauseButton.addActionListener(this);
	        restartButton.addActionListener(this);
	        
	        //adding the buttons to the JPanel and displaying to the UI
	        left.add(linesRemoved);
	        left.add(gameStatus);
	        
	        right.add(playPauseButton);
	        right.add(restartButton);
	        right.add(soundButton);
	        
	        add(left);
	        add(right);
	        
	        // Disabling button auto-focus
	        linesRemoved.setFocusable(false);
	        playPauseButton.setFocusable(false);
	        soundButton.setFocusable(false);
	        restartButton.setFocusable(false);
	        gameStatus.setFocusable(false);
		}

		/**
		 * Handles events fired off by buttons presses.
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton buttonPressed = (JButton) e.getSource();
			
			String id = buttonPressed.getText();
			
			switch (id)
			{
				case "Sound Off":
					soundButton.setIcon(soundOff);
					soundButton.setText("Sound On");
					soundButton.setToolTipText("Sound On");
					toggleMuteGame();
					break;
					
				case "Sound On":
					soundButton.setIcon(soundOn);
					soundButton.setText("Sound Off");
					soundButton.setToolTipText("Sound Off");
					toggleMuteGame();
					break;
					
				case "Play":
					playPauseButton.setIcon(pause);
					localGame.getBoard().pause();
					playPauseButton.setText("Pause");
					playPauseButton.setToolTipText("Pause");
					break;
					
				case "Pause":
					playPauseButton.setIcon(pause);
					localGame.getBoard().pause();
					playPauseButton.setText("Play");
					playPauseButton.setToolTipText("Play");
					break;
					
				case "Restart":
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
	     * Gets the restart button.
	     * 
	     */
	    public JButton getRestartButton(){
	    	return restartButton;
	    }
	    /**
	     * @return the game status label.
	     */
	    public JLabel getStatusLabel(){
	    	return gameStatus;
	    }
		
	    /**
	     * @return the Lines removed label.
	     */
	    public JLabel getLinesRemLabel(){
	    	return linesRemoved;
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
	protected class OutputBox extends JTextPane implements Serializable, ChatIF
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
			
			setBorder(BorderFactory.createMatteBorder(0, 0, 0, 18, backgroundColor));
			
			//setFocusable(false);
		}
		
		/**
		 * This method overrides the method in the ChatIF interface.
		 * It displays a message on the chatBox in the default color.
		 *
		 * @param message The string to be displayed.
		 */
		public void display(String message) 
		{
			if (message.equals(""))
				return;
			
			display(message, Color.WHITE);
		}
		
		/**
		 * This method overrides the method in the ChatIF interface.
		 * It displays a message on the chatBox in the default color.
		 *
		 * @param message The string to be displayed.
		 */
		public void display(String message, Color color) throws NullPointerException
		{
			if (message.equals(""))
				return;
			
			display(message, color, defaultFont);
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
			if (message.equals(""))
				return;
			
			// uses StyleContext
			StyleContext sc = StyleContext.getDefaultStyleContext();
			
			AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
			
			int len = getDocument().getLength(); // same value as getText().length();
			setCaretPosition(len); // place caret at the end (with no selection)
			
			setFont(font);
			setCharacterAttributes(aset, false);
			if (message.startsWith("&&"))
			{
				message = message.replaceFirst("&&", "");
				replaceSelection(message); // there is no selection, so inserts at caret
			}
			else
				replaceSelection("\n"+message); // there is no selection, so inserts on new line
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
	private class InputBox extends JTextField implements Serializable
	{
		/**
		 * Constructor method.
		 */
		private InputBox()
		{
			super(); 
			setBackground(backgroundColor);
			setPreferredSize(new Dimension(450, 30));
			
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
		}
		
		//***************************GRAPHICS***************************
		
		/**
		 * The paint method.
		 */
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			setPreferredSize(new Dimension((localGame.getWidth()*2+100)-serverInfo.getWidth(), 30));
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
	public class TetrisClient extends AbstractClient implements Serializable
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
	     * Variables that contains instance of users in the game
	     * 
	     */
	    private PlayerList playerList;
	    
	    //****************************CONSTRUCTOR****************************//
	    
		/**
		 * Constructs an instance of the Tetris client.
		 * Initially calls the Abstractclient constructor
		 *
		 * @param host The server to connect to.
		 * @param port The port number to connect on.
		 * @param clientUI The interface type variable.
		 */
		public TetrisClient(String host, int port, ChatIF clientUI, PlayerList userList)
		{
			super(host, port); 
			this.clientUI = (OutputBox) clientUI;
			playerList = userList;
		}

		//****************************MESSAGES****************************//
		  
		/**
		 * This method handles all data that comes in from the server.
		 *
		 * @param obj The message from the server.
		 */
		public void handleMessageFromServer(Object obj) 
		{
			if ( obj instanceof Updater)
			{ 
				Updater update = (Updater) obj;
				
				String command = update.getCommandMessage();
				
				if ( command != "")
					try
					{
						serverCommandMessage(command);
					}
					catch (IOException e)
					{
						clientUI.display("[ERROR] Could not parse server command message.", Color.LIGHT_GRAY);
						e.printStackTrace();
					}
				else // Updater should update the opponent's board
					opponentGame.getBoard().updateBoard(update);
			}
			else if ( obj instanceof String[])
			{ //the list of clients was sent from the server to update it locally
				playerList.updatePlayerList( (String[]) obj );
			}
			else
			{
				String message = (String) obj;
    			//If the message was a command message, send the instruction for interpretation
    			if (message.startsWith("/"))
    			{
    				try
					{
						commandMessage(message.substring(1));
					}
					catch (IOException e) { clientUI.display("[ERROR] Could not parse command message.", Color.LIGHT_GRAY); }
    			}
    			else if (message.startsWith("[INFO]"))
    			{
    				clientUI.display(message, Color.LIGHT_GRAY);
    			}
    			else if (message.startsWith("[SERVER MSG]"))
    			{
    				clientUI.display(message, Color.CYAN);
    			}
    			else 
    			{
    				clientUI.display(message, Color.WHITE);
    			}
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
    			clientUI.display("Could not send message \"", Color.LIGHT_GRAY);
    			clientUI.display("&&"+message);
    			clientUI.display("&&\" to server.", Color.LIGHT_GRAY);
    			
    			if (tetrisServer == null)
    				clientUI.display("[WARNING] No server. Chat disabled.", Color.YELLOW);
    		}
		}
		
		/**
		 * This method will determine the type of command that was sent by the server
		 * @param message The message from the server.
		 */
		public void serverCommandMessage( String msg ) throws IOException
		{
			if (msg.equals(""))
				return;
			
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
					matchOver(true, operand);
				break;
				
				//The client lost the match.
				case ("gameLost"):
					matchOver(false, operand);
				break;
				
				//The match can start.
				case ("ready"): case ("reafy"):
    				// Other person has told us they are ready.
    				if (isMultiplayerOn && isMultiplayerReady)
    				{
    					toolBar.playPauseButton.doClick();
    					clientUI.display("[INFO] Match started.", Color.CYAN);
    				}
    				else if (isMultiplayerOn && !isMultiplayerReady)
    				{
    					isMultiplayerReady = true;
    					localGame.getBoard().setMultiplayerEnabled(isMultiplayerReady);
    					
    					clientUI.display("[INFO] Opponent ready! Do ", Color.CYAN);
						clientUI.display("&&/ready", Color.GREEN);
						clientUI.display("&& to start", Color.CYAN);
						toolBar.getStatusLabel().setForeground(Color.ORANGE);
						toolBar.getStatusLabel().setText("Do /ready to start!");
						repaint();
    				}
				break;
			}
		}
		
		/**
		 * This method will determine the type of command that was inputed by the user
		 * @param message The message from the UI.
		 * @throws IOException 
		 */
		public void commandMessage( String msg ) throws IOException
		{
			if (msg.equals(""))
				return;
			
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
				
				//The player is ready.
				case ("ready"): case ("reafy"):
					// Other person has told us they are ready.
					if (isMultiplayerOn && isMultiplayerReady)
					{
						// Tell other player we are ready
						Updater cmd = new Updater("reafy");
						sendToServer(cmd);
						
						toolBar.playPauseButton.doClick();
    					clientUI.display("[INFO] Match started.", Color.CYAN);
					}
					else if (isMultiplayerOn && !isMultiplayerReady)
					{
						isMultiplayerReady = true;
						localGame.getBoard().setMultiplayerEnabled(isMultiplayerReady);
						
						Updater cmd = new Updater("reafy");
						sendToServer(cmd);
						
						toolBar.getStatusLabel().setForeground(Color.ORANGE);
						toolBar.getStatusLabel().setText("Waiting for opponent to be ready!");
						clientUI.display("[INFO] You are ready!", Color.CYAN);
					}
				break;
				
				//Terminates the client
				case ("exit"): case ("quit"):
					quit();
				break;
				
				//*******************************************************************//
				// Setter methods
				
				//Sets the host if client not connected
				case ("sethost"):
					if(this.isConnected())
						disconnect();
					setHost(operand);
					clientUI.display("Host set: " + getHost(), Color.YELLOW);
					connect();
				break;
				
				//Sets the port if client not connected
				case ("setport"):
					if(this.isConnected())
						disconnect();
					setPort(Integer.parseInt(operand));
					clientUI.display("Port set: " + getPort(), Color.YELLOW);
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
		
		//****************************CONNECTIONS****************************//
		
		/**
		 * Method informs the user server has been terminated and closes the client
		 */
		protected void connectionClosed(){
			clientUI.display("Disconnected from server.", Color.ORANGE);
			serverInfo.setText("");
			isMultiplayerOn = false;
			localGame.getBoard().setMultiplayerEnabled(isMultiplayerOn);
			playerList.clearList();
			repaint();
		}
		
		/**
		 * Method informs the user server has been terminated and closes the client
		 */
		protected void connectionException(Exception exception)
		{
			clientUI.display("Server closed. Abnormal termination of connection.", Color.ORANGE);
			serverInfo.setText("");
			isMultiplayerOn = false;
			localGame.getBoard().setMultiplayerEnabled(isMultiplayerOn);
		}
		
		/**
		 * Hook method called when a client successfully connects to a server
		 */
		protected void connectionEstablished()
		{
			clientUI.display("Connected to server.");
			serverInfo.setText("Multiplayer @ "+getHost()+" : "+getPort());
			isMultiplayerOn = true;
			localGame.getBoard().setMultiplayerEnabled(isMultiplayerOn);
		}
		
		//****************************GETTER/SETTER****************************//
		
		/**
		 * Returns the chat interface of the TetrisClient
		 * @return clientUI
		 */
		public ChatIF getClientUI(){
			return clientUI;
		}
		
		//********************************CONTROL********************************//
		
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
		 * 
		 * @param playerWon is {@code true} if this player won the match.
		 * @param opponent this player's opponent.
		 */
		private void matchOver(boolean playerWon, String opponent)
		{
			if (playerWon)
				clientUI.display("You won!", Color.BLUE, new Font("Malgun Gothic", Font.BOLD, 16));
			else
				clientUI.display("You lost to "+opponent, Color.BLUE, new Font("Malgun Gothic", Font.BOLD, 16));
			
			isMultiplayerReady = false;
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
