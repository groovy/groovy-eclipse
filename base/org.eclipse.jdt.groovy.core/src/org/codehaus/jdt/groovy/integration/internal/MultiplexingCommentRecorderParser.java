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
public class MultiplexingCommentRecorderParser extends CommentRecorderParser {

    GroovyParser groovyParser;
    private boolean allowTransforms = true;

    // FIXASC how often is the LanguageSupport impl looked up? should be once then
    // we remember what happened
    public MultiplexingCommentRecorderParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter,
            boolean optimizeStringLiterals, boolean allowTransforms) {
        super(problemReporter, optimizeStringLiterals);
        // The superclass that is extended is in charge of parsing .java files
        groovyParser = new GroovyParser(requestor, compilerOptions, problemReporter, allowTransforms, true);
        this.allowTransforms = allowTransforms;
    }

    public MultiplexingCommentRecorderParser(Object requestor, CompilerOptions compilerOptions, ProblemReporter problemReporter,
            boolean optimizeStringLiterals) {
        this(requestor, compilerOptions, problemReporter, optimizeStringLiterals, true);
    }

    @Override
    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        if (ContentTypeUtils.isGroovyLikeFileName(sourceUnit.getFileName())) {
            // even though the Java scanner is not used, its contents can be asked for.
            if (this.scanner != null) {
                this.scanner.setSource(sourceUnit.getContents());
            }
            // FIXASC Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one
            // FIXASC ought to reuse to ensure types end up in same groovy CU
            return new GroovyParser(this.groovyParser.requestor, this.groovyParser.getCompilerOptions(),
                    this.groovyParser.problemReporter, allowTransforms, true).dietParse(sourceUnit, compilationResult);
            // return groovyParser.dietParse(sourceUnit, compilationResult);
        } else {
            return super.dietParse(sourceUnit, compilationResult);
        }
    }

    @Override
    public void reset() {
        groovyParser.reset();
    }
}
