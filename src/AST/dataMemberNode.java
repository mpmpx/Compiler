package AST;

import visitor.Visitor;

public class DataMemberNode extends ASTNode{

	public DataMemberNode(String v) {
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
