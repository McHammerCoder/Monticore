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

	public char getLowerBound(){
		return LowerBound;
		
	}

	public char getUpperBound(){
		return UpperBound;
		
	}

	public boolean isNegative(){
		return isNegative;
	}



	public static String[] union(ArrayList<Range> ranges){
		Set<String> set = new HashSet<>();
		for(Range range : ranges){
			if(!range.isNegative()){
				set.addAll(Arrays.asList(range.toArray()));		
			}
			else if(range.isNegative()){
				String[] ar = range.getInverted();
				for(String s : ar){
					set.add(s);
				}
				
			}
		}
		return set.toArray(new String[0]);
	}


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
		return allChars.toArray(new String[0]);
	}
}	