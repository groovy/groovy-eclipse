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
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.eclipse.jdt.ui.PreferenceConstants.TYPEFILTER_ENABLED
import static org.junit.Assume.assumeTrue

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
        String contents = '@Generated @ class Foo { }'
        def proposals = getProposals(contents, '@')

        assertThat(proposals).includes('Deprecated')
    }

    @Test
    void testAnno0b() {
        String contents = '@ @Generated class Foo { }'
        def proposals = getProposals(contents, '@')

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
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            @Target(ElementType.TYPE)
            public @interface Anno {
              String one();
              String two();
            }
            ''', 'Anno', 'p'

        String contents = '''\
            import p.Anno
            @Anno()
            class Something {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, '@Anno(')

        assertThat(proposals).includes('one', 'two')
    }

    @Test
    void testAnnoAttr3() {
        addJavaSource '''\
            package p;
            import java.lang.annotation.*;
            @Target(ElementType.TYPE)
            public @interface Another {
              String one();
              String two();
            }
            ''', 'Anno', 'p'

        String contents = '''\
            import p.Another
            Another(one=null,)
            class Something {
            }
            '''.stripIndent()
        def proposals = getProposals(contents, ',')

        assertThat(proposals).excludes('one').includes('two')
    }

    //--------------------------------------------------------------------------

    // create an internal DSL similar to AssertJ
    private def assertThat(ICompletionProposal[] proposals) {
        Expando exp = new Expando()

        def check = { Integer count, String... values ->
            for (value in values)
                proposalExists(proposals, value, count, value.charAt(0).isUpperCase())
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
