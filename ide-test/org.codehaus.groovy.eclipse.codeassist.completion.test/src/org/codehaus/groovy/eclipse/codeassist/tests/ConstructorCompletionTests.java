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

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jface.text.contentassist.ICompletionProposal;



/**
 * @author Andrew Eisenberg
 * @created Nov 1, 2010
 *
 * Tests that constructor completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 * 
 */
public class ConstructorCompletionTests extends CompletionTestCase {


    public ConstructorCompletionTests(String name) {
        super(name);
    }

    boolean orig;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        orig = GroovyPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
    }
    @Override
    protected void tearDown() throws Exception {
        try {   
            super.tearDown();
        } finally {
            GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, orig);
        }
    }
    
    public void testConstructorCompletion1() throws Exception {
        String contents = "package f\n\nclass YY { YY() { } }\nnew Y\nkkk";
        String expected = "package f\n\nclass YY { YY() { } }\nnew YY()\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new Y"), "YY");
    }
    
    public void testConstructorCompletion2() throws Exception {
        String contents = "package f\n\nclass YY { YY(x) { } }\nnew Y\nkkk";
        String expected = "package f\n\nclass YY { YY(x) { } }\nnew YY(x)\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new Y"), "YY");
    }
    
    public void testConstructorCompletion3() throws Exception {
        String contents = "package f\n\nclass YY { YY(x, y) { } }\nnew Y\nkkk";
        String expected = "package f\n\nclass YY { YY(x, y) { } }\nnew YY(x, y)\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new Y"), "YY");
    }
    
    /**
     * no named args since an explicit constructor exists
     * same file
     * @throws Exception
     */
    public void testNoNamedArgs1() throws Exception {
        String contents = 
                "class Flar {\n" +
                "  Flar() { }\n" +
        		"  String aaa\n" +
        		"  int bbb\n" +
        		"  Date ccc\n" +
        		"}\n" +
        		"new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "Flar", 1);
    }
    
    /**
     * no named args since an explicit constructor exists
     * Same package different file
     * @throws Exception
     */
    public void testNoNamedArgs2() throws Exception {
        create("Flar",
                "class Flar {\n" +
                "  Flar() { }\n" +
                "  Flar(a,b,c) { }\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "Flar", 2);
    }
    /**
     * no named args since an explicit constructor exists
     * different file and package
     * @throws Exception
     */
    public void testNoNamedArgs3() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  Flar() { }\n" +
                "  Flar(a,b,c) { }\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "Flar", 2);
    }
    
    
    /**
     * same file
     * @throws Exception
     */
    public void testNamedArgs1() throws Exception {
        String contents = 
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n" +
                "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }
    
    /**
     * Same package different file
     * @throws Exception
     */
    public void testNamedArgs2() throws Exception {
        create("Flar",
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }
    /**
     * different file and package
     * @throws Exception
     */
    public void testNamedArgs3() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }
    
    /**
     * Same package different file
     * Some args filled in
     * @throws Exception
     */
    public void testNamedArgs4() throws Exception {
        create("Flar",
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar(aaa:9)";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }
    
    /**
     * Same package different file
     * Some args filled in
     * @throws Exception
     */
    public void testNamedArgs5() throws Exception {
        create("Flar",
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar(bbb: 7,aaa:9)";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }
    
    /**
     * Same package different file
     * Some args filled in
     * @throws Exception
     */
    public void testNamedArgs6() throws Exception {
        create("Flar",
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = "new Flar(bbb: 7,ccc:8, aaa:9)";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "Flar", 1);
    }
    
    
    public void testParamGuessing1() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = 
                "import p.Flar\n" +
                "String xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }
    public void testParamGuessing2() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = 
                "String xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new p.Flar()";
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }
    public void testParamGuessing3() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = 
                "import p.Flar\n" +
                "String xxx\n" +
                "Integer yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }
    public void testParamGuessing4() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  String aaa\n" +
                "  Integer bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = 
                "import p.Flar\n" +
                "String xxx\n" +
                "Integer yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        // FIXADE should have better parameter guessing support
//        String[] expectedChoices = new String[] { "yyy", "0", "null" };
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }
    public void testParamGuessing5() throws Exception {
        create("p", "Flar",
                "package p\n" +
                "class Flar {\n" +
                "  String aaa\n" +
                "  Integer bbb\n" +
                "  Date ccc\n" +
                "}\n");
        String contents = 
                "import p.Flar\n" +
                "String xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        // FIXADE should have better parameter guessing support
//      String[] expectedChoices = new String[] { "yyy", "0", "null" };
      String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }

}
