/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.junit.Assert.*

import java.util.regex.Pattern

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyExtendedCompletionContext
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaGuessingCompletionProposal
import org.codehaus.groovy.eclipse.codeassist.completions.NamedParameterProposal
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.CompletionProposal
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.JavaModelException
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor
import org.eclipse.jdt.groovy.core.util.JavaConstants
import org.eclipse.jdt.groovy.search.ITypeRequestor
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor
import org.eclipse.jdt.groovy.search.TypeLookupResult
import org.eclipse.jdt.groovy.search.VariableScope
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ICompletionProposal

/**
 * Includes utilities to help with all Content assist tests.
 */
abstract class CompletionTestSuite extends GroovyEclipseTestSuite {

    protected ICompletionProposal[] performContentAssist(ICompilationUnit unit, int offset, Class<? extends IJavaCompletionProposalComputer> computerClass) {
        waitForIndex()
        JavaEditor editor = openInEditor(unit)
        JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer()
        JavaContentAssistInvocationContext context = new JavaContentAssistInvocationContext(viewer, offset, editor)
        List<ICompletionProposal> proposals = computerClass.newInstance().computeCompletionProposals(context, null)

        return proposals.toArray(new ICompletionProposal[proposals.size()])
    }

    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount) {
        boolean isType = name.contains(" - ")
        proposalExists(proposals, name, expectedCount, isType)
    }

    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount, boolean isType) {
        int foundCount = 0
        for (ICompletionProposal proposal : proposals) {
            String propName = proposal.getDisplayString()
            // if a field
            if (propName.startsWith(name + " ")) {
                foundCount += 1
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                foundCount += 1
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                foundCount += 1
            }
        }
        if (foundCount != expectedCount) {
            fail("Expected to find proposal '" + name + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals: " + printProposals(proposals))
        }
    }

    /**
     * Finds the next proposal that matches the passed in name
     * @param proposals all proposals
     * @param name name to match
     * @param isType true if looking for a type proposal
     * @param startFrom index to start from
     * @return the index of the proposal that matches, or -1 if no match
     */
    protected int findProposal(ICompletionProposal[] proposals, String name, boolean isType, int startFrom) {
        for (int i = startFrom; i < proposals.length; i++) {
            ICompletionProposal proposal = proposals[i]
            // if a field
            String propName = proposal.getDisplayString()
            if (propName.startsWith(name + " ")) {
                return i
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                return i
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                return i
            } else
            // if a keyword
            if (name.equals(proposal.getDisplayString())) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns the first proposal that matches the criteria passed in
     */
    protected ICompletionProposal findFirstProposal(ICompletionProposal[] proposals, String name, boolean isType) {
        for (ICompletionProposal proposal : proposals) {
            // if a field
            String propName = proposal.getDisplayString()
            if (propName.startsWith(name + " ") &&
                    !(proposal instanceof LazyGenericTypeProposal)) {
                return proposal
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                return proposal
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                return proposal
            }
        }
        fail("Expected at least one proposal that matches '" + name + "', but found none")
        return null
    }

    protected void applyProposalAndCheck(IDocument document, ICompletionProposal proposal, String expected) {
        // reconciler runs asynchronously; give it a chance to get caught up before creating edits
        SynchronizationUtils.joinBackgroudActivities()

        proposal.apply(document)
        String actual = document.get()
        actual = actual.replaceAll("\r\n", "\n")
        expected = expected.replaceAll("\r\n", "\n")
        assertEquals("Completion proposal applied but different results found.", expected, actual)
    }

    protected void checkReplacementRegexp(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal
            String replacement = javaProposal.getReplacementString()
            if (Pattern.matches(expectedReplacement, replacement)) {
                foundCount += 1
            }
        }

        if (foundCount != expectedCount) {
            StringBuilder sb = new StringBuilder()
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal
                sb.append("\n" + javaProposal.getReplacementString())
            }
            fail("Expected to find proposal '" + expectedReplacement + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb)
        }
    }

    protected void checkReplacementString(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal
            String replacement = javaProposal.getReplacementString()
            if (replacement.equals(expectedReplacement)) {
                foundCount += 1
            }
        }

        if (foundCount != expectedCount) {
            StringBuilder sb = new StringBuilder()
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal
                sb.append("\n" + javaProposal.getReplacementString())
            }
            fail("Expected to find proposal '" + expectedReplacement + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb)
        }
    }

    /**
     * The build machine sometimes seems to be running on a JDK with different local variable tables for
     * system classes.  This variant of checkReplacementString() can take a couple of options rather
     * than just one when asserting what is expected to be found.
     */
    protected void checkReplacementString(ICompletionProposal[] proposals, String[] expectedReplacementOptions, int expectedCount) {
        int foundCount = 0
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal
            String replacement = javaProposal.getReplacementString()
            if (replacement.equals(expectedReplacementOptions[0]) || replacement.equals(expectedReplacementOptions[1])) {
                foundCount += 1
            }
        }

        if (foundCount != expectedCount) {
            StringBuilder sb = new StringBuilder()
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal
                sb.append("\n" + javaProposal.getReplacementString())
            }
            fail("Expected to find proposal '" + expectedReplacementOptions[0] + "' or '"+expectedReplacementOptions[1]+"' " + expectedCount + " times, but found them " + foundCount + " times.\nAll Proposals:" + sb)
        }
    }

    protected void validateProposal(CompletionProposal proposal, String name) {
        assertEquals(proposal.getName(), name)
    }

    protected int getIndexOf(String contents, String lookFor) {
        return contents.indexOf(lookFor)+lookFor.length()
    }

    protected int getLastIndexOf(String contents, String lookFor) {
        return contents.lastIndexOf(lookFor)+lookFor.length()
    }

    protected ICompletionProposal[] createProposalsAtOffset(String contents, int completionOffset) {
        return createProposalsAtOffset(contents, null, completionOffset)
    }

    protected ICompletionProposal[] createProposalsAtOffset(String contents, String javaContents, int completionOffset) {
        if (javaContents != null) {
            addJavaSource("public class JavaClass { }\n" + javaContents, "JavaClass", "")
        }
        def gunit = addGroovySource(contents, nextUnitName())
        return createProposalsAtOffset(gunit, completionOffset)
    }

    protected ICompletionProposal[] createProposalsAtOffset(ICompilationUnit unit, int completionOffset) {
        int count = 0
        int maxCount = 5
        ICompletionProposal[] proposals
        while ((proposals == null || proposals.length == 0) && count < maxCount) {
            if (count > 0) {
                SimpleProgressMonitor spm = new SimpleProgressMonitor("unit reconcile")
                unit.reconcile(JavaConstants.AST_LEVEL, true, null, spm)
                spm.waitForCompletion()
            }
            proposals = performContentAssist(unit, completionOffset, GroovyCompletionProposalComputer)
            count += 1
        }

        if (count >= maxCount) {
            println "Reached max ($maxCount) attempts and still got no proposals -- hopefully that is what the test expects"
        }

        return proposals
    }

    protected ICompletionProposal[] orderByRelevance(ICompletionProposal[] proposals) {
        Arrays.sort(proposals, 0, proposals.length, new Comparator<ICompletionProposal>() {
            public int compare(ICompletionProposal lhs, ICompletionProposal rhs) {
                int leftRel = ((IJavaCompletionProposal) lhs).getRelevance()
                int rghtRel = ((IJavaCompletionProposal) rhs).getRelevance()
                if (leftRel != rghtRel) {
                    return rghtRel - leftRel
                }
                // sort lexically
                return lhs.getDisplayString().compareTo(rhs.getDisplayString())
            }
        })
        return proposals
    }

    protected String printProposals(ICompletionProposal[] proposals) {
        StringBuilder sb = new StringBuilder()
        for (ICompletionProposal proposal : proposals) {
            sb.append('\n').append(proposal.getDisplayString())
            if (proposal instanceof IJavaCompletionProposal) {
                sb.append(" (").append(((IJavaCompletionProposal) proposal).getRelevance()).append(')')
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
            ICompletionProposal firstProposal = findFirstProposal(proposals, proposalNames[i], false)
            applyProposalAndCheck(new Document(contents), firstProposal, expecteds[i])
        }
    }

    protected void assertProposalOrdering(ICompletionProposal[] proposals, String... order) {
        int prev = -1
        for (int i = 0; i < order.length; i += 1) {
            int next = findProposal(proposals, order[i], false, prev + 1)

            String message
            if (i == 0) {
                message = String.format("Proposal '%s' should have been found in: ", order[i])
            } else {
                message = String.format("Proposal '%s' should have preceded proposal '%s' in: ", order[i - 1], order[i])
            }
            assertTrue(message + printProposals(proposals), next > prev)

            prev = next
        }
    }

    protected void assertExtendedContextElements(GroovyExtendedCompletionContext context, String signature, String...expectedNames) {
        IJavaElement[] visibleElements = context.getVisibleElements(signature)
        assertEquals("Incorrect number of visible elements\nexpected: " + Arrays.toString(expectedNames) +
                "\nfound: " + elementsToNames(visibleElements), expectedNames.length, visibleElements.length)

        for (String name : expectedNames) {
            boolean found = false
            for (IJavaElement element : visibleElements) {
                if (element.getElementName().equals(name)) {
                    found = true
                    break
                }
            }
            if (! found) {
                fail ("couldn't find element named " + name + " in " + elementsToNames(visibleElements))
            }
        }
    }

    private String elementsToNames(IJavaElement[] visibleElements) {
        String[] names = new String[visibleElements.length]
        for (int i = 0; i < names.length; i++) {
            names[i] = visibleElements[i].getElementName()
        }
        return Arrays.toString(names)
    }

    protected GroovyExtendedCompletionContext getExtendedCoreContext(ICompilationUnit unit, int invocationOffset) throws JavaModelException {
        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit
        GroovyCompletionProposalComputer computer = new GroovyCompletionProposalComputer()
        ContentAssistContext context = computer.createContentAssistContext(gunit, invocationOffset, new Document(String.valueOf(gunit.getContents())))

        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(gunit)
        SearchRequestor requestor = new SearchRequestor(context.completionNode)
        visitor.visitCompilationUnit(requestor)

        return new GroovyExtendedCompletionContext(context, requestor.currentScope)
    }

    static class SearchRequestor implements ITypeRequestor {

        public VariableScope currentScope
        public ASTNode node

        public SearchRequestor(ASTNode node) {
            this.node = node
        }

        public VisitStatus acceptASTNode(ASTNode visitorNode, TypeLookupResult visitorResult, IJavaElement enclosingElement) {
            if (node == visitorNode) {
                this.currentScope = visitorResult.scope
                return VisitStatus.STOP_VISIT
            }
            return VisitStatus.CONTINUE
        }
    }

    protected void checkProposalChoices(String contents, String toFind, String lookFor, String replacementString, String[] expectedChoices) {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, toFind))
        checkReplacementString(proposals, replacementString, 1)
        ICompletionProposal proposal = findFirstProposal(proposals, lookFor, false)
        NamedParameterProposal guessingProposal = (NamedParameterProposal) proposal
        ICompletionProposal[] choices = guessingProposal.getChoices()
        assertEquals(expectedChoices.length, choices.length)
        for (int i = 0; i < expectedChoices.length; i++) {
            assertEquals("unexpected choice", expectedChoices[i], choices[i].getDisplayString())
        }
    }

    protected void checkProposalChoices(String contents, String lookFor, String replacementString, String[][] expectedChoices) {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, lookFor))
        checkReplacementString(proposals, replacementString, 1)
        ICompletionProposal proposal = findFirstProposal(proposals, lookFor, false)
        GroovyJavaGuessingCompletionProposal guessingProposal = (GroovyJavaGuessingCompletionProposal) proposal
        guessingProposal.getReplacementString();  // instantiate the guesses.
        ICompletionProposal[][] choices = guessingProposal.getChoices()
        assertEquals(expectedChoices.length, choices.length)
        for (int i = 0; i < expectedChoices.length; i++) {
            assertEquals(expectedChoices[i].length, choices[i].length)

            // proposal ordering is arbitrary
            Comparator<ICompletionProposal> c = new Comparator<ICompletionProposal>() {
                public int compare(ICompletionProposal c1, ICompletionProposal c2) {
                    return c1.getDisplayString().compareTo(c2.getDisplayString())
                }
            }
            Arrays.sort(choices[i], 0, choices[i].length, c)
            Arrays.sort(expectedChoices[i], 0, expectedChoices[i].length)
            for (int j = 0; j < expectedChoices[i].length; j++) {
                assertEquals("unexpected choice", expectedChoices[i][j], choices[i][j].getDisplayString())
            }
        }
    }
}
