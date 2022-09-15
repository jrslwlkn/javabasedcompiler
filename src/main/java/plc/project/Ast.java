package plc.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Ast {

    public interface Visitor<T> {

        default T visit(Ast ast) {
            if (ast instanceof Struct) {
                return visit((Struct) ast);
            } else if (ast instanceof Source) {
                return visit((Source) ast);
            } else if (ast instanceof Field) {
                return visit((Field) ast);
            } else if (ast instanceof Method) {
                return visit((Method) ast);
            } else if (ast instanceof Stmt.Expression) {
                return visit((Stmt.Expression) ast);
            } else if (ast instanceof Stmt.Declaration) {
                return visit((Stmt.Declaration) ast);
            } else if (ast instanceof Stmt.Assignment) {
                return visit((Stmt.Assignment) ast);
            } else if (ast instanceof Stmt.If) {
                return visit((Stmt.If) ast);
            } else if (ast instanceof Stmt.For) {
                return visit((Stmt.For) ast);
            } else if (ast instanceof Stmt.While) {
                return visit((Stmt.While) ast);
            } else if (ast instanceof Stmt.Return) {
                return visit((Stmt.Return) ast);
            } else if (ast instanceof Expr.Literal) {
                return visit((Expr.Literal) ast);
            } else if (ast instanceof Expr.Group) {
                return visit((Expr.Group) ast);
            } else if (ast instanceof Expr.Binary) {
                return visit((Expr.Binary) ast);
            } else if (ast instanceof Expr.Access) {
                return visit((Expr.Access) ast);
            } else if (ast instanceof Expr.Function) {
                return visit((Expr.Function) ast);
            } else {
                throw new AssertionError("Unimplemented AST type: " + ast.getClass().getName() + ".");
            }
        }

        T visit(Source ast);

        T visit(Struct ast);

        T visit(Field ast);

        T visit(Method ast);

        T visit(Stmt.Expression ast);

        T visit(Stmt.Declaration ast);

        T visit(Stmt.Assignment ast);

        T visit(Stmt.If ast);

        T visit(Stmt.For ast);

        T visit(Stmt.While ast);

        T visit(Stmt.Return ast);

        T visit(Expr.Literal ast);

        T visit(Expr.Group ast);

        T visit(Expr.Binary ast);

        T visit(Expr.Access ast);

        T visit(Expr.Function ast);

    }

    public static class Source extends Ast {

        private final List<Field> _fields;
        private final List<Method> _methods;
        private final List<Struct> _structs;

        public Source(List<Field> fields, List<Method> methods, List<Struct> structs) {
            _fields = fields;
            _methods = methods;
            _structs = structs;
        }

        public List<Field> getFields() {
            return _fields;
        }

        public List<Method> getMethods() {
            return _methods;
        }

        public List<Struct> getStructs() {
            return _structs;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Source &&
                    _fields.equals(((Source) obj)._fields) &&
                    _methods.equals(((Source) obj)._methods);
        }

        @Override
        public String toString() {
            return "Ast.Source{" +
                    "fields=" + _fields +
                    "functions=" + _methods +
                    '}';
        }

    }

    public static final class Field extends Ast {

        private final String _name;
        private final String _typeName;
        private final Optional<Expr> _value;
        private Environment.Variable _variable = null;

        public Field(String name, Optional<Expr> value) {
            this(name, "Any", value);
        }

        public Field(String name, String typeName, Optional<Expr> value) {
            _name = name;
            _typeName = typeName;
            _value = value;
        }

        public String getName() {
            return _name;
        }

        public String getTypeName() {
            return _typeName;
        }

        public Optional<Expr> getValue() {
            return _value;
        }

        public Environment.Variable getVariable() {
            if (_variable == null) {
                throw new IllegalStateException("variable is uninitialized");
            }
            return _variable;
        }

        public void setVariable(Environment.Variable variable) {
            this._variable = variable;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Field &&
                    _name.equals(((Field) obj)._name) &&
                    _typeName.equals(((Field) obj)._typeName) &&
                    _value.equals(((Field) obj)._value) &&
                    Objects.equals(_variable, ((Field) obj)._variable);
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + _name + '\'' +
                    ", typeName=" + _typeName +
                    ", value=" + _value +
                    ", variable=" + _variable +
                    '}';
        }

    }

    public static class Struct extends Source {

        private final String _name;

        public Struct(String name, List<Field> fields, List<Method> methods) {
            super(fields, methods, null);
            _name = name;
        }

        public String getName() {
            return _name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Struct && equals(obj);
        }

        @Override
        public String toString() {
            return "Ast.Struct{" +
                    "fields=" + getFields() +
                    "functions=" + getMethods() +
                    '}';
        }
    }

    public static final class Method extends Ast {

        private final String _name;
        private final List<String> _parameters;
        private final List<String> _parameterTypeNames;
        private final Optional<String> _returnTypeName;
        private final List<Stmt> _statements;
        private Environment.Function _function = null;
        private final List<Ast.Struct> _structs;

        public Method(String name, List<String> parameters, List<Stmt> statements, List<Ast.Struct> structs) {
            this(name, parameters, new ArrayList<>(), Optional.of("Any"), statements, structs);
            for (int i = 0; i < parameters.size(); i++) {
                _parameterTypeNames.add("Any");
            }
        }

        public Method(String name, List<String> parameters, List<String> parameterTypeNames, Optional<String> returnTypeName, List<Stmt> statements, List<Ast.Struct> structs) {
            _name = name;
            _parameters = parameters;
            _parameterTypeNames = parameterTypeNames;
            _returnTypeName = returnTypeName;
            _statements = statements;
            _structs = structs;
        }

        public String getName() {
            return _name;
        }

        public List<String> getParameters() {
            return _parameters;
        }

        public List<String> getParameterTypeNames() {
            return _parameterTypeNames;
        }

        public Optional<String> getReturnTypeName() {
            return _returnTypeName;
        }

        public List<Stmt> getStatements() {
            return _statements;
        }

        public List<Ast.Struct> getStructs() {
            return _structs;
        }

        public Environment.Function getFunction() {
            if (_function == null) {
                throw new IllegalStateException("function is uninitialized");
            }
            return _function;
        }

        public void setFunction(Environment.Function function) {
            _function = function;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Method &&
                    _name.equals(((Method) obj)._name) &&
                    _parameters.equals(((Method) obj)._parameters) &&
                    _parameterTypeNames.equals(((Method) obj)._parameterTypeNames) &&
                    _returnTypeName.equals(((Method) obj)._returnTypeName) &&
                    _statements.equals(((Method) obj)._statements) &&
                    Objects.equals(_function, ((Method) obj)._function);
        }

        @Override
        public String toString() {
            return "Method{" +
                    "name='" + _name + '\'' +
                    ", parameters=" + _parameters +
                    ", parameterTypeNames=" + _parameterTypeNames +
                    ", returnTypeName='" + _returnTypeName + '\'' +
                    ", statements=" + _statements +
                    ", function=" + _function +
                    ", structs=" + _structs +
                    '}';
        }

    }

    public static abstract class Stmt extends Ast {

        public static final class Expression extends Stmt {

            private final Expr _expression;

            public Expression(Expr expression) {
                _expression = expression;
            }

            public Expr getExpression() {
                return _expression;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Expression &&
                        _expression.equals(((Expression) obj)._expression);
            }

            @Override
            public String toString() {
                return "Ast.Stmt.Expression{" +
                        "expression=" + _expression +
                        '}';
            }

        }

        public static final class Declaration extends Stmt {

            private final String _name;
            private final Optional<String> _typeName;
            private final Optional<Expr> _value;
            private Environment.Variable _variable;

            public Declaration(String name, Optional<Expr> value) {
                this(name, Optional.empty(), value);
            }

            public Declaration(String name, Optional<String> typeName, Optional<Expr> value) {
                _name = name;
                _typeName = typeName;
                _value = value;
            }

            public String getName() {
                return _name;
            }

            public Optional<String> getTypeName() {
                return _typeName;
            }

            public Optional<Expr> getValue() {
                return _value;
            }

            public Environment.Variable getVariable() {
                if (_variable == null) {
                    throw new IllegalStateException("variable is uninitialized");
                }
                return _variable;
            }

            public void setVariable(Environment.Variable variable) {
                this._variable = variable;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Declaration &&
                        _name.equals(((Declaration) obj)._name) &&
                        _typeName.equals(((Declaration) obj)._typeName) &&
                        _value.equals(((Declaration) obj)._value) &&
                        Objects.equals(_variable, ((Declaration) obj)._variable);
            }

            @Override
            public String toString() {
                return "Declaration{" +
                        "name='" + _name + '\'' +
                        ", typeName=" + _typeName +
                        ", value=" + _value +
                        ", variable=" + _variable +
                        '}';
            }

        }

        public static final class Assignment extends Stmt {

            private final Expr _receiver;
            private final Expr _value;

            public Assignment(Expr receiver, Expr value) {
                _receiver = receiver;
                _value = value;
            }

            public Expr getReceiver() {
                return _receiver;
            }

            public Expr getValue() {
                return _value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Assignment &&
                        _receiver.equals(((Assignment) obj)._receiver) &&
                        _value.equals(((Assignment) obj)._value);
            }

            @Override
            public String toString() {
                return "Ast.Stmt.Assignment{" +
                        "receiver=" + _receiver +
                        ", value=" + _value +
                        '}';
            }

        }

        public static final class If extends Stmt {

            private final Expr _condition;
            private final List<Stmt> _thenStatements;
            private final List<Stmt> _elseStatements;


            public If(Expr condition, List<Stmt> thenStatements, List<Stmt> elseStatements) {
                this._condition = condition;
                this._thenStatements = thenStatements;
                this._elseStatements = elseStatements;
            }

            public Expr getCondition() {
                return _condition;
            }

            public List<Stmt> getThenStatements() {
                return _thenStatements;
            }

            public List<Stmt> getElseStatements() {
                return _elseStatements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof If &&
                        _condition.equals(((If) obj)._condition) &&
                        _thenStatements.equals(((If) obj)._thenStatements) &&
                        _elseStatements.equals(((If) obj)._elseStatements);
            }

            @Override
            public String toString() {
                return "Ast.Stmt.If{" +
                        "condition=" + _condition +
                        ", thenStatements=" + _thenStatements +
                        ", elseStatements=" + _elseStatements +
                        '}';
            }

        }

        public static final class For extends Stmt {

            private final String _name;
            private final Expr _value;
            private final List<Stmt> _statements;

            public For(String name, Expr value, List<Stmt> statements) {
                _name = name;
                _value = value;
                _statements = statements;
            }

            public String getName() {
                return _name;
            }

            public Expr getValue() {
                return _value;
            }

            public List<Stmt> getStatements() {
                return _statements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof For &&
                        _name.equals(((For) obj)._name) &&
                        _value.equals(((For) obj)._value) &&
                        _statements.equals(((For) obj)._statements);
            }

            @Override
            public String toString() {
                return "For{" +
                        "name='" + _name + '\'' +
                        ", value=" + _value +
                        ", statements=" + _statements +
                        '}';
            }

        }

        public static final class While extends Stmt {

            private final Expr _condition;
            private final List<Stmt> _statements;

            public While(Expr condition, List<Stmt> statements) {
                _condition = condition;
                _statements = statements;
            }

            public Expr getCondition() {
                return _condition;
            }

            public List<Stmt> getStatements() {
                return _statements;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof While &&
                        _condition.equals(((While) obj)._condition) &&
                        _statements.equals(((While) obj)._statements);
            }

            @Override
            public String toString() {
                return "Ast.Stmt.While{" +
                        "condition=" + _condition +
                        ", statements=" + _statements +
                        '}';
            }

        }

        public static final class Return extends Stmt {

            private final Expr _value;

            public Return(Expr value) {
                this._value = value;
            }

            public Expr getValue() {
                return _value;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Return &&
                        _value.equals(((Return) obj)._value);
            }

            @Override
            public String toString() {
                return "Ast.Stmt.Return{" +
                        "value=" + _value +
                        '}';
            }

        }

    }

    public static abstract class Expr extends Ast {

        public abstract Environment.Type getType();

        public static final class Literal extends Expr {

            private final Object _literal;
            private Environment.Type _type = null;

            public Literal(Object literal) {
                _literal = literal;
            }

            public Literal(Object literal, Environment.Type type) {
                this(literal);
//                _type = type; // FIXME: why some tests fail otherwise?
            }

            public Object getLiteral() {
                return _literal;
            }

            @Override
            public Environment.Type getType() {
                if (_type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return _type;
            }

            public void setType(Environment.Type type) {
                _type = type;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Literal &&
                        Objects.equals(_literal, ((Literal) obj)._literal) &&
                        Objects.equals(_type, ((Literal) obj)._type);
            }

            @Override
            public String toString() {
                return "Ast.Expr.Literal{" +
                        "literal=" + _literal +
                        ", type=" + _type +
                        '}';
            }

        }

        public static final class Group extends Expr {

            private final Expr _expression;
            private Environment.Type _type = null;

            public Group(Expr expression) {
                _expression = expression;
            }

            public Expr getExpression() {
                return _expression;
            }

            @Override
            public Environment.Type getType() {
                if (_type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return _type;
            }

            public void setType(Environment.Type type) {
                _type = type;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Group &&
                        _expression.equals(((Group) obj)._expression) &&
                        Objects.equals(_type, ((Group) obj)._type);
            }

            @Override
            public String toString() {
                return "Ast.Expr.Group{" +
                        "expression=" + _expression +
                        ", type=" + _type +
                        '}';
            }

        }

        public static final class Binary extends Expr {

            private final String _operator;
            private final Expr _left;
            private final Expr _right;
            private Environment.Type _type = null;

            public Binary(String operator, Expr left, Expr right) {
                _operator = operator;
                _left = left;
                _right = right;
            }

            public String getOperator() {
                return _operator;
            }

            public Expr getLeft() {
                return _left;
            }

            public Expr getRight() {
                return _right;
            }

            @Override
            public Environment.Type getType() {
                if (_type == null) {
                    throw new IllegalStateException("type is uninitialized");
                }
                return _type;
            }

            public void setType(Environment.Type type) {
                _type = type;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Binary &&
                        _operator.equals(((Binary) obj)._operator) &&
                        _left.equals(((Binary) obj)._left) &&
                        _right.equals(((Binary) obj)._right) &&
                        Objects.equals(_type, ((Binary) obj)._type);
            }

            @Override
            public String toString() {
                return "Ast.Expr.Binary{" +
                        "operator='" + _operator + '\'' +
                        ", left=" + _left +
                        ", right=" + _right +
                        ", type=" + _type +
                        '}';
            }

        }

        public static final class Access extends Expr {

            private final Optional<Expr> _receiver;
            private final String _name;
            private Environment.Variable _variable = null;

            public Access(Optional<Expr> receiver, String name) {
                _receiver = receiver;
                _name = name;
            }

            public Optional<Expr> getReceiver() {
                return _receiver;
            }

            public String getName() {
                return _name;
            }

            public Environment.Variable getVariable() {
                if (_variable == null) {
                    throw new IllegalStateException("variable is uninitialized");
                }
                return _variable;
            }

            public void setVariable(Environment.Variable variable) {
                _variable = variable;
            }

            @Override
            public Environment.Type getType() {
                return getVariable().getType();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Access &&
                        _receiver.equals(((Access) obj)._receiver) &&
                        _name.equals(((Access) obj)._name) &&
                        Objects.equals(_variable, ((Access) obj)._variable);
            }

            @Override
            public String toString() {
                return "Ast.Expr.Access{" +
                        "receiver=" + _receiver +
                        ", name='" + _name + '\'' +
                        ", variable=" + _variable +
                        '}';
            }

        }

        public static final class Function extends Expr {

            private final Optional<Expr> _receiver;
            private final String _name;
            private final List<Expr> _arguments;
            private Environment.Function _function = null;

            public Function(Optional<Expr> receiver, String name, List<Expr> arguments) {
                _receiver = receiver;
                _name = name;
                _arguments = arguments;
            }

            public Optional<Expr> getReceiver() {
                return _receiver;
            }

            public String getName() {
                return _name;
            }

            public List<Expr> getArguments() {
                return _arguments;
            }

            public Environment.Function getFunction() {
                if (_function == null) {
                    throw new IllegalStateException("function is uninitialized");
                }
                return _function;
            }

            public void setFunction(Environment.Function function) {
                _function = function;
            }

            @Override
            public Environment.Type getType() {
                return getFunction().getReturnType();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Function &&
                        _receiver.equals(((Function) obj)._receiver) &&
                        _name.equals(((Function) obj)._name) &&
                        _arguments.equals(((Function) obj)._arguments) &&
                        Objects.equals(_function, ((Function) obj)._function);
            }

            @Override
            public String toString() {
                return "Ast.Expr.Function{" +
                        "receiver=" + _receiver +
                        ", name='" + _name + '\'' +
                        ", arguments=" + _arguments +
                        ", function=" + _function +
                        '}';
            }

        }

    }

}
