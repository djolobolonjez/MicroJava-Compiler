// generated with ast extension for cup
// version 0.8
// 1/0/2024 22:39:24


package rs.ac.bg.etf.pp1.ast;

public abstract class VisitorAdaptor implements Visitor { 

    public void visit(Mulop Mulop) { }
    public void visit(Relop Relop) { }
    public void visit(ArraySquares ArraySquares) { }
    public void visit(StatementList StatementList) { }
    public void visit(LastVarDecl LastVarDecl) { }
    public void visit(ConstVarDeclList ConstVarDeclList) { }
    public void visit(Addop Addop) { }
    public void visit(ForCondition ForCondition) { }
    public void visit(NamespaceDeclList NamespaceDeclList) { }
    public void visit(Factor Factor) { }
    public void visit(CondTerm CondTerm) { }
    public void visit(MethodTypeAndName MethodTypeAndName) { }
    public void visit(Designator Designator) { }
    public void visit(Term Term) { }
    public void visit(FormParsList FormParsList) { }
    public void visit(ForDesignatorStmtList ForDesignatorStmtList) { }
    public void visit(Condition Condition) { }
    public void visit(DesignatorStmtList DesignatorStmtList) { }
    public void visit(ConstDeclList ConstDeclList) { }
    public void visit(MultipleVarDecl MultipleVarDecl) { }
    public void visit(ActParsList ActParsList) { }
    public void visit(IfCondition IfCondition) { }
    public void visit(VarDeclList VarDeclList) { }
    public void visit(Expr Expr) { }
    public void visit(ActPars ActPars) { }
    public void visit(DesignatorStatement DesignatorStatement) { }
    public void visit(ConstAssign ConstAssign) { }
    public void visit(DesignatorChoice DesignatorChoice) { }
    public void visit(Statement Statement) { }
    public void visit(Type Type) { }
    public void visit(CondFact CondFact) { }
    public void visit(MethodVarDeclList MethodVarDeclList) { }
    public void visit(MethodDeclList MethodDeclList) { }
    public void visit(FormPars FormPars) { }
    public void visit(Modulo Modulo) { visit(); }
    public void visit(Divide Divide) { visit(); }
    public void visit(Multiply Multiply) { visit(); }
    public void visit(Subtract Subtract) { visit(); }
    public void visit(Add Add) { visit(); }
    public void visit(LessOrEqual LessOrEqual) { visit(); }
    public void visit(Less Less) { visit(); }
    public void visit(GreaterOrEqual GreaterOrEqual) { visit(); }
    public void visit(Greater Greater) { visit(); }
    public void visit(NotEqual NotEqual) { visit(); }
    public void visit(Equal Equal) { visit(); }
    public void visit(Assignop Assignop) { visit(); }
    public void visit(Label Label) { visit(); }
    public void visit(ActualParam ActualParam) { visit(); }
    public void visit(ActualParams ActualParams) { visit(); }
    public void visit(NoActualPars NoActualPars) { visit(); }
    public void visit(ActualPars ActualPars) { visit(); }
    public void visit(NoDesignator NoDesignator) { visit(); }
    public void visit(DesignatorExists DesignatorExists) { visit(); }
    public void visit(LastDesignator LastDesignator) { visit(); }
    public void visit(DesignatorListElement DesignatorListElement) { visit(); }
    public void visit(DesignatorUnpacking DesignatorUnpacking) { visit(); }
    public void visit(DesignatorDecrement DesignatorDecrement) { visit(); }
    public void visit(DesignatorIncrement DesignatorIncrement) { visit(); }
    public void visit(FunctionCall FunctionCall) { visit(); }
    public void visit(DesignatorAssignment DesignatorAssignment) { visit(); }
    public void visit(DesignatorListDot DesignatorListDot) { visit(); }
    public void visit(DesignatorListArray DesignatorListArray) { visit(); }
    public void visit(DesignatorVariable DesignatorVariable) { visit(); }
    public void visit(DesignatorNamespace DesignatorNamespace) { visit(); }
    public void visit(FactorExpression FactorExpression) { visit(); }
    public void visit(FactorNewType FactorNewType) { visit(); }
    public void visit(FactorNewArray FactorNewArray) { visit(); }
    public void visit(FactorConstBoolean FactorConstBoolean) { visit(); }
    public void visit(FactorConstChar FactorConstChar) { visit(); }
    public void visit(FactorConstNum FactorConstNum) { visit(); }
    public void visit(FactorDesignatorFunction FactorDesignatorFunction) { visit(); }
    public void visit(FactorDesignatorVar FactorDesignatorVar) { visit(); }
    public void visit(TermSingleFactor TermSingleFactor) { visit(); }
    public void visit(TermMultipleFactors TermMultipleFactors) { visit(); }
    public void visit(ExprSingleTermMinus ExprSingleTermMinus) { visit(); }
    public void visit(ExprSingleTerm ExprSingleTerm) { visit(); }
    public void visit(ExprMultipleTerm ExprMultipleTerm) { visit(); }
    public void visit(MultipleExprCond MultipleExprCond) { visit(); }
    public void visit(SingleExprCond SingleExprCond) { visit(); }
    public void visit(SingleConditionFactor SingleConditionFactor) { visit(); }
    public void visit(MultipleConditionFactors MultipleConditionFactors) { visit(); }
    public void visit(SingleConditionTerm SingleConditionTerm) { visit(); }
    public void visit(MultipleConditionTerms MultipleConditionTerms) { visit(); }
    public void visit(ForNoCondition ForNoCondition) { visit(); }
    public void visit(ForWithCondition ForWithCondition) { visit(); }
    public void visit(ForNoStatements ForNoStatements) { visit(); }
    public void visit(ForSingleStatement ForSingleStatement) { visit(); }
    public void visit(ForMultipleStatements ForMultipleStatements) { visit(); }
    public void visit(ElseStart ElseStart) { visit(); }
    public void visit(IfStart IfStart) { visit(); }
    public void visit(ErrorInIfCondition ErrorInIfCondition) { visit(); }
    public void visit(IfWithCondition IfWithCondition) { visit(); }
    public void visit(ForStart ForStart) { visit(); }
    public void visit(LoopBody LoopBody) { visit(); }
    public void visit(LoopStart LoopStart) { visit(); }
    public void visit(BeforeLoopBody BeforeLoopBody) { visit(); }
    public void visit(StatementError StatementError) { visit(); }
    public void visit(BlockStatementList BlockStatementList) { visit(); }
    public void visit(ForStatement ForStatement) { visit(); }
    public void visit(PrintStatementWidth PrintStatementWidth) { visit(); }
    public void visit(PrintStatementNoWidth PrintStatementNoWidth) { visit(); }
    public void visit(ReadStatement ReadStatement) { visit(); }
    public void visit(ReturnEmptyStatement ReturnEmptyStatement) { visit(); }
    public void visit(ReturnExpressionStatement ReturnExpressionStatement) { visit(); }
    public void visit(ContinueStatement ContinueStatement) { visit(); }
    public void visit(BreakStatement BreakStatement) { visit(); }
    public void visit(IfElseStatement IfElseStatement) { visit(); }
    public void visit(IfStatement IfStatement) { visit(); }
    public void visit(DesignatorStmt DesignatorStmt) { visit(); }
    public void visit(NoStatement NoStatement) { visit(); }
    public void visit(MultipleStatements MultipleStatements) { visit(); }
    public void visit(FormalParamsParenError FormalParamsParenError) { visit(); }
    public void visit(FormalParamsCommaError FormalParamsCommaError) { visit(); }
    public void visit(SingleFormalParam SingleFormalParam) { visit(); }
    public void visit(MultipleFormalParams MultipleFormalParams) { visit(); }
    public void visit(FormParamDecl FormParamDecl) { visit(); }
    public void visit(NoFormalParams NoFormalParams) { visit(); }
    public void visit(FormalParamsList FormalParamsList) { visit(); }
    public void visit(MethodVoidType MethodVoidType) { visit(); }
    public void visit(MethodTypeName MethodTypeName) { visit(); }
    public void visit(NoMethodVariableDeclarations NoMethodVariableDeclarations) { visit(); }
    public void visit(MethodVariableDeclarations MethodVariableDeclarations) { visit(); }
    public void visit(MethodDecl MethodDecl) { visit(); }
    public void visit(NoMethodDeclarations NoMethodDeclarations) { visit(); }
    public void visit(MethodDeclarations MethodDeclarations) { visit(); }
    public void visit(IdentifierWithNamespace IdentifierWithNamespace) { visit(); }
    public void visit(Identifier Identifier) { visit(); }
    public void visit(NoConstDeclarations NoConstDeclarations) { visit(); }
    public void visit(ConstDeclarationList ConstDeclarationList) { visit(); }
    public void visit(NoSquares NoSquares) { visit(); }
    public void visit(Squares Squares) { visit(); }
    public void visit(VariableDeclSemiError VariableDeclSemiError) { visit(); }
    public void visit(LastVariableDecl LastVariableDecl) { visit(); }
    public void visit(VariableDeclCommaError VariableDeclCommaError) { visit(); }
    public void visit(MultipleVariablesDecl MultipleVariablesDecl) { visit(); }
    public void visit(SingleVariable SingleVariable) { visit(); }
    public void visit(MultipleVariables MultipleVariables) { visit(); }
    public void visit(VarDecl VarDecl) { visit(); }
    public void visit(ConstBool ConstBool) { visit(); }
    public void visit(ConstChar ConstChar) { visit(); }
    public void visit(ConstInt ConstInt) { visit(); }
    public void visit(ConstDecl ConstDecl) { visit(); }
    public void visit(NoDeclList NoDeclList) { visit(); }
    public void visit(DeclListForVar DeclListForVar) { visit(); }
    public void visit(DeclListForConst DeclListForConst) { visit(); }
    public void visit(NamespaceStart NamespaceStart) { visit(); }
    public void visit(Namespace Namespace) { visit(); }
    public void visit(NoNamespaceDecl NoNamespaceDecl) { visit(); }
    public void visit(MultipleNamespaceDecl MultipleNamespaceDecl) { visit(); }
    public void visit(ProgramName ProgramName) { visit(); }
    public void visit(Program Program) { visit(); }


    public void visit() { }
}
