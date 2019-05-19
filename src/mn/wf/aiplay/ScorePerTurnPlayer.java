package mn.wf.aiplay;

import mn.wf.core.*;

public class ScorePerTurnPlayer extends Player{
	public ScorePerTurnPlayer(String stoneSet){
		super(stoneSet);
	}

	public int[] getMoveIndex(ScorerResults scores) {
		return new int[]{0,0};
	}


}
