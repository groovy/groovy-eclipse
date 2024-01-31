/*
 * Copyright 2009-2024 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.junit.Test;

public final class TraitsTests extends GroovyCompilerTestSuite {

    @Test
    public void testTraits1() {
        //@formatter:off
        String[] sources = {
            "T.groovy",
            "trait T {\n" +
            "  String getFoo() {\n" +
            "    'foo'\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("T.groovy",
            "public @groovy.transform.Trait interface T {\n" +
            "  public @org.codehaus.groovy.transform.trait.Traits.Implemented String getFoo();\n" +
            "}");
    }

    @Test
    public void testTraits1a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C implements T {\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  final String foo = 'foo'\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkGCUDeclaration("C.groovy",
            "public class C implements T {\n" +
            "  public @groovy.transform.Generated C() {\n" +
            "  }\n" +
          //"  public @groovy.transform.Generated @org.codehaus.groovy.transform.trait.Traits.TraitBridge String getFoo() {\n" +
          //"  }\n" +
            "}");
        checkGCUDeclaration("T.groovy",
            "public @groovy.transform.Trait interface T {\n" +
          //"  public @groovy.transform.Generated @org.codehaus.groovy.transform.trait.Traits.Implemented String getFoo();\n" +
            "}");
    }

    @Test
    public void testTraits2() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "print p.greeting()\n",

            "Greetable.groovy",
            "trait Greetable {\n" +
            "  abstract String name()\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello, Bob!");
    }

    @Test
    public void testTraits2a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "print p.greeting()\n",

            "Greetable.groovy",
            "import groovy.transform.Trait;\n" +
            "@Trait\n" +
            "class Greetable {\n" +
            "  abstract String name()\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello, Bob!");
    }

    @Test // Abstract Methods
    public void testTraits3() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Greetable {\n" +
            "  abstract String name()\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}\n" +
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "print p.greeting()\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello, Bob!");
    }

    @Test // Private Methods - positive test
    public void testTraits4() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Greeter {\n" +
            "  private String greetingMessage() {\n" +
            "    'Hello from a private method!'\n" +
            "  }\n" +
            "  String greet() {\n" +
            "    def m = greetingMessage()\n" +
            "    println m\n" +
            "    m\n" +
            "  }\n" +
            "}\n" +
            "class GreetingMachine implements Greeter {}\n" +
            "def g = new GreetingMachine()\n" +
            "g.greet()\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello from a private method!");
    }

    @Test // Private Methods - negative test
    public void testTraits4a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Greeter {\n" +
            "  private String greetingMessage() {\n" +
            "    'Hello from a private method!'\n" +
            "  }\n" +
            "  String greet() {\n" +
            "    def m = greetingMessage()\n" +
            "    println m\n" +
            "    m\n" +
            "  }\n" +
            "}\n" +
            "class GreetingMachine implements Greeter {}\n" +
            "def g = new GreetingMachine()\n" +
            "try {\n" +
            "  g.greetingMessage()\n" +
            "  throw new AssertionError()\n" +
            "} catch (MissingMethodException e) {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // Meaning of this
    public void testTraits5() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Introspector {\n" +
            "  def whoAmI() { this.getClass() }\n" +
            "}\n" +
            "class Foo implements Introspector {}\n" +
            "def foo = new Foo()\n" +
            "print foo.whoAmI()\n",
        };
        //@formatter:on

        runConformTest(sources, "class Foo");
    }

    @Test
    public void testTraits5a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Introspector {\n" +
            "  void whoAmI() { print this }\n" +
            "}\n" +
            "class Foo implements Introspector {\n" +
            "  String toString() { 'Foo' }\n" +
            "}\n" +
            "def foo = new Foo()\n" +
            "foo.whoAmI()\n",
        };
        //@formatter:on

        runConformTest(sources, "Foo");
    }

    @Test // Interfaces
    public void testTraits6() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface Named {\n" +
            "  String name()\n" +
            "}\n" +
            "trait Greetable implements Named {\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}\n" +
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "print p.greeting()\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello, Bob!");

        CompilationUnit unit = getCUDeclFor("Script.groovy").getCompilationUnit();
        ClassNode classNode = unit.getClassNode("Person");
        ClassNode face = unit.getClassNode("Greetable");
        assertTrue(classNode.implementsInterface(face));
        face = unit.getClassNode("Named");
        assertTrue(classNode.implementsInterface(face));
    }

    @Test // Properties
    public void testTraits7() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Named {\n" +
            "  String name\n" +
            "}\n" +
            "class Person implements Named {}\n" +
            "def p = new Person(name: 'Bob')\n" +
            "print p.name == 'Bob'\n" +
            "print p.getName()\n",
        };
        //@formatter:on

        runConformTest(sources, "trueBob");
    }

    @Test // Private fields
    public void testTraits8() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Counter {\n" +
            "  private int count = 0\n" +
            "  int count() { count += 1; count }\n" +
            "}\n" +
            "class Foo implements Counter {}\n" +
            "def f = new Foo()\n" +
            "print f.count()\n",
        };
        //@formatter:on

        runConformTest(sources, "1");
    }

    @Test // Public fields
    public void testTraits9() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Named {\n" +
            "  public String name\n" +
            "}\n" +
            "class Person implements Named {}\n" +
            "def p = new Person()\n" +
            "p.Named__name = 'Bob'\n" +
            "print p.Named__name\n",
        };
        //@formatter:on

        runConformTest(sources, "Bob");
    }

    @Test // Composition of Behaviors
    public void testTraits10() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait FlyingAbility {\n" +
            "  String fly() { \"I'm flying!\" }\n" +
            "}\n" +
            "trait SpeakingAbility {\n" +
            "  String speak() { \"I'm speaking!\" }\n" +
            "}\n" +
            "class Duck implements FlyingAbility, SpeakingAbility {}\n" +
            "def d = new Duck()\n" +
            "print d.fly()\n" +
            "print d.speak()\n",
        };
        //@formatter:on

        runConformTest(sources, "I'm flying!I'm speaking!");
    }

    @Test // Overriding default methods
    public void testTraits11() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait FlyingAbility {\n" +
            "  String fly() { \"I'm flying!\" }\n" +
            "}\n" +
            "trait SpeakingAbility {\n" +
            "  String speak() { \"I'm speaking!\" }\n" +
            "}\n" +
            "class Duck implements FlyingAbility, SpeakingAbility {\n" +
            "  String quack() { \"Quack!\" }\n" +
            "  String speak() { quack() }\n" +
            "}\n" +
            "def d = new Duck()\n" +
            "print d.fly()\n" +
            "print d.quack()\n" +
            "print d.speak()\n",
        };
        //@formatter:on

        runConformTest(sources, "I'm flying!Quack!Quack!");
    }

    @Test // Simple Inheritance
    public void testTraits12() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Named {\n" +
            "  String name\n" +
            "}\n" +
            "trait Polite extends Named {\n" +
            "  String introduce() { \"Hello, I am $name\" }\n" +
            "}\n" +
            "class Person implements Polite {}\n" +
            "def p = new Person(name: 'Alice')\n" +
            "print p.introduce()\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello, I am Alice");
    }

    @Test // Multiple Inheritance
    public void testTraits13() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Pogo implements Identified { }\n" +
            "def obj = new Pogo(name: 'Frank Grimes')\n" +
            "print obj.getName()\n",

            "Identified.groovy",
            "trait WithId {\n" +
            "  Long id\n" +
            "}\n" +
            "trait WithName {\n" +
            "  String name\n" +
            "}\n" +
            "trait Identified implements WithId, WithName {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "Frank Grimes");
    }

    @Test // Dynamic code
    public void testTraits14() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait SpeakingDuck {\n" +
            "  String speak() { quack() }\n" +
            "}\n" +
            "class Duck implements SpeakingDuck {\n" +
            "  String methodMissing(String name, args) {\n" +
            "    \"${name.capitalize()}!\"\n" +
            "  }\n" +
            "}\n" +
            "def d = new Duck()\n" +
            "print d.speak()\n",
        };
        //@formatter:on

        runConformTest(sources, "Quack!");
    }

    @Test // Dynamic methods in trait
    public void testTraits15() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait DynamicObject {\n" +
            "  private Map props = [:]\n" +
            "  def methodMissing(String name, args) {\n" +
            "    name.toUpperCase()\n" +
            "  }\n" +
            "  def propertyMissing(String prop) {\n" +
            "    props['prop']\n" +
            "  }\n" +
            "  void setProperty(String prop, Object value) {\n" +
            "    props['prop'] = value\n" +
            "  }\n" +
            "}\n" +
            "class Dynamic implements DynamicObject {\n" +
            "  String existingProperty = 'ok'\n" +
            "  String existingMethod() { 'ok' }\n" +
            "}\n" +
            "def d = new Dynamic()\n" +
            "print d.existingProperty\n" +
            "print d.foo\n" +
            "d.foo = 'bar'\n" +
            "print d.foo\n" +
            "print d.existingMethod()\n" +
            "print d.someMethod()\n",
        };
        //@formatter:on

        runConformTest(sources, "oknullbarokSOMEMETHOD");
    }

    @Test // Multiple inheritance conflicts - Default conflict resolution
    public void testTraits16() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  String exec() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String exec() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {}\n" +
            "def c = new C()\n" +
            "print c.exec()\n",
        };
        //@formatter:on

        runConformTest(sources, "B");
    }

    @Test // Multiple inheritance conflicts - Default conflict resolution
    public void testTraits17() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  String getIdentity() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String getIdentity() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "}\n" +
            "print new C().getIdentity()\n",
        };
        //@formatter:on

        runConformTest(sources, "B");
    }

    @Test
    public void testTraits17a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  final String identity = 'A'\n" +
            "}\n" +
            "trait B {\n" +
            "  final String identity = 'B'\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "}\n" +
            "print new C().getIdentity()\n",
        };
        //@formatter:on

        runConformTest(sources, "B");
    }

    @Test
    public void testTraits17b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  String getIdentity() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String getIdentity() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "}\n" +
            "trait T {\n" +
            "  String getIdentity() { 'T' }\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            "}\n" +
            "print new D().getIdentity()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test // Multiple inheritance conflicts - User conflict resolution
    public void testTraits18() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  String getIdentity() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String getIdentity() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  String getIdentity() { A.super.getIdentity() }\n" +
            "}\n" +
            "print new C().getIdentity()\n",
        };
        //@formatter:on

        runConformTest(sources, "A");
    }

    @Test // Implementing a trait at runtime
    public void testTraits19() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Extra {\n" +
            "  String extra() { 'Extra' }\n" +
            "}\n" +
            "class Something {\n" +
            "  String doSomething() { 'Something' }\n" +
            "}\n" +
            "def s = new Something() as Extra\n" +
            "print s.extra()\n" +
            "print s.doSomething()\n",
        };
        //@formatter:on

        runConformTest(sources, "ExtraSomething");
    }

    @Test // Implementing multiple traits at once - negative
    public void testTraits20() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A { String methodFromA() { 'A' } }\n" +
            "trait B { String methodFromB() { 'B' } }\n" +
            "class C {}\n" +
            "def c = new C()\n" +
            "print c.methodFromA()\n" +
            "print c.methodFromB()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
            "No signature of method: C.methodFromA() is applicable for argument types: () values: []");
    }

    @Test // Implementing multiple traits at once - positive
    public void testTraits21() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A { String methodFromA() { 'A' } }\n" +
            "trait B { String methodFromB() { 'B' } }\n" +
            "class C {}\n" +
            "def c = new C()\n" +
            "def d = c.withTraits(A, B)\n" +
            "print d.methodFromA()\n" +
            "print d.methodFromB()\n",
        };
        //@formatter:on

        runConformTest(sources, "AB");
    }

    @Test // Chaining behavior
    public void testTraits22() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "class SimpleHandler implements DefaultHandler {}\n" +
            "def handler = new SimpleHandler()\n" +
            "handler.on('test logging', [:])\n",
        };
        //@formatter:on

        runConformTest(sources, "Received test logging with payload [:]");
    }

    @Test // Chaining behavior
    public void testTraits23() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "class SimpleHandlerWithLogging implements DefaultHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    println \"Seeing $message with payload $payload\"\n" +
            "    DefaultHandler.super.on(message, payload)\n" +
            "  }\n" +
            "}\n" +
            "def handler = new SimpleHandlerWithLogging()\n" +
            "handler.on('test logging', [:])\n",
        };
        //@formatter:on

        runConformTest(sources, "Seeing test logging with payload [:]\nReceived test logging with payload [:]");
    }

    @Test // Chaining behavior
    public void testTraits24() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "trait SayHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    if (message.startsWith('say')) {\n" +
            "      println \"I say ${message - 'say'}!\"\n" +
            "    } else {\n" +
            "      super.on(message, payload)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "trait LoggingHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    println \"Seeing $message with payload $payload\"\n" +
            "    super.on(message, payload)\n" +
            "  }\n" +
            "}\n" +
            "class Handler implements DefaultHandler, SayHandler, LoggingHandler {}\n" +
            "def handler = new Handler()\n" +
            "handler.on('foo', [:])\n" +
            "handler.on('sayHello', [:])\n",
        };
        //@formatter:on

        runConformTest(sources, "Seeing foo with payload [:]\nReceived foo with payload [:]\nSeeing sayHello with payload [:]\nI say Hello!");
    }

    @Test // Chaining behavior
    public void testTraits25() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "trait SayHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    if (message.startsWith('say')) {\n" +
            "      println \"I say ${message - 'say'}!\"\n" +
            "    } else {\n" +
            "      super.on(message, payload)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "trait LoggingHandler implements MessageHandler {\n" +
            "  void on(String message, Map<?, ?> payload) {\n" +
            "    println \"Seeing $message with payload $payload\"\n" +
            "    super.on(message, payload)\n" +
            "  }\n" +
            "}\n" +
            "class AlternateHandler implements DefaultHandler, LoggingHandler, SayHandler {}\n" +
            "def handler = new AlternateHandler()\n" +
            "handler.on('foo', [:])\n" +
            "handler.on('sayHello', [:])\n",
        };
        //@formatter:on

        runConformTest(sources, "Seeing foo with payload [:]\nReceived foo with payload [:]\nI say Hello!");
    }

    @Test // Chaining behavior - Semantics of super inside a trait
    public void testTraits26() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Filtering {\n" +
            "  StringBuilder append(String str) {\n" +
            "    def subst = str.replace('o', '')\n" +
            "    super.append(subst)\n" +
            "  }\n" +
            "  String toString() { super.toString() }\n" +
            "}\n" +
            "def sb = new StringBuilder().withTraits Filtering\n" +
            "sb.append('Groovy')\n" +
            "print sb.toString()\n",
        };
        //@formatter:on

        runConformTest(sources, "Grvy");
    }

    @Test // SAM type coercion
    public void testTraits27() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Greeter {\n" +
            "  String greet() { \"Hello $name\" }\n" +
            "  abstract String getName()\n" +
            "}\n" +
            "Greeter greeter = { 'Alice' }\n" +
            "print greeter.getName()\n",
        };
        //@formatter:on

        runConformTest(sources, "Alice");
    }

    @Test // SAM type coercion
    public void testTraits28() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Greeter {\n" +
            "  String greet() { \"Hello $name\" }\n" +
            "  abstract String getName()\n" +
            "}\n" +
            "void greet(Greeter g) { println g.greet() }\n" +
            "greet { 'Alice' }\n",
        };
        //@formatter:on

        runConformTest(sources, "Hello Alice");
    }

    @Test // Differences with Java 8 default methods
    public void testTraits29() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Person {\n" +
            "  String name\n" +
            "}\n" +
            "trait Bob {\n" +
            "  String getName() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person(name: 'Alice')\n" +
            "print p.name\n" +
            "def p2 = p as Bob\n" +
            "print p2.name\n",
        };
        //@formatter:on

        runConformTest(sources, "AliceBob");
    }

    @Test // Differences with mixins
    public void testTraits30() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class A { String methodFromA() { 'A' } }\n" +
            "class B { String methodFromB() { 'B' } }\n" +
            "A.metaClass.mixin B\n" +
            "def o = new A()\n" +
            "print o.methodFromA()\n" +
            "print o.methodFromB()\n" +
            "print(o instanceof A)\n" +
            "print(o instanceof B)\n",
        };
        //@formatter:on

        runConformTest(sources, "ABtruefalse");
    }

    @Test // Static methods, properties and fields
    public void testTraits31() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait TestHelper {\n" +
            "  public static boolean called = false\n" +
            "  static boolean getState() { called }\n" +
            "  static void init() {\n" +
            "    called = true\n" +
            "  }\n" +
            "}\n" +
            "class Foo implements TestHelper {}\n" +
            "Foo.init()\n" +
            "print Foo.state\n" +
            "print Foo.TestHelper__called\n",
        };
        //@formatter:on

        runConformTest(sources, "truetrue");
    }

    @Test // Static methods, properties and fields
    public void testTraits32() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait TestHelper {\n" +
            "  public static boolean called = false\n" +
            "  static void init() {\n" +
            "    called = true\n" +
            "  }\n" +
            "}\n" +
            "class Bar implements TestHelper {}\n" +
            "class Baz implements TestHelper {}\n" +
            "Bar.init()\n" +
            "print Bar.TestHelper__called\n" +
            "print Baz.TestHelper__called\n",
        };
        //@formatter:on

        runConformTest(sources, "truefalse");
    }

    @Test // Inheritance of state gotchas
    public void testTraits33() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  int x = 1\n" +
            "  int y = 2\n" +
            "  int f() { x + y }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  int g() { f() }\n" +
            "}\n" +
            "print new C().g()\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test // Inheritance of state gotchas
    public void testTraits34() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  int x = 1\n" +
            "  int y = 2\n" +
            "  int f() { x + y }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  int x = 3\n" +
            "  int y = 4\n" +
            "  int g() { f() }\n" +
            "}\n" +
            "print new C().g()\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test // Inheritance of state gotchas
    public void testTraits35() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  int x = 1\n" +
            "  int y = 2\n" +
            "  int f() { getX() + getY() }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  int x = 3\n" +
            "  int y = 4\n" +
            "  int g() { f() }\n" +
            "}\n" +
            "print new C().g()\n",
        };
        //@formatter:on

        runConformTest(sources, "7");
    }

    @Test // Limitations - Prefix and postfix operations
    public void testTraits36() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Counting {\n" +
            "  int x\n" +
            "  void inc() {\n" +
            "    x++\n" +
            "  }\n" +
            "  void dec() {\n" +
            "    --x\n" +
            "  }\n" +
            "}\n" +
            "class Counter implements Counting {}\n" +
            "def c = new Counter()\n" +
            "c.inc()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 4)\n" +
            "\tx++\n" +
            "\t ^\n" +
            "Groovy:Postfix expressions on trait fields/properties " + (!isAtLeastGroovy(40) ? " " : "") + "are not supported in traits.\n" +
            "----------\n" +
            "2. ERROR in Script.groovy (at line 7)\n" +
            "\t--x\n" +
            "\t^\n" +
            "Groovy:Prefix expressions on trait fields/properties are not supported in traits.\n" +
            "----------\n");
    }

    @Test // @Trait annotation
    public void testTraits37() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test // @Trait annotation
    public void testTraits38() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.Trait\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test // @Trait annotation
    public void testTraits39() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test // @Trait annotation
    public void testTraits40() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@interface Trait {\n" +
            "}\n" +
            "@Trait\n" +
            "class NotTrait {\n" +
            "}\n" +
            "class MyClass implements NotTrait {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 6)\n" +
            "\tclass MyClass implements NotTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'NotTrait', use extends instead.\n" +
            "----------\n");
    }

    @Test // @Trait annotation
    public void testTraits41() {
        //@formatter:off
        String[] sources = {
            "p/Trait.groovy",
            "package p\n" +
            "@interface Trait {\n" +
            "}\n",

            "Script.groovy",
            "import p.Trait\n" +
            "@Trait\n" +
            "class NotTrait {\n" +
            "}\n" +
            "class MyClass implements NotTrait {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 5)\n" +
            "\tclass MyClass implements NotTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'NotTrait', use extends instead.\n" +
            "----------\n");
    }

    @Test // @Trait annotation
    public void testTraits42() {
        //@formatter:off
        String[] sources = {
            "p/Trait.groovy",
            "package p\n" +
            "@interface Trait {}\n",

            "Script.groovy",
            "@p.Trait\n" +
            "class NotTrait {\n" +
            "}\n" +
            "class MyClass implements NotTrait {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 4)\n" +
            "\tclass MyClass implements NotTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'NotTrait', use extends instead.\n" +
            "----------\n");
    }

    @Test // @Trait annotation
    public void testTraits43() {
        //@formatter:off
        String[] sources = {
            "p/Trait.groovy",
            "package p\n" +
            "@interface Trait {}\n",

            "Script.groovy",
            "import p.Trait\n" +
            "import groovy.transform.*\n" +
            "@Trait\n" +
            "class NotTrait {\n" +
            "}\n" +
            "class MyClass implements NotTrait {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 6)\n" +
            "\tclass MyClass implements NotTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'NotTrait', use extends instead.\n" +
            "----------\n");
    }

    @Test // public method of superclass overridden by trait method
    public void testTraits44() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def m() { 'T' }\n" +
            "}\n" +
            "class C {\n" +
            "  def m() { 'C' }\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            // T.m overrides C.m
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test // public method of superclass overridden by static trait method
    public void testTraits45() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C {\n" +
            "  def m() { 'C' }\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            // T.m overrides C.m
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test // protected method of superclass overridden by trait method
    public void testTraits46() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def m() { 'T' }\n" +
            "}\n" +
            "class C {\n" +
            "  protected def m() { 'C' }\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            // T.m overrides C.m
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test // package-private method of superclass overridden by trait method
    public void testTraits47() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def m() { 'T' }\n" +
            "}\n" +
            "class C {\n" +
            "  @groovy.transform.PackageScope\n" +
            "  def m() { 'C' }\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            // T.m overrides C.m
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test // method of superclass overridden by (different package) trait method
    public void testTraits48() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "def myClass = new c.MyClass()\n" +
            "print myClass.m()\n",

            "MyTrait.groovy",
            "package a\n" +
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n",

            "MySuperClass.groovy",
            "package b\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n",

            "MyClass.groovy",
            "package c\n" +
            "class MyClass extends b.MySuperClass implements a.MyTrait {}\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test // protected method of superclass overriding by trait method - different packages
    public void testTraits49() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "def myClass = new c.MyClass()\n" +
            "print myClass.m()\n",

            "MyTrait.groovy",
            "package a\n" +
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n",

            "MySuperClass.groovy",
            "package b\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n",

            "MyClass.groovy",
            "package c\n" +
            "import a.MyTrait\n" +
            "import b.MySuperClass\n" +
            "class MyClass extends MySuperClass implements MyTrait {}\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test // protected method of superclass and traits method overriding by class
    public void testTraits50() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {\n" +
            "  def m() { 'c' }\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "c");
    }

    @Test // Test protected method of superclass and traits method overriding by class - negative test
    public void testTraits51() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait MyTrait {\n" +
            "  abstract def m()\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 7)\n" +
            "\tclass MyClass extends MySuperClass implements MyTrait {}\n" +
            "\t      ^^^^^^^\n" +
            "The inherited method MySuperClass.m() cannot hide the public abstract method in MyTrait\n" +
            "----------\n");
    }

    @Test // protected method of superclass and traits method overriding by class - positive test
    public void testTraits52() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait MyTrait {\n" +
            "  abstract def m()\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {\n" +
            "  def m() { 'c' }\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "c");
    }

    @Test
    public void testTraits53() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  final m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  def m() { 'C' }\n" + // "override" of final is apparently allowed
            "}\n" +
            "print new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "C");
    }

    @Test // final method of superclass cannot be overridden by trait method
    public void testTraits54() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def m() {}\n" +
            "}\n" +
            "class C {\n" +
            "  final m() {}\n" +
            "}\n" +
            "class D extends C implements T {\n" +
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 1)\n" +
            "\ttrait T {\n" +
            "\t^\n" +
            "Groovy:You are not allowed to override the final method m() from class 'C'.\n" +
            "----------\n");
    }

    @Test // final trait method cannot be overridden by subclass
    public void testTraits55() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  final m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "class D extends C {\n" +
            "  def m() {'D'}\n" +
            "}\n" +
            "print new D().m()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 7)\n" +
            "\tdef m() {'D'}\n" +
            "\t    ^^^\n" +
            "Groovy:You are not allowed to override the final method m() from class 'C'.\n" +
            "----------\n");
    }

    @Test // final trait method cannot be overridden by subclass
    public void testTraits56() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print new J().m()\n",

            "G.groovy",
            "trait T {\n" +
            "  final m() { 'T' }\n" +
            "}\n" +
            "class G implements T {\n" +
            "}\n",

            "J.java",
            "class J extends G {\n" +
            "  public Object m() {\n" +
            "    return \"J\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in J.java (at line 2)\n" +
            "\tpublic Object m() {\n" +
            "\t              ^^^\n" +
            "Cannot override the final method from T\n" +
            "----------\n");
    }

    @Test // final trait method cannot be overridden by subclass
    public void testTraits57() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "print new J().m()\n",

            "G.groovy",
            "trait T {\n" +
            "  final m() {}\n" +
            "}\n" +
            "class C {\n" +
            "  protected m() {}\n" +
            "}\n" +
            "class G extends C implements T {\n" +
            // T.m overrides C.m
            "}\n",

            "J.java",
            "class J extends G {\n" +
            "  public Object m() {\n" +
            "    return \"J\";\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in J.java (at line 2)\n" +
            "\tpublic Object m() {\n" +
            "\t              ^^^\n" +
            "Cannot override the final method from G\n" +
            "----------\n");
    }

    @Test
    public void testTraits58() {
        //@formatter:off
        String[] sources = {
            "T.groovy",
            "trait T {\n" +
            "  protected int f\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in T.groovy (at line 2)\n" +
            "\tprotected int f\n" +
            "\t^\n" +
            "Groovy:Cannot have protected field in a trait (T#f)\n" +
            "----------\n");
    }

    @Test
    public void testTraits59() {
        //@formatter:off
        String[] sources = {
            "T.groovy",
            "trait T {\n" +
            "  protected int m() {}\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in T.groovy (at line 2)\n" +
            "\tprotected int m() {}\n" +
            "\t^\n" +
            "Groovy:Cannot have protected/package-private method in a trait (T#int m())\n" +
            "----------\n");
    }

    @Test
    public void testTraits60() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  @groovy.transform.PackageScope int f = 42\n" +
            "  def m() { print f }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTraits61() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  @groovy.transform.PackageScope int m() {\n" +
            "    42\n" +
            "  }\n" +
            "  def x() { print m() }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().x()\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Script.groovy (at line 2)\n" +
            "\t@groovy.transform.PackageScope int m() {\n" +
            "\t^\n" +
            "Groovy:Can't use @PackageScope for method 'm' which has explicit visibility.\n" +
            "----------\n");
    }

    @Test
    public void testTraits62() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  def x() { m() }\n" +
            "}\n" +
            "print new C().x()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test
    public void testTraits63() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  def x() { C.m() }\n" +
            "}\n" +
            "print new C().x()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test
    public void testTraits64() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "print C.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test
    public void testTraits65() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  static m() { 'C' }\n" +
            "}\n" +
            "print C.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "C");
    }

    @Test
    public void testTraits66() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(C.m());\n" +
            "  }\n" +
            "}\n",

            "Types.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test
    public void testTraits67() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "print T.m()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
            "No signature of method: static T.m() is applicable for argument types: () values: []");
    }

    @Test
    public void testTraits68() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(T.m());\n" +
            "  }\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  static m() {}\n" +
            "}\n",
        };
        //@formatter:on

        if (isAtLeastJava(JDK9)) {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in Main.java (at line 3)\n" +
                "\tSystem.out.print(T.m());\n" +
                "\t                   ^\n" +
                "The method m() from the type T is not visible\n" +
                "----------\n");
        } else { // TODO: This is not ideal:
            runConformTest(sources, "", "java.lang.NoSuchMethodError");
        }
    }

    @Test
    public void testTraits69() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.print(C.m());\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "trait T {\n" +
            "  static m() { 'T' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test
    public void testTraits70() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main extends C {\n" +
            "  static main(args) {\n" +
            "    // TODO\n" +
            "  }\n" +
            "}\n",

            "C.groovy",
            "class C implements T, U {\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  void one() {}\n" +
            "}\n",

            "U.groovy",
            "trait U {\n" +
            "  void two() {}\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test // trait forwarding
    public void testTraits7135() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.TypeChecked\n" +
            "trait T {\n" +
            "  def foo() {\n" +
            "    super.bar()\n" + // $self.Ttrait$super$bar(); could fail STC...
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().foo()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
            "No signature of method: C.Ttrait$super$bar() is applicable for argument types: () values: []");
    }

    @Test
    public void testTraits7191() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static Number bar() { 1 }\n" +
            "         Number foo() { bar() }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "class D extends C {\n" +
            "}\n" +
            "print(new C().foo())\n" +
            "print(new D().foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "11");
    }

    @Test
    public void testTraits7213() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  private String secret() { 'secret' }\n" +
            "  String foo() { secret() }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "print(new C().foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "secret");
    }

    @Test
    public void testTraits7242() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def f() {\n" +
            "    ['a'].collect { String s -> g(s) }\n" +
            "  }\n" +
            "  String g(String s) {\n" +
            "    s.toUpperCase()\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "def c = new C()\n" +
            "assert c.f() == ['A']\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits7242a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def f() {\n" +
            "    ['a'].collect { g(it) }\n" +
            "  }\n" +
            "  String g(String s) {\n" +
            "    s.toUpperCase()\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "def c = new C()\n" +
            "assert c.f() == ['A']\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits7255() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static List stuff = [1,2,3]\n" +
            "  static initStuff(List list) {\n" +
            "    stuff = stuff + list\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "C.initStuff([4,5,6])\n" +
            "print(C.stuff)\n",
        };
        //@formatter:on

        runConformTest(sources, "[1, 2, 3, 4, 5, 6]");
    }

    @Test
    public void testTraits7288() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  final foo = 'bar'\n" +
            "}\n" +
            "class D implements T {\n" +
            "  def m() {\n" +
            "    print 'works'\n" +
            "  }\n" +
            "}\n" +
            "class C {\n" + // The class must be declared abstract or the method 'java.lang.String T__foo$get()' must be implemented
            "  @Delegate D provider = new D()\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits7293() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  @groovy.transform.Memoized\n" +
            "  long traitLongComputation(int seed) {\n" +
            "    System.nanoTime()\n" +
            "  }\n" +
            "}\n" +
            "class C {\n" +
            "  @groovy.transform.Memoized\n" +
            "  long classLongComputation(int seed) {\n" +
            "    System.nanoTime()\n" +
            "  }\n" +
            "}\n" +
            "def ct = new C() as T\n" +
            "assert ct.classLongComputation(1) == ct.classLongComputation(1)\n"+
            "assert ct.traitLongComputation(1) == ct.traitLongComputation(1)\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits7322() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static String bar() { 'static method' }\n" +
            "  static String foo() { bar() }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "class D extends C {\n" +
            "}\n" +
            "assert C.foo() == 'static method'\n" +
            "assert D.foo() == 'static method'\n" +
            "assert new C().foo() == 'static method'\n" +
            "assert new D().foo() == 'static method'\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits7399() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Bar implements FooTrait {\n" +
            "  def whoAmI() { \"It's Bar\" }\n" +
            "}\n" +
            "class Foo {\n" +
            "  def whoAmI() { \"It's Foo\" }\n" +
            "}\n" +
            "trait FooTrait {\n" +
            "  Foo f = new Foo()\n" +
            "  def hiFoo() {\n" +
            "    f.with {\n" +
            "      whoAmI()\n" + // Is it Foo or Bar?
            "    }\n" +
            "  }\n" +
            "}\n" +
            "def b = new Bar()\n" +
            "assert b.hiFoo() == b.f.whoAmI()\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits7456() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def f() {\n" +
            "    ['a'].collect { String s -> g(s) }\n" +
            "  }\n" +
            "  private String g(String s) {\n" +
            "    s.toUpperCase()\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "assert new C().f() == ['A']\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits7512() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  Closure<Void> bar\n" +
            "}\n" +
            "trait T {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  Foo getFoo() {\n" +
            "    new Foo(bar: { ->\n" +
            "      baz 'zzz'\n" + // ClassCastException: java.lang.Class cannot be cast to T
            "    })\n" +
            "  }\n" +
            "  void baz(text) {\n" +
            "    println text\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().foo.bar()\n",
        };
        //@formatter:on

        runConformTest(sources, "zzz");
    }

    @Test
    public void testTraits7512a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class Foo {\n" +
            "  Closure<Void> bar\n" +
            "}\n" +
            "trait T {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  Foo getFoo() {\n" +
            "    Foo foo = new Foo()\n" +
            "    foo.bar = { -> baz 'zzz' }\n" +
            "    foo\n" +
            "  }\n" +
            "  void baz(text) {\n" +
            "    println text\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().foo.bar()\n",
        };
        //@formatter:on

        runConformTest(sources, "zzz");
    }

    @Test
    public void testTraits7759() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  @Lazy String x = { -> 'works' }()\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "print new C().getX()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits7843() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "trait T {\n" +
            "  void m(Closure block) {\n" +
            "    block.call()\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "trait U extends T {\n" +
            "  void threeDeep() {\n" +
            "    m {\n" +
            "      m {\n" +
            "        m {\n" +
            "          print 'works'\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "class C implements U {\n" +
            "}\n" +
            "new C().threeDeep()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits7909() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            // defining Three before One and Two
            "trait Three implements One, Two {\n" +
            "  def postMake() {\n" +
            "    One.super.postMake()\n" +
            "    Two.super.postMake()\n" +
            "    print 'Three'\n" +
            "  }\n" +
            "}\n" +
            "trait One {\n" +
            "  def postMake() { print 'One'}\n" +
            "}\n" +
            "trait Two {\n" +
            "  def postMake() { print 'Two'}\n" +
            "}\n" +
            "class Four implements Three {\n" +
            "  def make() {\n" +
            "    Three.super.postMake()\n" +
            "    print 'Four'\n" +
            "  }\n" +
            "}\n" +
            "new Four().make()\n",
        };
        //@formatter:on

        runConformTest(sources, "OneTwoThreeFour");
    }

    @Test
    public void testTraits7950() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  String bar\n" +
            "}\n" +
            "@groovy.transform.TupleConstructor(includes='foo,bar', allProperties=true)\n" +
            "class C implements T {\n" +
            "  String foo\n" +
            "}\n" +
            "def c = new C('foo','bar')\n" +
            "print c.foo\n" +
            "print c.bar\n",
        };
        //@formatter:on

        runConformTest(sources, "foobar");
    }

    @Test
    public void testTraits8000() {
        //@formatter:off
        String[] sources = {
            "Implementation.groovy",
            "trait TopTrait<X> { X getSomeThing() {}\n" +
            "}\n" +
            "trait MiddleTrait<Y> implements TopTrait<Y> {\n" +
            "}\n" +
            "trait BottomTrait<Z> implements MiddleTrait<Z> {\n" +
            "}\n" +
            "class Implementation implements BottomTrait<String> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // see also GROOVY-7135
    public void testTraits8021() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "trait T {\n" +
            "  void m() {\n" +
            "    super.m()\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
            "No signature of method: java.lang.Object.m() is applicable for argument types: () values: []");
    }

    @Test
    public void testTraits8049() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "interface Foo {\n" +
            "  String getBar()\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "trait T {\n" +
            "  abstract Foo getFoo()\n" +
            "  def m() {\n" +
            "    foo.with {\n" +
            "      bar.toUpperCase()\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "class C implements T {\n" +
            "  Foo foo = { -> 'works' } as Foo\n" +
            "}\n" +
            "print new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "WORKS");
    }

    @Test // see also GROOVY-7950
    public void testTraits8219() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def x = 42\n" +
            "}\n" +
            "@groovy.transform.TupleConstructor(includeFields=true)\n" + // works without includeFields or with trait field T__x excluded
            "class A implements T {\n" +
            "  def a\n" +
            "  private b\n" +
            "}\n" +
            "print new A().x\n", // overrides default
        };
        //@formatter:on

        runConformTest(sources, isAtLeastGroovy(50) ? "42" : "null");
    }

    @Test
    public void testTraits8243() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  abstract foo(int n)\n" +
            "  def bar(double n) {\n" +
            "    print \"bar $n;\"\n" +
            "  }\n" +
            "}\n" +
            "interface I extends T {\n" +
            "}\n" +
            "I i = { print \"foo $it;\" }\n" + // should proxy only foo
            "i.foo(123)\n" +
            "i.bar(4.5)\n",
        };
        //@formatter:on

        runConformTest(sources, "foo 123;bar 4.5;");
    }

    @Test
    public void testTraits8244() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  abstract def foo(a, b = 2)\n" +
            "}\n" +
            "T t = { x, y ->\n" + // should proxy only foo(a,b)
            "  print \"$x,$y\"\n" +
            "}\n" +
            "t.foo(1)\n", // MissingMethodException
        };
        //@formatter:on

        runConformTest(sources, "1,2");
    }

    @Test
    public void testTraits8272() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  static sm() {\n" +
            "    'A'\n" +
            "  }\n" +
            "}\n" +
            "trait B extends A {\n" +
            "  String m() {\n" +
            "    sm().toLowerCase()\n" +
            "  }\n" +
            "}\n" +
            "class C implements B {\n" +
            "}\n" +
            "print(new C().m())\n",
        };
        //@formatter:on

        runConformTest(sources, "a");
    }

    @Test
    public void testTraits8587() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  def m() {\n" +
            "    'A'\n" +
            "  }\n" +
            "}\n" +
            "trait B extends A {\n" +
            "}\n" +
            "class C implements B {\n" +
            "  @Override\n" +
            "  def m() {\n" +
            "    B.super.m()\n" + // MissingMethodException
            "  }\n" +
            "}\n" +
            "print new C().m()\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(50)) {
            runConformTest(sources, "A");
        } else {
            runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
                "No signature of method: " + (isAtLeastGroovy(40) ? "B$Trait$Helper" : "static B") + ".m() is applicable for argument types: (C) values: [C");
        }
    }

    @Test
    public void testTraits8820() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def m() {\n" +
            "    String[] strings = []\n" +
            "    strings.with {\n" +
            "      length\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "print new C().m()\n",
        };
        //@formatter:on

        runConformTest(sources, "0");
    }

    @Test
    public void testTraits8854() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static class Child extends Pogo {}\n" +
            "  static main(args) { new Child(name:'foo').audit() }\n" +
            "}\n",

            "Pogo.groovy",
            "class Pogo implements Auditable {\n" +
            "  String name\n" +
            "}\n",

            "Auditable.groovy",
            "trait Auditable {\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" + // MissingMethodException
            "      print ' '\n" +
            "    }\n" +
            "    print 'audited'\n" +
            "  }\n" +
            "  private static boolean check() {\n" +
            "    print 'checked'\n" +
            "    return true\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "checked audited");
        } else {
            runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
                "No signature of method: static Main.check() is applicable for argument types: () values: []");
        }
    }

    @Test
    public void testTraits8856() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main {\n" +
            "  static T myMethod() {\n" +
            "    return [1, 2, 3]\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\tstatic T myMethod() {\n" +
            "\t       ^\n" +
            "Groovy:unable to resolve class T\n" +
            "----------\n");
    }

    @Test
    public void testTraits8859() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  public  String bar() {\n" +
            "    'public '\n" +
            "  }\n" +
            "  private String baz() {\n" +
            "    'private'\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  def foo() {\n" +
            "    bar() + baz()\n" +
            "  }\n" +
            "}\n" +
            "print(new C().foo())\n",
        };
        //@formatter:on

        runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
            "No signature of method: C.baz() is applicable for argument types: () values: []");
    }

    @Test
    public void testTraits8954() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface DomainProp {\n" +
            "  boolean isNullable()\n" +
            "}\n" +
            "abstract class OrderedProp implements DomainProp {\n" +
            "}\n" +
            "trait Nullable {\n" +
            "  boolean nullable = true\n" +
            "}\n" +
            "abstract class CustomProp extends OrderedProp implements Nullable {\n" +
            "}\n" +
            "new CustomProp() {}\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits9031() {
        //@formatter:off
        String[] sources = {
            "Trait9031.groovy",
            "trait Trait9031<V> {\n" +
            "  V value\n" +
            "}\n",

            "Class9031.groovy",
            "class Class9031 implements Trait9031<String> {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");

        checkDisassemblyFor("Class9031.class",
            "  public bridge synthetic java.lang.Object getValue();\n");
        checkDisassemblyFor("Class9031.class",
            "  @org.codehaus.groovy.transform.trait.Traits.TraitBridge(traitClass=Trait9031,\n" +
            "    desc=\"()Ljava/lang/Object;\")\n" +
            "  public java.lang.String getValue();\n");

        checkDisassemblyFor("Class9031.class",
            "  public bridge synthetic void setValue(java.lang.Object arg0);\n");
        checkDisassemblyFor("Class9031.class",
            "  @org.codehaus.groovy.transform.trait.Traits.TraitBridge(traitClass=Trait9031,\n" +
            "    desc=\"(Ljava/lang/Object;)V\")\n" +
            "  public void setValue(java.lang.String value);\n");
    }

    @Test
    public void testTraits9255() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def proper = 'value'\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testTraits9255a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  boolean isProper() {}\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "false");
    }

    @Test
    public void testTraits9255b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def proper = 'value'\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    T.super.proper += 's'\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        // TODO: runConformTest(sources, "values");
        runConformTest(sources, "", "groovy.lang.MissingPropertyException: No such property: super for class: T");
    }

    @Test
    public void testTraits9256() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  int proper = 42\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    { ->\n" +
            "      print T.super.getProper()\n" +
            "    }()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTraits9256a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  int proper = 42\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    { p = T.super.getProper() ->\n" +
            "      print p\n" +
            "    }()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTraits9386() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class P {\n" +
            "  int prop\n" +
            "}\n" +
            "trait T {\n" +
            "  P pogo = new P().with {\n" +
            "    prop = 42\n" + // MissingPropertyException: No such property: prop for class: C
            "    return it\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "print new C().pogo.prop\n",
        };
        //@formatter:on

        runConformTest(sources, "42");
    }

    @Test
    public void testTraits9586() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  def m(@DelegatesTo(strategy=Closure.OWNER_ONLY, type='Void') Closure<?> x) {\n" +
            "    x.setResolveStrategy(Closure.OWNER_ONLY)\n" +
            "    x.setDelegate(null)\n" +
            "    return x.call()\n" +
            "  }\n" +
            "  void p() { print 'C' }\n" +
            "}\n" +
            "trait T {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    new C().m { -> p() }\n" + // "p" must come from owner
            "  }\n" +
            "  void p() { print 'T' }\n" +
            "}\n" +
            "class U implements T {}\n" +
            "new U().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "T");
    }

    @Test
    public void testTraits9586a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  def m(@DelegatesTo(strategy=Closure.DELEGATE_ONLY, value=C) Closure<?> x) {\n" +
            "    x.setResolveStrategy(Closure.OWNER_ONLY)\n" +
            "    x.setDelegate(this)\n" +
            "    return x.call()\n" +
            "  }\n" +
            "  void p() { print 'C' }\n" +
            "}\n" +
            "trait T {\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void test() {\n" +
            "    new C().m { -> p() }\n" + // "p" must come from delegate
            "  }\n" +
            "  void p() { print 'T' }\n" +
            "}\n" +
            "class U implements T {}\n" +
            "new U().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "C");
    }

    @Test
    public void testTraits9672() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  static m() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  static m() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void test() {\n" +
            "    print m()\n" +
            "    print A.super.m()\n" +
            "    print B.super.m()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "BAB");
    }

    @Test
    public void testTraits9672a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static getProper() { 'value' }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testTraits9672b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static proper = 'value'\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testTraits9672c() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static boolean isProper() { true }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testTraits9672d() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static boolean proper = true\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    print T.super.proper\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "true");
    }

    @Test
    public void testTraits9672e() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static setProper(value) { print value }\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    T.super.proper = 'value'\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testTraits9672f() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static proper\n" +
            "}\n" +
            "class C implements T {\n" +
            "  void test() {\n" +
            "    T.super.proper = 'value'\n" +
            "    print getProper()\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "value");
    }

    @Test
    public void testTraits9672g() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  static setProper(value) { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  static setProper(value) { 'B' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  def test() {\n" +
            "    A.super.proper = 'value'\n" +
            "  }\n" +
            "}\n" +
            "print new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "A");
    }

    @Test
    public void testTraits9673() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  void setProp(Number n) { print 'Number' }\n" +
            "  void setProp(String s) { print 'String' }\n" +
            "}\n" +
            "trait B {\n" +
            "  void setProp(Object o) { print 'Object' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  void test() { A.super.prop = 'x' }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "String");
    }

    @Test
    public void testTraits9678() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C implements T {\n" +
            "  static main(args) {\n" +
            "    p = 2\n" +
            "    p += 1\n" +
            "    print p\n" +
            "  }\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  static p = 1\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test
    public void testTraits9678a() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C implements T {\n" +
            "  static main(args) {\n" +
            "    setP(2)\n" +
            "    setP(getP() + 1)\n" +
            "    print getP()\n" +
            "  }\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  static p = 1\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "3");
    }

    @Test
    public void testTraits9739() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class Main implements ClientSupport {\n" +
            "  static main(args) {\n" +
            "    def tester = new Main(client: new Client())\n" +
            "    assert tester.isReady() : 'unprepared'\n" +
            "    print tester.client.getValue()\n" +
            "  }\n" +
            "}\n",

            "Client.groovy",
            "class Client {\n" +
            "  def getValue() { 'works' }\n" +
            "  boolean waitForServer(int seconds) { true }\n" +
            "}\n",

            "ClientDelegate.groovy",
            "trait ClientDelegate {\n" +
            "  @Delegate Client client\n" +
            "}\n",

            "ClientSupport.groovy",
            "trait ClientSupport implements ClientDelegate {\n" +
            "  boolean isReady() {\n" +
            "    boolean ready = client.waitForServer(60)\n" +
            "    // assert, log, etc.\n" +
            "    return ready\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits9760() {
        //@formatter:off
        String[] sources = {
            "C.groovy",
            "class C implements T {\n" +
            "  static main(args) {\n" +
            "    print new C().one\n" +
            "  }\n" +
            "}\n",

            "T.groovy",
            "trait T<X> {\n" +
            "  X getOne() { 'works' }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits9763() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "@groovy.transform.CompileStatic\n" +
            "void test() {\n" +
            "  C.m({ -> print 'works'; return 0 })\n" +
            "}\n" +
            "test()\n",

            "C.groovy",
            "class C implements T {\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  static <U> U m(Closure<U> callable) { callable.call() }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits9901() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  @groovy.transform.Memoized\n" +
            "  double m() { Math.random() }\n" +
            "}\n" +
            "class C implements T {}\n" +
            "class D implements T {}\n" +

            "def c = new C()\n" +
            "def n = c.m()\n" +
            "def x = c.m()\n" +
            "def y = new C().m()\n" +
            "def z = new D().m()\n" +
            "assert n == x\n" +
            "assert n != y\n" +
            "assert n != z\n" +
            "assert y != z\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits9938() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@groovy.transform.CompileStatic\n" +
            "class Main {\n" +
            "  interface I {\n" +
            "    void m(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<?> c)\n" +
            "  }\n" +
            "  trait T {\n" +
            "    void m(@DelegatesTo(value=D, strategy=Closure.DELEGATE_FIRST) Closure<?> c) {\n" +
            "      new D().with(c)\n" +
            "    }\n" +
            "  }\n" +
            "  static class C implements T {\n" + // generates m(Closure) that delegates to T$TraitHelper#m(Closure)
            "  }\n" +
            "  static class D {\n" +
            "    void f() {\n" +
            "      print 'works'\n" +
            "    }\n" +
            "  }\n" +
            "  static main(args) {\n" +
            "    new C().m { f() }\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits10000() {
        //@formatter:off
        String[] sources = {
            "J.java",
            "class J {\n" +
            "  private String f = T.FOO;\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  public static final String FOO = 'foo'\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in J.java (at line 2)\n" +
            "\tprivate String f = T.FOO;\n" +
            "\t                     ^^^\n" +
            "FOO cannot be resolved or is not a field\n" +
            "----------\n");
    }

    @Test
    public void testTraits10102() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "import groovy.transform.*\n" +
            "trait A {\n" +
            "  String foo = 'foo'\n" +
            "  String m(String s, Closure x) {\n" +
            "    s + x()\n" +
            "  }\n" +
            "}\n" +
            "@SelfType(A)\n" +
            "trait B {\n" +
            "}\n" +
            "@SelfType(B)\n" +
            "trait C {\n" +
            "}\n" +
            "@CompileStatic\n" +
            "@SelfType(C)\n" +
            "trait D {\n" +
            "  void test() {\n" +
            "    String s = foo\n" +
            "    print(m(s) {\n" +
            "      s.toUpperCase()\n" +
            "    })\n" +
            "  }\n" +
            "}\n" +
            "class X implements A, B, C, D {\n" +
            "}\n" +
            "new X().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "fooFOO");
    }

    @Test
    public void testTraits10106() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "class C {\n" +
            "  String s\n" +
            "}\n" +
            "@groovy.transform.CompileStatic\n" +
            "trait T {\n" +
            "  final C c = new C().tap {\n" +
            "    config(it)\n" +
            "  }\n" +
            "  static void config(C c) {\n" +
            "    c.s = 'works'\n" +
            "  }\n" +
            "}\n" +
            "class X implements T {\n" +
            "}\n" +
            "print new X().c.s\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test
    public void testTraits10312() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  static staticMethod(object) {\n" +
            "    object\n" +
            "  }\n" +
            "}\n" +
            "trait B extends A {\n" +
            "  static staticMethodWithDefaultArgument(String string = 'x') {\n" +
            "    staticMethod(string)\n" + // MissingMethodException
            "  }\n" +
            "}\n" +
            "class C implements B {\n" +
            "  static one() {\n" +
            "    staticMethodWithDefaultArgument()\n" +
            "  }\n" +
            "  def two() {\n" +
            "    staticMethodWithDefaultArgument()\n" +
            "  }\n" +
            "}\n" +
            "print(C.one())\n" +
            "print(new C().two())\n",
        };
        //@formatter:on

        runConformTest(sources, "xx");
    }

    @Test // see also GROOVY-10000
    public void testTraits10312a() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait A {\n" +
            "  public static final String BANG = '!'\n" +
            "}\n" +
            "trait B implements A {\n" +
            "  static staticMethodWithDefaultArgument(String string = 'x') {\n" +
            "    string + A__BANG\n" + // MissingPropertyException
            "  }\n" +
            "}\n" +
            "class C implements B {\n" +
            "  static one() {\n" +
            "    staticMethodWithDefaultArgument()\n" +
            "  }\n" +
            "  def two() {\n" +
            "    staticMethodWithDefaultArgument()\n" +
            "  }\n" +
            "}\n" +
            "print(C.one())\n" +
            "print(new C().two())\n",
        };
        //@formatter:on

        runConformTest(sources, "x!x!");
    }

    @Test
    public void testTraits10312b() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "interface A {\n" +
            "  public static final String BANG = '!'\n" +
            "}\n" +
            "trait B implements A {\n" +
            "  static staticMethodWithDefaultArgument(String string = 'x') {\n" +
            "    string + BANG\n" + // MissingPropertyException
            "  }\n" +
            "}\n" +
            "class C implements B {\n" +
            "  static one() {\n" +
            "    staticMethodWithDefaultArgument()\n" +
            "  }\n" +
            "  def two() {\n" +
            "    staticMethodWithDefaultArgument()\n" +
            "  }\n" +
            "}\n" +
            "print(C.one())\n" +
            "print(new C().two())\n",
        };
        //@formatter:on

        runConformTest(sources, "x!x!");
    }

    @Test
    public void testTraits10521() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "abstract class A {\n" +
            "}\n" +
            "class C extends A implements T {\n" +
            "  void test() {\n" +
            "    p(90,'x')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "T.groovy",
            "trait T {\n" +
            "  void p(one, Object... zeroOrMore) {\n" +
            "    print one; print zeroOrMore\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "90[x]");
    }

    @Test
    public void testTraits10894() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "new C()\n",

            "C.groovy",
            "class C implements T {\n" +
            "}\n",

            "T.groovy",
            "trait T {\n" +
            "  @Validate String foo\n" +
            "}\n",

            "Validate.java",
            "import java.lang.annotation.*;\n" +
            "@Target(ElementType.TYPE_USE) \n" +
            "@Retention(RetentionPolicy.RUNTIME)\n" +
            "public @interface Validate { /**/ }\n" ,
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources);

            ClassNode helper = getCUDeclFor("T.groovy").getCompilationUnit().getClassNode("T$Trait$FieldHelper");
            for (FieldNode field : helper.getFields()) {
                if (field.getName().endsWith("__foo")) {
                    java.util.List<AnnotationNode> typeTags =
                        field.getOriginType().getTypeAnnotations();
                    assertEquals(1, typeTags.size()); // no duplication
                }
            }
        } else {
            runNegativeTest(sources,
                "----------\n" +
                "1. ERROR in C.groovy (at line 1)\n" +
                "\tclass C implements T {\n" +
                "\t^^^^^^^^^^^^^^^^^^^^^\n" +
                "Groovy:Annotation @Validate is not allowed on element FIELD\n" +
                "----------\n" +
                "----------\n" +
                "1. ERROR in T.groovy (at line 2)\n" +
                "\t@Validate String foo\n" +
                "\t^^^^^^^^^\n" +
                "Groovy:Annotation @Validate is not allowed on element FIELD\n" +
                "----------\n");
        }
    }

    @Test
    public void testTraits11012() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait Bar<T> {\n" +
            "  T get(x,y) {\n" +
            "  }\n" +
            "}\n" +
            "class Foo<V> implements Bar<V> {\n" +
            "}\n" +
            "@groovy.transform.TypeChecked test(Foo<Number> foo) {\n" +
            "  Number x = foo.get(null, null)\n" + // Cannot assign value of type Object to variable of type Number
            "}\n" +
            "test(new Foo<Number>())\n",
        };
        //@formatter:on

        runConformTest(sources);
    }

    @Test
    public void testTraits11142() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  def m() { print proxyTarget }\n" + // what about "this"
            "}\n" +
            "class C {\n" +
            "  String toString() { 'C' }\n" +
            "}\n" +
            "(new C() as T).m()\n",
        };
        //@formatter:on

        runConformTest(sources, "C");
    }

    @Test
    public void testTraits11267() {
        //@formatter:off
        String[] sources = {
            "Script.groovy",
            "trait T {\n" +
            "  static one() {\n" +
            "    def me = this\n" +
            "    two('good') + ' ' +\n" +
            "    'bad '.with { me.two(it) } +\n" +
            "    'ugly'.with {    two(it) }  \n" +
            "  }\n" +
            "  static two(String s) {\n" +
            "    three(s)\n" +
            "  }\n" +
            "  static three(String s) {\n" +
            "    s\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "print(C.one())\n",
        };
        //@formatter:on

        if (isAtLeastGroovy(40)) {
            runConformTest(sources, "good bad ugly");
        } else {
            runConformTest(sources, "", "groovy.lang.MissingMethodException: " +
                "No signature of method: static T.three() is applicable for argument types: (String) values: [ugly]");
        }
    }
}
