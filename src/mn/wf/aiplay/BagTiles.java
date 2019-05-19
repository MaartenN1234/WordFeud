package mn.wf.aiplay;

import java.util.ArrayList;
import java.util.Collections;
import mn.wf.core.TableAndIndexes.WordList;
import mn.wf.ui.PlayState;

public class BagTiles {
	private ArrayList<Character> bag;

	public BagTiles(){
		bag = new ArrayList<Character>();
		for (int i = 0; i < PlayState.letterCount.length; i++){
			for (int j = 0; j < PlayState.letterCount[i]; j++){
				if (i==26){
					bag.add('?');
				} else {
					bag.add(WordList.convertCharIndex(i));
				}
			}
		}
		randomize();
	}
	public BagTiles(String bagValue){
		bag = new ArrayList<Character>();
		for (char c: bagValue.toCharArray()){
			bag.add(c);
		}
		randomize();
	}
	private void randomize(){
		Collections.shuffle(bag);
	}

	public String pickTiles(int amount){
		String out = "";
		for (int i=0; i<amount; i++){
			if (bag.size() != 0){
				out += bag.get(bag.size()-1);
				bag.remove(bag.size()-1);
			}
		}
		return out;
	}
	public String swapTiles(String intoBag){
		String out = pickTiles(intoBag.length());
		for (char c: intoBag.toCharArray()){
			bag.add(c);
		}
		randomize();
		return out;
	}
}
