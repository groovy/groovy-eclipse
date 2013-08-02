/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Andrew Eisenberg
 * @created Jun 3, 2009
 *
 */
public class CodeSelectMethodsTest extends BrowsingTestCase {

    public CodeSelectMethodsTest() {
        super(CodeSelectMethodsTest.class.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCodeSelectDefaultParams1() throws Exception {
        String structureContents = "class Structure {\n" + "  def meth(int a, int b = 9, int c=8) {}\n" + "}";
        String javaContents = "class Java { { new Structure().meth(0, 0, 0); } }";
        String groovyContents = "new Structure().meth";
        String toFind = "meth";
        assertGroovyCodeSelect(structureContents, javaContents, groovyContents, toFind);
    }

    public void testCodeSelectDefaultParams2() throws Exception {
        String structureContents = "class Structure {\n" + "  def meth(int a, int b = 9, int c=8) {}\n" + "}";
        String javaContents = "class Java { { new Structure().meth(0); } }";
        String groovyContents = "new Structure().meth(0)";
        String toFind = "meth";
        assertGroovyCodeSelect(structureContents, javaContents, groovyContents, toFind);
    }

    public void testCodeSelectDefaultParams3() throws Exception {
        String structureContents = "class Structure {\n" + "  def meth(int a, int b = 9, int c=8) {}\n" + "}";
        String javaContents = "class Java { { new Structure().meth(0, 0); } }";
        String groovyContents = "new Structure().meth(0, 0)";
        String toFind = "meth";
        assertGroovyCodeSelect(structureContents, javaContents, groovyContents, toFind);
    }

    public void testCodeSelectDefaultParams4() throws Exception {
        String structureContents = "class Structure {\n" + "  def meth(int a, int b = 9, int c=8) {}\n" + "}";
        String javaContents = "class Java { { new Structure().meth(0, 0, 0); } }";
        String groovyContents = "new Structure().meth(0, 0, 0)";
        String toFind = "meth";
        assertGroovyCodeSelect(structureContents, javaContents, groovyContents, toFind);
    }

    public void testCodeSelectClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x = {\n"+
        "t -> print t\n"+
        "}\n"+
        "x(\"hello\")\n";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectInt() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x = 9\n"+
        "x++\n";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectReAssign() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x = {\n"+
        "t -> print t\n"+
        "}\n"+
        "x(\"hello\")\n" +
        "x = 9\n";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }
    public void testCodeSelectMethodInClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class PlantController {\n"+
        "def redirect(controller, action)  { }\n"+
        "def checkUser() {\n" +
        "redirect(controller:'user',action:'login')\n" +
        "}}\n";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("redirect"), "redirect".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'redirect'", "redirect", elt[0].getElementName());
    }

    public void testCodeSelectMethodInOtherClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class PlantController {\n"+
        "def redirect(controller, action)  { }\n"+
        "def checkUser() {\n" +
        "redirect(controller:'user',action:'login')\n" +
        "}}\n";
        env.addGroovyClass(root, "", "Hello", contents);

        String contents2 = "class Other {\ndef doNothing() {\nnew PlantController().redirect(controller:'user',action:'login')\n}}";
        env.addGroovyClass(root, "", "Hello2", contents2);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello2.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf("redirect"), "redirect".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'redirect'", "redirect", elt[0].getElementName());
    }

    public void testCodeSelectMethodInSuperClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class PlantController {\n"+
        "def redirect(controller, action)  { }\n"+
        "def checkUser() {\n" +
        "redirect(controller:'user',action:'login')\n" +
        "}}\n";
        env.addGroovyClass(root, "", "Hello", contents);

        String contents2 = "class Other extends PlantController {\ndef doNothing() {\nredirect(controller:'user',action:'login')\n}}";
        env.addGroovyClass(root, "", "Hello2", contents2);
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello2.groovy");
        unit.becomeWorkingCopy(null);
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf("redirect"), "redirect".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'redirect'", "redirect", elt[0].getElementName());
    }

    public void testCodeSelectMethodInScriptFromScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x() { }\n"+
        "x()\n";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local method 'x'", "x", elt[0].getElementName());
    }
    public void testCodeSelectMethodInClassFromScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Inner { def x() { } }\n"+
        "new Inner().x()\n";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local method 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectStaticMethodFromClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Inner { static def x() { }\n" +
        		"def y() {\n Inner.x()\n } }";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local method 'x'", "x", elt[0].getElementName());
    }
    public void testCodeSelectStaticMethodFromScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Inner { static def x() { } }\n" +
                "def y() {\n Inner.x()\n }";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local method 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectStaticMethodInOtherClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class PlantController {\n"+
        "static def redirect(controller, action)  { }\n"+
        "def checkUser() {\n" +
        "redirect(controller:'user',action:'login')\n" +
        "}}\n";
        env.addGroovyClass(root, "", "Hello", contents);

        String contents2 = "class Other {\ndef doNothing() {\nPlantController.redirect(controller:'user',action:'login')\n}}";
        env.addGroovyClass(root, "", "Hello2", contents2);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello2.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf("redirect"), "redirect".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'redirect'", "redirect", elt[0].getElementName());
    }

    public void testCodeSelectStaticMethodInOtherClass2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents =
            "class C {\n"+
               "static def r(controller)  { }\n"+
               "def checkUser() {\n" +
                  "r(List)\n" +
               "}" +
            "}\n";
        env.addGroovyClass(root, "", "Hello", contents);

        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("List"), "List".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'List'", "List", elt[0].getElementName());
    }
    public void testCodeSelectStaticMethodInSuperClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class PlantController {\n"+
        "def redirect(controller, action)  { }\n"+
        "static def checkUser() {\n" +
        "redirect(controller:'user',action:'login')\n" +
        "}}\n";
        env.addGroovyClass(root, "", "Hello", contents);

        String contents2 = "class Other extends PlantController {\nstatic def doNothing() {\nredirect(controller:'user',action:'login')\n}}";
        env.addGroovyClass(root, "", "Hello2", contents2);
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello2.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf("redirect"), "redirect".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'redirect'", "redirect", elt[0].getElementName());
    }

    public void testCodeSelectStaticInScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "doSomething()\nstatic void doSomething() { }";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("doSomething"),
                "doSomething".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'doSomething'", "doSomething",
                elt[0].getElementName());
    }

    public void testCodeSelectStaticMethod1() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Parent {\n" + "    static p() {}\n" + "}\n" + "class Child extends Parent {\n" + "    def c() {\n"
                + "        p()\n" + "    }\n" + "}";
        env.addGroovyClass(root, "", "Child", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Child.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("p"), "p".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found type 'Parent'", "Parent", elt[0].getParent().getElementName());
    }

    public void testCodeSelectStaticMethod2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", "Parent", "class Parent {\n" + "    static p() {}\n" + "}\n");

        String contents = "class Child extends Parent {\n" + "    def c() {\n"
                + "        p()\n" + "    }\n" + "}";
        env.addGroovyClass(root, "", "Child", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Child.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("p"), "p".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found type 'Parent'", "Parent", elt[0].getParent().getElementName());
    }

    public void testCodeSelectStaticProperty1() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Super {\n" + "    def static getSql() {   }\n"
                + "}\n" + " \n" + "class Sub extends Super {\n"
                + "    def static foo() {\n" + "        sql  \n" + "     }\n"
                + "} ";
        env.addGroovyClass(root, "", "Super", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Super.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("sql"),
                "sql".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'getSql'", "getSql",
                elt[0].getElementName());
    }

    public void testCodeSelectStaticProperty2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Super {\n" + "    def getSql() {   }\n"
                + "}\n" + " \n" + "class Sub extends Super {\n"
                + "    def foo() {\n" + "        sql  \n" + "     }\n" + "} ";
        env.addGroovyClass(root, "", "Super", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Super.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("sql"),
                "sql".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'getSql'", "getSql",
                elt[0].getElementName());
    }

    public void testCodeSelectStaticProperty3() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Super {\n" + "    def getSql() {   }\n"
                + "    def foo() {\n" + "        sql  \n" + "     }\n" + "} ";
        env.addGroovyClass(root, "", "Super", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Super.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("sql"),
                "sql".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'getSql'", "getSql",
                elt[0].getElementName());
    }

    // test for GRECLIPSE-831
    public void testCodeSelectOverloadedMethod1() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "''.substring(0)";
        env.addGroovyClass(root, "", "Super", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Super.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("substring"), "substring".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'substring'", "substring", elt[0].getElementName());
        assertEquals("Wrong number of parameters to method", 1, ((IMethod) elt[0]).getParameterTypes().length);
    }

    // test for GRECLIPSE-831
    public void testCodeSelectOverloadedMethod2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "''.substring(0,1)";
        env.addGroovyClass(root, "", "Super", contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Super.groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("substring"), "substring".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'substring'", "substring", elt[0].getElementName());
        assertEquals("Wrong number of parameters to method", 2, ((IMethod) elt[0]).getParameterTypes().length);
    }

    private IMethod assertConstructor(IPath root, String packName, String className, String toSearch, String contents)
            throws Exception,
            JavaModelException {
        env.addGroovyClass(root, packName, className, contents);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root.append(packName), className + ".groovy");
        unit.becomeWorkingCopy(null);
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf(toSearch), toSearch.length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found constructor 'Foo'", "Foo", elt[0].getElementName());
        assertTrue("Should be a constructor", ((IMethod) elt[0]).isConstructor());
        return (IMethod) elt[0];
    }

    public void testConstuctorSimple() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "package p\nclass Foo { Foo() { } }\nnew Foo()";
        assertConstructor(root, "p", "Foo2", "Foo", contents);
    }

    public void testConstuctorQualName() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "package p\nclass Foo { Foo() { } }\nnew p.Foo()";
        assertConstructor(root, "p", "Foo2", "p.Foo", contents);
    }

    public void testConstuctorOtherFile() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p", "Foo", "package p\nclass Foo { Foo() { } }");
        String contents = "package p\nnew Foo()";
        assertConstructor(root, "p", "Foo2", "Foo", contents);
    }

    public void testConstuctorJavaFile() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addClass(root, "p", "Foo", "package p;\nclass Foo { Foo() { } }");
        String contents = "package p\nnew Foo()";
        assertConstructor(root, "p", "Foo2", "Foo", contents);
    }

    public void testConstuctorMultipleConstructors() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p", "Foo", "package p\nclass Foo { Foo() { }\nFoo(a) { } }");
        String contents = "package p\nnew Foo()";
        IMethod method = assertConstructor(root, "p", "Foo2", "Foo", contents);
        // should have arbitrarily found the first method
        assertEquals("Should have found a constructor with no args", 0, method.getParameterNames().length);
    }

    public void testConstuctorMultipleConstructors2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "p", "Foo", "package p\nclass Foo { Foo() { }\nFoo(a) { } }");
        String contents = "package p\nnew Foo(0)";
        IMethod method = assertConstructor(root, "p", "Foo2", "Foo", contents);
        // should have arbitrarily found the first method
        assertEquals("Should have found a constructor with no args", 0, method.getParameterNames().length);
    }
}
