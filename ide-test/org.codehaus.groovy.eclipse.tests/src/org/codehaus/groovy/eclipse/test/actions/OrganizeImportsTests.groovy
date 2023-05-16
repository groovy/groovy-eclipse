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
final class OrganizeImportsTests extends OrganizeImportsTestSuite {

    @Test
    void testAddImport1() {
        String contents = '''\
            |FirstClass f
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport2() {
        String contents = '''\
            |def f = new FirstClass()
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport3() {
        String contents = '''\
            |def x(FirstClass f) { }
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport4() {
        String contents = '''\
            |def x = { FirstClass f -> print '' }
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport5() {
        String contents = '''\
            |class Main {
            |  FirstClass f
            |}
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport6() {
        String contents = '''\
            |class Main {
            |  FirstClass x() { }
            |}
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport7() {
        String contents = '''\
            |class Main {
            |  def x(FirstClass f) { }
            |}
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport8() {
        String contents = '''\
            |class Main {
            |  def x(FirstClass[] f) { }
            |}
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport9() {
        String contents = '''\
            |FirstClass[][] fs = []
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport10() {
        String contents = '''\
            |def fs = new FirstClass[0]
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport11() {
        String contents = '''\
            |def f = (FirstClass) null;
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddImport12() {
        String contents = '''\
            |def f = [:] as FirstClass
            |'''
        doAddImportTest(contents, ['import other.FirstClass'])
    }

    @Test
    void testAddInnerImport1() {
        String contents = '''\
            |class Main {
            |  def x(Inner i) { }
            |}
            |'''
        doAddImportTest(contents, ['import other.Outer.Inner'])
    }

    @Test
    void testAddInnerImport2() {
        String contents = '''\
            |class Main {
            |  def x(Outer.Inner i) { }
            |}
            |'''
        doAddImportTest(contents, ['import other.Outer'])
    }

    @Test
    void testAddInnerImport3() {
        String contents = '''\
            |import other.Outer
            |class Main {
            |  def x(Outer.Inner i) { }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testAddInnerImport4() {
        String contents = '''\
            |class Main {
            |  def x(UnknownTag t) { }
            |}
            |'''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML.UnknownTag'])
    }

    @Test
    void testAddInnerImport5() {
        String contents = '''\
            |class Main {
            |  def x(HTML.UnknownTag t) { }
            |}
            |'''
        doAddImportTest(contents, ['import javax.swing.text.html.HTML'])
    }

    @Test // GRECLIPSE-470
    void testImportWithinMapLiteral() {
        String contents = '''\
            |import javax.util.concurrent.TimeUnit
            |[value: TimeUnit.SECONDS]
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testAddImportWithinMapLiteral() {
        String contents = '''\
            |[value: TimeUnit.SECONDS]
            |'''
        doAddImportTest(contents, ['javax.util.concurrent.TimeUnit'])
    }

    @Test
    void testAddImportForExtends() {
        String contents = '''\
            |class C extends FirstClass {
            |}
            |'''
        doAddImportTest(contents, ['other.FirstClass'])
    }

    @Test
    void testAddImportForImplements() {
        String contents = '''\
            |class C implements ConcurrentMap {
            |}
            |'''
        doAddImportTest(contents, ['import java.util.concurrent.ConcurrentMap'])
    }

    @Test
    void testAddImportForGenerics1() {
        String originalContents = '''\
            |import java.util.Map.Entry
            |
            |Entry<SecondClass, HTML> entry
            |'''
        String expectedContents = '''\
            |import java.util.Map.Entry
            |
            |import javax.swing.text.html.HTML
            |
            |import other.SecondClass
            |
            |Entry<SecondClass, HTML> entry
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testAddImportForGenerics2() {
        String originalContents = '''\
            |import java.util.Map.Entry
            |
            |Entry<SecondClass, FirstClass<HTML>>[] array
            |'''
        String expectedContents = '''\
            |import java.util.Map.Entry
            |
            |import javax.swing.text.html.HTML
            |
            |import other.FirstClass
            |import other.SecondClass
            |
            |Entry<SecondClass, FirstClass<HTML>>[] array
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testAddImportForGenerics3() {
        String originalContents = '''\
            |import java.util.Map.Entry
            |
            |def array = new Entry<SecondClass, FirstClass<HTML>>[0]
            |'''
        String expectedContents = '''\
            |import java.util.Map.Entry
            |
            |import javax.swing.text.html.HTML
            |
            |import other.FirstClass
            |import other.SecondClass
            |
            |def array = new Entry<SecondClass, FirstClass<HTML>>[0]
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testAddImportForGenerics4() {
        createGroovyType 'p1', 'Foo', 'class Foo<T> {}'
        createGroovyType 'p2', 'Bar', 'class Bar {}'

        String originalContents = '''\
            |import p1.Foo
            |class Baz extends Foo<Bar> {
            |}
            |'''
        String expectedContents = '''\
            |import p1.Foo
            |import p2.Bar
            |class Baz extends Foo<Bar> {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testAddImportForGenerics5() {
        createGroovyType 'foo', 'Bar', 'class Bar {}'

        String contents = '''\
            |class Baz<T extends Bar> {
            |}
            |'''
        doAddImportTest(contents, ['import foo.Bar'])
    }

    @Test
    void testAddImportForGenerics6() {
        createGroovyType 'foo', 'Bar', 'class Bar {}'

        String contents = '''\
            |def maps = Collections.<Bar>emptyList();
            |'''
        doAddImportTest(contents, ['import foo.Bar'])
    }

    @Test
    void testAddImportForGenerics7() {
        createGroovyType 'foo', 'Bar', 'interface Bar<T> extends Comparable<T> {}'

        String contents = '''\
            |class Baz<T extends Bar<? super T>> {
            |  Set<T> getBars() {
            |  }
            |}
            |'''
        doAddImportTest(contents, ['import foo.Bar'])
    }

    @Test
    void testRemoveImport1() {
        String originalContents = '''\
            |import other.SecondClass
            |'''
        String expectedContents = ''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport2() {
        String originalContents = '''\
            |import other.SecondClass
            |a
            |'''
        String expectedContents = '''\
            |a
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport3() {
        String originalContents = '''\
            |import other.SecondClass
            |other.SecondClass a
            |'''
        String expectedContents = '''\
            |other.SecondClass a
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport4() {
        String originalContents = '''\
            |import other.SecondClass
            |import javax.swing.text.html.HTML
            |class Main {
            |  HTML f = null
            |}
            |'''
        String expectedContents = '''\
            |import javax.swing.text.html.HTML
            |class Main {
            |  HTML f = null
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport5() {
        String originalContents = '''\
            |import other.ThirdClass
            |import javax.swing.text.html.HTML
            |import other.SecondClass
            |class Main {
            |  HTML f = null
            |}
            |'''
        String expectedContents = '''\
            |import javax.swing.text.html.HTML
            |class Main {
            |  HTML f = null
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRemoveImport6() {
        String originalContents = '''\
            |import java.util.logging.Logger
            |import groovy.util.logging.Log
            |@Log
            |class Main {
            |  void method() {
            |    log.info 'in method'
            |  }
            |}
            |'''
        String expectedContents = '''\
            |import groovy.util.logging.Log
            |@Log
            |class Main {
            |  void method() {
            |    log.info 'in method'
            |  }
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRepeatImport() {
        String originalContents = '''\
            |import javax.swing.text.html.HTML
            |import javax.swing.text.html.HTML
            |HTML.class
            |'''
        String expectedContents = '''\
            |import javax.swing.text.html.HTML
            |HTML.class
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testRetainImport1() {
        String contents = '''\
            |import javax.swing.text.html.HTML
            |HTML.class
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport2() {
        String contents = '''\
            |import java.lang.reflect.Method
            |for (Method m : Object.class.getDeclaredMethods()) {
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport3() {
        String contents = '''\
            |import java.lang.reflect.Method
            |for (Method m in Object.class.getDeclaredMethods()) {
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport4() {
        // generics make the node length > "java.util.concurrent.Callable".length()
        String contents = '''\
            |import java.util.concurrent.Callable
            |class C implements Callable<java.util.List<java.lang.Object>> {
            |  List<Object> call() { }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport5() {
        // multiple generics caused type to be perceived as "ConcurrentMap<java.util.regex.Pattern,"
        String contents = '''\
            |import java.util.concurrent.ConcurrentMap
            |ConcurrentMap<java.util.regex.Pattern, String> m
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport6() {
        String contents = '''\
            |import java.util.regex.Pattern
            |class C {
            |  @Lazy Pattern p = ~/123/
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport7() {
        String contents = '''\
            |import java.util.regex.Pattern
            |class C {
            |  @Lazy def p = Pattern.compile('123')
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport8() {
        String contents = '''\
            |import static java.util.regex.Pattern.compile
            |class C {
            |  @Lazy def p = compile('123')
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport9() {
        String contents = '''\
            |import static java.util.regex.Pattern.compile
            |class C {
            |  @groovy.transform.Memoized
            |  def meth() {
            |    compile('123')
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport10() {
        String contents = '''\
            |import java.util.regex.Pattern
            |def parse = Pattern.&compile
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport11() {
        String contents = '''\
            |import groovy.transform.AnnotationCollector
            |import groovy.transform.EqualsAndHashCode
            |import groovy.transform.ToString
            |
            |@AnnotationCollector([EqualsAndHashCode, ToString])
            |public @interface Custom {
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport12() {
        String contents = '''\
            |import groovy.transform.AnnotationCollector
            |import groovy.transform.EqualsAndHashCode
            |import groovy.transform.ToString
            |
            |@ToString
            |@EqualsAndHashCode
            |@AnnotationCollector
            |public @interface Custom {
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testRetainImport13() {
        String contents = '''\
            |import p.T
            |def m(T t) {}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testChoices() {
        String contents = '''\
            |FourthClass f = null
            |'''
        doChoiceTest(contents, ['other2.FourthClass', 'other3.FourthClass', 'other4.FourthClass'])
    }

    @Test // GRECLIPSE-506
    void testImportDateFormat1() {
        String contents = '''\
            |import java.text.DateFormat
            |new String(DateFormat.getDateInstance())
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-546
    void testImportDateFormat2() {
        String contents = '''\
            |import java.text.DateFormat
            |class Foo {
            |  Foo(DateFormat arg) { }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE 546
    void testImportDateFormat3() {
        String contents = '''\
            |class Foo {
            |  Foo(DateFormat arg) { }
            |}
            |'''
        doAddImportTest(contents, ['java.text.DateFormat'])
    }

    @Test // GRECLIPSE-643
    void testNoStackOverflowOnEnum() {
        String contents = '''\
            |enum MyEnum {
            |  ONE_VALUE, ANOTHER_VALUE
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test @NotYetImplemented // Currently not possible due to heuristic in FindUnresolvedReferencesVisitor.handleVariable()
    void testDynamicVariable1() {
        String contents = '''\
            |HTML.NULL_ATTRIBUTE_VALUE
            |'''
        doAddImportTest(contents, ['javax.swing.text.html.HTML'])
    }

    @Test
    void testDynamicVariable2() {
        String contents = '''\
            |nothing.HTML.NULL_ATTRIBUTE_VALUE
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testDynamicVariable3() {
        String contents = '''\
            |new String(DateFormat.getDateInstance())
            |'''
        doAddImportTest(contents, ['java.text.DateFormat'])
    }

    @Test
    void testTitleCaseVariable() {
        createGroovyType 'p', 'Q', 'class Q {}'

        String contents = '''\
            |def Q = null
            |def q = Q
            |'''
        doContentsCompareTest(contents, contents)
    }

    @Test // GRECLISPE-823
    void testThrownExceptions() {
        String originalContents = '''\
            |import java.util.zip.ZipException
            |
            |def x() throws BadLocationException {
            |}
            |def y() throws ZipException {
            |}
            |'''
        String expectedContents = '''\
            |import java.util.zip.ZipException
            |
            |import javax.swing.text.BadLocationException
            |
            |def x() throws BadLocationException {
            |}
            |def y() throws ZipException {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-895
    void testCaughtExceptions1() {
        String originalContents = '''\
            |import java.util.zip.ZipException
            |
            |try {
            |    nothing
            |} catch (ZipException e1) {
            |} catch (BadLocationException e2) {
            |}
            |'''
        String expectedContents = '''\
            |import java.util.zip.ZipException
            |
            |import javax.swing.text.BadLocationException
            |
            |try {
            |    nothing
            |} catch (ZipException e1) {
            |} catch (BadLocationException e2) {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testCaughtExceptions2() {
        String originalContents = '''\
            |import java.util.zip.ZipException
            |try {
            |  ;
            |} catch (java.util.zip.ZipException e) {
            |}
            |'''
        String expectedContents = '''\
            |try {
            |  ;
            |} catch (java.util.zip.ZipException e) {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations1() {
        createGroovyType 'anns', 'Annotations', '''\
            |@interface NamedQueries {
            |  NamedQuery value();
            |}
            |
            |@interface NamedQuery {
            |}
            |'''

        String contents = '''\
            |@NamedQueries(
            |  @NamedQuery
            |)
            |class MyEntity {  }
            |'''
        doAddImportTest(contents, ['anns.NamedQueries', 'anns.NamedQuery'])
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations2() {
        createGroovyType 'anns', 'Annotations', '''\
            |@interface NamedQueries {
            |  NamedQuery value();
            |}
            |
            |@interface NamedQuery {
            |}
            |'''

        String contents = '''\
            |import anns.NamedQueries
            |import anns.NamedQuery
            |
            |@NamedQueries(
            |  @NamedQuery
            |)
            |class MyEntity {  }
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations3() {
        createGroovyType 'anns', 'Annotations', '''\
            |@interface NamedQueries {
            |  NamedQuery[] value();
            |}
            |@interface NamedQuery {
            |}
            |'''

        String contents = '''\
            |@NamedQueries(
            |  [@NamedQuery]
            |)
            |class MyEntity {  }
            |'''
        doAddImportTest(contents, ['anns.NamedQueries', 'anns.NamedQuery'])
    }

    @Test // GRECLIPSE-600
    void testNestedAnnotations4() {
        createGroovyType 'anns', 'Annotations', '''\
            |@interface NamedQueries {
            |  NamedQuery[] value();
            |}
            |@interface NamedQuery {
            |}
            |'''

        String contents = '''\
            |import anns.NamedQueries
            |import anns.NamedQuery
            |
            |@NamedQueries(
            |  [@NamedQuery]
            |)
            |class MyEntity {  }
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass1() {
        createGroovyType 'inner', 'HasInner', '''\
            |class HasInner {
            |  class InnerInner { }
            |}
            |'''

        String contents = '''\
            |InnerInner f
            |'''
        doAddImportTest(contents, ['inner.HasInner.InnerInner'])
    }

    @Test
    void testInnerClass2() {
        createGroovyType 'inner', 'HasInner', '''\
            |class HasInner {
            |  class InnerInner { }
            |}
            |'''

        String contents = '''\
            |import inner.HasInner.InnerInner
            |InnerInner f
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass3() {
        createGroovyType 'inner', 'HasInner', '''\
            |class HasInner {
            |  class InnerInner { }
            |}
            |'''

        String contents = '''\
            |HasInner.InnerInner f
            |'''
        doAddImportTest(contents, ['inner.HasInner'])
    }

    @Test
    void testInnerClass4() {
        createGroovyType 'inner', 'HasInner', '''\
            |class HasInner {
            |  class InnerInner { }
            |}
            |'''

        String contents = '''\
            |import inner.HasInner
            |HasInner.InnerInner f
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/923
    void testInnerClass5() {
        createGroovyType 'foo', 'Bar', '''\
            |class Bar {
            |  static class Baz {
            |  }
            |}
            |'''

        String originalContents = '''\
            |import foo.Bar
            |import foo.Bar.Baz
            |Bar.Baz baz = null
            |'''
        String expectedContents = '''\
            |import foo.Bar
            |Bar.Baz baz = null
            |'''

        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testInnerClass6() {
        createGroovyType 'foo', 'Bar', '''\
            |class Bar {
            |  static class Baz {
            |  }
            |}
            |'''

        String originalContents = '''\
            |import foo.Bar
            |import foo.Bar.Baz
            |foo.Bar.Baz baz = null
            |'''
        String expectedContents = '''\
            |foo.Bar.Baz baz = null
            |'''

        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testInnerClass7() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E
            |E x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass8() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F
            |F x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass9() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import a.b.c.d.E.F.G
            |G x
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass10() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { interface F { interface G { String H = 'I' } } }
            |'''

        String contents = '''\
            |import static a.b.c.d.E.F.G.H
            |def x = H
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1184
    void testInnerClass11() {
        createGroovyType 'p', 'A', '''\
            |abstract class A {
            |  protected static class B {
            |  }
            |}
            |'''

        String contents = '''\
            |import p.A
            |import p.A.B
            |class C extends A {
            |  def m(B b) {
            |  }
            |}
            |'''
        doContentsCompareTest(contents, contents - ~/\|import p.A.B\s+/)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1184
    void testInnerClass12() {
        String contents = '''\
            |import java.util.Map.Entry
            |class C implements Map {
            |  def m(Entry e) {
            |  }
            |}
            |'''
        doContentsCompareTest(contents, contents - ~/\|import java.util.Map.Entry\s+/)
    }

    @Test
    void testInnerClass13() {
        String contents = '''\
            |import java.util.Map.*
            |Entry entry
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testInnerClass14() {
        String contents = '''\
            |import static java.util.Map.*
            |Entry entry
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport1() {
        String contents = '''\
            |import static java.lang.String.format
            |'''
        doContentsCompareTest(contents, '')
    }

    @Test
    void testStaticImport2() {
        String contents = '''\
            |import static java.lang.String.format
            |format
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport3() {
        String originalContents = '''\
            |import static java.lang.String.format
            |formage
            |'''
        String expectedContents = '''\
            |formage
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStaticImport4() {
        String contents = '''\
            |import static java.lang.String.format
            |format('Some %d', 1)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport5() {
        String contents = '''\
            |import static java.lang.String.format
            |def formatter = { format 'blah', 42 }
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport6() {
        String contents = '''\
            |import static java.lang.Math.PI
            |def area = n * PI
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport7() {
        String contents = '''\
            |import static java.lang.Math.PI
            |def nan = PI.isNaN()
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport8() {
        String contents = '''\
            |import static java.lang.Math.PI
            |def area = compute(n, PI)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport9() {
        String originalContents = '''\
            |import static java.lang.Math.PI
            |def area = compute(n, Math.PI)
            |'''
        String expectedContents = '''\
            |def area = compute(n, Math.PI)
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStaticImport10() {
        String contents = '''\
            |import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY
            |@SuppressWarnings(value=EMPTY_STRING_ARRAY)
            |def area = compute(n, PI)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport11() {
        String contents = '''\
            |import static java.util.Collections.emptyList
            |class C {
            |  def method() {
            |    List list = emptyList();
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport12() {
        String contents = '''\
            |import static java.util.Collections.emptyList
            |@groovy.transform.CompileStatic
            |class C {
            |  def method() {
            |    List list = emptyList();
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport13() {
        String contents = '''\
            |import static java.util.Collections.emptyList
            |@groovy.transform.TypeChecked
            |class C {
            |  def method() {
            |    List list = emptyList();
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport14() {
        createGroovyType 'a.b.c.d', 'E', '''\
            |interface E { String F = 'G.H' }
            |'''

        String contents = '''\
            |import static a.b.c.d.E.F
            |class Foo {
            |  @SuppressWarnings(F)
            |  def one() {}
            |  @SuppressWarnings(F)
            |  def two() {}
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport15() {
        addJavaSource('interface I { static String NLS = "nls"; }', 'I', 'p')

        String contents = '''\
            |import static p.I.NLS
            |class Foo {
            |  @SuppressWarnings(NLS)
            |  def method() {
            |    System.getProperty('non.localized.string')
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticImport16() {
        String contents = '''\
            |import static p.T.f
            |print f
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1243
    void testStaticImport17() {
        String contents = '''\
            |import static p.T.m
            |m('x')
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1243
    void testStaticImport18() {
        String contents = '''\
            |import static p.T.m
            |m 'x'
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1244
    void testStaticImport19() {
        addJavaSource('class C { static Object f }', 'C', '')

        String contents = '''\
            |import static C.f
            |print f
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1244
    void testStaticImport20() {
        addJavaSource('class C { static void m() {} }', 'C', '')

        String contents = '''\
            |import static C.m
            |m('x')
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1244
    void testStaticImport21() {
        addJavaSource('class C { static void m() {} }', 'C', '')

        String contents = '''\
            |import static C.m
            |m 'x'
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1309
    void testStaticImport22() {
        createGroovyType 'p', 'C', '''\
            |class C {
            |  static getClosure_property() {
            |    return { -> }
            |  }
            |}
            |'''

        for (tag in ['', '@groovy.transform.CompileStatic']) {
            String contents = """\
                |import static p.C.closure_property
                |$tag
                |void test() {
                |  closure_property()
                |}
                |"""
            doContentsCompareTest(contents)
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1309
    void testStaticImport23() {
        createGroovyType 'p', 'C', '''\
            |class C {
            |  static final callable_property = new C()
            |  static final closure_property = { -> }
            |  def call(... args) {
            |  }
            |}
            |'''

        for (tag in ['', '@groovy.transform.CompileStatic']) {
            String contents = """\
                |import static p.C.callable_property
                |import static p.C.closure_property
                |$tag
                |void test() {
                |  callable_property()
                |  closure_property()
                |}
                |"""
            doContentsCompareTest(contents)
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1309
    void testStaticImport24() {
        createGroovyType 'p', 'C', '''\
            |class C {
            |  public static callable_property = new C()
            |  public static closure_property = { -> }
            |  def call(... args) {
            |  }
            |}
            |'''

        for (tag in ['', '@groovy.transform.CompileStatic']) {
            String contents = """\
                |import static p.C.callable_property
                |import static p.C.closure_property
                |$tag
                |void test() {
                |  callable_property()
                |  closure_property()
                |}
                |"""
            doContentsCompareTest(contents)
        }
    }

    @Test
    void testRepeatStaticImport1() {
        String originalContents = '''\
            |import static java.util.Collections.emptyList
            |import static java.util.Collections.emptyList
            |List list = emptyList();
            |'''
        String expectedContents = '''\
            |import static java.util.Collections.emptyList
            |List list = emptyList();
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test @NotYetImplemented // https://github.com/groovy/groovy-eclipse/issues/732
    void testRepeatStaticImport2() {
        addJavaSource('class A { public static boolean isSomething(String s) { return true; } }', 'A', 'p')
        addJavaSource('class B { public static boolean isSomething(Iterable i) { return true; } }', 'B', 'p')

        String contents = '''\
            |import static p.A.isSomething
            |import static p.B.isSomething
            |String s
            |Iterable i
            |isSomething(s)
            |isSomething(i)
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStarImport1() {
        String contents = '''\
            |import javax.swing.text.html.*
            |'''
        doContentsCompareTest(contents, '')
    }

    @Test
    void testStarImport2() {
        String contents = '''\
            |import javax.swing.text.html.*
            |HTML
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStarImport3() {
        String originalContents = '''\
            |import javax.swing.text.html.*
            |println 'trace'
            |'''
        String expectedContents = '''\
            |println 'trace'
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStarImport4() {
        String originalContents = '''\
            |import java.util.regex.Pattern
            |import java.util.regex.*
            |Pattern p = ~/123/
            |'''
        String expectedContents = '''\
            |import java.util.regex.*
            |Pattern p = ~/123/
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStarImport5() {
        String originalContents = '''\
            |import java.util.regex.*
            |import java.util.regex.Pattern
            |Pattern p = ~/123|456/
            |Matcher m = p.matcher('456')
            |'''
        String expectedContents = '''\
            |import java.util.regex.*
            |Pattern p = ~/123|456/
            |Matcher m = p.matcher('456')
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStaticStarImport1() {
        String contents = '''\
            |import static java.lang.String.*
            |'''
        doContentsCompareTest(contents, '')
    }

    @Test // GRECLIPSE-929
    void testStaticStarImport2() {
        String contents = '''\
            |import static java.lang.String.*
            |format('fmt str', 'arg str', 929)
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-929
    void testStaticStarImport3() {
        String contents = '''\
            |import static java.lang.String.*
            |@groovy.transform.CompileStatic
            |void test() {
            |  format('fmt str', 'arg str', 929)
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testStaticStarImport4() {
        String originalContents = '''\
            |import static java.lang.String.*
            |println 'trace'
            |'''
        String expectedContents = '''\
            |println 'trace'
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testStaticStarImport5() {
        String originalContents = '''\
            |import static java.util.Collections.emptyList
            |import static java.util.Collections.*
            |List l = emptyList()
            |'''
        String expectedContents = '''\
            |import static java.util.Collections.*
            |List l = emptyList()
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/840
    void testStaticStarImport6() {
        createGroovyType 'foo', 'BarBaz', '''\
            |class Bar {
            |  static boolean isThing() {}
            |}
            |class Baz extends Bar {
            |  static boolean isThang() {}
            |}
            |'''

        String contents = '''\
            |import static foo.Bar.*
            |import static foo.Baz.*
            |class Three {
            |  void meth() {
            |    isThang()
            |    isThing()
            |  }
            |}
            |'''
        doContentsCompareTest(contents, contents - ~/\|import static foo.Bar.\*\s+/)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/840
    void testStaticStarImport7() {
        createGroovyType 'foo', 'BarBaz', '''\
            |class Bar {
            |  static boolean isThing() {}
            |}
            |class Baz extends Bar {
            |}
            |'''

        String contents = '''\
            |import static foo.Bar.*
            |import static foo.Baz.*
            |class Three {
            |  void meth() {
            |    thing
            |  }
            |}
            |'''
        doContentsCompareTest(contents, contents - ~/\|import static foo.Bar.\*\s+/)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/840
    void testStaticStarImport8() {
        createGroovyType 'foo', 'BarBaz', '''\
            |class Bar {
            |  static boolean isThing() {}
            |}
            |class Baz extends Bar {
            |}
            |'''

        String contents = '''\
            |import static foo.Bar.isThing
            |import static foo.Baz.*
            |class Three {
            |  void meth() {
            |    thing
            |  }
            |}
            |'''
        doContentsCompareTest(contents, contents - ~/\|import static foo.Bar.isThing\s+/)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1309
    void testStaticStarImport9() {
        createGroovyType 'p', 'C', '''\
            |class C {
            |  static final callable_property = new C()
            |  static final closure_property = { -> }
            |  def call(... args) {
            |  }
            |}
            |'''

        for (tag in ['', '@groovy.transform.CompileStatic']) {
            String contents = """\
                |import static p.C.*
                |$tag
                |void test() {
                |  callable_property()
                |  closure_property()
                |}
                |"""
            doContentsCompareTest(contents)
        }
    }

    @Test // GRECLIPSE-1219
    void testAnnotationsOnImports1() {
        String contents = '''\
            |@Deprecated
            |import javax.swing.text.html.*
            |'''
        doContentsCompareTest(contents, '')
    }

    @Test // GRECLIPSE-1219
    void testAnnotationsOnImports2() {
        String contents = '''\
            |@Deprecated
            |import javax.swing.text.html.HTML
            |'''
        doContentsCompareTest(contents, '')
    }

    @Test // GRECLIPSE-1219
    void testAnnotationsOnImports3() {
        String contents = '''\
            |@Deprecated
            |import javax.swing.text.html.*
            |HTML
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testFieldAnnotationImport1() {
        String contents = '''\
            |@Field
            |def x = 0
            |'''
        doAddImportTest(contents, ['groovy.transform.Field'])
    }

    @Test // GRECLIPSE-1692
    void testFieldAnnotationImport2() {
        String contents = '''\
            |import groovy.transform.Field
            |@Field
            |def x = 0
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testBuilderAnnotationImport1() {
        String contents = '''\
            |@Builder
            |class Main {
            |}
            |'''
        doAddImportTest(contents, ['groovy.transform.builder.Builder'])
    }

    @Test
    void testBuilderAnnotationImport2() {
        String originalContents = '''\
            |@Builder
            |class Main {
            |}
            |'''
        String expectedContents = '''\
            |import groovy.transform.builder.Builder
            |
            |@Builder
            |class Main {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testCompileDynamicImport1() {
        String contents = '''
            |@CompileDynamic
            |public void x() {
            |}
            |'''
        doAddImportTest(contents, ['groovy.transform.CompileDynamic'])
    }

    @Test // GRECLIPSE-1794
    void testCompileDynamicImport2() {
        String contents = '''\
            |import groovy.transform.CompileDynamic
            |@CompileDynamic
            |public void x() {
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1794
    void testCompileDynamicImport3() {
        String originalContents = '''\
            |import groovy.transform.CompileDynamic
            |import groovy.transform.CompileStatic
            |@CompileDynamic
            |public void x() {
            |}
            |'''
        String expectedContents = '''\
            |import groovy.transform.CompileDynamic
            |@CompileDynamic
            |public void x() {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testDefaultImport0() {
        String contents = '''\
            |interface Pattern {} // this should shadow java.util.regex.Pattern, et al.
            |Pattern p = null
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport1() {
        String originalContents = '''\
            |import java.util.List
            |import groovy.util.Node
            |List list
            |Node node
            |'''
        String expectedContents = '''\
            |List list
            |Node node
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport2() {
        String originalContents = '''\
            |import java.util.*
            |import groovy.util.*
            |List list
            |Node node
            |'''
        String expectedContents = '''\
            |List list
            |Node node
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // GRECLIPSE-1392
    void testDefaultImport3() {
        String originalContents = '''\
            |import java.math.BigDecimal
            |import java.math.BigInteger
            |BigDecimal bd
            |BigInteger bi
            |'''
        String expectedContents = '''\
            |BigDecimal bd
            |BigInteger bi
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testDefaultImport5() {
        String originalContents = '''\
            |import java.awt.*
            |import java.util.*
            |import java.util.List
            |List list
            |'''
        String expectedContents = '''\
            |List list
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testDefaultImport6() {
        String originalContents = '''\
            |import java.awt.*
            |import java.util.*
            |import java.util.List
            |Font font
            |List list
            |'''
        String expectedContents = '''\
            |import java.awt.*
            |import java.util.List
            |Font font
            |List list
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testDefaultImport7() {
        String originalContents = '''\
            |import java.sql.*
            |import java.util.*
            |import java.sql.Date
            |Date date
            |'''
        String expectedContents = '''\
            |import java.sql.*
            |import java.sql.Date
            |Date date
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1354
    void testDefaultImport8() {
        ['groovy.util', 'java.net', 'java.lang.reflect'].each {
            String contents = """\
                |import ${it}.Proxy
                |Proxy proxy
                |"""
            doContentsCompareTest(contents)
        }
    }

    @Test
    void testDefaultImport9() {
        String contents = '''\
            |import static java.util.Collections.swap
            |swap
            |'''
        doContentsCompareTest(contents)
    }

    @Test // GRECLIPSE-1553
    void testCompileStaticAndMapStyleConstructor() {
        createGroovyType 'example2', 'Bar', '''\
            |class Bar {
            |    String name
            |}
            |'''

        String contents = '''
            |import groovy.transform.CompileStatic
            |
            |import example2.Bar
            |
            |@CompileStatic
            |class Foo {
            |  void apply() {
            |      new Bar([name: 'test'])
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testReorderExistingImports() {
        // I've seen the static import get wiped out because it's not in the right position
        String originalContents = '''\
            |import java.util.List
            |import org.w3c.dom.Node
            |import static java.util.Collections.emptyList
            |import java.util.regex.Pattern
            |
            |Collection c = emptyList()
            |Pattern p = ~/abc/
            |Node n = null
            |'''
        String expectedContents = '''\
            |import static java.util.Collections.emptyList
            |
            |import java.util.regex.Pattern
            |
            |import org.w3c.dom.Node
            |
            |Collection c = emptyList()
            |Pattern p = ~/abc/
            |Node n = null
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testOrganizeOnInvalidSource() {
        String contents = '''\
            |import java.util.regex.Matcher
            |import java.util.regex.Pattern
            |
            |class C {
            |  Pattern pattern = ~/123/
            |  Matcher matcher(String string) {
            |    def x /*=*/ pattern.matcher(string)
            |    return x
            |  }
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testOrganizeWithExtraImports1() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    normal 'java.util.regex.Matcher'
            |    normal 'java.util.regex.Pattern'
            |  }
            |}
            |'''

        String originalContents = '''\
            |import java.util.regex.Matcher
            |
            |class C {
            |  Pattern pattern = ~/123/
            |  Matcher matcher(String string) {
            |    pattern.matcher(string)
            |  }
            |}
            |'''
        String expectedContents = '''\
            |class C {
            |  Pattern pattern = ~/123/
            |  Matcher matcher(String string) {
            |    pattern.matcher(string)
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
            |    star 'groovy.transform'
            |    normal 'java.util.concurrent.TimeUnit'
            |    staticStar 'java.util.concurrent.TimeUnit'
            |  }
            |}
            |'''

        String contents = '''\
            |@Field
            |TimeUnit units = DAYS
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testOrganizeWithExtraImports3() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'groovy.transform'
            |  }
            |}
            |'''

        String originalContents = '''\
            |import groovy.transform.CompileStatic
            |
            |@CompileStatic
            |def method() {
            |}
            |'''
        String expectedContents = '''\
            |@CompileStatic
            |def method() {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testOrganizeWithExtraImports4() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'java.lang.annotation'
            |  }
            |}
            |'''

        String originalContents = '''\
            |import java.lang.annotation.*
            |import java.lang.annotation.ElementType
            |
            |@Target(ElementType.TYPE)
            |@interface Tag {
            |}
            |'''
        String expectedContents = '''\
            |@Target(ElementType.TYPE)
            |@interface Tag {
            |}
            |'''
        doContentsCompareTest(originalContents, expectedContents)
    }

    @Test
    void testOrganizeWithExtraImports5() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'java.awt'
            |  }
            |}
            |'''

        String contents = '''\
            |import java.util.List
            |
            |Font font
            |List list
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testOrganizeWithExtraImports6() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'java.sql'
            |  }
            |}
            |'''

        String contents = '''\
            |import java.sql.Date
            |
            |Date sqlDateNotUtilDate
            |'''
        doContentsCompareTest(contents)
    }

    @Test
    void testOrganizeWithExtraImports7() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'groovy.transform'
            |    alias 'Regexp', 'java.util.regex.Pattern'
            |  }
            |}
            |'''

        createGroovyType '', 'One', '''\
            |@Canonical
            |class One {
            |  String string
            |  Number number
            |  private Regexp pattern
            |  void setPattern(Regexp pattern) {
            |    this.pattern = pattern
            |  }
            |}
            |'''

        String contents = '''\
            |@CompileStatic
            |final class Tests {
            |  @org.junit.Test
            |  void testCtors() {
            |    One one = new One('value') // error: Cannot find matching method
            |    Two two = new Two('value')
            |  }
            |}
            |@Canonical @CompileStatic
            |class Two {
            |  String value
            |}
            |'''
        doContentsCompareTest(contents)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/840
    void testOrganizeWithExtraImports8() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    staticStar 'foo.Baz'
            |  }
            |}
            |'''

        createGroovyType 'foo', 'BarBaz', '''\
            |class Bar {
            |  static boolean isThing() {}
            |}
            |class Baz extends Bar {
            |  static boolean isThang() {}
            |}
            |'''

        String contents = '''\
            |import static foo.Bar.*
            |class Three {
            |  void meth() {
            |    isThang()
            |    isThing()
            |  }
            |}
            |'''
        doContentsCompareTest(contents, contents - ~/\|import static foo.Bar.\*\s+/)
    }

    @Test
    void testOrganizeWithExtraImports9() {
        addConfigScript '''\
            |withConfig(configuration) {
            |  imports {
            |    star 'java.time'
            |  }
            |}
            |'''

        String contents = '''\
            |import groovy.transform.*
            |/*
            |*/
            |'''
        doContentsCompareTest(contents, contents - ~/\|import groovy.transform.\*\s+/)
    }

    @Test @NotYetImplemented
    void testOrganizeWithInterleavedComments() {
        String originalContents = '''\
            |import java.util.regex.Pattern
            |// blah blah blah blah blah...
            |import java.util.regex.Matcher
            |
            |class C {
            |  Pattern pattern = ~/123/
            |  Matcher matcher(String string) {
            |    def x = pattern.matcher(string)
            |    return x
            |  }
            |}
            |'''
        String expectedContents = '''\
            |// blah blah blah blah blah...
            |import java.util.regex.Matcher
            |import java.util.regex.Pattern
            |
            |class C {
            |  Pattern pattern = ~/123/
            |  Matcher matcher(String string) {
            |    def x = pattern.matcher(string)
            |    return x
            |  }
            |}
            |'''

        doContentsCompareTest(originalContents, expectedContents)
    }

    // TODO: Ensure imports come below header comment for class in the default package.
}
