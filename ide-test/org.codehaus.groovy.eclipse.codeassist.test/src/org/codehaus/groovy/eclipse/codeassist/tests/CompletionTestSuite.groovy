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
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.junit.Assert.*

import java.util.regex.Pattern

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyExtendedCompletionContext
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor
import org.eclipse.jdt.groovy.search.ITypeRequestor
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor
import org.eclipse.jdt.groovy.search.TypeLookupResult
import org.eclipse.jdt.groovy.search.VariableScope
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2
import org.junit.After
import org.junit.AfterClass
import org.junit.Before

abstract class CompletionTestSuite extends GroovyEclipseTestSuite {

    @Before
    final void setUpCompletionTestCase() {
        SynchronizationUtils.waitForDSLDProcessingToComplete()
    }

    @After
    final void tearDownCompletionTestCase() {
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, '')
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, '')
    }

    @AfterClass
    static final void tearDownCompletionTestSuite() {
        GroovyContentAssist.getDefault().preferenceStore.with {
            storePreferences.@properties.keys().each { k ->
                if (!isDefault(k)) {
                    println "Resetting '$k' to its default"
                    setToDefault(k)
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    protected ICompletionProposal[] createProposalsAtOffset(CharSequence contents, int offset) {
        return createProposalsAtOffset(addGroovySource(contents, nextUnitName()), offset)
    }

    protected ICompletionProposal[] createProposalsAtOffset(ICompilationUnit unit, int offset) {
        return performContentAssist(unit, offset, GroovyCompletionProposalComputer)
    }

    /** Use {@link #createProposalsAtOffset} if testing {@link GroovyCompletionProposalComputer}. */
    protected ICompletionProposal[] performContentAssist(ICompilationUnit unit, int offset, Class<? extends IJavaCompletionProposalComputer> computerClass) {
        JavaEditor editor = openInEditor(unit)
        SynchronizationUtils.waitForIndexingToComplete(unit)
        JavaSourceViewer viewer = editor.viewer
        viewer.setSelectedRange(offset, 0)

        JavaContentAssistInvocationContext context = new JavaContentAssistInvocationContext(viewer, offset, editor)
        SimpleProgressMonitor monitor = new SimpleProgressMonitor("Create completion proposals for $unit.elementName")
        List<ICompletionProposal> proposals = computerClass.newInstance().computeCompletionProposals(context, monitor)

        return proposals.toArray(new ICompletionProposal[proposals.size()])
    }

    protected int getIndexOf(CharSequence contents, String lookFor) {
        int index = contents.toString().indexOf(lookFor); assert index != -1
        return index + lookFor.length()
    }

    protected int getLastIndexOf(CharSequence contents, String lookFor) {
        int index = contents.toString().lastIndexOf(lookFor); assert index != -1
        return index + lookFor.length()
    }

    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount, boolean isType = name.contains(' - ')) {
        int foundCount = 0
        for (proposal in proposals) {
            // field
            if (proposal.displayString.startsWith(name + ' ') && !(proposal instanceof LazyJavaTypeCompletionProposal)) {
                foundCount += 1
            } else
            // method
            if (proposal.displayString.startsWith(name + '(')) {
                foundCount += 1
            } else
            // type
            if (isType && (name.contains('.') ? proposal.displayString == name : proposal.displayString.startsWith(name))) {
                foundCount += 1
            }
        }
        if (foundCount != expectedCount) {
            fail("Expected to find proposal '$name' $expectedCount times, but found it $foundCount times.  All Proposals:" + printProposals(proposals))
        }
    }

    /**
     * Finds the next proposal that matches the given criteria.
     *
     * @param name name to match
     * @param startFrom index to start from in {@code proposals}
     * @param isType {@code true} if looking for a type proposal
     * @return index of the proposal that matches or {@code -1} if no match
     */
    protected int indexOfProposal(ICompletionProposal[] proposals, String name, int startFrom = 0, boolean isType = name.contains(' - ')) {
        assert startFrom >= 0 && startFrom < proposals.length
        for (i in startFrom..<proposals.length) {
            ICompletionProposal proposal = proposals[i]
            // field
            if (proposal.displayString.startsWith(name + ' ') && !(proposal instanceof LazyJavaTypeCompletionProposal)) {
                return i
            }
            // method
            if (proposal.displayString.startsWith(name + '(')) {
                return i
            }
            // type
            if (isType && proposal.displayString.startsWith(name)) {
                return i
            }
        }
        return -1
    }

    /**
     * Finds the first proposal that matches the given criteria.
     */
    protected ICompletionProposal findFirstProposal(ICompletionProposal[] proposals, String name, boolean isType = name.contains(' - ')) {
        int i = indexOfProposal(proposals, name, 0, isType)
        if (i != -1)
            return proposals[i]
        fail("Expected at least one proposal that matches '$name', but found none.")
    }

    protected void applyProposalAndCheck(IDocument document, ICompletionProposal proposal, String expected) {
        proposal.apply(document)
        String expect = expected.normalize()
        String actual = document.get().normalize()
        assertEquals('Completion proposal applied but different results found.', expect, actual)
    }

    /**
     * Applies the specified completion proposal to the active editor and checks
     * against the expected result. Assumes performContentAssist(...) was called
     * by some means to get {@code proposal}.
     */
    protected void applyProposalAndCheck(ICompletionProposal proposal, String expected, char trigger = 0, int stateMask = 0) {
        assert proposal instanceof ICompletionProposalExtension2
        JavaContentAssistInvocationContext context = proposal.@fInvocationContext
        proposal.apply(context.viewer, trigger, stateMask, proposal.replacementOffset)

        String expect = expected.normalize()
        String actual = context.document.get().normalize()
        assertEquals('Completion proposal applied but different results found.', expect, actual)
    }

    /**
     * Applies the specified completion proposal to the active editor and checks
     * against the expected replacement, the initial parameter selection,
     * and the target cursor position after after quitting parameter linked mode.
     */
    protected void applyProposalAndCheckCursor(ICompletionProposal proposal, String expected,
            int expectedSelectionOffset, int expectedSelectionLength = 0, int expectedCursorPosition = expectedSelectionOffset) {
        applyProposalAndCheck(proposal, expected)

        JavaContentAssistInvocationContext context = proposal.@fInvocationContext
        assertEquals('selection range offset', expectedSelectionOffset, proposal.getSelection(context.document).x)
        assertEquals('selection range length', expectedSelectionLength, proposal.getSelection(context.document).y)
        assertEquals('cursor position', expectedCursorPosition, proposal.replacementOffset + proposal.cursorPosition)
    }

    protected void checkReplacementRegexp(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0
        for (proposal in proposals) {
            def replacement = proposal.replacementString
            if (Pattern.matches(expectedReplacement, replacement)) {
                foundCount += 1
            }
        }

        if (foundCount != expectedCount) {
            def sb = new StringBuilder("Expected to find proposal '$expectedReplacement' $expectedCount times, but found it $foundCount times.  All Proposals:")
            for (proposal in proposals) {
                sb.append('\n').append(proposal.replacementString)
            }
            fail(sb.toString())
        }
    }

    protected void checkReplacementString(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0
        for (proposal in proposals) {
            def replacement = proposal.replacementString
            if (replacement == expectedReplacement) {
                foundCount += 1
            }
        }

        if (foundCount != expectedCount) {
            def sb = new StringBuilder("Expected to find proposal '$expectedReplacement' $expectedCount times, but found it $foundCount times.  All Proposals:")
            for (proposal in proposals) {
                sb.append('\n').append(proposal.replacementString)
            }
            fail(sb.toString())
        }
    }

    /**
     * The build machine sometimes seems to be running on a JDK with different
     * local variable tables for system classes.  This variant can take a couple
     * of options rather than just one when asserting what is expected to be found.
     */
    protected void checkReplacementString(ICompletionProposal[] proposals, String[] expectedReplacementOptions, int expectedCount) {
        int foundCount = 0
        for (proposal in proposals) {
            def replacement = proposal.replacementString
            if (replacement == expectedReplacementOptions[0] || replacement == expectedReplacementOptions[1]) {
                foundCount += 1
            }
        }

        if (foundCount != expectedCount) {
            def sb = new StringBuilder("Expected to find proposal '${expectedReplacementOptions[0]}' or '${expectedReplacementOptions[1]}' $expectedCount times, but found them $foundCount times.  All Proposals:")
            for (proposal in proposals) {
                sb.append('\n').append(proposal.replacementString)
            }
            fail(sb.toString())
        }
    }

    protected ICompletionProposal[] orderByRelevance(ICompletionProposal[] proposals) {
        return proposals.sort { ICompletionProposal lhs, ICompletionProposal rhs ->
            rhs.relevance <=> lhs.relevance ?: lhs.displayString <=> rhs.displayString
        }
    }

    protected String printProposals(ICompletionProposal[] proposals) {
        def sb = new StringBuilder()
        for (proposal in proposals) {
            sb.append('\n').append(proposal.displayString)
            if (proposal instanceof IJavaCompletionProposal) {
                sb.append(' (').append(proposal.relevance).append(')')
            }
        }
        return sb.toString()
    }

    protected void checkProposalApplicationType(String contents, String expected, int proposalLocation, String proposalName) {
        checkProposalApplication(contents, expected, proposalLocation, proposalName, true)
    }

    protected void checkProposalApplicationNonType(String contents, String expected, int proposalLocation, String proposalName) {
        checkProposalApplication(contents, expected, proposalLocation, proposalName, false)
    }

    protected void checkProposalApplication(String contents, String expected, int proposalLocation, String proposalName, boolean isType) {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, proposalLocation)
        ICompletionProposal firstProposal = findFirstProposal(proposals, proposalName, isType)
        applyProposalAndCheck(new Document(contents), firstProposal, expected)
    }

    protected void checkProposalApplication(String contents, int proposalLocation, String[] expecteds, String[] proposalNames) {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, proposalLocation)
        for (int i = 0; i < expecteds.length; i += 1) {
            ICompletionProposal firstProposal = findFirstProposal(proposals, proposalNames[i])
            applyProposalAndCheck(new Document(contents), firstProposal, expecteds[i])
        }
    }

    protected void assertProposalOrdering(ICompletionProposal[] proposals, String... order) {
        int prev = -1
        for (int i = 0; i < order.length; i += 1) {
            int next = indexOfProposal(proposals, order[i], prev + 1)

            String message
            if (i == 0) {
                message = "Proposal '${order[i]}' should have been found in: "
            } else {
                message = "Proposal '${order[i - 1]}' should have preceded proposal '${order[i]}' in: "
            }
            assertTrue(message + printProposals(proposals), next > prev)

            prev = next
        }
    }

    protected void assertProposalSignature(ICompletionProposal proposal, String expected) {
        String actual = proposal.displayString
        int descrSeparator = actual.indexOf('-')
        if (descrSeparator != -1) {
            actual = actual.substring(0, descrSeparator).trim()
        }
        assertEquals(expected, actual)
    }

    protected void assertExtendedContextElements(GroovyExtendedCompletionContext context, String signature, String... expectedNames) {
        IJavaElement[] visibleElements = context.getVisibleElements(signature)
        assertEquals("Incorrect number of visible elements\nexpected: ${Arrays.toString(expectedNames)}\nfound: ${elementsToNames(visibleElements)}",
            expectedNames.length, visibleElements.length)

        for (name in expectedNames) {
            boolean found = false
            for (element in visibleElements) {
                if (element.elementName == name) {
                    found = true
                    break
                }
            }
            if (!found) {
                fail("couldn't find element named $name in ${elementsToNames(visibleElements)}")
            }
        }
    }

    private String elementsToNames(IJavaElement[] visibleElements) {
        String[] names = new String[visibleElements.length]
        for (int i = 0; i < names.length; i += 1) {
            names[i] = visibleElements[i].elementName
        }
        return Arrays.toString(names)
    }

    protected GroovyExtendedCompletionContext getExtendedCoreContext(ICompilationUnit unit, int invocationOffset) throws JavaModelException {
        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit
        GroovyCompletionProposalComputer computer = new GroovyCompletionProposalComputer()
        ContentAssistContext context = computer.createContentAssistContext(gunit, invocationOffset, new Document(String.valueOf(gunit.contents)))

        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(gunit)
        SearchRequestor requestor = new SearchRequestor(context.completionNode)
        visitor.visitCompilationUnit(requestor)

        return new GroovyExtendedCompletionContext(context, requestor.currentScope)
    }

    static class SearchRequestor implements ITypeRequestor {

        public VariableScope currentScope
        public final ASTNode node

        SearchRequestor(ASTNode node) {
            this.node = node
        }

        @Override
        VisitStatus acceptASTNode(ASTNode visitorNode, TypeLookupResult visitorResult, IJavaElement enclosingElement) {
            if (node == visitorNode) {
                this.currentScope = visitorResult.scope
                return VisitStatus.STOP_VISIT
            }
            return VisitStatus.CONTINUE
        }
    }

    protected ICompletionProposal checkUniqueProposal(CharSequence contents, String completionExpr, String completionName, String replacementString = completionName) {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, completionExpr))
        checkReplacementString(proposals, replacementString, 1)
        findFirstProposal(proposals, completionName)
    }

    protected void checkProposalChoices(String contents, String completionExpr, String completionName, String replacementString, String[] expectedChoices) {
        ICompletionProposal proposal = checkUniqueProposal(contents, completionExpr, completionName, replacementString)

        ICompletionProposal[] choices = proposal.choices
        assertEquals(expectedChoices.length, choices.length)
        for (int i = 0; i < expectedChoices.length; i += 1) {
            assertEquals('unexpected choice', expectedChoices[i], choices[i].displayString)
        }
    }

    protected void checkProposalChoices(String contents, String completion, String replacementString, String[][] expectedChoices) {
        ICompletionProposal proposal = checkUniqueProposal(contents, completion, completion, replacementString)
        List<ICompletionProposal[]> choices = proposal.choices
        assertEquals(expectedChoices.length, choices.size())
        for (int i = 0; i < expectedChoices.length; i += 1) {
            assertEquals(expectedChoices[i].length, choices[i].length)

            // fix order for comparison
            expectedChoices[i].sort(true)
            choices[i].sort(true) { ICompletionProposal p -> p.displayString }

            for (int j = 0; j < expectedChoices[i].length; j += 1) {
                assertEquals('unexpected choice', expectedChoices[i][j], choices[i][j].displayString)
            }
        }
    }
}
