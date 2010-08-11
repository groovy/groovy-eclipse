/*
 * Copyright 2003-2009 the original author or authors.
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
 * tests to ensure proper ordering of completion proposals
 * @author Andrew Eisenberg
 * @created Aug 11, 2010
 */
public class RelevanceTests extends CompletionTestCase {
    public RelevanceTests(String name) {
        super(name);
    }
    
    private static String NEWLINE = "\n  ";
    private static String THIS_TO = "this.to";
    private static String PU = "pu";
    
    public void testParamThenFieldThenMethodThenDGM() throws Exception {
        String contents = "class Outer {\n def f\n def m(p) {\nnull\n  \n}\n}";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, NEWLINE)));
        assertProposalOrdering(proposals, "p", "f", "getF", "m", "find");
    }

    public void testLocalVarThenFieldThenMethodThenDGM() throws Exception {
        String contents = "class Outer {\n def f\n def m() {\ndef p\n  \n}\n}";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, NEWLINE)));
        assertProposalOrdering(proposals, "p", "f", "getF", "m", "find");
    }
    
    public void testObjectMethods() throws Exception {
        String contents = "class Outer {\n def toZZZ(p) {\n this.to \n}\n}";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, THIS_TO)));
        assertProposalOrdering(proposals, "toZZZ", "toString");
    }
    
    public void testOverriddenObjectMethods() throws Exception {
        String contents = "class Outer {\n def toZZZ(p) {\n this.to } \n String toString() { \n}\n}";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, THIS_TO)));
        assertProposalOrdering(proposals, "toString", "toZZZ");
    }
    
    public void testNewMethodThenModifier() throws Exception {
        String contents = "class Other extends Outer { \n pu\n def x() { } }\n class Outer {\n def pub() { \n}\n}";
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, PU)));
        assertProposalOrdering(proposals, "pub", "public");
    }
    
    
    private void assertProposalOrdering(ICompletionProposal[] proposals, String...order) {
        int startFrom = 0;
        for (String propName : order) {
            startFrom = findProposal(proposals, propName, false, startFrom) + 1;
            if (startFrom == -1) {
                fail("Failed to find '" + propName + "' in order inside of:\n" + printProposals(proposals));
            }
        }
    }
    
}
