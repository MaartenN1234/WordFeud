package mn.wf.aiplay;

import mn.wf.core.*;

public class PointsPerTilePlayer extends Player{

	private Scorer sc;

	public PointsPerTilePlayer(String stoneSet){
		super(stoneSet);
		sc = new Scorer();
	}
	public PointsPerTilePlayer(String stoneSet, Scorer sc){
		super(stoneSet);
		this.sc = sc;
	}
	public Scorer getExpectedScorer(){
		return sc;
	}
	public PointsPerTilePlayer evolvePlayer(){
		Scorer scn = sc.evolvedPercievedLetterValueStoreClone();
		return new PointsPerTilePlayer(stoneSet, scn);
	}

	public int[] getMoveIndex(ScorerResults scores) {
		return new int[]{1,0};
	}

	public String toString (){
		return super.toString() + "\t\t" +sc;
	}

}
