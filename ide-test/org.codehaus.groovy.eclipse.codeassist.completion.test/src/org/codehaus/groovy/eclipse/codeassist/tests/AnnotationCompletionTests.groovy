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
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.eclipse.jdt.ui.PreferenceConstants.TYPEFILTER_ENABLED
import static org.junit.Assume.assumeTrue

import groovy.transform.NotYetImplemented

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

    @Test
    void testAnno1() {
        String contents = '@Dep class Foo { }'
        def proposals = getProposals(contents, '@Dep')

        assertThat(proposals).includes('Deprecated').hasSize(1, 'Only @Deprecated should have been proposed\n')
    }

    @Test
    void testAnno2() {
        assumeTrue(isAtLeastGroovy(21))
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
        assumeTrue(isAtLeastGroovy(21))
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
            @SuppressWarnings()
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '@SuppressWarnings(')

        assertThat(proposals).includes('value').excludes('equals', 'public') // no Object methods or Java keywords
    }

    @Test
    void testAnnoAttr1() {
        String contents = '''\
            @SuppressWarnings(v)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '@SuppressWarnings(v')

        assertThat(proposals).includes('value').excludes('equals', 'public') // no Object methods or Java keywords
    }

    @Test
    void testAnnoAttr2() {
        String contents = '''\
            @SuppressWarnings(value=)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '@SuppressWarnings(value=')

        assertThat(proposals).excludes('value')
    }

    @Test
    void testAnnoAttr3() {
        String contents = '''\
            @SuppressWarnings(value=v)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '@SuppressWarnings(value=v')

        assertThat(proposals).excludes('value')
    }

    @Test
    void testAnnoAttr4() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            @Target(ElementType.TYPE)
            public @interface A {
              String one();
              String two();
            }
            ''', 'A', 'p'

        String contents = '''\
            import p.A
            @A()
            class Something {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '@A(')

        assertThat(proposals).includes('one', 'two')
    }

    @Test
    void testAnnoAttr5() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            @Target(ElementType.TYPE)
            public @interface B {
              String one();
              String two();
            }
            ''', 'B', 'p'

        String contents = '''\
            import p.B
            @B(one=null,)
            class Something {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, ',')

        assertThat(proposals).excludes('one').includes('two')
    }

    @Test
    void testAnnoAttrConst() {
        String contents = '''\
            @SuppressWarnings(value=V)
            class C {
              public static final String VALUE = ''
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '=V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrTypes() {
        String contents = '''\
            @SuppressWarnings(value=Obj)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '=Obj')

        assertThat(proposals).includes('Object - java.lang', 'ObjectRange - groovy.lang')
    }

    @Test
    void testAnnoAttrPacks() {
        String contents = '''\
            @SuppressWarnings(value=jav)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '=jav')

        assertThat(proposals).includes('java.lang', 'java.util')
    }

    @Test
    void testAnnoAttrConst2() {
        String contents = '''\
            @SuppressWarnings(V)
            class C {
              public static final String VALUE = ''
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst3() {
        String contents = '''\
            @SuppressWarnings(V)
            class C {
              public static String VARIES = ''
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).excludes('VARIES')
    }

    @Test
    void testAnnoAttrConst4() {
        String contents = '''\
            @SuppressWarnings(V)
            class C {
              public static final CharSequence VALUE = ''
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).excludes('VALUE')
    }

    @Test
    void testAnnoAttrConst5() {
        addJavaSource 'public interface I { String VALUE = ""; }', 'I', 'p'

        String contents = '''\
            import static p.I.VALUE
            @SuppressWarnings(V)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst6() {
        addJavaSource('public interface J { String VALUE = ""; }', 'J', 'p')

        String contents = '''\
            import static p.J.*
            @SuppressWarnings(V)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(V')

        assertThat(proposals).includes('VALUE')
    }

    @Test
    void testAnnoAttrConst7() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            @Target(ElementType.TYPE)
            public @interface K {
              int one();
              int two();
            }
            ''', 'K', 'p'

        String contents = '''\
            import p.K
            @K(one=null, two = )
            class C {
              public static final int TWO = 2
            }
            '''.stripIndent()
        def proposals = getProposals(contents, ' = ')

        assertThat(proposals).includes('TWO')
    }

    @Test
    void testAnnoAttrEnumConst1() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface U {
              TimeUnit value();
            }
            ''', 'U', 'p'

        String contents = '''\
            @p.U()
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(')

        assertThat(proposals).includes('SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit')
    }

    @Test @NotYetImplemented
    void testAnnoAttrEnumConst2() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface V {
              TimeUnit value();
            }
            ''', 'V', 'p'

        String contents = '''\
            import java.util.concurrent.TimeUnit
            @p.V(TimeUnit.)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '.')

        assertThat(proposals).includes('SECONDS', 'MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS').excludes('TimeUnit')
    }

    @Test
    void testAnnoAttrEnumConst3() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface W {
              TimeUnit value();
            }
            ''', 'W', 'p'

        String contents = '''\
            @p.W(SE)
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '(SE')

        assertThat(proposals).includes('SECONDS').excludes('MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit')
    }

    @Test @NotYetImplemented
    void testAnnoAttrEnumConst4() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface X {
              TimeUnit[] value();
            }
            ''', 'X', 'p'

        String contents = '''\
            import static java.util.concurrent.TimeUnit.SECONDS
            @p.X([SECONDS, ])
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, ', ')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit').excludes('SECONDS')
    }

    @Test @NotYetImplemented
    void testAnnoAttrEnumConst4a() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface Y {
              TimeUnit[] value();
            }
            ''', 'Y', 'p'

        String contents = '''\
            import static java.util.concurrent.TimeUnit.SECONDS
            @p.Y(value=[SECONDS, ])
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, ', ')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS', 'NANOSECONDS', 'TimeUnit').excludes('SECONDS')
    }

    @Test
    void testAnnoAttrEnumConst4b() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface Z {
              TimeUnit[] value();
            }
            ''', 'Z', 'p'

        String contents = '''\
            import static java.util.concurrent.TimeUnit.SECONDS
            @p.Z(value = [SECONDS, M])
            class C {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, 'M')

        assertThat(proposals).includes('MILLISECONDS', 'MICROSECONDS').excludes('SECONDS', 'NANOSECONDS', 'TimeUnit')
    }

    @Test
    void testAnnoAttrEnumConst5() {
        addJavaSource '''\
            package time;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface Unit {
              TimeUnit value();
            }
            ''', 'Unit', 'time'

        String contents = '''\
            @time.Unit()
            class C {
            }
            '''.stripIndent()
        String expected = '''\
            import static java.util.concurrent.TimeUnit.SECONDS

            @time.Unit(SECONDS)
            class C {
            }
            '''.stripIndent()
        checkProposalApplication(contents, expected, contents.indexOf('(') + 1, 'SECONDS', false)
    }

    @Test
    void testAnnoAttrEnumConst6() {
        addJavaSource '''\
            package time_;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface Unit {
              TimeUnit value();
            }
            ''', 'Unit', 'time_'

        String contents = '''\
            import static java.util.concurrent.TimeUnit.SECONDS

            @time_.Unit()
            class C {
            }
            '''.stripIndent()
        String expected = '''\
            import static java.util.concurrent.TimeUnit.SECONDS

            @time_.Unit(SECONDS)
            class C {
            }
            '''.stripIndent()
        checkProposalApplication(contents, expected, contents.indexOf('(') + 1, 'SECONDS', false)
    }

    @Test
    void testAnnoAttrEnumConst7() {
        addJavaSource '''\
            package time__;
            import java.lang.annotation.*;
            import java.util.concurrent.*;
            @Target(ElementType.TYPE)
            public @interface Unit {
              TimeUnit value();
            }
            ''', 'Unit', 'time__'

        String contents = '''\
            import static java.util.concurrent.TimeUnit.*

            @time__.Unit()
            class C {
            }
            '''.stripIndent()
        String expected = '''\
            import static java.util.concurrent.TimeUnit.*

            @time__.Unit(SECONDS)
            class C {
            }
            '''.stripIndent()
        checkProposalApplication(contents, expected, contents.indexOf('(') + 1, 'SECONDS', false)
    }

    @Test @NotYetImplemented
    void testQualifierForTypeAnnoScope() {
        String contents = '''\
            @SuppressWarnings(V)
            class C {
              public static final String VALUE = 'nls'
            }
            '''.stripIndent()
        String expected = '''\
            @SuppressWarnings(C.VALUE)
            class C {
              public static final String VALUE = 'nls'
            }
            '''.stripIndent()
        checkProposalApplication(contents, expected, contents.indexOf('(V') + 2, 'VALUE', false)
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
