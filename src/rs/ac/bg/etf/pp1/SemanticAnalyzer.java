package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	public static final Struct boolType = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool)).getType();
	
	private boolean errorDetected = false;
	private boolean variableIsArray = false;
	private Struct currentType = null;
	
	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(ProgramName programName) {
		programName.obj = Tab.insert(Obj.Prog, programName.getProgramName(), Tab.noType);
		Tab.openScope();
	}
	
	public void visit(Program program) {
		Tab.chainLocalSymbols(program.getProgramName().obj);
		Tab.closeScope();
	}
	
	public void visit(ConstDecl constDecl) {
		String constName = constDecl.getConstName();
		if (Tab.find(constName) != Tab.noObj) {
			report_error("Konstanta " + constName + " je vec deklarisana!", constDecl);
			return;
		}
		
		report_info("Deklarisana konstanta "+ constDecl.getConstName(), constDecl);
		
		Tab.insert(Obj.Con, constDecl.getConstName(), constDecl.getType().struct);
	}
	
	public void visit(Identifier ident) {
		String typeName = ident.getTypeName();
		Obj typeNode = Tab.find(typeName);
		
		if (typeNode == Tab.noObj) {
			report_error("Ne postoji tip " + typeName + " u tabeli simbola!", null);
			ident.struct = Tab.noType;
		} else {
			if (typeNode.getKind() == Obj.Type) {
				ident.struct = typeNode.getType();
			} else {
				report_error("Greska: Ime " + typeName + " ne predstavlja tip!", ident);
				ident.struct = Tab.noType;
			}
		}
		currentType = typeNode.getType();
	}
	
	private void variableDetected(String varName, SyntaxNode info) {
		Struct varType = currentType;
		if (variableIsArray) {
			varType = new Struct(Struct.Array, currentType);
		}
		report_info("Deklarisana promenljiva " + varName + 
				(variableIsArray ? "[]": ""), info);
		
		Tab.insert(Obj.Var, varName, varType);
	}
	
	public void visit(MultipleVariablesDecl varDecl) {
		variableDetected(varDecl.getVarName(), varDecl);
		variableIsArray = false;
	}
	
	public void visit(LastVariableDecl varDecl) {
		variableDetected(varDecl.getVarName(), varDecl);
		variableIsArray = false;
	}
	
	public void visit(Squares squares) {
		variableIsArray = true;
	}
	
	public void visit(NoSquares noSquares) {
		variableIsArray = false;
	}
	
	public void visit(DesignatorGlobal designator) {
		designator.obj = Tab.find(designator.getIdentName());
	}
	
	public void visit(DesignatorFunctionNoParams function) {
		Obj functionNode = function.getDesignator().obj;
		
		if (functionNode.getKind() != Obj.Meth) {
			report_error("Simbol " + functionNode.getName() + " ne predstavlja metodu!", function);
		}
	}
}

