${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleName()>

package ${genHelper.getParserPackage()};
import ${genHelper.getParserPackage()}.${grammarName}Actions;

import com.upstandinghackers.hammer.*;

import ${genHelper.getParseTreePackage()}.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;

import java.lang.Exception;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class ${grammarName}Parser
{
	// Load Hammer Library via JNI
	static 
	{
		try 
		{
			System.loadLibrary("jhammer");
			System.loadLibrary("jhammer_actions");
		} 
		catch (UnsatisfiedLinkError e) 
		{
			// Load Hammer Library from Jar or Dependencies
			try 
			{    
				NativeUtils.loadLibraryFromJar("/resources/libjhammer.so");
				NativeUtils.loadLibraryFromJar("/resources/libjhammer_actions.so");  
			} 
			catch (IOException e1)
			{    
				e1.printStackTrace();
			} 
		}
	}
	
	/** Binary Fragment Token **/
	
	private com.upstandinghackers.hammer.Parser ubit = Hammer.bits(1, false);
	private com.upstandinghackers.hammer.Parser bit = Hammer.bits(1, true);
	
	private com.upstandinghackers.hammer.Parser bit0 = Hammer.intRange( ubit, 0, 0);
	private com.upstandinghackers.hammer.Parser bit1 = Hammer.intRange( ubit, 1, 1);
	
	/** Binary Token **/
	
	private com.upstandinghackers.hammer.Parser uInt_8 = Hammer.uInt8();
	private com.upstandinghackers.hammer.Parser uInt_16 = Hammer.uInt16();
	private com.upstandinghackers.hammer.Parser uInt_32 = Hammer.uInt32();
	private com.upstandinghackers.hammer.Parser uInt_64 = Hammer.uInt64();
	
	private com.upstandinghackers.hammer.Parser int_8 = Hammer.int8();
	private com.upstandinghackers.hammer.Parser int_16 = Hammer.int16();
	private com.upstandinghackers.hammer.Parser int_32 = Hammer.int32();
	private com.upstandinghackers.hammer.Parser int_64 = Hammer.int64();
	
	/** Indirect Parsers **/
<#list genHelper.getIndirectRulesToGenerate() as indirectRule>
	private final com.upstandinghackers.hammer.Parser ${indirectRule} = Hammer.indirect();
</#list>
	
	/** Final Parser **/
	
	com.upstandinghackers.hammer.Parser parser = Hammer.nothingP();

	/** Constructor **/
	public ${grammarName}Parser()
	{
<#list genHelper.getLexerRulesToGenerate() as lexRule>
	<#list hammerGenerator.createHammerCode(lexRule) as lexRuleCode>
		${lexRuleCode}
	</#list>
</#list>	
	
<#list genHelper.getBinaryRulesToGenerate() as binRule>
	<#list hammerGenerator.createHammerCode(binRule) as binRuleCode>
		${binRuleCode}
	</#list>
</#list>
	
<#list genHelper.getParserRulesToGenerate() as parserRule>
	<#list hammerGenerator.createHammerCode(parserRule) as parserRuleCode>
		${parserRuleCode}
	</#list>
</#list>

<#list genHelper.getInterfaceRulesToGenerate() as interfaceRule>
	<#list hammerGenerator.createHammerInterfaceCode(interfaceRule) as interfaceRuleCode>
		${interfaceRuleCode}
	</#list>
</#list>

		parser = _${startRule} ;
	}

	private class Range
	{
		private long start, end;
		
		public Range(long start, long end)
		{
			this.start = start;
			this.end = end;
		}
		
		public long getStart()
		{
			return this.start;
		}
		
		public long getEnd()
		{
			return this.end;
		}
	}

	private List<Range> ranges = new ArrayList<Range>();

	/**
	 * parses a binary input
	 * @param bytes DNS-message
	 * @return Antlr-ParseTree
	 */
	public ParseTree parse( byte[] bytes ) throws Exception
	{
		long offset = 0;
		ParseResult parseResult = Hammer.parse(parser, bytes, bytes.length);
		
		if( parseResult == null )
		{
			throw new Exception("Parse Failed !");
		}
		
		HAParseTree parseTree = new HARuleNode(new HARuleContext(${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal()));
		parseTree.addChild(${grammarName}TreeConverter.create(parseResult));
		
		ranges.add(new Range(offset,offset+getSize(parseTree)));
		
		printRanges();
		
		if( getOffsets(parseTree).size() > 0 )
		{
			System.out.println("Offsets Found!");
		}
				
		return parseTree;
	}
	
	private List<HABinaryToken> getOffsets(HAParseTree parseTree)
	{	
		List<HABinaryToken> offsets = new ArrayList<HABinaryToken>();

		for( int i = 0; i < parseTree.getChildCount(); i++ )
		{
			ParseTree child = parseTree.getChild(i);
			
			if( child instanceof HATerminalNode )
			{
				Token token = ((HATerminalNode)child).getSymbol();
				if( token instanceof HABinaryToken )
				{
					if( ((HABinaryToken)token).isOffset() )
					{
						offsets.add((HABinaryToken)token);
					}
				}
			}
			else
			{
				offsets.addAll(getOffsets((HAParseTree)child));
			}
		}
		
		return offsets;
	}
	
	private long getSize(HAParseTree parseTree)
	{
		long size = 0;

		for( int i = 0; i < parseTree.getChildCount(); i++ )
		{
			ParseTree child = parseTree.getChild(i);
			
			if( child instanceof HATerminalNode )
			{
				Token token = ((HATerminalNode)child).getSymbol();
				if( token instanceof HABinaryToken )
				{
					size += ((HABinaryToken)token).getBits();
				}
				else
				{
					size += child.getText().getBytes().length*8;
				}
			}
			else
			{
				size += getSize((HAParseTree)child);
			}
		}
		
		return size;
	}
	
	private void printRanges()
	{
		System.out.println("RANGES:");
		for( int i = 0; i < ranges.size(); i++ )
		{
			System.out.println("[" + ranges.get(i).getStart() + "," + ranges.get(i).getEnd() + "]");
		}
	}
}