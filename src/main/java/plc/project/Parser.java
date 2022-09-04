package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 * <p>
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 * <p>
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Field> fields = new ArrayList<>();
        List<Ast.Method> methods = new ArrayList<>();

        while (tokens.has(0)) {
            if (peek("LET")) {
                fields.add(parseField());
            } else if (peek("DEF")) {
                methods.add(parseMethod());
            } else if (peek("")) { // todo: not sure if this is correct
                match("");
            } else {
                throw new ParseException("Unexpected token: "
                        + getPeekedLiteral()
                        + ". Expected: LET or DEF.",
                        tokens.get(0).getIndex());
            }
        }

        return new Ast.Source(fields, methods);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException {
        match("LET"); // LET x = 42;

        String name;
        Ast.Expr value = null;

        try {
            name = getPeekedLiteral();
            if (!match(Token.Type.IDENTIFIER)) {
                throw new ParseException("Unexpected token: "
                        + getPeekedLiteral()
                        + ". Expected: Identifier.",
                        getPeekedIndex());
            }
            if (match("=")) {
                value = parseExpression();
            }
            if (!match(";")) {
                throw new ParseException("Unexpected token: "
                        + getPeekedLiteral()
                        + ". Expected: ;.",
                        getPeekedIndex());
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException("Unexpected token: "
                    + getPeekedLiteral(),
                    getPeekedIndex());
        }

        return new Ast.Field(name, value == null ? Optional.empty() : Optional.ofNullable(value));
    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {
        match("DEF"); // DEF func(a, b, c) DO LET x = 42; END

        String name;
        List<String> parameters;
        List<Ast.Stmt> statements;

        try {
            name = getPeekedLiteral();
            if (!match(Token.Type.IDENTIFIER, "(")) {
                throw new ParseException("Unexpected token: "
                        + getPeekedLiteral()
                        + ". Expected: Identifier followed by (.",
                        getPeekedIndex());
            }
            parameters = getCurrentParameters();
            if (!match(")", "DO")) {
                throw new ParseException("Unexpected token: "
                        + getPeekedLiteral()
                        + ". Expected: ).",
                        getPeekedIndex());
            }
            statements = getCurrentStatements();
            if (!match("END")) {
                throw new ParseException("Unexpected token: "
                        + getPeekedLiteral()
                        + ". Expected: END.",
                        getPeekedIndex());
            }
        } catch (ParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ParseException("Unexpected token: "
                    + getPeekedLiteral(),
                    getPeekedIndex());
        }

        return new Ast.Method(name, parameters, statements);
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        Ast.Expr expression = parseEqualityExpression();

        while (match("AND") || match("OR")) {
            expression = new Ast.Expr.Binary(getMatchedLiteral(), expression, parseEqualityExpression());
        }

        return expression;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        Ast.Expr expression = parseAdditiveExpression();

        while (match("<")
                || match("<=")
                || match(">")
                || match(">=")
                || match("==")
                || match("!=")
        ) {
            expression = new Ast.Expr.Binary(getMatchedLiteral(), expression, parseAdditiveExpression());
        }

        return expression;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        Ast.Expr expression = parseMultiplicativeExpression();

        while (match("+") || match("-")) {
            expression = new Ast.Expr.Binary(getMatchedLiteral(), expression, parseMultiplicativeExpression());
        }

        return expression;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        Ast.Expr expression = parseSecondaryExpression();

        while (match("*") || match("/")) {
            expression = new Ast.Expr.Binary(getMatchedLiteral(), expression, parseSecondaryExpression());
        }

        return expression;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        Ast.Expr currentExpression = parsePrimaryExpression();

        if (match(".")) {
            return parseExpressionAfterDot(currentExpression);
        }

        return currentExpression;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        if (match("NIL")) {
            return new Ast.Expr.Literal(null);

        } else if (match("TRUE")) {
            return new Ast.Expr.Literal(Boolean.TRUE);

        } else if (match("FALSE")) {
            return new Ast.Expr.Literal(Boolean.FALSE);

        } else if (match(Token.Type.INTEGER)) {
            return new Ast.Expr.Literal(new BigInteger(getMatchedLiteral()));

        } else if (match(Token.Type.DECIMAL)) {
            return new Ast.Expr.Literal(new BigDecimal(getMatchedLiteral()));

        } else if (match(Token.Type.CHARACTER)) {
            return new Ast.Expr.Literal(getCleanedCharValue(getMatchedLiteral()));

        } else if (match(Token.Type.STRING)) {
            return new Ast.Expr.Literal(getCleanedStringValue(getMatchedLiteral()));

        } else if (peek("(")) {
            return parseGroup();

        } else if (peek(Token.Type.IDENTIFIER, "(")) {
            match(Token.Type.IDENTIFIER);
            String name = getMatchedLiteral();
            match("(");
            Ast.Expr expression = new Ast.Expr.Function(Optional.empty(), name, getPeekedArguments()); // fixme: not getCurrentArguments!!!

            if (!match(")")) {
                throw new ParseException("Expected: `)`, received: `" + getPeekedLiteral() + "`.", getPeekedIndex());
            }

            return expression;

        } else if (match(Token.Type.IDENTIFIER)) {
            return new Ast.Expr.Access(Optional.empty(), getMatchedLiteral());

        } else {
            throw new ParseException("Unexpected token: "
                    + getPeekedLiteral(),
                    getPeekedIndex());
        }
    }

    private List<Ast.Expr> getPeekedArguments() {
        List<Ast.Expr> arguments = new ArrayList<>();

        while (!peek(")")) {
            arguments.add(parseExpression());

            if (match(",") && peek(")")) {
                throw new ParseException("Expected: Literal or Identifier, received: `" + getPeekedLiteral() + "`.",
                        getPeekedIndex());
            }
        }

        return arguments;
    }

    private Ast.Expr parseExpressionAfterDot(Ast.Expr receiver) {
        // foo.bar().baz.func() // todo: is `foo.bar().baz()` the receiver ?
        // foo.bar.baz.func(a, b) ---> tokens: [`foo`,  `.`, `bar`, `.`, `baz`, `.`, `func(a, b)`]
        // receiver: foo.bar.baz  // todo: not sure if this is right
        // name: func
        // arguments: [a, b]

        Ast.Expr currentExpression;

        if (peek(Token.Type.IDENTIFIER, "(")) {
            // Ast.Expr.Function
            match(Token.Type.IDENTIFIER);
            String name = getMatchedLiteral();
            match("(");
            List<Ast.Expr> arguments = getPeekedArguments();

            if (!match(")")) {
                throw new ParseException("Expected: `)`, received: `" + getPeekedLiteral() + "`.", getPeekedIndex()); // expected )
            }

            currentExpression = new Ast.Expr.Function(Optional.of(receiver), name, arguments);
        } else if (peek(Token.Type.IDENTIFIER)) {
            // Ast.Expr.Access
            currentExpression = new Ast.Expr.Access(Optional.of(receiver), getPeekedLiteral());
        } else {
            // not a Ast.Expr.Function or Ast.Expr.Access
            throw new ParseException("Expected: Identifier, received: `" + getPeekedLiteral() + "`.", getPeekedIndex()); // expected Identifier
        }

        if (match(".")) {
            // recursive expression construction
            return parseExpressionAfterDot(currentExpression);
        }

        return currentExpression;
    }

    private Ast.Expr.Group parseGroup() {
        match("(");

        Ast.Expr expression = parseExpression();

        if (!match(")")) { // fixme: make error messages consistent
            throw new ParseException("Unexpected token: "
                    + getPeekedLiteral()
                    + ". Expected: ).",
                    getPeekedIndex());
        }

        return new Ast.Expr.Group(expression);
    }

    private String getMatchedLiteral() {
        return tokens.get(-1).getLiteral();
    }

    private int getMatchedIndex() {
        return tokens.get(-1).getIndex();
    }

    private String getPeekedLiteral() {
        return tokens.get(0).getLiteral();
    }

    private int getPeekedIndex() {
        return tokens.get(0).getIndex();
    }

    private String getCleanedStringValue(String rawValue) {
        String value = rawValue.substring(1, rawValue.length() - 1);
        return value.replaceAll("\\\\b", "\b")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\t", "\t")
                .replaceAll("\\\\", "\\")
                .replaceAll("\\\\\"", "\"");
    }

    private Character getCleanedCharValue(String rawValue) {
        return getCleanedStringValue(rawValue).charAt(0);
    }

    private List<Ast.Stmt> getCurrentStatements() {
        List<Ast.Stmt> statements = new ArrayList<>();

        while (isStatementAhead()) {
            statements.add(parseStatement());
        }

        return statements;
    }

    private boolean isStatementAhead() {
        return peek("LET")
                || peek("IF")
                || peek("FOR")
                || peek("WHILE")
                || peek("RETURN")
                || isExpressionAhead();
    }

    private boolean isExpressionAhead() {
        return peek("NIL")
                || peek("FALSE")
                || peek("TRUE")
                || peek("(")
                || peek(Token.Type.CHARACTER)
                || peek(Token.Type.DECIMAL)
                || peek(Token.Type.IDENTIFIER)
                || peek(Token.Type.STRING)
                || peek(Token.Type.IDENTIFIER);
    }

    private List<String> getCurrentParameters() {
        List<String> parameters = new ArrayList<>();

        while (peek(Token.Type.IDENTIFIER)) {
            parameters.add(getPeekedLiteral());
            match(Token.Type.IDENTIFIER);

            if (!match(",")) {
                break;
            }
        }

        return parameters;
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     * <p>
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; ++i) {
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            } else {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }

        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        if (!peek(patterns)) {
            return false;
        }

        for (int i = 0; i < patterns.length; ++i) {
            tokens.advance();
        }

        return true;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
