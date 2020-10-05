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

import java.util.Collections;
import java.util.Optional;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementNotifier;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;
import org.eclipse.jdt.internal.core.util.Util;

class MultiplexingIndexingParser extends IndexingParser {

    private final SourceElementNotifier notifier;
    private final boolean reportReferenceInfo;
    private ISourceElementRequestor requestor;

    MultiplexingIndexingParser(final ISourceElementRequestor requestor, final IProblemFactory problemFactory, final CompilerOptions options, final boolean reportLocalDeclarations, final boolean optimizeStringLiterals, final boolean useSourceJavadocParser) {
        super(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
        this.notifier = ReflectionUtils.getPrivateField(SourceElementParser.class, "notifier", this);
        this.reportReferenceInfo = reportLocalDeclarations;
        this.requestor = requestor;
    }

    @Override
    public void setRequestor(final ISourceElementRequestor requestor) {
        super.setRequestor(requestor);
        this.requestor = requestor;
    }

    @Override
    public CompilationUnitDeclaration parseCompilationUnit(final ICompilationUnit compilationUnit, final boolean fullParse, final IProgressMonitor pm) {
        if (GroovyParser.isGroovyParserEligible(compilationUnit, readManager)) {
            // ASSUMPTIONS:
            // 1) parsing is for the entire CU (ie- from character 0 to compilationUnit.getContents().length)
            // 2) nodesToCategories map is not necessary. I think it has something to do with JavaDoc, but not sure
            // 3) there is no difference between a diet and full parse in the groovy works, so can ignore the fullParse parameter

            char[] contents = GroovyParser.getContents(compilationUnit, readManager);
            String fileName = CharOperation.charToString(compilationUnit.getFileName());
            CompilationResult compilationResult = new CompilationResult(compilationUnit, 0, 0, options.maxProblemsPerUnit);
            GroovyCompilationUnitDeclaration gcud = new GroovyParser(options, problemReporter, false, true).dietParse(contents, fileName, compilationResult);

            Optional.ofNullable(gcud.getModuleNode()).ifPresent(module -> {
                try {
                    new GroovyIndexingVisitor(requestor).visitModule(module);
                } catch (RuntimeException e) {
                    Util.log(e);
                }
            });

            notifier.notifySourceElementRequestor(gcud, 0, contents.length, reportReferenceInfo, gcud.sourceEnds, Collections.EMPTY_MAP);

            return gcud;
        }
        return super.parseCompilationUnit(compilationUnit, fullParse, pm);
    }
}
