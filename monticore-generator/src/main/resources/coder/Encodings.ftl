${tc.signature("coderGenerator","genHelper")}
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getCoderPackage()};

import java.util.Map;

import de.monticore.codegen.mccoder.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ${parserName}Encodings implements java.io.Serializable{

	private ArrayList<Encoding> allEncodings = new ArrayList<Encoding>();
	private Boolean[] hasEncodingArray;
	
	public void setAllEncodings(ArrayList<Encoding> ens){
			this.allEncodings = ens;
	}
	
	public void setHasEncodingArray(Boolean[] hea){
		this.hasEncodingArray = hea;
	}
	
		
	public Encoding getEncoding(int type) throws Exception{
	${parserName}CoderHelper helper = new ${parserName}CoderHelper();
	ArrayList<Encoding> customEncodings = helper.getCustomEncodings();
		for(Encoding e:customEncodings){
			if(e.getMap().size() != 0 && type == e.getType()) {
				return e;
			}	
		}
		
		for(Encoding encodingMap : allEncodings){
			if(encodingMap.getMap().size() != 0 && type == encodingMap.getType()) {
				return encodingMap;
			}	
		}
		
		System.err.println("[ERR]  A map for : " + type + " is needed but none exists!");
		throw new Exception();
	}
	
	public boolean hasEncoding(int type){
		
		if( type > hasEncodingArray.length-1 )
			return false;
		
		return hasEncodingArray[type];

	}

	public ArrayList<Encoding> getAllEncodings(){
		${parserName}CoderHelper helper = new ${parserName}CoderHelper();
		ArrayList<Encoding> customEncodings = helper.getCustomEncodings();
		ArrayList<Encoding> res = new ArrayList<Encoding>();
		for(Encoding e:customEncodings){
		  res.add(e);
		}
		for(Encoding e: allEncodings){
			res.add(e);
		}
		return res;
	}
}
