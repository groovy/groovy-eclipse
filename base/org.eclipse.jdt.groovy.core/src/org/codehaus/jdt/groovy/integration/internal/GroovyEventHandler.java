/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.jdt.groovy.integration.EventHandler;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.internal.core.JavaProject;

@SuppressWarnings("restriction")
public class GroovyEventHandler implements EventHandler {

	// Recognized events:
	//
	//
	// "cleanOutputFolders" - called when a clean occurs (either when forced or when part of a full build)
	// "close" - called when a java project is closed
	public void handle(JavaProject javaProject, String event) {
		if (event.equals("cleanOutputFolders")) {
			if (javaProject != null) {
				GroovyParser.tidyCache(javaProject.getProject().getName());
			}
		} else if (event.equals("close")) {
			if (javaProject != null) {
				String projectName = javaProject.getProject().getName();
				GroovyParser.closeClassLoader(projectName);
				GroovyParser.tidyCache(projectName);
			}
		}
	}

}
