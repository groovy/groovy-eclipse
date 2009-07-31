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
/* $Id: GroovySourceLocator.java 5472 2007-03-08 21:40:44Z emp $
 * Created on 31.03.2004
 */
package org.codehaus.groovy.eclipse.launchers;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.ui.JavaUISourceLocator;
import org.eclipse.jdt.launching.sourcelookup.IJavaSourceLocation;
import org.eclipse.jdt.launching.sourcelookup.JavaProjectSourceLocation;

/**
 * @author <a href="mailto:blib@mail.com">Boris Bliznukov</a> 
 */
@SuppressWarnings("deprecation")
public class GroovySourceLocator extends JavaUISourceLocator {

    public static final String ID = "org.codehaus.groovy.eclipse.editor.groovySourceLocator";

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(org.eclipse.debug.core.model.IStackFrame)
     */
    public Object getSourceElement(IStackFrame stackFrame) {
        Object element = super.getSourceElement(stackFrame);
        if (element != null) {
            return element;
        }
        
        // if JDT can't find it, then it is a groovy source file
        // this is likely due to bug 284612 
        // (.java is hard coded in PacakageFragmentRootSourceLocation)
        if (stackFrame instanceof IJavaStackFrame) {
            IJavaStackFrame frame = (IJavaStackFrame) stackFrame;
            try {
                String name = getFullyQualfiedName(frame);
                IJavaSourceLocation[] locations = getSourceLocations();
                for (IJavaSourceLocation location : locations) {
                    // only look at source jars
                    if (location instanceof JavaProjectSourceLocation) {
                        IType type = ((JavaProjectSourceLocation) location).getJavaProject().findType(name, new NullProgressMonitor());
                        if (type != null) {
                            return type.getTypeRoot();
                        }
                    }
                }
                
                
            } catch (CoreException e) {
                GroovyPlugin.getDefault().logException("Failed to get source element", e);
            }
        }
        return null;
    }
    
    private String getFullyQualfiedName(IJavaStackFrame frame) throws CoreException {
        String name = null;
        if (frame.isObsolete()) {
            return null;
        }
        String sourceName = frame.getSourceName();
        if (sourceName == null) {
            // no debug attributes, guess at source name
            name = frame.getDeclaringTypeName();
        } else {
            // build source name from debug attributes using
            // the source file name and the package of the declaring
            // type
                    
            // @see bug# 21518 - remove absolute path prefix
            int index = sourceName.lastIndexOf('\\');
            if (index == -1) {
                index = sourceName.lastIndexOf('/');
            }
            if (index >= 0) {
                sourceName = sourceName.substring(index + 1);
            }
                    
            String declName= frame.getDeclaringTypeName();
            index = declName.lastIndexOf('.');
            if (index >= 0) {
                name = declName.substring(0, index + 1);
            } else {
                name = ""; //$NON-NLS-1$
            }
            index = sourceName.lastIndexOf('.');
            if (index >= 0) {
                name += sourceName.substring(0, index) ;
            }                   
        }
        return name;        
    }
}
