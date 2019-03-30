package visitor;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

import AST.*;
import symbolTable.Entry;
import symbolTable.Kind;
import symbolTable.SymbolTable;
import utilities.FileWriterWrapper;

public class SymTabCreationVisitor extends Visitor {
	public void visit(ProgNode node) {
		String rootDir = (Paths.get("").toAbsolutePath().toString());
		String outputDir = rootDir + "\\src\\parser\\output\\";
		String symbolTableOutput = outputDir + "symbolTableOutput.txt";
		FileWriterWrapper outputWriter = new FileWriterWrapper(symbolTableOutput);
		
		node.setSymbolTable(new SymbolTable("global"));
		
		ASTNode classPtr = node.getChild(0).getLeftmostChild();
		
		while (!classPtr.getClass().equals(EpsilonNode.class)) {
			
			Entry classEntry = classPtr.getEntry();
			Entry dupEntry = node.getSymbolTable().lookup(classEntry.getName());
			
			if (dupEntry != null) {
				System.out.println("Semantic Error: multiply declared class \"" + dupEntry.getName()
					+ "\" at line " + classPtr.getChild(0).getLineNo() + ".");
			}
			
			node.getSymbolTable().addEntry(classPtr.getEntry());
			classPtr = classPtr.next();
		}
		
		for (Entry eachEntry : node.getSymbolTable().getEntries()) {
			LinkedList<String> inherList = eachEntry.getClassScope();
			if (inherList.size() > 0) {
				for (String scope : inherList) {
					Entry entry = node.getSymbolTable().lookup(scope);
					//entry.link().addEntry(classPtr.getEntry());
					if (entry == null) {
						System.out.println("Sematic Error: class \"" 
								+ scope + "\" is not defined." );
					}
					else {
						eachEntry.link().setParent(entry.link());
						checkShadowing(eachEntry.link());
					}
				}
			}
			else {
				eachEntry.link().setParent(node.getSymbolTable());
			}
		}
		
		
		ASTNode funcPtr = node.getChild(1).getLeftmostChild();
		while (!funcPtr.getClass().equals(EpsilonNode.class)) {
			if (funcPtr.getEntry().getScope() == null) {
				funcPtr.getEntry().link().setParent(node.getSymbolTable());
				
				Entry funcEntry = funcPtr.getEntry();
				Entry dupEntry = node.getSymbolTable().lookup(funcEntry.getName());
				if (dupEntry != null) {
					System.out.println("Semantic Error: multiply declared function \"" + dupEntry.getName()
						+ "\" at line " + funcPtr.getChild(2).getLineNo() + ".");
				}
				
			}
			else {
				String scope = funcPtr.getEntry().getScope();
				SymbolTable classSymbTab = node.getSymbolTable().lookup(scope).link();
				Entry funcEntry = classSymbTab.lookup(funcPtr.getEntry().getName());
				if (funcEntry == null) {
					System.out.println("Semantic Error: member function \"" + funcPtr.getEntry().getName()
					+ "\" is not defined in the class \"" + classSymbTab.getName() + "\" at line " + funcPtr.getChild(2).getLineNo() + ".");
				}
				else if (funcEntry.link() != null) {
					System.out.println("Semantic Error: multiply declared function \"" + funcEntry.getName()
					+ "\" at line " + funcPtr.getChild(2).getLineNo() + ".");
				}
				else {
					//funcEntry.setLink(funcPtr.getEntry().link());
					funcPtr.getEntry().link().setParent(classSymbTab);
				}
			}
			node.getSymbolTable().addEntry(funcPtr.getEntry());
			funcPtr = funcPtr.next();
		}
		
		ASTNode statNode = node.getChild(2);
		if (!statNode.getClass().equals(EpsilonNode.class)) {
			
			SymbolTable statBlockSymbTab = statNode.getSymbolTable();
			statBlockSymbTab.setParent(node.getSymbolTable());
			statBlockSymbTab.setName("program");
			node.getSymbolTable().addEntry(new Entry(statBlockSymbTab, "program", Kind.Function));
		}
		else {
			node.getSymbolTable().addEntry(new Entry(null, "program", Kind.Function));
		}
		
		node.getSymbolTable().print(outputWriter);
		outputWriter.close();
	}
	
	public void visit(ClassDeclNode node) {
		String className = node.getLeftmostChild().getData();
		SymbolTable symbTab = new SymbolTable(className);
		node.setSymbolTable(symbTab);
		
		ASTNode inherNode = node.getChild(1).getLeftmostChild();
		LinkedList<String> inherList = new LinkedList<String>();
		while (!inherNode.getClass().equals(EpsilonNode.class)) {
			inherList.add(inherNode.getData());
			inherNode = inherNode.next();
		}
		
		ASTNode childNode = node.getChild(2).getLeftmostChild();
		while (!childNode.getClass().equals(EpsilonNode.class)) {
			ASTNode membNode = childNode.getLeftmostChild();
			
			if (membNode.getEntry() != null) {
				Entry childEntry = membNode.getEntry();
				Entry dupEntry = node.getSymbolTable().lookup(childEntry.getName());
				
				if (dupEntry != null) {
					System.out.println("Semantic Error: multiply declared identifier \"" + dupEntry.getName()
						+ "\" in class \"" + node.getSymbolTable().getName() + "\" at line " + membNode.getChild(1).getLineNo() + ".");
				}
				symbTab.addEntry(membNode.getEntry());
			}
			childNode = childNode.next();
		}

		node.setSymbolTable(symbTab);
		node.setEntry(new Entry(node.getSymbolTable(), className, Kind.Class));
		node.getEntry().setClassScope(inherList);
	}
	
	public void visit(VarDeclNode node) {
		String varName = node.getChild(1).getData();
		String type = node.getChild(0).getData();
		//ArrayList<String> dimList = new ArrayList<String>();
		
		ASTNode numNode = node.getChild(2).getLeftmostChild();
		while (!numNode.getClass().equals(EpsilonNode.class)) {
			//dimList.add(node.getData());
			type += ("[" + numNode.getData() + "]");
			numNode = numNode.next();
		}
		Entry entry = new Entry(null, varName, Kind.Variable, type);
		//entry.setDimList(dimList);
		node.setEntry(entry);
	}
	
	public void visit(FuncDeclNode node) {
		String funcName = node.getChild(1).getData();
		String type = node.getChild(0).getData();
		ArrayList<String> typeArray = new ArrayList<String>();
		typeArray.add(type);

		ASTNode fParams = node.getChild(2).getLeftmostChild();
		while (!fParams.getClass().equals(EpsilonNode.class)) {
			String fParamsType = fParams.getChild(0).getData();
			ASTNode numNode = fParams.getChild(2).getLeftmostChild();
			while (!numNode.getClass().equals(EpsilonNode.class)) {
				fParamsType += ("[" +  numNode.getData() + "]");
				numNode = numNode.next();
			}
			typeArray.add(fParamsType);
			fParams = fParams.next();
		}
		
		if (typeArray.size() == 1) {
			typeArray.add("nil");
		}

		String[] typeList = typeArray.toArray(new String[typeArray.size()]);
		node.setEntry(new Entry(null, funcName, Kind.Function, typeList));
	}
	
	public void visit(FuncDefNode node) {
		String funcName = node.getChild(2).getData();
		String funcType = node.getChild(0).getData();
		ArrayList<String> typeArray = new ArrayList<String>();
		typeArray.add(funcType);
		String scopeName = "";
		String scopePrefix = "";

		if (!node.getChild(1).getLeftmostChild().getClass().equals(EpsilonNode.class)) {
			scopeName = node.getChild(1).getLeftmostChild().getData();
			scopePrefix = scopeName + ":";
		}
		
		SymbolTable symbTab = new SymbolTable(scopePrefix + funcName);
		ASTNode fParams = node.getChild(3).getLeftmostChild();
		while (!fParams.getClass().equals(EpsilonNode.class)) {
			String fParamsType = fParams.getChild(0).getData();
			ASTNode numNode = fParams.getChild(2).getLeftmostChild();
			while (!numNode.getClass().equals(EpsilonNode.class)) {
				fParamsType += ("[" +  numNode.getData() + "]");
				numNode = numNode.next();
			}
			typeArray.add(fParamsType);
			fParams = fParams.next();
		}
		
		if (typeArray.size() == 1) {
			typeArray.add("nil");
		}

		ASTNode membNode = node.getChild(3).getLeftmostChild();
		while (!membNode.getClass().equals(EpsilonNode.class)) {
			symbTab.addEntry(membNode.getEntry());
			membNode = membNode.next();
		}
		
		if (!node.getChild(4).getClass().equals(EpsilonNode.class)) {
			LinkedList<Entry> entries = node.getChild(4).getSymbolTable().getEntries();
			
			for (Entry eachEntry : entries) {
				symbTab.addEntry(eachEntry);
			}
		}
		
		node.setSymbolTable(symbTab);
		String[] typeList = typeArray.toArray(new String[typeArray.size()]);
		node.setEntry(new Entry(symbTab, funcName, Kind.Function, typeList));
		
		if (scopeName.length() > 0) {
			node.getEntry().setScope(scopeName);
		}
	}
	
	public void visit(StatBlockNode node) {
		SymbolTable symbTab = new SymbolTable();
		node.setSymbolTable(symbTab);
		
		ASTNode statOrVarDeclNode = node.getLeftmostChild();
		
		while (!statOrVarDeclNode.getClass().equals(EpsilonNode.class)) {
			if (statOrVarDeclNode.getEntry() != null) {
				Entry childEntry = statOrVarDeclNode.getEntry();
				Entry dupEntry = node.getSymbolTable().lookup(childEntry.getName());
		
				if (dupEntry != null) {
					System.out.println("Semantic Error: multiply declared identifier \"" + dupEntry.getName()
						+ "\" at line " + statOrVarDeclNode.getChild(1).getLineNo() + ".");
				}
				
				node.getSymbolTable().addEntry(statOrVarDeclNode.getEntry());
			}
			statOrVarDeclNode = statOrVarDeclNode.next();
		}
	}
	
	public void visit(FParamsNode node) {
		String fParamsType = node.getChild(0).getData();
	
		ASTNode numNode = node.getChild(2).getLeftmostChild();
		while (!numNode.getClass().equals(EpsilonNode.class)) {
			fParamsType += ("[" +  numNode.getData() + "]");
			numNode = numNode.next();
		}
		
		node.setEntry(new Entry(null, node.getChild(1).getData(), Kind.Parameter, fParamsType));
	}
	
	private void checkShadowing(SymbolTable symbTab) {
		for (Entry eachEntry : symbTab.getEntries()) {
			SymbolTable currentTable = symbTab.parent();
			Kind kind = eachEntry.getKind();
			while (currentTable != null) {
				if (kind.equals(Kind.Variable) || kind.equals(Kind.Function)) {
					Entry parentEntry = currentTable.lookup(eachEntry.getName());
					if (parentEntry != null) {
						if (parentEntry.getKind().equals(eachEntry.getKind())) {
							System.out.println("Shadowing Warning: " + eachEntry.getName() + " in class " + symbTab.getName() + " is shadowed.");
							break;
						}
					}
				}
				currentTable = currentTable.parent();
			}
		}
	}
}
