 /*
 * Copyright 2003-2009 the original author or authors.
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
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTNode;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jface.text.Position;

/**
 * Find all unknown references, regex expressions, field references, and static
 * references
 *
 * @author Andrew Eisenberg
 * @created Oct 29, 2009
 */
public class SemanticHighlightingReferenceRequestor implements ITypeRequestor {
    /**
     * this set contains positions in a non-overlapping,
     * increasing lexical order
     * but the inferencing visitor does not always search in a lexical order.
     * This list should be changed to an ordered list
     */
    SortedSet<HighlightedTypedPosition> typedPosition = new TreeSet<HighlightedTypedPosition>();

    final char[] contents;

    public SemanticHighlightingReferenceRequestor(char[] contents) {
        this.contents = contents;
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        // ignore statements
        if (!(node instanceof AnnotatedNode)) {
            return VisitStatus.CONTINUE;
        }

        // ignore nodes with invalid slocs
        if (node.getEnd() <= 0 || (node.getStart() == 0 && node.getEnd() == 1)) {
            return VisitStatus.CONTINUE;
        }

        HighlightedTypedPosition pos = null;
        if (result.confidence == TypeConfidence.UNKNOWN && node.getEnd() > 0) {
            Position p = getPosition(node);
            typedPosition.add(new HighlightedTypedPosition(p, HighlightKind.UNKNOWN));

            // don't continue if we have an unknown reference
            return VisitStatus.CANCEL_BRANCH;
        } else if (node instanceof AnnotatedNode && isDeprecated(result.declaration)) {
            Position p = getPosition(node);
            pos = new HighlightedTypedPosition(p, HighlightKind.DEPRECATED);
        } else if (result.declaration instanceof FieldNode || result.declaration instanceof PropertyNode) {
            Position p = getPosition(node);
            if (isStatic(result.declaration)) {
                pos = new HighlightedTypedPosition(p, HighlightKind.STATIC_FIELD);
            } else {
                pos = new HighlightedTypedPosition(p, HighlightKind.FIELD);
            }
        } else if (result.declaration instanceof MethodNode) {
            Position p = getPosition(node);
            if (isStatic(result.declaration)) {
                pos = new HighlightedTypedPosition(p, HighlightKind.STATIC_METHOD);
            } else {
                pos = new HighlightedTypedPosition(p, HighlightKind.METHOD);
            }
        } else if (node instanceof ConstantExpression && node.getStart() < contents.length) {
            if (contents[node.getStart()] == '/') {
                Position p = getPosition(node);
                pos = new HighlightedTypedPosition(p, HighlightKind.REGEX);
            } else if (isNumber(((ConstantExpression) node).getType())) {
                Position p = getPosition(node);
                pos = new HighlightedTypedPosition(p, HighlightKind.NUMBER);
            }
        }

        if (pos != null && (pos.getOffset() > 0 || pos.getLength() > 1)) {
            typedPosition.add(pos);
        }

        return VisitStatus.CONTINUE;
    }

    private boolean isNumber(ClassNode type) {
        return ClassHelper.isNumberType(type) || type == ClassHelper.BigDecimal_TYPE || type == ClassHelper.BigInteger_TYPE;
    }

    private Position getPosition(ASTNode node) {
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

    private boolean isDeprecated(ASTNode declaration) {
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
        List<AnnotationNode> anns = declaration.getAnnotations();
        for (AnnotationNode ann : anns) {
            if (ann.getClassNode() != null && ann.getClassNode().getName().equals("java.lang.Deprecated")) {
                return true;
            }
        }
        return false;
    }

    private boolean isStatic(ASTNode declaration) {
        return (declaration instanceof MethodNode && ((MethodNode) declaration).isStatic())
                || (declaration instanceof PropertyNode && ((PropertyNode) declaration).isStatic())
                || (declaration instanceof FieldNode && ((FieldNode) declaration).isStatic());
    }

}
