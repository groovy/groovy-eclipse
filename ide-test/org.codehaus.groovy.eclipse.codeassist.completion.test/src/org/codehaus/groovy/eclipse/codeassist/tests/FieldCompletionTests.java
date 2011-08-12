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
    
    
    // now repeat the tests above. but with content assist on method calls instead of constructor calls
    public void testProperties1a() throws Exception {
        String contents = "class Other { def x } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 1);
        proposalExists(proposals, "setX", 1);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties2a() throws Exception {
        String contents = "class Other { public def x } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties3a() throws Exception {
        String contents = "class Other { private def x } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties4a() throws Exception {
        String contents = "class Other { public static final int x = 9 } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    public void testProperties5a() throws Exception {
        String contents = "def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        
        // java class...no properties
        env.addClass(env.getProject("Project").getFolder("src").getFullPath(), "Other", "class Other { int x = 9; }");
        fullBuild();
        
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 0);
        proposalExists(proposals, "setX", 0);
        proposalExists(proposals, "x", 1);
    }
    
    // GRECLIPSE-1162
    // 'is' method proposals
    public void testProperties6() throws Exception {
        String contents = "class Other { boolean x } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getX", 1);
        proposalExists(proposals, "setX", 1);
        proposalExists(proposals, "isX", 1);
        proposalExists(proposals, "x", 1);
    }
    
    // GRECLIPSE-1162
    // 'is' method proposals
    public void testProperties6a() throws Exception {
        String contents = "class Other { boolean xx } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getXx", 1);
        proposalExists(proposals, "setXx", 1);
        proposalExists(proposals, "isXx", 1);
        proposalExists(proposals, "xx", 1);
    }
    
    // GRECLIPSE-1162
    // 'is' method proposals
    public void testProperties7() throws Exception {
        String contents = "class Other { boolean isX() {} } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "isX", 1);
        proposalExists(proposals, "x", 1);
    }
    
    // GRECLIPSE-1162
    // 'is' method proposals
    public void testProperties7a() throws Exception {
        String contents = "class Other { boolean isXx() {} } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "isXx", 1);
        proposalExists(proposals, "xx", 1);
    }
    
    // GRECLIPSE-1162
    // 'is' method proposals
    public void testProperties8() throws Exception {
        String contents = "class Other { boolean isxx() {} } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "isxx", 1);
        proposalExists(proposals, "xx", 0);
    }
    
    // GRECLIPSE-1162
    // 'get' method proposals
    public void testProperties9() throws Exception {
        String contents = "class Other { boolean getxx() {} } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getxx", 1);
        proposalExists(proposals, "xx", 0);
    }
    
    public void testClosure1() throws Exception {
        String contents = "class Other { def xxx = { a, b -> }  } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        // the field
        proposalExists(proposals, "xxx", 2);
        // the method
        proposalExists(proposals, "xxx(Object a, Object b)", 1);
    }
    public void testClosure2() throws Exception {
        String contents = "class Other { def xxx = { int a, int b -> }  } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        // the field
        proposalExists(proposals, "xxx", 2);
        // the method
        proposalExists(proposals, "xxx(int a, int b)", 1);
    }
    public void testClosure3() throws Exception {
        String contents = "class Other { def xxx = { }  } \n def o = new Other()\no.x";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        // the field
        proposalExists(proposals, "xxx", 2);
        // the method
        proposalExists(proposals, "xxx()", 1);
    }
    
    // GRECLIPSE-1175
    public void testInitializer1() throws Exception {
        String contents = 
                "class MyClass {\n" + 
        		"    def something = Class.\n" + 
        		"}";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "forName", 2);  // two public and one private
    }
}
