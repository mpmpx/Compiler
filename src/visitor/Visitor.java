package visitor;

import AST.*;

public class Visitor {
	public void visit(AddOpNode node){};
	public void visit(AParamsNode node){};
	public void visit(ArithExprNode node){};
	public void visit(AssignStatNode node){};
	public void visit(ASTNode node){};
	public void visit(ClassDeclNode node){};
	public void visit(ClassListNode node){};
	public void visit(DataMemberNode node){};
	public void visit(DimListNode node){};
	public void visit(EpsilonNode node){};
	public void visit(ExprNode node){};
	public void visit(FactorNode node){};
	public void visit(FCallNode node){};
	public void visit(ForStatNode node){};
	public void visit(FParamsListNode node){};
	public void visit(FParamsNode node){};
	public void visit(FuncDeclNode node){};
	public void visit(FuncDefListNode node){};
	public void visit(FuncDefNode node){};
	public void visit(GetStatNode node){};
	public void visit(IdNode node){};
	public void visit(IfStatNode node){};
	public void visit(IndexListNode node){};
	public void visit(InherListNode node){};
	public void visit(MembDeclNode node){};
	public void visit(MembListNode node){};
	public void visit(MulOpNode node){};
	public void visit(NotNode node){};
	public void visit(NumNode node){};
	public void visit(ProgNode node){};
	public void visit(PutStatNode node){};
	public void visit(RelExprNode node){};
	public void visit(RelOpNode node){};
	public void visit(ReturnStatNode node){};
	public void visit(ScopeSpecNode node){};
	public void visit(SignNode node){};
	public void visit(StatBlockNode node){};
	public void visit(StatOrVarDeclNode node){};
	public void visit(TermNode node){};
	public void visit(TypeNode node){};
	public void visit(VarDeclNode node){};
	public void visit(VarElementNode node){};
	public void visit(VarNode node){};
}
