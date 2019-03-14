package AST;

public class AST {

	private ASTNode root;
	
	public AST(ASTNode root) {
		 this.root = root;
	}
	
	public void setRoot(ASTNode root) {
		this.root = root;
	}
	
	public static ASTNode makeNode() {
		return new EpsilonNode();
	}
	
	public static ASTNode makeNode(String op, String... value) {
		switch (op) {
			case "addOp": return new AddOpNode(op);
			case "aParams": return new AParamsNode(op);
			case "arithExpr": return new ArithExprNode(op);
			case "assignStat": return new AssignStatNode(op);
			case "classDecl": return new ClassDeclNode(op);
			case "classList": return new ClassListNode(op);
			case "dataMember": return new DataMemberNode(op);
			case "dimList": return new DimListNode(op);
			case "expr": return new ExprNode(op);
			case "factor": return new FactorNode(op);
			case "fCall": return new FCallNode(op);
			case "forStat": return new ForStatNode(op);
			case "fParams": return new FParamsNode(op);
			case "fParamsList": return new FParamsListNode(op);
			case "funcDecl": return new FuncDeclNode(op);
			case "funcDefList": return new FuncDefListNode(op);
			case "funcDef": return new FuncDefNode(op);
			case "getStat": return new GetStatNode(op);
			case "id": return new IdNode(op, value[0]);
			case "ifStat": return new IfStatNode(op);
			case "indexList": return new IndexListNode(op);
			case "inherList": return new InherListNode(op);
			case "membDecl": return new MembDeclNode(op);
			case "membList": return new MembListNode(op);
			case "mulOp": return new MulOpNode(op);
			case "not": return new NotNode(op);
			case "num": return new NumNode(op, value[0]);
			case "prog": return new ProgNode(op);
			case "putStat": return new PutStatNode(op);
			case "relExpr": return new RelExprNode(op);
			case "relOp": return new RelOpNode(op);
			case "returnStat": return new ReturnStatNode(op);
			case "scopeSpec": return new ScopeSpecNode(op);
			case "sign": return new SignNode(op);
			case "statBlock": return new StatBlockNode(op);
			case "stat": return new StatNode(op);
			case "statOrVarDecl": return new StatOrVarDeclNode(op);
			case "term": return new TermNode(op);
			case "type": return new TypeNode(op);
			case "varDecl": return new VarDeclNode(op);
			case "varElement": return new VarElementNode(op);
			case "var": return new VarNode(op);
			default: System.out.println("Error when making the node: " + op); return null;
		}
	}
	
	public static ASTNode makeFamily(String op, ASTNode... nodes) {
		ASTNode parentNode = makeNode(op);
		ASTNode leftmostChild = nodes[0];
		for (int i = 1; i < nodes.length; i++){
			leftmostChild.makeSibling(nodes[i]);
		}
		parentNode.adoptChildren(leftmostChild);
		return parentNode;
	}
	
	public void print() {
		root.print(0);
	}
	
	public static void main(String[] args) {
		
		ASTNode node1 = new ASTNode("1");
		ASTNode node2 = new ASTNode("2");
		ASTNode node3 = new ASTNode("3");
		
		ASTNode node4 = new ASTNode("4");
		ASTNode node5 = new ASTNode("5");
		node4.makeSibling(node5);
		node1.adoptChildren(node5);
		
		ASTNode root = makeFamily("root", node1, node2, node3);
		
		root.print(1);
	}
}
