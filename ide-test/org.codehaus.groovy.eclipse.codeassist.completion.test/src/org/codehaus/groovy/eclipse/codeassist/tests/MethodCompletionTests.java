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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Dec 8, 2009
 * 
 * Tests that Method completions are working properly
 */
public class MethodCompletionTests extends CompletionTestCase {

    public MethodCompletionTests(String name) {
        super(name);
    }


    public void testAfterParens1() throws Exception {
        String contents = "HttpRetryException f() {\nnull\n}\nf().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }

    public void testAfterParens2() throws Exception {
        String contents = "HttpRetryException f() {\nnull\n}\nthis.f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }
    
    public void testAfterParens3() throws Exception {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\nnew Super().f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }
    
    public void testAfterParens4() throws Exception {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\nclass Sub extends Super { }\nnew Sub().f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }
    
    public void testAfterParens5() throws Exception {
        String contents = "class Super {HttpRetryException f(arg) {\nnull\n}}\ndef s = new Super()\ns.f(null).";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f(null)."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }
    
    public void testAfterParens6() throws Exception {
        String contents = "class Super {HttpRetryException f() {\nnull\n}}\ndef s = new Super()\ns.f().";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "f()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "cause", 1);
    }
    
    private ICompilationUnit create(String contents) throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "GroovyClass", contents);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
}
