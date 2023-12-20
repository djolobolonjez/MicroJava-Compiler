package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

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
	private Obj currentMethod = null;
	private ArrayList<Struct> actualParams = new ArrayList<>();
	
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
		
		// Cisto probavanje
		if (designator.obj.getType().getKind() == Struct.Array) {
			report_info("Pristup nekom elementu niza " + designator.getIdentName(), designator);
		}
	}
	
	public void visit(FunctionCall function) {
		Obj functionNode = function.getDesignator().obj;
		currentMethod = functionNode;
		
		if (functionNode.getKind() != Obj.Meth) {
			report_error("Simbol " + functionNode.getName() + " ne predstavlja metodu!", function);
		}
		else {
			report_info("Poziv funkcije " + functionNode.getName(), function);
		}
	}
	
	private Obj methodDetected(String methodName, Struct type, SyntaxNode info) {
		Obj methodNode = Tab.insert(Obj.Meth, methodName, type);
		
		Tab.openScope();
		report_info("Deklarisana metoda " + methodName, info);
		return methodNode;
	}
	
	public void visit(MethodTypeName method) {
		method.obj = methodDetected(method.getMethodName(), method.getType().struct, method);
		currentMethod = method.obj;
	}
	
	public void visit(MethodVoidType method) {
		method.obj = methodDetected(method.getMethodName(), new Struct(Struct.None), method);
		currentMethod = method.obj;
	}
	
	public void visit(MethodDecl method) {
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
	}
	
	public void visit(FormParamDecl formalParam) {
		String paramName = formalParam.getParamName();
		Struct formalParamType = currentType;

		if (variableIsArray) {
			formalParamType = new Struct(Struct.Array, currentType);
		}
		Tab.insert(Obj.Var, formalParam.getParamName(), formalParamType);
		
		report_info("Pronadjen parametar " + paramName + 
					(variableIsArray ? "[]" : "") + " funkcije " 
					+ currentMethod.getName(), formalParam);
		
		variableIsArray = false;
	}
	
	public void visit(ActualParam param) {
		report_info("Argument funkcije tipa " + 
				param.getExpr().struct.getKind(), param);
		actualParams.add(param.getExpr().struct);
	}
	
	public void visit(ActualParams param) {
		report_info("Argument funkcije tipa " + 
				param.getExpr().struct.getKind(), param);
		actualParams.add(param.getExpr().struct);
	}
	
	public void visit(ExprSingleTerm expr) {
		expr.struct = expr.getTerm().struct;
	}
	
	public void visit(ExprSingleTermMinus minusExpr) {
		if (minusExpr.getTerm().struct != Tab.intType) {
			report_error("Negirana vrednost mora biti celobrojnog tipa!", minusExpr);
		} else {
			minusExpr.struct = Tab.intType;
		}
	}
	
	public void visit(ExprMultipleTerm multipleTerms) {
		Struct texp = multipleTerms.getExpr().struct, term = multipleTerms.getTerm().struct;
		
		if (texp.equals(term) && texp == Tab.intType) {
			multipleTerms.struct = Tab.intType;
		} else {
			report_error("Nekompatibilni tipovi u izrazu za sabiranje!", multipleTerms);
		}
	}
	
	public void visit(TermSingleFactor singleFactor) {
		singleFactor.struct = singleFactor.getFactor().struct;
	}
	
	public void visit(TermMultipleFactors factors) {
		Struct fterm = factors.getTerm().struct, factor = factors.getFactor().struct;
		
		if (fterm.equals(factor) && fterm == Tab.intType) {
			factors.struct = Tab.intType;
		} else {
			report_error("Nekompatibilni tipovi u izrazu za mnozenje!", factors);
			factors.struct = Tab.noType;
		}
	}
	
	public void visit(FactorExpression expression) {
		expression.struct = expression.getExpr().struct;
	}
	
	public void visit(FactorNewArray arrayFactor) {
		// dodati proveru za expression unutar []
		arrayFactor.struct = new Struct(Struct.Array, arrayFactor.getType().struct);
	}
	
	public void visit(FactorDesignatorVar var) {
		var.struct = var.getDesignator().obj.getType();
	}
	
	public void visit(FactorDesignatorFunction func) {
		// proveriti da li tip parametra odgovara tipu stvarnog argumenta
		func.struct = func.getDesignator().obj.getType();
	}
	
	public void visit(FactorConstNum factor) {
		factor.struct = Tab.intType;
	}
	
	public void visit(FactorConstChar factor) {
		factor.struct = Tab.charType;
	}
	
	public void visit(FactorConstBoolean factor) {
		factor.struct = boolType;
	}
}

