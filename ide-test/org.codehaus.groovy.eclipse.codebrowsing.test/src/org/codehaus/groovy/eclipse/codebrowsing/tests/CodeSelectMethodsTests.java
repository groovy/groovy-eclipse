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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import static java.util.Arrays.asList;

import junit.framework.Test;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedBinaryMethod;
import org.codehaus.groovy.eclipse.codebrowsing.elements.GroovyResolvedSourceMethod;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.tests.util.GroovyUtils;

public final class CodeSelectMethodsTests extends BrowsingTestCase {

    public static Test suite() {
        return newTestSuite(CodeSelectMethodsTests.class);
    }

    public void testCodeSelectDefaultParams1() {
        String one = "class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}";
        String two = "class Java { { new Structure().meth(0, 0, 0); } }";
        assertCodeSelect(asList(one, two, "new Structure().meth"), "meth");
    }

    public void testCodeSelectDefaultParams2() {
        String one = "class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}";
        String two = "class Java { { new Structure().meth(0); } }";
        assertCodeSelect(asList(one, two, "new Structure().meth(0)"), "meth");
    }

    public void testCodeSelectDefaultParams3() {
        String one = "class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}";
        String two = "class Java { { new Structure().meth(0, 0); } }";
        assertCodeSelect(asList(one, two, "new Structure().meth(0, 0)"), "meth");
    }

    public void testCodeSelectDefaultParams4() {
        String one = "class Structure {\n  def meth(int a, int b = 9, int c=8) {}\n}";
        String two = "class Java { { new Structure().meth(0, 0, 0); } }";
        assertCodeSelect(asList(one, two, "new Structure().meth(0, 0, 0)"), "meth");
    }

    public void testCodeSelectGenericMethod1() {
        String contents = "[a: Number].keySet()";
        IJavaElement elem = assertCodeSelect(asList(contents), "keySet");
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement());
        assertEquals("java.util.Set <java.lang.String>", method.getReturnType().toString(false));
    }

    public void testCodeSelectGenericMethod2() {
        String contents = "[a: Number].values()";
        IJavaElement elem = assertCodeSelect(asList(contents), "values");
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement());
        assertEquals("java.util.Collection <java.lang.Class>", method.getReturnType().toString(false));
    }

    public void testCodeSelectGenericMethod3() {
        String contents = "[a: Number].entrySet()";
        IJavaElement elem = assertCodeSelect(asList(contents), "entrySet");
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement());
        assertEquals("java.util.Set <java.util.Map$Entry>", method.getReturnType().toString(false));
    }

    public void testCodeSelectGenericCategoryMethod3() {
        String contents = "[a: Number].getAt('a')";
        IJavaElement elem = assertCodeSelect(asList(contents), "getAt");
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement());
        assertEquals("java.lang.Class <java.lang.Number>", method.getReturnType().toString(false));
    }

    public void testCodeSelectClosure() {
        String contents = "def x = { t -> print t }\nx('hello')";
        IJavaElement elem = assertCodeSelect(asList(contents), "x");
        assertEquals("QClosure;", ((ILocalVariable) elem).getTypeSignature());
    }

    public void testCodeSelectInt() {
        String contents = "def x = 9\nx++\n";
        assertCodeSelect(asList(contents), "x");
    }

    public void testCodeSelectReAssign() {
        String contents = "def x = {\nt -> print t\n}\nx(\"hello\")\nx = 9\n";
        assertCodeSelect(asList(contents), "x");
    }

    public void testCodeSelectMethodInClass() {
        String contents = "class PlantController {\ndef redirect(controller, action) { }\n" +
            "def checkUser() {\nredirect(controller:'user',action:'login')\n}}\n";
        assertCodeSelect(asList(contents), "redirect");
    }

    public void testCodeSelectMethodInOtherClass() {
        String contents = "class PlantController {\ndef redirect(controller, action) { }\n" +
            "def checkUser() {\nredirect(controller:'user',action:'login')\n}}\n";
        String contents2 =
            "class Other {\ndef doNothing() {\nnew PlantController().redirect(controller:'user',action:'login')\n}}";

        assertCodeSelect(asList(contents, contents2), "redirect");
    }

    public void testCodeSelectMethodInSuperClass() {
        String contents = "class PlantController {\ndef redirect(controller, action) { }\n" +
            "def checkUser() {\nredirect(controller:'user',action:'login')\n}}\n";
        String contents2 =
            "class Other extends PlantController {\ndef doNothing() {\nredirect(controller:'user',action:'login')\n}}";

        assertCodeSelect(asList(contents, contents2), "redirect");
    }

    // GRECLIPSE-1755
    public void testCodeSelectMethodInSuperInterface() {
        String contents = "interface SuperInterface {\ndef foo(String string);\n}\n";
        String contents2 =
            "interface SubInterface extends SuperInterface {\n" + "def foo(String string, int integer);\n}";
        String contents3 = "class Foo implements SubInterface{\ndef foo(String string) {}\n" +
            "def foo(String string, int integer) {}\n}";
        String contents4 =
            "class Bar {\ndef main() {\ndef bar = new Foo();\n" + "((SubInterface) bar).foo(\"string\");\n}}";

        IJavaElement elem = assertCodeSelect(asList(contents, contents2, contents3, contents4), "foo");
        assertEquals("Declaring type is expected to be 'SuperInterface", "SuperInterface",
            ((MethodNode) ((GroovyResolvedSourceMethod) elem).getInferredElement()).getDeclaringClass().getNameWithoutPackage());
    }

    public void testCodeSelectMethodInScriptFromScript() {
        String contents = "def x() { }\nx()\n";
        assertCodeSelect(asList(contents), "x");
    }

    public void testCodeSelectMethodInClassFromScript() {
        String contents = "class Inner { def x() { } }\nnew Inner().x()\n";
        assertCodeSelect(asList(contents), "x");
    }

    public void testCodeSelectStaticMethodFromClass() {
        String contents = "class Inner { static def x() { }\ndef y() {\n Inner.x()\n } }";
        assertCodeSelect(asList(contents), "x");
    }

    public void testCodeSelectStaticMethodFromScript() {
        String contents = "class Inner { static def x() { } }\ndef y() {\n Inner.x()\n }";
        assertCodeSelect(asList(contents), "x");
    }

    public void testCodeSelectStaticMethodInOtherClass() {
        String contents = "class PlantController {\nstatic def redirect(controller, action)  { }\n" +
            "def checkUser() {\nredirect(controller:'user',action:'login')\n}}\n";
        String contents2 =
            "class Other {\ndef doNothing() {\nPlantController.redirect(controller:'user',action:'login')\n}}";

        assertCodeSelect(asList(contents, contents2), "redirect");
    }

    public void testCodeSelectStaticMethodInOtherClass2() {
        String contents = "class C {\nstatic def r(controller)  { }\ndef checkUser() {\nr(List)\n}" + "}\n";
        assertCodeSelect(asList(contents), "List");
    }

    public void testCodeSelectStaticMethodInSuperClass() {
        String contents = "class PlantController {\ndef redirect(controller, action)  { }\n" +
            "static def checkUser() {\nredirect(controller:'user',action:'login')\n}}\n";
        String contents2 =
            "class Other extends PlantController {\nstatic def doNothing() {\nredirect(controller:'user',action:'login')\n}}";

        assertCodeSelect(asList(contents, contents2), "redirect");
    }

    public void testCodeSelectStaticInScript() {
        String contents = "doSomething()\nstatic void doSomething() { }";
        assertCodeSelect(asList(contents), "doSomething");
    }

    public void testCodeSelectStaticMethod1() {
        String contents = "class Parent {\n  static p() {}\n}\nclass Child extends Parent {\n  def c() {\n    p()\n  }\n}";
        IJavaElement elem = assertCodeSelect(asList(contents), "p");
        assertEquals("Parent", elem.getParent().getElementName());
    }

    public void testCodeSelectStaticMethod2() {
        String another = "class Parent {\n    static p() {}\n}";
        String contents = "class Child extends Parent {\n  def c() {\n    p()\n  }\n}";
        IJavaElement elem = assertCodeSelect(asList(another, contents), "p");
        assertEquals("Parent", elem.getParent().getElementName());
    }

    public void testCodeSelectStaticMethod3() {
        String contents = "class Foo { def m() { java.util.Collections.<Object>emptyList() } }";
        assertCodeSelect(asList(contents), "Collections");
    }

    public void testCodeSelectStaticMethod4() {
        String contents = "def empty = Collections.&emptyList";
        IJavaElement elem = assertCodeSelect(asList(contents), "emptyList");
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement());
        assertEquals("java.util.List <T>", method.getReturnType().toString(false)); // want T to be java.lang.String
    }

    public void testCodeSelectStaticMethod5() throws Exception {
        if (GroovyUtils.GROOVY_LEVEL < 20) return;
        String contents = "import static java.util.Collections.singletonList\n" +
            "@groovy.transform.TypeChecked\n" +
            "class Foo { static {\n" +
            "  singletonList('')\n" +
            "}}";
        IJavaElement elem = assertCodeSelect(asList(contents), "singletonList");
        MethodNode method = ((MethodNode) ((GroovyResolvedBinaryMethod) elem).getInferredElement());
        assertEquals("java.util.List <java.lang.String>", method.getReturnType().toString(false));
    }

    public void testCodeSelectStaticProperty1() {
        String contents = "class Super {\n    def static getSql() {   }\n}\n \n" +
            "class Sub extends Super {\n    def static foo() {\n        sql  \n     }\n} ";
        assertCodeSelect(asList(contents), "sql", "getSql");
    }

    public void testCodeSelectStaticProperty2() {
        String contents = "class Super {\n    def getSql() {   }\n}\n \nclass Sub extends Super {\n" +
            "    def foo() {\n        sql  \n     }\n} ";
        assertCodeSelect(asList(contents), "sql", "getSql");
    }

    public void testCodeSelectStaticProperty3() {
        String contents = "class Super {\n    def getSql() {   }\n    def foo() {\n        sql  \n" + "     }\n} ";
        assertCodeSelect(asList(contents), "sql", "getSql");
    }

    // test for GRECLIPSE-831
    public void testCodeSelectOverloadedMethod1() {
        String contents = "''.substring(0)";
        IJavaElement elem = assertCodeSelect(asList(contents), "substring");
        assertEquals("Wrong number of parameters to method", 1, ((IMethod) elem).getParameterTypes().length);
    }

    // test for GRECLIPSE-831
    public void testCodeSelectOverloadedMethod2() {
        String contents = "''.substring(0,1)";
        IJavaElement elem = assertCodeSelect(asList(contents), "substring");
        assertEquals("Wrong number of parameters to method", 2, ((IMethod) elem).getParameterTypes().length);
    }

    //

    public void testCodeSelectConstructor() throws Exception {
        String contents = "def x = new java.util.Date()";
        IJavaElement elem = assertCodeSelect(asList(contents), "Date");
        assertTrue("Expected method not type", elem instanceof IMethod);
        assertTrue("Expected ctor not method", ((IMethod) elem).isConstructor());

        // check the preceding elements for selection bleedthrough
        assertCodeSelect(asList(contents), "util", "java.util");
        assertCodeSelect(asList(contents), "java", "java");
        assertCodeSelect(asList(contents), "new", "");
        assertCodeSelect(asList(contents), "x", "x");
    }

    public void testCodeSelectConstuctorSimple() throws Exception {
        assertConstructor("p", "Bar", "Foo", "class Foo { Foo() { } }\nnew Foo()");
    }

    public void testCodeSelectConstuctorQualName() throws Exception {
        assertConstructor("p", "Bar", "p.Foo", "class Foo { Foo() { } }\nnew p.Foo()");
    }

    public void testCodeSelectConstuctorOtherFile() throws Exception {
        addGroovySource("class Foo { Foo() { } }", "Foo", "p");
        assertConstructor("p", "Bar", "Foo", "new Foo()");
    }

    public void testCodeSelectConstuctorOtherFile2() throws Exception {
        addGroovySource("class Foo {\nFoo(a) { } }", "Foo", "p");
        assertConstructor("p", "Bar", "Foo", "new Foo()");
    }

    public void testCodeSelectConstuctorOtherFile3() throws Exception {
        addGroovySource("class Foo { Foo() { }\nFoo(a) { } }", "Foo", "p");
        assertConstructor("p", "Bar", "Foo", "new Foo()");
    }

    public void testCodeSelectConstuctorJavaFile() throws Exception {
        addJavaSource("class Foo { Foo() { } }", "Foo", "p");
        assertConstructor("p", "Bar", "Foo", "new Foo()");
    }

    public void testCodeSelectConstuctorMultipleConstructors() throws Exception {
        addGroovySource("class Foo { Foo() { }\nFoo(a) { } }", "Foo", "p");
        IMethod method = assertConstructor("p", "Bar", "Foo", "new Foo()");
        assertEquals("Should have found constructor with no args", 0, method.getParameters().length);
    }

    public void testCodeSelectConstuctorMultipleConstructors2() throws Exception {
        addGroovySource("class Foo { Foo() { } \n Foo(a) { } }", "Foo", "p");
        IMethod method = assertConstructor("p", "Bar", "Foo", "new Foo(0)");
        assertEquals("Should have found constructor with 1 arg", 1, method.getParameters().length);
    }

    public void testCodeSelectConstuctorMultipleConstructors3() throws Exception {
        IMethod method = assertConstructor("p", "Bar", "Date", "new Date(0)");
        assertEquals("Should have found constructor with 1 arg", 1, method.getParameters().length);
        assertEquals("Should have found constructor Date(long)", "J", method.getParameterTypes()[0]);
    }

    public void testCodeSelectConstuctorMultipleConstructors4() throws Exception {
        // single-arg constructor is defined last and use of constant reference in ctor call means arg types not resolved at time of ctor selection
        addGroovySource("class Foo { Foo(String s1, String s2) { } \n Foo(String s1) { } }", "Foo", "p");
        addGroovySource("interface Bar { String CONST = 'whatever' }", "Bar", "p");
        IMethod method = assertConstructor("p", "Baz", "Foo", "new Foo(Bar.CONST)");
        assertEquals("Should have found constructor with 1 arg", 1, method.getParameters().length);
    }

    private IMethod assertConstructor(String packName, String className, String toSearch, String contents) throws Exception {
        ICompilationUnit unit = addGroovySource(contents, className, packName);
        prepareForCodeSelect(unit);

        IJavaElement[] elt = unit.codeSelect(unit.getSource().lastIndexOf(toSearch), toSearch.length());
        assertEquals("Should have found a selection", 1, elt.length);
        String elementName = toSearch.substring(toSearch.lastIndexOf('.') + 1);
        assertEquals("Should have found constructor '" + elementName + "'", elementName, elt[0].getElementName());
        assertTrue("Should be a constructor", ((IMethod) elt[0]).isConstructor());
        return (IMethod) elt[0];
    }
}
