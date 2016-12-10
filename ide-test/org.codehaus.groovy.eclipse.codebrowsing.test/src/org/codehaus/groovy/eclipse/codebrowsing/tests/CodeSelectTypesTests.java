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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import static java.util.Arrays.asList;

import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.tests.util.GroovyUtils;

/**
 * @author Andrew Eisenberg
 * @created Jul 14, 2009
 */
public final class CodeSelectTypesTests extends BrowsingTestCase {

    public static junit.framework.Test suite() {
        return newTestSuite(CodeSelectTypesTests.class);
    }

    public void testSelectThisClass() {
        String contents = "class Type { }";
        assertCodeSelect(asList(contents), "Type");
    }

    public void testSelectSuperClass() {
        String another = "class Super { }";
        String contents = "class Type extends Super { }";
        assertCodeSelect(asList(another, contents), "Super");
    }

    public void testSelectSuperClass2() {
        String contents = "class Type extends java.util.Date { }";
        assertCodeSelect(asList(contents), "Date");
    }

    public void testSelectSuperClass2a() {
        String contents = "class Type extends java.util.Date { }";
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectSuperClass3() {
        String contents = "abstract class Type extends java.lang.Number { }";
        assertCodeSelect(asList(contents), "Number");
    }

    public void testSelectSuperClass3a() {
        String contents = "abstract class Type extends java.lang.Number { }";
        assertCodeSelect(asList(contents), "lang", "java.lang");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectSuperInterface() {
        String another = "interface Super { }";
        String contents = "class Type implements Super { }";
        assertCodeSelect(asList(another, contents), "Super");
    }

    public void testSelectSuperInterface2() {
        String contents = "abstract class Type implements java.util.List { }";
        assertCodeSelect(asList(contents), "List");
    }

    public void testSelectSuperInterface2a() {
        String contents = "abstract class Type implements java.util.List { }";
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectSuperInterface3() {
        String contents = "abstract class Type implements groovy.lang.MetaClass { }";
        assertCodeSelect(asList(contents), "MetaClass");
    }

    public void testSelectSuperInterface3a() {
        String contents = "abstract class Type implements groovy.lang.MetaClass { }";
        assertCodeSelect(asList(contents), "lang", "groovy.lang");
        assertCodeSelect(asList(contents), "groovy", "groovy");
    }

    public void testSelectAnnotationClass() {
        String another = "@interface Anno { }";
        String contents = "@Anno class Type { }";
        assertCodeSelect(asList(another, contents), "Anno");
    }

    public void testSelectAnnotationClass2() {
        String contents = "@java.lang.Deprecated class Type { }";
        assertCodeSelect(asList(contents), "Deprecated");
    }

    public void testSelectAnnotationClass2a() {
        String contents = "@java.lang.Deprecated class Type { }";
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "lang", "java.lang");
    }

    public void testSelectAnnotationClass3() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return; // CompileDynamic was added in 2.1
        // CompileDynamic is an AnnotationCollector, so it is not in the AST after transformation
        String contents = "import groovy.transform.CompileDynamic; @CompileDynamic class Type { }";
        assertCodeSelect(asList(contents), "CompileDynamic");
    }

    public void testSelectAnnotationClass4() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return; // AnnotationCollector was added in 2.1
        String contents = "import groovy.transform.*; @AnnotationCollector([EqualsAndHashCode]) public @interface Custom { }";
        assertCodeSelect(asList(contents), "EqualsAndHashCode");
    }

    public void testSelectAnnotationClass4a() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return; // AnnotationCollector was added in 2.1
        String contents = "import groovy.transform.*; @EqualsAndHashCode @AnnotationCollector public @interface Custom { }";
        assertCodeSelect(asList(contents), "EqualsAndHashCode");
    }

    public void testSelectAnnotationValue() {
        String another = "@interface RunWith {\n  Class value()\n}\nclass Runner { }";
        String contents = "@RunWith(Runner)\nclass ATest { }";
        assertCodeSelect(asList(another, contents), "Runner");
    }

    public void testSelectAnnotationValue2() {
        String another = "enum Foo {\nFOO1, FOO2\n} \n@interface RunWith {\nFoo value();\n}";
        String contents = "@RunWith(Foo.FOO1)\nclass ATest { }";
        assertCodeSelect(asList(another, contents), "Foo");
    }

    public void testSelectAnnotationValue3() {
        String another = "enum Foo {\nFOO1, FOO2\n} \n@interface RunWith {\nFoo value();\n}";
        String contents = "@RunWith(Foo.FOO1)\nclass ATest { }";
        assertCodeSelect(asList(another, contents), "FOO1");
    }

    // fields

    public void testSelectFieldType1() {
        String contents = "class Type { Date x }";
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "x");
    }

    public void testSelectFieldType1a() {
        String contents = "class Type { java.util.Date x }";
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectFieldType2() {
        String contents = "class Type { java.lang.Number x }";
        assertCodeSelect(asList(contents), "Number");
        assertCodeSelect(asList(contents), "x");
    }

    public void testSelectFieldType2a() {
        String contents = "class Type { java.lang.Number x }";
        assertCodeSelect(asList(contents), "lang", "java.lang");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectFieldType3() {
        String contents = "class Type { List<java.util.Date> x }";
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "x");
    }

    public void testSelectFieldType3a() {
        String contents = "class Type { List<java.util.Date> x }";
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectFieldType4() {
        String contents = "class Type { List<java.lang.Number> x }";
        assertCodeSelect(asList(contents), "Number");
        assertCodeSelect(asList(contents), "x");
    }

    public void testSelectFieldType4a() {
        String contents = "class Type { List<java.lang.Number> x }";
        assertCodeSelect(asList(contents), "lang", "java.lang");
        assertCodeSelect(asList(contents), "java", "java");
    }

    // methods

    public void testSelectMethodReturnType1() {
        String contents = "class Type { List x() { return [] } }";
        assertCodeSelect(asList(contents), "List");
    }

    public void testSelectMethodReturnType1a() {
        String contents = "class Type { java.util.List x() { return [] } }";
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectMethodReturnType2() {
        String contents = "class Type { groovy.lang.MetaClass x() { return [] } }";
        assertCodeSelect(asList(contents), "MetaClass");
    }

    public void testSelectMethodReturnType2a() {
        String contents = "class Type { groovy.lang.MetaClass x() { return [] } }";
        assertCodeSelect(asList(contents), "lang", "groovy.lang");
    }

    public void testSelectMethodReturnType3() {
        String contents = "class Type { List<java.util.Date> x() { return [] } }";
        assertCodeSelect(asList(contents), "Date");
    }

    public void testSelectMethodReturnType3a() {
        String contents = "class Type { List<java.util.Date> x() { return [] } }";
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectMethodReturnType4() {
        String contents = "class Type { List<java.lang.Number> x() { return [] } }";
        assertCodeSelect(asList(contents), "Number");
        assertCodeSelect(asList(contents), "x");
    }

    public void testSelectMethodReturnType4a() {
        String contents = "class Type { List<java.lang.Number> x() { return [] } }";
        assertCodeSelect(asList(contents), "lang", "java.lang");
        assertCodeSelect(asList(contents), "java", "java");
    }

    public void testSelectMethodParamType1() {
        String contents = "class Type { def x(List y) {} }";
        assertCodeSelect(asList(contents), "List");
    }

    public void testSelectMethodParamType1a() {
        String contents = "class Type { def x(java.util.List y) {} }";
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectMethodParamType2() {
        String contents = "class Type { def x(groovy.lang.MetaClass y) {} }";
        assertCodeSelect(asList(contents), "MetaClass");
    }

    public void testSelectMethodParamType2a() {
        String contents = "class Type { def x(groovy.lang.MetaClass y) {} }";
        assertCodeSelect(asList(contents), "lang", "groovy.lang");
    }

    public void testSelectMethodParamType3() {
        String contents = "class Type { def x(List<java.util.Date> y) {} }";
        assertCodeSelect(asList(contents), "Date");
    }

    public void testSelectMethodParamType3a() {
        String contents = "class Type { def x(List<java.util.Date> y) {} }";
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectMethodParamType4() {
        String contents = "class Type { def x(List<java.lang.Number> y) {} }";
        assertCodeSelect(asList(contents), "Number");
    }

    public void testSelectMethodParamType4a() {
        String contents = "class Type { def x(List<java.lang.Number> y) {} }";
        assertCodeSelect(asList(contents), "lang", "java.lang");
    }

    public void testSelectMethodVarargType1() {
        String contents = "class Type { def x(Date... y) {} }";
        assertCodeSelect(asList(contents), "Date");
    }

    public void testSelectMethodVarargType1a() {
        String contents = "class Type { def x(java.util.Date... y) {} }";
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "Date");
    }

    public void testSelectMethodVarargType2() {
        String contents = "class Type { def x(Number... y) {} }";
        assertCodeSelect(asList(contents), "Number");
    }

    public void testSelectMethodVarargType2a() {
        String contents = "class Type { def x(java.lang.Number... y) {} }";
        assertCodeSelect(asList(contents), "lang", "java.lang");
        assertCodeSelect(asList(contents), "Number");
    }

    // variables

    public void testSelectLocalVarType() {
        String contents = "class Type { def x() { List y } }";
        assertCodeSelect(asList(contents), "List");
    }

    public void testSelectLocalVarTypeInClosure() {
        String contents = "class Type { def x() { def foo = {\n   List y } } }";
        assertCodeSelect(asList(contents), "List");
    }

    public void testSelectLocalVarTypeInScript() {
        String contents = "List y";
        assertCodeSelect(asList(contents), "List");
    }

    // GRECLIPSE-548
    public void testSelectThis1() {
        String contents = "class AClass { def x() { this } }";
        assertCodeSelect(asList(contents), "this", "AClass");
    }

    // GRECLIPSE-548
    public void testSelectThis2() {
        String contents = "class AClass { def x() { this.toString() } }";
        assertCodeSelect(asList(contents), "this", "AClass");
    }

    // GRECLIPSE-548
    public void testSelectSuper1() {
        String contents = "class Super { } \n class AClass extends Super { def x() { super } }";
        assertCodeSelect(asList(contents), "super", "Super");
    }

    // GRECLIPSE-548
    public void testSelectSuper2() {
        String contents = "class Super { } \n class AClass extends Super { def x() { super.toString() } }";
        assertCodeSelect(asList(contents), "super", "Super");
    }

    // GRECLIPSE-800
    public void testSelectInnerType() {
        String contents = "class Outer { \n def m() { Inner x = new Inner() } \n class Inner { } }";
        assertCodeSelect(contents, new SourceRange(contents.indexOf("Inner"), 1), "Inner");
    }

    // GRECLIPSE-800
    public void testSelectInnerType2() {
        String contents = "class Outer { \n def m() { new Inner() } \n class Inner { } }";
        assertCodeSelect(contents, new SourceRange(contents.indexOf("Inner"), 1), "Inner");
    }

    // GRECLIPSE-800
    public void testSelectInnerType3() {
        String contents = "class Outer { \n def m() { Inner } \n class Inner { } }";
        assertCodeSelect(contents, new SourceRange(contents.indexOf("Inner"), 1), "Inner");
    }

    // GRECLIPSE-803
    public void testSelectInnerType4() {
        String contents = "class Outer { \n class Inner { } }";
        assertCodeSelect(asList(contents), "Inner");
    }

    // GRECLIPSE-803
    public void testSelectInnerType5() {
        String contents = "class Outer { \n class Inner { \n class InnerInner { } } }";
        assertCodeSelect(asList(contents), "InnerInner");
    }

    // GRECLIPSE-803
    public void testSelectInnerType6() {
        String contents = "class Outer { \n class Inner { \n class InnerInner { \n class InnerInnerInner { } } } }";
        assertCodeSelect(asList(contents), "InnerInnerInner");
    }

    public void testSelectQualifyingType() {
        String contents = "for (Map.Entry e : [:].entrySet()) { }";
        assertCodeSelect(asList(contents), "Map");
    }

    public void testSelectAliasedQualifyingType() {
        String contents = "import java.util.Map as Foo; for (Foo.Entry e : [:].entrySet()) { }";
        assertCodeSelect(asList(contents), "Foo", "Map");
    }

    // GRECLIPSE-1219
    public void testSelectAnnotationOnImport() {
        String contents = "@Deprecated import java.util.List; class Type { }";
        assertCodeSelect(asList(contents), "Deprecated");
    }

    public void testSelectAnnotationOnMethod() {
        String contents = "class Type { @Deprecated void method() {} }";
        assertCodeSelect(asList(contents), "Deprecated");
    }

    public void testSelectAnnotationOnMethod2() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return; // CompileDynamic was added in 2.0
        String contents = "import groovy.transform.*; class Type { @CompileStatic void method() {} }";
        assertCodeSelect(asList(contents), "CompileStatic");
    }

    public void testSelectAnnotationOnMethod3() {
        if (GroovyUtils.GROOVY_LEVEL < 21) return; // CompileDynamic was added in 2.1
        String contents = "import groovy.transform.*; class Type { @CompileDynamic void method() {} }";
        assertCodeSelect(asList(contents), "CompileDynamic");
    }

    public void testSelectAnnotationOnMethod4() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return; // TypeChecked was added in 2.0
        String contents = "import groovy.transform.*; class Type { @TypeChecked(TypeCheckingMode.SKIP) void method() {} }";
        assertCodeSelect(asList(contents), "TypeChecked");
        assertCodeSelect(asList(contents), "TypeCheckingMode");
    }

    public void testSelectAnnotationAsValue() {
        String support = "@interface Tag { String value() }\n@interface Wrapper { Tag[] value() }";
        String contents = "@Wrapper(@Tag('1')) def x";
        assertCodeSelect(asList(support, contents), "Wrapper");
        assertCodeSelect(asList(support, contents), "Tag");
    }

    public void testSelectAnnotationAsDefaultValue() {
        String contents = "@interface Suppressor {\n  SuppressWarnings value() default @SuppressWarnings('nls')\n}";
        assertCodeSelect(asList(contents), "SuppressWarnings");
    }

    public void testSelectLazyFieldType() {
        String contents = "class Type { @Lazy Date x }";
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "Lazy");
    }

    public void testSelectLineEndingsSupport() {
        // fails if visitClass -> getSource doesn't account for line endings
        String contents = "@Deprecated\r\n\r\nclass Type { Date x }";
        assertCodeSelect(asList(contents), "Date");
    }

    public void testSelectPackageOnFullyQualifiedName1() {
        String contents = "java.util.Date x";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectPackageOnFullyQualifiedName1a() {
        String contents = "java.util.regex.Pattern x";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "Pattern");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "regex", "java.util.regex");
    }

    public void testSelectPackageOnFullyQualifiedName3() {
        String contents = "java.util.Date[][] x";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectPackageOnFullyQualifiedName3a() {
        String contents = "java.util.regex.Pattern[][] x";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "Pattern");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "regex", "java.util.regex");
    }

    public void testSelectPackageOnFullyQualifiedName4() {
        String contents = "java.util.List<java.util.Date> x";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "List");
        assertCodeSelect(contents, new SourceRange(contents.indexOf("java"), 1), "java");
        assertCodeSelect(contents, new SourceRange(contents.indexOf("util"), 1), "java.util");
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectPackageOnFullyQualifiedName4a() {
        String contents = "java.util.List<java.util.regex.Pattern> x";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "List");
        assertCodeSelect(contents, new SourceRange(contents.indexOf("java"), 1), "java");
        assertCodeSelect(contents, new SourceRange(contents.indexOf("util"), 1), "java.util");
        assertCodeSelect(asList(contents), "Pattern");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "regex", "java.util.regex");
    }

    public void testSelectPackageOnFullyQualifiedName5() {
        String contents = "def x = new java.util.Date()";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    public void testSelectPackageOnFullyQualifiedName5a() {
        String contents = "def z = new java.util.regex.Pattern()";
        assertCodeSelect(asList(contents), "z");
        assertCodeSelect(asList(contents), "Pattern");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "regex", "java.util.regex");
    }

    public void testSelectPackageOnFullyQualifiedName6() {
        String contents = "def x = java.util.Collections.emptyList()";
        assertCodeSelect(asList(contents), "x");
        assertCodeSelect(asList(contents), "Collections");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "util", "java.util");
    }

    // generics

    public void testSelectGenericType1() {
        String contents = "class Foo<T> {}";
        assertCodeSelect(asList(contents), "T");
    }

    public void testSelectGenericType2() {
        String contents = "class Foo<T extends Date> {}";
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "T");
    }

    public void testSelectGenericType3() {
        String contents = "class Foo<T extends Date & List> {}";
        assertCodeSelect(asList(contents), "List");
        assertCodeSelect(asList(contents), "Date");
        assertCodeSelect(asList(contents), "T");
    }

    public void testSelectGenericType4() {
        String contents = "class Foo<T extends Object & MetaClass> {}";
        assertCodeSelect(asList(contents), "MetaClass");
        assertCodeSelect(asList(contents), "Object");
        assertCodeSelect(asList(contents), "T");
    }

    public void testSelectMethodGenericType1() {
        String contents = "class Foo { def <T> T x() { null } }";
        assertCodeSelect(contents, new SourceRange(contents.indexOf("T"), 1), "T");
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf("T"), 1), "T");
    }

    public void testSelectMethodGenericType2() {
        String contents = "class Foo<T> { def T x() { null } }";
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf("T"), 1), "T");
    }

    // javadocs

    public void testSelectTypeInJavadocLink() {
        String contents = "/** {@link java.util.regex.Pattern} */ class X { }";
        assertCodeSelect(asList(contents), "Pattern");
    }

    public void testSelectTypeInJavadocLink2() {
        String contents = "import java.util.regex.Pattern; /** {@link Pattern} */ class X { }";
        assertCodeSelect(asList(contents), "Pattern");
    }
}
