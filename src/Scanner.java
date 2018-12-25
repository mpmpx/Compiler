import java.util.HashMap;

public class Scanner {
	
	private static final int EOF = -1;
	private FileReaderWrapper fileReader;
	private int lineNum;
	private char currentChar;
	private boolean isBackUp;
	private boolean isEOF;
	
	
	public Scanner(FileReaderWrapper fileReader) {
		this.fileReader = fileReader;
		lineNum = 1;
		currentChar = '\n';
		isBackUp = false;
		isEOF = false;
	}
	
	public Token nextToken() {
		if (isEOF) {
			return new Token(TokenType.EOF, "EOF", lineNum);
		}
		
		nextChar();
		// number
		if (Character.isDigit(currentChar)) {
			String value = "";
			
			// integer
			value += makeInteger();
			nextChar();
			
			if (currentChar != '.') {
				backUp();
				return new Token(TokenType.INT_NUM, value, lineNum);
			}
			// fraction [e[+|-] integer]
			else {
				// fraction
				String fraction = makeFraction();
				value += fraction;
				
				// check whether the last digit of the fraction is nonzero
				if (fraction.length() > 2 && fraction.charAt(fraction.length() - 1) == '0') {
					return new Token(TokenType.ERROR_NUM, value, lineNum);
				}
				nextChar();
				
				// [e[+|-] integer]
				if (currentChar == 'e') {
					// e
					value += 'e';
					nextChar();
					
					// [+|-]
					if (currentChar == '+' || currentChar == '-') {
						value += currentChar;
						nextChar();
					} 
					
					// integer
					if (Character.isDigit(currentChar)) {
						value += makeInteger();
					}
				}
				
				if (Character.isDigit(value.charAt(value.length() - 1))) {
					return new Token(TokenType.FLOAT_NUM, value, lineNum);
				}
				else {
					return new Token(TokenType.ERROR_NUM, value, lineNum);
				}
			}
		}
		// id
		else if (Character.isAlphabetic(currentChar)) {
			String value = "" + currentChar;
			nextChar();
			while (isAlphanum((char) currentChar)) {
				value += currentChar;
				nextChar();
			}
			
			backUp();
			
			if (TokenType.reservedKeysMap.containsKey(value)) {
				return new Token(TokenType.reservedKeysMap.get(value), value, lineNum);
			}
			else {
				return new Token(TokenType.ID, value, lineNum);
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
					backUp();
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
					backUp();
					return new Token(TokenType.LT, "<", lineNum);
				}
			case '>' :
				nextChar();
				if (currentChar == '=') {
					return new Token(TokenType.GEQ, ">=", lineNum);
				}
				else {
					backUp();
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
				if (currentChar == '/') {
					nextChar();
					while (currentChar != '\n') {
						nextChar();
						return nextToken();
					}
				}
				else if (currentChar == '*') {
					char reservedChar = ' ';
					nextChar();
					while (true) {
						if (currentChar == '\n') {
							lineNum++;
						}
						
						if (currentChar == '/' && reservedChar == '*') {
							return nextToken();
						}
						reservedChar = currentChar;
						nextChar();
					}
				}
				else {
					backUp();
					return new Token(TokenType.DIV, "/", lineNum);
				}
			// Punctuation
			case ':' :
				nextChar();
				if (currentChar == ':') {
					return new Token(TokenType.SR, "::", lineNum);
				}
				else {
					backUp();
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
				return new Token(TokenType.LBRACKET, "]", lineNum);
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
            	backUp();
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
            	String value = String.valueOf(currentChar);
				return new Token(TokenType.ERROR_ID, value, lineNum);
			}
		}
		
		
	}
	
	
	private String makeFraction() {
		String value = "" + currentChar;
		nextChar();
		
		// error format of fraction
		if (!Character.isDigit(currentChar)) {
			return value;
		}
		// .0
		else if (currentChar == '0') {
			value += currentChar;
			nextChar();
			
			if (!Character.isDigit(currentChar)) {
				backUp();
				return value;
			}
		}
		
		// digit* nonzero
		value += currentChar;
		nextChar();
		while (Character.isDigit(currentChar)) {
			value += currentChar;
			nextChar();
		}
		backUp();
		return value;
	}
	
	private String makeInteger() {
		String value = "" + currentChar;
		if (currentChar == '0') {
			return value;
		}
		
		nextChar();
		while (Character.isDigit(currentChar)) {
			value += currentChar;
			nextChar();
		}
		
		backUp();
		return value;
	}
	
	private void nextChar() {
		if (isBackUp) {
			isBackUp = false;
		}
		else {
			currentChar = fileReader.read();
		}
	}
	
	private void backUp() {
		isBackUp = true;
	}
	
	private boolean isAlphanum(char c) {
		return Character.isDigit(c) || Character.isAlphabetic(c) || c == '_';
	}
	

}