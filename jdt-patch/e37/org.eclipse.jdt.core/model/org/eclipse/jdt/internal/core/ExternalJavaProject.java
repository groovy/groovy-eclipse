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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ExternalJavaProject extends JavaProject {

	/*
	 * Note this name can be surfaced in the UI (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=128258)
	 */
	public static final String EXTERNAL_PROJECT_NAME = " "; //$NON-NLS-1$

	public ExternalJavaProject(IClasspathEntry[] rawClasspath) {
		super(ResourcesPlugin.getWorkspace().getRoot().getProject(EXTERNAL_PROJECT_NAME), JavaModelManager.getJavaModelManager().getJavaModel());
		try {
			getPerProjectInfo().setRawClasspath(rawClasspath, defaultOutputLocation(), JavaModelStatus.VERIFIED_OK/*no .classpath format problem*/);
		} catch (JavaModelException e) {
			// getPerProjectInfo() never throws JavaModelException for an ExternalJavaProject
		}
	}

	public boolean equals(Object o) {
		return this == o;
	}

	public boolean exists() {
		// external project never exists
		return false;
	}

	public String getOption(String optionName, boolean inheritJavaCoreOptions) {
		if (JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE.equals(optionName)
				|| JavaCore.COMPILER_PB_DISCOURAGED_REFERENCE.equals(optionName))
			return JavaCore.IGNORE;
		return super.getOption(optionName, inheritJavaCoreOptions);
	}

	public boolean isOnClasspath(IJavaElement element) {
		// since project is external, no element is on classpath (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=61013#c16)
		return false;
	}

	public boolean isOnClasspath(IResource resource) {
		// since project is external, no resource is on classpath (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=61013#c16)
		return false;
	}

	protected IStatus validateExistence(IResource underlyingResource) {
		// allow opening of external project
		return JavaModelStatus.VERIFIED_OK;
	}
}
