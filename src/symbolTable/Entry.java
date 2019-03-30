package symbolTable;

import java.util.ArrayList;
import java.util.LinkedList;

public class Entry {

	private String name;
	private Kind kind;
	private String[] type;
	private SymbolTable link;
	private boolean isMembFunc = false;
	private String scope;
	private LinkedList<String> inherList;
	private ArrayList<String> dimList;
	
	public Entry(SymbolTable link, String name, Kind kind, String... type) {
		this.name = name;
		this.kind = kind;
		this.type = type;
		
		if (kind.equals(Kind.Class) || kind.equals(Kind.Function)) {
			this.link = link;
		}
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public String getScope() {
		return this.scope;
	}
	
	public void setClassScope(LinkedList<String> inherList) {
		this.inherList = inherList;
	}
	
	public LinkedList<String> getClassScope() {
		return inherList;
	}
	
	public void setLink(SymbolTable link) {
		this.link = link;
	}
	
	public SymbolTable link() {
		return link;
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getType() {
		return this.type;
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public void isMembFunc(boolean status) {
		isMembFunc = status;
	}
	
	public boolean isMembFunc() {
		return isMembFunc;
	}
	
	public void setDimList(ArrayList<String> list) {
		this.dimList = list;
	}
	
	public ArrayList<String> getDimList() {
		return dimList;
	}
	
	public String toString() {
		String result =  name + " " + kind + " ";
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
}
