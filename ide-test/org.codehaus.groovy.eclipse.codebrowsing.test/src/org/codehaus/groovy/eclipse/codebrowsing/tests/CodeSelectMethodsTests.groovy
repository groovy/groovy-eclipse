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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.core.IJavaElement.TYPE

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.SourceRange
import org.junit.Test

final class CodeSelectMethodsTests extends BrowsingTestSuite {

    @Test
    void testCodeSelectDefaultParams1() {
        String one = 'class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}'
        String two = 'class Java { { new Structure().meth(0, 0, 0); } }'
        assertCodeSelect([one, two, 'new Structure().meth(0)'], 'meth')
    }

    @Test
    void testCodeSelectDefaultParams2() {
        String one = 'class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}'
        String two = 'class Java { { new Structure().meth(0); } }'
        assertCodeSelect([one, two, 'new Structure().meth(0)'], 'meth')
    }

    @Test
    void testCodeSelectDefaultParams3() {
        String one = 'class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}'
        String two = 'class Java { { new Structure().meth(0, 0); } }'
        assertCodeSelect([one, two, 'new Structure().meth(0, 0)'], 'meth')
    }

    @Test
    void testCodeSelectDefaultParams4() {
        String one = 'class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}'
        String two = 'class Java { { new Structure().meth(0, 0, 0); } }'
        assertCodeSelect([one, two, 'new Structure().meth(0, 0, 0)'], 'meth')
    }

    @Test
    void testCodeSelectClosure() {
        String contents = 'def x = { t -> print t }\nx("hello")'
        IJavaElement elem = assertCodeSelect([contents], 'x')
        assert elem.typeSignature =~ 'groovy.lang.Closure'
    }

    @Test
    void testCodeSelectInt() {
        String contents = 'def x = 9\nx++\n'
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectReAssign() {
        String contents = 'def x = {\nt -> print t\n}\nx(\"hello\")\nx = 9\n'
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectMethodInClass() {
        String contents = '''\
            |class PlantController {
            |  def redirect(Map args) {
            |  }
            |  def checkUser() {
            |    redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'redirect')
    }

    @Test
    void testCodeSelectMethodInOtherClass() {
        String contents = '''\
            |class PlantController {
            |  def redirect(Map args) {
            |  }
            |  def checkUser() {
            |    redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |class Other {
            |  def whatever() {
            |    new PlantController().redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()

        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test
    void testCodeSelectMethodInSuperClass() {
        String contents1 = '''\
            |class PlantController {
            |  def redirect(Map args) {
            |  }
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |class Other extends PlantController {
            |  def checkUser() {
            |    redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()

        IJavaElement elem = assertCodeSelect([contents1, contents2], 'redirect')
        assert elem.declaringType.fullyQualifiedName == 'PlantController'
    }

    @Test // GRECLIPSE-1755
    void testCodeSelectMethodInSuperInterface() {
        String contents1 = '''\
            |interface SuperInterface {
            |  def foo(String string);
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |interface SubInterface extends SuperInterface {
            |  def foo(String string, int integer);
            |}
            |'''.stripMargin()
        String contents3 = '''\
            |class Foo implements SubInterface {
            |  def foo(String string) {}
            |  def foo(String string, int integer) {}
            |}
            |'''.stripMargin()
        String contents4 = '''\
            |class Bar {
            |  def main() {
            |    def bar = new Foo();
            |    ((SubInterface) bar).foo("string");
            |  }
            |}
            |'''.stripMargin()

        IJavaElement elem = assertCodeSelect([contents1, contents2, contents3, contents4], 'foo')
        assert elem.declaringType.fullyQualifiedName == 'SuperInterface'
    }

    @Test
    void testCodeSelectScriptMethod() {
        // ensure private method is not hidden by script's run() method
        String contents = '''\
            |private String method() {
            |  def local = null
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'method')
    }

    @Test
    void testCodeSelectMethodInScriptFromScript() {
        String contents = '''\
            |def x() {}
            |x()
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectMethodInClassFromScript() {
        String contents = '''\
            |class Inner { def x() {} }
            |new Inner().x()
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodInClassFromScript() {
        String contents = '''\
            |class Inner { static def x() {} }
            |Inner.x()
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodInClassFromScriptMethod() {
        String contents = '''\
            |class Inner {
            |  static def x() {}
            |}
            |def y() {
            |  Inner.x()
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodFromClass1() {
        String contents = '''\
            |class Inner {
            |  static def x() {}
            |  def y() {
            |    Inner.x()
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodFromClass2() {
        String contents = '''\
            |class Inner {
            |  static def x() {}
            |  def y() {
            |    x()
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1152
    void testCodeSelectStaticMethodFromTrait1() {
        String contents = '''\
            |trait Trait {
            |  static def x() {}
            |  def y() {
            |    Trait.x()
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'x', null)
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/755
    void testCodeSelectStaticMethodFromTrait2() {
        String contents = '''\
            |trait Trait {
            |  static def x() {}
            |  def y() {
            |    x()
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'x')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/756
    void testCodeSelectStaticMethodFromTrait3() {
        String contents = '''\
            |trait T {
            |  static def x() {}
            |}
            |class C implements T {
            |  def y() {
            |    x()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'x')
        assert elem.declaringType.fullyQualifiedName == 'T'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/960
    void testCodeSelectStaticMethodFromTrait4() {
        String contents = '''\
            |trait T {
            |  Number number
            |}
            |class C implements T {
            |  def m() {
            |    getNumber()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'getNumber')
        assert elem.declaringType.fullyQualifiedName == 'T'
        assert elem.elementInfo.nameSourceStart == contents.indexOf('number')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/960
    void testCodeSelectStaticMethodFromTrait5() {
        String contents = '''\
            |trait T {
            |  Number number
            |}
            |class C implements T {
            |  def m() {
            |    setNumber(42)
            |  }
            |}
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'setNumber')
        assert elem.declaringType.fullyQualifiedName == 'T'
        assert elem.elementInfo.nameSourceStart == contents.indexOf('number')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/960
    void testCodeSelectStaticMethodFromTrait6() {
        String contents = '''\
            |trait T {
            |  boolean condition
            |}
            |class C implements T {
            |  def m() {
            |    isCondition()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'isCondition')
        assert elem.declaringType.fullyQualifiedName == 'T'
        assert elem.elementInfo.nameSourceStart == contents.indexOf('condition')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1113
    void testCodeSelectStaticMethodFromTrait7() {
        addGroovySource'''\
            |trait T {
            |  String foo
            |}
            |'''.stripMargin()
        String contents = '''\
            |class C implements T {
            |  def m() {
            |    getFoo()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'getFoo')
        assert elem.declaringType.fullyQualifiedName == 'T'
        assert elem.elementInfo.nameSourceStart == 19
    }

    @Test
    void testCodeSelectStaticMethodInOtherClass() {
        String contents = '''\
            |class PlantController {
            |  static def redirect(Map args) {
            |  }
            |  def checkUser() {
            |    redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |class Other {
            |  def whatever() {
            |    PlantController.redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test
    void testCodeSelectStaticMethodInOtherClass2() {
        String contents = '''\
            |class C {
            |  static def r(controller) {
            |  }
            |  def checkUser() {
            |    r(List)
            |  }
            |}
            '''.stripMargin()
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testCodeSelectStaticMethodInSuperClass() {
        String contents = '''\
            |class PlantController {
            |  static def redirect(controller, action) {
            |  }
            |  static def checkUser() {
            |    redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |class Other extends PlantController {
            |  static def doNothing() {
            |    redirect(controller:'user', action:'login')
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test
    void testCodeSelectStaticInScript1() {
        String contents = 'static void doSomething() { }\ndoSomething()'
        assertCodeSelect([contents], 'doSomething')
    }

    @Test
    void testCodeSelectStaticInScript2() {
        String contents = 'doSomething()\nstatic void doSomething() { }'
        assertCodeSelect([contents], 'doSomething')
    }

    @Test
    void testCodeSelectStaticMethod1() {
        String contents = 'class Parent {\n  static p() {}\n}\nclass Child extends Parent {\n  def c() {\n    p()\n  }\n}'
        IJavaElement elem = assertCodeSelect([contents], 'p')
        assert elem.parent.elementName == 'Parent'
    }

    @Test
    void testCodeSelectStaticMethod2() {
        String another = 'class Parent {\n    static p() {}\n}'
        String contents = 'class Child extends Parent {\n  def c() {\n    p()\n  }\n}'
        IJavaElement elem = assertCodeSelect([another, contents], 'p')
        assert elem.parent.elementName == 'Parent'
    }

    @Test
    void testCodeSelectStaticMethod3() {
        String contents = 'class Foo { def m() { java.util.Collections.<Object>emptyList() } }'
        assertCodeSelect([contents], 'Collections')
    }

    @Test
    void testCodeSelectStaticMethod4() {
        String contents = 'List<String> empty = Collections.&emptyList'
        IJavaElement elem = assertCodeSelect([contents], 'emptyList')
        assert elem.inferredElement.returnType.toString(false) == 'java.util.List <T>' // TODO: want T to be java.lang.String
    }

    @Test
    void testCodeSelectStaticMethod5() {
        String contents = '''\
            |import static java.util.Collections.singletonList
            |@groovy.transform.TypeChecked
            |class Foo {
            |  static {
            |    singletonList("")
            |  }
            |}'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'singletonList')
        assert elem.inferredElement.returnType.toString(false) == 'java.util.List <java.lang.String>'
    }

    @Test
    void testCodeSelectStaticMethod6() {
        String contents = '''\
            |@groovy.transform.Sortable
            |class Foo {
            |  String number
            |}
            |Foo.comparatorByNumber()
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'comparatorByNumber')
        assert elem.inferredElement.returnType.toString(false) == 'java.util.Comparator'
    }

    @Test
    void testCodeSelectStaticMethod7() {
        String contents = '''\
            |@Singleton(property='foo')
            |class Foo {
            |}
            |Foo.getFoo()
            |'''.stripMargin()
        IJavaElement elem = assertCodeSelect([contents], 'getFoo')
        assert elem.inferredElement.returnType.toString(false) == 'Foo'
    }

    @Test // GRECLIPSE-831
    void testCodeSelectOverloadedMethod1() {
        String contents = '\"\".substring(0)'
        IJavaElement elem = assertCodeSelect([contents], 'substring')
        assert elem.parameterTypes.length == 1 : 'Wrong number of parameters to method'
    }

    @Test // GRECLIPSE-831
    void testCodeSelectOverloadedMethod2() {
        String contents = '"".substring(0,1)'
        IJavaElement elem = assertCodeSelect([contents], 'substring')
        assert elem.parameterTypes.length == 2 : 'Wrong number of parameters to method'
    }

    @Test
    void testCodeSelectEnumConstant() {
        String contents = '''\
            |enum Color {
            |  RED, BLACK
            |}
            |enum Suit {
            |  CLUBS(Color.BLACK),
            |  DIAMONDS(Color.RED),
            |  HEARTS(Color.RED),
            |  SPADES(Color.BLACK),
            |
            |  final Color color
            |  Suit(Color color) {
            |    this.color = color
            |  }
            |}
            |'''.stripMargin()
        // enum Suit was visited for each constant, which covered the initializer params
        assertCodeSelect(contents, new SourceRange(contents.indexOf('Color.'), 5), 'Color')
    }

    @Test
    void testCodeSelectEnumAbstractMethod() {
        String contents = '''\
            |enum E {
            |  X() {
            |    def verb(noun) { }
            |  }
            |  abstract def verb(noun);
            |}
            |'''.stripMargin()
        // Is cancel member handled properly?
        assertCodeSelect([contents], 'verb')
        assertCodeSelect([contents], 'noun')

        contents = '''\
            |class C {
            |  enum E {
            |    X {
            |      def m(p) {
            |        support()
            |      }
            |    }
            |    abstract def m(p)
            |    static def support() {}
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'support')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/420
    void testCodeSelectAnonVariadicMethod() {
        String contents = '''\
            |interface LibarayFunction {
            |  String compute(String... args)
            |}
            |
            |def stringFunction = new LibarayFunction() {
            |  @Override
            |  String compute(String... args) {
            |  }
            |}
            |'''.stripMargin()

        IJavaElement elem = assertCodeSelect([contents], 'compute')
        assert elem.sourceRange.offset > 0
    }

    @Test
    void testCodeSelectHotkeyHandling() {
        String contents = 'def list = Arrays.asList("1", "2", "3")'
        // F3 (Open Declaration) key binding selects at offset of cursor with length 0
        assertCodeSelect(contents, new SourceRange(contents.indexOf('sList'), 0), 'asList')
    }

    @Test
    void testCodeSelectNamedArguments1() {
        String contents = '''\
            void meth(Map agrs) {}
            meth(one: null, two: Date)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectNamedArguments2() {
        String contents = '''\
            void meth(Map agrs, int three) {}
            meth(one: null, two: Date, 3)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectNamedArguments2a() {
        String contents = '''\
            void meth(Map agrs, int three) {}
            meth(one: null, 3, two: Date)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectNamedArguments2b() {
        String contents = '''\
            void meth(Map agrs, int three) {}
            meth(3, two: Date, one: null)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/827
    void testCodeSelectNamedArguments3() {
        String contents = '''\
            void meth(Map agrs, Class type) {}
            meth(one: null, two: null, Date)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/827
    void testCodeSelectNamedArguments3a() {
        String contents = '''\
            void meth(Map agrs, Class type) {}
            meth(one: null, Date, two: null)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/827
    void testCodeSelectNamedArguments3b() {
        String contents = '''\
            void meth(Map agrs, Class type) {}
            meth(Date, one: null, two: null)
            '''.stripIndent()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectConstructor() {
        String contents = 'def x = new java.util.Date()'
        IJavaElement elem = assertCodeSelect([contents], 'Date')
        assert elem instanceof IMethod : 'Expected method not type'
        assert elem.isConstructor() : 'Expected ctor not method'

        // check the preceding elements for selection bleedthrough
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'new', '')
        assertCodeSelect([contents], 'x', 'x')
    }

    @Test
    void testCodeSelectNewifyConstructor1() {
        String contents = 'class Foo { @Newify(Date) def x = Date(123L) }'
        IJavaElement elem = assertCodeSelect([contents], 'Date')
        assert elem instanceof IMethod : 'Expected method not type'
        assert elem.isConstructor() : 'Expected ctor not method'
    }

    @Test
    void testCodeSelectNewifyConstructor2() {
        String contents = 'class Foo { @Newify(Date) def x = Date.new(123L) }'
        IJavaElement elem = assertCodeSelect([contents], 'new', 'Date')
        assert elem instanceof IMethod : 'Expected method not type'
        assert elem.isConstructor() : 'Expected ctor not method'
    }

    @Test
    void testCodeSelectNewifyConstructor2a() {
        String contents = 'class Foo { @Newify(Date) def x = Date.new(123L) }'
        IJavaElement elem = assertCodeSelect([contents], 'Date')
        assert elem instanceof IType : 'Expected type not ctor'
    }

    @Test
    void testCodeSelectConstuctorSimple() {
        assertConstructor('class Foo { Foo() { } }\nnew Foo()', 'Foo')
    }

    @Test
    void testCodeSelectConstuctorQualName() {
        assertConstructor('class Foo { Foo() { } }\nnew p.Foo()', 'p.Foo')
    }

    @Test
    void testCodeSelectConstuctorOtherFile() {
        addGroovySource('class Foo { Foo() { } }', nextUnitName(), 'p')
        assertConstructor('new Foo()', 'Foo')
    }

    @Test
    void testCodeSelectConstuctorOtherFile2() {
        addGroovySource('class Foo {\nFoo(a) { } }', nextUnitName(), 'p')
        assertConstructor('new Foo()', 'Foo')
    }

    @Test
    void testCodeSelectConstuctorOtherFile3() {
        addGroovySource('class Foo { Foo() { }\nFoo(a) { } }', nextUnitName(), 'p')
        assertConstructor('new Foo()', 'Foo')
    }

    @Test
    void testCodeSelectConstuctorJavaFile() {
        addJavaSource('class Foo { Foo() { } }', nextUnitName(), 'p')
        assertConstructor('new Foo()', 'Foo')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1103
    void testCodeSelectConstuctorJavaFile2() {
        addJavaSource('''\
            |class Foo {
            |  Foo(String s, Map<String, ? extends Iterable<String>> m) {}
            |  Foo(String s, String... strings) {}
            |}
            |'''.stripMargin(), nextUnitName(), 'p')

        IMethod ctor = assertConstructor('new Foo("", [:])', 'Foo')
        assert ctor.parameterNames[1] == 'm'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/452
    void testCodeSelectConstuctorMapStyle() {
        addGroovySource('class Bean { Number number; String string }', 'Bean', 'p')

        ICompilationUnit unit = addGroovySource('def bean = new Bean(number: 0, string: "")', nextUnitName(), 'p')
        prepareForCodeSelect(unit)

        IJavaElement[] elems = unit.codeSelect(unit.source.lastIndexOf('Bean'), 4)
        assert elems[0].elementName == 'Bean'
        assert elems[0].elementType == TYPE
    }

    @Test
    void testCodeSelectConstuctorMapStyle2() {
        addGroovySource('class Bean2 {\n Bean2() {}\n Number number; String string\n }', 'Bean2', 'p')
        assertConstructor('def bean = new Bean2(number: 0, string: "")', 'Bean2')
    }

    @Test
    void testCodeSelectConstuctorNamedArgs() {
        addGroovySource '''\
            |class Classy {
            |  Classy(Number n, String s) {
            |    ;
            |  }
            |}
            |'''.stripMargin(), 'Classy', 'p'

        // NOTE: I don't think this is correct syntax for calling the 2-arg constructor
        IMethod method = assertConstructor('def c = new Classy(n: 0, s: "")', 'Classy')
        assert method.parameters.length == 2
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors() {
        addGroovySource('class Foo { Foo() { }\nFoo(a) { } }', nextUnitName(), 'p')
        IMethod method = assertConstructor('new Foo()', 'Foo')
        assert method.parameters.length == 0 : 'Should have found constructor with no args'
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors2() {
        addGroovySource('class Foo { Foo() { } \n Foo(a) { } }', nextUnitName(), 'p')
        IMethod method = assertConstructor('new Foo(0)', 'Foo')
        assert method.parameters.length == 1 : 'Should have found constructor with 1 arg'
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors3() {
        IMethod method = assertConstructor('new Date(0)', 'Date')
        assert method.parameters.length == 1: 'Should have found constructor with 1 arg'
        assert method.parameterTypes[0] == 'J' : 'Should have found constructor Date(long)'
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors4() {
        // single-arg constructor is defined last and use of constant reference in ctor call means arg types not resolved at time of ctor selection
        addGroovySource('class Foo { Foo(String s1, String s2) { } \n Foo(String s1) { } }', nextUnitName(), 'p')
        addGroovySource('interface Bar { String CONST = "whatever" }', nextUnitName(), 'p')

        IMethod method = assertConstructor('new Foo(Bar.CONST)', 'Foo')
        assert method.parameters.length == 1 : 'Should have found constructor with 1 arg'
    }

    private IMethod assertConstructor(String contents, String toSearch) {
        ICompilationUnit unit = addGroovySource(contents, nextUnitName(), 'p')
        prepareForCodeSelect(unit)

        IJavaElement[] elems = unit.codeSelect(unit.source.lastIndexOf(toSearch), toSearch.length())
        assert elems.length == 1 : 'Should have found a selection'
        String elementName = toSearch.substring(toSearch.lastIndexOf('.') + 1)
        assert elems[0].elementName == elementName : "Should have found constructor '$elementName'"
        assert elems[0].isConstructor() : 'Should be a constructor'
        return elems[0]
    }
}
