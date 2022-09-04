package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Method method;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {
        for (Ast.Field field : ast.getFields()) {
            visit(field);
        }

        Ast.Method main = null;
        for (Ast.Method method : ast.getMethods()) {
            visit(method);
            if (method.getName().equals("main") && method.getParameters().size() == 0) {
                main = method;
            }
        }
        if (main == null || !main.getReturnTypeName().isPresent() || !main.getReturnTypeName().get().equals("Integer")) {
            throw new RuntimeException("main() is not defined.");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        if (ast.getValue().isPresent()) {
            visit(ast.getValue().get());
            requireAssignable(Environment.getType(ast.getName()), ast.getValue().get().getType());
        }

        ast.setVariable(new Environment.Variable(ast.getName(), ast.getName(), Environment.getType(ast.getName()), Environment.NIL));
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        List<Environment.Type> paramTypes = new ArrayList<>();
        for (String param : ast.getParameters()) {
            paramTypes.add(scope.lookupVariable(param).getType());
        }

        Environment.Type retType = null;
        if (!ast.getReturnTypeName().isPresent()) {
            retType = Environment.Type.NIL;
        } else if (ast.getReturnTypeName().get().equals("Boolean")) {
            retType = Environment.Type.BOOLEAN;
        } else if (ast.getReturnTypeName().get().equals("Integer")) {
            retType = Environment.Type.INTEGER;
        } else if (ast.getReturnTypeName().get().equals("String")) {
            retType = Environment.Type.STRING;
        } else if (ast.getReturnTypeName().get().equals("Character")) {
            retType = Environment.Type.CHARACTER;
        } else if (ast.getReturnTypeName().get().equals("Decimal")) {
            retType = Environment.Type.DECIMAL;
        } // TODO: include Comparable ?

        scope.defineFunction(ast.getName(), ast.getName(), paramTypes, retType, plcObjects -> Environment.NIL);

        Environment.Type retTypeProvided = Environment.Type.ANY;

        try {
            for (Ast.Stmt statement : ast.getStatements()) {
                scope = new Scope(scope);
                visit(statement);
                if (statement instanceof Ast.Stmt.Return) {
                    ((Ast.Stmt.Return) statement).getValue().getType().getScope().defineVariable("!ret", "!ret", ast.getFunction().getReturnType(), Environment.NIL);
                }
                visit(statement);
            }
        } finally {
            scope = scope.getParent();
        }

        ast.setFunction(scope.lookupFunction(ast.getName(), ast.getParameters().size()));
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        if (!(ast.getExpression() instanceof Ast.Expr.Function)) {
            throw new RuntimeException("Expression statement is not a Function.");
        }

        visit(ast.getExpression());
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        if (!ast.getTypeName().isPresent() && !ast.getValue().isPresent()) {
            throw new RuntimeException("Declaration specifies neither type nor value.");
        }

        Environment.Type type = null;
        if (ast.getTypeName().isPresent()) {
            type = Environment.getType(ast.getTypeName().get());
        }
        if (ast.getValue().isPresent()) {
            visit(ast.getValue().get());

            if (type == null) {
                type = ast.getValue().get().getType();
            }

            requireAssignable(type, ast.getValue().get().getType());
        }

        ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), type, Environment.NIL));
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        if (!(ast.getReceiver() instanceof Ast.Expr.Access)) {
            throw new RuntimeException("Receiver is not an Access expression.");
        }

        visit(ast.getReceiver());
        visit(ast.getValue());
        requireAssignable(ast.getReceiver().getType(), ast.getValue().getType());
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        if (ast.getThenStatements().isEmpty()) {
            throw new RuntimeException("If statement does not include any Then statements.");
        }

        visit(ast.getCondition());
        requireType(Environment.Type.BOOLEAN, ast.getCondition().getType());

        try {
            scope = new Scope(scope);
            for (Ast.Stmt statement : ast.getThenStatements()) {
                visit(statement);
            }
        } finally {
            scope = scope.getParent();
        }

        try {
            scope = new Scope(scope);
            for (Ast.Stmt statement : ast.getElseStatements()) {
                visit(statement);
            }
        } finally {
            scope = scope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        requireType(Environment.Type.INTEGER_ITERABLE, ast.getValue().getType());
        if (ast.getStatements().isEmpty()) {
            throw new RuntimeException("Then statements are empty.");
        }

        try {
            scope = new Scope(scope);
            scope.defineVariable(ast.getName(), ast.getName(), Environment.Type.INTEGER, Environment.NIL);
            for (Ast.Stmt statement : ast.getStatements()) {
                visit(statement);
            }
        } finally {
            scope = scope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        visit(ast.getCondition());

        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());
        try {
            scope = new Scope(scope);
            for (Ast.Stmt statement : ast.getStatements()) {
                visit(statement);
            }
        } finally {
            scope = scope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        Environment.Type providedType = ast.getValue().getType().getScope().lookupVariable("!ret").getType();
        visit(ast.getValue());
        requireType(ast.getValue().getType(), providedType);
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        Object literal = ast.getLiteral();
        Environment.Type type = null;

        if (literal == null) {
            type = Environment.Type.NIL;

        } else if (literal instanceof BigInteger) {
            if (((BigInteger) literal).compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0 || ((BigInteger) literal).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                throw new RuntimeException("The value is out of the Integer range.");
            }

            type = Environment.Type.INTEGER;

        } else if (literal instanceof BigDecimal) {
            double temp = ((BigDecimal) literal).doubleValue();
            if (temp == Double.NEGATIVE_INFINITY || temp == Double.POSITIVE_INFINITY) {
                throw new RuntimeException("The value is out of the Decimal range.");
            }

            type = Environment.Type.DECIMAL;

        } else if (literal instanceof String) {
            type = Environment.Type.STRING;

        } else if (literal instanceof Character) {
            type = Environment.Type.CHARACTER;

        } else if (literal instanceof Boolean) {
            type = Environment.Type.BOOLEAN;

        }

        ast.setType(type);
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        if (!(ast.getExpression() instanceof Ast.Expr.Binary)) {
            throw new RuntimeException("Contained expression is not Binary.");
        }

        visit(ast.getExpression());
        ast.setType(ast.getExpression().getType());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        String op = ast.getOperator();
        Ast.Expr lhs = ast.getLeft();
        Ast.Expr rhs = ast.getRight();
        visit(lhs);
        visit(rhs);

        if (op.equals("AND") || op.equals("OR")) {
            requireType(Environment.Type.BOOLEAN, lhs.getType());
            requireType(Environment.Type.BOOLEAN, rhs.getType());

            ast.setType(Environment.Type.BOOLEAN);

        } else if (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=") || op.equals("==") || op.equals("!=")) {
            requireType(Environment.Type.COMPARABLE, lhs.getType());
            requireType(Environment.Type.COMPARABLE, rhs.getType());
            requireType(rhs.getType(), lhs.getType());

            ast.setType(Environment.Type.BOOLEAN);

        } else if (op.equals("+")) {
            if (lhs.getType().getName().equals("String") || rhs.getType().getName().equals("String")) {
                ast.setType(Environment.Type.STRING);

            } else if (lhs.getType().getName().equals("Integer") || lhs.getType().getName().equals("Decimal")) {
                requireType(lhs.getType(), rhs.getType());

                ast.setType(lhs.getType());

            } else {
                throw new RuntimeException(lhs.getType() + " does not support operator `+`.");

            }
        } else if (op.equals("-") || op.equals("*") || op.equals("/")) {
            if (lhs.getType().getName().equals("Integer") || lhs.getType().getName().equals("Decimal")) {
                requireType(lhs.getType(), rhs.getType());

                ast.setType(lhs.getType());

            } else {
                throw new RuntimeException(lhs.getType() + " does not support operator `-`.");

            }
        } else {
            throw new RuntimeException(lhs.getType() + " does not support operator " + op + ".");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            Ast.Expr.Access receiver = (Ast.Expr.Access) ast.getReceiver().get();
            visit(receiver);
            ast.setVariable(receiver.getVariable().getType().getScope().lookupVariable(ast.getName()));
        } else {
            ast.setVariable(scope.lookupVariable(ast.getName()));
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        if (ast.getReceiver().isPresent()) {
            Ast.Expr.Access receiver = (Ast.Expr.Access) ast.getReceiver().get();
            visit(receiver);

            Environment.Function func = receiver.getType().getMethod(ast.getName(), ast.getArguments().size());

            for (int i = 1; i < func.getParameterTypes().size(); ++i) {
                visit(ast.getArguments().get(i));
                requireType(func.getParameterTypes().get(i), ast.getArguments().get(i - 1).getType());
            }

            ast.setFunction(receiver.getType().getScope().lookupFunction(ast.getName(), ast.getArguments().size() + 1));
        } else {
            Environment.Function func = scope.lookupFunction(ast.getName(), ast.getArguments().size());

            for (int i = 0; i < func.getParameterTypes().size(); ++i) {
                visit(ast.getArguments().get(i));
                requireType(func.getParameterTypes().get(i), ast.getArguments().get(i).getType());
            }

            ast.setFunction(scope.lookupFunction(ast.getName(), ast.getArguments().size()));
        }

        return null;
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        String _target = target.getName();
        String _type = type.getName();
        if (_target.equals("Any")) {
            return;
        }
        if (_target.equals("Comparable")) {
            if (_type.equals("Integer") || _type.equals("Decimal") || _type.equals("Character") || _type.equals("String")) {
                return;
            }
        }
        if (_target.equals(_type)) {
            return;
        }
        throw new RuntimeException("Specified type does not match the target type.");
    }

    private static void requireType(Environment.Type required, Environment.Type given) {
        if (required.getName().equals("Any")) {
            return;
        }
        if (required.getName().equals("Comparable")) {
            if (given.getName().equals("Integer") || given.getName().equals("Decimal") || given.getName().equals("Character") || given.getName().equals("String")) {
                return;
            } else {
                throw new RuntimeException(given.getName() + " is not Comparable.");
            }
        }
        if (!required.getName().equals(given.getName())) {
            throw new RuntimeException("Expected type: " + required.getName() + ", received: " + given.getName() + ".");
        }
    }

}
