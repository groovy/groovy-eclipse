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

import static org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal.MODIFIER_TOGGLE_COMPLETION_MODE

import groovy.transform.NotYetImplemented

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Tests that constructor completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 */
final class ConstructorCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        // filter some legacy packages
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'sun.*;com.sun.*;org.omg.*')

        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'true')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
    }

    @Test
    void testConstructorCompletion1() {
        String contents = '''\
            |class C {
            |  @Deprecated
            |  C() {}
            |  C(int val) {}
            |}
            |def c = new C
            |'''.stripMargin()
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.ENABLED)
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'C'))

        proposalExists(proposals, 'C()', 0)
        proposalExists(proposals, 'C(int val)', 1)
    }

    @Test
    void testConstructorCompletion2() {
        String contents = 'class YYY { YYY() {} }\nnew YY\nkkk'
        String expected = 'class YYY { YYY() {} }\nnew YYY()\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion3() {
        String contents = 'class YYY { YYY(x) {} }\nnew YY\nkkk'
        String expected = 'class YYY { YYY(x) {} }\nnew YYY(x)\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion4() {
        String contents = 'class YYY { YYY(x, y) {} }\nnew YY\nkkk'
        String expected = 'class YYY { YYY(x, y) {} }\nnew YYY(x, y)\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion5() {
        String contents = 'class YYY { YYY() {} }\nnew YY\nkkk'
        String expected = 'class YYY { YYY() {} }\nnew YYY()\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_INSERT_COMPLETION, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion6() {
        String contents = 'class YYY { YYY() {} }\nnew YY()\nkkk'
        String expected = 'class YYY { YYY() {} }\nnew YYY()\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_INSERT_COMPLETION, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'new YY'))
        applyProposalAndCheck(findFirstProposal(proposals, 'YYY', false), expected) // completion overwrites
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/471
    void testConstructorCompletion7() {
        String contents = 'class YYY { YYY() {} }\nnew YY()\nkkk'
        String expected = 'class YYY { YYY() {} }\nnew YYY()\nkkk'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'new YY'))
        applyProposalAndCheck(findFirstProposal(proposals, 'YYY', false), expected, 0 as char, MODIFIER_TOGGLE_COMPLETION_MODE)
    }

    @Test
    void testContructorCompletionWithClosure1() {
        String contents = '''\
            |class Foo {
            |  Foo(Number number, Closure closure) {
            |    closure()
            |  }
            |}
            |new Foo
            |'''.stripMargin()
        String expected = contents.replace('new Foo', 'new Foo(null, null)')
        checkProposalApplicationNonType(contents, expected, getLastIndexOf(contents, 'Foo'), 'Foo')
    }

    @Test
    void testContructorCompletionWithClosure2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')

        String contents = '''\
            |class Foo {
            |  Foo(Number number, Closure closure) {
            |    closure()
            |  }
            |}
            |new Foo
            |'''.stripMargin()
        String expected = contents.replace('new Foo', 'new Foo(number, closure)')
        checkProposalApplicationNonType(contents, expected, getLastIndexOf(contents, 'Foo'), 'Foo')
    }

    @Test
    void testContructorCompletionWithClosure3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, true)

        String contents = '''\
            |class Foo {
            |  Foo(Number number, Closure closure) {
            |    closure()
            |  }
            |}
            |new Foo
            |'''.stripMargin()
        String expected = contents.replace('new Foo', 'new Foo(number, closure)')
        checkProposalApplicationNonType(contents, expected, getLastIndexOf(contents, 'Foo'), 'Foo')
    }

    @Test
    void testContructorCompletionWithQualifier() {
        String contents = 'new java.text.Anno'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, contents.length())
        proposalExists(proposals, 'AnnotationVisitor', 0)
        proposalExists(proposals, 'Annotation', 1)
    }

    @Test
    void testConstructorCompletionWithGenerics1() {
        String contents = 'List<String> list = new ArrayL'
        String expected = 'List<String> list = new ArrayList()'
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new ArrayL'), 'ArrayList()')
    }

    @Test
    void testContructorCompletionWithinEnumDeclaration1() {
        String contents = '''\
            |class YYY { YYY() {} }
            |enum F {
            |  Aaa() {
            |    @Override int foo() {
            |      new YY
            |    }
            |  }
            |  int foo() {
            |  }
            |}
            |'''.stripMargin()
        String expected = contents.replace('new YY', 'new YYY()')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testContructorCompletionWithinEnumDeclaration2() {
        String contents = '''\
            |class YYY { YYY() {} }
            |enum F {
            |  Aaa { // no parens
            |    @Override int foo() {
            |      new YY
            |    }
            |  }
            |  int foo() {
            |  }
            |}
            |'''.stripMargin()
        String expected = contents.replace('new YY', 'new YYY()')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletionInnerClass1() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'a'

        String contents = '''\
            |new a.Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - a.Outer.Inner', '()'), contents.replace('Inn', 'Inner()'))
    }

    @Test
    void testConstructorCompletionInnerClass2() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'b'

        String contents = '''\
            |new Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - b.Outer.Inner', '()'), '''\
            |import b.Outer
            |
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass3() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'c'

        String contents = '''\
            |new Outer.Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - c.Outer.Inner', '()'), '''\
            |new c.Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass4() {
        addGroovySource '''\
            |class Outer {
            |  static class XyzInner {
            |    XyzInner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'd'

        String contents = '''\
            |new XyzInn
            |'''.stripMargin()
        applyProposalAndCheck(checkUniqueProposal(contents, 'XyzInn', 'XyzInner() - d.Outer.XyzInner', '()'), '''\
            |import d.Outer.XyzInner
            |
            |new XyzInner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass5() {
        addGroovySource '''\
            |class Outer {
            |  static class XyzInner {
            |    XyzInner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'e'

        String contents = '''\
            |new XyzInn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'XyzInn', 'XyzInner() - e.Outer.XyzInner', '()'), '''\
            |new e.Outer.XyzInner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass6() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'f'

        String contents = '''\
            |import f.Outer
            |new Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - f.Outer.Inner', '()'), '''\
            |import f.Outer
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass7() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'g'

        String contents = '''\
            |import g.Outer
            |new Outer.Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - g.Outer.Inner', '()'), '''\
            |import g.Outer
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass8() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'h'

        String contents = '''\
            |import h.*
            |new Outer.Inn
            |'''.stripMargin()
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - h.Outer.Inner', '()'), '''\
            |import h.*
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass9() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'i'

        String contents = '''\
            |import i.*
            |new Outer.Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        applyProposalAndCheck(checkUniqueProposal(contents, 'Inn', 'Inner() - i.Outer.Inner', '()'), '''\
            |import i.*
            |new Outer.Inner()
            |'''.stripMargin())
    }

    @Test
    void testConstructorCompletionInnerClass10() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner(Number number, String string) {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'j'

        String contents = '''\
            |new j.Outer.Inn
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        applyProposalAndCheck(checkUniqueProposal(
            contents, 'Inn', 'Inner(Number number, String string) - j.Outer.Inner', '(number, string)'
        ), contents.replace('Inn', 'Inner(number, string)'))
    }

    @Test
    void testConstructorCompletionInnerClass11() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Inner(Number number, String string) {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'k'

        String contents = '''\
            |new k.Outer.Inner()
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false') // TODO: Should not need to remove the qualifier
        applyProposalAndCheck(checkUniqueProposal(
            contents, '(', 'Inner(Number number, String string) - k.Outer.Inner' - ~/k.Outer./, ''
        ), contents) // context display
    }

    @Test
    void testConstructorCompletionInnerClass12() {
        String contents = '''\
            |new Map.Entry() {
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'Map'))
        proposalExists(proposals, 'Map - java.util', 1)
        proposalExists(proposals, 'MapWithDefault - groovy.lang', 1)
        proposalExists(proposals, 'MapWithDefault(Map<K,V> m, Closure initClosure) - groovy.lang.MapWithDefault', 1)
    }

    @Test
    void testContructorCompletionImportHandling0() {
        String contents = '''\
            |def a = new java.text.Anno
            |'''.stripMargin()
        String expected = '''\
            |def a = new java.text.Annotation(value)
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'Anno'), 'Annotation')
    }

    @Test
    void testContructorCompletionImportHandling1() {
        String contents = '''\
            |def a = new Anno
            |'''.stripMargin()
        String expected = '''\
            |import java.text.Annotation
            |
            |def a = new Annotation(value)
            |'''.stripMargin()
        setJavaPreference(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, 'false')
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new Anno'), 'Annotation')
    }

    @Test @NotYetImplemented
    void testConstructorCompletionCanonicalTransform() {
        String contents = '''\
            |@groovy.transform.Canonical
            |class One {
            |  Number two
            |}
            |class Foo {
            |  def bar() {
            |    def baz = new One
            |  }
            |}
            |'''.stripMargin()
        checkUniqueProposal(contents, 'One', 'One(Number two)', '(null)')
    }

    @Test @NotYetImplemented
    void testConstructorCompletionImmutableTransform() {
        String contents = '''\
            |@groovy.transform.Immutable
            |class One {
            |  Number two
            |}
            |class Foo {
            |  def bar() {
            |    def baz = new One
            |  }
            |}
            |'''.stripMargin()
        checkUniqueProposal(contents, 'One', 'One(Number two)', '(null)')
    }

    @Test @NotYetImplemented
    void testConstructorCompletionInheritConstructorsTransform() {
        String contents = '''\
            |class Num {
            |  Num(Number n) {
            |  }
            |}
            |@groovy.transform.InheritConstructors
            |class One extends Num {
            |}
            |class Foo {
            |  def bar() {
            |    def baz = new One
            |  }
            |}
            |'''.stripMargin()
        checkUniqueProposal(contents, 'One', 'One(Number n)', '(null)')
    }

    @Test
    void testConstructorCompletionSelfConstructorCall0() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(bar) {
            |    th
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'th'))
        proposalExists(proposals, 'this(Object bar)', 0)
        proposalExists(proposals, 'this()', 1)
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall1() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/789
    void testConstructorCompletionSelfConstructorCall1a() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |    this(
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall2() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(arg) {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Object arg)', 0)
        proposalExists(proposals, 'Foo()', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/789
    void testConstructorCompletionSelfConstructorCall2a() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(arg) {
            |    this(
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Object arg)', 0)
        proposalExists(proposals, 'Foo()', 1)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall3() {
        String contents = '''\
            |class Foo {
            |  Foo(arg) {
            |  }
            |  Foo() {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Object arg)', 1)
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/789
    void testConstructorCompletionSelfConstructorCall3a() {
        String contents = '''\
            |class Foo {
            |  Foo(arg) {
            |  }
            |  Foo() {
            |    this(
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Object arg)', 1)
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall4() {
        String contents = '''\
            |class Foo {
            |  Foo(... args) {
            |  }
            |  Foo() {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Object... args)', 1)
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall5() {
        String contents = '''\
            |class Foo {
            |  Foo(String param, other = 'value') {
            |  }
            |  Foo() {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(String param, Object other)', 1)
        proposalExists(proposals, 'Foo(String param)', 1)
        proposalExists(proposals, 'Foo()', 0)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall6() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(String param, other = 'value') {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(String param, Object other)', 0)
        proposalExists(proposals, 'Foo(String param)', 0)
        proposalExists(proposals, 'Foo()', 1)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall7() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(Map.Entry param, other = 'value') {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Entry param, Object other)', 0)
        proposalExists(proposals, 'Foo(Entry param)', 0)
        proposalExists(proposals, 'Foo()', 1)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall8() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(List<String> param, other = 'value') {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(List param, Object other)', 0)
        proposalExists(proposals, 'Foo(List param)', 0)
        proposalExists(proposals, 'Foo()', 1)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall9() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |  }
            |  Foo(String[][][] param, other = 'value') {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(String[][][] param, Object other)', 0)
        proposalExists(proposals, 'Foo(String[][][] param)', 0)
        proposalExists(proposals, 'Foo()', 1)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall10() {
        String contents = '''\
            |class Foo {
            |  Foo(Date utilDate) {
            |  }
            |  Foo(java.sql.Date sqlDate) {
            |    this()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Date sqlDate)', 0)
        proposalExists(proposals, 'Foo(Date utilDate)', 1)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall11() {
        String contents = '''\
            |class Foo {
            |  Foo(Date utilDate) {
            |    this()
            |  }
            |  Foo(java.sql.Date sqlDate) {
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Date sqlDate)', 1)
        proposalExists(proposals, 'Foo(Date utilDate)', 0)
    }

    @Test
    void testConstructorCompletionSelfConstructorCall12() {
        String contents = '''\
            |import java.util.Map as Dictionary
            |class Foo {
            |  Foo(Dictionary d) {
            |  }
            |  Foo(Dictionary.Entry e) {
            |    this(Collections.singletonMap(e.key, e.value))
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'this('))
        proposalExists(proposals, 'Foo(Entry e)', 0)
        proposalExists(proposals, 'Foo(Map d)', 1)
    }

    @Test
    void testConstructorCompletionSuperConstructorCall0() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |    su
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'su'))
        proposalExists(proposals, 'Object()', 0)
        proposalExists(proposals, 'super()', 1)
    }

    @Test
    void testConstructorCompletionSuperConstructorCall1() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |    super()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'Object()', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/789
    void testConstructorCompletionSuperConstructorCall1a() {
        String contents = '''\
            |class Foo {
            |  Foo() {
            |    super(
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'Object()', 1)
    }

    @Test
    void testConstructorCompletionSuperConstructorCall2() {
        String contents = '''\
            |class Bar {
            |  Bar() {}
            |  Bar(arg) {}
            |  Bar(... args) {}
            |}
            |class Foo extends Bar {
            |  Foo() {
            |    super()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'Bar()', 1)
        proposalExists(proposals, 'Bar(Object arg)', 1)
        proposalExists(proposals, 'Bar(Object... args)', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/789
    void testConstructorCompletionSuperConstructorCall2a() {
        String contents = '''\
            |class Bar {
            |  Bar() {}
            |  Bar(arg) {}
            |  Bar(... args) {}
            |}
            |class Foo extends Bar {
            |  Foo() {
            |    super(
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'Bar()', 1)
        proposalExists(proposals, 'Bar(Object arg)', 1)
        proposalExists(proposals, 'Bar(Object... args)', 1)
    }

    @Test
    void testConstructorCompletionSuperConstructorCall3() {
        String contents = '''\
            |class Bar {
            |  def baz
            |}
            |class Foo extends Bar {
            |  Foo() {
            |    super()
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'baz : __', 0)
        proposalExists(proposals, 'Bar()', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/789
    void testConstructorCompletionSuperConstructorCall3a() {
        String contents = '''\
            |class Bar {
            |  def baz
            |}
            |class Foo extends Bar {
            |  Foo() {
            |    super(
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'baz : __', 0)
        proposalExists(proposals, 'Bar()', 1)
    }

    //--------------------------------------------------------------------------

    @Test
    void testNamedArgs1() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |new Foo()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs2() {
        addGroovySource('''\
            |class Foo {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Foo')

        String contents = 'new Foo()' // separate source

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs3() {
        addGroovySource('''\
            |class Foo {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Foo', 'p')

        String contents = 'new p.Foo()' // fully-qualified reference

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs4() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |new Foo(aaa:'1', )
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, ', '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs5() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |new Foo(aaa:'1', bbb:2, )
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, ', '))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs6() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |new Foo(aaa:'1', bbb:2, ccc:null)
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
    }

    @Test // STS-2628: ensure no double adding of named properties for booleans
    void testNamedArgs7() {
        String contents = '''\
            |class Foo {
            |  boolean aaa
            |  boolean bbb
            |  boolean ccc
            |}
            |new Foo()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs8() {
        String contents = '''\
            |class Foo {
            |  String bar
            |  private String baz
            |  def setBaz(String baz) { this.baz = baz }
            |}
            |new Foo()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'bar : __', 1)
        proposalExists(proposals, 'baz : __', 1)
    }

    @Test // explicit no-arg constructor exists
    void testNamedArgs9() {
        String contents = '''\
            |class Foo {
            |  Foo() { }
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |new Foo()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
    }

    @Test
    void testNamedArgs10() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  Number bbb
            |}
            |new Foo(a)
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 0)
    }

    @Test
    void testNamedArgs11() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  Number abc
            |}
            |new Foo(a)
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'abc : __', 1)
    }

    @Test
    void testNamedArgs12() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  void setAbc(Number abc) {}
            |}
            |new Foo(a)
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'abc : __', 1)
    }

    @Test
    void testNamedArgs13() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  void setXyz(Number xyz) {}
            |}
            |new Foo(a)
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'a'))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'xyz : __', 0)
    }

    @Test
    void testNamedArgs14() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Number number
            |    String string
            |    Inner() {}
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'pack'

        String contents = '''\
            new pack.Outer.Inner()
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'number : __', 1)
        proposalExists(proposals, 'string : __', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/404
    void testNamedArgs15() {
        addGroovySource '''\
            |class Outer {
            |  static class Inner {
            |    Number number
            |    String string
            |  }
            |}
            |'''.stripMargin(), 'Outer', 'qual'

        String contents = '''\
            |import qual.Outer
            |new Outer.Inner()
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'number : __', 1)
        proposalExists(proposals, 'string : __', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/766
    void testNamedArgs16() {
        addGroovySource '''\
            |class One {
            |  String foo
            |}
            |class Two extends One {
            |  String bar
            |}
            |class Three extends Two {
            |  String baz
            |}
            |'''.stripMargin(), 'Types', 'pack'

        String contents = '''\
            |new pack.Three()
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'foo : __', 1)
        proposalExists(proposals, 'bar : __', 1)
        proposalExists(proposals, 'baz : __', 1)
    }

    @Test
    void testNamedArgs17() {
        String contents = '''\
            |class Foo {
            |  String aaa
            |  String aaaBbbCccDdd
            |  void setAaaBbbCcc(Object value) {}
            |}
            |new Foo(aBC)
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'aBC'))
        proposalExists(proposals, 'aaaBbbCccDdd : __', 1)
        proposalExists(proposals, 'aaaBbbCcc : __', 1)
        proposalExists(proposals, 'aaa : __', 0)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/991
    void testNamedArgs18() {
        String contents = '''\
            |class Foo {
            |  String abc
            |}
            |new Foo(abc: "xyz")
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'abc: '))
        /*java.lang.NullPointerException
            at o.c.g.e.codeassist.processors.TypeCompletionProcessor.doTypeCompletion(TypeCompletionProcessor.java:133)
            at o.c.g.e.codeassist.processors.TypeCompletionProcessor.generateProposals(TypeCompletionProcessor.java:71)
            at o.c.g.e.codeassist.requestor.GroovyCompletionProposalComputer.computeCompletionProposals(GroovyCompletionProposalComputer.java:221)
        */
    }

    @Test // explicit no-arg and tuple constructors exist
    void testNoNamedArgs() {
        String contents = '''\
            |class Foo {
            |  Foo() { }
            |  Foo(a,b,c) { }
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |new Foo()
            |'''.stripMargin()

        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'Foo', 2)
    }

    @Test
    void testNamedArgumentTrigger1() {
        addGroovySource '''\
            |class Foo {
            |  Number number
            |  String string
            |}
            |'''.stripMargin()

        String contents = 'new Foo()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        applyProposalAndCheck(findFirstProposal(proposals, 'number : __'), 'new Foo(number: __,)', ',' as char)
    }

    @Test @Ignore
    void testNamedArgumentTrigger2() {
        addGroovySource '''\
            |class Foo {
            |  Number number
            |  String string
            |}
            |'''.stripMargin()

        String contents = 'new Foo()'
        setJavaPreference(PreferenceConstants.EDITOR_SMART_SEMICOLON, 'true')
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        applyProposalAndCheck(findFirstProposal(proposals, 'number : __'), 'new Foo(number: __);', ';' as char)
    }

    @Test
    void testParamGuessing1() {
        addGroovySource('''\
            |class Flar {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |import p.Flar
            |String xxx
            |int yyy
            |boolean zzz
            |new Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing2() {
        addGroovySource('''\
            |class Flar {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |String xxx
            |int yyy
            |boolean zzz
            |new p.Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing3() {
        addGroovySource('''\
            |class Flar {
            |  String aaa
            |  int bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |import p.Flar
            |String xxx
            |Integer yyy
            |boolean zzz
            |new Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing4() {
        addGroovySource('''\
            |class Flar {
            |  String aaa
            |  Integer bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |import p.Flar
            |String xxx
            |Integer yyy
            |boolean zzz
            |new Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing5() {
        addGroovySource('''\
            |class Flar {
            |  String aaa
            |  Integer bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |import p.Flar
            |String xxx
            |int yyy
            |boolean zzz
            |new Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __', expectedChoices)
    }

    @Test
    void testParamGuessing6() {
        addGroovySource('''\
            |class Flar {
            |  String aaa
            |  Integer bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |import p.Flar
            |String xxx
            |int yyy
            |boolean zzz
            |new Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'xxx', '""' ]
        checkProposalChoices(contents, 'Flar(', 'aaa', 'aaa: __', expectedChoices)
    }

    @Test
    void testParamGuessing7() {
        addGroovySource('''\
            |class Flar {
            |  Closure aaa
            |  Integer bbb
            |  Date ccc
            |}
            |'''.stripMargin(), 'Flar', 'p')

        String contents = '''\
            |import p.Flar
            |Closure xxx
            |int yyy
            |boolean zzz
            |new Flar()
            |'''.stripMargin()

        String[] expectedChoices = [ 'xxx', '{  }' ]
        checkProposalChoices(contents, 'Flar(', 'aaa', 'aaa: __', expectedChoices)
    }

    @Test
    void testParamGuessTrigger1() {
        addGroovySource '''\
            |class Foo {
            |  Number number
            |  String string
            |}
            |'''.stripMargin()

        def proposal = checkUniqueProposal('new Foo()', 'new Foo(', 'number', 'number: __')

        applyProposalAndCheck(proposal, 'new Foo(number: __)')
        //TODO:applyProposalAndCheck(proposal.choices[0], 'new Foo(number: null,)', ',' as char)
        applyProposalAndCheck(new Document('new Foo(number: __)'), proposal.choices[0], 'new Foo(number: null)')
    }
}
