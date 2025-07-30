public class ASTPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }


    private String parenthesize(String name, Expr... expressions) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Expr expr : expressions) {
            builder.append(" ").append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }


    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        // TODO Auto-generated method stub
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }


    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        // TODO Auto-generated method stub
        return parenthesize("group", expr.expression);
    }


    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        // TODO Auto-generated method stub
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }


    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        // TODO Auto-generated method stub
        return parenthesize(expr.operator.lexeme, expr.right);
    }


    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(new Expr.Literal(45.67))
        );

        ASTPrinter printer = new ASTPrinter();
        String result = printer.print(expression);
        System.out.println(result); // Output: (* (- 123) (group 45.67))
    }


    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVariableExpr'");
    }


    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
    }


    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLogicalExpr'");
    }


    @Override
    public String visitCallExpr(Expr.Call expr) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }
}
