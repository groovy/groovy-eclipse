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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;

/*
 * Validates the raw classpath format and the resolved classpath of this project,
 * updating markers if necessary.
 */
public class ClasspathValidation {

	private JavaProject project;

	public ClasspathValidation(JavaProject project) {
		this.project = project;
	}

	public void validate() {
		JavaModelManager.PerProjectInfo perProjectInfo;
		try {
			perProjectInfo = this.project.getPerProjectInfo();
		} catch (JavaModelException e) {
			// project doesn't exist
			IProject resource = this.project.getProject();
			if (resource.isAccessible()) {
				this.project.flushClasspathProblemMarkers(true/*flush cycle markers*/, true/*flush classpath format markers*/);

				// remove problems and tasks created  by the builder
				JavaBuilder.removeProblemsAndTasksFor(resource);
			}
			return;
		}

		// use synchronized block to ensure consistency
		IClasspathEntry[] rawClasspath;
		IPath outputLocation;
		IJavaModelStatus status;
		synchronized (perProjectInfo) {
			rawClasspath = perProjectInfo.rawClasspath;
			outputLocation = perProjectInfo.outputLocation;
			status = perProjectInfo.rawClasspathStatus; // status has been set during POST_CHANGE
		}

		// update classpath format problems
		this.project.flushClasspathProblemMarkers(false/*cycle*/, true/*format*/);
		if (!status.isOK())
			this.project.createClasspathProblemMarker(status);

		// update resolved classpath problems
		this.project.flushClasspathProblemMarkers(false/*cycle*/, false/*format*/);

		if (rawClasspath != JavaProject.INVALID_CLASSPATH && outputLocation != null) {
		 	for (int i = 0; i < rawClasspath.length; i++) {
				status = ClasspathEntry.validateClasspathEntry(this.project, rawClasspath[i], false/*src attach*/, false /*not referred by a container*/);
				if (!status.isOK()) {
					this.project.createClasspathProblemMarker(status);
				}
			 }
			status = ClasspathEntry.validateClasspath(this.project, rawClasspath, outputLocation);
			if (!status.isOK())
				this.project.createClasspathProblemMarker(status);
		 }
	}

}
