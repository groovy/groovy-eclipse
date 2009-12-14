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
package org.codehaus.groovy.eclipse.codebrowsing.adapter;

import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;

@SuppressWarnings("unchecked")
public class CodebrowsingEditorAdapterFactory implements IAdapterFactory {
private final Class[] adapters = new Class[]{IJavaElement.class};
  
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (!(adaptableObject instanceof GroovyEditor || adaptableObject instanceof GroovyCompilationUnit)) { 
        throw new IllegalArgumentException("GroovyEditor or GroovyCompilationUnit expected");
    }
    if (adaptableObject instanceof GroovyEditor) {
        IFile file;
        file = (IFile) ((GroovyEditor) adaptableObject).getEditorInput().getAdapter(IFile.class);
        if (file==null) {
            return null;
        }
        return JavaCore.createCompilationUnitFrom(file);
    } else {
        return null;
    }
  }

  public Class[] getAdapterList() {
    return adapters;
  }
  
}
