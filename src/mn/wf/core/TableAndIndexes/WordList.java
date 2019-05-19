package mn.wf.core.TableAndIndexes;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;



public class WordList extends ArrayList<String>{
	// Basic static properties (settings)
	private static final long serialVersionUID = 1644478458707218003L;
	private static final char base_ref  = 'A';
	private static final int maxwordsize = 15;

	// Basic properties
	private int wordlength = 0;
	private String description;

	// Indexes for data access
	private BitmapIndex[][] indexes;
	private HashSet<String> checkExistanceSet;
	private HashSet<String> containedSet;

	// IndexjoinCache
	private String                        cachedForAvailableStones = null;
	private HashMap<Integer, BitmapIndex> cacheForAvailableStones  = null;


	// Performance measurement storage
	private int logicalJoinCallCounter;
	private int executedJoinCallCounter;


	private WordList(String description){
		super();
		this.description = description;
	}

	public WordList(){
		this("");
	}


	public static HashMap<Integer, WordList> loadDataFromFiles(String sourceFileName, String blacklistFileName) throws FileNotFoundException{
		HashMap<Integer, WordList> out ;
		File sourcePath = new File(sourceFileName);
		BufferedReader blacklistReader = new BufferedReader(new InputStreamReader(new FileInputStream(blacklistFileName)));
		ArrayList<BufferedReader> sourceReaders = new ArrayList<BufferedReader>();

		if (sourcePath.isDirectory()){
			for (File f : sourcePath.listFiles()){
				if (!(f.isDirectory())){
					sourceReaders.add(new BufferedReader(new InputStreamReader(new FileInputStream(f))));
				}
			}
			out = loadDataFromFiles(sourceReaders.toArray(new BufferedReader[0]), blacklistReader);
		} else {
			BufferedReader sourceReader = new BufferedReader(new InputStreamReader(new FileInputStream(sourcePath)));
			out = loadDataFromFiles(new BufferedReader[]{sourceReader}, blacklistReader);
		}
		return out;
	}
	private static String cleanString(String in){
		return in.toUpperCase().replace('Ö','O');
	}

	private static HashMap<Integer, WordList> loadDataFromFiles(BufferedReader [] sourceFileReaders, BufferedReader blacklistFileReader){
		HashMap<Integer, WordList> out         = new HashMap<Integer, WordList>();
		HashMap<Integer, HashSet<String>> temp = new HashMap<Integer, HashSet<String>>();

		for (int i =2; i<=maxwordsize; i++){
			temp.put(i, new HashSet<String>());
		}

		for (BufferedReader sourceFileReader : sourceFileReaders){
			try{
				String ls = sourceFileReader.readLine();

				while(ls!=null){
					StringTokenizer st = new StringTokenizer(ls, "  \t\n\r\f.,;:?!@#$%^&*()1234567890[]{}|=+-_`~'\"\\<>/?");
					while(st.hasMoreTokens()){
						String s = st.nextToken();
						if (s.length() <=maxwordsize && s.length() >1){
							temp.get(s.length()).add(cleanString(s));
						}
					}
					ls = sourceFileReader.readLine();
				}
				sourceFileReader.close();
			}catch(Exception e){e.printStackTrace();}
		}

		try{
			String ls = blacklistFileReader.readLine();

			while(ls!=null){
				StringTokenizer st = new StringTokenizer(ls, "  \t\n\r\f.,;:?!@#$%^&*()1234567890[]{}|=+-_`~'\"\\<>/?");
				while(st.hasMoreTokens()){
					String s = st.nextToken();
					if (s.length() <=maxwordsize && s.length() >1){
						temp.get(s.length()).remove(s.toUpperCase());
					}
				}
				ls = blacklistFileReader.readLine();
			}
			blacklistFileReader.close();
		}catch(Exception e){e.printStackTrace();}


		for (int i : temp.keySet()){
			out.put(i, new WordList());
			for (String s : temp.get(i).toArray(new String[0])){
				out.get(i).add(s);
			}
			out.get(i).buildIndexes();
		}

		return out;
	}

	public boolean add(String e) {
		if (wordlength ==0) {
			wordlength = e.length();
		} else if (wordlength != e.length()) {
			throw new RuntimeException("Wordlist.add: Incompatible string sizes for \""+e+"\" in collection " + description);
		}
		boolean out = super.add(e.toUpperCase());
		return out;
	}

	public static int getInternalCharIndex(char c){
		return c-base_ref;
	}
	public static char convertCharIndex(int i){
		return (char) (i+base_ref);
	}
	public String bitmapIndexToString(BitmapIndex bm){
		String s = "";
		for (int i : bm.toIndexList()){
			s += (", " +get(i));
		}
		return s;
	}
	public void buildIndexes() {
		indexes = new BitmapIndex[wordlength][26];

		for (int i=0; i<wordlength; i++)
			for (int j=0; j<26; j++)
				indexes[i][j] = new BitmapIndex(this, i, (char) (j+base_ref));
	}
	public void buildCountainmentIndexes(WordList [] checkAgainst){
		containedSet = new HashSet<String>();
		HashSet<String> tempAllWords = new HashSet<String>(this);

		for(WordList checkAgainstWordList : checkAgainst){
			int checkAgainstWordlength = checkAgainstWordList.wordlength;
			if (checkAgainstWordlength > this.wordlength){
				for (String checkAgainstWord : checkAgainstWordList.toArray(new String[0])){
					for (int i = 0; i<checkAgainstWordlength; i++){
						for(int j = i+this.wordlength; j<checkAgainstWordlength; j++){
							String subword = checkAgainstWord.substring(i,j);
							if (tempAllWords.contains(subword)){
								containedSet.add(subword);
							}
						}
					}
				}
			}
		}
	}
	public boolean percievedAsValid(String s){
		if (s.length()!=wordlength){
			throw new RuntimeException("WordList.percievedAsValid: String size mismatch");
		}
		if (checkExistanceSet == null){
			checkExistanceSet  = new HashSet<String>(this);
		}
		return checkExistanceSet.contains(s);
	}

	public boolean canBeContained(String s){
		if (containedSet==null){
			throw new RuntimeException("WordList.canBeContained: ContainmentIndexes not build");
		}
		if (s.length()!=wordlength){
			throw new RuntimeException("WordList.canBeContained: String size mismatch");
		}
		if (percievedAsValid(s)){
			return containedSet.contains(s);
		}
		System.out.println("WordList.canBeContained: New word candidate: "+s);
		return true;
	}

	public String getPerformanceStats(){
		return "WordLength: "+wordlength+"    logicalJoinCallCounter: "+logicalJoinCallCounter+ "   executedJoinCallCounter:"+executedJoinCallCounter;
	}

	public BitmapIndex getIx(int charpos, char token){
		char uctoken = Character.toUpperCase(token);
		BitmapIndex out = indexes[charpos][(char) (uctoken-base_ref)];
		return out;
	}
	public BitmapIndex getIx(String p_availableStones, int p_boardStoneSpot){
		logicalJoinCallCounter++;

		if (!p_availableStones.equals(cachedForAvailableStones)){
			cachedForAvailableStones = p_availableStones;
			cacheForAvailableStones  = new HashMap<Integer, BitmapIndex>();
		}
		BitmapIndex out = cacheForAvailableStones.get(p_boardStoneSpot);
		if (out == null){
			out = getIxInternal(p_availableStones, p_boardStoneSpot);
			cacheForAvailableStones.put(p_boardStoneSpot, out);
		}
		return out;
	}
	private BitmapIndex getIxInternal(String p_availableStones, int p_boardStoneSpot){
		executedJoinCallCounter++;

		String availableStones =p_availableStones.replace("?", "").toUpperCase();
		int availableJokers   = p_availableStones.length() - availableStones.length();

		BitmapIndex out = null;
		BitmapIndex       and_restrictedS         = BitmapIndex.trivialallBitmapIndex;
		BitmapIndex       or_restrictedS          = BitmapIndex.trivialnillBitmapIndex;
		BitmapIndex []    and_restricted          = new BitmapIndex[wordlength];
		BitmapIndex []    or_restricted           = new BitmapIndex[availableStones.length()];
		BitmapIndex []    and_restricted_ex_joker = new BitmapIndex[wordlength+1];
		BitmapIndex []    or_restricted_joker     = new BitmapIndex[wordlength*wordlength];


		// bitmap index-lookup
		for (int i=0; i<wordlength; i++){
			if (i==p_boardStoneSpot){
				or_restrictedS = BitmapIndex.trivialallBitmapIndex;
			} else {
				for (int j=0; j<availableStones.length(); j++){
					or_restricted[j] = getIx(i, availableStones.charAt(j));
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
		}

		// index join and joker handling
		if (availableJokers == 0){
			out = and_restrictedS;
		} else if (availableJokers == 1){
			int m =0;
			for (int i=0; i<wordlength; i++){
				and_restricted_ex_joker[0] = BitmapIndex.trivialallBitmapIndex;
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
			out = BitmapIndex.or(or_restricted_joker);
		} else if (availableJokers == 2){
			int m = 0;
			for (int i=0; i<wordlength; i++)
				for (int l=i+1; l<wordlength; l++){
					and_restricted_ex_joker[0] = BitmapIndex.trivialallBitmapIndex;
					int k = 1;
					for (int j=0; j<wordlength; j++){
						if (i!=j && l!=j) {
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
			out = BitmapIndex.or(or_restricted_joker);
		}
		return out;
	}

}
