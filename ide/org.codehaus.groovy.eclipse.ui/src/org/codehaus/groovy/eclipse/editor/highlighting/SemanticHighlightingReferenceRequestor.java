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

import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind;
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
public class SemanticHighlightingReferenceRequestor extends SemanticReferenceRequestor implements ITypeRequestor {
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
            // GRECLIPSE-1327 check to see if this is a synthetic call() on a closure reference
            if (isRealASTNode(node)) {
                Position p = getPosition(node);
                typedPosition.add(new HighlightedTypedPosition(p, HighlightKind.UNKNOWN));

                // don't continue if we have an unknown reference
                return VisitStatus.CANCEL_BRANCH;
            }
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
        } else if (result.declaration instanceof MethodNode && !(result.declaration instanceof ConstructorNode)) {
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

        if (pos != null && ((pos.getOffset() > 0 || pos.getLength() > 1) ||
                // Expression nodes can be still valid and have an offset of 0 and a
                // length of 1
                // whereas field/method nodes, this is not allowed.
                node instanceof Expression)) {
            typedPosition.add(pos);
        }

        return VisitStatus.CONTINUE;
    }

    /**
     * An AST node is "real" if it is an expression and the
     * text of the expression matches the actual text in the file
     */
    private boolean isRealASTNode(ASTNode node) {
        int contentsLen = contents.length;
        String text = node.getText();
        if (text.length() != node.getLength()) {
            return false;
        }
        char[] textArr = text.toCharArray();
        for (int i = 0, j = node.getStart(); i < textArr.length && j < contentsLen; i++, j++) {
            if (textArr[i] != contents[j]) {
                return false;
            }
        }

        return true;
    }

}
