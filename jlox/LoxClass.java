import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    private final String name;
    private final LoxClass superclass; // Added superclass for inheritance
    private final Map<String, LoxFunction> methods;

    public LoxClass(String name , LoxClass superclass , Map<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;

    }

    public String getName() {
        return name;
    }

    public LoxFunction findMethod(String name) {
        if(methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name); // Check superclass for method
        }
        
        return null; // Method not found
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance; // Return a new instance of the class
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            return initializer.arity();
        }
        return 0; // If no initializer, return 0 arity
    }
}
