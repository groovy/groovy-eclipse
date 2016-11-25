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
package org.codehaus.groovy.eclipse.test.ui

import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.*

import org.codehaus.groovy.eclipse.GroovyPlugin
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.editor.highlighting.GatherSemanticReferences
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition
import org.codehaus.groovy.eclipse.test.EclipseTestCase
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.tests.util.GroovyUtils
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor

final class SemanticHighlightingTests extends EclipseTestCase {

    int counter
    boolean semanticHightlightOriginalValue

    @Override
    protected void setUp() {
        super.setUp()

        testProject.createJavaTypeAndPackage('other', 'Java.java',
            'public @Deprecated class Java {\n  @Deprecated public static final String CONST = "";\n}')

        GroovyPlugin.getDefault().getPreferenceStore().with {
            semanticHightlightOriginalValue = getBoolean(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING)
            setValue(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING, true)
        }
    }

    @Override
    protected void tearDown() {
      GroovyPlugin.getDefault().getPreferenceStore().setValue(
          PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING, semanticHightlightOriginalValue)
      super.tearDown()
    }

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

    void testStaticMethods4() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

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

    // GRECLIPSE-1138
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

    void testDefaultGroovyMethods() {
        String contents = '["one", "two"].grep().first()'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('grep'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('first'), 5, GROOVY_CALL))
    }

    void testDefaultGroovyMethods2() {
        String contents = '(["one", "two"] as String[]).first()'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('first'), 5, GROOVY_CALL))
    }

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
            new HighlightedTypedPosition(contents.indexOf('i)'), 1, VARIABLE))
    }

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
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), FIELD), // TODO: MAP_KEY?
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), FIELD)) // TODO: MAP_KEY?
    }

    void testNamedParams2() {
        String contents = '''\
            def map = Collections.singletonMap(key: 'k', value: 'v')
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('singletonMap'), 'singletonMap'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.indexOf('value'), 'value'.length(), MAP_KEY))
    }

    void testMultiAssign() {
        String contents = '''\
            def (a, b) = ['one', 'two']
            '''.stripIndent()

    assertHighlighting(contents,
        new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
        new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE))
    }

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

    void testChainAssign4a() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

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

    void testImplicitParam() {
        String contents = '''\
            def f = { it * "string" }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('f'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('it'), 2, GROOVY_CALL))
    }

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
            new HighlightedTypedPosition(contents.indexOf('extensions'), 'extensions'.length(), GroovyUtils.isAtLeastGroovy(21) ? TAG_KEY : UNKNOWN))
    }

    void testAnnoElems2() {
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

    void testRegex() {
        String contents = '/fdsfasdfas/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/fdsfasdfas/'), '/fdsfasdfas/'.length(), REGEXP))
    }

    void testSlashyString0() {
        String contents = '// just a comment'
        assertHighlighting(contents)
    }

    void testSlashyString1() {
        String contents = '/ends with dollar$/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/ends with dollar$/'), '/ends with dollar$/'.length(), REGEXP))
    }

    void testSlashyString2() {
        String contents = '/$/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/$/'), '/$/'.length(), REGEXP))
    }

    void testSlashyString3() {
        String contents = 'def a = /a/\ndef b = /$a/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/$'), 2, REGEXP),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('/'), 1, REGEXP))
    }

    void testSlashyString4() {
        String contents = 'def a = /a/\ndef b = /${a}/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/$'), 2, REGEXP),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('/'), 1, REGEXP))
    }

    void testSlashyString5() {
        String contents = '/\\/with slash/'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/\\/with slash/'), '/\\/with slash/'.length(), REGEXP))
    }

    void testSlashyString6() {
        String contents = '/with slash\\//'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/with slash\\//'), '/with slash\\//'.length(), REGEXP))
    }

    void testMultiLineSlashyString1() {
        String contents = '$/\nSlashy String\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/\nSlashy String\n/$'), '$/\nSlashy String\n/$'.length(), REGEXP))
    }

    void testMultiLineSlashyString2() {
        String contents = '$/\nSlashy$ String\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/\nSlashy$ String\n/$'), '$/\nSlashy$ String\n/$'.length(), REGEXP))
    }

    void testMultiLineSlashyString3() {
        String contents = '$/\nSlashy String$\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/\nSlashy String$\n/$'), '$/\nSlashy String$\n/$'.length(), REGEXP))
    }

    void testMultiLineSlashyString4() {
        String contents = 'def a = /a/\ndef b = $/\n${a}$\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/\n$'), '$/\n$'.length(), REGEXP),
            new HighlightedTypedPosition(contents.indexOf('$\n/$'), '$\n/$'.length(), REGEXP))
    }

    void testMultiLineSlashyString5() {
        String contents = 'def a = /a/\ndef b = $/\n$a$\n/$'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/\n$'), '$/\n$'.length(), REGEXP),
            new HighlightedTypedPosition(contents.indexOf('$\n/$'), '$\n/$'.length(), REGEXP))
    }

    void testUnknown() {
        String contents = 'unknown'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('unknown'), 'unknown'.length(), UNKNOWN))
    }

    void testDeprecated() {
        String contents = 'import other.Java\nJava.CONST'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Java'), 'Java'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('Java'), 'Java'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('CONST'), 'CONST'.length(), DEPRECATED))
    }

    void testDeprecated2() {
        String contents = '@Deprecated\nclass FOO {\n FOO x }'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('FOO'), 'FOO'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('FOO'), 'FOO'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 'x'.length(), FIELD))
    }

    void testDeprecated3() {
        String contents = 'class FOO {\n @Deprecated FOO x\n def y() { x } }'
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 'x'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('y'), 'y'.length(), METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 'x'.length(), DEPRECATED))
    }

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

    void testOctal() {
        String contents = ' 01 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    void testHex() {
        String contents = ' 0x1fff '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 6, NUMBER))
    }

    void testDecimal() {
        String contents = ' 8881.23 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 7, NUMBER))
    }

    void testExponent() {
        String contents = ' 1.23e-23 '
        assertHighlighting(contents,
                new HighlightedTypedPosition(1, 8, NUMBER))
    }

    void testUnaryPlus() {
        String contents = ' +1 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    void testUnaryMinus() {
        String contents = ' -1 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 2, NUMBER))
    }

    void testNumberRange() {
        String contents = ' 0..<100 '
        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('100'), 3, NUMBER))
    }

    // GRECLIPSE-878
    void testMapKey1() {
        String contents = 'def map = [key: "value"]'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), MAP_KEY))
    }

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

    void testWithBlock1() {
        String contents = '''\
            new Date().with {
              setTime(1234L)
              hours
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Date'),    4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),    4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('hours'),   5, DEPRECATED))
    }

    void testWithBlock2() {
        String contents = '''\
            @groovy.transform.CompileStatic
            class X {
              static {
                new Date().with {
                  setTime(1234L)
                  hours
                }
              }
            }
            '''.stripIndent()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Date'),    4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'),    4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'),   5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('hours'),   5, DEPRECATED))
    }

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

    void testScriptField() {
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

    void testMethodOverload() {
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

    void testTailCallMethods() {
        if (GroovyUtils.GROOVY_LEVEL < 23) return

        String contents = '''\
            import groovy.transform.*
            class X {
              @TailRecursive
              int sizeOfList(List list, int count = 0) {
                // list and count have been transformed into something,
                // which hampers refactoring and semantic highlighting
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

    void testTypeCheckedMethods() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

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
            '''

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE))
    }

    void testStaticCompiledMethods() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return

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
            '''

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE))
    }

    void testSpockStyleMethods() {
        // make sure these retain string highlighting
        assertHighlighting('class X { def "test case name"() {} }')
        assertHighlighting('class X { def \'test case name\'() {} }')
        assertHighlighting('class X { def """test case name"""() {} }')
        assertHighlighting('class X { def \'\'\'test case name\'\'\'() {} }')
        assertHighlighting('class X { def /test case name/() {} }')
        assertHighlighting('class X { def $/test case name/$() {} }')
    }

    void testTraits() {
        if (GroovyUtils.GROOVY_LEVEL < 23) return

        String contents = '''\
            trait Whatever {
              String property
              private String field
              def method() {
                field + property + getProperty() + Math.PI
              }
            }
            '''

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.indexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('getProperty'), 11, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('PI'), 2, STATIC_VALUE))
    }

    //

    private void assertHighlighting(String contents, HighlightedTypedPosition... expectedPositions) {
        GroovyCompilationUnit unit = testProject.createGroovyTypeAndPackage('', "Highlighting${++counter}.groovy", contents)
        checkStyles(unit, expectedPositions as List)
    }

    private void checkStyles(GroovyCompilationUnit unit, List<HighlightedTypedPosition> expectedPositions) {
        GatherSemanticReferences references = new GatherSemanticReferences(unit)
        references.factory = new TypeInferencingVisitorFactory() {
            TypeInferencingVisitorWithRequestor createVisitor(GroovyCompilationUnit gcu) {
                def visitor = super.createVisitor(gcu)
                visitor.DEBUG = true // enable checks
                return visitor
            }
        }
        List<HighlightedTypedPosition> actualPositions = references.findSemanticHighlightingReferences().toList()

        actualPositions.sort { HighlightedTypedPosition h1, HighlightedTypedPosition h2 ->
            h1.offset <=> h2.offset ?: h1.kind.ordinal() <=> h2.kind.ordinal()
        }
        expectedPositions.sort { HighlightedTypedPosition h1, HighlightedTypedPosition h2 ->
            h1.offset <=> h2.offset ?: h1.kind.ordinal() <=> h2.kind.ordinal()
        }

        assertEquals(expectedPositions.join('\n'), actualPositions.join('\n'))
    }
}
