${tc.signature("hammerGenerator","genHelper")}
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
TT_${iter}=${iter}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
${lexRuleName}=${iter}
<#assign iter=iter+1>
</#list>		
<#list genHelper.getBinaryRuleNames() as binRuleName>
${binRuleName} = ${iter}
<#assign iter=iter+1>
</#list>
<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
'${lexString}'=${iter}
<#assign iter=iter+1>
</#list>