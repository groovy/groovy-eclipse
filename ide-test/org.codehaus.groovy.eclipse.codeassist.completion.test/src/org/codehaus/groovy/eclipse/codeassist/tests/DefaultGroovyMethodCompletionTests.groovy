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

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.core.Flags
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests DefaultGroovyMethods that they appear when and where expected.
 */
final class DefaultGroovyMethodCompletionTests extends CompletionTestSuite {

    private static final String CONTENTS = 'class Class { public Class() {\n }\n void doNothing(int x) { this.toString(); new Object().toString(); } }'
    private static final String SCRIPTCONTENTS = 'def x = 9\nx++\nnew Object().toString()'
    private static final String CLOSURECONTENTS = 'def x = { t -> print t }'
    private static final String LISTCONTENTS = '[].findA'

    private void setDGMFilter(String... filter) {
        GroovyContentAssist.default.setFilteredDGMs(filter as Set)
    }

    private org.codehaus.jdt.groovy.model.GroovyCompilationUnit createGroovy() {
        addGroovySource(CONTENTS, nextUnitName())
    }

    private org.codehaus.jdt.groovy.model.GroovyCompilationUnit createGroovyForScript() {
        addGroovySource(SCRIPTCONTENTS, nextUnitName())
    }

    private org.codehaus.jdt.groovy.model.GroovyCompilationUnit createGroovyForClosure() {
        addGroovySource(CLOSURECONTENTS, nextUnitName())
    }

    //--------------------------------------------------------------------------

    @Test
    void testDGMInJavaFile() {
        def unit = addJavaSource(CONTENTS, 'Class')
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'this.'))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInMethodScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'this.'))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMInMethodScopeFromOther() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'new Object().'))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMInConstructorScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'Class() {\n'))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMInScriptScope() {
        def unit = createGroovyForScript()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(SCRIPTCONTENTS, '\n'))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMInScriptOtherClassScope() {
        def unit = createGroovyForScript()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(SCRIPTCONTENTS, 'new Object().'))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMInClassScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'Class() { }'))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInMethodParamScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'void doNothing('))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInConstructorParamScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'Class('))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInModuleScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, '; } }'))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInClosure() {
        def unit = createGroovyForClosure()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CLOSURECONTENTS, ' t -> '))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testPropertyDGM() {
        String contents = '''\
            import java.util.regex.*
            Matcher m
            m.co
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.co'))
        proposalExists(proposals, 'count', 1)
    }

    @Test
    void testIrrelevantDGM() {
        String contents = '''\
            import java.util.regex.*
            Pattern p
            p.co
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.co'))
        proposalExists(proposals, 'count', 0) // irrelevant category accessor StringGroovyMethods.getCount(Matcher)
    }

    @Test
    void testPropertyRelevance() {
        String contents = '''\
            import java.util.regex.*
            Matcher m
            m = m.la
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.la'))
        assertProposalOrdering(orderByRelevance(proposals), 'lastMatcher', 'lastAppendPosition') // lastMatcher has more relevant type
    }

    @Test
    void testThreadDGSM1() {
        String contents = 'Thread.startD'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'startDaemon', 2)
    }

    @Test
    void testThreadDGSM2() {
        String contents = 'new Thread().startD'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'startDaemon', 2)
    }

    @Test
    void testSystemDGSM1() {
        String contents = 'System.class.cu'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'currentTimeSeconds', 1)
    }

    @Test
    void testSystemDGSM2() {
        String contents = 'def sys = System.class\nsys.cu'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        def proposal = findFirstProposal(proposals, 'currentTimeSeconds')
        assert Flags.toString(proposal.proposal.flags).contains('static')
    }

    @Test // GRECLIPSE-1013
    void testPopertyVariantOfDGM() {
        String contents = '"".toURL().text'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'toURL().t'))
        proposalExists(proposals, 'text', 1)
    }

    @Test // GRECLIPSE-1158
    void testDateGM() {
        String contents = 'new Date().toCal'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'toCal'))
        proposalExists(proposals, 'toCalendar', 1)
    }

    @Test // GRECLIPSE-1158
    void testProcessGM() {
        String contents = 'Process p\n' + 'p.get'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'get'))
        proposalExists(proposals, 'getIn', 1)
    }

    @Test // GRECLIPSE-1158
    void testEncodingGM() {
        String contents = 'byte[] p\n' + 'p.encodeBase64'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'encodeBase64'))
        proposalExists(proposals, 'encodeBase64', 2)
    }

    @Test // GRECLIPSE-1158
    void testXmlGM() {
        String contents = 'org.w3c.dom.NodeList p\n' + 'p.iterator'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'iterator'))
        proposalExists(proposals, 'iterator', 1)
    }

    @Test // GRECLIPSE-1182
    void testDGMFilter1() {
        try {
            setDGMFilter('inspect')
            String contents = 'this.insp'
            ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'insp'))
            proposalExists(proposals, 'inspect', 0)
            setDGMFilter()
            proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'insp'))
            proposalExists(proposals, 'inspect', 1)
        } finally {
            setDGMFilter()
        }
    }

    @Test // GRECLIPSE-1182
    void testDGMFilter2() {
        try {
            setDGMFilter('inspect', 'each', 'fsafd fdafsd fafds')
            String contents = 'this.insp'
            ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'insp'))
            proposalExists(proposals, 'inspect', 0)
            setDGMFilter()
            proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'insp'))
            proposalExists(proposals, 'inspect', 1)
        } finally {
            setDGMFilter()
        }
    }

    @Test // GRECLIPSE-1422
    void testNoDups1() {
        ICompletionProposal[] proposals = createProposalsAtOffset(LISTCONTENTS, getIndexOf(LISTCONTENTS, 'findA'))
        // should find 2, not 4.  dups removed
        proposalExists(proposals, 'findAll', 2)
    }

    @Test
    void testNoDups2() {
        ICompletionProposal[] proposals = createProposalsAtOffset('[].collectEnt', 13)
        proposalExists(proposals, 'collectEntries', 4) // collectEntries(), collectEntries(Closure), collectEntries(Map), collectEntries(Map, Closure)
    }
}
