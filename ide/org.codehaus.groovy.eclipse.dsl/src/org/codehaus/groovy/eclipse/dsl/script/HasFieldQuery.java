package org.codehaus.groovy.eclipse.dsl.script;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.dsl.script.IContextQueryResult.ResultKind;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A query that returns a field or an array of fields
 * if the node passed in matches the name
 * 
 * @author andrew
 * @created Nov 20, 2010
 */
class HasFieldQuery implements IContextQuery {

    private final String name;
    private final IContextQuery innerQuery;
    
    public HasFieldQuery(IContextQuery innerQuery) {
        this.innerQuery = innerQuery;
        this.name = null;
    }

    public HasFieldQuery(String name) {
        this.name = name;
        this.innerQuery = null;
    }

    public IContextQueryResult<?> evaluate(AnnotatedNode node, VariableScope currentScope) {
        FieldNode[] fields = getFields(node);
        if (fields != null) {
            if (name != null) {
                List<FieldNode> matches = new ArrayList<FieldNode>();
                for (FieldNode field : fields) {
                    if (field.getName().matches(name)) {
                        matches.add(field);
                    }
                }
                if (matches.isEmpty()) {
                    return EmptyResult.INSTANCE;
                } else if (matches.size() == 1) {
                    return new SingleNodeResult<FieldNode>(matches.get(0));
                } else {
                    return new MultipleNodeResult<FieldNode>(matches);
                }
            } else {
                for (FieldNode field : fields) {
                    IContextQueryResult<?> result = innerQuery.evaluate(field, currentScope);
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

    private FieldNode[] getFields(AnnotatedNode node) {
        if (node instanceof FieldNode) {
            return new FieldNode[] { (FieldNode) node };
        } else if (node instanceof ClassNode) {
            // include cast so safe for 1.5
            @SuppressWarnings("cast")
            List<FieldNode> fields = (List<FieldNode>) ((ClassNode) node).getFields();
            return fields.toArray(new FieldNode[0]);
        } else {
            return null;
        }
    }
}