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

import static org.eclipse.jdt.groovy.core.util.ContentTypeUtils.isGroovyLikeFileName;
import static org.eclipse.jdt.groovy.core.util.ContentTypeUtils.isJavaLikeButNotGroovyLikeFileName;

import java.util.regex.Pattern;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.jdt.groovy.core.util.CharArraySequence;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.Util;

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
        if (isGroovyLikeFileName(compilationUnit.getFileName()) || isGroovySource(compilationUnit)) {
            if (this.scanner != null) {
                this.scanner.setSource(compilationUnit.getContents());
            }
            return groovyParser.dietParse(compilationUnit, compilationResult);
        }
        return super.dietParse(compilationUnit, compilationResult);
    }

    private static boolean isGroovySource(final ICompilationUnit compilationUnit) {
        if (compilationUnit.getFileName() == null || !isJavaLikeButNotGroovyLikeFileName(new CharArraySequence(compilationUnit.getFileName()))) {
            if (GROOVY_SOURCE_DISCRIMINATOR.matcher(new CharArraySequence(compilationUnit.getContents())).find()) {
                Util.log(1, "Identified a Groovy source unit through inspection of its contents: " +
                    String.valueOf(compilationUnit.getContents(), 0, Math.min(250, compilationUnit.getContents().length)));
                return true;
            }
        }
        return false;
    }

    private static final Pattern GROOVY_SOURCE_DISCRIMINATOR = Pattern.compile("\\A(/\\*.*?\\*/\\s*)?package\\s+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*(?:\\s*\\.\\s*\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)*\\s++(?!;)", Pattern.DOTALL);

    @Override
    public void reset() {
        groovyParser.reset();
    }
}
