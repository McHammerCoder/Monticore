import java.util.*;

public class Range {
		private char LowerBound;
		private char UpperBound;
		private boolean isNegative;
		
	public Range(char L, char U, boolean neg){
		LowerBound = L;
		UpperBound = U;
		isNegative = neg;		
	}
	
	public String[] toArray(){
		char U = this.getUpperBound();
		char L = this.getLowerBound();
		List<String> tmpList = new ArrayList<String>();
		for(char i = L; i <= U; i++){
			tmpList.add(""+i);
		}
		
		String[] res = tmpList.toArray(new String[0]);
		return res;
		
	}
	
	
	public char getLowerBound(){
		return LowerBound;
		
	}
	
	public char getUpperBound(){
		return UpperBound;
		
	}
	
	public boolean isNegative(){
		return isNegative;
	}
	
	public String[] union(Range range){
		Set<String> set = new HashSet<>(Arrays.asList(this.toArray()));
		set.addAll(Arrays.asList(range.toArray())); // skips duplicate as per Set implementation
		return set.toArray(new String[0]);
	}
	
	public static String[] union(ArrayList<Range> ranges){
		
		Set<String> set = new HashSet<>();
		for(Range range : ranges){
			set.addAll(Arrays.asList(range.toArray()));		
		}
		return set.toArray(new String[0]);
	}
	
	
	 public static void main(String[] args) {
		Range range = new Range('#', '.',false);
		//String[] res = range.toArray();
		//System.out.println(Arrays.deepToString(res));
		Range range2 = new Range('a', 'x', true);
		//String[] res2 = range2.union(range);
		//System.out.println(Arrays.deepToString(res2));
		ArrayList<Range> ranges = new ArrayList<Range>(); 
		ranges.add(new Range('a', 'z', true));
		ranges.add(range);
		ranges.add(range2);
		
		System.out.println(Arrays.deepToString(union(ranges)));
		
		//todo intersection
		 
	 }

}