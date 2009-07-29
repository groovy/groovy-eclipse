/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
