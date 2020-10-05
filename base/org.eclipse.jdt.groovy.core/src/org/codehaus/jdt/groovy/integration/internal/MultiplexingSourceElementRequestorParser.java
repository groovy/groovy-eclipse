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

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementNotifier;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;
import org.eclipse.jdt.internal.core.ImportContainerInfo;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.Util;

public class MultiplexingSourceElementRequestorParser extends SourceElementParser {

    private final GroovyParser groovyParser;

    public MultiplexingSourceElementRequestorParser(final ProblemReporter problemReporter, final ISourceElementRequestor requestor, final IProblemFactory problemFactory, final CompilerOptions options, final boolean reportLocalDeclarations, final boolean optimizeStringLiterals) {
        super(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals);
        groovyParser = new GroovyParser(requestor, this.options, problemReporter, false, true);
        // ensure import annotations are seen by the compilation unit
        if (requestor instanceof CompilationUnitStructureRequestor) {
            final CompilationUnitStructureRequestor compUnitStructureRequestor = (CompilationUnitStructureRequestor) requestor;
            ReflectionUtils.setPrivateField(SourceElementParser.class, "notifier", this, new SourceElementNotifier(requestor, reportLocalDeclarations) {
                @Override
                protected void notifySourceElementRequestor(ImportReference importReference, boolean isPackage) {
                    super.notifySourceElementRequestor(importReference, isPackage);
                    if (!isPackage && importReference.annotations != null) {
                        try {
                            ImportContainerInfo importContainerInfo = ReflectionUtils.getPrivateField(CompilationUnitStructureRequestor.class, "importContainerInfo", compUnitStructureRequestor);
                            for (Annotation annotation : importReference.annotations) {
                                IJavaElement[] imports = ReflectionUtils.throwableExecutePrivateMethod(CompilationUnitStructureRequestor.class, "getChildren", new Class[] {Object.class}, compUnitStructureRequestor, new Object[] {importContainerInfo});

                                //requestor.acceptAnnotation(Annotation annotation, AnnotatableInfo parentInfo, JavaElement parentHandle);
                                ReflectionUtils.throwableExecutePrivateMethod(CompilationUnitStructureRequestor.class, "acceptAnnotation",
                                    new Class[]  {Annotation.class, AnnotatableInfo.class, JavaElement.class}, compUnitStructureRequestor,
                                    new Object[] {annotation,       null,                  imports[imports.length - 1]});
                            }
                        } catch (Throwable t) {
                            Util.log(t);
                        }
                    }
                }
            });
        }
    }

    @Override
    public CompilationUnitDeclaration parseCompilationUnit(final ICompilationUnit compilationUnit, final boolean fullParse, final IProgressMonitor progressMonitor) {
        if (GroovyParser.isGroovyParserEligible(compilationUnit, readManager)) {
            char[] contents = GroovyParser.getContents(compilationUnit, readManager);
            String fileName = CharOperation.charToString(compilationUnit.getFileName());

            // ASSUMPTIONS:
            // 1) parsing is for the entire CU (ie- from character 0 to compilationUnit.getContents().length)
            // 2) nodesToCategories map is not necessary. I think it has something to do with Javadoc, but not sure

            boolean disableGlobalXforms = !fullParse || optimizeStringLiterals;
            // FIXASC Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one.
            // FIXASC ought to reuse to ensure types end up in same groovy CU
            GroovyParser groovyParser = new GroovyParser(this.groovyParser.requestor, options, problemReporter, !disableGlobalXforms, true);
            CompilationResult compilationResult = new CompilationResult(compilationUnit, 0, 0, options.maxProblemsPerUnit);
            GroovyCompilationUnitDeclaration compUnitDecl = groovyParser.dietParse(contents, fileName, compilationResult);

            scanner.setSource(contents);

            SourceElementNotifier notifier = ReflectionUtils.getPrivateField(SourceElementParser.class, "notifier", this);
            notifier.notifySourceElementRequestor(compUnitDecl, 0, scanner.source.length, false, compUnitDecl.sourceEnds, Collections.EMPTY_MAP);

            return compUnitDecl;
        }
        return super.parseCompilationUnit(compilationUnit, fullParse, progressMonitor);
    }

    @Override
    public CompilationUnitDeclaration dietParse(final ICompilationUnit compilationUnit, final CompilationResult compilationResult) {
        if (GroovyParser.isGroovyParserEligible(compilationUnit, readManager)) {
            char[] contents = GroovyParser.getContents(compilationUnit, readManager);
            String fileName = CharOperation.charToString(compilationUnit.getFileName());

            return groovyParser.dietParse(contents, fileName, compilationResult);
        }
        return super.dietParse(compilationUnit, compilationResult);
    }

    @Override
    public void reset() {
        groovyParser.reset();
    }
}
