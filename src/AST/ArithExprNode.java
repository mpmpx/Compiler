package AST;

import visitor.Visitor;

public class ArithExprNode extends ASTNode{

	public ArithExprNode(String v) {
		super(v);
	}

	public void accept(Visitor visitor) {
		ASTNode childNode = this.leftmostChild;
		
		while (childNode != null) {
			childNode.accept(visitor);
			childNode = childNode.next();
		}
		
		visitor.visit(this);
	}
}
