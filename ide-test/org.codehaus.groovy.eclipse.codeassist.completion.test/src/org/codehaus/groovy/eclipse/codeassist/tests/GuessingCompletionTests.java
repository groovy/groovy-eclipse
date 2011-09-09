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

import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyJavaGuessingCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Jul 23, 2009
 *
 */
public class GuessingCompletionTests extends CompletionTestCase {

    
    public GuessingCompletionTests() {
        super("Parameter guessing test cases");
    }
    
    public void testParamGuessing1() throws Exception {
        String contents = "String yyy\n" +
        		"def xxx(String x) { }\n" +
        		"xxx";
        String[][] expectedChoices = new String[][] { new String[] { "yyy", "null" } };
        checkProposalChoices(contents, "xxx", "xxx(yyy)", expectedChoices);
    }

    public void testParamGuessing2() throws Exception {
        String contents = 
                "String yyy\n" +
                "int zzz\n" +
                "def xxx(String x, int z) { }\n" +
                "xxx";
        String[][] expectedChoices = new String[][] { new String[] { "yyy", "null" }, new String[] { "zzz", "0" } };
        checkProposalChoices(contents, "xxx", "xxx(yyy, zzz)", expectedChoices);
    }
    
    public void testParamGuessing3() throws Exception {
        String contents = 
                "String yyy\n" +
                        "Integer zzz\n" +
                        "boolean aaa\n" +
                        "def xxx(String x, int z, boolean a) { }\n" +
                        "xxx";
        String[][] expectedChoices = new String[][] { new String[] { "yyy", "null" }, new String[] { "zzz", "0" }, new String[] { "aaa", "false", "true" } };
        checkProposalChoices(contents, "xxx", "xxx(yyy, zzz, aaa)", expectedChoices);
    }
    
    private void checkProposalChoices(String contents, String lookFor, String replacementString,
            String[][] expectedChoices) throws Exception {
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, lookFor));
        checkReplacementString(proposals, replacementString, 1);
        ICompletionProposal proposal = findFirstProposal(proposals, lookFor, false);
        GroovyJavaGuessingCompletionProposal guessingProposal = (GroovyJavaGuessingCompletionProposal) proposal;
        guessingProposal.getReplacementString();  // instantiate the guesses.
        ICompletionProposal[][] choices = guessingProposal.getChoices();
        assertEquals(expectedChoices.length, choices.length);
        for (int i = 0; i < expectedChoices.length; i++) {
            assertEquals(expectedChoices[i].length, choices[i].length);
            for (int j = 0; j < expectedChoices[i].length; j++) {
                assertEquals("unexpected choice", expectedChoices[i][j], choices[i][j].getDisplayString());
            }
        }
    }
}
