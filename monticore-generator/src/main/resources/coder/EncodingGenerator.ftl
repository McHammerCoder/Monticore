${tc.signature("coderGenerator","outputFolder")}
<#assign genHelper = glex.getGlobalValue("genHelper")>
<#assign grammarSymbol = genHelper.getGrammarSymbol()>
<#assign parserName = genHelper.getQualifiedGrammarName()?cap_first>
<#assign startRule = genHelper.getStartRuleNameLowerCase()>

package ${genHelper.getParserPackage()};

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

//http://viralpatel.net/blogs/freemaker-template-hello-world-tutorial/

public class ${parserName}EncodingGenerator {

	public static void main(String[] args) {
         
        //Freemarker configuration object
        @SuppressWarnings( "deprecation" )
        Configuration cfg = new Configuration();
        try {
            //Load template from source folder
            Template template = cfg.getTemplate("${outputFolder}/${parserName}Encodings.ftl");
             
            // Build the data-model
            Map<String, Object> data = new HashMap<String, Object>();
            //data.put("message", "Hello World!");
            //List parsing
            ${parserName}CoderHelper helper = new ${parserName}CoderHelper();
            ${parserName}CoderCoder coder = new ${parserName}CoderCoder(helper.getTypes(),helper.getKeywords(), helper.getFreeSymbols());
            data.put("hasEncoding", coder.getHasEncoding());
            data.put("encodings", coder.getCodeSection());
   
            // File output
            Writer file = new FileWriter (new File("${outputFolder}/${parserName}Encodings.java"));
            template.process(data, file);
            file.flush();
            file.close();
             
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }

}