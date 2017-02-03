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
package org.codehaus.groovy.eclipse.codeassist.tests;

import static org.eclipse.jdt.ui.PreferenceConstants.TYPEFILTER_ENABLED;

import junit.framework.Test;
import org.codehaus.groovy.eclipse.test.EclipseTestSetup;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that type completions are working properly.
 */
public final class TypeCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(TypeCompletionTests.class);
    }

    private static final String HTML = "HTML";
    private static final String HTML_PROPOSAL = "HTML - javax.swing.text.html";

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
        String contents = "class Foo {\ndef x() {\nHTML\n}}";
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
        String contents = "class Foo {\nHTML\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }

    public void testCompletionTypesInExtends() throws Exception {
        String contents = "class Foo extends HTML { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, HTML));
        proposalExists(proposals, HTML_PROPOSAL, 1);
    }

    public void testCompletionTypesInImplements() throws Exception {
        String contents = "class Foo implements HTMLAnchElem { }";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "HTMLAnchElem"));
        proposalExists(proposals, "HTMLAnchorElement - org.w3c.dom.html", 1);
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
        // found twice: once as a type proposal and once as a constructor proposal
        proposalExists(proposals, "MissingPropertyExceptionNoStack", 2, true);
    }

    public void testField1() throws Exception {
        String contents = "class Foo {\n	JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testField2() throws Exception {
        String contents = "class Foo {\n	private JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testField3() throws Exception {
        String contents = "class Foo {\n	public JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testField4() throws Exception {
        String contents = "class Foo {\n	protected JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testField5() throws Exception {
        String contents = "class Foo {\n	public static JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testField6() throws Exception {
        String contents = "class Foo {\n	public final JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testField7() throws Exception {
        String contents = "class Foo {\n	public static final JFr\n}";
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, "JFr"));
        proposalExists(proposals, "JFrame - javax.swing", 1, true);
    }

    public void testTypeFilter1() throws Exception {
        try {
            EclipseTestSetup.setJavaPreference(TYPEFILTER_ENABLED, "javax.swing.JFrame");
            ICompletionProposal[] proposals = createProposalsAtOffset("JFr", 2);
            proposalExists(proposals, "JFrame - javax.swing", 0, true);
        } finally {
            EclipseTestSetup.setJavaPreference(TYPEFILTER_ENABLED, "");
        }
    }

    public void testTypeFilter2() throws Exception {
        try {
            EclipseTestSetup.setJavaPreference(TYPEFILTER_ENABLED, "javax.swing.*");
            ICompletionProposal[] proposals = createProposalsAtOffset("JFr", 2);
            proposalExists(proposals, "JFrame - javax.swing", 0, true);
        } finally {
            EclipseTestSetup.setJavaPreference(TYPEFILTER_ENABLED, "");
        }
    }
}
