package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 * <p>
 * - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 * - {@link #lexToken()}, which lexes the next token
 * - {@link CharStream}, which manages the state of the lexer and literals
 * <p>
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 * <p>
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    private final String whitespace = "[ \b\n\r\t]";
    private final String identifierStart = "[A-Za-z_]";
    private final String identifierNext = "[A-Za-z0-9_-]";
    private final String numberSign = "[+-]";
    private final String dotSign = "\\.";
    private final String numberNext = "[0-9]";
    private final String characterBoundary = "'";
    private final String characterMatch = "[^'\\\n\\\r\\\\]";
    private final String stringBoundary = "\\\"";
    private final String stringMatch = "[^\\\"\\\n\\\r\\\\]";
    private final String operatorStart = "[<>!=]";
    private final String equalSign = "=";
    private final String anyMatch = ".";
    private final String escapeStart = "\\\\";
    private final String escapeEnd = "[bnrt'\\\"\\\\]";


    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();

        while (chars.has(0)) {
            if (peek(whitespace)) {
                lexEscape();
            } else {
                tokens.add(lexToken());
            }
        }

        return tokens;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     * <p>
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        if (peek(identifierStart)) {
            return lexIdentifier();
        } else if (peek(numberSign, numberNext) || peek(numberNext)) {
            return lexNumber();
        } else if (peek(characterBoundary)) {
            return lexCharacter();
        } else if (peek(stringBoundary)) {
            return lexString();
        } else {
            return lexOperator();
        }
    }

    public Token lexIdentifier() {
        match(identifierStart);
        while (true) {
            if (!match(identifierNext))
                break;
        }
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        match(numberSign);
        while (true) {
            if (!match(numberNext))
                break;
        }

        // checking whether a decimal and proceeding accordingly
        if (match(dotSign, numberNext)) {
            while (true) {
                if (!match(numberNext))
                    break;
            }
            return chars.emit(Token.Type.DECIMAL);
        }

        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() throws ParseException {
        match(characterBoundary);
        if (!match(escapeStart, escapeEnd, characterBoundary) && !match(characterMatch, characterBoundary)) {
            if (peek(escapeStart)) { // got the start of the escape char but the char is actually not escaped
                throw new ParseException("Character literal contains illegal token after backslash (\\): '"
                        + (chars.has(1) && !peek(escapeStart, characterBoundary) ? chars.get(1) : "")
                        + "'. Possible alternatives: b, n, r, t, ', \", \\.",
                        chars.index + 1);
            }
            throw new ParseException("Character literal is not terminated. "
                    + "Did you forget the closing single quote (') after '"
                    + (chars.has(0) ? chars.get(0) : '\0')
                    + "'?",
                    chars.index + 1);
        }

        return chars.emit(Token.Type.CHARACTER);
    }

    public Token lexString() throws ParseException {
        match(stringBoundary);
        while (true) {
            if (!match(stringMatch) && !match(escapeStart, escapeEnd))
                break;
        }

        if (peek(escapeStart) && !peek(escapeStart, escapeEnd)) {
            throw new ParseException("String literal contains illegal token after backslash (\\): '"
                    + (chars.has(1) ? chars.get(1) : chars.has(0) ? chars.get(0) : "")
                    + "'. Possible alternatives: b, n, r, t, ', \", \\.",
                    chars.index + 1);
        }

        if (!match(stringBoundary)) {
            throw new ParseException("String literal is not terminated. "
                    + "Did you forget the closing double quote (\") after '"
                    + (chars.has(-1) ? chars.get(-1) : "")
                    + "'?",
                    chars.index);
        }

        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        match(whitespace);
        chars.skip();
    }

    public Token lexOperator() {
        if (match(operatorStart, equalSign)) {
            return chars.emit(Token.Type.OPERATOR);
        }

        match(anyMatch);
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; ++i) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        if (!peek(patterns)) {
            return false;
        }

        for (int i = 0; i < patterns.length; ++i) {
            chars.advance();
        }

        return true;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     * <p>
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
