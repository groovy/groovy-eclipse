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
package org.codehaus.groovy.eclipse.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class GroovyClasspathContainerInitializer extends
        ClasspathContainerInitializer {

    public void initialize(IPath containerPath, IJavaProject project)
            throws CoreException {
        IClasspathContainer container = new GroovyClasspathContainer();
        JavaCore.setClasspathContainer(containerPath, 
        		new IJavaProject[] {project}, 
        		new IClasspathContainer[] {container}, null);
    }

}
