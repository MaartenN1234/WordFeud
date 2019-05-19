package mn.wf.aiplay;

public class SimAnnealing {
	GameEngine ge;
	PointsPerTilePlayer     current;


	public SimAnnealing(GameEngine ge){
		this.ge = ge;
		current = new PointsPerTilePlayer("ABCDEFG");
	}


	public boolean iterate(int simPlayCount){
		GameSimulateAggregatedResults res;
		PointsPerTilePlayer considered = current.evolvePlayer();
		int currentPlayerVictories    = 0;
		int consideredPlayerVictories = 0;

		ge.setPlayers(current, considered);
		res = ge.simulateGames(simPlayCount);
		currentPlayerVictories    += res.homePlayerVictories;
		consideredPlayerVictories += res.awayPlayerVictories;
		System.out.println(res);

		// swap beginning player
		ge.setPlayers(considered, current);
		res = ge.simulateGames(simPlayCount);
		currentPlayerVictories    += res.awayPlayerVictories;
		consideredPlayerVictories += res.homePlayerVictories;
		System.out.println(res);

		if ((currentPlayerVictories*1.1) < consideredPlayerVictories){
			System.out.println("Mutated: "+ currentPlayerVictories +" wins against " +consideredPlayerVictories);
			current = considered;
			System.out.println("New Player: " + current);
			return true;
		} else {
			System.out.println("No mutation: "+ currentPlayerVictories +" wins against " +consideredPlayerVictories);
		}
		return false;
	}
	public void optimize(int iterations){
		for (int i=0; i<iterations;i++){
			iterate(100+i/4);
		}
	}
}
