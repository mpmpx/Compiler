package parser;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
//import java.util.Stack;

import AST.*;
import lexer.Scanner;
import token.Token;
import token.TokenType;
import utilities.FileWriterWrapper;

public class Parser {

	private Scanner scanner;
	private Token lookahead;
	
	private String rootDir;
	private String outputDir;
	private String selectedFile;
	
	private FileWriterWrapper outputWriter;
	private FileWriterWrapper errorWriter;
	
	private Stack<ASTNode> stack;
	
	
	class Stack<T> {
		
		LinkedList<T> array;
		
		public Stack() {
			array = new LinkedList<T>();
		}
		
		public void push(T t) {
			array.addFirst(t);
			print();
		}
		
		public T pop() {
			T t = array.removeFirst();
			print();
			return t;
		}
		
		public T peek() {
			return array.getFirst();
		}
		
		private void print() {
			for (T t : array) {
				System.out.println(t);
			}
			System.out.println("---------------------");
		}
	}
	
	
	private enum NonTerminal {
		prog, classDecl, classDeclList, varOrfuncDeclTail, varOrfuncDeclList,
		extendClass, idList, funcDecl, funcDeclList, funcHead, classScope,
		funcDef, funcDefList, funcBody, varstat, varstatList, varstatPrime,
		varDecl, varDeclList, statement, statementList, statementPrime,
		assignStat, statBlock, expr, exprTail, relExpr, arithExpr, 
		arithExprTail, sign, term, termTail, factor, factorTemp, 
		factorPrime, factorTempTemp, variable, variableTail, variablePrime,
		indice, indiceList, arraySize, arraySizeList, type, fParams,
		fParamsTail, fParamsTailList, aParams, aParamsTail, aParamsTailList,
		assignOp, relOp, addOp, multOp
	};
	
	public Parser(String fileName) {
		scanner = new Scanner(fileName);
		rootDir = (Paths.get("").toAbsolutePath().toString());
		outputDir = rootDir + "\\src\\parser\\output\\";
		selectedFile = fileName;
		File output = new File(outputDir);
		if (!output.exists()) {
			output.mkdirs();
		}
		
		String resultFileName = outputDir + getFileName(selectedFile) + "_out.txt";
		String errorFileName = outputDir + getFileName(selectedFile) + "_error.txt";
		outputWriter = new FileWriterWrapper(resultFileName);
		errorWriter = new FileWriterWrapper(errorFileName);
		stack = new Stack<ASTNode>();
	}
	
	private void makeFamily(String op, int stackNum) {
		LinkedList<ASTNode> list = new LinkedList<ASTNode>();
		for (int i = 0; i < stackNum; i++) {
			list.addFirst(stack.pop());
		}
		
		ASTNode result = null;
		for (ASTNode node : list) {
			if (result == null) {
				result = node;
			}
			else {
				result.makeSibling(node);
			}
		}
		stack.push(AST.makeFamily(op, result));
	}
	
	private void mergeNode(int num) {
		LinkedList<ASTNode> list = new LinkedList<ASTNode>();
		for (int i = 0; i < num; i++) {
			list.addFirst(stack.pop());
		}
		ASTNode result = null;
		for (ASTNode node : list) {
			if (result == null) {
				result = node;
			}
			else {
				result.makeSibling(node);
			}
		}
		stack.push(result);
	}
	
	private void write(String str) {
		outputWriter.write(str + "\n");
	}
	
	private void error(String str) {
		System.out.println(str);
		errorWriter.write(str + "\n");
	}
	
	
	private void nextToken() {
		lookahead = scanner.nextToken();
	}
	
	private boolean match(TokenType tokenType) {
		if (lookahead.type == tokenType) {
			
			if (tokenType == TokenType.ID) {
				stack.push(AST.makeNode("id", lookahead.value));
			}
			
			if (tokenType == TokenType.INT_NUM || tokenType == TokenType.FLOAT_NUM) {
				stack.push(AST.makeNode("num", lookahead.value));
			}
			
			nextToken();
			return true;
		}
		else {
			error("Syntax error at line " + lookahead.lineNum + ". Expected (terminal: " + tokenType + ").");
			return false;
		}
	}
	
	private boolean skipErrors(NonTerminal nonTerminal) 
	{
		LinkedList<TokenType> firstSet = firstSet(nonTerminal);
		LinkedList<TokenType> followSet = followSet(nonTerminal);
		
		if (firstSet.contains(lookahead.type) | ((firstSet.contains(TokenType.EPSILON) & followSet.contains(lookahead.type)))) {
			return true;
		}
		else {
			error("Syntax error at line " + lookahead.lineNum  + ". Expected (non-terminal: " + nonTerminal + ").");
			while (!firstSet.contains(lookahead.type) & !followSet.contains(lookahead.type) & lookahead.type != TokenType.EOF) {
				nextToken();
				if (firstSet.contains(TokenType.EPSILON) & followSet.contains(lookahead.type)) {
					return false;
				}
			}
			
			return true;
		}
	}
	
	public boolean parse(AST ast) {
		nextToken();
		if (prog() & match(TokenType.EOF)) {
			errorWriter.close();
			outputWriter.close();
			scanner.close();
			ast.setRoot(stack.pop());
			//ast.print();
			return true;
		}
		else {
			errorWriter.close();
			outputWriter.close();
			scanner.close();
			return false;
		}

	}
	
	private boolean prog() { 
	// prog -> classDeclList funcDefList 'main' funcBody ';'
		if (!skipErrors(NonTerminal.prog)) return false;
		if (firstSetContains(NonTerminal.classDeclList) | followSetContains(NonTerminal.classDeclList)) {
			write("prog -> classDeclList funcDefList 'main' funcBody ';'");
			if (classDeclList() & funcDefList() & match(TokenType.MAIN) & funcBody() & match(TokenType.SEMICOLON)) {
				makeFamily("prog", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean classDecl() {
	// classDecl -> 'class' 'id' extendClass '{' varOrfuncDeclList '}' ';'
		if (!skipErrors(NonTerminal.classDecl)) return false;
		if (firstSetContains(TokenType.CLASS)) {
			write("classDecl -> 'class' 'id' extendClass '{' varOrfuncDeclList '}' ';'");
			if (match(TokenType.CLASS) & match(TokenType.ID) & extendClass() &
					match(TokenType.LBRACE) & varOrfuncDeclList() & 
					match(TokenType.RBRACE) & match(TokenType.SEMICOLON)) {
				
				makeFamily("classDecl", 3);
				return true;
			}
			else {
				return false;
			}
		} 
		else {
			return false;
		}
	}
	
	private boolean classDeclList() {
	// classDeclList -> classDecl classDeclList | EPSILON
		if (!skipErrors(NonTerminal.classDeclList)) return false;
		if (firstSetContains(NonTerminal.classDecl)) {
			write("classDeclList -> classDecl classDeclList");
			if (classDecl() & classDeclList()) {
				stack.push(stack.pop().getLeftmostChild());
				makeFamily("classList", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.classDeclList)) {
			write("classDeclList -> EPSILON");
			stack.push(AST.makeNode());
			makeFamily("classList", 1);
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean varOrfuncDeclTail() {
	// varOrfuncDeclTail -> arraySizeList | '(' fParams ')'
		if (!skipErrors(NonTerminal.varOrfuncDeclTail)) return false;
		if (firstSetContains(NonTerminal.arraySizeList) | followSetContains(NonTerminal.varOrfuncDeclTail)) {
			write("varOrfuncDeclTail -> arraySizeList");
			if (arraySizeList()) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)){
			write("varOrfuncDeclTail -> '(' fParams ')'");
			if (match(TokenType.LPAREN) & fParams() & match(TokenType.RPAREN)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean varOrfuncDeclList() {
	// varOrfuncDeclList -> type 'id' varOrfuncDeclTail ';' varOrfuncDeclList 
	//	| EPSILON
		if (!skipErrors(NonTerminal.varOrfuncDeclList)) return false;
		if (firstSetContains(NonTerminal.type)) {
			write("varOrfuncDeclList -> type 'id' varOrfuncDeclTail ';' varOrfuncDeclList");
			if (type() & match(TokenType.ID) & varOrfuncDeclTail() &
					match(TokenType.SEMICOLON) & varOrfuncDeclList()) {
				
				ASTNode varOrfuncDeclList = stack.pop().getLeftmostChild();
				ASTNode varOrfuncDeclTail = stack.peek();
				if ( varOrfuncDeclTail.getClass().equals(FParamsListNode.class)) {
					makeFamily("funcDecl", 3);
				}
				else {
					makeFamily("varDecl", 3);
				}
				makeFamily("membDecl", 1);
				stack.push(varOrfuncDeclList);
				makeFamily("membList", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.varOrfuncDeclList)) {
			stack.push(AST.makeNode());
			makeFamily("membList", 1);
			write("varOrfuncDeclList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private boolean extendClass() {
	// extendClass -> ':' 'id' idList | EPSILON
		if (!skipErrors(NonTerminal.extendClass)) return false;
		if (firstSetContains(TokenType.COLON)) {
			write("extendClass -> ':' 'id' idList");
			if (match(TokenType.COLON) & match(TokenType.ID) & idList()) {
				mergeNode(2);
				makeFamily("inherList", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.extendClass)) {
			stack.push(AST.makeNode());
			makeFamily("inherList", 1);
			write("extendClass -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean idList() {
	// idList -> ',' 'id' idList | EPSILON
		if (!skipErrors(NonTerminal.idList)) return false;
		if (firstSetContains(TokenType.COMMA)) {
			write("idList -> ',' 'id' idList");
			if (match(TokenType.COMMA) & match(TokenType.ID) & idList()) {
				mergeNode(2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.idList)) {
			stack.push(AST.makeNode());
			write("idList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean funcHead() {
	// funcHead -> type 'id' classScope '(' fParams ')'
		if (!skipErrors(NonTerminal.funcHead)) return false;
		if (firstSetContains(NonTerminal.type)) {
			write("funcHead -> type 'id' classScope '(' fParams ')'");
			if (type() & match(TokenType.ID) & classScope() &
					match(TokenType.LPAREN) & fParams() & match(TokenType.RPAREN)) {
				
				ASTNode fParams = stack.pop();
				ASTNode node1 = stack.pop();
				

				if (node1.getClass().equals(EpsilonNode.class)) {
					ASTNode node2 = stack.pop();
					stack.push(node1);
					makeFamily("scopeSpec", 1);
					stack.push(node2);
				}
				else {
					makeFamily("scopeSpec", 1);
					stack.push(node1);
				}
				stack.push(fParams);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean classScope() {
	// classScope -> 'sr' 'id' | EPSILON
		if (!skipErrors(NonTerminal.classScope)) return false;
		if (firstSetContains(TokenType.SR)) {
			write("classScope -> 'sr' 'id'");
			if (match(TokenType.SR) & match(TokenType.ID)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.classScope)) {
			stack.push(AST.makeNode());
			write("classScope -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean funcDef() {
	// funcDef -> funcHead funcBody ';'
		if (!skipErrors(NonTerminal.funcDef)) return false;
		if (firstSetContains(NonTerminal.funcHead)) {
			write("funcDef -> funcHead funcBody ';'");
			if (funcHead() & funcBody() & match(TokenType.SEMICOLON)) {
				makeFamily("funcDef", 5);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean funcDefList() {
	// funcDefList -> funcDef funcDefList | EPSILON	
		if (!skipErrors(NonTerminal.funcDefList)) return false;
		if (firstSetContains(NonTerminal.funcDef)) {
			write("funcDefList -> funcDef funcDefList");
			if (funcDef() & funcDefList()) {
				stack.push(stack.pop().getLeftmostChild());
				makeFamily("funcDefList", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.funcDefList)) {
			write("funcDefList -> EPSILON");
			stack.push(AST.makeNode());
			makeFamily("funcDefList", 1);
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean funcBody() {
	// funcBody -> '{' varstatList'}'
		if (!skipErrors(NonTerminal.funcBody)) return false;
		if (firstSetContains(TokenType.LBRACE)) {
			write("funcBody -> '{' varstatList'}'");
			if (match(TokenType.LBRACE) & varstatList() & match(TokenType.RBRACE)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean varstat() {
	// varstat -> 'id' varstatPrime ';'
	//	 | 'integer' 'id' arraySizeList ';'
	//	 | 'float' 'id' arraySizeList ';'
	//	 | statementPrime	
		if (!skipErrors(NonTerminal.varstat)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("varstat -> 'id' varstatPrime ';'");
			if (match(TokenType.ID) & varstatPrime() & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.INTEGER)) {
			write("varstat -> 'integer' 'id' arraySizeList ';'");
			if (match(TokenType.INTEGER) & match(TokenType.ID) & arraySizeList() & match(TokenType.SEMICOLON)) {
				ASTNode arraySizeList = stack.pop();
				ASTNode id = stack.pop();
				stack.push(AST.makeNode("type"));
				stack.push(id);
				stack.push(arraySizeList);
				makeFamily("varDecl", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FLOAT)) {
			write("varstat -> 'float' 'id' arraySizeList ';'");
			if (match(TokenType.FLOAT) & match(TokenType.ID) & arraySizeList() & match(TokenType.SEMICOLON)) {
				ASTNode arraySizeList = stack.pop();
				ASTNode id = stack.pop();
				stack.push(AST.makeNode("type"));
				stack.push(id);
				stack.push(arraySizeList);
				makeFamily("varDecl", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.statementPrime)) {
			write("varstat -> statementPrime");
			if (statementPrime()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean varstatList() {
	// varstatList -> varstat varstatList | EPSILON	\
		if (!skipErrors(NonTerminal.varstatList)) return false;
		if (firstSetContains(NonTerminal.varstat)) {
			write("varstatList -> varstat varstatList");
			if (varstat() & varstatList()) {
				stack.push(stack.pop().getLeftmostChild());
				makeFamily("statBlock", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.varstatList)) {
			write("varstatList -> EPSILON");
			stack.push(AST.makeNode());
			makeFamily("statBlock", 1);
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean varstatPrime() {
	//varstatPrime -> 'id' arraySizeList
	//	 | variableTail assignOp expr	
		if (!skipErrors(NonTerminal.varstatPrime)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("varstatPrime -> 'id' arraySizeList");
			if (match(TokenType.ID) & arraySizeList()) {
				ASTNode arraySizeList = stack.pop();
				ASTNode id = stack.pop();
				stack.pop();
				stack.push(AST.makeNode("type"));
				stack.push(id);
				stack.push(arraySizeList);
				makeFamily("varDecl", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.variableTail) | followSetContains(NonTerminal.variableTail)) {
			write("varstatPrime -> variableTail assignOp expr");
			if (variableTail() & assignOp() & expr()) {
				makeFamily("assignStat", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean statement() {
	// statement -> assignStat ';'
	//	 | 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'
	//	 | 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'
	//	 | 'read' '(' variable ')' ';'
	//	 | 'write' '(' expr ')' ';'
	//	 | 'return' '(' expr ')' ';'
		if (!skipErrors(NonTerminal.statement)) return false;
		if (firstSetContains(NonTerminal.assignStat)) {
			write("statement -> assignStat ';'");
			if (assignStat() & match(TokenType.SEMICOLON)) {
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.IF)) {
			write("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
			if (match(TokenType.IF) & match(TokenType.LPAREN) & expr() &
					match(TokenType.RPAREN) & match(TokenType.THEN) & statBlock() &
					match(TokenType.ELSE) & statBlock() & match(TokenType.SEMICOLON)) {
				makeFamily("ifStat", 3);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FOR)) {
			write("statement -> 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'");
			if (match(TokenType.FOR) & match(TokenType.LPAREN) & type() & match(TokenType.ID) &
					assignOp() & expr() & match(TokenType.SEMICOLON) & relExpr() & 
					match(TokenType.SEMICOLON) & assignStat() & match(TokenType.RPAREN) &
					statBlock() & match(TokenType.SEMICOLON)) {
				makeFamily("forStat", 6);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.READ)) {
			write("statement -> 'read' '(' variable ')' ';'");
			if (match(TokenType.READ) & match(TokenType.LPAREN) & variable() & 
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				makeFamily("getStat", 1);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.WRITE)) {
			write("statement -> 'write' '(' expr ')' ';'");
			if (match(TokenType.WRITE) & match(TokenType.LPAREN) & expr() &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				makeFamily("putStat", 1);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.RETURN)) {
			write("statement -> 'return' '(' expr ')' ';'");
			if (match(TokenType.RETURN) & match(TokenType.LPAREN) & expr() &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				makeFamily("returnStat", 1);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean statementList() {
	// statementList -> statement statementList | EPSILON
		if (!skipErrors(NonTerminal.statementList)) return false;
		if (firstSetContains(NonTerminal.statement)) {
			write("statementList -> statement statementList");
			if (statement() & statementList()) {
				mergeNode(2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.statementList)) {
			stack.push(AST.makeNode());
			write("statementList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean statementPrime() {
	// statementPrime -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'
	//	 | 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'
	//	 | 'read' '(' variable ')' ';'
	//	 | 'write' '(' expr ')' ';'
	//	 | 'return' '(' expr ')' ';'	
		if (!skipErrors(NonTerminal.statementPrime)) return false;
		if (firstSetContains(TokenType.IF)) {
			write("statementPrime -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
			if (match(TokenType.IF) & match(TokenType.LPAREN) & expr() &
					match(TokenType.RPAREN) & match(TokenType.THEN) & statBlock() &
					match(TokenType.ELSE) & statBlock()) {
				makeFamily("ifStat", 3);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FOR)) {
			write("statementPrime -> 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'");
			if (match(TokenType.FOR) & match(TokenType.LPAREN) & type() & match(TokenType.ID) &
					assignOp() & expr() & match(TokenType.SEMICOLON) & relExpr() & 
					match(TokenType.SEMICOLON) & assignStat() & match(TokenType.RPAREN) &
					statBlock() & match(TokenType.SEMICOLON)) {
				makeFamily("forStat", 6);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.READ)) {
			write("statementPrime -> 'read' '(' variable ')' ';'");
			if (match(TokenType.READ) & match(TokenType.LPAREN) & variable() & 
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				makeFamily("getStat", 1);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.WRITE)) {
			write("statementPrime -> 'write' '(' expr ')' ';'");
			if (match(TokenType.WRITE) & match(TokenType.LPAREN) & expr() &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				makeFamily("putStat", 1);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.RETURN)) {
			write("statementPrime -> 'return' '(' expr ')' ';'");
			if (match(TokenType.RETURN) & match(TokenType.LPAREN) & expr() &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				makeFamily("returnStat", 1);
				makeFamily("stat", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean assignStat() {
	// assignStat -> variable assignOp expr	
		if (!skipErrors(NonTerminal.assignStat)) return false;
		if (firstSetContains(NonTerminal.variable)) {
			write("assignStat -> variable assignOp expr");
			if (variable() & assignOp() & expr()) {
				makeFamily("assignStat", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean statBlock() {
	// statBlock -> '{' statementList '}' | statement | EPSILON	
		if (!skipErrors(NonTerminal.statBlock)) return false;
		if (firstSetContains(TokenType.LBRACE)) {
			write("statBlock -> '{' statementList '}'");
			if (match(TokenType.LBRACE) & statementList() & match(TokenType.RBRACE)) {
				makeFamily("statBlock", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.statement)) {
			write("statBlock -> statement");
			if (statement()) {
				makeFamily("statBlock", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.statBlock)) {
			stack.push(AST.makeNode());
			makeFamily("statBlock", 1);
			write("statBlock -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean expr() {
	// expr -> arithExpr exprTail	
		if (!skipErrors(NonTerminal.expr)) return false;
		if (firstSetContains(NonTerminal.arithExpr)) {
			write("expr -> arithExpr exprTail");
			if (arithExpr() & exprTail()) {
				makeFamily("expr", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean exprTail() {
	// exprTail -> relOp arithExpr | EPSILON	
		if (!skipErrors(NonTerminal.exprTail)) return false;
		if (firstSetContains(NonTerminal.relOp)) {
			write("exprTail -> relOp arithExpr");
			if (relOp() & arithExpr()) {
				makeFamily("relExpr", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.exprTail)) {
			write("exprTail -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean relExpr() {
	// relExpr -> arithExpr relOp arithExpr	
		if (!skipErrors(NonTerminal.relExpr)) return false;
		if (firstSetContains(NonTerminal.arithExpr)) {
			write("relExpr -> arithExpr relOp arithExpr");
			if (arithExpr() & relOp() & arithExpr()) {
				makeFamily("relExpr", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean arithExpr() {
	// arithExpr -> term arithExprTail	
		if (!skipErrors(NonTerminal.arithExpr)) return false;
		if (firstSetContains(NonTerminal.term)) {
			write("arithExpr -> term arithExprTail");
			if (term() & arithExprTail()) {
				if (stack.peek().getClass().equals(EpsilonNode.class)) {
					stack.pop();
					makeFamily("arithExpr", 1);
				}
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean arithExprTail() {
	// arithExprTail -> addOp term arithExprTail | EPSILON
		if (!skipErrors(NonTerminal.arithExprTail)) return false;
		if (firstSetContains(NonTerminal.addOp)) {
			write("arithExprTail -> addOp term arithExprTail");
			if (addOp() & term() & arithExprTail()) {
				ASTNode arithExprTail = stack.peek();
				if (arithExprTail.getClass().equals(EpsilonNode.class)) {
					stack.pop();
					makeFamily("addOp", 2);
					makeFamily("arithExpr", 1);
				}
				else {
					makeFamily("addOp", 2);
					makeFamily("arithExpr", 1);
				}
				
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.arithExprTail)) {
			stack.push(AST.makeNode());
			write("arithExprTail -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean sign() {
	// sign -> '+' | '-'	
		if (!skipErrors(NonTerminal.sign)) return false;
		if (firstSetContains(TokenType.ADD)) {
			write("sign -> '+'");
			if (match(TokenType.ADD)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.SUB)) {
			write("sign -> '-'");
			if (match(TokenType.SUB)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean term() {
	// term -> factor termTail	
		if (!skipErrors(NonTerminal.term)) return false;
		if (firstSetContains(NonTerminal.factor)) {
			write("term -> factor termTail");
			if (factor() & termTail()) {
				if (stack.peek().getClass().equals(EpsilonNode.class)) {
					stack.pop();
					makeFamily("term", 1);
				}
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean termTail() {
	// termTail -> multOp factor termTail | EPSILON
		if (!skipErrors(NonTerminal.termTail)) return false;
		if (firstSetContains(NonTerminal.multOp)) {
			write("termTail -> multOp factor termTail");
			if (multOp() & factor() & termTail()) {
				ASTNode termTail = stack.peek();
				if (termTail.getClass().equals(EpsilonNode.class)) {
					stack.pop();
					makeFamily("term", 1);
					makeFamily("mulOp", 2);
					makeFamily("term", 1);
				}
				else {
					makeFamily("mulOp", 2);
					makeFamily("term", 1);
				}
				
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.termTail)) {
			stack.push(AST.makeNode());
			write("termTail -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}

	private boolean factor() {
	// factor -> factorTemp 
	//	 | 'int_num' | 'float_num'
	//	 | '(' arithExpr ')'
	//	 | 'not' factor
	//	 | sign factor
		if (!skipErrors(NonTerminal.factor)) return false;
		if (firstSetContains(NonTerminal.factorTemp)) {
			write("factor -> factorTemp");
			if (factorTemp()) {
				makeFamily("factor", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.INT_NUM)) {
			write("factor -> 'int_num'");
			if (match(TokenType.INT_NUM)) {
				makeFamily("factor", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FLOAT_NUM)) {
			write("factor -> 'float_num'");
			if (match(TokenType.FLOAT_NUM)) {
				makeFamily("factor", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)) {
			write("factor -> '(' arithExpr ')'");
			if (match(TokenType.LPAREN) & arithExpr() & match(TokenType.RPAREN)) {
				makeFamily("factor", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.NOT)) {
			write("factor -> 'not' factor");
			if (match(TokenType.NOT) & factor()) {
				makeFamily("factor", 1);
				makeFamily("not", 1);
				makeFamily("factor", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.sign)) {
			write("factor -> sign factor");
			if (sign() & factor()) {
				makeFamily("factor", 1);
				makeFamily("sign", 1);
				makeFamily("factor", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean factorTemp() {
	// factorTemp -> 'id' factorPrime	
		if (!skipErrors(NonTerminal.factorTemp)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("factorTemp -> 'id' factorPrime");
			if (match(TokenType.ID) & factorPrime()) {
				
				ASTNode factorPrime = stack.pop();
				if (factorPrime.getClass().equals(FCallNode.class)) {
					stack.push(factorPrime);
				} 
				else if (factorPrime.getClass().equals(VarElementNode.class)){
					stack.push(factorPrime);
					makeFamily("var", 1);
				}
				
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean factorPrime() {
	// factorPrime -> indiceList factorTempTemp 
	//	 | '(' aParams ')' factorTempTemp	
		if (!skipErrors(NonTerminal.factorPrime)) return false;
		if (firstSetContains(NonTerminal.indiceList) | followSetContains(NonTerminal.factorPrime) | followSetContains(NonTerminal.indiceList)) {
			write("factorPrime -> indiceList factorTempTemp");
			if (indiceList() & factorTempTemp()) {
				if (stack.peek().getClass().equals(EpsilonNode.class)) {
					stack.pop();
					makeFamily("dataMember", 2);
					makeFamily("varElement", 1);
				}
				else if (stack.peek().getClass().equals(VarElementNode.class)){
					ASTNode varElement = stack.pop();
					makeFamily("dataMember", 2);
					makeFamily("varElement", 1);
					stack.push(varElement);
				}
				else {
					makeFamily("varElement", 1);
					ASTNode varElement = stack.pop();
					makeFamily("dataMember", 2);
					makeFamily("varElement", 1);
					stack.push(varElement);
					mergeNode(2);
				}
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)) {
			write("factorPrime -> '(' aParams ')' factorTempTemp");
			if (match(TokenType.LPAREN) & aParams() & match(TokenType.RPAREN) &
					factorTempTemp()) {
				
				if (stack.peek().getClass().equals(EpsilonNode.class)) {
					stack.pop();
					makeFamily("fCall", 2);
				}
				else if (stack.peek().getClass().equals(VarElementNode.class)){
					ASTNode varElement = stack.pop();
					makeFamily("fCall", 2);
					makeFamily("varElement", 1);
					stack.push(varElement);
					mergeNode(2);
				}
				else {
					makeFamily("varElement", 1);
					ASTNode varElement = stack.pop();
					makeFamily("fCall", 2);
					makeFamily("varElement", 1);
					stack.push(varElement);
					mergeNode(2);
				}
				return true;
			}
			else {
				return false;
			}
		}
		else {
			System.out.println(lookahead.value);
			return false;
		}
	}
	private boolean factorTempTemp() {
	// factorTempTemp -> '.' factorTemp | EPSILON 	
		if (!skipErrors(NonTerminal.factorTempTemp)) return false;
		if (firstSetContains(TokenType.DOT)) {
			write("factorTempTemp -> '.' factorTemp");
			if (match(TokenType.DOT) & factorTemp()) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.factorTempTemp)) {
			stack.push(AST.makeNode());
			write("factorTempTemp -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean variable() {
	// variable -> 'id' variableTail 	
		if (!skipErrors(NonTerminal.variable)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("variable -> 'id' variableTail");
			if (match(TokenType.ID) & variableTail()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean variableTail() {
	// variableTail -> indiceList variablePrime  
	//	  | '(' aParams ')' '.' variable  	
		if (!skipErrors(NonTerminal.variableTail)) return false;
		if (firstSetContains(NonTerminal.indiceList) | followSetContains(NonTerminal.variableTail) | followSetContains(NonTerminal.indiceList)) {
			write("variableTail -> indiceList variablePrime");
			if (indiceList() & variablePrime()) {
				ASTNode variablePrime = stack.pop();
				makeFamily("dataMember", 2);
				if (!variablePrime.getClass().equals(EpsilonNode.class)) {
					stack.push(variablePrime.getLeftmostChild());
					makeFamily("varElement", 1);
					makeFamily("var", 2);
				}
				else {
					makeFamily("var", 1);
				}
				
				
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)) {
			write("variableTail -> '(' aParams ')' '.' variable");
			if (match(TokenType.LPAREN) & aParams() & match(TokenType.RPAREN) &
					match(TokenType.DOT) & variable()) {
				
				ASTNode variable = stack.pop();
				makeFamily("fCall", 2);
				makeFamily("varElement", 1);
				stack.push(variable.getLeftmostChild());
				mergeNode(2);
				makeFamily("var", 1);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean variablePrime() {
	// variablePrime -> '.' variable 
	//	  | EPSILON 	
		if (!skipErrors(NonTerminal.variablePrime)) return false;
		if (firstSetContains(TokenType.DOT)) {
			write("variablePrime -> '.' variable");
			if (match(TokenType.DOT) & variable()) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.variablePrime)) {
			stack.push(AST.makeNode());
			write("variablePrime -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean indice(){
	// indice -> '[' arithExpr ']'	
		if (!skipErrors(NonTerminal.indice)) return false;
		if (firstSetContains(TokenType.LBRACKET)) {
			write("indice -> '[' arithExpr ']'");
			if (match(TokenType.LBRACKET) & arithExpr() & match(TokenType.RBRACKET)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean indiceList() {
	// indiceList -> indice indiceList | EPSILON	
		if (!skipErrors(NonTerminal.indiceList)) return false;
		if (firstSetContains(NonTerminal.indice)) {
			write("indiceList -> indice indiceList");
			if (indice() & indiceList()) {
				stack.push(stack.pop().getLeftmostChild());
				makeFamily("indexList", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.indiceList)) {
			stack.push(AST.makeNode());
			makeFamily("indexList", 1);
			write("indiceList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean arraySize() {
	// arraySize -> '[' 'int_num' ']'	
		if (!skipErrors(NonTerminal.arraySize)) return false;
		if (firstSetContains(TokenType.LBRACKET)) {
			write("arraySize -> '[' 'int_num' ']'");
			if (match(TokenType.LBRACKET) & match(TokenType.INT_NUM) & match(TokenType.RBRACKET)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean arraySizeList() {
	// arraySizeList -> arraySize arraySizeList | EPSILON
		if (!skipErrors(NonTerminal.arraySizeList)) return false;
		if (firstSetContains(NonTerminal.arraySize)) {
			write("arraySizeList -> arraySize arraySizeList");
			if (arraySize() & arraySizeList()) {
				stack.push(stack.pop().getLeftmostChild());
				makeFamily("dimList", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.arraySizeList)) {
			stack.push(AST.makeNode());
			makeFamily("dimList", 1);
			write("arraySizeList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean type() {
	// type -> 'integer' | 'float' | 'id'	
		if (!skipErrors(NonTerminal.type)) return false;
		if (firstSetContains(TokenType.INTEGER)) {
			write("type -> 'integer'");
			if (match(TokenType.INTEGER)) {
				stack.push(AST.makeNode("type"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FLOAT)) {
			write("type -> 'float'");
			if (match(TokenType.FLOAT)) {
				stack.push(AST.makeNode("type"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.ID)) {
			write("type -> 'id'");
			if (match(TokenType.ID)) {
				stack.pop();
				stack.push(AST.makeNode("type"));
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean fParams() {
	// fParams -> type 'id' arraySizeList fParamsTailList | EPSILON
		if (!skipErrors(NonTerminal.fParams)) return false;
		if (firstSetContains(NonTerminal.type)) {
			write("fParams -> type 'id' arraySizeList fParamsTailList");
			if (type() & match(TokenType.ID) & arraySizeList() & fParamsTailList()) {
				ASTNode fParamsTailList = stack.pop();
				makeFamily("fParams", 3);
				stack.push(fParamsTailList);
				makeFamily("fParamsList", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.fParams)) {
			stack.push(AST.makeNode());
			makeFamily("fParamsList", 1);
			write("fParams ->EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean fParamsTail() {
	// fParamsTail -> ',' type 'id' arraySizeList	
		if (!skipErrors(NonTerminal.fParamsTail)) return false;
		if (firstSetContains(TokenType.COMMA)) {
			write("fParamsTail -> ',' type 'id' arraySizeList");
			if (match(TokenType.COMMA) & type() & match(TokenType.ID) & arraySizeList()) {
				makeFamily("fParams", 3);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean fParamsTailList() {
	// fParamsTailList -> fParamsTail fParamsTailList | EPSILON	
		if (!skipErrors(NonTerminal.fParamsTailList)) return false;
		if (firstSetContains(NonTerminal.fParamsTail)) {
			write("fParamsTailList -> fParamsTail fParamsTailList");
			if (fParamsTail() & fParamsTailList()) {
				mergeNode(2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.fParamsTailList)) {
			stack.push(AST.makeNode());
			write("fParamsTailList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean aParams() {
	// aParams -> expr aParamsTailList | EPSILON	
		if (!skipErrors(NonTerminal.aParams)) return false;
		if (firstSetContains(NonTerminal.expr)) {
			write("aParams -> expr aParamsTailList");
			if (expr() & aParamsTailList()) {
				makeFamily("aParams", 2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.aParams)) {
			stack.push(AST.makeNode());
			makeFamily("aParams", 1);
			write("aParams -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean aParamsTail() {
	// aParamsTail -> ',' expr	
		if (!skipErrors(NonTerminal.aParamsTail)) return false;
		if (firstSetContains(TokenType.COMMA)) {
			write("aParamsTail -> ',' expr");
			if (match(TokenType.COMMA) & expr()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean aParamsTailList() {
	// aParamsTailList -> aParamsTail aParamsTailList | EPSILON
		if (!skipErrors(NonTerminal.aParamsTailList)) return false;
		if (firstSetContains(NonTerminal.aParamsTail)) {
			write("aParamsTailList -> aParamsTail aParamsTailList");
			if (aParamsTail() & aParamsTailList()) {
				mergeNode(2);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.aParamsTailList)) {
			stack.push(AST.makeNode());
			write("aParamsTailList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean assignOp() {
	// assignOp -> '='	
		if (!skipErrors(NonTerminal.assignOp)) return false;
		if (firstSetContains(TokenType.ASSIGN)) {
			write("assignOp -> '='");
			if (match(TokenType.ASSIGN)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean relOp() {
	// relOp -> 'eq' | 'neq' | 'lt' | 'gt' | 'leq' | 'geq'	
		if (!skipErrors(NonTerminal.relOp)) return false;
		if (firstSetContains(TokenType.EQ)) {
			write("relOp -> 'eq'");
			if (match(TokenType.EQ)) {
				stack.push(AST.makeNode("relOp"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.NEQ)) {
			write("relOp -> 'neq'");
			if (match(TokenType.NEQ)) {
				stack.push(AST.makeNode("relOp"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LT)) {
			write("relOp -> 'lt'");
			if (match(TokenType.LT)) {
				stack.push(AST.makeNode("relOp"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.GT)) {
			write("relOp -> 'gt'");
			if (match(TokenType.GT)) {
				stack.push(AST.makeNode("relOp"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LEQ)) {
			write("relOp -> 'leq'");
			if (match(TokenType.LEQ)) {
				stack.push(AST.makeNode("relOp"));
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.GEQ)) {
			write("relOp -> 'geq'");
			if (match(TokenType.GEQ)) {
				stack.push(AST.makeNode("relOp"));
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean addOp() {
	// addOp -> '+' | '-' | 'or'	
		if (!skipErrors(NonTerminal.addOp)) return false;
		if (firstSetContains(TokenType.ADD)) {
			write("addOp -> '+'");
			if (match(TokenType.ADD)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.SUB)) {
			write("addOp -> '-'");
			if (match(TokenType.SUB)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.OR)) {
			write("addOp -> 'or'");
			if (match(TokenType.OR)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	private boolean multOp() {
	// multOp -> '*' | '/' | 'and'	
		if (!skipErrors(NonTerminal.multOp)) return false;
		if (firstSetContains(TokenType.MUL)) {
			write("multOp -> '*'");
			if (match(TokenType.MUL)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.DIV)) {
			write("multOp -> '/'");
			if (match(TokenType.DIV)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.AND)) {
			write("multOp -> 'and'");
			if (match(TokenType.AND)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	private boolean firstSetContains(NonTerminal nonTerminal) {
		return firstSet(nonTerminal).contains(lookahead.type);
	}
	
	private LinkedList<TokenType> firstSet(NonTerminal nonTerminal) {
		LinkedList<TokenType> firstSet = new LinkedList<TokenType>();
		switch(nonTerminal) {
			case prog:
				firstSet.add(TokenType.CLASS);
				firstSet.add(TokenType.FLOAT);
				firstSet.add(TokenType.ID);
				firstSet.add(TokenType.INTEGER);
				firstSet.add(TokenType.MAIN);
				break;
			case classDeclList:
				firstSet.add(TokenType.EPSILON);
			case classDecl: 
				firstSet.add(TokenType.CLASS);
				break;
			case varOrfuncDeclTail:
				firstSet.add(TokenType.EPSILON);
				firstSet.add(TokenType.LBRACKET);
				firstSet.add(TokenType.LPAREN);
				break;
			case varOrfuncDeclList:
			case funcDefList:
			case fParams:
				firstSet.add(TokenType.EPSILON);
			case funcHead:
			case funcDef:
			case varDecl:
			case varDeclList:
			case type:
				firstSet.add(TokenType.FLOAT);
				firstSet.add(TokenType.ID);
				firstSet.add(TokenType.INTEGER);
				break;
			case extendClass:
				firstSet.add(TokenType.EPSILON);
				firstSet.add(TokenType.COLON);
				break;
			case idList:
			case fParamsTailList:
			case aParamsTailList:
				firstSet.add(TokenType.EPSILON);
			case fParamsTail:
			case aParamsTail:
				firstSet.add(TokenType.COMMA);
				break;
			case classScope:
				firstSet.add(TokenType.EPSILON);
				firstSet.add(TokenType.SR);
				break;
			case funcBody:
				firstSet.add(TokenType.LBRACE);
				break;
			case varstatList:
				firstSet.add(TokenType.EPSILON);
			case varstat:
				firstSet.add(TokenType.FOR);
				firstSet.add(TokenType.IF);
				firstSet.add(TokenType.READ);
				firstSet.add(TokenType.RETURN);
				firstSet.add(TokenType.WRITE);
				firstSet.add(TokenType.ID);
				firstSet.add(TokenType.INTEGER);
				firstSet.add(TokenType.FLOAT);
				break;
			case varstatPrime:
				firstSet.add(TokenType.LPAREN);
				firstSet.add(TokenType.LBRACKET);
				firstSet.add(TokenType.DOT);
				firstSet.add(TokenType.ASSIGN);
				firstSet.add(TokenType.ID);
				break;
			case statBlock:
				firstSet.add(TokenType.LBRACE);
			case statementList:
				firstSet.add(TokenType.EPSILON);
			case statement:
				firstSet.add(TokenType.ID);
			case statementPrime:
				firstSet.add(TokenType.READ);
				firstSet.add(TokenType.RETURN);
				firstSet.add(TokenType.WRITE);
				firstSet.add(TokenType.FOR);
				firstSet.add(TokenType.IF);
				break;
			case assignStat:
				firstSet.add(TokenType.ID);
				break;
			case aParams:
				firstSet.add(TokenType.EPSILON);
			case expr:
			case relExpr:
			case arithExpr:
			case term:
			case factor:
				firstSet.add(TokenType.LPAREN);
				firstSet.add(TokenType.FLOAT_NUM);
				firstSet.add(TokenType.INT_NUM);
				firstSet.add(TokenType.NOT);
				firstSet.add(TokenType.ID);
				firstSet.add(TokenType.ADD);
				firstSet.add(TokenType.SUB);
				break;
			case exprTail:
				firstSet.add(TokenType.EPSILON);
			case relOp:
				firstSet.add(TokenType.EQ);
				firstSet.add(TokenType.GEQ);
				firstSet.add(TokenType.GT);
				firstSet.add(TokenType.LEQ);
				firstSet.add(TokenType.LT);
				firstSet.add(TokenType.NEQ);
				break;
			case arithExprTail:
				firstSet.add(TokenType.EPSILON);
			case addOp:
				firstSet.add(TokenType.ADD);
				firstSet.add(TokenType.SUB);
				firstSet.add(TokenType.OR);
				break;
			case sign:
				firstSet.add(TokenType.SUB);
				firstSet.add(TokenType.ADD);
				break;
			case termTail:
				firstSet.add(TokenType.EPSILON);
			case multOp:
				firstSet.add(TokenType.MUL);
				firstSet.add(TokenType.DIV);
				firstSet.add(TokenType.AND);
				break;
			case factorTemp:
			case variable:
				firstSet.add(TokenType.ID);
				break;
			case factorPrime:
			case variableTail:
				firstSet.add(TokenType.EPSILON);
				firstSet.add(TokenType.LBRACKET);
				firstSet.add(TokenType.DOT);
				firstSet.add(TokenType.LPAREN);
				break;
			case factorTempTemp:
			case variablePrime:
				firstSet.add(TokenType.EPSILON);
				firstSet.add(TokenType.DOT);
				break;
			case arraySizeList:
			case indiceList:
				firstSet.add(TokenType.EPSILON);
			case indice:
			case arraySize:
				firstSet.add(TokenType.LBRACKET);
				break;
			case assignOp:
				firstSet.add(TokenType.ASSIGN);
				break;
			default:
				break;
		}
		return firstSet;
	}
	
	private boolean firstSetContains(TokenType tokenType) {
		return lookahead.type == tokenType;
	}
	
	private boolean followSetContains(NonTerminal nonTerminal) {
		return followSet(nonTerminal).contains(lookahead.type);
	}
	
	private LinkedList<TokenType> followSet(NonTerminal nonTerminal) {
		LinkedList<TokenType> followSet = new LinkedList<TokenType>();
		
		switch(nonTerminal) {
			case classDecl:
				followSet.add(TokenType.CLASS);
			case classDeclList:
				followSet.add(TokenType.FLOAT);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.INTEGER);
				followSet.add(TokenType.MAIN);
				break;
			case varOrfuncDeclTail:
			case varstatPrime:
				followSet.add(TokenType.SEMICOLON);
				break;
			case varOrfuncDeclList:
			case varstatList:
			case statementList:
				followSet.add(TokenType.RBRACE);
				break;
			case funcHead:
			case extendClass:
			case idList:
				followSet.add(TokenType.LBRACE);
				break;
			case classScope:
				followSet.add(TokenType.LPAREN);
				break;
			case funcDef:
				followSet.add(TokenType.FLOAT);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.INTEGER);
			case funcDefList:
				followSet.add(TokenType.MAIN);
				break;
			case statement:
				followSet.add(TokenType.FOR);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.IF);
				followSet.add(TokenType.READ);
				followSet.add(TokenType.RETURN);
				followSet.add(TokenType.WRITE);
				followSet.add(TokenType.RBRACE);
			case statBlock:
				followSet.add(TokenType.SEMICOLON);
				followSet.add(TokenType.ELSE);
				break;
			case indice:
				followSet.add(TokenType.LBRACKET);
			case indiceList:
				followSet.add(TokenType.DOT);
				followSet.add(TokenType.ASSIGN);
			case factor:
			case factorPrime:
			case factorTemp:
			case factorTempTemp:
				followSet.add(TokenType.MUL);
				followSet.add(TokenType.DIV);
				followSet.add(TokenType.AND);
			case term:
			case termTail:
				followSet.add(TokenType.ADD);
				followSet.add(TokenType.SUB);
				followSet.add(TokenType.OR);
			case arithExpr:
			case arithExprTail:
				followSet.add(TokenType.EQ);
				followSet.add(TokenType.GEQ);
				followSet.add(TokenType.GT);
				followSet.add(TokenType.LEQ);
				followSet.add(TokenType.LT);
				followSet.add(TokenType.NEQ);
				followSet.add(TokenType.RBRACKET);
			case expr:
			case exprTail:
			case arraySizeList:
				followSet.add(TokenType.SEMICOLON);
				followSet.add(TokenType.RPAREN);
				followSet.add(TokenType.COMMA);
				break;
			case arraySize:
				followSet.add(TokenType.LBRACKET);
				followSet.add(TokenType.SEMICOLON);
				followSet.add(TokenType.RPAREN);
				followSet.add(TokenType.COMMA);
				break;
			case variable:
			case variableTail:
			case variablePrime:
				followSet.add(TokenType.ASSIGN);
				followSet.add(TokenType.RPAREN);
				break;
			case aParamsTail:
			case fParamsTail:
				followSet.add(TokenType.COMMA);
			case fParams:
			case fParamsTailList:
			case aParams:
			case aParamsTailList:
				followSet.add(TokenType.RPAREN);
				break;
			case addOp:
				followSet.add(TokenType.LPAREN);
				followSet.add(TokenType.ADD);
				followSet.add(TokenType.SUB);
				followSet.add(TokenType.FLOAT_NUM);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.INT_NUM);
				followSet.add(TokenType.NOT);
				break;
			case multOp:
			case relOp:
			case sign:
			case assignOp:
				followSet.add(TokenType.LPAREN);
				followSet.add(TokenType.ADD);
				followSet.add(TokenType.SUB);
				followSet.add(TokenType.FLOAT_NUM);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.INT_NUM);
				followSet.add(TokenType.NOT);
				break;
			case assignStat:
				followSet.add(TokenType.RPAREN);
				followSet.add(TokenType.SEMICOLON);
				break;
			case relExpr:
			case funcBody:
				followSet.add(TokenType.SEMICOLON);
				break;
			case prog:
				followSet.add(TokenType.EOF);
				break;
			case statementPrime:
				followSet.add(TokenType.FLOAT);
				followSet.add(TokenType.FOR);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.IF);
				followSet.add(TokenType.INTEGER);
				followSet.add(TokenType.READ);
				followSet.add(TokenType.RETURN);
				followSet.add(TokenType.WRITE);
				break;
			case type:
				followSet.add(TokenType.ID);
				break;
			case varstat:
				followSet.add(TokenType.FLOAT);
				followSet.add(TokenType.FOR);
				followSet.add(TokenType.ID);
				followSet.add(TokenType.IF);
				followSet.add(TokenType.INTEGER);
				followSet.add(TokenType.READ);
				followSet.add(TokenType.RETURN);
				followSet.add(TokenType.WRITE);
				break;
			default:
				break;
		}
		
		return followSet;
	}
	
	private String getFileName(String fileName) {
		fileName = new File(fileName).getName();
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
}
