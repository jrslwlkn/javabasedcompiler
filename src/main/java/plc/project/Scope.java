package plc.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Scope {

    private final Scope parent;
    private final Map<String, Environment.Variable> variables = new HashMap<>();
    private final Map<String, Environment.Function> functions = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public void defineVariable(String name, Environment.PlcObject value) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("The variable " + name + " is already defined in this scope.");
        } else {
            variables.put(name, new Environment.Variable(name, value));
        }
    }

    public Environment.Variable lookupVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.lookupVariable(name);
        } else {
            throw new RuntimeException("The variable " + name + " is not defined in this scope.");
        }
    }

    public void defineFunction(String name, int arity, Function<List<Environment.PlcObject>, Environment.PlcObject> function) {
        if (functions.containsKey(name + "/" + arity)) {
            throw new RuntimeException("The function " + name + "/" + arity + " is already defined in this scope.");
        } else {
            functions.put(name + "/" + arity, new Environment.Function(name, arity, function));
        }
    }

    public Environment.Function lookupFunction(String name, int arity) {
        if (functions.containsKey(name + "/" + arity)) {
            return functions.get(name + "/" + arity);
        } else if (parent != null) {
            return parent.lookupFunction(name, arity);
        } else {
            throw new RuntimeException("The function " + name + "/" + arity + " is not defined in this scope.");
        }
    }

    @Override
    public String toString() {
        return "Scope{" +
                "parent=" + parent +
                ", variables=" + variables +
                ", functions=" + functions +
                '}';
    }

}
