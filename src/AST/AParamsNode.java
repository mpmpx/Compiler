package AST;

import visitor.Visitor;

public class AParamsNode extends ASTNode{

	public AParamsNode(String v) {
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
