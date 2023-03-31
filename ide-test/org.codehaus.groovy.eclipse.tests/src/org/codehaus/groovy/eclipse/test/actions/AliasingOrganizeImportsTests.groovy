/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.actions

import groovy.test.NotYetImplemented

import org.junit.Test

/**
 * Tests for {@link org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports}.
 */
final class AliasingOrganizeImportsTests extends OrganizeImportsTestSuite {

    @Test
    void testNoSpuriousEdits() {
        String contents = '''\
            |import org.w3c.dom.Node as N
            |N x
            |'''
        doAddImportTest(contents)
    }

    @Test
    void testRetainTypeAlias1() {
        String contents = '''\
            |import org.w3c.dom.Node as N
            |N x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias2() {
        String contents = '''\
            |import org.w3c.dom.Node as N
            |N[] x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias3() {
        // List is a default import
        String contents = '''\
            |import java.util.List as L
            |L list = []
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias4() {
        String contents = '''\
            |import java.util.LinkedList as LL
            |def list = [] as LL
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias5() {
        String contents = '''\
            |import java.util.LinkedList as LL
            |def list = (LL) []
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias6() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E as X
            |X x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias6a() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E as X
            |X.F x = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias6b() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E as X
            |X.F.G x = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias6c() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E as X
            |def x = X.F.G.H
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias7() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F as X
            |X x = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias7a() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F as X
            |X.G x = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias7b() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F as X
            |def x = X.G.H
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias8() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F.G as X
            |X x = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias8a() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F.G as X
            |def x = X.H
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRemoveTypeAlias1() {
        String originalContents = '''\
            |import org.w3c.dom.Node as N
            |def x
            |'''
        String expectedContents = '''\
            |def x
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveTypeAlias2() {
        String originalContents = '''\
            |import java.util.List as L
            |def x
            |'''
        String expectedContents = '''\
            |def x
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainInnerTypeAlias1() {
        String contents = '''\
            |import java.util.Map.Entry as E
            |E x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainInnerTypeAlias2() {
        String contents = '''\
            |import java.util.Map.Entry as E
            |E[] x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRemoveInnerTypeAlias1() {
        String originalContents = '''\
            |import java.util.Map.Entry as E
            |def x
            |'''
        String expectedContents = '''\
            |def x
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainStaticAlias1() {
        String contents = '''\
            |import static java.lang.Math.PI as Pie
            |def x = Pie
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias1a() {
        String contents = '''\
            |import static java.lang.Math.PI as Pie
            |@groovy.transform.CompileStatic
            |void test() {
            |  def x = Pie
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias2() {
        String contents = '''\
            |import static java.lang.Math.pow as f
            |f(2,Math.PI)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias2a() {
        String contents = '''\
            |import static java.lang.Math.pow as f
            |@groovy.transform.CompileStatic
            |void test() {
            |  f(2,Math.PI)
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias3() {
        String contents = '''\
            |import static java.lang.Math.pow as f
            |class C {
            |  void method() {
            |    f(2,Math.PI)
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias4() {
        String contents = '''\
            |import static java.math.RoundingMode.CEILING as ceiling
            |BigDecimal one = 1.0, two = one.divide(0.5, ceiling)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias5() {
        String contents = '''\
            |import static java.util.concurrent.TimeUnit.MILLISECONDS as msec
            |msec.toNanos(1234)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias6() {
        createGroovyType 'p', 'Q', '''\
            |class Q {
            |  static getBadName() {}
            |  static getFineName() {}
            |}
            |'''

        String contents = '''\
            |import static p.Q.getBadName as getGoodName
            |import static p.Q.getFineName
            |def one = goodName
            |def two = fineName
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias6a() {
        createGroovyType 'p', 'Q', '''\
            |class Q {
            |  static void setBadName(value) {}
            |  static void setFineName(value) {}
            |}
            |'''

        String contents = '''\
            |import static p.Q.setBadName as setGoodName
            |import static p.Q.setFineName
            |goodName = null
            |fineName = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias6b() {
        createGroovyType 'p', 'Q', '''\
            |class Q {
            |  static getBadName() {}
            |  static getFineName() {}
            |}
            |'''

        String contents = '''\
            |import static p.Q.getBadName as getGoodName
            |import static p.Q.getFineName
            |
            |@groovy.transform.CompileStatic
            |class C {
            |  def one = goodName
            |  def two = fineName
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias6c() {
        createGroovyType 'p', 'Q', '''\
            |class Q {
            |  static void setBadName(value) {}
            |  static void setFineName(value) {}
            |}
            |'''

        String contents = '''\
            |import static p.Q.setBadName as setGoodName
            |import static p.Q.setFineName
            |
            |@groovy.transform.CompileStatic
            |class C {
            |  def m() {
            |    goodName = null
            |    fineName = null
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias7() {
        createGroovyType 'other', 'Wrapper', '''
            |class Wrapper {
            |  enum Feature {
            |    TopRanking,
            |    SomethingElse
            |  }
            |}
            |'''

        String contents = '''\
            |import static other.Wrapper.Feature.TopRanking as feature
            |import static other.Wrapper.Feature.values as features
            |new Object() == feature
            |for (f in features()) {
            |  print f
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias8() {
        createGroovyType 'a.b.c.d', 'E', '''
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import static a.b.c.d.E.F.G.H as X
            |def x = X
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/731
    void testRetainStaticAlias9() {
        addJavaSource('enum E { ONE, TWO; }', 'E', 'p')

        String contents = '''\
            |import static p.E.*
            |import static p.E.ONE as WON
            |ONE
            |TWO
            |WON
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1243
    void testRetainStaticAlias10() {
        String contents = '''\
            |import static p.T.f as v
            |print v
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1243
    void testRetainStaticAlias11() {
        String contents = '''\
            |import static p.T.m as f
            |f('x')
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1244
    void testRetainStaticAlias12() {
        addJavaSource('class C { static Object f }', 'C', '')
        String contents = '''\
            |import static C.f as v
            |print v
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1244
    void testRetainStaticAlias13() {
        addJavaSource('class C { static void m() {} }', 'C', '')
        String contents = '''\
            |import static C.m as f
            |f('x')
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1244
    void testRetainStaticAlias14() {
        addJavaSource('class C { static void m() {} }', 'C', '')
        String contents = '''\
            |import static C.m as f
            |f 'x'
            |'''
        doContentsCompareTest(contents)
    }

    @Test @NotYetImplemented // https://github.com/groovy/groovy-eclipse/issues/732
    void testRetainStaticAlias15() {
        addJavaSource('class A { public static boolean isSomething(String s) { return true; } }', 'A', 'p')
        addJavaSource('class B { public static boolean isPoorlyNamed(Iterable i) { return true; } }', 'B', 'p')

        String contents = '''\
            |import static p.A.isSomething
            |import static p.B.isPoorlyNamed as isSomething
            |String s
            |Iterable i
            |isSomething(s)
            |isSomething(i)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRemoveStaticAlias1() {
        String originalContents = '''\
            |import static java.lang.Math.PI as P
            |def x
            |'''
        String expectedContents = '''\
            |def x
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveStaticAlias2() {
        String originalContents = '''\
            |import static java.util.List.emptyList as empty
            |def x
            |'''
        String expectedContents = '''\
            |def x
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveStaticAlias3() {
        String originalContents = '''\
            |import static java.math.RoundingMode.CEILING as ceiling
            |def x
            |'''
        String expectedContents = '''\
            |def x
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveStaticAlias4() {
        addJavaSource('enum E { ONE, TWO; }', 'E', 'p')

        String originalContents = '''\
            |import static p.E.*
            |import static p.E.ONE as UNUSED
            |ONE
            |TWO
            |'''
        String expectedContents = '''\
            |import static p.E.*
            |ONE
            |TWO
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainAnnotatedStaticAlias1() {
        String contents = '''\
            |@Deprecated
            |import static java.lang.Math.PI as P
            |P x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAnnotatedStaticAlias2() {
        String contents = '''\
            |@Deprecated
            |import static java.lang.Math as M
            |@Deprecated
            |import static java.lang.Math.PI as P
            |P x
            |'''
        doContentsCompareTest(contents)
    }

    @Test // STS-3314
    void testMultiAliasing1() {
        String contents = '''\
            |import javax.xml.soap.Node as SoapNode
            |
            |import org.w3c.dom.Node
            |
            |class SomeType {
            |  Node n
            |  SoapNode s
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testMultiAliasing2() {
        String contents = '''\
            |import other2.FourthClass
            |import other3.FourthClass as FourthClass2
            |import other4.FourthClass as FourthClass3
            |
            |class TypeHelper {
            |  FourthClass f1
            |  FourthClass2 f2
            |  FourthClass3 f3
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testMultiAliasing3() {
        String originalContents = '''\
            |import other2.FourthClass
            |import other3.FourthClass as FourthClass2
            |import other4.FourthClass as FourthClass3
            |
            |class TypeHelper {
            |  FourthClass f1
            |  FourthClass2 f2
            |}
            |'''
        String expectedContents = '''\
            |import other2.FourthClass
            |import other3.FourthClass as FourthClass2
            |
            |class TypeHelper {
            |  FourthClass f1
            |  FourthClass2 f2
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testMultiAliasing4() {
        String originalContents = '''\
            |import other3.FourthClass as FourthClass2
            |import other4.FourthClass as FourthClass3
            |import other2.FourthClass
            |
            |class TypeHelper {
            |  FourthClass f1
            |  FourthClass2 f2
            |}
            |'''
        String expectedContents = '''\
            |import other2.FourthClass
            |import other3.FourthClass as FourthClass2
            |
            |class TypeHelper {
            |  FourthClass f1
            |  FourthClass2 f2
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testMultiAliasing5() {
        String originalContents = '''\
            |import other2.FourthClass
            |import other3.FourthClass as FourthClass2
            |import other4.FourthClass as FourthClass3
            |
            |class TypeHelper {
            |  FourthClass2 f2
            |}
            |'''
        String expectedContents = '''\
            |import other3.FourthClass as FourthClass2
            |
            |class TypeHelper {
            |  FourthClass2 f2
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testOrganizeWithExtraImports1() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    alias 'Regexp', 'java.util.regex.Pattern'
            |  }
            |}
            |'''

        String originalContents = '''\
            |import java.util.regex.Matcher
            |
            |class C {
            |  Regexp regexp = ~/123/
            |  Matcher matcher(String string) {
            |    regexp.matcher(string)
            |  }
            |}
            |'''
        String expectedContents = '''\
            |import java.util.regex.Matcher
            |
            |class C {
            |  Regexp regexp = ~/123/
            |  Matcher matcher(String string) {
            |    regexp.matcher(string)
            |  }
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testOrganizeWithExtraImports2() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    staticMember 'java.util.concurrent.TimeUnit', 'DAYS'
            |    staticMember 'MILLIS', 'java.util.concurrent.TimeUnit', 'MILLISECONDS'
            |  }
            |}
            |'''

        String contents = '''\
            |import java.util.concurrent.TimeUnit
            |
            |TimeUnit units = DAYS
            |units = MILLIS
            |'''
        doContentsCompareTest(contents)
    }

    // TODO: What about an alias that is the same as the type or field/method?

    // TODO: What happens if an alias is repeated multiple times in the compliation unit?
}
