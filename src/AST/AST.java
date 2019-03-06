package AST;

public class AST {

	public AST() {
		 
	}
	
	public static ASTNode makeNode() {
		return new ASTNode();
	}
	
	public static ASTNode makeNode(String op) {
		return new ASTNode(op);
	}
	
	public static ASTNode makeFamily(String op, ASTNode... nodes) {
		ASTNode parentNode = makeNode(op);
		ASTNode leftmostChild = nodes[0];
		if (leftmostChild == null) {
			return null;
		}
		
		for (int i = 1; i < nodes.length; i++){
			leftmostChild.makeSibling(nodes[i]);
		}
		parentNode.adoptChildren(leftmostChild);
		return parentNode;
	}
	
	
	public static void main(String[] args) {
		
		ASTNode node1 = new ASTNode("1");
		ASTNode node2 = new ASTNode("2");
		ASTNode node3 = new ASTNode("3");
		
		ASTNode node4 = new ASTNode("4");
		ClassDeclNode node5 = new ClassDeclNode("5");
		node4.makeSibling(node5);
		node1.adoptChildren(node5);
		
		ASTNode root = makeFamily("root", node1, node2, node3);
		
		root.print(1);
	}
}
