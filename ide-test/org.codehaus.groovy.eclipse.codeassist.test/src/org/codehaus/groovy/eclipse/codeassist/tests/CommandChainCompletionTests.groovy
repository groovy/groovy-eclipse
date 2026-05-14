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

/**
 * Tests for command chain style method invocation.
 */
final class CommandChainCompletionTests extends CompletionTestSuite {

    private static final String INITIAL_CONTENTS = '''\
        |class Inner {
        |  Inner first(arg) {}
        |  Inner second(arg) {}
        |  Inner third(arg) {}
        |  Inner aField
        |}
        |def start = new Inner()
        |'''.stripMargin()

    @Test
    void testCommandChain1() {
        String contents = INITIAL_CONTENTS + 'start.first "foo" sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 1)
    }

    @Test
    void testCommandChain2() {
        String contents = INITIAL_CONTENTS + 'start.first "foo" third "foo" sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 1)
    }

    @Test
    void testCommandChain3() {
        String contents = INITIAL_CONTENTS + 'start.first "foo" third "foo" aFi'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'aFi'))
        proposalExists(proposals, 'aField', 1)
    }

    @Test
    void testCommandChain4() {
        String contents = INITIAL_CONTENTS + 'start.first "foo" third "foo","bar" sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 1)
    }

    @Test
    void testCommandChain5() {
        String contents = INITIAL_CONTENTS + 'start.first "foo" sec "foo" third'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 1)
    }

    @Test
    void testCommandChain6() {
        String contents = INITIAL_CONTENTS + 'start.first "foo" third foo.bar(nuthin) sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 1)
    }

    @Test
    void testNoCommandChain1() {
        String contents = INITIAL_CONTENTS + 'first "foo" third "foo" sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 0)
    }

    @Test
    void testNoCommandChain2() {
        String contents = INITIAL_CONTENTS + 'first "foo" third foo.bar(nuthin)\nsec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 0)
    }

    @Test
    void testNoCommandChain3() {
        String contents = INITIAL_CONTENTS + 'first "foo" third foo.bar(nuthin).sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 0)
    }

    @Test
    void testNoCommandChain4() {
        String contents = INITIAL_CONTENTS + 'first "foo" third sec'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'sec'))
        proposalExists(proposals, 'second', 0)
    }
}
