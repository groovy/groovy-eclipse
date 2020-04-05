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

final class InferencingCompletionTests extends CompletionTestSuite {

    private static final String CONTENTS = '''\
        |final class TransformerTest {
        |  void testTransformer() {
        |    def s = "string"
        |    s.st
        |  }
        |}
        |'''.stripMargin()
    private static final String CONTENTS_SCRIPT = '''\
        |def s = "string"
        |s.st
        |s.substring(0).sub
        |class AClass {
        |  def g() {
        |    def t
        |    t = ""
        |    t.st
        |  }
        |}
        |'''.stripMargin()
    private static final String CONTENTS_GETAT1 = '''\
        |class GetAt {
        |  String getAt(foo) { }
        |}
        |
        |new GetAt()[0].star
        |GetAt g
        |g[0].star
        |'''.stripMargin()
    private static final String CONTENTS_GETAT2 = '''\
        |class GetAt {
        |}
        |
        |new GetAt()[0].star
        |GetAt g
        |g[0].star
        |'''.stripMargin()
    private static final String CONTENTS_CLOSURE = '''\
        |def file = new File("/tmp/some-file.txt")
        |def writer = file.newWriter()
        |new URL(url).eachLine { line ->
        |  writer.close()
        |}
        |'''.stripMargin()

    @Test
    void testInferenceOfLocalStringInMethod() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS, getIndexOf(CONTENTS, 's.st'))
        proposalExists(proposals, 'startsWith', 2)
    }

    @Test
    void testInferenceOfLocalString() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_SCRIPT, getIndexOf(CONTENTS_SCRIPT, 's.st'))
        proposalExists(proposals, 'startsWith', 2)
    }

    @Test
    void testInferenceOfLocalString2() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_SCRIPT, getIndexOf(CONTENTS_SCRIPT, '0).sub'))
        proposalExists(proposals, 'substring', 2)
    }

    @Test
    void testInferenceOfStringInClass() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_SCRIPT, getIndexOf(CONTENTS_SCRIPT, 't.st'))
        proposalExists(proposals, 'startsWith', 2)
    }

    @Test
    void testInferenceInClosure() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_CLOSURE, getIndexOf(CONTENTS_CLOSURE, 'writer.clos'))
        proposalExists(proposals, 'close', 1)
    }

    @Test
    void testGetAt1() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT1, getIndexOf(CONTENTS_GETAT1, ')[0].star'))
        proposalExists(proposals, 'startsWith', 2)
    }

    @Test
    void testGetAt2() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT1, getIndexOf(CONTENTS_GETAT1, 'g[0].star'))
        proposalExists(proposals, 'startsWith', 2)
    }

    @Test
    void testGetAt3() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT2, getIndexOf(CONTENTS_GETAT2, ')[0].star'))
        proposalExists(proposals, 'startsWith', 0)
    }

    @Test
    void testGetAt4() {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT2, getIndexOf(CONTENTS_GETAT2, 'g[0].star'))
        proposalExists(proposals, 'startsWith', 0)
    }
}
