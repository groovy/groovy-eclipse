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
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;

/**
 * @author maxime hamm
 * @created 11 avr. 2011
 */
public abstract class OField extends SourceField implements IOJavaElement {

    protected ASTNode node;

    private OFieldInfo cachedInfo;

    public OField(JavaElement parent, ASTNode node, String name) {
        super(parent, name);
        this.node = node;
    }

    public ASTNode getNode() {
        return node;
    }

    /**
     * The element name node.
     * i.e the node that will be highlighted int the editor when you will select
     * the field in the outline view
     *
     * @return
     */
    public ASTNode getElementNameNode() {
        return node;
    }

    /**
     * @return The field type to display.
     */
    @Override
    public abstract String getTypeSignature();

    @Override
    public Object getElementInfo() throws JavaModelException {
        if (cachedInfo == null) {
            cachedInfo = (OFieldInfo) createElementInfo();
        }
        return cachedInfo;
    }

    @Override
    protected Object createElementInfo() {
        return new OFieldInfo();
    }

    public GroovyCompilationUnit getUnit() {
        if (getParent() instanceof OType) {
            return ((OType) getParent()).getUnit();
        } else {
            return (GroovyCompilationUnit) getParent();
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
     * @created 11 avr. 2011
     */
    public class OFieldInfo extends SourceFieldElementInfo {

        @Override
        public int getNameSourceStart() {
            return getElementNameNode().getStart();
        }

        @Override
        public int getNameSourceEnd() {
            // whitespace is sometimes included at the end of a variable
            // expression,
            // so we are best off calculating the end based on the start and the length
            ASTNode elementNameNode = getElementNameNode();
            return elementNameNode.getEnd() - 1;
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
        public String getTypeSignature() {
            return OField.this.getTypeSignature();
        }
    }

}
