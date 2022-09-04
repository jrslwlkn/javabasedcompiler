package plc.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Scope {

    private final Scope _parent;
    private final Map<String, Environment.Variable> _variables = new HashMap<>();
    private final Map<String, Environment.Function> _functions = new HashMap<>();

    public Scope(Scope parent) {
        _parent = parent;
    }

    public Scope getParent() {
        return _parent;
    }

    public void defineVariable(String name, Environment.PlcObject value) {
        defineVariable(name, name, Environment.Type.ANY, value);
    }

    public Environment.Variable defineVariable(String name, String jvmName, Environment.Type type, Environment.PlcObject value) {
        if (_variables.containsKey(name)) {
            throw new RuntimeException("The variable " + name + " is already defined in this scope.");
        } else {
            Environment.Variable variable = new Environment.Variable(name, jvmName, type, value);
            _variables.put(variable.getName(), variable);
            return _variables.get(name);
        }
    }

    public Environment.Variable lookupVariable(String name) {
        if (_variables.containsKey(name)) {
            return _variables.get(name);
        } else if (_parent != null) {
            return _parent.lookupVariable(name);
        } else {
            throw new RuntimeException("The variable " + name + " is not defined in this scope.");
        }
    }

    public void defineFunction(String name, int arity, Function<List<Environment.PlcObject>, Environment.PlcObject> function) {
        List<Environment.Type> parameterTypes = new ArrayList<>();
        for (int i = 0; i < arity; i++) {
            parameterTypes.add(Environment.Type.ANY);
        }
        defineFunction(name, name, parameterTypes, Environment.Type.ANY, function);
    }

    public Environment.Function defineFunction(String name, String jvmName, List<Environment.Type> parameterTypes, Environment.Type returnType, java.util.function.Function<List<Environment.PlcObject>, Environment.PlcObject> function) {
        if (_functions.containsKey(name + "/" + parameterTypes.size())) {
            throw new RuntimeException("The function " + name + "/" + parameterTypes.size() + " is already defined in this scope.");
        } else {
            Environment.Function func = new Environment.Function(name, jvmName, parameterTypes, returnType, function);
            _functions.put(func.getName() + "/" + func.getParameterTypes().size(), func);
            return func;
        }
    }

    public Environment.Function lookupFunction(String name, int arity) {
        if (_functions.containsKey(name + "/" + arity)) {
            return _functions.get(name + "/" + arity);
        } else if (_parent != null) {
            return _parent.lookupFunction(name, arity);
        } else {
            throw new RuntimeException("The function " + name + "/" + arity + " is not defined in this scope.");
        }
    }

    @Override
    public String toString() {
        return "Scope{" +
                "parent=" + _parent +
                ", variables=" + _variables.keySet() +
                ", functions=" + _functions.keySet() +
                '}';
    }

}
