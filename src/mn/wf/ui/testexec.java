package mn.wf.ui;

import java.util.*;

import mn.wf.aiplay.GameEngine;
import mn.wf.aiplay.MonteCarloSimulationEngine;
import mn.wf.core.*;



public class testexec {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long      lss = System.currentTimeMillis();

		boolean forceReloadWordList = false; //true;

		Matcher   m = null;


		if (!forceReloadWordList){
			try{
				m = Matcher.loadFromFile("d:\\personal\\wf\\objectSaves\\matcherNL.obj");
			} catch (Exception e){};
		}

		if(m == null){
			m = new Matcher("d:\\personal\\wf\\wordlists\\NL\\SRC\\",
					"d:\\personal\\wf\\wordlists\\NL\\blacklist.txt");
			m.saveToFile("d:\\personal\\wf\\objectSaves\\matcherNL.obj" );		}

		Scorer    s   = new Scorer();
		PlayState ps  = PlayState.initFor("SH"); // ML, EM, RU, SH, 2D, MAX
		//ps = ps.playWordOwnTilesSample(new ConsideredMove(new int[]{4,7,5,7}, "ON"));
		//ps = ps.playPassSample();
		BoardScanner    bs   = new BoardScanner(m, s, ps);

		MonteCarloSimulationEngine mcse = new MonteCarloSimulationEngine(bs);
		mcse.setConsideredMove(new ConsideredMove(new int[]{1,15,5,15}, "INPAK"));

		new GameEngine(bs);


		System.out.println("Timing setup: "+ (System.currentTimeMillis()-lss)+"ms");
		long lss2 = System.currentTimeMillis();

		//WordChecker.test(m);
		//wordList   (m,s,"?E?E?N");
		//wordFinder (m,s, "EEEIGD?");
		//for(int i=0;i<99;i++){String ssc = bs.fullScanOpportunities().toString();};

		//mcse.test();
		//runSimulations(bs);
		//new SimAnnealing(new ParallelGameEngineMaster(bs)).optimize(1600);

		//System.out.println(Arrays.toString(m.matchSingle("?", "BARON?")));
		// /*


		System.out.println("Stoneset: " +bs.getPlayState().availableStones);
		System.out.println(bs.fullScanOpportunities());
		System.out.println("Stones remaining(Bag + Opponent): " +bs.getPlayState().getRemainderStones());
		// */
		bs.performanceSubTiming();
		m.performanceSubTiming();


		System.out.println("Timing calc: "+ (System.currentTimeMillis()-lss2)+"ms");

		/*
		 * scorerMatcherTester_sub("FINDER", m,s,"AAAAAAABBCCDDDDDEEEEEEEEEEEEEEEEEEFFGGGHHIIIIJJKKKLLLMMMNNNNNNNNNNNOOOOOOPPQRRRRRSSSSSTTTTTUUUVVWWXYZZ","???????????????", 				"111111212111111",				"000000000000000");
		ArrayList<String>    words = m.match("AAAAAAABBCCDDDDDEEEEEEEEEEEEEEEEEEFFGGGHHIIIIJJKKKLLLMMMNNNNNNNNNNNOOOOOOPPQRRRRRSSSSSTTTTTUUUVVWWXYZZ", "???????????????");
		ArrayList<String>    filteredWords = new ArrayList<String>();
		for (String w : words){
			if (m.inWordList(w.substring(1, 4)) &&
				m.inWordList(w.substring(11, 14))
					){
				filteredWords.add(w);
			}
		}
		ScorerResults score = s.getScoresForMatchList(filteredWords, "AAAAAAABBCCDDDDDEEEEEEEEEEEEEEEEEEFFGGGHHIIIIJJKKKLLLMMMNNNNNNNNNNNOOOOOOPPQRRRRRSSSSSTTTTTUUUVVWWXYZZ", "???????????????", "111111212111111", 0, 1, "000000000000000", null, 5);
		System.out.println(filteredWords);
		System.out.println("score :"+score.toString(true));
		System.out.println();

		//*/
	}
	private static void scorerMatcherTester_sub(String desc, Matcher m, Scorer s, String tiles, String pattern){
		scorerMatcherTester_sub(desc, m, s, tiles, pattern, "111111111111111".substring(0, pattern.length()), "000000000000000".substring(0, pattern.length()));
	}
	private static void scorerMatcherTester_sub(String desc, Matcher m, Scorer s, String tiles, String pattern, String scoreMask, String connectPattern){
		ArrayList<String>    words = m.match(tiles, pattern);
		Collections.sort(words);
		ScorerResults score = s.getScoresForMatchList(words, tiles, pattern, scoreMask, 0, 1, connectPattern, null, 5);

		if (words.size()==0) {
			System.out.println(desc +", tileset: "+tiles +", no words found.");
			System.out.println();
		} else {
			System.out.println(desc +", tileset: "+tiles);
			System.out.println(words);
			System.out.println("score :"+score.toString(true));
			System.out.println();
		}
	}

}
