package AST;

public class NumNode extends ASTNode{

	String value;
	public NumNode(String type, String value) {
		super(type);
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
