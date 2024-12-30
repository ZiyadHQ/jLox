import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Lox
{
    private static Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private static void runFile(String filePath) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(bytes, Charset.defaultCharset()));

        if(hadError) System.exit(65); 
        if(hadRuntimeError) System.exit(70);
    }

    ///read the input stream line by line until reaching a null line, in which case exit the script loop.
    /// each line must be a valid jLox statement/expression.
    public static void runPrompt() throws IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        //equivelant to while(true)
        for(;;)
        {
            System.out.print(">");
            String line = reader.readLine();
            if(line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) throws IOException
    {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // stop if there was a syntax error
        if(hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message)
    {
        report(line, "", message);
    }

    private static void report(int line, String where, String message)
    {
        System.err.println
        (
            "[line " + line + "] Error " + where + ": " + message
        );
        hadError = true;
    }

    static void error(Token token, String message){
        if(token.type == TokenType.EOF){
            report(token.line, " at end", message);
        }else{
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error){
        System.err.println(error.getMessage()
        + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;       
    }

    public static void main(String[] args) throws IOException {

        if(args.length > 1)
        {
            System.out.println("Usage: jLox [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
    }

}