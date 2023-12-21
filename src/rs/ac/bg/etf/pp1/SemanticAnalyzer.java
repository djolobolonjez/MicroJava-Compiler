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
	private boolean returnFound = true;
	
	private ArrayList<Struct> multipleDesignatorTypes = new ArrayList<>();
	
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
	
//	public void visit(ConstDecl constDecl) {
//		String constName = constDecl.getConstName();
//		if (Tab.find(constName) != Tab.noObj) {
//			report_error("Konstanta " + constName + " je vec deklarisana!", constDecl);
//			return;
//		}
//		
//		report_info("Deklarisana konstanta "+ constDecl.getConstName(), constDecl);
//		
//		Tab.insert(Obj.Con, constDecl.getConstName(), constDecl.getType().struct);
//	}
	
	private String typeCodeToType(int kind) {
		String type = null;
		
		switch(kind) {
		case 0: type = "void"; break;
		case 1: type = "int"; break;
		case 2: type = "char"; break;
		case 5: type = "bool"; break;
		}
		
		return type;
	}
	
	public void visit(ConstInt cnst) {
		if (!currentType.equals(Tab.intType)) {
			report_error("Konstanti tipa " + typeCodeToType(currentType.getKind()) +
						" dodeljena je int vrednost", cnst);
		}
	}
	
	public void visit(ConstChar cnst) {
		if (!currentType.equals(Tab.charType)) {
			report_error("Konstanti tipa " + typeCodeToType(currentType.getKind()) +
						" dodeljena je char vrednost", cnst);
		}
	}
	
	public void visit(ConstBool cnst) {
		if (!currentType.equals(boolType)) {
			report_error("Konstanti tipa " + typeCodeToType(currentType.getKind()) +
						" dodeljena je bool vrednost", cnst);
		}
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
		currentType = ident.struct;
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
	
	public void visit(DesignatorVariable designator) {
		designator.obj = Tab.find(designator.getIdentName());
		
		//proveriti da li je promenljiva deklarisana
	}
	
	public void visit(DesignatorListArray array) {
		Obj arrayNode = array.getDesignator().obj;
		
		if (arrayNode.getType().getKind() != Struct.Array) {
			report_error("Promenljiva mora biti nizovskog tipa ", array);
			return;
		}
		
		if (array.getExpr().struct != Tab.intType) {
			report_error("Izraz koji predstavlja indeks niza mora biti celobrojnog tipa!", array);
			return;
		}
		report_info("Pristup nekom elementu niza " + arrayNode.getName(), array);
		
		array.obj = new Obj(Obj.Elem, arrayNode.getName(), arrayNode.getType().getElemType());
	}
	
	public void visit(FunctionCall function) {
		Obj functionNode = function.getDesignator().obj;
		
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
		report_info("Deklarisana metoda " + methodName + " tipa " + type.getKind(), info);
		return methodNode;
	}
	
	public void visit(MethodTypeName method) {
		method.obj = methodDetected(method.getMethodName(), currentType, method);
		currentMethod = method.obj;
	}
		
	public void visit(MethodVoidType method) {
		method.obj = methodDetected(method.getMethodName(), Tab.noType, method);
		currentMethod = method.obj;
	}
	
	public void visit(MethodDecl method) {
		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Semanticka greska na liniji " + method.getLine()
						+ ": funkcija " + currentMethod.getName() + " nema return naredbu!", null);
		}
		
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
		returnFound = false;
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
		// i da li se broj argumenata poklapa - to preko nekog niza sta vec
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
	
	public void visit(DesignatorAssignment assignment) {
		Struct designatorType = assignment.getDesignator().obj.getType();
		Struct exprType = assignment.getExpr().struct;
		
		int kind = assignment.getDesignator().obj.getKind();
		
		if (kind != Obj.Var && kind != Obj.Elem) {
			report_error("Dezignator sa leve strane mora biti promenljiva ili element niza!", assignment);
			return;
		}
		
		if (exprType.assignableTo(designatorType)) {
			report_info("Uspesna dodela vrednost", assignment);
		} else {
			report_error("Nekompatibilni tipovi pri dodeli vrednosti!", assignment);
		}
	}
	
	public void visit(ReturnExpressionStatement returnStmt) {
		// dodati proveru da li je return unutar neke funkcije (void ne sme vracati neki izraz)
		
		returnFound = true;
		Struct currentMethodType = currentMethod.getType();

		if (currentMethodType.compatibleWith(returnStmt.getExpr().struct)) {
			report_info("Ispravna povratna vrednost funkcije", returnStmt);
		} else {
			report_error("Tip izraza u return naredbi ne odgovara povratnom tipu funkcije " + 
						currentMethod.getName() + ". Izraz je tipa " + returnStmt.getExpr().struct.getKind() + 
						" a metoda je tipa " + currentMethodType.getKind(), returnStmt);
		}
	}
	
	public void visit(DesignatorIncrement inc) {
		Struct nodeType = inc.getDesignator().obj.getType();
		
		if (nodeType != Tab.intType) {
			report_error("Promenljiva koja se inkrementira mora biti tipa int!", inc);
			return;
		}
		
		int kind = inc.getDesignator().obj.getKind();
		
		if (kind != Obj.Var && kind != Obj.Elem) {
			report_error("Dezignator mora biti promenljiva ili element niza!", inc);
		} else {
			report_info("Inkrementiranje", inc);
		}
	}
	
	public void visit(DesignatorDecrement dec) {
		Struct nodeType = dec.getDesignator().obj.getType();
		
		if (nodeType != Tab.intType) {
			report_error("Promenljiva koja se dekrementira mora biti tipa int!", dec);
			return;
		}
		
		int kind = dec.getDesignator().obj.getKind();
		
		if (kind != Obj.Var && kind != Obj.Elem) {
			report_error("Dezignator mora biti promenljiva ili element niza!", dec);
		} else {
			report_info("Dekrementiranje", dec);
		}
	}
	
	public void visit(DesignatorUnpacking unpacking) {
		Struct lastElement = unpacking.getDesignator().obj.getType();
		Struct rhs = unpacking.getDesignator1().obj.getType();
		
		if (rhs.getKind() != Struct.Array || lastElement.getKind() != Struct.Array) {
			report_error("Izraz sa desne strane jednakosti mora biti niz!", null);
			return;
		}
		
		if (!rhs.compatibleWith(lastElement)) {
			report_error("Nekompatibilnost tipova nizova sa leve i desne strane!", null);
			return;
		}
		
		for (Struct elemType : multipleDesignatorTypes) {
			if (!rhs.getElemType().assignableTo(elemType)) {
				report_error("Tip dezignatora ne odgovara tipu elementa niza sa desne strane jednakosti ", null);
			} else {
				report_info("OK", null);
			}
		}
		
		multipleDesignatorTypes.clear();
	}
	
	public void visit(DesignatorListElement unpackedElement) {
		int elementKind = unpackedElement.getDesignator().obj.getKind();
		
		if (elementKind != Obj.Var && elementKind != Obj.Elem) {
			report_error("Dezignator mora biti element niza ili promenljiva!", null);
			return;
		}
		
		multipleDesignatorTypes.add(unpackedElement.getDesignator().obj.getType());
	}
}

