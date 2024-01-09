package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	public static final Struct boolType = Tab.insert(Obj.Type, "bool", new Struct(Struct.Bool)).getType();
	
	private boolean errorDetected = false;
	private boolean mainDefined = false;	
	
	private boolean variableIsArray = false;
	private Struct currentType = null;
	private Obj currentMethod = null;
	private boolean returnFound = true;
	private int loopCounter = 0;
	private String currentNamespace = null;
	private int nVars;
	
	private HashSet<String> namespaces = new HashSet<>();
	
	private ArrayList<Struct> multipleDesignatorTypes = new ArrayList<>();
	
	private ArrayList<Struct> actualParams = new ArrayList<>();
	
	Logger log = Logger.getLogger(getClass());
	
	public SemanticAnalyzer() {
		System.out.print("=========================");
		System.out.print("SEMANTICKA OBRADA");
		System.out.println("=========================");
	}

	public void report_error(String message, SyntaxNode info) {
		setErrorDetected(true);
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
	
	public void printSymbol(String symName) {
		StringBuilder msg = new StringBuilder();
		Obj obj = Tab.find(symName);
		msg.append("Objektni cvor pronadjenog simbola -> ");
		msg.append(codeToKind(obj.getKind()) + " ");
		msg.append(obj.getName() + ": ");
		msg.append(typeCodeToType(obj.getType().getKind()));
		if (obj.getType().getKind() == Struct.Array) {
			msg.append(" of "); 
			msg.append(typeCodeToType(obj.getType().getElemType().getKind()));
		} 
		msg.append(", ");
		msg.append(obj.getAdr() + ", ");
		msg.append(obj.getLevel());
		System.out.println(msg.toString());
	}

	public void visit(ProgramName programName) {
		programName.obj = Tab.insert(Obj.Prog, programName.getProgramName(), Tab.noType);
		Tab.openScope();
		Obj indexNode = Tab.insert(Obj.Var, "$1", Tab.intType);
		Obj leftIndexNode = Tab.insert(Obj.Var, "$2", Tab.intType);
		
		Helper helperInstance = Helper.getInstance();
		helperInstance.setIndexNode(indexNode);
		helperInstance.setLeftIndexNode(leftIndexNode);
	}
	
	public void visit(Program program) {
		if (!mainDefined) {
			report_error("Metoda main() mora biti definisana!", program);
		}
		
		Tab.chainLocalSymbols(program.getProgramName().obj);
		this.nVars = Tab.currentScope().getnVars();
		Tab.closeScope();
	}
	
	public int getnVars() {
		return nVars;
	}

	public void setnVars(int nVars) {
		this.nVars = nVars;
	}
	
	private String codeToKind(int code) {
		String kind = null;
		
		switch(code) {
		case 0: kind = "Con"; break;
		case 1: kind = "Var"; break;
		case 3: kind = "Meth"; break;
		}
		
		return kind;
	}

	private String typeCodeToType(int kind) {
		String type = null;
		
		switch(kind) {
		case 0: type = "void"; break;
		case 1: type = "int"; break;
		case 2: type = "char"; break;
		case 3: type = "Arr"; break;
		case 5: type = "bool"; break;
		}
		
		return type;
	}
	
	public void addConstSymbol(String constName, SyntaxNode info, Struct constType, int value) {
		String symName = constName;
		
		if (currentNamespace != null) {
			symName = currentNamespace + "::" + constName;
			if (isNamespaceSymbolDeclared(symName)) {
				report_error("Konstanta " + constName + " je vec deklarisana unutar "
							+ currentNamespace + " prostora imena", info);
				return;
			}
		} else {
			if (isSymbolAlreadyDeclared(constName)) {
				report_error("Konstanta " + constName + " je vec deklarisana", info);
				return;
			}
		}
		
		Obj obj = Tab.insert(Obj.Con, symName, constType);
		obj.setAdr(value);
		
		report_info("Deklarisana konstanta " + symName, info);
		printSymbol(symName);
	}
	
	public void visit(ConstInt cnst) {
		if (!currentType.equals(Tab.intType)) {
			report_error("Konstanti tipa " + typeCodeToType(currentType.getKind()) +
						" dodeljena je int vrednost", cnst);
		}
		
		addConstSymbol(cnst.getConstName(), cnst, Tab.intType, cnst.getValue());
	}
	
	public void visit(ConstChar cnst) {
		if (!currentType.equals(Tab.charType)) {
			report_error("Konstanti tipa " + typeCodeToType(currentType.getKind()) +
						" dodeljena je char vrednost", cnst);
		}
		
		addConstSymbol(cnst.getConstName(), cnst, Tab.charType, cnst.getValue());
	}
	
	public void visit(ConstBool cnst) {
		if (!currentType.equals(boolType)) {
			report_error("Konstanti tipa " + typeCodeToType(currentType.getKind()) +
						" dodeljena je bool vrednost", cnst);
		}
		int value = (cnst.getValue() == true ? 1 : 0);
		addConstSymbol(cnst.getConstName(), cnst, boolType, value);
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
	
	private boolean isSymbolAlreadyDeclared(String symName) {
		Obj obj = Tab.currentScope.findSymbol(symName);
		return obj != null;
	}
	
	private boolean isNamespaceSymbolDeclared(String symName) {
		Obj obj = (currentMethod != null ? Tab.currentScope().findSymbol(symName) : Tab.find(symName));
		
		if (currentMethod != null) {
			return obj != null;
		}
		return obj != Tab.noObj;
	}
	
	private void variableDetected(String varName, SyntaxNode info) {
		String symName = varName;
		
		if (currentNamespace != null) {
			if (currentMethod == null) {
				symName = currentNamespace + "::" + varName;
			}
			if (isNamespaceSymbolDeclared(symName)) {
				report_error("Promenljiva " + varName + " je vec deklarisana unutar " 
							+ currentNamespace + " prostora imena", info);
				return;
			}
			
		} else {
			if (isSymbolAlreadyDeclared(symName)) {
				report_error("Promenljiva sa imenom " + varName + " je vec deklarisana! Greska", info);
				return;
			}
		}
		
		
		Struct varType = currentType;
		if (variableIsArray) {
			varType = new Struct(Struct.Array, currentType);
		}
		
		Obj obj = Tab.insert(Obj.Var, symName, varType);
		
		report_info((variableIsArray ? "Deklarisan niz " : "Deklarisana promenljiva ") + symName, info);
		printSymbol(symName);
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
		String name = designator.getIdentName();
		
		designator.obj = Tab.find(name);
		if (currentNamespace != null && designator.obj == Tab.noObj) {
			name = currentNamespace + "::" + designator.getIdentName();
			designator.obj = Tab.find(name);
		}
		
		if (designator.obj == Tab.noObj) {
			report_error("Promenljiva " + name + " mora biti deklarisana pre upotrebe", designator);
		}
	}
	
	public void visit(DesignatorNamespace designator) {
		String outerName = designator.getOuterTypeName();
		String innerName = designator.getInnerName();
		
		if (!namespaces.contains(outerName)) {
			report_error("Ne postoji prostor imena sa nazivom " + outerName, designator);
			designator.obj = Tab.noObj;
			return;
		}
		
		Obj varObj = Tab.find(outerName + "::" + innerName);
		designator.obj = varObj;
		
		if (varObj == Tab.noObj) {
			report_error("Ne postoji deklarisana promenljiva, metoda ili konstanta u prostoru imena " +
					outerName+ " sa imenom " + innerName, designator);
			return;
		}
		
		int varKind = varObj.getKind();
		if (!(varKind == Obj.Con || varKind == Obj.Meth || varKind == Obj.Var)) {
			report_error("Dezignator mora biti konstanta, promenljiva ili metoda", designator);
			return;
		}
	}
	
	public void visit(DesignatorListArray array) {
		Obj arrayNode = array.getDesignator().obj;
		
		if (arrayNode.getType().getKind() != Struct.Array) {
			report_error("Promenljiva mora biti nizovskog tipa ", array);
			return;
		}
		
		if (array.getExpr().struct != Tab.intType) {
			report_error("Izraz koji predstavlja indeks niza mora biti celobrojnog tipa!", array);
			array.obj = new Obj(Obj.Elem, arrayNode.getName(), Tab.noType);
			return;
		}
		report_info("Pristup elementu niza " + arrayNode.getName(), array);
		printSymbol(arrayNode.getName());
		
		array.obj = new Obj(Obj.Elem, arrayNode.getName(), arrayNode.getType().getElemType());
	}
	
	private boolean isFunction(Obj obj, SyntaxNode info) {
		if (obj.getKind() != Obj.Meth) {
			report_error("Simbol " + obj.getName() + " ne predstavlja metodu!", info);
			return false;
		}
		report_info("Poziv funkcije " + obj.getName(), info);
		printSymbol(obj.getName());
		
		return true;
	}
	
	private void compareFunctionParams(Obj obj, SyntaxNode info) {
		Collection<Obj> formalParams = obj.getLocalSymbols();
		int numParams = obj.getLevel();
		
		if (numParams != actualParams.size()) {
			report_error("Broj formalnih argumenata ne odgovara broju stvarnih parametara funkcije", info);
			return;
		}
		
		Iterator<Obj> iter = formalParams.iterator();
		
		for (int i = 0; i < numParams; i++) {
			Obj nextParam = iter.next();
			if (!nextParam.getType().assignableTo(actualParams.get(i))) {
				report_error("Tip formalnog parametra i stvarnog argumenta se ne poklapa", info);
			} 
		}
		actualParams.clear();
	}
	
	public void visit(FunctionCall function) {
		Obj functionNode = function.getDesignator().obj;
		
		if (!isFunction(functionNode, function)) {
			return;
		}
		
		compareFunctionParams(functionNode, function);
	}
	
	private Obj methodDetected(String methodName, Struct type, SyntaxNode info) {
		String symName = methodName;
		
		if (currentNamespace != null) {
			symName = currentNamespace + "::" + methodName;
			if (isNamespaceSymbolDeclared(symName)) {
				report_error("Metoda " + methodName + " je vec deklarisana unutar "
							+ currentNamespace + " prostora imena", info);
				return Tab.noObj;
			}
		} else {
			if (isSymbolAlreadyDeclared(methodName)) {
				report_error("Funkcija sa imenom " + methodName + " vec postoji! Greska", info);
				return Tab.noObj;
			}
		}
		
		
		Obj methodNode = Tab.insert(Obj.Meth, symName, type);
		
		Tab.openScope();
		if (methodName.equals("main") && type != Tab.noType) {
			report_error("Metoda main() mora biti void tipa", info);
		}
		report_info("Deklarisana metoda " + symName + " tipa " + typeCodeToType(type.getKind()), info);
		printSymbol(symName);
		
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
		if (currentMethod == Tab.noObj) {
			// Desila se greska, metoda je vec prethodno bila deklarisana
			currentMethod = null;
			returnFound = false;
			return;
		}
		
		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Semanticka greska na liniji " + method.getLine()
						+ ": funkcija " + currentMethod.getName() + " nema return naredbu!", null);
		}
		
		if (currentMethod.getName().equals("main")) {
			mainDefined = true;
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
		
		if (isSymbolAlreadyDeclared(paramName)) {
			report_error("Promenljiva sa imenom " + paramName + " je vec deklarisana! Greska", formalParam);
			variableIsArray = false;
			return;
		}
		
		Tab.insert(Obj.Var, paramName, formalParamType);
		
		report_info("Pronadjen parametar " + paramName + " funkcije " 
					+ currentMethod.getName(), formalParam);
		printSymbol(paramName);
		
		variableIsArray = false;
	}
	
	public void visit(FormalParamsList formalParams) {
		int nVars = Tab.currentScope.getnVars();
		if (nVars > 0 && currentMethod.getName().equals("main")) {
			report_error("Metoda main() ne sme imati parametre!", formalParams);
		}
		currentMethod.setLevel(nVars);
	}
	
	public void visit(NoFormalParams noParams) {
		currentMethod.setLevel(Tab.currentScope.getnVars());
	}
	
	public void visit(ActualParam param) {
		report_info("Argument funkcije tipa " + 
				typeCodeToType(param.getExpr().struct.getKind()), param);
		actualParams.add(param.getExpr().struct);
	}
	
	public void visit(ActualParams param) {
		report_info("Argument funkcije tipa " + 
				typeCodeToType(param.getExpr().struct.getKind()), param);
		actualParams.add(param.getExpr().struct);
	}
	
	public void visit(ExprSingleTerm expr) {
		expr.struct = expr.getTerm().struct;
	}
	
	public void visit(ExprSingleTermMinus minusExpr) {
		if (minusExpr.getTerm().struct != Tab.intType) {
			report_error("Negirana vrednost mora biti celobrojnog tipa!", minusExpr);
			minusExpr.struct = Tab.noType;
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
			multipleTerms.struct = Tab.noType;
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
		if (arrayFactor.getExpr().struct != Tab.intType) {
			report_error("Izraz koji oznacava velicinu niza mora biti celobrojnog tipa", arrayFactor);
			arrayFactor.struct = Tab.noType;
		} else { 
			arrayFactor.struct = new Struct(Struct.Array, arrayFactor.getType().struct);
		}
	}
	
	public void visit(FactorDesignatorVar var) {
		var.struct = var.getDesignator().obj.getType();
	}
	
	public void visit(FactorDesignatorFunction func) {
		Obj funcNode = func.getDesignator().obj;
		
		func.struct = funcNode.getType();
		if (!isFunction(funcNode, func)) {
			func.struct = Tab.noType;
		}
		
		if (func.struct == Tab.noType) {
			report_error("Funkcija koja nema povratnu vrednost ne moze se koristiti za evaluaciju izraza", func);
			return;
		}
		
		compareFunctionParams(funcNode, func);
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
			report_error("Dezignator sa leve strane mora biti promenljiva ili element niza", assignment);
			return;
		}
		
		if (!exprType.assignableTo(designatorType)) {
			report_error("Nekompatibilni tipovi pri dodeli vrednosti", assignment);
		} 
	}
	
	public void visit(ReturnExpressionStatement returnStmt) {
		if (currentMethod == null) {
			report_error("Return naredba se mora nalaziti unutar neke funckije!", null);
			return;
		}
		
		returnFound = true;
		Struct currentMethodType = currentMethod.getType();

		if (!currentMethodType.compatibleWith(returnStmt.getExpr().struct)) {
			report_error("Tip izraza u return naredbi ne odgovara povratnom tipu funkcije " + 
					currentMethod.getName(), returnStmt);
		} 
	}
	
	public void visit(ReturnEmptyStatement emptyReturn) {
		if (currentMethod == null) {
			report_error("Return naredba se mora nalaziti unutar neke funckije!", null);
			return;
		}
		
		if (currentMethod.getType() != Tab.noType) {
			report_error("Prazna return naredba se sme naci samo unutar void metode", null);
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
			report_info((nodeType.getKind() == Struct.Array) ? 
						"Inkrementiranje elementa niza " : "Inkrementiranje promenljive "
							+ inc.getDesignator().obj.getName(), inc);
			printSymbol(inc.getDesignator().obj.getName());
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
			report_info((nodeType.getKind() == Struct.Array) ? 
					"Dekrementiranje elementa niza " : "Dekrementiranje promenljive "
						+ dec.getDesignator().obj.getName(), dec);
		printSymbol(dec.getDesignator().obj.getName());
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
			} 
		}
		
		multipleDesignatorTypes.clear();
	}
	
	public void visit(DesignatorExists unpackedElement) {
		int elementKind = unpackedElement.getDesignator().obj.getKind();
		
		if (elementKind != Obj.Var && elementKind != Obj.Elem) {
			report_error("Dezignator mora biti element niza ili promenljiva!", null);
			return;
		}
		
		multipleDesignatorTypes.add(unpackedElement.getDesignator().obj.getType());
	}
	
	public void visit(SingleExprCond condition) {
		if (condition.getExpr().struct != boolType) {
			condition.struct = Tab.noType;
			report_error("Uslov mora biti logickog tipa", condition);
		}
		condition.struct = boolType;
	}
	
	public void visit(MultipleExprCond conditions) {
		Struct left = conditions.getExpr().struct, right = conditions.getExpr1().struct;
		
		if (!left.compatibleWith(right)) {
			conditions.struct = Tab.noType;
			report_error("Nevalidno poredjenje razlicitih tipova", conditions);
			return;
		}
		if (left.getKind() == Struct.Array) {
			Relop operator = conditions.getRelop();
			if (!(operator instanceof Equal || operator instanceof NotEqual)) {
				conditions.struct = Tab.noType;
				report_error("Prilikom poredjenja nizova moguce je koristiti iskljucivo " +
							"operatore != i ==", conditions);
			}
		}
		
		conditions.struct = boolType;
	}
	
	public void visit(SingleConditionFactor singleFactor) {
		if (singleFactor.getCondFact().struct != boolType) {
			singleFactor.struct = Tab.noType;
			report_error("Uslov mora biti logickog tipa", singleFactor);
			return;
		}
		singleFactor.struct = boolType;
	}
	
	public void visit(MultipleConditionFactors factors) {
		if (factors.getCondFact().struct != boolType 
			|| 
			factors.getCondTerm().struct != boolType) {
			factors.struct = Tab.noType;
			report_error("Prilikom koriscenja AND operatora moguci su samo logicki izrazi", factors);
		}
		
		factors.struct = boolType;
	}
	
	public void visit(SingleConditionTerm term) {
		if (term.getCondTerm().struct != boolType) {
			term.struct = Tab.noType;
			report_error("Uslov mora biti logickog tipa", term);
			return;
		}
		
		term.struct = boolType;
	}
	
	public void visit(MultipleConditionTerms terms) {
		if (terms.getCondition().struct != boolType || terms.getCondTerm().struct != boolType) {
			terms.struct = Tab.noType;
			report_error("Prilikom koriscenja OR operatora moguci su samo logicki izrazi", terms);
		}
		terms.struct = boolType;
	}
	
	public void visit(ReadStatement readStmt) {
		Obj readObj = readStmt.getDesignator().obj;
		int objKind = readObj.getKind();
		
		if (objKind != Obj.Var && objKind != Obj.Elem) {
			report_error("Dezignator mora biti promenljiva ili element niza", readStmt);
		} else {
			Struct objType = readObj.getType();
			if (!(objType == Tab.intType || objType == Tab.charType || objType == boolType)) {
				report_error("Dezignator mora biti char, int ili bool", readStmt);
				return;
			}
		}
	}
	
	public void visit(PrintStatementNoWidth printStmt) {
		Struct printType = printStmt.getExpr().struct;
		if (!(printType == Tab.charType || printType == Tab.intType || printType == boolType)) {
			report_error("Izraz koji se ispisuje mora biti tipa int, char ili bool", printStmt);
		} 
	}
	
	public void visit(PrintStatementWidth printStmt) {
		Struct printType = printStmt.getExpr().struct;
		if (!(printType == Tab.charType || printType == Tab.intType || printType == boolType)) {
			report_error("Izraz koji se ispisuje mora biti tipa int, char ili bool", printStmt);
		}
	}
	
	public void visit(IfWithCondition condition) {
		Struct condType = condition.getCondition().struct;
		
		if (condType != boolType) {
			report_error("Tip uslovnog izraza mora biti bool", condition);
		} 
	}
	
	public void visit(ForStart forStart) {
		++loopCounter;
	}
	
	public void visit(ForStatement forStmt) {
		--loopCounter;
	}
	
	public void visit(BreakStatement breakStmt) {
		if (loopCounter == 0) {
			report_error("Break naredba se sme koristiti samo unutar petlje", breakStmt);
		} 
	}
	
	public void visit(ContinueStatement contStmt) {
		if (loopCounter == 0) {
			report_error("Continue naredba se sme koristiti samo unutar petlje", contStmt);
		} 
	}
	
	public void visit(NamespaceStart namespaceStart) {
		currentNamespace = namespaceStart.getNamespaceName();
		if (!namespaces.contains(currentNamespace)) {
			namespaces.add(currentNamespace);
		}
	}
	
	public void visit(Namespace namespace) {
		currentNamespace = null;
	}

	public boolean isErrorDetected() {
		return errorDetected;
	}

	public void setErrorDetected(boolean errorDetected) {
		this.errorDetected = errorDetected;
	}
}

