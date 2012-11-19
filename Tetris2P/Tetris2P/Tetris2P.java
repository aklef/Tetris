package Tetris2P;

import java.awt.AlphaComposite;
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
import Tetris2P.Tetris.ToolBar;

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
     * Static variable representing the backroung color of the board.
     */
    private static Color backgroundColor;
    /**
     * Variable that holds the GUI for the current connected users
     */
    private final PlayerList userList;
    
    /**
     * Boolean variable that determines if the gmae will make sounds..
     */
    public boolean isMusicOn = true;
  
    
    /**
     * Constructor for the Teris multiplayer game.
     */
    public Tetris2P() {
        // GridLayout for frame
        GridLayout myGrid = new GridLayout(1, 2, 30, 0);
        
        // Awesome feature! but don't use...
        //setUndecorated(true);
        
        // Creating instances of Tetris panels
        localGame	 = new Tetris();
        opponentGame = new Tetris();
        userList	 = new PlayerList();
        
        // Default background color
        backgroundColor = new Color(13,13,13);
        
        // Setting the frame's background
        getContentPane().setBackground(backgroundColor);
        // Setting each component's background
        localGame.setBackground(backgroundColor);
        opponentGame.setBackground(backgroundColor);
        userList.setBackground(backgroundColor);
        
        // Setting the frame layout manager
        setLayout(myGrid);
        
        // Adding components to frame
        add(localGame);
        add(opponentGame);
        add(userList);
        
        // ABSOLUTELY REQUIRED - DO NOT FUCK WITH THE NUMBERS
        getContentPane().setPreferredSize(new Dimension(600,473));
        // Necsesary
        pack();
        
        // Not needed but works fine
        //revalidate();
        
        opponentGame.setFocusable(false);
        
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

	public void run() {
        // Makes the window open in the center of the screen.
        setLocationRelativeTo(null);
        
        // Makes the frame steady
        //setResizable(false);
        
        // Shows the window
        setVisible(true);
	}


    //*************************************CHATBOX*************************************//


	public class ChatBox extends JPanel implements ActionListener
	{
        
        /**
         * Holds the chat contents
        */
        private final JTextArea textArea;
        /**
         * Holds the chat contents
        */
        private final JTextField textInput;

        /**
         * Constructor for the chat box
         */
        public ChatBox()
        {
        	//creates the user interface for the chat box
            //Creating the chat box
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(200, 400));
            
            // The chat display and input areas
            textArea = new JTextArea();
            textInput = new JTextField(20);
            
            // Interface buttons
            // Should be replaced with key commands.
            JButton send = new JButton("Send");
            JButton reset = new JButton("Clear");
            
            // Adding listeners to elements
            send.addActionListener(this);
            reset.addActionListener(this);
            
            // 
            
            
            //Adding Components to the frame.
            add(textArea, BorderLayout.CENTER);
            add(textInput, BorderLayout.SOUTH);
            
            // Display the window
            setVisible(true);
        }

        
        public void actionPerformed(ActionEvent e)
        {
        	//This will modify the textPane according to messages inputed
            
        }

       /**
        * Returns the content of the text area
        **/
        public JTextArea getTextPane() {
          	return textArea;
        }
        
	}
	

    //*************************************USERLIST*************************************//

	public class PlayerList extends JPanel
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
		public PlayerList() {
	    	
	        setLayout(new BorderLayout());
	        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));
	        
	        users = new LinkedList<String>();
	        userList = new DefaultListModel();
	        JList list = new JList(userList);
	        
	        // XXX Remove! Test addition to the list
	        for(int i=0; i<10; i++){
	        	addUserToList("Dingletronic" + i);
	        }
	        // Attach a ScrollPane to the list to make it scrollable
	        JScrollPane scrollPane = new JScrollPane();
	        scrollPane.getViewport().add(list);
	        scrollPane.setPreferredSize(new Dimension(100, 100));
	        
	        // Title of the playerlist
	        label = new JLabel(" Players :");
	        label.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
	        label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
	        
	        add(scrollPane, BorderLayout.CENTER);
	        add(label, BorderLayout.NORTH);
	    }

	    /**
	     * Adds the given player's name into the list
	     * 
	     * @param username {@code String} name of player.
	     */
	    @SuppressWarnings("unchecked")
		public void addUserToList(String username){
	    	users.addLast(username); //add to end of list so the new user will be last in the queue to play
	    	userList.addElement(username); //adds a user to the list GUI
	    }

	    /**
	     * Removes the given player's name from the list
	     * 
	     * @param username {@code String} name of player.
	     */
	    public void removeUserFromList(String username){
	    	users.remove(username); //removes the user from the list
	    	userList.removeElement(username); //adds a user to the list GUI
	    }
	}
}