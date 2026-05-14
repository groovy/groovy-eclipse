/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
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

    public BreakpointLocationFinder(final ModuleNode module) {
        locator = module.getNodeMetaData(LocationSupport.class);
        TreeSet<ASTNode> nodes = new TreeSet<>(Comparator.comparing(this::lineNumber).thenComparing(ASTNode::getColumnNumber)
            .thenComparing(Comparator.comparing(ASTNode::getLastLineNumber).thenComparing(ASTNode::getLastColumnNumber).reversed()));

        new DepthFirstVisitor() {
            @Override
            protected void visitAnnotation(final AnnotationNode annotation) {
                for (Expression expression : annotation.getMembers().values()) {
                    if (expression instanceof ClosureExpression) {
                        expression.visit(this);
                    }
                }
            }

            @Override
            protected void visitExpression(final Expression expression) {
                if (expression.getLineNumber() > 0 &&
                        !(expression instanceof ClosureExpression || expression instanceof TupleExpression)) {
                    nodes.add(expression);
                }
                super.visitExpression(expression);
            }

            @Override
            public void visitMethod(final MethodNode node) {
                if (node.getLineNumber() > 0) {
                    nodes.add(node);
                }
                super.visitMethod(node);
            }

            @Override
            public void visitField(final FieldNode node) {
                if (node.getLineNumber() > 0) {
                    nodes.add(node);
                }
                super.visitField(node);
            }

            @Override
            public void visitClass(final ClassNode node) {
                if (node.getLineNumber() > 0) {
                    nodes.add(node);
                }
                super.visitClass(node);
            }
        }.visitModule(module);

        this.nodes = Collections.unmodifiableSet(nodes);
    }

    public ASTNode findBreakpointLocation(final int lineNumber) {
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

    protected int lineNumber(final ASTNode node) {
        if (locator != null && (node instanceof AnnotatedNode && !(node instanceof Expression))) {
            // annotations, modifiers and generics may be on separate line(s)
            int[] row_col = locator.getRowCol(((AnnotatedNode) node).getNameStart());
            if (row_col != null && row_col.length > 0) {
                return row_col[0];
            }
        }
        return node.getLineNumber();
    }
}
