/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
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
	/* public List<String> invertedToArray(){
		char U = this.getUpperBound();
		char L = this.getLowerBound();
		List<String> res = new LinkedList<String>();
		for(char i = L; i <= U; i++){
			if(121 <= i && i >= 49792){
			res.add(""+(char)(i));
			System.out.println(i);
			}
		}
		return res;
		
	} */

	public char getLowerBound(){
		return LowerBound;
		
	}

	public char getUpperBound(){
		return UpperBound;
		
	}

	public boolean isNegative(){
		return isNegative;
	}

	/*public String[] union(Range range){
		if(range.isNegative() && this.isNegative()) return null;
		if(this.isNegative()) return range.toArray();
		if(range.isNegative()) return this.toArray();
		Set<String> set = new HashSet<>(Arrays.asList(this.toArray()));
		set.addAll(Arrays.asList(range.toArray())); // skips duplicate as per Set implementation
		return set.toArray(new String[0]);
	} */

	public static String[] union(ArrayList<Range> ranges){
		//Range inv = new Range('0','0', false);
		Set<String> set = new HashSet<>();
		for(Range range : ranges){
			if(!range.isNegative()){
				set.addAll(Arrays.asList(range.toArray()));		
			}
			else if(range.isNegative()){
				String[] ar = range.getInverted();
				//List<String> invList = inv.invertedToArray();
				for(String s : ar){
					set.add(s);
				}
				
			}
		}
		return set.toArray(new String[0]);
	}

	/* public Range invertRange(){
		char u = this.getUpperBound();
		if(u == Character.MAX_VALUE){
			u = 0;
		}
		else{
			u++;
			if(u == 122){ //0079
				u = 49792; //c280
			}	
		}
		
		
		char l = this.getLowerBound();
		if(l == 0){
			l = Character.MAX_VALUE ;
		}
		else
		{
			l--;		
			if(l == 49791){
				l = 121;
			}
		}
		if (u < l){
			return new Range(u,l, false);	
		}
		else
			return new Range(l, u, false);
		//System.out.println(l + "  " +u);
			
	} */
	public String[] getInverted(){
		List<String> allChars = new ArrayList<String>();
		for(char i = 0; i < Character.MAX_VALUE; i++){
			allChars.add(""+((char) i));
		}
		
		List<String> fs = Arrays.asList(this.toArray());
		for(String s: fs){
			if(allChars.contains(s)){
				allChars.remove(s); 
			}
		}
		//System.out.println(Arrays.toString(allChars.toArray()));
		return allChars.toArray(new String[0]);
	}
}	