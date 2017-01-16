/*
 * Copyright 2009-2017 the original author or authors.
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

import static org.junit.Assert.*

import org.codehaus.groovy.eclipse.refactoring.actions.AddImportOnSelectionAction
import org.codehaus.groovy.eclipse.test.ui.GroovyEditorTest
import org.eclipse.jdt.ui.PreferenceConstants

final class AddImportOnSelectionTests extends GroovyEditorTest {

    @Override
    protected void setUp() {
        super.setUp()
        // filter some type suggestions to prevent the import select dialog during tests
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'com.sun.*;org.omg.*')
        // ensure consistent ordering of imports regardless of the target platform's defaults
        setJavaPreference(PreferenceConstants.ORGIMPORTS_IMPORTORDER, '\\#;java;javax;groovy;groovyx;;')
    }

    private void addImportOnSelection(CharSequence sourceCode) {
        makeEditor(sourceCode.stripIndent().toString())
        new AddImportOnSelectionAction(editor).run()
    }

    void testAddImportOnScriptVarType1() {
        addImportOnSelection "P${CARET}attern p = ~/123/"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern p = ~/123/"
    }

    void testAddImportOnScriptVarType1a() {
        addImportOnSelection "java.util.regex.P${CARET}attern p = ~/123/"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern p = ~/123/"
    }

    void testAddImportOnScriptVarType2() {
        addImportOnSelection "P${CARET}attern[] p = [~/123/]"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern[] p = [~/123/]"
    }

    void testAddImportOnScriptVarType2a() {
        addImportOnSelection "java.util.regex.P${CARET}attern[] p = [~/123/]"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern[] p = [~/123/]"
    }

    void testAddImportOnScriptVarValue1() {
        addImportOnSelection "def p = P${CARET}attern.compile('123')"
        assertEditorContents "import java.util.regex.Pattern\n\ndef p = Pattern.compile('123')"
    }

    void testAddImportOnScriptVarValue1a() {
        addImportOnSelection "def p = java.util.regex.P${CARET}attern.compile('123')"
        assertEditorContents "import java.util.regex.Pattern\n\ndef p = Pattern.compile('123')"
    }

    void testAddImportOnScriptVarValue2() {
        addImportOnSelection "def p = Pattern.c${CARET}ompile('123')"
        assertEditorContents "import static java.util.regex.Pattern.compile\n\ndef p = compile('123')"
    }

    void testAddImportOnScriptVarValue2a() {
        addImportOnSelection "def p = java.util.regex.Pattern.c${CARET}ompile('123')"
        assertEditorContents "import static java.util.regex.Pattern.compile\n\ndef p = compile('123')"
    }

    void testAddImportOnScriptVarValue3() {
        addImportOnSelection "def unit = Time${CARET}Unit.SECONDS"
        assertEditorContents "import java.util.concurrent.TimeUnit\n\ndef unit = TimeUnit.SECONDS"
    }

    void testAddImportOnScriptVarValue3a() {
        addImportOnSelection "def unit = java.util.concurrent.Time${CARET}Unit.SECONDS"
        assertEditorContents "import java.util.concurrent.TimeUnit\n\ndef unit = TimeUnit.SECONDS"
    }

    void testAddImportOnScriptVarValue4() {
        addImportOnSelection "def unit = TimeUnit.SEC${CARET}ONDS"
        assertEditorContents "import static java.util.concurrent.TimeUnit.SECONDS\n\ndef unit = SECONDS"
    }

    void testAddImportOnScriptVarValue4a() {
        addImportOnSelection "def unit = java.util.concurrent.TimeUnit.SEC${CARET}ONDS"
        assertEditorContents "import static java.util.concurrent.TimeUnit.SECONDS\n\ndef unit = SECONDS"
    }

    // types

    void testAddImportOnSuperType1() {
        addImportOnSelection "class B extends B${CARET}ufferedReader {}"
        assertEditorContents "import java.io.BufferedReader\n\nclass B extends BufferedReader {}"
    }

    void testAddImportOnSuperType1a() {
        addImportOnSelection "class B extends java.io.B${CARET}ufferedReader {}"
        assertEditorContents "import java.io.BufferedReader\n\nclass B extends BufferedReader {}"
    }

    void testAddImportOnSuperInterface1() {
        addImportOnSelection "class C implements java.util.concurrent.C${CARET}allable {}"
        assertEditorContents "import java.util.concurrent.Callable\n\nclass C implements Callable {}"
    }

    void testAddImportOnSuperInterface1a() {
        addImportOnSelection "class C implements java.util.concurrent.C${CARET}allable {}"
        assertEditorContents "import java.util.concurrent.Callable\n\nclass C implements Callable {}"
    }

    void testAddImportOnSuperInterfaceGenerics1() {
        addImportOnSelection "class C implements java.util.concurrent.Callable<P${CARET}attern> {}"
        assertEditorContents "import java.util.regex.Pattern\n\nclass C implements java.util.concurrent.Callable<Pattern> {}"
    }

    void testAddImportOnSuperInterfaceGenerics1a() {
        addImportOnSelection "class C implements java.util.concurrent.Callable<java.util.regex.P${CARET}attern> {}"
        assertEditorContents "import java.util.regex.Pattern\n\nclass C implements java.util.concurrent.Callable<Pattern> {}"
    }

    void testAddImportOnTypeCast1() {
        addImportOnSelection "def x = (Collect${CARET}ion) [1,2,3]"
        assertEditorContents "import java.util.Collection\n\ndef x = (Collection) [1,2,3]"
    }

    void testAddImportOnTypeCast1a() {
        addImportOnSelection "def x = (java.util.Collect${CARET}ion) [1,2,3]"
        assertEditorContents "import java.util.Collection\n\ndef x = (Collection) [1,2,3]"
    }

    void testAddImportOnTypeCoercion1() {
        addImportOnSelection "def x = [1,2,3] as Hash${CARET}Set"
        assertEditorContents "import java.util.HashSet\n\ndef x = [1,2,3] as HashSet"
    }

    void testAddImportOnTypeCoercion1a() {
        addImportOnSelection "def x = [1,2,3] as java.util.Hash${CARET}Set"
        assertEditorContents "import java.util.HashSet\n\ndef x = [1,2,3] as HashSet"
    }

    void testAddImportOnLocalClass0() {
        addImportOnSelection "package a.b.c class F${CARET}oo {}"
        assertEditorContents "package a.b.c class Foo {}"
    }

    void testAddImportOnInnerClass0() {
        addImportOnSelection "package a.b.c class Foo { class B${CARET}ar {} }"
        assertEditorContents "package a.b.c class Foo { class Bar {} }"
    }

    void testAddImportOnAnonymousInnerClass() {
        addImportOnSelection "class C { def meth() { def x = new ArrayL${CARET}ist() {}; } }"
        assertEditorContents "import java.util.ArrayList\n\nclass C { def meth() { def x = new ArrayList() {}; } }"
    }

    // inner/outer variations

    void testAddImportOnInnerClass1() {
        addImportOnSelection "Map.E${CARET}ntry entry = null"
        assertEditorContents "import java.util.Map.Entry\n\nEntry entry = null"
    }

    void testAddImportOnInnerClass1a() {
        addImportOnSelection "Map.Entry${CARET} entry = null"
        assertEditorContents "import java.util.Map.Entry\n\nEntry entry = null"
    }

    void testAddImportOnInnerClass1b() {
        addImportOnSelection "Map.${CARET}Entry entry = null"
        assertEditorContents "import java.util.Map.Entry\n\nEntry entry = null"
    }

    void testAddImportOnOuterClass1() {
        addImportOnSelection "M${CARET}ap.Entry entry = null"
        assertEditorContents "import java.util.Map\n\nMap.Entry entry = null"
    }

    void testAddImportOnOuterClass1a() {
        addImportOnSelection "Map${CARET}.Entry entry = null"
        assertEditorContents "import java.util.Map\n\nMap.Entry entry = null"
    }

    void testAddImportOnOuterClass1b() {
        addImportOnSelection "${CARET}Map.Entry entry = null"
        assertEditorContents "import java.util.Map\n\nMap.Entry entry = null"
    }

    void testAddImportOnQualifiedOuterClass1() {
        addImportOnSelection "java.util.M${CARET}ap.Entry entry = null"
        assertEditorContents "import java.util.Map\n\nMap.Entry entry = null"
    }

    void testAddImportOnQualifiedOuterClass1a() {
        addImportOnSelection "java.util.Map${CARET}.Entry entry = null"
        assertEditorContents "import java.util.Map\n\nMap.Entry entry = null"
    }

    void testAddImportOnQualifiedOuterClass1b() {
        addImportOnSelection "java.util.${CARET}Map.Entry entry = null"
        assertEditorContents "import java.util.Map\n\nMap.Entry entry = null"
    }

    void testAddImportOnInnerClass2() {
        testProject.createGroovyTypeAndPackage 'a.b.c.d', 'E.groovy', 'interface E { interface F { interface G { String H = "I" } } }'

        addImportOnSelection "E${CARET}.F.G.H"
        assertEditorContents "import a.b.c.d.E\n\nE.F.G.H"
    }

    void testAddImportOnInnerClass2a() {
        testProject.createGroovyTypeAndPackage 'a.b.c.d', 'E.groovy', 'interface E { interface F { interface G { String H = "I" } } }'

        addImportOnSelection "a.b.c.d.E${CARET}.F.G.H"
        assertEditorContents "import a.b.c.d.E\n\nE.F.G.H"
    }

    void testAddImportOnInnerClass2b() {
        testProject.createGroovyTypeAndPackage 'a.b.c.d', 'E.groovy', 'interface E { interface F { interface G { String H = "I" } } }'

        addImportOnSelection "a.b.c.d.E.F${CARET}.G.H"
        assertEditorContents "import a.b.c.d.E.F\n\nF.G.H"
    }

    void testAddImportOnInnerClass2c() {
        testProject.createGroovyTypeAndPackage 'a.b.c.d', 'E.groovy', 'interface E { interface F { interface G { String H = "I" } } }'

        addImportOnSelection "a.b.c.d.E.F.G${CARET}.H"
        assertEditorContents "import a.b.c.d.E.F.G\n\nG.H"
    }

    void testAddImportOnInnerClass2d() {
        testProject.createGroovyTypeAndPackage 'a.b.c.d', 'E.groovy', 'interface E { interface F { interface G { String H = "I" } } }'

        addImportOnSelection "a.b.c.d.E.F.G.H${CARET}"
        assertEditorContents "import static a.b.c.d.E.F.G.H\n\nH"
    }

    void testAddImportOnPackageQualifier1() {
        addImportOnSelection "java.util.con${CARET}current.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    void testAddImportOnPackageQualifier2() {
        addImportOnSelection "java.ut${CARET}il.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    void testAddImportOnPackageQualifier3() {
        addImportOnSelection "ja${CARET}va.util.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    void testAddImportOnPackageQualifier3a() {
        addImportOnSelection "java${CARET}.util.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    void testAddImportOnPackageQualifier3b() {
        addImportOnSelection "${CARET}java.util.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    void testAddImportOnPackageQualifier4() {
        testProject.createGroovyTypeAndPackage 'a.b.c.d', 'E.groovy', 'interface E { interface F { interface G { String H = "I" } } }'

        addImportOnSelection "a${CARET}.b.c.d.E.F.G.H"
        assertEditorContents "a.b.c.d.E.F.G.H"
    }

    // constructors/initializers

    void testAddImportOnConstructorParam() {
        addImportOnSelection """\
            class C {
              C(java.util.regex. /*goes away*/ P${CARET}attern p) {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              C(Pattern p) {}
            }
            """.stripIndent()
    }

    void testAddImportOnConstructorParamGenerics() {
        addImportOnSelection """\
            class C {
              C(List<java.util.regex.P${CARET}attern> pats) {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              C(List<Pattern> pats) {}
            }
            """.stripIndent()
    }

    void testAddImportOnConstructorBody() {
        addImportOnSelection """\
            class C {
              C() {
                java.util.regex.P${CARET}attern p = ~/123/
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              C() {
                Pattern p = ~/123/
              }
            }
            """.stripIndent()
    }

    void testAddImportOnTypeInClassInit() {
        addImportOnSelection """\
            class C {
              {
                java.util.regex.P${CARET}attern p = ~/123/
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              {
                Pattern p = ~/123/
              }
            }
            """.stripIndent()
    }

    void testAddImportOnTypeInStaticInit() {
        addImportOnSelection """\
            class C {
              static {
                java.util.regex.P${CARET}attern p = ~/123/
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              static {
                Pattern p = ~/123/
              }
            }
            """.stripIndent()
    }

    // fields

    void testAddImportOnFieldType() {
        addImportOnSelection """\
            class C {
              java.util.regex.P${CARET}attern p = ~/123/
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              Pattern p = ~/123/
            }
            """.stripIndent()
    }

    void testAddImportOnLazyFieldType() {
        addImportOnSelection """\
            class C {
              @Lazy java.util.regex.P${CARET}attern p = ~/123/
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              @Lazy Pattern p = ~/123/
            }
            """.stripIndent()
    }

    void testAddImportOnFieldTypeGenerics() {
        addImportOnSelection """\
            class C {
              List<java.util.regex.P${CARET}attern> pats
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              List<Pattern> pats
            }
            """.stripIndent()
    }

    void testAddImportOnFieldInit() {
        addImportOnSelection """\
            class C {
              def p = java.util.regex.P${CARET}attern.compile('123')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              def p = Pattern.compile('123')
            }
            """.stripIndent()
    }

    void testAddImportOnFieldInit2() {
        addImportOnSelection """\
            class C {
              def p = java.util.regex.Pattern.c${CARET}ompile('123')
            }
            """
        assertEditorContents """\
            import static java.util.regex.Pattern.compile

            class C {
              def p = compile('123')
            }
            """.stripIndent()
    }

    // methods

    void testAddImportOnMethodReturnType() {
        addImportOnSelection """\
            class C {
              java.util.regex.P${CARET}attern meth() {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              Pattern meth() {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodReturnTypeGenerics() {
        addImportOnSelection """\
            import java.util.concurrent.Callable
            class C {
              Callable<java.util.regex.P${CARET}attern> meth() {}
            }
            """
        assertEditorContents """\
            import java.util.concurrent.Callable
            import java.util.regex.Pattern
            class C {
              Callable<Pattern> meth() {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodGenerics0() {
        addImportOnSelection """\
            class C {
              def <T${CARET} extends Iterable> T meth(T parm) {}
            }
            """
        assertEditorContents """\
            class C {
              def <T extends Iterable> T meth(T parm) {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodGenerics1() {
        addImportOnSelection """\
            class C {
              def <T extends java.lang.Iter${CARET}able> T meth(T parm) {}
            }
            """
        assertEditorContents """\
            class C {
              def <T extends Iterable> T meth(T parm) {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodGenerics2() {
        addImportOnSelection """\
            class C {
              def <T extends java.util.concurrent.C${CARET}allable> T meth(T parm) {}
            }
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            class C {
              def <T extends Callable> T meth(T parm) {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodTypeParam() {
        addImportOnSelection """\
            class C {
              def <T> T${CARET} meth(T parm) {}
            }
            """
        assertEditorContents """\
            class C {
              def <T> T meth(T parm) {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodParams() {
        addImportOnSelection """\
            class C {
              def meth(java.util.regex.P${CARET}attern p) {
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              def meth(Pattern p) {
              }
            }
            """.stripIndent()
    }

    void testAddImportOnMethodParamGenerics() {
        addImportOnSelection """\
            class C {
              def meth(List<java.util.regex.P${CARET}attern> pats) {}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              def meth(List<Pattern> pats) {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodCallGenerics() {
        addImportOnSelection """\
            def callables = Collections.<java.util.concurrent.C${CARET}allable>emptyList()
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            def callables = Collections.<Callable>emptyList()
            """.stripIndent()
    }

    void testAddImportOnCtorCallGenerics() {
        addImportOnSelection """\
            def callables = new ArrayList<java.util.concurrent.C${CARET}allable>()
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            def callables = new ArrayList<Callable>()
            """.stripIndent()
    }

    // annotations

    void testAddImportOnClassAnnotation() {
        addImportOnSelection "@groovy.transform.T${CARET}ypeChecked class C {}"
        assertEditorContents "import groovy.transform.TypeChecked\n\n@TypeChecked class C {}"
    }

    void testAddImportOnImportAnnotation() {
        addImportOnSelection "@javax.annotation.G${CARET}enerated import java.lang.StringBuffer\n\nStringBuffer sb"
        assertEditorContents "@Generated import java.lang.StringBuffer\n\nimport javax.annotation.Generated\n\nStringBuffer sb"
    }

    void testAddImportOnPackageAnnotation() {
        addImportOnSelection "@javax.annotation.G${CARET}enerated package a.b.c\ndef x"
        assertEditorContents "@Generated package a.b.c\n\nimport javax.annotation.Generated\n\ndef x"
    }

    void testAddImportOnAnnotationAnnotation() {
        addImportOnSelection "@java.lang.annotation.D${CARET}ocumented @interface Tag {}"
        assertEditorContents "import java.lang.annotation.Documented\n\n@Documented @interface Tag {}"
    }

    void testAddImportOnFieldAnnotation() {
        addImportOnSelection """\
            class C {
              @javax.annotation.G${CARET}enerated Object o
            }
            """
        assertEditorContents """\
            import javax.annotation.Generated

            class C {
              @Generated Object o
            }
            """.stripIndent()
    }

    void testAddImportOnMethodAnnotation() {
        addImportOnSelection """\
            class C {
              @javax.annotation.G${CARET}enerated
              def meth() {}
            }
            """
        assertEditorContents """\
            import javax.annotation.Generated

            class C {
              @Generated
              def meth() {}
            }
            """.stripIndent()
    }

    void testAddImportOnMethodParamAnnotation() {
        addImportOnSelection """\
            class C {
              def meth(@javax.annotation.G${CARET}enerated Object o) {}
            }
            """
        assertEditorContents """\
            import javax.annotation.Generated

            class C {
              def meth(@Generated Object o) {}
            }
            """.stripIndent()
    }

    void testAddImportOnLocalVariableAnnotation() {
        addImportOnSelection """\
            class C {
              def meth() {
                @java.lang.Suppress${CARET}Warnings('unused')
                int i = 100
              }
            }
            """
        assertEditorContents """\
            class C {
              def meth() {
                @SuppressWarnings('unused')
                int i = 100
              }
            }
            """.stripIndent()
    }

    // closures

    void testAddImportOnClosureParams1() {
        addImportOnSelection """\
            def cl = { P${CARET}attern p ->
              p.matcher('')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { Pattern p ->
              p.matcher('')
            }
            """.stripIndent()
    }

    void testAddImportOnClosureParams1a() {
        addImportOnSelection """\
            def cl = { java.util.regex.P${CARET}attern p ->
              p.matcher('')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { Pattern p ->
              p.matcher('')
            }
            """.stripIndent()
    }

    void testAddImportOnClosureParams2() {
        addImportOnSelection """\
            def cl = { P${CARET}attern[] p ->
              p*.matcher('')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { Pattern[] p ->
              p*.matcher('')
            }
            """.stripIndent()
    }

    void testAddImportOnClosureParams2a() {
        addImportOnSelection """\
            def cl = { java.util.regex.P${CARET}attern[] p ->
              p*.matcher('')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { Pattern[] p ->
              p*.matcher('')
            }
            """.stripIndent()
    }

    void testAddImportOnClosureParamGenerics1() {
        addImportOnSelection """\
            def cl = { List<P${CARET}attern> p ->
              p.matcher('')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { List<Pattern> p ->
              p.matcher('')
            }
            """.stripIndent()
    }

    void testAddImportOnClosureParamGenerics1a() {
        addImportOnSelection """\
            def cl = { List<java.util.regex.P${CARET}attern> p ->
              p.matcher('')
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { List<Pattern> p ->
              p.matcher('')
            }
            """.stripIndent()
    }

    void testAddImportOnClosureStatement() {
        addImportOnSelection """\
            def cl = { P${CARET}attern.compile(it) }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { Pattern.compile(it) }
            """.stripIndent()
    }

    void testAddImportOnMethodPointer() {
        addImportOnSelection """\
            def cl = P${CARET}attern.&compile
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = Pattern.&compile
            """.stripIndent()
    }

    // non-method parameters

    void testAddImportOnCountingLoopParams1() {
        addImportOnSelection """\
            for (java.math.BigI${CARET}nteger i = 0; i < 10; i += 1) {
            }
            """
        assertEditorContents """\
            import java.math.BigInteger

            for (BigInteger i = 0; i < 10; i += 1) {
            }
            """.stripIndent()
    }

    void testAddImportOnCountingLoopParams2() {
        addImportOnSelection """\
            for (java.util.I${CARET}terator i = [].iterator(); i.hasNext();) {
            }
            """
        assertEditorContents """\
            import java.util.Iterator

            for (Iterator i = [].iterator(); i.hasNext();) {
            }
            """.stripIndent()
    }

    void testAddImportOnForEachLoopParams1() {
        addImportOnSelection """\
            for (P${CARET}attern p : []) {
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            for (Pattern p : []) {
            }
            """.stripIndent()
    }

    void testAddImportOnForEachLoopParams1a() {
        addImportOnSelection """\
            for (java.util.concurrent.C${CARET}allable c : []) {
            }
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            for (Callable c : []) {
            }
            """.stripIndent()
    }

    void testAddImportOnForInLoopParams1() {
        addImportOnSelection """\
            for (P${CARET}attern p in []) {
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            for (Pattern p in []) {
            }
            """.stripIndent()
    }

    void testAddImportOnForInLoopParams1a() {
        addImportOnSelection """\
            for (java.util.concurrent.C${CARET}allable c in []) {
            }
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            for (Callable c in []) {
            }
            """.stripIndent()
    }

    void testAddImportOnCatchBlockParams() {
        addImportOnSelection """\
            try {
              getClass().getResourceAsStream('...').read()
            } catch (java.io.IO${CARET}Exception ex) {
            }
            """
        assertEditorContents """\
            import java.io.IOException

            try {
              getClass().getResourceAsStream('...').read()
            } catch (IOException ex) {
            }
            """.stripIndent()
    }

    // miscellaneous

    void testAddImportInInnerClassBody() {
        addImportOnSelection """\
            class Foo {
              class Bar {
                def pat
                Bar() {
                  pat = Pat${CARET}tern.compile('123')
                }
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class Foo {
              class Bar {
                def pat
                Bar() {
                  pat = Pattern.compile('123')
                }
              }
            }
            """.stripIndent()
    }

    void testAddImportInStaticInnerClassBody() {
        addImportOnSelection """\
            class Foo {
              static class Bar {
                def pat
                Bar() {
                  pat = Pat${CARET}tern.compile('123')
                }
              }
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class Foo {
              static class Bar {
                def pat
                Bar() {
                  pat = Pattern.compile('123')
                }
              }
            }
            """.stripIndent()
    }

    void testAddImportInAnonymousInnerClassBody() {
        addImportOnSelection """\
            class C {
              def x = new HashMap() {{
                put('pat', Pat${CARET}tern.compile('123'))
              }}
            }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            class C {
              def x = new HashMap() {{
                put('pat', Pattern.compile('123'))
              }}
            }
            """.stripIndent()
    }

    void testAddImportInGString1() {
        addImportOnSelection """\
            String s = "units: \${Time${CARET}Unit.SECONDS.name()}"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = "units: \${TimeUnit.SECONDS.name()}"
            """.stripIndent()
    }

    void testAddImportInGString1a() {
        addImportOnSelection """\
            String s = "units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = "units: \${TimeUnit.SECONDS.name()}"
            """.stripIndent()
    }

    void testAddImportInMultilineGString1() {
        addImportOnSelection """\
            String s = \"\"\"units: \${Time${CARET}Unit.SECONDS.name()}\"\"\"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \"\"\"units: \${TimeUnit.SECONDS.name()}\"\"\"
            """.stripIndent()
    }

    void testAddImportInMultilineGString1a() {
        addImportOnSelection """\
            String s = \"\"\"units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}\"\"\"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \"\"\"units: \${TimeUnit.SECONDS.name()}\"\"\"
            """.stripIndent()
    }

    void testAddImportInSlashyGString1() {
        addImportOnSelection """\
            String s = /units: \${Time${CARET}Unit.SECONDS.name()}/
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = /units: \${TimeUnit.SECONDS.name()}/
            """.stripIndent()
    }

    void testAddImportInSlashyGString1a() {
        addImportOnSelection """\
            String s = /units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}/
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = /units: \${TimeUnit.SECONDS.name()}/
            """.stripIndent()
    }

    void testAddImportInDollarSlashyGString1() {
        addImportOnSelection """\
            String s = \$/units: \${Time${CARET}Unit.SECONDS.name()}/\$
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \$/units: \${Time${CARET}Unit.SECONDS.name()}/\$
            """.stripIndent()
    }

    void testAddImportInDollarSlashyGString1a() {
        addImportOnSelection """\
            String s = \$/units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}/\$
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \$/units: \${Time${CARET}Unit.SECONDS.name()}/\$
            """.stripIndent()
    }

    void testTryAddConflictingType() {
        addImportOnSelection "import a.b.c.Pattern\n\ndef pat = java.util.regex.Pat${CARET}tern.compile('123')"
        assertEditorContents "import a.b.c.Pattern\n\ndef pat = java.util.regex.Pattern.compile('123')"
        assertStatusLineText "Import would conflict with an other import declaration or visible type."
    }

    void testTryAddUnresolvedType() {
        addImportOnSelection "def x = Unresolvable${CARET}ClassName.WHATEVER"
        assertEditorContents "def x = UnresolvableClassName.WHATEVER"
        assertStatusLineText "Type 'UnresolvableClassName' could not be found or is not visible."
    }
}
