/**
 * 
 */
package Tetris2P;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import Tetris2P.ChatIF;

/**
 * @author Andréas K.LeF.
 * @author Dmitry Anglinov
 */

public class ServerConsole implements ChatIF{ 

//Class variables *************************************************
/**
 * The default port to listen on.
 */
final public static int DEFAULT_PORT = 1337;

//Instance variables **************************************************
/**
 * The default port to listen on.
 */
 TetrisServer server;


/**
 * Constructs an instance of the chat client.
 *
 * @param host The server to connect to.
 * @param port The port number to connect on.
 * @param clientUI The interface type variable.
 */

public ServerConsole(int port) 
{
	server = new TetrisServer(port, this);
	
	try 
	{
		server.listen(); //Start listening for connections
		display("[INFO] Connected to TetrisServer");
	}
	catch (Exception ex) 
	{
		display("[ERROR] Could not listen for clients on port: "+port);
		System.exit(0);
	}  
}

/**
 * This method overrides the method in the ChatIF interface.  It
 * displays a message onto the screen and also sends the string to be displayed to all clients.
 *
 * @param message The string to be displayed.
 */
public void display(String message) 
{
	System.out.println(message); //printing message to server console
}

/**
 * This method waits for input from the console.  Once it is 
 * received, it sends it to the server's message handler to be displayed.
 */
public void accept() 
{
	try
	{
		BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
		String message;
	
		while (true)
		{
			message = fromConsole.readLine();
			this.display(message); //sending message to be printed
		}
	}
	catch (Exception ex)
	{
		display("[ERROR] Cannot read from console!");
	}
}

public static void main(String[] args) 
{
	int port = 0;
	
	try
	{
		port = Integer.parseInt(args[0]);
	}
	catch(ArrayIndexOutOfBoundsException e)
	{
		port = DEFAULT_PORT;
	}
	
	ServerConsole serverChat = new ServerConsole(port);	
	serverChat.display("[INFO] ServerConsole started");
	serverChat.accept();  //Wait for console data    
	serverChat.display("[INFO] Accepting console input");
	}
	
}
