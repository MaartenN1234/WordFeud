package mn.wf.aiplay;

import mn.wf.core.*;
import mn.wf.ui.PlayState;

public class OpponentAwarePlayer extends Player{
	public OpponentAwarePlayer(String stoneSet){
		super(stoneSet);
	}

	public int[] getMoveIndex(ScorerResults scores) {
		throw new RuntimeException("Assertion fail, method should not have been accessed");
	}

	public int[] getMoveIndex(ScorerResults scores, PlayState ps) {
		return null;
	}


}
