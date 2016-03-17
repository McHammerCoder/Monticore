import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.io.FileInputStream;
import java.io.InputStream;




public class JavaScriptSimpleTool {
	public static void unparse(ParseTree tree){
		System.out.println();
		System.out.println("Encoded Template:");
		EncoderVisitor encoder = new EncoderVisitor();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(encoder, tree); 
		System.out.println();
		}	

	public static void parse(String is){
		System.out.println();
		System.out.println("Decoded Template:");
		DecoderVisitor decoder = new DecoderVisitor();
		ANTLRInputStream input = new ANTLRInputStream(is);
		JavaScriptSimpleLexer lexer = new JavaScriptSimpleLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaScriptSimpleParser parser = new JavaScriptSimpleParser(tokens);
		ParseTree tree = parser.javascriptprogram();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(decoder, tree); 
	}
	public static void main(String[] args) throws Exception {
		/* This first part reads and creates a template - taken from a file
		The template is printed, after which it is modified to have an injection
		The modification is currently done by encode. The now modified tempate
		is then encoded, printed out. The last step is to decode it, build the tree of it
		and print out the decoded string */

		// create a CharStream that reads from standard input
		String inputFile = null;
			if ( args.length>0 ) inputFile = args[0];
		InputStream is = System.in;
			if ( inputFile!=null ) is = new FileInputStream(inputFile);
		// create a CharStream that reads from standard input
		ANTLRInputStream input = new ANTLRInputStream(is);
		// create a lexer that feeds off of input CharStream
		JavaScriptSimpleLexer lexer = new JavaScriptSimpleLexer(input);
		// create a buffer of tokens pulled from the lexer
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		// create a parser that feeds off the tokens buffer
		JavaScriptSimpleParser parser = new JavaScriptSimpleParser(tokens);
		ParseTree tree = parser.javascriptprogram(); // begin parsing at javascriptprogram
		System.out.println("Template Input:");
		System.out.println(tree.getText());
		System.out.println();
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new Injector(), tree); 
		System.out.println("Injected Template:");
		System.out.println(tree.getText());
		//Save the text in order to check everything

		//String originaltext = tree.getText();
		//PP the template and inject something bad in it
		unparse(tree);
		//Parse the encoded string again and decode it
		//System.out.println(tree.getText());
		System.out.println(tree.getText());
		parse(tree.getText());
		//unparse(tree, originaltext);
		//parse(tree.getText(), originaltext);

}
}
