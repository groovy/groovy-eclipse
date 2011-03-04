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
 * @created Jul 23, 2009
 *
 */
public class InferencingCompletionTests extends CompletionTestCase {

    
    public InferencingCompletionTests() {
        super("Inferencing Completion Test Cases");
    }

    private static final String CONTENTS = "class TransformerTest {\nvoid testTransformer() {\ndef s = \"string\"\ns.st\n}}";
    private static final String CONTENTS_SCRIPT = 
        "def s = \"string\"\n" +
        "s.st\n" +
        "s.substring(0).sub\n" +
        "class AClass {\n " +
        "  def g() {\n" +
        "    def t\n" +
        "    t = \"\"\n" +
        "    t.st\n" +
        "  }" +
        "}";
    
    private static final String CONTENTS_GETAT1 = 
    		"class GetAt {\n" +
    		"  String getAt(foo) { }\n" +
    		"}\n" +
    		"\n" +
    		"new GetAt()[0].star\n" +
    		"GetAt g\n" +
    		"g[0].star";

    private static final String CONTENTS_GETAT2 = 
        "class GetAt {\n" +
        "}\n" +
        "\n" +
        "new GetAt()[0].star\n" +
        "GetAt g\n" +
        "g[0].star";
    
    private static final String CONTENTS_CLOSURE = "def file = new File(\"/tmp/some-file.txt\")\ndef writer = file.newWriter()\nnew URL(url).eachLine { line ->\nwriter.close()\n}";
    public void testInferenceOfLocalStringInMethod() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS, getIndexOf(CONTENTS, "s.st"));
        proposalExists(proposals, "startsWith", 2);
    }

    public void testInferenceOfLocalString() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_SCRIPT, getIndexOf(CONTENTS_SCRIPT, "s.st"));
        proposalExists(proposals, "startsWith", 2);
    }
    public void testInferenceOfLocalString2() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_SCRIPT, getIndexOf(CONTENTS_SCRIPT, "0).sub"));
        proposalExists(proposals, "substring", 2);
    }
    
    public void testInferenceOfStringInClass() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_SCRIPT, getIndexOf(CONTENTS_SCRIPT, "t.st"));
        proposalExists(proposals, "startsWith", 2);
    }
    public void testInferenceInClosure() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_CLOSURE, getIndexOf(CONTENTS_CLOSURE, "writer.clos"));
        proposalExists(proposals, "close", 1);
    }
    
    public void testGetAt1() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT1, getIndexOf(CONTENTS_GETAT1, ")[0].star"));
        proposalExists(proposals, "startsWith", 2);
    }
    public void testGetAt2() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT1, getIndexOf(CONTENTS_GETAT1, "g[0].star"));
        proposalExists(proposals, "startsWith", 2);
    }
    public void testGetAt3() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT2, getIndexOf(CONTENTS_GETAT2, ")[0].star"));
        proposalExists(proposals, "startsWith", 0);
    }
    public void testGetAt4() throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(CONTENTS_GETAT2, getIndexOf(CONTENTS_GETAT2, "g[0].star"));
        proposalExists(proposals, "startsWith", 0);
    }
    
}
