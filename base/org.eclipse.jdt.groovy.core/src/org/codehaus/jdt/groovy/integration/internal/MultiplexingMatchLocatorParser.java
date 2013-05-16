/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.jdt.groovy.integration.internal;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

/**
 * @author Andrew Eisenberg
 * @created Nov 17, 2009
 * 
 */
public class MultiplexingMatchLocatorParser extends MatchLocatorParser {
	GroovyParser groovyParser;

	protected MultiplexingMatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
		super(problemReporter, locator);
		// The superclass that is extended is in charge of parsing .java files
		groovyParser = new GroovyParser(locator.options, problemReporter, false, true);
	}

	@Override
	public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
		if (sourceUnit instanceof PossibleMatch ? ((PossibleMatch) sourceUnit).isInterestingSourceFile() : ContentTypeUtils
				.isGroovyLikeFileName(sourceUnit.getFileName())) {
			// FIXASC Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one
			// FIXASC ought to reuse to ensure types end up in same groovy CU
			return new GroovyParser(this.groovyParser.getCompilerOptions(), this.groovyParser.problemReporter, false, true)
					.dietParse(sourceUnit, compilationResult);
			// return groovyParser.dietParse(sourceUnit, compilationResult);
		} else {
			return super.dietParse(sourceUnit, compilationResult);
		}
	}

}
