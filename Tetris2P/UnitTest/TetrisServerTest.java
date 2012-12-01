/**
 * 
 */
package UnitTest;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import Tetris2P.ServerConsole;
import Tetris2P.Tetris2P;


/**
 * @author Andréas K.LeF.
 *
 */
@SuppressWarnings("unused")
public class TetrisServerTest {

	/**
	 * Test method for {@link Tetris2P.TetrisServer#clientConnected(ocsf.server.ConnectionToClient)}.
	 */
	@Test
	public void testClientConnected() {
		int port = 1337;
		
		ServerConsole serverChat = new ServerConsole(port);	
		
		//THIS METHOD CREATES AN EXCEPTION AND DOESN'T INITIALIZE
		//NEEDS FIXING
		new Tetris2P().getTetrisClient().connect();
		int listSize = serverChat.getTetrisServer().getClientList().size();	
		assertEquals("Testing if client connected", 1, listSize);

	}

	/**
	 * Test method for {@link Tetris2P.TetrisServer#serverStarted()}.
	 */
	@Test
	public void testServerStarted() {
		int port = 1337;
		ServerConsole serverChat = new ServerConsole(port);	
		assertEquals("If server started it should be listening for connections", true, serverChat.getTetrisServer().isListening());
		
	}

	/**
	 * Test method for {@link Tetris2P.TetrisServer#closeServer()}.
	 */
	@Test
	public void testCloseServer() {
		int port = 1337;
		ServerConsole serverChat = new ServerConsole(port);	
		serverChat.getTetrisServer().closeServer();
		assertEquals("Should not be listening for connections", false, serverChat.getTetrisServer().isListening() );
		
	}

}
