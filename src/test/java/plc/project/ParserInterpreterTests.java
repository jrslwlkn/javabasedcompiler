package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from Homework 1
 * or the LexerTests file from the last project part for more information.
 */
final class ParserInterpreterTests {

    @ParameterizedTest
    @MethodSource
    void testSource(String test, List<Token> tokens, Ast.Source expected) {
        test(tokens, expected, ParserInterpreter::parseSource);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Some Garbage Test Case That should Fail",
                        Arrays.asList(
                                //DEF name() DO obj.method expr; END
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.IDENTIFIER, "expr", 4),
                                new Token(Token.Type.IDENTIFIER, ";", 20),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        null
                ),
                Arguments.of("Method without closing paren",
                        Arrays.asList(
                                //DEF name DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.IDENTIFIER, ";", 20),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        null),
                Arguments.of("Method without parens",
                        Arrays.asList(
                                //DEF name DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.IDENTIFIER, ";", 20),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        null),
                Arguments.of("Method with params",
                        Arrays.asList(
                                //DEF name(x, y, z) DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.IDENTIFIER, "x", 14),
                                new Token(Token.Type.OPERATOR, ",", 9),
                                new Token(Token.Type.IDENTIFIER, "y", 14),
                                new Token(Token.Type.OPERATOR, ",", 9),
                                new Token(Token.Type.IDENTIFIER, "z", 14),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.IDENTIFIER, ";", 20),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new Ast.Source(new ArrayList<>(),
                                List.of(new Ast.Method("name", Arrays.asList("x", "y", "z"), List.of(
                                                new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt"))
                                        ), null)
                                ), null
                        )),
                Arguments.of("Missing END in method",
                        Arrays.asList(
                                //DEF name() DO stmt;
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.IDENTIFIER, ";", 20)
                        ),
                        null
                ),
                Arguments.of("Missing semicolon in method",
                        Arrays.asList(
                                //DEF name() DO stmt END
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        null
                ),
                Arguments.of("Field after method",
                        Arrays.asList(
                                //DEF name() DO stmt; END LET x = 5;
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20),
                                new Token(Token.Type.IDENTIFIER, "LET", 20),
                                new Token(Token.Type.IDENTIFIER, "x", 20),
                                new Token(Token.Type.OPERATOR, "=", 20),
                                new Token(Token.Type.INTEGER, "5", 20),
                                new Token(Token.Type.OPERATOR, ";", 20)
                        ),
                        null
                ),
                Arguments.of("Method",
                        Arrays.asList(
                                //DEF name() DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "DEF", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "(", 8),
                                new Token(Token.Type.OPERATOR, ")", 9),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new Ast.Source(
                                List.of(),
                                List.of(new Ast.Method("name", List.of(), List.of(
                                        new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt"))
                                ), null)),
                                List.of()
                        )
                ),
                Arguments.of("Zero Statements",
                        List.of(),
                        new Ast.Source(List.of(), List.of(), List.of())
                ),
                Arguments.of("Field",
                        Arrays.asList(
                                //LET name = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Source(
                                List.of(new Ast.Field("name", Optional.of(new Ast.Expr.Access(Optional.empty(), "expr")))),
                                List.of(),
                                List.of()
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testExpressionStatement(String test, List<Token> tokens, Ast.Stmt.Expression expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testExpressionStatement() {
        return Stream.of(
                Arguments.of("Invalid Expression",
                        List.of(
                                //name
                                new Token(Token.Type.OPERATOR, "?", 0)
                        ),
                        null
                ),
                Arguments.of("Variable Expression Missing Semicolon",
                        List.of(
                                //name
                                new Token(Token.Type.IDENTIFIER, "name", 0)
                        ),
                        null
                ),
                Arguments.of("Variable Expression",
                        Arrays.asList(
                                //name;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "name"))
                ),
                Arguments.of("Function Expression",
                        Arrays.asList(
                                //name();
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5),
                                new Token(Token.Type.OPERATOR, ";", 6)
                        ),
                        new Ast.Stmt.Expression(new Ast.Expr.Function(Optional.empty(), "name", List.of()))
                ),
                Arguments.of("Function Expression Missing Semicolon",
                        Arrays.asList(
                                //name()
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDeclarationStatement(String test, List<Token> tokens, Ast.Stmt.Declaration expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Definition",
                        Arrays.asList(
                                //LET name;
                                new Token(Token.Type.IDENTIFIER, "LET", -1),
                                new Token(Token.Type.IDENTIFIER, "name", -1),
                                new Token(Token.Type.OPERATOR, ";", -1)
                        ),
                        new Ast.Stmt.Declaration("name", Optional.empty())
                ),
                Arguments.of("Initialization",
                        Arrays.asList(
                                //LET name = expr;
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.IDENTIFIER, "expr", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Stmt.Declaration("name", Optional.of(new Ast.Expr.Access(Optional.empty(), "expr")))
                ),
                Arguments.of("String Escape Initialization",
                        Arrays.asList(
                                //LET name = '\n'; // name is a Character containing a new line char
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.CHARACTER, "'\\n'", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Stmt.Declaration("name", Optional.of(new Ast.Expr.Literal('\n')))
                ),
                Arguments.of("String Escape Initialization 2",
                        Arrays.asList(
                                //LET name = '\''; // name is a Character containing a single quote
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.CHARACTER, "'\\''", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Stmt.Declaration("name", Optional.of(new Ast.Expr.Literal('\'')))
                ),
                Arguments.of("String Escape Initialization 3",
                        Arrays.asList(
                                //LET name = '\\'; // name is a Character containing a backslash
                                new Token(Token.Type.IDENTIFIER, "LET", 0),
                                new Token(Token.Type.IDENTIFIER, "name", 4),
                                new Token(Token.Type.OPERATOR, "=", 9),
                                new Token(Token.Type.CHARACTER, "'\\\\'", 11),
                                new Token(Token.Type.OPERATOR, ";", 15)
                        ),
                        new Ast.Stmt.Declaration("name", Optional.of(new Ast.Expr.Literal('\\')))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAssignmentStatement(String test, List<Token> tokens, Ast.Stmt.Assignment expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testAssignmentStatement() {
        return Stream.of(
                Arguments.of("Expression Assignment",
                        Arrays.asList(
                                //x.y = z;
                                new Token(Token.Type.IDENTIFIER, "x", 0),
                                new Token(Token.Type.OPERATOR, ".", 0),
                                new Token(Token.Type.IDENTIFIER, "y", 0),
                                new Token(Token.Type.OPERATOR, "=", 4),
                                new Token(Token.Type.IDENTIFIER, "z", 6),
                                new Token(Token.Type.OPERATOR, ";", 7)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.of(new Ast.Expr.Access(Optional.empty(), "x")), "y"),
                                new Ast.Expr.Access(Optional.empty(), "z")
                        )
                ),
                Arguments.of("Expression Assignment to Expression",
                        Arrays.asList(
                                //obj.field = expr + 1;
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 2),
                                new Token(Token.Type.IDENTIFIER, "field", 4),
                                new Token(Token.Type.OPERATOR, "=", 6),
                                new Token(Token.Type.IDENTIFIER, "expr", 8),
                                new Token(Token.Type.OPERATOR, "+", 9),
                                new Token(Token.Type.INTEGER, "1", 9),
                                new Token(Token.Type.OPERATOR, ";", 9)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.of(new Ast.Expr.Access(Optional.empty(), "obj")), "field"),
                                new Ast.Expr.Binary(
                                        "+",
                                        new Ast.Expr.Access(Optional.empty(), "expr"),
                                        new Ast.Expr.Literal(new BigInteger("1"))
                                )
                        )
                ),
                Arguments.of("Expression Assignment to Expression",
                        Arrays.asList(
                                //x = x + field;
                                new Token(Token.Type.IDENTIFIER, "x", 0),
                                new Token(Token.Type.OPERATOR, "=", 2),
                                new Token(Token.Type.IDENTIFIER, "x", 4),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "field", 8),
                                new Token(Token.Type.OPERATOR, ";", 9)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "x"),
                                new Ast.Expr.Binary(
                                        "+",
                                        new Ast.Expr.Access(Optional.empty(), "x"),
                                        new Ast.Expr.Access(Optional.empty(), "field")
                                )
                        )
                ),
                Arguments.of("Assignment",
                        Arrays.asList(
                                //name = value;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.IDENTIFIER, "value", 7),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "name"),
                                new Ast.Expr.Access(Optional.empty(), "value")
                        )
                ),

                Arguments.of("Expression Assignment",
                        Arrays.asList(
                                //x.y = z;
                                new Token(Token.Type.IDENTIFIER, "x.y", 0),
                                new Token(Token.Type.OPERATOR, "=", 4),
                                new Token(Token.Type.IDENTIFIER, "z", 6),
                                new Token(Token.Type.OPERATOR, ";", 7)
                        ),
                        new Ast.Stmt.Assignment(
                                new Ast.Expr.Access(Optional.empty(), "x.y"),
                                new Ast.Expr.Access(Optional.empty(), "z")
                        )
                ),
                Arguments.of("Missing Value Assignment",
                        Arrays.asList(
                                //name = ;
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "=", 5),
                                new Token(Token.Type.OPERATOR, ";", 12)
                        ),
                        null
                )
        );
    }


    @ParameterizedTest
    @MethodSource
    void testIfStatement(String test, List<Token> tokens, Ast.Stmt.If expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("Else",
                        Arrays.asList(
                                //IF expr DO stmt1; ELSE stmt2; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 11),
                                new Token(Token.Type.OPERATOR, ";", 16),
                                new Token(Token.Type.IDENTIFIER, "ELSE", 18),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 23),
                                new Token(Token.Type.OPERATOR, ";", 28),
                                new Token(Token.Type.IDENTIFIER, "END", 30)
                        ),
                        new Ast.Stmt.If(
                                new Ast.Expr.Access(Optional.empty(), "expr"),
                                List.of(new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt1"))),
                                List.of(new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt2")))
                        )
                ),
                Arguments.of("If",
                        Arrays.asList(
                                //IF expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "DO", 8),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        new Ast.Stmt.If(
                                new Ast.Expr.Access(Optional.empty(), "expr"),
                                List.of(new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt"))),
                                List.of()
                        )
                ),
                Arguments.of("Missing DO in If",
                        Arrays.asList(
                                //IF expr stmt; END
                                new Token(Token.Type.IDENTIFIER, "IF", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 3),
                                new Token(Token.Type.IDENTIFIER, "stmt", 11),
                                new Token(Token.Type.OPERATOR, ";", 15),
                                new Token(Token.Type.IDENTIFIER, "END", 17)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testForStatement(String test, List<Token> tokens, Ast.Stmt.For expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testForStatement() {
        return Stream.of(
                Arguments.of("For",
                        Arrays.asList(
                                //FOR elem IN list DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "FOR", 0),
                                new Token(Token.Type.IDENTIFIER, "elem", 6),
                                new Token(Token.Type.IDENTIFIER, "IN", 9),
                                new Token(Token.Type.IDENTIFIER, "list", 12),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "stmt", 20),
                                new Token(Token.Type.OPERATOR, ";", 24),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new Ast.Stmt.For(
                                "elem",
                                new Ast.Expr.Access(Optional.empty(), "list"),
                                List.of(new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt")))
                        )
                ),
                Arguments.of("Empty Statement in For",
                        Arrays.asList(
                                //FOR elem IN list DO  END
                                new Token(Token.Type.IDENTIFIER, "FOR", 0),
                                new Token(Token.Type.IDENTIFIER, "elem", 6),
                                new Token(Token.Type.IDENTIFIER, "IN", 9),
                                new Token(Token.Type.IDENTIFIER, "list", 12),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        new Ast.Stmt.For(
                                "elem",
                                new Ast.Expr.Access(Optional.empty(), "list"),
                                List.of()
                        )
                ),
                Arguments.of("Invalid Identifier in For",
                        Arrays.asList(
                                //FOR elem IN list DO  END
                                new Token(Token.Type.IDENTIFIER, "FOR", 0),
                                new Token(Token.Type.INTEGER, "7", 6),
                                new Token(Token.Type.IDENTIFIER, "IN", 9),
                                new Token(Token.Type.IDENTIFIER, "list", 12),
                                new Token(Token.Type.IDENTIFIER, "DO", 17),
                                new Token(Token.Type.IDENTIFIER, "END", 26)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testWhileStatement(String test, List<Token> tokens, Ast.Stmt.While expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testWhileStatement() {
        return Stream.of(
                Arguments.of("Missing END in While",
                        Arrays.asList(
                                //WHILE expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18)
                        ),
                        null
                ),
                Arguments.of("While",
                        Arrays.asList(
                                //WHILE expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new Ast.Stmt.While(
                                new Ast.Expr.Access(Optional.empty(), "expr"),
                                List.of(new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt")))
                        )
                ),
                Arguments.of("Multiple Statements in While",
                        Arrays.asList(
                                //WHILE expr DO stmt; END
                                new Token(Token.Type.IDENTIFIER, "WHILE", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 6),
                                new Token(Token.Type.IDENTIFIER, "DO", 11),
                                new Token(Token.Type.IDENTIFIER, "stmt1", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "stmt2", 14),
                                new Token(Token.Type.OPERATOR, ";", 18),
                                new Token(Token.Type.IDENTIFIER, "END", 20)
                        ),
                        new Ast.Stmt.While(
                                new Ast.Expr.Access(Optional.empty(), "expr"),
                                Arrays.asList(new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt1")),
                                        new Ast.Stmt.Expression(new Ast.Expr.Access(Optional.empty(), "stmt2")))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testReturnStatement(String test, List<Token> tokens, Ast.Stmt.Return expected) {
        test(tokens, expected, ParserInterpreter::parseStatement);
    }

    private static Stream<Arguments> testReturnStatement() {
        return Stream.of(
                Arguments.of("Return Statement",
                        Arrays.asList(
                                //RETURN expr;
                                new Token(Token.Type.IDENTIFIER, "RETURN", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 7),
                                new Token(Token.Type.OPERATOR, ";", 11)
                        ),
                        new Ast.Stmt.Return(new Ast.Expr.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Missing Value in Return Statement",
                        Arrays.asList(
                                //RETURN;
                                new Token(Token.Type.IDENTIFIER, "RETURN", 0),
                                new Token(Token.Type.OPERATOR, ";", 11)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLiteralExpression(String test, List<Token> tokens, Ast.Expr.Literal expected) {
        test(tokens, expected, ParserInterpreter::parseExpression);
    }

    private static Stream<Arguments> testLiteralExpression() {
        return Stream.of(
                Arguments.of("Boolean Literal",
                        List.of(new Token(Token.Type.IDENTIFIER, "TRUE", 0)),
                        new Ast.Expr.Literal(Boolean.TRUE)
                ),
                Arguments.of("Integer Literal",
                        List.of(new Token(Token.Type.INTEGER, "1", 0)),
                        new Ast.Expr.Literal(new BigInteger("1"))
                ),
                Arguments.of("Decimal Literal",
                        List.of(new Token(Token.Type.DECIMAL, "2.0", 0)),
                        new Ast.Expr.Literal(new BigDecimal("2.0"))
                ),
                Arguments.of("Character Literal",
                        List.of(new Token(Token.Type.CHARACTER, "'c'", 0)),
                        new Ast.Expr.Literal('c')
                ),
                Arguments.of("String Literal",
                        List.of(new Token(Token.Type.STRING, "\"string\"", 0)),
                        new Ast.Expr.Literal("string")
                ),
                Arguments.of("Escape Character",
                        List.of(new Token(Token.Type.STRING, "\"Hello,\\nWorld!\"", 0)),
                        new Ast.Expr.Literal("Hello,\nWorld!")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGroupExpression(String test, List<Token> tokens, Ast.Expr.Group expected) {
        test(tokens, expected, ParserInterpreter::parseExpression);
    }

    private static Stream<Arguments> testGroupExpression() {
        return Stream.of(
                Arguments.of("Missing Paren Grouped Variable",
                        Arrays.asList(
                                //(expr
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1)
                        ),
                        null
                ),
                Arguments.of("Grouped Variable",
                        Arrays.asList(
                                //(expr)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr", 1),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expr.Group(new Ast.Expr.Access(Optional.empty(), "expr"))
                ),
                Arguments.of("Grouped Binary",
                        Arrays.asList(
                                //(expr1 + expr2)
                                new Token(Token.Type.OPERATOR, "(", 0),
                                new Token(Token.Type.IDENTIFIER, "expr1", 1),
                                new Token(Token.Type.OPERATOR, "+", 7),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9),
                                new Token(Token.Type.OPERATOR, ")", 14)
                        ),
                        new Ast.Expr.Group(new Ast.Expr.Binary("+",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        ))
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testBinaryExpression(String test, List<Token> tokens, Ast.Expr.Binary expected) {
        test(tokens, expected, ParserInterpreter::parseExpression);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("Binary Equals/Not Equals",
                        Arrays.asList(
                                //expr1 == expr2 != expr3
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10),
                                new Token(Token.Type.OPERATOR, "!=", 10),
                                new Token(Token.Type.IDENTIFIER, "expr3", 10)
                        ),
                        new Ast.Expr.Binary("!=",
                                new Ast.Expr.Binary("==",
                                        new Ast.Expr.Access(Optional.empty(), "expr1"),
                                        new Ast.Expr.Access(Optional.empty(), "expr2")),
                                new Ast.Expr.Access(Optional.empty(), "expr3")
                        )
                ),
                Arguments.of("Binary Priority",
                        Arrays.asList(
                                //x < 10 OR y AND z
                                new Token(Token.Type.IDENTIFIER, "x", 0),
                                new Token(Token.Type.OPERATOR, "<", 6),
                                new Token(Token.Type.INTEGER, "10", 10),
                                new Token(Token.Type.IDENTIFIER, "OR", 10),
                                new Token(Token.Type.IDENTIFIER, "y", 10),
                                new Token(Token.Type.IDENTIFIER, "AND", 10),
                                new Token(Token.Type.IDENTIFIER, "z", 10)
                        ),
                        new Ast.Expr.Binary("AND",
                                new Ast.Expr.Binary("OR",
                                        new Ast.Expr.Binary("<",
                                                new Ast.Expr.Access(Optional.empty(), "x"),
                                                new Ast.Expr.Literal(new BigInteger("10"))),
                                        new Ast.Expr.Access(Optional.empty(), "y")),
                                new Ast.Expr.Access(Optional.empty(), "z")
                        )
                ),
                Arguments.of("Binary And",
                        Arrays.asList(
                                //expr1 AND expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.IDENTIFIER, "AND", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 10)
                        ),
                        new Ast.Expr.Binary("AND",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Equality",
                        Arrays.asList(
                                //expr1 == expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "==", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 9)
                        ),
                        new Ast.Expr.Binary("==",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Addition",
                        Arrays.asList(
                                //expr1 + expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "+", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("+",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Binary Multiplication",
                        Arrays.asList(
                                //expr1 * expr2
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6),
                                new Token(Token.Type.IDENTIFIER, "expr2", 8)
                        ),
                        new Ast.Expr.Binary("*",
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2")
                        )
                ),
                Arguments.of("Missing Operand in Binary Multiplication",
                        Arrays.asList(
                                //expr1 *
                                new Token(Token.Type.IDENTIFIER, "expr1", 0),
                                new Token(Token.Type.OPERATOR, "*", 6)
                        ),
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAccessExpression(String test, List<Token> tokens, Ast.Expr.Access expected) {
        test(tokens, expected, ParserInterpreter::parseExpression);
    }

    private static Stream<Arguments> testAccessExpression() {
        return Stream.of(
                Arguments.of("Invalid Name in Field Access",
                        Arrays.asList(
                                //obj.5
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.INTEGER, "5", 4)
                        ),
                        null
                ),
                Arguments.of("Variable",
                        List.of(new Token(Token.Type.IDENTIFIER, "name", 0)),
                        new Ast.Expr.Access(Optional.empty(), "name")
                ),
                Arguments.of("Field Access",
                        Arrays.asList(
                                //obj.field
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "field", 4)
                        ),
                        new Ast.Expr.Access(Optional.of(new Ast.Expr.Access(Optional.empty(), "obj")), "field")
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFunctionExpression(String test, List<Token> tokens, Ast.Expr.Function expected) {
        test(tokens, expected, ParserInterpreter::parseExpression);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Zero Arguments",
                        Arrays.asList(
                                //name()
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.OPERATOR, ")", 5)
                        ),
                        new Ast.Expr.Function(Optional.empty(), "name", List.of())
                ),
                Arguments.of("Multiple Arguments",
                        Arrays.asList(
                                //name(expr1, expr2, expr3)
                                new Token(Token.Type.IDENTIFIER, "name", 0),
                                new Token(Token.Type.OPERATOR, "(", 4),
                                new Token(Token.Type.IDENTIFIER, "expr1", 5),
                                new Token(Token.Type.OPERATOR, ",", 10),
                                new Token(Token.Type.IDENTIFIER, "expr2", 12),
                                new Token(Token.Type.OPERATOR, ",", 17),
                                new Token(Token.Type.IDENTIFIER, "expr3", 19),
                                new Token(Token.Type.OPERATOR, ")", 24)
                        ),
                        new Ast.Expr.Function(Optional.empty(), "name", Arrays.asList(
                                new Ast.Expr.Access(Optional.empty(), "expr1"),
                                new Ast.Expr.Access(Optional.empty(), "expr2"),
                                new Ast.Expr.Access(Optional.empty(), "expr3")
                        ))
                ),
                Arguments.of("Method Call",
                        Arrays.asList(
                                //obj.method()
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.OPERATOR, ")", 11)
                        ),
                        new Ast.Expr.Function(Optional.of(new Ast.Expr.Access(Optional.empty(), "obj")), "method", List.of())
                ),
                Arguments.of("Trailing Comma in Method Call",
                        Arrays.asList(
                                //obj.method(expr, )
                                new Token(Token.Type.IDENTIFIER, "obj", 0),
                                new Token(Token.Type.OPERATOR, ".", 3),
                                new Token(Token.Type.IDENTIFIER, "method", 4),
                                new Token(Token.Type.OPERATOR, "(", 10),
                                new Token(Token.Type.IDENTIFIER, "expr", 4),
                                new Token(Token.Type.OPERATOR, ",", 4),
                                new Token(Token.Type.OPERATOR, ")", 11)
                        ),
                        null
                )
        );
    }

    @Test
    void testExample1() {

        /* LET first = 1;
         * DEF main() DO
         *     WHILE first != 16 DO
         *         print(first);
         *         first = first * 2;
         *     END
         * END
         */

        List<Token> input = Arrays.asList(

                //LET first = 1;
                new Token(Token.Type.IDENTIFIER, "LET", 0),
                new Token(Token.Type.IDENTIFIER, "first", 4),
                new Token(Token.Type.OPERATOR, "=", 10),
                new Token(Token.Type.INTEGER, "1", 12),
                new Token(Token.Type.OPERATOR, ";", 13),
                //DEF main() DO
                new Token(Token.Type.IDENTIFIER, "DEF", 15),
                new Token(Token.Type.IDENTIFIER, "main", 19),
                new Token(Token.Type.OPERATOR, "(", 23),
                new Token(Token.Type.OPERATOR, ")", 24),
                new Token(Token.Type.IDENTIFIER, "DO", 26),
                //    WHILE first != 16 DO
                new Token(Token.Type.IDENTIFIER, "WHILE", 33),
                new Token(Token.Type.IDENTIFIER, "first", 39),
                new Token(Token.Type.OPERATOR, "!=", 45),
                new Token(Token.Type.INTEGER, "16", 48),
                new Token(Token.Type.IDENTIFIER, "DO", 51),
                //        print(first);
                new Token(Token.Type.IDENTIFIER, "print", 62),
                new Token(Token.Type.OPERATOR, "(", 67),
                new Token(Token.Type.IDENTIFIER, "first", 68),
                new Token(Token.Type.OPERATOR, ")", 73),
                new Token(Token.Type.OPERATOR, ";", 74),
                //        first = first * 2;
                new Token(Token.Type.IDENTIFIER, "first", 84),
                new Token(Token.Type.OPERATOR, "=", 90),
                new Token(Token.Type.IDENTIFIER, "first", 92),
                new Token(Token.Type.OPERATOR, "*", 98),
                new Token(Token.Type.INTEGER, "2", 100),
                new Token(Token.Type.OPERATOR, ";", 101),
                //    END
                new Token(Token.Type.IDENTIFIER, "END", 107),
                //END
                new Token(Token.Type.IDENTIFIER, "END", 111)
        );
        Ast.Source expected = new Ast.Source(
                List.of(new Ast.Field("first", Optional.of(new Ast.Expr.Literal(BigInteger.ONE)))),
                List.of(new Ast.Method("main", List.of(), List.of(
                                new Ast.Stmt.While(
                                        new Ast.Expr.Binary("!=",
                                                new Ast.Expr.Access(Optional.empty(), "first"),
                                                new Ast.Expr.Literal(BigInteger.valueOf(16))
                                        ),
                                        Arrays.asList(
                                                new Ast.Stmt.Expression(
                                                        new Ast.Expr.Function(Optional.empty(), "print", List.of(
                                                                new Ast.Expr.Access(Optional.empty(), "first"))
                                                        )
                                                ),
                                                new Ast.Stmt.Assignment(
                                                        new Ast.Expr.Access(Optional.empty(), "first"),
                                                        new Ast.Expr.Binary("*",
                                                                new Ast.Expr.Access(Optional.empty(), "first"),
                                                                new Ast.Expr.Literal(BigInteger.valueOf(2))
                                                        )
                                                )
                                        )
                                )
                        ), null)
                ), null);
        test(input, expected, ParserInterpreter::parseSource);
    }

    @Test
    void testExample2() {

        // LET i = -1;
        // LET inc = 2;
        // DEF foo() DO
        //      WHILE i <= 1 DO
        //          IF i > 0 DO
        //              print(\"bar\");
        //         END
        //         i = i + inc;
        //      END
        // END

        List<Token> input = Arrays.asList(

                //LET i = -1;
                new Token(Token.Type.IDENTIFIER, "LET", 0),
                new Token(Token.Type.IDENTIFIER, "i", 4),
                new Token(Token.Type.OPERATOR, "=", 6),
                new Token(Token.Type.INTEGER, "-1", 8),
                new Token(Token.Type.OPERATOR, ";", 10),

                //LET inc = 2;
                new Token(Token.Type.IDENTIFIER, "LET", 12),
                new Token(Token.Type.IDENTIFIER, "inc", 16),
                new Token(Token.Type.OPERATOR, "=", 20),
                new Token(Token.Type.INTEGER, "2", 22),
                new Token(Token.Type.OPERATOR, ";", 23),

                //DEF foo() DO
                new Token(Token.Type.IDENTIFIER, "DEF", 25),
                new Token(Token.Type.IDENTIFIER, "foo", 29),
                new Token(Token.Type.OPERATOR, "(", 32),
                new Token(Token.Type.OPERATOR, ")", 33),
                new Token(Token.Type.IDENTIFIER, "DO", 35),

                //    WHILE i <= 1 DO
                new Token(Token.Type.IDENTIFIER, "WHILE", 42),
                new Token(Token.Type.IDENTIFIER, "i", 48),
                new Token(Token.Type.OPERATOR, "<=", 50),
                new Token(Token.Type.INTEGER, "1", 53),
                new Token(Token.Type.IDENTIFIER, "DO", 55),

                //        IF i > 0 DO
                new Token(Token.Type.IDENTIFIER, "IF", 66),
                new Token(Token.Type.IDENTIFIER, "i", 69),
                new Token(Token.Type.OPERATOR, ">", 71),
                new Token(Token.Type.INTEGER, "0", 73),
                new Token(Token.Type.IDENTIFIER, "DO", 75),

                //            print(\"bar\");
                new Token(Token.Type.IDENTIFIER, "print", 90),
                new Token(Token.Type.OPERATOR, "(", 95),
                new Token(Token.Type.STRING, "\"bar\"", 96),
                new Token(Token.Type.OPERATOR, ")", 101),
                new Token(Token.Type.OPERATOR, ";", 102),

                //        END
                new Token(Token.Type.IDENTIFIER, "END", 112),

                //        i = i + inc;
                new Token(Token.Type.IDENTIFIER, "i", 124),
                new Token(Token.Type.OPERATOR, "=", 126),
                new Token(Token.Type.IDENTIFIER, "i", 128),
                new Token(Token.Type.OPERATOR, "+", 130),
                new Token(Token.Type.IDENTIFIER, "inc", 132),
                new Token(Token.Type.OPERATOR, ";", 135),

                //    END
                new Token(Token.Type.IDENTIFIER, "END", 141),

                //END
                new Token(Token.Type.IDENTIFIER, "END", 145)
        );

        Ast.Source expected = new Ast.Source(
                Arrays.asList(
                        new Ast.Field("i", Optional.of(new Ast.Expr.Literal(BigInteger.valueOf(-1)))),
                        new Ast.Field("inc", Optional.of(new Ast.Expr.Literal(BigInteger.valueOf(2))))
                ),
                List.of(
                        new Ast.Method(
                                "foo",
                                List.of(),
                                List.of(
                                        new Ast.Stmt.While(
                                                new Ast.Expr.Binary(
                                                        "<=",
                                                        new Ast.Expr.Access(Optional.empty(), "i"),
                                                        new Ast.Expr.Literal(BigInteger.ONE)
                                                ),
                                                Arrays.asList(
                                                        new Ast.Stmt.If(
                                                                new Ast.Expr.Binary(
                                                                        ">",
                                                                        new Ast.Expr.Access(Optional.empty(), "i"),
                                                                        new Ast.Expr.Literal(BigInteger.ZERO)
                                                                ),
                                                                List.of(
                                                                        new Ast.Stmt.Expression(
                                                                                new Ast.Expr.Function(
                                                                                        Optional.empty(),
                                                                                        "print",
                                                                                        List.of(
                                                                                                new Ast.Expr.Literal("bar")
                                                                                        )
                                                                                )
                                                                        )
                                                                ),
                                                                List.of()
                                                        ),
                                                        new Ast.Stmt.Assignment(
                                                                new Ast.Expr.Access(Optional.empty(), "i"),
                                                                new Ast.Expr.Binary(
                                                                        "+",
                                                                        new Ast.Expr.Access(Optional.empty(), "i"),
                                                                        new Ast.Expr.Access(Optional.empty(), "inc")
                                                                )
                                                        )
                                                )
                                        )
                                ),
                                null
                        )
                ),
                null
        );
    }

    /**
     * Standard test function. If expected is null, a ParseException is expected
     * to be thrown (not used in the provided tests).
     */
    private static <T extends Ast> void test(List<Token> tokens, T expected, Function<ParserInterpreter, T> function) {
        ParserInterpreter parserInterpreter = new ParserInterpreter(tokens);
        if (expected != null) {
            Assertions.assertEquals(expected, function.apply(parserInterpreter));
        } else {
            Assertions.assertThrows(ParseException.class, () -> function.apply(parserInterpreter));
        }
    }

}
