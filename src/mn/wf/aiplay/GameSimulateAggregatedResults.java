package mn.wf.aiplay;

public class GameSimulateAggregatedResults {

	Player homePlayer;
	int homePlayerVictories;
	int homePlayerScoreTotal;
	Player awayPlayer;
	int awayPlayerVictories;
	int awayPlayerScoreTotal;

	public GameSimulateAggregatedResults(Player homePlayer,
			int homePlayerVictories, int homePlayerScoreTotal,
			Player awayPlayer, int awayPlayerVictories, int awayPlayerScoreTotal) {
		this.homePlayer           = homePlayer;
		this.homePlayerVictories  = homePlayerVictories;
		this.homePlayerScoreTotal = homePlayerScoreTotal;
		this.awayPlayer           = awayPlayer;
		this.awayPlayerVictories  = awayPlayerVictories;
		this.awayPlayerScoreTotal = awayPlayerScoreTotal;
	}
	public String toString(){
		String out = "";
		out += "HomePlayer\t\tVictories " +homePlayerVictories +"\t\tScoreTotal " +homePlayerScoreTotal+"\t\t["+homePlayer+"]\n";
		out += "AwayPlayer\t\tVictories " +awayPlayerVictories +"\t\tScoreTotal " +awayPlayerScoreTotal+"\t\t["+awayPlayer+"]\n";
		return out;
	}

}
