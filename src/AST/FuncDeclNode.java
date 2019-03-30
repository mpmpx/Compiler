package AST;

import visitor.Visitor;

public class FuncDeclNode extends ASTNode{

	public FuncDeclNode(String type) {
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
