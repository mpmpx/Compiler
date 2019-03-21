package AST;

import visitor.Visitor;

public class ClassDeclNode extends ASTNode {

	public ClassDeclNode(String type) {
		super(type);
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
