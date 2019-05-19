package mn.wf.aiplay;

import mn.wf.core.BoardScanner;
import mn.wf.core.ConsideredMove;
import mn.wf.core.ScorerResults;
import mn.wf.ui.PlayState;

public class GameEngine {
	private final static int TILES_PER_PLAYER = 7;
	private final static String DEFAULT_TILES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(0, TILES_PER_PLAYER);
	private static final boolean DEBUG_OUTPUT_SHORT = false;
	private static final boolean DEBUG_OUTPUT = false;


	protected Player homePlayer;
	protected Player awayPlayer;
	private PlayState ps;
	private BagTiles bt;
	private boolean homePlayersTurn;
	protected BoardScanner bs;


	public GameEngine(BoardScanner bs){
		this.bs = bs;
		homePlayer = new ScorePerTurnPlayer(DEFAULT_TILES);
		awayPlayer = new PassPlayer(DEFAULT_TILES);
		resetGame();
	}
	public GameEngine(BoardScanner bs, Player homePlayer, Player awayPlayer){
		this.bs = bs;
		setPlayers(homePlayer, awayPlayer);
		resetGame();
	}

	public void setPlayers (Player homePlayer, Player awayPlayer){
		this.homePlayer = homePlayer;
		this.awayPlayer = awayPlayer;
	}
	public void resetGame(){
		bt = new BagTiles();
		homePlayer.reset(bt.pickTiles(TILES_PER_PLAYER));
		awayPlayer.reset(bt.pickTiles(TILES_PER_PLAYER));
		homePlayersTurn = true;
		ps = new PlayState(homePlayer.getStones());
		if (DEBUG_OUTPUT){
			System.out.println(homePlayer.getStones().length() +", " + awayPlayer.getStones().length());
		}
	}

	public boolean playMove(){
		Player actor = homePlayersTurn ? homePlayer : awayPlayer;

		ps.availableStones = actor.getStones();
		bs.setPlayState(ps);
		bs.setScorer(actor.getScorer());
		if (DEBUG_OUTPUT) System.out.println((homePlayersTurn ? "Home" : "Away") + " player's turn.   Tileset "+ ps.availableStones);

		ScorerResults scores = bs.fullScanOpportunities();
		ConsideredMove move  = actor.getMove(scores, ps);
		actor.addMoveScore(scores, ps);

		if (DEBUG_OUTPUT) System.out.println("Move "+ move);

		ps = ps.playMove(move);
		String newAvailableStone = ps.availableStones +
				bt.pickTiles(TILES_PER_PLAYER-ps.availableStones.length());
		actor.stoneSet = newAvailableStone;

		homePlayersTurn = !homePlayersTurn;

		if (DEBUG_OUTPUT) System.out.println(actor);
		return move.moveType == ConsideredMove.PLAY;
	}
	public void playGame(){
		boolean finished = false;
		int passesCount = 0;

		while(!finished){
			if(playMove()){
				passesCount = 0;
			} else {
				passesCount++;
			}

			finished = (homePlayer.getStones().length() == 0 ||
					awayPlayer.getStones().length() == 0 ||
					passesCount >= 6);
			if (DEBUG_OUTPUT && finished){
				System.out.println(homePlayer.getStones().length());
				System.out.println(awayPlayer.getStones().length());
				System.out.println(passesCount);
			}
		}
		if (DEBUG_OUTPUT_SHORT) System.out.println(toStringGameStatus());
	}
	public GameSimulateAggregatedResults simulateGames(int number){
		int homePlayerScoreTotal = 0;
		int awayPlayerScoreTotal = 0;
		int homePlayerVictories = 0;
		int awayPlayerVictories = 0;

		for(int i=0; i<number; i++){
			resetGame();
			playGame();
			homePlayerScoreTotal += homePlayer.getScore();
			awayPlayerScoreTotal += awayPlayer.getScore();
			homePlayerVictories  += (homePlayer.getScore() > awayPlayer.getScore() ? 1 : 0);
			awayPlayerVictories  += (homePlayer.getScore() < awayPlayer.getScore() ? 1 : 0);
		}

		GameSimulateAggregatedResults out = new GameSimulateAggregatedResults(homePlayer, homePlayerVictories, homePlayerScoreTotal, awayPlayer, awayPlayerVictories, awayPlayerScoreTotal);
		return out;
	}

	public String toStringGameStatus(){
		return ps + "\n" +
				"Home Player " + homePlayer + "\n" +
				"Away Player " + awayPlayer;
	}
}
