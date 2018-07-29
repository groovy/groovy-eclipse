/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator
import org.codehaus.groovy.eclipse.test.TestProject
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite
import org.junit.Before
import org.junit.Test

/**
 * Tests type inferencing that involve DSLs.
 */
final class DSLInferencingTests extends DSLInferencingTestSuite {

    private static final String SET_DELEGATE_TYPE_SCRIPT = '''\
        interface Obj {
          String getFoo()
          int FOO1 = 9
          int FOO2 = 9
          int BAR  = 9
          int BAZ1 = 9
          int BAZ2 = 9
          int BAZ3 = 9
          int OTHER = 9
        }
        void meth(@DelegatesTo(String) Closure cl) {
        }
        meth { delegate }
        meth { this }
        meth { getFoo() }
        meth { FOO1 }
        meth { delegate.FOO2 }
        meth { ''.OTHER }
        meth { delegate.with { BAR } }
        meth { 1.BAZ1 }
        meth { 1.with { BAZ2 } }
        meth { this.BAZ3 }
        '''.stripIndent()

    private static final String SET_DELEGATE_TYPE_DSLD = '''\
        contribute(inClosure() & currentType(String)) {
          setDelegateType('Obj')
        }
        '''.stripIndent()

    @Before
    void setUp() {
        addPlainText(getTestResourceContents('SomeInterestingExamples.dsld'), 'SomeInterestingExamples.dsld')
        buildProject()
    }

    @Test
    void testRegisteredPointcut1() {
        String contents = '2.phat'
        String name = 'phat'

        assertDeclaringType(contents, contents.indexOf(name), contents.indexOf(name) + name.length(), 'java.lang.Integer')
    }

    @Test
    void testRegisteredPointcut2() {
        String contents = '2.valueInteger'
        String name = 'valueInteger'

        assertDeclaringType(contents, contents.indexOf(name), contents.indexOf(name) + name.length(), 'java.lang.Integer')
    }

    @Test
    void testContiribution1() {
        createDsls('contribute(currentType("Foo")) { delegatesTo "Other" }')
        String contents = '''\
            class Foo {}
            class Other { Class<String> blar() {} }
            new Foo().blar()
            '''.stripIndent()

        int offset = contents.lastIndexOf('blar')
        assertType(contents, offset, offset + 'blar'.length(), 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, offset, offset + 'blar'.length(), 'Other')
    }

    @Test
    void testContiribution2() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')
        String contents = '''\
            class Foo {
              def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_FIRST) Closure block) {
              }
              def baz() {
                bar {
                  owner
                  delegate
                  something
                }
              }
            }
            '''.stripIndent()

        String target = 'owner'
        int offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'Foo')

        target = 'delegate'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.String')

        target = 'something'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.Runnable')
        assertDeclaringType(contents, offset, offset + target.length(), 'Foo') // aka "owner"
    }

    @Test
    void testContiribution3() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')
        String contents = '''\
            class Foo {
              def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_FIRST) Closure block) {
              }
              def baz() {
                bar {
                  owner
                  delegate
                  something
                }
              }
            }
            '''.stripIndent()

        String target = 'owner'
        int offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'Foo')

        target = 'delegate'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.String')

        target = 'something'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.Runnable')
        assertDeclaringType(contents, offset, offset + target.length(), 'Foo') // aka "owner"
    }

    @Test
    void testContiribution4() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')
        String contents = '''\
            class Foo {
              def bar(@DelegatesTo(value=String, strategy=Closure.OWNER_ONLY) Closure block) {
              }
              def baz() {
                bar {
                  owner
                  delegate
                  something
                }
              }
            }
            '''.stripIndent()

        String target = 'owner'
        int offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'Foo')

        target = 'delegate'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.String')

        target = 'something'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.Runnable')
        assertDeclaringType(contents, offset, offset + target.length(), 'Foo') // aka "owner"
    }

    @Test
    void testContiribution5() {
        createDsls('contribute(currentType("Foo")) { property name:"something", type:Runnable }')
        String contents = '''\
            class Foo {
              def bar(@DelegatesTo(value=String, strategy=Closure.DELEGATE_ONLY) Closure block) {
              }
              def baz() {
                bar {
                  owner
                  delegate
                  something
                }
              }
            }
            '''.stripIndent()

        String target = 'owner'
        int offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'Foo')

        target = 'delegate'
        offset = contents.indexOf(target)
        assertType(contents, offset, offset + target.length(), 'java.lang.String')

        target = 'something'
        offset = contents.indexOf(target)
        assertUnknownConfidence(contents, offset, offset + target.length(), 'Foo')
    }

    @Test
    void testDelegatesTo1() {
        createDsls('contribute(currentType("Foo")) { delegatesTo "Other" }')
        String contents = '''\
            class Foo { }
            class Other {
              Class<String> blar() { }
            }
            new Foo().blar()
            '''.stripIndent()
        int start = contents.lastIndexOf('blar')
        int end = start + 'blar'.length()
        assertType(contents, start, end, 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, start, end, 'Other')
    }

    @Test
    void testDelegatesTo2() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other" }')
        String contents =
            'class Foo { }\n' +
            'class Other { Class<String> blar() { } }\n' +
            'new Foo().blar()'
        int start = contents.lastIndexOf('blar')
        int end = start + 'blar'.length()
        assertType(contents, start, end, 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, start, end, 'Other')
    }

    @Test
    void testDelegatesTo3() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other" }')
        String contents =
            'class Foo { }\n' +
            'class Other { Class<String> blar() { } }\n' +
            'Foo.blar()'
        int start = contents.lastIndexOf('blar')
        int end = start + 'blar'.length()
        // unknown confidence because accessing in static context
        assertUnknownConfidence(contents, start, end, 'Other')
    }

    @Test
    void testDelegatesTo4() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", isStatic:true }')
        String contents =
            'class Foo { }\n' +
            'class Other { Class<String> blar() { } }\n' +
            'Foo.blar()'
        int start = contents.lastIndexOf('blar')
        int end = start + 'blar'.length()
        assertType(contents, start, end, 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, start, end, 'Other')
    }

    @Test
    void testDelegatesTo5() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", asCategory:true }')
        String contents =
            'class Foo { }\n' +
            'class Other { Class<String> blar(Foo x) { }\n' +
            'Class<String> flar() { } }\n' +
            'new Foo().blar()\n' +
            'new Foo().flar()'

        int start = contents.lastIndexOf('blar')
        int end = start + 'blar'.length()
        assertType(contents, start, end, 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, start, end, 'Other')

        start = contents.lastIndexOf('flar')
        end = start + 'flar'.length()
        assertUnknownConfidence(contents, start, end, 'Foo')
    }

    @Test
    void testDelegatesTo6() {
        createDsls('contribute(currentType("Foo")) { delegatesTo type:"Other", except: ["glar"]}')
        String contents =
            'class Foo {\n' +
            '  Class<String> glar() { }\n' +
            '}\n' +
            'class Other {\n' +
            '  Class<String> blar() { }\n' +
            '  Class<String> flar() { }\n' +
            '  Class<String> glar() { }\n' +
            '}\n' +
            'new Foo().flar()\n' +
            'new Foo().glar()'
        int start = contents.lastIndexOf('glar')
        int end = start + 'glar'.length()
        assertType(contents, start, end, 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, start, end, 'Foo')

        start = contents.lastIndexOf('flar')
        end = start + 'flar'.length()
        assertType(contents, start, end, 'java.lang.Class<java.lang.String>')
        assertDeclaringType(contents, start, end, 'Other')
    }

    @Test // GRECLIPSE-1321
    void testDelegatesTo7() {
        createDsls(
            'contribute(currentType(String)) {\n' +
            '  delegatesTo "Obj"\n' +
            '}')
        String contents =
            'public interface Obj {\n' +
            '    String getFoo();\n' +
            '    int foo(arg);\n' +
            ' }\n' +
            '"".getFoo()' +
            '"".foo()'
        int start = contents.lastIndexOf('foo')
        int end = start + 'foo'.length()
        assertType(contents, start, end, 'java.lang.Integer')
        start = contents.lastIndexOf('getFoo')
        end = start + 'getFoo'.length()
        assertType(contents, start, end, 'java.lang.String')
    }

    @Test // GRECLIPSE-1442
    void testDelegatesTo8() {
        createDsls(
            'contribute(currentType("Delegatee")) {\n' +
            '    delegatesTo type: "MyCategory", asCategory: true\n' +
            '}')
        String contents =
            'class MyCategory {\n' +
            '    static int getSomething(Delegatee d) { }\n' +
            '}\n' +
            'class Delegatee { }\n' +
            'new Delegatee().something \n' +
            'new Delegatee().getSomething()'
        int start = contents.lastIndexOf('getSomething')
        int end = start + 'getSomething'.length()
        assertType(contents, start, end, 'java.lang.Integer')
        start = contents.lastIndexOf('something')
        end = start + 'something'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    @Test // GRECLIPSE-1442
    void testDelegatesTo9() {
        createDsls(
            'contribute(currentType("Delegatee")) {\n' +
            '    delegatesTo type: "MyCategory", asCategory: true\n' +
            '}')
        String contents =
            'class MyCategory {\n' +
            '    static boolean isSomething(Delegatee d) { }\n' +
            '}\n' +
            'class Delegatee { }\n' +
            'new Delegatee().something \n' +
            'new Delegatee().isSomething()'
        int start = contents.lastIndexOf('isSomething')
        int end = start + 'isSomething'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
        start = contents.lastIndexOf('something')
        end = start + 'something'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test
    void testGenerics1() {
        createDsls('contribute(currentType("Foo")) { property name: "fooProp", type: "List<Class<Foo>>" }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        assertType(contents, start, end, 'java.util.List<java.lang.Class<Foo>>')
    }

    @Test
    void testGenerics2() {
        createDsls('contribute(currentType("Foo")) { property name: "fooProp", type: "List<Class<Foo>>" }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'def x = new Foo().fooProp[0]\n' +
            'x'
        int start = contents.lastIndexOf('x')
        int end = start + 'x'.length()
        assertType(contents, start, end, 'java.lang.Class<Foo>')
    }

    @Test
    void testGenerics3() {
        createDsls('contribute(currentType("Foo")) { property name: "fooProp", type: "Map< Integer, Long>" }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        assertType(contents, start, end, 'java.util.Map<java.lang.Integer,java.lang.Long>')
    }

    @Test
    void testDeprecated1() {
        createDsls('contribute(currentType("Foo")) { property name: "fooProp", type: "Map< Integer, Long>", isDeprecated:true }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        assertType(contents, start, end, 'java.util.Map<java.lang.Integer,java.lang.Long>')
        assertDeprecated(contents, start, end)
    }

    @Test
    void testDeprecated2() {
        createDsls('contribute(currentType("Foo")) { method name: "fooProp", type: "Map< Integer, Long>", isDeprecated:true }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        assertType(contents, start, end, 'java.util.Map<java.lang.Integer,java.lang.Long>')
        assertDeprecated(contents, start, end)
    }

    @Test
    void testAssertVersion1() {
        createDsls('assertVersion(groovyEclipse:"9.5.9")\n' +
            'contribute(currentType("Foo")) { property name: "fooProp", type: "Map< Integer, Long>" }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        // script should not be executed
        assertUnknownConfidence(contents, start, end, 'Foo')
    }

    @Test
    void testAssertVersion2() {
        createDsls('assertVersion(groovyEclipse:"1.5.9")\n' +
            'contribute(currentType("Foo")) { property name: "fooProp", type: "Map< Integer, Long>" }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        // script should be executed
        assertType(contents, start, end, 'java.util.Map<java.lang.Integer,java.lang.Long>')
    }

    @Test
    void testSupportsVersion3() {
        createDsls('if (supportsVersion(groovyEclipse:"9.5.9"))\n' +
            '  contribute(currentType("Foo")) { property name: "fooProp", type: "Map< Integer, Long>" }')
        String contents =
            'class Foo {\n' +
            '}\n' +
            'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        // script should not be executed
        assertUnknownConfidence(contents, start, end, 'Foo')
    }

    @Test
    void testSupportsVersion2() {
        createDsls('if (supportsVersion(groovyEclipse:"1.5.9"))\n' +
                '  contribute(currentType("Foo")) { property name: "fooProp", type: "Map< Integer, Long>" }')
        String contents =
                'class Foo {\n' +
                        '}\n' +
                        'new Foo().fooProp'
        int start = contents.lastIndexOf('fooProp')
        int end = start + 'fooProp'.length()
        // script should be executed
        assertType(contents, start, end, 'java.util.Map<java.lang.Integer,java.lang.Long>')
    }

    @Test
    void testEnclosingCall1() {
        createDsls('contribute(enclosingCall(name("foo")) & isThisType()) {  ' +
            'property name: "yes", type: Double } ')

        String contents = 'foo( yes )'
        int start = contents.lastIndexOf('yes')
        int end = start + 'yes'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testEnclosingCall2() {
        createDsls('contribute(enclosingCall("foo") & isThisType()) {  ' +
            'property name: "yes", type: Double } ')

        String contents = 'foo( yes )'
        int start = contents.lastIndexOf('yes')
        int end = start + 'yes'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testEnclosingCall3() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(name("arg") & bind(values: value()))) & isThisType()) {  ' +
            'values.each { property name: "${it}Prop", type: Double } }')

        String contents = 'foo(arg:"yes", arg2:yesProp)'
        int start = contents.lastIndexOf('yesProp')
        int end = start + 'yesProp'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testEnclosingCall4() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(names: name("arg"))) & isThisType()) {  ' +
            'names.each { property name: "${it}Prop", type: Double } }')

        String contents = 'foo(arg:argProp)'
        int start = contents.lastIndexOf('argProp')
        int end = start + 'argProp'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testEnclosingCall5() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(bind(names: name("arg")) | bind(names: name("arg2")))) & isThisType()) {  ' +
            'names.each { property name: "${it}Prop", type: Double } }')

        String contents = 'foo(arg:argProp)'
        int start = contents.lastIndexOf('argProp')
        int end = start + 'argProp'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testEnclosingCall6() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(bind(names: name("arg"))) & hasArgument(name("arg2"))) & isThisType()) {  ' +
            'names.each { property name: "${it}Prop", type: Double } }')

        String contents = 'foo(arg:argProp, arg2: nuthin)'
        int start = contents.lastIndexOf('argProp')
        int end = start + 'argProp'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testEnclosingCall7() {
        createDsls('contribute(enclosingCall(name("foo") & hasArgument(bind(values: value()) & name("arg"))) & isThisType()) {  ' +
            'values.each { property name: "${it}Prop", type: Double } }')

        String contents = 'foo(arg:"arg", arg2:argProp)'
        int start = contents.lastIndexOf('argProp')
        int end = start + 'argProp'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testAnnotatedBy1() {
        createDsls(
            'contribute(enclosingMethod(annotatedBy(\n' +
            '    name("MyAnno") &   \n' +
            '    hasAttribute(\n' +
            '        name("name") & \n' +
            '        bind(vals: value()))))) {\n' +
            '    vals.each { property name:it, type: Double }\n' +
            '}')
        String contents =
            '@interface MyAnno {\n' +
            '    String name() \n' +
            '}  \n' +
            '@MyAnno(name = "name")\n' +
            'def method() {\n' +
            '    name\n' +
            '}'
        int start = contents.lastIndexOf('name')
        int end = start + 'name'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testAnnotatedBy2() {
        createDsls(
            'contribute(enclosingMethod(annotatedBy(\n' +
            '    name("MyAnno") &   \n' +
            '    hasAttribute(\n' +
            '        name("name") & \n' +
            '        bind(names: value())) &\n' +
            '    hasAttribute(\n' +
            '        name("type") & \n' +
            '        bind(types: value()))))) {\n' +
            '    property name: names.iterator().next(), type: types.iterator().next()\n' +
            '}')
        String contents =
            '@interface MyAnno {\n' +
            '    String name() \n' +
            '    Class type() \n' +
            '}\n' +
            '@MyAnno(name = "name", type = Double)\n' +
            'def method() {\n' +
            '    name  \n' +
            '}'
        int start = contents.lastIndexOf('name')
        int end = start + 'name'.length()
        assertType(contents, start, end, 'java.lang.Double')
    }

    @Test
    void testHasArgument1() {
        createDsls '''\
            contribute(enclosingMethod(name('foo') & declaringType('Flart') & hasArgument('arg'))) {
              property name:'arg', type:'Flart'
            }
            '''.stripIndent()

        String contents = '''\
            class Flart {
              def foo(arg) {
                arg
              }
            }
            '''
        int offset = contents.lastIndexOf('arg')
        assertType(contents, offset, offset + 'arg'.length(), 'Flart')
    }

    @Test
    void testHasArgument2() {
        createDsls '''\
            contribute(enclosingMethod(name('foo') & type('Flart') & hasArgument('arg'))) {
              property name:'arg', type:'Flart'
            }
            '''.stripIndent()

        String contents = '''\
            class Flart { }
            class Other {
              Flart foo(arg) {
                arg
              }
            }
            '''.stripIndent()

        int offset = contents.lastIndexOf('arg')
        assertType(contents, offset, offset + 'arg'.length(), 'Flart')
    }

    @Test
    void testHasArgument3() {
        createDsls '''\
            contribute(enclosingCall(name('foo') & hasArgument('arg')) & inClosure()) {
              property name:'bar', type:BigDecimal
            }
            '''.stripIndent()

        String contents = '''\
            def arg
            def foo(... args) {}
            def baz = foo(arg) { ->
              bar
            }
            '''.stripIndent()

        int offset = contents.lastIndexOf('bar')
        assertType(contents, offset, offset + 'bar'.length(), 'java.math.BigDecimal')
    }

    @Test // GRECLIPSE-1261
    void testStaticContext1() {
        createDsls('contribute(currentType("Flart")) { method name: "testme", type: boolean }')
        String contents =
            'class Flart { }\n' +
            'static ahem() {\n' +
            '  new Flart().testme' +
            '}'
        int start = contents.lastIndexOf('testme')
        int end = start + 'testme'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1261
    void testStaticContext2() {
        createDsls('contribute(currentType("Flart")) { method name: "testme", type: boolean }')
        String contents =
            'class Flart { }\n' +
            'static ahem() {\n' +
            '  Flart.testme' +
            '}'
        int start = contents.lastIndexOf('testme')
        int end = start + 'testme'.length()
        assertUnknownConfidence(contents, start, end, 'Flart')
    }

    @Test // GRECLIPSE-1261
    void testStaticContext3() {
        createDsls('contribute(currentType("Flart")) { method name: "testme", type: boolean, isStatic:true }')
        String contents =
            'class Flart {\n' +
            'static ahem() {\n' +
            '  testme' +
            '} }'
        int start = contents.lastIndexOf('testme')
        int end = start + 'testme'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1261
    void testStaticContext4() {
        createDsls('contribute(currentType("Flart")) { method name: "testme", type: boolean }')
        String contents =
            'class Flart { \n' +
            'static ahem() {\n' +
            '  Flart.testme' +
            '} }'
        int start = contents.lastIndexOf('testme')
        int end = start + 'testme'.length()
        assertUnknownConfidence(contents, start, end, 'Flart')
    }

    @Test // GRECLIPSE-1261
    void testStaticContext5() {
        createDsls('contribute(currentType("Flart")) { method name: "testme", type: boolean, isStatic:true }')
        String contents =
            'class Flart { \n' +
            'static ahem() {\n' +
            '  new Flart().testme' +
            '} }'
        int start = contents.lastIndexOf('testme')
        int end = start + 'testme'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1290
    void testOperatorOverloading1() {
        createDsls('contribute(currentType("Flart")) { method name: "plus", params: [a:Object], type: boolean }')
        String contents =
            'class Flart { }\n' +
            'def xxx = new Flart() + nuthin\n' +
            'xxx'
        int start = contents.lastIndexOf('xxx')
        int end = start + 'xxx'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1290
    void testOperatorOverloading2() {
        createDsls('contribute(currentType("Flart")) { method name: "getAt", params: [a:Object], type: boolean }')
        String contents =
            'class Flart { }\n' +
            'def xxx = new Flart()[nuthin]\n' +
            'xxx'
        int start = contents.lastIndexOf('xxx')
        int end = start + 'xxx'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading3() {
        createDsls('contribute(currentType("Flart")) { method name: "getAt", params: [a:Object], type: boolean }')
        String contents =
            'class Flart { }\n' +
            'def xxx = new Flart()[nuthin]\n' +
            'xxx'
        int start = contents.lastIndexOf('xxx')
        int end = start + 'xxx'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading4() {
        createDsls('contribute(currentType("Flart")) { method name: "positive", type: boolean }')
        String contents =
            'class Flart { }\n' +
            'def xxx = +(new Flart())\n' +
            'xxx'
        int start = contents.lastIndexOf('xxx')
        int end = start + 'xxx'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading5() {
        createDsls('contribute(currentType("Flart")) { method name: "negative", type: boolean }')
        String contents =
            'class Flart { }\n' +
            'def xxx = -(new Flart())\n' +
            'xxx'
        int start = contents.lastIndexOf('xxx')
        int end = start + 'xxx'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test // GRECLIPSE-1291
    void testOperatorOverloading6() {
        createDsls('contribute(currentType("Flart")) { method name: "bitwiseNegate", type: boolean }')
        String contents =
            'class Flart { }\n' +
            'def xxx = ~(new Flart())\n' +
            'xxx'
        int start = contents.lastIndexOf('xxx')
        int end = start + 'xxx'.length()
        assertType(contents, start, end, 'java.lang.Boolean')
    }

    @Test
    void testIsThisType1() {
        createDsls '''\
            contribute(isThisType()) {
              property name: 'thisType', type: Integer
            }
            '''.stripIndent()

        String contents = '''\
            class Foo {
              def m() {
                thisType
                new Foo().thisType
                [].thisType
              }
            }
            '''.stripIndent()

        int offset = contents.indexOf('thisType')
        assertType(contents, offset, offset + 'thisType'.length(), 'java.lang.Integer')

        offset = contents.indexOf('thisType', offset + 1)
        assertUnknownConfidence(contents, offset, offset + 'thisType'.length(), 'Foo')

        offset = contents.indexOf('thisType', offset + 1)
        assertUnknownConfidence(contents, offset, offset + 'thisType'.length(), 'java.util.List<E>')
    }

    @Test
    void testIsThisType2() {
        createDsls '''\
            contribute(isThisType()) {
              property name: 'thisType', type: Integer
            }
            '''.stripIndent()

        String contents = '''\
            class Foo {
              def f = {
                thisType
                new Foo().thisType
                [].thisType
              }
            }
            '''.stripIndent()

        int offset = contents.indexOf('thisType')
        assertType(contents, offset, offset + 'thisType'.length(), 'java.lang.Integer')

        offset = contents.indexOf('thisType', offset + 1)
        assertUnknownConfidence(contents, offset, offset + 'thisType'.length(), 'Foo')

        offset = contents.indexOf('thisType', offset + 1)
        assertUnknownConfidence(contents, offset, offset + 'thisType'.length(), 'java.util.List<E>')
    }

    @Test // GRECLIPSE-1295
    void testIsThisType3() {
        createDsls '''\
            contribute(isThisType()) {
              property name: 'hi', type: int
            }
            '''.stripIndent()

        String contents = '''\
            class Foo {
              def meth(Closure code) {}
            }
            new Foo().meth { hi }
            '''.stripIndent()

        int offset = contents.lastIndexOf('hi')
        assertType(contents, offset, offset + 'hi'.length(), 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeIsEnclosingType1() {
        createDsls '''\
            contribute(currentTypeIsEnclosingType()) {
              property name: 'hi', type: int
            }
            '''.stripIndent()

        String contents = '''\
            class Foo {
              def meth(@DelegatesTo(Foo) Closure code) {}
            }
            new Foo().meth { hi }
            '''.stripIndent()

        int offset = contents.lastIndexOf('hi')
        assertType(contents, offset, offset + 'hi'.length(), 'java.lang.Integer')
    }

    @Test // GRECLIPSE-1295
    void testCurrentTypeIsEnclosingType2() {
        createDsls '''\
            contribute(currentTypeIsEnclosingType()) {
              property name: 'hi', type: int
            }
            '''.stripIndent()

        String contents = '''\
            class Foo {
              def meth(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_ONLY) Closure code) {}
            }
            new Foo().meth { hi } // enclosing type of closure is script type
            '''.stripIndent()

        int offset = contents.lastIndexOf('hi')
        assertUnknownConfidence(contents, offset, offset + 'hi'.length(), 'Foo')
    }

    @Test // GRECLIPSE-1301
    void testEnclosingCallName1() {
        createDsls(
            'contribute(~ enclosingCallName("foo")) {\n' +
            '  property name:"hi"\n' +
            '}')
        String contents = '''\
            foo {
              bar {
                hi
              }
            }
            '''.stripIndent()
        int start = contents.lastIndexOf('hi')
        int end = start + 'hi'.length()
        assertUnknownConfidence(contents, start, end, 'Search')
    }

    @Test // GRECLIPSE-1301
    void testEnclosingCallName2() {
        createDsls(
            'contribute(enclosingCall(~name("foo"))) {\n' +
            '    property name:"hi", type:int\n' +
            '}')

        String contents =
            'foo {\n' +
            '    bar {\n' +
            '        hi\n' +
            '    }\n' +
            '}'
        int start = contents.lastIndexOf('hi')
        int end = start + 'hi'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    @Test
    void testSetDelegateType1() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('delegate')
        int end = start + 'delegate'.length()
        assertType(contents, start, end, 'Obj')
    }

    @Test
    void testSetDelegateType1a() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('this')
        int end = start + 'this'.length()
        GroovyCompilationUnit unit = addGroovySource(contents, 'Search')
        InferencingTestSuite.assertType(unit, start, end, 'Search')
    }

    @Test
    void testSetDelegateType2() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('getFoo')
        int end = start + 'getFoo'.length()
        assertType(contents, start, end, 'java.lang.String')
    }

    @Test
    void testSetDelegateType3() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('FOO1')
        int end = start + 'FOO1'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    @Test
    void testSetDelegateType4() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('FOO2')
        int end = start + 'FOO2'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    @Test
    void testSetDelegateType5() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('OTHER')
        int end = start + 'OTHER'.length()
        assertUnknownConfidence(contents, start, end, 'java.lang.String')
    }

    @Test
    void testSetDelegateType6() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('BAR')
        int end = start + 'BAR'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    @Test
    void testSetDelegateType7() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('BAZ1')
        int end = start + 'BAZ1'.length()
        assertUnknownConfidence(contents, start, end, 'java.lang.Integer')
    }

    @Test
    void testSetDelegateType8() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('BAZ2')
        int end = start + 'BAZ2'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    @Test
    void testSetDelegateType9() {
        createDsls(SET_DELEGATE_TYPE_DSLD)
        String contents = SET_DELEGATE_TYPE_SCRIPT
        int start = contents.lastIndexOf('BAZ3')
        int end = start + 'BAZ3'.length()
        assertUnknownConfidence(contents, start, end, 'Search')
    }

    @Test // GRECLIPSE-1459
    void testNullType() {
        createDsls '''\
            contribute(enclosingCall(hasArgument(type()))) {
              property name:'foo', type:Integer
            }
            '''.stripIndent()
        String contents = '''\
            String flart(val, closure) { }
            flart '', {
              foo
            }
            '''.stripIndent()
        int start = contents.lastIndexOf('fo')
        int end = start + 'foo'.length()
        assertType(contents, start, end, 'java.lang.Integer')
    }

    private static final String ARRAY_TYPE_DSLD = '''\
        contribute(currentType()) {
          property name:'foot1', type:'java.lang.String[]'
          property name:'foot2', type:'java.lang.String[][]'
          property name:'foot3', type:'java.util.List<java.lang.String[][]>'
          property name:'foot4', type:'java.util.List<java.lang.String>[]'
          property name:'foot5', type:'java.util.List<java.lang.String[]>[]'
          property name:'foot6', type:'java.util.Map<java.lang.String[],java.lang.Integer[]>'
        }'''.stripIndent()

    @Test // GRECLIPSE-1555
    void testArrayType1() {
        createDsls(ARRAY_TYPE_DSLD)
        String contents = 'foot1'
        assertType(contents, 0, contents.length(), 'java.lang.String[]')
    }

    @Test
    void testArrayType2() {
        createDsls(ARRAY_TYPE_DSLD)
        String contents = 'foot2'
        assertType(contents, 0, contents.length(), 'java.lang.String[][]')
    }

    @Test
    void testArrayType3() {
        createDsls(ARRAY_TYPE_DSLD)
        String contents = 'foot3'
        assertType(contents, 0, contents.length(), 'java.util.List<java.lang.String[][]>')
    }

    @Test
    void testArrayType4() {
        createDsls(ARRAY_TYPE_DSLD)
        String contents = 'foot4'
        assertType(contents, 0, contents.length(), 'java.util.List<java.lang.String>[]')
    }

    @Test
    void testArrayType5() {
        createDsls(ARRAY_TYPE_DSLD)
        String contents = 'foot5'
        assertType(contents, 0, contents.length(), 'java.util.List<java.lang.String[]>[]')
    }

    @Test
    void testArrayType6() {
        createDsls(ARRAY_TYPE_DSLD)
        String contents = 'foot6'
        assertType(contents, 0, contents.length(), 'java.util.Map<java.lang.String[],java.lang.Integer[]>')
    }

    @Test
    void testNestedCalls() {
        createDsls '''\
            contribute(bind(x: enclosingCall())) {
              x.each {
                property name: it.methodAsString + 'XXX', type: Long
              }
            }
            '''.stripIndent()

        String contents = '''\
            bar {
              foo {
                fooXXX
                barXXX
              }
            }
            '''.stripIndent()

        int offset = contents.indexOf('fooXXX')
        assertType(contents, offset, offset + 'fooXXX'.length(), 'java.lang.Long')

        offset = contents.indexOf('barXXX')
        assertType(contents, offset, offset + 'barXXX'.length(), 'java.lang.Long')
    }

    @Test // GRECLIPSE-1458
    void testMultiProject() {
        def otherProject = new TestProject('Other')
        try {
            otherProject.createFile('dsld/other.dsld', '''\
                package dsld
                contribute(currentType(String)) {
                  property name: 'other', type: Integer
                }
                '''.stripIndent())
            otherProject.fullBuild()

            addProjectReference(otherProject.javaProject)
            GroovyDSLCoreActivator.default.contextStoreManager.initialize(project, true)

            String contents = '"".other'
            int offset = contents.lastIndexOf('other')
            assertType(contents, offset, offset + 'other'.length(), 'java.lang.Integer')
        } finally {
            otherProject.dispose()
        }
    }
}
