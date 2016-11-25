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
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 *
 * Tests that Local variable completions are working properly
 * They should only be active when inside a script or in a closure
 */
public final class LocalVariableCompletionTests extends CompletionTestCase {

    public static Test suite() {
        return newTestSuite(LocalVariableCompletionTests.class);
    }

    private static final String CONTENTS = "class LocalsClass { public LocalsClass() {\n }\n void doNothing(int x) { def xxx\n def xx\n def y = { t -> print t\n }\n } }";
    private static final String SCRIPTCONTENTS = "def xx = 9\ndef xxx\ndef y = { t -> print t\n }\n";
    private static final String SCRIPTCONTENTS2 = "def xx = 9\ndef xxx\ndef y = { t -> print t\n.toString() }\n";
    private static final String SELFREFERENCINGSCRIPT = "def xx = 9\nxx = xx\nxx.abs()";

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

    // GRECLIPSE=1267
    public void testClsoureVar1() throws Exception {
        String contents = "def x = { o }";
        String expected = "def x = { owner }";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "{ o"), "owner");
    }

    // GRECLIPSE=1267
    public void testClsoureVar2() throws Exception {
        String contents = "def x = { d }";
        String expected = "def x = { delegate }";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "{ d"), "delegate");
    }

    // GRECLIPSE=1267
    public void testClsoureVar3() throws Exception {
        String contents = "def x = { getO }";
        String expected = "def x = { getOwner() }";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "{ getO"), "getOwner");
    }

    // GRECLIPSE=1267
    public void testClsoureVar4() throws Exception {
        String contents = "def x = { getD }";
        String expected = "def x = { getDelegate() }";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "{ getD"), "getDelegate");
    }

    // GRECLIPSE=1387
    public void testClsoureVar4a() throws Exception {
        String contents = "def x = { thisO }";
        String expected = "def x = { thisObject }";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "{ thisO"), "thisObject");
    }

    // GRECLIPSE=1267
    public void testClsoureVar5() throws Exception {
        String contents = "o\nd\nge";
        ICompilationUnit unit = create(contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "o"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "owner", 0);
        proposals = performContentAssist(unit, getIndexOf(contents, "d"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "delegate", 0);
        proposals = performContentAssist(unit, getIndexOf(contents, "ge"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getDelegate", 0);
        proposalExists(proposals, "getOwner", 0);
    }

    private ICompilationUnit createJava() throws Exception {
        return addJavaSource(CONTENTS, "LocalsClass", "");
    }

    private ICompilationUnit createGroovy() throws Exception {
        return addGroovySource(CONTENTS, "LocalsClass", "");
    }

    private ICompilationUnit createGroovyForScript() throws Exception {
        return addGroovySource(SCRIPTCONTENTS, "LocalsScript", "");
    }

    private ICompilationUnit createGroovyForScript2() throws Exception {
        return addGroovySource(SCRIPTCONTENTS2, "LocalsScript2", "");
    }

    private ICompilationUnit createGroovyForSelfReferencingScript() throws Exception {
        return addGroovySource(SELFREFERENCINGSCRIPT, "SelfRefScripr", "");
    }
}
