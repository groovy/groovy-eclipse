/*
 * Copyright 2003-2010 the original author or authors.
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

import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 *
 * @author maxime
 * @created 1 avr. 2011
 */
public abstract class OCompilationUnit extends GroovyCompilationUnit implements IOJavaElement {

    private GroovyCompilationUnit unit;

    private IJavaElement[] children = null;

    public OCompilationUnit(GroovyCompilationUnit unit) {
        super((PackageFragment) unit.getParent(), unit.getElementName(), unit.getOwner());
        this.unit = unit;
        this.owner = unit.owner;
        // refresh();
    }

    /**
     * refresh children list
     *
     * @return
     */
    public abstract IJavaElement[] refreshChildren();

    /**
     * get the mock method link to active carret offset. Defaults to
     * {@link #getElementAt(int)}
     *
     * Can override to provide domain specific information here
     *
     * @param caretOffset
     * @return
     */
    public ISourceReference getOutlineElementAt(int caretOffset) {
        try {
            IJavaElement elementAt = getElementAt(caretOffset);
            return (elementAt instanceof ISourceReference) ? (ISourceReference) elementAt : this;
        } catch (JavaModelException e) {
            // ignore this. seems that this happens when there is a parsing
            // error
            // GroovyCore.logException("Exception when finding child elements",
            // e);
            return this;
        }
    }

    /**
     * get groovy node linked to this elemen
     */
    public ASTNode getNode() {
        return unit.getModuleNode();
    }

    /**
     * refresh children
     */
    protected void refresh() {
        if (this.exists()) {
            this.children = refreshChildren();
        }
    }

    /**
     * get children
     */
    @Override
    public IJavaElement[] getChildren() {
        if (children == null) {
            refresh();
        }
        if (children == null) {
            return new IJavaElement[0];
        }
        return children;
    }

    /**
     * get groovy compilation unit
     *
     * @return
     */
    public GroovyCompilationUnit getUnit() {
        return unit;
    }

    /**
     * This method will probably never get called, but if it ever is by
     * accident, it could cause some big problems with the mode.
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
            throws JavaModelException {
        return true;
    }
}
