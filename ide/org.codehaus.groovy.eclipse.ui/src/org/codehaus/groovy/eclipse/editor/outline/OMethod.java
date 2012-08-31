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
package org.codehaus.groovy.eclipse.editor.outline;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceMethodInfo;

/**
 * @author maxime hamm
 * @created 1 avr. 2011
 */
public abstract class OMethod extends SourceMethod implements IOJavaElement {

    protected ASTNode node;

    private OMethodInfo cachedInfo;

    /**
     * @param parent the parent type
     * @param node the node that matches the whole method source source (not
     *            only the method name or method signature but also the method
     *            body)
     * @param name the method name
     */
    public OMethod(JavaElement parent, ASTNode node, String name) {
        super(parent, name, null);
        this.node = node;
    }

    public ASTNode getNode() {
        return node;
    }

    /**
     * The element name node.
     * i.e the node that will be highlighted int the editor when you will select
     * the method in the outline view
     *
     * @return
     */
    public ASTNode getElementNameNode() {
        return node;
    }

    /**
     * @return The return type to display.
     */
    public abstract String getReturnTypeName();

    @Override
    public Object getElementInfo() throws JavaModelException {
        if (cachedInfo == null) {
            cachedInfo = (OMethodInfo) createElementInfo();
        }
        return cachedInfo;
    }

    @Override
    protected Object createElementInfo() {
        return new OMethodInfo();
    }

    public GroovyCompilationUnit getUnit() {
        ICompilationUnit unit = (ICompilationUnit) getAncestor(IJavaElement.COMPILATION_UNIT);
        if (unit instanceof GroovyCompilationUnit) {
            return (GroovyCompilationUnit) unit;
        } else {
            Assert.isTrue(false, "Expecting GroovyCompilationUnit, but found " + unit);
            return null; // won't get here
        }
    }

    @Override
    public String[] getCategories() throws JavaModelException {
        // categories not supported
        //        if (exists()) {
        //            return super.getCategories();
        //        }
        return NO_CATEGORIES;
    }

    /****************************************************************************
     * @author maxime hamm
     * @created 3 avr. 2011
     */
    public class OMethodInfo extends SourceMethodInfo {

        @Override
        public int getNameSourceStart() {
            return getElementNameNode().getStart();
        }

        @Override
        public int getNameSourceEnd() {
            return getNameSourceStart() + getElementNameNode().getLength();
        }

        @Override
        public int getDeclarationSourceStart() {
            return node.getStart();
        }

        @Override
        public int getDeclarationSourceEnd() {
            return node.getEnd();
        }

        @Override
        protected ISourceRange getSourceRange() {
            return new SourceRange(node.getStart(), node.getLength());
        }

        @Override
        public char[] getReturnTypeName() {
            return OMethod.this.getReturnTypeName().toCharArray();
        }
    }

}
