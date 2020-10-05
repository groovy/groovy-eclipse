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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;

/**
 * The multiplexing parser can delegate file parsing to multiple parsers. In this scenario it subtypes 'Parser' (which is the Java
 * parser) but is also aware of a groovy parser. Depending on what kind of file is to be parsed, it will invoke the relevant parser.
 */
class MultiplexingCommentRecorderParser extends CommentRecorderParser {

    private final GroovyParser groovyParser;

    MultiplexingCommentRecorderParser(final Object requestor, final CompilerOptions compilerOptions, final ProblemReporter problemReporter, final boolean optimizeStringLiterals) {
        this(requestor, compilerOptions, problemReporter, optimizeStringLiterals, true);
    }

    MultiplexingCommentRecorderParser(final Object requestor, final CompilerOptions compilerOptions, final ProblemReporter problemReporter, final boolean optimizeStringLiterals, final boolean allowTransforms) {
        super(problemReporter, optimizeStringLiterals);
        groovyParser = new GroovyParser(requestor, compilerOptions, problemReporter, allowTransforms, true);
    }

    @Override
    public CompilationUnitDeclaration dietParse(final ICompilationUnit compilationUnit, final CompilationResult compilationResult) {
        if (GroovyParser.isGroovyParserEligible(compilationUnit, readManager)) {
            char[] contents = GroovyParser.getContents(compilationUnit, readManager);
            String fileName = CharOperation.charToString(compilationUnit.getFileName());

            if (scanner != null) scanner.setSource(contents);
            return groovyParser.dietParse(contents, fileName, compilationResult);
        }
        return super.dietParse(compilationUnit, compilationResult);
    }

    @Override
    public void reset() {
        groovyParser.reset();
    }
}
