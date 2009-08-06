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
