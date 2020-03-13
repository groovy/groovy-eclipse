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
package org.codehaus.groovy.eclipse.quickfix.test.resolvers

import static org.eclipse.jdt.internal.core.util.Util.getProblemArgumentsFromMarker
import static org.junit.Assert.*

import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixProcessor
import org.codehaus.groovy.eclipse.quickfix.proposals.AddClassCastResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.GroovyQuickFixResolverRegistry
import org.codehaus.groovy.eclipse.quickfix.proposals.IQuickFixResolver
import org.codehaus.groovy.eclipse.quickfix.proposals.ProblemType
import org.codehaus.groovy.eclipse.quickfix.proposals.QuickFixProblemContext
import org.codehaus.groovy.eclipse.quickfix.proposals.AddClassCastResolver.AddClassCastProposal
import org.codehaus.groovy.eclipse.quickfix.proposals.AddMissingGroovyImportsResolver.AddMissingImportProposal
import org.codehaus.groovy.eclipse.quickfix.test.QuickFixTestSuite
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaModelMarker
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.internal.ui.text.correction.AssistContext
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal

/**
 * Harness class containing helper methods for Groovy Quick Fix testing.
 */
abstract class QuickFixHarness extends QuickFixTestSuite {

    /**
     * Tests the selection of a type to import with only one proposal.
     * <p>
     * This test will verify that a resolver exists for the unresolved type that
     * need to be imported, and the resolver's quick fix proposal contains the
     * correct fix, including the correct IType that should be imported. In
     * addition, it checks that the quick fix display expression, which takes
     * the form of "Import 'SomeType' (packageName)" is correct.
     *
     * @param typeToImportSimple
     *            the simple name of the type to import
     * @param typeToImportFullyQualified
     *            the fully qualified name of the type to import
     * @param typeToImportContent
     *            the content of the type to import, usually just an empty class
     * @param expectedQuickFixDisplay
     *            the expected quick fix display expression
     * @param typeToAddImport
     *            the simple name of the type that has the unresolved type and
     *            requires the import quick fix
     * @param typeToAddImportContent
     *            the content of the type that has the unresolved type
     */
    protected void testSelectImportGroovyTypeFromNewPackage(
            String typeToImportSimple,
            String typeToImportFullyQualified,
            String expectedQuickFixDisplay,
            String typeToAddImport,
            String typeToAddImportContent) {

        ICompilationUnit unit = addGroovySource(typeToAddImportContent, typeToAddImport, 'com.test')

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(typeToImportSimple, unit)

        assertNotNull("Expected a resolver for " + typeToImportSimple, resolver)

        AddMissingImportProposal proposal = getAddMissingImportsProposal(expectedQuickFixDisplay, resolver)

        assertNotNull("Expected a quick fix proposal for " + typeToImportSimple, proposal)

        assertEquals("Actual quick fix display expression should be: " + expectedQuickFixDisplay, expectedQuickFixDisplay, proposal.getDisplayString())

        IType proposedJavaType = proposal.getSuggestedJavaType()

        assertEquals("Expected a proposal for " + typeToImportFullyQualified, getDotForm(proposedJavaType.getFullyQualifiedName()), typeToImportFullyQualified)
    }

    /**
     * Tests quick fix proposals with multiple suggestions for the same type
     * name. Verifies that a resolver can be obtained that contains the same
     * exact number of proposals as expected. The expected proposals should be
     * defined as a Map where the key is the expected quick fix display in the
     * form of "Import 'SomeType' (package)", and the value the fully qualified
     * name.
     *
     * @param typeToImportSimple
     * @param expectedQuickFixes
     *            Map of expected quick fixes, where the key is the expected
     *            display name, and the value the expected fully qualified type
     * @param typeToAddImport
     * @param typeToAddImportContent
     */
    protected void testMultipleProposalsSameTypeName(
            String typeToImportSimple,
            Map<String, String> expectedQuickFixes,
            String typeToAddImport,
            String typeToAddImportContent) {

        assertTrue("Must pass non-null, non-empty expected quick fixes", expectedQuickFixes != null && !expectedQuickFixes.isEmpty())

        ICompilationUnit unit = addGroovySource(typeToAddImportContent, typeToAddImport, 'com.test')

        AddMissingGroovyImportsResolver resolver = getAddMissingImportsResolver(typeToImportSimple, unit)

        assertNotNull("Expected a resolver for " + typeToImportSimple, resolver)

        Map<String, IJavaCompletionProposal> proposalsMap = [:]
        for (proposal in resolver.getQuickFixProposals()) {
            proposalsMap.put(proposal.getDisplayString(), proposal)
        }

        // The number of expected quick fixes must match the number of proposals to make sure duplicates are not present
        assertEquals("Number of expected proposals does not match actual number", expectedQuickFixes.size(), proposalsMap.size())

        for (Map.Entry<String, String> entry : expectedQuickFixes.entrySet()) {
            String expectedDisplay = entry.getKey()
            String expectedFullyQualifiedName = entry.getValue()

            IJavaCompletionProposal actualProposal = proposalsMap.get(expectedDisplay)
            assertNotNull("Expected a proposal for " + expectedFullyQualifiedName, actualProposal)
            assertEquals("Expected quick fix display to be " + expectedDisplay, expectedDisplay, actualProposal.getDisplayString())
            String actualFullyQualifiedType = ((AddMissingImportProposal) actualProposal).getSuggestedJavaType().getFullyQualifiedName()
            assertEquals("Expected proposal type to import to be " + expectedFullyQualifiedName, expectedFullyQualifiedName, getDotForm(actualFullyQualifiedType))
        }
    }

    protected String getDotForm(String fullyQualifiedName) {
        return fullyQualifiedName.replace('$', '.')
    }

    /**
     * Returns all quick fix resolvers based on a specific expected problem
     * type. If none of the markers contain that problem type, null is returned.
     * <p>
     * It also verifies that the marker resource matches the resource of the
     * compilation unit
     *
     * @param markers
     *            for which a resolver should be obtained
     * @param type
     *            problem type expected in at least one of the markers
     * @param unit
     *            compilation unit containing the problem.
     * @return resolver if the specified problem type can be found in the
     *         markers, and a resolver exists for that problem type. Null
     *         otherwise
     */
    protected List<IQuickFixResolver> getAllQuickFixResolversForType(IMarker[] markers, ProblemType type, ICompilationUnit unit) {
        if (markers != null) {
            List<IQuickFixResolver> totalResolvers = new ArrayList<IQuickFixResolver>()
            for (IMarker marker : markers) {
                QuickFixProblemContext context = getSimpleProblemContext(marker, unit, type)
                if (context != null) {
                    List<IQuickFixResolver> resolvers = new GroovyQuickFixResolverRegistry(context).getQuickFixResolvers()
                    assertTrue("Expected resolvers for " + context.getProblemDescriptor().getMarkerMessages()[0], resolvers != null && resolvers.size() > 0)
                    totalResolvers.addAll(resolvers)
                }
            }
            if (!totalResolvers.isEmpty()) return totalResolvers
        }
        return Collections.emptyList()
    }

    protected IJavaCompletionProposal getCompletionProposal(String quickFixDisplay, IQuickFixResolver resolver) {
        List<IJavaCompletionProposal> proposals = resolver.getQuickFixProposals()
        if (proposals == null) {
            return null
        }
        for (proposal in proposals) {
            if (proposal.getDisplayString().equals(quickFixDisplay)) {
                return proposal
            }
        }
        return null
    }

    protected AddMissingImportProposal getAddMissingImportsProposal(String quickFixDisplay, IQuickFixResolver resolver) {
        IJavaCompletionProposal proposal = getCompletionProposal(quickFixDisplay, resolver)
        if (proposal instanceof AddMissingImportProposal) {
            return (AddMissingImportProposal) proposal
        }
        return null
    }

    protected AddClassCastProposal getAddClassCastProposal(String quickFixDisplay, IQuickFixResolver resolver) {
        IJavaCompletionProposal proposal = getCompletionProposal(quickFixDisplay, resolver)
        if (proposal instanceof AddClassCastProposal) {
            return (AddClassCastProposal) proposal
        }
        return null
    }

    /**
     * Generates a problem context only containing basic marker information and
     * compilation unit. It however, does not contain AST information like the
     * covered or covering node, or the AST root. Note that the resource in the
     * marker must be the same as the resource for the compilation unit,
     * otherwise null is returned.
     * <p>
     * Consequently, this should only be used to test resolvers that can fix a
     * problem purely on the information contained in a marker (the marker type,
     * messages, problem ID, offset of problem, and length of problem)
     */
    protected QuickFixProblemContext getSimpleProblemContext(IMarker marker, ICompilationUnit unit, ProblemType problemType) {
        // make sure the marker's associated resource matches the compilation
        // unit that needs to be fixed, as
        // to not solve a similar marker from another compilation unit, in case
        // a project has multiple markers
        // from different Groovy files
        IResource markResource = marker.resource, unitResource = unit.resource
        if (markResource != unitResource) {
            return null
        }
        if (marker.getAttribute(IMarker.SEVERITY) == IMarker.SEVERITY_ERROR) {
            def arguments = getProblemArgumentsFromMarker(marker.getAttribute(IJavaModelMarker.ARGUMENTS, ''))
            int problemId = marker.getAttribute('id')
            def descriptor = new GroovyQuickFixProcessor().getProblemDescriptor(problemId, marker.type, arguments)
            if (descriptor != null && descriptor.type == problemType) {
                int offset = ((Integer) marker.getAttribute(IMarker.CHAR_START))
                int length = ((Integer) marker.getAttribute(IMarker.CHAR_END)) - offset
                return new QuickFixProblemContext(descriptor, new AssistContext(unit, offset, length), new ProblemLocation(offset, length, problemId, null, true, marker.type))
            }
        }
        return null
    }

    /**
     * Gets the first enountered add missing imports resolver, or null if none
     * are found for the given unresolved type. If multiple resolvers are found,
     * it returns the first one.
     */
    protected AddMissingGroovyImportsResolver getAddMissingImportsResolver(String unresolvedSimpleName, ICompilationUnit unit) {
        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.MISSING_IMPORTS_TYPE, unit)
        if (resolvers == null) {
            return null
        }

        for (resolver in resolvers) {
            if (resolver instanceof AddMissingGroovyImportsResolver) {
                AddMissingGroovyImportsResolver importResolver = (AddMissingGroovyImportsResolver) resolver
                List<IJavaCompletionProposal> proposals = importResolver.quickFixProposals
                if (proposals != null) {
                    for (proposal in proposals) {
                        if (proposal instanceof AddMissingImportProposal) {
                            AddMissingImportProposal importProposal = (AddMissingImportProposal) proposal
                            if (importProposal.suggestedJavaType.elementName == unresolvedSimpleName) {
                                return importResolver
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    /**
     * Gets the first enountered add class cast resolver, or null if none
     * are found for the given unresolved type. If multiple resolvers are found,
     * it returns the first one.
     */
    protected AddClassCastResolver getAddClassCastResolver(ICompilationUnit unit) {
        IMarker[] markers = getCompilationUnitJDTFailureMarkers(unit)
        List<IQuickFixResolver> resolvers = getAllQuickFixResolversForType(markers, ProblemType.STATIC_TYPE_CHECKING_CANNOT_ASSIGN, unit)
        if (resolvers == null) {
            return null
        }
        for (resolver in resolvers) {
            if (resolver instanceof AddClassCastResolver) {
                AddClassCastResolver classCastResolver = (AddClassCastResolver) resolver
                List<IJavaCompletionProposal> proposals = classCastResolver.quickFixProposals
                if (proposals != null) {
                    for (proposal in proposals) {
                        if (proposal instanceof AddClassCastProposal) {
                            return classCastResolver
                        }
                    }
                }
            }
        }
        return null
    }

    protected IMarker[] getJDTFailureMarkers(IResource resource) {
        buildProject()
        waitForIndex()
        resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE)
    }

    protected IMarker[] getCompilationUnitJDTFailureMarkers(ICompilationUnit unit) {
        getJDTFailureMarkers(unit.resource)
    }
}
