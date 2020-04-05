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

import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Test

/**
 * Tests that content assist works as expected for inner classes.
 */
final class InnerTypeCompletionTests extends CompletionTestSuite {

    private ICompletionProposal assertProposalCreated(String contents, String expression, String expectedProposal) {
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, expression))
        proposalExists(proposals, expectedProposal, 1)
        findFirstProposal(proposals, expectedProposal)
    }

    @Test
    void testInInnerClass1() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    HTML
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testInInnerClass2() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def x(HTML) {
            |      ;
            |    }
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testInInnerClass3() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def x() {
            |      HTML
            |    }
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    @Test
    void testInInnerClass4() {
        String contents = '''\
            |class Outer {
            |  class Inner extends HTML {
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'HTML', 'HTML - javax.swing.text.html')
    }

    //

    @Test
    void testInnerClass1() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    Inner f
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'Inner', 'Inner - Outer')
    }

    @Test
    void testInnerClass2() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    private Inner f
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'Inner', 'Inner - Outer')
    }

    @Test
    void testInnerClass3() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - Outer'), contents.replaceFirst(/Inn\b/, 'Inner'))
    }

    @Test
    void testInnerClass4() {
        String contents = '''\
            |Map.Ent
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Ent', 'Entry - java.util.Map'), contents.replace('Ent', 'Entry'))
    }

    @Test
    void testInnerClass5() {
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        testInnerClass4() // no difference; no qualifier should be inserted
    }

    @Test
    void testInnerClass6() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'f'

        String contents = '''\
            |import f.Outer
            |Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - f.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass7() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'g'

        String contents = '''\
            |g.Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - g.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass8() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'h'

        String contents = '''\
            |import h.Outer
            |class Foo extends Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - h.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass9() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'i'

        String contents = '''\
            |import s.Outer
            |class Foo implements Outer.Inn
            |'''.stripMargin()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'Inn'))
        assert !proposals
    }

    @Test
    void testInnerClass10() {
        addGroovySource '''\
            |class Outer {
            |  interface Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'j'

        String contents = '''\
            |import j.Outer
            |class Foo extends Outer.Inn
            |'''.stripMargin()
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'Inn'))
        assert !proposals
    }

    @Test
    void testInnerClass11() {
        addGroovySource '''\
            |class Outer {
            |  interface Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'k'

        String contents = '''\
            |import k.Outer
            |class Foo implements Outer.Inn
            |'''.stripMargin()
            applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - k.Outer'), contents.replace('Inn', 'Inner'))
    }

    @Test
    void testInnerClass12() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'l'

        String contents = '''\
            |Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - l.Outer'), '''\
            |import l.Outer
            |
            |Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass13() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'm'

        String contents = '''\
            |Outer.Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - m.Outer'), '''\
            |m.Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass14() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |    class Nucleus {
            |    }
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'n'

        String contents = '''\
            |n.Outer.Inner.N
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'N', 'Nucleus - n.Outer.Inner'), contents.replace('N', 'Nucleus'))
    }

    @Test
    void testInnerClass15() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'o'

        String contents = '''\
            |Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - o.Outer'), '''\
            |import o.Outer.Inner
            |
            |Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass16() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'p'

        String contents = '''\
            |Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - p.Outer'), '''\
            |p.Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass17() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'q'

        String contents = '''\
            |new Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - q.Outer'), '''\
            |import q.Outer
            |
            |new Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass18() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'r'

        String contents = '''\
            |new Outer.Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(assertProposalCreated(contents, 'Inn', 'Inner - r.Outer'), '''\
            |new r.Outer.Inner
            |'''.stripMargin())
    }

    @Test
    void testInnerClass19() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 's'

        def unit = addGroovySource '''\
            |class Other {
            |  Inn
            |}
            |'''.stripMargin(), nextUnitName(), 's'

        def proposals = createProposalsAtOffset(unit, getLastIndexOf(String.valueOf(unit.contents), 'Inn'))
        applyProposalAndCheck(findFirstProposal(proposals, 'Inner - s.Outer'), '''\
            |package s;
            |
            |import s.Outer.Inner
            |
            |class Other {
            |  Inner
            |}
            |'''.stripMargin())
    }

    @Test
    void testInnerClass20() {
        addGroovySource '''\
            |class Outer {
            |  class Inner {
            |  }
            |}
            |'''.stripMargin(), 'Outer', 't'

        def unit = addGroovySource '''\
            |class Other {
            |  Outer.Inn
            |}
            |'''.stripMargin(), nextUnitName(), 't'

        def proposals = createProposalsAtOffset(unit, getLastIndexOf(String.valueOf(unit.contents), 'Inn'))
        applyProposalAndCheck(findFirstProposal(proposals, 'Inner - t.Outer'), '''\
            |package t;
            |
            |class Other {
            |  Outer.Inner
            |}
            |'''.stripMargin())
    }

    @Test
    void testInnerClass21() {
        def unit = addGroovySource '''\
            |class Outer {
            |  class Inner {
            |    class Point {
            |    }
            |    Poi p
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'u'

        def proposals = createProposalsAtOffset(unit, getLastIndexOf(String.valueOf(unit.contents), 'Poi'))
        applyProposalAndCheck(findFirstProposal(proposals, 'Point - u.Outer.Inner'), '''\
            |package u;
            |
            |class Outer {
            |  class Inner {
            |    class Point {
            |    }
            |    Point p
            |  }
            |}
            |'''.stripMargin())
    }

    @Test
    void testInnerClass22() {
        def unit = addGroovySource '''\
            |class Outer {
            |  class Inner {
            |    class Point {
            |    }
            |  }
            |  Poi p
            |}
            |'''.stripMargin(), 'Outer', 'v'

        def proposals = createProposalsAtOffset(unit, getLastIndexOf(String.valueOf(unit.contents), 'Poi'))
        applyProposalAndCheck(findFirstProposal(proposals, 'Point - v.Outer.Inner'), '''\
            |package v;
            |
            |import v.Outer.Inner.Point
            |
            |class Outer {
            |  class Inner {
            |    class Point {
            |    }
            |  }
            |  Point p
            |}
            |'''.stripMargin())
    }

    @Test
    void testInnerClass23() {
        def unit = addGroovySource '''\
            |class Outer {
            |  class Inner {
            |    class Point {
            |    }
            |  }
            |  Inner.Poi p
            |}
            |'''.stripMargin(), 'Outer', 'w'

        def proposals = createProposalsAtOffset(unit, getLastIndexOf(String.valueOf(unit.contents), 'Poi'))
        applyProposalAndCheck(findFirstProposal(proposals, 'Point - w.Outer.Inner'), '''\
            |package w;
            |
            |class Outer {
            |  class Inner {
            |    class Point {
            |    }
            |  }
            |  Inner.Point p
            |}
            |'''.stripMargin())
    }

    //

    @Test
    void testInnerMember1() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def xxx
            |    def y() {
            |      xxx
            |    }
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember2() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def xxx
            |  }
            |  Inner i
            |  def y() {
            |    i.xxx
            |  }
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember3() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def xxx
            |  }
            |}
            |def y(Outer.Inner i) {
            |  i.xxx
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember4() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def getXxx() {}
            |  }
            |}
            |def y(Outer.Inner i) {
            |  i.xxx
            |}
            |'''.stripMargin()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember5() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def xxx
            |  }
            |}
            |Outer.Inner i
            |i.xxx
            |'''.stripMargin()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }

    @Test
    void testInnerMember6() {
        String contents = '''\
            |class Outer {
            |  class Inner {
            |    def getXxx() {}
            |  }
            |}
            |Outer.Inner i
            |i.xxx
            |'''.stripMargin()
        assertProposalCreated(contents, 'xxx', 'xxx')
    }
}
