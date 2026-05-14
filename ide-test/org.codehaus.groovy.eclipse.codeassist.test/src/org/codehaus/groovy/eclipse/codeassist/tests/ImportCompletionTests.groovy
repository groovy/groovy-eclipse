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

import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

final class ImportCompletionTests extends CompletionTestSuite {

    private ICompletionProposal assertProposalCreated(String contents, String expression, String expectedProposal, boolean isPackage = false) {
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, expression))
        if (isPackage) {
            ICompletionProposal proposal = proposals.find {
                it.displayString == expectedProposal
            }
            assert proposal != null : "Expected to find proposal '$expectedProposal'.  All Proposals:${-> printProposals(proposals)}"
            return proposal
        } else {
            proposalExists(proposals, expectedProposal, 1)
            findFirstProposal(proposals, expectedProposal)
        }
    }

    @Test
    void testPack1() {
        String contents = '''\
            |import ja
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'ja', 'java', true), contents.replace('ja', 'java'))
    }

    @Test
    void testPack1a() {
        String contents = '''\
            |import static ja
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'ja', 'java', true), contents.replace('ja', 'java'))
    }

    @Test
    void testPack2() {
        String contents = '''\
            |import gr
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'gr', 'groovy.lang', true), contents.replace('gr', 'groovy.lang'))
    }

    @Test
    void testPack2a() {
        String contents = '''\
            |import static gr
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'gr', 'groovy.lang', true), contents.replace('gr', 'groovy.lang'))
    }

    @Test
    void testPack3() {
        String contents = '''\
            |import groovy.tr
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'tr', 'groovy.transform', true), contents.replace('tr', 'transform'))
    }

    @Test
    void testPack3a() {
        String contents = '''\
            |import static groovy.tr
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'tr', 'groovy.transform', true), contents.replace('tr', 'transform'))
    }

    @Test
    void testPack4() {
        String contents = '''\
            |import groovy.
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, '.', 'groovy.transform', true), contents.replace('.', '.transform'))
    }

    @Test
    void testPack4a() {
        String contents = '''\
            |import static groovy.
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, '.', 'groovy.transform', true), contents.replace('.', '.transform'))
    }

    @Test
    void testType1() {
        String contents = '''\
            |import groovy.lang.Gr
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Gr', 'GroovyObject - groovy.lang'), contents.replace('Gr', 'GroovyObject'))
    }

    @Test
    void testType1a() {
        String contents = '''\
            |import static groovy.lang.Gr
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Gr', 'GroovyObject - groovy.lang'), contents.replace('Gr', 'GroovyObject'))
    }

    @Test
    void testType2() {
        String contents = '''\
            |import java.util.Map.Ent
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Ent', 'Entry - java.util.Map'), contents.replace('Ent', 'Entry'))
    }

    @Test
    void testType2a() {
        String contents = '''\
            |import static java.util.Map.Ent
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Ent', 'Entry - java.util.Map'), contents.replace('Ent', 'Entry'))
    }

    @Test
    void testType3() {
        String contents = '''\
            |import TiUn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'TiUn', 'TimeUnit - java.util.concurrent'), contents.replace('TiUn', 'java.util.concurrent.TimeUnit'))
    }

    @Test
    void testType3a() {
        String contents = '''\
            |import static TiUn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'TiUn', 'TimeUnit - java.util.concurrent'), contents.replace('TiUn', 'java.util.concurrent.TimeUnit'))
    }

    @Test
    void testType4() {
        String contents = '''\
            |import org.codehaus.groovy.transform.tailrec.
            |'''.stripMargin()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, '_closure1', 0)
        proposalExists(proposals, '_closure2', 0)
    }

    @Test
    void testType4a() {
        String contents = '''\
            |import static org.codehaus.groovy.transform.tailrec.
            |'''.stripMargin()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, '_closure1', 0)
        proposalExists(proposals, '_closure2', 0)
    }

    @Test
    void testExtraMembers() {
        String contents = '''\
            |import static org.
            |'''.stripMargin()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'clone()', 0)
        proposalExists(proposals, 'notify()', 0)
        proposalExists(proposals, 'registerNatives()', 0)
    }

    @Test
    void testStaticField1() {
        String contents = '''\
            |import java.lang.Boolean.FA
            |'''.stripMargin()
        proposalExists(createProposalsAtOffset(contents, getLastIndexOf(contents, 'FA')), 'FALSE', 0)
    }

    @Test
    void testStaticField2() {
        String contents = '''\
            |import static java.lang.Boolean.FA
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'FA', 'FALSE'), contents.replace('FA', 'FALSE'))
    }

    @Test
    void testStaticMethod1() {
        String contents = '''\
            |import java.lang.Boolean.pa
            |'''.stripMargin()
        proposalExists(createProposalsAtOffset(contents, getLastIndexOf(contents, 'pa')), 'parseBoolean', 0)
    }

    @Test
    void testStaticMethod2() {
        String contents = '''\
            |import static java.lang.Boolean.pa
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'pa', 'parseBoolean'), contents.replace('pa', 'parseBoolean'))
    }
}
