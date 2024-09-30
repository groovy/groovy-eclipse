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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.eclipse.jdt.core.IJavaElement.TYPE

import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.SourceRange
import org.junit.Test

final class CodeSelectMethodsTests extends BrowsingTestSuite {

    private IMethod assertConstructor(final CharSequence contents, final String toSearch) {
        def unit = addGroovySource(contents, nextUnitName(), 'p')
        prepareForCodeSelect(unit)

        IJavaElement[] elements = unit.codeSelect(unit.source.lastIndexOf(toSearch), toSearch.length())
        assert elements.length == 1 : 'Should have found a selection'

        String elementName = toSearch.substring(toSearch.lastIndexOf((int) '.') + 1)
        assert elements[0].elementName == elementName : "Should have found constructor '$elementName'"
        assert elements[0].isConstructor() : 'Should be a constructor'
        return elements[0]
    }

    @Test
    void testCodeSelectDefaultParams1() {
        String one = 'class One {\n  def m(int a, int b = 1, int c = 2) {}\n}'
        String two = 'class Two {\n  {\n\tnew One().m(0, 0, 0)\n  }\n}'
        assertCodeSelect([one, two], 'm')
    }

    @Test
    void testCodeSelectDefaultParams2() {
        String one = 'class One {\n  def m(int a, int b = 1, int c = 2) {}\n}'
        String two = 'class Two {\n  {\n\tnew One().m(0, 0)\n  }\n}'
        assertCodeSelect([one, two], 'm')
    }

    @Test
    void testCodeSelectDefaultParams3() {
        String one = 'class One {\n  def m(int a, int b = 1, int c = 2) {}\n}'
        String two = 'class Two {\n  {\n\tnew One().m(0)\n  }\n}'
        assertCodeSelect([one, two], 'm')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/794
    void testCodeSelectDefaultParams4() {
        def unit = addGroovySource('class C {\n  def m(a, b = 1, c = 2) {}\n}', nextUnitName(), 'p')
        prepareForCodeSelect(unit)

        IJavaElement[] elements = unit.codeSelect(unit.source.lastIndexOf((int) 'm'), 1)
        assert elements.length == 3 : 'Should have found three choices'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/794
    void testCodeSelectDefaultParams5() {
        def unit = addGroovySource('class C {\n  C(a, b = 1, c = 2) {}\n}', nextUnitName(), 'p')
        prepareForCodeSelect(unit)

        IJavaElement[] elements = unit.codeSelect(unit.source.lastIndexOf((int) 'C'), 1)
        assert elements.length == 3 : 'Should have found three choices'
    }

    @Test
    void testCodeSelectClosure() {
        String contents = 'def x = { t -> print t }\nx("hello")'
        IJavaElement element = assertCodeSelect([contents], 'x')
        assert element.typeSignature =~ 'groovy.lang.Closure'
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

        IJavaElement element = assertCodeSelect([contents1, contents2], 'redirect')
        assert element.declaringType.fullyQualifiedName == 'PlantController'
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

        IJavaElement element = assertCodeSelect([contents1, contents2, contents3, contents4], 'foo')
        assert element.declaringType.fullyQualifiedName == 'SuperInterface'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1457
    void testCodeSelectMethodInParameterizedClass() {
        String contents = '''\
            |class Foo<Bar> {
            |  Set<Bar> meth(Set<Bar> bars) {
            |  }
            |  void test() {
            |    def returnValue = meth(null)
            |  }
            |}
            |'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents], 'meth')
        assert element.key == 'LFoo;.meth(Ljava/util/Set<TBar;>;)Ljava/util/Set<TBar;>;'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1588
    void testCodeSelectMethodOfParameterizedField() {
        String contents = '''\
            |class Foo<Bar> {
            |  Set<Bar> bars
            |  void test() {
            |    bars.add(null)
            |    bars.findAll()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents], 'add')
        assert element.key == 'Ljava/util/Set;.add(TBar;)Z'

        element = assertCodeSelect([contents], 'findAll')
        assert element.key == 'Lorg/codehaus/groovy/runtime/DefaultGroovyMethods;.findAll(Ljava/util/Set<TBar;>;)Ljava/util/Set<TBar;>;'
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
        IJavaElement element = assertCodeSelect([contents], 'x')
        assert element.declaringType.fullyQualifiedName == 'T'
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
        IJavaElement element = assertCodeSelect([contents], 'getNumber')
        assert element.declaringType.fullyQualifiedName == 'T'
        assert element.elementInfo.nameSourceStart == contents.indexOf('number')
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
        IJavaElement element = assertCodeSelect([contents], 'setNumber')
        assert element.declaringType.fullyQualifiedName == 'T'
        assert element.elementInfo.nameSourceStart == contents.indexOf('number')
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
        IJavaElement element = assertCodeSelect([contents], 'isCondition')
        assert element.declaringType.fullyQualifiedName == 'T'
        assert element.elementInfo.nameSourceStart == contents.indexOf('condition')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1113
    void testCodeSelectStaticMethodFromTrait7() {
        String contents1 = '''\
            |trait T {
            |  def p
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |class C implements T {
            |  def m() {
            |    getP()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents1, contents2], 'getP')
        assert element.declaringType.fullyQualifiedName == 'T'
        assert element.elementInfo.nameSourceStart == 16
    }

    @Test
    void testCodeSelectStaticMethodFromTrait8() {
        String contents1 = '''\
            |trait T {
            |  int p
            |}
            |'''.stripMargin()
        String contents2 = '''\
            |class C implements T {
            |  def m() {
            |    getP()
            |  }
            |}
            |'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents1, contents2], 'getP')
        assert element.declaringType.fullyQualifiedName == 'T'
        assert element.elementInfo.nameSourceStart == 16
    }

    @Test
    void testCodeSelectStaticMethodInOtherClass() {
        String contents1 = '''\
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
        assertCodeSelect([contents1, contents2], 'redirect')
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
        String contents1 = '''\
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
        assertCodeSelect([contents1, contents2], 'redirect')
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
        IJavaElement element = assertCodeSelect([contents], 'p')
        assert element.parent.elementName == 'Parent'
    }

    @Test
    void testCodeSelectStaticMethod2() {
        String another = 'class Parent {\n    static p() {}\n}'
        String contents = 'class Child extends Parent {\n  def c() {\n    p()\n  }\n}'
        IJavaElement element = assertCodeSelect([another, contents], 'p')
        assert element.parent.elementName == 'Parent'
    }

    @Test
    void testCodeSelectStaticMethod3() {
        String contents = 'def list = java.util.Collections.<Object>emptyList()'
        assertCodeSelect([contents], 'Collections')
        IJavaElement element = assertCodeSelect([contents], 'emptyList')
        assert element.inferredElement.returnType.toString(false) == 'java.util.List<java.lang.Object>'
        assert element.key == 'Ljava/util/Collections;.emptyList()Ljava/util/List<Ljava/lang/Object;>;'
    }

    @Test
    void testCodeSelectStaticMethod4() {
        String contents = 'java.util.function.Supplier<String> getter = Collections.&emptyList'
        IJavaElement element = assertCodeSelect([contents], 'emptyList')
        assert element.inferredElement.returnType.toString(false) == 'java.util.List<java.lang.String>'
        assert element.key == 'Ljava/util/Collections;.emptyList()Ljava/util/List<Ljava/lang/String;>;'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1595
    void testCodeSelectStaticMethod5() {
        String contents = '''\
            |@groovy.transform.TypeChecked test() {
            |  def list = Collections.emptyList()
            |}'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents], 'emptyList')
        assert element.inferredElement.returnType.toString(false) == 'java.util.List<java.lang.Object>'
        assert element.key == 'Ljava/util/Collections;.emptyList()Ljava/util/List<Ljava/lang/Object;>;'
    }

    @Test
    void testCodeSelectStaticMethod6() {
        String contents = '''\
            |import static java.util.Collections.singletonList
            |@groovy.transform.TypeChecked
            |class Foo {
            |  static {
            |    singletonList("")
            |  }
            |}'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents], 'singletonList')
        assert element.inferredElement.returnType.toString(false) == 'java.util.List<java.lang.String>'
        assert element.key == 'Ljava/util/Collections;.singletonList(Ljava/lang/String;)Ljava/util/List<Ljava/lang/String;>;'
    }

    @Test
    void testCodeSelectStaticMethod7() {
        String contents = '''\
            |@groovy.transform.Sortable
            |class Foo {
            |  String number
            |}
            |Foo.comparatorByNumber()
            |'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents], 'comparatorByNumber')
        assert element.inferredElement.returnType.toString(false) == 'java.util.Comparator'
    }

    @Test
    void testCodeSelectStaticMethod8() {
        String contents = '''\
            |@Singleton(property='foo')
            |class Foo {
            |}
            |Foo.getFoo()
            |'''.stripMargin()
        IJavaElement element = assertCodeSelect([contents], 'getFoo')
        assert element.inferredElement.returnType.toString(false) == 'Foo'
    }

    @Test // GRECLIPSE-831
    void testCodeSelectOverloadedMethod1() {
        String contents = '\"\".substring(0)'
        IJavaElement element = assertCodeSelect([contents], 'substring')
        assert element.parameterTypes.length == 1 : 'Wrong number of parameters to method'
    }

    @Test // GRECLIPSE-831
    void testCodeSelectOverloadedMethod2() {
        String contents = '"".substring(0,1)'
        IJavaElement element = assertCodeSelect([contents], 'substring')
        assert element.parameterTypes.length == 2 : 'Wrong number of parameters to method'
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
        IJavaElement element = assertCodeSelect([contents], 'compute')
        assert element.sourceRange.offset > 0
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
            |void meth(Map agrs) {}
            |meth(one: null, two: Date)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectNamedArguments2() {
        String contents = '''\
            |void meth(Map agrs, int three) {}
            |meth(one: null, two: Date, 3)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectNamedArguments2a() {
        String contents = '''\
            |void meth(Map agrs, int three) {}
            |meth(one: null, 3, two: Date)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectNamedArguments2b() {
        String contents = '''\
            |void meth(Map agrs, int three) {}
            |meth(3, two: Date, one: null)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/827
    void testCodeSelectNamedArguments3() {
        String contents = '''\
            |void meth(Map agrs, Class type) {}
            |meth(one: null, two: null, Date)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/827
    void testCodeSelectNamedArguments3a() {
        String contents = '''\
            |void meth(Map agrs, Class type) {}
            |meth(one: null, Date, two: null)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/827
    void testCodeSelectNamedArguments3b() {
        String contents = '''\
            |void meth(Map agrs, Class type) {}
            |meth(Date, one: null, two: null)
            |'''.stripMargin()
        assertCodeSelect([contents], 'Date')
    }

    @Test
    void testCodeSelectConstructor() {
        String contents = 'def x = new java.util.Date()'
        IJavaElement element = assertCodeSelect([contents], 'Date')
        assert element instanceof IMethod : 'Expected method not type'
        assert element.isConstructor() : 'Expected ctor not method'

        // check the preceding elements for selection bleedthrough
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'new', '')
        assertCodeSelect([contents], 'x', 'x')
    }

    @Test
    void testCodeSelectNewifyConstructor1() {
        String contents = 'class Foo { @Newify(Date) def x = Date(123L) }'
        IJavaElement element = assertCodeSelect([contents], 'Date')
        assert element instanceof IMethod : 'Expected method not type'
        assert element.isConstructor() : 'Expected ctor not method'
    }

    @Test
    void testCodeSelectNewifyConstructor2() {
        String contents = 'class Foo { @Newify(Date) def x = Date.new(123L) }'
        IJavaElement element = assertCodeSelect([contents], 'new', 'Date')
        assert element instanceof IMethod : 'Expected method not type'
        assert element.isConstructor() : 'Expected ctor not method'
    }

    @Test
    void testCodeSelectNewifyConstructor2a() {
        String contents = 'class Foo { @Newify(Date) def x = Date.new(123L) }'
        IJavaElement element = assertCodeSelect([contents], 'Date')
        assert element instanceof IType : 'Expected type not ctor'
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

        IJavaElement[] elements = unit.codeSelect(unit.source.lastIndexOf('Bean'), 4)
        assert elements[0].elementName == 'Bean'
        assert elements[0].elementType == TYPE
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
}
