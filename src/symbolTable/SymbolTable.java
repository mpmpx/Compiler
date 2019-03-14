package symbolTable;

import java.util.LinkedList;

public class SymbolTable {

	private String name;
	private LinkedList<Entry> entry;
	
	public SymbolTable(String name) {
		this.name = name;
		entry = new LinkedList<Entry>();
	}
	
	public void insert(String entryName, Kind kind, String... type) {
		Entry newEntry = new Entry(entryName, kind, type);
		newEntry.setScope(this);
		entry.add(newEntry);
	}
	
	public void print() {
		System.out.println("Symbol table: " + name);
		for (Entry entries : entry) {
			System.out.println(entries);
		}
		System.out.println();
		
		for (Entry entries : entry) {
			SymbolTable link = entries.link();
			if (link != null) {
				link.print();
			}
		}
	}
	
}
