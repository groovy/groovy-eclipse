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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

final class GuessingCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        setJavaPreference(PreferenceConstants.CODEASSIST_INSERT_COMPLETION, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'true')
        GroovyContentAssist.getDefault().preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.getDefault().preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
    }

    @Test
    void testParamGuessing1() {
        String contents = '''\
            |String yyy
            |def xxx(String x) { }
            |xxx
            |'''.stripMargin()
        String[][] expectedChoices = [ [ 'yyy', '""' ] as String[] ]
        checkProposalChoices(contents, 'xxx', 'xxx(yyy)', expectedChoices)
    }

    @Test
    void testParamGuessing2() {
        String contents = '''\
            |String yyy
            |int zzz
            |def xxx(String x, int z) { }
            |xxx
            |'''.stripMargin()
        String[][] expectedChoices = [
            [ 'yyy', '""' ] as String[],
            [ 'zzz', '0' ] as String[]
        ]
        checkProposalChoices(contents, 'xxx', 'xxx(yyy, zzz)', expectedChoices)
    }

    @Test
    void testParamGuessing3() {
        String contents = '''\
            |String yyy
            |Integer zzz
            |boolean aaa
            |def xxx(String x, int z, boolean a) { }
            |xxx
            |'''.stripMargin()
        String[][] expectedChoices = [
            [ 'yyy', '""' ] as String[],
            [ 'zzz', '0' ] as String[],
            [ 'aaa', 'false', 'true' ] as String[]
        ]
        checkProposalChoices(contents, 'xxx', 'xxx(yyy, zzz, aaa)', expectedChoices)
    }

    @Test // GRECLIPSE-1268
    void testParamGuessing4() {
        // This test may fail in some environments since the ordering of guessed
        // parameters is not based on actual source location.  Need a way to map
        // from variable name to local variable declaration in
        // GroovyExtendedCompletionContext.computeVisibleElements(String)
        String contents = '''\
            |Closure yyy
            |def zzz = { -> }
            |def xxx(Closure c, int i) { }
            |xxx
            |'''.stripMargin()
        String[][] expectedChoices = [
            ['zzz', 'yyy', '{  }'] as String[],
            ['0'] as String[]
        ]
        checkProposalChoices(contents, 'xxx', 'xxx({  }, 0)', expectedChoices)
    }

    @Test
    void testParamGuessing4a() {
        String contents = '''\
            |Closure yyy
            |def zzz = { -> }
            |def xxx(Closure c) { }
            |xxx
            |'''.stripMargin()
        String[][] expectedChoices = [
            ['{  }'] as String[]
        ]
        checkProposalChoices(contents, 'xxx', 'xxx {  }', expectedChoices)
    }

    @Test
    void testParamGuessing5() {
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')

        addGroovySource '''\
            |import java.util.concurrent.TimeUnit
            |class Util {
            |  static void util(TimeUnit units) {
            |  }
            |}
            |'''.stripMargin(), 'Util', 'pack'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.ut
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'ut', 'util', 'util(MILLIS)')

        // apply initial proposal to generate parameter proposals
        applyProposalAndCheck(proposal, '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util(MILLIS)
            |'''.stripMargin());

        // check the parameter guesses
        ICompletionProposal[] choices = proposal.choices[0]
        assert choices*.displayString == ['MILLIS', 'DAYS', 'HOURS', 'MINUTES', 'SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'null']

        applyProposalAndCheck(choices[1], '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util(java.util.concurrent.TimeUnit.DAYS)
            |'''.stripMargin())
    }

    @Test
    void testParamGuessing5a() {
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_INSERT_COMPLETION, 'false')

        addGroovySource '''\
            |import java.util.concurrent.TimeUnit
            |class Util {
            |  static void util(TimeUnit units) {
            |  }
            |}
            |'''.stripMargin(), 'Util', 'pack'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.ut
            |'''.stripMargin()
        ICompletionProposal proposal = checkUniqueProposal(contents, 'ut', 'util', 'util(MILLIS)')

        // apply initial proposal to generate parameter proposals
        applyProposalAndCheck(proposal, '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util(MILLIS)
            |'''.stripMargin());

        ICompletionProposal[] choices = proposal.choices[0]
        applyProposalAndCheck(choices[1], '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util(java.util.concurrent.TimeUnit.DAYS)
            |'''.stripMargin())
    }

    @Test
    void testCtorParamGuessing() {
        addGroovySource '''\
            |class C {
            |  C(java.lang.String string, java.util.concurrent.TimeUnit units) {
            |  }
            |}
            |'''.stripMargin(), 'C', 'p'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |String s = ''
            |new p.C
            |'''.stripMargin()

        ICompletionProposal proposal = checkUniqueProposal(contents, 'new p.C', 'C', '(s, MILLIS)')
        List<ICompletionProposal[]> choices = proposal.choices

        assert choices.size() == 2
        assert choices[0]*.displayString == ['s', '""']
        assert choices[1]*.displayString == ['MILLIS', 'DAYS', 'HOURS', 'MINUTES', 'SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'null']
    }
}
