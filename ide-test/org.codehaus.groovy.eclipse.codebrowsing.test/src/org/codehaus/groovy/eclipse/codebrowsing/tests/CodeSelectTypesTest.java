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
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testSelectSuperClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSuper = "class Super { }";
        String contentsSub = "class Sub extends Super { }";
        env.addGroovyClass(root, "", "Super", contentsSuper);
        env.addGroovyClass(root, "", "Sub", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("Super"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'Super'", "Super", elt[0].getElementName());
        assertTrue("Java Element for type 'Super' should exist", elt[0].exists());
    }
    public void testSelectSuperClass2() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSuper = "class Super { }";
        String contentsSub = "class Sub extends Super { }";
        env.addGroovyClass(root, "", "Super2", contentsSuper);
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("Super"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'Super'", "Super", elt[0].getElementName());
        assertTrue("Java Element for type 'Super' should exist", elt[0].exists());
    }
    
    public void testSelectThisClass() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class This { }";
        env.addGroovyClass(root, "", "This", contents);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "This.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.indexOf("This"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'This'", "This", elt[0].getElementName());
        assertTrue("Java Element for type 'This' should exist", elt[0].exists());
    }
    
    
    public void testSelectFieldType() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSub = "class Type { List x }";
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List", elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }
    
    public void testSelectMethodType() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSub = "class Type { List x() { new ArrayList() } }";
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List", elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }
    public void testSelectMethodParamType() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSub = "class Type { def x(List y) {} }";
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List", elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }
    
    public void testSelectLocalVarType() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSub = "class Type { def x() { List y } }";
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List", elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }
    public void testSelectLocalVarTypeInClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSub = "class Type { def x() { def foo = {\n   List y } } }";
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List", elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }
    public void testSelectLocalVarTypeInScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contentsSub = "List y";
        env.addGroovyClass(root, "", "Sub2", contentsSub);
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Sub2.groovy");
        IJavaElement[] elt = unit.codeSelect(contentsSub.indexOf("List"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found super type 'List'", "List", elt[0].getElementName());
        assertTrue("Java Element for type 'List' should exist", elt[0].exists());
    }
    
    
}
