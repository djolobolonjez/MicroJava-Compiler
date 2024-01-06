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
	
	Obj indexNode = null, leftIndexNode = null;
	
	private Stack<List<Integer>> andConditionPatch = new Stack<>();
	private Stack<List<Integer>> orConditionPatch = new Stack<>();
	private Stack<List<Integer>> termConditionPatch = new Stack<>();
	private Stack<List<Integer>> loopStartConditionPatch = new Stack<>();
	private Stack<List<Integer>> loopEndConditionPatch = new Stack<>();
	private Stack<List<Integer>> loopBodyPatch = new Stack<>();
	private Stack<List<Integer>> afterLoopConditionPatch = new Stack<>();
	
	private ArrayList<Obj> assignmentDesignators = new ArrayList<>();
	private ArrayList<Obj> assignmentArrayDesignators = new ArrayList<>();
	
	private int condTermCount = 0;
	private boolean loopBegin = false;
	
	public CodeGenerator() {
		// len, chr i ord 
		
		Tab.lenObj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1); Code.put(1);
		Code.put(Code.load_n);
		Code.put(Code.arraylength);
		Code.put(Code.exit);
		Code.put(Code.return_);
		
		Tab.chrObj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1); Code.put(1);
		Code.put(Code.load_n);
		Code.put(Code.exit);
		Code.put(Code.return_);
		
		Tab.ordObj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1); Code.put(1);
		Code.put(Code.load_n);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
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
		if (!(parent instanceof DesignatorAssignment || parent instanceof FunctionCall || 
			  parent instanceof DesignatorExists || parent instanceof DesignatorUnpacking
			)) {
			Code.load(var.obj);
		}
		
		if (parent instanceof DesignatorExists) {
			assignmentDesignators.add(var.obj);
		} else if (parent instanceof DesignatorUnpacking) {
			assignmentArrayDesignators.add(var.obj);
		}
		
	}
	
	public void visit(DesignatorNamespace var) {
		SyntaxNode parent = var.getParent();
		if (!(parent instanceof DesignatorAssignment || parent instanceof FunctionCall || 
			  parent instanceof DesignatorExists || parent instanceof DesignatorUnpacking)) {
			Code.load(var.obj);
		}
		
		if (parent instanceof DesignatorExists) {
			assignmentDesignators.add(var.obj);
		} else if (parent instanceof DesignatorUnpacking) {
			assignmentArrayDesignators.add(var.obj);
		}
	}
	
	public void visit(DesignatorListArray elem) {
		SyntaxNode parent = elem.getParent();
		
		if (parent instanceof DesignatorExists) {
			assignmentDesignators.add(elem.obj);
		} else if (parent instanceof DesignatorUnpacking) {
			assignmentArrayDesignators.add(elem.obj);
		}
	}
	
	public void visit(NoDesignator noDesignator) {
		assignmentDesignators.add(Tab.noObj);
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
		
		if (loopBegin) {
			// skokovi za petlje
			Code.loadConst(1);
			Code.putFalseJump(Code.eq, 0);
			loopEndConditionPatch.peek().add(Code.pc - 2);
			
			Code.putJump(0);
			loopBodyPatch.peek().add(Code.pc - 2);
			afterLoopConditionPatch.peek().add(Code.pc);
			
		} else {
			// skokovi za if-else kontrolne strukture
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
		
		if (loopBegin) {
			Code.putFalseJump(relop, 0);
			loopEndConditionPatch.peek().add(Code.pc - 2);
			Code.putJump(0);
			loopBodyPatch.peek().add(Code.pc - 2);
			afterLoopConditionPatch.peek().add(Code.pc);
		} else {
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
		
	}
	
	public void visit(IfStart ifStart) {
		andConditionPatch.push(new ArrayList<Integer>());
		orConditionPatch.push(new ArrayList<Integer>());
		termConditionPatch.push(new ArrayList<Integer>());
		
		Condition condition = ((IfWithCondition)ifStart.getParent()).getCondition();
		
		CondTermCounter counter = new CondTermCounter();
		condition.traverseTopDown(counter);
		
		condTermCount = counter.getCount();
		
		loopBegin = false;
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
	
	public void visit(ForStart forStart) {
		loopStartConditionPatch.push(new ArrayList<Integer>());
		loopEndConditionPatch.push(new ArrayList<Integer>());
		loopBodyPatch.push(new ArrayList<Integer>());
		afterLoopConditionPatch.push(new ArrayList<Integer>());
		
		loopBegin = true;
	}
	
	public void visit(LoopStart loopStart) {
		loopStartConditionPatch.peek().add(Code.pc);
	}
	
	public void visit(LoopBody loopBody) {
		for (int address : loopBodyPatch.peek()) {
			Code.fixup(address);
		}
	}
	
	public void visit(BeforeLoopBody beforeLoop) {
		int offset = loopStartConditionPatch.peek().get(0) - Code.pc;
		Code.put(Code.jmp);
		Code.put2(offset);
	}
	
	public void visit(ForStatement forStmt) {
		int offset = afterLoopConditionPatch.peek().get(0) - Code.pc;
		Code.put(Code.jmp);
		Code.put2(offset);
		
		for (int address : loopEndConditionPatch.peek()) {
			Code.fixup(address);
		}
		
		loopStartConditionPatch.pop();
		loopEndConditionPatch.pop();
		loopBodyPatch.pop();
		afterLoopConditionPatch.pop();
	}
	
	public void visit(ContinueStatement continueStmt) {
		int offset = afterLoopConditionPatch.peek().get(0) - Code.pc;
		Code.put(Code.jmp);
		Code.put2(offset);
	}
	
	public void visit(BreakStatement breakStmt) {
		Code.putJump(0);
		loopEndConditionPatch.peek().add(Code.pc - 2);
	}
	
	public void visit(DesignatorUnpacking unpack) {
		int index = 0;		
		// poslednji element je visak zbog epsilon smene pre *designator
		assignmentDesignators.remove(assignmentDesignators.size() - 1);
		
		System.out.println(assignmentDesignators.size());
		
		Obj arrayNode = assignmentArrayDesignators.get(1);
		Obj leftArrayNode = assignmentArrayDesignators.get(0);
		
		// provera za duzinu niza
		Code.loadConst(assignmentDesignators.size());
		Code.load(leftArrayNode);
		Code.put(Code.arraylength);
		Code.put(Code.add);
		Code.load(arrayNode);
		Code.put(Code.arraylength);
		Code.putFalseJump(Code.gt, Code.pc + 5);
		Code.put(Code.trap);
		Code.put(Code.const_1);
		
		for (int i = 0; i < assignmentDesignators.size(); i++) {
			Obj obj = assignmentDesignators.get(i);
			if (obj == Tab.noObj) {
				System.out.println("INDEKS: " + i);
				continue;
			}
			
			if (obj.getKind() != Obj.Elem) {
				Code.load(arrayNode);
				Code.loadConst(i);
				if (arrayNode.getType() == Tab.charType) {
					Code.put(Code.baload);
				} else {
					Code.put(Code.aload);
				}
				Code.store(obj);
			}
		}
		
		for (int i = assignmentDesignators.size() - 1; i >= 0; i--) {
			Obj obj = assignmentDesignators.get(i);
			if (obj == Tab.noObj) {
				continue;
			}
			
			if (obj.getKind() == Obj.Elem) {
				Code.load(arrayNode);
				Code.loadConst(i);
				if (arrayNode.getType() == Tab.charType) {
					Code.put(Code.baload);
				} else {
					Code.put(Code.aload);
				}
				Code.store(obj);
			}
		}
		
		indexNode = Helper.getInstance().getIndexNode();
		leftIndexNode = Helper.getInstance().getLeftIndexNode();
		
		index = assignmentDesignators.size();
		int leftIndex = 0;
		
		Code.loadConst(index);
		Code.put(Code.putstatic);
		Code.put2(indexNode.getAdr());
		Code.loadConst(leftIndex);
		Code.put(Code.putstatic);
		Code.put2(leftIndexNode.getAdr());
		
		int indexCheck = Code.pc;
		
		Code.load(arrayNode);
		Code.put(Code.arraylength);
		Code.put(Code.getstatic);
		Code.put2(indexNode.getAdr());
		Code.putFalseJump(Code.ne, 0);
		
		int fixAddress = Code.pc - 2;
		
		Code.load(leftArrayNode);
		Code.put(Code.getstatic);
		Code.put2(leftIndexNode.getAdr());
		Code.load(arrayNode);
		Code.put(Code.getstatic);
		Code.put2(indexNode.getAdr());
		
		if (arrayNode.getType() == Tab.charType) {
			Code.put(Code.baload);
			Code.put(Code.bastore);
		} else {
			Code.put(Code.aload);
			Code.put(Code.astore);
		}
		
		Code.put(Code.getstatic);
		Code.put2(leftIndexNode.getAdr());
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.putstatic);
		Code.put2(leftIndexNode.getAdr());
		
		Code.put(Code.getstatic);
		Code.put2(indexNode.getAdr());
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.putstatic);
		Code.put2(indexNode.getAdr());
		
		Code.putJump(indexCheck);
		
		Code.fixup(fixAddress);
		
		assignmentDesignators.clear();
		assignmentArrayDesignators.clear();
	}
}
