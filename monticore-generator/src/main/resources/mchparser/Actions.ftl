${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign actionsName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

import com.upstandinghackers.hammer.ParseResult;
import com.upstandinghackers.hammer.ParsedToken;

import de.mchammer.tree.*;

/**
 * Class that contains all actions the parser might call while parsing
 */
public class ${actionsName}Actions 
{
	// TODO
}