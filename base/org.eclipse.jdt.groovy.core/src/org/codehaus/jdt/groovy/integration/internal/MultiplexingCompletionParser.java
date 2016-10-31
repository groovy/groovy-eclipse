/*
 * Copyright 2009-2016 the original author or authors.
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

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class MultiplexingCompletionParser extends CompletionParser {

    private GroovyParser groovyParser;

    public MultiplexingCompletionParser(CompilerOptions compilerOptions, ProblemReporter problemReporter,
            boolean storeExtraSourceEnds, IProgressMonitor monitor) {
        super(problemReporter, storeExtraSourceEnds);
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
    public void parseBlockStatements(ConstructorDeclaration cd, CompilationUnitDeclaration unit) {
        if (!(unit instanceof GroovyCompilationUnitDeclaration)) {
            super.parseBlockStatements(cd, unit);
        }
    }

    @Override
    public MethodDeclaration parseSomeStatements(int start, int end, int fakeBlocksCount, CompilationUnitDeclaration unit) {
        if (!(unit instanceof GroovyCompilationUnitDeclaration)) {
            return super.parseSomeStatements(start, end, fakeBlocksCount, unit);
        }
        return null;
    }

    @Override
    public void parseBlockStatements(AbstractMethodDeclaration md, CompilationUnitDeclaration unit) {
        if (!(unit instanceof GroovyCompilationUnitDeclaration)) {
            super.parseBlockStatements(md, unit);
        }
    }

    @Override
    public void parseBlockStatements(Initializer initializer, TypeDeclaration type, CompilationUnitDeclaration unit) {
        if (!(unit instanceof GroovyCompilationUnitDeclaration)) {
            super.parseBlockStatements(initializer, type, unit);
        }
    }

    @Override
    public void parseBlockStatements(MethodDeclaration md, CompilationUnitDeclaration unit) {
        if (!(unit instanceof GroovyCompilationUnitDeclaration)) {
            super.parseBlockStatements(md, unit);
        }
    }

    @Override
    public void reset() {
        super.reset();
        groovyParser.reset();
    }
}
