import java.util.List;

abstract class Expr{
    interface Visitor<R>{
    R visitExpressionExpr(Expression expr);
    R visitPrintExpr(Print expr);
    }
    static class Expression extends Expr{
    Expression(Expr expression){
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitExpressionExpr(this);
    }
    final Expr expression;
    }
    static class Print extends Expr{
    Print(Expr expression){
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitPrintExpr(this);
    }
    final Expr expression;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
