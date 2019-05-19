package mn.wf.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mn.wf.core.ConsideredMove;
import mn.wf.core.Scorer;
import mn.wf.core.TableAndIndexes.WordList;




public class PlayState implements Cloneable{
	public static char[][] emptyTiles = new char[][]{
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', '*', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}
	};

	/* n>=0 for letter values:
	   occupied, Lettervalue: A:1, Z:5, Joker(Z): 0
	   empty, normal -1
	   empty, DL     -2, TL -3
	   empty, DW    -12, TW -13 */

	public static int[][] emptyValuesStandard =  new int[][]{
		{ -3,  -1,  -1,  -1, -13,  -1,  -1,  -2,  -1,  -1, -13,  -1,  -1,  -1,  -3},
		{ -1,  -2,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -2,  -1},
		{ -1,  -1, -12,  -1,  -1,  -1,  -2,  -1,  -2,  -1,  -1,  -1, -12,  -1,  -1},
		{ -1,  -1,  -1,  -3,  -1,  -1,  -1, -12,  -1,  -1,  -1,  -3,  -1,  -1,  -1},
		{-13,  -1,  -1,  -1, -12,  -1,  -2,  -1,  -2,  -1, -12,  -1,  -1,  -1, -13},
		{ -1,  -3,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -3,  -1},
		{ -1,  -1,  -2,  -1,  -2,  -1,  -1,  -1,  -1,  -1,  -2,  -1,  -2,  -1,  -1},
		{ -2,  -1,  -1, -12,  -1,  -1,  -1,  -1,  -1,  -1,  -1, -12,  -1,  -1,  -2},
		{ -1,  -1,  -2,  -1,  -2,  -1,  -1,  -1,  -1,  -1,  -2,  -1,  -2,  -1,  -1},
		{ -1,  -3,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -3,  -1},
		{-13,  -1,  -1,  -1, -12,  -1,  -2,  -1,  -2,  -1, -12,  -1,  -1,  -1, -13},
		{ -1,  -1,  -1,  -3,  -1,  -1,  -1, -12,  -1,  -1,  -1,  -3,  -1,  -1,  -1},
		{ -1,  -1, -12,  -1,  -1,  -1,  -2,  -1,  -2,  -1,  -1,  -1, -12,  -1,  -1},
		{ -1,  -2,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -3,  -1,  -1,  -1,  -2,  -1},
		{ -3,  -1,  -1,  -1, -13,  -1,  -1,  -2,  -1,  -1, -13,  -1,  -1,  -1,  -3}
	};
	public static int[] letterCount = new int[]
			// 104 stones in total
			{ 7, 2, 2, 5,18, // A,B,C,D,E
		2, 3, 2, 4, 2, // F,G,H,I,J
		3, 3, 3,11, 6, // K,L,M,N,O
		2, 1, 5, 5, 5, // P,Q,R,S,T
		3, 2, 2, 1, 1, // U,V,W,X,Y
		2, 2};         // Z,?

	public static final int BORD_WIDTH           = emptyTiles[0].length;
	public static final int BORD_HEIGHT          = emptyTiles.length;

	public static PlayState initFor(String id){
		switch(id){
		case "ML":
			return new PlayState(
					"UUMBNZD",
					new char[][] {
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'Q'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'G', 'A'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'J', 'E', 'T'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'M', 'E', 'N', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'V', 'A', 'N', 'E', 'N'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'P', 'A', 'F', ' ', 'T', 'I'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'K', 'E', 'T', ' ', ' ', ' ', 'C'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', 'J', 'A', 'P', 'E', 'N', ' ', 'E', 'H'},
							{' ', ' ', ' ', ' ', ' ', ' ', 'I', 'E', 'L', ' ', 'N', 'O', 'E', 'N', 'E'},
							{' ', ' ', ' ', ' ', ' ', 'M', 'A', 'N', 'E', 'N', ' ', 'D', ' ', 'D', ' '},
							{' ', ' ', ' ', ' ', 'L', 'A', ' ', ' ', 'R', 'O', 'K', 'E', ' ', ' ', 'Z'},
							{' ', ' ', ' ', 'Y', 'O', ' ', ' ', ' ', ' ', ' ', 'R', ' ', 'W', ' ', 'A'},
							{' ', ' ', 'C', 'O', 'S', ' ', ' ', ' ', 'H', 'E', 'E', ' ', 'E', 'N', 'G'},
							{' ', 'T', 'E', ' ', 'E', 'B', ' ', 'F', 'A', 'X', 'E', 'N', 'D', ' ', 'E'},
							{'V', 'U', 'S', ' ', 'R', 'I', ' ', 'A', 'D', ' ', 'K', ' ', 'T', 'O', 'R'}
					});
		case "EM":
			return new PlayState(
					"UADSCWE",
					new char[][] {
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'O', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'M', 'O', 'P', ' ', ' '},
							{' ', 'Z', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'M', 'I', ' ', ' '},
							{' ', 'I', 'D', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'K', 'S', ' ', ' '},
							{'H', 'E', 'U', 'G', ' ', ' ', ' ', ' ', ' ', 'B', 'Y', 'E', ' ', ' ', ' '},
							{' ', ' ', 'H', 'A', 'N', 'D', 'S', ' ', ' ', ' ', ' ', 'r', ' ', ' ', ' '},
							{' ', 'T', ' ', 'V', ' ', ' ', ' ', ' ', ' ', ' ', 'G', 'E', ' ', ' ', ' '},
							{'R', 'A', 'T', 'E', 'N', ' ', ' ', 'R', 'A', 'C', 'E', 'N', ' ', ' ', ' '},
							{'O', 'F', ' ', ' ', 'E', ' ', ' ', ' ', ' ', ' ', 'L', ' ', ' ', ' ', ' '},
							{'K', ' ', ' ', 'M', 'E', ' ', 'J', ' ', ' ', ' ', 'E', ' ', ' ', 'V', ' '},
							{'E', ' ', 'K', 'A', 'R', 'T', 'E', 'L', ' ', ' ', 'G', 'E', 'N', 'E', 'N'},
							{'R', ' ', ' ', 'X', ' ', ' ', 'N', 'O', ' ', ' ', 'D', ' ', ' ', 'S', ' '},
							{'E', ' ', 'W', 'I', 'N', ' ', 'T', 'O', 'Q', 'U', 'E', ' ', ' ', 'T', ' '},
							{'N', ' ', ' ', ' ', 'A', ' ', ' ', 'S', ' ', ' ', ' ', ' ', ' ', 'E', ' '},
							{'D', ' ', ' ', ' ', 'R', ' ', ' ', ' ', 'B', 'O', 'l', 'L', 'E', 'N', ' '}
					});
		case "RU":
			return new PlayState(
					"?NNEEAR",
					new char[][] {
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', 'C', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', 'O', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', 'A', ' ', ' ', ' ', 'E', 'S', ' ', ' '},
							{' ', ' ', ' ', ' ', 'V', 'E', 'S', 'T', ' ', ' ', 'M', 'E', 'N', 'G', 'T'},
							{' ', 'J', 'U', 'R', 'A', ' ', ' ', ' ', 'P', 'I', 'A', 'N', 'O', ' ', ' '},
							{' ', ' ', ' ', ' ', 'Z', ' ', ' ', ' ', 'U', ' ', 'A', ' ', 'D', ' ', ' '},
							{' ', ' ', ' ', 'W', 'E', 'D', ' ', 'D', 'I', 'K', 'S', 'T', 'E', ' ', 'N'},
							{' ', ' ', 'H', 'E', 'N', ' ', ' ', 'O', 'N', ' ', ' ', ' ', ' ', ' ', 'E'},
							{' ', 'W', 'E', 'I', ' ', ' ', ' ', 'R', ' ', ' ', ' ', 'B', ' ', 'J', 'E'},
							{'G', 'O', 'U', 'D', ' ', ' ', ' ', 'S', ' ', ' ', 'G', 'E', 'M', 'E', 'P'},
							{' ', 'K', ' ', 'E', 'B', ' ', 'Z', 'E', ' ', ' ', 'O', 'F', ' ', ' ', ' '},
							{' ', 'T', ' ', ' ', 'A', ' ', 'I', 'N', ' ', ' ', 'N', ' ', ' ', ' ', ' '},
							{'M', 'E', ' ', 'V', 'L', 'O', 'N', 'd', 'E', 'R', 'S', ' ', ' ', ' ', ' '},
							{'E', 'N', ' ', ' ', 'K', ' ', ' ', ' ', ' ', ' ', 'T', ' ', ' ', ' ', ' '}
					});
		case "SH":
			return new PlayState(
					"PSV",
					new char[][] {
							{' ', ' ', ' ', ' ', 'M', 'E', 'E', 't', 'B', 'A', 'R', 'E', 'N', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', 'G', ' ', ' ', ' ', ' ', 'O', ' ', 'A', ' ', ' '},
							{' ', ' ', ' ', ' ', 'Q', ' ', ' ', ' ', 'Z', 'O', 'N', 'E', 'T', ' ', ' '},
							{' ', 'C', ' ', ' ', 'U', ' ', ' ', ' ', ' ', ' ', 'D', ' ', 'I', ' ', ' '},
							{'T', 'R', 'E', 'D', 'E', 'N', ' ', ' ', 'Z', 'O', 'E', 'N', 'E', 'N', ' '},
							{' ', 'I', ' ', ' ', 'N', 'U', ' ', ' ', 'E', 'H', ' ', ' ', ' ', 'E', 'N'},
							{' ', 'S', ' ', ' ', 'A', ' ', ' ', ' ', 'L', ' ', ' ', ' ', ' ', 'Y', 'O'},
							{' ', 'E', ' ', 'G', ' ', 'W', 'E', 'E', 'F', ' ', ' ', 'L', ' ', ' ', 'K'},
							{' ', 'S', 'T', 'U', 'R', 'E', 'N', ' ', ' ', ' ', 'J', 'A', 'B', ' ', 'T'},
							{' ', ' ', ' ', 'P', 'I', 'L', 'S', ' ', ' ', ' ', ' ', 'K', 'O', 'M', 'E'},
							{' ', ' ', ' ', ' ', 'F', ' ', ' ', 'D', ' ', ' ', 'J', ' ', 'M', 'I', 'N'},
							{' ', ' ', ' ', ' ', ' ', ' ', 'V', 'E', 'R', 'G', 'O', 'D', 'E', 'N', ' '},
							{' ', ' ', ' ', ' ', ' ', 'C', 'A', 'S', ' ', ' ', 'H', 'A', 'R', 'K', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'T', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'W', 'A', 'X', 'E', ' '}
					});
		case "2D":
			return new PlayState(
					"NZ?TAON",
					new char[][]{
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
							{' ', ' ', 'W', 'E', 'S', 'P', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', 'I', 'N', 'W', 'O', 'N', ' ', ' ', 'S', ' ', ' '},
							{' ', ' ', ' ', 'Y', ' ', ' ', ' ', 'A', ' ', ' ', 'H', 'E', 'M', 'D', 'E'},
							{' ', ' ', 'K', 'E', 'T', 'T', 'E', 'R', 'S', ' ', 'U', ' ', 'E', ' ', 'X'},
							{' ', 'J', 'U', 'N', 'I', ' ', ' ', 'R', ' ', ' ', 'I', ' ', 'E', 'N', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', 'L', 'I', 'D', ' ', 'V', ' ', 'K', 'A', 'L'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', 'G', 'E', 'P', 'E', 'L', ' ', 'T', 'E'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', 'E', ' ', ' ', 'R', ' ', ' ', ' ', 'N'},
							{' ', ' ', ' ', ' ', ' ', 'M', ' ', ' ', ' ', ' ', 'D', ' ', ' ', ' ', 'G'},
							{' ', ' ', ' ', ' ', ' ', 'A', ' ', ' ', ' ', 'N', 'E', 'R', 'F', ' ', 'E'},
							{' ', ' ', ' ', ' ', 'R', 'A', 'C', 'E', ' ', ' ', ' ', ' ', 'O', ' ', ' '},
							{' ', ' ', ' ', ' ', 'E', 'N', ' ', 'G', 'E', 'L', 'A', 'K', 'T', 'E', 'N'},
							{' ', 'H', 'O', 'O', 'F', 'D', ' ', ' ', ' ', ' ', ' ', ' ', 'O', ' ', ' '},
							{' ', ' ', ' ', ' ', 'S', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}
					}
					);
		case "MAX":
			// play 		ZELFZUCHTIGHEID
			return new PlayState(
					"ZZCUTGD",
					new char[][]{
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'W'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'V', ' ', ' ', ' ', 'E'},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'A', ' ', ' ', ' ', 'G'},
							{' ', ' ', ' ', ' ', 'S', ' ', ' ', ' ', ' ', ' ', 'S', ' ', ' ', ' ', 'C'},
							{' ', ' ', ' ', ' ', 'P', ' ', ' ', ' ', ' ', ' ', 'T', ' ', ' ', ' ', 'I'},
							{'B', ' ', ' ', ' ', 'L', ' ', ' ', ' ', ' ', ' ', 'G', ' ', ' ', ' ', 'J'},
							{'A', 'M', 'S', 'O', 'I', ' ', ' ', 'R', 'O', 'R', 'O', ' ', 'M', ' ', 'F'},
							{'B', ' ', 'T', ' ', 'J', 'A', 'N', 'U', 'S', ' ', 'E', 'D', 'E', 'L', 'E'},
							{'Y', ' ', 'O', ' ', 'T', ' ', ' ', 'S', ' ', ' ', 'D', ' ', 'T', ' ', 'R'},
							{' ', 'E', 'L', 'F', ' ', ' ', ' ', 'H', ' ', 'I', ' ', 'H', 'E', 'I', ' '},
							{'A', ' ', 'D', ' ', 'W', ' ', ' ', ' ', ' ', 'E', 'R', ' ', 'E', ' ', 'E'},
							{'A', 'R', 'E', 'N', 'A', ' ', ' ', ' ', ' ', ' ', 'O', 'E', 'N', 'E', 'N'},
							{'K', ' ', ' ', ' ', 'M', ' ', ' ', ' ', ' ', ' ', 'E', ' ', ' ', ' ', ' '},
							{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'P', ' ', ' ', ' ', ' '}
					}
					);
		}
		return new PlayState("");
	}



	public String   availableStones;
	public char[][] boardTiles;
	public int[][]  boardValues;

	private Scorer sc ;
	private PlayState rollback;

	private PlayState(){
		sc = new Scorer();
		this.boardTiles = emptyTiles;
	}

	public PlayState(String availableStones){
		this();
		this.availableStones = availableStones.toUpperCase();
		initBoardFromTiles(emptyValuesStandard);
	}

	public PlayState(String availableStones, char[][] tiles){
		this();
		this.boardTiles = tiles;
		this.availableStones = availableStones.toUpperCase();
		initBoardFromTiles(emptyValuesStandard);
	}

	public PlayState(String availableStones, char[][] tiles, int[][] emptyValuesRandom){
		this();
		this.boardTiles      = tiles;
		this.availableStones = availableStones.toUpperCase();
		initBoardFromTiles(emptyValuesRandom);
	}

	private PlayState(String availableStones, char[][] boardTiles, int[][] boardValues, PlayState source){
		this();
		this.availableStones = availableStones.toUpperCase();
		this.boardTiles      = boardTiles;
		this.boardValues     = boardValues;
		this.rollback        = source;
	}

	// Playing and Rollback methods
	public PlayState rollback(){
		return rollback;
	}
	public PlayState playSwapTilesSample(String availableStonesRemainder){
		return new PlayState(
				getOwnStonesSample(availableStonesRemainder),
				boardTiles,
				boardValues,
				this);
	}
	public PlayState playPassSample(){
		return new PlayState(
				getOppStonesSample(),
				boardTiles,
				boardValues,
				this);
	}
	public PlayState playWord(ConsideredMove cm){
		char[][] boardTilesNew  = new char[boardTiles.length][boardTiles[0].length];
		int[][]  boardValuesNew = new int[boardTiles.length][boardTiles[0].length];
		for (int i=0; i< boardTilesNew.length; i++){
			for (int j=0; j< boardTilesNew[i].length; j++){
				boardTilesNew[i][j] = boardTiles[i][j];
				boardValuesNew[i][j] = boardValues[i][j];
			}
		}
		int ix = 0;
		for (int i=cm.location[1]-1; i<cm.location[3]; i++){
			for (int j=cm.location[0]-1; j<cm.location[2]; j++){
				char c  = cm.word.charAt(ix++);
				char cu = Character.toUpperCase(c);
				if (boardTilesNew[i][j] == ' ' ){
					boardTilesNew[i][j]  = cu;
					boardValuesNew[i][j] = (cu == c) ? sc.getLetterValue(c) : sc.getLetterValue('?');
				}
			}
		}
		// Determine remainderTokens
		char[] remainderTokens   = this.availableStones.toCharArray();
		ix = 0;
		for (int i=cm.location[1]-1; i<cm.location[3]; i++){
			for (int j=cm.location[0]-1; j<cm.location[2]; j++){
				char c  = cm.word.charAt(ix++);
				char cu = Character.toUpperCase(c);
				if (boardTiles[i][j] == ' ' ){
					char playedToken = ((cu == c) ? c : '?');
					for (int k=0; k<remainderTokens.length; k++){
						if (remainderTokens[k] == playedToken){
							remainderTokens[k] = ' ';
							break;
						}
					}
				}
			}
		}
		return new PlayState(
				new String(remainderTokens).replace(" ", ""),
				boardTilesNew,
				boardValuesNew,
				this);
	}
	public PlayState playWordOwnTilesSample(ConsideredMove cm){
		PlayState  out = playWord(cm);
		out = out.playSwapTilesSample(out.availableStones.replace(" ", ""));
		out.rollback   = this;
		return out;
	}
	public PlayState playWordOppTilesSample(ConsideredMove cm){
		PlayState  out = playWordOwnTilesSample(cm);
		out = out.playPassSample();
		out.rollback   = this;
		return out;
	}

	public PlayState playMove(ConsideredMove move) {
		PlayState ps;

		switch(move.moveType){
		case ConsideredMove.PASS:
			return playPassSample();
		case ConsideredMove.SWAP:
			ps = playPassSample();
			ps.availableStones = move.remainderTiles;
			return ps;
		case ConsideredMove.PLAY:
			return playWord(move);
		}
		return null;
	}

	// privates Playing and Rollback methods
	private String getOwnStonesSample(String availableStonesRemainder){
		String s = getRemainderStones();
		if (s.length() < 7){
			return availableStonesRemainder;
		}
		int maxGet1 = s.length() - 7;
		int maxGet2 = 7- availableStonesRemainder.length();
		int maxGet  = maxGet1 < maxGet2 ? maxGet1 : maxGet2;

		ArrayList<Character> cs = new ArrayList<Character>();
		for (char c: s.toCharArray()){
			cs.add(c);
		}
		Collections.shuffle(cs);
		List <Character> cs2 = cs.subList(0, maxGet);
		s = availableStonesRemainder;
		for (Character c: (cs2.toArray(new Character [0]))){
			s += c;
		}
		return s;
	}
	private String getOppStonesSample(){
		String s = getRemainderStones();
		if (s.length() < 7){
			return s;
		}
		ArrayList<Character> cs = new ArrayList<Character>();
		for (char c: s.toCharArray()){
			cs.add(c);
		}
		Collections.shuffle(cs);
		List <Character> cs2 = cs.subList(0, 7);
		s = "";
		for (Character c: (cs2.toArray(new Character [0]))){
			s += c;
		}
		return s;
	}


	public String toString(){
		String s = "";
		s += "Available stones: "+ availableStones + "\n";
		for (int i=0; i< boardTiles.length; i++)
			s += "  "+Arrays.toString(boardTiles[i]) + "\n";

		return s;
	}



	public String getRemainderStones(){
		String s = "";
		int[] availCount = Arrays.copyOf(letterCount, 27);
		for (char c : availableStones.toCharArray()){
			if (c=='?') {
				availCount[26]--;
			} else {
				availCount[WordList.getInternalCharIndex(c)]--;
			}
		}
		for (int i=0; i<BORD_WIDTH; i++)
			for (int j=0; j<BORD_HEIGHT; j++){
				if (boardValues[i][j] == 0) {
					availCount[26]--;
				} else if (boardTiles[i][j] != ' '){
					availCount[WordList.getInternalCharIndex(boardTiles[i][j])]--;
				}
			}

		for (int i=0; i<27; i++){
			while (availCount[i] > 0){
				if (i==26){
					s += "?";
				} else {
					s += WordList.convertCharIndex(i);
				}
				availCount[i]--;
			}
		}


		return s;
	}

	private void initBoardFromTiles(int[][] emptyValues){
		if(boardTiles[7][7] == '*'){
			boardTiles[7][7] = ' ';
		}

		// Starts blank, the algorithm does the rest
		boardValues = emptyValues;

		for (int i=0; i<BORD_HEIGHT; i++)
			for (int j=0; j<BORD_WIDTH; j++){
				if (boardTiles[i][j] != ' ' && boardTiles[i][j] != '*'){
					char c = Character.toUpperCase(boardTiles[i][j]);
					if (boardTiles[i][j] == c) {
						boardValues [i][j] = sc.getLetterValue(c);
					} else {
						// joker
						boardValues [i][j] = 0;
						boardTiles[i][j]   = c;
					}
				}
			}
	}

	public PlayState getClone() {
		try {
			return (PlayState) (clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
