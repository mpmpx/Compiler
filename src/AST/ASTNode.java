package AST;

import java.util.ArrayList;

import symbolTable.Entry;
import symbolTable.SymbolTable;
import visitor.Visitor;

public class ASTNode {
	protected ASTNode parent;
	protected ASTNode leftmostSibling;
	protected ASTNode rightSibling;
	protected ASTNode leftmostChild;
	
	protected SymbolTable symTab;
	protected Entry entry;
	
	protected String dataType;
	protected String type;
	protected String data;
	protected int lineNo;
	protected ArrayList<String> dimList;
	
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
	
	public ASTNode(String type, String data) {
		parent = null;
		leftmostSibling = this;
		rightSibling = null;
		leftmostChild = null;
		this.type = type;
		this.data = data;
	}
	 
	public ASTNode(String type, String data, String lineNo) {
		parent = null;
		leftmostSibling = this;
		rightSibling = null;
		leftmostChild = null;
		this.type = type;
		this.data = data;
		this.lineNo = Integer.parseInt(lineNo);
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
	
	public void accept(Visitor visitor) {
		ASTNode childNode = this.leftmostChild;
		
		while (childNode != null) {
			childNode.accept(visitor);
			childNode = childNode.next();
		}
		
		visitor.visit(this);
	}
	
	public ArrayList<String> getDimList() {
		return dimList;
	}
	
	public void setDimList(ArrayList<String> list) {
		dimList = list;
	}
	
	public void addDim(String s) {
		if (dimList == null) {
			dimList = new ArrayList<String>();
		}
		dimList.add(s);
	}
	
	public void setDataType(String type) {
		this.dataType = type;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public String getType() {
		return type;
	}
	
	public String getData() {
		return data;
	}
	
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}
	
	public int getLineNo() {
		return lineNo;
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
	
	public ASTNode getChild(int index) {
		return leftmostChild.getSibling(index);
	}
	
	private ASTNode getSibling(int index) {
		if (index == 0) {
			return this;
		}
		else {
			return rightSibling.getSibling(index - 1);
		}
	}
	
	public ASTNode parent() {
		return parent;
	}
	
	public ASTNode next() {
		return rightSibling;
	}
	
	public void setSymbolTable(SymbolTable symbTab) {
		this.symTab = symbTab;
	}
	
	public SymbolTable getSymbolTable() {
		return this.symTab;
	}
	
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	public Entry getEntry() {
		return this.entry;
	}
}