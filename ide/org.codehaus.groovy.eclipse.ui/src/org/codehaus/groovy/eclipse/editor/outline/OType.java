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

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

/**
 * @author maxime
 * @created 1 avr. 2011
 */
public class OType extends SourceType implements IOJavaElement {

    protected ASTNode node;

    private List<IMember> children = new LinkedList<IMember>();

    private OTypeInfo cachedInfo;

    /**
     * @param parent the parent type
     * @param node the node that matches the whole type source source (not
     *            only the type name but also the type body)
     * @param name the type name
     */
    public OType(IJavaElement parent, ASTNode node, String name) {
        super((JavaElement) parent, name);
        this.node = node;
    }

    /**
     * The element name node
     * i.e the node that will be highlighted int the editor when you will select
     * the type in the outline view
     */
    public ASTNode getElementNameNode() {
        return node;
    }

    @Override
    public Object getElementInfo() throws JavaModelException {
        if (cachedInfo == null) {
            cachedInfo = (OTypeInfo) createElementInfo();
        }
        return cachedInfo;
    }

    @Override
    protected Object createElementInfo() {
        return new OTypeInfo();
    }

    public GroovyCompilationUnit getUnit() {
        if (getParent() instanceof IType) {
            return (GroovyCompilationUnit) ((IType) getParent()).getTypeRoot();
        } else {
            return (GroovyCompilationUnit) getParent();
        }
    }

    public ASTNode getNode() {
        return node;
    }

    @Override
    public IMember[] getChildren() throws JavaModelException {
        return this.children.toArray(new IMember[] {});
    }

    public List<IMember> getChildrenList() {
        return children;
    }

    public void addChild(IMember child) {
        children.add(child);
    }

    /**
     * get the mock method link to active carret offset
     *
     * @param caretOffset
     * @return
     */
    public IMember getOutlineElementAt(int caretOffset) {
        try {
            if (!hasChildren()) {
                return this;
            }
            for (IMember je : getChildren()) {
                if (je instanceof IOJavaElement) {
                    IOJavaElement m = (IOJavaElement) je;
                    ASTNode n = m.getNode();
                    if (n.getStart() <= caretOffset && n.getEnd() >= caretOffset) {
                        if (m instanceof OType) {
                            return ((OType) je).getOutlineElementAt(caretOffset);
                        } else {
                            return (IMember) m;
                        }
                    }
                } else {
                    // call getSourceElementAt()
                }
            }
        } catch (JavaModelException e) {
            GroovyCore.logException(e.getMessage(), e);
        }
        return this;
    }

    @Override
    public String[] getCategories() throws JavaModelException {
        // categories not supported
        //      if (exists()) {
        //          return super.getCategories();
        //      }
        return NO_CATEGORIES;
    }

    /****************************************************************************
     * @author maxime
     * @created 3 avr. 2011
     */
    public class OTypeInfo extends SourceTypeElementInfo {

        @Override
        public int getNameSourceStart() {
            return getElementNameNode().getStart();
        }

        @Override
        public int getNameSourceEnd() {
            return getElementNameNode().getEnd();
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
    }


}
