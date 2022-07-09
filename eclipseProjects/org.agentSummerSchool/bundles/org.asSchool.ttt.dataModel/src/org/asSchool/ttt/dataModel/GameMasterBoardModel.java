package org.asSchool.ttt.dataModel;

import java.util.List;

import org.asSchool.ttt.dataModel.ontology.AbstractPlayer;
import org.asSchool.ttt.dataModel.ontology.Game;

/**
 * The Class GameMasterBoardModel is managed by the game master agent and 
 * contains all active agents and games.
 *
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class GameMasterBoardModel {
	
	private Game gamePending;
	private  GameHashMap gameHashMap;
	private List<AbstractPlayer> listPlaingAgents;
	
	
	public List<AbstractPlayer> getListPlaingAgents() {
		return listPlaingAgents;
	}


	public void setListPlaingAgents(List<AbstractPlayer> listPlaingAgents) {
		this.listPlaingAgents = listPlaingAgents;
	}


	/**
	 * Return the HashMap of all running games.
	 * @return the game hash map
	 */
	public GameHashMap getGameHashMap() {
		if (gameHashMap==null) {	
			gameHashMap = new GameHashMap();
		}
		return gameHashMap;
	}

	
	/**
	 * Return the game pending that waits for another player.
	 * @return the game pending
	 */
	public Game getGamePending() {
		return gamePending;
	}
	/**
	 * Sets the game pending that waits for another player.
	 * @param gamePending the new game pending
	 */
	public void setGamePending(Game gamePending) {
		this.gamePending = gamePending;
	}

}
