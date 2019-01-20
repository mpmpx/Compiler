package token;

public class Token {

	public TokenType type;
	public String value;
	public int lineNum;
	
	public Token(TokenType type, String value, int lineNum) {
		this.type = type;
		this.value = value;
		this.lineNum = lineNum;
	}
	
	public String toString() {
		return "Token type: " + type + ", value: " + value + ", line " + lineNum;
	}
}
