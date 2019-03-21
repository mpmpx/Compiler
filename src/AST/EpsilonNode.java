package AST;

import visitor.Visitor;

public class EpsilonNode extends ASTNode{

	public EpsilonNode() {
		super("Epsilon");
	}

	public void accept(Visitor visitor) {
		ASTNode childNode = this.leftmostChild;
		
		while (childNode != null) {
			childNode.accept(visitor);
			childNode = childNode.next();
		}
		
		visitor.visit(this);
	}
	
	public static void main(String[] args) {
		ASTNode node = new EpsilonNode();
		System.out.println(node.getClass().equals(ASTNode.class));
	}
	
	
}
