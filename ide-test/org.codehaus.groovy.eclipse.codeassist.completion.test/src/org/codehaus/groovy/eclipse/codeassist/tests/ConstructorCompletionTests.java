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

import junit.framework.Test;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Nov 1, 2010
 *
 * Tests that constructor completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 *
 */
public final class ConstructorCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(ConstructorCompletionTests.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
    }

    public void testConstructorCompletion1() throws Exception {
        String contents = "class YYY { YYY() { } }\nnew YY\nkkk";
        String expected = "class YYY { YYY() { } }\nnew YYY()\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new YY"), "YYY");
    }

    public void testConstructorCompletion1a() throws Exception {
        String contents = "class YYY { YYY() { } }\nnew YY()\nkkk";
        String expected = "class YYY { YYY() { } }\nnew YYY()\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new YY"), "YYY");
    }

    public void testConstructorCompletion2() throws Exception {
        String contents = "class YYY { YYY(x) { } }\nnew YY\nkkk";
        String expected = "class YYY { YYY(x) { } }\nnew YYY(x)\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new YY"), "YYY");
    }

    public void testConstructorCompletion3() throws Exception {
        String contents = "class YYY { YYY(x, y) { } }\nnew YY\nkkk";
        String expected = "class YYY { YYY(x, y) { } }\nnew YYY(x, y)\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new YY"), "YYY");
    }

    public void testContructorCompletionWithinEnumDeclaration1() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 21) {
            return;
        }
        String contents = "class YYY { YYY() { } }\nenum F {\n"
                + "	Aaa() {\n@Override int foo() {\nnew YY\n}\n}\nint foo() {\n	}\n}";
        String expected = "class YYY { YYY() { } }\nenum F {\n"
                + "	Aaa() {\n@Override int foo() {\nnew YYY()\n}\n}\nint foo() {\n	}\n}";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new YY"), "YYY");
    }

    public void testContructorCompletionWithinEnumDeclaration2() throws Exception {
        String contents = "class YYY { YYY() { } }\nenum F {\n"
                + "	Aaa {\n@Override int foo() {\nnew YY\n}\n}\nint foo() {\n	}\n}";
        String expected = "class YYY { YYY() { } }\nenum F {\n"
                + "	Aaa {\n@Override int foo() {\nnew YYY()\n}\n}\nint foo() {\n	}\n}";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new YY"), "YYY");
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
     */
    public void testNoNamedArgs2() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  Flar() { }\n" +
                "  Flar(a,b,c) { }\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "");
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
     */
    public void testNoNamedArgs3() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  Flar() { }\n" +
                "  Flar(a,b,c) { }\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar","p");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "Flar", 2);
    }

    /**
     * same file
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
     */
    public void testNamedArgs2() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }

    /**
     * different file and package
     */
    public void testNamedArgs3() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
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
     */
    public void testNamedArgs4() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "");
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
     */
    public void testNamedArgs5() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "");
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
     */
    public void testNamedArgs6() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "");
        String contents = "new Flar(bbb: 7,ccc:8, aaa:9)";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 0);
        proposalExists(proposals, "bbb : __", 0);
        proposalExists(proposals, "ccc : __", 0);
        proposalExists(proposals, "Flar", 1);
    }

    /**
     * STS-2628
     * ensure no double adding of named properties for booleans
     */
    public void testNamedArgs7() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  boolean aaa\n" +
                "  boolean bbb\n" +
                "  boolean ccc\n" +
                "}", "Flar", "");
        String contents = "new Flar()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, "("));
        proposalExists(proposals, "aaa : __", 1);
        proposalExists(proposals, "bbb : __", 1);
        proposalExists(proposals, "ccc : __", 1);
        proposalExists(proposals, "Flar", 1);
    }

    public void testParamGuessing1() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
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
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
        String contents =
                "String xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new p.Flar()";
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }

    public void testParamGuessing3() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  int bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
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
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  Integer bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
        String contents =
                "import p.Flar\n" +
                "String xxx\n" +
                "Integer yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }

    public void testParamGuessing5() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  Integer bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
        String contents =
                "import p.Flar\n" +
                "String xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
      String[] expectedChoices = new String[] { "yyy", "0" };
        checkProposalChoices(contents, "Flar(", "bbb", "bbb: __, ", expectedChoices);
    }

    public void testParamGuessing6() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  String aaa\n" +
                "  Integer bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
        String contents =
                "import p.Flar\n" +
                "String xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        String[] expectedChoices = new String[] { "xxx", "\"\"" };
        checkProposalChoices(contents, "Flar(", "aaa", "aaa: __, ", expectedChoices);
    }

    public void testParamGuessing7() throws Exception {
        addGroovySource(
                "class Flar {\n" +
                "  Closure aaa\n" +
                "  Integer bbb\n" +
                "  Date ccc\n" +
                "}", "Flar", "p");
        String contents =
                "import p.Flar\n" +
                "Closure xxx\n" +
                "int yyy\n" +
                "boolean zzz\n" +
                "new Flar()";
        String[] expectedChoices = new String[] { "xxx", "{  }" };
        checkProposalChoices(contents, "Flar(", "aaa", "aaa: __, ", expectedChoices);
    }
}
