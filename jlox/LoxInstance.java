import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public LoxClass getKlass() {
        return klass;
    }

    public Object get(Token name) {
       if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) {
            return method.bind(this); // Bind the method to this instance
        }
    
        throw new RuntimeError(null, "Undefined property '" + name + "'.");
    }
    

    

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return "<instance " + klass.getName() + ">";
    }
}
