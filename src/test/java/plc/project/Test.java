package plc.project;

public class Test {
    public static void main(String[] args) {
        Lexer lexer;
        try {
            lexer = new Lexer("'abc");
            lexer.lex();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getIndex());
        }

        try {
            lexer = new Lexer("'\\'"); // --> '\'
            lexer.lex();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getIndex());
        }

        try {
            lexer = new Lexer("\"abc \\hello\"");
            lexer.lex();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getIndex());
        }

        try {
            lexer = new Lexer("\"abc");
            lexer.lex();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getIndex());
        }

        try {
            lexer = new Lexer("\"invalid\\escape\"");
            lexer.lex();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getIndex());
        }

        try {
            lexer = new Lexer("\"");
            lexer.lex();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getIndex());
        }
    }
}
