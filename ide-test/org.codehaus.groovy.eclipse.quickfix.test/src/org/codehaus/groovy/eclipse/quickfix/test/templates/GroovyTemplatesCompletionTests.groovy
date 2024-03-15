/*
 * Copyright 2009-2024 the original author or authors.
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
        def matches = proposals.findAll { it.displayString.startsWith(which) }
        assert matches.size() == 1 : 'Expected a match, but got ' + matches.size()

        // apply template
        matches[0].apply(editor.viewer, 'x' as char, 0, offset)
        SynchronizationUtils.runEventQueue() // allow the change to show in the editor

        String expect = expected.toString().replace('#','').normalize()
        String actual = editor.viewer.document.get().normalize()
        org.junit.Assert.assertEquals(expect,actual)
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
            |} catch (e) {
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
            |final class Spec {
            |  Bef
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.Before
            |
            |final class Spec {
            |  @Before
            |  void setUp() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Bef', 'Before ')
    }

    @Test
    void testJUnitBeforeEach() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Bef
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.jupiter.api.BeforeEach
            |
            |final class Spec {
            |  @BeforeEach
            |  void setUp() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Bef', 'BeforeEach')
    }

    @Test
    void testJUnitAfter() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Aft
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.After
            |
            |final class Spec {
            |  @After
            |  void tearDown() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Aft', 'After ')
    }

    @Test
    void testJUnitAfterEach() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Aft
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.jupiter.api.AfterEach
            |
            |final class Spec {
            |  @AfterEach
            |  void tearDown() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Aft', 'AfterEach')
    }

    @Test
    void testJUnit3TestCase() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  tes
            |}
            |'''.stripMargin()
        String output = '''\
            |final class Spec {
            |  void testName() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'tes', 'test')
    }

    @Test
    void testJUnit4TestCase() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Tes
            |}
            |'''.stripMargin()
        String output = '''\
            |import static org.junit.Assert.*
            |import static org.junit.Assume.*
            |
            |import org.junit.Test
            |
            |final class Spec {
            |  @Test
            |  void testName() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Tes', 'Test - test method (JUnit 4)')
    }

    @Test
    void testJUnit5TestCase() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Tes
            |}
            |'''.stripMargin()
        String output = '''\
            |import static org.junit.jupiter.api.Assertions.*
            |import static org.junit.jupiter.api.Assumptions.*
            |
            |import org.junit.jupiter.api.DisplayName
            |import org.junit.jupiter.api.Test
            |
            |final class Spec {
            |  @Test @DisplayName('scenario_description')
            |  void testName() {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Tes', 'Test - test method (JUnit 5)')
    }

    @Test
    void testJUnit5TestCases() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Tes
            |}
            |'''.stripMargin()
        String output = '''\
            |import org.junit.jupiter.params.ParameterizedTest
            |import org.junit.jupiter.params.provider.*
            |
            |final class Spec {
            |  @ParameterizedTest @MethodSource()
            |  void testName(input) {
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Tes', 'Test - tests method (JUnit 5)')
    }

    @Test
    void testJUnit5TestFactory() {
        //@formatter:off
        String input = '''\
            |final class Spec {
            |  Tes
            |}
            |'''.stripMargin()
        String output = '''\
            |import static org.junit.jupiter.api.Assertions.*
            |import static org.junit.jupiter.api.Assumptions.*
            |import static org.junit.jupiter.api.DynamicContainer.*
            |import static org.junit.jupiter.api.DynamicTest.*
            |
            |import org.junit.jupiter.api.DynamicNode
            |import org.junit.jupiter.api.TestFactory
            |
            |final class Spec {
            |  @TestFactory
            |  DynamicNode testFactoryName() {
            |    // TODO: generate dynamic tests
            |    #
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Tes', 'Test - test factory method (JUnit 5)')
    }

    @Test
    void testGContractsInvariant() {
        //@formatter:off
        String input = '''\
            |Inv
            |class Pogo {
            |}
            |'''.stripMargin()
        String output = '''\
            |import groovy.contracts.Invariant
            |
            |@Invariant({ predicate })
            |class Pogo {
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Inv', 'Invariant')
    }

    @Test
    void testGContractsRequires() {
        //@formatter:off
        String input = '''\
            |class Pogo {
            |  Req
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        String output = '''\
            |import groovy.contracts.Requires
            |
            |class Pogo {
            |  @Requires({ predicate })
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Req', 'Requires')
    }

    @Test
    void testGContractsEnsures() {
        //@formatter:off
        String input = '''\
            |class Pogo {
            |  Ens
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        String output = '''\
            |import groovy.contracts.Ensures
            |
            |class Pogo {
            |  @Ensures({ predicate })
            |  def meth() {
            |  }
            |}
            |'''.stripMargin()
        //@formatter:on
        runTest(input, output, 'Ens', 'Ensures')
    }
}
