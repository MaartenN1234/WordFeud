package mn.wf.core;
import java.util.ArrayList;
import java.util.Arrays;

import mn.wf.ui.PlayState;

public class BoardScanner implements Cloneable{
	private static final int NORMAL_EMPTY         = -1;
	private static final int DOUBLE_LETTER_MARKER = -2;
	private static final int TRIPLE_LETTER_MARKER = -3;
	private static final int DOUBLE_WORD_MARKER   = -12;
	private static final int TRIPLE_WORD_MARKER   = -13;

	private static final int SCORES_TOP_SIZE_LOCATION = 5;
	private static final int SCORES_TOP_SIZE_SCANLINE = 10;
	private static final int SCORES_TOP_SIZE_BOARD = 100;

	public static final int BORD_WIDTH           = PlayState.BORD_WIDTH;
	public static final int BORD_HEIGHT          = PlayState.BORD_HEIGHT;
	public static final int MAX_TILES_COUNT      = 7;

	private Matcher m;
	private Scorer  sc;

	private final boolean performanceLog    = true;
	private final boolean oldImplementation = false;
	private long timerRowScanPredicates;
	private long timerRowScanFindOpportunities;
	private long timerMatching;
	private long timerScorer;
	private PlayState ps;

	public BoardScanner(Matcher m, Scorer sc, PlayState ps){
		this.m  = m;
		this.sc = sc;
		this.ps = ps;
		this.timerRowScanPredicates        = 0;
		this.timerRowScanFindOpportunities = 0;
		this.timerMatching                 = 0;
		this.timerScorer                   = 0;
	}


	private static final int CL_UNCONSTRAINED   = 1;
	private static final int CL_OCCUPIED        = 2;
	private static final int CL_CONSTRAINED     = 3;
	private static final int CL_OVERCONSTRAINED = 4;
	private class BoardScanLineResults{
		char []  contents ;
		int[]    scanLineClassification;
		char[][] scanLineConstraints;
		int[]    offsetScores ;
		int[]    tileBonus;
		BoardScanLineResults(int lineLength){
			contents               = new char[lineLength];
			scanLineClassification = new int[lineLength];
			scanLineConstraints    = new char[lineLength][];
			offsetScores           = new int[lineLength];
			tileBonus              = new int[lineLength];
		}
		public String toString(){
			String s = "";
			s += "contents:          " +Arrays.toString(contents) +"\n";
			s += "class:             " +Arrays.toString(scanLineClassification) +"\n";

			s += "constraints:       ";
			for (char [] cs : scanLineConstraints){
				s += "{" + Arrays.toString(cs) +  "} ";
			}
			s +=  "\n";

			s += "opportunityScores: " +Arrays.toString(offsetScores)+"\n";
			s += "tileBonus: " +Arrays.toString(tileBonus);
			return s;
		}
	}
	public void debugAccessPrivate(){
		int lineNr = 5;
		BoardScanLineResults s = getBoardScanLineResults(true, lineNr, "XE");
		System.out.println(s);
		ArrayList<int[]> tmp = getPlacingOpportunities(s, (lineNr==7)) ;
		System.out.println(tmp);
		for (int[] c : tmp){
			System.out.println(Arrays.toString(c));
		}
	}
	public void performanceSubTiming(){
		if (performanceLog){
			System.out.println("timerRowScanPredicates " +timerRowScanPredicates+"ms " +
					"    timerRowScanFindOpportunities "+timerRowScanFindOpportunities+"ms " +
					"    timerMatching "+timerMatching+"ms " +
					"    timerScorer "+timerScorer+"ms " +
					""
					);
		}
	}

	private BoardScanLineResults  getBoardScanLineResults(boolean horizontalOrientation, int lineNr, String tileSet){
		final int lineLength = horizontalOrientation ? BORD_WIDTH : BORD_HEIGHT;

		// the results
		BoardScanLineResults out = new BoardScanLineResults(lineLength);

		for (int i = 0; i<lineLength; i++){
			int x = horizontalOrientation ? lineNr : i;
			int y = horizontalOrientation ? i : lineNr;
			int perpShiftX = horizontalOrientation ? 1 : 0;
			int perpShiftY = horizontalOrientation ? 0 : 1;

			out.contents[i]  = ps.boardTiles[x][y];
			out.tileBonus[i] = NORMAL_EMPTY;
			switch (ps.boardValues[x][y]){
			case DOUBLE_LETTER_MARKER:
			case TRIPLE_LETTER_MARKER:
			case DOUBLE_WORD_MARKER:
			case TRIPLE_WORD_MARKER:
				out.tileBonus[i] = ps.boardValues[x][y];
				break;
			case 0:
				out.tileBonus[i] = 0;
				break;
			default:
				out.tileBonus[i] = NORMAL_EMPTY;
			}

			if (ps.boardTiles[x][y]!=' '){
				out.scanLineClassification[i] = CL_OCCUPIED;
				out.scanLineConstraints[i]    = new char[]{ps.boardTiles[x][y]};
			} else if   (!(x+perpShiftX < BORD_WIDTH && y+perpShiftY < BORD_HEIGHT && (ps.boardTiles[x+perpShiftX][y+perpShiftY] != ' ') ||
					(x-perpShiftX >=0          && y-perpShiftY >=0           && ps.boardTiles[x-perpShiftX][y-perpShiftY] !=' '))) {
				out.scanLineClassification[i] = CL_UNCONSTRAINED;
				out.scanLineConstraints[i]    = null;
			} else {
				out.scanLineClassification[i] = CL_CONSTRAINED; // maybe overconstrained, but that will be figuered out by finding the constraints

				String leadingTokens = "";
				int iLd = 1;
				while(x-iLd*perpShiftX >= 0          && y-iLd*perpShiftY >= 0           && ps.boardTiles[x-iLd*perpShiftX][y-iLd*perpShiftY] !=' '){
					leadingTokens =   ps.boardTiles[x-iLd*perpShiftX][y-iLd*perpShiftY] + leadingTokens;
					out.offsetScores[i] += ps.boardValues[x-iLd*perpShiftX][y-iLd*perpShiftY];
					iLd++;
				}

				String laggingTokens = "";
				int iLg = 1;
				while(x+iLg*perpShiftX < BORD_WIDTH  && y+iLg*perpShiftY < BORD_HEIGHT && ps.boardTiles[x+iLg*perpShiftX][y+iLg*perpShiftY] !=' '){
					laggingTokens = laggingTokens + ps.boardTiles[x+iLg*perpShiftX][y+iLg*perpShiftY];
					out.offsetScores[i] += ps.boardValues[x+iLg*perpShiftX][y+iLg*perpShiftY];
					iLg++;
				}

				int wordmutiplier = 1;
				switch (ps.boardValues[x][y]){
				case DOUBLE_WORD_MARKER: wordmutiplier = 2; break;
				case TRIPLE_WORD_MARKER: wordmutiplier = 3; break;
				default: wordmutiplier = 1;
				}

				out.offsetScores[i]       *= wordmutiplier;
				out.scanLineConstraints[i] = m.matchSingle(tileSet, leadingTokens + "?" + laggingTokens);

				if (out.scanLineConstraints[i] == null){
					out.scanLineClassification[i] = CL_OVERCONSTRAINED;
				}

			}

		}

		return out;
	}
	private ArrayList<int[]> getPlacingOpportunities(BoardScanLineResults boardScanLineResults, boolean isMiddleLine){
		int lineLength       = boardScanLineResults.contents.length;
		ArrayList<int[]> out = new ArrayList<int[]>();

		for (int i=0; i<lineLength; i++){
			boolean adjacentHit  = false;
			int     placedStones = 0;
			String  placedWordOnLine = "";
			if ((boardScanLineResults.scanLineClassification[i] != CL_OVERCONSTRAINED) &&
					(i == 0 || boardScanLineResults.scanLineClassification[i-1] != CL_OCCUPIED)){
				adjacentHit = adjacentHit ||
						(isMiddleLine && i==7) ||
						boardScanLineResults.scanLineClassification[i] == CL_OCCUPIED ||
						boardScanLineResults.scanLineClassification[i] == CL_CONSTRAINED;
				placedStones += boardScanLineResults.scanLineClassification[i] == CL_OCCUPIED ? 0 : 1;
				placedWordOnLine += ((boardScanLineResults.scanLineClassification[i] == CL_OCCUPIED) ? boardScanLineResults.contents[i] : "");
				for (int j=i+1; j<lineLength; j++){
					if (boardScanLineResults.scanLineClassification[j] == CL_OVERCONSTRAINED){
						j = lineLength;
					} else {
						adjacentHit = adjacentHit ||
								(isMiddleLine && j==7) ||
								boardScanLineResults.scanLineClassification[j] == CL_OCCUPIED ||
								boardScanLineResults.scanLineClassification[j] == CL_CONSTRAINED;
						placedStones += boardScanLineResults.scanLineClassification[j] == CL_OCCUPIED ? 0 : 1;
						placedWordOnLine = ((boardScanLineResults.scanLineClassification[j] == CL_OCCUPIED) ? placedWordOnLine + boardScanLineResults.contents[j] : "");
						if (!oldImplementation &&((j<14 && boardScanLineResults.scanLineClassification[j+1] != CL_OCCUPIED && !m.canBeContained(placedWordOnLine)) ||
								(j==15 && !m.canBeContained(placedWordOnLine)))
								){
							j = lineLength;
						} else if (placedStones > MAX_TILES_COUNT){
							j = lineLength;
						} else if ((placedStones > 0) &&
								(adjacentHit) &&
								((j == lineLength-1) || boardScanLineResults.scanLineClassification[j+1] != CL_OCCUPIED)
								){
							// Found an opportunity !
							out.add(new int[]{i,j});
						}

					}
				}
			}
		}
		return out;
	}


	public ScorerResults scanLineOpportunities (boolean horizontalOrientation, int lineNr){
		long pTs;
		String tileSet    = ps.availableStones;
		ScorerResults out = new ScorerResults(SCORES_TOP_SIZE_SCANLINE);

		// Constraint solver
		if (performanceLog) pTs = System.currentTimeMillis();
		BoardScanLineResults boardScanLineResults = getBoardScanLineResults(horizontalOrientation, lineNr, tileSet);
		if (performanceLog)	timerRowScanPredicates += (System.currentTimeMillis()-pTs);

		// Placing opportunities
		if (performanceLog) pTs = System.currentTimeMillis();
		ArrayList<int[]>     placingOpportunities = getPlacingOpportunities(boardScanLineResults, (lineNr==7));
		if (performanceLog) timerRowScanFindOpportunities += (System.currentTimeMillis()-pTs);

		for (int[] is : placingOpportunities){
			int x = is[0];
			int y = is[1];

			int      offsetScore  = 0;
			char[][] constraints  = new char[1+y-x][];
			String   placedTokens = "";
			String   bonusPattern = "";
			String   connectorPattern = "";
			int      wordMutiplier = 1;
			int      jokerCorrection = 0;

			for (int i=x; i<=y; i++){
				offsetScore     += boardScanLineResults.offsetScores[i];
				constraints[i-x] = boardScanLineResults.scanLineConstraints[i];
				placedTokens    += boardScanLineResults.contents[i];
				int bonusAdd     = 0;
				int connectorMultiply = 1;
				switch (boardScanLineResults.tileBonus[i]){
				case DOUBLE_LETTER_MARKER:
					bonusAdd = 2;
					break;
				case TRIPLE_LETTER_MARKER:
					bonusAdd = 3;
					break;
				case DOUBLE_WORD_MARKER:
					wordMutiplier *= 2;
					bonusAdd = 1;
					connectorMultiply = 2;
					break;
				case TRIPLE_WORD_MARKER:
					wordMutiplier *= 3;
					bonusAdd = 1;
					connectorMultiply = 3;
					break;
				case 0:
					jokerCorrection = sc.getLetterValue(boardScanLineResults.contents[i]);
				default:
					bonusAdd = 1;
				}


				bonusPattern     += bonusAdd;
				connectorPattern += (boardScanLineResults.scanLineClassification[i]== CL_CONSTRAINED) ?
						connectorMultiply : "0";

			}
			offsetScore -= jokerCorrection * wordMutiplier;
			placedTokens = placedTokens.replace(" ", "?");

			// Matcher
			if (performanceLog) pTs = System.currentTimeMillis();
			ArrayList<String> foundWords = m.match(tileSet, constraints, placedTokens);
			if (performanceLog)	timerMatching += (System.currentTimeMillis()-pTs);

			// Scorer
			if (performanceLog) pTs = System.currentTimeMillis();
			ScorerResults score = sc.getScoresForMatchList(foundWords, tileSet, placedTokens, bonusPattern, offsetScore, wordMutiplier, connectorPattern, ps, SCORES_TOP_SIZE_LOCATION);
			if (performanceLog)	timerScorer += (System.currentTimeMillis()-pTs);

			if (foundWords.size()>0){
				score.setLocation(horizontalOrientation, lineNr,x, y);
				out.mergeWith(score);
			}
		}

		return out;
	}

	public ScorerResults fullScanOpportunities (){
		ScorerResults out = new ScorerResults(SCORES_TOP_SIZE_BOARD);
		for (int i=0; i<BORD_WIDTH; i++){
			out.mergeWith(scanLineOpportunities(false, i));
		}

		for (int i=0; i<BORD_HEIGHT; i++){
			out.mergeWith(scanLineOpportunities(true, i));
		}

		return out;
	}
	public void setPlayState(PlayState ps) {
		this.ps = ps;

	}
	public PlayState getPlayState() {
		return ps;
	}
	public Scorer getScorer() {
		return sc;
	}
	public void setScorer(Scorer sc) {
		this.sc = sc;
	}
	public BoardScanner getClone(){
		try {
			return (BoardScanner) (clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

