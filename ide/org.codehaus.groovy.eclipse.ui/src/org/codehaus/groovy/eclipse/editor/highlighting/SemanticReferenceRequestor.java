/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.editor.highlighting;

import java.util.List;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTNode;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jface.text.Position;

/**
 * Assists with searching for kinds of references in Groovy files.
 *
 * @author Andrew Eisenberg
 * @created Aug 28, 2011
 */
public abstract class SemanticReferenceRequestor implements ITypeRequestor {

    protected static Position getPosition(ASTNode node) {
        int start, length;
        if (node instanceof FieldNode || node instanceof MethodNode || node instanceof PropertyNode ||
                (node instanceof ClassNode && ((ClassNode) node).getNameEnd() > 0)) {
            AnnotatedNode an = (AnnotatedNode) node;
            start = an.getNameStart();
            length = an.getNameEnd() - start + 1;
        } else if (node instanceof Parameter) {
            Parameter p = (Parameter) node;
            start = p.getNameStart();
            length = p.getNameEnd() - start;
        } else if (node instanceof ImportNode) {
            ClassNode clazz = ((ImportNode) node).getType();
            start = clazz.getStart();
            length = clazz.getLength();
        } else if (node instanceof StaticMethodCallExpression) {
            start = node.getStart();
            length = ((StaticMethodCallExpression) node).getMethod().length();
        } else if (node instanceof MethodCallExpression) {
            Expression e = ((MethodCallExpression) node).getMethod();
            // FIXADE : determine if we need to ignore funky method calls that
            // use things like GStrings in the
            // name
            // if (e instanceof ConstantExpression) {
            start = e.getStart();
            length = e.getLength();
            // }
        } else {
            start = node.getStart();
            length = node.getLength();
        }

        return new Position(start, length);
    }

    protected static boolean isDeprecated(ASTNode declaration) {
        if (declaration instanceof ClassNode) {
            declaration = ((ClassNode) declaration).redirect();
        }

        if (declaration instanceof PropertyNode && ((PropertyNode) declaration).getField() != null) {
            // make sure we are using the associated field node because property nodes are never the declaration
            declaration = ((PropertyNode) declaration).getField();
        }

        if (declaration instanceof JDTNode) {
            return ((JDTNode) declaration).isDeprecated();
        } else if (declaration instanceof ClassNode || declaration instanceof FieldNode || declaration instanceof MethodNode) {
            return hasDeprecatedAnnotation((AnnotatedNode) declaration);
        }

        return false;
    }

    private static boolean hasDeprecatedAnnotation(AnnotatedNode declaration) {
        // only DSLDs will have the deprecation flag set
        if (isDeprecated(declaration)) {
            return true;
        }
        List<AnnotationNode> anns = declaration.getAnnotations();
        for (AnnotationNode ann : anns) {
            if (ann.getClassNode() != null && ann.getClassNode().getName().equals("java.lang.Deprecated")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDeprecated(AnnotatedNode node) {
        int flags;
        if (node instanceof ClassNode) {
            flags = ((ClassNode) node).getModifiers();
        } else if (node instanceof MethodNode) {
            flags = ((MethodNode) node).getModifiers();
        } else if (node instanceof FieldNode) {
            flags = ((FieldNode) node).getModifiers();
        } else {
            flags = 0;
        }

        return (flags & Opcodes.ACC_DEPRECATED) != 0;
    }

    protected static boolean isFinal(ASTNode node) {
        if (node instanceof FieldNode) {
            return ((FieldNode) node).isFinal();
        }
        if (node instanceof MethodNode) {
            return (((MethodNode) node).getModifiers() & Opcodes.ACC_FINAL) != 0;
        }
        return false;
    }

    protected static boolean isForLoopParam(Variable param, VariableScope scope) {
        VariableScope.VariableInfo info = scope.lookupName(param.getName());
        return (info != null && info.scopeNode instanceof ForStatement);
    }

    protected static boolean isCatchParam(Variable param, VariableScope scope) {
        VariableScope.VariableInfo info = scope.lookupName(param.getName());
        return (info != null && info.scopeNode instanceof CatchStatement);
    }

    protected static boolean isNumber(ClassNode type) {
        return ClassHelper.isNumberType(type) || ClassHelper.BigDecimal_TYPE.equals(type) || ClassHelper.BigInteger_TYPE.equals(type);
    }

    protected static boolean isStatic(ASTNode node) {
        if (node instanceof FieldNode) {
            return ((FieldNode) node).isStatic();
        }
        if (node instanceof MethodNode) {
            return ((MethodNode) node).isStatic();
        }
        if (node instanceof PropertyNode) {
            return ((PropertyNode) node).isStatic();
        }
        return false;
    }
}
