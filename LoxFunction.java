import java.util.List;

public class LoxFunction implements LoxCallable{
    private final Stmt.Function declaration;

    LoxFunction(Stmt.Function declaration){
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'arity'");
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for(int i=0; i<declaration.params.size(); i++){
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        interpreter.executeBlock(declaration.body, environment);
        return null;
    }
    
}
