package AST;

public class IdNode extends ASTNode{

	protected String value;
	
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
}
