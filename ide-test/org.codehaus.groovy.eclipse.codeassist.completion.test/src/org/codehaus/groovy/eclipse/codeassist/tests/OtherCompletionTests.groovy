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
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

/**
 * Tests specific bug reports.
 */
final class OtherCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.PARAMETER_GUESSING, false)
    }

    @Test // GRECLIPSE-414
    void testNoIndexOutOfBoundsException() {
        String contents = '''\
            public class Test {
              int i
              Test() {
                this.i = 42
              }
              Test(Test other) {
                this.i = other.i
              }
            }
            '''.stripIndent()
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, 'this.'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'i', 1)
    }

    @Test // GRECLIPSE-422: type signatures were popping up in various places in the display string
    void testCategoryMethodDisplayString() {
        addJavaSource '''\
            public class StringExtension {
              public static String bar(String self) {
                return self;
              }
            }
            '''.stripIndent(), 'StringExtension'

        String contents = '''\
            public class MyClass {
              public void foo() {
                String foo = 'foo'
                use (StringExtension) {
                  foo.bar()
                }
                this.collect
              }
            }
            '''.stripIndent()
        def gunit = addGroovySource(contents)
        ICompletionProposal[] proposals = performContentAssist(gunit, getIndexOf(contents, 'foo.ba'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'bar', 1)
        assert proposals[0].displayString == 'bar() : String - StringExtension (Groovy)'

        proposals = performContentAssist(gunit, getIndexOf(contents, 'this.collect'), GroovyCompletionProposalComputer)
        Arrays.sort(proposals, { ICompletionProposal o1, ICompletionProposal o2 ->
            o2.displayString <=> o1.displayString
        } as Comparator<ICompletionProposal>)
        proposalExists(proposals, 'collect', 3)
        assert proposals[0].displayString ==~ /collect\(Collection \w+, Closure \w+\) : Collection - DefaultGroovyMethods \(Groovy\)/ : printProposals(proposals)
        assert proposals[1].displayString ==~ /collect\(Closure \w+\) : List - DefaultGroovyMethods \(Groovy\)/ : printProposals(proposals)
        assert proposals[2].displayString ==~ /collect\(\) : Collection - DefaultGroovyMethods \(Groovy\)/ : printProposals(proposals)
    }

    @Test
    void testVisibility() {
        String contents = '''\
            class B { }
            class C {
              B theB
            }
            new C().th
            '''.stripIndent()
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '().th'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'theB', 1)
        assert proposals[0].displayString == 'theB : B - C'
    }

    @Test
    void testGString1() {
        String contents = '"${new String().c}"'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '.c'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'center', 2)
    }

    @Test
    void testGString2() {
        String contents = '"""${new String().c}"""'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '.c'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'center', 2)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInInitializers1() {
        String contents = 'class A { { aa }\n def aaaa }'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, 'aa'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'aaaa', 1)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInInitializers2() {
        String contents = 'class A { {  }\n def aaaa }'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '{ { '), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'aaaa', 1)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInStaticInitializers1() {
        String contents = 'class A { static { aa }\n static aaaa }'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, 'aa'), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'aaaa', 1)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInStaticInitializers2() {
        String contents = 'class A { static {  }\n static aaaa }'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, 'static { '), GroovyCompletionProposalComputer)
        proposalExists(proposals, 'aaaa', 1)
    }

    @Test // GRECLIPSE-692
    void testMethodWithSpaces() {
        String contents = 'class A { def "ff f"()  { ff } }'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '{ ff'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, '"ff f"()', 1)
    }

    @Test // GRECLIPSE-692
    void testMethodWithSpaces2() {
        String contents = 'class A { def "fff"()  { fff } }'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '{ fff'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'fff()', 1)
    }

    @Test // STS-1165 content assist after a static method call was broken
    void testAfterStaticCall() {
        String contents = 'class A { static xxx(x) { }\n def something() {\nxxx oth }\ndef other}'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, 'oth'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'other', 1)
    }

    @Test
    void testArrayCompletion1() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].x'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, 'x'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'xx', 1)
    }

    @Test
    void testArrayCompletion2() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].getX'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, 'getX'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'getXx()', 1)
    }

    @Test
    void testArrayCompletion3() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].setX'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, 'setX'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'setXx(value)', 1)
    }

    @Test
    void testArrayCompletion4() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().getXx()[0].x'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, 'x'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'xx', 1)
    }

    @Test
    void testListCompletion1() {
        String contents = '[].'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, ['removeAll(arg0)', 'removeAll(c)'] as String[], 1)
    }

    @Test
    void testListCompletion2() {
        String contents = '[].re'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.re'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, ['removeAll(arg0)', 'removeAll(c)'] as String[], 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion1() {
        String contents = '[1,2,3]*.intValue()[0].value'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion2() {
        String contents = '[1,2,3]*.intValue()[0].value'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.va'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion3() {
        String contents = '[x:1,y:2,z:3]*.getKey()'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'getKey()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion4() {
        String contents = '[x:1,y:2,z:3]*.getKey()'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.get'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'getKey()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion5() {
        String contents = '[x:1,y:2,z:3]*.key[0].toLowerCase()'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'toLowerCase()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion6() {
        String contents = '[x:1,y:2,z:3]*.key[0].toLowerCase()'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.to'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'toLowerCase()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion7() {
        String contents = '[x:1,y:2,z:3]*.value[0].intValue()'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'intValue()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion8() {
        String contents = '[x:1,y:2,z:3]*.value[0].intValue()'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.int'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'intValue()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion9() {
        String contents = '[1,2,3]*.value[0].value'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion10() {
        String contents = '[1,2,3]*.value[0].value'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.val'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion11() {
        String contents = '[1,2,3]*.value'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion12() {
        String contents = '[1,2,3]*.value'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getLastIndexOf(contents, '.val'), GroovyCompletionProposalComputer)
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1388
    void testBeforeScript() {
        String contents = '\n\ndef x = 9'
        ICompletionProposal[] proposals = performContentAssist(addGroovySource(contents), getIndexOf(contents, '\n'), GroovyCompletionProposalComputer)
        assertProposalOrdering(proposals, 'binding')
    }
}
