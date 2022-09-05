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
            throw new RuntimeException("Unknown type " + name + ".");
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

        private final Type type;
        private final Scope scope;
        private final Object value;

        public PlcObject(Scope scope, Object value) {
            this(new Type("Unknown", "Unknown", scope), scope, value);
        }

        public PlcObject(Type type, Scope scope, Object value) {
            this.type = type;
            this.scope = scope;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public Variable getField(String name) {
            return scope.lookupVariable(name);
        }

        public void setField(String name, PlcObject value) {
            scope.lookupVariable(name).setValue(value);
        }

        public PlcObject callMethod(String name, List<PlcObject> arguments) {
            Function function = type.getMethod(name, arguments.size());
            arguments = new ArrayList<>(arguments);
            arguments.add(0, this);
            return function.invoke(arguments);
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Object{" +
                    "type=" + type +
                    ", value=" + value +
                    ", scope=" + scope +
                    '}';
        }

    }

    public static final class Variable {

        private final String name;
        private final String jvmName;
        private final Type type;
        private PlcObject value;

        public Variable(String name, PlcObject value) {
            this(name, name, Type.ANY, value);
        }

        public Variable(String name, String jvmName, Type type, PlcObject value) {
            this.name = name;
            this.jvmName = jvmName;
            this.type = type;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getJvmName() {
            return jvmName;
        }

        public PlcObject getValue() {
            return value;
        }

        public void setValue(PlcObject value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Variable &&
                    name.equals(((Variable) obj).name) &&
                    jvmName.equals(((Variable) obj).jvmName) &&
                    type.equals(((Variable) obj).type);
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "name='" + name + '\'' +
                    ", jvmName'" + jvmName + '\'' +
                    ", type=" + type +
                    ", value=" + value +
                    '}';
        }

    }

    public static final class Function {

        private final String name;
        private final String jvmName;
        private final List<Type> parameterTypes;
        private final Type returnType;
        private final java.util.function.Function<List<PlcObject>, PlcObject> function;

        public Function(String name, int arity, java.util.function.Function<List<PlcObject>, PlcObject> function) {
            this(name, name, new ArrayList<>(), Type.ANY, function);
            for (int i = 0; i < arity; i++) {
                this.parameterTypes.add(Type.ANY);
            }
        }

        public Function(String name, String jvmName, List<Type> parameterTypes, Type returnType, java.util.function.Function<List<PlcObject>, PlcObject> function) {
            this.name = name;
            this.jvmName = jvmName;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
            this.function = function;
        }

        public String getName() {
            return name;
        }

        public String getJvmName() {
            return jvmName;
        }

        public List<Type> getParameterTypes() {
            return parameterTypes;
        }

        public Type getReturnType() {
            return returnType;
        }

        public PlcObject invoke(List<PlcObject> arguments) {
            return function.apply(arguments);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Function &&
                    name.equals(((Function) obj).name) &&
                    jvmName.equals(((Function) obj).jvmName) &&
                    parameterTypes.equals(((Function) obj).parameterTypes) &&
                    returnType.equals(((Function) obj).returnType);
        }

        @Override
        public String toString() {
            return "Function{" +
                    "name='" + name + '\'' +
                    ", jvmName='" + jvmName + '\'' +
                    ", arity=" + parameterTypes.size() +
                    ", parameterTypes=" + parameterTypes +
                    ", returnType=" + returnType +
                    ", function=" + function +
                    '}';
        }

    }

    public static final class Type {

        public static final Type ANY = new Type("Any", "Object", new Scope(null));
        public static final Type NIL = new Type("Nil", "Void", new Scope(ANY._scope));
        public static final Type INTEGER_ITERABLE = new Type("IntegerIterable", "Iterable<Integer>", new Scope(ANY._scope));
        public static final Type COMPARABLE = new Type("Comparable", "Comparable", new Scope(ANY._scope));
        public static final Type BOOLEAN = new Type("Boolean", "boolean", new Scope(ANY._scope));
        public static final Type INTEGER = new Type("Integer", "int", new Scope(COMPARABLE._scope));
        public static final Type DECIMAL = new Type("Decimal", "double", new Scope(COMPARABLE._scope));
        public static final Type CHARACTER = new Type("Character", "char", new Scope(COMPARABLE._scope));
        public static final Type STRING = new Type("String", "String", new Scope(COMPARABLE._scope));

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
            return this._scope;
        }

        public Variable getField(String name) {
            return _scope.lookupVariable(name);
        }

        public Function getMethod(String name, int arity) {
            return _scope.lookupFunction(name, arity + 1);
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
