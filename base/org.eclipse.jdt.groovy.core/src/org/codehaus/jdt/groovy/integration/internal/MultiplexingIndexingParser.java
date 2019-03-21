/*
 * Copyright 2009-2019 the original author or authors.
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

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
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

public class MultiplexingIndexingParser extends IndexingParser {

    SourceElementNotifier notifier;
    boolean groovyReportReferenceInfo;
    ISourceElementRequestor requestor;

    public MultiplexingIndexingParser(ISourceElementRequestor requestor, IProblemFactory problemFactory, CompilerOptions options,
            boolean reportLocalDeclarations, boolean optimizeStringLiterals, boolean useSourceJavadocParser) {
        super(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, useSourceJavadocParser);
        this.notifier = ReflectionUtils.getPrivateField(SourceElementParser.class, "notifier", this);
        this.groovyReportReferenceInfo = reportLocalDeclarations;
        this.requestor = requestor;
    }

    @Override
    public void setRequestor(ISourceElementRequestor requestor) {
        super.setRequestor(requestor);
        this.requestor = requestor;
    }

    @Override
    public CompilationUnitDeclaration parseCompilationUnit(ICompilationUnit unit, boolean fullParse, IProgressMonitor pm) {
        if (!ContentTypeUtils.isGroovyLikeFileName(unit.getFileName())) {
            return super.parseCompilationUnit(unit, fullParse, pm);
        } else {
            // ASSUMPTIONS:
            // 1) there is no difference between a diet and full parse in the groovy works, so can ignore the fullParse parameter
            // 2) parsing is for the entire CU (ie- from character 0, to unit.getContents().length)
            // 3) nodesToCategories map is not necessary. I think it has something to do with JavaDoc, but not sure

            CompilationResult compilationResult = new CompilationResult(unit, 0, 0, options.maxProblemsPerUnit);

            // FIXASC Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one
            GroovyCompilationUnitDeclaration cud = new GroovyParser(options, problemReporter, false, true).dietParse(unit, compilationResult);

            if (cud.getModuleNode() != null) {
                try {
                    GroovyIndexingVisitor visitor = new GroovyIndexingVisitor(requestor);
                    visitor.visitModule(cud.getModuleNode());
                } catch (RuntimeException e) {
                    Util.log(e);
                }
            }

            notifier.notifySourceElementRequestor(cud, 0, unit.getContents().length, groovyReportReferenceInfo, cud.sourceEnds, Collections.EMPTY_MAP);
            return cud;
        }
    }
}
