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
import Tetris2P.Tetris.HotBar.ShapeArea;

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
        opponentGame.setAudioCanPlay(false);
        
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
		if(localGame.getAudioCanPlay()){
			localGame.setAudioCanPlay(false);
		}
		else
			localGame.setAudioCanPlay(true);
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
	public class OutputBox extends JPanel /*implements ChatIF*/
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
	public class InputBox extends JPanel
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
}