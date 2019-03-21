package AST;

import visitor.Visitor;

public class FuncDefNode extends ASTNode{

	public FuncDefNode(String type) {
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
