import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException{}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<Stmt>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        // try {
        //     return expression();
        // } catch (ParseError e) {
        //     return null;
        // }

        return statements;
    }

    private Expr expression(){
        return assignment();
    }

    private Stmt declaration(){
        try {
            if(match(TokenType.VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }   
    }

    private Stmt statement(){
        if(match(TokenType.PRINT)) return printStatement();

        return expressionStatement();
    }

    private Stmt printStatement(){
        Expr value = expression();
        
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration(){
        Token identifier = consume(TokenType.IDENTIFIER, "Expected variable name after 'var'.");

        Expr expression = null;
        // check if there is an equal token after the identifier name, if there is one then it means that its both variable definition and assignment, not just definition.
        // here the assignment is considered an initialization and not just a macro for definition then assignment.
        if(match(TokenType.EQUAL)){
            expression = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        Stmt stmt = new Stmt.Var(identifier, expression);
        // return new Stmt.Var(identifier, expression);
        return stmt;
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr assignment(){
        Expr expr = equality();

        if(match(TokenType.EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();
        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison(){
        Expr expr = term();
        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term(){
        Expr expr = factor();
        while(match(TokenType.MINUS, TokenType.PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor(){
        Expr expr = unary();
        while(match(TokenType.STAR, TokenType.SLASH)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary(){
        if(match(TokenType.BANG, TokenType.MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary(){
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);

        if(match(TokenType.NUMBER, TokenType.STRING)){
            return new Expr.Literal(previous().literal);
        }

        // case for identifier names, when an identifier token is found then its turned into a variable invocation expression
        // implemented by the interpreter.
        if(match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if(match(TokenType.LEFT_PAREN)){
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType...types){
        for (TokenType type : types) {
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message){
        if(check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd(){
        return peek().type == TokenType.EOF;
    }

    private Token previous(){
        return this.tokens.get(current - 1);
    }

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize(){
        advance();

        // we advance the counter until we exit a statement, signified by the ';' token.
        while(!isAtEnd()){
            if(previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS: case FOR: case FUN: case IF: case PRINT: case RETURN:
                case VAR: case WHILE:
                return;
            }

            advance();
        }
    }

    private Token peek(){
        return this.tokens.get(current);
    }

    private Token advance(){
        if(!isAtEnd()) return this.tokens.get(current++);
        return previous();
    }

}
