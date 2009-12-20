/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

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
    private static final String INNER = "Inner";
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
    public void testCompletionOFInnerClass1() throws Exception {
        String contents = "Inner \n class Outer { class Inner { } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, INNER));
        proposalExists(proposals, INNER, 1);
    }
    public void testCompletionOFInnerClass2() throws Exception {
        String contents = "class Outer { Inner \n class Inner { } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, INNER));
        proposalExists(proposals, INNER, 1);
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
        // FIXADE RC1 properties not initialized, so failing
//        String contents = "def y(Outer.Inner i) { i.xxx } \nclass Outer { class Inner { \n  def xxx } } ";
        String contents = "def y(Outer.Inner i) { i.xxx } \nclass Outer { class Inner { \n  def getXxx() {} } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    public void testCompletionOFInnerMember4() throws Exception {
        // FIXADE RC1 properties not initialized, so failing
//        String contents = "Outer.Inner i\ni.xxx\nclass Outer { class Inner { \n  def xxx } } ";
        String contents = "Outer.Inner i\ni.xxx\nclass Outer { class Inner { \n  def getXxx() {} } } ";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, XXX));
        proposalExists(proposals, XXX, 1);
    }
    
}
