/*
 * Copyright 2009-2017 the original author or authors.
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

import org.junit.Test

/**
 * Tests for {@link org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports}.
 */
final class AliasingOrganizeImportsTests extends OrganizeImportsTestSuite {

    @Test
    void testNoSpuriousEdits() {
        String contents = '''\
            import org.w3c.dom.Node as N
            N x
            '''
        doAddImportTest(contents)
    }

    @Test
    void testRetainTypeAlias() {
        String contents = '''\
            import org.w3c.dom.Node as N
            N x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias2() {
        String contents = '''\
            import org.w3c.dom.Node as N
            N[] x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias3() {
        // List is a default import
        String contents = '''\
            import java.util.List as L
            L list = []
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias4() {
        String contents = '''\
            import java.util.LinkedList as LL
            def list = [] as LL
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainTypeAlias5() {
        String contents = '''\
            import java.util.LinkedList as LL
            def list = (LL) []
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias6() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E as X
            X x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias6a() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E as X
            X.F x = null
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias6b() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E as X
            X.F.G x = null
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias6c() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E as X
            def x = X.F.G.H
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias7() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F as X
            X x = null
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias7a() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F as X
            X.G x = null
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias7b() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F as X
            def x = X.G.H
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias8() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F.G as X
            X x = null
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAlias8a() {
        createGroovyType 'a.b.c.d', 'E', '''\
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import a.b.c.d.E.F.G as X
            def x = X.H
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRemoveTypeAlias() {
        String originalContents = '''\
            import org.w3c.dom.Node as N
            def x
            '''
        String expectedContents = '''\
            def x
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveTypeAlias1() {
        String originalContents = '''\
            import java.util.List as L
            def x
            '''
        String expectedContents = '''\
            def x
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainInnerTypeAlias() {
        String contents = '''\
            import java.util.Map.Entry as E
            E x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainInnerTypeAlias2() {
        String contents = '''\
            import java.util.Map.Entry as E
            E[] x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRemoveInnerTypeAlias() {
        String originalContents = '''\
            import java.util.Map.Entry as E
            def x
            '''
        String expectedContents = '''\
            def x
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainStaticAlias() {
        String contents = '''\
            import static java.lang.Math.PI as Pie
            def x = Pie
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias2() {
        String contents = '''\
            import static java.lang.Math.pow as f
            f(2,Math.PI)
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias3() {
        String contents = '''\
            import static java.lang.Math.pow as f
            class C {
              void method() {
                f(2,Math.PI)
              }
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias4() {
        String contents = '''\
            import static java.math.RoundingMode.CEILING as ceiling
            BigDecimal one = 1.0, two = one.divide(0.5, ceiling)
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias5() {
        String contents = '''\
            import static java.util.concurrent.TimeUnit.MILLISECONDS as msec
            msec.toNanos(1234)
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias6() {
        createGroovyType 'other', 'Wrapper', '''
            class Wrapper {
              enum Feature {
                TopRanking,
                SomethingElse
              }
            }
            '''
        String contents = '''\
            import static other.Wrapper.Feature.TopRanking as feature
            import static other.Wrapper.Feature.values as features
            new Object().equals(feature)
            for (f in features()) {
              print f
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainStaticAlias7() {
        createGroovyType 'a.b.c.d', 'E', '''
            interface E { interface F { interface G { String H = 'I' } } }
            '''
        String contents = '''\
            import static a.b.c.d.E.F.G.H as X
            def x = X
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRemoveStaticAlias() {
        String originalContents = '''\
            import static java.lang.Math.PI as P
            def x
            '''
        String expectedContents = '''\
            def x
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveStaticAlias2() {
        String originalContents = '''\
            import static java.util.List.emptyList as empty
            def x
            '''
        String expectedContents = '''\
            def x
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveStaticAlias3() {
        String originalContents = '''\
            import static java.math.RoundingMode.CEILING as ceiling
            def x
            '''
        String expectedContents = '''\
            def x
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainAnnotatedStaticAlias() {
        String contents = '''\
            @Deprecated
            import static java.lang.Math.PI as P
            P x
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainAnnotatedStaticAlias2() {
        String contents = '''\
            @Deprecated
            import static java.lang.Math as M
            @Deprecated
            import static java.lang.Math.PI as P
            P x
            '''
        doContentsCompareTest(contents)
    }

    @Test // STS-3314
    void testMultiAliasing() {
        String contents = '''\
            import javax.xml.soap.Node as SoapNode

            import org.w3c.dom.Node

            class SomeType {
              Node n
              SoapNode s
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testMultiAliasing2() {
        String contents = '''\
            import other2.FourthClass
            import other3.FourthClass as FourthClass2
            import other4.FourthClass as FourthClass3

            class TypeHelper {
              FourthClass f1
              FourthClass2 f2
              FourthClass3 f3
            }
            '''
        doContentsCompareTest(contents)
    }

    @Test
    void testMultiAliasing3() {
        String originalContents = '''\
            import other2.FourthClass
            import other3.FourthClass as FourthClass2
            import other4.FourthClass as FourthClass3

            class TypeHelper {
              FourthClass f1
              FourthClass2 f2
            }
            '''
        String expectedContents = '''\
            import other2.FourthClass
            import other3.FourthClass as FourthClass2

            class TypeHelper {
              FourthClass f1
              FourthClass2 f2
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testMultiAliasing4() {
        String originalContents = '''\
            import other3.FourthClass as FourthClass2
            import other4.FourthClass as FourthClass3
            import other2.FourthClass

            class TypeHelper {
              FourthClass f1
              FourthClass2 f2
            }
            '''
        String expectedContents = '''\
            import other2.FourthClass
            import other3.FourthClass as FourthClass2

            class TypeHelper {
              FourthClass f1
              FourthClass2 f2
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testMultiAliasing5() {
        String originalContents = '''\
            import other2.FourthClass
            import other3.FourthClass as FourthClass2
            import other4.FourthClass as FourthClass3

            class TypeHelper {
              FourthClass2 f2
            }
            '''
        String expectedContents = '''\
            import other3.FourthClass as FourthClass2

            class TypeHelper {
              FourthClass2 f2
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // TODO: What about an alias that is the same as the type or field/method?

    // TODO: What happens if an alias is repeated multiple times in the compliation unit?
}
