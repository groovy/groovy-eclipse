/*
 * Copyright 2003-2010 the original author or authors.
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

import groovyjarjarasm.asm.Opcodes;

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTNode;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jface.text.Position;

/**
 * Abstract class to assist with searching for kinds of references in groovy
 * files
 * 
 * @author andrew
 * @created Aug 28, 2011
 */
public abstract class SemanticReferenceRequestor implements ITypeRequestor {

    protected boolean isNumber(ClassNode type) {
        return ClassHelper.isNumberType(type) || type == ClassHelper.BigDecimal_TYPE || type == ClassHelper.BigInteger_TYPE;
    }

    protected Position getPosition(ASTNode node) {
        int start, length;
        if (node instanceof MethodNode || node instanceof FieldNode || node instanceof PropertyNode
                || (node instanceof ClassNode && ((ClassNode) node).getNameEnd() > 0)) {
            AnnotatedNode an = (AnnotatedNode) node;
            start = an.getNameStart();
            length = an.getNameEnd() - start + 1;
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

    protected boolean isDeprecated(ASTNode declaration) {
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

    private boolean hasDeprecatedAnnotation(AnnotatedNode declaration) {
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

    private boolean isDeprecated(AnnotatedNode declaration) {
        int flags;

        if (declaration instanceof ClassNode) {
            flags = ((ClassNode) declaration).getModifiers();
        } else if (declaration instanceof MethodNode) {
            flags = ((MethodNode) declaration).getModifiers();
        } else if (declaration instanceof FieldNode) {
            flags = ((FieldNode) declaration).getModifiers();
        } else {
            flags = 0;
        }

        return (flags & Opcodes.ACC_DEPRECATED) != 0;
    }

    protected boolean isStatic(ASTNode declaration) {
        return (declaration instanceof MethodNode && ((MethodNode) declaration).isStatic())
                || (declaration instanceof PropertyNode && ((PropertyNode) declaration).isStatic())
                || (declaration instanceof FieldNode && ((FieldNode) declaration).isStatic());
    }

}