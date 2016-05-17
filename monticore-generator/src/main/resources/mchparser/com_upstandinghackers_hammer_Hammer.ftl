${tc.signature("hammerGenerator")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign grammarName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign grammarNameLowerCase = genHelper.getQualifiedGrammarName()?lower_case>

#include "resources/jhammer.h"
#include "internal.h"
#include "com_upstandinghackers_hammer_${grammarName}Hammer.h"
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

bool callValidation(const HParseResult *p, const char* name)
{
    JNIEnv *env;
    jint rs = (*jvm)->GetEnv(jvm, (void**) &env, JNI_VERSION_1_6);
    assert (rs == JNI_OK);
    rs = (*jvm)->AttachCurrentThread(jvm, (void**) &env, NULL);
    assert (rs == JNI_OK);

    jclass actionsClass;
    FIND_CLASS(actionsClass, env, "htmlred/_mch_parser/HTMLRedActions");
   
    jmethodID mid = (*env)->GetStaticMethodID(env, actionsClass, name, "(Lcom/upstandinghackers/hammer/ParseResult;)Z");
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

    jboolean validation = (*env)->CallStaticBooleanMethod(env, actionsClass, mid, parseResult);
    
    return validation;
}

HParsedToken* act_Undefined(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actUndefined");
}

HParsedToken* act_UInt(const HParseResult *p, void* user_data) 
{
	return callAction(p,"actUInt");
}

<#list genHelper.getParserRuleNames() as ruleName>
HParsedToken* act_${ruleName}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"act${ruleName}");
}
</#list>

<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
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
<#list genHelper.getBinaryRuleNames() as binRuleName>
HParsedToken* act_${binRuleName}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"act${binRuleName}");
}
</#list>
<#list [8,16,32,64] as bits>
HParsedToken* act_UInt${bits}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actUInt${bits}");
}
</#list>
<#list [8,16,32,64] as bits>
HParsedToken* act_Int${bits}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actInt${bits}");
}
</#list>
<#list 1..64 as bits>
HParsedToken* act_UBits${bits}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actUBits${bits}");
}
</#list>
<#list 1..64 as bits>
HParsedToken* act_Bits${bits}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actBits${bits}");
}
</#list>

<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
HParsedToken* act_${offsetProd.getName()}(const HParseResult *p, void* user_data) 
{
    return callAction(p,"act${offsetProd.getName()}");
}
</#list>

<#list hammerGenerator.getLengthFields() as lengthField>
bool length_${lengthField}(HParseResult *p, void* user_data) 
{
    return callValidation(p,"length_${lengthField}");
}

bool length_${lengthField}_Reset(HParseResult *p, void* user_data) 
{
    return callValidation(p,"length_${lengthField}_Reset");
}

bool length_${lengthField}_Data(HParseResult *p, void* user_data) 
{
    return callValidation(p,"length_${lengthField}_Data");
}

bool length_${lengthField}_DataIter(HParseResult *p, void* user_data) 
{
    return callValidation(p,"length_${lengthField}_DataIter");
}
</#list>

HParsedToken* act_EOF(const HParseResult *p, void* user_data) 
{
    return callAction(p,"actEOF");
}

JNIEXPORT jobject JNICALL Java_com_upstandinghackers_hammer_${grammarName}Hammer_action
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
	else if( strcmp(actionName,"actUInt") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_UInt, NULL) );
	}
<#list genHelper.getParserRuleNames() as ruleName>
	else if( strcmp(actionName,"act${ruleName}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_${ruleName}, NULL) );
	}
</#list>
<#assign iter=1>
<#list hammerGenerator.getLexStrings() as lexString>
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
<#list genHelper.getBinaryRuleNames() as binRuleName>
	else if( strcmp(actionName,"act${binRuleName}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_${binRuleName}, NULL) );
	}
</#list>
	
<#list [8,16,32,64] as bits>
	else if( strcmp(actionName,"actUInt${bits}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_UInt${bits}, NULL) );
	}
</#list>
<#list [8,16,32,64] as bits>
	else if( strcmp(actionName,"actInt${bits}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_Int${bits}, NULL) );
	}
</#list>
<#list 1..64 as bits>
	else if( strcmp(actionName,"actUBits${bits}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_UBits${bits}, NULL) );
	}
</#list>
<#list 1..64 as bits>
	else if( strcmp(actionName,"actBits${bits}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_Bits${bits}, NULL) );
	}
</#list>
	else if( strcmp(actionName,"actEOF") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_EOF, NULL) );
	}
<#list hammerGenerator.getLengthFields() as lengthField>
	else if( strcmp(actionName,"length_${lengthField}") == 0 )
	{
		RETURNWRAP( env, h_attr_bool(UNWRAP(env, p), length_${lengthField}, NULL) );
	}
	else if( strcmp(actionName,"length_${lengthField}_Reset") == 0 )
	{
		RETURNWRAP( env, h_attr_bool(UNWRAP(env, p), length_${lengthField}_Reset, NULL) );
	}
	else if( strcmp(actionName,"length_${lengthField}_Data") == 0 )
	{
		RETURNWRAP( env, h_attr_bool(UNWRAP(env, p), length_${lengthField}_Data, NULL) );
	}
	else if( strcmp(actionName,"length_${lengthField}_DataIter") == 0 )
	{
		RETURNWRAP( env, h_attr_bool(UNWRAP(env, p), length_${lengthField}_DataIter, NULL) );
	}
</#list>
<#list genHelper.getOffsetRulesToGenerate() as offsetProd>
	else if( strcmp(actionName,"act${offsetProd.getName()}") == 0 )
	{
		RETURNWRAP( env, h_action(UNWRAP(env, p), act_${offsetProd.getName()}, NULL) );
	}
</#list>
	else return p;
}