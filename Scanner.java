import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static{
        keywords = new HashMap<String,TokenType>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {

            // at the begining of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;

            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
                
            case '/':
                //check if comment block
                if (match('/')) {

                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                }else if(match('*')){
                    // counter represents how many opened comment blocks are there, and also is the number of the required closing
                    // blocks needed to form a proper C-style comment block, any number other than 0 means that the comment blocks
                    // weren't used correctly.
                    int counter = 1;
                    while(!isAtEnd())
                    {
                        if(peek() == '*' && peekNext() == '/'){
                            counter--;
                            if(counter == 0){
                                advance();
                                advance();
                                break;
                            }
                        }else if(peek() == '/' && peekNext() == '*'){
                            advance();
                            counter++;
                        }
                        advance();
                    }
                }else {
                    addToken(TokenType.SLASH);
                }
                break;

            // ignores whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;
            
            case '"': string(); break;

            default:
                if(isDigit(c)){
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }else{
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private boolean isAlpha(char c)
    {
        return c >= 'a' && c <= 'z' ||
            c >= 'A' && c <= 'Z' ||
            c == '_';
    }

    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier()
    {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = TokenType.IDENTIFIER;
        
        addToken(type, text);
    }

    private void string()
    {
        while(peek() != '"' && !isAtEnd())
        {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd())
        {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    // 1337 haxer code
    /// reads the source and returns either EOF or the char at the 'current' string pointer
    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext()
    {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private void number()
    {
        // read every next digit
        while(isDigit(peek())) advance();

        // if there is a '.' after the digits then its either a fractional number or a dot operator, if
        // the char after that is a digit then it must be a fractional number
        if(peek() == '.' && isDigit(peekNext()))
        {
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
