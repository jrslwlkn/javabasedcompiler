package plc.project;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GeneratorTests {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testSource(String test, Ast.Source ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of(
                        "Multiple Fields & Methods",

                        new Ast.Source(
                                Arrays.asList(
                                        init(new Ast.Stmt.Field("x", "Integer", Optional.empty()), ast -> ast.setVariable(new Environment.Variable("x", "x", Environment.Type.INTEGER, Environment.NIL))),
                                        init(new Ast.Stmt.Field("y", "Integer", Optional.of(
                                                init(new Ast.Expr.Literal(new BigInteger("10")), ast -> ast.setType(Environment.Type.INTEGER))
                                        )), ast -> ast.setVariable(new Environment.Variable("y", "y", Environment.Type.INTEGER, Environment.NIL)))
                                ),

                                Arrays.asList(
                                        init(new Ast.Method("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                                new Ast.Stmt.Expression(init(new Ast.Expr.Function(Optional.empty(), "print", Arrays.asList(
                                                        init(new Ast.Expr.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                                )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                                new Ast.Stmt.Return(init(new Ast.Expr.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                        )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))),

                                        init(new Ast.Method(
                                                        "area",

                                                        //parameters
                                                        Arrays.asList(
                                                                "radius"
                                                        ),

                                                        // parameter type names
                                                        Arrays.asList(
                                                                "Decimal"
                                                        ),

                                                        // return type name
                                                        Optional.of("Decimal"),

                                                        // method statements
                                                        Arrays.asList(
                                                                new Ast.Stmt.Return(
                                                                        new Ast.Expr.Binary(
                                                                                "*",
                                                                                new Ast.Expr.Binary(
                                                                                        "*",
                                                                                        init(new Ast.Expr.Literal(new BigDecimal("3.14")), ast -> ast.setType(Environment.Type.DECIMAL)),
                                                                                        init(new Ast.Expr.Access(Optional.empty(), "radius"), ast -> ast.setVariable(new Environment.Variable("radius", "radius", Environment.Type.DECIMAL, Environment.NIL)))
                                                                                ),
                                                                                init(new Ast.Expr.Access(Optional.empty(), "radius"), ast -> ast.setVariable(new Environment.Variable("radius", "radius", Environment.Type.DECIMAL, Environment.NIL)))
                                                                        )
                                                                )
                                                        )
                                                ),
                                                ast -> ast.setFunction(new Environment.Function("area", "area", Arrays.asList(Environment.Type.DECIMAL), Environment.Type.DECIMAL, args -> Environment.NIL)))
                                )
                        ),

                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    int x;",
                                "    int y = 10;",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "    double area(double radius) {",
                                "        return 3.14 * radius * radius;",
                                "    }",
                                "",
                                "}"
                        )
                ),

                Arguments.of("Hello, World!",
                        // DEF main(): Integer DO
                        //     print("Hello, World!");
                        //     RETURN 0;
                        // END
                        new Ast.Source(
                                Arrays.asList(),
                                Arrays.asList(init(new Ast.Method("main", Arrays.asList(), Arrays.asList(), Optional.of("Integer"), Arrays.asList(
                                        new Ast.Stmt.Expression(init(new Ast.Expr.Function(Optional.empty(), "print", Arrays.asList(
                                                init(new Ast.Expr.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL)))),
                                        new Ast.Stmt.Return(init(new Ast.Expr.Literal(BigInteger.ZERO), ast -> ast.setType(Environment.Type.INTEGER)))
                                )), ast -> ast.setFunction(new Environment.Function("main", "main", Arrays.asList(), Environment.Type.INTEGER, args -> Environment.NIL))))
                        ),
                        String.join(System.lineSeparator(),
                                "public class Main {",
                                "",
                                "    public static void main(String[] args) {",
                                "        System.exit(new Main().main());",
                                "    }",
                                "",
                                "    int main() {",
                                "        System.out.println(\"Hello, World!\");",
                                "        return 0;",
                                "    }",
                                "",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testMethodStatement(String test, Ast.Stmt.Method ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testMethodStatement() {
        return Stream.of(
                Arguments.of(
                        "Method",

                        init(new Ast.Method(
                                        "area",

                                        //parameters
                                        Arrays.asList(
                                                "radius"
                                        ),

                                        // parameter type names
                                        Arrays.asList(
                                                "Decimal"
                                        ),

                                        // return type name
                                        Optional.of("Decimal"),

                                        // method statements
                                        Arrays.asList(
                                                new Ast.Stmt.Return(
                                                        new Ast.Expr.Binary(
                                                                "*",
                                                                new Ast.Expr.Binary(
                                                                        "*",
                                                                        init(new Ast.Expr.Literal(new BigDecimal("3.14")), ast -> ast.setType(Environment.Type.DECIMAL)),
                                                                        init(new Ast.Expr.Access(Optional.empty(), "radius"), ast -> ast.setVariable(new Environment.Variable("radius", "radius", Environment.Type.DECIMAL, Environment.NIL)))
                                                                ),
                                                                init(new Ast.Expr.Access(Optional.empty(), "radius"), ast -> ast.setVariable(new Environment.Variable("radius", "radius", Environment.Type.DECIMAL, Environment.NIL)))
                                                        )
                                                )
                                        )
                                ),
                                ast -> ast.setFunction(new Environment.Function("area", "area", Arrays.asList(Environment.Type.DECIMAL), Environment.Type.DECIMAL, args -> Environment.NIL))),

                        String.join(System.lineSeparator(),
                                "double area(double radius) {",
                                "    return 3.14 * radius * radius;",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testDeclarationStatement(String test, Ast.Stmt.Declaration ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testDeclarationStatement() {
        return Stream.of(
                Arguments.of("Declaration",
                        // LET name: Integer;
                        init(new Ast.Stmt.Declaration("name", Optional.of("Integer"), Optional.empty()), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.INTEGER, Environment.NIL))),
                        "int name;"
                ),
                Arguments.of("Initialization",
                        // LET name = 1.0;
                        init(new Ast.Stmt.Declaration("name", Optional.empty(), Optional.of(
                                init(new Ast.Expr.Literal(new BigDecimal("1.0")), ast -> ast.setType(Environment.Type.DECIMAL))
                        )), ast -> ast.setVariable(new Environment.Variable("name", "name", Environment.Type.DECIMAL, Environment.NIL))),
                        "double name = 1.0;"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testWhileStatement(String test, Ast.Stmt.While ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testWhileStatement() {
        return Stream.of(
                Arguments.of(
                        "Empty While",
                        new Ast.Stmt.While(
                                // condition
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),

                                // while statements
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "while (expr) {}"
                        )
                ),

                Arguments.of(
                        "While",
                        new Ast.Stmt.While(
                                // condition
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),

                                // while statements
                                Arrays.asList(
                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, Environment.NIL))))
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "while (expr) {",
                                "    stmt;",
                                "}"
                        )
                ),

                Arguments.of(
                        "While with If",
                        new Ast.Stmt.While(
                                // condition
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),

                                // while statements
                                Arrays.asList(
                                        new Ast.Stmt.If(
                                                init(new Ast.Expr.Access(Optional.empty(), "expr1"), ast -> ast.setVariable(new Environment.Variable("expr1", "expr1", Environment.Type.BOOLEAN, Environment.NIL))),
                                                Arrays.asList(new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, Environment.NIL))))),
                                                Arrays.asList()
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "while (expr) {",
                                "    if (expr1) {",
                                "        stmt;",
                                "    }",
                                "}"
                        )
                ),

                Arguments.of(
                        "While with For",
                        new Ast.Stmt.While(
                                // condition
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),

                                // while statements
                                Arrays.asList(
                                        new Ast.Stmt.For(
                                                "expr1",

                                                init(new Ast.Expr.Access(Optional.empty(), "expr1"), ast -> ast.setVariable(new Environment.Variable("expr1", "expr1", Environment.Type.BOOLEAN, Environment.NIL))),

                                                Arrays.asList(new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, Environment.NIL)))))
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "while (expr) {",
                                "    for (int expr1 : expr1) {",
                                "        stmt;",
                                "    }",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testIfStatement(String test, Ast.Stmt.If ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testIfStatement() {
        return Stream.of(
                Arguments.of("Empty If",
                        // IF expr DO
                        //     stmt;
                        // END
                        new Ast.Stmt.If(
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),
                                Arrays.asList(),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {}"
                        )
                ),
                Arguments.of("If",
                        // IF expr DO
                        //     stmt;
                        // END
                        new Ast.Stmt.If(
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),
                                Arrays.asList(new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, Environment.NIL))))),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    stmt;",
                                "}"
                        )
                ),
                Arguments.of("Else",
                        // IF expr DO
                        //     stmt1;
                        // ELSE
                        //     stmt2;
                        // END
                        new Ast.Stmt.If(
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),
                                Arrays.asList(new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt1"), ast -> ast.setVariable(new Environment.Variable("stmt1", "stmt1", Environment.Type.NIL, Environment.NIL))))),
                                Arrays.asList(new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt2"), ast -> ast.setVariable(new Environment.Variable("stmt2", "stmt2", Environment.Type.NIL, Environment.NIL)))))
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    stmt1;",
                                "} else {",
                                "    stmt2;",
                                "}"
                        )
                ),
                Arguments.of("If with While",
                        new Ast.Stmt.If(
                                init(new Ast.Expr.Access(Optional.empty(), "expr"), ast -> ast.setVariable(new Environment.Variable("expr", "expr", Environment.Type.BOOLEAN, Environment.NIL))),
                                Arrays.asList(
                                        new Ast.Stmt.While(
                                                // condition
                                                init(new Ast.Expr.Access(Optional.empty(), "expr1"), ast -> ast.setVariable(new Environment.Variable("expr1", "expr1", Environment.Type.BOOLEAN, Environment.NIL))),

                                                // while statements
                                                Arrays.asList(new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt"), ast -> ast.setVariable(new Environment.Variable("stmt", "stmt", Environment.Type.NIL, Environment.NIL)))))
                                        )
                                ),
                                Arrays.asList()
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr) {",
                                "    while (expr1) {",
                                "        stmt;",
                                "    }",
                                "}"
                        )
                ),
                Arguments.of("If and Else with For",
                        // IF expr DO
                        //     stmt1;
                        // ELSE
                        //     stmt2;
                        // END
                        new Ast.Stmt.If(
                                // condition
                                init(new Ast.Expr.Access(Optional.empty(), "expr1"), ast -> ast.setVariable(new Environment.Variable("expr1", "expr1", Environment.Type.BOOLEAN, Environment.NIL))),

                                // then block
                                Arrays.asList(
                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt1"), ast -> ast.setVariable(new Environment.Variable("stmt1", "stmt1", Environment.Type.NIL, Environment.NIL)))),

                                        // first for loop
                                        new Ast.Stmt.For(
                                                "expr2",

                                                init(new Ast.Expr.Access(Optional.empty(), "expr2"), ast -> ast.setVariable(new Environment.Variable("expr2", "expr2", Environment.Type.BOOLEAN, Environment.NIL))),

                                                // for loop statements
                                                Arrays.asList(
                                                        // if statement
                                                        new Ast.Stmt.If(
                                                                init(new Ast.Expr.Access(Optional.empty(), "expr3"), ast -> ast.setVariable(new Environment.Variable("expr3", "expr3", Environment.Type.BOOLEAN, Environment.NIL))),

                                                                // then statements
                                                                Arrays.asList(
                                                                        // for loop
                                                                        new Ast.Stmt.For(
                                                                                "expr4",
                                                                                init(new Ast.Expr.Access(Optional.empty(), "expr4"), ast -> ast.setVariable(new Environment.Variable("expr4", "expr4", Environment.Type.BOOLEAN, Environment.NIL))),
                                                                                Arrays.asList(
                                                                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt2"), ast -> ast.setVariable(new Environment.Variable("stmt2", "stmt2", Environment.Type.NIL, Environment.NIL))))
                                                                                )
                                                                        )
                                                                ),

                                                                // else statements
                                                                Arrays.asList()
                                                        )
                                                        , new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt11"), ast -> ast.setVariable(new Environment.Variable("stmt11", "stmt11", Environment.Type.NIL, Environment.NIL))))
                                                )
                                        )
                                ),

                                // else block statements
                                Arrays.asList(
                                        new Ast.Stmt.Expression(
                                                init(new Ast.Expr.Access(Optional.empty(), "stmt3"), ast -> ast.setVariable(new Environment.Variable("stmt3", "stmt3", Environment.Type.NIL, Environment.NIL)))
                                        ),

                                        new Ast.Stmt.For(
                                                "expr5",

                                                init(new Ast.Expr.Access(Optional.empty(), "expr5"), ast -> ast.setVariable(new Environment.Variable("expr5", "expr5", Environment.Type.BOOLEAN, Environment.NIL))),

                                                // for loop statements
                                                Arrays.asList(
                                                        // if statement
                                                        new Ast.Stmt.If(
                                                                init(new Ast.Expr.Access(Optional.empty(), "expr6"), ast -> ast.setVariable(new Environment.Variable("expr6", "expr6", Environment.Type.BOOLEAN, Environment.NIL))),

                                                                // then statements
                                                                Arrays.asList(
                                                                        // for loop
                                                                        new Ast.Stmt.For(
                                                                                "expr7",
                                                                                init(new Ast.Expr.Access(Optional.empty(), "expr7"), ast -> ast.setVariable(new Environment.Variable("expr7", "expr7", Environment.Type.BOOLEAN, Environment.NIL))),
                                                                                Arrays.asList(
                                                                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt4"), ast -> ast.setVariable(new Environment.Variable("stmt4", "stmt4", Environment.Type.NIL, Environment.NIL))))
                                                                                )
                                                                        ),
                                                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt5"), ast -> ast.setVariable(new Environment.Variable("stmt5", "stmt5", Environment.Type.NIL, Environment.NIL))))
                                                                ),

                                                                // else statements
                                                                Arrays.asList(
                                                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt13"), ast -> ast.setVariable(new Environment.Variable("stmt13", "stmt13", Environment.Type.NIL, Environment.NIL))))
                                                                )
                                                        ),

                                                        new Ast.Stmt.Expression(init(new Ast.Expr.Access(Optional.empty(), "stmt12"), ast -> ast.setVariable(new Environment.Variable("stmt12", "stmt12", Environment.Type.NIL, Environment.NIL))))
                                                )
                                        )
                                )
                        ),
                        String.join(System.lineSeparator(),
                                "if (expr1) {",
                                "    stmt1;",
                                "    for (int expr2 : expr2) {",
                                "        if (expr3) {",
                                "            for (int expr4 : expr4) {",
                                "                stmt2;",
                                "            }",
                                "        }",
                                "        stmt11;",
                                "    }",
                                "} else {",
                                "    stmt3;",
                                "    for (int expr5 : expr5) {",
                                "        if (expr6) {",
                                "            for (int expr7 : expr7) {",
                                "                stmt4;",
                                "            }",
                                "            stmt5;",
                                "        } else {",
                                "            stmt13;",
                                "        }",
                                "        stmt12;",
                                "    }",
                                "}"
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testBinaryExpression(String test, Ast.Expr.Binary ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testBinaryExpression() {
        return Stream.of(
                Arguments.of("And",
                        // TRUE AND FALSE
                        init(new Ast.Expr.Binary("AND",
                                init(new Ast.Expr.Literal(true), ast -> ast.setType(Environment.Type.BOOLEAN)),
                                init(new Ast.Expr.Literal(false), ast -> ast.setType(Environment.Type.BOOLEAN))
                        ), ast -> ast.setType(Environment.Type.BOOLEAN)),
                        "true && false"
                ),
                Arguments.of("Concatenation",
                        // "Ben" + 10
                        init(new Ast.Expr.Binary("+",
                                init(new Ast.Expr.Literal("Ben"), ast -> ast.setType(Environment.Type.STRING)),
                                init(new Ast.Expr.Literal(BigInteger.TEN), ast -> ast.setType(Environment.Type.INTEGER))
                        ), ast -> ast.setType(Environment.Type.STRING)),
                        "\"Ben\" + 10"
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testFunctionExpression(String test, Ast.Expr.Function ast, String expected) {
        test(ast, expected);
    }

    private static Stream<Arguments> testFunctionExpression() {
        return Stream.of(
                Arguments.of("Print",
                        // print("Hello, World!")
                        init(new Ast.Expr.Function(Optional.empty(), "print", Arrays.asList(
                                init(new Ast.Expr.Literal("Hello, World!"), ast -> ast.setType(Environment.Type.STRING))
                        )), ast -> ast.setFunction(new Environment.Function("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL))),
                        "System.out.println(\"Hello, World!\")"
                ),
                Arguments.of("String Slice",
                        // "string".slice(1, 5)
                        init(new Ast.Expr.Function(Optional.of(
                                init(new Ast.Expr.Literal("string"), ast -> ast.setType(Environment.Type.STRING))
                        ), "slice", Arrays.asList(
                                init(new Ast.Expr.Literal(BigInteger.ONE), ast -> ast.setType(Environment.Type.INTEGER)),
                                init(new Ast.Expr.Literal(BigInteger.valueOf(5)), ast -> ast.setType(Environment.Type.INTEGER))
                        )), ast -> ast.setFunction(new Environment.Function("slice", "substring", Arrays.asList(Environment.Type.ANY, Environment.Type.INTEGER, Environment.Type.INTEGER), Environment.Type.NIL, args -> Environment.NIL))),
                        "\"string\".substring(1, 5)"
                )
        );
    }

    /**
     * Helper function for tests, using a StringWriter as the output stream.
     */
    private static void test(Ast ast, String expected) {
        StringWriter writer = new StringWriter();
        new Generator(new PrintWriter(writer)).visit(ast);
        Assertions.assertEquals(expected, writer.toString());
    }

    /**
     * Runs a callback on the given value, used for inline initialization.
     */
    private static <T> T init(T value, Consumer<T> initializer) {
        initializer.accept(value);
        return value;
    }

}
