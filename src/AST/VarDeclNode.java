package AST;

import visitor.Visitor;

public class VarDeclNode extends ASTNode{

	public VarDeclNode(String type) {
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
