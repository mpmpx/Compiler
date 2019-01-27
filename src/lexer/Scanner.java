package lexer;

import token.Token;
import token.TokenType;
import utilities.FileReaderWrapper;

public class Scanner {
	
	private static final int EOF = -1;
	private FileReaderWrapper fileReader;
	private int lineNum;
	private char currentChar;
	private boolean isBackUp;
	private boolean isEOF;
	private String tokenValue;
	
	public Scanner(String fileName) {

		lineNum = 1;
		currentChar = '\n';
		isBackUp = false;
		isEOF = false;
		
		fileReader = new FileReaderWrapper(fileName);
	}
	
	public Token nextToken() {
		tokenValue = "";
		
		if (isEOF) {
			return new Token(TokenType.EOF, "EOF", lineNum);
		}
		
		nextChar();
		// number
		if (Character.isDigit(currentChar)) {
			// integer
			makeInteger();
			nextChar();
			
			if (currentChar != '.') {
				backup();
				return new Token(TokenType.INT_NUM, tokenValue, lineNum);
			}
			// fraction [e[+|-] integer]
			else {
				// fraction
				if (!makeFraction()) {
					return new Token(TokenType.ERROR_NUM, tokenValue, lineNum);
				}
				
				nextChar();
				
				// [e[+|-] integer]
				if (currentChar == 'e') {
					// e
					tokenValue += 'e';
					nextChar();
					
					// [+|-]
					if (currentChar == '+' || currentChar == '-') {
						tokenValue += currentChar;
						nextChar();
					} 
					
					// integer
					if (Character.isDigit(currentChar)) {
						makeInteger();
					} 
					else {
						backup();
						return new Token(TokenType.ERROR_NUM, tokenValue, lineNum);
					}
				}
				
				return new Token(TokenType.FLOAT_NUM, tokenValue, lineNum);
			}
		}
		// id
		else if (Character.isAlphabetic(currentChar)) {
			tokenValue += currentChar;
			nextChar();
			while (isAlphanum((char) currentChar)) {
				tokenValue += currentChar;
				nextChar();
			}
			
			backup();
			
			if (TokenType.reservedWordsMap.containsKey(tokenValue)) {
				return new Token(TokenType.reservedWordsMap.get(tokenValue), tokenValue, lineNum);
			}
			else {
				return new Token(TokenType.ID, tokenValue, lineNum);
			}
		}
		// Operators and punctuation
		else {
			switch (currentChar) {
			// Operators
			case '=' :	
				nextChar();
				if (currentChar == '=') {
					return new Token(TokenType.EQ, "==", lineNum);
				}
				else {
					backup();
					return new Token(TokenType.ASSIGN, "=", lineNum);
				}
			case '<' :
				nextChar();
				if (currentChar == '=') {
					return new Token(TokenType.LEQ, "<=", lineNum);
				}
				else if (currentChar == '>') {
					return new Token(TokenType.NEQ, "<>", lineNum);
				}
				else {
					backup();
					return new Token(TokenType.LT, "<", lineNum);
				}
			case '>' :
				nextChar();
				if (currentChar == '=') {
					return new Token(TokenType.GEQ, ">=", lineNum);
				}
				else {
					backup();
					return new Token(TokenType.GT, ">", lineNum);
				}
			case '+' :
				return new Token(TokenType.ADD, "+", lineNum);
			case '-' :
				return new Token(TokenType.SUB, "-", lineNum);
			case '*' :
				return new Token(TokenType.MUL, "*", lineNum);
			case '/' :
				nextChar();
				// // comment
				if (currentChar == '/') {
					nextChar();
					while (currentChar != '\n' && currentChar != (char) EOF) {
						nextChar();
					}
					backup();
					
					return nextToken();
				}
				// /* ... */ comment
				else if (currentChar == '*') {
					char reservedChar = ' ';
					nextChar();
					while (true) {
						if (currentChar == '\n') {
							lineNum++;
						}
						else if (currentChar == '/' && reservedChar == '*') {
							return nextToken();
						} else if (currentChar == (char) EOF) {
							backup();
							return nextToken();
						}
						reservedChar = currentChar;
						nextChar();
					}
				}
				else {
					backup();
					return new Token(TokenType.DIV, "/", lineNum);
				}
			case '!' :
				return new Token(TokenType.NOT, "!", lineNum);

			case '&' :
				nextChar();
				if (currentChar == '&') {
					return new Token(TokenType.AND, "&&", lineNum);
				}
				else {
					backup();
	            	return new Token(TokenType.ERROR_ID, "&", lineNum);
				}
				
			case '|' :
				nextChar();
				if (currentChar == '|') {
					return new Token(TokenType.OR, "||", lineNum);
				}
				else {
					backup();
	            	return new Token(TokenType.ERROR_ID, "|", lineNum);
				}
				
			// Punctuation
			case ':' :
				nextChar();
				if (currentChar == ':') {
					return new Token(TokenType.SR, "::", lineNum);
				}
				else {
					backup();
					return new Token(TokenType.COLON, ":", lineNum);
				}
			case ',' :
				return new Token(TokenType.COMMA, ",", lineNum);
			case '.' :
				return new Token(TokenType.DOT, ".", lineNum);
			case ';' :
				return new Token(TokenType.SEMICOLON, ";", lineNum);
			case '[' :
				return new Token(TokenType.LBRACKET, "[", lineNum);
			case ']' :
				return new Token(TokenType.RBRACKET, "]", lineNum);
			case '{' :
				return new Token(TokenType.LBRACE, "{", lineNum);
			case '}' :
				return new Token(TokenType.RBRACE, "}", lineNum);
			case '(' :
				return new Token(TokenType.LPAREN, "(", lineNum);
			case ')' :
				return new Token(TokenType.RPAREN, ")", lineNum);
			
			// space, line feeder and the end of the line
            case ' ':
            	nextChar();
            	while (currentChar == ' ') {
            		nextChar();
            	}
            	backup();
            	return nextToken();
            case '\t':
            case '\r':
				return nextToken();
			case '\n' :
				lineNum++;
				return nextToken();
				
			// the end of the file
			case (char)EOF :
				isEOF = true;
				return new Token(TokenType.EOF, "EOF", lineNum);
            // invalid identifier
			default :
            	tokenValue += currentChar;
            	return new Token(TokenType.ERROR_ID, tokenValue, lineNum);
			}
		}
		
		
	}
	
	
	private Boolean makeFraction() {
		tokenValue += currentChar;
		nextChar();
		
		// error format of fraction
		if (!Character.isDigit(currentChar)) {
			backup();
			return false;
		}
		// .0
		else if (currentChar == '0') {
			tokenValue += currentChar;
			nextChar();
			
			if (!Character.isDigit(currentChar)) {
				backup();
				return true;
			}
		}
		
		// digit* nonzero
		tokenValue += currentChar;
		nextChar();
		while (Character.isDigit(currentChar)) {
			tokenValue += currentChar;
			nextChar();
		}
		backup();
		return !tokenValue.endsWith("0");
	}
	
	private void makeInteger() {
		tokenValue += currentChar;
		if (currentChar == '0') {
			return;
		}
		
		nextChar();
		while (Character.isDigit(currentChar)) {
			tokenValue += currentChar;
			nextChar();
		}
		
		backup();
		return;
	}
	
	private void nextChar() {
		if (isBackUp) {
			isBackUp = false;
		}
		else {
			currentChar = fileReader.read();
		}
	}
	
	private void backup() {
		isBackUp = true;
	}
	
	private boolean isAlphanum(char c) {
		return Character.isDigit(c) || Character.isAlphabetic(c) || c == '_';
	}
	
	public void close() {
		fileReader.close();;
	}
	
}