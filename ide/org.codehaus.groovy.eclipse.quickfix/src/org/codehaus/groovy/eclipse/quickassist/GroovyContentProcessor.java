/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

public class GroovyContentProcessor {

	/**
	 * True if the problem is contained in an accessible (open and existing)
	 * Groovy project in the workspace. False otherwise.
	 * 
	 * @param unit
	 *            compilation unit containing the resource with the problem
	 * @return true if and only if the problem is contained in an accessible
	 *         Groovy project. False otherwise
	 */
	protected boolean isProblemInGroovyProject(IInvocationContext context) {
		if (context == null) {
			return false;
		}
		return isContentInGroovyProject(context.getCompilationUnit());
	}

	/**
	 * True if the problem is contained in an accessible (open and existing)
	 * Groovy project in the workspace. False otherwise.
	 * 
	 * @param unit
	 *            compilation unit containing the resource with the problem
	 * @return true if and only if the problem is contained in an accessible
	 *         Groovy project. False otherwise
	 */
	protected boolean isContentInGroovyProject(ICompilationUnit unit) {

		if (unit != null) {
			IResource resource = unit.getResource();
			if (resource != null) {
				IProject project = resource.getProject();
				if (project != null && project.isAccessible()
						&& GroovyNature.hasGroovyNature(project)) {
					return true;
				}
			}
		}
		return false;
	}
	
	
}
