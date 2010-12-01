/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Dec 1, 2010
 * 
 * Tests that Completions show static import proposals
 */
public class StaticImportsCompletionTests extends CompletionTestCase {

    public StaticImportsCompletionTests(String name) {
        super(name);
    }
    
    public void testStaticImportField() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.NULL_ATTRIBUTE_VALUE\nNULL_ATTRIBUTE_VALUE";
        ICompilationUnit unit = create(contents);
        fullBuild();
        expectingNoProblems();
        
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "NULL_ATTRIBUTE_VALUE"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "NULL_ATTRIBUTE_VALUE", 1);
    }
    public void testStaticImportMethod() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.getAttributeKey\ngetAttributeKey";
        ICompilationUnit unit = create(contents);
        fullBuild();
        
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(contents, "getAttributeKey"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getAttributeKey", 1);
    }
    public void testStaticStarImportField() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.*\nNULL_ATTRIBUTE_VALUE";
        ICompilationUnit unit = create(contents);
        fullBuild();
        expectingNoProblems();
        
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "NULL_ATTRIBUTE_VALUE"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "NULL_ATTRIBUTE_VALUE", 1);
    }
    public void testStaticStarImportMethod() throws Exception {
        String contents = "import static javax.swing.text.html.HTML.*\ngetAttributeKey";
        ICompilationUnit unit = create(contents);
        fullBuild();
        expectingNoProblems();
        
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "getAttributeKey"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getAttributeKey", 1);
    }
}
