/**
 * 
 */
package UnitTest;

import static org.junit.Assert.*;

import Tetris2P.Tetris2P;
import Tetris2P.Board;
import Tetris2P.Tetris;
import Tetris2P.Shape.Tetromino;
import Tetris2P.Tetris2P.ToolBar;


import org.junit.Test;

/**
 * @author Andréas K.LeF.
 *
 */
public class BoardTest {

	/**
	 * Test method for {@link Tetris2P.Board#Board(Tetris2P.Tetris, Tetris2P.Tetris2P.OutputBox, Tetris2P.Tetris2P.ToolBar)}.
	 */
	@Test
	public void testBoard() {
		// the Board is constructed inside the Tetris class constructor called by this constructor
		Tetris2P newGame = new Tetris2P(); 
		Board localBoard = newGame.getLocalGame().getBoard();
		Board opponentBoard = newGame.getOpponentGame().getBoard();
		
		assertTrue("The local and opponent instances of Board should have been created" , localBoard != null && opponentBoard != null);		
		
	}

	/**
	 * Test method for {@link Tetris2P.Board#setBoardAudio(boolean)}.
	 */
	@Test
	public void testSetBoardAudio() {
		
		Tetris2P newGame = new Tetris2P(); 
		
		//The game starts out paused and muted, thus when the mute button is clicked the audio should have been set to active
		assertFalse(" The game should launch muted" , newGame.getLocalGame().isAudioPlaybackAllowed());
		newGame.getToolBar().getSoundButton().doClick();
		assertTrue(" The sound should be activated when mute button is hit", newGame.getLocalGame().isAudioPlaybackAllowed());

	}

	/**
	 * Test method for {@link Tetris2P.Board#restart()}.
	 */
	@Test
	public void testRestart() {
		
		Tetris2P newGame = new Tetris2P();
		Board localBoard = newGame.getLocalGame().getBoard();
		localBoard.getHoldPiece().setShape(Tetromino.LineShape);
		Tetromino shapeHeld = localBoard.getHoldPiece().getShape();

		//if the game is restarted while active the restart method should do nothing
		//if the restart method did not complete, then the currently set shape should not have changed
		localBoard.pause();
		newGame.getToolBar().getRestartButton().doClick();
		assertTrue("Restart method should have broken out immediately" , shapeHeld == localBoard.getHoldPiece().getShape());
		
		//if the game is paused and then restarted, the restart method should complete
		//if the restart method completes, the held peice should be reset to NoPiece
		localBoard.pause();
		newGame.getToolBar().getRestartButton().doClick();
		assertTrue("Restart method should have completed" , localBoard.getHoldPiece().getShape() == Tetromino.NoShape);
		
		
	}

}
