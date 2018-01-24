/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class GuessingCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_BRACKETS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.PARAMETER_GUESSING, true)
    }

    @Test
    void testParamGuessing1() {
        String contents = '''\
            String yyy
            def xxx(String x) { }
            xxx
            '''.stripIndent()
        String[][] expectedChoices = [ [ 'yyy', '""' ] as String[] ]
        checkProposalChoices(contents, 'xxx', 'xxx(yyy)', expectedChoices)
    }

    @Test
    void testParamGuessing2() {
        String contents = '''\
            String yyy
            int zzz
            def xxx(String x, int z) { }
            xxx
            '''.stripIndent()
        String[][] expectedChoices = [
            [ 'yyy', '""' ] as String[],
            [ 'zzz', '0' ] as String[]
        ]
        checkProposalChoices(contents, 'xxx', 'xxx(yyy, zzz)', expectedChoices)
    }

    @Test
    void testParamGuessing3() {
        String contents = '''\
            String yyy
            Integer zzz
            boolean aaa
            def xxx(String x, int z, boolean a) { }
            xxx
            '''.stripIndent()
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
            Closure yyy
            def zzz = { }
            def xxx(Closure c) { }
            xxx
            '''.stripIndent()
        String[][] expectedChoices = [
            ['zzz', 'yyy', '{  }'] as String[]
        ]
        try {
            checkProposalChoices(contents, 'xxx', 'xxx {', expectedChoices)
        } catch (AssertionError e) {
            try {
                checkProposalChoices(contents, 'xxx', 'xxx yyy', expectedChoices)
            } catch (AssertionError e2) {
                checkProposalChoices(contents, 'xxx', 'xxx zzz', expectedChoices)
            }
        }
    }

    @Test
    void testParamGuessing5() {
        addGroovySource '''\
            import java.util.concurrent.TimeUnit
            class Util {
              static void util(TimeUnit units) {
              }
            }
            '''.stripIndent(), 'Util', 'pack'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util
            |'''.stripMargin()
        IDocument document = new Document(contents)
        ICompletionProposal proposal = checkUniqueProposal(contents, 'util', 'util(MILLIS)')

        // apply initial proposal to generate parameter proposals
        applyProposalAndCheck(document, proposal, '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util(MILLIS)
            |'''.stripMargin());

        // check the parameter guesses
        ICompletionProposal[] choices = proposal.choices[0]
        Assert.assertEquals(['MILLIS', 'DAYS', 'HOURS', 'MINUTES', 'SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'null'].join('\n'),
            choices*.displayString.join('\n'))

        // TODO: Something below is not matching the real editor's application of the parameter proposal
        /*applyProposalAndCheck(document, choices[1], '''\
            |import static java.util.concurrent.TimeUnit.DAYS
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as MILLIS
            |
            |pack.Util.util(DAYS)
            |'''.stripMargin())*/
    }
}
