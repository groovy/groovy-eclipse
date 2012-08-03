/*******************************************************************************
 * Copyright (c) 2011 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codeassist.tests;


/**
 * @author Andrew Eisenberg
 * @created Sep 9, 2011
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
        String[][] expectedChoices = new String[][] { new String[] { "yyy", "\"\"" } };
        checkProposalChoices(contents, "xxx", "xxx(yyy)", expectedChoices);
    }

    public void testParamGuessing2() throws Exception {
        String contents = 
                "String yyy\n" +
                "int zzz\n" +
                "def xxx(String x, int z) { }\n" +
                "xxx";
        String[][] expectedChoices = new String[][] { new String[] { "yyy", "\"\"" }, new String[] { "zzz", "0" } };
        checkProposalChoices(contents, "xxx", "xxx(yyy, zzz)", expectedChoices);
    }
    
    public void testParamGuessing3() throws Exception {
        String contents = 
                "String yyy\n" +
                "Integer zzz\n" +
                "boolean aaa\n" +
                "def xxx(String x, int z, boolean a) { }\n" +
                "xxx";
        String[][] expectedChoices = new String[][] { new String[] { "yyy", "\"\"" }, new String[] { "zzz", "0" }, new String[] { "aaa", "false", "true" } };
        checkProposalChoices(contents, "xxx", "xxx(yyy, zzz, aaa)", expectedChoices);
    }

    // GRECLIPSE-1268  This test may fail in some environments since the ordering of
    // guessed parameters is not based on actual source location.  Need a way to map 
    // from variable name to local variable declaration in GroovyExtendedCompletionContext.computeVisibleElements(String)
    public void testParamGuessing4() throws Exception {
        String contents = 
                "Closure yyy\n" +
                "def zzz = { }\n" +
                "def xxx(Closure c) { }\n" +
                "xxx";
        String[][] expectedChoices = new String[][] { new String[] { "zzz", "yyy", "{  }" } };
        
        
        try {
            checkProposalChoices(contents, "xxx", "xxx {", expectedChoices);
        } catch (AssertionError e) {
            try {
                checkProposalChoices(contents, "xxx", "xxx zzz", expectedChoices);
            } catch (AssertionError e2) {
                // this version is also a correct result
                checkProposalChoices(contents, "xxx", "xxx yyy", expectedChoices);
            }
        }
    }
}
