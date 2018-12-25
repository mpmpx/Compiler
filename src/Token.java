
public class Token {

	TokenType type;
	String value;
	int lineNum;
	
	public Token(TokenType type, String value, int lineNum) {
		this.type = type;
		this.value = value;
		this.lineNum = lineNum;
	}
}
