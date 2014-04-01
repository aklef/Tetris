package Tetris2P;

import java.io.Serializable;

import Tetris2P.Shape.Tetromino;


/**
     * This class contains variables used to update the board of the opponent Tetris game.
     * The updater is updated every time dropPiece is called
     * 
	 * @author Andréas K.LeF.
	 * @author Dmitry Anglinov
     */
    public class Updater implements Serializable
    {
		/**
		 * Variable holds the currently held piece by the local player.
		 * It will be passed onto the opponent to update their opponent ghost board
		 */
    	protected Shape newHoldPiece;
    	/**
		 * Variable holds the new next piece that the local player will obtain.
		 * It will be passed onto the opponent to update their opponent ghost board
		 */
    	protected Shape newNextPiece;
    	/**
		 * Variable holds the current piece that the local player has obtained.
		 * It will be passed onto the opponent to update their opponent ghost board.
		 */
    	protected Shape newCurPiece;
    	/**
		 * Variable that holds the new updated board of the local player.
		 * It will be passed onto the opponent to update their opponent ghost board.
		 */
    	protected Tetromino[] newBoard;
		/**
		 * If set, represents a command sent by the server to a specific player.
		 */
    	protected String command;
    	
    	/**
    	 * This constructor updates the local clients game with the new input after a piece has been dropped.
    	 * This information will be passed onto the opponent to update the opponents "ghost" board.
    	 * The inputs are taken from board since Updater is a nested class.
    	 * 
    	 */
    	protected Updater(Shape holdPiece, Shape nextPiece, Shape curPiece, Tetromino[] board)
    	{
    		newHoldPiece = holdPiece;
    		newNextPiece = nextPiece;
    		newCurPiece = curPiece;
    		newBoard = board;
    	}
    	
    	/**
    	 * Alternate constructor to only pass string commands to clients.
    	 * @param msg the command to be sent.
    	 */
    	protected Updater(String msg)
    	{
    		command = msg;
    	}
    	
    	/**
    	 * This method returns the string command and is used to check if 
    	 * the Updater was sending a command such as "Game Over"
    	 * 
    	 */
    	public String getCommandMessage()
    	{
    		return command;
    	}
    	
    	/**
    	 * Returns a {@code String} representation of this {@code Updater}.
    	 */
    	@Override
    	public String toString()
    	{
    		return "[UPDATER]: [CMD]: "+ command + newHoldPiece + newNextPiece + newCurPiece + " [board] "+ newBoard.toString();
    	}
    }