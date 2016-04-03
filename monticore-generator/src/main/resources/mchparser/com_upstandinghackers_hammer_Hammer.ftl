${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

#include "jhammer.h"
#include "internal.h"
#include "com_upstandinghackers_hammer_Hammer.h"
#include <stdlib.h>

static JavaVM* jvm;

HParsedToken* callAction(const HParseResult *p, const char* name)
{
    JNIEnv *env;
    jint rs = (*jvm)->GetEnv(jvm, (void**) &env, JNI_VERSION_1_6);
    assert (rs == JNI_OK);
    rs = (*jvm)->AttachCurrentThread(jvm, (void**) &env, NULL);
    assert (rs == JNI_OK);

    jclass actionsClass;
    FIND_CLASS(actionsClass, env, "${genHelper.getParserPackageC()}/${grammarName}Actions");
   
    jmethodID mid = (*env)->GetStaticMethodID(env, actionsClass, name, "(Lcom/upstandinghackers/hammer/ParseResult;)Lcom/upstandinghackers/hammer/ParsedToken;");
    if (mid == 0)
    {
	return NULL;
    }

    jclass argumentClass;
    FIND_CLASS(argumentClass, env, "com/upstandinghackers/hammer/ParseResult");
    assert(argumentClass != NULL);
    jmethodID constructor = REFCONSTRUCTOR_(env, argumentClass);
    assert(constructor != NULL);
    jobject parseResult = (*env)->NewObject(env, argumentClass, constructor, (jlong)p);

    jobject parsedToken = (*env)->CallStaticObjectMethod(env, actionsClass, mid, parseResult);
    assert(parsedToken != NULL);
    
    return (HParsedToken *)((*env)->GetLongField(env, parsedToken, (*env)->GetFieldID(env, FIND_CLASS_(env, "com/upstandinghackers/hammer/ParsedToken"), "inner", "J")));
}

HParsedToken* act_Undefined(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actUndefined");
}

<#list genHelper.getParserRuleNames() as ruleName>
HParsedToken* act_${ruleName}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"act${ruleName}");
}
</#list>

<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
HParsedToken* act_TT_${iter}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actTT_${iter}");
}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
HParsedToken* act_${lexRuleName}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"act${lexRuleName}");
}
</#list>

JNIEXPORT jobject JNICALL Java_com_upstandinghackers_hammer_Hammer_action
  (JNIEnv *env, jclass class, jobject p, jstring a)
{
	//RETURNWRAP(env, h_middle(UNWRAP(env, p), UNWRAP(env, x), UNWRAP(env, q)));

	jint rs = (*env)->GetJavaVM(env, &jvm);
	assert (rs == JNI_OK);

	const char *actionName = (*env)->GetStringUTFChars(env, a, 0);

    if( strcmp(actionName,"actUndefined") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_Undefined, NULL) );
	}
<#list genHelper.getParserRuleNames() as ruleName>
	else if( strcmp(actionName,"act${ruleName}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_${ruleName}, NULL) );
	}
</#list>
<#assign iter=1>
<#list genHelper.getLexStrings() as lexString>
	else if( strcmp(actionName,"actTT_${iter}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_TT_${iter}, NULL) );
	}
<#assign iter=iter+1>
</#list>
<#list genHelper.getLexerRuleNames() as lexRuleName>
	else if( strcmp(actionName,"act${lexRuleName}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_${lexRuleName}, NULL) );
	}
</#list>
	
	else return p;
}