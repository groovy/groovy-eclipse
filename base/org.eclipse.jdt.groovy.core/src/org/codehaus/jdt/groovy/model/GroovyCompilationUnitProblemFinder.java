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

import org.codehaus.jdt.groovy.integration.LanguageSupportFactory;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;


/**
 * @author Andrew Eisenberg
 * @created Jun 16, 2009 This stub class exists so that we can have access to the constructor of
 *          {@link CompilationUnitProblemFinder}.
 */
@SuppressWarnings("restriction")
public class GroovyCompilationUnitProblemFinder extends CompilationUnitProblemFinder {

	protected GroovyCompilationUnitProblemFinder(INameEnvironment environment, IErrorHandlingPolicy policy,
			CompilerOptions compilerOptions, ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, compilerOptions, requestor, problemFactory);
	}

	@Override
	public void initializeParser() {
		this.parser = LanguageSupportFactory.getParser(this.lookupEnvironment, this.problemReporter,
				this.options.parseLiteralExpressionsAsConstants, 1);
	}
}
