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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

//http://viralpatel.net/blogs/freemaker-template-hello-world-tutorial/

public class ${parserName}EncodingGenerator {
  private static ${parserName}CoderHelper helper = new ${parserName}CoderHelper();
  
	public static String[] getSiso(){
		List<String> allChars = new ArrayList<String>();
		for(char i = 0; i < Character.MAX_VALUE; i++){
			allChars.add(StringEscapeUtils.escapeJava(""+((char) i)));
		}
		
		List<String> fs = Arrays.asList(helper.getFreeSymbols());
		for(String s: fs){
			if(allChars.contains(s)){
				allChars.remove(s); 
			}
		}
		//System.out.println(Arrays.toString(allChars.toArray()));
		return allChars.toArray(new String[0]);
	}


	public static void main(String[] args) {
		String siso = "";
		if( args.length != 0){
         siso = args[0];
         }	
         //Test ", / , 
        //Freemarker configuration object
        @SuppressWarnings( "deprecation" )
        Configuration cfg = new Configuration();
        try {
            //Load template from source folder
            cfg.setDirectoryForTemplateLoading(new File("${outputFolder}"));
            Template template = cfg.getTemplate("${parserName}Encodings.ftl");
             
            // Build the data-model
            Map<String, Object> data = new HashMap<String, Object>();
            //data.put("message", "Hello World!");
            //List parsing
            if(siso.equals("siso")){
            System.out.println("LET THE MADNESS BEGIN");
            Set<String> set = new HashSet<>();
            set.addAll(Arrays.asList(helper.getKeywords()));
            set.addAll(Arrays.asList(getSiso()));	
            String[] keywords = set.toArray(new String[0]);
            System.out.println(Arrays.toString(keywords));
         	 ${parserName}CoderCoder coder = new ${parserName}CoderCoder(helper.getTypes(), keywords , helper.getFreeSymbols());
         	  data.put("hasEncoding", coder.getHasEncoding());
            data.put("encodings", coder.getCodeSection());
              // File output
            Writer file = new FileWriter (new File("${outputFolder}/${parserName}Encodings.java"));
            template.process(data, file);
            file.flush();
            file.close();
            }
            else{
            ${parserName}CoderCoder coder = new ${parserName}CoderCoder(helper.getTypes(),helper.getKeywords(), helper.getFreeSymbols());
             data.put("hasEncoding", coder.getHasEncoding());
            data.put("encodings", coder.getCodeSection());
              // File output
            Writer file = new FileWriter (new File("${outputFolder}/${parserName}Encodings.java"));
            template.process(data, file);
            file.flush();
            file.close();
            }
           
   
          
             
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }

}