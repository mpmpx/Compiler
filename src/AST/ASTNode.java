package AST;

import token.TokenType;

public class ASTNode {
	protected ASTNode parent;
	protected ASTNode leftmostSibling;
	protected ASTNode rightSibling;
	protected ASTNode leftmostChild;
	
	protected String type;
	
	public ASTNode() {
		parent = null;
		leftmostSibling = this;
		rightSibling = null;
		leftmostChild = null;
	}
	
	public ASTNode(String type) {
		parent = null;
		leftmostSibling = this;
		rightSibling = null;
		leftmostChild = null;
		this.type = type;
	}
	
	
	public void print(int level) {
		
		//System.out.println("level " + level + ": " + type);
		
		for (int i = 0; i < level; i++) {
			System.out.print("-");
		}
		System.out.println(type);
		
		if (leftmostChild != null) {
			leftmostChild.print(level + 1);
		}
		
		if (rightSibling != null) {
			rightSibling.print(level);
		}
	}
	
	public ASTNode makeSibling(ASTNode newNode) {
		ASTNode currentNode = this;
		
		if (newNode == null) {
			return this;
		}
		
		while (currentNode.rightSibling != null) {
			currentNode = currentNode.rightSibling;
		}
		currentNode.rightSibling = newNode.leftmostSibling;
		currentNode = newNode.leftmostSibling;
		while (currentNode != null) {
			currentNode.parent = parent;
			currentNode.leftmostSibling = leftmostSibling;
			currentNode = currentNode.rightSibling;
		}
		
		return this;
	}
	
	public void adoptChildren(ASTNode newNode) {
		if (newNode == null) {
			return;
		}
		
		if (leftmostChild != null) {
			leftmostChild.makeSibling(newNode);
		}
		else {
			ASTNode currentNode = newNode.leftmostSibling;
			leftmostChild = currentNode;
			while (currentNode != null) {
				currentNode.parent = this;
				currentNode = currentNode.rightSibling;
			}
		}
	}
	
	public ASTNode getLeftmostChild() {
		return leftmostChild;
	}
}