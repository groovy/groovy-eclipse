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
package org.codehaus.groovy.eclipse.test.ui

import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.*
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.junit.Assert.assertEquals
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.editor.highlighting.GatherSemanticReferences
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

final class SemanticHighlightingTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        GroovyPlugin.default.preferenceStore.setValue(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING, true)

        addJavaSource('public @Deprecated class Java {\n  @Deprecated public static final String CONST = "";\n}', 'Java', 'other')
    }

    @Test
    void testFields() {
        String contents = '''\
            class X {
              String one
              public Object two
              private Integer three
              private @Lazy Collection four
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('one'), 'one'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('two'), 'two'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('three'), 'three'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('four'), 'four'.length(), FIELD))
    }

    @Test
    void testScriptFields() {
        String contents = '''\
            @groovy.transform.Field List list = [1, 2]
            list << 'three'
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('list'), 4, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('list'), 4, FIELD));
    }

    @Test
    void testScriptFields2() {
        String contents = '''\
              import groovy.transform.Field
              @Field String one
              @Field Integer two = 1234
              @Field private Object three // four
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('one'), 'one'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('two'), 'two'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('1234'), '1234'.length(), NUMBER),
            new HighlightedTypedPosition(contents.indexOf('three'), 'three'.length(), FIELD))
    }

    @Test
    void testStaticFields() {
        String contents = '''\
            class X {
              static FOO
              def x() { FOO; }
              static { FOO = [] }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('FOO'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.indexOf('FOO;'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('FOO'), 3, STATIC_FIELD))
    }

    @Test
    void testStaticFinals() {
        String contents = '''\
            Math.PI
            ObjectStreamConstants.STREAM_MAGIC
            '''.stripIndent()

        // Math is a class, ObjectStreamConstants is an interface

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('PI'), 2, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('STREAM_MAGIC'), 'STREAM_MAGIC'.length(), STATIC_VALUE))
    }

    @Test
    void testStaticFinals2() {
        String contents = '''\
            import static java.lang.Math.PI
            def pi = PI
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('pi'), 2, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('PI'), 2, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('PI'), 2, STATIC_VALUE))
    }

    @Test
    void testMethods() {
        String contents = '''\
            import groovy.transform.PackageScope
            class X {
              def a() {}
              public def b() {}
              protected def c() {}
              @PackageScope def d() {}
              private synchronized def e() {}
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('b('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('c('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('d('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('e('), 1, METHOD))
    }

    @Test
    void testClassMethods() {
        String contents = '''\
            Number.getClass() // Object method on Class instance
            String.getSimpleName()

            class X {
              static {
                getCanonicalName()
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('getClass'), 'getClass'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('getSimpleName'), 'getSimpleName'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('getCanonicalName'), 'getCanonicalName'.length(), METHOD_CALL))
    }

    @Test
    void testClassMethods2() {
        String contents = '''\
            import java.util.regex.Pattern
            Pattern.class.matcher("")
            Pattern.matcher('')
            def pat = Pattern
            pat.matcher("")
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('pat'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('pat'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('matcher("'), 'matcher'.length(), UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('matcher(\''), 'matcher'.length(), UNKNOWN),
            new HighlightedTypedPosition(contents.lastIndexOf('matcher('), 'matcher'.length(), UNKNOWN))
    }

    @Test
    void testStaticMethods() {
        String contents = '''\
            class X {
              static FOO() { FOO() }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('FOO'), 'FOO'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('FOO'), 'FOO'.length(), STATIC_CALL))
    }

    @Test
    void testStaticMethods2() {
        String contents = '''\
            class X {
              static {
                def y = Collections.emptyMap()
                def z = java.util.Collections.emptySet()
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('y ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('z ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('emptyMap'), 'emptyMap'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('emptySet'), 'emptySet'.length(), STATIC_CALL))
    }

    @Test
    void testStaticMethods3() {
        String contents = '''\
            import static java.util.Collections.singletonList
            class X {
              def meth() {
                return singletonList('x')
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('singletonList'), 'singletonList'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('singletonList'), 'singletonList'.length(), STATIC_CALL))
    }

    @Test
    void testStaticMethods4() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            import static java.lang.Integer.valueOf
            @groovy.transform.CompileStatic
            class C {
              String number
              int getN() {
                valueOf(number) // needs sloc; see MethodCallExpression.setSourcePosition
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('getN'), 'getN'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('number'), 'number'.length(), FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('number'), 'number'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('valueOf'), 'valueOf'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('valueOf'), 'valueOf'.length(), STATIC_CALL))
    }

    @Test // GRECLIPSE-1138
    void testMultipleStaticMethods() {
        String contents = '''\
            f(1,2)

            static f(List a, List b = null) {
            }
            static f(int a, int b) {
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('f(1'), 1, STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('f(List'), 1, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('a,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('b '), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('f(int'), 1, STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('a,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('b)'), 1, PARAMETER))
    }

    @Test
    void testMethodsAsProperties() {
        String contents = '''\
            import java.lang.management.ManagementFactory
            // compact form:
            ManagementFactory.runtimeMXBean.inputArguments
            // expanded form:
            ManagementFactory.getRuntimeMXBean().getInputArguments()
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('runtimeMXBean'), 'runtimeMXBean'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('inputArguments'), 'inputArguments'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('getRuntimeMXBean'), 'getRuntimeMXBean'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('getInputArguments'), 'getInputArguments'.length(), METHOD_CALL))
    }

    @Test
    void testMethodsAsProperties2() {
        addGroovySource '''\
            class Foo {
              private static final String value = ''
              static String getValue() {
                return value
              }
            }
            '''.stripIndent()

        String contents = 'Foo.value'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('value'), 5, STATIC_CALL))
    }

    @Test
    void testMethodsAsProperties3() {
        addGroovySource '''\
            interface Bar { def getOne() }
            interface Baz extends Bar { def getTwo() }
            '''.stripIndent()

        String contents = '''\
            class Foo {
              def meth(Baz b) {
                b.one + b.two
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('b)'),   1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('b.o'),  1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('b.t'),  1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('one'),  3, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('two'),  3, METHOD_CALL))
    }

    @Test
    void testDefaultGroovyMethods() {
        String contents = '["one", "two"].grep().first()'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('grep'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('first'), 5, GROOVY_CALL))
    }

    @Test
    void testDefaultGroovyMethods2() {
        String contents = '(["one", "two"] as String[]).first()'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('first'), 5, GROOVY_CALL))
    }

    @Test
    void testDefaultGroovyMethods3() {
        String contents = '''\
            class Foo {
              static {
                getAt("staticProperty")
              }
              Foo() {
                getAt("instanceProperty")
              }
              def m() {
                println "message of importance"
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo()'), 3, CTOR),
            new HighlightedTypedPosition(contents.indexOf('m() {'), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('getAt'), 5, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('getAt'), 5, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('println'), 7, GROOVY_CALL))
    }

    @Test
    void testUseCategoryMethods() {
        String contents = '''\
            use(groovy.time.TimeCategory) {
              new Date().getDaylightSavingsOffset()
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('use'), 3, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('getDaylightSavingsOffset'), 'getDaylightSavingsOffset'.length(), GROOVY_CALL))
    }

    @Test
    void testNotCategoryMethod() {
        String contents = 'def x = "equals"' // equals is a DGM and had been improperly identified by CategoryTypeLookup
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE))
    }

    @Test
    void testVariadicMehtods() {
        String contents = '''\
            class X {
              def one() {
                int i;
              }
              def two(String... strings) {
                int j;
              }
              def three(x, ... y) {
                int k;
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('one'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('three'), 5, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('y'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE))
    }

    @Test
    void testParamsAndLocals() {
        String contents = '''\
            class X {
              def loop(int n) {
                def f = { int x -> x * n }
                for (int i = 0; i < n; i += 1) {
                  f(i) // ignore result
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('loop'), 'loop'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('n)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('f ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('x ->'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('x *'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('n }'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('i <'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n;'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i +'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('f('), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('f('), 1, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('i)'), 1, VARIABLE))
    }

    @Test
    void testNamedParams() {
        String contents = '''\
            class Person { String firstName, lastName }
            def p = new Person(firstName: 'John', lastName: 'Doe')
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('firstName'), 'firstName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('lastName'), 'lastName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('p'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Person'), 'Person'.length(), CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), FIELD))
    }

    @Test
    void testNamedParams2() {
        String contents = '''\
            class Person {
              String firstName, lastName

              Person() {}
              Person(Map m) {} // trumps default+setters
            }
            def p = new Person(firstName: 'John', lastName: 'Doe')
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('firstName'), 'firstName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('lastName'), 'lastName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('Person', contents.indexOf('lastName')), 'Person'.length(), CTOR),
            new HighlightedTypedPosition(contents.indexOf('Person', contents.indexOf('() {}')), 'Person'.length(), CTOR),
            new HighlightedTypedPosition(contents.indexOf('m)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('p ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Person'), 'Person'.length(), CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), MAP_KEY))
    }

    @Test
    void testNamedParams3() {
        String contents = '''\
            def map = Collections.singletonMap(key: 'k', value: 'v')
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('singletonMap'), 'singletonMap'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.indexOf('value'), 'value'.length(), MAP_KEY))
    }

    @Test
    void testMultiAssign() {
        String contents = '''\
            def (a, b) = ['one', 'two']
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE))
    }

    @Test
    void testChainAssign() {
        String contents = '''\
            class C {
              String fld
              C() {
                String var
                fld = var = ''
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('C'),   1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('fld'),     3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('var'),     3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('fld'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('var'), 3, VARIABLE))
    }

    @Test
    void testChainAssign2() {
        String contents = '''\
            class C {
              String one, two
              C() {
                one = two = ''
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('C'),   1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('one'),     3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('two'),     3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('two'), 3, FIELD))
    }

    @Test
    void testChainAssign3() {
        String contents = '''\
            class C {
              String one, two
            }
            def c = new C()
            c.one = c.two = ''
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('one'),     3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('two'),     3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('c ='),     1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('C'),   1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('c.'),      1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('c.'),  1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('two'), 3, FIELD))
    }

    @Test
    void testChainAssign4() {
        // property notation that maps to setter; this kind of chain assignment does work
        String contents = '''\
            class B {
              private String z
              void setZero(String zero) { z = zero }
            }

            class C {
              String x
              B b
              C() {
                x = b.zero = 'X'
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('z'),        1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('setZero'),  7, METHOD),
            new HighlightedTypedPosition(contents.indexOf('zero'),     4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('z ='),      1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('zero }'),   4, PARAMETER),

            new HighlightedTypedPosition(contents.indexOf('x'),        1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('b'),        1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('C'),    1, CTOR),
            new HighlightedTypedPosition(contents.lastIndexOf('x'),    1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('b'),    1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('zero'), 4, METHOD_CALL))
    }

    @Test
    void testChainAssign5() {
        assumeTrue(isAtLeastGroovy(20))

        // property notation that maps to setter; this kind of chain assignment does work
        // static compilation produces list of expressions (temp,call) for "_ = b.zero ="
        String contents = '''\
            class B {
              private String z
              void setZero(String zero) { z = zero }
            }

            @groovy.transform.CompileStatic
            class C {
              String x
              B b
              C() {
                x = b.zero = 'X'
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('z'),        1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('setZero'),  7, METHOD),
            new HighlightedTypedPosition(contents.indexOf('zero'),     4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('z ='),      1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('zero }'),   4, PARAMETER),

            new HighlightedTypedPosition(contents.indexOf('x'),        1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('b'),        1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('C'),    1, CTOR),
            new HighlightedTypedPosition(contents.lastIndexOf('x'),    1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('b'),    1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('zero'), 4, METHOD_CALL))
    }

    @Test
    void testCatchParam() {
        // don't want PARAMETER
        String contents = '''\
            class X {
              def except() {
                try {
                } catch (Exception specific) {
                  specific.printStackTrace()
                } catch (unspecified) {
                  unspecified.printStackTrace()
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('except'), 'except'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('specific'), 'specific'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('specific'), 'specific'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('unspecified'), 'unspecified'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('unspecified'), 'unspecified'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('printStackTrace'), 'printStackTrace'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('printStackTrace'), 'printStackTrace'.length(), METHOD_CALL))
    }

    @Test
    void testCatchParamWithInstanceOf() {
        // don't want PARAMETER
        String contents = '''\
            class X {
              def m() {
                try {
                } catch (Exception ex) {
                  if (ex instanceof RuntimeException) {
                    ex // instanceof flow typing caused catch param check to break down
                  } else {
                    ex
                  }
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('m()'),    1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('ex)'),    2, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('ex in'),  2, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('ex //'),  2, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('ex'), 2, VARIABLE))
    }

    @Test
    void testForEachParam() {
        // don't want PARAMETER
        String contents = '''\
            class X {
              def loop() {
                for (Object x : []) {
                    x + ""
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('loop'), 'loop'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, VARIABLE))
    }

    @Test
    void testForEachInParam() {
        // don't want PARAMETER
        String contents = '''\
            class X {
              def loop() {
                for (x in []) {
                    x + ""
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('loop'), 'loop'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, VARIABLE))
    }

    @Test
    void testForEachInParamWithInstanceOf() {
        // don't want PARAMETER
        String contents = '''\
            class X {
              def loop() {
                for (x in []) {
                  if (x instanceof String) {
                    x // instanceof flow typing caused for-each param check to break down
                  } else {
                    x
                  }
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('loop'),  4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x in '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('x ins'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('x // '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, VARIABLE))
    }

    @Test
    void testImplicitParam() {
        String contents = '''\
            def f = { it * "string" }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('f'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('it'), 2, GROOVY_CALL))
    }

    @Test
    void testThisAndSuper() {
        // the keywords super and this are identified/highlighted by GroovyTagScanner
        String contents = '''\
            def f = {
              super
              this
            }
            '''.stripIndent()

        assertHighlighting(contents,
            //new HighlightedTypedPosition(contents.indexOf('this'), 4, ???),
            //new HighlightedTypedPosition(contents.indexOf('super'), 5, ???),
            new HighlightedTypedPosition(contents.indexOf('f ='), 1, VARIABLE))
    }

    @Test
    void testGStringThisAndSuper() {
        // except when appearing within a GString
        String contents = '''\
            "this: $this, super: $super"
            "this: ${this}, super: ${super}"
            "${this.hashCode()}, ${super.hashCode()}"
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$this') + 1, 4, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('$super') + 1, 5, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('${this}') + 2, 4, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('${super}') + 2, 5, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('${this.') + 2, 4, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('${super.') + 2, 5, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('hashCode'), 'hashCode'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('hashCode'), 'hashCode'.length(), METHOD_CALL))
    }

    @Test
    void testCtorCalls() {
        // the keywords super and this are identified/highlighted by GroovyTagScanner
        String contents = '''\
            class X {
              X() {
                super();
              }
              X(String s) {
                this();
              }
            }
            def x = new X();
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            //new HighlightedTypedPosition(contents.indexOf('super'), 5, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('X(S'), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('s)'), 1, PARAMETER),
            //new HighlightedTypedPosition(contents.indexOf('this'), 4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CTOR_CALL))
    }

    @Test
    void testInnerClassCtorCalls() {
        String contents = '''\
            class X {
              class Y {
                String foo
                Integer bar
              }
              def baz() {
                def y = new Y()
                def why = new Y(foo: '1', bar: 2) // non-static inner class causes an AST variation
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('baz'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('y ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Y()'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('why'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Y'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('bar'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('bar'), 3, MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('2'), 1, NUMBER))
    }

    @Test
    void testNewifyTransformCtorCalls() {
        String contents = '''\
            class X {
              X() {}
              X(String s) {}
            }
            @Newify(X)
            class Y {
              X x1 = X()
              X x2 = X.new()
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('X(S'), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('s)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('x1'), 2, FIELD),
            new HighlightedTypedPosition(contents.indexOf('x2'), 2, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('X()'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('new'), 3, CTOR_CALL))
    }

    @Test
    void testNewifyTransformLocalVars() {
        String contents = '@Newify def d = Date.new(123L)'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('d'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('new'), 3, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('123L'), 4, NUMBER))
    }

    @Test
    void testEnumDefs() {
        String contents = '''\
            enum X {
              ONE(1), TWO(Math.PI)

              X(Number val) {
                this.val = val
              }

              X(Number val, Object alt) {
              }

              Number val
              Object alt
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('ONE'), 3, STATIC_VALUE), // OK?
            new HighlightedTypedPosition(contents.indexOf('TWO'), 3, STATIC_VALUE), // OK?
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('PI'), 2, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('val'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('.val') + 1, 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('= val') + 2, 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('val,'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('alt)'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('alt'), 3, FIELD))
    }

    @Test
    void testEnumAnno() {
        addGroovySource '''\
            import java.lang.annotation.*
            @Target(ElementType.FIELD)
            @Retention(RetentionPolicy.RUNTIME)
            @interface Tag { String value() }
            '''.stripIndent()

        String contents = '''\
            enum X {
              @Tag('why') Y
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Y'), 1, STATIC_VALUE))
    }

    @Test
    void testEnumInner() {
        assumeTrue(isAtLeastGroovy(21))

        String contents = '''\
            import groovy.transform.*
            @CompileStatic
            enum X {
              ONE(1) {
                @Override
                def meth(Number param) {
                }
              }

              X(Number val) {
              }

              def meth() {}
            }
            '''.stripIndent()

        assertHighlighting(contents,
            // ensure static $INIT call from line 4 does not result in any highlighting
            new HighlightedTypedPosition(contents.indexOf('ONE'     ), 3, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('1'       ), 1, NUMBER      ),
            new HighlightedTypedPosition(contents.indexOf('meth'    ), 4, METHOD      ),
            new HighlightedTypedPosition(contents.indexOf('param'   ), 5, PARAMETER   ),
            new HighlightedTypedPosition(contents.indexOf('X('      ), 1, CTOR        ),
            new HighlightedTypedPosition(contents.indexOf('val'     ), 3, PARAMETER   ),
            new HighlightedTypedPosition(contents.lastIndexOf('meth'), 4, METHOD      ))
    }

    @Test
    void testAnnoElems() {
        String contents = '''\
            @Grab( module = 'something:anything' )
            import groovy.transform.*

            @SuppressWarnings(value=['rawtypes','unchecked'])
            @TypeChecked(extensions=['something','whatever'])
            class C {
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('module'), 'module'.length(), TAG_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('value'), 'value'.length(), TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('extensions'), 'extensions'.length(), isAtLeastGroovy(21) ? TAG_KEY : UNKNOWN))
    }

    @Test @Ignore("failing on CI server")
    void testAnnoElems2() {
        String contents = '''\
            import groovy.util.logging.Log
            @Log(value='logger') // this logger should not be seen as property by DSLDTypeLookup
            class C {
              static {
                logger.log('msg')
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('value'), 'value'.length(), TAG_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('logger'), 'logger'.length(), STATIC_FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('log'), 'log'.length(), METHOD_CALL))
    }

    @Test
    void testAnnoElems3() {
        String contents = '''\
            class C {
              public static final String VALUE = 'value'
              @SuppressWarnings(C.VALUE)
              def method() {
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('VALUE'), 'VALUE'.length(), STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('VALUE'), 'VALUE'.length(), STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('method'), 'method'.length(), METHOD))
    }

    @Test
    void testAnnoElems4() {
        addGroovySource '''\
            class Bar {
              public static final String VALUE = 'nls'
            }
            '''.stripIndent(), 'Bar', 'foo'

        String contents = '''\
            import static foo.Bar.VALUE
            class C {
              @SuppressWarnings(VALUE)
              def method() {
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('VALUE'), 'VALUE'.length(), STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('VALUE'), 'VALUE'.length(), STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('method'), 'method'.length(), METHOD))
    }

    @Test
    void testGString() {
        String contents = '''\
            class X {
              int i;
              def x(int j) {
                int k;
                "$i + $j + $k"
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('x('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('j)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$i') + 1, 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('$j') + 1, 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('$k') + 1, 1, VARIABLE))
    }

    @Test
    void testGString2() {
        String contents = '''\
            class X {
              int i;
              def x(int j) {
                int k;
                "${i} + ${j} + ${k}"
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('x('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('j)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('i}'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('j}'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k}'), 1, VARIABLE))
    }

    @Test
    void testGString3() {
        String contents = 'def a, b = "/$a/b/c"' // no regex at offset 15
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$a') + 1, 1, VARIABLE))
    }

    @Test
    void testRegex() {
        String contents = '/fdsfasdfas/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/fdsfasdfas/'), '/fdsfasdfas/'.length(), REGEXP))
    }

    @Test
    void testSlashyString0() {
        String contents = '// just a comment'
        assertHighlighting(contents)
    }

    @Test
    void testSlashyString1() {
        String contents = '/ends with dollar$/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/ends with dollar$/'), '/ends with dollar$/'.length(), REGEXP))
    }

    @Test
    void testSlashyString2() {
        String contents = '/$/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/$/'), '/$/'.length(), REGEXP))
    }

    @Test
    void testSlashyString3() {
        String contents = 'def a = /a/\ndef b = /$a/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/$'), '/$a/'.length(), REGEXP),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE))
    }

    @Test
    void testSlashyString4() {
        String contents = 'def a = /a/\ndef b = /${a}/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/$'), '/${a}/'.length(), REGEXP),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE))
    }

    @Test
    void testSlashyString5() {
        String contents = '/\\/with slash/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/\\/with slash/'), '/\\/with slash/'.length(), REGEXP))
    }

    @Test
    void testSlashyString6() {
        String contents = '/with slash\\//'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/with slash\\//'), '/with slash\\//'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString1() {
        String contents = '$/\nSlashy String\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/\nSlashy String\n/$'), '$/\nSlashy String\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString2() {
        String contents = '$/\nSlashy$ String\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/\nSlashy$ String\n/$'), '$/\nSlashy$ String\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString3() {
        String contents = '$/\nSlashy String$\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/\nSlashy String$\n/$'), '$/\nSlashy String$\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString4() {
        String contents = 'def a = /a/\ndef b = $/\n${a}$\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\n${a}$\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString5() {
        String contents = 'def a = /a/\ndef b = $/\n$a$\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\n$a$\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString6() {
        String contents = '''\
            def regexp = $/(?x)       # enable whitespace and comments
            ((?:19|20)\\d\\d)           # year (group 1) (non-capture alternation for century)
            -                         # separator
            (0[1-9]|1[012])           # month (group 2)
            -                         # seperator
            (0[1-9]|[12][0-9]|3[01])  # day (group 3)
            /$
            '''.stripIndent()

        def occurrenceOf = { String str ->
            [contents.indexOf(str), str.length()]
        }

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('regexp'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/'), (contents.indexOf('/$') + 2) - contents.indexOf('$/'), REGEXP),
            new HighlightedTypedPosition(*occurrenceOf('# enable whitespace and comments'), COMMENT),
            new HighlightedTypedPosition(*occurrenceOf('# year (group 1) (non-capture alternation for century)'), COMMENT),
            new HighlightedTypedPosition(*occurrenceOf('# separator'), COMMENT),
            new HighlightedTypedPosition(*occurrenceOf('# month (group 2)'), COMMENT),
            new HighlightedTypedPosition(*occurrenceOf('# seperator'), COMMENT),
            new HighlightedTypedPosition(*occurrenceOf('# day (group 3)'), COMMENT))
    }

    @Test
    void testMultiLineSlashyString7() {
        String contents = '''\
            def regexp = $/
            ((?:19|20)\\d\\d) # comments are not enabled
            /$
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('regexp'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/'), (contents.indexOf('/$') + 2) - contents.indexOf('$/'), REGEXP))
    }

    @Test
    void testUnknown() {
        String contents = 'unknown'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('unknown'), 'unknown'.length(), UNKNOWN))
    }

    @Test
    void testDeprecated() {
        String contents = 'import other.Java\nJava.CONST'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Java'), 'Java'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('Java'), 'Java'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('CONST'), 'CONST'.length(), DEPRECATED))
    }

    @Test
    void testDeprecated2() {
        String contents = '@Deprecated\nclass FOO {\n FOO x }'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('FOO'), 'FOO'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('FOO'), 'FOO'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 'x'.length(), FIELD))
    }

    @Test
    void testDeprecated3() {
        String contents = 'class FOO {\n @Deprecated FOO x\n def y() { x } }'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 'x'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('y'), 'y'.length(), METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 'x'.length(), DEPRECATED))
    }

    @Test
    void testNumberWithSuffix() {
        String contents = ' 11 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1I '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1i '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1L '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1l '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1G '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1g '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1D '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1d '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1F '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))

        contents = ' 1f '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    @Test
    void testOctal() {
        String contents = ' 01 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    @Test
    void testHex() {
        String contents = ' 0x1fff '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 6, NUMBER))
    }

    @Test
    void testDecimal() {
        String contents = ' 8881.23 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 7, NUMBER))
    }

    @Test
    void testExponent() {
        String contents = ' 1.23e-23 '
        assertHighlighting(contents,
                new HighlightedTypedPosition(1, 8, NUMBER))
    }

    @Test
    void testUnaryPlus() {
        String contents = ' +1 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    @Test
    void testUnaryMinus() {
        String contents = ' -1 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    @Test
    void testNumberRange() {
        String contents = ' 0..<100 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('100'), 3, NUMBER))
    }

    @Test // GRECLIPSE-878
    void testMapKey() {
        String contents = 'def map = [key: "value"]'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), MAP_KEY))
    }

    @Test
    void testMapKey2() {
        String contents = '''\
            def key = "key1"
            def map = [(key): "1", key2: "2", \'key3\': "3", "key4": "4"]
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key)'), 'key'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key2'), 'key2'.length(), MAP_KEY))
    }

    @Test
    void testUseBlock() {
        String contents = '''\
            use (groovy.time.TimeCategory) {
              new Date().getDaylightSavingsOffset()
              1.minute.from.now
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('use'),                      3, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('Date'),                     4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('getDaylightSavingsOffset'), 24, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('1'),                        1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('minute'),                   6, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('from'),                     4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('now'),                      3, METHOD_CALL))
    }

    @Test
    void testWithBlock() {
        addGroovySource '''\
            class Foo {
              String val
            }
            '''.stripIndent()

        String contents = '''\
            new Foo().with {
              val = ''
              val.length()
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo'),     3, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),    4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('val'),     3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('length'),  6, METHOD_CALL))
    }

    @Test
    void testWithBlock2() {
        String contents = '''\
            class X { static {
              new Date().with {
                setTime(1234L)
                time = 5678L
                not1
                not2 = hours
              }
            }}
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Date'),    4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),    4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('time'),    4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('5678L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('not1'),    4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('not2'),    4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('hours'),   5, DEPRECATED))
    }

    @Test
    void testWithBlock3() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.TypeChecked
            class X { static {
              new Date().with {
                setTime(1234L)
                time = 5678L
                not1
                not2 = hours
              }
            }}
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Date'),    4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),    4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('time'),    4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('5678L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('not1'),    4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('not2'),    4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('hours'),   5, DEPRECATED))
    }

    @Test
    void testWithBlock4() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.CompileStatic
            class X { static {
              new Date().with {
                setTime(1234L)
                time = 5678L
                not1
                not2 = hours
              }
            }}
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Date'),    4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),    4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('time'),    4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('5678L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('not1'),    4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('not2'),    4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('hours'),   5, DEPRECATED))
    }

    @Test
    void testWithBlock5() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            @groovy.transform.CompileStatic
            class X {
              def getReadOnly() {}
              static {
                new X().with {
                  def val = readOnly
                  readOnly = []
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('getReadOnly'), 11, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('X'),        1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),         4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('val'),      3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('readOnly'),     8, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('readOnly'), 8, UNKNOWN))
    }

    @Test
    void testWithBlock6() {
        assumeTrue(isAtLeastGroovy(24))

        String contents = '''\
            @groovy.transform.CompileStatic
            class X {
              void setWriteOnly(value) {}
              static {
                new X().with {
                  def val = writeOnly
                  writeOnly = []
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('setWriteOnly'), 12, METHOD),
            new HighlightedTypedPosition(contents.indexOf('value'),         5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('X'),         1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),          4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('val'),       3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('writeOnly'),     9, UNKNOWN),
            new HighlightedTypedPosition(contents.lastIndexOf('writeOnly'), 9, METHOD_CALL))
    }

    @Test
    void testLazyInitExpr() {
        String contents = '''\
            class X {
              String x
              @Lazy Collection y = [x]
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('y'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, FIELD))
    }

    @Test
    void testLazyInitExpr2() {
        assumeTrue(isAtLeastGroovy(20))

        addGroovySource '''\
            class Directory {
              static Object lookup(String id) {
                null
              }
            }
            '''.stripIndent()

        String contents = '''\
            @groovy.transform.CompileStatic
            class X {
              String id
              @Lazy Object thing = { ->
                Directory.lookup(id)
              }()
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('id'), 2, FIELD),
            new HighlightedTypedPosition(contents.indexOf('thing'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('lookup'), 6, STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('id'), 2, FIELD))
    }

    @Test
    void testMethodPointer() {
        String contents = '''\
            def x = ''.&length
            def y = Math.&random
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('y'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('length'), 'length'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('random'), 'random'.length(), STATIC_CALL))
    }

    @Test
    void testMethodPointer2() {
        String contents = '''\
            def c = String.class
            def f = c.&toLowerCase
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('c ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('f ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('c.&'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('toLowerCase'), 'toLowerCase'.length(), METHOD_CALL))
    }

    @Test
    void testMethodOverloads() {
        // overloads with generics caused confusion in TypeInferencingVisitor
        String contents = '''\
            class X {
                def findSomething(String string, Set<CharSequence> strings) {
                }
                protected def findSomething(Map<String, ? extends Object> inputs) {
                }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('findSomething'), 'findSomething'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('string'), 'string'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('strings'), 'strings'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('findSomething'), 'findSomething'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('inputs'), 'inputs'.length(), PARAMETER))
    }

    @Test
    void testCategoryMethodOverloads() {
        // implicit 'self' parameter added by transformation caused confusion in TypeInferencingVisitor
        String contents = '''\
            import java.util.regex.Pattern
            @Category(Number)
            class X {
                void method(String string) {
                  println this
                }
                void method(Pattern regex) {
                  println this
                }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('method'), 'method'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('string'), 'string'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('println'), 'println'.length(), GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('method'), 'method'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('regex'), 'regex'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('println'), 'println'.length(), GROOVY_CALL))
    }

    @Test
    void testMemoizedMethod() {
        String contents = '''\
            import groovy.transform.*
            class X {
              @Memoized
              def objectMethod(Object param) {
                'prefix' + param;
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param)'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('param;'), 5, PARAMETER))
    }

    @Test
    void testMemoizedStatic() {
        String contents = '''\
            import groovy.transform.*
            class X {
              @Memoized
              static def staticMethod(Object param) {
                param + 'suffix'
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('param)'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('param +'), 5, PARAMETER))
    }

    @Test
    void testSynchedMethods() {
        String contents = '''\
            import groovy.transform.*
            class X {
              @Synchronized
              def method1(Object param1) {
                int i;
              }
              @WithReadLock
              def method2(Object param2) {
                int j;
              }
              @WithWriteLock
              def method3(Object param3) {
                int k;
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('method1'), 'method1'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param1'), 'param1'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('method2'), 'method2'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param2'), 'param2'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('method3'), 'method2'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param3'), 'param3'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE))
    }

    @Test @Ignore('list and count have been transformed into something, which hampers refactoring and semantic highlighting')
    void testTailCallMethods() {
        assumeTrue(isAtLeastGroovy(23))

        String contents = '''\
            import groovy.transform.*
            class X {
              @TailRecursive
              int sizeOfList(List list, int count = 0) {
                if (list.size() == 0) {
                  count;
                } else {
                  sizeOfList(list.tail(), count + 1);
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('sizeOfList'), 'sizeOfList'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('list'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('count'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('list.'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('size()'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('count;'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('sizeOfList'), 'sizeOfList'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('list.'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('tail'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('count'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER))
    }

    @Test
    void testTypeCheckedMethods() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            import groovy.transform.*
            class X {
              @TypeChecked
              def objectMethod(Object param) {
                int i;
              }
              @TypeChecked
              static def staticMethod(Object param) {
                int j;
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE))
    }

    @Test
    void testStaticCompiledMethods() {
        assumeTrue(isAtLeastGroovy(20))

        String contents = '''\
            import groovy.transform.*
            @CompileStatic
            class X {
              def objectMethod(Object param) {
                int i;
              }
              static def staticMethod(Object param) {
                int j;
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE))
    }

    @Test
    void testSpockStyleMethods() {
        // make sure these retain string highlighting
        assertHighlighting('class X { def "test case name"() {} }')
        assertHighlighting('class X { def \'test case name\'() {} }')
        assertHighlighting('class X { def """test case name"""() {} }')
        assertHighlighting('class X { def \'\'\'test case name\'\'\'() {} }')
    }

    @Test
    void testTraits() {
        assumeTrue(isAtLeastGroovy(23))

        String contents = '''\
            trait Whatever {
              private String field
              String property
              def method() {
                field + property + getProperty() + Math.PI
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('getProperty'), 11, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('PI'), 2, STATIC_VALUE))
    }

    @Test
    void testTraits2() {
        assumeTrue(isAtLeastGroovy(23))

        String contents = '''\
            trait Whatever {
              private String field; String property
              private String method(String param) {
                "$field $param $property $unknown"
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('unknown'), 7, UNKNOWN))
    }

    //
    private int counter

    private void assertHighlighting(String contents, HighlightedTypedPosition... expectedPositions) {
        def references = new GatherSemanticReferences(
            addGroovySource(contents, "Highlighting${++counter}"))
        references.factory = new TypeInferencingVisitorFactory() {
            TypeInferencingVisitorWithRequestor createVisitor(GroovyCompilationUnit gcu) {
                def visitor = super.createVisitor(gcu)
                visitor.DEBUG = true // enable checks
                return visitor
            }
        }
        List<HighlightedTypedPosition> actualPositions = references.findSemanticHighlightingReferences().toList()

        String actual = actualPositions.sort { HighlightedTypedPosition h1, HighlightedTypedPosition h2 ->
            h1.offset <=> h2.offset ?: h1.kind.ordinal() <=> h2.kind.ordinal()
        }.join('\n')
        String expect = expectedPositions.sort { HighlightedTypedPosition h1, HighlightedTypedPosition h2 ->
            h1.offset <=> h2.offset ?: h1.kind.ordinal() <=> h2.kind.ordinal()
        }.join('\n')

        assertEquals(expect, actual)
    }
}
