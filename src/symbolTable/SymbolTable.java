package symbolTable;

import java.nio.file.Paths;
import java.util.LinkedList;

import utilities.FileWriterWrapper;

public class SymbolTable {

	private SymbolTable parent;
	private String name;
	private LinkedList<Entry> entry;
	private FileWriterWrapper outputWriter;
	
	public SymbolTable() {
		entry = new LinkedList<Entry>();
	}
	
	public SymbolTable(String name) {
		this.name = name;
		entry = new LinkedList<Entry>();
	}
	
	private void write(String msg) {
		outputWriter.write(msg);
	}
	
	private void writeln(String msg) {
		outputWriter.write(msg + "\n");
	}
	
	public void close() {
		outputWriter.close();
	}
	
	public void addEntry(Entry entry) {
		this.entry.add(entry);
	}
	
	public LinkedList<Entry> getEntries() {
		return entry;
	}
	
	public void setParent(SymbolTable symTab) {
		parent = symTab;
	}
	
	public SymbolTable parent() {
		return this.parent;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Entry lookup(String name) {
		for (Entry eachEntry : entry) {
			if (eachEntry.getName().equals(name)) {
				return eachEntry;
			}
		}
		return null;
	}
	
	public void print(FileWriterWrapper outputWriter) {
		this.outputWriter = outputWriter;
		LinkedList<String[]> content = new LinkedList<String[]>();
		for (Entry entries : entry) {
			content.add(entries.toString().split(" "));
		}

		String topBorder = "";
		int nameLen = 5;
		int kindLen = 5;
		int typeLen = 5;
		int linkLen = 6;
		int tableLength = 0;
		
		for (String[] eachEntry : content) {
			if (eachEntry[0].length() > nameLen) {
				nameLen = eachEntry[0].length();
			}
			if (eachEntry[1].length() > kindLen) {
				kindLen = eachEntry[1].length();
			}
			
			if (eachEntry.length > 2) {
				int eachTypeLen = eachEntry[2].length();
				//eachTypeLen += eachEntry
				
				
				if (eachEntry[2].length() > typeLen) {
					typeLen = eachEntry[2].length();
				}
			}
		}
		
		nameLen += 1;
		kindLen += 1;
		typeLen += 1;
		
		tableLength = 5 + nameLen + kindLen + typeLen + linkLen;
		for (int i = 0; i < tableLength; i++){
			topBorder += "-";
		}
		
		if (parent != null) {
			writeln(parent.getName());
		}
		else {
			writeln("null");
		}
		writeln(topBorder);
		write("|");
		write(name);
		for (int i = 0; i < tableLength - 2 - (name).length(); i++) {
			write(" ");
		}
		write("|\n");
		writeln(topBorder);
		write("|");
		write("name");
		for (int i = 0; i < nameLen - 4; i++) {
			write(" ");
		}
		write("|");
		write("kind");
		for (int i = 0; i < kindLen - 4; i++) {
			write(" ");
		}
		write("|");
		write("type");
		for (int i = 0; i < typeLen - 4; i++) {
			write(" ");
		}
		write("|");
		write("link  ");
		write("|\n");
		writeln(topBorder);
		for (String[] eachEntry : content) {
			write("|");
			write(eachEntry[0]);
			for (int i = 0; i < nameLen - eachEntry[0].length(); i++) {
				write(" ");
			}
			write("|");
			write(eachEntry[1]);
			for (int i = 0; i <kindLen - eachEntry[1].length(); i++) {
				write(" ");
			}
			write("|");
			if (eachEntry.length > 2) {
				write(eachEntry[2]);
				for (int i = 0; i < typeLen - eachEntry[2].length(); i++) {
					write(" ");
				}
			}
			else {
				for (int i = 0; i < typeLen; i++) {
					write(" ");
				}
			}
			write("|");
			for (Entry entries : entry) {
				if (entries.getName().equals(eachEntry[0])) {
					if (entries.link() != null) {
						write("true  ");
					}
					else {
						write("false ");
					}
					break;
				}
			}
			
			write("|\n");
			writeln(topBorder);
		}
		
		for (Entry entries : entry) {
			SymbolTable link = entries.link();
			if (link != null) {
				link.print(outputWriter);
			}
		}
	}
}
