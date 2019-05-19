package mn.wf.core;

public class ConsideredMove {
	public final static int PLAY = 1;
	public final static int PASS = 2;
	public final static int SWAP = 3;

	public int moveType;
	public String word;
	public int[]  location;
	public String remainderTiles;

	public ConsideredMove(){
		this.moveType = PASS;
	}

	public ConsideredMove(String remainderTiles){
		this.moveType       = SWAP;
		this.remainderTiles = remainderTiles;
	}

	public ConsideredMove(int [] location, String word){
		if (location != null){
			int x = 1 + Math.abs(location[0] - location[2]);
			int y = 1 + Math.abs(location[1] - location[3]);
			if (x*y != word.length()){
				throw new RuntimeException("Illegal considered move, word length does not match location length.");
			}
		}

		this.moveType = PLAY;
		this.location = location;
		this.word     = word;
	}

	public String toString(){
		switch (moveType){
		case PLAY:
			if (location == null) {
				return "Play "+word;
			}
			return "Play "+word +" at (" + location[0]+","+location[1]+"):("+location[2]+","+location[3]+") ";
		case PASS:
			return "Pass turn";
		case SWAP:
			return "Swap, remaining tiles "+remainderTiles.length()+" tiles: "+remainderTiles;
		}
		return "Unknown move type: " +moveType;
	}

}
