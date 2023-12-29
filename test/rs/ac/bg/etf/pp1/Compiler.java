package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.CoderMalfunctionError;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.mj.runtime.Run;
import rs.etf.pp1.mj.runtime.disasm;
import rs.etf.pp1.symboltable.Tab;

public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(Compiler.class);
		String inputFilename = null, outputFilename = null;
		if (args.length >= 2) {
			inputFilename = args[0];
			outputFilename = args[1];
		} else {
			log.error("Missing prgoram arguments! Usage: Compiler <source-file> <object-file>");
		}
		
		
		Reader br = null;
		try {
			File sourceCode = new File(inputFilename);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	        Program prog = null;
	        if (!p.errorDetected) 
	        {
	        	prog = (Program)(s.value); 
				Tab.init();

				// ispis prepoznatih programskih konstrukcija
				SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
				prog.traverseBottomUp(semanticAnalyzer); 
				
				Tab.dump();
				
				if (!semanticAnalyzer.isErrorDetected()) {
					log.info("USPESNA SEMANTICKA ANALIZA");
					File outputFile = new File(outputFilename);
					
					if (outputFile.exists()) {
						outputFile.delete();
					}
					
					CodeGenerator codeGen = new CodeGenerator();
					prog.traverseBottomUp(codeGen);
					Code.dataSize = semanticAnalyzer.getnVars();
					Code.mainPc = codeGen.getMainPc();
					Code.write(new FileOutputStream(outputFile));
					
					disasm.main( new String[] {outputFilename} );
					Run.main( new String[] {outputFilename} );
					
				}
				//log.info(prog.toString(""));
				//log.info("===================================");
	        }
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
