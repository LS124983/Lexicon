import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestFile {

    private Lexer lexer;

    @Before
    public void setUp() {
        String document = "while (x < 10) {\n" +
                          "    print(\"Hello, World!\");\n" +
                          "}";
        lexer = new Lexer(document);
    }

    @Test
    public void testLex() {
        List<Token> tokens = lexer.lex();

        // Ensure the correct number of tokens
        assertEquals(15, tokens.size());

        // Check some specific tokens
        assertToken(tokens.get(0), TokenType.WHILE, "while", 1, 0);
        assertToken(tokens.get(1), TokenType.ONECHARSYMBOL, "(", 1, 6);
        assertToken(tokens.get(2), TokenType.WORD, "x", 1, 7);
        assertToken(tokens.get(3), TokenType.ONECHARSYMBOL, "<", 1, 9);
        assertToken(tokens.get(4), TokenType.NUMBER, "10", 1, 11);
        assertToken(tokens.get(5), TokenType.ONECHARSYMBOL, ")", 1, 13);
        assertToken(tokens.get(6), TokenType.ONECHARSYMBOL, "{", 1, 15);
        assertToken(tokens.get(8), TokenType.PRINT, "print", 2, 4);
        assertToken(tokens.get(9), TokenType.ONECHARSYMBOL, "(", 2, 9);
        assertToken(tokens.get(11), TokenType.ONECHARSYMBOL, ")", 2, 23);
    }

    private void assertToken(Token token, TokenType expectedType, String expectedValue, int expectedLineNumber,
            int expectedPosition) {
        assertNotNull(token);
        assertEquals(expectedType, token.getType());
        assertEquals(expectedValue, token.getValue());
        assertEquals(expectedLineNumber, token.getLineNumber());
        assertEquals(expectedPosition, token.getPosition());
    }
    
    @Test
    public void testLexWithMultipleOperations() {
        String input = "5 + 3 * (2 - 1) / 2";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.lex();


        assertEquals(TokenType.NUMBER, tokens.get(0).getType());
        assertEquals("5", tokens.get(0).getValue());
    }

    @Test
    public void testLexWithIncrementDecrement() {
        String input = "x++ --y";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.lex();

        assertEquals(4, tokens.size());

        // Check specific tokens and their types
        assertToken(tokens.get(0), TokenType.WORD, "x", 1, 0);
        assertToken(tokens.get(1), TokenType.TWOCCHARSYMBOL, "++", 1, 1);
        assertToken(tokens.get(2), TokenType.TWOCCHARSYMBOL, "--", 1, 4);
        assertToken(tokens.get(3), TokenType.WORD, "y", 1, 6);
    }
    
    @Test
    public void testGetOperationTokenType() throws Exception {
        Method method = Lexer.class.getDeclaredMethod("getOperationTokenType", Operation.class);
        method.setAccessible(true);
        TokenType tokenType = (TokenType) method.invoke(lexer, Operation.EXPONENT_ASSIGN);
        assertEquals(TokenType.EXPONENT_ASSIGN, tokenType);
    }
}
