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
package org.codehaus.groovy.eclipse.debug.ui;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.core.search.LexicalClassVisitor;

/**
 * @author Andrew Eisenberg
 * @created Aug 4, 2009
 *
 * Compute a valid location where to put a breakpoint from a ModuleNode.
 * The result is the first valid location with a line number greater or equals than the given position.
 * A valid location is considered to be the last expression or statement on a given line
 *
 */
public class ValidBreakpointLocationFinder {

    private class VisitCompleted extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private ASTNode lastValid = null;

    private int startLine;

    public ValidBreakpointLocationFinder(int startLine) {
        this.startLine = startLine;
    }

    private void validateNode(ASTNode node) throws VisitCompleted {

        // can't set a breakpoint at these locations
        if (node.getLineNumber() == -1 || node instanceof Statement || node instanceof ClosureExpression
                || node instanceof ClassNode || /* node instanceof MethodNode || */node instanceof FieldNode) {
            return;
        }

        if (node.getLineNumber() == startLine) {
            lastValid = node;
            // keep on searching until the line is over
        } else if (node.getLineNumber() > startLine) {
            if (lastValid == null) {
                lastValid = node;
            }
            throw new VisitCompleted();
        }
    }

    public ASTNode findValidBreakpointLocation(ModuleNode module) {
        LexicalClassVisitor visitor = new LexicalClassVisitor(module);
        try {
            boolean skipNext = false;
            while (visitor.hasNextNode()) {
                ASTNode node = visitor.getNextNode();
                // can't set a breakpoint at a variable declaration that has
                // no initializer
                if (node instanceof DeclarationExpression) {
                    skipNext = true;
                    Expression rightExpression = ((DeclarationExpression) node).getRightExpression();
                    if (rightExpression == null || "null".equals(rightExpression.getText())) {
                        continue;
                    }
                } else if (skipNext) {
                    // variable expression in a declaration expression with no
                    // initializer
                    skipNext = false;
                } else {
                    validateNode(node);
                }
            }
        } catch (VisitCompleted vc) {}
        return lastValid;
    }
}
