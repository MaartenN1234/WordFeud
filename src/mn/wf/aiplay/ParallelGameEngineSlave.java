package mn.wf.aiplay;

import mn.wf.core.BoardScanner;

public class ParallelGameEngineSlave extends GameEngine implements Runnable {
	private ParallelGameEngine master;
	private int iterations;

	public ParallelGameEngineSlave(BoardScanner bs, Player homePlayer, Player awayPlayer, int iterations, ParallelGameEngine master){
		super(bs, homePlayer, awayPlayer);
		this.iterations = iterations;
		this.master     = master;
		resetGame();
	}


	public void run() {
		GameSimulateAggregatedResults results = simulateGames(iterations);
		master.fillResults(results);
	}

}
