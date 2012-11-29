package Tetris2P;

import java.io.Serializable;


/**
 * This will group clients into a player and their respective opponent ID
 * 
 * @author Dmitry Anglinov
 * @author Andréas K.LeF.
*/
public class ClientNode implements Serializable
{
	/**
	 * The current client ID
	 */
	private Long playerID;
	/**
	 * The current client's name
	 */
	private String name;
	/**
	 * The ID of the opponent of the current client
	 */
	private Long opponentID;
	
	/**
	 * Constructor used to create a default pair of client and opponent
	 * @param playerID
	 */
	protected ClientNode(Long playerID)
	{
		this.setPlayerID(playerID);
		this.name = "Player"+playerID;
		this.setOpponentID(playerID);
	}
	
	/**
	 * Alternate constructor that allows to specify player name on creation.
	 * 
	 * @param playerID this player's ID
	 * @param playerName custom player name
	 */
	protected ClientNode(Long playerID, String playerName)
	{
		this(playerID);
		this.name = playerName;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the opponentID
	 */
	public Long getOpponentID()
	{
		return opponentID;
	}

	/**
	 * @param opponentID the opponentID to set
	 */
	public void setOpponentID(Long opponentID)
	{
		this.opponentID = opponentID;
	}

	/**
	 * @return the playerID
	 */
	public Long getPlayerID()
	{
		return playerID;
	}

	/**
	 * @param playerID the playerID to set
	 */
	public void setPlayerID(Long playerID)
	{
		this.playerID = playerID;
	}
}