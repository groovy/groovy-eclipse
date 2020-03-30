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
package org.codehaus.groovy.eclipse.quickfix

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.codehaus.groovy.eclipse.quickfix.proposals.AddGroovyLibrariesProposal
import org.codehaus.groovy.eclipse.quickfix.proposals.ImportResolvedTypeProposal
import org.codehaus.groovy.eclipse.quickfix.proposals.InsertCastOrCoerceProposal
import org.codehaus.groovy.eclipse.quickfix.proposals.MakeJavaGroovyProposal
import org.codehaus.groovy.eclipse.refactoring.actions.TypeSearch
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.runtime.Adapters
import org.eclipse.core.runtime.CoreException
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.SourceRange
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.ui.text.java.IInvocationContext
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IProblemLocation
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor

/**
 * Integrates Groovy proposals into JDT Quick Fix framework.
 */
@CompileStatic
class GroovyQuickFixProcessor implements IQuickFixProcessor {

    private static final int[] RECOGNIZED_PROBLEM_IDS = [
        0,
        IProblem.UndefinedType,
        IProblem.IsClassPathCorrect,
        IProblem.NotAccessibleType,
        IProblem.ParsingError,
        IProblem.ParsingErrorInsertTokenAfter,
        IProblem.ParsingErrorInsertToComplete,
    ]
    static {
        Arrays.sort(RECOGNIZED_PROBLEM_IDS)
    }

    @Override
    boolean hasCorrections(ICompilationUnit unit, int problemId) {
        (GroovyQuickFixPlugin.isGroovyProject(unit) && Arrays.binarySearch(RECOGNIZED_PROBLEM_IDS, problemId) >= 0)
    }

    @Override
    public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        if (!GroovyQuickFixPlugin.isGroovyProject(context) || !(locations = GroovyQuickFixPlugin.getJavaProblems(locations))) {
            return new IJavaCompletionProposal[0]
        }

        Collection<IJavaCompletionProposal> proposals = []

        def unit = Adapters.adapt(context.compilationUnit, GroovyCompilationUnit)
        if (unit) {
            Set<String> types = []
            for (location in locations) {
                if (location.problemId != 0) {
                    continue
                }
                def matcher = location.problemArguments[0] =~ /(?:Groovy:unable to resolve class |Groovy:\[Static type checking\] - The variable \[)(\p{javaJavaIdentifierStart}\p{javaJavaIdentifierPart}*)/
                if (matcher) {
                    def typeName = matcher.group(1)
                    boolean isAnnotation = (locations.any { IProblemLocation it -> it.problemArguments && it.problemArguments[0] =~ / (an|for) annotation/ })
                    def typeData = new TypeSearch.UnresolvedTypeData(typeName, isAnnotation, new SourceRange(location.offset, location.length))

                    new TypeSearch().searchForTypes(unit, Collections.singletonMap(typeName, typeData), null)

                    typeData.foundInfos.findResults { it.type }.each { type ->
                        if (types.add(type.fullyQualifiedName)) proposals << new ImportResolvedTypeProposal(unit: unit, type: type)
                    }
                }
            }

            locations.findAll(InsertCastOrCoerceProposal.&appliesTo).each { location ->
                proposals << new InsertCastOrCoerceProposal(unit: unit, offset: location.offset, string: location.problemArguments[0])
            }

            addChangeModifierProposals(context, locations, proposals)

            addRequiresModuleProposals(context, locations, proposals)

            addUnimplementedMethodsProposals(context, locations, proposals)
        } else {
            if (locations.any(MakeJavaGroovyProposal.&appliesTo)) {
                proposals << new MakeJavaGroovyProposal(resource: context.compilationUnit.resource)
            }
        }

        if (locations.any(AddGroovyLibrariesProposal.&appliesTo) &&
                !GroovyRuntime.hasGroovyClasspathContainer(context.compilationUnit.javaProject)) {
            proposals << new AddGroovyLibrariesProposal(project: context.compilationUnit.javaProject)
        }

        proposals as IJavaCompletionProposal[]
    }

    @CompileDynamic
    private void addChangeModifierProposals(IInvocationContext context, IProblemLocation[] locations, Collection<IJavaCompletionProposal> proposals)
            throws CoreException {
        for (location in locations) {
            if (location.problemId != 0) {
                continue
            }
            /* TODO: Other 'kind' options:
                case IProblem.InheritedMethodReducesVisibility:
                case IProblem.MethodReducesVisibility:
                case IProblem.OverridingNonVisibleMethod:
                    ModifierCorrectionSubProcessor.TO_VISIBLE
                case IProblem.FinalMethodCannotBeOverridden:
                    ModifierCorrectionSubProcessor.TO_NON_FINAL
                case IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod:
                    ModifierCorrectionSubProcessor.TO_NON_STATIC
             */
            int kind
            switch (location.problemArguments[0]) {
            case ~/Groovy:You are not allowed to override the final method .*/:
                kind = org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor.TO_NON_FINAL
                break
            case ~/.* attempting to assign weaker access privileges; was .*/:
                kind = org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor.TO_VISIBLE
                break
            default:
                continue
            }

            org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor.
                addChangeOverriddenModifierProposal(context, location, proposals, kind)
        }
    }

    @CompileDynamic
    private void addRequiresModuleProposals(IInvocationContext context, IProblemLocation[] locations, Collection<IJavaCompletionProposal> proposals) {
        Collection<org.eclipse.jdt.core.dom.Name> names = Arrays.asList(locations).findResults {
            if (it.problemId == IProblem.IsClassPathCorrect || it.problemId == IProblem.NotAccessibleType || it.problemId == IProblem.UndefinedType) {
                def root = context.ASTRoot, node = it.getCoveredNode(root) ?: it.getCoveringNode(root)
                if (!(node instanceof org.eclipse.jdt.core.dom.Name)) {
                    String[] tokens = it.problemArguments[0].split(/\./)
                    org.eclipse.jdt.core.dom.Name name = root.AST.newSimpleName(tokens[0])
                    for (i in 1..<tokens.length) {
                        name = root.AST.newQualifiedName(name, root.AST.newSimpleName(tokens[i]))
                    }
                    return name
                }
            }
        }

        if (!names) return

        names.each { name ->
            org.eclipse.jdt.internal.ui.text.correction.UnresolvedElementsSubProcessor.addRequiresModuleProposals(context.compilationUnit,
                name, org.eclipse.jdt.internal.ui.text.correction.IProposalRelevance.IMPORT_NOT_FOUND_ADD_REQUIRES_MODULE, proposals, true)
        }

        proposals.unique { proposal -> proposal.displayString }
    }

    @CompileDynamic
    private void addUnimplementedMethodsProposals(IInvocationContext context, IProblemLocation[] locations, Collection<IJavaCompletionProposal> proposals) {
        for (location in locations) {
            if (location.problemId == 0 && location.problemArguments[0] =~ /Can't have an abstract method in (a non-abstract class|(an )?enum constant)/) {
                org.eclipse.jdt.internal.ui.text.correction.LocalCorrectionsSubProcessor.addUnimplementedMethodsProposals(context, location, proposals)
            }
        }
    }
}
