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

package org.codehaus.jdt.groovy.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Andrew Eisenberg
 * @created May 26, 2009
 * 
 *          Performs code select in a Groovy-aware way.
 */
public interface ICodeSelectHelper {

	public IJavaElement[] select(GroovyCompilationUnit unit, int start, int length);

}

class CodeSelectHelperFactory {
	// Inject the code select helper
	private final static String CODE_SELECT_HELPER_EXTENSION = "org.eclipse.jdt.groovy.core.codeSelectHelper";

	static ICodeSelectHelper selectHelper;
	static {

		// Exactly one code select helper is allowed. Do a check for this.
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(CODE_SELECT_HELPER_EXTENSION);
		IExtension[] exts = extPoint.getExtensions();
		if (exts.length < 1) {
			throw new IllegalArgumentException("No Code Select Helper found");
		} else if (exts.length > 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("Too many Code Select Helpers found:\n");
			for (IExtension ext : exts) {
				sb.append("    " + ext.getNamespaceIdentifier() + "." + ext.getSimpleIdentifier());
			}
			throw new IllegalArgumentException(sb.toString());
		}
		IConfigurationElement[] elts = exts[0].getConfigurationElements();
		if (elts.length < 1) {
			throw new IllegalArgumentException("No Code Select Helper found");
		} else if (elts.length > 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("Too many Code Select Helpers found:\n");
			for (IConfigurationElement elt : elts) {
				sb.append("    " + elt.getNamespaceIdentifier());
			}
			throw new IllegalArgumentException(sb.toString());
		}

		// all good. Now, instantiate and assign the code select helper
		try {
			selectHelper = (ICodeSelectHelper) elts[0].createExecutableExtension("class");
		} catch (CoreException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void setSelectHelper(ICodeSelectHelper newSelectHelper) {
		selectHelper = newSelectHelper;
	}
}
