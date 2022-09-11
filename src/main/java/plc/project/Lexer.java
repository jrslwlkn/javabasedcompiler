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
 * If the lexer fails to parse something (such as an unterminated string) it
 * throws a {@link ParseException} with an index at the character which is
 * invalid or missing.
 */
public final class Lexer {

    private final CharStream _chars;

    private final String WHITESPACE = "[ \b\n\r\t]";
    private final String BANG = "!";
    private final String IDENTIFIER_START = "[A-Za-z_]";
    private final String IDENTIFIER_END = "[A-Za-z0-9_-]";
    private final String NUMBER_SIGN = "[+-]";
    private final String EQUAL_SIGN = "=";
    private final String DOT = "\\.";
    private final String NUMBER_END = "[0-9]";
    private final String CHAR_QUOTE = "'";
    private final String STRING_QUOTE = "\\\"";
    private final String CHAR_MATCH = "[^'\\\n\\\r\\\\]";
    private final String STRING_MATCH = "[^\\\"\\\n\\\r\\\\]";
    private final String ANY_MATCH = ".";
    private final String OPERATOR_MATCH = "[<>!=]";
    private final String ESCAPE_START = "\\\\";
    private final String ESCAPE_END = "[bnrt'\\\"\\\\]";


    public Lexer(String input) {
        _chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> tokens = new ArrayList<>();

        while (_chars.has(0)) {
            if (peek(WHITESPACE)) {
                lexEscape();
            } else if (peek(BANG, BANG, BANG)) {
                lexComment();
            } else {
                tokens.add(lexToken());
            }
        }

        return tokens;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method.
     */
    public Token lexToken() {
        if (peek(IDENTIFIER_START)) {
            return lexIdentifier();
        } else if (peek(NUMBER_SIGN, NUMBER_END) || peek(NUMBER_END)) {
            return lexNumber();
        } else if (peek(CHAR_QUOTE)) {
            return lexCharacter();
        } else if (peek(STRING_QUOTE)) {
            return lexString();
        } else {
            return lexOperator();
        }
    }

    public Token lexIdentifier() {
        match(IDENTIFIER_START);
        while (true) {
            if (!match(IDENTIFIER_END))
                break;
        }
        return _chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber() {
        match(NUMBER_SIGN);
        while (true) {
            if (!match(NUMBER_END))
                break;
        }

        // checking whether a decimal and proceeding accordingly
        if (match(DOT, NUMBER_END)) {
            while (true) {
                if (!match(NUMBER_END))
                    break;
            }
            return _chars.emit(Token.Type.DECIMAL);
        }

        return _chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter() throws ParseException {
        match(CHAR_QUOTE);
        if (!match(ESCAPE_START, ESCAPE_END, CHAR_QUOTE) && !match(CHAR_MATCH, CHAR_QUOTE)) {
            if (peek(ESCAPE_START)) { // got the start of the escape char but the char is actually not escaped
                throw new ParseException("Character literal contains illegal token after backslash (\\): '"
                        + (_chars.has(1) && !peek(ESCAPE_START, CHAR_QUOTE) ? _chars.get(1) : "")
                        + "'. Possible alternatives: b, n, r, t, ', \", \\.",
                        _chars._index + 1);
            }
            throw new ParseException("Character literal is not terminated. "
                    + "Did you forget the closing single quote (') after '"
                    + (_chars.has(0) ? _chars.get(0) : '\0')
                    + "'?",
                    _chars._index + 1);
        }

        return _chars.emit(Token.Type.CHARACTER);
    }

    public Token lexString() throws ParseException {
        match(STRING_QUOTE);
        while (true) {
            if (!match(STRING_MATCH) && !match(ESCAPE_START, ESCAPE_END))
                break;
        }

        if (peek(ESCAPE_START) && !peek(ESCAPE_START, ESCAPE_END)) {
            throw new ParseException("String literal contains illegal token after backslash (\\): '"
                    + (_chars.has(1) ? _chars.get(1) : _chars.has(0) ? _chars.get(0) : "")
                    + "'. Possible alternatives: b, n, r, t, ', \", \\.",
                    _chars._index + 1);
        }

        if (!match(STRING_QUOTE)) {
            throw new ParseException("String literal is not terminated. "
                    + "Did you forget the closing double quote (\") after '"
                    + (_chars.has(-1) ? _chars.get(-1) : "")
                    + "'?",
                    _chars._index);
        }

        return _chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {
        match(WHITESPACE);
        _chars.skip();
    }

    public void lexComment() {
        while (true) {
            if (match("\n") || !_chars.has(0)) {
                _chars.skip();
                break;
            }
            match(".*");
            _chars.skip();
        }
    }

    public Token lexOperator() {
        if (match(OPERATOR_MATCH, EQUAL_SIGN)) {
            return _chars.emit(Token.Type.OPERATOR);
        }

        match(ANY_MATCH);
        return _chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; ++i) {
            if (!_chars.has(i) || !String.valueOf(_chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true.
     */
    public boolean match(String... patterns) {
        if (!peek(patterns)) {
            return false;
        }

        for (int i = 0; i < patterns.length; ++i) {
            _chars.advance();
        }

        return true;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     */
    public static final class CharStream {

        private final String _input;
        private int _index = 0;
        private int _length = 0;

        public CharStream(String input) {
            _input = input;
        }

        public boolean has(int offset) {
            return _index + offset < _input.length();
        }

        public char get(int offset) {
            return _input.charAt(_index + offset);
        }

        public void advance() {
            _index++;
            _length++;
        }

        public void skip() {
            _length = 0;
        }

        public Token emit(Token.Type type) {
            int start = _index - _length;
            skip();
            return new Token(type, _input.substring(start, _index), start);
        }

    }

}
