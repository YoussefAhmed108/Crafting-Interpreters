import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Interpreter implements Expr.Visitor<Object>, 
                                    Stmt.Visitor<Void> {
    
    final Enviroment globals = new Enviroment();
    private Enviroment enviroment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();


    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println("clock called" );
                return (double) System.currentTimeMillis() / 1000.0; // Return current time in seconds
            }

            @Override
            public int arity() {
                return 0; // clock takes no arguments
            }
        });
    }

    public void interpret(List<Stmt> statments) {
       try {
           for (Stmt statement : statments) {
               execute(statement);
           }
       } catch (RuntimeError error) {
           Lox.runtimeError(error);
       }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // TODO Auto-generated method stub
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right; // Concatenate strings
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left - (double) right;
                }

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left * (double) right;
                }

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    if ((double) right == 0.0) {
                        throw new RuntimeException("Division by zero.");
                    }
                    return (double) left / (double) right;
                }
                throw new RuntimeException("Operands must be numbers for '/' operator.");

            // Equality operators
            case EQUAL_EQUAL:
                return isEquals(left, right);
            case BANG_EQUAL:
                return !isEquals(left, right);

            // Comparison operators
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
            default:
                throw new RuntimeException("Unknown operator: " + expr.operator.lexeme);
        }
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // TODO Auto-generated method stub
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        // TODO Auto-generated method stub
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // TODO Auto-generated method stub
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right; // Assuming right is a number
            case BANG:
                return !isTruthy(right); // Negate the truthiness
        }

        return null; // Should not reach here
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false; // nil is false
        if (object instanceof Boolean)
            return (Boolean) object; // true is true, false is false
        return true; // all other objects are truthy
    }

    private boolean isEquals(Object left, Object right) {
        if (left == null && right == null)
            return true; // both are nil
        if (left == null || right == null)
            return false; // one is nil, the other is not
        return left.equals(right); // use equals method for other types
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator,
                "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator,
                "Operand must be a number.");
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
       evaluate(stmt.expression);
       return null; // No return value for expression statements
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
         Object value = evaluate(stmt.expression);
         System.out.println(stringify(value));
         return null; // No return value for print statements
    }

    @Override
    public Void visitVariableStmt(Stmt.Variable stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        enviroment.define(stmt.name.lexeme, value);
        return null; // No return value for variable statements
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // TODO Auto-generated method stub
        return lookupVariable(expr.name, expr);
    }

    private Object lookupVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return enviroment.getAt(distance, name.lexeme);
        }
       
        return globals.get(name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            enviroment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value; // Return the assigned value
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Enviroment(enviroment));
        return null; // No return value for block statements
    }

    public void executeBlock(List<Stmt> statements, Enviroment env) {
        Enviroment previous = this.enviroment;
        try {
            this.enviroment = env;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.enviroment = previous; // Restore the previous environment
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object condition = evaluate(stmt.condition);
        if (isTruthy(condition)) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null; // No return value for if statements
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left; // Short-circuit evaluation for OR
            }
        } else if (expr.operator.type == TokenType.AND) {
            if (!isTruthy(left)) {
                return left; // Short-circuit evaluation for AND
            }
        }
        // If we reach here, evaluate the right side
        return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null; // No return value for while statements
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt , enviroment , false);
        enviroment.define(stmt.name.lexeme, function);
        return null; // No return value for function declarations
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new Return(value); // Throw a Return exception to exit the function
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }
        enviroment.define(stmt.name.lexeme, null); // Define the class in the environment
        if(stmt.superclass != null){
            enviroment = new Enviroment(enviroment);
            enviroment.define("super", superclass);
        }
        
        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, enviroment , method.name.lexeme.equals("init")      );
            methods.put(method.name.lexeme, function); // Add method to the class
        }
        
        LoxClass klass = new LoxClass(stmt.name.lexeme ,(LoxClass) superclass , methods ); // Create a new class instance
        if(stmt.superclass != null){
            enviroment = enviroment.enclosing;
        }
        enviroment.assign(stmt.name, klass); // Assign the class instance to the environment
        return null; // No return value for class declarations
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookupVariable(expr.keyword, expr);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }
        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if(!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have properties.");
        }
        LoxInstance instance = (LoxInstance) object;
        Object value = evaluate(expr.value);
        instance.set(expr.name, value);
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) enviroment.getAt(distance, "super");
        LoxInstance object = (LoxInstance) enviroment.getAt(distance - 1, "this");
        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }
        return method.bind(object);
    }

}
