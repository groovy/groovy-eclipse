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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser
import static org.junit.Assume.assumeTrue

import groovy.test.NotYetImplemented

import org.eclipse.jdt.core.SourceRange
import org.junit.Test

final class CodeSelectTypesTests extends BrowsingTestSuite {

    @Test
    void testSelectThisClass() {
        String contents = 'class Type { }'
        assertCodeSelect([contents], 'Type')
    }

    @Test
    void testSelectSuperClass() {
        String another = 'class Super { }'
        String contents = 'class Type extends Super { }'
        assertCodeSelect([another, contents], 'Super')
    }

    @Test
    void testSelectSuperClass2() {
        String contents = 'class Type extends java.util.Date { }'
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testSelectSuperClass2a() {
        String contents = 'class Type extends java.util.Date { }'
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectSuperClass3() {
        String contents = 'abstract class Type extends java.lang.Number { }'
        assertCodeSelect([contents], 'Number')
    }

    @Test
    void testSelectSuperClass3a() {
        String contents = 'abstract class Type extends java.lang.Number { }'
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectSuperClass4() {
        String another = 'class Super { }'
        // "<T extends Type<T>>" allows methods to return this as sub-type (aka T)
        String contents = 'abstract class Type<T extends Type<T>> extends Super { }'
        assertCodeSelect([another, contents], 'Super')
    }

    @Test
    void testSelectSuperInterface() {
        String another = 'interface Super { }'
        String contents = 'class Type implements Super { }'
        assertCodeSelect([another, contents], 'Super')
    }

    @Test
    void testSelectSuperInterface2() {
        String contents = 'abstract class Type implements java.util.List { }'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testSelectSuperInterface2a() {
        String contents = 'abstract class Type implements java.util.List { }'
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectSuperInterface3() {
        String contents = 'abstract class Type implements groovy.lang.MetaClass { }'
        assertCodeSelect([contents], 'MetaClass')
    }

    @Test
    void testSelectSuperInterface3a() {
        String contents = 'abstract class Type implements groovy.lang.MetaClass { }'
        assertCodeSelect([contents], 'lang', 'groovy.lang')
        assertCodeSelect([contents], 'groovy', 'groovy')
    }

    @Test
    void testSelectSuperInterface4() {
        String contents = '@groovy.transform.AutoImplement(code={ 0 }) class Type implements Iterator<String> { }'
        assertCodeSelect([contents], 'Iterator')
        assertCodeSelect([contents], 'String')
    }

    @Test
    void testSelectAnnotationClass1() {
        String another = '@interface Anno { }'
        String contents = '@Anno class Type { }'
        assertCodeSelect([another, contents], 'Anno')
    }

    @Test
    void testSelectAnnotationClass2() {
        String contents = '@Deprecated class Type { }'
        assertCodeSelect([contents], 'Deprecated')
    }

    @Test
    void testSelectAnnotationClass2a() {
        String contents = '@java.lang.Deprecated class Type { }'
        assertCodeSelect([contents], 'Deprecated')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'lang', 'java.lang')
    }

    @Test
    void testSelectAnnotationClass3() {
        String contents = 'import groovy.transform.*; @Field String field'
        assertCodeSelect([contents], 'Field')
    }

    @Test
    void testSelectAnnotationClass4() {
        // CompileDynamic is an AnnotationCollector, so it is not in the AST after transformation
        String contents = 'import groovy.transform.CompileDynamic; @CompileDynamic class Type { }'
        assertCodeSelect([contents], 'CompileDynamic')
    }

    @Test
    void testSelectAnnotationClass5() {
        String contents = 'import groovy.transform.*; @AnnotationCollector(EqualsAndHashCode) @interface Custom { }'
        assertCodeSelect([contents], 'EqualsAndHashCode')
    }

    @Test
    void testSelectAnnotationClass5a() {
        String contents = 'import groovy.transform.*; @EqualsAndHashCode @AnnotationCollector @interface Custom { }'
        assertCodeSelect([contents], 'EqualsAndHashCode')
    }

    @Test
    void testSelectAnnotationClass6() {
        String another = 'import java.lang.annotation.*; @Target(ElementType.FIELD) @interface Tag { String value() }'
        String contents = 'enum Foo { @Tag("Bar") Baz }'
        assertCodeSelect([another, contents], 'Tag')
        assertCodeSelect([another, contents], 'Baz')
    }

    // fields

    @Test
    void testSelectFieldType1() {
        String contents = 'class Type { Date x }'
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testSelectFieldType1a() {
        String contents = 'class Type { java.util.Date x }'
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectFieldType2() {
        String contents = 'class Type { java.lang.Number x }'
        assertCodeSelect([contents], 'Number')
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testSelectFieldType2a() {
        String contents = 'class Type { java.lang.Number x }'
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectFieldType3() {
        String contents = 'class Type { List<java.util.Date> x }'
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testSelectFieldType3a() {
        String contents = 'class Type { List<java.util.Date> x }'
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectFieldType4() {
        String contents = 'class Type { List<java.lang.Number> x }'
        assertCodeSelect([contents], 'Number')
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testSelectFieldType4a() {
        String contents = 'class Type { List<java.lang.Number> x }'
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'java', 'java')
    }

    // methods

    @Test
    void testSelectMethodReturnType1() {
        String contents = 'class Type { List x() { return [] } }'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testSelectMethodReturnType1a() {
        String contents = 'class Type { java.util.List x() { return [] } }'
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectMethodReturnType2() {
        String contents = 'class Type { groovy.lang.MetaClass x() { return [] } }'
        assertCodeSelect([contents], 'MetaClass')
    }

    @Test
    void testSelectMethodReturnType2a() {
        String contents = 'class Type { groovy.lang.MetaClass x() { return [] } }'
        assertCodeSelect([contents], 'lang', 'groovy.lang')
    }

    @Test
    void testSelectMethodReturnType3() {
        String contents = 'class Type { List<java.util.Date> x() { return [] } }'
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testSelectMethodReturnType3a() {
        String contents = 'class Type { List<java.util.Date> x() { return [] } }'
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectMethodReturnType4() {
        String contents = 'class Type { List<java.lang.Number> x() { return [] } }'
        assertCodeSelect([contents], 'Number')
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testSelectMethodReturnType4a() {
        String contents = 'class Type { List<java.lang.Number> x() { return [] } }'
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectMethodParamType1() {
        String contents = 'class Type { def x(List y) {} }'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testSelectMethodParamType1a() {
        String contents = 'class Type { def x(java.util.List y) {} }'
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectMethodParamType2() {
        String contents = 'class Type { def x(groovy.lang.MetaClass y) {} }'
        assertCodeSelect([contents], 'MetaClass')
    }

    @Test
    void testSelectMethodParamType2a() {
        String contents = 'class Type { def x(groovy.lang.MetaClass y) {} }'
        assertCodeSelect([contents], 'lang', 'groovy.lang')
    }

    @Test
    void testSelectMethodParamType3() {
        String contents = 'class Type { def x(List<java.util.Date> y) {} }'
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testSelectMethodParamType3a() {
        String contents = 'class Type { def x(List<java.util.Date> y) {} }'
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectMethodParamType4() {
        String contents = 'class Type { def x(List<java.lang.Number> y) {} }'
        assertCodeSelect([contents], 'Number')
    }

    @Test
    void testSelectMethodParamType4a() {
        String contents = 'class Type { def x(List<java.lang.Number> y) {} }'
        assertCodeSelect([contents], 'lang', 'java.lang')
    }

    @Test
    void testSelectMethodVarargType1() {
        String contents = 'class Type { def x(Date... y) {} }'
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testSelectMethodVarargType1a() {
        String contents = 'class Type { def x(java.util.Date... y) {} }'
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testSelectMethodVarargType2() {
        String contents = 'class Type { def x(Number... y) {} }'
        assertCodeSelect([contents], 'Number')
    }

    @Test
    void testSelectMethodVarargType2a() {
        String contents = 'class Type { def x(java.lang.Number... y) {} }'
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'Number')
    }

    // variables

    @Test
    void testSelectLocalVarType() {
        String contents = 'class Type { def x() { List y } }'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testSelectLocalVarTypeInClosure() {
        String contents = 'class Type { def x() { def foo = {\n   List y } } }'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testSelectLocalVarTypeInScript() {
        String contents = 'List y'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testSelectCatchParamType() {
        String contents = 'try {\n} catch (Exception ex) { ex.printStackTrace() }'
        assertCodeSelect([contents], 'Exception')
    }

    @Test
    void testSelectCatchParamType2() {
        String contents = 'try {\n} catch (Exception | java.lang.Error e) { e.cause }'
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'Exception')
        assertCodeSelect([contents], 'Error')
    }

    @Test
    void testSelectForEachParamType() {
        String contents = 'List<String> list; for (String s : list) { println s }'
        assertCodeSelect([contents], 'String')
    }

    @Test
    void testSelectForEachInParamType() {
        String contents = 'List<String> list; for (String s in list) { println s }'
        assertCodeSelect([contents], 'String')
    }

    @Test
    void testSelectForEachInParamTypeTC() {
        String contents = '@groovy.transform.TypeChecked def m() { List<String> list; for (String s in list) { println s } }'
        assertCodeSelect([contents], 'String')
    }

    @Test
    void testSelectForEachInParamTypeCS() {
        // @see StaticCompilationVisitor.visitForLoop -- param type is set to class node from 'List<String>', which impacts code select
        String contents = '@groovy.transform.CompileStatic def m() { List<String> list; for (String s in list) { println s } }'
        assertCodeSelect([contents], 'String')
    }

    @Test // GRECLIPSE-800
    void testSelectInnerType() {
        String contents = 'class Outer { \n def m() { Inner x = new Inner() } \n class Inner { } }'
        assertCodeSelect(contents, new SourceRange(contents.indexOf('Inner'), 1), 'Inner')
    }

    @Test // GRECLIPSE-800
    void testSelectInnerType2() {
        String contents = 'class Outer { \n def m() { new Inner() } \n class Inner { } }'
        assertCodeSelect(contents, new SourceRange(contents.indexOf('Inner'), 1), 'Inner')
    }

    @Test // GRECLIPSE-800
    void testSelectInnerType3() {
        String contents = 'class Outer { \n def m() { Inner } \n class Inner { } }'
        assertCodeSelect(contents, new SourceRange(contents.indexOf('Inner'), 1), 'Inner')
    }

    @Test // GRECLIPSE-803
    void testSelectInnerType4() {
        String contents = 'class Outer { \n class Inner { } }'
        assertCodeSelect([contents], 'Inner')
    }

    @Test // GRECLIPSE-803
    void testSelectInnerType5() {
        String contents = 'class Outer { \n class Inner { \n class InnerInner { } } }'
        assertCodeSelect([contents], 'InnerInner')
    }

    @Test // GRECLIPSE-803
    void testSelectInnerType6() {
        String contents = 'class Outer { class Inner { class InnerInner { class InnerInnerInner { } } } }'
        assertCodeSelect([contents], 'InnerInnerInner')
    }

    @Test
    void testSelectInnerType7() {
        String contents = '''\
            |@groovy.transform.TypeChecked
            |class Outer {
            |  def method() { }
            |  @Deprecated
            |  static class Inner extends Object { }
            |  @Deprecated
            |  abstract static class Inert extends Number { }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'Inert')
    }

    @Test
    void testSelectAnonymousInnerEnum() {
        String contents = 'enum Outer { ONE() { def m() { true } } \n abstract def m(); }'
        assertCodeSelect(contents, new SourceRange(contents.indexOf('ONE() {') + 7, 0), null)
    }

    @Test
    void testSelectAnonymousInnerType() {
        String contents = 'def entry = new Map.Entry() {}'
        assertCodeSelect([contents], 'Map')
        assertCodeSelect([contents], 'Entry')
        assertCodeSelect([contents], '{', null)
    }

    @Test
    void testSelectAnonymousInnerType2() {
        String contents = 'def runner = new java.lang.Runnable() {}'
        assertCodeSelect([contents], 'Runnable')
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectAnonymousInnerType3() {
        String contents = 'def throwable = new java.lang.Exception() {}'
        assertCodeSelect([contents], 'Exception')
        assertCodeSelect([contents], 'lang', 'java.lang')
        assertCodeSelect([contents], 'java', 'java')
    }

    @Test
    void testSelectQualifyingType() {
        String contents = 'for (Map.Entry e : [:].entrySet()) { }'
        assertCodeSelect([contents], 'Map')
    }

    @Test
    void testSelectInnerQualifiedType() {
        String contents = 'for (Map.Entry e : [:].entrySet()) { }'
        assertCodeSelect([contents], 'Entry')
    }

    @Test
    void testSelectAliasedQualifyingType() {
        String contents = 'import java.util.Map as Foo; for (Foo.Entry e : [:].entrySet()) { }'
        assertCodeSelect([contents], 'Foo', 'Map')
    }

    @Test
    void testSelectAliasedInnerQualifiedType() {
        String contents = 'import java.util.Map as Foo; for (Foo.Entry e : [:].entrySet()) { }'
        assertCodeSelect([contents], 'Entry')
    }

    @Test
    void testSelectNestedQualifiedType() {
        addGroovySource('interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d')
        String contents = 'a.b.c.d.E.F.G.H'
        assertCodeSelect([contents], 'd', 'a.b.c.d')
        assertCodeSelect([contents], 'E')
        assertCodeSelect([contents], 'F')
        assertCodeSelect([contents], 'G')
        assertCodeSelect([contents], 'H')
    }

    @Test
    void testSelectNestedQualifyingType() {
        addGroovySource('interface M { interface N { interface O { String P = "Q" } } }', 'M', 'i.j.k.l')
        String contents = 'import i.j.k.l.M\nM.N.O.P'
        assertCodeSelect([contents], 'M')
        assertCodeSelect([contents], 'N')
        assertCodeSelect([contents], 'O')
        assertCodeSelect([contents], 'P')
    }

    @Test
    void testSelectAliasedNestedQualifyingType() {
        addGroovySource('interface V { interface W { interface X { String Y = "Z" } } }', 'V', 'r.s.t.u')
        String contents = 'import r.s.t.u.V as AA\nAA.W.X.Y'
        assertCodeSelect([contents], 'AA', 'V')
        assertCodeSelect([contents], 'W')
        assertCodeSelect([contents], 'X')
        assertCodeSelect([contents], 'Y')
    }

    @Test
    void testSelectCoercionType1() {
        String contents = 'def i = 1 as int'
        assertCodeSelect([contents], 'int', null)
    }

    @Test
    void testSelectCoercionType2() {
        String contents = 'def c = [] as Collection'
        assertCodeSelect([contents], 'Collection')
    }

    @Test
    void testSelectCoercionType3() {
        String contents = 'def p = /abc/ as java.util.regex.Pattern'
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectTypecastType1() {
        String contents = 'def i = (int) 1'
        assertCodeSelect([contents], 'int', null)
    }

    @Test
    void testSelectTypecastType2() {
        String contents = 'def c = (Collection) []'
        assertCodeSelect([contents], 'Collection')
    }

    @Test
    void testSelectTypecastType3() {
        String contents = 'def p = (java.util.regex.Pattern) "abc"'
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectTypecastType4() {
        String contents = 'def arr = (Object[]) null'
        assertCodeSelect([contents], 'Object')
    }

    @Test // GRECLIPSE-1219
    void testSelectAnnotationOnImport() {
        String contents = '@Deprecated import java.util.List; class Type { }'
        assertCodeSelect([contents], 'Deprecated')
    }

    @Test
    void testSelectAnnotationOnMethod() {
        String contents = 'class Type { @Deprecated void method() {} }'
        assertCodeSelect([contents], 'Deprecated')
    }

    @Test
    void testSelectAnnotationOnMethod2() {
        String contents = 'import groovy.transform.*; class Type { @CompileStatic void method() {} }'
        assertCodeSelect([contents], 'CompileStatic')
    }

    @Test
    void testSelectAnnotationOnMethod3() {
        String contents = 'import groovy.transform.*; class Type { @CompileDynamic void method() {} }'
        assertCodeSelect([contents], 'CompileDynamic')
    }

    @Test
    void testSelectAnnotationOnMethod4() {
        String contents = 'import groovy.transform.*; class Type { @TypeChecked(TypeCheckingMode.SKIP) void method() {} }'
        assertCodeSelect([contents], 'TypeChecked')
        assertCodeSelect([contents], 'TypeCheckingMode')
    }

    @Test
    void testSelectAnnotationAsValue() {
        String support = '@interface Tag { String value() }\n@interface Wrapper { Tag[] value() }'
        String contents = '@Wrapper(@Tag("1")) def x'
        assertCodeSelect([support, contents], 'Wrapper')
        assertCodeSelect([support, contents], 'Tag')
    }

    @Test
    void testSelectAnnotationAsDefaultValue() {
        String contents = '@interface Suppressor {\n  SuppressWarnings value() default @SuppressWarnings("nls")\n}'
        assertCodeSelect([contents], 'SuppressWarnings')
    }

    @Test
    void testSelectLazyFieldType() {
        String contents = 'class Type { @Lazy Date x }'
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'Lazy')
    }

    @Test
    void testSelectLineEndingsSupport() {
        // fails if visitClass -> getSource doesn't account for line endings
        String contents = '@Deprecated\r\n\r\nclass Type { Date x }'
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName1() {
        String contents = 'java.util.Date x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName1a() {
        String contents = 'java.util.regex.Pattern x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName3() {
        String contents = 'java.util.Date[][] x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName3a() {
        String contents = 'java.util.regex.Pattern[][] x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName4() {
        String contents = 'java.util.List<java.util.Date> x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'List')
        assertCodeSelect(contents, new SourceRange(contents.indexOf('java'), 1), 'java')
        assertCodeSelect(contents, new SourceRange(contents.indexOf('util'), 1), 'java.util')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName4a() {
        String contents = 'java.util.List<java.util.regex.Pattern> x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'List')
        assertCodeSelect(contents, new SourceRange(contents.indexOf('java'), 1), 'java')
        assertCodeSelect(contents, new SourceRange(contents.indexOf('util'), 1), 'java.util')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName4b() {
        String contents = 'java.util.List<java.util.regex.Pattern>[] x'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'List')
        assertCodeSelect(contents, new SourceRange(contents.indexOf('java'), 1), 'java')
        assertCodeSelect(contents, new SourceRange(contents.indexOf('util'), 1), 'java.util')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName5() {
        String contents = 'def z = new java.util.Date()'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName5a() {
        String contents = 'def z = new java.util.regex.Pattern()'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName6() {
        String contents = 'def z = new java.util.Date[9][10]'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName6a() {
        String contents = 'def z = new java.util.regex.Pattern[9][10][]'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName7() {
        assumeTrue(isParrotParser())

        String contents = 'def z = new java.util.Date[][] {}'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName7a() {
        assumeTrue(isParrotParser())

        String contents = 'def z = new java.util.regex.Pattern[][][] {      }'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'Pattern')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'regex', 'java.util.regex')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName7b() {
        assumeTrue(isParrotParser())

        String contents = 'def z = new java.lang.String[] { "a", "b", "c" }'
        assertCodeSelect([contents], 'z')
        assertCodeSelect([contents], 'String')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'lang', 'java.lang')
    }

    @Test
    void testSelectPackageOnFullyQualifiedName8() {
        String contents = 'def x = java.util.Collections.emptyList()'
        assertCodeSelect([contents], 'x')
        assertCodeSelect([contents], 'Collections')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'util', 'java.util')
    }

    // generics

    @Test
    void testSelectGenericType1() {
        String contents = 'class Foo<T> {}'
        assertCodeSelect([contents], 'T')
    }

    @Test
    void testSelectGenericType2() {
        String contents = 'class Foo<T extends Date> {}'
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'T')
    }

    @Test
    void testSelectGenericType3() {
        String contents = 'class Foo<T extends Date & List> {}'
        assertCodeSelect([contents], 'List')
        assertCodeSelect([contents], 'Date')
        assertCodeSelect([contents], 'T')
    }

    @Test
    void testSelectGenericType4() {
        String contents = 'class Foo<T extends Object & MetaClass> {}'
        assertCodeSelect([contents], 'MetaClass')
        assertCodeSelect([contents], 'Object')
        assertCodeSelect([contents], 'T')
    }

    @Test
    void testSelectMethodGenericType1() {
        String contents = 'class Foo { def <T> T x() { null } }'
        assertCodeSelect(contents, new SourceRange(contents.indexOf('T'), 1), 'T')
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf('T'), 1), 'T')
    }

    @Test
    void testSelectMethodGenericType2() {
        String contents = 'class Foo<T> { def T x() { null } }'
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf('T'), 1), 'T')
    }

    @Test
    void testSelectMethodCallGenericType1() {
        String contents = '''\
            |import java.util.regex.*
            |class Foo {
            |  def <T> T m() { null }
            |}
            |new Foo().<Matcher>m()
            |'''.stripMargin()
        assertCodeSelect([contents], 'Matcher')
    }

    @Test
    void testSelectMethodCallGenericType2() {
        String contents = 'import java.util.regex.*; Collections.<Matcher>emptyList()'
        assertCodeSelect([contents], 'Matcher')
    }

    // javadocs

    @Test @NotYetImplemented
    void testSelectTypeInJavadocLink() {
        String contents = '/** {@link java.util.regex.Pattern} */ class X { }'
        assertCodeSelect([contents], 'Pattern')
    }

    @Test @NotYetImplemented
    void testSelectTypeInJavadocLink2() {
        String contents = 'import java.util.regex.Pattern; /** {@link Pattern} */ class X { }'
        assertCodeSelect([contents], 'Pattern')
    }
}
