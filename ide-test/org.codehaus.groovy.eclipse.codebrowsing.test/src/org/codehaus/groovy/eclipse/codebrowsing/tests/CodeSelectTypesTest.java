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
 * @created Jul 14, 2009
 *
 */
public class CodeSelectTypesTest extends BrowsingTestCase {

    public CodeSelectTypesTest() {
        super(CodeSelectTypesTest.class.getName());
    }

    public void testSelectSuperClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSuper = "class Super { }";
        String contentsSub = "class Sub extends Super { }";
        env.addGroovyClass(root, "", "Super", contentsSuper);
        env.addGroovyClass(root, "", "Sub", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Sub.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("Super"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'Super'", "Super",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Super' should exist",
                elt[0].exists());
    }

    public void testSelectSuperClass2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSuper = "class Super { }";
        String contentsSub = "class Sub extends Super { }";
        env.addGroovyClass(root, "", "Super2", contentsSuper);
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("Super"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'Super'", "Super",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Super' should exist",
                elt[0].exists());
    }

    public void testSelectThisClass() throws Exception {
        String contents = "class This { }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("This"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'This'", "This",
                elt[0].getElementName());
        assertTrue("Java Element for type 'This' should exist", elt[0].exists());
    }

    public void testSelectFieldType() throws Exception {
        String contents = "class Type { List x }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List",
                elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }

    public void testSelectMethodType() throws Exception {
        String contents = "class Type { List x() { new ArrayList() } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List",
                elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }

    public void testSelectMethodParamType() throws Exception {
        String contents = "class Type { def x(List y) {} }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List",
                elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }

    public void testSelectLocalVarType() throws Exception {
        String contents = "class Type { def x() { List y } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List",
                elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }

    public void testSelectLocalVarTypeInClosure() throws Exception {
        String contents = "class Type { def x() { def foo = {\n   List y } } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List",
                elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }

    public void testSelectLocalVarTypeInScript() throws Exception {
        String contents = "List y";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List",
                elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }

    public void testSelectTypeInAnnotation1() throws Exception {
        String contents = "@RunWith(ATest)\n" + "class ATest { }\n"
                + "@interface RunWith {\n" + "Class value()\n}";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("ATest"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'ATest'", "ATest",
                elt[0].getElementName());
        assertTrue("Java Element for type 'ATest' should exist",
                elt[0].exists());
    }

    public void testSelectTypeInAnnotation2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String javaContents = "enum Foo {\n" + "FOO1, FOO2\n" + "} \n"
                + "@interface RunWith {\n" + "Foo value();\n" + "}";
        env.addClass(root, "Foo", javaContents);
        String contents = "@RunWith(Foo.FOO1)\n" + "class ATest { }\n"
                + "@interface RunWith {\n" + "Class value()\n}";
        env.addGroovyClass(root, "", "Sub2", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("Foo"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'Foo'", "Foo",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Foo' should exist", elt[0].exists());
    }

    public void testSelectTypeInAnnotation3() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String javaContents = "enum Foo {\n" + "FOO1, FOO2\n" + "} \n"
                + "@interface RunWith {\n" + "Foo value();\n" + "}";
        env.addClass(root, "Foo", javaContents);
        String contents = "@RunWith(Foo.FOO1)\n" + "class ATest { }\n"
                + "@interface RunWith {\n" + "Class value()\n}";
        env.addGroovyClass(root, "", "Sub2", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("FOO1"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'FOO1'", "FOO1",
                elt[0].getElementName());
        assertTrue("Java Element for type 'FOO1' should exist", elt[0].exists());
    }

    /**
     * GRECLIPSE-548
     */
    public void testSelectThis1() throws Exception {
        String contents = "class AClass { " + "def x() { this } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("this"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'AClass'", "AClass",
                elt[0].getElementName());
        assertTrue("Java Element for type 'AClass' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-548
     */
    public void testSelectThis2() throws Exception {
        String contents = "class AClass { " + "def x() { this.toString() } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("this"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'AClass'", "AClass",
                elt[0].getElementName());
        assertTrue("Java Element for type 'AClass' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-548
     */
    public void testSelectSuper1() throws Exception {
        String contents = "class Super { } \n class AClass extends Super { "
                + "def x() { super } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("super"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'Super'", "Super",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Super' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-548
     */
    public void testSelectSuper2() throws Exception {
        String contents = "class Super { } \n class AClass extends Super { "
                + "def x() { super.toString() } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("super"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'Super'", "Super",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Super' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-800
     */
    public void testSelectInnerType() throws Exception {
        String contents = "class Outer { \n def m() { Inner x = new Inner() } \n class Inner { } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("Inner"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'Inner'", "Inner",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Inner' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-800
     */
    public void testSelectInnerType2() throws Exception {
        String contents = "class Outer { \n def m() { new Inner() } \n class Inner { } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("Inner"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'Inner'", "Inner",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Inner' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-800
     */
    public void testSelectInnerType3() throws Exception {
        String contents = "class Outer { \n def m() { Inner } \n class Inner { } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("Inner"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'Inner'", "Inner",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Inner' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-803
     */
    public void testSelectInnerType4() throws Exception {
        String contents = "class Outer { \n class Inner { } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("Inner"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'Inner'", "Inner",
                elt[0].getElementName());
        assertTrue("Java Element for type 'Inner' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-803
     */
    public void testSelectInnerType5() throws Exception {
        String contents = "class Outer { \n class Inner { \n class InnerInner { } } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("InnerInner"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'InnerInner'", "InnerInner",
                elt[0].getElementName());
        assertTrue("Java Element for type 'InnerInner' should exist",
                elt[0].exists());
    }

    /**
     * GRECLIPSE-803
     */
    public void testSelectInnerType6() throws Exception {
        String contents = "class Outer { \n class Inner { \n class InnerInner { \n class InnerInnerInner { } } } }";
        GroovyCompilationUnit unit = createCompilationUnit(contents);
        IJavaElement[] elt = unit.codeSelect(
                contents.indexOf("InnerInnerInner"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found class 'InnerInnerInner'",
                "InnerInnerInner", elt[0].getElementName());
        assertTrue("Java Element for type 'InnerInnerInner' should exist",
                elt[0].exists());
    }

    private GroovyCompilationUnit createCompilationUnit(String contents)
            throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", "Clazz", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root,
                "Clazz.groovy");
        return unit;
    }
}
