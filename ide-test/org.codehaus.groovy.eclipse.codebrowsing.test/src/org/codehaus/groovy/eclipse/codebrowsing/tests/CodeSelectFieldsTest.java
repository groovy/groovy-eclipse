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
public class CodeSelectFieldsTest extends BrowsingTestCase {

    public CodeSelectFieldsTest() {
        super(CodeSelectFieldsTest.class.getName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCodeSelectVarInScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x = 9\nx++\n";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectFieldInClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Foo { def x = 9\n" + "def y() {\nx++\n } }";
        env.addGroovyClass(root, "", "Hello1", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello1.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x",
                elt[0].getElementName());
    }

    public void testCodeSelectFieldInOtherClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "package p\nclass Foo { def x = 9\n }\n";
        env.addGroovyClass(root, "p", "Hello", contents);
        String contents2 = "package p\nclass Bar { def y() { new Foo().x++\n }\n }";
        IPath hello2Path = env.addGroovyClass(root, "p", "Hello2", contents2);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(hello2Path);
        assertTrue("Hello2 groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectFieldInSuperClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "package p\nclass Foo { def x = 9\n }\n";
        env.addGroovyClass(root, "p", "Hello", contents);
        String contents2 = "package p\nclass Bar extends Foo { def y() { x++\n }\n }";
        IPath hello2Path = env.addGroovyClass(root, "p", "Hello2", contents2);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(hello2Path);
        assertTrue("Hello2 groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectStaticFieldInClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Foo { static def x = 9\n"+
        "def y() {\nFoo.x++\n } }";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectStaticFieldInOtherClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class Foo { static def x = 9\n }\n";
        env.addGroovyClass(root, "", "Hello", contents);
        String contents2 = "class Bar { def y() { Foo.x++\n }\n }";
        env.addGroovyClass(root, "", "Hello2", contents2);
        incrementalBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello2.groovy");
        assertTrue("Hello2 groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf('x'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'x'", "x", elt[0].getElementName());
    }

    public void testCodeSelectInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x = {\n"+
        "t -> print t\n"+
        "}\n"+
        "x(\"hello\")\n";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('t'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 't'", "t", elt[0].getElementName());
    }
    public void testCodeSelectInClosure2Params() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def x = {\n"+
        "s, t -> print t\n"+
        "}\n"+
        "x(\"hello\")\n";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('t'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 't'", "t", elt[0].getElementName());
    }
    public void testCodeSelectLocalVarInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def y = 9\ndef x = {\n" + "t -> print y\n" + "}\n";
        env.addGroovyClass(root, "", "Hello2", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello2.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('y'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'y'", "y",
                elt[0].getElementName());
    }

    public void testCodeSelectFieldInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class X { \n def y=9\n } \n" +
        		"def x = {\n"+
        		"t -> print new X().y\n"+
        		"}\n";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('y'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'y'", "y", elt[0].getElementName());
    }

    public void testCodeSelectFieldFromSuperInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class X { \n def y=9\n } \n" +
                "class Y extends X { }\n" +
                "def x = {\n"+
                "t -> print new Y().y\n"+
                "}\n";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('y'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'y'", "y", elt[0].getElementName());
    }

    public void testCodeSelectStaticFieldInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class X { \n static def y=9\n " +
        		"\ndef z() {\n" +
        		    "def x = {\n"+
        		            "t -> print X.y\n"+
        		        "}\n" +
      		        "}\n" +
        		"}";
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('y'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'y'", "y", elt[0].getElementName());
    }

    public void testCodeSelectStaticFieldFromOtherInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class X { \n static def y=9\n } \n" +
                "def x = {\n"+
                "t -> print X.y\n"+
                "}\n";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf('y'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'y'", "y", elt[0].getElementName());
    }

    public void testCodeSelectInFieldInitializer() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class X { \n def y= { z() }\ndef z() { } }";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.indexOf('z'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'z'", "z", elt[0].getElementName());
    }
    public void testCodeSelectInStaticFieldInitializer() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class X { \n static y= { z() }\nstatic z() { } }";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.indexOf('z'), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'z'", "z", elt[0].getElementName());
    }

    // GRECLIPSE-516
    public void testCodeSelectOfGeneratedGetter() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class C { \n int num\ndef foo() {\n getNum() } }";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("getNum"), "getNum".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'num'", "num", elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }
    // GRECLIPSE-516
    public void testCodeSelectOfGeneratedSetter() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class C { \n int num\ndef foo() {\n setNum() } }";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("setNum"), "setNum".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'num'", "num", elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }

    public void testCodeSelectInsideGString1() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def foo\n\"${foo}\"";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("foo"),
                "foo".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'foo'", "foo",
                elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }

    public void testCodeSelectInsideGString2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def foo\n\"${foo.toString()}\"";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("foo"),
                "foo".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'foo'", "foo",
                elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }

    public void testCodeSelectInsideGString3() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def foo\n\"${foo.toString()}\"";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("toString"),
                "toString".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'toString'", "toString",
                elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }

    public void testCodeSelectInsideGString4() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def foo\n\"${foo}\"";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("o") + 1, 0);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'foo'", "foo",
                elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }

    public void testCodeSelectInsideGString5() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "def foo\n\"${toString()}\"";
        env.addGroovyClass(root, "", "Hello", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("toString"),
                "toString".length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found method 'toString'", "toString",
                elt[0].getElementName());
        assertTrue("Element should exist", elt[0].exists());

    }
}