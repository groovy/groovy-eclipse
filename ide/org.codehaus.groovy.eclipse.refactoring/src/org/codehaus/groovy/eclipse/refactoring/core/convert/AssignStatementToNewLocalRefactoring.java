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
package org.codehaus.groovy.eclipse.refactoring.core.convert;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
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

    private final GroovyCompilationUnit unit;

    private final int length;
    private final int offset;

    // for setting new selection
    private int newLength;

    private int newOffset;

    private boolean atExpressionStatement;

    private Region region;

    private Expression expression;

    public AssignStatementToNewLocalRefactoring(GroovyCompilationUnit unit, int offset) {
        this.unit = unit;
        length = 0;
        this.offset = offset;
    }

    public void applyRefactoring(IDocument document) {
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

    public boolean isApplicable() {
        if (unit == null) {
            return false;
        }
        return this.atExpressionStatement();
    }

    private boolean atExpressionStatement() {
        region = new Region(offset, length);
        atExpressionStatement = false;

        ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {

            private void processExpression(Expression statementExpression) {
                if (statementExpression instanceof org.codehaus.groovy.ast.expr.BinaryExpression) {
                    BinaryExpression bexp = (BinaryExpression) statementExpression;

                    if (!bexp.getOperation().getText().equals("=")) {
                        expression = statementExpression;
                        atExpressionStatement = true;
                    } else if (bexp.getRightExpression() instanceof ClosureExpression) {
                        return;
                    } else {
                        throw new VisitCompleteException();
                    }

                } else {
                    expression = statementExpression;
                    atExpressionStatement = true;
                }
            }

            @Override
            public void visitExpressionStatement(ExpressionStatement statement) {
                if (region.regionIsCoveredByNode(statement)) {
                    processExpression(statement.getExpression());
                }
                super.visitExpressionStatement(statement);
            }

            @Override
            public void visitReturnStatement(ReturnStatement statement) {
                if (region.regionIsCoveredByNode(statement)) {
                    processExpression(statement.getExpression());
                }
                super.visitReturnStatement(statement);
            }
        };
        ModuleNode moduleNode = unit.getModuleNode();
        List<ClassNode> classes = moduleNode.getClasses();

        if (classes.isEmpty()) {
            classes.add(moduleNode.getScriptClassDummy());
        }

        for (ClassNode classNode : classes) {
            try {
                visitor.visitClass(classNode);
            } catch (VisitCompleteException expected) {
                break;
            }
        }

        return atExpressionStatement;
    }

    public TextEdit createEdit(IDocument doc) {
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

        Set<Variable> vars = ASTTools.getVariablesInScope(unit.getModuleNode(), expression);
        String[] variableNames = new String[vars.size()];
        int i = 0;
        for (Variable v : vars) {
            variableNames[i] = v.getName();
            i++;
        }

        String[] names = NamingConventions.suggestVariableNames(NamingConventions.VK_LOCAL, NamingConventions.BK_NAME,
            candidate, null, 0, variableNames, true);

        edit.addChild(new InsertEdit(expression.getStart(), "def " + names[0] + " = "));
        // add 4 for "def ".
        newOffset = expression.getStart() + 4;
        newLength = names[0].length();

        return edit;
    }

    public Point getNewSelection() {
        return new Point(newOffset, newLength);
    }
}
