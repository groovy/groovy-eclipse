/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.adapters;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

public class GroovyEditorAdapterFactory implements IAdapterFactory {
    private static final Class<?>[] classes = new Class[] { ModuleNode.class };

    @SuppressWarnings("unchecked")
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof GroovyEditor)) {
            throw new IllegalArgumentException(
                    "adaptable is not the GroovyEditor");
        }

        Object adapter = ((GroovyEditor) adaptableObject).getEditorInput()
                .getAdapter(adapterType);
        if (adapter != null)
            return adapter;

        IFile file = (IFile) ((GroovyEditor) adaptableObject).getEditorInput()
                .getAdapter(IFile.class);
        if (file != null) {
            return adaptFromFile(adapterType, file);
        }
        return null;
    }

    private Object adaptFromFile(Class<?> adapterType, IFile file) {
        if (adapterType.isAssignableFrom(ModuleNode.class)) {
            return getModuleNodeFromFile(file);
        }
        return null;
    }

    private static ModuleNode getModuleNodeFromFile(IFile file) {
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (unit instanceof GroovyCompilationUnit) {
            return ((GroovyCompilationUnit) unit).getModuleNode();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        return classes;
    }
}
