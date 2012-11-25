package Tetris2P;

import java.io.*;
import javax.sound.sampled.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Tetris2P.Shape.Tetromino;
import Tetris2P.Tetris.HotBar;

/**
 * This class represents an instance on the board where a player interacts with the game and moves pieces.
 * 
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */
@SuppressWarnings("unused")
public class Board extends JPanel implements ActionListener {

    /**
     * The width of the board units of blocks.
     */
	private static int SQUARES_IN_WIDTH = 10;
    /**
     * The height of the board in units of blocks.
     */
    private static int SQUARES_IN_HEIGHT = 20;
    /**
     * The font used for labels.
     */
    private static Font labelFont;

    /**
     * Array containing all the colors that can be used in the game.
     */
    private static final Color colors[] = {
    		new Color(0, 0, 0), // NoShape Black, Ok
    		new Color(240,30,61), // Z Red
    		new Color(60, 197, 80), // Z Green, OK
    		new Color(32, 165, 247), // Stick Blue
    		new Color(182, 36, 166), // T Mauve, OK
    		new Color(255,202,14), // Square Yellow
    		new Color(32, 58, 247), // L Dark Blue
    		new Color(250,114,0) // L Orange, OK
    };
    /**
     * The Initial delay before starting to generate game ticks in miliseconds.
     */
	private static int	INITIAL_DELAY = 700;
    /**
     * The delay between game ticks in miliseconds.
     */
	private static int	DELAY = 600;
    /**
     * The {@code Timer} object used to generate game ticks.
     */
    private Ticker timer;
    /**
     * Boolean variable that determines whether the current piece has finished falling. False otherwise.
     */
    private boolean isFallingFinished = false;
    /**
     * Boolean variable that determines whether the game has started. False otherwise.
     */
    private boolean isStarted = false;
    /**
     * Boolean variable that determines if the game is paused. False if active.
     */
    private boolean isPaused = false;
    /**
     * Returns true if the initial newPiece call has been made.
     */
    private boolean isFirstPieceMade = false;
    /**
     * The number of lines the user has nerfed.
     */
    private int numLinesRemoved = 0;
    /**
     * The current X-axis position of the cursor on the board.
     */
    private int curX = 0;
    /**
     * The current X-axis position of the cursor on the board.
     */
    private int curY = 0;
    /**
     * The current game status is displayed here. This will be replaced with console/in-game chat output.
     */
    private final JLabel statusBar;
    /**
     * The HUD for the Next and the Hold shapes, and the score(?).
     */
    private final HotBar toolBar;
    /**
     * The current {@code Shape} object being moved on the board.
     */
    private Shape curPiece;
    /**
     * The current {@code Shape} object being held.
     */
    private Shape holdPiece;
    /**
     * The next {@code Shape} object to be placed on the board.
     */
    private Shape nextPiece;
    /**
     * This variable will show wether the user has held a piece during the current
     * falling shape cycle
     */
    private boolean isPieceHeld;
    /** 
     * The timer thread that generates board update events.
     */
    private Thread myTimer;
    /**
     * Array of {@code Tetrominoes} that encodes all the Tetrominoes on the board into a single array.
     */
    private Tetromino[] board;
    /**
     * Sound effect for when a piece is rotated.
     */
    private Clip rotateSound;
    /**
     * Sound effect for when a piece is rotated.
     */
    private Clip moveSound;
    /**
     * Sound effect for when a piece is rotated.
     */
    private Clip dropSound;
    /**
     * Soundtrack for the game
     */
    private Clip tetrisTheme; 
    
    
    /**
     * This variable will be true if audio can play
     * 
     */
    private boolean boardAudio;

    /**
     * Instance of the tetris client that allows updates to the server
     */
    //private TetrisClient client;
    
    /**
     * Constructor method.
     * @param {@code Tetris} The parent class of this board. 
     */    
    protected Board( Tetris parent )
    {    	
        //Starts playing the soundtrack
        playSoundtrack();
       
       // Setting the initial piece conditions.
       setFocusable(true);
       curPiece = new Shape();
       nextPiece = new Shape();
       holdPiece = new Shape();
       holdPiece.setShape(Tetromino.NoShape); //player has no shape held at start
       
       //checking if muted
       boardAudio = parent.isAudioPlaybackAllowed();
       
       toolBar = parent.getToolBar();
       statusBar = parent.getStatusBar();
       labelFont = new Font(statusBar.getFont().getName(), Font.ITALIC+Font.BOLD, statusBar.getFont().getSize());
       statusBar.setFont(labelFont);
       statusBar.setForeground(Color.WHITE);
       
       board = new Tetromino[SQUARES_IN_WIDTH * SQUARES_IN_HEIGHT];
       // Sets the listener for the board to an instance of the TAdapter class.
       addKeyListener(new TAdapter());
       
       addComponentListener(new ComponentAdapter() {
           /**
            * Overrides the default resizing behaviour of the Tetris panel.
            */
           @Override
           public void componentResized(ComponentEvent e)
           {
               Rectangle rect = e.getComponent().getBounds();
               e.getComponent().setBounds(rect.x, rect.y, rect.height/2, rect.height);
               // nerfs the subcomponents' dimensions and recalculates everything
               revalidate();
           }
           });
       // Starts the timer for the board
       // will automatically be paused because isPaused is TRUE
       start();
    }
    
    //*************************************SETTER/GETTER*************************************//
    
    /**
     * Setting board audio
     * 
     */
    public boolean getBoardAudio(){
    	return boardAudio;
    }
    
    /**
     * Setting board audio
     * 
     */
    public void setBoardAudio(boolean audioState){
    	boardAudio = audioState;
    }
    
    /**
     * Receives a game tick update event from the {@code Timer} class every {@code timer} miliseconds.
     */
    public synchronized void actionPerformed(ActionEvent e)
    {
    	if (isFallingFinished)
        { // current piece in its final spot
            isFallingFinished = false; // reset falling
            isPieceHeld = false; // reset swap priviledge
            newPiece();
        }
        else 
        {// current piece still falling
        	oneLineDown();
        }
    	repaint();
    }

    /**
     * Returns the width in pixels of one square on the board
     */
    protected int squareWidth()
    {
    	return (int) getSize().getWidth() / SQUARES_IN_WIDTH;
    }

    /**
     * Returns the height in pixels of one square on the board
     */
    protected int squareHeight()
    {
    	return (int) getSize().getHeight() / SQUARES_IN_HEIGHT;
    }

    /**
     * Returns the {@code Tetromino} {@code Shape} enum type at a certain point on the board.
     */
    private synchronized Tetromino shapeAt (int x, int y)
    {
    	return board[(y * SQUARES_IN_WIDTH) + x];
    }
    
    //*************************************CONTROL*************************************//
    

    /**
     * Public method used to start the timer for this board.
     * This method is called by the constructor during initialization
     * The method also initializes the board
     */
    public void start()
    {
        if (isPaused)
            return;
		
        isStarted = true;
        isFallingFinished = false;
        isPieceHeld = false;
        numLinesRemoved = 0;
        clearBoard();
        
        //Initializing the timer
		timer = new Ticker(DELAY, this);
		timer.setInitialDelay(INITIAL_DELAY);
		
		myTimer = new Thread(timer);
        
        newPiece();
        myTimer.start();
        pause();
    }


    /**
     * Pauses the game by stoping the timer. Changes the statusBar message.
     * If the method was called when game was paused, game resumes
     */
    private void pause()
    {
        if (!isStarted)
            return;
        
        isPaused = !isPaused;
        timer.setPaused(isPaused);
        if (isPaused) { //pausing the game
            statusBar.setText(" Game [P]aused. ");
            statusBar.setForeground(Color.magenta);
        } else { // resuming the game
            statusBar.setText(" "+numLinesRemoved);
            statusBar.setForeground(Color.green);
        }
        repaint();
    }

    /**
     * Restarts the game.
     */
    private void restart()
    {
        if (!isPaused)
            return;
        
        isStarted = true;
        isFallingFinished = false;
        isFirstPieceMade = false;
        isPieceHeld = false;
        boardAudio = true;
        numLinesRemoved = 0;
        holdPiece.setShape(Tetromino.NoShape);
        nextPiece.setShape(Tetromino.NoShape);
        statusBar.setText(" Game paused");
        statusBar.setForeground(Color.magenta);
        
        playSoundtrack();
        clearBoard();
        newPiece();
        repaint();
        try
		{
			Thread.sleep(300);
		}
		catch (InterruptedException e){}
        
        pause();
    }
    
    /**
     * Method called when the player tops out.
     */
    public void gameOver()
    {
        isStarted = false;
        isFallingFinished = false;
        isFirstPieceMade = false;
        isPieceHeld = false;
        isPaused = true;
        
        numLinesRemoved = 0;
        
        curPiece.setShape(Tetromino.NoShape);
        statusBar.setText(" Game over. Press [Q]uit [R]estart");
        statusBar.setForeground(Color.ORANGE);
        
        timer.setPaused(isPaused);
        
        repaint();
        pause();
    }
    
    //*************************************LOGIC*************************************//
    
    /**
     * Generates a new random piece.
     */
    private void newPiece()
    {
        if (!isFirstPieceMade){
        	// the first next piece
        	nextPiece.setRandomShape();
        	isFirstPieceMade = true;
        }
        curPiece.setShape(nextPiece.getShape());
        // Generates a new next piece.
        nextPiece.setRandomShape();
        toolBar.setNextPiece(nextPiece);
        
        // Resets the cursor's position to the top of the board.
        curX = (SQUARES_IN_WIDTH / 2) + 1;
        // XXX WHYYYYYYY
        curY = SQUARES_IN_HEIGHT - 1 + curPiece.minY();
        // XXX Determines if the game is over.
        boolean isGameOver = !tryMove(curPiece, curX, curY);
        if (isGameOver) {
        	gameOver();
        }
    }

    
    /**
     * Method that releases the held piece (if applicable),
     *  then puts the current piece on hold.
     */
    private void hold()
    {
    	//The user has already swapped a piece during the current falling shape phase
    	if(isPieceHeld)
    		return;
    
        if (holdPiece.getShape()==Tetromino.NoShape)
        {// If there is a no piece being held generate a new one 
            holdPiece.setShape(curPiece.getShape());
            newPiece();
        }
        else
        { // If there is, then replace the current piece
            //Assign the current piece as the previously held piece
        	Shape pastHoldPiece = new Shape();
        	pastHoldPiece.setShape(holdPiece.getShape());
            holdPiece.setShape(curPiece.getShape());
            curPiece.setShape(pastHoldPiece.getShape());
        }
        
        // Resets the cursor's position to the top of the board.
        curX = SQUARES_IN_WIDTH / 2;
        curY = SQUARES_IN_HEIGHT - 1 + curPiece.minY();
        isPieceHeld = true;
        isFallingFinished = false;
        toolBar.setHoldShape(holdPiece);
        repaint();
    }
    
    /**
     * Method that immediately drops a piece to the bottom of the board.
     */
    private void dropDown()
    {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1))
                break;
            --newY;
        }
        pieceDropped();
    }

    /**
     * Method that tries to lower the current piece by one line.
     * If it cannot then the piece is in its final location.
     */
    private synchronized void oneLineDown()
    {
        // attempts to lower the piece
    	if (!tryMove(curPiece, curX, curY - 1))
    		// if cannot lower piece
            pieceDropped();
    }

    /**
     * Method called when a shape is in its final location.
     */
    private synchronized void pieceDropped()
    {
        try
		{
			Thread.sleep(100);
		}
		catch (InterruptedException e){}
    	
    	for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * SQUARES_IN_WIDTH) + x] = curPiece.getShape();
        }
        
        removeFullLines();
        isPieceHeld = false;
        
        if (!isFallingFinished)
            newPiece();
        
        //Saving the current contents of the game to be able to pass to the opponent ghost board
        Updater update = new Updater();
        
        
    }

    /**
     * Fills the board with empty shapes.
     */
    private void clearBoard()
    {
        for (int i = 0; i < SQUARES_IN_HEIGHT * SQUARES_IN_WIDTH; ++i)
            board[i] = Tetromino.NoShape;
    }
    
    /**
     * This method updates the opponent ghost with the new information. 
     * It is called through the opponentGame instance .
     * @param Updater which contains data to update the opponent's board.
     */
    public void updateBoard(Updater updater){
		
    	//updates the opponent toolbar
    	holdPiece.setShape(updater.newHoldPiece.getShape());
		nextPiece.setShape(updater.newNextPiece.getShape());
		curPiece.setShape(updater.newCurPiece.getShape());
		board = updater.newBoard;
		
		//repainting the opponent game board
		repaint();
    }

    /**
     *      * Attemps to lower a given {@author Shape} at a certain {@code x} and {@code y} corrdinate by one vertically.
     * This method needs fixing!
     * 
     * Iterates through all the possible locations defined for a shape and checks if any of them are out of bounds or illegal.
     *
     * 
     * @param newPiece the {@code Shape} that we are checking
     * @param newX the new
     * @param newY
     * @return
     */
    private synchronized boolean tryMove(Shape newPiece, int newX, int newY)
    {
    	for (int i = 0; i < 4; ++i)
    	{
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            // if the x value is out of range return
            if (x < 0 || x >= SQUARES_IN_WIDTH || y < 0 || y >= SQUARES_IN_HEIGHT || shapeAt(x, y) != Tetromino.NoShape)
                return false;
        }
        
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        
        return true;
    
    }

    /**
     * Checks all the rows of the board for full lines. If there is at least
     * one full full line, remove it and increment the counter.
     */
    private void removeFullLines()
    {
        int numFullLines = 0;
        
        //checks if the line is full
        for (int i = SQUARES_IN_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            
            for (int j = 0; j < SQUARES_IN_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetromino.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            
            //moving all lines above the full line one line down
            //This will effectively destroy the line
            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < SQUARES_IN_HEIGHT - 1; ++k) {
                    for (int j = 0; j < SQUARES_IN_WIDTH; ++j)
                         board[(k * SQUARES_IN_WIDTH) + j] = shapeAt(j, k + 1);
                }
            }
        }

		//Updating the total number of lines removed by the user
        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            statusBar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            isPieceHeld = false;
            curPiece.setShape(Tetromino.NoShape);
            repaint();
        }
     }

	/**
	 * Timer class used to generate game ticks.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	private class Ticker extends Thread {
		
		
		private int m_delay;
		private int m_initial_delay = -1;
		private boolean m_paused = true;
		private ActionListener m_cb;
		private final ActionEvent m_ae;
		
		/**
		 * 
		 * @param delay The delay between ticks in miliseconds.
		 * @param cb The {@code ActionListener} listening for events.
		 */
		public Ticker(int delay, ActionListener cb) {
			m_delay = delay;
			m_cb = cb;
			m_ae = new ActionEvent(this, 444, "");
		}
		public void setInitialDelay(int delay) {
			m_initial_delay = delay;
		}
		public void setPaused(boolean pause) { 
			m_paused = pause;
			if(m_paused) {
				tetrisTheme.stop();
				boardAudio = false;
			}
			else {
				tetrisTheme.loop(Clip.LOOP_CONTINUOUSLY);
				boardAudio = true;
				synchronized(this) {
					this.notify();
				}
			}
		}

		public void run()
		{
			while(true)
			{
				try {
					if (m_initial_delay != -1)
					{ // waits the initial delay
						sleep(m_initial_delay);
						m_initial_delay = -1;
					} // wait the specified amount of time
					sleep(m_delay); 
				} catch (Exception e) {}
				
				if(m_paused) {
					try {
						synchronized(this) {
							this.wait();
						}
					} catch(InterruptedException ie) {}
				}
				// XXX Send an action event to the board
				synchronized(this) {
					m_cb.actionPerformed(m_ae);
				}
			}
		}
	} // end class Timer

    //*************************************GRAPHICS*************************************//

    /**
     * Overrides the {@code Board} panel's default painting behaviour.
     */
	@Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        int SUBDIVISION_SIZE = this.getWidth()/SQUARES_IN_WIDTH;
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(Color.DARK_GRAY);
        // Draws vertical lines
        for (int i = 1; i < SQUARES_IN_WIDTH; i++) {
           int x = i * SUBDIVISION_SIZE;
           g2.drawLine(x, 0, x, getSize().height);
        }
        
        SUBDIVISION_SIZE = this.getHeight()/SQUARES_IN_HEIGHT;
        
        // Draws horizontal lines.
        for (int i = 1; i < SQUARES_IN_HEIGHT; i++) {
           int y = i * SUBDIVISION_SIZE;
           g2.drawLine(0, y, getSize().width, y);
        }
        
        int boardTop = (int)  getSize().getHeight() - (SQUARES_IN_HEIGHT * squareHeight()); // in pixels
        
        // Draws all the pieces on the board.
        for (int i = 0; i < SQUARES_IN_HEIGHT; ++i) {
            for (int j = 0; j < SQUARES_IN_WIDTH; ++j) {
                Tetromino shape = shapeAt(j, SQUARES_IN_HEIGHT - i - 1);
                if (shape != Tetromino.NoShape)
                    drawSquare(g, 0 + j * squareWidth(),
                               boardTop + i * squareHeight(), shape);
            }
        }
        // Draws the current piece.
        if (curPiece.getShape() != Tetromino.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 0 + x * squareWidth(),
                           boardTop + (SQUARES_IN_HEIGHT - y - 1) * squareHeight(),
                           curPiece.getShape());
            }
        }
    }

    /**
     * Draws a square on the given {@code Graphics} {@code g} with accent and shading lines.
     * 
     * @param g the {@code Graphics} area on which to draw.
     * @param x the x coordinate of the square in pixels.
     * @param y the y coordinate of the square in pixels.
     * @param shape the {@code Tetromino} Shape to be drawn.
     */
    protected void drawSquare(Graphics g, int x, int y, Tetromino shape)
    {
        Color color = colors[shape.ordinal()];
        
        g.setColor(color);
        // ORIGIN IS AT (0,0) IN THE TOP LEFT CORNER OF YOUR SCREEN
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2); // all units are in pixels
        
        // draws accent lines
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        // draws shaping lines
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
        
    }


    //*************************************AUDIO*************************************//
    
    /**
     * This method initiates and plays a rotate sound effect
     *  
     */
    public void initRotateSound(){
        try {
    		AudioInputStream rotateAudio = AudioSystem.getAudioInputStream(new File("Media/rotateSound.wav"));
    		rotateSound = AudioSystem.getClip();
    		rotateSound.open(rotateAudio);
    		rotateSound.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * This method initiates and plays a move sound effect
     */
    public void initMoveSound(){
        try {
    		AudioInputStream moveAudio = AudioSystem.getAudioInputStream(new File("Media/moveSound.wav"));
    		moveSound = AudioSystem.getClip();
    		moveSound.open(moveAudio);
    		moveSound.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * This method initiates and plays a drop sound effect
     */
    public void initDropSound(){
        try {
    		AudioInputStream dropAudio = AudioSystem.getAudioInputStream(new File("Media/moveSound.wav"));
    		dropSound = AudioSystem.getClip();
    		dropSound.open(dropAudio);
    		dropSound.start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Begins playing the Tetris theme song in a continuous loop
     *
     */
    public void playSoundtrack(){
        try {
    		AudioInputStream music = AudioSystem.getAudioInputStream(new File("Media/tetris_nintendo_8bit.wav"));
    		tetrisTheme = AudioSystem.getClip();
            tetrisTheme.open(music);
            tetrisTheme.loop(Clip.LOOP_CONTINUOUSLY); 
        }catch(Exception e){
            e.printStackTrace();
        }
	}

    //*************************************INPUT*************************************//

    /**
     * Method that extends the abstract KeyAdapter class (overriding only the methods of interest).
     * Listens for keyboard input.
     * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
     */
    protected class TAdapter extends KeyAdapter {
         public void keyPressed(KeyEvent e)
         {
             int keycode = e.getKeyCode();
             
             // Command statement switch
             switch (keycode) {
                 case 'Q': case 'q':
                     System.exit(0);
                     break;
                 case 'R': case 'r':
                     restart();
                     break;
                 case 'P': case 'p':
                	 pause();
                	 break;
             }
             
             // Parses no input if the game is paused.
             if (isPaused || !isStarted || curPiece.getShape() == Tetromino.NoShape)
                 return;
             
             // Switch on input key value
             switch (keycode){
             case KeyEvent.VK_UP: case 'W': case 'w': // rotate
            	 synchronized(timer) {
            		 tryMove(curPiece.rotate(), curX, curY);
            		 
            		 //generates sound effect
                	 if(boardAudio)
                		 initRotateSound();
            	 }
                 break;
             case KeyEvent.VK_LEFT: case 'A': case 'a': // move left
            	 synchronized(timer) {
            		 tryMove(curPiece, curX - 1, curY);
            		 
            		 //moveSound sound effect       
            		 if(boardAudio)
            			 initMoveSound();
            	 }
                 break;
             case KeyEvent.VK_RIGHT: case 'D': case 'd': // move right
            	 synchronized(timer) {
            		 tryMove(curPiece, curX + 1, curY);
            		 
            		 //generates sound effect
                	 if(boardAudio)
                		 initMoveSound();
            	 }
                 break;
             case KeyEvent.VK_DOWN: case 'S': case 's': // nudge down
            	 oneLineDown();
            	 
        		 //generates sound effect
            	 if(boardAudio)
            		 initMoveSound();
            	 break;
             case KeyEvent.VK_SHIFT: case 'H': case 'h': // hold
                 hold();
                 break;
             case KeyEvent.VK_SPACE: // drops piece to bottom
            	 
        		 //generates sound effect
            	 if(boardAudio)
            		 initDropSound();
                 dropDown();
                 break;
             }
         }
     }
    
    /**
     * This class contains variables used to update the board of the opponent Tetris game.
     * The updater is updated every time dropPiece is called
     * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
     */
    public class Updater
    {
		private Shape newHoldPiece;
    	private Shape newNextPiece;
    	private Shape newCurPiece;
    	private Tetromino[] newBoard;
    	
    	/**
    	 * This constructor updates the local clients game with the new input after a piece has been dropped.
    	 * This information will be passed onto the opponent to update the opponents "ghost" board.
    	 * The inputs are taken from board since Updater is a nested class.
    	 * 
    	 */
    	public Updater()
    	{
    		newHoldPiece = holdPiece;
    		newNextPiece = nextPiece;
    		newCurPiece = curPiece;
    		newBoard = board;
    	}
    }
}
