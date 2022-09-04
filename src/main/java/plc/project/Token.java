package plc.project;

public final class Token {

    public enum Type {
        IDENTIFIER,
        INTEGER,
        DECIMAL,
        CHARACTER,
        STRING,
        OPERATOR
    }

    private final Type _type;
    private final String _literal;
    private final int _index;

    public Token(Type type, String literal, int index) {
        _type = type;
        _literal = literal;
        _index = index;
    }

    public Type getType() {
        return _type;
    }

    public String getLiteral() {
        return _literal;
    }

    public int getIndex() {
        return _index;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token
                && _type == ((Token) obj)._type
                && _literal.equals(((Token) obj)._literal)
                && _index == ((Token) obj)._index;
    }

    @Override
    public String toString() {
        return _type + "=" + _literal + "@" + _index;
    }

}
