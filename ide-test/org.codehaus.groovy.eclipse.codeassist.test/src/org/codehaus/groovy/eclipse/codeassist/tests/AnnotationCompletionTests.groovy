/*
 * Copyright 2009-2025 the original author or authors.
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

import static org.eclipse.jdt.ui.PreferenceConstants.CODEASSIST_ADDIMPORT
import static org.eclipse.jdt.ui.PreferenceConstants.TYPEFILTER_ENABLED
import static org.osgi.framework.Version.parseVersion

import groovy.test.NotYetImplemented

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Assert
import org.junit.Before
import org.junit.Test

final class AnnotationCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        // filter some legacy packages
        setJavaPreference(TYPEFILTER_ENABLED, 'com.sun.*;org.omg.*')
    }

    @Test
    void testAnno0() {
        String contents = '@ class Foo { }'
        def proposals = getProposals(contents, '@')

        assertThat(proposals).includes('Deprecated')
    }

    @Test
    void testAnno0a() {
        String contents = '@Deprecated class Foo { }'
        def proposals = getProposals(contents, '@')

        assertThat(proposals).includes('Deprecated')
    }

    @Test
    void testAnno0b() {
        String contents = 'import javax.annotation.*\n @ @Generated("") class Foo { }'
        def proposals = getProposals(contents, '@')

        assertThat(proposals).includes('Deprecated')
    }

    @Test
    void testAnno0c() {
        String contents = 'import javax.annotation.*\n @Generated("") @ class Foo { }'
        def proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '@'))

        assertThat(proposals).includes('Deprecated')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/994
    void testAnno0d() {
        String contents = '@ class Foo { }'
        def proposals = getProposals(contents, '@')

        assertThat(proposals).includes('AutoExternalize', 'CompileDynamic')
    }

    @Test
    void testAnno1() {
        String contents = '@Dep class Foo { }'
        def proposals = getProposals(contents, '@Dep')

        assertThat(proposals).includes('Deprecated').hasSize(1, 'Only @Deprecated should have been proposed\n')
    }

    @Test
    void testAnno2() {
        String contents = '@Compile class Foo { }'
        def proposals = getProposals(contents, '@Compile')

        assertThat(proposals).includes('CompileStatic', 'CompileDynamic')
        int total = 2
        try {
            assertThat(proposals).includes('Compiled') // java.lang.invoke.LambdaForm.Compiled is in 1.8+
            total += 1
        } catch (Throwable notJava8) {
        }
        assertThat(proposals).hasSize(total, 'Only @CompileStatic and @CompileDynamic should have been proposed\n')
    }

    @Test
    void testAnno2a() {
        String contents = '@ComDyn class Foo { }'
        def proposals = getProposals(contents, '@ComDyn') // check camel case matching
        assertThat(proposals).includes('CompileDynamic').hasSize(1, 'Only @CompileDynamic should have been proposed\n')
    }

    @Test
    void testAnno3() {
        String contents = '@Single class Foo { }'
        def proposals = getProposals(contents, '@Single')

        assertThat(proposals).includes('Singleton').hasSize(1, 'Only @Singleton should have been proposed\n')
    }

    @Test
    void testAnno4() {
        // not exactly right since @Singleton is only allowed on classes, but good enough for testing
        String contents = 'class Foo { @Single def foo }'
        def proposals = getProposals(contents, '@Single')

        assertThat(proposals).includes('Singleton').hasSize(1, 'Only @Singleton should have been proposed\n')
    }

    @Test
    void testAnno5() {
        // not exactly right since @Singleton is only allowed on classes, but good enough for testing
        String contents = 'class Foo { @Single def foo() {} }'
        def proposals = getProposals(contents, '@Single')

        assertThat(proposals).includes('Singleton').hasSize(1, 'Only @Singleton should have been proposed\n')
    }

    @Test
    void testAnno6() {
        // not exactly right since @Singleton is only allowed on classes, but good enough for testing
        String contents = '@Single import java.util.List\nclass Foo { }'
        def proposals = getProposals(contents, '@Single')

        assertThat(proposals).includes('Singleton').hasSize(1, 'Only @Singleton should have been proposed\n')
    }

    @Test
    void testAnnoAttr0() {
        String contents = '''\
            |@SuppressWarnings()
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '@SuppressWarnings(')

        assertThat(proposals).includes('value').excludes('equals', 'public') // no Object methods or Java keywords
    }

    @Test
    void testAnnoAttr1() {
        String contents = '''\
            |@SuppressWarnings(v)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '@SuppressWarnings(v')

        assertThat(proposals).includes('value').excludes('equals', 'public') // no Object methods or Java keywords
    }

    @Test
    void testAnnoAttr2() {
        String contents = '''\
            |@SuppressWarnings(value=)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '@SuppressWarnings(value=')

        assertThat(proposals).excludes('value')
    }

    @Test
    void testAnnoAttr3() {
        String contents = '''\
            |@SuppressWarnings(value=v)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '@SuppressWarnings(value=v')

        assertThat(proposals).excludes('value')
    }

    @Test
    void testAnnoAttr4() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  String one();
            |  String two();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |@A()
            |class Something {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '@A(')

        assertThat(proposals).includes('one', 'two')
    }

    @Test
    void testAnnoAttr5() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  String one();
            |  String two();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |@A(one=null,)
            |class Something {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ',')

        assertThat(proposals).excludes('one').includes('two')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/761
    void testAnnoAttr6() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.METHOD)
            |public @interface A {
            |  String one();
            |  String two();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |class Something {
            |  @A(one=null,)
            |  void meth() {}
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ',')

        assertThat(proposals).excludes('one').includes('two')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/761
    void testAnnoAttr7() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  boolean one();
            |  String two();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |@A(one=false)
            |class Something {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, 'false')

        assertThat(proposals).excludes('one', 'two')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/761
    void testAnnoAttr8() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  boolean one();
            |  String two();
            |  int three();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |@A(one=false, t)
            |class Something {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', t')

        assertThat(proposals).excludes('one').includes('two', 'three')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/761
    void testAnnoAttr9() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.METHOD)
            |public @interface A {
            |  boolean one();
            |  String two();
            |  int three();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |class Something {
            |  @A(one=false, t)
            |  void meth() {}
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', t')

        assertThat(proposals).excludes('one').includes('two', 'three')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/761
    void testAnnoAttr10() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.METHOD)
            |public @interface A {
            |  boolean one();
            |  String two();
            |  int three();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |class Something {
            |  @A(one=false, w)
            |  void meth() {}
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', w')

        assertThat(proposals).excludes('one', 'three').includes('two')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/671
    void testAnnoAttr11() {
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.ENABLED)

        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  boolean one();
            |  String two();
            |  @Deprecated
            |  int three();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |@A(one=false, t)
            |class Something {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', t')

        assertThat(proposals).excludes('one', 'three').includes('two')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/769
    void testAnnoAttr12() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  boolean one();
            |  String two();
            |}
            |'''.stripMargin(), 'A', 'p'

        addJavaSource '''\
            |package p;
            |public class Three {
            |}
            |'''.stripMargin(), 'Three', 'p'

        String contents = '''\
            |import p.*
            |@A(one=false, t)
            |class Something {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', t')

        assertThat(proposals).excludes('one', 'Three').includes('two')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/769
    void testAnnoAttr13() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.METHOD)
            |public @interface A {
            |  boolean one();
            |}
            |'''.stripMargin(), 'A', 'p'

        String contents = '''\
            |import p.A
            |class Something {
            |  @A(one=false)
            |  def meth() {}
            |  boolean someFalseCheck() {}
            |  private boolean someFalseFlag
            |  public static final boolean SOME_FALSE_CONST = false
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, 'false')

        assertThat(proposals).excludes('someFalseCheck', 'someFalseFlag').includes('SOME_FALSE_CONST')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1355
    void testAnnoAttr14() {
        String contents = '''\
            |import groovy.transform.Canonical
            |@Canonical()
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '@Canonical(')

        assertThat(proposals).includes('useCanEqual', 'includeSuper') // from EqualsAndHashCode and ToString
    }

    @Test
    void testAnnoAttrPacks() {
        String contents = '''\
            |@SuppressWarnings(value=jav)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '=jav')

        assertThat(proposals).includes('java.lang', 'java.util')
    }

    @Test
    void testAnnoAttrTypes() {
        String contents = '''\
            |@SuppressWarnings(value=Obj)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '=Obj')

        assertThat(proposals).includes('Object - java.lang', 'ObjectRange - groovy.lang')
    }

    @Test
    void testAnnoAttrConst() {
        String contents = '''\
            |@SuppressWarnings(value=V)
            |class C {
            |  public static final String VALUE = ''
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '=V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst2() {
        String contents = '''\
            |@SuppressWarnings(V)
            |class C {
            |  public static final String VALUE = ''
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst3() {
        String contents = '''\
            |@SuppressWarnings(V)
            |class C {
            |  public static String VARIES = ''
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).excludes('VARIES')
    }

    @Test
    void testAnnoAttrConst4() {
        String contents = '''\
            |@SuppressWarnings(V)
            |class C {
            |  public static final CharSequence VALUE = ''
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).excludes('VALUE')
    }

    @Test
    void testAnnoAttrConst5() {
        addJavaSource '''\
            |public interface I {
            |  String VALUE = "";
            |}
            |'''.stripMargin(), 'I', 'p'

        String contents = '''\
            |import static p.I.VALUE
            |@SuppressWarnings(V)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst6() {
        addJavaSource('''\
            |public interface J {
            |  String VALUE = "";
            |}
            |'''.stripMargin(), 'J', 'p')

        String contents = '''\
            |import static p.J.*
            |@SuppressWarnings(V)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst7() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface K {
            |  int one();
            |  int two();
            |}
            |'''.stripMargin(), 'K', 'p'

        String contents = '''\
            |import p.K
            |@K(one=null, two = )
            |class C {
            |  public static final int TWO = 2
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ' = ')

        assertThat(proposals).includes('TWO')
    }

    @Test
    void testAnnoAttrConst8() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.METHOD)
            |public @interface L {
            |  int one();
            |  int two();
            |}
            |'''.stripMargin(), 'L', 'p'

        String contents = '''\
            |import p.L
            |class C {
            |  @L(one=null, two = )
            |  String somethingSpecial() {}
            |  public static final int TWO = 2
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ' = ')

        assertThat(proposals).includes('TWO')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/671
    void testAnnoAttrConst9() {
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.ENABLED)

        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface M {
            |  int one();
            |  int two();
            |}
            |'''.stripMargin(), 'M', 'p'

        String contents = '''\
            |import p.M
            |@M(two=T)
            |class C {
            |  @Deprecated
            |  public static final int TWO = 2
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '=T')

        assertThat(proposals).excludes('TWO')
    }

    @Test
    void testAnnoAttrEnumConst1() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface U {
            |  TimeUnit value();
            |}
            |'''.stripMargin(), 'U', 'p'

        String contents = '''\
            |@p.U()
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(')

        assertThat(proposals).includes('SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit')
    }

    @Test @NotYetImplemented
    void testAnnoAttrEnumConst2() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface V {
            |  TimeUnit value();
            |}
            |'''.stripMargin(), 'V', 'p'

        String contents = '''\
            |import java.util.concurrent.TimeUnit
            |@p.V(TimeUnit.)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '.')

        assertThat(proposals).includes('SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS').excludes('TimeUnit')
    }

    @Test
    void testAnnoAttrEnumConst3() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface W {
            |  TimeUnit value();
            |}
            |'''.stripMargin(), 'W', 'p'

        String contents = '''\
            @p.W(MI)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(MI')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS').excludes('SECONDS', 'NANOSECONDS', 'TimeUnit')
    }

    @Test @NotYetImplemented
    void testAnnoAttrEnumConst4() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface X {
            |  TimeUnit[] value();
            |}
            |'''.stripMargin(), 'X', 'p'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.SECONDS
            |@p.X([SECONDS, ])
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', ')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit').excludes('SECONDS')
    }

    @Test @NotYetImplemented
    void testAnnoAttrEnumConst4a() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface Y {
            |  TimeUnit[] value();
            |}
            |'''.stripMargin(), 'Y', 'p'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.SECONDS
            |@p.Y(value=[SECONDS, ])
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, ', ')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit').excludes('SECONDS')
    }

    @Test
    void testAnnoAttrEnumConst4b() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface Z {
            |  TimeUnit[] value();
            |}
            |'''.stripMargin(), 'Z', 'p'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.SECONDS
            |@p.Z(value = [SECONDS, M])
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, 'M')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS').excludes('SECONDS', 'NANOSECONDS', 'TimeUnit')
    }

    @Test
    void testAnnoAttrEnumConst5() {
        addJavaSource '''\
            |package time;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface Unit {
            |  TimeUnit value();
            |}
            |'''.stripMargin(), 'Unit', 'time'

        String contents = '''\
            |@time.Unit()
            |class C {
            |}
            |'''.stripMargin()
        String expected = '''\
            |import static java.util.concurrent.TimeUnit.SECONDS
            |
            |@time.Unit(SECONDS)
            |class C {
            |}
            |'''.stripMargin()
        checkProposalApplication(contents, expected, contents.indexOf('(') + 1, 'SECONDS', false)
    }

    @Test
    void testAnnoAttrEnumConst6() {
        addJavaSource '''\
            |package time_;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface Unit {
            |  TimeUnit value();
            |}
            |'''.stripMargin(), 'Unit', 'time_'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.SECONDS
            |
            |@time_.Unit()
            |class C {
            |}
            |'''.stripMargin()
        String expected = '''\
            |import static java.util.concurrent.TimeUnit.SECONDS
            |
            |@time_.Unit(SECONDS)
            |class C {
            |}
            |'''.stripMargin()
        checkProposalApplication(contents, expected, contents.indexOf('(') + 1, 'SECONDS', false)
    }

    @Test
    void testAnnoAttrEnumConst7() {
        addJavaSource '''\
            |package time__;
            |import java.lang.annotation.*;
            |import java.util.concurrent.*;
            |@Target(ElementType.TYPE)
            |public @interface Unit {
            |  TimeUnit value();
            |}
            |'''.stripMargin(), 'Unit', 'time__'

        String contents = '''\
            |import static java.util.concurrent.TimeUnit.*
            |
            |@time__.Unit()
            |class C {
            |}
            |'''.stripMargin()
        String expected = '''\
            |import static java.util.concurrent.TimeUnit.*
            |
            |@time__.Unit(SECONDS)
            |class C {
            |}
            |'''.stripMargin()
        checkProposalApplication(contents, expected, contents.indexOf('(') + 1, 'SECONDS', false)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/671
    void testAnnoAttrEnumConst8() {
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.ENABLED)

        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  E value();
            |}
            |'''.stripMargin(), 'A', 'p'

        addJavaSource '''\
            |package p;
            |@Deprecated
            |public enum E {
            |  ABC, DEF;
            |}
            |'''.stripMargin(), 'E', 'p'

        String contents = '''\
            |import p.A
            |import p.E
            |@A()
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, '(')
        if (JavaCore.plugin.bundle.version < parseVersion('3.44')) {
            assertThat(proposals).excludes('E', 'ABC', 'DEF').includes('value')
        } else {
            assertThat(proposals).excludes('E').includes('ABC', 'DEF', 'value')
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/671
    void testAnnoAttrEnumConst9() {
        setJavaPreference(AssistOptions.OPTION_PerformDeprecationCheck, AssistOptions.ENABLED)

        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface A {
            |  E value();
            |}
            |'''.stripMargin(), 'A', 'p'

        addJavaSource '''\
            |package p;
            |public enum E {
            |  @Deprecated ABC, DEF;
            |}
            |'''.stripMargin(), 'E', 'p'

        String contents = '''\
            |import p.A
            |import p.E
            |@A(value=E.)
            |class C {
            |}
            |'''.stripMargin()
        def proposals = getProposals(contents, 'E.')

        assertThat(proposals).excludes('ABC').includes('DEF')
    }

    @Test
    void testConfigScriptCompletion() {
        addPlainText('''\
            |withConfig(configuration) {
            |  imports {
            |    star 'java.util.regex'
            |  }
            |}
            |'''.stripMargin(), '../config.groovy')
        setJavaPreference(CompilerOptions.OPTIONG_GroovyCompilerConfigScript, 'config.groovy')
        // addition of imports through compiler configuration should not affect proposal application

        String contents = '''\
            |@TypeCh
            |class C {
            |}
            |'''.stripMargin()
        String expected = '''\
            |import groovy.transform.TypeChecked
            |
            |@TypeChecked
            |class C {
            |}
            |'''.stripMargin()
        checkProposalApplication(contents, expected, getIndexOf(contents, 'Ch'), 'TypeChecked', true)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/365
    void testQualifierForTypeAnnoScope1() {
        String contents = '''\
            |@SuppressWarnings(V)
            |class C {
            |  public static final String VALUE = 'nls'
            |}
            |'''.stripMargin()
        String expected = '''\
            |@SuppressWarnings(C.VALUE)
            |class C {
            |  public static final String VALUE = 'nls'
            |}
            |'''.stripMargin()
        setJavaPreference(CODEASSIST_ADDIMPORT, false)
        checkProposalApplication(contents, expected, getIndexOf(contents, '(V'), 'VALUE', false)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/478
    void testQualifierForTypeAnnoScope2() {
        addJavaSource '''\
            |package a;
            |import java.lang.annotation.*;
            |@Target(ElementType.TYPE)
            |public @interface B {
            |  Class<?> value();
            |}
            |'''.stripMargin(), 'B', 'a'

        String contents = '''\
            |@a.B(Nes)
            |class C {
            |  static class Nested {}
            |}
            |'''.stripMargin()
        String expected = '''\
            |@a.B(C.Nested)
            |class C {
            |  static class Nested {}
            |}
            |'''.stripMargin()
        setJavaPreference(CODEASSIST_ADDIMPORT, false)
        checkProposalApplication(contents, expected, getIndexOf(contents, '(Nes'), 'Nested - C', true)
    }

    //--------------------------------------------------------------------------

    // create an internal DSL similar to AssertJ
    private def assertThat(ICompletionProposal[] proposals) {
        Expando exp = new Expando()

        def check = { Integer count, String... values ->
            for (value in values)
                proposalExists(proposals, value, count, value.charAt(0).isUpperCase() || value.contains('.'))
            return exp
        }
        exp.excludes = check.curry(0)
        exp.includes = check.curry(1)

        exp.hasSize = { Integer expected, CharSequence message = null ->
            Assert.assertEquals(message, expected, proposals.length)
            return exp
        }

        return exp
    }

    // provides a slightly simpler interface for initiating content assist
    private ICompletionProposal[] getProposals(CharSequence contents, String target) {
        createProposalsAtOffset(contents, getIndexOf(contents, target))
    }
}
