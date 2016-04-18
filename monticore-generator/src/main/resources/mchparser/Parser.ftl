${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleName()>

package ${genHelper.getParserPackage()};
import ${genHelper.getParserPackage()}.${grammarName}Actions;

import com.upstandinghackers.hammer.*;

import java.io.IOException;

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

	/**
	 * parses a binary input
	 * @param bytes DNS-message
	 * @return Antlr-ParseTree
	 */
	public ParseResult parse( byte[] bytes )
	{
		return Hammer.parse(parser, bytes, bytes.length);
	}
}