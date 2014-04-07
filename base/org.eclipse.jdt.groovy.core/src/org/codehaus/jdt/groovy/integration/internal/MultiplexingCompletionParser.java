/*******************************************************************************
 * Copyright (c) 2014 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Boyko        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class MultiplexingCompletionParser extends CompletionParser {

	private GroovyParser groovyParser;

	public MultiplexingCompletionParser(CompilerOptions compilerOptions, ProblemReporter problemReporter,
			boolean storeExtraSourceEnds, IProgressMonitor monitor) {
		super(problemReporter, storeExtraSourceEnds, monitor);
		// The superclass that is extended is in charge of parsing .java files
		groovyParser = new GroovyParser(compilerOptions, problemReporter, true, false);
	}

	@Override
	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
		if (ContentTypeUtils.isGroovyLikeFileName(sourceUnit.getFileName())) {
			return groovyParser.dietParse(sourceUnit, compilationResult);
		} else {
			return super.dietParse(sourceUnit, compilationResult);
		}
	}

	@Override
	public void reset() {
		super.reset();
		groovyParser.reset();
	}

}
