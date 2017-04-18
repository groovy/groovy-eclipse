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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import static org.junit.Assert.*

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryMethod
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceMethod
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.ILocalVariable
import org.eclipse.jdt.core.IMethod
import org.eclipse.jdt.core.tests.util.GroovyUtils
import org.junit.Test

final class CodeSelectMethodsTests extends BrowsingTestCase {

    @Test
    void testCodeSelectDefaultParams1() {
        String one = 'class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}'
        String two = 'class Java { { new Structure().meth(0, 0, 0); } }'
        assertCodeSelect([one, two, 'new Structure().meth'], 'meth')
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
    void testCodeSelectGenericMethod1() {
        String contents = '[a: Number].keySet()'
        IJavaElement elem = assertCodeSelect([contents], 'keySet')
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement())
        assertEquals('java.util.Set <java.lang.String>', method.getReturnType().toString(false))
    }

    @Test
    void testCodeSelectGenericMethod2() {
        String contents = '[a: Number].values()'
        IJavaElement elem = assertCodeSelect([contents], 'values')
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement())
        assertEquals('java.util.Collection <java.lang.Class>', method.getReturnType().toString(false))
    }

    @Test
    void testCodeSelectGenericMethod3() {
        String contents = '[a: Number].entrySet()'
        IJavaElement elem = assertCodeSelect([contents], 'entrySet')
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement())
        assertEquals('java.util.Set <java.util.Map$Entry>', method.getReturnType().toString(false))
    }

    @Test
    void testCodeSelectGenericCategoryMethod3() {
        String contents = '[a: Number].getAt("a")'
        IJavaElement elem = assertCodeSelect([contents], 'getAt')
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement())
        assertEquals('java.lang.Class <java.lang.Number>', method.getReturnType().toString(false))
    }

    @Test
    void testCodeSelectClosure() {
        String contents = 'def x = { t -> print t }\nx("hello")'
        IJavaElement elem = assertCodeSelect([contents], 'x')
        assertEquals('QClosure;', ((ILocalVariable) elem).getTypeSignature())
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
        String contents = 'class PlantController {\ndef redirect(controller, action) { }\n' +
            'def checkUser() {\nredirect(controller:"user",action:"login")\n}}\n'
        assertCodeSelect([contents], 'redirect')
    }

    @Test
    void testCodeSelectMethodInOtherClass() {
        String contents = 'class PlantController {\ndef redirect(controller, action) { }\n' +
            'def checkUser() {\nredirect(controller:\"user\",action:\"login\")\n}}\n'
        String contents2 =
            'class Other {\ndef doNothing() {\nnew PlantController().redirect(controller:\"user\",action:\"login\")\n}}'

        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test
    void testCodeSelectMethodInSuperClass() {
        String contents = 'class PlantController {\ndef redirect(controller, action) { }\n' +
            'def checkUser() {\nredirect(controller:\"user\",action:\"login\")\n}}\n'
        String contents2 =
            'class Other extends PlantController {\ndef doNothing() {\nredirect(controller:\"user\",action:\"login\")\n}}'

        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test // GRECLIPSE-1755
    void testCodeSelectMethodInSuperInterface() {
        String contents = 'interface SuperInterface {\ndef foo(String string);\n}\n'
        String contents2 = 'interface SubInterface extends SuperInterface {\n' +
            'def foo(String string, int integer);\n}'
        String contents3 = 'class Foo implements SubInterface{\ndef foo(String string) {}\n' +
            'def foo(String string, int integer) {}\n}'
        String contents4 = 'class Bar {\ndef main() {\ndef bar = new Foo();\n' +
            '((SubInterface) bar).foo(\"string\");\n}}'

        IJavaElement elem = assertCodeSelect([contents, contents2, contents3, contents4], 'foo')
        assertEquals('Declaring type is expected to be SuperInterface', 'SuperInterface',
            ((MethodNode) ((GroovyResolvedSourceMethod) elem).getInferredElement()).getDeclaringClass().getNameWithoutPackage())
    }

    @Test
    void testCodeSelectMethodInScriptFromScript() {
        String contents = 'def x() { }\nx()\n'
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectMethodInClassFromScript() {
        String contents = 'class Inner { def x() { } }\nnew Inner().x()\n'
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodFromClass() {
        String contents = 'class Inner { static def x() { }\ndef y() {\n Inner.x()\n } }'
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodFromScript() {
        String contents = 'class Inner { static def x() { } }\ndef y() {\n Inner.x()\n }'
        assertCodeSelect([contents], 'x')
    }

    @Test
    void testCodeSelectStaticMethodInOtherClass() {
        String contents = 'class PlantController {\nstatic def redirect(controller, action)  { }\n' +
            'def checkUser() {\nredirect(controller:\"user\",action:\"login\")\n}}\n'
        String contents2 =
            'class Other {\ndef doNothing() {\nPlantController.redirect(controller:\"user\",action:\"login\")\n}}'

        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test
    void testCodeSelectStaticMethodInOtherClass2() {
        String contents = 'class C {\nstatic def r(controller)  { }\ndef checkUser() {\nr(List)\n}' + '}\n'
        assertCodeSelect([contents], 'List')
    }

    @Test
    void testCodeSelectStaticMethodInSuperClass() {
        String contents = 'class PlantController {\ndef redirect(controller, action)  { }\n' +
            'static def checkUser() {\nredirect(controller:\"user\",action:\"login\")\n}}\n'
        String contents2 =
            'class Other extends PlantController {\nstatic def doNothing() {\nredirect(controller:\"user\",action:\"login\")\n}}'

        assertCodeSelect([contents, contents2], 'redirect')
    }

    @Test
    void testCodeSelectStaticInScript() {
        String contents = 'doSomething()\nstatic void doSomething() { }'
        assertCodeSelect([contents], 'doSomething')
    }

    @Test
    void testCodeSelectStaticMethod1() {
        String contents = 'class Parent {\n  static p() {}\n}\nclass Child extends Parent {\n  def c() {\n    p()\n  }\n}'
        IJavaElement elem = assertCodeSelect([contents], 'p')
        assertEquals('Parent', elem.getParent().getElementName())
    }

    @Test
    void testCodeSelectStaticMethod2() {
        String another = 'class Parent {\n    static p() {}\n}'
        String contents = 'class Child extends Parent {\n  def c() {\n    p()\n  }\n}'
        IJavaElement elem = assertCodeSelect([another, contents], 'p')
        assertEquals('Parent', elem.getParent().getElementName())
    }

    @Test
    void testCodeSelectStaticMethod3() {
        String contents = 'class Foo { def m() { java.util.Collections.<Object>emptyList() } }'
        assertCodeSelect([contents], 'Collections')
    }

    @Test
    void testCodeSelectStaticMethod4() {
        String contents = 'def empty = Collections.&emptyList'
        IJavaElement elem = assertCodeSelect([contents], 'emptyList')
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement())
        assertEquals('java.util.List <T>', method.getReturnType().toString(false)) // want T to be java.lang.String
    }

    @Test
    void testCodeSelectStaticMethod5() {
        if (GroovyUtils.GROOVY_LEVEL < 20) return
        String contents = 'import static java.util.Collections.singletonList\n' +
            '@groovy.transform.TypeChecked\n' +
            'class Foo { static {\n' +
            '  singletonList(\"\")\n' +
            '}}'
        IJavaElement elem = assertCodeSelect([contents], 'singletonList')
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement())
        assertEquals('java.util.List <java.lang.String>', method.getReturnType().toString(false))
    }

    @Test
    void testCodeSelectStaticProperty1() {
        String contents = 'class Super {\n    def static getSql() {   }\n}\n \n' +
            'class Sub extends Super {\n    def static foo() {\n        sql  \n     }\n} '
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty2() {
        String contents = 'class Super {\n    def getSql() {   }\n}\n \nclass Sub extends Super {\n' +
            '    def foo() {\n        sql  \n     }\n} '
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test
    void testCodeSelectStaticProperty3() {
        String contents = 'class Super {\n    def getSql() {   }\n    def foo() {\n        sql  \n' + '     }\n} '
        assertCodeSelect([contents], 'sql', 'getSql')
    }

    @Test // GRECLIPSE-831
    void testCodeSelectOverloadedMethod1() {
        String contents = '\"\".substring(0)'
        IJavaElement elem = assertCodeSelect([contents], 'substring')
        assertEquals('Wrong number of parameters to method', 1, ((IMethod) elem).getParameterTypes().length)
    }

    @Test // GRECLIPSE-831
    void testCodeSelectOverloadedMethod2() {
        String contents = '\"\".substring(0,1)'
        IJavaElement elem = assertCodeSelect([contents], 'substring')
        assertEquals('Wrong number of parameters to method', 2, ((IMethod) elem).getParameterTypes().length)
    }

    //

    @Test
    void testCodeSelectConstructor() {
        String contents = 'def x = new java.util.Date()'
        IJavaElement elem = assertCodeSelect([contents], 'Date')
        assertTrue('Expected method not type', elem instanceof IMethod)
        assertTrue('Expected ctor not method', ((IMethod) elem).isConstructor())

        // check the preceding elements for selection bleedthrough
        assertCodeSelect([contents], 'util', 'java.util')
        assertCodeSelect([contents], 'java', 'java')
        assertCodeSelect([contents], 'new', '')
        assertCodeSelect([contents], 'x', 'x')
    }

    @Test
    void testCodeSelectConstuctorSimple() {
        assertConstructor('p', 'Bar', 'Foo', 'class Foo { Foo() { } }\nnew Foo()')
    }

    @Test
    void testCodeSelectConstuctorQualName() {
        assertConstructor('p', 'Bar', 'p.Foo', 'class Foo { Foo() { } }\nnew p.Foo()')
    }

    @Test
    void testCodeSelectConstuctorOtherFile() {
        addGroovySource('class Foo { Foo() { } }', 'Foo', 'p')
        assertConstructor('p', 'Bar', 'Foo', 'new Foo()')
    }

    @Test
    void testCodeSelectConstuctorOtherFile2() {
        addGroovySource('class Foo {\nFoo(a) { } }', 'Foo', 'p')
        assertConstructor('p', 'Bar', 'Foo', 'new Foo()')
    }

    @Test
    void testCodeSelectConstuctorOtherFile3() {
        addGroovySource('class Foo { Foo() { }\nFoo(a) { } }', 'Foo', 'p')
        assertConstructor('p', 'Bar', 'Foo', 'new Foo()')
    }

    @Test
    void testCodeSelectConstuctorJavaFile() {
        addJavaSource('class Foo { Foo() { } }', 'Foo', 'p')
        assertConstructor('p', 'Bar', 'Foo', 'new Foo()')
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors() {
        addGroovySource('class Foo { Foo() { }\nFoo(a) { } }', 'Foo', 'p')
        IMethod method = assertConstructor('p', 'Bar', 'Foo', 'new Foo()')
        assertEquals('Should have found constructor with no args', 0, method.getParameters().length)
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors2() {
        addGroovySource('class Foo { Foo() { } \n Foo(a) { } }', 'Foo', 'p')
        IMethod method = assertConstructor('p', 'Bar', 'Foo', 'new Foo(0)')
        assertEquals('Should have found constructor with 1 arg', 1, method.getParameters().length)
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors3() {
        IMethod method = assertConstructor('p', 'Bar', 'Date', 'new Date(0)')
        assertEquals('Should have found constructor with 1 arg', 1, method.getParameters().length)
        assertEquals('Should have found constructor Date(long)', 'J', method.getParameterTypes()[0])
    }

    @Test
    void testCodeSelectConstuctorMultipleConstructors4() {
        // single-arg constructor is defined last and use of constant reference in ctor call means arg types not resolved at time of ctor selection
        addGroovySource('class Foo { Foo(String s1, String s2) { } \n Foo(String s1) { } }', 'Foo', 'p')
        addGroovySource('interface Bar { String CONST = \"whatever\" }', 'Bar', 'p')
        IMethod method = assertConstructor('p', 'Baz', 'Foo', 'new Foo(Bar.CONST)')
        assertEquals('Should have found constructor with 1 arg', 1, method.getParameters().length)
    }

    private IMethod assertConstructor(String packName, String className, String toSearch, String contents) {
        ICompilationUnit unit = addGroovySource(contents, className, packName)
        prepareForCodeSelect(unit)

        IJavaElement[] elt = unit.codeSelect(unit.getSource().lastIndexOf(toSearch), toSearch.length())
        assertEquals('Should have found a selection', 1, elt.length)
        String elementName = toSearch.substring(toSearch.lastIndexOf('.') + 1)
        assertEquals('Should have found constructor \"' + elementName + '\"', elementName, elt[0].getElementName())
        assertTrue('Should be a constructor', ((IMethod) elt[0]).isConstructor())
        return (IMethod) elt[0]
    }
}
