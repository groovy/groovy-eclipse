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
package org.codehaus.groovy.eclipse.quickfix.test.templates

import org.codehaus.groovy.eclipse.quickfix.templates.TemplateProposalComputer
import org.codehaus.groovy.eclipse.quickfix.test.QuickFixTestSuite
import org.codehaus.groovy.eclipse.test.SynchronizationUtils
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext
import org.junit.Before
import org.junit.Test

/**
 * Tests the Groovy templates contributed by the quickfix plug-in.
 */
final class GroovyTemplatesCompletionTests extends QuickFixTestSuite {

    @Before
    void setUp() {
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE)
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 2)
    }

    /**
     * @param contents editor begin state
     * @param expected editor end state -- use '#' at the end of a line to preserve the whitespace
     * @param target substring to find for code completion proposals
     * @param which name of completion proposal to select and apply
     */
    protected void runTest(CharSequence contents, CharSequence expected, String target, String which = target) {
        def editor = openInEditor(addGroovySource(contents, nextUnitName()))
        int offset = contents.toString().indexOf(target) + target.length()
        def context = new JavaContentAssistInvocationContext(editor.viewer, offset, editor)

        // find proposal
        def proposals = new TemplateProposalComputer().computeCompletionProposals(context, null)
        assert proposals != null && !proposals.empty : 'Expected some proposals, but got none'
        def matches = proposals.findAll { it.displayString.startsWith(which + ' - ') }
        assert matches.size() == 1 : 'Expected a match, but got ' + matches.size()

        // apply template
        matches[0].apply(editor.viewer, 'x' as char, 0, offset)
        SynchronizationUtils.runEventQueue() // allow the change to show in the editor

        String expect = expected.toString().replace('#', '').normalize()
        String actual = editor.viewer.document.get().normalize()
        assert actual == expect
    }

    //--------------------------------------------------------------------------

    @Test
    void testNoProposal() {
        String target = 'try'
        for (input in ['var. try', 'var.@ try', 'var.& try']) {
            def editor = openInEditor(addGroovySource(input, nextUnitName()))
            int offset = input.indexOf(target) + target.length()
            def context = new JavaContentAssistInvocationContext(editor.viewer, offset, editor)
            def proposals = new TemplateProposalComputer().computeCompletionProposals(context, null)

            assert proposals.isEmpty()
        }
    }

    @Test
    void testNewVariable() {
        //@formatter:off
        String input = '''\
            |d
            |'''.stripMargin()
        String output = '''\
            |def name = value
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'd', 'def')
    }

    @Test
    void testTryAndCatch() {
        //@formatter:off
        String input = '''\
            |try
            |'''.stripMargin()
        String output = '''\
            |try {
            |  #
            |} catch (Exception e) {
            |  e.printStackTrace()
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'try')
    }

    @Test
    void testJUnitBefore() {
        //@formatter:off
        String input = '''\
            |final class SomeTest {
            |  Bef
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.Before
            |
            |final class SomeTest {
            |  @Before
            |  void before() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Bef', 'Before')
    }

    @Test
    void testJUnitAfter() {
        //@formatter:off
        String input = '''\
            |final class SomeTest {
            |  Aft
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.After
            |
            |final class SomeTest {
            |  @After
            |  void after() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Aft', 'After')
    }

    @Test
    void testGContractsEnsures() {
        //@formatter:off
        String input = '''\
            |final class SomeTest {
            |  Ens
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.gcontracts.annotations.Ensures
            |
            |final class SomeTest {
            |  @Ensures({ predicate })
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Ens', 'Ensures')
    }

    @Test
    void testGContractsRequires() {
        //@formatter:off
        String input = '''\
            |final class SomeTest {
            |  Req
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.gcontracts.annotations.Requires
            |
            |final class SomeTest {
            |  @Requires({ predicate })
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Req', 'Requires')
    }
}
