package org.codehaus.groovy.eclipse.dsl.script;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult.ResultKind;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A query that returns a method or an array of methods
 * if the node passed in matches the name
 * 
 * @author andrew
 * @created Nov 20, 2010
 */
class HasMethodQuery implements IContextQuery {

    private final String name;
    private final IContextQuery innerQuery;
    
    public HasMethodQuery(IContextQuery innerQuery) {
        this.innerQuery = innerQuery;
        this.name = null;
    }

    public HasMethodQuery(String name) {
        this.name = name;
        this.innerQuery = null;
    }

    public IContextQueryResult<?> evaluate(AnnotatedNode node, VariableScope currentScope) {
        MethodNode[] methods = getMethods(node);
        if (methods != null) {
            if (name != null) {
                List<MethodNode> matches = new ArrayList<MethodNode>();
                for (MethodNode method : methods) {
                    if (method.getName().matches(name)) {
                        matches.add(method);
                    }
                }
                if (matches.isEmpty()) {
                    return EmptyResult.INSTANCE;
                } else if (matches.size() == 1) {
                    return new SingleNodeResult<MethodNode>(matches.get(0));
                } else {
                    return new MultipleNodeResult<MethodNode>(matches);
                }
            } else {
                for (MethodNode method : methods) {
                    IContextQueryResult<?> result = innerQuery.evaluate(method, currentScope);
                    if (result.getResultKind() == ResultKind.EMPTY || result.getResultKind() == ResultKind.SINGLE_NODE) {
                        return result;
                    } else if (result instanceof MultipleNodeResult) {
                        return result;
                    } else {
                        return EmptyResult.INSTANCE;
                    }
                }
            }
        }
        return EmptyResult.INSTANCE;
    }

    private MethodNode[] getMethods(AnnotatedNode node) {
        if (node instanceof MethodNode) {
            return new MethodNode[] { (MethodNode) node };
        } else if (node instanceof ClassNode) {
            // include cast so safe for 1.5
            @SuppressWarnings("cast")
            List<MethodNode> methods = (List<MethodNode>) ((ClassNode) node).getMethods();
            return methods.toArray(new MethodNode[0]);
        } else {
            return null;
        }
    }
}