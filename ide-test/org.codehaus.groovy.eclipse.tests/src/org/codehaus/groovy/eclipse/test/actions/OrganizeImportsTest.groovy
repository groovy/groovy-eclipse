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
package org.codehaus.groovy.eclipse.test.actions

import org.eclipse.jdt.core.tests.util.GroovyUtils

/**
 * Tests for {@link org.codehaus.groovy.eclipse.refactoring.actions.OrganizeGroovyImports}
 */
final class OrganizeImportsTest extends AbstractOrganizeImportsTest {

    void testAddImport1() {
        String contents = '''
            FirstClass f
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport2() {
        String contents = '''
            def f = new FirstClass()
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport3() {
        String contents = '''
            def x(FirstClass f) { }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport4() {
        String contents = '''
            def x = { FirstClass f -> print '' }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport5() {
        String contents = '''
            class Main {
              FirstClass f
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport6() {
        String contents = '''
            class Main {
              FirstClass x() { }
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport7() {
        String contents = '''
            class Main {
              def x(FirstClass f) { }
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport8() {
        String contents = '''
            class Main {
              def x(FirstClass[] f) { }
            }
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport9() {
        String contents = '''
            FirstClass[][] fs = []
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport10() {
        String contents = '''
            def f = (FirstClass) null;
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddImport11() {
        String contents = '''
            def f = [:] as FirstClass
            '''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    void testAddInnerImport1() {
        String contents = '''
            class Main {
              def x(Inner i) { }
            }
            '''
        doAddImportTest(contents, ['import other.Outer.Inner'])
    }

    void testAddInnerImport2() {
        String contents = '''
            class Main {
              def x(Outer.Inner i) { }
            }
            '''
        doAddImportTest(contents, ['import other.Outer'])
    }

    void testAddInnerImport3() {
        String contents = '''
            import other.Outer
            class Main {
              def x(Outer.Inner i) { }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testAddInnerImport4() {
        String contents = '''
            class Main {
              def x(UnknownTag t) { }
            }
            '''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML.UnknownTag'])
    }

    void testAddInnerImport5() {
        String contents = '''
            class Main {
              def x(HTML.UnknownTag t) { }
            }
            '''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML'])
    }

    // GRECLIPSE-470
    void testImportWithinMapLiteral() {
        String contents = '''
            import javax.xml.XMLConstants
            [value: XMLConstants.XML_NS_URI]
            '''
        doContentsCompareTest(contents, contents)
    }

    void testAddImportWithinMapLiteral() {
        String contents = '''
            [value: XMLConstants.XML_NS_URI]
            '''
        doChoiceTest(contents, ['javax.xml.XMLConstants',
            'com.sun.xml.internal.ws.encoding.xml.XMLConstants',
            'com.sun.xml.internal.fastinfoset.stax.events.XMLConstants'
        ])
    }

    void testAddImportForExtends() {
        String contents = '''
            class C extends FirstClass {
            }
            '''
        doAddImportTest(contents, ['other.FirstClass'])
    }

    void testAddImportForImplements() {
        String contents = '''
            class C implements ConcurrentMap {
            }
            '''
        doAddImportTest(contents, ['import java.util.concurrent.ConcurrentMap'])
    }

    void testAddImportForGenerics() {
        String contents = '''
            import java.util.Map.Entry
            Entry<SecondClass, HTML> entry
            '''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML', 'import other.SecondClass'])
    }

    // GRECLIPSE-1693
    void testAddImportForGenerics2() {
        String contents = '''
            class Foo<T> {
            }
            '''
        doAddImportTest('p1', 'Foo', contents, [])

        createGroovyType 'p2', 'GroovyBar.groovy', '''
            class GroovyBar {
            }
            '''
        contents = '''
            import p1.Foo
            class Boo extends Foo<GroovyBar> {
            }
            '''
        doAddImportTest('p3', 'Boo', contents, ['import p2.GroovyBar'])
    }

    void testAddImportForGenerics3() {
        String contents = '''
            def maps = Collections.<ConcurrentMap>emptyList();
            '''
        doAddImportTest(contents, ['import java.util.concurrent.ConcurrentMap'])
    }

    void testRemoveImport() {
        String originalContents = '''\
            import other.SecondClass
            '''
        String expectedContents = '\n'
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRemoveImport2() {
        String originalContents = '''\
            import other.SecondClass
            a
            '''
        String expectedContents = '''\
            a
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRemoveImport3() {
        String originalContents = '''\
            import other.SecondClass
            other.SecondClass a
            '''
        String expectedContents = '''\
            other.SecondClass a
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRemoveImport4() {
        String originalContents = '''\
            import other.SecondClass
            import javax.swing.text.html.HTML
            class Main {
              HTML f = null
            }
            '''
        String expectedContents = '''\
            import javax.swing.text.html.HTML
            class Main {
              HTML f = null
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRemoveImport5() {
        String originalContents = '''\
            import other.ThirdClass
            import javax.swing.text.html.HTML
            import other.SecondClass
            class Main {
              HTML f = null
            }
            '''
        String expectedContents = '''\
            import javax.swing.text.html.HTML
            class Main {
              HTML f = null
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRemoveImport6() {
        String originalContents = '''\
            import java.util.logging.Logger
            import groovy.util.logging.Log
            @Log
            class Main {
              void method() {
                log.info 'in method'
              }
            }
            '''
        String expectedContents = '''\
            import groovy.util.logging.Log
            @Log
            class Main {
              void method() {
                log.info 'in method'
              }
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRepeatImport() {
        String originalContents = '''\
            import javax.swing.text.html.HTML
            import javax.swing.text.html.HTML
            HTML.class
            '''
        String expectedContents = '''\
            import javax.swing.text.html.HTML
            HTML.class
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testRetainImport() {
        String contents = '''\
            import javax.swing.text.html.HTML
            HTML.class
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport2() {
        String contents = '''\
            import java.lang.reflect.Method
            for (Method m : Object.class.getDeclaredMethods()) {
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport3() {
        String contents = '''\
            import java.lang.reflect.Method
            for (Method m in Object.class.getDeclaredMethods()) {
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport4() {
        // generics make the node length > "java.util.concurrent.Callable".length()
        String contents = '''\
            import java.util.concurrent.Callable
            class C implements Callable<java.util.List<java.lang.Object>> {
              List<Object> call() { }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport5() {
        String contents = '''\
            import java.util.regex.Pattern
            class C {
              @Lazy Pattern p = ~/123/
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport6() {
        String contents = '''\
            import java.util.regex.Pattern
            class C {
              @Lazy def p = Pattern.compile('123')
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport7() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            class C {
              @Lazy def p = compile('123')
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRetainImport8() {
        String contents = '''\
            import static java.util.regex.Pattern.compile
            class C {
              @groovy.transform.Memoized
              def meth() {
                compile('123')
              }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testChoices() {
        String contents = '''
            FourthClass f = null
            '''
        doChoiceTest(contents, ['other2.FourthClass', 'other3.FourthClass', 'other4.FourthClass'])
    }

    // GRECLIPSE-506
    void testImportDateFormat() {
        String contents = '''\
            import java.text.DateFormat
            new String(DateFormat.getDateInstance())
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-546
    void testImportDateFormat2() {
        String contents = '''\
            import java.text.DateFormat
            class Foo {
              Foo(DateFormat arg) { }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE 546
    void testImportDateFormat3() {
        String contents = '''
            class Foo {
              Foo(DateFormat arg) { }
            }
            '''
        doAddImportTest(contents, ['java.text.DateFormat'])
    }

    // GRECLIPSE-643
    void testNoStackOverflowOnEnum() {
        String contents = '''\
            enum MyEnum {
              ONE_VALUE, ANOTHER_VALUE
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    // Currently not possible due to heuristic in FindUnresolvedReferencesVisitor.handleVariable()
    void _testDynamicVariable() {
        String contents = '''
            HTML.NULL_ATTRIBUTE_VALUE
            '''
        doAddImportTest(contents, ['javax.swing.text.html.HTML'])
    }

    void testDynamicVariable2() {
        String contents = '''\
            nothing.HTML.NULL_ATTRIBUTE_VALUE
            '''
        doContentsCompareTest(contents, contents)
    }

    void testDynamicVariable3() {
        String contents = '''
            new String(DateFormat.getDateInstance())
            '''
        doAddImportTest(contents, ['java.text.DateFormat'])
    }

    // GRECLISPE-823
    void testThrownExceptions() {
        String contents = '''
            import java.util.zip.ZipException

            def x() throws BadLocationException {
            }
            def y() throws ZipException {
            }
            '''
        doAddImportTest(contents, ['javax.swing.text.BadLocationException'])
    }

    // GRECLIPSE-895
    void testCatchClausesExceptions() {
        String contents = '''
            import java.util.zip.ZipException

            try {
                nothing
            } catch (ZipException e1) {
            } catch (BadLocationException e2) {
            }
            '''
        doAddImportTest(contents, ['javax.swing.text.BadLocationException'])
    }

    // GRECLIPSE-600
    void testNestedAnnotations1() {
        createGroovyType 'anns', 'Annotations.groovy', '''
            @interface NamedQueries {
              NamedQuery value();
            }

            @interface NamedQuery {
            }
            '''
        String contents = '''
            @NamedQueries(
              @NamedQuery
            )
            class MyEntity {  }
            '''
        doAddImportTest(contents, ['anns.NamedQueries', 'anns.NamedQuery'])
    }

    // GRECLIPSE-600
    void testNestedAnnotations2() {
        createGroovyType 'anns', 'Annotations.groovy', '''
            @interface NamedQueries {
              NamedQuery value();
            }

            @interface NamedQuery {
            }'''
        String contents = '''\
            import anns.NamedQueries
            import anns.NamedQuery

            @NamedQueries(
              @NamedQuery
            )
            class MyEntity {  }
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-600
    void testNestedAnnotations3() {
        createGroovyType 'anns', 'Annotations.groovy', '''
            @interface NamedQueries {
              NamedQuery[] value();
            }
            @interface NamedQuery {
            }
            '''

        String contents = '''
            @NamedQueries(
              [@NamedQuery]
            )
            class MyEntity {  }
            '''
        doAddImportTest(contents, ['anns.NamedQueries', 'anns.NamedQuery'])
    }

    // GRECLIPSE-600
    void testNestedAnnotations4() {
        createGroovyType 'anns', 'Annotations.groovy', '''
            @interface NamedQueries {
              NamedQuery[] value();
            }
            @interface NamedQuery {
            }
            '''
        String contents = '''\
            import anns.NamedQueries
            import anns.NamedQuery

            @NamedQueries(
              [@NamedQuery]
            )
            class MyEntity {  }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testInnerClass1() {
        createGroovyType 'inner', 'HasInner.groovy', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''
            InnerInner f
            '''
        doAddImportTest(contents, ['inner.HasInner.InnerInner'])
    }

    void testInnerClass2() {
        createGroovyType 'inner', 'HasInner.groovy', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''\
            import inner.HasInner.InnerInner
            InnerInner f
            '''
        doContentsCompareTest(contents, contents)
    }

    void testInnerClass3() {
        createGroovyType 'inner', 'HasInner.groovy', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''
            HasInner.InnerInner f
            '''
        doAddImportTest(contents, ['inner.HasInner'])
    }

    void testInnerClass4() {
        createGroovyType 'inner', 'HasInner.groovy', '''
            class HasInner {
              class InnerInner { }
            }
            '''
        String contents = '''\
            import inner.HasInner
            HasInner.InnerInner f
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport() {
        String contents = '''\
            import static java.lang.String.format
            format
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImportX() {
        String originalContents = '''\
            import static java.lang.String.format
            formage
            '''
        String expectedContents = '''\
            formage
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testStaticImport2() {
        String contents = '''\
            import static java.lang.String.format
            format('Some %d', 1)
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport3() {
        String contents = '''\
            import static java.lang.String.format
            def formatter = { format 'blah', 42 }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport4() {
        String contents = '''\
            import static java.lang.Math.PI
            def area = n * PI
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport4a() {
        String contents = '''\
            import static java.lang.Math.PI
            def nan = PI.isNaN()
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport5() {
        String contents = '''\
            import static java.lang.Math.PI
            def area = compute(n, PI)
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport5a() {
        String originalContents = '''\
            import static java.lang.Math.PI
            def area = compute(n, Math.PI)
            '''
        String expectedContents = '''\
            def area = compute(n, Math.PI)
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    void testStaticImport6() {
        String contents = '''\
            import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY
            @SuppressWarnings(value=EMPTY_STRING_ARRAY)
            def area = compute(n, PI)
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport7() {
        String contents = '''\
            import static java.util.Collections.emptyList
            class C {
              def method() {
                List list = emptyList();
              }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport7a() {
        String contents = '''\
            import static java.util.Collections.emptyList
            @groovy.transform.CompileStatic
            class C {
              def method() {
                List list = emptyList();
              }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticImport7b() {
        String contents = '''\
            import static java.util.Collections.emptyList
            @groovy.transform.TypeChecked
            class C {
              def method() {
                List list = emptyList();
              }
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    void testRepeatStaticImport() {
        String originalContents = '''\
            import static java.util.Collections.emptyList
            import static java.util.Collections.emptyList
            List list = emptyList();
            '''
        String expectedContents = '''\
            import static java.util.Collections.emptyList
            List list = emptyList();
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-929
    void testStarImport() {
        String contents = '''\
            import javax.swing.text.html.*
            HTML
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-929
    void testStarImport2() {
        // never remove star imports
        String contents = '''\
            import javax.swing.text.html.*
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStarImport3() {
        String originalContents = '''\
            import java.util.regex.Pattern
            import java.util.regex.*
            Pattern p = ~/123/
            '''
        String expectedContents = '''\
            import java.util.regex.*
            Pattern p = ~/123/
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-929
    void testStaticStarImport() {
        // never remove static star imports
        String contents = '''\
            import static java.lang.String.*
            format
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-929
    void testStaticStarImport2() {
        // never remove static star imports
        String contents = '''\
            import static java.lang.String.*
            '''
        doContentsCompareTest(contents, contents)
    }

    void testStaticStarImport3() {
        String originalContents = '''\
            import static java.util.Collections.emptyList
            import static java.util.Collections.*
            List l = emptyList()
            '''
        String expectedContents = '''\
            import static java.util.Collections.*
            List l = emptyList()
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1219
    void testAnnotationsOnImports1() {
        String contents = '''\
            @Deprecated
            import javax.swing.text.html.*
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-1692
    void testFieldAnnotationImport() {
        String contents = '''\
            import groovy.transform.Field
            @Field
            def x = 0
            '''
        doContentsCompareTest(contents, contents)
    }

    void testCompileDynamicImport() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return
        String contents = '''
            @CompileDynamic
            public void x() {
            }
            '''
        doAddImportTest(contents, ['groovy.transform.CompileDynamic'])
    }

    // GRECLIPSE-1794
    void testCompileDynamicImport2() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return
        String contents = '''\
            import groovy.transform.CompileDynamic
            @CompileDynamic
            public void x() {
            }
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-1794
    void testCompileDynamicImport3() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return
        String originalContents = '''\
            import groovy.transform.CompileDynamic
            import groovy.transform.CompileStatic
            @CompileDynamic
            public void x() {
            }
            '''
        String expectedContents = '''\
            import groovy.transform.CompileDynamic
            @CompileDynamic
            public void x() {
            }
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1219
    void testAnnotationsOnImports2() {
        String originalContents = '''\
            @Deprecated
            import javax.swing.text.html.HTML
            '''
        String expectedContents = '\n'
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1219
    void testAnnotationsOnImports3() {
        String contents = '''\
            @Deprecated
            import javax.swing.text.html.*
            HTML
            '''
        doContentsCompareTest(contents, contents)
    }

    void testDefaultImport0() {
        String contents = '''\
        interface Pattern {} // this should shadow java.util.regex.Pattern, et al.
        Pattern p = null
        '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-1392
    void testDefaultImport1() {
        // test a simple default import is removed
        String originalContents = '''\
            import java.util.List
            import groovy.util.Proxy
            List
            Proxy
            '''
        String expectedContents = '''\
            List
            Proxy
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1392
    void testDefaultImport2() {
        // test that star default imports are removed
        String originalContents = '''\
            import java.util.*
            import groovy.util.*
            List
            Proxy
            '''
        String expectedContents = '''\
            List
            Proxy
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1392
    void testDefaultImport3() {
        // test that BigInteger and BigDecimal are removed
        String originalContents = '''\
            import java.math.BigDecimal
            import java.math.BigInteger
            BigDecimal
            BigInteger
            '''
        String expectedContents = '''\
            BigDecimal
            BigInteger
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1392
    void testDefaultImport4() {
        // test that aliased default import not removed
        String originalContents = '''\
            import java.util.List as LL
            import groovy.util.Proxy as PP
            LL
            PP
            '''
        String expectedContents = '''\
            import java.util.List as LL

            import groovy.util.Proxy as PP
            LL
            PP
            '''
        doContentsCompareTest(originalContents, expectedContents)
    }

    // GRECLIPSE-1392
    void testDefaultImport5() {
        // test that static import whose container is default is not removed
        String contents = '''\
            import static java.util.Collections.swap
            swap
            '''
        doContentsCompareTest(contents, contents)
    }

    // GRECLIPSE-1553
    void testCompileStaticAndMapStyleConstructor() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

        createGroovyType 'example2', 'Bar', '''
            package example2

            class Bar {
                String name
            }'''

        String contents = '''
            package example

            import groovy.transform.CompileStatic
            import example2.Bar

            @CompileStatic
            class Foo {
              void apply() {
                  new Bar([name: 'test'])
              }
            }'''

        doContentsCompareTest(contents, contents)
    }

    void testReorderExistingImports() {
        // I've seen the static import get wiped out because it's not in the right position
        String originalContents = '''\
            import java.util.List
            import org.w3c.dom.Node
            import static java.util.Collections.emptyList
            import java.util.regex.Pattern

            Collection c = emptyList()
            Pattern p = ~/abc/
            Node n = null
            '''
        String expectedContents = '''\
            import static java.util.Collections.emptyList

            import java.util.regex.Pattern

            import org.w3c.dom.Node

            Collection c = emptyList()
            Pattern p = ~/abc/
            Node n = null
            '''

        doContentsCompareTest(originalContents, expectedContents)
    }

    // TODO: Ensure imports come below header comment for class in the default package.
}
