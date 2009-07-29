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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.messages.Message;

/**
 * A subtype of the Groovy ErrorCollector that can be made more JDT friendly (not throwing exceptions when errors occur and doing
 * nicer mapping from errors to JDT problems). Still much to be done with this.
 * 
 * @author Andy Clement
 */
public class GroovyErrorCollectorForJDT extends ErrorCollector {

	public GroovyErrorCollectorForJDT(CompilerConfiguration configuration) {
		super(configuration);
	}

	@Override
	public void addErrorAndContinue(Message message) {
		System.err.println(message);
		super.addErrorAndContinue(message);
	}

	@Override
	protected void failIfErrors() throws CompilationFailedException {
		// super.failIfErrors();
	}

}
