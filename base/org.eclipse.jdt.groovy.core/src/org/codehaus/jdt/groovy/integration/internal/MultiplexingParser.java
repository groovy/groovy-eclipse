/*
 * Copyright 2009-2017 the original author or authors.
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * The multiplexing parser can delegate file parsing to multiple parsers. In this scenario it subtypes 'Parser' (which is the Java
 * parser) but is also aware of a groovy parser. Depending on what kind of file is to be parsed, it will invoke the relevant parser.
 */
public class MultiplexingParser extends Parser {

    private Object requestor;
    private CompilerOptions compilerOptions;
    private GroovyParser groovyParser;

    public MultiplexingParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter,
            boolean optimizeStringLiterals) {
        // The superclass that is extended is in charge of parsing .java files
        super(problemReporter, optimizeStringLiterals);
        this.requestor = requestor;
        this.compilerOptions = compilerOptions;
    }

    @Override
    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        if (ContentTypeUtils.isGroovyLikeFileName(sourceUnit.getFileName())) {
            if (groovyParser == null) {
                groovyParser = new GroovyParser(this.requestor, this.compilerOptions, this.problemReporter, true, false);
            }
            return groovyParser.dietParse(sourceUnit, compilationResult);
        } else {
            return super.dietParse(sourceUnit, compilationResult);
        }
    }

    @Override
    public void reset() {
        groovyParser = null;
    }
}
