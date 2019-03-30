package visitor;

import java.util.ArrayList;
import java.util.LinkedList;
import AST.*;
import symbolTable.Kind;
import symbolTable.SymbolTable;

public class TypeCheckingVisitor extends Visitor {
	
	private SymbolTable globalTable;

	
	public void setGlobalTable(SymbolTable table) {
		globalTable = table;
	}
	
	public void visit(VarDeclNode node) {
		String type = node.getChild(0).getData();
		ASTNode currentNode = node;
		SymbolTable symbTab = null;
		
		while (symbTab == null) {

			currentNode = currentNode.parent();
			symbTab = currentNode.getSymbolTable();
		}
		if (symbTab.getName() == null) {
			symbTab = currentNode.parent().getSymbolTable();
		}
		symbTab = symbTab.parent();
		
		if (!type.equals("integer") && !type.equals("float")) {
			if (symbTab.lookup(type) == null) {
				System.out.println("Semantic Error: class \"" + type + "\" is not defined at line " + node.getChild(1).getLineNo() + ".");
			}
			else if (!symbTab.lookup(type).getKind().equals(Kind.Class)) {
				System.out.println("Semantic Error: class \"" + type + "\" is not defined at line " + node.getChild(1).getLineNo() + ".");
			}
		}
	}
	
	public void visit(ClassDeclNode node) {
		ASTNode currentNode = node;
		SymbolTable symbTab =  currentNode.getSymbolTable();
		while (symbTab == null || !symbTab.getName().equals("global")) {
			currentNode = currentNode.parent();
			symbTab = currentNode.getSymbolTable();
		}
		ArrayList<String> className = new ArrayList<String>();
		currentNode = node;
		symbTab =  currentNode.getSymbolTable();
		while (symbTab != null) {
			if (className.contains(symbTab.getName())) {
				System.out.println("Semantic Error: circular class denpendencies at line " + node.getChild(0).getLineNo() + ".");
				break;
			}
			
			className.add(symbTab.getName());
			symbTab = symbTab.parent();
		}
	}
	
	public void visit(TypeNode node) {
		node.setDataType(node.getData());
	}
	
	public void visit(VarNode node) {
		ASTNode currentNode = node;
		SymbolTable symbTab = null;
		
		while (symbTab == null) {
			currentNode = currentNode.parent();
			symbTab = currentNode.getSymbolTable();
			if (symbTab != null && symbTab.getName() == null) {
				symbTab = null;
			}
		}
		
		int lastIndex = 0;
		currentNode = node.getChild(0);
		while (currentNode != null) {
			lastIndex++;
			currentNode = currentNode.next();
		}
		
		if (lastIndex > 1) {
			currentNode = node.getChild(0);
			while (!currentNode.getClass().equals(IdNode.class)) {
				currentNode = currentNode.getChild(0);
			}
		
			String data = currentNode.getData();
			if (symbTab.lookup(data) == null) {
				System.out.println("Semantic Error: " + data + " is not defined at line " + currentNode.getLineNo() + ".");
				node.setDataType("errorType");
				return;
			}
			else if (symbTab.lookup(data).getType()[0].equals("integer") || symbTab.lookup(data).getType()[0].equals("float")) {
				System.out.println("Semantic Error: " + data + " is not a class variable at line " + currentNode.getLineNo() + ".");
				node.setDataType("errorType");
				return;
			}
			
			currentNode = node.getChild(1);
			while (!currentNode.getClass().equals(IdNode.class)) {
				currentNode = currentNode.getChild(0);
			}
			String data2 = currentNode.getData();
			
			if (globalTable.lookup(symbTab.lookup(data).getType()[0]).link().lookup(data2) == null) {
				System.out.println("Semantic Error: " + data2 + " is not defined as a member of class \"" + 
						symbTab.parent().lookup(symbTab.lookup(data).getType()[0]).getName() + "\" at line " + currentNode.getLineNo() + ".");
			}
			else {
				node.setDataType(symbTab.parent().lookup(symbTab.lookup(data).getType()[0]).link().lookup(data2).getType()[0]);
				node.setData(node.getChild(0).getData() + "." + node.getChild(1).getData());
			}
			
		}
		else {
			currentNode = node.getChild(0);
			while (!currentNode.getClass().equals(IdNode.class)) {
				currentNode = currentNode.getChild(0);
			}
		
			String data = currentNode.getData();
			node.setData(data);
		
			if (symbTab.lookup(data) == null) {
				System.out.println("Semantic Error: " + data + " is not defined at line " + currentNode.getLineNo() + ".");
				node.setDataType("errorType");
			}
			else {
				node.setDataType(symbTab.lookup(data).getType()[0]);
			}
			node.setLineNo(node.getLeftmostChild().getLineNo());
		}
	}
	
	public void visit(VarElementNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
	}
	
	public void visit(RelExprNode node) {
		String leftType = node.getChild(0).getDataType();
		String rightType = node.getChild(2).getDataType();
		
		if (leftType == null) {
			leftType = "unknown";
		}
		
		if (rightType == null) {
			rightType = "unknown";
		}
		
		if (leftType.indexOf('[') != -1) {
			leftType = leftType.substring(0, leftType.indexOf('['));
		}
		
		if (rightType.indexOf('[') != -1) {
			rightType = rightType.substring(0, rightType.indexOf('['));
		}
		if (!leftType.equals(rightType)) {
			System.out.println("RelExprNode Error: " + node.getChild(0).getData() + "(" + leftType + ") and "
					+ node.getChild(2).getData() + "(" + rightType + ").");
		}
	}
	
	public void visit(ExprNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
	}
	
	public void visit(ArithExprNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
	}
	
	public void visit(AssignStatNode node) {
		String leftType = node.getChild(0).getDataType();
		String rightType = node.getChild(1).getDataType();
		
		if (leftType == null) {
			leftType = "unknown";
		}
		
		if (rightType == null) {
			rightType = "unknown";
		}
		
		if (leftType.indexOf('[') != -1) {
			leftType = leftType.substring(0, leftType.indexOf('['));
		}
		
		if (rightType.indexOf('[') != -1) {
			rightType = rightType.substring(0, rightType.indexOf('['));
		}
		
		
		if (!leftType.equals(rightType)) {
			System.out.println("AssignStat Error: " + node.getChild(0).getData() + "(" + leftType + ") and "
					+ node.getChild(1).getData() + "(" + rightType + ").");
		}
	}
	
	public void visit(AddOpNode node) {
		String leftType = node.getChild(0).getDataType();
		String rightType = node.getChild(1).getDataType();
		if (leftType.indexOf('[') != -1) {
			leftType = leftType.substring(0, leftType.indexOf('['));
		}
		
		if (rightType.indexOf('[') != -1) {
			rightType = rightType.substring(0, rightType.indexOf('['));
		}
		
		if (!leftType.equals(rightType)) {
			System.out.println("AddOpNode Error: " + node.getChild(0).getData() + "(" + leftType + ") and "
					+ node.getChild(1).getData() + "(" + rightType + ").");
			node.setDataType("errorType");
		} 
		else {
			node.setDataType(leftType);
		}
		
		node.setData(node.getChild(0).getData() + " + " + node.getChild(1).getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
	}
	
	public void visit(MulOpNode node) {
		String leftType = node.getChild(0).getDataType();
		String rightType = node.getChild(1).getDataType();
		if (leftType.indexOf('[') != -1) {
			leftType = leftType.substring(0, leftType.indexOf('['));
		}
		
		if (rightType.indexOf('[') != -1) {
			rightType = rightType.substring(0, rightType.indexOf('['));
		}
		
		if (!leftType.equals(rightType)) {
			System.out.println("MulOpNode Error: " + node.getChild(0).getData() + "(" + leftType + ") and "
					+ node.getChild(1).getData() + "(" + rightType + ").");
			node.setDataType("errorType");
		} 
		else {
			node.setDataType(leftType);
		}
		
		node.setData(node.getChild(0).getData() + " * " + node.getChild(1).getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
	}
	
	public void visit(FCallNode node) {
		ASTNode currentNode = node;
		String funcName = node.getChild(0).getData();
		SymbolTable symbTab = null;
		LinkedList<String> paramsType = new LinkedList<String>();
		String[] funcDeclType = null;
		
		node.setData(funcName);
		
		// Find the symbol table of current scope.
		while(symbTab == null || symbTab.getName() == null) {
			currentNode = currentNode.parent();
			symbTab = currentNode.getSymbolTable();
		}
		if (symbTab.getName().equals("program")) {
			symbTab = currentNode.parent().getSymbolTable();
		}

		// Check whether the function is defined. If so, get its type.
		if (symbTab.lookup(funcName) != null) {
			// Make sure that the find the correct scope of the function
			symbTab = symbTab.lookup(funcName).link().parent();
			// member function is not defined.
			if (symbTab == null) {
				return;
			}
			
			funcDeclType = symbTab.lookup(funcName).getType();
			node.setDataType(funcDeclType[0]);
			node.setLineNo(node.getChild(0).getLineNo());
		}
		else {
			System.out.println("Semantic Error: function " + funcName + " is not defined at line " + node.getChild(0).getLineNo() + ".");
			return;
		}
		
		currentNode = node.getChild(1).getLeftmostChild();
		while (!currentNode.getClass().equals(EpsilonNode.class)) {
			paramsType.add(currentNode.getDataType());
			currentNode = currentNode.next();
		}
		
		int length = 1;
		if (funcDeclType[1].equals("nil")) {
			length = 2;
		}
		
		if (paramsType.size() != funcDeclType.length - length) {
			System.out.println("Semantic Error: number of parameters of function " + funcName 
					+ " is " + paramsType.size() + " at line " + node.getChild(0).getLineNo() + ". Excepted " + (funcDeclType.length - length ) + ".");
		}
		else {
			for (int i = 0; i < paramsType.size(); i++) {
				if (!paramsType.get(i).equals(funcDeclType[i + 1])) {
					System.out.print("Semantic Error: type of parameters of function " + funcName + " is (");
					System.out.print(paramsType.get(0));
					for (int j = 1; j < paramsType.size(); j++) {
						System.out.print(", " + paramsType.get(j));
					}
					System.out.print(") at line " + node.getChild(0).getLineNo() + ". Expected (");
					System.out.print(funcDeclType[1]);
					for (int j = 2; j < funcDeclType.length; j++) {
						System.out.print(", " + funcDeclType[j]);
					}
					System.out.println(").");
				}
			}
		}
	}
	
	public void visit(DataMemberNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
		node.setDimList(node.getLeftmostChild().getDimList());
		SymbolTable symbTab = null;
		
		ASTNode currentNode = node.getChild(1).getLeftmostChild();
		int indexNum = 0;
		while (!currentNode.getClass().equals(EpsilonNode.class)) {
			indexNum++;
			currentNode = currentNode.next();
		}
		
		// Find the symbol table of current scope.
		while(symbTab == null || symbTab.getName() == null) {
			currentNode = currentNode.parent();
			symbTab = currentNode.getSymbolTable();
		}
		if (symbTab.getName() == null) {
			symbTab = currentNode.parent().getSymbolTable();
		}
		
		if (symbTab.lookup(node.getLeftmostChild().getData()) != null) {
			ArrayList<String> dimList = symbTab.lookup(node.getLeftmostChild().getData()).getDimList();
			int declIndexNum = 0;
			
			if (dimList != null) {
				declIndexNum = dimList.size();
			}
			
			if (indexNum > declIndexNum) {
				System.out.println("Semantic Error: number of dimensions of variable \"" + node.getLeftmostChild().getData() + "\" is "
						+ indexNum + " at line " + node.getLineNo() + ". Expected " + declIndexNum + "." );
			}
		
		}
	}
	
	public void visit(TermNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
		node.setDimList(node.getLeftmostChild().getDimList());
	}
	
	public void visit(FactorNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
		node.setDimList(node.getLeftmostChild().getDimList());
	}
	
	public void visit(NotNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
		node.setDimList(node.getLeftmostChild().getDimList());
	}
	
	public void visit(SignNode node) {
		node.setDataType(node.getLeftmostChild().getDataType());
		node.setData(node.getLeftmostChild().getData());
		node.setLineNo(node.getLeftmostChild().getLineNo());
		node.setDimList(node.getLeftmostChild().getDimList());
	}
	
	public void visit(ReturnStatNode node) {
		ASTNode currentNode = node;
		SymbolTable symbTab = null;
		
		while (symbTab == null) {
			currentNode = currentNode.parent();
			symbTab = currentNode.getSymbolTable();
			if (symbTab != null && symbTab.getName() == null) {
				symbTab = null;
			}
			
		}
		String funcName = symbTab.getName();
		symbTab = symbTab.parent();
		String returnType = symbTab.lookup(funcName).getType()[0];
		
		if (!returnType.equals(node.getChild(0).getDataType())) {
			System.out.println("Semantic Error: Return type of class " + funcName + " is "
					+ node.getChild(0).getDataType() + ". Expected " + returnType + ".");
		}
		
		
		String data = currentNode.getData();
		node.setData(data);
	}
	
	public void visit(IndexListNode node) {
		ASTNode childNode = node.getLeftmostChild();
		while (!childNode.getClass().equals(EpsilonNode.class)) {
			if (!childNode.getDataType().equals("integer")) {
				System.out.println("Semantic Error: type of index shoud be integer at line " + childNode.getLineNo() + ". Encountered " + childNode.getDataType());
			}
			childNode = childNode.next();
		}
	}
	
}
