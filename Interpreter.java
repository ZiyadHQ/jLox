
public class Interpreter implements Expr.Visitor<Object> {

	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case MINUS:
                return (Double)left - (Double)right;
            
            case SLASH:
                return (Double)left / (Double)right;
            
            case STAR:
                return (Double)left * (Double)right;
            
            case PLUS:
                if(left instanceof String && right instanceof String)
                    return (String)left + (String)right;
                else
                    return (Double)left + (Double)right;
        }

        return null;
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
                return -(Double)right; 

            case BANG:
                return !isTruthy(right);
        }

        return null;
    }

	@Override
	public Object visitPrintExpr(Expr.Print expr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitPrintExpr'");
	}

    private Boolean isTruthy(Object object){
        if(object == null) return false;
        if(object instanceof Boolean) return (Boolean)object;
        return true;
    }

    private Object evaluate(Expr expr){
        return expr.accept(this);
    }
    
}