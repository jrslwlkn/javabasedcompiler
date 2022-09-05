package plc.project;

public final class ParseException extends RuntimeException {

    private final int _index;

    public ParseException(String message, int index) {
        super(message);
        _index = index;
    }

    public int getIndex() {
        return _index;
    }

}
