package AST;

import visitor.Visitor;

public class AssignStatNode extends ASTNode{

	public AssignStatNode(String v) {
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
