/**
 * 
 */
package UnitTest;

import static org.junit.Assert.*;

import org.junit.Test;
import Tetris2P.Tetris2P;
import Tetris2P.Tetris.HotBar;

/**
 * @author Andréas K.LeF.
 *
 */
public class TetrisTest {

	/**
	 * Test method for {@link Tetris2P.Tetris#Tetris(Tetris2P.Tetris2P.OutputBox, Tetris2P.Tetris2P.ToolBar)}.
	 */
	@Test
	public void testTetrisOutputBoxToolBar() {
		
		Tetris2P newGame = new Tetris2P();
		assertTrue("The local game instance of Tetris should be created" , newGame.getLocalGame()!=null);
	
	}

	/**
	 * Test method for {@link Tetris2P.Tetris#getHotBar()}.
	 */
	@Test
	public void testGetHotBar() {
		Tetris2P newGame = new Tetris2P();
		HotBar localHotBar = newGame.getLocalGame().getHotBar();
		HotBar opponentHotBar = newGame.getOpponentGame().getHotBar();
		
		assertTrue("The local and opponent instances of HotBar should have been created" , localHotBar != null && opponentHotBar != null);
		
	}

}
