package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope _scope;

    public Interpreter(Scope parent) {
        _scope = new Scope(parent);
        _scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return _scope;
    }

    @Override
    public Environment.PlcObject visit(Ast ast) {
        return Ast.Visitor.super.visit(ast);
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {

        for (Ast.Field field : ast.getFields()) {
            if (field.getValue().isPresent()) {
                _scope.defineVariable(field.getName(), visit(field.getValue().get()));
            } else {
                _scope.defineVariable(field.getName(), Environment.NIL);
            }
        }

        for (Ast.Method method : ast.getMethods()) {
            visit(method);
        }

        return _scope.lookupFunction("main", 0).invoke(new ArrayList<>());
    }

    @Override
    public Environment.PlcObject visit(Ast.Struct ast) {
        return null;
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        if (ast.getValue().isPresent()) {
            _scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        } else {
            _scope.defineVariable(ast.getName(), Environment.NIL);
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        Function<List<Environment.PlcObject>, Environment.PlcObject> func = (args) -> {
            Scope prev = _scope;
            Scope top = _scope;
            while (top.getParent().getParent() != null) {
                top = top.getParent();
            }
            _scope = new Scope(top);
            Environment.PlcObject ret = Environment.NIL;

            List<String> params = ast.getParameters();
            for (int i = 0; i < params.size(); i++) {
                _scope.defineVariable(params.get(i), args.get(i));
            }

            try {
                for (Ast.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
            } catch (Return e) {
                ret = e._value;
            }

            _scope = prev;
            return ret;
        };

        _scope.defineFunction(ast.getName(), ast.getParameters().size(), func);
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        visit(ast.getExpression());

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        if (ast.getValue().isPresent()) {
            _scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        } else {
            _scope.defineVariable(ast.getName(), Environment.NIL);
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        if (!ast.getReceiver().getClass().equals(Ast.Expr.Access.class)) {
            throw new RuntimeException("Operand is not of an assignable type.");
        }

        Environment.PlcObject value = visit(ast.getValue());
        Ast.Expr.Access lhs = (Ast.Expr.Access) ast.getReceiver();
        if (lhs.getReceiver().isPresent()) {
            visit(lhs.getReceiver().get()).setField(lhs.getName(), value);
        } else {
            _scope.lookupVariable(lhs.getName()).setValue(value);
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        try {
            _scope = new Scope(_scope);

            if (requireType(Boolean.class, visit(ast.getCondition()))) {
                for (Ast.Stmt stmt : ast.getThenStatements()) {
                    visit(stmt);
                }
            } else {
                for (Ast.Stmt stmt : ast.getElseStatements()) {
                    visit(stmt);
                }
            }
        } finally {
            _scope = _scope.getParent();
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        Iterable<Environment.PlcObject> list = requireType(Iterable.class, visit(ast.getValue()));
        for (Environment.PlcObject o : list) {
            try {
                _scope = new Scope(_scope);
                _scope.defineVariable(ast.getName(), o);
                for (Ast.Stmt statement : ast.getStatements()) {
                    visit(statement);
                }
            } finally {
                _scope = _scope.getParent();
            }
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        while (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                _scope = new Scope(_scope);
                for (Ast.Stmt stmt : ast.getStatements()) {
                    visit(stmt);
                }
            } finally {
                _scope = _scope.getParent();
            }
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
        throw new Return(visit(ast.getValue()));
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast) {
        if (ast.getLiteral() == null) {
            return Environment.NIL;
        }
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast) { // fixme: break in separate methods
        Environment.PlcObject leftResult;
        Environment.PlcObject rightResult;
        String operator = ast.getOperator();

        switch (operator) {
            case "AND" -> {
                leftResult = visit(ast.getLeft());
                if (!requireType(Boolean.class, leftResult)) {
                    return Environment.create(false);
                } else {
                    rightResult = visit(ast.getRight());
                    if (!requireType(Boolean.class, rightResult)) {
                        return Environment.create(false);
                    }

                    return Environment.create(true);
                }
            }
            case "OR" -> {
                leftResult = visit(ast.getLeft());
                if (requireType(Boolean.class, leftResult)) {
                    return Environment.create(true);
                } else {
                    rightResult = visit(ast.getRight());
                    if (requireType(Boolean.class, rightResult)) {
                        return Environment.create(true);
                    }

                    return Environment.create(false);
                }
            }
            case "<", "<=", ">", ">=" -> {
                leftResult = visit(ast.getLeft());
                rightResult = visit(ast.getRight());

                Comparable<Object> lhs = (Comparable<Object>) leftResult.getValue();
                Comparable<Object> rhs = (Comparable<Object>) requireType(lhs.getClass(), rightResult);

                int result = lhs.compareTo(rhs);
                if (result < 0 && operator.equals("<")) {
                    return Environment.create(true);
                } else if (result <= 0 && operator.equals("<=")) {
                    return Environment.create(true);
                } else if (result > 0 && operator.equals(">")) {
                    return Environment.create(true);
                } else if (result >= 0 && operator.equals(">=")) {
                    return Environment.create(true);
                } else {
                    return Environment.create(false);
                }
            }
            case "==", "!=" -> {
                leftResult = visit(ast.getLeft());
                rightResult = visit(ast.getRight());
                boolean result = Objects.equals(leftResult.getValue(), rightResult.getValue());
                if (operator.equals("!=")) {
                    result = !result;
                }

                return Environment.create(result);
            }
            case "+" -> {
                leftResult = visit(ast.getLeft());
                rightResult = visit(ast.getRight());
                Object result;

                if (leftResult.getValue().getClass().equals(String.class) || rightResult.getValue().getClass().equals(String.class)) {
                    result = leftResult.getValue() + rightResult.getValue().toString();
                } else if (leftResult.getValue().getClass().equals(BigInteger.class) && rightResult.getValue().getClass().equals(BigInteger.class)) {
                    result = ((BigInteger) leftResult.getValue()).add((BigInteger) rightResult.getValue());
                } else if (leftResult.getValue().getClass().equals(BigDecimal.class) && rightResult.getValue().getClass().equals(BigDecimal.class)) {
                    result = ((BigDecimal) leftResult.getValue()).add((BigDecimal) rightResult.getValue());
                } else {
                    requireType(leftResult.getValue().getClass(), rightResult);
                    throw new RuntimeException("Incompatible types for arithmetic operator");
                }

                return Environment.create(result);
            }
            case "-", "*" -> {
                leftResult = visit(ast.getLeft());
                rightResult = visit(ast.getRight());
                Object result;

                if (leftResult.getValue().getClass().equals(BigInteger.class) && rightResult.getValue().getClass().equals(BigInteger.class)) {
                    if (operator.equals("-")) {
                        result = ((BigInteger) leftResult.getValue()).subtract((BigInteger) rightResult.getValue());
                    } else {
                        result = ((BigInteger) leftResult.getValue()).multiply((BigInteger) rightResult.getValue());
                    }
                } else if (leftResult.getValue().getClass().equals(BigDecimal.class) && rightResult.getValue().getClass().equals(BigDecimal.class)) {
                    if (operator.equals("-")) {
                        result = ((BigDecimal) leftResult.getValue()).subtract((BigDecimal) rightResult.getValue());
                    } else {
                        result = ((BigDecimal) leftResult.getValue()).multiply((BigDecimal) rightResult.getValue());
                    }
                } else {
                    requireType(leftResult.getValue().getClass(), rightResult);
                    throw new RuntimeException("Incompatible types for arithmetic operator");
                }

                return Environment.create(result);
            }
            default -> { // divide
                leftResult = visit(ast.getLeft());
                rightResult = visit(ast.getRight());
                Object result;

                if (leftResult.getValue().getClass().equals(BigInteger.class) && rightResult.getValue().getClass().equals(BigInteger.class)) {
                    if (rightResult.getValue().equals(BigInteger.ZERO)) {
                        throw new RuntimeException("Division by zero.");
                    }
                    result = ((BigInteger) leftResult.getValue()).divide((BigInteger) rightResult.getValue());
                } else if (leftResult.getValue().getClass().equals(BigDecimal.class) && rightResult.getValue().getClass().equals(BigDecimal.class)) {
                    if (rightResult.getValue().equals(BigDecimal.ZERO)) {
                        throw new RuntimeException("Division by zero.");
                    }
                    result = ((BigDecimal) leftResult.getValue()).divide((BigDecimal) rightResult.getValue(), RoundingMode.HALF_EVEN);
                } else {
                    requireType(leftResult.getValue().getClass(), rightResult);
                    throw new RuntimeException("Incompatible types for arithmetic operator");
                }

                return Environment.create(result);
            }
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            Environment.PlcObject receiver = visit(ast.getReceiver().get());
            return receiver.getField(ast.getName()).getValue();
        } else {
            return _scope.lookupVariable(ast.getName()).getValue();
        }
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        List<Environment.PlcObject> arguments = new ArrayList<>();
        for (Ast.Expr a : ast.getArguments()) {
            arguments.add(visit(a));
        }

        if (ast.getReceiver().isPresent()) {
            Environment.PlcObject receiver = visit(ast.getReceiver().get());
            return receiver.callMethod(ast.getName(), arguments);
        } else {
            return _scope.lookupFunction(ast.getName(), ast.getArguments().size()).invoke(arguments);
        }
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject _value;

        private Return(Environment.PlcObject value) {
            _value = value;
        }

    }

}
