package plc.project;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        try {
            var source = """
                    DEF main() DO
                       LET a1 : Integer;
                    END
                    """;
            var lexer = new Lexer(source);
            var tokens = lexer.lex();
            var parser = new ParserCompiler(tokens);
            var ast = parser.parseSource();
            var analyzer = new Analyzer(null);
            analyzer.visit(ast);
            var writer = new PrintWriter("out.txt");
            var generator = new Generator(writer);
            generator.visit(ast);
            writer.flush();
        } catch (FileNotFoundException e) {
            System.out.println("Failed creating the out file.");
        }
    }
}
