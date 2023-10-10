import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum TokenType {
    WORD, NUMBER, SEPARATOR, STRINGLITERAL, PATTERN, TWOCCHARSYMBOL, ONECHARSYMBOL,
    // Add token types for keywords
    WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, NEXTFILE, FUNCTION,
    POSTINC, POSTDEC, PREINC, PREDEC, EXPONENT_ASSIGN, MODULO_ASSIGN, MULTIPLY_ASSIGN,
    DIVIDE_ASSIGN, ADD_ASSIGN, SUBTRACT_ASSIGN, NOTMATCH, AND, OR, CONDITIONAL,
    EXP_ASSIGN, MOD_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, SUB_ASSIGN, ASSIGN
}

enum Operation {
    EXPONENT, MULTIPLY, DIVIDE, MODULO, ADD, SUBTRACT, CONCATENATION,
    LT, LE, GT, GE, EQ, NE, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
    PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
    EXPONENT_ASSIGN, MODULO_ASSIGN, MULTIPLY_ASSIGN,
    DIVIDE_ASSIGN, ADD_ASSIGN, SUBTRACT_ASSIGN, CONDITIONAL,
    EXP_ASSIGN, MOD_ASSIGN, MUL_ASSIGN, DIV_ASSIGN, SUB_ASSIGN, ASSIGN
}

class Token {
    private TokenType type;
    private String value;
    private int lineNumber;
    private int position;
    private Operation operation;

    public Token(TokenType type, String value, int lineNumber, int position) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
        this.position = position;
        this.operation = operation;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return type + "(" + value + ") at line " + lineNumber + ", position " + position;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

class StringHandler {
    private String document;
    private int index;

    public StringHandler(String document) {
        this.document = document;
        this.index = 0;
    }

    public char peek(int i) {
        if (index + i < document.length()) {
            return document.charAt(index + i);
        }
        return '\0'; // End of document
    }

    public String peekString(int i) {
        StringBuilder builder = new StringBuilder();
        int peekIndex = index;
        while (peekIndex < document.length() && i > 0) {
            builder.append(document.charAt(peekIndex));
            peekIndex++;
            i--;
        }
        return builder.toString();
    }

    public char getChar() {
        if (index < document.length()) {
            char currentChar = document.charAt(index);
            index++;
            return currentChar;
        }
        return '\0'; // End of document
    }

    public void swallow(int i) {
        while (i > 0 && index < document.length()) {
            index++;
            i--;
        }
    }

    public boolean isDone() {
        return index >= document.length();
    }

    public String remainder() {
        return document.substring(index);
    }
}

class Lexer {
    private StringHandler stringHandler;
    private int lineNumber;
    private int linePosition;
    private Map<String, TokenType> keywordMap;
    private Map<String, TokenType> twoCharSymbolMap;
    private Map<String, TokenType> oneCharSymbolMap;

    public Lexer(String document) {
        this.stringHandler = new StringHandler(document);
        this.lineNumber = 1;
        this.linePosition = 0;
        initializeKeywordMap();
        initializeSymbolMaps();
    }

    private void initializeKeywordMap() {
        keywordMap = new HashMap<>();
        // Populate the keyword map
        keywordMap.put("while", TokenType.WHILE);
        keywordMap.put("if", TokenType.IF);
        keywordMap.put("do", TokenType.DO);
        keywordMap.put("for", TokenType.FOR);
        keywordMap.put("break", TokenType.BREAK);
        keywordMap.put("continue", TokenType.CONTINUE);
        keywordMap.put("else", TokenType.ELSE);
        keywordMap.put("return", TokenType.RETURN);
        keywordMap.put("BEGIN", TokenType.BEGIN);
        keywordMap.put("END", TokenType.END);
        keywordMap.put("print", TokenType.PRINT);
        keywordMap.put("printf", TokenType.PRINTF);
        keywordMap.put("next", TokenType.NEXT);
        keywordMap.put("in", TokenType.IN);
        keywordMap.put("delete", TokenType.DELETE);
        keywordMap.put("getline", TokenType.GETLINE);
        keywordMap.put("exit", TokenType.EXIT);
        keywordMap.put("nextfile", TokenType.NEXTFILE);
        keywordMap.put("function", TokenType.FUNCTION);
    }

    private void initializeSymbolMaps() {
        twoCharSymbolMap = new HashMap<>();
        oneCharSymbolMap = new HashMap<>();
        // Populate the two-character symbol map
        twoCharSymbolMap.put(">=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("++", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("--", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("<=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("==", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("!=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("^=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("%=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("*=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("/=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("+=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("-=", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("!~", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("&&", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put(">>", TokenType.TWOCCHARSYMBOL);
        twoCharSymbolMap.put("||", TokenType.TWOCCHARSYMBOL);

        // Populate the one-character symbol map
        oneCharSymbolMap.put("{", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("}", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("[", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("]", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("(", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put(")", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("$", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("~", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("=", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("<", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put(">", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("!", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("+", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("^", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("-", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("?", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put(":", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("*", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("/", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("%", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put("|", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put(",", TokenType.ONECHARSYMBOL);
        oneCharSymbolMap.put(";", TokenType.SEPARATOR);
        oneCharSymbolMap.put("\n", TokenType.SEPARATOR); // Newline or semicolon as separator
    }

    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();
        while (!stringHandler.isDone()) {
            char currentChar = stringHandler.peek(0);
            if (currentChar == ' ' || currentChar == '\t' || currentChar == '\r') {
                // Ignore whitespace
                stringHandler.swallow(1);
                linePosition++;
            } else if (currentChar == '\n') {
                // Newline
                tokens.add(new Token(TokenType.SEPARATOR, "", lineNumber, linePosition));
                stringHandler.getChar();
                lineNumber++;
                linePosition = 0;
            } else if (currentChar == '#') {
                // Comment - skip to end of the line
                while (currentChar != '\n' && !stringHandler.isDone()) {
                    currentChar = stringHandler.getChar();
                }
                // No need to create a token for comments
            } else if (Character.isLetter(currentChar)) {
                // Process words
                Token wordToken = processWord();
                tokens.add(wordToken);
            } else if (Character.isDigit(currentChar)) {
                // Process numbers
                Token numberToken = processNumber();
                tokens.add(numberToken);
            } else if (currentChar == '"') {
                // Process string literals
                Token stringLiteralToken = handleStringLiteral();
                tokens.add(stringLiteralToken);
            } else if (currentChar == '`') {
                // Process patterns
                Token patternToken = handlePattern();
                tokens.add(patternToken);
            } else {
                // Process symbols
                Token symbolToken = processSymbol();
                if (symbolToken != null) {
                    tokens.add(symbolToken);
                } else {
                    // Unrecognized character
                    throw new IllegalArgumentException("Unrecognized character: " + currentChar);
                }
            }
        }
        return tokens;
    }

    private Token processWord() {
        StringBuilder wordBuilder = new StringBuilder();
        while (!stringHandler.isDone() && (Character.isLetterOrDigit(stringHandler.peek(0)) || stringHandler.peek(0) == '_')) {
            wordBuilder.append(stringHandler.getChar());
            linePosition++;
        }
        String word = wordBuilder.toString();
        // Check if the word is a keyword
        TokenType tokenType = keywordMap.getOrDefault(word, TokenType.WORD);
        return new Token(tokenType, word, lineNumber, linePosition - word.length());
    }

    private Token processNumber() {
        StringBuilder numberBuilder = new StringBuilder();
        while (!stringHandler.isDone() && (Character.isDigit(stringHandler.peek(0)) || stringHandler.peek(0) == '.')) {
            numberBuilder.append(stringHandler.getChar());
            linePosition++;
        }
        return new Token(TokenType.NUMBER, numberBuilder.toString(), lineNumber, linePosition - numberBuilder.length());
    }

    private Token handleStringLiteral() {
        StringBuilder stringLiteralBuilder = new StringBuilder();
        stringLiteralBuilder.append(stringHandler.getChar()); // Consume the opening "
        while (!stringHandler.isDone()) {
            char currentChar = stringHandler.getChar();
            if (currentChar == '"') {
                // End of string literal
                return new Token(TokenType.STRINGLITERAL, stringLiteralBuilder.toString(), lineNumber, linePosition - stringLiteralBuilder.length());
            } else if (currentChar == '\\') {
                // Handle escaped characters
                char escapedChar = stringHandler.getChar();
                stringLiteralBuilder.append(escapedChar);
            } else {
                stringLiteralBuilder.append(currentChar);
            }
            linePosition++;
        }
        // If we reach here, the string literal is unterminated
        throw new IllegalArgumentException("Unterminated string literal at line " + lineNumber + ", position " + linePosition);
    }

    private Token handlePattern() {
        StringBuilder patternBuilder = new StringBuilder();
        patternBuilder.append(stringHandler.getChar()); // Consume the opening `
        while (!stringHandler.isDone()) {
            char currentChar = stringHandler.getChar();
            if (currentChar == '`') {
                // End of pattern
                return new Token(TokenType.PATTERN, patternBuilder.toString(), lineNumber, linePosition - patternBuilder.length());
            } else if (currentChar == '\\') {
                // Handle escaped characters
                char escapedChar = stringHandler.getChar();
                patternBuilder.append(escapedChar);
            } else {
                patternBuilder.append(currentChar);
            }
            linePosition++;
        }
        // If we reach here, the pattern is unterminated
        throw new IllegalArgumentException("Unterminated pattern at line " + lineNumber + ", position " + linePosition);
    }

    private Token processSymbol() {
        String twoCharSymbol = stringHandler.peekString(2);
        if (twoCharSymbolMap.containsKey(twoCharSymbol)) {
            // Two-character symbol
            stringHandler.swallow(2);
            Token token = new Token(twoCharSymbolMap.get(twoCharSymbol), twoCharSymbol, lineNumber, linePosition);
            linePosition += 2;
            return token;
        } else {
        // Check for increment and decrement operations
        Token symbolToken = processOperation();
        if (symbolToken != null) {
            return symbolToken;
        	} else {
            // One-character symbol
            String oneCharSymbol = stringHandler.peekString(1);
            if (oneCharSymbolMap.containsKey(oneCharSymbol)) {
                stringHandler.swallow(1);
                Token token = new Token(oneCharSymbolMap.get(oneCharSymbol), oneCharSymbol, lineNumber, linePosition);
                linePosition++;
                return token;
            } else {
                return null; // Not a recognized symbol
            }
        }
        }
    }
    
    private TokenType getOperationTokenType(Operation operation) {
        // Map operations to corresponding token types
	        if (operation == Operation.EXPONENT_ASSIGN) {
	        return TokenType.EXPONENT_ASSIGN;
	    } else if (operation == Operation.MODULO_ASSIGN) {
	        return TokenType.MODULO_ASSIGN;
	    } else if (operation == Operation.MULTIPLY_ASSIGN) {
	        return TokenType.MULTIPLY_ASSIGN;
	    } else if (operation == Operation.DIVIDE_ASSIGN) {
	        return TokenType.DIVIDE_ASSIGN;
	    } else if (operation == Operation.ADD_ASSIGN) {
	        return TokenType.ADD_ASSIGN;
	    } else if (operation == Operation.SUBTRACT_ASSIGN) {
	        return TokenType.SUBTRACT_ASSIGN;
	    } else if (operation == Operation.NOTMATCH) {
	        return TokenType.NOTMATCH;
	    } else if (operation == Operation.AND) {
	        return TokenType.AND;
	    } else if (operation == Operation.OR) {
	        return TokenType.OR;
	    } else if (operation == Operation.CONDITIONAL) {
	        return TokenType.CONDITIONAL;
	    } else if (operation == Operation.EXP_ASSIGN) {
	        return TokenType.EXP_ASSIGN;
	    } else if (operation == Operation.MOD_ASSIGN) {
	        return TokenType.MOD_ASSIGN;
	    } else if (operation == Operation.MUL_ASSIGN) {
	        return TokenType.MUL_ASSIGN;
	    } else if (operation == Operation.DIV_ASSIGN) {
	        return TokenType.DIV_ASSIGN;
	    } else if (operation == Operation.ADD_ASSIGN) {
	        return TokenType.ADD_ASSIGN;
	    } else if (operation == Operation.SUB_ASSIGN) {
	        return TokenType.SUB_ASSIGN;
	    } else if (operation == Operation.ASSIGN) {
	        return TokenType.ASSIGN;
	    } else {
	        return null; // Unknown operation
	    }
    }
    
    public Token processOperation() {
        Operation operation = parseOperation();
        if (operation != null) {
            return new Token(TokenType.TWOCCHARSYMBOL, operation.toString(), lineNumber, linePosition);
        }
        return null; // Return null if no operation is found
    }

    private Operation parseOperation() {
        char currentChar = stringHandler.peek(0);
        char nextChar = stringHandler.peek(1);

        if (currentChar == '^') {
            stringHandler.swallow(1);
            return Operation.EXPONENT;
        } else if (currentChar == '*' && nextChar == '=') {
            stringHandler.swallow(2);
            return Operation.MULTIPLY_ASSIGN;
        } else if (currentChar == '/') {
            stringHandler.swallow(1);
            return Operation.DIVIDE;
        } else if (currentChar == '%' && nextChar == '=') {
            stringHandler.swallow(2);
            return Operation.MODULO_ASSIGN;
        } else if (currentChar == '+' && nextChar == '=') {
            stringHandler.swallow(2);
            return Operation.ADD_ASSIGN;
        } else if (currentChar == '-' && nextChar == '=') {
            stringHandler.swallow(2);
            return Operation.SUBTRACT_ASSIGN;
        } else if (currentChar == '+' && nextChar == '+') {
            stringHandler.swallow(2);
            return Operation.POSTINC;
        } else if (currentChar == '-' && nextChar == '-') {
            stringHandler.swallow(2);
            return Operation.POSTDEC;
        } else if (currentChar == '!') {
            if (nextChar == '~') {
                stringHandler.swallow(2);
                return Operation.NOTMATCH;
            }
            stringHandler.swallow(1);
            return Operation.NOT;
        } else if (currentChar == '+' && nextChar == '=') {
            stringHandler.swallow(2);
            return Operation.UNARYPOS;
        } else if (currentChar == '-' && nextChar == '=') {
            stringHandler.swallow(2);
            return Operation.UNARYNEG;
        } else if (currentChar == 'i' && nextChar == 'n') {
            stringHandler.swallow(2);
            return Operation.IN;
        }

        return null; // Return null if no operation is recognized
    }
    
class MathOpNode {
	    private Node left;
	    private Operation operation;
	    private Node right;
	
	    public MathOpNode(Node left, Operation operation, Node right) {
	        this.left = left;
	        this.operation = operation;
	        this.right = right;
	    }
	}
	
class TernaryNode {
	    private Node condition;
	    private Node trueCase;
	    private Node falseCase;
	
	    public TernaryNode(Node condition, Node trueCase, Node falseCase) {
	        this.condition = condition;
	        this.trueCase = trueCase;
	        this.falseCase = falseCase;
	    }
	}
	
class Node {
	    private TokenType type;
	    private String value;
	    private int lineNumber;
	    private int position;
	    private Operation operation;
	
	    public Node(TokenType type, String value, int lineNumber, int position) {
	        this.type = type;
	        this.value = value;
	        this.lineNumber = lineNumber;
	        this.position = position;
	    }
	}

    public static void main(String[] args) {
        if (args.length >= 1) {
            System.out.println("Usage: java Lexer <filename>");
            return;
        }
        String filePath = "test.txt";
        try {
            Path myPath = Paths.get(filePath);
            String content = new String(Files.readAllBytes(myPath));

            Lexer lexer = new Lexer(content);
            List<Token> tokens = lexer.lex();

	        for (Token token : tokens) {
	            if (token.getType() == TokenType.TWOCCHARSYMBOL) {
	                System.out.println("Operation: " + token.getValue());
	            } else {
	                System. out.println(token);
	            }
	        }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
