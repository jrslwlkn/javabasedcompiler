package plc.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No source file. Exiting...");
            System.exit(1);
        }
        PrintWriter writer = null;
        try {
            var lexer = new Lexer(Files.readString(Path.of(args[0]), StandardCharsets.UTF_8));
            var tokens = lexer.lex();
            var parser = new ParserCompiler(tokens);
            var ast = parser.parseSource();
            var analyzer = new Analyzer(null);
            analyzer.visit(ast);
            writer = new PrintWriter("Main.java");
            var generator = new Generator(writer);
            generator.visit(ast);
        } catch (IOException e) {
            System.err.println("Failed reading the source file or creating the out file.");
            System.exit(2);
        } finally {
            if (writer != null)
                writer.flush();
        }
    }
}
