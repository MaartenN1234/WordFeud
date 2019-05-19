package mn.wf.aiplay;

import mn.wf.core.ScorerResults;

public class AltPointsPerTilePlayer extends Player {
	public AltPointsPerTilePlayer(String stoneSet){
		super(stoneSet);
	}
	public int[] getMoveIndex(ScorerResults scores) {
		return new int[]{2,0};
	}

}
