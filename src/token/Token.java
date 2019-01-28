package token;

/**
 * 
 * @author Peixing Ma
 * This a data structure stores type, content and position in the file of a token.
 */
public class Token {

	public TokenType type;
	public String value;
	public int lineNum;
	
	/**
	 * Constructor. Initialize the token with given parameters.
	 * @param type type of the token.
	 * @param value content of the token.
	 * @param lineNum position of the token in the file.
	 */
	public Token(TokenType type, String value, int lineNum) {
		this.type = type;
		this.value = value;
		this.lineNum = lineNum;
	}
	
	/**
	 * Customized toString method.
	 */
	public String toString() {
		return "["+ type + ", " + value + ", " + lineNum + "]";
	}
}
