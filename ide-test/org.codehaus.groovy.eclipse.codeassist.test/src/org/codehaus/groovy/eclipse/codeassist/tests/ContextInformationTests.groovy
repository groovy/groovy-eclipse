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

    private void runTest(String script, String target, String proposalName, int proposalCount) {
        ICompilationUnit unit = addGroovySource(script, nextUnitName())

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
        addGroovySource('''\
            |class Other {
            |  /*def meth() { }*/ // methods with 0 args do not have context info
            |  def meth(a) { }
            |  def meth(int a, int b) { }
            |  def method(int a, int b) { }
            |}
            |'''.stripMargin(), 'Other', 'a')

        runTest('new a.Other().meth()', 'meth(', 'meth', 2)
    }

    @Test
    void testMethodContext2() {
        addGroovySource('''\
            |class Other extends Super {
            |  /*def meth() { }*/ // methods with 0 args do not have context info
            |  def meth(a) { }
            |  def meth(int a, int b) { }
            |}
            |class Super {
            |  def meth(String d) { }
            |  def method(String d) { }
            |}
            |'''.stripMargin(), 'Other', 'b')

        runTest('new b.Other().meth()', 'meth(', 'meth', 3)
    }

    @Test
    void testMethodContext3() {
        addGroovySource('''\
            |class Other extends Super {
            |  /*def meth() { }*/ // methods with 0 args do not have context info
            |  def meth(a) { }
            |  def meth(int a, int b) { }
            |}
            |class Super {
            |  def meth(String d) { }
            |  def method(String d) { }
            |}
            |'''.stripMargin(), 'Other', 'c')

        runTest('new c.Other().meth(a)', 'meth(', 'meth', 3)
    }

    @Test
    void testMethodContext4() {
        addGroovySource('''\
            |class Other extends Super {
            |  /*def meth() { }*/ // methods with 0 args do not have context info
            |  def meth(a) { }
            |  def meth(int a, int b) { }
            |}
            |class Super {
            |  def meth(String d) { }
            |  def method(String d) { }
            |}
            |'''.stripMargin(), 'Other', 'd')

        runTest('new d.Other().meth(a,b)', 'meth(a,', 'meth', 3)
    }

    @Test
    void testConstructorContext1() {
        addGroovySource('''\
            |class Other {
            |  Other(a) { }
            |  Other(int a, int b) { }
            |}
            |'''.stripMargin(), 'Other', 'e')

        runTest('new e.Other()', 'Other(', 'Other', 2)
    }

    @Test
    void testConstructorContext2() {
        addGroovySource('''\
            |class Other {
            |  Other(a) { }
            |  Other(int a, int b) { }
            |}
            |'''.stripMargin(), 'Other', 'f')

        runTest('new f.Other()', 'Other(', 'Other', 2)
    }

    @Test
    void testConstructorContext3() {
        addGroovySource('''\
            |class Other {
            |  Other(a) { }
            |  Other(int a, int b) { }
            |}
            |'''.stripMargin(), 'Other', 'g')

        runTest('import g.Other; new Other()', 'Other(', 'Other', 2)
    }

    @Test
    void testConstructorContext4() {
        addGroovySource('''\
            |class Other {
            |  Other(a) { }
            |  Other(int a, int b) { }
            |}
            |'''.stripMargin(), 'Other', 'h')

        runTest('new h.Other(a)', 'Other(', 'Other', 2)
    }

    @Test
    void testConstructorContext5() {
        addGroovySource('''\
            |class Other {
            |  Other(a) { }
            |  Other(int a, int b) { }
            |}
            |'''.stripMargin(), 'Other', 'i')

        runTest('new i.Other(a,b)', 'Other(a,', 'Other', 2)
    }

    @Test
    void testConstructorContext6() {
        addGroovySource('''\
            |class Other {
            |  Other(a) { }
            |  Other(int a, int b) { }
            |}
            |class Super {
            |  Super(String d) { }
            |  Super(String d, String e) { }
            |}
            |'''.stripMargin(), 'Super')

        runTest('new Super()', 'Super(', 'Super', 2)
    }
}
