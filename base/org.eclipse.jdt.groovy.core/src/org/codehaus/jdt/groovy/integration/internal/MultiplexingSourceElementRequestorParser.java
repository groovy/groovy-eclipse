/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.Collections;

import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyParser;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.core.util.ContentTypeUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementNotifier;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.core.AnnotatableInfo;
import org.eclipse.jdt.internal.core.CompilationUnitStructureRequestor;
import org.eclipse.jdt.internal.core.ImportContainerInfo;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * The multiplexing parser can delegate source parsing to multiple parsers.
 * In this scenario it subtypes 'Parser' (which is the Java parser) but is
 * also aware of a groovy parser. Depending on what kind of file is to be
 * parsed, it will invoke the relevant parser.
 */
public class MultiplexingSourceElementRequestorParser extends SourceElementParser {

    private GroovyParser groovyParser;
    private boolean groovyReportReferenceInfo;

    public MultiplexingSourceElementRequestorParser(
        ProblemReporter problemReporter,
        ISourceElementRequestor requestor,
        IProblemFactory problemFactory,
        CompilerOptions options,
        boolean reportLocalDeclarations,
        boolean optimizeStringLiterals
    ) {
        // the superclass that is extended is in charge of parsing .java files
        super(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals);
        this.groovyParser = new GroovyParser(requestor, this.options, problemReporter, false, true);

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
    public void reset() {
        groovyParser.reset();
    }

    @Override
    public CompilationUnitDeclaration parseCompilationUnit(ICompilationUnit unit, boolean fullParse, IProgressMonitor pm) {
        if (ContentTypeUtils.isGroovyLikeFileName(unit.getFileName())) {
            // ASSUMPTIONS:
            // 1) there is no difference between a diet and full parse in the groovy works, so can ignore the fullParse parameter
            // 2) parsing is for the entire CU (ie- from character 0, to unit.getContents().length)
            // 3) nodesToCategories map is not necessary. I think it has something to do with JavaDoc, but not sure

            CompilationResult compilationResult = new CompilationResult(unit, 0, 0, options.maxProblemsPerUnit);

            // FIXASC Is it ok to use a new parser here everytime? If we don't we sometimes recurse back into the first one.
            // FIXASC ought to reuse to ensure types end up in same groovy CU
            GroovyParser groovyParser = new GroovyParser(this.groovyParser.requestor, options, problemReporter, false, true);
            CompilationUnitDeclaration cud = groovyParser.dietParse(unit, compilationResult);

            SourceElementNotifier notifier = ReflectionUtils.getPrivateField(SourceElementParser.class, "notifier", this);
            notifier.notifySourceElementRequestor(cud, 0, unit.getContents().length, groovyReportReferenceInfo, createSourceEnds(cud), Collections.EMPTY_MAP); // we don't care about the @category tag, so pass empty map

            return cud;
        } else {
            return super.parseCompilationUnit(unit, fullParse, pm);
        }
    }

    @Override
    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {
        if (ContentTypeUtils.isGroovyLikeFileName(sourceUnit.getFileName())) {
            return groovyParser.dietParse(sourceUnit, compilationResult);
        } else {
            return super.dietParse(sourceUnit, compilationResult);
        }
    }

    //--------------------------------------------------------------------------

    // FIXASC This should be calculated in GroovyCompilationUnitDeclaration
    private static HashtableOfObjectToInt createSourceEnds(CompilationUnitDeclaration cDecl) {
        HashtableOfObjectToInt table = new HashtableOfObjectToInt();
        if (cDecl.types != null) {
            for (TypeDeclaration tDecl : cDecl.types) {
                createSourceEndsForType(tDecl, table);
            }
        }
        return table;
    }

    // FIXASC This should be calculated in GroovyCompilationUnitDeclaration
    private static void createSourceEndsForType(TypeDeclaration tDecl, HashtableOfObjectToInt table) {
        table.put(tDecl, tDecl.sourceEnd);
        if (tDecl.fields != null) {
            for (FieldDeclaration fDecl : tDecl.fields) {
                table.put(fDecl, fDecl.sourceEnd);
            }
        }
        if (tDecl.methods != null) {
            for (AbstractMethodDeclaration mDecl : tDecl.methods) {
                table.put(mDecl, mDecl.sourceEnd);
                if (mDecl.statements != null && mDecl.statements.length > 0) {
                    for (Statement expr : mDecl.statements) {
                        if (expr instanceof QualifiedAllocationExpression) {
                            // assume anon inner type
                            createSourceEndsForType(((QualifiedAllocationExpression) expr).anonymousType, table);
                        }
                    }
                }
            }
        }
        if (tDecl.memberTypes != null) {
            for (TypeDeclaration innerTDecl : tDecl.memberTypes) {
                createSourceEndsForType(innerTDecl, table);
            }
        }
    }
}
