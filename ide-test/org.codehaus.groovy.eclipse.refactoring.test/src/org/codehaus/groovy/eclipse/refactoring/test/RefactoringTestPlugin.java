/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Copied from {@link org.eclipse.jdt.ui.tests.refactoring.infra.RefactoringTestPlugin}
 * 
 * @created Mar 27, 2010
 */
public class RefactoringTestPlugin extends Plugin {

	private static RefactoringTestPlugin fgDefault;

	public RefactoringTestPlugin() {
		fgDefault= this;
	}

	public static RefactoringTestPlugin getDefault() {
		return fgDefault;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public InputStream getTestResourceStream(String fileName) throws IOException {
		IPath path= new Path("resources").append(fileName);
		URL url= new URL(getBundle().getEntry("/"), path.toString());
		return url.openStream();
	}
    public File getFileInPlugin(IPath path) throws CoreException {
        try {
            URL installURL= new URL(getBundle().getEntry("/"), path.toString());
            URL localURL= FileLocator.toFileURL(installURL);
            return new File(localURL.getFile());
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, e.getMessage(), e));
        }
    }
    
    public static String getPluginId() {
        return "org.codehaus.groovy.eclipse.refactoring";
    }


}
