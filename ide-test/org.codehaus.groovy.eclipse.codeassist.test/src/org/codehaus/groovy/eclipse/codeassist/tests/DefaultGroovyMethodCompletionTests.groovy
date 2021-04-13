/*
 * Copyright 2009-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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

    private static final String CONTENTS = '''\
        |class Type {
        |  public Type(int x) {
        |  }
        |  void doNothing(int x) {
        |    this.toString();
        |    new Object().toString();
        |  }
        |}
        |'''.stripMargin()
    private static final String SCRIPTCONTENTS = '''\
        |def x = 9
        |x++
        |new Object().toString()
        |'''.stripMargin()
    private static final String CLOSURECONTENTS = 'def x = { t -> print t }'

    private void setDGMFilter(String... filter) {
        GroovyContentAssist.default.filteredDGMs = filter as Set
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
        def unit = addJavaSource(CONTENTS, 'Type')
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
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'Type(int x) {\n'))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMInClassScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, '}')) // after ctor
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
    void testDGMInMethodParamScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'void doNothing('))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInConstructorParamScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf(CONTENTS, 'Type('))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInModuleScope() {
        def unit = createGroovy()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getLastIndexOf(CONTENTS, '}'))
        proposalExists(proposals, 'identity', 0)
    }

    @Test
    void testDGMInClosure() {
        def unit = createGroovyForClosure()
        ICompletionProposal[] proposals = createProposalsAtOffset(unit, getIndexOf('def x = { t -> print t }', ' t -> '))
        proposalExists(proposals, 'identity', 1)
    }

    @Test
    void testDGMJavadoc() {
        String contents = '[].so', target = 'so'
        ICompletionProposal[] proposals = orderByRelevance(createProposalsAtOffset(contents, getIndexOf(contents, target)))

        // Java 8 adds default method sort(Comparator) to the List interface
        boolean jdkListSort
        try {
            List.getDeclaredMethod('sort', Comparator)
            jdkListSort = true
        } catch (any) {
            jdkListSort = false
        }

        String info = proposals[jdkListSort ? 1 : 0].proposalInfo.getInfo(null)
        assert info ==~ /(?s)Sorts the Collection\. .*/ : 'CategoryProposalCreator.CategoryMethodProposal.createJavaProposal locates javadoc'
    }

    @Test
    void testDGMParameters() {
        String contents = '[].collect'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'collect'))
        proposalExists(proposals, 'collect(Collection<T> collector, Closure<? extends T> transform)', 1)
        proposalExists(proposals, 'collect(Closure<T> transform)', 1)
        proposalExists(proposals, 'collect()', 1)
    }

    @Test
    void testDGMReference() {
        String contents = '[:].&every'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'every'))
        proposalExists(proposals, 'every(Closure)', 1)
        proposalExists(proposals, 'every()', 1)
    }

    @Test
    void testPropertyDGM() {
        String contents = '''\
            |import java.util.regex.*
            |Matcher m
            |m.co
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.co'))
        proposalExists(proposals, 'count', 1)
    }

    @Test
    void testIrrelevantDGM() {
        String contents = '''\
            |import java.util.regex.*
            |Pattern p
            |p.co
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.co'))
        proposalExists(proposals, 'count', 0) // irrelevant category accessor StringGroovyMethods.getCount(Matcher)
    }

    @Test
    void testPropertyRelevance() {
        String contents = '''\
            |import java.util.regex.*
            |Matcher m
            |m = m.la
            |'''.stripMargin()
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
        String contents = 'def sys = System\nsys.cu'
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

    @Test
    void testNoDups1() {
        ICompletionProposal[] proposals = createProposalsAtOffset('[].collectEnt', 13)
        proposalExists(proposals, 'collectEntries', 4) // (), (Map), (Closure), (Map,Closure)
    }

    @Test // GRECLIPSE-1422
    void testNoDups2() {
        ICompletionProposal[] proposals = createProposalsAtOffset('[].findA', getIndexOf('[].findA', 'findA'))
        proposalExists(proposals, 'findAll', 2) // should find 2, not 4
    }

    @Test
    void testNoDups3() {
        ICompletionProposal[] proposals = createProposalsAtOffset('List<String> strings = []; strings.find', 39)
        proposalExists(proposals, 'find(Closure closure) : T', 1) // not Object
        proposalExists(proposals, 'find() : T', 1) // not Object
    }

    @Test
    void testNoDups4() {
        ICompletionProposal[] proposals = createProposalsAtOffset('List<String> strings = []; strings.findA', 40)
        proposalExists(proposals, 'findAll(Closure closure) : List<T>', 1) // not Collection<T>
        proposalExists(proposals, 'findAll() : List<T>', 1) // not Collection<T>
    }

    @Test
    void testNoExtras() {
        ICompletionProposal[] proposals = createProposalsAtOffset('[].stream().collect()\n', 20)
        proposalExists(proposals, 'collect', 5) // (), (Closure), (Collector), (Collection,Closure), (Supplier,BiConsumer,BiConsumer)
    }
}
