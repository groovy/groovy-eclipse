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

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Assert
import org.junit.Test

/**
 * Tests that context information is appropriately available.
 *
 * And also that completion proposals that come from the {@link ContentAssistLocation#METHOD_CONTEXT}
 * location do not modify source.
 */
final class ContextInformationTests extends CompletionTestSuite {

    private void runTest(ICompilationUnit unit, String target, String proposalName, int proposalCount) {
        String source = unit.source
        int offset = getIndexOf(source, target)
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, offset)

        if (proposalCount != proposals.length) {
            Assert.fail("Expected $proposalCount proposals, but found ${proposals.length}\nin:\n${printProposals(proposals)}")
        }
        def doc = new Document(source)
        for (proposal in proposals) {
            if (!proposal.displayString.startsWith(proposalName)) {
                Assert.fail("Unexpected disoplay string for proposal $proposalCount.  All proposals:\n${printProposals(proposals)}")
            }
            if (proposal.contextInformation == null) {
                Assert.fail("No context information for proposal $proposalCount.  All proposals:\n${printProposals(proposals)}")
            }
            proposal.apply(doc)
            Assert.assertEquals('Invalid proposal application.  Should have no changes.', source, doc.get())
        }
    }

    //--------------------------------------------------------------------------

    @Test
    void testMethodContext1() {
        addGroovySource(
            "class Other {\n" +
            "  //def meth() { }\n" +  // methods with 0 args do not have context info
            "  def meth(a) { }\n" +
            "  def meth(int a, int b) { }\n" +
            "  def method(int a, int b) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other().meth()")

        runTest(unit, "meth(", "meth", 2)
    }

    @Test
    void testMethodContext2() {
        addGroovySource(
            "class Other extends Super {\n" +
            "  //def meth() { }\n" +  // methods with 0 args do not have context info
            "  def meth(a) { }\n" +
            "  def meth(int a, int b) { }\n" +
            "}\n" +
            "class Super {\n" +
            "  def meth(String d) { }\n" +
            "  def method(String d) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other().meth()")

        runTest(unit, "meth(", "meth", 3)
    }

    @Test
    void testMethodContext3() {
        addGroovySource(
            "class Other extends Super {\n" +
            "  //def meth() { }\n" +  // methods with 0 args do not have context info
            "  def meth(a) { }\n" +
            "  def meth(int a, int b) { }\n" +
            "}\n" +
            "class Super {\n" +
            "  def meth(String d) { }\n" +
            "  def method(String d) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other().meth(a)")

        runTest(unit, "meth(", "meth", 3)
    }

    @Test
    void testMethodContext4() {
        addGroovySource(
            "class Other extends Super {\n" +
            "  //def meth() { }\n" +  // methods with 0 args do not have context info
            "  def meth(a) { }\n" +
            "  def meth(int a, int b) { }\n" +
            "}\n" +
            "class Super {\n" +
            "  def meth(String d) { }\n" +
            "  def method(String d) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other().meth(a,b)")

        runTest(unit, "meth(a,", "meth", 3)
    }

    @Test
    void testConstructorContext1() {
        addGroovySource(
            "class Other {\n" +
            "  Other(a) { }\n" +
            "  Other(int a, int b) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other()")

        runTest(unit, "Other(", "Other", 2)
    }

    @Test
    void testConstructorContext1a() {
        addGroovySource(
            "package p\n" +
            "class Other {\n" +
            "  Other(a) { }\n" +
            "  Other(int a, int b) { }\n" +
            "}", nextUnitName(), "p")
        ICompilationUnit unit = addGroovySource("new p.Other()")

        runTest(unit, "Other(", "Other", 2)
    }

    @Test
    void testConstructorContext1b() {
        addGroovySource(
            "package p\n" +
            "class Other {\n" +
            "  Other(a) { }\n" +
            "  Other(int a, int b) { }\n" +
            "}", nextUnitName(), "p")

        ICompilationUnit unit = addGroovySource("import p.Other\nnew Other()")

        runTest(unit, "Other(", "Other", 2)
    }

    @Test
    void testConstructorContext2() {
        addGroovySource(
            "class Other {\n" +
            "  Other(a) { }\n" +
            "  Other(int a, int b) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other(a)")

        runTest(unit, "Other(", "Other", 2)
    }

    @Test
    void testConstructorContext3() {
        addGroovySource(
            "class Other {\n" +
            "  Other(a) { }\n" +
            "  Other(int a, int b) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Other(a,b)")

        runTest(unit, "Other(a,", "Other", 2)
    }

    @Test
    void testConstructorContext4() {
        addGroovySource(
            "class Other {\n" +
            "  Other(a) { }\n" +
            "  Other(int a, int b) { }\n" +
            "}\n" +
            "class Super {\n" +
            "  Super(String d) { }\n" +
            "  Super(String d, String e) { }\n" +
            "}", nextUnitName())

        ICompilationUnit unit = addGroovySource("new Super()")

        runTest(unit, "Super(", "Super", 2)
    }
}
