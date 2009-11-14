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
 * @created Jun 5, 2009
 * 
 * Tests that Local variable completions are working properly
 * They should only be active when inside a script or in a closure
 */
public class LocalVariableCompletionTests extends CompletionTestCase {

    private static final String CONTENTS = "class LocalsClass { public LocalsClass() {\n }\n void doNothing(int x) { def xxx\n def xx\n def y = { t -> print t\n }\n } }";
    private static final String SCRIPTCONTENTS = "def xx = 9\ndef xxx\ndef y = { t -> print t\n }\n";
    private static final String SCRIPTCONTENTS2 = "def xx = 9\ndef xxx\ndef y = { t -> print t\n.toString() }\n";
    private static final String SELFREFERENCINGSCRIPT = "def xx = 9\nxx = xx\nxx.abs()";
    
    
    public LocalVariableCompletionTests(String name) {
        super(name);
    }
    
    
    // should not find local vars here
    public void testLocalVarsInJavaFile() throws Exception {
        ICompilationUnit unit = createJava();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "y\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "xxx", 0);
        proposalExists(proposals, "xx", 0);
        proposalExists(proposals, "y", 0);
    }

    // should not find local vars here.. They are calculated by JDT
    public void testLocalVarsInGroovyFile() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "y\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "xxx", 0);
        proposalExists(proposals, "xx", 0);
        proposalExists(proposals, "y", 0);
    }

    // should find local vars here
    public void testLocalVarsInScript() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "}\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "xxx", 1);
        proposalExists(proposals, "xx", 1);
        proposalExists(proposals, "y", 1);
    }

    // should find local vars here
    public void testLocalVarsInClosureInScript() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "print t\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "xxx", 1);
        proposalExists(proposals, "xx", 1);
        proposalExists(proposals, "y", 1);
    }

    // should not find local vars here
    public void testLocalVarsInClosureInScript2() throws Exception {
        ICompilationUnit unit = createGroovyForScript2();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS2, "print t\n.toStr"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "xxx", 0);
        proposalExists(proposals, "xx", 0);
        proposalExists(proposals, "y", 0);
    }

    // should find local vars here
    public void testLocalVarsInClosureInMethod() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "print t\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "xxx", 1);
        proposalExists(proposals, "xx", 1);
        proposalExists(proposals, "y", 1);
    }

    // should not have a stack overflow here
    // see GRECLIPSE-369
    public void testSelfReferencingLocalVar() throws Exception {
        ICompilationUnit unit = createGroovyForSelfReferencingScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getLastIndexOf(SELFREFERENCINGSCRIPT, "xx."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "abs", 1);
    }
    
    private ICompilationUnit createJava() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addClass(src, "LocalsClass", CONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    
    private ICompilationUnit createGroovy() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "LocalsClass", CONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    private ICompilationUnit createGroovyForScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "LocalsScript", SCRIPTCONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    private ICompilationUnit createGroovyForScript2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "LocalsScript2", SCRIPTCONTENTS2);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    private ICompilationUnit createGroovyForSelfReferencingScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "SelfRefScripr", SELFREFERENCINGSCRIPT);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }


}
