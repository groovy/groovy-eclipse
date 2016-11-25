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
package org.codehaus.groovy.eclipse.codeassist.tests;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyExtendedCompletionContext;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaGuessingCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.completions.NamedParameterProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.groovy.eclipse.test.EclipseTestSetup;
import org.codehaus.groovy.eclipse.test.SynchronizationUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.groovy.tests.builder.SimpleProgressMonitor;
import org.eclipse.jdt.groovy.core.util.JavaConstants;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyGenericTypeProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 *
 * Includes utilities to help with all Content assist tests
 */
public abstract class CompletionTestCase extends TestCase {

    /**
     * Parent class should define:<pre>
     * public static Test suite() {
     *   return newTestSuite(Whatever.class);
     * }</pre>
     */
    protected static Test newTestSuite(Class<? extends Test> test) {
        return new EclipseTestSetup(new TestSuite(test));
    }

    @Override
    protected void tearDown() throws Exception {
        EclipseTestSetup.removeSources();
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Starting: " + getName());
    }

    protected GroovyCompilationUnit addGroovySource(CharSequence contents, String name, String pack) {
        return EclipseTestSetup.addGroovySource(contents, name, pack);
    }

    protected CompilationUnit addJavaSource(CharSequence contents, String name, String pack) {
        return EclipseTestSetup.addJavaSource(contents, name, pack);
    }

    @Deprecated
    protected ICompilationUnit create(String contents) throws Exception {
        return addGroovySource(contents, "GroovyClass", "");
    }

    @Deprecated
    protected ICompilationUnit create(String cuName, String contents) throws Exception {
        return addGroovySource(contents, cuName, "");
    }

    @Deprecated
    protected ICompilationUnit create(String pkg, String cuName, String contents) throws Exception {
        return addGroovySource(contents, cuName, pkg);
    }

    protected ICompletionProposal[] performContentAssist(ICompilationUnit unit, int offset, Class<? extends IJavaCompletionProposalComputer> computerClass) throws Exception {
        EclipseTestSetup.buildProject();
        JavaEditor editor = EclipseTestSetup.openInEditor(unit);
        JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer();
        JavaContentAssistInvocationContext context = new JavaContentAssistInvocationContext(viewer, offset, editor);
        List<ICompletionProposal> proposals = computerClass.newInstance().computeCompletionProposals(context, null);

        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount) {
        boolean isType = name.contains(" - ");
        proposalExists(proposals, name, expectedCount, isType);
    }

    protected void proposalExists(ICompletionProposal[] proposals, String name, int expectedCount, boolean isType) {
        int foundCount = 0;
        for (ICompletionProposal proposal : proposals) {

            // if a field
            String propName = proposal.getDisplayString();
            if (propName.startsWith(name + " ")) {
                foundCount += 1;
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                foundCount += 1;
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                foundCount += 1;
            }
        }

        if (foundCount != expectedCount) {
            StringBuffer sb = new StringBuffer();
            for (ICompletionProposal proposal : proposals) {
                sb.append("\n" + proposal.toString());
            }
            fail("Expected to find proposal '" + name + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb);
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
            ICompletionProposal proposal = proposals[i];
            // if a field
            String propName = proposal.getDisplayString();
            if (propName.startsWith(name + " ")) {
                return i;
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                return i;
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                return i;
            } else
            // if a keyword
            if (name.equals(proposal.getDisplayString())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first proposal that matches the criteria passed in
     */
    protected ICompletionProposal findFirstProposal(ICompletionProposal[] proposals, String name, boolean isType) {
        for (ICompletionProposal proposal : proposals) {
            // if a field
            String propName = proposal.getDisplayString();
            if (propName.startsWith(name + " ") &&
                    !(proposal instanceof LazyGenericTypeProposal)) {
                return proposal;
            } else
            // if a method
            if (propName.startsWith(name + "(")) {
                return proposal;
            } else
            // if a type
            if (isType && propName.startsWith(name)) {
                return proposal;
            }
        }
        return null;
    }

    protected void applyProposalAndCheck(IDocument document, ICompletionProposal proposal, String expected) {
        // reconciler runs asynchronously; give it a chance to get caught up before creating edits
        SynchronizationUtils.joinBackgroudActivities();

        proposal.apply(document);
        String actual = document.get();
        actual = actual.replaceAll("\r\n", "\n");
        expected = expected.replaceAll("\r\n", "\n");
        assertEquals("Completion proposal applied but different results found.", expected, actual);
    }

    protected void checkReplacementRegexp(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0;
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
            String replacement = javaProposal.getReplacementString();
            if (Pattern.matches(expectedReplacement, replacement)) {
                foundCount ++;
            }
        }

        if (foundCount != expectedCount) {
            StringBuffer sb = new StringBuffer();
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
                sb.append("\n" + javaProposal.getReplacementString());
            }
            fail("Expected to find proposal '" + expectedReplacement + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb);
        }
    }

    protected void checkReplacementString(ICompletionProposal[] proposals, String expectedReplacement, int expectedCount) {
        int foundCount = 0;
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
            String replacement = javaProposal.getReplacementString();
            if (replacement.equals(expectedReplacement)) {
                foundCount ++;
            }
        }

        if (foundCount != expectedCount) {
            StringBuffer sb = new StringBuffer();
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
                sb.append("\n" + javaProposal.getReplacementString());
            }
            fail("Expected to find proposal '" + expectedReplacement + "' " + expectedCount + " times, but found it " + foundCount + " times.\nAll Proposals:" + sb);
        }
    }

    /**
     * The build machine sometimes seems to be running on a JDK with different local variable tables for
     * system classes.  This variant of checkReplacementString() can take a couple of options rather
     * than just one when asserting what is expected to be found.
     */
    protected void checkReplacementString(ICompletionProposal[] proposals, String[] expectedReplacementOptions, int expectedCount) {
        int foundCount = 0;
        for (ICompletionProposal proposal : proposals) {
            AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
            String replacement = javaProposal.getReplacementString();
            if (replacement.equals(expectedReplacementOptions[0]) || replacement.equals(expectedReplacementOptions[1])) {
                foundCount ++;
            }
        }

        if (foundCount != expectedCount) {
            StringBuffer sb = new StringBuffer();
            for (ICompletionProposal proposal : proposals) {
                AbstractJavaCompletionProposal javaProposal = (AbstractJavaCompletionProposal) proposal;
                sb.append("\n" + javaProposal.getReplacementString());
            }
            fail("Expected to find proposal '" + expectedReplacementOptions[0] + "' or '"+expectedReplacementOptions[1]+"' " + expectedCount + " times, but found them " + foundCount + " times.\nAll Proposals:" + sb);
        }
    }

    protected void validateProposal(CompletionProposal proposal, String name) {
        assertEquals(proposal.getName(), name);
    }

    protected int getIndexOf(String contents, String lookFor) {
        return contents.indexOf(lookFor)+lookFor.length();
    }

    protected int getLastIndexOf(String contents, String lookFor) {
        return contents.lastIndexOf(lookFor)+lookFor.length();
    }

    protected ICompletionProposal[] createProposalsAtOffset(String contents, int completionOffset) throws Exception {
        return createProposalsAtOffset(contents, null, completionOffset);
    }

    protected ICompletionProposal[] createProposalsAtOffset(String contents, String javaContents, int completionOffset) throws Exception {
        if (javaContents != null) {
            addJavaSource("public class JavaClass { }\n" + javaContents, "JavaClass", "");
        }

        String groovyClassName = "CompletionTest"; // TODO: Create a more dynamic name?
        ICompilationUnit gunit = addGroovySource(contents, groovyClassName, "");
        EclipseTestSetup.buildProject();

        System.err.println("--- " + groovyClassName + ".groovy ---");
        System.err.println(contents);
        System.err.println("--- " + groovyClassName + ".groovy ---");

        return createProposalsAtOffset(gunit, completionOffset);
    }

    protected ICompletionProposal[] createProposalsAtOffset(ICompilationUnit unit, int completionOffset) throws Exception {
        int count = 0;
        int maxCount = 15;
        ICompletionProposal[] proposals;
        System.err.println("Attempting createProposalsAtOffset(unit="+unit.getElementName()+",completionOffset="+completionOffset+")");
        do {
            if (count > 0) {
                SimpleProgressMonitor spm = new SimpleProgressMonitor("unit reconcile");
                unit.reconcile(JavaConstants.AST_LEVEL, true, null, spm);
                spm.waitForCompletion();

                SynchronizationUtils.waitForIndexingToComplete(unit);
            }

            System.err.println("Content assist for " + unit.getElementName());
            proposals = performContentAssist(unit, completionOffset, GroovyCompletionProposalComputer.class);
            if (proposals == null) {
                System.err.println("Found null proposals");
            } else {
                System.err.println("Found : " + Arrays.toString(proposals));
            }
            count++;
        } while ((proposals == null || proposals.length == 0) && count < maxCount);

        if (count >= maxCount) {
            System.err.println("Reached maxcount("+maxCount+") attempts and still got no proposals - hopefully that is what the test expects");
        }
        return proposals;
    }

    protected ICompletionProposal[] orderByRelevance(ICompletionProposal[] proposals) {
        Arrays.sort(proposals, 0, proposals.length, new Comparator<ICompletionProposal>() {
            public int compare(ICompletionProposal left, ICompletionProposal right) {
                int initial = ((IJavaCompletionProposal) right).getRelevance() - ((IJavaCompletionProposal) left).getRelevance();
                if (initial != 0) {
                    return initial;
                } else {
                    // sort lexically
                    return left.toString().compareTo(right.toString());
                }
            }
        });
        return proposals;
    }

    protected String printProposals(ICompletionProposal[] proposals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Incorrect proposals:\n");
        for (ICompletionProposal proposal : proposals) {
            sb.append(proposal.getDisplayString() + "\n");
        }
        return sb.toString();
    }

    protected void checkProposalApplicationType(String contents, String expected, int proposalLocation, String proposalName) throws Exception {
        checkProposalApplication(contents, expected, proposalLocation, proposalName, true);
    }

    protected void checkProposalApplicationNonType(String contents, String expected, int proposalLocation, String proposalName) throws Exception {
        checkProposalApplication(contents, expected, proposalLocation, proposalName, false);
    }

    protected void checkProposalApplication(String contents, String expected, int proposalLocation, String proposalName, boolean isType) throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, proposalLocation);
        ICompletionProposal firstProposal = findFirstProposal(proposals, proposalName, isType);
        if (firstProposal == null) {
            fail("Expected at least one proposal, but found none");
        }
        applyProposalAndCheck(new Document(contents), firstProposal, expected);
    }

    protected void checkProposalApplication(String contents, int proposalLocation, String[] expecteds, String[] proposalNames) throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, proposalLocation);
        for (int i = 0; i < expecteds.length; i++) {
            ICompletionProposal firstProposal = findFirstProposal(proposals, proposalNames[i], false);
            if (firstProposal == null) {
                fail("Expected at least one proposal, but found none");
            }
            applyProposalAndCheck(new Document(contents), firstProposal, expecteds[i]);
        }
    }

    protected void assertProposalOrdering(ICompletionProposal[] proposals, String...order) {
        int startFrom = 0;
        for (String propName : order) {
            startFrom = findProposal(proposals, propName, false, startFrom) + 1;
            if (startFrom == 0) {
                fail("Failed to find '" + propName + "' in order inside of:\n" + printProposals(proposals));
            }
        }
    }

    protected void assertExtendedContextElements(GroovyExtendedCompletionContext context, String signature, String...expectedNames) {
        IJavaElement[] visibleElements = context.getVisibleElements(signature);
        assertEquals("Incorrect number of visible elements\nexpected: " + Arrays.toString(expectedNames) +
                "\nfound: " + elementsToNames(visibleElements), expectedNames.length, visibleElements.length);

        for (String name : expectedNames) {
            boolean found = false;
            for (IJavaElement element : visibleElements) {
                if (element.getElementName().equals(name)) {
                    found = true;
                    break;
                }
            }
            if (! found) {
                fail ("couldn't find element named " + name + " in " + elementsToNames(visibleElements));
            }
        }
    }

    private String elementsToNames(IJavaElement[] visibleElements) {
        String[] names = new String[visibleElements.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = visibleElements[i].getElementName();
        }
        return Arrays.toString(names);
    }

    protected GroovyExtendedCompletionContext getExtendedCoreContext(ICompilationUnit unit, int invocationOffset) throws JavaModelException {
        GroovyCompilationUnit gunit = (GroovyCompilationUnit) unit;
        GroovyCompletionProposalComputer computer = new GroovyCompletionProposalComputer();
        ContentAssistContext context = computer.createContentAssistContext(gunit, invocationOffset, new Document(String.valueOf(gunit.getContents())));

        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(gunit);
        SearchRequestor requestor = new SearchRequestor(context.completionNode);
        visitor.visitCompilationUnit(requestor);

        return new GroovyExtendedCompletionContext(context, requestor.currentScope);
    }

    public static class SearchRequestor implements ITypeRequestor {

        public VariableScope currentScope;
        public ASTNode node;

        public SearchRequestor(ASTNode node) {
            this.node = node;
        }

        public VisitStatus acceptASTNode(ASTNode visitorNode, TypeLookupResult visitorResult, IJavaElement enclosingElement) {
            if (node == visitorNode) {
                this.currentScope = visitorResult.scope;
                return VisitStatus.STOP_VISIT;
            }
            return VisitStatus.CONTINUE;
        }
    }

    protected void checkProposalChoices(String contents, String toFind, String lookFor, String replacementString, String[] expectedChoices) throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, toFind));
        checkReplacementString(proposals, replacementString, 1);
        ICompletionProposal proposal = findFirstProposal(proposals, lookFor, false);
        NamedParameterProposal guessingProposal = (NamedParameterProposal) proposal;
        ICompletionProposal[] choices = guessingProposal.getChoices();
        assertEquals(expectedChoices.length, choices.length);
        for (int i = 0; i < expectedChoices.length; i++) {
            assertEquals("unexpected choice", expectedChoices[i], choices[i].getDisplayString());
        }
    }

    protected void checkProposalChoices(String contents, String lookFor, String replacementString, String[][] expectedChoices) throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, lookFor));
        checkReplacementString(proposals, replacementString, 1);
        ICompletionProposal proposal = findFirstProposal(proposals, lookFor, false);
        GroovyJavaGuessingCompletionProposal guessingProposal = (GroovyJavaGuessingCompletionProposal) proposal;
        guessingProposal.getReplacementString();  // instantiate the guesses.
        ICompletionProposal[][] choices = guessingProposal.getChoices();
        assertEquals(expectedChoices.length, choices.length);
        for (int i = 0; i < expectedChoices.length; i++) {
            assertEquals(expectedChoices[i].length, choices[i].length);

            // proposal ordering is arbitrary
            Comparator<ICompletionProposal> c = new Comparator<ICompletionProposal>() {
                public int compare(ICompletionProposal c1, ICompletionProposal c2) {
                    return c1.getDisplayString().compareTo(c2.getDisplayString());
                }
            };
            Arrays.sort(choices[i], 0, choices[i].length, c);
            Arrays.sort(expectedChoices[i], 0, expectedChoices[i].length);
            for (int j = 0; j < expectedChoices[i].length; j++) {
                assertEquals("unexpected choice", expectedChoices[i][j], choices[i][j].getDisplayString());
            }
        }
    }
}
