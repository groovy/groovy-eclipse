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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that Completions show static import proposals.
 */
final class StaticImportsCompletionTests extends CompletionTestCase {

    @Test
    void testStaticImportField() {
        String contents = "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\nNULL_ATTRIBUTE_VALUE"
        ICompilationUnit unit = addGroovySource(contents, "File", "")

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "NULL_ATTRIBUTE_VALUE"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "NULL_ATTRIBUTE_VALUE", 1)
    }

    @Test
    void testStaticImportMethod() {
        String contents = "import static javax.swing.text.html.HTML.getAttributeKey\ngetAttributeKey"
        ICompilationUnit unit = addGroovySource(contents, "File", "")

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "getAttributeKey"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "getAttributeKey", 1)
    }

    @Test
    void testStaticStarImportField() {
        String contents = "import static javax.swing.text.html.HTML.*\nNULL_ATTRIBUTE_VALUE"
        ICompilationUnit unit = addGroovySource(contents, "File", "")

        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "NULL_ATTRIBUTE_VALUE"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "NULL_ATTRIBUTE_VALUE", 1)
    }

    @Test
    void testStaticStarImportMethod() {
        String contents = "import static javax.swing.text.html.HTML.*\ngetAttributeKey"
        ICompilationUnit unit = addGroovySource(contents, "File", "")

        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "getAttributeKey"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "getAttributeKey", 1)
    }

    @Test
    void testStaticFieldImport() {
        String contents = "import static java.lang.Boolean.FA"
        ICompilationUnit unit = addGroovySource(contents, "File", "")

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "FA"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "FALSE", 1)
    }

    @Test
    void testStaticMethodImport() {
        String contents = "import static java.lang.Boolean.pa"
        ICompilationUnit unit = addGroovySource(contents, "File", "")

        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "pa"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "parseBoolean", 1)
    }
}
