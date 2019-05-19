package mn.wf.aiplay;

import mn.wf.core.ConsideredMove;
import mn.wf.core.Scorer;
import mn.wf.core.ScorerResults;
import mn.wf.ui.PlayState;

public abstract class Player  implements Cloneable{
	private static final Scorer defaultScorer = new Scorer();
	protected String stoneSet;
	private   float    score;

	protected Player(String stoneSet){
		this.stoneSet = stoneSet;
		this.score    = 0;
	}

	public abstract int[] getMoveIndex(ScorerResults scores);
	protected  int[]  getMoveIndex(ScorerResults scores, PlayState ps){
		return getMoveIndex(scores);
	}
	private final  int[]  getMoveIndexCloner(ScorerResults scores, PlayState ps){
		return getMoveIndex(scores, ps.getClone());
	}
	protected ConsideredMove getDefaultMove(){
		return new ConsideredMove();
	}

	public final ConsideredMove getMove(ScorerResults scores, PlayState ps){
		if (scores.isEmpty())
			return getDefaultMove();
		int [] moveIx = getMoveIndexCloner(scores, ps);
		if (moveIx == null) return getDefaultMove();
		ConsideredMove out = scores.moves[moveIx[0]][moveIx[1]];
		if (out == null) return getDefaultMove();
		return out;
	}
	public final void addMoveScore(ScorerResults scores, PlayState ps) {
		int [] moveIx = getMoveIndexCloner(scores, ps);
		if (moveIx == null) return;
		score  += scores.maxsScores[moveIx[0]][moveIx[1]][0];
	}

	public final void addScore(float score){
		this.score += score;
	}
	public final float getScore(){
		return score;
	}
	public String getStones() {
		return stoneSet;
	}
	public String toString (){
		return this.getClass()+"";
	}
	public String toStringWithScore (){
		return this.getClass() + "   Score: "+score;
	}
	public void reset(String stoneSet) {
		score = 0;
		this.stoneSet = stoneSet;
	}

	public Scorer getScorer() {
		return defaultScorer;
	}
	public Player getClone(){
		try{
			return (Player) (clone());
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}


}
