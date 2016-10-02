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
package org.eclipse.jdt.groovy.core.tests.basic

import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest

final class TraitsTests extends AbstractGroovyRegressionTest {

    static junit.framework.Test suite() {
        buildMinimalComplianceTestSuite(TraitsTests, F_1_6)
    }

    TraitsTests(String name) {
        super(name)
    }

    void testTraits1() {
        String[] sources = [
            'Test.groovy', '''
            class Person implements Greetable {
                String name() { 'Bob' }
            }
            public class Test {
              public static void main(String[] argv) {
                def p = new Person()
                print p.greeting()
              }
            }
            ''',

            'Greetable.groovy', '''
            trait Greetable {
                abstract String name()
                String greeting() { "Hello, ${name()}!" }
            }
            '''
        ]

        runConformTest(sources, 'Hello, Bob!')

        def unit = getCUDeclFor('Greetable.groovy').compilationUnit
        def classNode = unit.getClassNode('Greetable')
        assertTrue(classNode.isInterface())
    }

    void testTraits1a() {
        String[] sources = [
            'Test.groovy', '''
            class Person implements Greetable {
                String name() { 'Bob' }
            }
            public class Test {
              public static void main(String[] argv) {
                def p = new Person()
                print p.greeting()
              }
            }
            ''',

            'Greetable.groovy', '''
            import groovy.transform.Trait;
            @Trait
            class Greetable {
                abstract String name()
                String greeting() { "Hello, ${name()}!" }
            }
            '''
        ]

        runConformTest(sources, 'Hello, Bob!')

        def unit = getCUDeclFor('Greetable.groovy').compilationUnit
        def classNode = unit.getClassNode('Greetable')
        assertTrue(classNode.isInterface())
    }

    // Abstract Methods
    void testTraits2() {
        String[] sources = [
            'A.groovy', '''
            trait Greetable {
                abstract String name()
                String greeting() { "Hello, ${name()}!" }
            }
            class Person implements Greetable {
                String name() { 'Bob' }
            }
            def p = new Person()
            print p.greeting()
            '''
        ]

        runConformTest(sources, 'Hello, Bob!')
    }

    // Private Methods - positive test
    void testTraits3() {
        String[] sources = [
            'A.groovy', '''
            trait Greeter {
                private String greetingMessage() {
                    'Hello from a private method!'
                }
                String greet() {
                    def m = greetingMessage()
                    println m
                    m
                }
            }
            class GreetingMachine implements Greeter {}
            def g = new GreetingMachine()
            g.greet()
            '''
        ]

        runConformTest(sources, 'Hello from a private method!')
    }

    // Private Methods - negative test
    void testTraits4() {
        String[] sources = [
            'A.groovy', '''
            trait Greeter {
                private String greetingMessage() {
                    'Hello from a private method!'
                }
                String greet() {
                    def m = greetingMessage()
                    println m
                    m
                }
            }
            class GreetingMachine implements Greeter {}
            def g = new GreetingMachine()
            try {
                g.greetingMessage()
            } catch (MissingMethodException e) {
            }
            '''
        ]

        runConformTest(sources)
    }

    // Meaning of this
    void testTraits5() {
        String[] sources = [
            'A.groovy', '''
            trait Introspector {
                def whoAmI() { this.getClass() }
            }
            class Foo implements Introspector {}
            def foo = new Foo()
            print foo.whoAmI()
            '''
        ]

        runConformTest(sources, 'class Foo')
    }

    // Interfaces
    void testTraits6() {
        String[] sources = [
            'A.groovy', '''
            interface Named {
                String name()
            }
            trait Greetable implements Named {
                String greeting() { "Hello, ${name()}!" }
            }
            class Person implements Greetable {
                String name() { 'Bob' }
            }
            def p = new Person()
            print p.greeting()
            '''
        ]

        runConformTest(sources, 'Hello, Bob!')

        def unit = getCUDeclFor('A.groovy').compilationUnit
        def classNode = unit.getClassNode('Person')
        def type = unit.getClassNode('Greetable')
        assertTrue(classNode.implementsInterface(type))
        type = unit.getClassNode('Named')
        assertTrue(classNode.implementsInterface(type))
    }

    // Properties
    void testTraits7() {
        String[] sources = [
            'A.groovy', '''
            trait Named {
                String name
            }
            class Person implements Named {}
            def p = new Person(name: 'Bob')
            print p.name == 'Bob'
            print p.getName()
            '''
        ]

        runConformTest(sources, 'trueBob')
    }

    // Private fields
    void testTraits8() {
        String[] sources = [
            'A.groovy', '''
            trait Counter {
                private int count = 0
                int count() { count += 1; count }
            }
            class Foo implements Counter {}
            def f = new Foo()
            print f.count()
            '''
        ]

        runConformTest(sources, '1')
    }

    // Public fields
    void testTraits9() {
        String[] sources = [
            'A.groovy', '''
            trait Named {
                public String name
            }
            class Person implements Named {}
            def p = new Person()
            p.Named__name = 'Bob'
            print p.Named__name
            '''
        ]

        runConformTest(sources, 'Bob')
    }

    // Composition of Behaviors
    void testTraits10() {
        String[] sources = [
            'A.groovy', '''
            trait FlyingAbility {
                String fly() { "I'm flying!" }
            }
            trait SpeakingAbility {
                String speak() { "I'm speaking!" }
            }
            class Duck implements FlyingAbility, SpeakingAbility {}
            def d = new Duck()
            print d.fly()
            print d.speak()
            '''
        ]

        runConformTest(sources, 'I\'m flying!I\'m speaking!')
    }

    // Overriding default methods
    void testTraits11() {
        String[] sources = [
            'A.groovy', '''
            trait FlyingAbility {
                String fly() { "I'm flying!" }
            }
            trait SpeakingAbility {
                String speak() { "I'm speaking!" }
            }
            class Duck implements FlyingAbility, SpeakingAbility {
                String quack() { "Quack!" }
                String speak() { quack() }
            }
            def d = new Duck()
            print d.fly()
            print d.quack()
            print d.speak()
            '''
        ]

        runConformTest(sources, 'I\'m flying!Quack!Quack!')
    }

    // Simple Inheritance
    void testTraits12() {
        String[] sources = [
            'A.groovy', '''
            trait Named {
                String name
            }
            trait Polite extends Named {
                String introduce() { "Hello, I am $name" }
            }
            class Person implements Polite {}
            def p = new Person(name: 'Alice')
            print p.introduce()
            '''
        ]

        runConformTest(sources, 'Hello, I am Alice')
    }

    // Multiple Inheritance
    void testTraits13() {
        String[] sources = [
            'A.groovy', '''
            trait WithId {
                Long id
            }
            trait WithName {
                String name
            }
            trait Identified implements WithId, WithName {
            }
            '''
        ]

        runConformTest(sources)
    }

    // Dynamic code
    void testTraits14() {
        String[] sources = [
            'A.groovy', '''
            trait SpeakingDuck {
                String speak() { quack() }
            }
            class Duck implements SpeakingDuck {
                String methodMissing(String name, args) {
                    "${name.capitalize()}!"
                }
            }
            def d = new Duck()
            print d.speak()
            '''
        ]

        runConformTest(sources, 'Quack!')
    }

    // Dynamic methods in trait
    void testTraits15() {
        String[] sources = [
            'A.groovy', '''
            trait DynamicObject {
                private Map props = [:]
                def methodMissing(String name, args) {
                    name.toUpperCase()
                }
                def propertyMissing(String prop) {
                    props['prop']
                }
                void setProperty(String prop, Object value) {
                    props['prop'] = value
                }
            }
            class Dynamic implements DynamicObject {
                String existingProperty = 'ok'
                String existingMethod() { 'ok' }
            }
            def d = new Dynamic()
            print d.existingProperty
            print d.foo
            d.foo = 'bar'
            print d.foo
            print d.existingMethod()
            print d.someMethod()
            '''
        ]

        runConformTest(sources, 'oknullbarokSOMEMETHOD')
    }

    // Multiple inheritance conflicts - Default conflict resolution
    void testTraits16() {
        String[] sources = [
            'Sample.groovy', '''
            trait A {
                String exec() { 'A' }
            }
            trait B {
                String exec() { 'B' }
            }
            class C implements A, B {}
            def c = new C()
            print c.exec()
            '''
        ]

        runConformTest(sources, 'B')
    }

    // Multiple inheritance conflicts - Default conflict resolution
    void testTraits17() {
        String[] sources = [
            'Sample.groovy', '''
            trait A {
                String exec() { 'A' }
            }
            trait B {
                String exec() { 'B' }
            }
            class C implements B, A {}
            def c = new C()
            print c.exec()
            '''
        ]

        runConformTest(sources, 'A')
    }

    // Multiple inheritance conflicts - User conflict resolution
    void testTraits18() {
        String[] sources = [
            'Sample.groovy', '''
            trait A {
                String exec() { 'A' }
            }
            trait B {
                String exec() { 'B' }
            }
            class C implements A, B {
                String exec() { A.super.exec() }
            }
            def c = new C()
            print c.exec()
            '''
        ]

        runConformTest(sources, 'A')
    }

    // Implementing a trait at runtime
    void testTraits19() {
        String[] sources = [
            'Sample.groovy', '''
            trait Extra {
                String extra() { 'Extra' }
            }
            class Something {
                String doSomething() { 'Something' }
            }
            def s = new Something() as Extra
            print s.extra()
            print s.doSomething()
            '''
        ]

        runConformTest(sources, 'ExtraSomething')
    }

    // Implementing multiple traits at once - negative
    void testTraits20() {
        String[] sources = [
            'Sample.groovy', '''
            trait A { String methodFromA() { 'A' } }
            trait B { String methodFromB() { 'B' } }
            class C {}
            def c = new C()
            print c.methodFromA()
            print c.methodFromB()
            '''
        ]

        runConformTest(
            // test directory preparation
            true, /* flush output directory */
            sources,
            null /* no class libraries */,
            compilerOptions /* custom options */,
            '' /* expected compiler log */,
            '' /* expected output string */,
            'groovy.lang.MissingMethodException: No signature of method: C.methodFromA() is applicable for argument types: () values: []',
            new AbstractRegressionTest.JavacTestOptions())
    }

    // Implementing multiple traits at once - positive
    void testTraits21() {
        String[] sources = [
            'Sample.groovy', '''
            trait A { String methodFromA() { 'A' } }
            trait B { String methodFromB() { 'B' } }
            class C {}
            def c = new C()
            def d = c.withTraits A, B
            print d.methodFromA()
            print d.methodFromB()
            '''
        ]

        runConformTest(sources, 'AB')
    }

    // Chaining behavior
    void testTraits22() {
        String[] sources = [
            'Sample.groovy', '''
            interface MessageHandler {
                void on(String message, Map payload)
            }
            trait DefaultHandler implements MessageHandler {
                void on(String message, Map payload) {
                    println "Received $message with payload $payload"
                }
            }
            class SimpleHandler implements DefaultHandler {}
            def handler = new SimpleHandler()
            handler.on('test logging', [:])
            '''
        ]

        runConformTest(sources, 'Received test logging with payload [:]')
    }

    // Chaining behavior
    void testTraits23() {
        String[] sources = [
            'Sample.groovy', '''
            interface MessageHandler {
                void on(String message, Map payload)
            }
            trait DefaultHandler implements MessageHandler {
                void on(String message, Map payload) {
                    println "Received $message with payload $payload"
                }
            }
            class SimpleHandlerWithLogging implements DefaultHandler {
                void on(String message, Map payload) {
                    println "Seeing $message with payload $payload"
                    DefaultHandler.super.on(message, payload)
                }
            }
            def handler = new SimpleHandlerWithLogging()
            handler.on('test logging', [:])
            '''
        ]

        runConformTest(sources, 'Seeing test logging with payload [:]\nReceived test logging with payload [:]')
    }

    // Chaining behavior
    void testTraits24() {
        String[] sources = [
            'Sample.groovy', '''
            interface MessageHandler {
                void on(String message, Map payload)
            }
            trait DefaultHandler implements MessageHandler {
                void on(String message, Map payload) {
                    println "Received $message with payload $payload"
                }
            }
            trait SayHandler implements MessageHandler {
                void on(String message, Map payload) {
                    if (message.startsWith("say")) {
                        println "I say ${message - 'say'}!"
                    } else {
                        super.on(message, payload)
                    }
                }
            }
            trait LoggingHandler implements MessageHandler {
                void on(String message, Map payload) {
                    println "Seeing $message with payload $payload"
                    super.on(message, payload)
                }
            }
            class Handler implements DefaultHandler, SayHandler, LoggingHandler {}
            def handler = new Handler()
            handler.on('foo', [:])
            handler.on('sayHello', [:])
            '''
        ]

        runConformTest(sources, 'Seeing foo with payload [:]\nReceived foo with payload [:]\nSeeing sayHello with payload [:]\nI say Hello!')
    }

    // Chaining behavior
    void testTraits25() {
        String[] sources = [
            'Sample.groovy', '''
            interface MessageHandler {
                void on(String message, Map payload)
            }
            trait DefaultHandler implements MessageHandler {
                void on(String message, Map payload) {
                    println "Received $message with payload $payload"
                }
            }
            trait SayHandler implements MessageHandler {
                void on(String message, Map payload) {
                    if (message.startsWith("say")) {
                        println "I say ${message - 'say'}!"
                    } else {
                        super.on(message, payload)
                    }
                }
            }
            trait LoggingHandler implements MessageHandler {
                void on(String message, Map payload) {
                    println "Seeing $message with payload $payload"
                    super.on(message, payload)
                }
            }
            class AlternateHandler implements DefaultHandler, LoggingHandler, SayHandler {}
            def handler = new AlternateHandler()
            handler.on('foo', [:])
            handler.on('sayHello', [:])
            '''
        ]

        runConformTest(sources, 'Seeing foo with payload [:]\nReceived foo with payload [:]\nI say Hello!')
    }

    // Chaining behavior - Semantics of super inside a trait
    void testTraits26() {
        String[] sources = [
            'Sample.groovy', '''
            trait Filtering {
                StringBuilder append(String str) {
                    def subst = str.replace('o', '')
                    super.append(subst)
                }
                String toString() { super.toString() }
            }
            def sb = new StringBuilder().withTraits Filtering
            sb.append('Groovy')
            print sb.toString()
            '''
        ]

        runConformTest(sources, 'Grvy')
    }

    // SAM type coercion
    void testTraits27() {
        String[] sources = [
            'Sample.groovy', '''
            trait Greeter {
                String greet() { "Hello $name" }
                abstract String getName()
            }
            Greeter greeter = { 'Alice' }
            print greeter.getName()
            '''
        ]

        runConformTest(sources, 'Alice')
    }

    // SAM type coercion
    void testTraits28() {
        String[] sources = [
            'Sample.groovy', '''
            trait Greeter {
                String greet() { "Hello $name" }
                abstract String getName()
            }
            void greet(Greeter g) { println g.greet() }
            greet { 'Alice' }
            '''
        ]

        runConformTest(sources, 'Hello Alice')
    }

    // Differences with Java 8 default methods
    void testTraits29() {
        String[] sources = [
            'Sample.groovy', '''
            class Person {
                String name
            }
            trait Bob {
                String getName() { 'Bob' }
            }
            def p = new Person(name: 'Alice')
            print p.name
            def p2 = p as Bob
            print p2.name
            '''
        ]

        runConformTest(sources, 'AliceBob')
    }

    // Differences with mixins
    void testTraits30() {
        String[] sources = [
            'Sample.groovy', '''
            class A { String methodFromA() { 'A' } }
            class B { String methodFromB() { 'B' } }
            A.metaClass.mixin B
            def o = new A()
            print o.methodFromA()
            print o.methodFromB()
            print(o instanceof A)
            print(o instanceof B)
            '''
        ]

        runConformTest(sources, 'ABtruefalse')
    }

    // Static methods, properties and fields
    void testTraits31() {
        String[] sources = [
            'Sample.groovy', '''
            trait TestHelper {
                public static boolean called = false
                static void init() {
                    called = true
                }
            }
            class Foo implements TestHelper {}
            Foo.init()
            print Foo.TestHelper__called
            '''
        ]

        runConformTest(sources, 'true')
    }

    // Static methods, properties and fields
    void testTraits32() {
        String[] sources = [
            'Sample.groovy', '''
            trait TestHelper {
                public static boolean called = false
                static void init() {
                    called = true
                }
            }
            class Bar implements TestHelper {}
            class Baz implements TestHelper {}
            Bar.init()
            print Bar.TestHelper__called
            print Baz.TestHelper__called
            '''
        ]

        runConformTest(sources, 'truefalse')
    }

    // Inheritance of state gotchas
    void testTraits33() {
        String[] sources = [
            'Sample.groovy', '''
            trait IntCouple {
                int x = 1
                int y = 2
                int sum() { x+y }
            }
            class BaseElem implements IntCouple {
                int f() { sum() }
            }
            def base = new BaseElem()
            print base.f()
            '''
        ]

        runConformTest(sources, '3')
    }

    // Inheritance of state gotchas
    void testTraits34() {
        String[] sources = [
            'Sample.groovy', '''
            trait IntCouple {
                int x = 1\n
                int y = 2\n
                int sum() { x+y }
            }
            class Elem implements IntCouple {
                int x = 3
                int y = 4
                int f() { sum() }
            }
            def elem = new Elem()
            print elem.f()
            '''
        ]

        runConformTest(sources, '3')
    }

    // Inheritance of state gotchas
    void testTraits35() {
        String[] sources = [
            'Sample.groovy', '''
            trait IntCouple {
                int x = 1
                int y = 2
                int sum() { getX() + getY() }
            }
            class Elem implements IntCouple {
                int x = 3
                int y = 4
                int f() { sum() }
            }
            def elem = new Elem()
            print elem.f()
            '''
        ]

        runConformTest(sources, '7')
    }

    // Limitations - Prefix and postfix operations
    void testTraits36() {
        String[] sources = [
            'Sample.groovy', '''\
            trait Counting {
                int x
                void inc() {
                    x++
                }
                void dec() {
                    --x
                }
            }
            class Counter implements Counting {}
            def c = new Counter()
            c.inc()
            '''.stripIndent()
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 4)\n" +
            "\tx++\n" +
            "\t ^\n" +
            "Groovy:Postfix expressions on trait fields/properties  are not supported in traits. @ line 4, column 10.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 7)\n" +
            "\t--x\n" +
            "\t^\n" +
            "Groovy:Prefix expressions on trait fields/properties are not supported in traits. @ line 7, column 9.\n" +
            "----------\n")
    }

    // Test @Trait annotation
    void testTraits37() {
        String[] sources = [
            'Sample.groovy', '''
            @groovy.transform.Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Test @Trait annotation
    void testTraits38() {
        String[] sources = [
            'Sample.groovy', '''
            import groovy.transform.Trait
            @Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Test @Trait annotation
    void testTraits39() {
        String[] sources = [
            'Sample.groovy', '''
            import groovy.transform.*
            @Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Negative test for @Trait annotation
    void testTraits40() {
        String[] sources = [
            'Sample.groovy', '''\
            @interface Trait{}
            @Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''.stripIndent()
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n")
    }

    // Negative test for @Trait annotation
    void testTraits41() {
        String[] sources = [
            'Trait.groovy', '''
            package a
            @interface Trait {}
            ''',

            'Sample.groovy', '''\
            package b
            import a.Trait
            @Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''.stripIndent()
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 7)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'b.MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 7)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n")
    }

    // Negative test for @Trait annotation
    void testTraits42() {
        String[] sources = [
            'Trait.groovy', '''
            package a
            @interface Trait {}\n
            ''',

            'Sample.groovy', '''\
            package b
            @a.Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''.stripIndent()
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'b.MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n")
    }

    // Negative test for @Trait annotation
    void testTraits43() {
        String[] sources = [
            'Trait.groovy', '''
            package a
            @interface Trait {}
            ''',

            'Sample.groovy', '''\
            package b
            import a.Trait
            import groovy.transform.*
            @Trait
            class MyTrait {
                def m() { 'a' }
            }
            class MyClass implements MyTrait {
            }
            def myClass = new MyClass()
            print myClass.m()
            '''.stripIndent()
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 8)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'b.MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 8)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n")
    }

    // Test protected method of superclass overriding by trait method - default package
    void testTraits44() {
        String[] sources = [
            'Sample.groovy', '''
            trait MyTrait {
                def m() { 'a' }
            }
            class MySuperClass {
                protected def m() { 'b' }
            }
            class MyClass extends MySuperClass implements MyTrait {}
            def myClass = new MyClass()
            print myClass.m()
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Test protected method of superclass overriding by trait method - the same package
    void testTraits45() {
        String[] sources = [
            'Sample.groovy', '''
            def myClass = new a.MyClass()
            print myClass.m()
            ''',

            'Stuff.groovy', '''
            package a
            trait MyTrait {
                def m() { 'a' }
            }
            class MySuperClass {
                protected def m() { 'b' }
            }
            class MyClass extends MySuperClass implements MyTrait {}
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Test protected method of superclass overriding by trait method - different packages
    void testTraits46() {
        String[] sources = [
            'Sample.groovy', '''
            def myClass = new c.MyClass()
            print myClass.m()
            ''',

            'MyTrait.groovy', '''
            package a
            trait MyTrait {
                def m() { 'a' }
            }
            ''',

            'MySuperClass.groovy', '''
            package b
            class MySuperClass {
                protected def m() { 'b' }
            }
            ''',

            'MyClass.groovy', '''
            package c
            class MyClass extends b.MySuperClass implements a.MyTrait {}
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Test protected method of superclass overriding by trait method - different packages
    void testTraits47() {
        String[] sources = [
            'Sample.groovy', '''
            def myClass = new c.MyClass()
            print myClass.m()
            ''',

            'MyTrait.groovy', '''
            package a
            trait MyTrait {
                def m() { 'a' }
            }
            ''',

            'MySuperClass.groovy', '''
            package b
            class MySuperClass {
                protected def m() { 'b' }
            }
            ''',

            'MyClass.groovy', '''
            package c
            import a.MyTrait
            import b.MySuperClass
            class MyClass extends MySuperClass implements MyTrait {}
            '''
        ]

        runConformTest(sources, 'a')
    }

    // Test protected method of superclass and traits method overriding by class
    void testTraits48() {
        String[] sources = [
            'Sample.groovy', '''
            trait MyTrait {
                def m() { 'a' }
            }
            class MySuperClass {
                protected def m() { 'b' }
            }
            class MyClass extends MySuperClass implements MyTrait {
                def m() { 'c' }
            }
            def myClass = new MyClass()
            print myClass.m()
            '''
        ]

        runConformTest(sources, 'c')
    }

    // Test protected method of superclass and traits method overriding by class - negative test
    void testTraits49() {
        String[] sources = [
            'Sample.groovy', '''\
            trait MyTrait {
                abstract def m()
            }
            class MySuperClass {
                protected def m() { 'b' }
            }
            class MyClass extends MySuperClass implements MyTrait {}
            def myClass = new MyClass()
            print myClass.m()
            '''.stripIndent()
        ]

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 7)\n" +
            "\tclass MyClass extends MySuperClass implements MyTrait {}\n" +
            "\t      ^^^^^^^\n" +
            "The inherited method MySuperClass.m() cannot hide the public abstract method in MyTrait\n" +
            "----------\n")
    }

    // Test protected method of superclass and traits method overriding by class - positive test
    void testTraits50() {
        String[] sources = [
            'Sample.groovy', '''
            trait MyTrait {
                abstract def m()
            }
            class MySuperClass {
                protected def m() { 'b' }
            }
            class MyClass extends MySuperClass implements MyTrait {
                def m() { 'c' }
            }
            def myClass = new MyClass()
            print myClass.m()
            '''
        ]

        runConformTest(sources, 'c')
    }

    // Java classes should be able to implement traits as well...
    void _testTraitsInteroperability() {
        String[] sources = [
            'Sample.java', '''
            public class Sample implements Valuable {
                public String showMeTheMoney() {
                    return "$" + getValue() + "$";
                }
            }
            ''',
            'Valuable.groovy', '''
            trait Valuable {
                String value
            }
            '''
        ]

        runConformTest(sources)
    }
}
