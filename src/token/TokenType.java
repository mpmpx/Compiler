package token;
import java.util.HashMap;

/**
 * 
 * @author Peixing Ma
 * This enum class defines all token types.
 */
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
	INTEGER, 		// integer
	FLOAT, 			// float
	READ, 			// read
	WRITE, 			// write
	RETURN, 		// return
	MAIN,			// main

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
	NOT,			// !
	AND,			// &&
	OR,				// ||
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
	
	CMT,			// comment
	ERROR_NUM,      // invalid number
	ERROR_ID,		// invalid identifier
	ERROR_CMT,  // incomplete multiple-line comment
	EOL,			// \n
	EOF, 			// end of the file
	
	EPSILON;
	
	public static HashMap<String, TokenType> reservedWordsMap = new HashMap<String, TokenType>() {{
		put("if", TokenType.IF);
		put("then", TokenType.THEN);
		put("else", TokenType.ELSE);
		put("for", TokenType.FOR);
		put("class", TokenType.CLASS);
		put("integer", TokenType.INTEGER);
		put("float", TokenType.FLOAT);
		put("read", TokenType.READ);
		put("write", TokenType.WRITE);
		put("return", TokenType.RETURN);
		put("main", TokenType.MAIN);
	}};
}
