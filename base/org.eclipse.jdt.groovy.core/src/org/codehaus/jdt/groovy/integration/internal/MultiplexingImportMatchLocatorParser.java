/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.eclipse.jdt.internal.core.search.matching.ImportMatchLocatorParser;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

class MultiplexingImportMatchLocatorParser extends ImportMatchLocatorParser {

    private final GroovyParser groovyParser;

    MultiplexingImportMatchLocatorParser(final ProblemReporter problemReporter, final MatchLocator locator) {
        super(problemReporter, locator);
        groovyParser = new GroovyParser(locator.options, problemReporter, false, true);
    }

    @Override
    public CompilationUnitDeclaration dietParse(final ICompilationUnit compilationUnit, final CompilationResult compilationResult) {
        if (compilationUnit instanceof PossibleMatch
                ? ((PossibleMatch) compilationUnit).isInterestingSourceFile()
                : ContentTypeUtils.isGroovyLikeFileName(compilationUnit.getFileName())) {
            return groovyParser.dietParse(compilationUnit, compilationResult);
        } else {
            return super.dietParse(compilationUnit, compilationResult);
        }
    }

    @Override
    public void reset() {
        groovyParser.reset();
    }
}
