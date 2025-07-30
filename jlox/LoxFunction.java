import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Enviroment closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration , Enviroment closure , boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }


    public LoxFunction bind(LoxInstance instance) {
        Enviroment environment = new Enviroment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment , isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Enviroment environment = new Enviroment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) {
                return closure.getAt(0, "this"); // Return 'this' for initializers
            }
            return returnValue.value;
        }
        
        if(isInitializer) return closure.getAt(0,"this");
        return null; // If no return statement is executed    
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
    
}
