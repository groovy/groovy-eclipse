/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.junit.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 */
public class JUnitTestCase extends BuilderTests {

    public JUnitTestCase(String name) {
        super(name);
    }
    
    protected IPath createGenericProject() throws Exception {
        IPath projectPath = env.addProject("Project", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
        // remove old package fragment root so that names don't collide
        env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
        env.addGroovyNature("Project");
        env.addExternalJars(projectPath, Util.getJavaClassLibs());
        env.addJUnitJar(projectPath);
        env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
        env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
        return projectPath;
    }

    protected IFile getFile(IPath projectPath, String fileName) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(projectPath.append(fileName));
    }

    protected IFolder getolder(IPath projectPath, String folderName) {
        return ResourcesPlugin.getWorkspace().getRoot().getFolder(projectPath.append(folderName));
    }

    protected IProject getProject(IPath projectPath) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath.segment(0));
    }

}