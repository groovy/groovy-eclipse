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
package org.codehaus.groovy.eclipse.refactoring.core.convert;

import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.VisitCompleteException;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Common class to process the assign to new local variable refactoring.
 * Used by both the completion proposal and the refactor menu option.
 */
// FIXGWD: This class should be converted into a proper refactoring class which extends Refactoring.
public class AssignStatementToNewLocalRefactoring {

    private int newLength;
    private int newOffset;
    private Expression expression;
    private boolean atExpressionStatement;

    private final int offset;
    private final GroovyCompilationUnit unit;

    public AssignStatementToNewLocalRefactoring(final GroovyCompilationUnit unit, final int offset) {
        this.unit = unit;
        this.offset = offset;
    }

    public boolean isApplicable() {
        if (unit == null) {
            return false;
        }
        atExpressionStatement = false;
        ModuleNode moduleNode = unit.getModuleNode();
        List<ClassNode> classNodes = moduleNode.getClasses();
        if (classNodes.isEmpty()) {
            classNodes.add(moduleNode.getScriptClassDummy());
        }

        Region region = new Region(offset, 0);

        GroovyClassVisitor visitor = new ClassCodeVisitorSupport() {
            @Override
            public void visitExpressionStatement(final ExpressionStatement statement) {
                if (region.regionIsCoveredByNode(statement)) {
                    visitStatementExpression(statement.getExpression());
                }
                super.visitExpressionStatement(statement);
            }

            @Override
            public void visitReturnStatement(final ReturnStatement statement) {
                if (region.regionIsCoveredByNode(statement)) {
                    char[] contents = unit.getContents();
                    if (statement.getStart() >= 0 && statement.getStart() + statement.getLength() < contents.length) {
                        String source = String.valueOf(contents, statement.getStart(), statement.getLength());
                        if (!source.matches("return\\b.*")) { // skip if return keyword is present
                            visitStatementExpression(statement.getExpression());
                        }
                    }
                }
                super.visitReturnStatement(statement);
            }

            private void visitStatementExpression(final Expression statementExpression) {
                if (statementExpression instanceof BinaryExpression) {
                    BinaryExpression bexp = (BinaryExpression) statementExpression;
                    if (!Types.isAssignment(bexp.getOperation().getType())) {
                        expression = statementExpression;
                        atExpressionStatement = true;
                    } else if (!(bexp.getRightExpression() instanceof ClosureExpression)) {
                        throw new VisitCompleteException();
                    }
                } else {
                    expression = statementExpression;
                    atExpressionStatement = true;
                }
            }
        };

        for (ClassNode classNode : classNodes) {
            try {
                if (region.regionIsCoveredByNode(classNode)) {
                    visitor.visitClass(classNode);
                }
            } catch (VisitCompleteException expected) {
                break;
            }
        }

        return atExpressionStatement;
    }

    //--------------------------------------------------------------------------

    public void applyRefactoring(final IDocument document) {
        if (atExpressionStatement) {
            TextEdit thisEdit = createEdit(document);
            try {
                if (thisEdit != null) {
                    thisEdit.apply(document);
                }
            } catch (Exception e) {
                GroovyCore.logException("Oops.", e);
            }
        }
    }

    public TextEdit createEdit(final IDocument document) {
        TextEdit edit = new MultiTextEdit();

        String candidate;
        if (expression instanceof ConstantExpression) {
            candidate = ((ConstantExpression) expression).getText();
        } else if (expression instanceof VariableExpression) {
            candidate = ((VariableExpression) expression).getName();
        } else if (expression instanceof ClassExpression) {
            candidate = ((ClassExpression) expression).getType().getNameWithoutPackage();
        } else if (expression instanceof MethodCallExpression) {
            candidate = ((MethodCallExpression) expression).getMethodAsString();
        } else if (expression instanceof StaticMethodCallExpression) {
            candidate = ((StaticMethodCallExpression) expression).getMethod();
        } else if (expression instanceof MapExpression) {
            candidate = "map";
        } else if (expression instanceof ListExpression) {
            candidate = "list";
        } else {
            candidate = "temp";
        }

        String[] knownNames = ASTTools.getVariablesInScope(unit.getModuleNode(), expression).stream().map(Variable::getName).toArray(String[]::new);

        String[] names = NamingConventions.suggestVariableNames(NamingConventions.VK_LOCAL, NamingConventions.BK_NAME, candidate, null, 0, knownNames, true);

        edit.addChild(new InsertEdit(expression.getStart(), "def " + names[0] + " = "));
        newOffset = expression.getStart() + "def ".length();
        newLength = names[0].length();
        return edit;
    }

    public Point getNewSelection() {
        return new Point(newOffset, newLength);
    }
}
