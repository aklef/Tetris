/**
 * 
 */
package Tetris2P;

import java.io.IOException;
import java.net.Socket;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * @author Andréas K.LeF.
 *
 */
public class ConnectionToTetrisClient extends ConnectionToClient
{

	/**
	 * This client's opponent.
	 */
	protected ConnectionToTetrisClient opponent;
	
	/**
	 * This class extends the {@code ConnectionToTetrisclient} class i the ocsf package.
	 * This creates the idea of pairs of clients.
	 * 
	 * @see ConnectionToTetrisclient
	 */
	ConnectionToTetrisClient(ThreadGroup group, Socket clientSocket, AbstractServer server) throws IOException
	{
		super(group, clientSocket, server);
		
	}
	
}
