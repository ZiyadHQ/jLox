import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<Expr,Integer>();

    Interpreter(){
        NativeFunctions.registerFunctions(globals);
    }

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
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return -(Double) right;

            case BANG:
                return !isTruthy(right);
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr){
        Integer distance = locals.get(expr);
        if(distance != null){
            return environment.getAt(distance, name.lexeme);
        }else{
            return globals.get(name);
        }
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

    void resolve(Expr expr, int depth){
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try{
            this.environment = environment;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } finally{
            this.environment = previous;
        }
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
    
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);

        Integer distance = locals.get(expr);
        if(distance != null){
            environment.assignAt(distance, expr.name, value);
        }else{
            environment.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if(isTruthy(evaluate(stmt.expression))){
            execute(stmt.thenBranch);
        }else if(stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Boolean leftValue = isTruthy(evaluate(expr.left));

        if(expr.operator.type == TokenType.AND){
            if(leftValue == false) return leftValue;
        }else if(expr.operator.type == TokenType.OR){
            if(leftValue == true) return leftValue;
        }
        
        return isTruthy(evaluate(expr.right));
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<Object>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable)){
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;

        if(arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + "arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);

        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if(stmt.value != null){
            value = evaluate(stmt.value);
        }

        throw new Return(value);
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);
        LoxClass klass = new LoxClass(stmt.name.lexeme);
        environment.assign(stmt.name, klass);
        return null;
    }

}