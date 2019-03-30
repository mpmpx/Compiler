package AST;

import visitor.Visitor;

public class IdNode extends ASTNode{
	
	public IdNode(String type, String data, String lineNo) {
		super(type, data, lineNo);
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
