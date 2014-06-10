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
 * @created Jun 5, 2009
 *
 * Tests that type completions are working properly
 */
public class TypeCompletionTests extends CompletionTestCase {


    private static final String A_TEST = "ATest";
    private static final String RUN_WITH = "RunWith";
    private static final String HTML = "HTML";
    private static final String HTML_PROPOSAL = "HTML - javax.swing.text.html";
    private static final String HTML_ANCHOR = "HTMLAnchorElement";
    private static final String HTML_ANCHOR_PROPOSAL = "HTMLAnchorElement - org.w3c.dom.html";
    public TypeCompletionTests(String name) {
        super(name);
    }

    public void testCompletionTypesInScript() throws Exception {
    	String contents = HTML;
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInScript2() throws Exception {
        String contents = "new HTML()";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInMethod() throws Exception {
        String contents = "def x() {\nHTML\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInMethod2() throws Exception {
        String contents = "class X {\ndef x() {\nHTML\n}}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInParameter() throws Exception {
        String contents = "def x(HTML h) { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInParameter2() throws Exception {
        String contents = "def x(t, HTML h) { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInParameter3() throws Exception {
        String contents = "def x(t, HTML ... h) { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInParameter4() throws Exception {
        String contents = "def x(t, h = HTML) { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInClassBody() throws Exception {
        String contents = "class X {\nHTML\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInExtends() throws Exception {
        String contents = "class X extends HTML { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }
    public void testCompletionTypesInImplements() throws Exception {
        String contents = "class X implements HTMLAnchorElement { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML_ANCHOR));
        proposalExists(proposals, HTML_ANCHOR_PROPOSAL, 1);
    }
    public void testCompletionTypesInAnnotation1() throws Exception {
        String contents = "@RunWith(ATest)\n" +
        "class ATest { }\n" +
        "@interface RunWith {\n" +
        "Class value()\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, RUN_WITH));
        proposalExists(proposals, RUN_WITH, 1, true);
    }
    public void testCompletionTypesInAnnotation2() throws Exception {
        String contents = "@RunWith(ATest)\n" +
        "class ATest { }\n" +
        "@interface RunWith {\n" +
        "Class value()\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, A_TEST));
        proposalExists(proposals, A_TEST, 1, true);
    }
    public void testCompletionTypesInAnnotation3() throws Exception {
        String contents = "@RunWith(Foo.FOO1)\n" +
            "class ATest { }";
        String javaContents =
            "enum Foo {\n" +
            "FOO1, FOO2\n" +
            "} \n" +
            "@interface RunWith {\n" +
                "Foo value();\n" +
            "}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, javaContents, getIndexOf(contents, "FOO"));
        proposalExists(proposals, "FOO1", 1);
    }

    public void testCompleteFullyQualifiedTypeInScript() throws Exception {
        String contents = "javax.swing.text.html.HTMLDocume";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "HTMLDocume"));
        proposalExists(proposals, "HTMLDocument", 1, true);
    }

    public void testCompleteFullyQualifiedTypeInClass() throws Exception {
        String contents = "class Foo { javax.swing.text.html.HTMLDocume }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "HTMLDocume"));
        proposalExists(proposals, "HTMLDocument", 1, true);
    }

    public void testCompleteFullyQualifiedTypeInMethod() throws Exception {
        String contents = "class Foo { def x() { javax.swing.text.html.HTMLDocume } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "HTMLDocume"));
        proposalExists(proposals, "HTMLDocument", 1, true);
    }

    public void testCompleteFullyQualifiedTypeInMethodParams() throws Exception {
        String contents = "class Foo { def x(javax.swing.text.html.HTMLDocume) { } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "HTMLDocume"));
        proposalExists(proposals, "HTMLDocument", 1, true);
    }

    public void testCompleteFullyQualifiedTypeInImports() throws Exception {
        String contents = "import javax.swing.text.html.HTMLDocume";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "HTMLDocume"));
        proposalExists(proposals, "HTMLDocument", 1, true);
    }

    public void testCompletePackageInClass() throws Exception {
        String contents = "class Foo { javax.swing.text.html.p }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".p"));
        proposalExists(proposals, "javax.swing.text.html.parser", 1, true);
        // ensure no type proposals exist
        proposalExists(proposals, "Icons", 0, true);
    }

    public void testCompletePackageInMethod() throws Exception {
        String contents = "class Foo { def x() { javax.swing.text.html.p } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".p"));
        proposalExists(proposals, "javax.swing.text.html.parser", 1, true);
        // ensure no type proposals exist
        proposalExists(proposals, "Icons", 0, true);
    }

    public void testCompletePackageInMethodParams() throws Exception {
        String contents = "class Foo { def x(javax.swing.text.html.p ) { } }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".p"));
        proposalExists(proposals, "javax.swing.text.html.parser", 1, true);
        // ensure no type proposals exist
        proposalExists(proposals, "Icons", 0, true);
    }

    public void testCompletePackageInImports() throws Exception {
        String contents = "import javax.swing.text.html.p";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".p"));
        proposalExists(proposals, "javax.swing.text.html.parser", 1, true);
        // ensure no type proposals exist
        proposalExists(proposals, "Icons", 0, true);
    }

    public void testCompleteClass1() throws Exception {
        String contents = "class Foo { }\n def x \n Foo.clas";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".clas"));
        proposalExists(proposals, "class", 1, true);
    }

    public void testCompleteClass2() throws Exception {
        String contents = "class Foo { }\n Foo.class.canonicalName";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".canonicalName"));
        proposalExists(proposals, "canonicalName", 1, true);
    }

    public void testCompleteClass3() throws Exception {
        String contents = "class Foo { }\n Foo.class.getCanonicalName";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, ".getCanonicalName"));
        proposalExists(proposals, "getCanonicalName", 1, true);
    }

    public void testGRECLIPSE673() throws Exception {
        String contents = "throw new MPE";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "MPE"));
        // found twice:  once as a type proposal and once as a constructor proposal
        proposalExists(proposals, "MissingPropertyExceptionNoStack", 2, true);
    }
    
    public void testAnnotation1() throws Exception {
        String contents = "@Single class Foo { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "@Single"));
        proposalExists(proposals, "Singleton", 1, true);
        proposalExists(proposals, "SingletonASTTransformation", 0, true);
    }
    
    public void testAnnotation2() throws Exception {
        // not exactly right since @Singlton is only allowed on classes, but good enoug for testing
        String contents = "class Foo { @Single  def foo() {} }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "@Single"));
        proposalExists(proposals, "Singleton", 1, true);
        proposalExists(proposals, "SingletonASTTransformation", 0, true);
    }
    public void testAnnotation3() throws Exception {
        // not exactly right since @Singlton is only allowed on classes, but good enoug for testing
        String contents = "class Foo { @Single  def foo }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "@Single"));
        proposalExists(proposals, "Singleton", 1, true);
        proposalExists(proposals, "SingletonASTTransformation", 0, true);
    }
    public void testAnnotation4() throws Exception {
        // not exactly right since @Singlton is only allowed on classes, but good enoug for testing
        String contents = "@Single import java.util.List\nclass Foo { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "@Single"));
        proposalExists(proposals, "Singleton", 1, true);
        proposalExists(proposals, "SingletonASTTransformation", 0, true);
    }

    public void testField1() throws Exception {
        String contents = "class TestF {\n	JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }

    public void testField2() throws Exception {
        String contents = "class TestF {\n	private JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }

    public void testField3() throws Exception {
        String contents = "class TestF {\n	public JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }

    public void testField4() throws Exception {
        String contents = "class TestF {\n	protected JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }

    public void testField5() throws Exception {
        String contents = "class TestF {\n	public static JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }

    public void testField6() throws Exception {
        String contents = "class TestF {\n	public final JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }

    public void testField7() throws Exception {
        String contents = "class TestF {\n	public static final JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame", 1, true);
    }
}
