
public class Interpreter implements Expr.Visitor<Object> {

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

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left > (Double) right;

            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (Double) left < (Double) right;

            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left >= (Double)right;

            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (Double)left <= (Double)right;
            
            case BANG_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return !isEqual(left, right);
            
            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);
                
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right){
        if(left instanceof Double && right instanceof Double) return;
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
                return -(Double) right;

            case BANG:
                return !isTruthy(right);
        }

        return null;
    }

    private Boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (Boolean) object;
        return true;
    }

    private Boolean isEqual(Object left, Object right){
        if(left == null && right == null) return true;
        if(left == null) return false;

        return left.equals(right);
    }

    private String stringify(Object object){
        if(object == null) return "nil";

        if(object instanceof Double){
            String text = object.toString();
            if(text.startsWith(".0")){
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void interpret(Expr expression){
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

}