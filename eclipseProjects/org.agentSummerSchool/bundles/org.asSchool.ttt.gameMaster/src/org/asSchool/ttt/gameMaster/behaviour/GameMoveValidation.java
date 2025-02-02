package org.asSchool.ttt.gameMaster.behaviour;

import java.util.List;

import org.asSchool.ttt.dataModel.GameHashMap;
import org.asSchool.ttt.dataModel.GameWrapper;
import org.asSchool.ttt.dataModel.ontology.AbstractMarkType;
import org.asSchool.ttt.dataModel.ontology.AbstractPlayer;
import org.asSchool.ttt.dataModel.ontology.Circle;
import org.asSchool.ttt.dataModel.ontology.Cross;
import org.asSchool.ttt.dataModel.ontology.Game;
import org.asSchool.ttt.dataModel.ontology.GameAction;
import org.asSchool.ttt.dataModel.ontology.GameLost;
import org.asSchool.ttt.dataModel.ontology.GameMove;
import org.asSchool.ttt.dataModel.ontology.GameRemis;
import org.asSchool.ttt.dataModel.ontology.GameResult;
import org.asSchool.ttt.dataModel.ontology.GameWon;
import org.asSchool.ttt.dataModel.ontology.TicTacToeOntology;
import org.asSchool.ttt.gameMaster.GameMasterAgent;

import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


/**
 * The Class GameMoveValidation.
 * @author Christian Derksen - SOFTEC - ICB - University of Duisburg-Essen
 */
public class GameMoveValidation extends OneShotBehaviour {

	private static final long serialVersionUID = -7222793667703958346L;
	
	private GameMasterAgent gameMasterAgent;
	private GameAction gameAction;
	
	
	/**
	 * Instantiates a new game move validation.
	 *
	 * @param gameMasterAgent the game master agent
	 * @param gameAction the put game field
	 */
	public GameMoveValidation(GameMasterAgent gameMasterAgent, GameAction gameAction) {
		super(gameMasterAgent);
		this.gameMasterAgent = gameMasterAgent;
		this.gameAction = gameAction;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.Behaviour#action()
	 */
	@Override
	public void action() {
		 
		GameHashMap gameHashMap = this.gameMasterAgent.getGameMasterBoardModel().getGameHashMap();
		
		Game newGame = this.gameAction.getGame();
		Game oldGame = gameHashMap.get(newGame.getGameID());
		
		GameWrapper gameWrapperNew = new GameWrapper(newGame);
		
		AbstractMarkType[][] newGameBoardArray = newGame!=null ? GameWrapper.transformToMarkArray(newGame.getGameBoard()) : null;
		AbstractMarkType[][] oldGameBoardArray = oldGame!=null ? GameWrapper.transformToMarkArray(oldGame.getGameBoard()) : null;
		
		boolean isCross = false;
		
		GameMove gm = new GameMove();
		int newMarkCnt = 0; // counts the amount of new marks. It should be one, otherwise the GameMove wasn´t valid.
		boolean isCorrectNewGameMove = true;

		if (oldGameBoardArray!=null) {
			// Check if the new Move is valid (X remains X, O remains O and one previously
			// empty field has to be filled by a mark)
			outerForLoop:
			for (int col = 0; col<3; col++) {
				for (int row = 0; row<3; row++) {
					if (oldGameBoardArray[row][col] instanceof Cross) {
						// ---
						if (!(newGameBoardArray[row][col] instanceof Cross)) {
							isCorrectNewGameMove = false;
							break outerForLoop;
						}
						
					} else if (oldGameBoardArray[row][col] instanceof Circle) {
						// ---
						if (!(newGameBoardArray[row][col] instanceof Circle)) {
							isCorrectNewGameMove = false;
							break outerForLoop;
						}
						
					} else if (oldGameBoardArray[row][col] == null) {
						// ---
						if (newGameBoardArray[row][col] != null) {
							newMarkCnt++;
							if (newGameBoardArray[row][col] instanceof Cross) {
								isCross = true;
								gm.setGameColumn(col);
								gm.setGameRow(row);
								gm.setGameID(newGame.getGameID());
								gm.setMarkType(new Cross());
							}
							if (newGameBoardArray[row][col] instanceof Circle) {
								isCross = false;
								gm.setGameColumn(col);
								gm.setGameRow(row);
								gm.setGameID(newGame.getGameID());
								gm.setMarkType(new Circle());
							}
						}
					}
					
				}
			}
		}
		
		
		if (isCorrectNewGameMove==true && newMarkCnt == 1) {
			// Add new GameBoard to GameList
			gameHashMap.put(newGame.getGameID(), newGame);
			
			switch (gameWrapperNew.getGameState()) {
			case InitialState:
				// --- Should never happen ---
				if (isCross) {
					this.myAgent.addBehaviour(new SendGameMoveToPlayer(newGame, newGame.getOMarkPlayer()));
				} else {
					this.myAgent.addBehaviour(new SendGameMoveToPlayer(newGame, newGame.getXMarkPlayer()));
				}
				break;
				
			case InProgress: // Send the new GameBoard to the NextPlayer (dependent of the previous MarkType)
				if (isCross) {
					this.myAgent.addBehaviour(new SendGameMoveToPlayer(newGame, newGame.getOMarkPlayer()));
				} else {
					this.myAgent.addBehaviour(new SendGameMoveToPlayer(newGame, newGame.getXMarkPlayer()));
				}
				break;

			case FinalizedRemis: // Send information about result to players +++ end game, delete game in
			case FinalizedWon: // Send information about result to players +++ end game, delete game in
				this.sendGameResult(gameWrapperNew, true);
				gameHashMap.remove(newGame.getGameID());
				break;
				
			}
			
		} else {
			
			System.out.println("Wrong Game Move");
			gameWrapperNew.setWrongGameMove(true);
			this.sendGameResult(gameWrapperNew, false);
			gameHashMap.remove(newGame.getGameID());
		}
		
	}
	
	/**
	 * Send the game result to all player.
	 * @param gameWrapper the game wrapper
	 */
	private void sendGameResult(GameWrapper gameWrapper, boolean correctGame) {
		
		AbstractPlayer winner = gameWrapper.getWinner();
		AbstractPlayer loser  = gameWrapper.getLoser();
		
		if (winner==null && loser==null && correctGame == true) {
			// --- Create remis instance ---------------------------- 
			GameRemis gameRemis = new GameRemis();
			gameRemis.setGame(gameWrapper.getGame());
			
			this.sendGameResult(gameWrapper.getGame().getXMarkPlayer().getAid(), gameRemis);
			this.sendGameResult(gameWrapper.getGame().getOMarkPlayer().getAid(), gameRemis);
			this.gameMasterAgent.printToUiConsole("Remis in game between " + gameWrapper.getGame().getXMarkPlayer().getAid().getName() + " (X) and " + gameWrapper.getGame().getOMarkPlayer().getAid().getName() + " (O)", false);
			
		} else if (correctGame == true){
			// --- Create winner loser instance ---------------------
			GameWon gameWon = new GameWon();
			gameWon.setGame(gameWrapper.getGame());
			this.sendGameResult(winner.getAid(), gameWon);
			
			GameLost gameLost = new GameLost();
			gameLost.setGame(gameWrapper.getGame());
			this.sendGameResult(loser.getAid(), gameLost);
			winner.setScore(winner.getScore()+1);
			
			List<AbstractPlayer> agentList = this.gameMasterAgent.getGameMasterBoardModel().getListPlayingAgents();
			AbstractPlayer oldAbstractPlayer;
			if (agentList != null) {
				for (int i = 0; i < agentList.size(); i++) {
					oldAbstractPlayer = this.gameMasterAgent.getGameMasterBoardModel().getListPlayingAgents().get(i);
					if (oldAbstractPlayer.getAid().equals(winner.getAid())){
						this.gameMasterAgent.getGameMasterBoardModel().getListPlayingAgents().get(i).setScore(oldAbstractPlayer.getScore()+1);
					}
				}
						
			} 
			
			this.gameMasterAgent.printToUiConsole("Game was won by " + winner.getAid().getName() + " (against  " + loser.getAid().getName() + ")", false);
			
			
		} else {
			
			GameRemis gameRemis = new GameRemis();
			gameRemis.setGame(gameWrapper.getGame());
			
			this.sendGameResult(gameWrapper.getGame().getXMarkPlayer().getAid(), gameRemis);
			this.sendGameResult(gameWrapper.getGame().getOMarkPlayer().getAid(), gameRemis);
			this.gameMasterAgent.printToUiConsole("Wrong Game Move, Play new Game ", false);
			
		}
		
		this.gameMasterAgent.updateUI();
	}
	
	/**
	 * Sends the specified game result to the agent.
	 *
	 * @param receiver the receiver
	 * @param gameResult the game result
	 */
	private void sendGameResult(AID receiver, GameResult gameResult) {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM); 
		msg.addReceiver(receiver);
		msg.setLanguage(new SLCodec().getName());
		msg.setOntology(TicTacToeOntology.getInstance().getName());
		
		Action agentAction = new Action();
		agentAction.setActor(this.myAgent.getAID());	
		agentAction.setAction(gameResult);
		
		try {
			this.myAgent.getContentManager().fillContent(msg, agentAction);
			this.myAgent.send(msg);
			
		} catch (CodecException | OntologyException e) {
			System.err.println(this.myAgent.getLocalName() + " - Error: " + e.getMessage());
		}
	}
	
}
