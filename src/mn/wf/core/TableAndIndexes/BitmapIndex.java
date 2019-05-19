package mn.wf.core.TableAndIndexes;

import java.io.Serializable;
import java.util.ArrayList;



public class BitmapIndex implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9177972569818685825L;
	public static final BitmapIndex trivialnillBitmapIndex = new BitmapIndex(false);
	public static final BitmapIndex trivialallBitmapIndex  = new BitmapIndex(true);
	protected boolean trivialnill;
	protected boolean trivialall;


	private int[] self;
	private final int bitsize = 32;
	private final double bitsize_d = bitsize;

	protected BitmapIndex(boolean trivialall){
		trivialnill      = !trivialall;
		this.trivialall  = trivialall;
	}

	public BitmapIndex(WordList wl, int charposition, char chr){
		this(false);

		int i = 0, j=0, k=0;

		self = new int[(int) (Math.ceil(wl.size()/bitsize_d))];

		for (String s: wl){
			if (s.charAt(charposition) == chr) {
				self[j] = self[j] | 1 << k;
				trivialnill = false;
			}
			i++;
			j = i/bitsize;
			k = i - j*bitsize;
		}
	}

	private BitmapIndex (BitmapIndex in1, BitmapIndex in2, int operator){
		this(false);
		self = new int[in1.self.length];

		switch (operator){
		case 1:
			// bitwise AND
			for (int i=0; i < self.length; i++){
				self[i] = in1.self[i] & in2.self[i];
				if (self[i] != 0) {
					trivialnill = false;
				}
			}
			return;
		case 2:
			// bitwise OR
			for (int i=0; i < self.length; i++){
				self[i] = in1.self[i] | in2.self[i];
				if (self[i] != 0) {
					trivialnill = false;
				}
			}
			return;
		}
	}
	public static BitmapIndex and (BitmapIndex in1, BitmapIndex in2){
		// bitwise AND
		if (in1 == null){return in2;}
		if (in2 == null){return in1;}

		if (in1.trivialall  && in2.trivialall) { return trivialallBitmapIndex;}
		if (in1.trivialnill || in2.trivialnill) { return trivialnillBitmapIndex;}
		if (in1.trivialall) { return in2;}
		if (in2.trivialall) { return in1;}

		return new BitmapIndex(in1, in2, 1);
	}

	public static BitmapIndex or (BitmapIndex in1, BitmapIndex in2){
		// bitwise OR
		if (in1 == null){return in2;}
		if (in2 == null){return in1;}

		if (in1.trivialall  || in2.trivialall) { return trivialallBitmapIndex;}
		if (in1.trivialnill) { return in2;}
		if (in2.trivialnill) { return in1;}

		return new BitmapIndex(in1, in2, 2);
	}


	public static BitmapIndex and (BitmapIndex [] in){
		if (in.length==0) return trivialallBitmapIndex;
		BitmapIndex out = in[0];
		for (int i=1; i < in.length; i++){
			if (in[i] == null) return out;
			out = and(out, in[i]);
		}
		return out;
	}

	public static BitmapIndex or (BitmapIndex [] in){
		if (in.length==0) return trivialnillBitmapIndex;
		BitmapIndex out = in[0];
		for (int i=1; i < in.length; i++){
			if (in[i] == null) return out;
			out = or(out, in[i]);
		}
		return out;
	}


	public ArrayList<Integer> toIndexList() {
		java.util.ArrayList<Integer> output = new java.util.ArrayList<Integer>();
		if (trivialall){
			return null;
		}
		if (!trivialnill) {
			int k = 0;
			for(int i: self){
				for (int j = 0; j< bitsize; j++){
					if ((i & (1 << j)) != 0) {output.add(k+j);}
				}
				k = k + bitsize;
			}
		}
		return output;
	}

	public boolean isTrivialnill(){
		return trivialnill;
	}
	public String toString(){
		if(trivialnill ) return "Trivial Nill";
		if(trivialall ) return "Trivial All";
		return ""+self[0];
	}

}


