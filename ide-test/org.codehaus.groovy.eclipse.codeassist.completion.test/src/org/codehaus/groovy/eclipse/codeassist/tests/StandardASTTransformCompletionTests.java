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

import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Apr 22, 2009
 * 
 * Tests that the standard AST transforms have the proper completion proposals
 */
public class StandardASTTransformCompletionTests extends CompletionTestCase {

    public StandardASTTransformCompletionTests(String name) {
        super(name);
    }
    
    public void testSingleton1() throws Exception {
        String contents = 
            "public @Singleton class Test { }\n" +
            "Test.getInstance()\n" +
            "Test.instance";
        ICompilationUnit unit = create(contents);
        fullBuild();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "Test.getIn"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getInstance", 1);
        proposals = performContentAssist(unit, getIndexOf(contents, "Test.in"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "instance", 1);

    }
    public void testSingleton2() throws Exception {
        String contents = 
            "public @Singleton class Test { }";
        create(contents, "Other");
            
        contents = "Test.getInstance()\nTest.instance";
        ICompilationUnit unit = create(contents);
        performDummySearch(unit.getJavaProject());
        fullBuild();
        // ensure that there is no ArrayIndexOutOfBoundsException thrown.
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "Test.getIn"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getInstance", 1);
        proposals = performContentAssist(unit, getIndexOf(contents, "Test.in"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "instance", 1);
    }
}
