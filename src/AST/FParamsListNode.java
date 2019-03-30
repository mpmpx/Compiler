package AST;

import visitor.Visitor;

public class FParamsListNode extends ASTNode{

	public FParamsListNode(String v) {
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
