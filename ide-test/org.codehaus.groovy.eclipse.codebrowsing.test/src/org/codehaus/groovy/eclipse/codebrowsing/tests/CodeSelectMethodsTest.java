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

}
