package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

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
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Field ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Method ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        throw new UnsupportedOperationException();  // TODO
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
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        throw new UnsupportedOperationException();  // TODO
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
        throw new UnsupportedOperationException();  // TODO
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
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException();  // TODO
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
