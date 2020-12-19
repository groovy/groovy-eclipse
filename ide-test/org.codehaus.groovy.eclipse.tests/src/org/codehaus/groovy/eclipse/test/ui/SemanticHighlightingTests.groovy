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
package org.codehaus.groovy.eclipse.test.ui

import static org.codehaus.groovy.eclipse.GroovyPlugin.getDefault as getGroovyPlugin
import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.*
import static org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition.HighlightKind.GROOVY_CALL as GSTRING
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser
import static org.junit.Assert.assertEquals
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.editor.highlighting.GatherSemanticReferences
import org.codehaus.groovy.eclipse.editor.highlighting.HighlightedTypedPosition
import org.codehaus.groovy.eclipse.preferences.PreferenceConstants
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

final class SemanticHighlightingTests extends GroovyEclipseTestSuite {

    @Before
    void setUp() {
        groovyPlugin.preferenceStore.setValue(PreferenceConstants.GROOVY_SEMANTIC_HIGHLIGHTING, true)
    }

    @Test
    void testArrays1() {
        String contents = '''\
            |def strings = new String[42]
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('42'), 2, NUMBER))
    }

    @Test
    void testArrays2() {
        String contents = '''\
            |String[] strings = [] as String[]
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS))
    }

    @Test
    void testArrays3() {
        String contents = '''\
            |java.lang.String[] strings = (String[]) null
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS))
    }

    @Test
    void testArrays4() {
        String contents = '''\
            |def test(String[] strings) {
            |  return strings.toString()
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('test'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('strings'), 7, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('toString'), 8, GROOVY_CALL))
    }

    @Test
    void testFields1() {
        String contents = '''\
            |class X {
            |  String one
            |  public Object two
            |  private Integer three
            |  private @Lazy Collection four
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Integer'), 7, CLASS),
            new HighlightedTypedPosition(contents.indexOf('three'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Collection'), 10, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('four'), 4, FIELD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/876
    void testFields2() {
        addGroovySource '''\
            |import groovy.transform.PackageScope
            |class Pogo {
            |  @PackageScope String string
            |}
            |'''.stripMargin()

        String contents = '''\
            |class X extends Pogo {{
            |    string
            |    getString()
            |    setString('value')
            |}}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Pogo'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, FIELD),
            new HighlightedTypedPosition(contents.indexOf('getString'), 9, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('setString'), 9, UNKNOWN))
    }

    @Test
    void testScriptFields1() {
        String contents = '''\
            |@groovy.transform.Field List list = [1, 2]
            |list << 'three'
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('List'), 4, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('list'), 4, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('list'), 4, FIELD))
    }

    @Test
    void testScriptFields2() {
        String contents = '''\
            |import groovy.transform.Field
            |
            |@Field String one
            |@Field Integer two = 1234
            |@Field private Object three // four
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Integer'), 7, CLASS),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('1234'), 4, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('three'), 5, FIELD))
    }

    @Test
    void testScriptFields3() {
        String contents = '''\
            |import groovy.transform.Field
            |
            |@Field Number one
            |@Field static Double TWO
            |
            |one + TWO;
            |{ -> one + TWO }
            |{ -> one = 1; TWO = 2.0d }
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Double'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('TWO'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.indexOf('one '), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('TWO;'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.indexOf('one', contents.indexOf('->')), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('TWO', contents.indexOf('->')), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.lastIndexOf('TWO'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('2'), 4, NUMBER))
    }

    @Test
    void testStaticFields1() {
        String contents = '''\
            |class X {
            |  private static FOO
            |  def x() { FOO; }
            |  static { FOO = [] }
            |}
            '''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('FOO'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.indexOf('FOO;'), 3, STATIC_FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('FOO'), 3, STATIC_FIELD))
    }

    @Test
    void testStaticFinals1() {
        String contents = '''\
            |Math.PI
            |ObjectStreamConstants.STREAM_MAGIC
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Math'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('PI'), 2, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('ObjectStreamConstants'), 'ObjectStreamConstants'.length(), INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('STREAM_MAGIC'), 'STREAM_MAGIC'.length(), STATIC_VALUE))
    }

    @Test
    void testStaticFinals2() {
        String contents = '''\
            |import static java.lang.Math.PI
            |def pi = PI
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('pi'), 2, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('PI'), 2, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('PI'), 2, STATIC_VALUE))
    }

    @Test
    void testStaticFinals3() {
        String contents = '''\
            |class C {
            |  static final VALUE = 'value'
            |  static foo() {
            |    VALUE
            |  }
            |  static class Inner {
            |    void bar() {
            |      VALUE
            |    }
            |  }
            |}
            |class SamePack {
            |  def baz = C.VALUE
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('VALUE'), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('VALUE', contents.indexOf('foo')), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('Inner'), 5, CLASS),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('VALUE', contents.indexOf('bar')), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('SamePack'), 8, CLASS),
            new HighlightedTypedPosition(contents.indexOf('baz'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('VALUE', contents.indexOf('baz')), 5, STATIC_VALUE))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/905
    void testCtors() {
        String contents = '''\
            |import groovy.transform.PackageScope
            |class Example {
            |  Example() {}
            |  /*public*/ Example(a) {}
            |  private Example(b, c) {}
            |  protected Example(d, e, f) {}
            |  @PackageScope Example(... geez) {}
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Example'), 7, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Example('), 7, CTOR),
            new HighlightedTypedPosition(contents.indexOf('Example(a'), 7, CTOR),
            new HighlightedTypedPosition(contents.indexOf('Example(b'), 7, CTOR),
            new HighlightedTypedPosition(contents.indexOf('Example(d'), 7, CTOR),
            new HighlightedTypedPosition(contents.indexOf('Example(.'), 7, CTOR),
            new HighlightedTypedPosition(contents.indexOf('a)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('b,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('c)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('d,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('e,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('f)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('geez'), 4, PARAMETER))
    }

    @Test
    void testMethods() {
        String contents = '''\
            |import groovy.transform.PackageScope
            |class X {
            |  def a() {}
            |  /*public*/ def b() {}
            |  protected def  c() {}
            |  @PackageScope def d() {}
            |  private synchronized def e() {}
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('a('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('b('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('c('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('d('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('e('), 1, METHOD))
    }

    @Test
    void testClassMethods1() {
        String contents = '''\
            |Number.getClass() // Object method on Class instance
            |String.getSimpleName()
            |
            |class X {
            |  static {
            |    getCanonicalName()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('getClass'), 'getClass'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('getSimpleName'), 'getSimpleName'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('getCanonicalName'), 'getCanonicalName'.length(), METHOD_CALL))
    }

    @Test
    void testClassMethods2() {
        String contents = '''\
            |import java.util.regex.Pattern
            |Pattern.class.matcher("")
            |Pattern.matcher('')
            |def pat = Pattern
            |pat.matcher("")
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Pattern.'), 'Pattern'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('matcher('), 'matcher'.length(), UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('Pattern', contents.indexOf('""')), 'Pattern'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('matcher(\''), 'matcher'.length(), UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('pat'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Pattern', contents.indexOf('pat')), 'Pattern'.length(), CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('pat'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('matcher'), 'matcher'.length(), UNKNOWN))
    }

    @Test
    void testStaticMethods1() {
        String contents = '''\
            |class X {
            |  static FOO() { FOO() }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('FOO'), 3, STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('FOO'), 3, STATIC_CALL))
    }

    @Test
    void testStaticMethods2() {
        String contents = '''\
            |class X {
            |  static {
            |    def y = Collections.emptyMap()
            |    def z = java.util.Collections.emptySet()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('y ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Collections'), 'Collections'.length(), CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('emptyMap'), 'emptyMap'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('z ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Collections'), 'Collections'.length(), CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('emptySet'), 'emptySet'.length(), STATIC_CALL))
    }

    @Test
    void testStaticMethods3() {
        String contents = '''\
            |import static java.util.Collections.singletonList
            |class X {
            |  def meth() {
            |    return singletonList('x')
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('singletonList'), 'singletonList'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('singletonList'), 'singletonList'.length(), STATIC_CALL))
    }

    @Test
    void testStaticMethods4() {
        String contents = '''\
            |import static java.lang.Integer.valueOf
            |@groovy.transform.CompileStatic
            |class X {
            |  String number
            |  int getN() {
            |    valueOf(number) // needs sloc; see MethodCallExpression.setSourcePosition
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('getN'), 'getN'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('number'), 'number'.length(), FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('number'), 'number'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('valueOf'), 'valueOf'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('valueOf'), 'valueOf'.length(), STATIC_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/545
    void testStaticMethods5() {
        addGroovySource '''\
            |class Util {
            |  static def getSomething() { value }
            |  static void setSomething(value) { }
            |}
            |'''.stripMargin(), 'Util', 'pack'

        String contents = '''\
            |import static pack.Util.*
            |def thing = something
            |something = null
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('thing'), 'thing'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('something'), 'something'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('something'), 'something'.length(), STATIC_CALL))
    }

    @Test // GRECLIPSE-1138
    void testMultipleStaticMethods() {
        String contents = '''\
            |f(1,2)
            |
            |static f(List a, List b = null) {
            |}
            |static f(int a, int b) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('f(1'), 1, STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('f(List'), 1, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('List a'), 4, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('a,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('List b'), 4, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('b '), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('f(int'), 1, STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('a,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('b)'), 1, PARAMETER))
    }

    @Test // GROOVY-7363
    void testSyntheticBridgeMethod() {
        addJavaSource '''\
            |public interface Face<T> {
            |  T getItem();
            |}
            |'''.stripMargin(), 'Face'
        addGroovySource '''\
            |class Impl implements Face<Pogo> {
            |  Pogo item = new Pogo()
            |}
            |class Pogo {
            |  def prop
            |}
            |'''.stripMargin(), 'Impl'

        String contents = '''\
            |@groovy.transform.TypeChecked
            |void test(Impl impl) {
            |  assert impl.item.prop != null
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('test'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Impl'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('impl'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('impl'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('item'), 4, FIELD),
            new HighlightedTypedPosition(contents.indexOf('prop'), 4, FIELD))
    }

    @Test
    void testMethodsAsProperties1() {
        String contents = '''\
            |import java.lang.management.ManagementFactory
            |// compact form:
            |ManagementFactory.runtimeMXBean.inputArguments
            |// expanded form:
            |ManagementFactory.getRuntimeMXBean().getInputArguments()
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('ManagementFactory.r'), 'ManagementFactory'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('runtimeMXBean'), 'runtimeMXBean'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('inputArguments'), 'inputArguments'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('ManagementFactory.g'), 'ManagementFactory'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('getRuntimeMXBean'), 'getRuntimeMXBean'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('getInputArguments'), 'getInputArguments'.length(), METHOD_CALL))
    }

    @Test
    void testMethodsAsProperties2() {
        addGroovySource '''\
            |class Foo {
            |  private static final String value = ''
            |  static String getValue() {
            |    return value
            |  }
            |}
            |'''.stripMargin()

        String contents = 'Foo.value'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('value'), 5, STATIC_CALL))
    }

    @Test
    void testMethodsAsProperties3() {
        addGroovySource '''\
            |class Foo {
            |  private static final String value = ''
            |  static String getValue() {
            |    return value
            |  }
            |}
            |'''.stripMargin()

        String contents = 'new Foo().value'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('value'), 5, STATIC_CALL))
    }

    @Test
    void testMethodsAsProperties4() {
        addGroovySource '''\
            |interface Bar { def getOne() }
            |interface Baz extends Bar { def getTwo() }
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def fun(Baz b) {
            |    b.one + b.two
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('fun'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Baz'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('b) '), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('b.o'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('b.t'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, METHOD_CALL))
    }

    @Test
    void testMethodsAsProperties5() {
        String contents = '''\
            |class C {
            |  Object getFoo() {}
            |  boolean isBar() {}
            |  void setBaz(to) {}
            |  void method() {
            |    foo
            |    bar
            |    baz
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('getFoo'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('isBar' ), 5, METHOD),
            new HighlightedTypedPosition(contents.indexOf('setBaz'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('to'), 2, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('baz'), 3, METHOD_CALL))
    }

    @Test
    void testMethodsAsProperties6() {
        String contents = '''\
            |class C {
            |  static Object getFoo() {}
            |  static boolean isBar() {}
            |  static void setBaz(to) {}
            |  static void main(args) {
            |    foo
            |    bar
            |    baz
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('getFoo'), 6, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('isBar' ), 5, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('setBaz'), 6, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('to'), 2, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('main'), 4, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('args'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('baz'), 3, STATIC_CALL))
    }

    @Test
    void testDefaultGroovyMethods1() {
        String contents = '["one", "two"].grep().first()'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('grep'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('first'), 5, GROOVY_CALL))
    }

    @Test
    void testDefaultGroovyMethods2() {
        String contents = '(["one", "two"] as String[]).first()'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('first'), 5, GROOVY_CALL))
    }

    @Test
    void testDefaultGroovyMethods3() {
        String contents = '''\
            |class Foo {
            |  static {
            |    getAt("staticProperty")
            |  }
            |  Foo() {
            |    getAt("instanceProperty")
            |  }
            |  def m() {
            |    println "message of importance"
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Foo()'), 3, CTOR),
            new HighlightedTypedPosition(contents.indexOf('m() {'), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('getAt'), 5, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('getAt'), 5, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('println'), 7, GROOVY_CALL))
    }

    @Test
    void testNotCategoryMethod1() {
        String contents = 'def x = "equals"' // equals is a DGM and had been improperly identified by CategoryTypeLookup

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/512
    void testNotCategoryMethod2() {
        addGroovySource '''\
            |import java.lang.reflect.*
            |class Reflections {
            |  static Method findMethod(String methodName, Class<?> targetClass, Class<?>... paramTypes) {
            |  }
            |  static Object invokeMethod(Method method, Object target, Object... params) {
            |  }
            |}
            |'''.stripMargin(), 'Reflections'

        String contents = '''\
            |static void setThreadLocalProperty(String key, Object val) { Class target = null // redacted
            |  def setter = Reflections.findMethod('setThreadLocalProperty', target, String, Object)
            |  Reflections.invokeMethod(setter, target, key, val)
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('setThreadLocalProperty'), 'setThreadLocalProperty'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('key'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('val'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Class'), 5, CLASS),
            new HighlightedTypedPosition(contents.indexOf('target'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('setter'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Reflections'), 'Reflections'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('findMethod'), 'findMethod'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('target,'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('Reflections'), 'Reflections'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('invokeMethod'), 'invokeMethod'.length(), STATIC_CALL), // not DGM
            new HighlightedTypedPosition(contents.lastIndexOf('setter'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('target'), 6, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('key'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, PARAMETER))
    }

    @Test
    void testVariadicMehtods() {
        String contents = '''\
            |class X {
            |  def one() {
            |    int i;
            |  }
            |  def two(String... strings) {
            |    int j;
            |  }
            |  def three(x, ... y) {
            |    int k;
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('three'), 5, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('y'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE))
    }

    @Test
    void testScriptVariable1() {
        String contents = '''\
            |abc = null
            |def xyz = abc
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('abc'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('xyz'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('abc'), 3, VARIABLE))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1117
    void testScriptVariable2() {
        String contents = '''\
            |block = { -> }
            |block()
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('block'), 5, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('block'), 5, VARIABLE))
    }

    @Test
    void testParamsAndLocals() {
        String contents = '''\
            |class X {
            |  def loop(int n ) {
            |    def f = { int x -> x * n }
            |    for (int i = 0; i < n; i += 1) {
            |      f(i)
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('loop'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('n )'), 1, PARAMETER),
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
    void testParamDefault() {
        String contents = '''\
            |def closure = { int i = 2 ->
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('closure'), 'closure'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('i'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER))
    }

    @Test
    void testNamedParams1() {
        String contents = '''\
            |class Person { String firstName, lastName }
            |def p = new Person(firstName: 'John', lastName: 'Doe')
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Person'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('firstName'), 'firstName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('lastName'), 'lastName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('p'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Person'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('Person'), 6, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), FIELD))
    }

    @Test
    void testNamedParams2() {
        String contents = '''\
            |class Person {
            |  String firstName, lastName
            |
            |  Person() {}
            |  Person(Map m) {} // trumps default+setters
            |}
            |def p = new Person(firstName: 'John', lastName: 'Doe')
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Person'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('firstName'), 'firstName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('lastName'), 'lastName'.length(), FIELD),
            new HighlightedTypedPosition(contents.indexOf('Person', contents.indexOf('lastName')), 'Person'.length(), CTOR),
            new HighlightedTypedPosition(contents.indexOf('Person', contents.indexOf('() {}')), 'Person'.length(), CTOR),
            new HighlightedTypedPosition(contents.indexOf('Map'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('m)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('p ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Person'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('Person'), 6, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('firstName'), 'firstName'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('lastName'), 'lastName'.length(), MAP_KEY))
    }

    @Test
    void testNamedParams3() {
        String contents = 'def map = Collections.singletonMap(key: "k", value: "v")'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Collections'), 'Collections'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('singletonMap'), 'singletonMap'.length(), UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), MAP_KEY),
            new HighlightedTypedPosition(contents.indexOf('value'), 'value'.length(), MAP_KEY))
    }

    @Test
    void testMultiAssign() {
        String contents = 'def (a, b) = ["one", "two"]'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE))
    }

    @Test
    void testChainAssign1() {
        String contents = '''\
            |class C {
            |  String fld
            |  C() {
            |    String var
            |    fld = var = ''
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('fld'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('var'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('fld'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('var'), 3, VARIABLE))
    }

    @Test
    void testChainAssign2() {
        String contents = '''\
            |class C {
            |  String one, two
            |  C() {
            |    one = two = ''
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('two'), 3, FIELD))
    }

    @Test
    void testChainAssign3() {
        String contents = '''\
            |class C {
            |  String one, two
            |}
            |def c = new C()
            |c.one = c.two = ''
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('c ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('c.'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('one'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('c.'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('two'), 3, FIELD))
    }

    @Test
    void testChainAssign4() {
        // property notation that maps to setter; this kind of chain assignment does work
        String contents = '''\
            |class B {
            |  private String z
            |  void setZero(String zero) { z = zero }
            |}
            |
            |class C {
            |  String x
            |  B b
            |  C() {
            |    x = b.zero = 'X'
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('B'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('z'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('setZero'), 7, METHOD),
            new HighlightedTypedPosition(contents.indexOf('(String') + 1, 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('zero'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('z ='), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('zero }'), 4, PARAMETER),

            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('B'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CTOR),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('b'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('zero'), 4, METHOD_CALL))
    }

    @Test
    void testChainAssign5() {
        // property notation that maps to setter; this kind of chain assignment does work
        // static compilation produces list of expressions (temp,call) for "_ = b.zero ="
        String contents = '''\
            |class B {
            |  private String z
            |  void setZero(String zero) { z = zero }
            |}
            |
            |@groovy.transform.CompileStatic
            |class C {
            |  String x
            |  B b
            |  C() {
            |    x = b.zero = 'X'
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('B'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('z'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('setZero'), 7, METHOD),
            new HighlightedTypedPosition(contents.indexOf('(String') + 1, 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('zero'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('z ='), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('zero }'), 4, PARAMETER),

            new HighlightedTypedPosition(contents.indexOf('C {'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('B'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CTOR),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('b'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('zero'), 4, METHOD_CALL))
    }

    @Test
    void testMultiAssign1() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |void meth() {
            |  def (Integer i, String s) = [123, 'abc']
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Integer'), 7, CLASS),
            new HighlightedTypedPosition(contents.indexOf('i,'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('s)'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('123'), 3, NUMBER))
    }

    @Test
    void testCatchParams1() {
        // don't want PARAMETER
        String contents = '''\
            |class X {
            |  def except() {
            |    try {
            |    } catch (Exception specific) {
            |      specific.printStackTrace()
            |    } catch (unspecified) {
            |      unspecified.printStackTrace()
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('except'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Exception'), 9, CLASS),
            new HighlightedTypedPosition(contents.indexOf('specific'), 8, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('specific'), 8, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('unspecified'), 'unspecified'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('unspecified'), 'unspecified'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('printStackTrace'), 'printStackTrace'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('printStackTrace'), 'printStackTrace'.length(), METHOD_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1042
    void testCatchParams2() {
        String contents = '''\
            |try {
            |} catch (java.lang.Error | java.lang.Exception e) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Error'), 5, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Exception'), 9, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('e'), 1, VARIABLE))
    }

    @Test
    void testCatchParamWithInstanceOf() {
        // don't want PARAMETER
        String contents = '''\
            |class X {
            |  def m() {
            |    try {
            |    } catch (Exception ex) {
            |      if (ex instanceof RuntimeException) {
            |        ex // instanceof flow typing caused catch param check to break down
            |      } else {
            |        ex
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('m()'), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Exception'), 9, CLASS),
            new HighlightedTypedPosition(contents.indexOf('ex)'), 2, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('ex in'), 2, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('RuntimeException'), 16, CLASS),
            new HighlightedTypedPosition(contents.indexOf('ex //'), 2, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('ex'), 2, VARIABLE))
    }

    @Test
    void testForParam() {
        String contents = '''\
            |for (int i = 0; i < n; i++) {
            |  i
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('i ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('0'  ), 1, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('i <'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n; '), 1, UNKNOWN ),
            new HighlightedTypedPosition(contents.indexOf('i++'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('i'), 1, VARIABLE))
    }

    @Test
    void testForParams1() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |for (int i = 0, n = 999; i < n; i++) {
            |  i
            |  n
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('i ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('0'  ), 1, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('n ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('999'), 3, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('i <'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n; '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('i++'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('i'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('n'), 1, VARIABLE))
    }

    @Test
    void testForParams2() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |for (def (i, n) = [0, 999]; i < n; i++) {
            |  i
            |  n
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('i'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('0'  ), 1, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('999'), 3, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('i <'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n; '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('i++'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('i'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('n'), 1, VARIABLE))
    }

    @Test
    void testForParams3() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |for (def (int i, int n) = [0, 999]; i < n; i++) {
            |  i
            |  n
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('i,'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n)'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('0'  ), 1, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('999'), 3, NUMBER  ),
            new HighlightedTypedPosition(contents.indexOf('i <'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n; '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('i++'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('i'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('n'), 1, VARIABLE))
    }

    @Test
    void testForEachParam() {
        // don't want PARAMETER
        String contents = '''\
            |class X {
            |  def loop() {
            |    for (Object x : []) {
            |        x + ""
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('loop'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x : '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, VARIABLE))
    }

    @Test
    void testForEachInParam() {
        // don't want PARAMETER
        String contents = '''\
            |class X {
            |  def loop() {
            |    for (x in []) {
            |        x + ""
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'),     1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('loop'),  4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x in '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, VARIABLE))
    }

    @Test // assign within terminal scope
    void testForEachInParamWithReturn() {
        // don't want PARAMETER
        String contents = '''\
            |class X {
            |  def loop() {
            |    for (x in []) {
            |        x = ''
            |        return
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('loop'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x in '), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, VARIABLE))
    }

    @Test
    void testForEachInParamWithInstanceOf() {
        // don't want PARAMETER
        String contents = '''\
            |class X {
            |  def loop() {
            |    for (x in []) {
            |      if (x instanceof String) {
            |        x // instanceof flow typing caused for-each param check to break down
            |      } else {
            |        x
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'),      1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('loop'),   4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x in '),  1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('x ins'),  1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x // '),  1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('x'),  1, VARIABLE))
    }

    @Test
    void testImplicitParam() {
        String contents = 'def f = { it * "string" }'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('f'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('it'), 2, GROOVY_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1000
    void testLambdaParams1() {
        assumeTrue(isParrotParser())

        String contents = 'def f = p -> p * "string"'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('f'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf(    'p'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('p'), 1, PARAMETER))
    }

    @Test
    void testLambdaParams2() {
        assumeTrue(isParrotParser())

        String contents = 'def f = (p) -> { p * "string" }'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('f'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf(    'p'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('p'), 1, PARAMETER))
    }

    @Test
    void testLambdaParams3() {
        assumeTrue(isParrotParser())

        String contents = 'def f = (Object p) -> { p * "string" }'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('f'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf(    'p'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('p'), 1, PARAMETER))
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-9630
    void testVarKeyword0() {
        String contents = '''\
            |def var
            |var = null
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('var'), 3, VARIABLE))
    }

    @Test
    void testVarKeyword1() {
        String contents = '''\
            |def abc = null
            |int ijk = null
            |var xyz = null
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('abc'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('ijk'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('xyz'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('var'), 3, isParrotParser() ? RESERVED : UNKNOWN))
    }

    @Test
    void testVarKeyword2() {
        String contents = 'var var = null'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, isParrotParser() ? RESERVED : UNKNOWN),
            new HighlightedTypedPosition(contents.lastIndexOf('var'), 3, VARIABLE))
    }

    @Test
    void testVarKeyword3() {
        assumeTrue(isParrotParser())

        String contents = 'var (x, y, z) = [1, 2, 3]'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, RESERVED),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('y'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('z'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('3'), 1, NUMBER))
    }

    @Test
    void testVarKeyword4() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |for (var item : list) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, RESERVED),
            new HighlightedTypedPosition(contents.indexOf('item'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('list'), 4, UNKNOWN))
    }

    @Test
    void testVarKeyword5() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |for (var item in list) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, RESERVED),
            new HighlightedTypedPosition(contents.indexOf('item'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('list'), 4, UNKNOWN))
    }

    @Test
    void testVarKeyword6() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |for (var i = 0; i < n; i += 1) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, RESERVED),
            new HighlightedTypedPosition(contents.indexOf('i ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('i <'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('n'), 1, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('i +'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER))
    }

    @Test
    void testVarKeyword7() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |try (var str = getClass().getResourceAsStream('rsrc')) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('var'), 3, RESERVED),
            new HighlightedTypedPosition(contents.indexOf('str'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('getClass'), 'getClass'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('getResourceAsStream'), 'getResourceAsStream'.length(), METHOD_CALL))
    }

    @Test
    void testThisAndSuper() {
        // the keywords super and this are identified/highlighted by GroovyTagScanner
        String contents = '''\
            |def f = {
            |  super
            |  this
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('f ='), 1, VARIABLE))
    }

    @Test
    void testGStringThisAndSuper1() {
        // except when appearing within a GString
        String contents = '"this: $this, super: $super"'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('this:'), 6, STRING),
            new HighlightedTypedPosition(contents.indexOf('$this') + 1, 4, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf(', super: '), 9, STRING),
            new HighlightedTypedPosition(contents.indexOf('$super') + 1, 5, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('"'), '"this: $this, super: $super"'.length(), GSTRING))
    }

    @Test
    void testGStringThisAndSuper2() {
        String contents = '"this: ${this}, super: ${super}"'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('this:'), 6, STRING),
            new HighlightedTypedPosition(contents.indexOf('${this}') + 2, 4, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf(', super: '), 9, STRING),
            new HighlightedTypedPosition(contents.indexOf('${super}') + 2, 5, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('"'), '"this: ${this}, super: ${super}"'.length(), GSTRING))
    }

    @Test
    void testGStringThisAndSuper3() {
        String contents = '"${this.hashCode()}, ${super.hashCode()}"'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('this'), 4, KEYWORD),
            new HighlightedTypedPosition(contents.indexOf('hashCode'), 'hashCode'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf(', '), 2, STRING),
            new HighlightedTypedPosition(contents.indexOf('super'), 5, KEYWORD),
            new HighlightedTypedPosition(contents.lastIndexOf('hashCode'), 'hashCode'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('"'), '"${this.hashCode()}, ${super.hashCode()}"'.length(), GSTRING))
    }

    @Test // see testDeprecated10 for case where this and super calls are highlighted
    void testCtorCalls() {
        // the keywords super and this are identified/highlighted by GroovyTagScanner
        String contents = '''\
            |class X {
            |  X() {
            |    super();
            |  }
            |  X(String s) {
            |    this();
            |  }
            |}
            |def x = new X();
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('X(S'), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('s)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CTOR_CALL))
    }

    @Test
    void testInnerClassCtorCalls() {
        String contents = '''\
            |class X {
            |  class Y {
            |    String foo
            |    Integer bar
            |  }
            |  def baz() {
            |    def y = new Y()
            |    def why = new Y(foo: '1', bar: 2) // non-static inner class causes an AST variation
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Y'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Integer'), 7, CLASS),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('baz'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('y ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Y()'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('Y()'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('why'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Y'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('Y'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('bar'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('bar'), 3, MAP_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('2'), 1, NUMBER))
    }

    @Test
    void testNewifyTransformCtorCalls() {
        String contents = '''\
            |class X {
            |  X() {}
            |  X(String s) {}
            |}
            |@Newify(X)
            |class Y {
            |  X x1 = X()
            |  X x2 = X.new()
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('X(S'), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('s)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('X)'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Y'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('X x1'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x1'), 2, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('X()'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('X x2'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x2'), 2, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('new'), 3, CTOR_CALL))
    }

    @Test
    void testNewifyTransformLocalVars() {
        String contents = '@Newify def d = Date.new(123L)'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('d'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('Date'), 4, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('new'), 3, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('123L'), 4, NUMBER))
    }

    @Test
    void testEnumDefs() {
        String contents = '''\
            |enum X {
            |  ONE(1), TWO(Math.PI)
            |
            |  X(Number val) {
            |    this.val = val
            |  }
            |
            |  X(Number val, Object alt) {
            |  }
            |
            |  Number val
            |  Object alt
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('ONE'), 3, STATIC_VALUE), // OK?
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('TWO'), 3, STATIC_VALUE), // OK?
            new HighlightedTypedPosition(contents.indexOf('Math'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('PI'), 2, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('val'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('.val') + 1, 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('= val') + 2, 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('Number val,'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('val,'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('alt)'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('alt'), 3, FIELD))
    }

    @Test
    void testEnumAnno() {
        addGroovySource '''\
            |import java.lang.annotation.*
            |@Target(ElementType.FIELD)
            |@Retention(RetentionPolicy.RUNTIME)
            |@interface Tag { String value() }
            |'''.stripMargin()

        String contents = '''\
            |enum X {
            |  @Tag('why') Y
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('Y'), 1, STATIC_VALUE))
    }

    @Test
    void testEnumInner() {
        String contents = '''\
            |import groovy.transform.*
            |@CompileStatic
            |enum X {
            |  ONE(1) {
            |    @Override
            |    def meth(Number param) {
            |    }
            |  }
            |
            |  X(Number val) {
            |  }
            |
            |  def meth() {}
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, ENUMERATION),
            // ensure static $INIT call from line 4 does not result in any highlighting
            new HighlightedTypedPosition(contents.indexOf('ONE'), 3, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('ONE'), 3, ENUMERATION), // okay?
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('X('), 1, CTOR),
            new HighlightedTypedPosition(contents.lastIndexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('val'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('meth'), 4, METHOD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1004
    void testEnumMethod() {
        String contents = '''\
            |enum X {
            |  Y
            |}
            |X.Y.next().name()
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('Y'), 1, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, ENUMERATION),
            new HighlightedTypedPosition(contents.lastIndexOf('Y'), 1, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('next'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('name'), 4, METHOD_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/938
    void testEnumValues() {
        setJavaPreference(CompilerOptions.OPTION_AnnotationBasedNullAnalysis, CompilerOptions.ENABLED)

        String contents = '''\
            |enum X {
            |  Y
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('Y'), 1, STATIC_VALUE))
    }

    @Test
    void testAnnoElems1() {
        String contents = '''\
            |@Grab( module = 'something:anything' )
            |import groovy.transform.*
            |
            |@SuppressWarnings(value=['rawtypes','unchecked'])
            |@TypeChecked(extensions=['something','whatever'])
            |class C {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('module'), 'module'.length(), TAG_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('value'), 'value'.length(), TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('extensions'), 'extensions'.length(), TAG_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CLASS))
    }

    @Test
    void testAnnoElems2() {
        String contents = '''\
            |import groovy.util.logging.Log
            |@Log(value='logger') // this logger should not be seen as property by DSLDTypeLookup
            |class C {
            |  static {
            |    logger.log('msg')
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('value'), 5, TAG_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('logger'), 6, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('log'), 3, METHOD_CALL))
    }

    @Test
    void testAnnoElems3() {
        String contents = '''\
            |class C {
            |  public static final String VALUE = 'value'
            |  @SuppressWarnings(C.VALUE)
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('VALUE'), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('VALUE'), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD))
    }

    @Test
    void testAnnoElems4() {
        addGroovySource '''\
            |class Bar {
            |  public static final String VALUE = 'nls'
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import static foo.Bar.VALUE
            |class C {
            |  @SuppressWarnings(VALUE)
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('VALUE'), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('VALUE'), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD))
    }

    @Test
    void testAnnoElems5() {
        addGroovySource '''\
            |class Bar {
            |  public static final String RAW = 'raw'
            |  public static final String TYPES = 'types'
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import static foo.Bar.*
            |class C {
            |  @SuppressWarnings(RAW + TYPES)
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('RAW'), 3, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('TYPES'), 5, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD))
    }

    @Test
    void testAnnoElems6() {
        addGroovySource '''\
            |@interface Bar {
            |  String one() default '1'
            |  String two() default '2'
            |}
            |@interface Bars {
            |  Bar[] value()
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar
            |import foo.Bars
            |class C {
            |  @Bars([@Bar(one='x'), @Bar(one = 'y', two = 'z')])
            |  def method() {
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, TAG_KEY),
            new HighlightedTypedPosition(contents.lastIndexOf('one'), 3, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/959
    void testAnnoElems7() {
        String contents = '''\
            |@groovy.transform.EqualsAndHashCode
            |@groovy.transform.AnnotationCollector
            |@interface A {
            |}
            |@A(excludes = 'temporary')
            |class C {
            |  def temporary
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('A '), 1, ANNOTATION),
            new HighlightedTypedPosition(contents.indexOf('excludes'), 8, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('C '), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('temporary'), 9, FIELD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/959
    void testAnnoElems8() {
        addGroovySource '''\
            |@groovy.transform.EqualsAndHashCode
            |@groovy.transform.AnnotationCollector
            |@interface A {
            |}
            |'''.stripMargin(), 'A'

        buildProject()

        String contents = '''\
            |@A(excludes = 'temporary')
            |class C {
            |  def temporary
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('excludes'), 8, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('temporary'), 9, FIELD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/959
    void testAnnoElems9() {
        String contents = '''\
            |@groovy.transform.AutoExternalize(excludes = 'temporary')
            |class C {
            |  def temporary
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('excludes'), 8, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('temporary'), 9, FIELD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1155
    void testAnnoElems10() {
        String contents = '''\
            |import org.codehaus.groovy.control.*
            |class C {
            |  void m() {
            |    @groovy.transform.ASTTest(phase=CANONICALIZATION, value={
            |      assert node.text
            |    })
            |    def var = null
            |
            |    @groovy.transform.ASTTest(phase=CompilePhase.CANONICALIZATION, value={
            |      sourceUnit.comments
            |    })
            |    final val = null
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C {'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('m()'), 1, METHOD),
            //
            new HighlightedTypedPosition(contents.indexOf('phase'), 5, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('CANONICALIZATION'), 16, STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('value'), 5, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('node'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('text'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('var'), 3, VARIABLE),
            //
            new HighlightedTypedPosition(contents.lastIndexOf('phase'), 5, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('CompilePhase'), 12, ENUMERATION),
            new HighlightedTypedPosition(contents.lastIndexOf('CANONICALIZATION'), 16, STATIC_VALUE),
            new HighlightedTypedPosition(contents.lastIndexOf('value'), 5, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('sourceUnit'), 10, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('comments'), 8, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, VARIABLE))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1197
    void testAnnoElems11() {
        String contents = '''\
            |@groovy.transform.AutoImplement(code={
            |  throw new UnsupportedOperationException()
            |})
            |class C implements Iterator {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('code'), 4, TAG_KEY),
            new HighlightedTypedPosition(contents.indexOf('UnsupportedOperationException'), 29, CLASS),
            new HighlightedTypedPosition(contents.indexOf('UnsupportedOperationException'), 29, CTOR_CALL),
            //
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Iterator'), 8, INTERFACE))
    }

    @Test
    void testGString1() {
        String contents = '''\
            |class X {
            |  int i;
            |  def x(int j) {
            |    int k;
            |    "$i + $j + $k"
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('x('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('j)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$i') + 1, 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf(' + '), 3, STRING),
            new HighlightedTypedPosition(contents.indexOf('$j') + 1, 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf(' + '), 3, STRING),
            new HighlightedTypedPosition(contents.indexOf('$k') + 1, 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('"'), '"$i + $j + $k"'.length(), GSTRING))
    }

    @Test
    void testGString2() {
        String contents = '''\
            |class X {
            |  int i;
            |  def x(int j) {
            |    int k;
            |    "${i} + ${j} + ${k}"
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('x('), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('j)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('i}'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf(' + '), 3, STRING),
            new HighlightedTypedPosition(contents.indexOf('j}'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf(' + '), 3, STRING),
            new HighlightedTypedPosition(contents.indexOf('k}'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('"'), '"${i} + ${j} + ${k}"'.length(), GSTRING))
    }

    @Test
    void testGString3() {
        String contents = 'def a = null, b = "/$a/b/c"' // no regex at offset 15

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/'), 1, STRING),
            new HighlightedTypedPosition(contents.indexOf('$a') + 1, 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/b/c'), '/b/c'.length(), STRING),
            new HighlightedTypedPosition(contents.indexOf('"'), '"/$a/b/c"'.length(), GSTRING))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/511
    void testGString4() {
        String contents = '''\
            |import static java.net.URLEncoder.encode
            |def url = "/${encode('head','UTF-8')}/tail"
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('encode'), 'encode'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('url'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/'), '/'.length(), STRING),
            new HighlightedTypedPosition(contents.lastIndexOf('encode'), 'encode'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('/tail'), '/tail'.length(), STRING),
            new HighlightedTypedPosition(contents.indexOf('"'), '"/${encode(\'head\',\'UTF-8\')}/tail"'.length(), GSTRING))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/511
    void testGString5() {
        String contents = '''\
            |import static java.net.URLEncoder.encode
            |@groovy.transform.CompileStatic
            |class X {
            |  def url = "/${encode('head','UTF-8')}/tail"
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('encode'), 'encode'.length(), DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('url'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('/'), '/'.length(), STRING),
            new HighlightedTypedPosition(contents.lastIndexOf('encode'), 'encode'.length(), STATIC_CALL),
            new HighlightedTypedPosition(contents.indexOf('/tail'), '/tail'.length(), STRING),
            new HighlightedTypedPosition(contents.indexOf('"'), '"/${encode(\'head\',\'UTF-8\')}/tail"'.length(), GSTRING))
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
            new HighlightedTypedPosition(contents.indexOf('/$'), '/$a/'.length(), GSTRING),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE))
    }

    @Test
    void testSlashyString4() {
        String contents = 'def a = /a/\ndef b = /${a}/'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/$'), '/${a}/'.length(), GSTRING),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE))
    }

    @Test
    void testSlashyString5() {
        String contents = '/\\/with slash/'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/'), '/\\/with slash/'.length(), REGEXP))
    }

    @Test
    void testSlashyString6() {
        String contents = '/with slash\\//'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('/'), '/with slash\\//'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString1() {
        String contents = '$/\nSlashy String\n/$'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\nSlashy String\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString2() {
        String contents = '$/\nSlashy$ String\n/$'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\nSlashy$ String\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString3() {
        String contents = '$/\nSlashy String$\n/$'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\nSlashy String$\n/$'.length(), REGEXP))
    }

    @Test
    void testMultiLineSlashyString4() {
        String contents = 'def a = /a/\ndef b = $/\n${a}$\n/$'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/') + 2, 1, REGEXP),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('${a}') + 4, 2, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\n${a}$\n/$'.length(), GSTRING))
    }

    @Test
    void testMultiLineSlashyString5() {
        String contents = 'def a = /a/\ndef b = $/\n$a$\n/$'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('/a/'), 3, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('b'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$/') + 2, 1, REGEXP),
            new HighlightedTypedPosition(contents.lastIndexOf('a'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('$a') + 2, 2, REGEXP),
            new HighlightedTypedPosition(contents.indexOf('$/'), '$/\n$a$\n/$'.length(), GSTRING))
    }

    @Test
    void testMultiLineSlashyString6() {
        String contents = '''\
            |def regexp = $/(?x)      # enable whitespace and comments
            |((?:19|20)\\d\\d)        # year (group 1) (non-capture alternation for century)
            |-                        # separator
            |(0[1-9]|1[012])          # month (group 2)
            |-                        # seperator
            |(0[1-9]|[12][0-9]|3[01]) # day (group 3)
            |/$
            |'''.stripMargin()

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
            |def regexp = $/
            |((?:19|20)\\d\\d) # comments are not enabled
            |/$
            |'''.stripMargin()

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
    void testDeprecated1() {
        String contents = '''
            |class C {
            |  @Deprecated def x
            |  def y() { x }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('y'), 1, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, DEPRECATED))
    }

    @Test
    void testDeprecated2() {
        String contents = '''\
            |@Deprecated
            |class C {
            |  C x
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, FIELD))
    }

    @Test
    void testDeprecated3() {
        addGroovySource '''\
            |class Bar {
            |  @Deprecated public static final String CONST = ""
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar
            |Bar.CONST
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.lastIndexOf('Bar'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('CONST'), 'CONST'.length(), DEPRECATED))
    }

    @Test
    void testDeprecated4() {
        addGroovySource '''\
            |@Deprecated
            |class Bar {
            |  public static final String CONST = ""
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar
            |Bar.CONST
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('CONST'), 'CONST'.length(), DEPRECATED))
    }

    @Test
    void testDeprecated5() {
        addJavaSource '''\
            |@Deprecated
            |public class Bar {
            |  public static final String CONST = "";
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = 'foo.Bar.CONST'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('CONST'), 'CONST'.length(), DEPRECATED))
    }

    @Test
    void testDeprecated6() {
        addJavaSource '''\
            |@Deprecated
            |public class Bar {
            |  public static String FIELD = null;
            |  public static Object method() { return null; }
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = 'import foo.Bar.*;'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar'), 3, DEPRECATED))
    }

    @Test
    void testDeprecated7() {
        addJavaSource '''\
            |@Deprecated
            |public class Bar {
            |  public static String FIELD = null;
            |  public static Object method() { return null; }
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar;
            |import static foo.Bar.*;
            |import static foo.Bar.FIELD;
            |import static foo.Bar.method;
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar;'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.*'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.F'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('FIELD'), 5, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.m'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, DEPRECATED))
    }

    @Test
    void testDeprecated8() {
        addJavaSource '''\
            |@Deprecated
            |public class Bar {
            |  public static class Baz {
            |    public static String FIELD = null;
            |    public static Object method() { return null; }
            |  }
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = 'import foo.Bar.Baz.*;'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar'), 3, DEPRECATED))
    }

    @Test
    void testDeprecated9() {
        addJavaSource '''\
            |@Deprecated
            |public class Bar {
            |  public static class Baz {
            |    public static String FIELD = null;
            |    public static Object method() { return null; }
            |  }
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar.Baz;
            |import static foo.Bar.Baz.*;
            |import static foo.Bar.Baz.FIELD;
            |import static foo.Bar.Baz.method;
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar.Baz;'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.Baz.*'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.Baz.F'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('FIELD'), 5, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar.Baz.m'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, DEPRECATED))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/907
    void testDeprecated10() {
        addGroovySource '''\
            |class Foo {
            |  @Deprecated Foo() {}
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Bar extends Foo {
            |  @Deprecated Bar() { super() }
            |  Bar(def something) { this() }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Bar'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Bar()'), 3, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('super'), 5, DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('Bar'), 3, CTOR),
            new HighlightedTypedPosition(contents.indexOf('something'), 9, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('this'), 4, DEPRECATED))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/962
    void testDeprecated11() {
        addGroovySource '''\
            |class Foo {
            |  @Deprecated static def getSomeThing(one, two) {}
            |}
            |'''.stripMargin()

        String contents = '''\
            |import static Foo.getSomeThing as something
            |class Bar {
            |  @groovy.transform.CompileStatic
            |  def baz(x, y) {
            |    something(x, y)
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('getSomeThing'), 12, DEPRECATED),
            new HighlightedTypedPosition(contents.indexOf('Bar'), 3, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('baz'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('x,'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('y)'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('something'), 9, DEPRECATED),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('y'), 1, PARAMETER))
    }

    @Test
    void testNumberWithSuffix() {
        String contents

        contents = ' 11 '
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
    void testNumberWithUnderscore() {
        String contents = ' 0b11010010_01101001_10010100_10010010 '

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('0b'), '0b11010010_01101001_10010100_10010010'.length(), NUMBER))
    }

    @Test
    void testDecimal() {
        String contents = ' 8881.23 '

        assertHighlighting(contents,
            new HighlightedTypedPosition(1, 7, NUMBER))
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
    void testIntRange1() {
        String contents = ' 0..<100 '

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('100'), 3, NUMBER))
    }

    @Test
    void testIntRange2() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |void x() {
            |  for (z in 1..2) {
            |    z
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, METHOD),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('z'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('2'), 1, NUMBER),
            new HighlightedTypedPosition(contents.lastIndexOf('z'), 1, VARIABLE))
    }

    @Test // GRECLIPSE-878
    void testMapKey1() {
        String contents = 'def map = [key: "value"]'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), MAP_KEY))
    }

    @Test
    void testMapKey2() {
        String contents = '''\
            |def key = "key1"
            |def map = [(key): "1", key2: "2", \'key3\': "3", "key4": "4"]
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('key'), 'key'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('map'), 'map'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key)'), 'key'.length(), VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('key2'), 'key2'.length(), MAP_KEY))
    }

    @Test
    void testSpread() {
        String contents = '''\
            |list = []
            |meth(*list)
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('list'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.lastIndexOf('list'), 4, VARIABLE))
    }

    @Test
    void testSpreadMap() {
        String contents = '''\
            |map1 = [:]
            |map2 = [*:map1]
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('map1'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('map2'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('map1'), 4, VARIABLE))
    }

    @Test
    void testUseBlock() {
        String contents = '''\
            |use (groovy.time.TimeCategory) {
            |  new Date().getDaylightSavingsOffset()
            |  1.minute.from.now
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('use'), 3, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('TimeCategory'), 12, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('getDaylightSavingsOffset'), 24, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('minute'), 6, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('from'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('now'), 3, METHOD_CALL))
    }

    @Test
    void testWithBlock1() {
        addGroovySource '''\
            |class Foo {
            |  String val
            |}
            |'''.stripMargin()

        String contents = '''\
            |new Foo().with {
            |  val = ''
            |  val.length()
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('length'), 6, METHOD_CALL))
    }

    @Test
    void testWithBlock2() {
        addGroovySource '''\
            |class Foo {
            |  String val
            |}
            |'''.stripMargin()

        String contents = '''\
            |@groovy.transform.CompileStatic
            |def bar(Foo foo) {
            |  foo.with {
            |    val = ''
            |    val.length()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Foo'), 3, CLASS),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('length'), 6, METHOD_CALL))
    }

    @Test
    void testWithBlock3() {
        String contents = '''\
            |class X { static {
            |  new Date().with {
            |    setTime(1234L)
            |    time = 5678L
            |    not1
            |    not2 = hours
            |  }
            |}}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'), 5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('time'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('5678L'), 5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('not1'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('not2'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('hours'), 5, DEPRECATED))
    }

    @Test
    void testWithBlock4() {
        String contents = '''\
            |@groovy.transform.TypeChecked
            |class X { static {
            |  new Date().with {
            |    setTime(1234L)
            |    time = 5678L
            |    not1
            |    not2 = hours
            |  }
            |}}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'), 5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('time'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('5678L'), 5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('not1'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('not2'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('hours'), 5, DEPRECATED))
    }

    @Test
    void testWithBlock5() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class X { static {
            |  new Date().with {
            |    setTime(1234L)
            |    time = 5678L
            |    not1
            |    not2 = hours
            |  }
            |}}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Date'), 4, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.indexOf('setTime'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('1234L'), 5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('time'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('5678L'), 5, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('not1'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('not2'), 4, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('hours'), 5, DEPRECATED))
    }

    @Test
    void testWithBlock6() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class X {
            |  def getReadOnly() {}
            |  static {
            |    new X().with {
            |      def val = readOnly
            |      readOnly = []
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('getReadOnly'), 11, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('readOnly'), 8, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('readOnly'), 8, UNKNOWN))
    }

    @Test
    void testWithBlock7() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |class X {
            |  void setWriteOnly(value) {}
            |  static {
            |    new X().with {
            |      def val = writeOnly
            |      writeOnly = []
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('setWriteOnly'), 12, METHOD),
            new HighlightedTypedPosition(contents.indexOf('value'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('X'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('val'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('writeOnly'), 9, UNKNOWN),
            new HighlightedTypedPosition(contents.lastIndexOf('writeOnly'), 9, METHOD_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1015
    void testWithBlock8() {
        addGroovySource '''\
            |class Pogo {
            |  String prop
            |  void setProp(String value) {}
            |}
            |'''.stripMargin()

        // 'prop' references on left-hand side of compound assignment should infer as setter
        String contents = '''\
            |@groovy.transform.CompileStatic
            |void meth(Pogo pogo) {
            |  pogo.prop += 'x'
            |  pogo.with {
            |    prop += 'x'
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('Pogo'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('pogo'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('pogo.'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('prop'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('pogo.'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('with'), 4, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('prop'), 4, METHOD_CALL))
    }

    @Test
    void testLazyInitExpr1() {
        String contents = '''\
            |class X {
            |  String x
            |  @Lazy Collection y = [x]
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('x'), 1, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Collection'), 10, INTERFACE),
            new HighlightedTypedPosition(contents.lastIndexOf('y'), 1, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('x'), 1, FIELD))
    }

    @Test
    void testLazyInitExpr2() {
        addGroovySource '''\
            |class Directory {
            |  static Object lookup(String id) {
            |    null
            |  }
            |}
            |'''.stripMargin()

        String contents = '''\
            |@groovy.transform.CompileStatic
            |class X {
            |  String id
            |  @Lazy Object thing = { ->
            |    Directory.lookup(id)
            |  }()
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('id'), 2, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('thing'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('Directory'), 9, CLASS),
            new HighlightedTypedPosition(contents.indexOf('lookup'), 6, STATIC_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('id'), 2, FIELD))
    }

    @Test
    void testFieldInitExpr() {
        addGroovySource '''\
            |abstract class A {
            |  protected final String field
            |  protected A(String field) {
            |    this.field = field
            |  }
            |}
            |'''.stripMargin()

        String contents = '''\
            |class B extends A {
            |  Map map = [key: field] // init added to ctor body, where "field" refers to param
            |  B(String field) {
            |    super(field)
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('B'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('A'), 1, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('Map'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('map'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('key'), 3, MAP_KEY),
            new HighlightedTypedPosition(contents.indexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('B('), 1, CTOR),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('field)'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('field'), 5, PARAMETER))
    }

    @Test
    void testMethodPointer1() {
        String contents = '''\
            |def x = ''.&length
            |def y = Math.&random
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('x'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('y'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('length'), 6, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('Math'), 4, CLASS),
            new HighlightedTypedPosition(contents.indexOf('random'), 6, STATIC_CALL))
    }

    @Test
    void testMethodPointer2() {
        String contents = '''\
            |def s = 'SoMeThInG'
            |def f = s.&toLowerCase
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('s ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('f ='), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('s.&'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('toLowerCase'), 'toLowerCase'.length(), METHOD_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/878
    void testMethodPointer3() {
        String contents = 'String.&length'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('length'), 6, isAtLeastGroovy(30) ? METHOD_CALL : UNKNOWN))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1192
    void testMethodPointer4() {
        String contents = '''\
            |String.&size
            |Object.&sleep
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('size'  ), 4, isAtLeastGroovy(30) ? GROOVY_CALL : UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('sleep' ), 5, isAtLeastGroovy(30) ? GROOVY_CALL : UNKNOWN))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1048
    void testMethodPointer5() {
        String contents = 'String[].&new'

        if (isAtLeastGroovy(30)) {
            assertHighlighting(contents,
                new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS))
        } else {
            assertHighlighting(contents,
                new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
                new HighlightedTypedPosition(contents.indexOf('new'   ), 3, UNKNOWN))
        }
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1048
    void testMethodReference1() {
        assumeTrue(isParrotParser())

        String contents = 'String[]::new'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/878
    void testMethodReference2() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |String::length
            |Thread::yield
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('length'), 6, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('Thread'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('yield' ), 5, STATIC_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1192
    void testMethodReference3() {
        assumeTrue(isParrotParser())

        String contents = '''\
            |String::size
            |Object::sleep
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('size'  ), 4, isAtLeastGroovy(30) ? GROOVY_CALL : UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('sleep' ), 5, isAtLeastGroovy(30) ? GROOVY_CALL : UNKNOWN))
    }

    @Test
    void testMethodOverloads1() {
        // overloads with generics caused confusion in TypeInferencingVisitor
        String contents = '''\
            |class X {
            |  def findSomething(String string, Set<CharSequence> strings) {
            |  }
            |  protected def findSomething(Map<String, ? extends Object> inputs) {
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('findSomething'), 'findSomething'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Set'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('CharSequence'), 'CharSequence'.length(), INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('strings'), 7, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('findSomething'), 'findSomething'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Map'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('inputs'), 6, PARAMETER))
    }

    @Test
    void testMethodOverloads2() {
        String contents = '''\
            |class X {
            |  void meth(String one, Object two, ... three) {
            |    def var = three
            |  }
            |  void meth(String one, Object two) {
            |    def var = two
            |  }
            |  void meth(String one) {
            |    def var = one
            |  }
            |}
            |'''.stripMargin()

        int m1 = contents.indexOf('meth')
        int m2 = contents.indexOf('meth', m1 + 4)
        int m3 = contents.indexOf('meth', m2 + 4)

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(m1, 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('two'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('three'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('var'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('three', contents.indexOf('var')), 5, PARAMETER),
            new HighlightedTypedPosition(m2, 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String', m2), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one', m2), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Object', m2), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('two', m2), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('var', m2), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('two', contents.indexOf('var', m2)), 3, PARAMETER),
            new HighlightedTypedPosition(m3, 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String', m3), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('one', m3), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('var', m3), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('one', contents.indexOf('var', m3)), 3, PARAMETER))
    }

    @Test
    void testCategoryMethodOverloads() {
        // implicit 'self' parameter added by transformation caused confusion in TypeInferencingVisitor
        String contents = '''\
            |import java.util.regex.Pattern
            |@Category(Number)
            |class X {
            |    void method(String string) {
            |      println this
            |    }
            |    void method(Pattern regex) {
            |      println this
            |    }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Number'), 6, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('println'), 7, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('method'), 6, STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('Pattern'), 7, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('regex'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('println'), 7, GROOVY_CALL))
    }

    @Test
    void testMemoizedMethod() {
        String contents = '''\
            |import groovy.transform.*
            |class X {
            |  @Memoized
            |  def objectMethod(Object param) {
            |    'prefix' + param;
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param)'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('param;'), 5, PARAMETER))
    }

    @Test
    void testMemoizedStatic() {
        String contents = '''\
            |import groovy.transform.*
            |class X {
            |  @Memoized
            |  static def staticMethod(Object param) {
            |    param + 'suffix'
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 5, PARAMETER))
    }

    @Test
    void testSynchedMethods() {
        String contents = '''\
            |import groovy.transform.*
            |class X {
            |  @Synchronized
            |  def method1(Object param1) {
            |    int i;
            |  }
            |  @WithReadLock
            |  def method2(Object param2) {
            |    int j;
            |  }
            |  @WithWriteLock
            |  def method3(Object param3) {
            |    int k;
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('method1'), 'method1'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param1'), 'param1'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('method2'), 'method2'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object', contents.indexOf('method2')), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param2'), 'param2'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('method3'), 'method2'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object', contents.indexOf('method3')), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param3'), 'param3'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('k;'), 1, VARIABLE))
    }

    @Test @Ignore('list and count have been transformed into something, which hampers refactoring and semantic highlighting')
    void testTailCallMethods() {
        String contents = '''\
            |import groovy.transform.*
            |class X {
            |  @TailRecursive
            |  int sizeOfList(List list, int count = 0) {
            |    if (list.isEmpty()) {
            |      count;
            |    } else {
            |      sizeOfList(list.tail(), count + 1);
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('sizeOfList'), 'sizeOfList'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('list'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('count'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('list.'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('isEmpty'), 'isEmpty'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('count;'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('sizeOfList'), 'sizeOfList'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('list.'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('tail'), 4, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('count'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('1'), 1, NUMBER))
    }

    @Test
    void testTypeCheckedMethods() {
        String contents = '''\
            |import groovy.transform.*
            |class X {
            |  @TypeChecked
            |  def objectMethod(Object param) {
            |    int i;
            |  }
            |  @TypeChecked
            |  static def staticMethod(Object param) {
            |    int j;
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 'param'.length(), PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE))
    }

    @Test
    void testStaticCompiledMethods() {
        String contents = '''\
            |import groovy.transform.*
            |@CompileStatic
            |class X {
            |  def objectMethod(Object param) {
            |    int i;
            |  }
            |  static def staticMethod(Object param) {
            |    int j;
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('X'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('objectMethod'), 'objectMethod'.length(), METHOD),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('i;'), 1, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('staticMethod'), 'staticMethod'.length(), STATIC_METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('j;'), 1, VARIABLE))
    }

    @Test
    void testSpockStyleMethods() {
        // make sure these retain string highlighting
        assertHighlighting('class X { def "test case name"() {} }', new HighlightedTypedPosition(6, 1, CLASS))
        assertHighlighting('class X { def \'test case name\'() {} }', new HighlightedTypedPosition(6, 1, CLASS))
        assertHighlighting('class X { def """test case name"""() {} }', new HighlightedTypedPosition(6, 1, CLASS))
        assertHighlighting('class X { def \'\'\'test case name\'\'\'() {} }', new HighlightedTypedPosition(6, 1, CLASS))
    }

    @Test
    void testAliasType1() {
        String contents = '''\
            |import java.util.Map as Table
            |class C {
            |  def meth(key = 'x', value = 'y', Table table) {
            |    return table.getOrDefault(key, value)
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('key'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('value'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Table', contents.indexOf('C')), 5, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('table'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('table'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('getOrDefault'), 'getOrDefault'.length(), METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('key'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('value'), 5, PARAMETER))
    }

    @Test
    void testAliasType2() {
        addGroovySource 'enum PoorlyNamed {}', 'PoorlyNamed', 'foo'

        String contents = '''\
            |import foo.PoorlyNamed as Field
            |class C {
            |  void meth(value = null, Field... fields) {
            |    println value
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('value'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Field', contents.indexOf('C')), 5, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('fields'), 6, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('println'), 7, GROOVY_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('value'), 5, PARAMETER))
    }

    @Test
    void testAliasType3() {
        addGroovySource '''\
            |class Bar {
            |  enum Inner {
            |    ONE, TWO
            |  }
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar as Outer
            |class C {
            |  def meth(int idx = -1, obj = null, Outer.Inner inner) {
            |    if (idx > 0) {
            |        inner
            |    } else {
            |        obj
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('idx'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('-1'), 2, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('obj'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('Outer', contents.indexOf('null')), 5, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Inner'), 5, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('inner'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('idx'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.lastIndexOf('inner'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('obj'), 3, PARAMETER))
    }

    @Test
    void testAliasType4() {
        addGroovySource '''\
            |class Bar {
            |  enum Inner {
            |    ONE, TWO
            |  }
            |}
            |'''.stripMargin(), 'Bar', 'foo'

        String contents = '''\
            |import foo.Bar.Inner as Baz
            |class C {
            |  def meth(int idx = -1, obj = null, Baz baz) {
            |    if (idx > 0) {
            |        baz
            |    } else {
            |        obj
            |    }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('idx'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('-1'), 2, NUMBER),
            new HighlightedTypedPosition(contents.indexOf('obj'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('Baz'), 3, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('baz'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('idx'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('0'), 1, NUMBER),
            new HighlightedTypedPosition(contents.lastIndexOf('baz'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('obj'), 3, PARAMETER))
    }

    @Test
    void testLocalType() {
        addGroovySource '''\
            |interface I<T> extends Serializable {
            |  T bar()
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Whatever {
            |  def foo = new I<String>() {
            |    private static final long serialVersionUID = 123
            |    String bar() { 'baz' }
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Whatever'), 8, CLASS),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('I'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('I'), 1, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('serialVersionUID'), 'serialVersionUID'.length(), STATIC_VALUE),
            new HighlightedTypedPosition(contents.indexOf('123'), 3, NUMBER),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, METHOD))
    }

    @Test
    void testGenericType1() {
        String contents = '''\
            |interface I<T> {
            |  T bar()
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('I'), 1, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('T'), 1, PLACEHOLDER),
            new HighlightedTypedPosition(contents.lastIndexOf('T'), 1, PLACEHOLDER),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, METHOD))
    }

    @Test
    void testGenericType2() {
        String contents = 'Class<? extends List<? extends CharSequence>> clazz'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Class'), 5, CLASS),
            new HighlightedTypedPosition(contents.indexOf('List'), 4, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('CharSequence'), 12, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('clazz'), 5, VARIABLE))
    }

    @Test
    void testQualifiedType1() {
        addGroovySource '''\
            |class C {
            |  enum E {
            |    ITEM
            |  }
            |}
            |'''.stripMargin()

        String contents = 'def item = C.E.ITEM'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('item'), 4, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('E'), 1, ENUMERATION),
            new HighlightedTypedPosition(contents.indexOf('ITEM'), 4, STATIC_VALUE))
    }

    @Test
    void testQualifiedType2() {
        String contents = 'Map.Entry<String, Object> entry'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Map'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('Entry'), 5, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('entry'), 5, VARIABLE))
    }

    @Test
    void testQualifiedType3() {
        String contents = 'java.util.Map.Entry<String, Object> entry'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Map'), 3, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('Entry'), 5, INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('Object'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('entry'), 5, VARIABLE))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/929
    void testQualifiedType4() {
        String contents = 'def sdf = new java.text.SimpleDateFormat("MM.dd.yyyy")'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('sdf'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('SimpleDateFormat'), 'SimpleDateFormat'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('SimpleDateFormat'), 'SimpleDateFormat'.length(), CTOR_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/929
    void testQualifiedType5() {
        String contents = '''\
            |def foo = new java.lang.Runnable() {
            |  @Override void run() {}
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Runnable'), 'Runnable'.length(), INTERFACE),
            new HighlightedTypedPosition(contents.indexOf('Runnable'), 'Runnable'.length(), CTOR_CALL),
            new HighlightedTypedPosition(contents.indexOf('run'), 3, METHOD))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/930
    void testQualifiedType6() {
        String contents = 'def pat = java.util.regex.Pattern.compile("abc")'

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('pat'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('Pattern'), 'Pattern'.length(), CLASS),
            new HighlightedTypedPosition(contents.indexOf('compile'), 'compile'.length(), STATIC_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1042
    void testQualifiedType7() {
        String contents = '''\
            |if (obj instanceof java.lang.String) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('obj'), 3, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1042
    void testQualifiedType8() {
        String contents = '''\
            |if (obj instanceof java.lang.String [] ) {
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('obj'), 3, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS))
    }

    @Test
    void testTraits1() {
        String contents = '''\
            |trait Whatever {
            |  private String field
            |  String property
            |  def method() {
            |    field + property + getProperty() + Math.PI
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Whatever'), 8, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('getProperty'), 11, METHOD_CALL),
            new HighlightedTypedPosition(contents.indexOf('Math'), 4, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('PI'), 2, STATIC_VALUE))
    }

    @Test
    void testTraits2() {
        String contents = '''\
            |trait Whatever {
            |  private String field; String property
            |  private String method(String param) {
            |    "$field $param $property $unknown"
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Whatever'), 8, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.indexOf('String', contents.indexOf('field')), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.indexOf('String', contents.indexOf('property')), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String', contents.indexOf('method')), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('field'), 5, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('field') + 5, 1, STRING),
            new HighlightedTypedPosition(contents.lastIndexOf('param'), 5, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('param') + 5, 1, STRING),
            new HighlightedTypedPosition(contents.lastIndexOf('property'), 8, FIELD),
            new HighlightedTypedPosition(contents.lastIndexOf('property') + 8, 1, STRING),
            new HighlightedTypedPosition(contents.lastIndexOf('unknown'), 7, UNKNOWN),
            new HighlightedTypedPosition(contents.indexOf('"'), '"$field $param $property $unknown"'.length(), GSTRING))
    }

    @Test
    void testTraits3() {
        String contents = '''\
            |trait T {
            |  String string
            |  void method() {
            |    this.string
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('string'), 6, FIELD))
    }

    @Test
    void testTraits4() {
        String contents = '''\
            |trait T {
            |  String string
            |  void method() {
            |    this.getString()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('getString'), 9, METHOD_CALL))
    }

    @Test
    void testTraits5() {
        String contents = '''\
            |trait T {
            |  String string
            |  void method() {
            |    string = ''
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('string'), 6, FIELD))
    }

    @Test
    void testTraits6() {
        String contents = '''\
            |trait T {
            |  String string
            |  void method() {
            |    this.string = ''
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('string'), 6, FIELD))
    }

    @Test
    void testTraits7() {
        String contents = '''\
            |trait T {
            |  String string
            |  void method() {
            |    this.setString('')
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('String'), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('string'), 6, FIELD),
            new HighlightedTypedPosition(contents.indexOf('method'), 6, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('setString'), 9, METHOD_CALL))
    }

    @Test
    void testTraits8() {
        addGroovySource '''\
            |trait T {
            |  String getFoo() { 'foo' }
            |}
            |abstract class A implements T {
            |}
            |'''.stripMargin()

        String contents = '''\
            |class C extends A {
            |  static void main(args) {
            |    new C().foo
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('A'), 1, ABSTRACT_CLASS),
            new HighlightedTypedPosition(contents.indexOf('main'), 4, STATIC_METHOD),
            new HighlightedTypedPosition(contents.indexOf('args'), 4, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CTOR_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, METHOD_CALL))
    }

    @Test
    void testTraits9() {
        String contents = '''\
            |trait T {
            |  def whatever() {}
            |}
            |class C implements T {
            |  void meth() {
            |    T.super.whatever()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T {'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('whatever'), 8, METHOD),

            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('T {'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.lastIndexOf('whatever'), 8, METHOD_CALL))
    }

    @Test
    void testTraits10() {
        String contents = '''\
            |trait T {
            |  def foo
            |  void setBar(bar) {}
            |}
            |class C implements T {
            |  void meth() {
            |    T.super.foo = null
            |    T.super.bar = null
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('T {'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('foo'), 3, FIELD),
            new HighlightedTypedPosition(contents.indexOf('setBar'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('bar'), 3, PARAMETER),

            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.lastIndexOf('T {'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('meth'), 4, METHOD),
            new HighlightedTypedPosition(contents.indexOf('T.'), 1, TRAIT),
            new HighlightedTypedPosition(contents.lastIndexOf('foo'), 3, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.lastIndexOf('bar'), 3, METHOD_CALL))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/961
    void testTraits11() {
        // http://docs.groovy-lang.org/latest/html/documentation/#_semantics_of_super_inside_a_trait
        String contents = '''\
            |trait Filtering {
            |  StringBuilder append(String str) {
            |    def sub = str.replace('o', '')
            |    super.append(sub)
            |  }
            |  String toString() {
            |    super.toString()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('Filtering'), 9, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('StringBuilder'), 13, CLASS),
            new HighlightedTypedPosition(contents.indexOf('append'), 6, METHOD),
            new HighlightedTypedPosition(contents.indexOf('String '), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('str'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.indexOf('sub'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.indexOf('replace'), 7, METHOD_CALL),
            new HighlightedTypedPosition(contents.lastIndexOf('str'), 3, PARAMETER),
            new HighlightedTypedPosition(contents.lastIndexOf('append'), 6, UNKNOWN),
            new HighlightedTypedPosition(contents.lastIndexOf('sub'), 3, VARIABLE),
            new HighlightedTypedPosition(contents.lastIndexOf('String '), 6, CLASS),
            new HighlightedTypedPosition(contents.indexOf('toString'), 8, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('toString'), 8, UNKNOWN))
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1159
    void testTraits12() {
        addGroovySource '''\
            |package p
            |trait T {
            |  Number getFoo() { 'foo' }
            |}
            |'''.stripMargin(), 'T', 'p'
        buildProject()

        String contents = '''\
            |class C implements p.T {
            |  void test() {
            |    p.T.super.getFoo()
            |  }
            |}
            |'''.stripMargin()

        assertHighlighting(contents,
            new HighlightedTypedPosition(contents.indexOf('C'), 1, CLASS),
            new HighlightedTypedPosition(contents.indexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.indexOf('test'), 4, METHOD),
            new HighlightedTypedPosition(contents.lastIndexOf('T'), 1, TRAIT),
            new HighlightedTypedPosition(contents.lastIndexOf('getFoo'), 6, METHOD_CALL))
    }

    //
    private int counter

    private void assertHighlighting(String contents, HighlightedTypedPosition... expectedPositions) {
        def references = new GatherSemanticReferences(
            addGroovySource(contents, "Highlighting${++counter}"))
        references.factory = new TypeInferencingVisitorFactory() {
            @Override
            TypeInferencingVisitorWithRequestor createVisitor(GroovyCompilationUnit gcu) {
                def visitor = super.createVisitor(gcu)
                visitor.debug = true // enable checks
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
