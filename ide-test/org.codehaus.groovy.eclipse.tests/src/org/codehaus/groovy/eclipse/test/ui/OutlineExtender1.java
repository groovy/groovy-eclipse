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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.editor.outline.GroovyOutlinePage;
import org.codehaus.groovy.eclipse.editor.outline.IOJavaElement;
import org.codehaus.groovy.eclipse.editor.outline.IOutlineExtender;
import org.codehaus.groovy.eclipse.editor.outline.OCompilationUnit;
import org.codehaus.groovy.eclipse.editor.outline.OField;
import org.codehaus.groovy.eclipse.editor.outline.OMethod;
import org.codehaus.groovy.eclipse.editor.outline.OType;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.compiler.CharOperation;

public class OutlineExtender1 implements IOutlineExtender, IProjectNature {

    public static final String NATURE = "org.codehaus.groovy.eclipse.tests.testNature1";

    @Override
    public void configure() throws CoreException {
    }

    @Override
    public void deconfigure() throws CoreException {
    }

    private IProject project;

    @Override
    public void setProject(final IProject project) {
        this.project = project;
    }

    @Override
    public IProject getProject() {
        return project;
    }

    @Override
    public GroovyOutlinePage getGroovyOutlinePageForEditor(final String contextMenuID, final GroovyEditor editor) {
        TCompilationUnit ounit = new TCompilationUnit(this, editor.getGroovyCompilationUnit());
        return new TGroovyOutlinePage(null, editor, ounit);
    }

    @Override
    public boolean appliesTo(final GroovyCompilationUnit unit) {
        return CharOperation.contains('X', unit.getFileName());
    }

    public static class TGroovyOutlinePage extends GroovyOutlinePage {

        public TGroovyOutlinePage(final String contextMenuID, final GroovyEditor editor, final OCompilationUnit unit) {
            super(contextMenuID, editor, unit);
        }

        public JavaOutlineViewer getViewer() {
            return getOutlineViewer();
        }
    }

    public static class TCompilationUnit extends OCompilationUnit {

        public OutlineExtender1 outlineExtender;
        public TType type;

        public TCompilationUnit(final OutlineExtender1 extender, final GroovyCompilationUnit unit) {
            super(unit);
            this.outlineExtender = extender;
        }

        @Override
        public IMember getOutlineElementAt(final int caretOffset) {
            return type;
        }

        @Override
        public IMember[] refreshChildren() {
            type = new TType(this, getElementName());
            return new IMember[] {type};
        }
    }

    public static class TType extends OType {

        public TType(final IOJavaElement parent, final String name) {
            super(parent, new ConstantExpression(name), name);
            this.name = name;
        }

        @Override
        public ASTNode getElementNameNode() {
            return getNode();
        }

        public TType addTestType(final String name) {
            TType t = new TType(this, name);
            addChild(t);
            return t;
        }

        public TField addTestField(final String name, final String type) {
            TField f = new TField(this, name, type);
            addChild(f);
            return f;
        }

        public TMethod addTestMethod(final String name, final String returnType) {
            TMethod m = new TMethod(this, name, returnType);
            addChild(m);
            return m;
        }
    }

    public static class TMethod extends OMethod {

        private String returnType;

        public TMethod(final OType parent, final String name, final String returnType) {
            super(parent, new ConstantExpression(name), name);
            this.name = name;
            this.returnType = returnType;
        }

        @Override
        public ASTNode getElementNameNode() {
            return getNode();
        }

        @Override
        public String getReturnTypeName() {
            return returnType;
        }
    }

    public static class TField extends OField {

        private String typeSignature;

        public TField(final OType parent, final String name, final String typeSignature) {
            super(parent, new ConstantExpression(name), name);
            this.name = name;
            this.typeSignature = typeSignature;
        }

        @Override
        public ASTNode getElementNameNode() {
            return getNode();
        }

        @Override
        public String getTypeSignature() {
            return typeSignature;
        }
    }
}