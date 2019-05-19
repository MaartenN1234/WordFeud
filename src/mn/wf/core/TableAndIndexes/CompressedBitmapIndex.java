package mn.wf.core.TableAndIndexes;



import java.util.ArrayList;



public class CompressedBitmapIndex extends BitmapIndex {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2585824287744586208L;
	private byte [] self;
	public CompressedBitmapIndex(WordList wl, int charposition, char chr){
		super(false);
		ArrayList<Byte> selfForConstruction = new ArrayList<Byte>();

		int i = 0;
		for (String s: wl){
			i++;
			if (s.charAt(charposition) == chr) {
				// overflows
				while (i>255){
					i -= 255;
					selfForConstruction.add(new Byte((byte) (255)));
				}
				selfForConstruction.add(new Byte((byte) (i-1)));
				trivialnill = false;
				i = 0;
			}
		}

		// copy to self
		self = new byte[selfForConstruction.size()];
		i=0;
		for(byte b : selfForConstruction.toArray(new Byte[0])){
			self[i++] = b;
		}
	}
}
