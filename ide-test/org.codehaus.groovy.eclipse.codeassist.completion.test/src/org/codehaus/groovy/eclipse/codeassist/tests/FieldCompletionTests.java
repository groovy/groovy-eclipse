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
 * @created Jun 5, 2009
 * 
 * Tests that Field completions are working properly
 */
public class FieldCompletionTests extends CompletionTestCase {

    public FieldCompletionTests(String name) {
        super(name);
    }
    
    // test that safe dereferencing works
    // should find that someProperty is of type integer
    public void testSafeDeferencing() throws Exception {
        String contents = "public class SomeClass {\nint someProperty\nvoid someMethod() { someProperty?.x}}";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "?."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "abs", 1);
    }
    public void testSpaces1() throws Exception {
        String contents = "public class SomeClass {\nint someProperty\nvoid someMethod() { \nnew SomeClass()    .  \n}}";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "someProperty", 1);
    }
    public void testSpaces2() throws Exception {
        String contents = "public class SomeClass {\nint someProperty\nvoid someMethod() { \nnew SomeClass()    .  \n}}";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, ". "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "someProperty", 1);
    }
    public void testSpaces3() throws Exception {
        String contents = "public class SomeClass {\nint someProperty\nvoid someMethod() { \nnew SomeClass()    .  \n}}";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, ". "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "someProperty", 1);
    }
    
    
    // test some variations on properties
    // GRECLIPSE-616
    public void testProperties1() throws Exception {
        String contents = "class Other { def x } \n new Other().x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 1);
        proposalExists(proposals, "setX", 1);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties2() throws Exception {
        String contents = "class Other { public def x } \n new Other().x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties3() throws Exception {
        String contents = "class Other { private def x } \n new Other().x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties4() throws Exception {
        String contents = "class Other { public static final int x = 9 } \n new Other().x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties5() throws Exception {
        String contents = "new Other().x";
        ICompilationUnit unit = create(contents);
        env.addClass(env.getProject("Project").getFolder("src").getFullPath(), "Other", "class Other { int x = 9; }");
        fullBuild();
        
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
}
