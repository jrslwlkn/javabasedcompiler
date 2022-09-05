package plc.project;

import java.io.PrintWriter;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter _writer;
    private int _indent = 0;

    public Generator(PrintWriter writer) {
        this._writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                _writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        _writer.println();
        for (int i = 0; i < indent; i++) {
            _writer.write("    ");
        }
    }

    private void indent() {
        for (int i = 0; i < _indent; i++) {
            _writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        _writer.write("public class Main {");
        newline(0);

        for (Ast.Field field : ast.getFields()) {
            newline(1);
            visit(field);
        }

        if (ast.getFields().size() > 0) {
            newline(0);
        }

        newline(1);
        _writer.write("public static void main(String[] args) {");
        newline(2);
        _writer.write("System.exit(new Main().main());");
        newline(1);
        _writer.write("}");

        newline(0);
        _indent = 1;
        for (Ast.Method method : ast.getMethods()) {
            newline(0);
            visit(method);
            newline(0);
        }

        newline(0);
        _writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        _writer.write(Environment.getType(ast.getTypeName()).getJvmName());
        _writer.write(" ");
        _writer.write(ast.getName());

        if (ast.getValue().isPresent()) {
            _writer.write(" = ");
            visit(ast.getValue().get());
        }

        _writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        indent();
        _writer.write(ast.getFunction().getReturnType().getJvmName());
        _writer.write(" ");
        _writer.write(ast.getFunction().getJvmName());
        _writer.write("(");

        for (int i = 0; i < ast.getParameters().size(); ++i) {
            _writer.write(Environment.getType(ast.getParameterTypeNames().get(i)).getJvmName());
            _writer.write(" ");
            _writer.write(ast.getParameters().get(i));
            if (i < ast.getParameters().size() - 1) {
                _writer.write(", ");
            }
        }

        _writer.write(") {");

        _indent++;
        for (Ast.Stmt stmt : ast.getStatements()) {
            newline(0);
            visit(stmt);
        }
        _indent--;

        if (ast.getStatements().size() > 0) {
            newline(_indent);
        }
        _writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        indent();
        visit(ast.getExpression());
        _writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        indent();
        if (ast.getTypeName().isPresent()) {
            _writer.write(Environment.getType(ast.getTypeName().get()).getJvmName());
        } else {
            _writer.write(ast.getVariable().getType().getJvmName());
        }

        _writer.write(" ");
        _writer.write(ast.getName());

        if (ast.getValue().isPresent()) {
            _writer.write(" = ");
            visit(ast.getValue().get());
        }

        _writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        indent();
        visit(ast.getReceiver());
        _writer.write(" = ");
        visit(ast.getValue());
        _writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        indent();
        _writer.write("if (");
        visit(ast.getCondition());
        _writer.write(") {");

        if (ast.getThenStatements().size() > 0) {
            newline(0);
        }

        _indent++;
        for (Ast.Stmt stmt : ast.getThenStatements()) {
            visit(stmt);
            newline(0);
        }
        _indent--;

        if (ast.getThenStatements().size() > 0) {
            indent();
        }

        _writer.write("}");

        if (ast.getElseStatements().size() > 0) {
            _writer.write(" else {");
            newline(0);
            _indent++;
            for (Ast.Stmt stmt : ast.getElseStatements()) {
                visit(stmt);
                newline(0);
            }
            _indent--;
            indent();
            _writer.write("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        indent();
        _writer.write("for (int ");
        _writer.write(ast.getName());
        _writer.write(" : ");
        visit(ast.getValue());
        _writer.write(") {");

        _indent++;
        for (Ast.Stmt stmt : ast.getStatements()) {
            newline(0);
            visit(stmt);
        }
        _indent--;

        if (ast.getStatements().size() > 0) {
            newline(_indent);
        }
        _writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        indent();
        _writer.write("while (");
        visit(ast.getCondition());
        _writer.write(") {");

        _indent++;
        for (Ast.Stmt stmt : ast.getStatements()) {
            newline(0);
            visit(stmt);
        }
        _indent--;

        if (ast.getStatements().size() > 0) {
            newline(_indent);
        }
        _writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        indent();
        _writer.write("return ");
        visit(ast.getValue());
        _writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        Object value = ast.getLiteral();
        if (value instanceof String) {
            _writer.write("\"");
            _writer.write((String) value);
            _writer.write("\"");
        } else if (value instanceof Character) {
            _writer.write("'");
            _writer.write((Character) value);
            _writer.write("'");
        } else if (value == null) {
            _writer.write("null");
        } else {
            _writer.write(value.toString());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        _writer.write("(");
        visit(ast.getExpression());
        _writer.write(")");

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        visit(ast.getLeft());

        _writer.write(" ");
        if (ast.getOperator().equals("AND")) {
            _writer.write("&&");
        } else if (ast.getOperator().equals("OR")) {
            _writer.write("||");
        } else {
            _writer.write(ast.getOperator());
        }
        _writer.write(" ");

        visit(ast.getRight());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            visit(ast.getReceiver().get());
            _writer.write(".");
        }

        _writer.write(ast.getVariable().getJvmName());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        if (ast.getReceiver().isPresent()) {
            visit(ast.getReceiver().get());
            _writer.write(".");
        }

        _writer.write(ast.getFunction().getJvmName());
        _writer.write("(");

        for (int i = 0; i < ast.getArguments().size(); ++i) {
            visit(ast.getArguments().get(i));
            if (i < ast.getArguments().size() - 1) {
                _writer.write(", ");
            }
        }

        _writer.write(")");

        return null;
    }

}
