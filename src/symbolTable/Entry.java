package symbolTable;

class Entry {

	private String name;
	private Kind kind;
	private String[] type;
	private SymbolTable link;
	private SymbolTable scope;
	
	public Entry(String name, Kind kind, String... type) {
		this.name = name;
		this.kind = kind;
		this.type = type;
		
		if (kind.equals(Kind.Class) || kind.equals(Kind.Function)) {
			link = new SymbolTable(name);
		}
	}
	
	public Entry(SymbolTable scope, String name, Kind kind, String... type) {
		this.scope = scope;
		this.name = name;
		this.kind = kind;
		this.type = type;
		
		if (kind.equals(Kind.Class) || kind.equals(Kind.Function)) {
			link = new SymbolTable(name);
		}
	}
	
	public void setScope(SymbolTable scope) {
		this.scope = scope;
	}
	
	public SymbolTable parent() {
		return scope;
	}
	
	public SymbolTable link() {
		return link;
	}
	
	public String toString() {
		String result =  name + "  " + kind + "  ";
		if (type.length > 0) {
			result += type[0];

			if (type.length > 1) {
				result += ":";
				for (int i = 1; i < type.length; i++) {
					result = result + type[i] + ",";
				}
				result = result.substring(0, result.length() - 1);
			}
		}
		return result;
	}
	
	public static void main(String[] arg) {
		Entry entry = new Entry("table_name", Kind.Class, "int","int","int");
		System.out.println(entry);
	}
}
