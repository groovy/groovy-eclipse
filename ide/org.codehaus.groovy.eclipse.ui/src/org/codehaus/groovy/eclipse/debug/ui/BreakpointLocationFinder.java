 /*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.debug.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import org.codehaus.groovy.antlr.LocationSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.eclipse.jdt.groovy.core.util.DepthFirstVisitor;

public class BreakpointLocationFinder {

    protected final LocationSupport locator;
    protected final Iterable<ASTNode> nodes;

    public BreakpointLocationFinder(ModuleNode module) {
        TreeSet<ASTNode> nodes = new TreeSet<>(Comparator.comparing(ASTNode::getLineNumber).thenComparing(ASTNode::getColumnNumber)
            .thenComparing(Comparator.comparing(ASTNode::getLastLineNumber).thenComparing(ASTNode::getLastColumnNumber).reversed()));

        new DepthFirstVisitor() {
            @Override
            protected void visitAnnotation(AnnotationNode annotation) {
            }

            @Override
            protected void visitExpression(Expression expression) {
                if (expression.getLineNumber() > 0 &&
                        !(expression instanceof ClosureExpression || expression instanceof TupleExpression)) {
                    nodes.add(expression);
                }
                super.visitExpression(expression);
            }

            @Override
            public void visitMethod(MethodNode method) {
                if (method.getLineNumber() > 0) {
                    nodes.add(method);
                }
                super.visitMethod(method);
            }

        }.visitModule(module);

        this.nodes = Collections.unmodifiableSet(nodes);
        this.locator = module.getNodeMetaData(LocationSupport.class);
    }

    public ASTNode findBreakpointLocation(int lineNumber) {
        ASTNode bestMatch = null;
        boolean skipNext = false;
        for (ASTNode node : nodes) {
            if (skipNext) { skipNext = false;
            } else if (node instanceof DeclarationExpression) {
                Expression rightExpression = ((DeclarationExpression) node).getRightExpression();
                if (rightExpression == null || "null".equals(rightExpression.getText())) {
                    // variable expression in a declaration expression with no initializer
                    skipNext = true;
                }
            } else if (lineNumber(node) >= lineNumber) {
                bestMatch = node;
                break;
            }
        }

        return bestMatch;
    }

    protected int lineNumber(ASTNode node) {
        if (locator != null && node instanceof MethodNode) {
            // annotations, modifiers and generics may be on separate line(s)
            int[] row_col = locator.getRowCol(((MethodNode) node).getNameStart());
            if (row_col != null && row_col.length > 0) {
                return row_col[0];
            }
        }
        return node.getLineNumber();
    }
}
