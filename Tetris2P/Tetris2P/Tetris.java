package Tetris2P;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import Tetris2P.Shape.Tetromino;
import Tetris2P.Tetris2P.OutputBox;
import Tetris2P.Tetris2P.TetrisClient;

/**
 * This class represents one complete instance of a game of tetris played by a single user.
 * Is composed of a central {@code Board} area, where {@code Shapes} can be seen falling.
 * Has a small {@code HotBar} where the {@code currentPiece} and {@code nextPiece} are displayed.
 * 
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */
public class Tetris extends JPanel{

    /**
     * The width of the game.
     */
	private static int 		BOARD_WIDTH = 200;
    /**
     * The height of the game.
     */
    private static int 		BOARD_HEIGHT = 400;
    /**
     * The output {@code ChatIF} for game statuses.
     */
    @SuppressWarnings("unused")
	private OutputBox 		output;
    /**
     * The toolbar object.
     */
    private final HotBar 	hotBar;
    /**
     * The board object.
     */
    private final Board 	board;
    /**
     * Static variable representing the backroung color of the board.
     */
    private static Color 	backgroundColor;
    /**
     * Master on/off for all game audio.
     */
    private boolean 		isAudioPlaybackAllowed;

    //*************************************CONSTRUCTORS*************************************//
    
    /**
     * Constructor for a Tetris game.
     */
    public Tetris(OutputBox output)
    {
    	backgroundColor = new Color (13,13,13);
    	
    	isAudioPlaybackAllowed = false;
    	
    	hotBar = new HotBar();
    	hotBar.setBackground(backgroundColor);
    	
    	this.output = output;
        
        board = new Board(this, output);
        board.setMinimumSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        board.setSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        board.setBackground(backgroundColor);
        board.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        
        setLayout(new BorderLayout());
        
        add(hotBar, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        
        Dimension areaDim =  new Dimension(board.squareWidth()*4, board.squareHeight()*4);
        hotBar.holdArea.setPreferredSize(areaDim );
        hotBar.previewNextPieceArea.setPreferredSize(areaDim);
        
        setPreferredSize(getSize());
   }

    /**
     * Calls the default constructor of this class
     * 
	 * @param tetrisClient this game's parent's client.
	 * @param outputBox the output area.
	 */
	public Tetris(TetrisClient tetrisClient, OutputBox outputBox)
	{
		this(outputBox);
		
		board.setClient(tetrisClient);
	}

    //*************************************SETTER/GETTER*************************************//
	
	/**
     * Returns the {@code ToolBar} Object  belonging to this game.
     */
    protected HotBar getToolBar() {
       return hotBar;
   }

    /**
     * Returns the {@code Board} Object belonging to this game.
     */
    protected Board getBoard() {
       return board;
   }

    /**
     * Returns the background color of this panel.
     */
    @Override
    public Color getBackground() {
       return backgroundColor;
   }

    /**
     * Sets the master audio control to the given boolean value.
     * 
     * @param audioState True if audio playback is allowed, false otherwise.
     */
    protected void setAudioPlayback(boolean audioState) {
    	isAudioPlaybackAllowed = audioState;
    	board.setBoardAudio(audioState);
   }
    
    protected boolean isAudioPlaybackAllowed(){
    	return isAudioPlaybackAllowed;
    }

    //*************************************TOOLBAR*************************************//
	
	/**
	 * This is a nested class in Tetris.java that is a JPanel. It is displayed at the top of the main Tetris frame.
	 * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
	 */
	public class HotBar extends JPanel
	{
	    /**
	     * The {@code ShapeArea} to show piece on hold.
	     */
	    private ShapeArea holdArea;
	    /**
	     * The {@code ShapeArea} to show the upcoming piece.
	     */
	    private ShapeArea previewNextPieceArea;
	    
		/**
		 * Constructor method.
		 */
		public HotBar()
		{
			
			this.setLayout(new BorderLayout());
			
			holdArea = new ShapeArea();
			holdArea.setBackground(backgroundColor);
			holdArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLUE));
			add(holdArea, BorderLayout.WEST);
			
			previewNextPieceArea = new ShapeArea();
			
			previewNextPieceArea.setBackground(backgroundColor);
			previewNextPieceArea.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.YELLOW));
			add(previewNextPieceArea, BorderLayout.EAST);
			
			this.setFocusable(false);
			this.setVisible(true);
		}

		/**
		 * Sets a new next shape to be displayed.
		 * Calling this method forces the {@code ShapeArea} to call {@code repaint}.
		 * @param nextPiece The {@code Shape} that will drop next.
		 */
		protected void setNextPiece ( Shape nextPiece )
		{
			previewNextPieceArea.setShape(nextPiece);
		}
		
		/**
		 * Sets a new held shape to be displayed.
		 * Calling this method forces the {@code ShapeArea} to call {@code repaint()}.
		 * @param holdPiece The {@code Shape} to hold.
		 */
		protected void setHoldShape ( Shape holdPiece )
		{
			holdArea.setShape(holdPiece);
		}
		
		/**
		 * The paint method.
		 */
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
		}
    
	    //*************************************SHAPEAREA*************************************//

	    /**
	     * The {@code ShapeArea} class extends {@code JPanel} and is used as a display area for tetrominoes.
	     * 
	     * @author Andréas K.LeF.
	     * @author Dmitry Anglinov
	     */
		protected class ShapeArea extends JPanel
	    {
	    	/**
	         * The {@code Shape} that will be displayed.
	         */
	        private Shape piece;

	        /**
	         * The {@code Shape} that will be displayed.
	         */
	        //private TextOverlay overlay;

	    	/**
	    	 * The constructor for the ShapeArea. Its a JPanel to show the current piece.
	    	 */
	    	protected ShapeArea()
	    	{
	    		piece = new Shape();
	    		piece.setShape(Tetromino.NoShape);
	    		
	    		setFocusable(false);
	    		setVisible(true);
	    	}

	    	/**
	    	 * 
	    	 * @param piece The {@code Shape} to store.
	    	 */
	    	protected void setShape ( Shape newPiece )
	    	{
	    		piece = newPiece;
	    		repaint();
	    	}

	        /**
	         * Overrides the {@code ShaeArea} panel's default painting behaviour.
	         */
	        @Override
	        public void paintComponent(Graphics g)
	        {
	            super.paintComponent(g);
	            
	            setPreferredSize( new Dimension(board.squareWidth()*4, board.squareHeight()*4) );
	            
	            if (piece.getShape() != Tetromino.NoShape)
	            {
	            	int x, y;
	            	for (int i = 0; i < 4; ++i)
	                {
	                    x = piece.x(i);
	                    y = piece.y(i);
	                    board.drawSquare(g, 1 * board.squareWidth() + x * board.squareWidth(), (y+1) * board.squareHeight(), piece.getShape());
	                }
	            }
	        }

	      //*************************************TEXTOVERLAY*************************************//
	        
	        /**
	         * 
	         * 
	         * @author Andréas K.LeF.
	         * @author Dmitry Anglinov
	         */
			private class TextOverlay extends JComponent
	        {
	            /**
		         * The overlay text.
		         */
		        private String overlayText;
		        /**
		         * 
		         */
		        private AlphaComposite ac;
		        /**
		         * 
		         */
		        private float alpha;
		        
		        /**
		         * @param overlayText
		         */
		        protected TextOverlay(String overlayText)
		        {
		            this.overlayText = overlayText;
		            
		            setFocusable(false);
		            //setOpaque(false);
		            
		            alpha = 0.75f;
		            
		            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	            }

	            /**
	             * Overrides the {@code Board} panel's default painting behaviour.
	             * Casts the defualt {@code Graphics} to a {@code Graphics2D} object to
	             * possibly use a transparent overlay.
	             */
	            @Override
	            protected void paintComponent(Graphics g)
	            {
	                super.paintComponent( g );
	                Graphics2D g2 = (Graphics2D) g;
	                
	                g2.setColor(Color.CYAN);
	                g2.setComposite(ac);
	                
	                //g2.drawString(overlayText, board.squareWidth()*2, board.squareHeight()*2);
	                g2.fillRect(10, 10, 10, 10);
	            }

	            /**
	             * Setter method to change the message on the overlay.
	             * @param newOverLayText 
	             */
	            protected void setOverlayText(String newOverLayText)
	            {
	            	overlayText = newOverLayText;
	            	repaint();
	            }
	        }
	    }
	}
}