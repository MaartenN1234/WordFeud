package mn.wf.core;

import java.util.Arrays;

public class ScorerResults {
	public float[][] maxScores;
	public float[][][] maxsScores;
	public int topSize;
	public ConsideredMove [][] moves;
	private static final int scoresCount = 3;
	private boolean empty;

	ScorerResults(){
		this(3);
		empty = true;
	}

	ScorerResults(int topSize){
		this.topSize = topSize;
		maxScores   = new float[scoresCount][topSize];
		maxsScores  = new float[scoresCount][topSize][scoresCount];
		moves       = new ConsideredMove[scoresCount][topSize];
	}

	private void updateRankWith(ConsideredMove move, float [] feeds, int i){
		empty = false;
		for (int j=0; j<topSize; j++){
			if (feeds[i] > maxScores[i][j]){
				for(int k=topSize-2;k>=j; k--){
					maxScores[i][k+1] = maxScores[i][k];
					maxsScores[i][k+1] = maxsScores[i][k];
					moves[i][k+1]     = moves[i][k];
				}
				maxScores[i][j] = feeds[i];
				maxsScores[i][j] = feeds;
				moves[i][j]      = move;
				j = topSize;
			}
		}
	}

	void updateRankWith(String s, float[] feeds){
		for (int i=0; i<scoresCount; i++){
			updateRankWith(new ConsideredMove(null, s), feeds, i);
		}
	}

	public String toString(boolean shortRepresentation){
		String s = "";
		if (shortRepresentation){
			for (int i=0; i<1 && i <scoresCount; i++){
				s +="{";
				for (int j=0; j<topSize; j++){
					if ( moves[i][j]!= null){
						s += moves[i][j] + ":"+ maxScores[i][j] +
								(j<topSize-1 ? ", ": "");
					}
				}
				s +="}"+
						(i<scoresCount-1 ? "\n": "");
			}
		} else {
			for (int i=0; i<1 && i <scoresCount; i++){
				s +="{Topscores, score: " +getScoreName(i)+"\n";
				int topLimit = (i==0) ? topSize : 3;
				for (int j=0; j<topLimit; j++){
					if ( moves[i][j]!= null){
						String vs = ""+ (maxScores[i][j]);
						s += moves[i][j] +"\t\t"+
								": "+ vs + "\t\t"+
								" sa:"+Arrays.toString(maxsScores[i][j]) +
								(j<topSize-1 ? "\n": "");
					}
				}
				s +="}"+
						(i<scoresCount-1 ? "\n": "");
			}
		}
		return s;
	}

	public String toString(){
		return toString(false);

	}

	void setLocation(boolean hOrient, int linenr,  int x, int y){
		int x1=0,x2=0,y1=0,y2=0;
		if (hOrient){
			x1 = x;      x2 = y;
			y1 = linenr; y2 = linenr;
		} else {
			x1 = linenr; x2 = linenr;
			y1 = x;      y2 = y;
		}

		int offset =1;
		x1 += offset;
		x2 += offset;
		y1 += offset;
		y2 += offset;

		for (int i=0; i<scoresCount; i++){
			for (int j=0; j<topSize; j++){
				if (moves[i][j] != null){
					moves[i][j] = new ConsideredMove(new int[]{x1,y1,x2,y2}, moves[i][j].word);
				}
			}
		}
	}

	void mergeWith(ScorerResults other){
		for (int i=0; i<scoresCount; i++){
			for (int j=0; j<other.topSize; j++){
				updateRankWith(other.moves[i][j], other.maxsScores[i][j], i);
			}
		}
	}

	private String getScoreName(int i){
		switch(i){
		case 0: return "Points for turn";
		case 1: return "Points per (played stone perceived value minus for bag average perceived value)";
		case 2: return "Points per played stone perceived value";
		case 3: return "Opponent opportunity maximum";
		}
		return "Unknown";
	}

	public boolean isEmpty() {
		return empty;
	}
}