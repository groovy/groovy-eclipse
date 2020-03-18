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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser
import static org.junit.Assume.assumeTrue

import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jdt.ui.PreferenceConstants
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

final class MethodCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'true')
    }

    @Test
    void testAfterParens1() {
        String contents = '''\
            |HttpRetryException f() { null }
            |f().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens2() {
        String contents = '''\
            |HttpRetryException f() { null }
            |this.f().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens3() {
        String contents = '''\
            |class Super { HttpRetryException f() { null } }
            |new Super().f().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens4() {
        String contents = '''\
            |class Super { HttpRetryException f() { null } }
            |class Sub extends Super { }
            |new Sub().f().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens5() {
        String contents = '''\
            |class Super { HttpRetryException f(arg) { null } }
            |def s = new Super()
            |s.f(null).
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f(null).'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testAfterParens6() {
        String contents = '''\
            |class Super { HttpRetryException f() { null } }
            |def s = new Super()
            |s.f().
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, 'f().'))
        proposalExists(proposals, 'cause', 1)
    }

    @Test
    void testObjectExpr1() {
        String contents = '''\
            |1.
            |p
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test
    void testObjectExpr2() {
        String contents = '''\
            |1.
            |m()
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test
    void testObjectExpr3() {
        String contents = '''\
            |1.
            |'p'
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test
    void testObjectExpr4() {
        String contents = '''\
            |1.
            |'m'()
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test
    void testObjectExpr5() {
        String contents = '''\
            |1.
            |"$p"
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/792
    void testObjectExpr6() {
        String contents = '''\
            |1.
            |"$m"()
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/946
    void testObjectExpr7() {
        String contents = '''\
            |class C {
            |  static Date date(whatever) {
            |  }
            |  static main(args) {
            |    date('x').
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'time', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/946
    void testObjectExpr7a() {
        String contents = '''\
            |class C {
            |  static Date date(whatever) {
            |  }
            |  static main(args) {
            |    date('x').t
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 't'))
        proposalExists(proposals, 'time', 1)
    }

    @Test
    void testObjectExpr8() {
        String contents = '''\
            |class C {
            |  Date date(whatever) {
            |  }
            |  void meth() {
            |    date('x').
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'time', 1)
    }

    @Test
    void testObjectExpr8a() {
        String contents = '''\
            |class C {
            |  Date date(whatever) {
            |  }
            |  void meth() {
            |    date('x').t
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 't'))
        proposalExists(proposals, 'time', 1)
    }

    @Test // GRECLIPSE-1374
    void testParensExpr1() {
        String contents = '''\
            |(1).
            |def u
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // GRECLIPSE-1374
    void testParensExpr2() {
        String contents = '''\
            |(((1))).
            |def u
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test
    void testParensExpr3() {
        String contents = '(((1))).abs()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getIndexOf(contents, '.a'))
        proposalExists(proposals, 'abs', 1)
    }

    @Test // GRECLIPSE-1528
    void testGetterSetter1() {
        String contents = 'class A { private int value\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '\n'))
        proposalExists(proposals, 'getValue', 1)
        proposalExists(proposals, 'setValue', 1)
    }

    @Test
    void testGetterSetter2() {
        String contents = 'class A { private final int value\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '\n'))
        proposalExists(proposals, 'getValue', 1)
        proposalExists(proposals, 'setValue', 0)
    }

    @Test
    void testGetterSetter3() {
        String contents = 'class A { private boolean value\n}'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '\n'))
        proposalExists(proposals, 'isValue', 1)
        proposalExists(proposals, 'setValue', 1)
    }

    @Test
    void testOverride1() {
        String contents = '''\
            |interface I {
            |  String m(List<String> strings, Object[] objects)
            |}
            |class A implements I {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm(List<String> strings, Object[] objects) : String - Override method in \'I\'', 1)
        proposalExists(proposals, 'equals(Object obj) : boolean - Override method in \'Object\'', 1)
    }

    @Test
    void testOverride2() {
        String contents = '''\
            |class A implements Comparable<String> {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'compareTo(String o) : int - Override method in \'Comparable\'', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/711
    void testOverride3() {
        String contents = '''\
            |class A implements Comparable {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'compareTo(Object o) : int - Override method in \'Comparable\'', 1)
    }

    @Test
    void testOverride3a() {
        addGroovySource '''\
            |interface I<T extends CharSequence> {
            |  void m(T chars)
            |}
            |'''.stripMargin()
        String contents = '''\
            |class A implements I {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm(CharSequence chars) : void - Override method in \'I\'', 1)
    }

    @Test
    void testOverride3b() {
        addGroovySource '''\
            |interface I<T extends CharSequence & Serializable> {
            |  void m(T chars)
            |}
            |'''.stripMargin()
        String contents = '''\
            |class A implements I {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm(CharSequence chars) : void - Override method in \'I\'', 1)
    }

    @Test
    void testOverride4() {
        String contents = '''\
            |import java.util.concurrent.Callable
            |class A implements Callable<String> {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'call() : String - Override method in \'Callable\'', 1)
    }

    @Test
    void testOverride5() {
        String contents = '''\
            |// Comparator redeclares equals(Object)
            |class A implements Comparator<String> {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'equals(Object obj) : boolean - Override method in \'Comparator\'', 1)
    }

    @Test
    void testOverride6() {
        addGroovySource '''\
            |trait T {
            |  String getFoo() { 'foo' }
            |}
            |'''.stripMargin()

        String contents = '''\
            |class A implements T {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'getFoo() : String - Override method in \'T\'', 1)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/705
    void testOverride6a() {
        addGroovySource '''\
            |trait T {
            |  String getFoo() { 'foo' }
            |}
            |'''.stripMargin()

        buildProject()

        String contents = '''\
            |class A implements T {
            |  x
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'getFoo() : String - Override method in \'T\'', 1)
    }

    @Test // GRECLIPSE-1752
    void testStatic1() {
        String contents = '''\
            |class A {
            |  static void util() {}
            |  void foo() {
            |    A.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'A.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testStatic2() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class A {
            |  static void util() {
            |  }
            |  void foo() {
            |    A.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'A.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testClass1() {
        String contents = '''\
            |class A {
            |  static void util() {}
            |  void foo() {
            |    A.class.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'A.class.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testClass2() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class A {
            |  static void util() {}
            |  void foo() {
            |    A.class.
            |  }
            |}'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'util', 1)
    }

    @Test
    void testClass3() {
        String contents = '''\
            |import java.util.regex.Pattern
            |Pattern.com
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.com'))
        int release = Integer.parseInt(System.getProperty('java.version').split(/\./)[0])
        proposalExists(proposals, 'componentType', release < 12 ? 1 : 2) // from Class
        proposalExists(proposals, 'compile', 2) // from Pattern
    }

    @Test
    void testClass4() {
        String contents = '''\
            |import java.util.regex.Pattern
            |Pattern.class.com
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.com'))
        int release = Integer.parseInt(System.getProperty('java.version').split(/\./)[0])
        proposalExists(proposals, 'componentType', release < 12 ? 1 : 2) // from Class
        proposalExists(proposals, 'compile', 2) // from Pattern
    }

    @Test
    void testClass5() {
        String contents = '''\
            |import java.util.regex.Pattern
            |def pat = Pattern
            |pat.com
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.com'))
        int release = Integer.parseInt(System.getProperty('java.version').split(/\./)[0])
        proposalExists(proposals, 'componentType', release < 12 ? 1 : 2) // from Class
        proposalExists(proposals, 'compile', 2) // from Pattern
    }

    @Test
    void testClass6() {
        String contents = '''\
            |Class cls
            |cls.can
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'can'))
        proposalExists(proposals, 'canonicalName', 1)
    }

    @Test
    void testClass7() {
        String contents = '''\
            |Class cls
            |cls.get
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'get'))
        proposalExists(proposals, 'getCanonicalName', 1)
    }

    @Test
    void testStaticMethods1() {
        String contents = '''\
            |import java.util.regex.Pattern
            |Pattern.
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'compile', 2) // 2 static, 1 non-static
        proposalExists(proposals, 'flags', 0) // 1 non-static
    }

    @Test
    void testStaticMethods2() {
        addGroovySource '''\
            |abstract class A {
            |  static void someThing() {}
            |}
            |class C extends A {
            |  static void someThang() {}
            |}
            |'''.stripMargin(), 'C', 'p'
        String contents = '''\
            |import p.C
            |C.some
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'some'))
        proposalExists(proposals, 'someThang', 1)
        proposalExists(proposals, 'someThing', 1)
    }

    @Test
    void testImportStaticMethod1() {
        String contents = '''\
            |import static java.util.regex.Pattern.compile
            |comp
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)
    }

    @Test
    void testImportStaticMethod2() {
        addGroovySource '''\
            |abstract class A {
            |  static void someThing() {}
            |}
            |class C extends A {
            |  static void someThang() {}
            |}
            |'''.stripMargin(), 'C', 'p'
        String contents = '''\
            |import static p.C.someThang
            |import static p.C.someThing
            |some
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'some'))
        proposalExists(proposals, 'someThang', 1)
        proposalExists(proposals, 'someThing', 1)
    }

    @Test
    void testImportStaticStarMethod1() {
        String contents = '''\
            |import static java.util.regex.Pattern.*
            |comp
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)
    }

    @Test
    void testImportStaticStarMethod2() {
        addGroovySource '''\
            |abstract class A {
            |  static void someThing() {}
            |}
            |class C extends A {
            |  static void someThang() {}
            |}
            |'''.stripMargin(), 'C', 'p'
        String contents = '''\
            |import static p.C.*
            |some
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'some'))
        proposalExists(proposals, 'someThang', 1)
        proposalExists(proposals, 'someThing', 1)
    }

    @Test
    void testFavoriteStaticStarMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.*')

        String contents = '''\
            |comp
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
            |import static java.util.regex.Pattern.compile
            |
            |compile(regex)
            |'''.stripMargin())
    }

    @Test // these should not produce redundant proposals
    void testFavoriteStaticStarAndImportStaticStarMethod() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.*')

        String contents = '''\
            |import static java.util.regex.Pattern.*
            |comp
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)
    }

    @Test
    void testFavoriteStaticMethod1() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')

        String contents = '''\
            |comp
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
            |import static java.util.regex.Pattern.compile
            |
            |compile(regex)
            |'''.stripMargin())
    }

    @Test
    void testFavoriteStaticMethod2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')
        setJavaPreference(AssistOptions.OPTION_SuggestStaticImports, AssistOptions.DISABLED)
        try {
            String contents = '''\
                |comp
                |'''.stripMargin()
            ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
            proposalExists(proposals, 'compile', 2)

            applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
                |import java.util.regex.Pattern
                |
                |Pattern.compile(regex)
                |'''.stripMargin())
        } finally {
            setJavaPreference(AssistOptions.OPTION_SuggestStaticImports, AssistOptions.ENABLED)
        }
    }

    @Test
    void testFavoriteStaticMethod3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'java.util.regex.Pattern.compile')
        setJavaPreference(PreferenceConstants.CODEASSIST_ADDIMPORT, 'false')

        String contents = '''\
            |comp
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'comp'))
        proposalExists(proposals, 'compile', 2)

        applyProposalAndCheck(findFirstProposal(proposals, 'compile(String regex)'), '''\
            |java.util.regex.Pattern.compile(regex)
            |'''.stripMargin())
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/984
    void testFavoriteStaticMethod4() {
        setJavaPreference(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, 'zzz')

        assert createProposalsAtOffset('zz', 2).length == 0
    }

    @Test
    void testMethodPointer0() {
        String contents = 'class Foo { public static Foo instance }\nFoo.&in'
        proposalExists(createProposalsAtOffset(contents, getLastIndexOf(contents, 'in')), 'instance', 0)
    }

    @Test
    void testMethodPointer0a() {
        String contents = 'class Foo { public static Foo instance }\nFoo::in'
        proposalExists(createProposalsAtOffset(contents, getLastIndexOf(contents, 'in')), 'instance', 0)
    }

    @Test
    void testMethodPointer1() {
        String contents = 'String.&isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer1a() {
        assumeTrue(isParrotParser())
        String contents = 'String::isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer2() {
        String contents = 'String.&  isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer2a() {
        assumeTrue(isParrotParser())
        String contents = 'String::  isE'
        applyProposalAndCheck(checkUniqueProposal(contents, 'isE', 'isEmpty'), contents + 'mpty')
    }

    @Test
    void testMethodPointer3() {
        String contents = 'String.&isEmpty.mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testMethodPointer3a() {
        assumeTrue(isParrotParser())
        String contents = 'String::isEmpty.mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testMethodPointer4() {
        String contents = '(String.&isEmpty).mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testMethodPointer4a() {
        assumeTrue(isParrotParser())
        String contents = '(String::isEmpty).mem'
        applyProposalAndCheck(checkUniqueProposal(contents, 'mem', 'memoize()'), contents + 'oize()')
    }

    @Test
    void testAnnotatedMethod1() {
        String contents = '''\
            |class Foo {
            |  @SuppressWarnings(value=[])
            |  def bar(def baz) {
            |    baz.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'baz.'))
        proposalExists(proposals, 'equals', 1)
    }

    @Test
    void testAnnotatedMethod2() {
        String contents = '''\
            |class Foo {
            |  @SuppressWarnings(value=[])
            |  def bar() {
            |    def baz = whatever()
            |    baz.
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'baz.'))
        proposalExists(proposals, 'equals', 1)
    }

    @Test
    void testIncompleteMethodCall() {
        String contents = '''\
            |class Foo {
            |  void bar(Object param) {
            |    baz(param.getC
            |  }
            |  void baz(Object param) {
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'getC'))
        proposalExists(proposals, 'getClass', 1)
    }

    @Test
    void testStaticInitializerMethod() {
        String contents = '''\
            |class Foo {
            |  static {
            |    println '<clinit>'
            |  }
            |  void bar() {
            |    x
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, '<clinit>', 0)
    }

    @Test
    void testSyntheticBridgeMethod() {
        String contents = '''\
            |class Foo implements Comparable<Foo> {
            |  int compareTo(Foo that) { return 0 }
            |  void bar() {
            |    this.com
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, 'com'))
        proposalExists(proposals, 'compareTo', 1)
    }

    @Test
    void testTrailingClosure1() {
        String contents = 'def foo(Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Closure block)')

        // replacement should be "foo()" with initial cursor inside parens and exit position after parens
        applyProposalAndCheckCursor(proposal, contents + '()', contents.length() + 1, 0, contents.length() + 2)
    }

    @Test
    void testTrailingClosure1a() {
        String contents = 'def foo(Collection items, Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Collection items, Closure block)')

        // replacement should be "foo()" with initial cursor inside parens and exit position after parens
        applyProposalAndCheckCursor(proposal, contents + '()', contents.length() + 1, 0, contents.length() + 2)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/633
    void testTrailingClosure2() {
        String contents = 'def foo(Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Closure block)')

        String expected = contents + ' {  }'
        applyProposalAndCheck(proposal, expected, '{' as char)

        def selection = proposal.getSelection(proposal.@fInvocationContext.document)
        assert selection.x == getLastIndexOf(expected, '{ ') && selection.y == 0
        assert proposal.replacementOffset + proposal.cursorPosition == getLastIndexOf(expected, '{ ')
    }

    @Test
    void testTrailingClosure2a() {
        String contents = 'def foo(Collection items, Closure block) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Collection items, Closure block)')

        def expected = contents + '() {  }'
        applyProposalAndCheck(proposal, expected, '{' as char)

        def selection = proposal.getSelection(proposal.@fInvocationContext.document)
        assert selection.x == getLastIndexOf(expected, '(') && selection.y == 0
        assert proposal.replacementOffset + proposal.cursorPosition == getLastIndexOf(expected, '{ ')
    }

    @Test
    void testTrailingFunctionalInterface() {
        String contents = 'def foo(Comparator c) {}\nfoo'
        setJavaPreference(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, 'false')
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, contents.length()), 'foo(Comparator c)')

        def expected = contents + ' { o1, o2 -> }'
        applyProposalAndCheck(proposal, expected, '{' as char)

        def selection = proposal.getSelection(proposal.@fInvocationContext.document)
        assert selection.x == expected.lastIndexOf('o1') && selection.y == 'o1'.length()
        assert proposal.replacementOffset + proposal.cursorPosition == getLastIndexOf(expected, '->')
    }

    @Test
    void testRangeExpressionCompletion1() {
        String contents = '''\
            |(0..1).
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '.'))
        proposalExists(proposals, 'getTo', 1)
        proposalExists(proposals, 'getFrom', 1)
        proposalExists(proposals, 'isReverse', 1)
        proposalExists(proposals, 'containsWithinBounds', 1)
        // and some proposals from the java.util.List interface
        proposalExists(proposals, 'iterator() : Iterator<E> - List', 1)
        proposalExists(proposals, 'listIterator() : ListIterator<E> - List', 1)
    }

    @Test
    void testRangeExpressionCompletion2() {
        setJavaPreference(PreferenceConstants.CODEASSIST_AUTOACTIVATION, 'true')
        String contents = '''\
            |def range = 0.
            |'''.stripMargin()
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, getLastIndexOf(contents, '.')), 'hashCode')
        char[] triggers = proposal.triggerCharacters
        assert !triggers.contains('.' as char)

        // simulate typing while content assist popup is displayed
        proposal.isPrefix('h', proposal.displayString)

        triggers = proposal.triggerCharacters
        assert triggers.contains('.' as char)
    }

    @Test
    void testRangeExpressionCompletion3() {
        setJavaPreference(PreferenceConstants.CODEASSIST_AUTOACTIVATION, 'true')
        String contents = '''\
            |def other = 0.h
            |'''.stripMargin()
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, getLastIndexOf(contents, 'h')), 'hashCode')
        char[] triggers = proposal.triggerCharacters
        assert triggers.contains('.' as char)

        // simulate typing while content assist popup is displayed
        proposal.isPrefix('', proposal.displayString)

        //triggers = proposal.triggerCharacters
        //assert !triggers.contains('.' as char)
    }

    @Test
    void testRangeExpressionCompletion4() {
        setJavaPreference(PreferenceConstants.CODEASSIST_AUTOACTIVATION, 'true')
        String contents = '''\
            |def other = 0.
            |'''.stripMargin()
        ICompletionProposal proposal = findFirstProposal(
            createProposalsAtOffset(contents, getLastIndexOf(contents, '.')), 'equals')
        char[] triggers = proposal.triggerCharacters
        assert !triggers.contains('.' as char)
    }

    @Test
    void testTraitMethods1() {
        String contents = '''\
            |trait T {
            |  def m1() { x }
            |  private def m2() {}
            |  public static def m3() {}
            |  private static def m4() {}
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm1', 1)
        proposalExists(proposals, 'm2', 1)
        proposalExists(proposals, 'm3', 1)
        proposalExists(proposals, 'm4', 1)
    }

    @Test
    void testTraitMethods2() {
        String contents = '''\
            |trait T {
            |  def m1() {}
            |  private def m2() { x }
            |  public static def m3() {}
            |  private static def m4() {}
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm1', 1)
        proposalExists(proposals, 'm2', 1)
        proposalExists(proposals, 'm3', 1)
        proposalExists(proposals, 'm4', 1)
    }

    @Test
    void testTraitMethods3() {
        String contents = '''\
            |trait T {
            |  def m1() {}
            |  private def m2() {}
            |  public static def m3() { x }
            |  private static def m4() {}
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm1', 0)
        proposalExists(proposals, 'm2', 0)
        proposalExists(proposals, 'm3', 1)
        proposalExists(proposals, 'm4', 1)
    }

    @Test
    void testTraitMethods4() {
        String contents = '''\
            |trait T {
            |  def m1() {}
            |  private def m2() {}
            |  public static def m3() {}
            |  private static def m4() { x }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm1', 0)
        proposalExists(proposals, 'm2', 0)
        proposalExists(proposals, 'm3', 1)
        proposalExists(proposals, 'm4', 1)
    }

    @Test
    void testTraitMethods5() {
        String contents = '''\
            |trait T {
            |  def m1() {}
            |  private def m2() {}
            |  public static def m3() {}
            |  private static def m4() {}
            |}
            |class C implements T {
            |  def m() {
            |    x
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm1', 1)
        proposalExists(proposals, 'm2', 0)
        proposalExists(proposals, 'm3', 1)
        proposalExists(proposals, 'm4', 1)
    }

    @Test
    void testTraitMethods6() {
        String contents = '''\
            |trait T {
            |  def m1() {}
            |  private def m2() {}
            |  public static def m3() {}
            |  private static def m4() {}
            |}
            |class C implements T {
            |  static def m() {
            |    x
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'm1', 0)
        proposalExists(proposals, 'm2', 0)
        proposalExists(proposals, 'm3', 1)
        proposalExists(proposals, 'm4', 1)
    }

    @Test
    void testTraitSyntheticMethods1() {
        String contents = '''\
            |trait T {
            |  private String field1
            |  private static String field2
            |}
            |class C implements T {
            |  def m() {
            |    x
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'T__field1$get', 0)
        proposalExists(proposals, 'T__field1$set', 0)
        proposalExists(proposals, 'T__field2$get', 0)
        proposalExists(proposals, 'T__field2$set', 0)
    }

    @Test
    void testTraitSyntheticMethods2() {
        String contents = '''\
            |trait T {
            |  private String field1
            |  private static String field2
            |}
            |class C implements T {
            |  static def m() {
            |    x
            |  }
            |}
            |'''.stripMargin()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents.replace('x', ''), contents.indexOf('x'))
        proposalExists(proposals, 'T__field1$get', 0)
        proposalExists(proposals, 'T__field1$set', 0)
        proposalExists(proposals, 'T__field2$get', 0)
        proposalExists(proposals, 'T__field2$set', 0)
    }
}
