package mn.wf.aiplay;

import java.util.Arrays;

import mn.wf.core.BoardScanner;
import mn.wf.core.ConsideredMove;
import mn.wf.core.ScorerResults;
import mn.wf.ui.PlayState;

public class MonteCarloSimulationEngine {
	private BoardScanner   bs;
	private PlayState      basePS;
	private ConsideredMove cm;

	public MonteCarloSimulationEngine(BoardScanner bs){
		this.bs = bs;
		basePS = bs.getPlayState();
	}
	public void setConsideredMove(ConsideredMove cm){
		this.cm = cm;
	}

	public float avgOpponentScore(ConsideredMove cm, String [] sampleStoneSets){
		int samples = sampleStoneSets.length;

		int sumScore = 0;
		for (int i = 0; i<samples; i++){
			PlayState ps = basePS.playWordOppTilesSample(cm);
			bs.setPlayState(ps);
			ScorerResults srs = bs.fullScanOpportunities();

			sumScore += srs.maxScores[0][0];
		}
		float scoreAvg = (float) sumScore / (float) samples;
		return scoreAvg;
	}

	public void test(){
		final int samples  = 100;
		int sumScore = 0;
		float scoreAvg ;
		int [][] locationsHotspots = new int[basePS.boardTiles.length][basePS.boardTiles[0].length];


		sumScore = 0;
		for (int i = 0; i<samples; i++){
			PlayState ps = basePS.playWordOwnTilesSample(cm);
			bs.setPlayState(ps);
			ScorerResults srs = bs.fullScanOpportunities();

			sumScore += srs.maxScores[0][0];
			for(int x = srs.moves[0][0].location[0]-1; x< srs.moves[0][0].location[2]; x++){
				for(int y = srs.moves[0][0].location[1]-1; y< srs.moves[0][0].location[3]; y++){
					locationsHotspots[y][x]++;
				}
			}

		}
		scoreAvg = (float) sumScore / (float) samples;

		System.out.println("Play Own score Average " +scoreAvg);
		for(int [] locationsHotspotsLine : locationsHotspots){
			System.out.println(Arrays.toString(locationsHotspotsLine));
		}



		for (int i = 0; i<samples; i++){
			PlayState ps = basePS.playPassSample();
			bs.setPlayState(ps);
			ScorerResults srs = bs.fullScanOpportunities();

			sumScore += srs.maxScores[0][0];
		}
		scoreAvg = (float) sumScore / (float) samples;


		System.out.println("Pass opponent score Average " +scoreAvg);

		sumScore = 0;
		for (int i = 0; i<samples; i++){
			PlayState ps = basePS.playWordOppTilesSample(cm);
			bs.setPlayState(ps);
			ScorerResults srs = bs.fullScanOpportunities();

			sumScore += srs.maxScores[0][0];
		}
		scoreAvg = (float) sumScore / (float) samples;

		System.out.println("Play opponent score Average " +scoreAvg);

	}
}
