package mn.wf.core;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

import mn.wf.core.TableAndIndexes.BitmapIndex;
import mn.wf.core.TableAndIndexes.WordList;



public class Matcher implements Serializable{
	// testParameters
	private static final boolean debugOutput = false;
	private static final boolean performanceLog = true;
	private static final boolean useCachedJoins = true;

	// statics
	private static final ArrayList<String> emptySet = new ArrayList<String>();
	private static final long serialVersionUID = 5602817413022419935L;

	// contents
	private HashMap<Integer, WordList> wls = new HashMap<Integer, WordList>();

	// performance output variables
	private int notCachedJoinCallCounter;
	private int cachedJoinCallCounter;
	private long pTs;
	private long timerNotCachedJoin;
	private long timerCachedJoin;
	private int totalJoinCallCounter;
	private long totalJoinCallTimer;
	private long placingPatternTimer;


	public Matcher (String sourcePathName, String blacklistFileName){
		try {
			wls = WordList.loadDataFromFiles(sourcePathName, blacklistFileName);
		} catch (FileNotFoundException e) {	e.printStackTrace();}
		WordList [] wla = new WordList[wls.size()];
		int j = 0;
		for (int i : wls.keySet()){
			wla[j++] = wls.get(i);
		}
		for (WordList wl : wla){
			wl.buildCountainmentIndexes(wla);
		}
	}


	private boolean letterCountEquals(String compare, String availableStones, int p_availableJokers){
		if(debugOutput) System.out.println("letterCountEq " + compare + " st:"+availableStones+"("+p_availableJokers+")");

		int availableJokers = p_availableJokers;
		int[] testcount = new int[26];
		for(char c: availableStones.toCharArray()){
			testcount[WordList.getInternalCharIndex(c)]++;
		}
		for(char c: compare.toCharArray()){
			try{
				testcount[WordList.getInternalCharIndex(c)]--;
			} catch (Exception e){
				System.out.println("letterCountEq " + compare + " st:"+availableStones+"("+p_availableJokers+")");
			}
			if (testcount[WordList.getInternalCharIndex(c)] < 0) {
				availableJokers--;
				if (availableJokers <0) {
					return false;
				}
			}
		}
		return true;
	}
	public char[] matchSingle(String p_availableStones, String p_placing_pattern){
		if ((p_placing_pattern.replace("?", "").length()-p_placing_pattern.length()) != -1){
			throw new RuntimeException("Matcher.matchSingle is not called with a single open spot in placing pattern.");
		}
		ArrayList<String> temp = null;
		try{
			temp = match(p_availableStones, p_placing_pattern);
		} catch (Exception e){
			System.err.println("Match: p_availableStones"+p_availableStones+" p_placing_pattern:"+p_placing_pattern);
			e.printStackTrace();
		}

		if (temp.size() == 0) return null;

		char[] out = new char[temp.size()];
		int i=0;
		int offset = p_placing_pattern.indexOf("?");
		for (String s : temp){
			out[i++] = s.charAt(offset);
		}
		return out;
	}

	public ArrayList<String> match(String p_availableStones, String p_placing_pattern){
		if(debugOutput) System.out.println("MATCH1: "+ p_availableStones+", "+p_placing_pattern);
		char[][] placing_pattern = new char[p_placing_pattern.length()][];
		int i=0;
		for (char c :p_placing_pattern.toUpperCase().toCharArray()){
			if (WordList.getInternalCharIndex(c) >= 0 && WordList.getInternalCharIndex(c) <= 26){
				placing_pattern[i] = new char[]{c};
			} else {
				placing_pattern[i] = null;
			}
			i++;
		}
		return match (p_availableStones, placing_pattern, p_placing_pattern.replace("?", " "));
	}

	public ArrayList<String> match(String p_availableStones, char[][] p_placing_pattern, String p_boardPattern){
		int wordlength                   = p_placing_pattern.length;
		BitmapIndex       and_restricted = BitmapIndex.trivialallBitmapIndex;
		BitmapIndex       or_restricted  = BitmapIndex.trivialnillBitmapIndex;
		WordList          wl             = wls.get(wordlength);
		if(debugOutput) System.out.println("MATCH3: "+ p_availableStones+", "+p_boardPattern);

		if(debugOutput){ for (int i = 0; i<p_placing_pattern.length;i++) System.out.print(Arrays.toString(p_placing_pattern[i])); System.out.println();}


		if (wl == null) {
			return emptySet;
		}

		if (performanceLog) pTs = System.currentTimeMillis();
		// placing_pattern_restrictions
		for (int i=0; i<wordlength; i++){
			if (p_placing_pattern[i] != null){
				or_restricted = BitmapIndex.trivialnillBitmapIndex;
				for (int j=0; j<p_placing_pattern[i].length; j++){
					or_restricted = BitmapIndex.or(or_restricted, wl.getIx(i, p_placing_pattern[i][j]));
				}
				and_restricted = BitmapIndex.and(and_restricted, or_restricted);
			}
			if (and_restricted.isTrivialnill()){
				i = wordlength;
			}
		}
		if (performanceLog)	placingPatternTimer += (System.currentTimeMillis()-pTs);

		ArrayList<String> out ;
		if (performanceLog) pTs = System.currentTimeMillis();
		totalJoinCallCounter++;
		out = match(p_availableStones, wordlength, and_restricted, p_boardPattern);
		if (performanceLog)	totalJoinCallTimer += (System.currentTimeMillis()-pTs);
		return out;

	}



	private ArrayList<String> match(String p_availableStones, int p_length, BitmapIndex placing_pattern_restrictions, String p_boardPattern){
		String availableStones            = p_availableStones.toUpperCase();
		String availableAndBoardStonesExJ = availableStones.replace("?", "") + p_boardPattern.replaceAll("[? ]", "");
		int availableJokers   = p_availableStones.replaceAll("[^?]","").length();
		int boardStoneCount   = availableAndBoardStonesExJ.length() - availableStones.length();
		char[] boardPattern   = p_boardPattern.replace("?"," ").toCharArray();
		int wordlength        = p_length;
		WordList wl           = wls.get(wordlength);

		if(debugOutput) System.out.println("MATCHTRUE: "+ p_availableStones+", "+p_boardPattern + "  "+wl.bitmapIndexToString(placing_pattern_restrictions));

		// shortcut placing pattern size out of range
		if (placing_pattern_restrictions.isTrivialnill() || wl == null) return emptySet;
		ArrayList<Integer> ixl;
		ArrayList<String>  out = new ArrayList<String>();

		boolean cached   = (boardStoneCount < 2) && useCachedJoins;

		if(!cached){
			ixl = executeNotCachedBitmapJoin(availableAndBoardStonesExJ, availableJokers, wl, placing_pattern_restrictions);
		} else {
			cachedJoinCallCounter++;
			int boardStoneSpot = -1;
			int i=0;
			for (char c : boardPattern){
				if (c !=' '){
					boardStoneSpot = i;
				}
				i++;
			}
			if (performanceLog) pTs = System.currentTimeMillis();
			ixl = BitmapIndex.and(placing_pattern_restrictions, wl.getIx(availableStones, boardStoneSpot)).toIndexList();
			if (performanceLog)	timerCachedJoin += (System.currentTimeMillis()-pTs);
		}

		if(ixl== null){
			ixl = new ArrayList<Integer>();
			for (int i = 0; i<wl.size(); i++){
				ixl.add(i);
			}
		}
		// filter condition  match letter counts
		for (int i : ixl){
			String compare = wl.get (i);
			if (letterCountEquals(compare, availableAndBoardStonesExJ, availableJokers)){
				out.add(compare);
			}
		}

		return out;
	}


	private ArrayList<Integer> executeNotCachedBitmapJoin(String availableStones, int availableJokers, WordList wl, BitmapIndex placing_pattern_restrictions) {
		ArrayList<Integer> out = new ArrayList<Integer> ();
		int wordlength = (wl.size()==0) ? 9999 : wl.get(0).length();
		if (availableJokers+availableStones.length() < wordlength) return out;

		if(debugOutput) System.out.println(availableStones+", "+availableJokers+ "   "+wordlength);
		notCachedJoinCallCounter++;
		if (performanceLog) pTs = System.currentTimeMillis();


		// bitmap index-lookup
		BitmapIndex       and_restrictedS         = BitmapIndex.trivialallBitmapIndex;
		BitmapIndex       or_restrictedS          = BitmapIndex.trivialnillBitmapIndex;
		BitmapIndex []    and_restricted          = new BitmapIndex[wordlength];
		BitmapIndex []    or_restricted           = new BitmapIndex[availableStones.length()];
		BitmapIndex []    and_restricted_ex_joker = new BitmapIndex[wordlength+1];
		BitmapIndex []    or_restricted_joker     = new BitmapIndex[wordlength*wordlength];

		and_restrictedS = placing_pattern_restrictions;
		for (int i=0; i<wordlength; i++){
			for (int j=0; j<availableStones.length(); j++){
				or_restricted[j] = wl.getIx(i, availableStones.charAt(j));
			}
			or_restrictedS = BitmapIndex.or(or_restricted);
			if (availableJokers == 0){
				and_restrictedS = BitmapIndex.and(or_restrictedS, and_restrictedS);
				if (and_restrictedS.isTrivialnill()) {
					i = wordlength;
				}
			} else {
				and_restricted[i] = or_restrictedS;
			}
		}

		// joker handling
		if (availableJokers == 0){
			out = and_restrictedS.toIndexList();
		} else if (availableJokers == 1){
			int m =0;
			for (int i=0; i<wordlength; i++){
				and_restricted_ex_joker[0] = placing_pattern_restrictions;
				int k = 1;
				for (int j=0; j<wordlength; j++){
					if (i!=j) {
						and_restricted_ex_joker[k++] = and_restricted[j];
					}
				}
				if (k<and_restricted_ex_joker.length){
					and_restricted_ex_joker[k] = null;
				}
				or_restricted_joker[m++] = BitmapIndex.and(and_restricted_ex_joker);
			}
			if (m<or_restricted_joker.length){
				or_restricted_joker[m] = null;
			}
			out = BitmapIndex.or(or_restricted_joker).toIndexList();
		} else if (availableJokers == 2){
			int m = 0;
			for (int i=0; i<wordlength; i++)
				for (int l=i+1; l<wordlength; l++){
					and_restricted_ex_joker[0] = placing_pattern_restrictions;
					int k = 1;
					for (int j=0; j<wordlength; j++){
						if (i!=j && l!=j) {
							and_restricted_ex_joker[k++] = and_restricted[j];
						}
					}
					if (k<and_restricted_ex_joker.length){
						and_restricted_ex_joker[k] = null;
					}
					or_restricted_joker[m++] = BitmapIndex.and(placing_pattern_restrictions,
							BitmapIndex.and(and_restricted_ex_joker));
				}
			if (m<or_restricted_joker.length){
				or_restricted_joker[m] = null;
			}
			out = BitmapIndex.or(or_restricted_joker).toIndexList();
		}

		if (performanceLog)	timerNotCachedJoin += (System.currentTimeMillis()-pTs);

		return out;
	}

	public void performanceSubTiming() {
		performanceSubTiming(false);
	}

	public void performanceSubTiming(boolean showWordListCounters) {
		String s = "";
		if (showWordListCounters){
			for(int i : wls.keySet()){
				if (i<9){
					s += wls.get(i).getPerformanceStats() + "\n";
				}
			}
		}
		s += "timerNotCachedJoin: "+ timerNotCachedJoin+"ms\t\tnotCachedJoinCallCounter: "+notCachedJoinCallCounter +"\n";
		s += "timerCachedJoin   : " +timerCachedJoin+"ms\t\tcachedJoinCallCounter   : "+cachedJoinCallCounter+" \n";
		s += "totalJoinCallTimer: "+totalJoinCallTimer+"ms\t\ttotalJoinCallCounter    : "+totalJoinCallCounter +"\t\tplacingPatternTimer: "+placingPatternTimer+"ms";

		System.out.println(s);

	}

	public static Matcher loadFromFile(String filename){
		ObjectInputStream ois;
		Matcher out = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(filename));
			out = (Matcher) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;

	}
	public void saveToFile(String filename){
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public boolean canBeContained(String w) {
		int l = w.length();
		if (l < 2 || l >15)
			return true;
		return wls.get(l).canBeContained(w);
	}
	public boolean inWordList(String w){
		int l = w.length();
		if (l < 2 || l >15)
			return false;
		return wls.get(l).percievedAsValid(w);
	}
}
