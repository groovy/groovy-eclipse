/*
 * Copyright 2009-2020 the original author or authors.
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
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

final class OtherCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
    }

    @Test
    void testNoNullPointerException() {
        String contents = 'getClass().'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'toGenericString()', 1)
    }

    @Test // GRECLIPSE-414
    void testNoIndexOutOfBoundsException() {
        String contents = '''\
            |class Test {
            |  int i
            |  Test() {
            |    this.i = 42
            |  }
            |  Test(Test other) {
            |    this.i = other.i
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this.'))
        proposalExists(proposals, 'i', 1)
    }

    @Test
    void testCategoryMethodDisplayString() {
        addJavaSource '''\
            |public class StringExtension {
            |  public static String bar(String self) {
            |    return self;
            |  }
            |}
            |'''.stripMargin(), 'StringExtension'

        String contents = '''\
            |class Test {
            |  void meth() {
            |    String foo = 'foo'
            |    use (StringExtension) {
            |      foo.bar()
            |    }
            |    this.collect
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'foo.ba'))
        ICompletionProposal proposal = findFirstProposal(proposals, 'bar')
        assert proposal.displayString == 'bar() : String - StringExtension (Groovy)'

        proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this.collect'))
        Arrays.sort(proposals) { ICompletionProposal o1, ICompletionProposal o2 ->
            o2.displayString <=> o1.displayString
        }
        proposalExists(proposals, 'collect', 3)
        assert proposals[0].displayString == 'collect(Collection<T> collector, Closure<? extends T> transform) : Collection<T> - DefaultGroovyMethods (Groovy)'
        assert proposals[1].displayString == 'collect(Closure<T> transform) : List<T> - DefaultGroovyMethods (Groovy)'
        assert proposals[2].displayString == 'collect() : Collection - DefaultGroovyMethods (Groovy)'
    }

    @Test // GROOVY-5245
    void testCategoryMethodPropertyFilter() {
        addJavaSource '''\
            |public class StringExtension {
            |  public static boolean isBar(String self) {
            |    return self;
            |  }
            |}
            |'''.stripMargin(), 'StringExtension'

        String contents = '''\
            |class Test {
            |  void meth() {
            |    String foo = 'foo'
            |    use (StringExtension) {
            |      foo.ba
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'foo.ba'))
        proposalExists(proposals, 'isBar()', 1)
        proposalExists(proposals, 'bar', 0)
    }

    @Test
    void testGString1() {
        String contents = '"${new String().c}"'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.c'))
        proposalExists(proposals, 'center', 2)
    }

    @Test
    void testGString2() {
        String contents = '"""${new String().c}"""'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.c'))
        proposalExists(proposals, 'center', 2)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInInitializers1() {
        String contents = 'class A { { aa }\n def aaaa }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'aa'))
        proposalExists(proposals, 'aaaa', 1)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInInitializers2() {
        String contents = 'class A { {  }\n def aaaa }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '{ { '))
        proposalExists(proposals, 'aaaa', 1)
    }

    @Test
    void testContentAssistInInitializers3() {
        String contents = 'class A { { getCan } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'getCan'))
        proposalExists(proposals, 'getCanonicalName', 0)
    }

    @Test // GRECLIPSE-706
    void testContentAssistInStaticInitializers1() {
        String contents = 'class A { static { aa }\n static aaaa }'
        applyProposalAndCheck(checkUniqueProposal(contents, '{ aa', 'aaaa'), contents.replace('{ aa }', '{ aaaa }'))
    }

    @Test // GRECLIPSE-706
    void testContentAssistInStaticInitializers2() {
        String contents = 'class A { static {  }\n static aaaa }'
        applyProposalAndCheck(checkUniqueProposal(contents, 'static { ', 'aaaa'), contents.replace('{  }', '{ aaaa }'))
    }

    @Test
    void testContentAssistInStaticInitializers3() {
        String contents = 'class A { static { getCan } }'
        applyProposalAndCheck(checkUniqueProposal(contents, 'getCan', 'getCanonicalName()'), contents.replace('getCan', 'getCanonicalName()'))
    }

    @Test
    void testContentAssistInStaticInitializers4() {
        String contents = 'class A { public static final String NAME = getCan }'
        applyProposalAndCheck(checkUniqueProposal(contents, 'getCan', 'getCanonicalName()'), contents.replace('getCan', 'getCanonicalName()'))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/374
    void testContentAssistInStaticInitializers5() {
        String contents = 'class A { public static final String NAME = canNam }'
        applyProposalAndCheck(checkUniqueProposal(contents, 'canNam', 'canonicalName', 'this.canonicalName'), contents.replace('canNam', 'this.canonicalName'))
    }

    @Test
    void testContentAssistInStaticInitializers6() {
        String contents = 'class A { static { getMeta } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'get'))
        proposalExists(proposals, 'getMetaPropertyValues', 1)
        proposalExists(proposals, 'getMetaClass', 1)
    }

    @Test // GRECLIPSE-692
    void testMethodWithSpaces() {
        String contents = 'class A { def "ff f"()  { ff } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '{ ff'))
        checkReplacementString(proposals, '"ff f"()', 1)
    }

    @Test // GRECLIPSE-692
    void testMethodWithSpaces2() {
        String contents = 'class A { def "fff"()  { fff } }'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '{ fff'))
        checkReplacementString(proposals, 'fff()', 1)
    }

    @Test // STS-1165 content assist after a static method call was broken
    void testAfterStaticCall() {
        String contents = 'class A { static xxx(x) { }\n def something() {\nxxx oth }\ndef other}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'oth'))
        checkReplacementString(proposals, 'other', 1)
    }

    @Test
    void testArrayCompletion1() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].x'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'x'))
        checkReplacementString(proposals, 'xx', 1)
    }

    @Test
    void testArrayCompletion2() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].getX'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'getX'))
        checkReplacementString(proposals, 'getXx()', 1)
    }

    @Test
    void testArrayCompletion3() {
        String contents = 'class XX { \nXX[] xx\nXX yy }\nnew XX().xx[0].setX'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'setX'))
        checkReplacementString(proposals, 'setXx(value)', 1)
    }

    @Test
    void testArrayCompletion4() {
        String contents = '''\
            |class XX {
            |  XX[] xx
            |  XX yy
            |}
            |new XX().getXx()[0].x
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'x'))
        checkReplacementString(proposals, 'xx', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1004
    void testEnumCompletion1() {
        String contents = 'enum E {F,G}\nE.F.n'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'n'))
        checkReplacementString(proposals, 'next()', 2)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1004
    void testEnumCompletion2() {
        String contents = 'enum E {F,G}\nE.F.p'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'p'))
        checkReplacementString(proposals, 'previous()', 2)
    }

    @Test
    void testEnumCompletion3() {
        String contents = 'enum E {F,G}\nE.v'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'v'))
        checkReplacementString(proposals, 'values()', 1)
        checkReplacementString(proposals, 'valueOf(name)', 1)
        checkReplacementString(proposals, 'valueOf(enumType, name)', 1)
    }

    @Test
    void testEnumCompletion4() {
        String contents = 'enum E {F,G}\nE.M'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'M'))
        checkReplacementString(proposals, 'MIN_VALUE', 1)
        checkReplacementString(proposals, 'MAX_VALUE', 1)
    }

    @Test
    void testListCompletion1() {
        String contents = '[].'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, ['removeAll(arg0)', 'removeAll(c)'] as String[], 1)
    }

    @Test
    void testListCompletion2() {
        String contents = '[].re'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.re'))
        checkReplacementString(proposals, ['removeAll(arg0)', 'removeAll(c)'] as String[], 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion1() {
        String contents = '[1,2,3]*.intValue()[0].value'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion2() {
        String contents = '[1,2,3]*.intValue()[0].value'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.va'))
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion3() {
        String contents = '[x:1,y:2,z:3]*.getKey()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, 'getKey()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion4() {
        String contents = '[x:1,y:2,z:3]*.getKey()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.get'))
        checkReplacementString(proposals, 'getKey()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion5() {
        String contents = '[x:1,y:2,z:3]*.key[0].toLowerCase()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, 'toLowerCase()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion6() {
        String contents = '[x:1,y:2,z:3]*.key[0].toLowerCase()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.to'))
        checkReplacementString(proposals, 'toLowerCase()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion7() {
        String contents = '[x:1,y:2,z:3]*.value[0].intValue()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, 'intValue()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion8() {
        String contents = '[x:1,y:2,z:3]*.value[0].intValue()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.int'))
        checkReplacementString(proposals, 'intValue()', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion9() {
        String contents = '[1,2,3]*.value[0].value'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion10() {
        String contents = '[1,2,3]*.value[0].value'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.val'))
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion11() {
        String contents = '[1,2,3]*.value'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // GRECLIPSE-1165
    void testSpreadCompletion12() {
        String contents = '[1,2,3]*.value'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.val'))
        checkReplacementString(proposals, 'value', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/763
    void testSpreadCompletion13() {
        String contents = '[[[1,2,3]]]*.val'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        checkReplacementString(proposals, 'value', 1)
    }

    @Test
    void testSpreadCompletion14() {
        String contents = '[[[x:1,y:2,z:3]]]*.val'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        checkReplacementString(proposals, 'values()', 1)
    }

    @Test
    void testSwitchCompletion1() {
        addGroovySource('enum E { ONE, TWO, THREE }', 'E', 'p')
        String contents = '''\
            |void meth(p.E e) {
            |  switch (e) {
            |  case E
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'E'))
        proposalExists(proposals, 'E - p', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/762
    void testSwitchCompletion2() {
        String contents = '''\
            |enum E { ONE, TWO, THREE }
            |void meth(E e) {
            |  switch (e) {
            |  case E.T
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'T'))
        proposalExists(proposals, 'THREE', 1)
        proposalExists(proposals, 'TWO', 1)
        proposalExists(proposals, 'ONE', 0)
    }

    @Test // GRECLIPSE-1388
    void testBeforeScriptCompletion() {
        String contents = '\n\ndef x = 9'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '\n'))
        assertProposalOrdering(proposals, 'binding')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/371
    void testCompileStaticCompletion1() {
        String contents = '''\
            |import groovy.transform.*
            |class Bean {
            |  URL url
            |}
            |class Main {
            |  @CompileStatic
            |  static main(args) {
            |    Bean b = new Bean()
            |    b.with {
            |      url.
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'getProtocol', 1)
        proposalExists(proposals, 'protocol', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/371
    void testCompileStaticCompletion2() {
        String contents = '''\
            |import groovy.transform.*
            |class Bean {
            |  URL url
            |}
            |class Main {
            |  @CompileStatic
            |  static main(args) {
            |    Bean b = new Bean()
            |    b.with {
            |      url.pr
            |    }
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.pr'))
        proposalExists(proposals, 'protocol', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/391
    void testCompileStaticCompletion3() {
        String contents = '''\
            |import javax.swing.JFrame
            |import groovy.transform.*
            |@CompileStatic
            |enum E {
            |  A() {
            |    @Override
            |    String method(JFrame frame) {
            |      fr
            |    }
            |  }
            |
            |  abstract String method(JFrame jf);
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'fr'))
        proposalExists(proposals, 'frame', 1)
    }
}
