/*
 * Copyright 2009-2016 the original author or authors.
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

    public static String PLUGIN_ID = "org.codehaus.groovy.eclipse.refactoring.test";
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
        return PLUGIN_ID;
    }


}
