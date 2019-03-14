package AST;

import symbolTable.SymbolTable;

public class IdNode extends ASTNode{

	protected String value;
	private SymbolTable symbolTable;
	
	public IdNode(String type, String value) {
		super(type);
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setSymbolTable(SymbolTable table) {
		symbolTable = table;
	}
	
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
}
