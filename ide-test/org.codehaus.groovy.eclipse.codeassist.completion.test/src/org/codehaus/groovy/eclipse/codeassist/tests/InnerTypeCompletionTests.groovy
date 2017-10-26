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

import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that content assist works as expected in inner classes.
 */
final class InnerTypeCompletionTests extends CompletionTestSuite {

    @Test
    void testInInnerClass1() {
        String contents = 'class Outer { class Inner { \nHTML\n } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testInInnerClass2() {
        String contents = 'class Outer { class Inner { def x(HTML) { } } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testInInnerClass3() {
        String contents = 'class Outer { class Inner extends HTML { } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testInInnerClass4() {
        String contents = 'class Outer { class Inner { def x() {  HTML } } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'HTML'))
        proposalExists(proposals, 'HTML - javax.swing.text.html', 1)
    }

    @Test
    void testInnerClass1() {
        String contents = 'class Outer { class Inner { Inner f } } '
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'Inner'))
        proposalExists(proposals, 'Inner - Outer', 1)
    }

    @Test
    void testInnerClass2() {
        String contents = 'class Outer { class Inner { Inner f } } '
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'Inner'))
        proposalExists(proposals, 'Inner - Outer', 1)
    }

    @Test
    void testInnerMember1() {
        String contents = 'class Outer { class Inner { \n def y() { xxx } \n def xxx } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'xxx'))
        proposalExists(proposals, 'xxx', 1)
    }

    @Test
    void testCompletionOFInnerMember2() {
        String contents = 'class Outer { Inner i\ndef y() { i.xxx } \nclass Inner { \n  def xxx } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'xxx'))
        proposalExists(proposals, 'xxx', 1)
    }

    @Test
    void testInnerMember3() {
        String contents = 'def y(Outer.Inner i) { i.xxx } \nclass Outer { class Inner { \n  def xxx } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'xxx'))
        proposalExists(proposals, 'xxx', 1)
    }

    @Test
    void testInnerMember4() {
        String contents = 'def y(Outer.Inner i) { i.xxx } \nclass Outer { class Inner { \n  def getXxx() {} } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'xxx'))
        proposalExists(proposals, 'xxx', 1)
    }

    @Test
    void testInnerMember5() {
        String contents = 'Outer.Inner i\ni.xxx\nclass Outer { class Inner { \n  def xxx } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'xxx'))
        proposalExists(proposals, 'xxx', 1)
    }

    @Test
    void testInnerMember6() {
        String contents = 'Outer.Inner i\ni.xxx\nclass Outer { class Inner { \n  def getXxx() {} } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'xxx'))
        proposalExists(proposals, 'xxx', 1)
    }
}
