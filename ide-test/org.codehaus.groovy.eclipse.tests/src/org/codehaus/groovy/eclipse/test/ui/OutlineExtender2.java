/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.ui;

import java.util.Stack;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

public class OutlineExtender2 extends OutlineExtender1 {

    public static final String NATURE = "org.codehaus.groovy.eclipse.tests.testNature2";

    @Override
    public boolean appliesTo(final GroovyCompilationUnit unit) {
        return CharOperation.contains('Y', unit.getFileName());
    }

    @Override
    public GroovyOutlinePage getGroovyOutlinePageForEditor(final String contextMenuID, final GroovyEditor editor) {
        TCompilationUnit2 ounit = new TCompilationUnit2(this, editor.getGroovyCompilationUnit());
        return new TGroovyOutlinePage(null, editor, ounit);
    }

    public static class TCompilationUnit2 extends TCompilationUnit {

        public TCompilationUnit2(final OutlineExtender2 extender, final GroovyCompilationUnit unit) {
            super(extender, unit);
        }

        @Override
        public IMember[] refreshChildren() {
            type = new TType(this, getElementName().substring(0, getElementName().indexOf('.')));
            ModuleNode moduleNode = (ModuleNode) getNode();
            if (moduleNode != null) {
                new Finder(moduleNode, type).execute();
            }
            return new IMember[] {type};
        }

        @Override
        public void refresh() {
            super.refresh();
        }
    }

    public static class Finder extends ASTNodeFinder {

        private ModuleNode moduleNode;
        private Stack<TType> methodStack = new Stack<>();

        public Finder(final ModuleNode moduleNode, final TType rootType) {
            super(new Region(moduleNode));
            this.moduleNode = moduleNode;
            methodStack.push(rootType);
        }

        public void execute() {
            doVisit(moduleNode);
        }

        @Override
        public void visitMethod(final MethodNode method) {
            if (method.getLineNumber() > 1) {
                TType parentType = methodStack.peek();
                parentType.addTestMethod(method.getName(), method.getReturnType().getNameWithoutPackage());
            }
            runMethod = null; // visit normally
            super.visitMethod(method);
        }

        @Override
        public void visitMethodCallExpression(final MethodCallExpression methodCall) {
            if (methodCall.getEnd() > 0) {
                TType parentType = methodStack.peek();
                TType t = parentType.addTestType(methodCall.getMethodAsString());

                methodStack.push(t);
            }
            super.visitMethodCallExpression(methodCall);
            if (methodCall.getEnd() > 0) {
                methodStack.pop();
            }
        }

        @Override
        public void visitVariableExpression(final VariableExpression variable) {
            if (variable.getEnd() > 0) {
                TType parentType = methodStack.peek();
                parentType.addTestField(variable.getName(), GroovyUtils.getTypeSignature(variable.getType(), false, false));
            }
            super.visitVariableExpression(variable);
        }
    }
}
