package AST;

import token.TokenType;

public class ASTNode {
	protected ASTNode parent;
	protected ASTNode leftmostSibling;
	protected ASTNode rightSibling;
	protected ASTNode leftmostChild;
	
	protected String value;
	
	public ASTNode(String v) {
		parent = null;
		leftmostSibling = this;
		rightSibling = null;
		leftmostChild = null;
		value = v;
	}
	
	public void print(int level) {
		
		System.out.println("level " + level + ": " + value);
		
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
	
}