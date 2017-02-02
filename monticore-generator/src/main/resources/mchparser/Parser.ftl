${tc.signature("hammerGenerator","genHelper")}
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleName()>

package ${genHelper.getParserPackage()};
import ${genHelper.getParserPackage()}.${grammarName}Actions;

import com.upstandinghackers.hammer.*;
import de.monticore.mchammerparser.*;

import ${genHelper.getParseTreePackage()}.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.*;

import java.lang.Exception;
import java.io.IOException;

import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;

public class ${grammarName}Parser
{
	static 
	{
		loadLibrary("jhammer");
		loadLibrary("jhammer_${grammarName}");
	}
	
	private static void loadLibrary(String name)
	{
		// Load Hammer Library via JNI
		try 
		{			
			System.loadLibrary(name);
		} 
		catch (UnsatisfiedLinkError e) 
		{
			// Load Hammer Library from Jar or Dependencies
			try 
			{    
				NativeUtils.loadLibraryFromJar("/resources/lib" + name + ".so");
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
	protected final com.upstandinghackers.hammer.Parser ${indirectRule} = Hammer.indirect();
</#list>
<#list hammerGenerator.getDataFieldIndirects() as dataField>
	protected final com.upstandinghackers.hammer.Parser dataField_${dataField} = Hammer.indirect();
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

<#list hammerGenerator.getDataFields() as dataField>
	<#list hammerGenerator.createHammerDataFieldCode(dataField) as dataFieldCode>
		${dataFieldCode}
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
		
		public long getSize()
		{
			return end-start;
		}
		
		public boolean overlaps(Range range)
		{
			if( this.start >= range.getStart() && this.start <= range.getEnd() )
			{
				return true;
			}
			
			if( this.end >= range.getStart() && this.end <= range.getEnd() )
			{
				return true;
			}
			
			return false;
		}
		
		public void combine(Range range)
		{
			if( this.start > range.getStart() )
			{
				this.start = range.getStart();
			}
			
			if( this.end < range.getEnd() )
			{
				this.end = range.getEnd();
			}
		}
	}

	private List<Range> ranges = new ArrayList<Range>();

	/**
	 * parses a binary input
	 * @param bytes DNS-message
	 * @return Antlr-ParseTree
	 */
	public ParseTree parse( byte[] bytes ) throws MCHParserException
	{
		long offset = 0;
		ranges.clear();
		ParseResult parseResult = Hammer.parse(parser, bytes, bytes.length);
		
		if( parseResult == null )
		{
			throw new MCHParserException("Parsing failed !");
		}
		
		HAFileNode parseTree = new HAFileNode(new HARuleContext(${grammarName}TreeHelper.RuleType.RT_Undefined.ordinal()));
		parseTree.addChild(${grammarName}TreeConverter.create(parseResult),offset);
		
		parseResult.free();
		
		ranges.add(new Range(offset,offset+getSize(parseTree)));
		
		Map<HAParseTree,Long> offsets = parseOffsets(bytes,parseTree,offset);
		for( HAParseTree pt : offsets.keySet() )
		{
			parseTree.addChild(pt,offsets.get(pt));
		}
		
		//printRanges();
		
<#if genHelper.parseEntireFile()>
		if( !checkFullyParsed(bytes.length*8) )
		{
			throw new MCHParserException("Parsing failed: File has not been parsed entirely !");
		}
</#if>
				
		return parseTree;
	}
	
	private Map<HAParseTree,Long> parseOffsets( byte[] bytes, HAParseTree parseTree, long offsetOfParseTree ) throws MCHParserException
	{
		List<HAOffsetToken> offsets = getOffsets(parseTree,offsetOfParseTree);
		
		Map<HAParseTree,Long> offsetTrees = Maps.newHashMap();
		if( offsets.size() > 0 )
		{
			System.out.println("Offsets Found:");
			for( HAOffsetToken offsetToken : offsets )
			{
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
				if( offsetToken.getType() == ${grammarName}TreeHelper.TokenType.TT_${offsetProd.getName()}.ordinal()+1)
				{
					long offset = ${hammerGenerator.createOffsetLinearMethodCode(offsetProd)};
<#if genHelper.parseWithoutOverlapingOffsets()>
					long end = findEnd( offset, bytes.length*8 );
<#else>
					long end = bytes.length*8;
</#if>
					System.out.println("ParsedOffset for ${offsetProd.getName()}: " + offset);
					byte [] newBytes = getSubrange(bytes,offset,end);
					
					ParseResult parseResult = Hammer.parse( Hammer.sequence( Hammer.ignore(Hammer.bits((int)offset%8+(int)end%8,false)), _${offsetProd.getRuleName()} ), newBytes, newBytes.length);
																				
					if( parseResult == null )
					{
						throw new MCHParserException("Parsing failed: Offset - ${offsetProd.getName()}");
					}
					
					HAParseTree pt = (HAParseTree) ${grammarName}TreeConverter.create(parseResult);
					
					offsetTrees.put(pt,offset);
					
					parseResult.free();
					
					ranges.add(new Range(offset,offset+getSize(pt)));
					
					offsetTrees.putAll( parseOffsets(bytes,pt,offset) );
				}
</#list>
			}
		}
		
		return offsetTrees;
	}
	
	private byte[] getSubrange( byte [] bytes, long start, long end )
	{
		int byteOffset = (int)start/8;
		int byteEnd = (int)end/8 + ((end%8 > 0)? 1 : 0);
		int bitEnd = (int)end%8;
		
		byte [] byteArray = Arrays.copyOfRange(bytes,byteOffset,byteEnd);
		
		// Shift to the right
		byte [] res = new byte [ byteArray.length ];
		for( int i = byteArray.length-1; i >= 0; i-- )
		{
			int b = byteArray[i] >> bitEnd;
			int b2 = ( i > 0 ) ? byteArray[i-1] << (8-bitEnd) : 0;
			res[i] = (byte)(b | b2);
		}			
			
		return res;
	}
	
	private long findEnd( long start, long size ) throws MCHParserException
	{
		long end = size;
		for( Range range : ranges )
		{
			long rangeStart = range.getStart();
			long rangeEnd = range.getEnd();
<#if genHelper.parseWithoutOverlapingOffsets()>
			if( start > rangeStart && start < rangeEnd )
			{
				throw new MCHParserException("Parsing failed: Trying to parse offset at illegal position!");
			}
</#if>
			if( rangeStart > start && rangeStart < end )
			{
				end = rangeStart;
			}
		}
		
		return end;
	}
	
	private List<HAOffsetToken> getOffsets(HAParseTree parseTree, long offsetOfParseTree)
	{	
		List<HAOffsetToken> offsets = new ArrayList<HAOffsetToken>();
		long position = offsetOfParseTree;

		for( int i = 0; i < parseTree.getChildCount(); i++ )
		{
			ParseTree child = parseTree.getChild(i);
			
			if( child instanceof HATerminalNode )
			{
				Token token = ((HATerminalNode)child).getSymbol();
				position += getSize(((HATerminalNode)child));
				
				if( token instanceof HAOffsetToken )
				{
					if( ((HAOffsetToken)token).isLocal() )
					{
						((HAOffsetToken)token).setPosition(position);
					}
				
					offsets.add((HAOffsetToken)token);
				}
			}
			else
			{
				offsets.addAll(getOffsets((HAParseTree)child,offsetOfParseTree));
			}
		}
		
		return offsets;
	}
	
	private long getSize(HAParseTree parseTree)
	{
		long size = 0;

		if( parseTree instanceof HATerminalNode )
		{
			Token token = ((HATerminalNode)parseTree).getSymbol();
			if( token instanceof HABinarySequenceToken )
			{
				for( HABinaryEntry bin : ((HABinarySequenceToken)token).getValues() )
				{
					size += bin.getBitCount();
				}
			}
			else if( token instanceof HAOffsetToken )
			{
				size += ((HAOffsetToken)token).getValue().getBitCount();
			}
			else
			{
				size += parseTree.getText().getBytes().length*8;
			}
		}
		else
		{
			for( int i = 0; i < parseTree.getChildCount(); i++ )
			{
				ParseTree child = parseTree.getChild(i);
				size += getSize((HAParseTree)child);
			}
		}
		
		return size;
	}
	
	private boolean checkFullyParsed(long treeSize)
	{
		combineRanges();
	
		int size = 0;
		for( Range range : ranges )
		{
			size += range.getSize();
			
			for( Range range2 : ranges )
			{
				if( range != range2 && range.overlaps(range2) )
					return false;
			}
		}
		
		if( size == treeSize )
			return true;
		else
			return false;
	}
	
	private void combineRanges()
	{
		for( int i = 0; i < ranges.size(); i++ )
		{
			for( int j = 0; j < ranges.size(); j++ )
			{
				if( ranges.get(i) != ranges.get(j) && ranges.get(i).overlaps(ranges.get(j)) )
				{
					ranges.get(i).combine(ranges.get(j));
					ranges.remove(j);
					j=0;
				}
			}
		}
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