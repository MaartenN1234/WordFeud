package mn.wf.core;
import java.util.ArrayList;
import java.util.Arrays;

import mn.wf.core.TableAndIndexes.WordList;
import mn.wf.ui.PlayState;




public class Scorer {

	private static final byte  [] letterValueStore  = new byte[]
			{ 1, 4, 5, 2, 1, // A,B,C,D,E
		4, 3, 4, 2, 4, // F,G,H,I,J
		3, 3, 3, 1, 1, // K,L,M,N,O
		4,10, 2, 2, 2, // P,Q,R,S,T
		2, 4, 5, 8, 8, // U,V,W,X,Y
		5, 0};         // Z,?

	// parameters for scoring: stoneFaceScore, playedStoneFaceScore
	private static final float  minDivisionBase  = 1.5f;
	private float [] percievedLetterValueStoreDefault = new float[]
			{ 1, 4, 5, 2, 1, // A,B,C,D,E
			4, 3, 4, 2, 4, // F,G,H,I,J
			3, 3, 3, 1, 1, // K,L,M,N,O
			4,10, 2, 2, 2, // P,Q,R,S,T
			2, 4, 5, 8, 8, // U,V,W,X,Y
			5,10};         // Z,?
	private float [] percievedLetterValueStoreOptimized1 = new float []{0.9098605f,  4.2987967f, 4.3943443f, 1.8331472f, 1.7032851f, 2.9285707f, 2.3250532f, 4.0555468f, 2.262567f,  2.8781836f, 4.631871f, 2.5081985f, 3.667228f,  2.8585835f, -0.1651676f, 3.87808f,    9.982539f, 3.378157f,  2.1504447f, 0.23457375f, 2.7161086f, 4.4215636f, 5.1147904f, 7.574103f,  7.5820484f, 6.3308415f, 8.894633f};
	private float [] percievedLetterValueStoreOptimized2 = new float []{0.5353181f,  3.4701986f, 3.6506727f, 1.6375754f, 2.9812422f, 4.498815f,  3.4744413f, 4.864008f,  2.3723228f, 2.6019728f, 4.457926f, 1.8191775f, 4.8991337f, 2.2541618f, -1.004039f,  2.8958035f, 10.394305f, 4.318994f,  2.5464516f, 0.634018f,   2.7161086f, 3.5481014f, 5.495799f,  5.5924006f, 7.4656186f, 6.902236f,  7.891159f};
	private float [] percievedLetterValueStoreOptimized3 = new float []{0.57959545f, 4.102045f,  4.0837665f, 1.3593204f, 2.2313776f, 4.910033f,  3.6656687f, 4.3854094f, 2.5892956f, 2.2538009f, 4.268656f, 2.2863922f, 4.5418983f, 4.0814977f, -1.2121668f, 3.5580683f, 9.73755f, 4.8095055f, 2.5921023f, 0.6376075f, 3.3516252f, 4.053086f, 5.2535796f, 5.7678885f, 7.774792f, 6.2507176f, 8.561735f};
	private float [] percievedLetterValueStoreBase = new float []{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

	private float [] percievedLetterValueStore = percievedLetterValueStoreBase;

	public Scorer(){
	}
	public Scorer(int iterationSet){
		switch(iterationSet){
		case 1: percievedLetterValueStore = percievedLetterValueStoreOptimized1; break;
		case 2: percievedLetterValueStore = percievedLetterValueStoreOptimized2; break;
		case 3: percievedLetterValueStore = percievedLetterValueStoreOptimized3; break;
		default: percievedLetterValueStore = percievedLetterValueStoreDefault; break;

		}
	}

	public Scorer(float [] percievedLetterValueStore){
		this.percievedLetterValueStore = percievedLetterValueStore;
	}
	public Scorer evolvedPercievedLetterValueStoreClone(){
		float [] percievedLetterValueStoreEvolved = new float[percievedLetterValueStore.length];
		int j = (int) (Math.floor(Math.random() * percievedLetterValueStore.length));
		for (int i = 0; i<percievedLetterValueStore.length; i++){
			if (i != j){
				percievedLetterValueStoreEvolved[i] = percievedLetterValueStore[i];
			} else {
				percievedLetterValueStoreEvolved[i] = percievedLetterValueStore[i]+(float) (Math.random()-0.5);
			}
		}
		return new Scorer(percievedLetterValueStoreEvolved);
	}



	public ScorerResults getNewScorerResults(int topsize){
		ScorerResults out = new ScorerResults(topsize);
		return out;
	}
	public byte getLetterValue(char in){
		if (in=='?') return letterValueStore[26];
		return letterValueStore[WordList.getInternalCharIndex(in)];
	}
	public float getPercievedLetterValue(char in){
		if (in=='?') return percievedLetterValueStore[26];
		return percievedLetterValueStore[WordList.getInternalCharIndex(in)];
	}


	private int [] getTileValueOrder(int [] p_bonusPattern, int [] p_connectorPattern){
		int [] bonusPatternOut = new int[p_bonusPattern.length];
		for (int i = 0; i<bonusPatternOut.length; i++){
			if (p_connectorPattern[i]==1){
				bonusPatternOut[i] = 2 * p_bonusPattern[i];
			} else {
				bonusPatternOut[i] = p_bonusPattern[i];
			}
		}
		return getTileValueOrder(bonusPatternOut);
	}
	private int [] getTileValueOrder(int [] p_bonusPattern){
		final int wordSize = p_bonusPattern.length;
		int [] out = new int[wordSize];

		for (int i =0; i<wordSize; i++){
			int bestindex = -5;
			int best      = -5;
			for (int j =0; j<wordSize; j++){
				if (p_bonusPattern[j] > best){
					best = p_bonusPattern[j];
					bestindex = j;
				}
			}
			p_bonusPattern[bestindex] = -4;
			out[i] = bestindex;
		}
		return out;
	}

	private int createJokerPattern(String word, String availableStones, String p_boardStonePattern, int [] p_bonusPattern, int [] p_connectorPattern){
		final int  wordSize = word.length();
		String availableAndBoardStones = availableStones + p_boardStonePattern.replaceAll("[? ]", "");
		int out = 0;

		int [] counts       = new int[26];

		for (int i=0; i<26; i++){
			counts[i] = 0;
		}
		for (char c : availableAndBoardStones.toCharArray()){
			if (c !='?'){
				counts[WordList.getInternalCharIndex(c)]++;
			}
		}

		int [] bonusPattern = new int[p_bonusPattern.length];
		for (int i=0; i < p_bonusPattern.length;i++){
			if (p_boardStonePattern.charAt(i) == '?' || p_boardStonePattern.charAt(i) == ' '){
				bonusPattern[i] =  p_bonusPattern[i];
			} else {
				// bind existing tiles on the board first
				bonusPattern[i] =  9999;
			}
		}

		int [] ixs = getTileValueOrder(bonusPattern, p_connectorPattern);

		for (int ix = 0; ix < wordSize; ix++){
			char c = word.charAt(ixs[ix]);
			if (--counts[WordList.getInternalCharIndex(c)] < 0){
				out = out | 1 << ixs[ix];
			}

		}

		return out;
	}



	private int [] castPattern(String in){
		int [] out = new int[in.length()];
		int i = 0;
		for (char c: in.toCharArray()){
			out[i++] = Integer.parseInt(""+c);
		}
		return out;
	}

	public ScorerResults getScoresForMatchList(ArrayList<String> matchlist, String availableStones, String p_boardStonePattern, String p_bonusPattern, int scoreoffset, int wordMultiple, String p_connectorPattern, PlayState ps, int topScoresSize){
		ScorerResults out = new ScorerResults(topScoresSize);
		if (matchlist.size() == 0){
			return out;
		}

		int i;
		int [] bonusPattern      = castPattern(p_bonusPattern);
		int [] connectorPattern  = castPattern(p_connectorPattern);
		int    boardStonePattern = 0;
		int    playedStoneCount  = 0;


		String remainderStones = null;
		if (ps != null){
			remainderStones = ps.getRemainderStones();
		}
		int    endGameBonus      = 0;
		if (remainderStones != null && remainderStones.length()<=7){
			for (char c :remainderStones.toCharArray()){
				endGameBonus += getLetterValue(c);
			}
		}
		float[] expectedReplaceValue = new float[8];
		for (i=0; i<8; i++){
			expectedReplaceValue[i] = getSampleFaceExpectedValue(remainderStones, i);
		}


		// parse boardStonePattern
		i=0;
		for (char c: p_boardStonePattern.toCharArray()){
			if (c != '?'){
				boardStonePattern +=  1<<i;
			} else {
				playedStoneCount++;
			}
			i++;
		}

		for (String s : matchlist){
			int   score          = 0;
			int   connectorAdd   = 0;
			float sumLetterValue = 0;
			int   jokerPattern   = 0;

			// creating jokerPattern
			if (availableStones.indexOf("?") >= 0){
				jokerPattern = createJokerPattern (s, availableStones, p_boardStonePattern, bonusPattern, connectorPattern);
			}

			// the scoring
			i = 0;
			String wordWithJokerRepresentation = "";
			for (char c : s.toCharArray()){
				int     letterValue  = getLetterValue(c);
				boolean isJoker      = ((jokerPattern >> i) & 1) == 1;
				boolean isPlaced     = ((boardStonePattern >> i) & 1) == 1;

				if (!(isPlaced)) {
					sumLetterValue += (isJoker ? getPercievedLetterValue('?') : getPercievedLetterValue(c));
				}
				int letterScore = (isJoker ? 0 : letterValue) * bonusPattern[i];
				score          += letterScore;
				connectorAdd   += connectorPattern[i]* letterScore;
				wordWithJokerRepresentation += (isJoker ? Character.toLowerCase(c) : c);
				i++;
			}
			score *= wordMultiple;
			score += scoreoffset;
			score += connectorAdd;

			if (playedStoneCount == 7 ){if(availableStones.indexOf("?") >= 0) score += 40; else score += 50;}
			if (playedStoneCount == availableStones.length()){score += endGameBonus;}

			float floatScore     = score;
			float divisor        = minDivisionBase > sumLetterValue ? minDivisionBase : sumLetterValue;
			float divisor2       = sumLetterValue - (expectedReplaceValue[playedStoneCount <= 7 ? playedStoneCount:7]);
			divisor2       = minDivisionBase > divisor2 ? minDivisionBase : divisor2;
			float stoneFaceScore = floatScore / divisor;
			float stoneFaceScoreC = floatScore / divisor2;

			// the ranking
			out.updateRankWith(wordWithJokerRepresentation,  new float[]{floatScore, stoneFaceScoreC, stoneFaceScore});
		}
		return out;
	}


	public ScorerResults getMaxOpportunityScore(String availableStones, String p_boardStonePattern, String p_bonusPattern, int scoreoffset, int wordMultiple, String p_connectorPattern){
		final int  wordSize         = p_bonusPattern.length();
		ArrayList<String> matchlist = new ArrayList<String>();

		int i = 0;

		int [] bonusPattern      = castPattern(p_bonusPattern);
		int [] connectorPattern  = castPattern(p_connectorPattern);
		int [] tileValueOrder    = getTileValueOrder(bonusPattern, connectorPattern);


		char[] stones      = availableStones.toCharArray();
		int [] stoneValues = new int [availableStones.length()];
		char [] boardStonePattern = p_boardStonePattern.toCharArray();
		for (char c: stones){
			stoneValues[i++] = getLetterValue(c);
		}
		int [] jxs = getTileValueOrder(stoneValues);

		char[] word = new char [wordSize];
		int j=0;
		for (i =0; i<wordSize; i++){
			if(boardStonePattern[i] !='?'){
				word[tileValueOrder[i]] = boardStonePattern[i];
			} else {
				word[tileValueOrder[i]] = stones[jxs[j++]];
			}
		}
		matchlist.add(new String(word));

		return getScoresForMatchList(matchlist, availableStones, p_boardStonePattern, p_bonusPattern, scoreoffset, wordMultiple, p_connectorPattern, null, 1);
	}

	public float getSampleFaceExpectedValue(String remainderStones, int sampleSize) {
		if (remainderStones == null || sampleSize <=0){
			return 0;
		}
		int stoneCountRemainder = remainderStones.length();

		float totalValue = 0;
		for (char c: remainderStones.toCharArray()){
			totalValue += getPercievedLetterValue(c);
		}
		if (stoneCountRemainder >= sampleSize){
			totalValue = sampleSize * totalValue / stoneCountRemainder;
		}

		return totalValue;
	}

	public String toString(){
		return "PercievedLetterValueStore: "+ Arrays.toString(percievedLetterValueStore);
	}
}
