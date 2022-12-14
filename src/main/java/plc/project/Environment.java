package plc.project;

import java.util.*;

public final class Environment {

    public static final PlcObject NIL = new PlcObject(Type.NIL, new Scope(null), new Object() {

        @Override
        public String toString() {
            return "nil";
        }

    });

    private static final Map<String, Type> _types = new HashMap<>();

    static {
        registerType(Type.ANY);
        registerType(Type.NIL);
        registerType(Type.INTEGER_ITERABLE);
        registerType(Type.COMPARABLE);
        registerType(Type.BOOLEAN);
        registerType(Type.INTEGER);
        registerType(Type.DECIMAL);
        registerType(Type.CHARACTER);
        registerType(Type.STRING);
        Type.ANY._scope.defineFunction("stringify", "toString", List.of(), Type.STRING, args -> Environment.NIL);
        Type.COMPARABLE._scope.defineFunction("compare", "compareTo", Arrays.asList(Type.ANY, Type.COMPARABLE), Type.COMPARABLE, args -> Environment.NIL);
        Type.INTEGER._scope.defineFunction("compare", "compareTo", Arrays.asList(Type.ANY, Type.INTEGER), Type.INTEGER, args -> Environment.NIL);
        Type.DECIMAL._scope.defineFunction("compare", "compareTo", Arrays.asList(Type.ANY, Type.DECIMAL), Type.DECIMAL, args -> Environment.NIL);
        Type.CHARACTER._scope.defineFunction("compare", "compareTo", Arrays.asList(Type.ANY, Type.CHARACTER), Type.CHARACTER, args -> Environment.NIL);
        Type.STRING._scope.defineVariable("length", "length()", Type.INTEGER, Environment.NIL);
        Type.STRING._scope.defineFunction("slice", "substring", Arrays.asList(Type.ANY, Type.INTEGER, Type.INTEGER), Type.STRING, args -> Environment.NIL);
        Type.STRING._scope.defineFunction("compare", "compareTo", Arrays.asList(Type.ANY, Type.STRING), Type.STRING, args -> Environment.NIL);
    }

    public static Type getType(String name) {
        if (!_types.containsKey(name)) {
            return new Type(name, name, null);
        }
        return _types.get(name);
    }

    public static Type getType(String name, Scope scope) {
        if (!_types.containsKey(name)) {
            return new Type(name, name, scope);
        }
        return _types.get(name);
    }

    public static PlcObject create(Object value) {
        return new PlcObject(new Scope(null), value);
    }

    public static void registerType(Type type) {
        if (_types.containsKey(type.getName())) {
            throw new IllegalArgumentException("Duplicate registration of type " + type.getName() + ".");
        }
        _types.put(type.getName(), type);
    }

    public static final class PlcObject {

        private final Type _type;
        private final Scope _scope;
        private final Object _value;

        public PlcObject(Scope scope, Object value) {
            this(new Type("Unknown", "Unknown", scope), scope, value);
        }

        public PlcObject(Type type, Scope scope, Object value) {
            _type = type;
            _scope = scope;
            _value = value;
        }

        public Type getType() {
            return _type;
        }

        public Variable getField(String name) {
            return _scope.lookupVariable(name);
        }

        public void setField(String name, PlcObject value) {
            _scope.lookupVariable(name).setValue(value);
        }

        public PlcObject callMethod(String name, List<PlcObject> arguments) {
            Function function = _type.getMethod(name, arguments.size());
            arguments = new ArrayList<>(arguments);
            arguments.add(0, this);
            return function.invoke(arguments);
        }

        public Object getValue() {
            return _value;
        }

        @Override
        public String toString() {
            return "Object{" +
                    "type=" + _type +
                    ", value=" + _value +
                    ", scope=" + _scope +
                    '}';
        }

    }

    public static final class Variable {

        private final String _name;
        private final String _jvmName;
        private final Type _type;
        private PlcObject _value;

        public Variable(String name, PlcObject value) {
            this(name, name, Type.ANY, value);
        }

        public Variable(String name, String jvmName, Type type, PlcObject value) {
            _name = name;
            _jvmName = jvmName;
            _type = type;
            _value = value;
        }

        public Type getType() {
            return _type;
        }

        public String getName() {
            return _name;
        }

        public String getJvmName() {
            return _jvmName;
        }

        public PlcObject getValue() {
            return _value;
        }

        public void setValue(PlcObject value) {
            _value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Variable &&
                    _name.equals(((Variable) obj)._name) &&
                    _jvmName.equals(((Variable) obj)._jvmName) &&
                    _type.equals(((Variable) obj)._type);
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "name='" + _name + '\'' +
                    ", jvmName'" + _jvmName + '\'' +
                    ", type=" + _type +
                    ", value=" + _value +
                    '}';
        }

    }

    public static final class Function {

        private final String _name;
        private final String _jvmName;
        private final List<Type> _parameterTypes;
        private final Type _returnType;
        private final java.util.function.Function<List<PlcObject>, PlcObject> _function;

        public Function(String name, int arity, java.util.function.Function<List<PlcObject>, PlcObject> function) {
            this(name, name, new ArrayList<>(), Type.ANY, function);
            for (int i = 0; i < arity; i++) {
                _parameterTypes.add(Type.ANY);
            }
        }

        public Function(String name, String jvmName, List<Type> parameterTypes, Type returnType, java.util.function.Function<List<PlcObject>, PlcObject> function) {
            _name = name;
            _jvmName = jvmName;
            _parameterTypes = parameterTypes;
            _returnType = returnType;
            _function = function;
        }

        public String getName() {
            return _name;
        }

        public String getJvmName() {
            return _jvmName;
        }

        public List<Type> getParameterTypes() {
            return _parameterTypes;
        }

        public Type getReturnType() {
            return _returnType;
        }

        public PlcObject invoke(List<PlcObject> arguments) {
            return _function.apply(arguments);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Function &&
                    _name.equals(((Function) obj)._name) &&
                    _jvmName.equals(((Function) obj)._jvmName) &&
                    _parameterTypes.equals(((Function) obj)._parameterTypes) &&
                    _returnType.equals(((Function) obj)._returnType);
        }

        @Override
        public String toString() {
            return "Function{" +
                    "name='" + _name + '\'' +
                    ", jvmName='" + _jvmName + '\'' +
                    ", arity=" + _parameterTypes.size() +
                    ", parameterTypes=" + _parameterTypes +
                    ", returnType=" + _returnType +
                    ", function=" + _function +
                    '}';
        }

    }

    public static final class Type {

        public static final Type ANY = new Type("Any", "Object", new Scope(null));
        public static final Type NIL = new Type("Nil", "void", new Scope(ANY.getScope()));
        public static final Type INTEGER_ITERABLE = new Type("IntegerIterable", "Iterable<Integer>", new Scope(ANY.getScope()));
        public static final Type COMPARABLE = new Type("Comparable", "Comparable", new Scope(ANY.getScope()));
        public static final Type BOOLEAN = new Type("Boolean", "Boolean", new Scope(ANY.getScope()));
        public static final Type INTEGER = new Type("Integer", "Integer", new Scope(COMPARABLE.getScope()));
        public static final Type DECIMAL = new Type("Decimal", "Double", new Scope(COMPARABLE.getScope()));
        public static final Type CHARACTER = new Type("Character", "Character", new Scope(COMPARABLE.getScope()));
        public static final Type STRING = new Type("String", "String", new Scope(COMPARABLE.getScope()));

        private final String _name;
        private final String _jvmName;
        private final Scope _scope;

        public Type(String name, String jvmName, Scope scope) {
            _name = name;
            _jvmName = jvmName;
            _scope = scope;
        }

        public String getName() {
            return _name;
        }

        public String getJvmName() {
            return _jvmName;
        }

        public Scope getScope() {
            return _scope;
        }

        public Variable getField(String name) {
            return _scope.lookupVariable(name);
        }

        public Function getMethod(String name, int arity) {
            return _scope.lookupFunction(name, arity);
        }

        @Override
        public String toString() {
            return "Type{" +
                    "name='" + _name + '\'' +
                    ", jvmName='" + _jvmName + '\'' +
                    ", scope='" + _scope + '\'' +
                    '}';
        }

    }

}
