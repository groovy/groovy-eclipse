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
package org.codehaus.groovy.eclipse.test.actions

import org.codehaus.groovy.eclipse.test.ui.GroovyEditorTestSuite
import org.eclipse.jdt.ui.PreferenceConstants
import org.junit.Before
import org.junit.Test

final class AddImportOnSelectionTests extends GroovyEditorTestSuite {

    @Before
    void setUp() {
        // filter some type suggestions to prevent the import select dialog during tests
        setJavaPreference(PreferenceConstants.TYPEFILTER_ENABLED, 'com.sun.*;org.omg.*')
        // ensure consistent ordering of imports regardless of the target platform's defaults
        setJavaPreference(PreferenceConstants.ORGIMPORTS_IMPORTORDER, '\\#;java;javax;groovy;groovyx;;')
    }

    private void addImportOnSelection(CharSequence sourceCode) {
        makeEditor(sourceCode.stripIndent().toString())
        editor.getAction('AddImport').run()
    }

    @Test
    void testAddImportOnScriptVarType1() {
        addImportOnSelection "P${CARET}attern p = ~/123/"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern p = ~/123/"
    }

    @Test
    void testAddImportOnScriptVarType1a() {
        addImportOnSelection "java.util.regex.P${CARET}attern p = ~/123/"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern p = ~/123/"
    }

    @Test
    void testAddImportOnScriptVarType2() {
        addImportOnSelection "P${CARET}attern[] p = [~/123/]"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern[] p = [~/123/]"
    }

    @Test
    void testAddImportOnScriptVarType2a() {
        addImportOnSelection "java.util.regex.P${CARET}attern[] p = [~/123/]"
        assertEditorContents "import java.util.regex.Pattern\n\nPattern[] p = [~/123/]"
    }

    @Test
    void testAddImportOnScriptVarValue1() {
        addImportOnSelection "def p = P${CARET}attern.compile('123')"
        assertEditorContents "import java.util.regex.Pattern\n\ndef p = Pattern.compile('123')"
    }

    @Test
    void testAddImportOnScriptVarValue1a() {
        addImportOnSelection "def p = java.util.regex.P${CARET}attern.compile('123')"
        assertEditorContents "import java.util.regex.Pattern\n\ndef p = Pattern.compile('123')"
    }

    @Test
    void testAddImportOnScriptVarValue2() {
        addImportOnSelection "def p = Pattern.c${CARET}ompile('123')"
        assertEditorContents "import static java.util.regex.Pattern.compile\n\ndef p = compile('123')"
    }

    @Test
    void testAddImportOnScriptVarValue2a() {
        addImportOnSelection "def p = java.util.regex.Pattern.c${CARET}ompile('123')"
        assertEditorContents "import static java.util.regex.Pattern.compile\n\ndef p = compile('123')"
    }

    @Test
    void testAddImportOnScriptVarValue3() {
        addImportOnSelection "def unit = Time${CARET}Unit.SECONDS"
        assertEditorContents "import java.util.concurrent.TimeUnit\n\ndef unit = TimeUnit.SECONDS"
    }

    @Test
    void testAddImportOnScriptVarValue3a() {
        addImportOnSelection "def unit = java.util.concurrent.Time${CARET}Unit.SECONDS"
        assertEditorContents "import java.util.concurrent.TimeUnit\n\ndef unit = TimeUnit.SECONDS"
    }

    @Test
    void testAddImportOnScriptVarValue4() {
        addImportOnSelection "def unit = TimeUnit.SEC${CARET}ONDS"
        assertEditorContents "import static java.util.concurrent.TimeUnit.SECONDS\n\ndef unit = SECONDS"
    }

    @Test
    void testAddImportOnScriptVarValue4a() {
        addImportOnSelection "def unit = java.util.concurrent.TimeUnit.SEC${CARET}ONDS"
        assertEditorContents "import static java.util.concurrent.TimeUnit.SECONDS\n\ndef unit = SECONDS"
    }

    // types

    @Test
    void testAddImportOnSuperType0() {
        addImportOnSelection "class B extends B${CARET}ufferedReader {}"
        assertEditorContents "class B extends BufferedReader {}"
    }

    @Test
    void testAddImportOnSuperType0a() {
        addImportOnSelection "class B extends java.io.B${CARET}ufferedReader {}"
        assertEditorContents "class B extends BufferedReader {}"
    }

    @Test
    void testAddImportOnSuperType1() {
        addImportOnSelection "class B extends Float${CARET}Buffer {}"
        assertEditorContents "import java.nio.FloatBuffer\n\nclass B extends FloatBuffer {}"
    }

    @Test
    void testAddImportOnSuperType1a() {
        addImportOnSelection "class B extends java.nio.Float${CARET}Buffer {}"
        assertEditorContents "import java.nio.FloatBuffer\n\nclass B extends FloatBuffer {}"
    }

    @Test
    void testAddImportOnSuperInterface1() {
        addImportOnSelection "class C implements java.util.concurrent.C${CARET}allable {}"
        assertEditorContents "import java.util.concurrent.Callable\n\nclass C implements Callable {}"
    }

    @Test
    void testAddImportOnSuperInterface1a() {
        addImportOnSelection "class C implements java.util.concurrent.C${CARET}allable {}"
        assertEditorContents "import java.util.concurrent.Callable\n\nclass C implements Callable {}"
    }

    @Test
    void testAddImportOnSuperInterfaceGenerics1() {
        addImportOnSelection "class C implements java.util.concurrent.Callable<P${CARET}attern> {}"
        assertEditorContents "import java.util.regex.Pattern\n\nclass C implements java.util.concurrent.Callable<Pattern> {}"
    }

    @Test
    void testAddImportOnSuperInterfaceGenerics1a() {
        addImportOnSelection "class C implements java.util.concurrent.Callable<java.util.regex.P${CARET}attern> {}"
        assertEditorContents "import java.util.regex.Pattern\n\nclass C implements java.util.concurrent.Callable<Pattern> {}"
    }

    @Test
    void testAddImportOnTypeCast1() {
        addImportOnSelection "def x = (Float${CARET}Buffer) [1,2,3]"
        assertEditorContents "import java.nio.FloatBuffer\n\ndef x = (FloatBuffer) [1,2,3]"
    }

    @Test
    void testAddImportOnTypeCast1a() {
        addImportOnSelection "def x = (java.nio.Float${CARET}Buffer) [1,2,3]"
        assertEditorContents "import java.nio.FloatBuffer\n\ndef x = (FloatBuffer) [1,2,3]"
    }

    @Test
    void testAddImportOnTypeCoercion1() {
        addImportOnSelection "def x = [1,2,3] as Float${CARET}Buffer"
        assertEditorContents "import java.nio.FloatBuffer\n\ndef x = [1,2,3] as FloatBuffer"
    }

    @Test
    void testAddImportOnTypeCoercion1a() {
        addImportOnSelection "def x = [1,2,3] as java.nio.Float${CARET}Buffer"
        assertEditorContents "import java.nio.FloatBuffer\n\ndef x = [1,2,3] as FloatBuffer"
    }

    @Test
    void testAddImportOnLocalClass0() {
        addImportOnSelection "package a.b.c class F${CARET}oo {}"
        assertEditorContents "package a.b.c class Foo {}"
    }

    @Test
    void testAddImportOnInnerClass0() {
        addImportOnSelection "package a.b.c class Foo { class B${CARET}ar {} }"
        assertEditorContents "package a.b.c class Foo { class Bar {} }"
    }

    @Test
    void testAddImportOnAnonymousInnerClass() {
        addImportOnSelection "class C { def meth() { def x = new Float${CARET}Buffer() {}; } }"
        assertEditorContents "import java.nio.FloatBuffer\n\nclass C { def meth() { def x = new FloatBuffer() {}; } }"
    }

    // inner/outer variations

    @Test
    void testAddImportOnInnerClass1() {
        addImportOnSelection "Map.E${CARET}ntry entry = null"
        assertEditorContents "import java.util.Map.Entry\n\nEntry entry = null"
    }

    @Test
    void testAddImportOnInnerClass1a() {
        addImportOnSelection "Map.Entry${CARET} entry = null"
        assertEditorContents "import java.util.Map.Entry\n\nEntry entry = null"
    }

    @Test
    void testAddImportOnInnerClass1b() {
        addImportOnSelection "Map.${CARET}Entry entry = null"
        assertEditorContents "import java.util.Map.Entry\n\nEntry entry = null"
    }

    @Test
    void testAddImportOnOuterClass1() {
        addImportOnSelection "M${CARET}ap.Entry entry = null"
        assertEditorContents "Map.Entry entry = null"
    }

    @Test
    void testAddImportOnOuterClass1a() {
        addImportOnSelection "Map${CARET}.Entry entry = null"
        assertEditorContents "Map.Entry entry = null"
    }

    @Test
    void testAddImportOnOuterClass1b() {
        addImportOnSelection "${CARET}Map.Entry entry = null"
        assertEditorContents "Map.Entry entry = null"
    }

    @Test
    void testAddImportOnQualifiedOuterClass1() {
        addImportOnSelection "java.util.concurrent.Thread${CARET}PoolExecutor.AbortPolicy policy = null"
        assertEditorContents "import java.util.concurrent.ThreadPoolExecutor\n\nThreadPoolExecutor.AbortPolicy policy = null"
    }

    @Test
    void testAddImportOnQualifiedOuterClass1a() {
        addImportOnSelection "java.util.concurrent.ThreadPoolExecutor${CARET}.AbortPolicy policy = null"
        assertEditorContents "import java.util.concurrent.ThreadPoolExecutor\n\nThreadPoolExecutor.AbortPolicy policy = null"
    }

    @Test
    void testAddImportOnQualifiedOuterClass1b() {
        addImportOnSelection "java.util.concurrent.${CARET}Thread${CARET}PoolExecutor.AbortPolicy policy = null"
        assertEditorContents "import java.util.concurrent.ThreadPoolExecutor\n\nThreadPoolExecutor.AbortPolicy policy = null"
    }

    @Test
    void testAddImportOnInnerClass2() {
        addGroovySource 'interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d'

        addImportOnSelection "E${CARET}.F.G.H"
        assertEditorContents "import a.b.c.d.E\n\nE.F.G.H"
    }

    @Test
    void testAddImportOnInnerClass2a() {
        addGroovySource 'interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d'

        addImportOnSelection "a.b.c.d.E${CARET}.F.G.H"
        assertEditorContents "import a.b.c.d.E\n\nE.F.G.H"
    }

    @Test
    void testAddImportOnInnerClass2b() {
        addGroovySource 'interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d'

        addImportOnSelection "a.b.c.d.E.F${CARET}.G.H"
        assertEditorContents "import a.b.c.d.E.F\n\nF.G.H"
    }

    @Test
    void testAddImportOnInnerClass2c() {
        addGroovySource 'interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d'

        addImportOnSelection "a.b.c.d.E.F.G${CARET}.H"
        assertEditorContents "import a.b.c.d.E.F.G\n\nG.H"
    }

    @Test
    void testAddImportOnInnerClass2d() {
        addGroovySource 'interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d'

        addImportOnSelection "a.b.c.d.E.F.G.H${CARET}"
        assertEditorContents "import static a.b.c.d.E.F.G.H\n\nH"
    }

    @Test
    void testAddImportOnPackageQualifier1() {
        addImportOnSelection "java.util.con${CARET}current.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    @Test
    void testAddImportOnPackageQualifier2() {
        addImportOnSelection "java.ut${CARET}il.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    @Test
    void testAddImportOnPackageQualifier3() {
        addImportOnSelection "ja${CARET}va.util.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    @Test
    void testAddImportOnPackageQualifier3a() {
        addImportOnSelection "java${CARET}.util.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    @Test
    void testAddImportOnPackageQualifier3b() {
        addImportOnSelection "${CARET}java.util.concurrent.Callable call = null"
        assertEditorContents "java.util.concurrent.Callable call = null"
    }

    @Test
    void testAddImportOnPackageQualifier4() {
        addGroovySource 'interface E { interface F { interface G { String H = "I" } } }', 'E', 'a.b.c.d'

        addImportOnSelection "a${CARET}.b.c.d.E.F.G.H"
        assertEditorContents "a.b.c.d.E.F.G.H"
    }

    // constructors/initializers

    @Test
    void testAddImportOnConstructorCall1() {
        addImportOnSelection "def d = new java.sql.Da${CARET}te(123L)"
        assertEditorContents "import java.sql.Date\n\ndef d = new Date(123L)"
    }

    @Test
    void testAddImportOnConstructorCall2() {
        addImportOnSelection "def l = new java.util.concurrent.Future${CARET}Task<String>()"
        assertEditorContents "import java.util.concurrent.FutureTask\n\ndef l = new FutureTask<String>()"
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testAddImportOnMethodCallGenerics() {
        addImportOnSelection """\
            def callables = Collections.<java.util.concurrent.C${CARET}allable>emptyList()
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            def callables = Collections.<Callable>emptyList()
            """.stripIndent()
    }

    @Test
    void testAddImportOnCtorCallGenerics() {
        addImportOnSelection """\
            def callables = new ArrayList<java.util.concurrent.C${CARET}allable>()
            """
        assertEditorContents """\
            import java.util.concurrent.Callable

            def callables = new ArrayList<Callable>()
            """.stripIndent()
    }

    @Test
    void testAddImportOnStaticAccessor() {
        addImportOnSelection """\
            for(property in System.getProp${CARET}erties()) {
            }
            """
        assertEditorContents """\
            import static java.lang.System.getProperties

            for(property in getProperties()) {
            }
            """.stripIndent()
    }

    @Test
    void testAddImportOnStaticProperty() {
        addImportOnSelection """\
            for(property in System.prop${CARET}erties) {
            }
            """
        assertEditorContents """\
            import static java.lang.System.getProperties

            for(property in properties) {
            }
            """.stripIndent()
    }

    // annotations

    @Test
    void testAddImportOnClassAnnotation() {
        addImportOnSelection "@groovy.transform.T${CARET}ypeChecked class C {}"
        assertEditorContents "import groovy.transform.TypeChecked\n\n@TypeChecked class C {}"
    }

    @Test
    void testAddImportOnImportAnnotation() {
        addImportOnSelection "@javax.annotation.G${CARET}enerated import java.lang.StringBuffer\n\nStringBuffer sb"
        assertEditorContents "@Generated import java.lang.StringBuffer\n\nimport javax.annotation.Generated\n\nStringBuffer sb"
    }

    @Test
    void testAddImportOnPackageAnnotation1() {
        addImportOnSelection "@javax.annotation.G${CARET}enerated package a.b.c\n"
        assertEditorContents "@Generated package a.b.c\n\nimport javax.annotation.Generated\n"
    }

    @Test
    void testAddImportOnPackageAnnotation2() {
        addGroovySource 'abstract class ScriptType extends Script { def m() {} }'

        addImportOnSelection "@groovy.transform.B${CARET}aseScript(ScriptType) package a.b.c\n"
        assertEditorContents "@BaseScript(ScriptType) package a.b.c\n\nimport groovy.transform.BaseScript\n"
    }

    @Test
    void testAddImportOnAnnotationAnnotation() {
        addImportOnSelection "@java.lang.annotation.D${CARET}ocumented @interface Tag {}"
        assertEditorContents "import java.lang.annotation.Documented\n\n@Documented @interface Tag {}"
    }

    @Test
    void testAddImportOnFieldAnnotation1() {
        addImportOnSelection """\
            @groovy.transform.F${CARET}ield Object o
            """
        assertEditorContents """\
            import groovy.transform.Field

            @Field Object o
            """.stripIndent()
    }

    @Test
    void testAddImportOnFieldAnnotation2() {
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testAddImportOnUnqualifiedAnnotation1() {
        addImportOnSelection """\
            @Documented${CARET}
            @interface A {
            }
            """
        assertEditorContents """\
            import java.lang.annotation.Documented

            @Documented
            @interface A {
            }
            """.stripIndent()
    }

    @Test
    void testAddImportOnUnqualifiedAnnotation2() {
        addImportOnSelection """\
            @Builder${CARET}
            class C {
            }
            """
        assertEditorContents """\
            import groovy.transform.builder.Builder

            @Builder
            class C {
            }
            """.stripIndent()
    }

    // closures

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testAddImportOnClosureStatement() {
        addImportOnSelection """\
            def cl = { P${CARET}attern.compile(it) }
            """
        assertEditorContents """\
            import java.util.regex.Pattern

            def cl = { Pattern.compile(it) }
            """.stripIndent()
    }

    @Test
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

    @Test
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

    @Test
    void testAddImportOnCountingLoopParams2() {
        addImportOnSelection """\
            for (java.nio.Float${CARET}Buffer b = buffer(); b.hasRemaining();) {
            }
            """
        assertEditorContents """\
            import java.nio.FloatBuffer

            for (FloatBuffer b = buffer(); b.hasRemaining();) {
            }
            """.stripIndent()
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testAddImportOnCatchBlockParams() {
        addImportOnSelection """\
            try {
              getClass().getResourceAsStream('...').read()
            } catch (java.nio.Buffer${CARET}OverflowException ex) {
            }
            """
        assertEditorContents """\
            import java.nio.BufferOverflowException

            try {
              getClass().getResourceAsStream('...').read()
            } catch (BufferOverflowException ex) {
            }
            """.stripIndent()
    }

    // miscellaneous

    @Test
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

    @Test
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

    @Test
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

    @Test
    void testAddImportInGString1() {
        addImportOnSelection """\
            String s = "units: \${Time${CARET}Unit.SECONDS.name()}"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = "units: \${TimeUnit.SECONDS.name()}"
            """.stripIndent()
    }

    @Test
    void testAddImportInGString1a() {
        addImportOnSelection """\
            String s = "units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = "units: \${TimeUnit.SECONDS.name()}"
            """.stripIndent()
    }

    @Test
    void testAddImportInMultilineGString1() {
        addImportOnSelection """\
            String s = \"\"\"units: \${Time${CARET}Unit.SECONDS.name()}\"\"\"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \"\"\"units: \${TimeUnit.SECONDS.name()}\"\"\"
            """.stripIndent()
    }

    @Test
    void testAddImportInMultilineGString1a() {
        addImportOnSelection """\
            String s = \"\"\"units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}\"\"\"
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \"\"\"units: \${TimeUnit.SECONDS.name()}\"\"\"
            """.stripIndent()
    }

    @Test
    void testAddImportInSlashyGString1() {
        addImportOnSelection """\
            String s = /units: \${Time${CARET}Unit.SECONDS.name()}/
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = /units: \${TimeUnit.SECONDS.name()}/
            """.stripIndent()
    }

    @Test
    void testAddImportInSlashyGString1a() {
        addImportOnSelection """\
            String s = /units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}/
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = /units: \${TimeUnit.SECONDS.name()}/
            """.stripIndent()
    }

    @Test
    void testAddImportInDollarSlashyGString1() {
        addImportOnSelection """\
            String s = \$/units: \${Time${CARET}Unit.SECONDS.name()}/\$
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \$/units: \${Time${CARET}Unit.SECONDS.name()}/\$
            """.stripIndent()
    }

    @Test
    void testAddImportInDollarSlashyGString1a() {
        addImportOnSelection """\
            String s = \$/units: \${java.util.concurrent.Time${CARET}Unit.SECONDS.name()}/\$
            """
        assertEditorContents """\
            import java.util.concurrent.TimeUnit

            String s = \$/units: \${Time${CARET}Unit.SECONDS.name()}/\$
            """.stripIndent()
    }

    @Test
    void testTryAddConflictingType() {
        addImportOnSelection "import a.b.c.Pattern\n\ndef pat = java.util.regex.Pat${CARET}tern.compile('123')"
        assertEditorContents "import a.b.c.Pattern\n\ndef pat = java.util.regex.Pattern.compile('123')"
        assertStatusLineText "Import would conflict with an other import declaration or visible type."
    }

    @Test
    void testTryAddUnresolvedType() {
        addImportOnSelection "def x = Unresolvable${CARET}ClassName.WHATEVER"
        assertEditorContents "def x = UnresolvableClassName.WHATEVER"
        assertStatusLineText "Type 'UnresolvableClassName' could not be found or is not visible."
    }
}
