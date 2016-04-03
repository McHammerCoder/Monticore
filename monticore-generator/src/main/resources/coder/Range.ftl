${tc.signature("genHelper")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>

package ${genHelper.getParserPackage()};

import java.util.*;

public class ${parserName}Range {
		private char LowerBound;
		private char UpperBound;
		private boolean isNegative;
		
	public ${parserName}Range(char L, char U, boolean neg){
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
	
		public String[] union(${parserName}Range range){
		if(range.isNegative() && this.isNegative()) return null;
		if(this.isNegative()) return range.toArray();
		if(range.isNegative()) return this.toArray();
		Set<String> set = new HashSet<>(Arrays.asList(this.toArray()));
		set.addAll(Arrays.asList(range.toArray())); // skips duplicate as per Set implementation
		return set.toArray(new String[0]);
	}
	
	public static String[] union(ArrayList<${parserName}Range> ranges){
		
		Set<String> set = new HashSet<>();
		for(${parserName}Range range : ranges){
			if(!range.isNegative()){
				set.addAll(Arrays.asList(range.toArray()));		
			}
		}
		return set.toArray(new String[0]);
	}
	
}