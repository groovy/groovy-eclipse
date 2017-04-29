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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssistActivator
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests DefaultGroovyMethods that they appear when and where expected.
 */
final class DefaultGroovyMethodCompletionTests extends CompletionTestSuite {

    private static final String CONTENTS = "class Class { public Class() {\n }\n void doNothing(int x) { this.toString(); new Object().toString(); } }"
    private static final String SCRIPTCONTENTS = "def x = 9\nx++\nnew Object().toString()\nnew Thread().startD"
    private static final String CLOSURECONTENTS = "def x = { t -> print t }"
    private static final String LISTCONTENTS = "[].findA"

    private void setDGMFilter(String... filter) {
        GroovyContentAssistActivator.getDefault().setFilteredDGMs(filter as Set)
    }

    private ICompilationUnit createJava() {
        return addJavaSource(CONTENTS, "Class", "")
    }

    private ICompilationUnit createGroovy() {
        return addGroovySource(CONTENTS, "Class", "")
    }

    private ICompilationUnit createGroovyForScript() {
        return addGroovySource(SCRIPTCONTENTS, "Script", "")
    }

    private ICompilationUnit createGroovyForClosure() {
        return addGroovySource(CLOSURECONTENTS, "Closure", "")
    }

    private ICompilationUnit createGroovyWithContents(String name, String contents) {
        return addGroovySource(contents, name, "")
    }

    //--------------------------------------------------------------------------

    @Test
    void testDGMInJavaFile() {
        ICompilationUnit unit = createJava()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "this."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 0)
    }

    @Test
    void testDGMInMethodScope() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "this."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 1)
    }

    @Test
    void testDGMInMethodScopeFromOther() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "new Object()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 1)
    }

    @Test
    void testDGMInConstructorScope() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class() {\n"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 1)
    }

    @Test
    void testDGMInScriptScope() {
        ICompilationUnit unit = createGroovyForScript()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "\n"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 1)
    }

    @Test
    void testDGMInScriptOtherClassScope() {
        ICompilationUnit unit = createGroovyForScript()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Object()."), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 1)
    }

    @Test
    void testDGMInClassScope() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class() { }"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 0)
    }

    @Test
    void testDGMInMethodParamScope() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "void doNothing("), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 0)
    }

    @Test
    void testDGMInConstructorParamScope() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class("), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 0)
    }

    @Test
    void testDGMInModuleScope() {
        ICompilationUnit unit = createGroovy()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "; } }"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 0)
    }

    @Test
    void testDGMInClosure() {
        ICompilationUnit unit = createGroovyForClosure()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CLOSURECONTENTS, " t -> "), GroovyCompletionProposalComputer)
        proposalExists(proposals, "identity", 1)
    }

    @Test
    void testDGSM() {
        ICompilationUnit unit = createGroovyForScript()
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Thread().startD"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "startDaemon", 2)
    }

    @Test // GRECLIPSE-1013
    void testPopertyVariantOfDGM() {
        String contents = "''.toURL().text"
        ICompilationUnit unit = createGroovyWithContents("Script", contents)
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "toURL().t"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "text", 1)
    }

    @Test // GRECLIPSE-1158
    void testDateGM() {
        String contents = "new Date().toCal"
        ICompilationUnit unit = createGroovyWithContents("Script", contents)
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "toCal"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "toCalendar", 1)
    }

    @Test // GRECLIPSE-1158
    void testProcessGM() {
        String contents = "Process p\n" +
                "p.get"
        ICompilationUnit unit = createGroovyWithContents("Script", contents)
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "get"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "getIn", 1)
    }

    @Test // GRECLIPSE-1158
    void testEncodingGM() {
        String contents = "byte[] p\n" +
                "p.encodeBase64"
        ICompilationUnit unit = createGroovyWithContents("Script", contents)
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "encodeBase64"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "encodeBase64", 2)
    }

    @Test // GRECLIPSE-1158
    void testXmlGM() {
        String contents = "org.w3c.dom.NodeList p\n" +
                "p.iterator"
        ICompilationUnit unit = createGroovyWithContents("Script", contents)
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "iterator"), GroovyCompletionProposalComputer)
        proposalExists(proposals, "iterator", 1)
    }

    @Test // GRECLIPSE-1182
    void testDGMFilter1() {
        try {
            setDGMFilter("inspect")
            String contents = "this.insp"
            ICompilationUnit unit = createGroovyWithContents("Script", contents)
            ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer)
            proposalExists(proposals, "inspect", 0)
            setDGMFilter()
            proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer)
            proposalExists(proposals, "inspect", 1)
        } finally {
            setDGMFilter()
        }
    }

    @Test // GRECLIPSE-1182
    void testDGMFilter2() {
        try {
            setDGMFilter("inspect", "each", "fsafd fdafsd fafds")
            String contents = "this.insp"
            ICompilationUnit unit = createGroovyWithContents("Script", contents)
            ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer)
            proposalExists(proposals, "inspect", 0)
            setDGMFilter()
            proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer)
            proposalExists(proposals, "inspect", 1)
        } finally {
            setDGMFilter()
        }
    }

    @Test // GRECLIPSE-1422
    void testNoDups() {
        ICompilationUnit unit = createGroovyWithContents("Script", LISTCONTENTS)
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(LISTCONTENTS, "findA"), GroovyCompletionProposalComputer)
        // should find 2, not 4.  dups removed
        proposalExists(proposals, "findAll", 2)
    }
}
