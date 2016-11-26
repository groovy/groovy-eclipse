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

import junit.framework.Test;

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that context information is appropriately available.
 *
 * And also that completion proposals that come from the {@link ContentAssistLocation#METHOD_CONTEXT}
 * location do not modify source
 *
 * @author Andrew Eisenberg
 * @created Jul 15, 2011
 */
public final class ContextInformationTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(ContextInformationTests.class);
    }

    protected void runTest(ICompilationUnit unit, String target, String which, int count) throws Exception {
        String source = unit.getSource();
        int offset = getIndexOf(source, target);
        assertContextInformation(which, count, performContentAssist(unit, offset, GroovyCompletionProposalComputer.class), source);
    }

    private void assertContextInformation(String proposalName, int proposalCount, ICompletionProposal[] proposals, String source) {
        if (proposalCount != proposals.length) {
            fail("Expected " + proposalCount + " proposals, but found " + proposals.length + "\nin:\n" + printProposals(proposals));
        }

        IDocument doc = new Document(source);

        for (int i = 0; i < proposals.length; i++) {
            if (!proposals[i].getDisplayString().startsWith(proposalName)) {
                fail("Unexpected disoplay string for proposal " +
                    proposalCount +
                    ".  All proposals:\n" +
                    printProposals(proposals));
            }
            if (proposals[i].getContextInformation() == null) {
                fail("No context information for proposal " + proposalCount + ".  All proposals:\n" + printProposals(proposals));
            }
            proposals[i].apply(doc);
            assertEquals("Invalid proposal application.  Should have no changes.", source, doc.get());
        }
    }

    //--------------------------------------------------------------------------

    public void testMethodContext1() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "  def method(int a, int b) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other().meth()", "File", "");

        runTest(unit, "meth(", "meth", 2);
    }

    public void testMethodContext2() throws Exception {
        addGroovySource(
                "class Other extends Super {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  def meth(String d) { }\n" +
                "  def method(String d) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other().meth()", "File", "");

        runTest(unit, "meth(", "meth", 3);
    }

    public void testMethodContext3() throws Exception {
        addGroovySource(
                "class Other extends Super {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  def meth(String d) { }\n" +
                "  def method(String d) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other().meth(a)", "File", "");

        runTest(unit, "meth(", "meth", 3);
    }

    public void testMethodContext4() throws Exception {
        addGroovySource(
                "class Other extends Super {\n" +
                "  //def meth() { }\n" +  // methods with 0 args do not have context info
                "  def meth(a) { }\n" +
                "  def meth(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  def meth(String d) { }\n" +
                "  def method(String d) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other().meth(a,b)", "File", "");

        runTest(unit, "meth(a,", "meth", 3);
    }

    public void testConstructorContext1() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other()", "File", "");

        runTest(unit, "Other(", "Other", 2);
    }

    public void testConstructorContext1a() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other", "p");
        ICompilationUnit unit = addGroovySource("new p.Other()", "File", "");

        runTest(unit, "Other(", "Other", 2);
    }

    public void testConstructorContext1b() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other", "p");
        ICompilationUnit unit = addGroovySource("import p.Other\nnew Other()", "File", "");

        runTest(unit, "Other(", "Other", 2);
    }

    public void testConstructorContext2() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other(a)", "File", "");

        runTest(unit, "Other(", "Other", 2);
    }

    public void testConstructorContext3() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Other(a,b)", "File", "");

        runTest(unit, "Other(a,", "Other", 2);
    }

    public void testConstructorContext4() throws Exception {
        addGroovySource(
                "class Other {\n" +
                "  Other(a) { }\n" +
                "  Other(int a, int b) { }\n" +
                "}\n" +
                "class Super {\n" +
                "  Super(String d) { }\n" +
                "  Super(String d, String e) { }\n" +
                "}", "Other", "");
        ICompilationUnit unit = addGroovySource("new Super()", "File", "");

        runTest(unit, "Super(", "Super", 2);
    }
}
