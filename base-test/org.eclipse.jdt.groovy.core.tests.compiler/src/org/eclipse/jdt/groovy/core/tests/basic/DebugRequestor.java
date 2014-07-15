/*******************************************************************************
 * Copyright (c) 2014 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SpringSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.groovy.core.tests.basic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.IGroovyDebugRequestor;

class DebugRequestor implements IGroovyDebugRequestor {

	Map declarations;
	Map types;
	
	public DebugRequestor() {
		declarations = new HashMap();
	}

	public void acceptCompilationUnitDeclaration(GroovyCompilationUnitDeclaration gcuDeclaration) {
		System.out.println(gcuDeclaration);
		String filename = new String(gcuDeclaration.getFileName());
		filename=filename.substring(filename.lastIndexOf(File.separator)+1); // Filename now being just X.groovy or Foo.java
		declarations.put(filename,gcuDeclaration);
	}
	
}
