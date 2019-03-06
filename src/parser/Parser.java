package parser;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import AST.*;
import lexer.Scanner;
import token.Token;
import token.TokenType;
import utilities.FileWriterWrapper;

public class Parser {

	private Scanner scanner;
	private Token lookahead;
	private AST ast;
	
	private String rootDir;
	private String outputDir;
	private String selectedFile;
	
	private FileWriterWrapper outputWriter;
	private FileWriterWrapper errorWriter;
	
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
	}
	
	private void write(String str) {
		//System.out.println(str);
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
			nextToken();
			return true;
		}
		else {
			error("Syntax error at line " + lookahead.lineNum + ". Expected (terminal: " + tokenType + ").");
			nextToken();
			return false;
		}
	}
	
	private boolean match(TokenType tokenType, ASTNode node) {
		if (lookahead.type == tokenType) {
			switch (tokenType) {
				case ID: node = new IdNode("id", lookahead.value); break;
				case FLOAT_NUM:
				case INT_NUM: node = new NumNode("num", lookahead.value); break;
			default:
				break;
			}
			
			nextToken();
			return true;
		}
		else {
			error("Syntax error at line " + lookahead.lineNum + ". Expected (terminal: " + tokenType + ").");
			nextToken();
			return false;
		}
	}
	
	private boolean skipErrors(NonTerminal nonTerminal) 
	{
		LinkedList<TokenType> firstSet = firstSet(nonTerminal);
		LinkedList<TokenType> followSet = followSet(nonTerminal);
		if (firstSet.contains(lookahead.type) | (firstSet.contains(TokenType.EPSILON) & followSet.contains(lookahead.type))) {
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
	
	public boolean parse() {
		ast = new AST();
		ASTNode node = null;
		nextToken();
		if (prog(node) & match(TokenType.EOF)) {
			errorWriter.close();
			outputWriter.close();
			scanner.close();
			return true;
		}
		else {
			errorWriter.close();
			outputWriter.close();
			scanner.close();
			return false;
		}

	}
	
	private boolean prog(ASTNode node) { 
	// prog -> classDeclList funcDefList 'main' funcBody ';'
		
		ClassListNode classListNode = null;
		FuncDefListNode funcDefListNode = null;
		StatBlockNode statBlockNode = null;
		
		if (!skipErrors(NonTerminal.prog)) return false;
		if (firstSetContains(NonTerminal.classDeclList) | followSetContains(NonTerminal.classDeclList) | followSetContains(NonTerminal.funcDeclList)) {
			write("prog -> classDeclList funcDefList 'main' funcBody ';'");
			if (classDeclList(classListNode) & funcDefList(funcDefListNode) & match(TokenType.MAIN) & funcBody(statBlockNode) & match(TokenType.SEMICOLON)) {
				
				node = AST.makeFamily("prog", classListNode, funcDefListNode, statBlockNode);
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
	
	private boolean classDecl(ASTNode node) {
	// classDecl -> 'class' 'id' extendClass '{' varOrfuncDeclList '}' ';'
		IdNode idNode = null;
		InherListNode inherListNode = null;
		MembListNode memberListNode = null;
		
		if (!skipErrors(NonTerminal.classDecl)) return false;
		if (firstSetContains(TokenType.CLASS)) {
			write("classDecl -> 'class' 'id' extendClass '{' varOrfuncDeclList '}' ';'");
			if (match(TokenType.CLASS) & match(TokenType.ID, idNode) & extendClass(inherListNode) &
					match(TokenType.LBRACE) & varOrfuncDeclList(memberListNode) & 
					match(TokenType.RBRACE) & match(TokenType.SEMICOLON)) {
				
				node = AST.makeFamily("classDecl", idNode, inherListNode, memberListNode);
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
	
	private boolean classDeclList(ASTNode node) {
	// classDeclList -> classDecl classDeclList | EPSILON
		
		ClassListNode classListNode = null;
		ClassDeclNode classDeclNode = null;
		
		if (!skipErrors(NonTerminal.classDeclList)) return false;
		if (firstSetContains(NonTerminal.classDecl)) {
			write("classDeclList -> classDecl classDeclList");
			if (classDecl(classDeclNode) & classDeclList(classListNode)) {
				node = AST.makeFamily("classList", classDeclNode, null);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.classDeclList)) {
			write("classDeclList -> EPSILON");
			node = new ClassListNode("classList");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean varOrfuncDeclTail(ASTNode node) {
		DimListNode dimListNode = null;
		FparamListNode fParamListNode = null;
		
		
	// varOrfuncDeclTail -> arraySizeList | '(' fParams ')'
		if (!skipErrors(NonTerminal.varOrfuncDeclTail)) return false;
		if (firstSetContains(NonTerminal.arraySizeList)) {
			write("varOrfuncDeclTail -> arraySizeList");
			if (arraySizeList(dimListNode)) {
				node = dimListNode;
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)){
			write("varOrfuncDeclTail -> '(' fParams ')'");
			if (match(TokenType.LPAREN) & fParams(fParamListNode) & match(TokenType.RPAREN)) {
				node = fParamListNode;
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.varOrfuncDeclTail)) {
			write("varOrfuncDeclTail -> arraySizeList");
			if (arraySizeList(dimListNode)) {
				node = AST.makeNode();
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
	
	private boolean varOrfuncDeclList(ASTNode node) {
	// varOrfuncDeclList -> type 'id' varOrfuncDeclTail ';' varOrfuncDeclList 
	//	| EPSILON
		MembListNode membListNode = null;
		TypeNode typeNode = null;
		IdNode idNode = null;
		ASTNode dimOrfParamListNode = null;
		
		if (!skipErrors(NonTerminal.varOrfuncDeclList)) return false;
		if (firstSetContains(NonTerminal.type)) {
			write("varOrfuncDeclList -> type 'id' varOrfuncDeclTail ';' varOrfuncDeclList");
			if (type(typeNode) & match(TokenType.ID, idNode) & varOrfuncDeclTail(dimOrfParamListNode) &
					match(TokenType.SEMICOLON) & varOrfuncDeclList(membListNode)) {
				
				MembDeclNode membDeclNode = (MembDeclNode) AST.makeFamily("membDecl", typeNode, idNode, null);
				node = AST.makeFamily("membList", membDeclNode, null);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.varOrfuncDeclList)) {
			write("varOrfuncDeclList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private boolean extendClass(ASTNode node) {
	// extendClass -> ':' 'id' idList | EPSILON
		ASTNode idListNode = null;
		IdNode idNode = null;
		
		if (!skipErrors(NonTerminal.extendClass)) return false;
		if (firstSetContains(TokenType.COLON)) {
			write("extendClass -> ':' 'id' idList");
			if (match(TokenType.COLON) & match(TokenType.ID, idNode) & idList(idListNode)) {
				node = AST.makeFamily("inherList", idNode, idListNode);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.extendClass)) {
			write("extendClass -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean idList(ASTNode node) {
	// idList -> ',' 'id' idList | EPSILON
		IdNode idNode = null;
		if (!skipErrors(NonTerminal.idList)) return false;
		if (firstSetContains(TokenType.COMMA)) {
			write("idList -> ',' 'id' idList");
			if (match(TokenType.COMMA) & match(TokenType.ID, idNode) & idList(node)) {
				node = idNode.makeSibling(node);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.idList)) {
			write("idList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean funcHead(ASTNode node) {
	// funcHead -> type 'id' classScope '(' fParams ')'
		TypeNode typeNode = null;
		IdNode idNode = null;
		ScopeSpecNode scopeSpecNode = null;
		FparamListNode fParamListNode = null;
		
		if (!skipErrors(NonTerminal.funcHead)) return false;
		if (firstSetContains(NonTerminal.type)) {
			write("funcHead -> type 'id' classScope '(' fParams ')'");
			if (type(typeNode) & match(TokenType.ID, idNode) & classScope(scopeSpecNode) &
					match(TokenType.LPAREN) & fParams(fParamListNode) & match(TokenType.RPAREN)) {
				
				node = typeNode;
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
	
	private boolean classScope(ASTNode node) {
	// classScope -> 'sr' 'id' | EPSILON
		IdNode idNode = null;
		if (!skipErrors(NonTerminal.classScope)) return false;
		if (firstSetContains(TokenType.SR)) {
			write("classScope -> 'sr' 'id'");
			if (match(TokenType.SR) & match(TokenType.ID)) {
				node = AST.makeFamily("scopeNode", idNode);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.classScope)) {
			write("classScope -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean funcDef(ASTNode node) {
	// funcDef -> funcHead funcBody ';'
		ASTNode funcHeadNode = null;
		StatBlockNode statBlockNode = null;
		
		if (!skipErrors(NonTerminal.funcDef)) return false;
		if (firstSetContains(NonTerminal.funcHead)) {
			write("funcDef -> funcHead funcBody ';'");
			if (funcHead(funcHeadNode) & funcBody(statBlockNode) & match(TokenType.SEMICOLON)) {
				
				node = AST.makeFamily("funcDef", funcHeadNode, statBlockNode);
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
	
	private boolean funcDefList(ASTNode node) {
	// funcDefList -> funcDef funcDefList | EPSILON	
		FuncDefNode funcDefNode = null;
		FuncDefListNode funcDefListNode = null;
		
		if (!skipErrors(NonTerminal.funcDefList)) return false;
		if (firstSetContains(NonTerminal.funcDef)) {
			write("funcDefList -> funcDef funcDefList");
			if (funcDef(funcDefNode) & funcDefList(funcDefListNode)) {
				node = AST.makeFamily("funcDefList", funcDefNode, null);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.funcDefList)) {
			write("funcDefList -> EPSILON");
			node = AST.makeNode();
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean funcBody(ASTNode node) {
	// funcBody -> '{' varstatList'}'
		StatBlockNode statBlockNode = null;
		
		if (!skipErrors(NonTerminal.funcBody)) return false;
		if (firstSetContains(TokenType.LBRACE)) {
			write("funcBody -> '{' varstatList'}'");
			if (match(TokenType.LBRACE) & varstatList(statBlockNode) & match(TokenType.RBRACE)) {
				node = statBlockNode;
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
	
	private boolean varstat(ASTNode node) {
	// varstat -> 'id' varstatPrime ';'
	//	 | 'integer' 'id' arraySizeList ';'
	//	 | 'float' 'id' arraySizeList ';'
	//	 | statementPrime
		
		IdNode idNode = null;
		ASTNode varstatPrimeNode = AST.makeNode();
		
		
		if (!skipErrors(NonTerminal.varstat)) return false;
		if (firstSetContains(TokenType.ID)) {
			
			write("varstat -> 'id' varstatPrime ';'");
			if (match(TokenType.ID, idNode) & varstatPrime(node) & match(TokenType.SEMICOLON)) {
				
				node = varstatPrimeNode;
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.INTEGER)) {
			write("varstat -> 'integer' 'id' arraySizeList ';'");
			if (match(TokenType.INTEGER) & match(TokenType.ID) & arraySizeList(node) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FLOAT)) {
			write("varstat -> 'float' 'id' arraySizeList ';'");
			if (match(TokenType.FLOAT) & match(TokenType.ID) & arraySizeList(node) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.statementPrime)) {
			write("varstat -> statementPrime");
			if (statementPrime(node)) {
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
	
	private boolean varstatList(ASTNode node) {
	// varstatList -> varstat varstatList | EPSILON	\
		StatOrVarDeclNode statOrVarDeclNode = null;
		StatBlockNode statBlockNode = null;
		if (!skipErrors(NonTerminal.varstatList)) return false;
		if (firstSetContains(NonTerminal.varstat)) {
			write("varstatList -> varstat varstatList");
			if (varstat(statOrVarDeclNode) & varstatList(statBlockNode)) {
				
				node = AST.makeFamily("statBlock", statOrVarDeclNode, null);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.varstatList)) {
			write("varstatList -> EPSILON");
			node = AST.makeNode();
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean varstatPrime(ASTNode node) {
	//varstatPrime -> 'id' arraySizeList
	//	 | variableTail assignOp expr	
		DimListNode dimListNode = null;
		IdNode idNode = null;
		
		if (!skipErrors(NonTerminal.varstatPrime)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("varstatPrime -> 'id' arraySizeList");
			if (match(TokenType.ID, idNode) & arraySizeList(dimListNode)) {
				
				//node = AST.makeFamily("varDecl", node.getLeftmostChild(), idNode, dimListNode);
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.variableTail) | followSetContains(NonTerminal.variableTail)) {
			write("varstatPrime -> variableTail assignOp expr");
			
			VarNode varNode =  (VarNode)node;
			ExprNode exprNode = null;
			
			if (variableTail(varNode) & assignOp(node) & expr(exprNode)) {
				
				node = AST.makeFamily("assignStat", varNode, exprNode);
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
	
	private boolean statement(ASTNode node) {
	// statement -> assignStat ';'
	//	 | 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'
	//	 | 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'
	//	 | 'read' '(' variable ')' ';'
	//	 | 'write' '(' expr ')' ';'
	//	 | 'return' '(' expr ')' ';'
		if (!skipErrors(NonTerminal.statement)) return false;
		if (firstSetContains(NonTerminal.assignStat)) {
			write("statement -> assignStat ';'");
			if (assignStat(node) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.IF)) {
			write("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
			if (match(TokenType.IF) & match(TokenType.LPAREN) & expr(node) &
					match(TokenType.RPAREN) & match(TokenType.THEN) & statBlock(node) &
					match(TokenType.ELSE) & statBlock(node) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FOR)) {
			write("statement -> 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'");
			if (match(TokenType.FOR) & match(TokenType.LPAREN) & type(node) & match(TokenType.ID) &
					assignOp(node) & expr(node) & match(TokenType.SEMICOLON) & relExpr(node) & 
					match(TokenType.SEMICOLON) & assignStat(node) & match(TokenType.RPAREN) &
					statBlock(node) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.READ)) {
			write("statement -> 'read' '(' variable ')' ';'");
			if (match(TokenType.READ) & match(TokenType.LPAREN) & variable(node) & 
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.WRITE)) {
			write("statement -> 'write' '(' expr ')' ';'");
			if (match(TokenType.WRITE) & match(TokenType.LPAREN) & expr(node) &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.RETURN)) {
			write("statement -> 'return' '(' expr ')' ';'");
			if (match(TokenType.RETURN) & match(TokenType.LPAREN) & expr(node) &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
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
	
	private boolean statementList(ASTNode node) {
	// statementList -> statement statementList | EPSILON
		if (!skipErrors(NonTerminal.statementList)) return false;
		if (firstSetContains(NonTerminal.statement)) {
			write("statementList -> statement statementList");
			if (statement(node) & statementList(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.statementList)) {
			write("statementList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean statementPrime(ASTNode node) {
	// statementPrime -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'
	//	 | 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'
	//	 | 'read' '(' variable ')' ';'
	//	 | 'write' '(' expr ')' ';'
	//	 | 'return' '(' expr ')' ';'	
		if (!skipErrors(NonTerminal.statementPrime)) return false;
		if (firstSetContains(TokenType.IF)) {
			write("statementPrime -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
			if (match(TokenType.IF) & match(TokenType.LPAREN) & expr(node) &
					match(TokenType.RPAREN) & match(TokenType.THEN) & statBlock(node) &
					match(TokenType.ELSE) & statBlock(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FOR)) {
			write("statementPrime -> 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'");
			if (match(TokenType.FOR) & match(TokenType.LPAREN) & type(node) & match(TokenType.ID) &
					assignOp(node) & expr(node) & match(TokenType.SEMICOLON) & relExpr(node) & 
					match(TokenType.SEMICOLON) & assignStat(node) & match(TokenType.RPAREN) &
					statBlock(node) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.READ)) {
			write("statementPrime -> 'read' '(' variable ')' ';'");
			if (match(TokenType.READ) & match(TokenType.LPAREN) & variable(node) & 
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.WRITE)) {
			write("statementPrime -> 'write' '(' expr ')' ';'");
			if (match(TokenType.WRITE) & match(TokenType.LPAREN) & expr(node) &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.RETURN)) {
			write("statementPrime -> 'return' '(' expr ')' ';'");
			if (match(TokenType.RETURN) & match(TokenType.LPAREN) & expr(node) &
					match(TokenType.RPAREN) & match(TokenType.SEMICOLON)) {
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
	
	private boolean assignStat(ASTNode node) {
	// assignStat -> variable assignOp expr	
		if (!skipErrors(NonTerminal.assignStat)) return false;
		if (firstSetContains(NonTerminal.variable)) {
			write("assignStat -> variable assignOp expr");
			if (variable(node) & assignOp(node) & expr(node)) {
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
	
	private boolean statBlock(ASTNode node) {
	// statBlock -> '{' statementList '}' | statement | EPSILON	
		if (!skipErrors(NonTerminal.statBlock)) return false;
		if (firstSetContains(TokenType.LBRACE)) {
			write("statBlock -> '{' statementList '}'");
			if (match(TokenType.LBRACE) & statementList(node) & match(TokenType.RBRACE)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.statement)) {
			write("statBlock -> statement");
			if (statement(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.statBlock)) {
			write("statBlock -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean expr(ASTNode node) {
	// expr -> arithExpr exprTail	
		if (!skipErrors(NonTerminal.expr)) return false;
		if (firstSetContains(NonTerminal.arithExpr)) {
			write("expr -> arithExpr exprTail");
			if (arithExpr(node) & exprTail(node)) {
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
	
	private boolean exprTail(ASTNode node) {
	// exprTail -> relOp arithExpr | EPSILON	
		if (!skipErrors(NonTerminal.exprTail)) return false;
		if (firstSetContains(NonTerminal.relOp)) {
			write("exprTail -> relOp arithExpr");
			if (relOp(node) & arithExpr(node)) {
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
	
	private boolean relExpr(ASTNode node) {
	// relExpr -> arithExpr relOp arithExpr	
		if (!skipErrors(NonTerminal.relExpr)) return false;
		if (firstSetContains(NonTerminal.arithExpr)) {
			write("elExpr -> arithExpr relOp arithExpr");
			if (arithExpr(node) & relOp(node) & arithExpr(node)) {
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
	
	private boolean arithExpr(ASTNode node) {
	// arithExpr -> term arithExprTail	
		if (!skipErrors(NonTerminal.arithExpr)) return false;
		if (firstSetContains(NonTerminal.term)) {
			write("arithExpr -> term arithExprTail");
			if (term(node) & arithExprTail(node)) {
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
	
	private boolean arithExprTail(ASTNode node) {
	// arithExprTail -> addOp term arithExprTail | EPSILON
		if (!skipErrors(NonTerminal.arithExprTail)) return false;
		if (firstSetContains(NonTerminal.addOp)) {
			write("arithExprTail -> addOp term arithExprTail");
			if (addOp(node) & term(node) & arithExprTail(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.arithExprTail)) {
			write("arithExprTail -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean sign(ASTNode node) {
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
	
	private boolean term(ASTNode node) {
	// term -> factor termTail	
		if (!skipErrors(NonTerminal.term)) return false;
		if (firstSetContains(NonTerminal.factor)) {
			write("term -> factor termTail");
			if (factor(node) & termTail(node)) {
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
	
	private boolean termTail(ASTNode node) {
	// termTail -> multOp factor termTail | EPSILON
		if (!skipErrors(NonTerminal.termTail)) return false;
		if (firstSetContains(NonTerminal.multOp)) {
			write("termTail -> multOp factor termTail");
			if (multOp(node) & factor(node) & termTail(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.termTail)) {
			write("termTail -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}

	private boolean factor(ASTNode node) {
	// factor -> factorTemp 
	//	 | 'int_num' | 'float_num'
	//	 | '(' arithExpr ')'
	//	 | 'not' factor
	//	 | sign factor
		if (!skipErrors(NonTerminal.factor)) return false;
		if (firstSetContains(NonTerminal.factorTemp)) {
			write("factor -> factorTemp");
			if (factorTemp(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.INT_NUM)) {
			write("factor -> 'int_num'");
			if (match(TokenType.INT_NUM)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FLOAT_NUM)) {
			write("factor -> 'float_num'");
			if (match(TokenType.FLOAT_NUM)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)) {
			write("factor -> '(' arithExpr ')'");
			if (match(TokenType.LPAREN) & arithExpr(node) & match(TokenType.RPAREN)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.NOT)) {
			write("factor -> 'not' factor");
			if (match(TokenType.NOT) & factor(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(NonTerminal.sign)) {
			write("factor -> sign factor");
			if (sign(node) & factor(node)) {
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
	
	private boolean factorTemp(ASTNode node) {
	// factorTemp -> 'id' factorPrime	
		if (!skipErrors(NonTerminal.factorTemp)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("factorTemp -> 'id' factorPrime");
			if (match(TokenType.ID) & factorPrime(node)) {
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
	
	private boolean factorPrime(ASTNode node) {
	// factorPrime -> indiceList factorTempTemp 
	//	 | '(' aParams ')' factorTempTemp	
		if (!skipErrors(NonTerminal.factorPrime)) return false;
		if (firstSetContains(NonTerminal.indiceList)) {
			write("actorPrime -> indiceList factorTempTemp");
			if (indiceList(node) & factorTempTemp(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)) {
			write("factorPrime -> '(' aParams ')' factorTempTemp");
			if (match(TokenType.LPAREN) & aParams(node) & match(TokenType.RPAREN) &
					factorTempTemp(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.factorPrime) | followSetContains(NonTerminal.indiceList)) {
			write("factorPrime -> indiceList factorTempTemp");
			if (indiceList(node) & factorTempTemp(node)) {
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
	private boolean factorTempTemp(ASTNode node) {
	// factorTempTemp -> '.' factorTemp | EPSILON 	
		if (!skipErrors(NonTerminal.factorTempTemp)) return false;
		if (firstSetContains(TokenType.DOT)) {
			write("factorTempTemp -> '.' factorTemp");
			if (match(TokenType.DOT) & factorTemp(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.factorTempTemp)) {
			write("factorTempTemp -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean variable(ASTNode node) {
	// variable -> 'id' variableTail 	
		if (!skipErrors(NonTerminal.variable)) return false;
		if (firstSetContains(TokenType.ID)) {
			write("variable -> 'id' variableTail");
			if (match(TokenType.ID) & variableTail(node)) {
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
	private boolean variableTail(ASTNode node) {
	// variableTail -> indiceList variablePrime  
	//	  | '(' aParams ')' '.' variable  
		
		if (!skipErrors(NonTerminal.variableTail)) return false;
		if (firstSetContains(NonTerminal.indiceList)) {
			write("variableTail -> indiceList variablePrime");
			if (indiceList(node) & variablePrime(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LPAREN)) {
			write("variableTail -> '(' aParams ')' '.' variable");
			if (match(TokenType.LPAREN) & aParams(node) & match(TokenType.RPAREN) &
					match(TokenType.DOT) & variable(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.variableTail) | followSetContains(NonTerminal.indiceList)) {
			write("variableTail -> indiceList variablePrime");
			if (indiceList(node) & variablePrime(node)) {
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
	private boolean variablePrime(ASTNode node) {
	// variablePrime -> '.' variable 
	//	  | EPSILON 	
		if (!skipErrors(NonTerminal.variablePrime)) return false;
		if (firstSetContains(TokenType.DOT)) {
			write("variablePrime -> '.' variable");
			if (match(TokenType.DOT) & variable(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.variablePrime)) {
			write("variablePrime -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean indice(ASTNode node){
	// indice -> '[' arithExpr ']'	
		if (!skipErrors(NonTerminal.indice)) return false;
		if (firstSetContains(TokenType.LBRACKET)) {
			write("indice -> '[' arithExpr ']'");
			if (match(TokenType.LBRACKET) & arithExpr(node) & match(TokenType.RBRACKET)) {
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
	private boolean indiceList(ASTNode node) {
	// indiceList -> indice indiceList | EPSILON	
		if (!skipErrors(NonTerminal.indiceList)) return false;
		if (firstSetContains(NonTerminal.indice)) {
			write("indiceList -> indice indiceList");
			if (indice(node) & indiceList(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.indiceList)) {
			write("indiceList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean arraySize(ASTNode node) {
	// arraySize -> '[' 'int_num' ']'
		NumNode numNode = null;
		if (!skipErrors(NonTerminal.arraySize)) return false;
		if (firstSetContains(TokenType.LBRACKET)) {
			write("arraySize -> '[' 'int_num' ']'");
			if (match(TokenType.LBRACKET) & match(TokenType.INT_NUM, numNode) & match(TokenType.RBRACKET)) {
				node = numNode;
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
	private boolean arraySizeList(ASTNode node) {
	// arraySizeList -> arraySize arraySizeList | EPSILON
		NumNode numNode = null;
		DimListNode dimListNode = null;
		if (!skipErrors(NonTerminal.arraySizeList)) return false;
		if (firstSetContains(NonTerminal.arraySize)) {
			write("arraySizeList -> arraySize arraySizeList");
			if (arraySize(numNode) & arraySizeList(dimListNode)) {
				node = AST.makeFamily("dimList", numNode,null);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.arraySizeList)) {
			write("arraySizeList -> EPSILON");
			node = AST.makeNode();
			return true;
		}
		else {
			return false;
		}
	}
	private boolean type(ASTNode node) {
	// type -> 'integer' | 'float' | 'id'	
		if (!skipErrors(NonTerminal.type)) return false;
		if (firstSetContains(TokenType.INTEGER)) {
			write("type -> 'integer'");
			if (match(TokenType.INTEGER)) {
				node = AST.makeNode("type");
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.FLOAT)) {
			write("type -> 'float'");
			if (match(TokenType.FLOAT)) {
				node = AST.makeNode("type");
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.ID)) {
			write("type -> 'id'");
			if (match(TokenType.ID)) {
				node = AST.makeNode("type");
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
	private boolean fParams(ASTNode node) {
	// fParams -> type 'id' arraySizeList fParamsTailList | EPSILON
		TypeNode typeNode = null;
		IdNode idNode = null;
		DimListNode dimListNode = null;
		FparamListNode fParamListNode = null;
		
		if (!skipErrors(NonTerminal.fParams)) return false;
		if (firstSetContains(NonTerminal.type)) {
			write("fParams -> type 'id' arraySizeList fParamsTailList");
			if (type(typeNode) & match(TokenType.ID, idNode) & arraySizeList(dimListNode) & fParamsTailList(fParamListNode)) {
				
				FparamNode fParamNode = (FparamNode) AST.makeFamily("fParams", typeNode, idNode, dimListNode);
				node = AST.makeFamily("fParamsList", fParamNode, null);
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.fParams)) {
			write("fParams ->EPSILON");
			node = AST.makeNode();
			return true;
		}
		else {
			return false;
		}
	}
	private boolean fParamsTail(ASTNode node) {
	// fParamsTail -> ',' type 'id' arraySizeList	
		TypeNode typeNode = null;
		IdNode idNode = null;
		DimListNode dimListNode = null;
		
		if (!skipErrors(NonTerminal.fParamsTail)) return false;
		if (firstSetContains(TokenType.COMMA)) {
			write("fParamsTail -> ',' type 'id' arraySizeList");
			if (match(TokenType.COMMA) & type(typeNode) & match(TokenType.ID, idNode) & arraySizeList(dimListNode)) {
				node = AST.makeFamily("fParams", typeNode, idNode, dimListNode);
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
	private boolean fParamsTailList(ASTNode node) {
	// fParamsTailList -> fParamsTail fParamsTailList | EPSILON	
		FparamNode fParamNode = null;
		FparamListNode fParamListNode = null;
		
		if (!skipErrors(NonTerminal.fParamsTailList)) return false;
		if (firstSetContains(NonTerminal.fParamsTail)) {
			write("fParamsTailList -> fParamsTail fParamsTailList");
			if (fParamsTail(fParamNode) & fParamsTailList(fParamListNode)) {
				node = AST.makeFamily("fParamsList", fParamNode, fParamListNode.getLeftmostChild());
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.fParamsTailList)) {
			write("fParamsTailList -> EPSILON");
			node = AST.makeNode();
			return true;
		}
		else {
			return false;
		}
	}
	private boolean aParams(ASTNode node) {
	// aParams -> expr aParamsTailList | EPSILON	
		if (!skipErrors(NonTerminal.aParams)) return false;
		if (firstSetContains(NonTerminal.expr)) {
			write("aParams -> expr aParamsTailList");
			if (expr(node) & aParamsTailList(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.aParams)) {
			write("aParams -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean aParamsTail(ASTNode node) {
	// aParamsTail -> ',' expr	
		if (!skipErrors(NonTerminal.aParamsTail)) return false;
		if (firstSetContains(TokenType.COMMA)) {
			write("aParamsTail -> ',' expr");
			if (match(TokenType.COMMA) & expr(node)) {
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
	private boolean aParamsTailList(ASTNode node) {
	// aParamsTailList -> aParamsTail aParamsTailList | EPSILON
		if (!skipErrors(NonTerminal.aParamsTailList)) return false;
		if (firstSetContains(NonTerminal.aParamsTail)) {
			write("aParamsTailList -> aParamsTail aParamsTailList");
			if (aParamsTail(node) & aParamsTailList(node)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (followSetContains(NonTerminal.aParamsTailList)) {
			write("aParamsTailList -> EPSILON");
			return true;
		}
		else {
			return false;
		}
	}
	private boolean assignOp(ASTNode node) {
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
	private boolean relOp(ASTNode node) {
	// relOp -> 'eq' | 'neq' | 'lt' | 'gt' | 'leq' | 'geq'	
		if (!skipErrors(NonTerminal.relOp)) return false;
		if (firstSetContains(TokenType.EQ)) {
			write("relOp -> 'eq'");
			if (match(TokenType.EQ)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.NEQ)) {
			write("relOp -> 'neq'");
			if (match(TokenType.NEQ)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LT)) {
			write("relOp -> 'lt'");
			if (match(TokenType.LT)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.GT)) {
			write("relOp -> 'gt'");
			if (match(TokenType.GT)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.LEQ)) {
			write("relOp -> 'leq'");
			if (match(TokenType.LEQ)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (firstSetContains(TokenType.GEQ)) {
			write("relOp -> 'geq'");
			if (match(TokenType.GEQ)) {
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
	private boolean addOp(ASTNode node) {
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
	private boolean multOp(ASTNode node) {
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
				firstSet.add(TokenType.EPSILON);
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
