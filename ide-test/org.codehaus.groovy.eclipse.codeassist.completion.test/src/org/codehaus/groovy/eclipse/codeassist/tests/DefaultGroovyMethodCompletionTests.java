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

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Test;

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssistActivator;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Tests DefaultGroovyMethods that they appear when and where expected
 *
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 */
public final class DefaultGroovyMethodCompletionTests extends CompletionTestCase {

    private static final String CONTENTS = "class Class { public Class() {\n }\n void doNothing(int x) { this.toString(); new Object().toString(); } }";
    private static final String SCRIPTCONTENTS = "def x = 9\nx++\nnew Object().toString()\nnew Thread().startD";
    private static final String CLOSURECONTENTS = "def x = { t -> print t }";
    private static final String LISTCONTENTS = "[].findA";

    public static Test suite() {
        return newTestSuite(DefaultGroovyMethodCompletionTests.class);
    }

    private void setDGMFilter(String... filter) {
        GroovyContentAssistActivator.getDefault().setFilteredDGMs(new HashSet<String>(Arrays.asList(filter)));
    }

    private ICompilationUnit createJava() throws Exception {
        return addJavaSource(CONTENTS, "Class", "");
    }

    private ICompilationUnit createGroovy() throws Exception {
        return addGroovySource(CONTENTS, "Class", "");
    }

    private ICompilationUnit createGroovyForScript() throws Exception {
        return addGroovySource(SCRIPTCONTENTS, "Script", "");
    }

    private ICompilationUnit createGroovyForClosure() throws Exception {
        return addGroovySource(CLOSURECONTENTS, "Closure", "");
    }

    private ICompilationUnit createGroovyWithContents(String name, String contents) throws Exception {
        return addGroovySource(contents, name, "");
    }

    //--------------------------------------------------------------------------

    // should not find dgm here
    public void testDGMInJavaFile() throws Exception {
        ICompilationUnit unit = createJava();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "this."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 0);
    }

    // should find dgm here
    public void testDGMInMethodScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "this."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInMethodScopeFromOther() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "new Object()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInConstructorScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class() {\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInScriptScope() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "\n"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInScriptOtherClassScope() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Object()."), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 1);
    }

    // should not find dgm here
    public void testDGMInClassScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class() { }"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 0);
    }
    // should not find dgm here
    public void testDGMInMethodParamScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "void doNothing("), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 0);
    }
    // should not find dgm here
    public void testDGMInConstructorParamScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class("), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 0);
    }
    // should not find dgm here
    public void testDGMInModuleScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "; } }"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 0);
    }
    // should find dgm here
    public void testDGMInClosure() throws Exception {
        ICompilationUnit unit = createGroovyForClosure();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CLOSURECONTENTS, " t -> "), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "identity", 1);
    }

    // tests DefaultGroovyStaticMethods
    public void testDGSM() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Thread().startD"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "startDaemon", 2);
    }

    // tests GRECLIPSE-1013
    public void testPopertyVariantOfDGM() throws Exception {
        String contents = "''.toURL().text";
        ICompilationUnit unit = createGroovyWithContents("Script", contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "toURL().t"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "text", 1);
    }

    // tests GRECLIPSE-1158
    public void testDateGM() throws Exception {
        String contents = "new Date().toCal";
        ICompilationUnit unit = createGroovyWithContents("Script", contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "toCal"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "toCalendar", 1);
    }

    // tests GRECLIPSE-1158
    public void testProcessGM() throws Exception {
        String contents = "Process p\n" +
                "p.get";
        ICompilationUnit unit = createGroovyWithContents("Script", contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "get"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "getIn", 1);
    }

    // tests GRECLIPSE-1158
    public void testEncodingGM() throws Exception {
        String contents = "byte[] p\n" +
                "p.encodeBase64";
        ICompilationUnit unit = createGroovyWithContents("Script", contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "encodeBase64"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "encodeBase64", 2);
    }

    // tests GRECLIPSE-1158
    public void testXmlGM() throws Exception {
        String contents = "org.w3c.dom.NodeList p\n" +
                "p.iterator";
        ICompilationUnit unit = createGroovyWithContents("Script", contents);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "iterator"), GroovyCompletionProposalComputer.class);
        proposalExists(proposals, "iterator", 1);
    }

    // GRECLIPSE-1182
    public void testDGMFilter1() throws Exception {
        try {
            setDGMFilter("inspect");
            String contents = "this.insp";
            ICompilationUnit unit = createGroovyWithContents("Script", contents);
            ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer.class);
            proposalExists(proposals, "inspect", 0);
            setDGMFilter();
            proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer.class);
            proposalExists(proposals, "inspect", 1);
        } finally {
            setDGMFilter();
        }
    }

    // GRECLIPSE-1182
    public void testDGMFilter2() throws Exception {
        try {
            setDGMFilter("inspect","each","fsafd fdafsd fafds");
            String contents = "this.insp";
            ICompilationUnit unit = createGroovyWithContents("Script", contents);
            ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer.class);
            proposalExists(proposals, "inspect", 0);
            setDGMFilter();
            proposals = performContentAssist(unit, getIndexOf(contents, "insp"), GroovyCompletionProposalComputer.class);
            proposalExists(proposals, "inspect", 1);
        } finally {
            setDGMFilter();
        }
    }

    // GRECLIPSE-1422
    public void testNoDups() throws Exception {
        ICompilationUnit unit = createGroovyWithContents("Script", LISTCONTENTS);
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(LISTCONTENTS, "findA"), GroovyCompletionProposalComputer.class);
        // should find 2, not 4.  dups removed
        proposalExists(proposals, "findAll", 2);
    }
}
