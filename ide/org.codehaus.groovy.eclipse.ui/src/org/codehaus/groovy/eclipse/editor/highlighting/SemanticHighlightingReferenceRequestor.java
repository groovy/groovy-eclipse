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

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTClassNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTFieldNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
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

    List<Position> unknownNodes = new LinkedList<Position>();

    List<Position> regexNodes = new LinkedList<Position>();

    List<Position> deprecatedNodes = new LinkedList<Position>();

    List<Position> staticNodes = new LinkedList<Position>();

    List<Position> fieldReferenceNodes = new LinkedList<Position>();

    final char[] contents;

    public SemanticHighlightingReferenceRequestor(char[] contents) {
        this.contents = contents;
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
            IJavaElement enclosingElement) {
        if (node.getEnd() <= 0) {
            return VisitStatus.CONTINUE;
        }
        if (result.confidence == TypeConfidence.UNKNOWN && node.getEnd() > 0) {
            unknownNodes.add(getPosition(node));
            // don't continue if we have an unknown reference
            return VisitStatus.CANCEL_BRANCH;
        } else if (node instanceof ConstantExpression && node.getStart() < contents.length && contents[node.getStart()] == '/') {
            regexNodes.add(getPosition(node));
        }
        if (isDeprecated(result.declaration)) {
            deprecatedNodes.add(getPosition(node));
        }

        if (isStatic(result.declaration)) {
            staticNodes.add(getPosition(node));
        }

        if (result.declaration instanceof FieldNode) {
            fieldReferenceNodes.add(getPosition(node));
        }

        return VisitStatus.CONTINUE;
    }

    /**
     * @param node
     * @return
     */
    private Position getPosition(ASTNode node) {
        int start, length;
        if (node instanceof MethodNode || node instanceof FieldNode
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
        return (declaration instanceof JDTClassNode && ((JDTClassNode) declaration).getJdtBinding().isDeprecated())
                || (declaration instanceof JDTMethodNode && ((JDTMethodNode) declaration).getMethodBinding().isDeprecated())
                || (declaration instanceof JDTFieldNode && ((JDTFieldNode) declaration).getFieldBinding().isDeprecated());
    }

    private boolean isStatic(ASTNode declaration) {
        return (declaration instanceof MethodNode && ((MethodNode) declaration).isStatic())
                || (declaration instanceof PropertyNode && ((PropertyNode) declaration).isStatic())
                || (declaration instanceof FieldNode && ((FieldNode) declaration).isStatic());
    }

}
