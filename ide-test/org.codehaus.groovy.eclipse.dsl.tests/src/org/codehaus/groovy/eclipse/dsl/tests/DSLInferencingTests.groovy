/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.codehaus.groovy.eclipse.test.TestProject
import org.eclipse.jdt.groovy.core.util.GroovyUtils
import org.junit.Before
import org.junit.Test

/**
 * Tests type inferencing that involve DSLs.
 */
final class DSLInferencingTests extends DSLInferencingTestSuite {

    @Before
    void setUp() {
        addPlainText(getTestResourceContents('SomeInterestingExamples.dsld'), 'SomeInterestingExamples.dsld')
        buildProject()
    }

    @Test
    void testRegisteredPointcut1() {
        String contents = '2.phat'

        assert inferType(contents, 'phat').declaringTypeName == 'java.lang.Integer'
    }

    @Test
    void testRegisteredPointcut2() {
        String contents = '2.valueInteger'

        assert inferType(contents, 'valueInteger').declaringTypeName == 'java.lang.Integer'
    }

    @Test
    void testContiribution1() {
        createDsls('contribute(currentType("Foo")) { delegatesTo "Other" }')

        String contents = '''\
            |class Foo {}
            |class Other {
            |  Class<String> blar() { }
            |}
            |new Foo().blar()
            |'''.stripMargin()

        inferType(contents, 'blar').with {
            assert declaringTypeName == 'Other'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test
    void testContiribution2() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_FIRST) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      owner
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'owner').typeName == 'Foo'
    }

    @Test
    void testContiribution2a() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_FIRST) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      delegate
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'delegate').typeName == 'java.lang.String'
    }

    @Test
    void testContiribution2b() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_FIRST) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      something
            |    }
            |  }
            |}
            |'''.stripMargin()

        inferType(contents, 'something').with {
            assert typeName == 'java.lang.Runnable'
            assert declaringTypeName == 'Foo' // aka owner
        }
    }

    @Test
    void testContiribution3() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_FIRST) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      owner
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'owner').typeName == 'Foo'
    }

    @Test
    void testContiribution3a() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_FIRST) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      delegate
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'delegate').typeName == 'java.lang.String'
    }

    @Test
    void testContiribution3b() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_FIRST) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      something
            |    }
            |  }
            |}
            |'''.stripMargin()

        inferType(contents, 'something').with {
            assert typeName == 'java.lang.Runnable'
            assert declaringTypeName == 'Foo' // aka owner
        }
    }

    @Test
    void testContiribution4() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_ONLY) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      owner
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'owner').typeName == 'Foo'
    }

    @Test
    void testContiribution4a() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_ONLY) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      delegate
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'delegate').typeName == 'java.lang.String'
    }

    @Test
    void testContiribution4b() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_ONLY) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      something
            |    }
            |  }
            |}
            |'''.stripMargin()

        inferType(contents, 'something').with {
            assert typeName == 'java.lang.Runnable'
            assert declaringTypeName == 'Foo' // aka owner
        }
    }

    @Test
    void testContiribution5() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_ONLY) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      owner
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'owner').typeName == 'Foo'
    }

    @Test
    void testContiribution5a() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_ONLY) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      delegate
            |    }
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'delegate').typeName == 'java.lang.String'
    }

    @Test
    void testContiribution5b() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')

        String contents = '''\
            |class Foo {
            |  def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_ONLY) Closure block) {
            |  }
            |  def baz() {
            |    bar {
            |      something
            |    }
            |  }
            |}
            |'''.stripMargin()

        inferType(contents, 'something').with {
            assert declaringTypeName == 'Foo'
            assert result.confidence.name() == 'UNKNOWN'
        }
    }

    @Test
    void testDelegatesTo1() {
        createDsls('contribute(currentType("Foo")) { delegatesTo "Other" }')

        String contents = '''\
            |class Foo { }
            |class Other {
            |  Class<String> blar() { }
            |}
            |new Foo().blar()
            |'''.stripMargin()

        inferType(contents, 'blar').with {
            assert declaringTypeName == 'Other'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test
    void testDelegatesTo2() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other" }')

        String contents = '''
            |class Foo { }
            |class Other {
            |  Class<String> blar() { }
            |}
            |new Foo().blar()
            '''.stripMargin()

        inferType(contents, 'blar').with {
            assert declaringTypeName == 'Other'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test
    void testDelegatesTo3() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other" }')

        String contents = '''
            |class Foo { }
            |class Other {
            |  Class<String> blar() { }
            |}
            |Foo.blar()
            '''.stripMargin()

        inferType(contents, 'blar').with {
            assert declaringTypeName == 'Foo'
            assert result.confidence.name() == 'UNKNOWN' // because accessing in static context
        }
    }

    @Test
    void testDelegatesTo4() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", isStatic:true }')

        String contents = '''
            |class Foo { }
            |class Other {
            |  Class<String> blar() { }
            |}
            |Foo.blar()
            '''.stripMargin()

        inferType(contents, 'blar').with {
            assert declaringTypeName == 'Other'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test
    void testDelegatesTo5() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", asCategory:true }')

        String contents = '''\
            |class Foo { }
            |class Other {
            |  Class<String> blar(Foo x) { }
            |  Class<String> flar() { }
            |}
            |new Foo().blar()
            |new Foo().flar()
            |'''.stripMargin()

        inferType(contents, 'blar').with {
            assert declaringTypeName == 'Other'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test
    void testDelegatesTo5a() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", asCategory:true }')

        String contents = '''\
            |class Foo { }
            |class Other {
            |  Class<String> blar(Foo x) { }
            |  Class<String> flar() { }
            |}
            |new Foo().blar()
            |new Foo().flar()
            |'''.stripMargin()

        inferType(contents, 'flar').with {
            assert declaringTypeName == 'Foo'
            assert result.confidence.name() == 'UNKNOWN'
        }
    }

    @Test
    void testDelegatesTo6() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", except: ["glar"]}')

        String contents = '''\
            |class Foo {
            |  Class<String> glar() { }
            |}
            |class Other {
            |  Class<String> blar() { }
            |  Class<String> flar() { }
            |  Class<String> glar() { }
            |}
            |new Foo().flar()
            |new Foo().glar()
            |'''.stripMargin()

        inferType(contents, 'glar').with {
            assert declaringTypeName == 'Foo'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test
    void testDelegatesTo6a() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", except: ["glar"]}')

        String contents = '''\
            |class Foo {
            |  Class<String> glar() { }
            |}
            |class Other {
            |  Class<String> blar() { }
            |  Class<String> flar() { }
            |  Class<String> glar() { }
            |}
            |new Foo().flar()
            |new Foo().glar()
            |'''.stripMargin()

        inferType(contents, 'flar').with {
            assert declaringTypeName == 'Other'
            assert typeName == 'java.lang.Class<java.lang.String>'
        }
    }

    @Test // GRECLIPSE-1321
    void testDelegatesTo7() {
        createDsls('contribute(currentType(String)) { delegatesTo "Type" }')

        String contents = '''\
            |interface Type {
            |  String getFoo();
            |  int foo(arg);
            |}
            |"".getFoo()
            |'''.stripMargin()

        assert inferType(contents, 'getFoo').typeName == 'java.lang.String'
    }

    @Test // GRECLIPSE-1321
    void testDelegatesTo7a() {
        createDsls('contribute(currentType(String)) { delegatesTo "Type" }')

        String contents = '''\
            |interface Type {
            |  String getFoo();
            |  int foo(arg);
            |}
            |"".foo()
            |'''.stripMargin()

        assert inferType(contents, 'foo').typeName == 'java.lang.Integer'
    }

    @Test // GRECLIPSE-1442
    void testDelegatesTo8() {
        createDsls('contribute(currentType("Delegatee")) { delegatesTo type:"MyCategory", asCategory:true }')

        String contents = '''\
            |class MyCategory {
            |  static int getSomething(Delegatee d) { }
            |}
            |class Delegatee { }
            |new Delegatee().getSomething()
            |'''.stripMargin()

        assert inferType(contents, 'getSomething').typeName == 'java.lang.Integer'
    }

    @Test // GRECLIPSE-1442
    void testDelegatesTo8a() {
        createDsls('contribute(currentType("Delegatee")) { delegatesTo type:"MyCategory", asCategory:true }')

        String contents = '''\
            |class MyCategory {
            |  static int getSomething(Delegatee d) { }
            |}
            |class Delegatee { }
            |new Delegatee().something
            |'''.stripMargin()

        assert inferType(contents, 'something').typeName == 'java.lang.Integer'
    }

    @Test // GRECLIPSE-1442
    void testDelegatesTo8b() {
        createDsls('contribute(currentType("Delegatee")) { delegatesTo type:"MyCategory", asCategory:true }')

        String contents = '''\
            |class MyCategory {
            |  static boolean isSomething(Delegatee d) { }
            |}
            |class Delegatee { }
            |new Delegatee().isSomething()
            |'''.stripMargin()

        assert inferType(contents, 'isSomething').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1442
    void testDelegatesTo8c() {
        createDsls('contribute(currentType("Delegatee")) { delegatesTo type:"MyCategory", asCategory:true }')

        String contents = '''\
            |class MyCategory {
            |  static boolean isSomething(Delegatee d) { }
            |}
            |class Delegatee { }
            |new Delegatee().something
            |'''.stripMargin()

        assert inferType(contents, 'something').typeName == 'java.lang.Boolean'
    }

    @Test
    void testSetDelegateType1() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { delegate }
            |'''.stripMargin()

        assert inferType(contents, 'delegate').typeName == 'Type'
    }

    @Test
    void testSetDelegateType2() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { this }
            |'''.stripMargin()

        assert inferType(contents, 'this').typeName.startsWith('TestUnit')
    }

    @Test
    void testSetDelegateType3() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  String getFoo()
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { getFoo() }
            |'''.stripMargin()

        assert inferType(contents, 'getFoo').typeName == 'java.lang.String'
    }

    @Test
    void testSetDelegateType4() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { FOO }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').typeName == 'java.lang.Integer'
    }

    @Test
    void testSetDelegateType5() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { delegate.FOO }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').typeName == 'java.lang.Integer'
    }

    @Test
    void testSetDelegateType6() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { ''.FOO }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testSetDelegateType7() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { delegate.with { FOO } }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').typeName == 'java.lang.Integer'
    }

    @Test
    void testSetDelegateType8() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { 1.FOO }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testSetDelegateType9() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { 1.with { FOO } }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').typeName == 'java.lang.Integer'
    }

    @Test
    void testSetDelegateType10() {
        createDsls('''\
            |contribute(inClosure() & currentType(String)) {
            |  setDelegateType('Type')
            |}
            |'''.stripMargin())

        String contents = '''\
            |interface Type {
            |  int FOO
            |}
            |void meth(@DelegatesTo(String) Closure cl) {
            |}
            |meth { this.FOO }
            |'''.stripMargin()

        assert inferType(contents, 'FOO').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testGenerics1() {
        createDsls('contribute(currentType("Foo")) { property name:"fooProp", type:"List<Class<Foo>>" }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        assert inferType(contents, 'fooProp').typeName == 'java.util.List<java.lang.Class<Foo>>'
    }

    @Test
    void testGenerics2() {
        createDsls('contribute(currentType("Foo")) { property name:"fooProp", type:"List<Class<Foo>>" }')

        String contents = '''\
            |class Foo {
            |}
            |def x = new Foo().fooProp[0]
            |x
            |'''.stripMargin()

        assert inferType(contents, 'x').typeName == 'java.lang.Class<Foo>'
    }

    @Test
    void testGenerics3() {
        createDsls('contribute(currentType("Foo")) { property name:"fooProp", type:"Map<Integer,Long>" }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        assert inferType(contents, 'fooProp').typeName == 'java.util.Map<java.lang.Integer,java.lang.Long>'
    }

    @Test
    void testDeprecated1() {
        createDsls('contribute(currentType("Foo")) { property name:"fooProp", type:"Map<Integer,Long>", isDeprecated:true }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        inferType(contents, 'fooProp').with {
            assert GroovyUtils.isDeprecated(result.declaration)
            assert typeName == 'java.util.Map<java.lang.Integer,java.lang.Long>'
        }
    }

    @Test
    void testDeprecated2() {
        createDsls('contribute(currentType("Foo")) { method name:"fooProp", type:"Map<Integer,Long>", isDeprecated:true }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        inferType(contents, 'fooProp').with {
            assert GroovyUtils.isDeprecated(result.declaration)
            assert typeName == 'java.util.Map<java.lang.Integer,java.lang.Long>'
        }
    }

    @Test
    void testAssertVersion1() {
        createDsls('assertVersion(groovyEclipse:"9.5.9")\n' +
            'contribute(currentType("Foo")) { property name:"fooProp", type:"Map<Integer,Long>" }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        // script should not be executed
        assert inferType(contents, 'fooProp').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testAssertVersion2() {
        createDsls('assertVersion(groovyEclipse:"1.5.9")\n' +
            'contribute(currentType("Foo")) { property name:"fooProp", type:"Map<Integer,Long>" }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        // script should be executed
        assert inferType(contents, 'fooProp').typeName == 'java.util.Map<java.lang.Integer,java.lang.Long>'
    }

    @Test
    void testSupportsVersion1() {
        createDsls('if (supportsVersion(groovyEclipse:"9.5.9"))\n' +
            '  contribute(currentType("Foo")) { property name:"fooProp", type:"Map<Integer,Long>" }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        assert inferType(contents, 'fooProp').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testSupportsVersion2() {
        createDsls('if (supportsVersion(groovyEclipse:"1.5.9"))\n' +
                '  contribute(currentType("Foo")) { property name:"fooProp", type:"Map<Integer,Long>" }')

        String contents = '''\
            |class Foo {
            |}
            |new Foo().fooProp
            |'''.stripMargin()

        assert inferType(contents, 'fooProp').typeName == 'java.util.Map<java.lang.Integer,java.lang.Long>'
    }

    @Test
    void testEnclosingCall1() {
        createDsls('contribute(enclosingCall(name("foo")) & isThisType()) { property name:"yes", type:Double }')

        String contents = 'foo( yes )'

        assert inferType(contents, 'yes').typeName == 'java.lang.Double'
    }

    @Test
    void testEnclosingCall2() {
        createDsls('contribute(enclosingCall("foo") & isThisType()) { property name:"yes", type:Double } ')

        String contents = 'foo( yes )'

        assert inferType(contents, 'yes').typeName == 'java.lang.Double'
    }

    @Test
    void testEnclosingCall3() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(name("arg") & bind(values: value()))) & isThisType()) {\n' +
            '  values.each { property name:"${it}Prop", type:Double }\n}')

        String contents = 'foo(arg:"yes", arg2:yesProp)'

        assert inferType(contents, 'yesProp').typeName == 'java.lang.Double'
    }

    @Test
    void testEnclosingCall4() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(names: name("arg"))) & isThisType()) {\n' +
            '  names.each { property name:"${it}Prop", type:Double }\n}')

        String contents = 'foo(arg:argProp)'

        assert inferType(contents, 'argProp').typeName == 'java.lang.Double'
    }

    @Test
    void testEnclosingCall5() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(bind(names: name("arg")) | bind(names: name("arg2")))) & isThisType()) {\n' +
            '  names.each { property name:"${it}Prop", type:Double }\n}')

        String contents = 'foo(arg:argProp)'

        assert inferType(contents, 'argProp').typeName == 'java.lang.Double'
    }

    @Test
    void testEnclosingCall6() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(bind(names: name("arg"))) & hasArgument(name("arg2"))) & isThisType()) {\n' +
            '  names.each { property name:"${it}Prop", type:Double }\n}')

        String contents = 'foo(arg:argProp, arg2: nuthin)'

        assert inferType(contents, 'argProp').typeName == 'java.lang.Double'
    }

    @Test
    void testEnclosingCall7() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(bind(values: value()) & name("arg"))) & isThisType()) {\n' +
            '  values.each { property name:"${it}Prop", type:Double }\n}')

        String contents = 'foo(arg:"arg", arg2:argProp)'

        assert inferType(contents, 'argProp').typeName == 'java.lang.Double'
    }

    @Test
    void testAnnotatedBy1() {
        createDsls('''\
            |contribute(enclosingMethod(annotatedBy(name("MyAnno") & hasAttribute(name("name") & bind(vals:value()))))) {
            |  vals.each { property name:it, type:Double }
            |}
            |'''.stripMargin())

        String contents = '''\
            |@interface MyAnno {
            |  String name()
            |}
            |@MyAnno(name="name")
            |def method() {
            |  name
            |}
            |'''.stripMargin()

        assert inferType(contents, 'name').typeName == 'java.lang.Double'
    }

    @Test
    void testAnnotatedBy2() {
        createDsls('''\
            |contribute(enclosingMethod(annotatedBy(name("MyAnno") &
            |    hasAttribute(name("name") & bind(names: value())) &
            |    hasAttribute(name("type") & bind(types: value())) ))) {
            |  property name:names[0], type:types[0]
            |}
            |'''.stripMargin())

        String contents = '''\
            |@interface MyAnno {
            |  String name()
            |  Class type()
            |}
            |@MyAnno(name="name", type=Double)
            |def method() {
            |  name
            |}
            |'''.stripMargin()

        assert inferType(contents, 'name').typeName == 'java.lang.Double'
    }

    @Test
    void testHasArgument1() {
        createDsls '''\
            |contribute(enclosingMethod(name('foo') & declaringType('Flart') & hasArgument('arg'))) {
            |  property name:'arg', type:'Flart'
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Flart {
            |  def foo(arg) {
            |    arg
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'arg').typeName == 'Flart'
    }

    @Test
    void testHasArgument2() {
        createDsls '''\
            |contribute(enclosingMethod(name('foo') & type('Flart') & hasArgument('arg'))) {
            |  property name:'arg', type:'Flart'
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Flart { }
            |class Other {
            |  Flart foo(arg) {
            |    arg
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'arg').typeName == 'Flart'
    }

    @Test
    void testHasArgument3() {
        createDsls '''\
            |contribute(enclosingCall(name('foo') & hasArgument('arg')) & inClosure()) {
            |  property name:'bar', type:BigDecimal
            |}
            |'''.stripMargin()

        String contents = '''\
            |def arg
            |def foo(... args) {}
            |def baz = foo(arg) { ->
            |  bar
            |}
            |'''.stripMargin()

        assert inferType(contents, 'bar').typeName == 'java.math.BigDecimal'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/767
    void testHasArgument4() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.PARAMETER)
            |public @interface A {
            |}
            |'''.stripMargin(), 'A', 'p'

        createDsls '''\
            |import org.codehaus.groovy.ast.expr.*
            |contribute(enclosingMethod(args: hasArgument(annotatedBy('p.A')))) {
            |  if ((enclosingNode instanceof MethodCallExpression || enclosingNode instanceof PropertyExpression) &&
            |      enclosingNode.objectExpression instanceof VariableExpression &&
            |      enclosingNode.objectExpression.accessedVariable in args) {
            |    method name:'someMeth', type:void, params:[:]
            |  }
            |}
            |'''.stripMargin()

        String contents = '''\
            |import p.A
            |class C {
            |  def m(@A String param) {
            |    param.someMeth()
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'someMeth').typeName == 'java.lang.Void'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/767
    void testHasArgument5() {
        addJavaSource '''\
            |package p;
            |import java.lang.annotation.*;
            |@Target(ElementType.PARAMETER)
            |public @interface A {
            |}
            |'''.stripMargin(), 'A', 'p'

        createDsls '''\
            |import org.codehaus.groovy.ast.expr.*
            |contribute(enclosingMethod(args: hasArgument(annotatedBy('p.A')))) {
            |  if ((enclosingNode instanceof MethodCallExpression || enclosingNode instanceof PropertyExpression) &&
            |      enclosingNode.objectExpression instanceof VariableExpression &&
            |      enclosingNode.objectExpression.accessedVariable in args) {
            |    property name:'someProp', type:Double
            |  }
            |}
            |'''.stripMargin()

        String contents = '''\
            |import p.A
            |class C {
            |  def m(@A String param) {
            |    param.someProp
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'someProp').typeName == 'java.lang.Double'
    }

    @Test // GRECLIPSE-1261
    void testStaticContext1() {
        createDsls('contribute(currentType("Flart")) { method name:"testme", type:boolean }')

        String contents = '''\
            |class Flart { }
            |static meth() {
            |  new Flart().testme
            |}
            |'''.stripMargin()

        assert inferType(contents, 'testme').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1261
    void testStaticContext2() {
        createDsls('contribute(currentType("Flart")) { method name:"testme", type:boolean }')

        String contents = '''\
            |class Flart { }
            |static meth() {
            |  Flart.testme
            |}
            |'''.stripMargin()

        assert inferType(contents, 'testme').result.confidence.name() == 'UNKNOWN'
    }

    @Test // GRECLIPSE-1261
    void testStaticContext3() {
        createDsls('contribute(currentType("Flart")) { method name:"testme", type:boolean, isStatic:true }')

        String contents = '''\
            |class Flart {
            |  static meth() {
            |    testme
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'testme').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1261
    void testStaticContext4() {
        createDsls('contribute(currentType("Flart")) { method name:"testme", type:boolean }')

        String contents = '''\
            |class Flart {
            |  static meth() {
            |    Flart.testme
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'testme').result.confidence.name() == 'UNKNOWN'
    }

    @Test // GRECLIPSE-1261
    void testStaticContext5() {
        createDsls('contribute(currentType("Flart")) { method name:"testme", type:boolean, isStatic:true }')

        String contents = '''\
            |class Flart {
            |static meth() {
            |  new Flart().testme
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'testme').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1290
    void testOperatorOverloading1() {
        createDsls('contribute(currentType("Flart")) { method name:"plus", params:[a:Object], type:boolean }')

        String contents = '''\
            |class Flart { }
            |def xxx = new Flart() + nuthin
            |xxx
            |'''.stripMargin()

        assert inferType(contents, 'xxx').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1290
    void testOperatorOverloading2() {
        createDsls('contribute(currentType("Flart")) { method name:"getAt", params:[a:Object], type:boolean }')

        String contents = '''\
            |class Flart { }
            |def xxx = new Flart()[nuthin]
            |xxx
            |'''.stripMargin()

        assert inferType(contents, 'xxx').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading3() {
        createDsls('contribute(currentType("Flart")) { method name:"getAt", params:[a:Object], type:boolean }')

        String contents = '''\
            |class Flart { }
            |def xxx = new Flart()[nuthin]
            |xxx
            |'''.stripMargin()

        assert inferType(contents, 'xxx').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading4() {
        createDsls('contribute(currentType("Flart")) { method name:"positive", type:boolean }')

        String contents = '''\
            |class Flart { }
            |def xxx = +(new Flart())
            |xxx
            |'''.stripMargin()

        assert inferType(contents, 'xxx').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading5() {
        createDsls('contribute(currentType("Flart")) { method name:"negative", type:boolean }')

        String contents = '''\
            |class Flart { }
            |def xxx = -(new Flart())
            |xxx
            |'''.stripMargin()

        assert inferType(contents, 'xxx').typeName == 'java.lang.Boolean'
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading6() {
        createDsls('contribute(currentType("Flart")) { method name:"bitwiseNegate", type:boolean }')

        String contents = '''\
            |class Flart { }
            |def xxx = ~(new Flart())
            |xxx
            |'''.stripMargin()

        assert inferType(contents, 'xxx').typeName == 'java.lang.Boolean'
    }

    @Test
    void testIsThisType1() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'thisType', type: Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def m() {
            |    thisType
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'thisType').typeName == 'java.lang.Integer'
    }

    @Test
    void testIsThisType1a() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'thisType', type: Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def m() {
            |    new Foo().thisType
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'thisType').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testIsThisType1b() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'thisType', type: Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def m() {
            |    [].thisType
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'thisType').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testIsThisType2() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'thisType', type: Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def f = {
            |    thisType
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'thisType').typeName == 'java.lang.Integer'
    }

    @Test
    void testIsThisType2a() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'thisType', type: Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def f = {
            |    new Foo().thisType
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'thisType').result.confidence.name() == 'UNKNOWN'
    }

    @Test
    void testIsThisType2b() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'thisType', type: Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def f = {
            |    [].thisType
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'thisType').result.confidence.name() == 'UNKNOWN'
    }

    @Test // GRECLIPSE-1295
    void testIsThisType3() {
        createDsls '''\
            |contribute(isThisType()) {
            |  property name: 'hi', type: int
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def meth(Closure code) {}
            |}
            |new Foo().meth { hi }
            |'''.stripMargin()

        assert inferType(contents, 'hi').typeName == 'java.lang.Integer'
    }

    @Test
    void testCurrentTypeIsEnclosingType1() {
        createDsls '''\
            |contribute(currentTypeIsEnclosingType()) {
            |  property name: 'hi', type: int
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def meth(@DelegatesTo(Foo) Closure code) {}
            |}
            |new Foo().meth { hi }
            |'''.stripMargin()

        assert inferType(contents, 'hi').typeName == 'java.lang.Integer'
    }

    @Test // GRECLIPSE-1295
    void testCurrentTypeIsEnclosingType2() {
        createDsls '''\
            |contribute(currentTypeIsEnclosingType()) {
            |  property name: 'hi', type: int
            |}
            |'''.stripMargin()

        String contents = '''\
            |class Foo {
            |  def meth(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_ONLY) Closure code) {}
            |}
            |new Foo().meth { hi } // enclosing type of closure is script type
            |'''.stripMargin()

        assert inferType(contents, 'hi').result.confidence.name() == 'UNKNOWN'
    }

    @Test // GRECLIPSE-1301
    void testEnclosingCallName1() {
        createDsls('''\
            |contribute(~ enclosingCallName('foo')) {
            |  property name:'hi'
            |}
            |'''.stripMargin())

        String contents = '''\
            |foo {
            |  bar {
            |    hi
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'hi').result.confidence.name() == 'UNKNOWN'
    }

    @Test // GRECLIPSE-1301
    void testEnclosingCallName2() {
        createDsls('''\
            |contribute(enclosingCall(~name('foo'))) {
            |  property name:'hi', type:int
            |}
            |'''.stripMargin())

        String contents = '''\
            |foo {
            |  bar {
            |    hi
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'hi').typeName == 'java.lang.Integer'
    }

    @Test // GRECLIPSE-1459
    void testNullType() {
        createDsls '''\
            |contribute(enclosingCall(hasArgument(type()))) {
            |  property name:'foo', type:Integer
            |}
            |'''.stripMargin()

        String contents = '''\
            |String flart(val, closure) { }
            |flart '', {
            |  foo
            |}
            |'''.stripMargin()

        assert inferType(contents, 'foo').typeName == 'java.lang.Integer'
    }

    @Test
    void testArrayType1() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'java.lang.String[]'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.lang.String[]'
    }

    @Test
    void testArrayType1a() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:String[]
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.lang.String[]'
    }

    @Test
    void testArrayType2() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'java.lang.String[][]'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.lang.String[][]'
    }

    @Test
    void testArrayType2a() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'String[][]'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.lang.String[][]'
    }

    @Test
    void testArrayType3() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'java.util.List<java.lang.String[][]>'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.util.List<java.lang.String[][]>'
    }

    @Test
    void testArrayType4() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'java.util.List<java.lang.String>[]'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.util.List<java.lang.String>[]'
    }

    @Test
    void testArrayType5() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'java.util.List<java.lang.String[]>[]'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.util.List<java.lang.String[]>[]'
    }

    @Test
    void testArrayType6() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'java.util.Map<java.lang.String[],java.lang.Integer[]>'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.util.Map<java.lang.String[],java.lang.Integer[]>'
    }

    @Test
    void testWildcardType1() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'Class<? extends java.lang.annotation.Annotation>'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.lang.Class<? extends java.lang.annotation.Annotation>'
    }

    @Test
    void testWildcardType2() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'Map<String, ? extends java.lang.annotation.Annotation>'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.util.Map<java.lang.String,? extends java.lang.annotation.Annotation>'
    }

    @Test
    void testWildcardType3() {
        createDsls '''\
            |contribute(currentType()) {
            |  property name:'prop', type:'Map<String, List<? extends java.lang.annotation.Annotation>>'
            |}
            |'''.stripMargin()

        String contents = 'prop'

        assert inferType(contents, 'prop').typeName == 'java.util.Map<java.lang.String,java.util.List<? extends java.lang.annotation.Annotation>>'
    }

    @Test
    void testAnnotatedType1() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', type:void, params:[code:'@ClosureParams(value=SimpleType, options=["java.util.regex.Pattern"]) Closure']
            |}
            |'''.stripMargin()

        String contents = 'meth { one-> }'

        assert inferType(contents, 'one').typeName == 'java.util.regex.Pattern'
    }

    @Test
    void testAnnotatedType2() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', type:void, params:[code:'@ClosureParams(value=SimpleType, options=["java.util.regex.Pattern","java.util.regex.Matcher"]) Closure']
            |}
            |'''.stripMargin()

        String contents = 'meth { one, two-> }'

        assert inferType(contents, 'one').typeName == 'java.util.regex.Pattern'
    }

    @Test
    void testAnnotatedType3() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', type:void, params:[code:'@ClosureParams(value=SimpleType, options=["java.util.regex.Pattern","java.util.regex.Matcher"]) Closure']
            |}
            |'''.stripMargin()

        String contents = 'meth { one, two-> }'

        assert inferType(contents, 'two').typeName == 'java.util.regex.Matcher'
    }

    @Test
    void testAnnotatedType4() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', type:void, params:[code:'@ClosureParams(value=SimpleType, options=["java.util.regex.Pattern"]) Closure'], namedParams:[attr:String]
            |}
            |'''.stripMargin()

        String contents = 'meth(attr:"value") { one-> }'

        assert inferType(contents, 'one').typeName == 'java.util.regex.Pattern'
    }

    @Test
    void testAnnotatedType5() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', type:void, params:[code:'@ClosureParams(value=SimpleType, options=["java.util.regex.Pattern"]) Closure'], optionalParams:[attr:String]
            |}
            |'''.stripMargin()

        String contents = 'meth(attr:"value") { one-> }'

        assert inferType(contents, 'one').typeName == 'java.util.regex.Pattern'
    }

    @Test
    void testAnnotatedType6() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', params:[code:'@DelegatesTo(java.util.regex.Pattern) Closure']
            |}
            |'''.stripMargin()

        String contents = 'meth { matcher("") }'

        inferType(contents, 'matcher').with {
            assert typeName == 'java.util.regex.Matcher'
            assert declaringTypeName == 'java.util.regex.Pattern'
        }
    }

    @Test
    void testAnnotatedType7() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', params:[code:'@DelegatesTo(java.util.regex.Pattern) Closure'], namedParams:[attr:String]
            |}
            |'''.stripMargin()

        String contents = 'meth(attr:"value") { matcher("") }'

        inferType(contents, 'matcher').with {
            assert typeName == 'java.util.regex.Matcher'
            assert declaringTypeName == 'java.util.regex.Pattern'
        }
    }

    @Test
    void testAnnotatedType8() {
        createDsls '''\
            |contribute(currentType()) {
            |  method name:'meth', params:[code:'@DelegatesTo(java.util.regex.Pattern) Closure'], optionalParams:[attr:String]
            |}
            |'''.stripMargin()

        String contents = 'meth(attr:"value") { matcher("") }'

        inferType(contents, 'matcher').with {
            assert typeName == 'java.util.regex.Matcher'
            assert declaringTypeName == 'java.util.regex.Pattern'
        }
    }

    @Test
    void testNestedCalls() {
        createDsls '''\
            |contribute(bind(x:enclosingCall())) {
            |  x.each {
            |    String propertyName = it.methodAsString + 'XXX'
            |    property name:propertyName, type:java.lang.Long
            |  }
            |}
            |'''.stripMargin()

        String contents = '''\
            |bar {
            |  foo {
            |    fooXXX
            |  }
            |}
            |'''.stripMargin()

        assert inferType(contents, 'fooXXX').typeName == 'java.lang.Long'
    }

    @Test // GRECLIPSE-1458
    void testMultiProject() {
        def otherProject = new TestProject('Other')
        try {
            otherProject.createFile('dsld/other.dsld', '''\
                |package dsld
                |contribute(currentType(String)) {
                |  property name:'other', type:Integer
                |}
                |'''.stripMargin())
            otherProject.fullBuild()

            addProjectReference(otherProject.javaProject)
            GroovyDSLCoreActivator.default.contextStoreManager.initialize(project, true)

            assert inferType('"".other', 'other').typeName == 'java.lang.Integer'
        } finally {
            otherProject.dispose()
        }
    }
}
