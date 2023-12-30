// generated with ast extension for cup
// version 0.8
// 30/11/2023 18:29:45


package rs.ac.bg.etf.pp1.ast;

public interface Visitor { 

    public void visit(Mulop Mulop);
    public void visit(Relop Relop);
    public void visit(ArraySquares ArraySquares);
    public void visit(StatementList StatementList);
    public void visit(LastVarDecl LastVarDecl);
    public void visit(ConstVarDeclList ConstVarDeclList);
    public void visit(Addop Addop);
    public void visit(ForCondition ForCondition);
    public void visit(NamespaceDeclList NamespaceDeclList);
    public void visit(Factor Factor);
    public void visit(CondTerm CondTerm);
    public void visit(MethodTypeAndName MethodTypeAndName);
    public void visit(Designator Designator);
    public void visit(Term Term);
    public void visit(FormParsList FormParsList);
    public void visit(ForDesignatorStmtList ForDesignatorStmtList);
    public void visit(Condition Condition);
    public void visit(DesignatorStmtList DesignatorStmtList);
    public void visit(ConstDeclList ConstDeclList);
    public void visit(MultipleVarDecl MultipleVarDecl);
    public void visit(ActParsList ActParsList);
    public void visit(IfCondition IfCondition);
    public void visit(VarDeclList VarDeclList);
    public void visit(Expr Expr);
    public void visit(ActPars ActPars);
    public void visit(DesignatorStatement DesignatorStatement);
    public void visit(ConstAssign ConstAssign);
    public void visit(Statement Statement);
    public void visit(Type Type);
    public void visit(CondFact CondFact);
    public void visit(MethodVarDeclList MethodVarDeclList);
    public void visit(MethodDeclList MethodDeclList);
    public void visit(FormPars FormPars);
    public void visit(Modulo Modulo);
    public void visit(Divide Divide);
    public void visit(Multiply Multiply);
    public void visit(Subtract Subtract);
    public void visit(Add Add);
    public void visit(LessOrEqual LessOrEqual);
    public void visit(Less Less);
    public void visit(GreaterOrEqual GreaterOrEqual);
    public void visit(Greater Greater);
    public void visit(NotEqual NotEqual);
    public void visit(Equal Equal);
    public void visit(Assignop Assignop);
    public void visit(Label Label);
    public void visit(ActualParam ActualParam);
    public void visit(ActualParams ActualParams);
    public void visit(NoActualPars NoActualPars);
    public void visit(ActualPars ActualPars);
    public void visit(NoDesignatorStmtList NoDesignatorStmtList);
    public void visit(DesignatorListElement DesignatorListElement);
    public void visit(DesignatorListEmpty DesignatorListEmpty);
    public void visit(DesignatorUnpacking DesignatorUnpacking);
    public void visit(DesignatorDecrement DesignatorDecrement);
    public void visit(DesignatorIncrement DesignatorIncrement);
    public void visit(FunctionCall FunctionCall);
    public void visit(DesignatorAssignment DesignatorAssignment);
    public void visit(DesignatorListDot DesignatorListDot);
    public void visit(DesignatorListArray DesignatorListArray);
    public void visit(DesignatorVariable DesignatorVariable);
    public void visit(DesignatorNamespace DesignatorNamespace);
    public void visit(FactorExpression FactorExpression);
    public void visit(FactorNewType FactorNewType);
    public void visit(FactorNewArray FactorNewArray);
    public void visit(FactorConstBoolean FactorConstBoolean);
    public void visit(FactorConstChar FactorConstChar);
    public void visit(FactorConstNum FactorConstNum);
    public void visit(FactorDesignatorFunction FactorDesignatorFunction);
    public void visit(FactorDesignatorVar FactorDesignatorVar);
    public void visit(TermSingleFactor TermSingleFactor);
    public void visit(TermMultipleFactors TermMultipleFactors);
    public void visit(ExprSingleTermMinus ExprSingleTermMinus);
    public void visit(ExprSingleTerm ExprSingleTerm);
    public void visit(ExprMultipleTerm ExprMultipleTerm);
    public void visit(MultipleExprCond MultipleExprCond);
    public void visit(SingleExprCond SingleExprCond);
    public void visit(SingleConditionFactor SingleConditionFactor);
    public void visit(MultipleConditionFactors MultipleConditionFactors);
    public void visit(SingleConditionTerm SingleConditionTerm);
    public void visit(MultipleConditionTerms MultipleConditionTerms);
    public void visit(ForNoCondition ForNoCondition);
    public void visit(ForWithCondition ForWithCondition);
    public void visit(ForNoStatements ForNoStatements);
    public void visit(ForSingleStatement ForSingleStatement);
    public void visit(ForMultipleStatements ForMultipleStatements);
    public void visit(ElseStart ElseStart);
    public void visit(IfStart IfStart);
    public void visit(ErrorInIfCondition ErrorInIfCondition);
    public void visit(IfWithCondition IfWithCondition);
    public void visit(ForStart ForStart);
    public void visit(LoopBody LoopBody);
    public void visit(LoopStart LoopStart);
    public void visit(BeforeLoopBody BeforeLoopBody);
    public void visit(StatementError StatementError);
    public void visit(BlockStatementList BlockStatementList);
    public void visit(ForStatement ForStatement);
    public void visit(PrintStatementWidth PrintStatementWidth);
    public void visit(PrintStatementNoWidth PrintStatementNoWidth);
    public void visit(ReadStatement ReadStatement);
    public void visit(ReturnEmptyStatement ReturnEmptyStatement);
    public void visit(ReturnExpressionStatement ReturnExpressionStatement);
    public void visit(ContinueStatement ContinueStatement);
    public void visit(BreakStatement BreakStatement);
    public void visit(IfElseStatement IfElseStatement);
    public void visit(IfStatement IfStatement);
    public void visit(DesignatorStmt DesignatorStmt);
    public void visit(NoStatement NoStatement);
    public void visit(MultipleStatements MultipleStatements);
    public void visit(FormalParamsParenError FormalParamsParenError);
    public void visit(FormalParamsCommaError FormalParamsCommaError);
    public void visit(SingleFormalParam SingleFormalParam);
    public void visit(MultipleFormalParams MultipleFormalParams);
    public void visit(FormParamDecl FormParamDecl);
    public void visit(NoFormalParams NoFormalParams);
    public void visit(FormalParamsList FormalParamsList);
    public void visit(MethodVoidType MethodVoidType);
    public void visit(MethodTypeName MethodTypeName);
    public void visit(NoMethodVariableDeclarations NoMethodVariableDeclarations);
    public void visit(MethodVariableDeclarations MethodVariableDeclarations);
    public void visit(MethodDecl MethodDecl);
    public void visit(NoMethodDeclarations NoMethodDeclarations);
    public void visit(MethodDeclarations MethodDeclarations);
    public void visit(IdentifierWithNamespace IdentifierWithNamespace);
    public void visit(Identifier Identifier);
    public void visit(NoConstDeclarations NoConstDeclarations);
    public void visit(ConstDeclarationList ConstDeclarationList);
    public void visit(NoSquares NoSquares);
    public void visit(Squares Squares);
    public void visit(VariableDeclSemiError VariableDeclSemiError);
    public void visit(LastVariableDecl LastVariableDecl);
    public void visit(VariableDeclCommaError VariableDeclCommaError);
    public void visit(MultipleVariablesDecl MultipleVariablesDecl);
    public void visit(SingleVariable SingleVariable);
    public void visit(MultipleVariables MultipleVariables);
    public void visit(VarDecl VarDecl);
    public void visit(ConstBool ConstBool);
    public void visit(ConstChar ConstChar);
    public void visit(ConstInt ConstInt);
    public void visit(ConstDecl ConstDecl);
    public void visit(NoDeclList NoDeclList);
    public void visit(DeclListForVar DeclListForVar);
    public void visit(DeclListForConst DeclListForConst);
    public void visit(NamespaceStart NamespaceStart);
    public void visit(Namespace Namespace);
    public void visit(NoNamespaceDecl NoNamespaceDecl);
    public void visit(MultipleNamespaceDecl MultipleNamespaceDecl);
    public void visit(ProgramName ProgramName);
    public void visit(Program Program);

}
