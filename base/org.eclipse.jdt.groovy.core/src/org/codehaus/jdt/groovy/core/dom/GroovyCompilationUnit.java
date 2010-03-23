/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement     - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.core.dom;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class GroovyCompilationUnit extends CompilationUnit {

	public GroovyCompilationUnit(AST ast) {
		super(ast);
	}

	/**
	 * sneaky... required because we cannot get in the jdt.debug code and change it. Here the plan is that if the debug
	 * MethodSearchVisitor is being used we don't find the method. If it was found the body would be empty and the debug code would
	 * think the method has not changed across builds - when it may have. It is empty because we don't parse below the method level
	 * for groovy.
	 * 
	 */
	@Override
	protected void accept0(ASTVisitor visitor) {
		String visitorName = visitor.getClass().getName();
		if (visitorName.equals("org.eclipse.jdt.internal.debug.core.hcr.MethodSearchVisitor")) {
			// String message =
			// "Cannot correctly answer the findMethod() call for Groovy code whilst debugging, the method AST will be empty";
			// throw new CoreException(new Status(IStatus.CANCEL, Activator.PLUGIN_ID, IStatus.OK, message, null));
			return;
		}
		super.accept0(visitor);
	}
}
