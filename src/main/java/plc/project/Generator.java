package plc.project;

import java.io.PrintWriter;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    private void indent() {
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        writer.write("public class Main {");
        newline(0);
        newline(1);
        writer.write("public static void main(String[] args) {");
        newline(2);
        writer.write("System.exit(new Main().main());");
        newline(1);
        writer.write("}");

        for (Ast.Field field : ast.getFields()) {
            newline(1);
            visit(field);
        }
        indent = 1;
        for (Ast.Method method : ast.getMethods()) {
            newline(0);
            visit(method);
        }

        newline(0);
        writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        writer.write(Environment.getType(ast.getTypeName()).getJvmName());
        writer.write(" ");
        writer.write(ast.getName());

        if (ast.getValue().isPresent()) {
            writer.write(" = ");
            visit(ast.getValue().get());
        }

        writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        newline(indent);
        writer.write(ast.getFunction().getReturnType().getJvmName());
        writer.write(" ");
        writer.write(ast.getFunction().getJvmName());
        writer.write("(");

        for (int i = 0; i < ast.getParameters().size(); ++i) {
            writer.write(ast.getParameterTypeNames().get(i));
            writer.write(" ");
            writer.write(ast.getParameters().get(i));
            if (i < ast.getParameters().size() - 1) {
                writer.write(", ");
            }
        }

        writer.write(") {");

        indent++;
        for (Ast.Stmt stmt : ast.getStatements()) {
            newline(0);
            visit(stmt);
        }
        indent--;

        newline(indent);
        writer.write("}");
        newline(0);

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) {
        indent();
        visit(ast.getExpression());
        writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        if (ast.getTypeName().isPresent()) {
            writer.write(Environment.getType(ast.getTypeName().get()).getJvmName());
        } else {
            writer.write(ast.getVariable().getType().getJvmName());
        }

        writer.write(" ");
        writer.write(ast.getName());

        if (ast.getValue().isPresent()) {
            writer.write(" = ");
            visit(ast.getValue().get());
        }

        writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) {
        indent();
        visit(ast.getReceiver());
        writer.write(" = ");
        visit(ast.getValue());

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        indent();
        writer.write("if (");
        visit(ast.getCondition());
        writer.write(") {");
        newline(0);

        indent++;
        for (Ast.Stmt stmt : ast.getThenStatements()) {
            visit(stmt);
            newline(0);
        }
        indent--;
        indent();
        writer.write("}");

        if (ast.getElseStatements().size() > 0) {
            writer.write(" else {");
            newline(0);
            indent++;
            for (Ast.Stmt stmt : ast.getElseStatements()) {
                visit(stmt);
                newline(0);
            }
            indent--;
            indent();
            writer.write("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        indent();
        writer.write("for (int ");
        writer.write(ast.getName());
        writer.write(" : ");
        visit(ast.getValue());
        writer.write(") {");

        indent++;
        for (Ast.Stmt stmt : ast.getStatements()) {
            newline(0);
            visit(stmt);
        }
        indent--;

        newline(indent);
        writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) {
        indent();
        writer.write("while (");
        visit(ast.getCondition());
        writer.write(") {");

        indent++;
        for (Ast.Stmt stmt : ast.getStatements()) {
            newline(0);
            visit(stmt);
        }
        indent--;

        newline(indent);
        writer.write("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        indent();
        writer.write("return ");
        visit(ast.getValue());
        writer.write(";");

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) {
        Object value = ast.getLiteral();
        if (value instanceof String) {
            writer.write("\"");
            writer.write((String) value);
            writer.write("\"");
        } else if (value instanceof Character) {
            writer.write("'");
            writer.write((Character) value);
            writer.write("'");
        } else {
            writer.write(value.toString());
        }

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        writer.write("(");
        visit(ast.getExpression());
        writer.write(")");

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        visit(ast.getLeft());

        writer.write(" ");
        if (ast.getOperator().equals("AND")) {
            writer.write("&&");
        } else if (ast.getOperator().equals("OR")) {
            writer.write("||");
        } else {
            writer.write(ast.getOperator());
        }
        writer.write(" ");

        visit(ast.getRight());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent()) {
            visit(ast.getReceiver().get());
            writer.write(".");
        }

        writer.write(ast.getVariable().getJvmName());

        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        if (ast.getReceiver().isPresent()) {
            visit(ast.getReceiver().get());
            writer.write(".");
        }

        writer.write(ast.getFunction().getJvmName());
        writer.write("(");

        for (int i = 0; i < ast.getArguments().size(); ++i) {
            visit(ast.getArguments().get(i));
            if (i < ast.getArguments().size() - 1) {
                writer.write(", ");
            }
        }

        writer.write(")");

        return null;
    }

}
