package mn.wf.aiplay;

import java.util.Vector;

import mn.wf.core.BoardScanner;

public class ParallelGameEngine extends GameEngine {
	private final static int maxThreads = 8;
	private Vector<GameSimulateAggregatedResults> slaveResults;

	public ParallelGameEngine(BoardScanner bs) {
		super(bs);
	}
	public ParallelGameEngine(BoardScanner bs, Player homePlayer, Player awayPlayer){
		super(bs, homePlayer, awayPlayer);
	}

	public void fillResults(GameSimulateAggregatedResults results) {
		slaveResults.add(results);
	}

	public GameSimulateAggregatedResults simulateGames(int number){
		slaveResults = new Vector<GameSimulateAggregatedResults>();
		for(int i=0; i<maxThreads; i++){
			int j = number / maxThreads;
			j += ((number - (j*maxThreads)) > i) ? 1:0;
			ParallelGameEngineSlave pges = new ParallelGameEngineSlave(bs.getClone(), homePlayer.getClone(), awayPlayer.getClone(), j, this);
			Thread                  t    = new Thread(pges);
			t.start();
		}

		while(slaveResults.size() != maxThreads){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}


		int homePlayerScoreTotal = 0;
		int awayPlayerScoreTotal = 0;
		int homePlayerVictories = 0;
		int awayPlayerVictories = 0;
		for (GameSimulateAggregatedResults rs : slaveResults){
			homePlayerVictories  += rs.homePlayerVictories;
			awayPlayerVictories  += rs.awayPlayerVictories;
			homePlayerScoreTotal += rs.homePlayerScoreTotal;
			awayPlayerScoreTotal += rs.awayPlayerScoreTotal;
		}
		GameSimulateAggregatedResults out = new GameSimulateAggregatedResults(homePlayer, homePlayerVictories, homePlayerScoreTotal, awayPlayer, awayPlayerVictories, awayPlayerScoreTotal);
		return out;
	}


}
