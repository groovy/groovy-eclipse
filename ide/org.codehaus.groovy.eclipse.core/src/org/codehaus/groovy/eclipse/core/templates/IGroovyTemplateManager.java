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
/**
 * 
 */
package org.codehaus.groovy.eclipse.core.templates;

import java.io.IOException;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Implementation of this interface can execute templates to generate new Groovy
 * classes or tests.
 * 
 * @author Thorsten Kamann <thorsten.kamann@googlemail.com>
 */
public interface IGroovyTemplateManager {

	/**
	 * Processes the underlying template
	 * 
	 * @param bindings
	 *            The bindings to use in the template
	 * @param progressMonitor
	 *            A ProgressMonitor to visualize the progress
	 * @return The result of the processed template
	 */
	public String processTemplate(Map< String, Object > bindings, IProgressMonitor progressMonitor)
			throws CompilationFailedException, IOException;
}
