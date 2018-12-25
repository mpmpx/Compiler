import java.util.HashMap;

public enum TokenType {
	ID,				// identifier 
	FLOAT_NUM, 		// float number
	INT_NUM,		// integer number
	
	// keywords
	IF, 			// if
	THEN, 			// then
	ELSE, 			// else
	FOR, 			// for
	CLASS, 			// class
	INT, 			// int
	FLOAT, 			// float
	GET, 			// get
	PUT, 			// put
	RETURN, 		// return
	PROGRAM,		// program

	// Operators
	EQ, 			// ==
	NEQ,			// <>
	LT, 			// <
	LEQ, 			// <=
	GT, 			// >
	GEQ,			// >=
	ADD, 			// +
	SUB,			// -
	MUL,			// *
	DIV,			// /
	NOT,			// not
	AND,			// and
	OR,				// or
	ASSIGN,			// =
	SR,				// ::
	
	// Punctuation
	SEMICOLON, 		// ;
	COMMA,			// ,
	DOT,			// .
	COLON,			// :
	LBRACKET,		// [
	RBRACKET,		// ]
	LPAREN,			// (
	RPAREN,			// )
	LBRACE,			// {
	RBRACE,			// }
	
	ERROR_NUM,      // invalid number
	ERROR_ID,		// invalid identifier
	EOL,			// \n
	EOF; 			// end of the file
	
	public static HashMap<String, TokenType> reservedKeysMap = new HashMap<String, TokenType>() {{
		put("and", TokenType.AND);
		put("not", TokenType.NOT);
		put("or", TokenType.OR);
		put("if", TokenType.IF);
		put("then", TokenType.THEN);
		put("else", TokenType.ELSE);
		put("for", TokenType.FOR);
		put("class", TokenType.CLASS);
		put("int", TokenType.INT);
		put("float", TokenType.FLOAT);
		put("get", TokenType.GET);
		put("put", TokenType.PUT);
		put("return", TokenType.RETURN);
		put("program", TokenType.PROGRAM);
	}};
}
