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
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;

/**
 * The multiplexing parser can delegate file parsing to multiple parsers. In this scenario it subtypes 'Parser' (which is the Java
 * parser) but is also aware of a groovy parser. Depending on what kind of file is to be parsed, it will invoke the relevant parser.
 * 
 * @author Andy Clement
 */
@SuppressWarnings("restriction")
public class MultiplexingCommentRecorderParser extends CommentRecorderParser {

	GroovyParser groovyParser;

	// FIXASC (M2) how often is the LanguageSupport impl looked up? should be once then
	// we remember what happened
	public MultiplexingCommentRecorderParser(CompilerOptions compilerOptions, ProblemReporter problemReporter,
			boolean optimizeStringLiterals) {
		super(problemReporter, optimizeStringLiterals);
		// The superclass that is extended is in charge of parsing .java files
		groovyParser = new GroovyParser(compilerOptions, problemReporter);
	}

	@Override
	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
		if (ContentTypeUtils.isGroovyLikeFileName(sourceUnit.getFileName())) {
			// FIXASC (M2) Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one
			// FIXASC (M2) ought to reuse to ensure types end up in same groovy CU
			 return new GroovyParser(this.groovyParser.getCompilerOptions(), this.groovyParser.problemReporter).dietParse(
			 sourceUnit, compilationResult);
//			return groovyParser.dietParse(sourceUnit, compilationResult);
		} else {
			return super.dietParse(sourceUnit, compilationResult);
		}
	}

	@Override
	public void reset() {
		groovyParser.reset();
	}
}
