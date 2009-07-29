/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *     Andrew Eisenberg - Additional work
 *******************************************************************************/
package org.eclipse.jdt.core.groovy.tests.builder;

import org.eclipse.jdt.core.tests.builder.BuilderTests;

/**
 * Extension of the Builder Tests that can use generics.  Adds helpers suitable for groovy projects too.
 * 
 * @author Andy Clement
 *
 */
public class GroovierBuilderTests extends BuilderTests {

	public GroovierBuilderTests(String name) {
		super(name);
	}


	// varargs
	protected void expectingCompiledClassesV(String... compiledClasses) {
		 expectingCompiledClasses(compiledClasses);
	}

}
