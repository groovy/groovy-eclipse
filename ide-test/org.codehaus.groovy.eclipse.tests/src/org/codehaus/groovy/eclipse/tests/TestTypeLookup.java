package org.codehaus.groovy.eclipse.tests;

import javax.swing.text.html.HTML;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * A simple type lookup that all expressions are of type {@link HTML}
 */
public class TestTypeLookup implements ITypeLookup {


    public TypeLookupResult lookupType(Expression node, VariableScope scope,
            ClassNode objectExpressionType) {
        return new TypeLookupResult(ClassHelper.make(HTML.class), VariableScope.VOID_CLASS_NODE, VariableScope.STRING_CLASS_NODE
                .getMethod("toString", new Parameter[0]), TypeConfidence.LOOSELY_INFERRED, scope);
    }

    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }

    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        // do nothing
    }

}
