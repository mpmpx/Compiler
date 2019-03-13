package AST;

public class EpsilonNode extends ASTNode{

	public EpsilonNode() {
		super("Epsilon");
	}

	
	public static void main(String[] args) {
		ASTNode node = new EpsilonNode();
		System.out.println(node.getClass().equals(ASTNode.class));
	}
}
