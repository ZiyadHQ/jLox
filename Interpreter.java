import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for(Stmt statement : statements){
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {

        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case MINUS:
                checkNumberOperand(expr.operator, right);
                return (Double) left - (Double) right;

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left / (Double) right;

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left * (Double) right;

            // the PLUS operator can both add numbers and concat Strings.
            case PLUS:
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                if (left instanceof Double && right instanceof Double)
                    return (Double) left + (Double) right;
                if (left instanceof String || right instanceof String)
                    return stringify(left) + stringify(right);
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left > (Double) right;

            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left < (Double) right;

            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left >= (Double) right;

            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left <= (Double) right;

            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);

        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (operator.type == TokenType.SLASH && right instanceof Double && (Double) right == 0)
            throw new RuntimeError(operator, "Division by zero.");
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be a number.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr);

        switch (expr.operator.type) {
            case MINUS:
                System.out.println("dividing by: " + stringify(right));
                return -(Double) right;

            case BANG:
                return !isTruthy(right);
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        Object value = environment.get(expr.name);
        return value;
    }

    private Boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (Boolean) object;
        return true;
    }

    private Boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;

        return left.equals(right);
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.startsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt){
        stmt.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;

        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

}