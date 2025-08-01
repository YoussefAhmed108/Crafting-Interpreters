import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.io.IOException;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static final Interpreter interpreter = new Interpreter();

    private static void runFile(String path) {
        // Here you would implement the logic to read and execute a file
        try {
            byte[] bytes = Files.readAllBytes(java.nio.file.Paths.get(path));
            run(new String(bytes, Charset.defaultCharset()));
            if (hadError) {
                System.exit(65);
            }
            if (hadRuntimeError) {
                System.exit(70);
            }
        } catch (IOException e) {
            System.err.println("Could not read file: " + e.getMessage());
            System.exit(66);
        }
    }

    private static void runPrompt() {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print("> ");
            String line;
            try {
                line = reader.readLine();
            } catch (Exception e) {
                System.err.println("Error reading input: " + e.getMessage());
                continue;
            }
            if (line == null)
                break; // EOF
            run(line);
            hadError = false; // Reset error state after each line
        }

    }

    private static void run(String source) {
        // Here you would implement the logic to interpret or compile the source code
        System.out.println("Running: " + source);
        
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError)
            return;
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
        // System.out.println(new ASTPrinter().print(expression));
        interpreter.interpret(statements);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'",
                    message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);

        } else {
            runPrompt();
        }
    }
}
