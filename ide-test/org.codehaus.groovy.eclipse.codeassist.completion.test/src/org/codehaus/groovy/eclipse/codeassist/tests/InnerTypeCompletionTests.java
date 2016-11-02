/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests;

import org.eclipse.jface.text.contentassist.ICompletionProposal;


/**
 * @author Andrew Eisenberg
 * @created Dec 18, 2009
 *
 * Tests that content assist works as expected in inner classes
 */
public class InnerTypeCompletionTests extends CompletionTestCase {

    private static final String XXX = "xxx";
    private static final String HTML = "HTML";
    private static final String HTML_PROPOSAL = "HTML - javax.swing.text.html";
    public InnerTypeCompletionTests(String name) {
        super(name);
    }

    public void testCompletionInInnerClass1() throws Exception {
        String contents = "class Outer { class Inner { \nHTML\n } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionInInnerClass2() throws Exception {
        String contents = "class Outer { class Inner { def x(HTML) { } } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionInInnerClass3() throws Exception {
        String contents = "class Outer { class Inner extends HTML { } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionInInnerClass4() throws Exception {
        String contents = "class Outer { class Inner { def x() {  HTML } } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionOfInnerClass1() throws Exception {
        System.out.println("Disabled because failing on build server (only)");
//        String contents = "class Outer { class Inner { Inner f } } ";
//        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, INNER));
//        proposalExists(proposals, INNER, 1);
    }
    public void testCompletionOfInnerClass2() throws Exception {
        System.out.println("Disabled because failing on build server (only)");
//        String contents = "class Outer { class Inner { Inner f } } ";
//        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, INNER));
//        proposalExists(proposals, INNER, 1);
    }
    public void testCompletionOFInnerMember1() throws Exception {
        String contents = "class Outer { class Inner { \n def y() { xxx } \n def xxx } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    public void testCompletionOFInnerMember2() throws Exception {
        String contents = "class Outer { Inner i\ndef y() { i.xxx } \nclass Inner { \n  def xxx } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    public void testCompletionOFInnerMember3() throws Exception {
        String contents = "def y(Outer.Inner i) { i.xxx } \nclass Outer { class Inner { \n  def xxx } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    public void testCompletionOFInnerMember4() throws Exception {
        String contents = "def y(Outer.Inner i) { i.xxx } \nclass Outer { class Inner { \n  def getXxx() {} } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    public void testCompletionOFInnerMember5() throws Exception {
        String contents = "Outer.Inner i\ni.xxx\nclass Outer { class Inner { \n  def xxx } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    public void testCompletionOFInnerMember6() throws Exception {
        String contents = "Outer.Inner i\ni.xxx\nclass Outer { class Inner { \n  def getXxx() {} } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }

}
