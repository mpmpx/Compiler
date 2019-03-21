package AST;

import visitor.Visitor;

public class NumNode extends ASTNode{

	public NumNode(String type, String data, String dataType, String lineNo) {
		super(type, data, lineNo);
		super.setDataType(dataType);
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
