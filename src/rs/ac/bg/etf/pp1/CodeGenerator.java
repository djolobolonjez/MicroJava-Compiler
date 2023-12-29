package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rs.ac.bg.etf.pp1.CounterVisitor.CondTermCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.*;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	private Stack<List<Integer>> andConditionPatch = new Stack<>();
	private Stack<List<Integer>> orConditionPatch = new Stack<>();
	private Stack<List<Integer>> termConditionPatch = new Stack<>();
	private int condTermCount = 0;
	
	public int getMainPc() {
		return this.mainPc;
	}
	
	public void visit(PrintStatementWidth printStmt) {
		Code.loadConst(printStmt.getWidth());
		if (printStmt.getExpr().struct == Tab.charType) {
			Code.put(Code.bprint);
		} else {
			Code.put(Code.print);
		}
	}
	
	public void visit(PrintStatementNoWidth printStmt) {
		if (printStmt.getExpr().struct == Tab.charType) {
			Code.loadConst(1);
			Code.put(Code.bprint);
		} else {
			Code.loadConst(4);
			Code.put(Code.print);
		}
	}
	
	public void setMethodEntry(Obj methodNode) {
		methodNode.setAdr(Code.pc);
		if (methodNode.getName().equals("main")) {
			this.mainPc = Code.pc;
		}
		
		Code.put(Code.enter);
		int numArgs = methodNode.getLevel(), numLocals = methodNode.getLocalSymbols().size();
		Code.put(numArgs);
		Code.put(numLocals);
	}
	
	public void visit(MethodTypeName method) {
		setMethodEntry(method.obj);
	}
	
	public void visit(MethodVoidType method) {
		setMethodEntry(method.obj);
	}
	
	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(FactorConstBoolean factor) {
		Obj conObj = Tab.insert(Obj.Con, "$", factor.struct);
		conObj.setLevel(0);
		
		int value = (factor.getValue() == true ? 1 : 0);
		conObj.setAdr(value);
		Code.load(conObj);
	}
	
	public void visit(FactorConstChar factor) {
		Obj conObj = Tab.insert(Obj.Con, "$", factor.struct);
		conObj.setLevel(0);
		
		conObj.setAdr(factor.getValue());
		Code.load(conObj);
	}
	
	public void visit(FactorConstNum factor) {
		Obj conObj = Tab.insert(Obj.Con, "$", factor.struct);
		conObj.setLevel(0);
		
		conObj.setAdr(factor.getValue());
		Code.load(conObj);
	}
	
	public void visit(ReturnExpressionStatement returnStmt) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ReturnEmptyStatement emptyStmt) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(DesignatorVariable var) {
		SyntaxNode parent = var.getParent();
		if (!(parent instanceof DesignatorAssignment || parent instanceof FunctionCall)) {
			Code.load(var.obj);
		}
		
	}
	
	public void visit(DesignatorNamespace var) {
		SyntaxNode parent = var.getParent();
		if (!(parent instanceof DesignatorAssignment || parent instanceof FunctionCall)) {
			Code.load(var.obj);
		}
	}
	
	public void visit(DesignatorAssignment assign) {
		Code.store(assign.getDesignator().obj);
	}
	
	public void visit(FunctionCall function) {
		Obj funcObj = function.getDesignator().obj;
		System.out.println(funcObj.getName());
		int offset = funcObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		if (funcObj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}
	
	public void visit(FactorDesignatorFunction funcFactor) {
		Obj funcObj = funcFactor.getDesignator().obj;
		int offset = funcObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);	
		
		System.out.println("TUU");
	}
	
	public void visit(FactorDesignatorVar varFactor) {
		Obj varObj = varFactor.getDesignator().obj;
		if (varObj.getKind() == Obj.Elem) {
			if (varObj.getType() == Tab.charType) {
				Code.put(Code.baload);
			} else {
				Code.put(Code.aload);
			}
		}
	}
	
	public void visit(ExprMultipleTerm expr) {
		Addop op = expr.getAddop();
		if (op instanceof Add) {
			Code.put(Code.add);
		} else if (op instanceof Subtract) {
			Code.put(Code.sub);
		}
	}
	
	public void visit(ExprSingleTermMinus minusExpr) {
		Code.put(Code.neg);
	}
	
	public void visit(ReadStatement read) {
		Obj obj = read.getDesignator().obj;
		
		if (obj.getType().equals(Tab.charType)) {
			Code.put(Code.bread);
		} else {
			Code.put(Code.read);
		}
		Code.store(obj);
	}
	
	public void visit(DesignatorIncrement inc) {
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(inc.getDesignator().obj);
	}
	
	public void visit(DesignatorDecrement dec) {
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(dec.getDesignator().obj);
	}
	
	public void visit(TermMultipleFactors multipleTerms) {
		Mulop op = multipleTerms.getMulop();
		
		if (op instanceof Multiply) {
			Code.put(Code.mul);
		} else if (op instanceof Divide) {
			Code.put(Code.div);
		} else if (op instanceof Modulo) {
			Code.put(Code.rem);
		}
	}
	
	public void visit(FactorNewArray array) {
		Code.put(Code.newarray);
		int n = (array.getType().struct == Tab.charType ? 0 : 1);
		Code.put(n);
	}
	
	public void visit(SingleExprCond expr) {
		SyntaxNode factor = expr.getParent(), term = factor.getParent();

		if (condTermCount == 1) {
			Code.loadConst(1);
			Code.putFalseJump(Code.eq, 0);
			andConditionPatch.peek().add(Code.pc - 2);
		} else {
			if (term instanceof SingleConditionTerm || term instanceof MultipleConditionTerms) {
				Code.loadConst(1);
				Code.putFalseJump(Code.ne, 0);
				orConditionPatch.peek().add(Code.pc - 2);
			} else {
				Code.loadConst(1);
				Code.putFalseJump(Code.eq, 0);
				termConditionPatch.peek().add(Code.pc - 2);
			}
		}
	}
	
	private int getRelop(Relop op) {
		int relop;
		if (op instanceof Equal) {
			relop = Code.eq;
		} else if (op instanceof NotEqual) {
			relop = Code.ne;
		} else if (op instanceof Greater) {
			relop = Code.gt;
		} else if (op instanceof GreaterOrEqual) {
			relop = Code.ge;
		} else if (op instanceof Less) {
			relop = Code.lt;
		} else {
			relop = Code.le;
		}
		
		return relop;
	}
	
	public void visit(MultipleExprCond expr) {
		SyntaxNode factor = expr.getParent(), term = factor.getParent();
		int relop = getRelop(expr.getRelop());
		
		if (condTermCount == 1) {
			Code.putFalseJump(relop, 0);
			andConditionPatch.peek().add(Code.pc - 2);
		} else {
			if (term instanceof SingleConditionTerm || term instanceof MultipleConditionTerms) {
				Code.put(Code.jcc + relop); 
				Code.put2(0);
				orConditionPatch.peek().add(Code.pc - 2);
			} else {
				Code.putFalseJump(relop, 0);
				termConditionPatch.peek().add(Code.pc - 2);
			}
		}
	}
	
	public void visit(IfStart ifStart) {
		andConditionPatch.push(new ArrayList<Integer>());
		orConditionPatch.push(new ArrayList<Integer>());
		termConditionPatch.push(new ArrayList<Integer>());
		
		Condition condition = ((IfWithCondition)ifStart.getParent()).getCondition();
		
		CondTermCounter counter = new CondTermCounter();
		condition.traverseTopDown(counter);
		
		condTermCount = counter.getCount();
	}
	
	public void visit(IfStatement ifStmt) {
		for (int address : andConditionPatch.peek()) {
			Code.fixup(address);
		}
		andConditionPatch.pop();
		orConditionPatch.pop();
		termConditionPatch.pop();
	}
	
	public void visit(IfElseStatement ifElseStmt) {
		for (int address : andConditionPatch.peek()) {
			Code.fixup(address);
		}
		andConditionPatch.pop();
		orConditionPatch.pop();
		termConditionPatch.pop();
	}
	
	public void visit(ElseStart elseStart) {
		
		Code.putJump(0);
		for (int address : andConditionPatch.peek()) {
			Code.fixup(address);
		}
		andConditionPatch.peek().clear();
		
		andConditionPatch.peek().add(Code.pc - 2);
	}
	
	public void visit(IfWithCondition ifStmt) {
		for (int address : orConditionPatch.peek()) {
			Code.fixup(address);
		}
	}
	
	public void visit(MultipleConditionTerms terms) {
		--condTermCount;
		for (int address : termConditionPatch.peek()) {
			Code.fixup(address);
		}
		termConditionPatch.peek().clear();
	}
	
	public void visit(SingleConditionTerm term) {
		--condTermCount;
		for (int address : termConditionPatch.peek()) {
			Code.fixup(address);
		}
		termConditionPatch.peek().clear();
	}
}
