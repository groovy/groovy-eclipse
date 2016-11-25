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

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests that Completions show static import proposals
 *
 * @author Andrew Eisenberg
 * @created Dec 1, 2010
 */
public final class StaticImportsCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(StaticImportsCompletionTests.class);
    }

    public void testStaticImportField() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\nNULL_ATTRIBUTE_VALUE";
        ICompilationUnit unit = create(contents);

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "NULL_ATTRIBUTE_VALUE"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "NULL_ATTRIBUTE_VALUE", 1);
    }
    public void testStaticImportMethod() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.getAttributeKey\ngetAttributeKey";
        ICompilationUnit unit = create(contents);

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "getAttributeKey"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getAttributeKey", 1);
    }
    public void testStaticStarImportField() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.*\nNULL_ATTRIBUTE_VALUE";
        ICompilationUnit unit = create(contents);

        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "NULL_ATTRIBUTE_VALUE"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "NULL_ATTRIBUTE_VALUE", 1);
    }
    public void testStaticStarImportMethod() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.*\ngetAttributeKey";
        ICompilationUnit unit = create(contents);

        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "getAttributeKey"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getAttributeKey", 1);
    }
    public void testStaticFieldImport() throws Exception {
        String contents = "import static java.lang.Boolean.FA";
        ICompilationUnit unit = create(contents);

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "FA"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "FALSE", 1);
    }
    public void testStaticMethodImport() throws Exception {
        String contents = "import static java.lang.Boolean.co";
        ICompilationUnit unit = create(contents);

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "co"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "compare", 1);
    }
}
