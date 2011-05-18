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
package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * The multiplexing parser can delegate file parsing to multiple parsers. In this scenario it subtypes 'Parser' (which is the Java
 * parser) but is also aware of a groovy parser. Depending on what kind of file is to be parsed, it will invoke the relevant parser.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class MultiplexingParser extends Parser {

	GroovyParser groovyParser;

	public MultiplexingParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter,
			boolean optimizeStringLiterals) {
		super(problemReporter, optimizeStringLiterals);
		// The superclass that is extended is in charge of parsing .java files
		groovyParser = new GroovyParser(requestor, compilerOptions, problemReporter, true, false);
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
		groovyParser.reset();
	}
}
