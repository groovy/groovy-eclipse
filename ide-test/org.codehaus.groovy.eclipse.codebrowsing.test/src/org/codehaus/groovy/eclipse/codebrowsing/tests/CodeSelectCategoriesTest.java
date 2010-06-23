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
 * @created Aug 5, 2009
 * 
 *
 */
public class CodeSelectCategoriesTest extends BrowsingTestCase {

    public CodeSelectCategoriesTest() {
        super(CodeSelectCategoriesTest.class.getName());
    }
    
    public void testDGM() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "each { }";
        env.addGroovyClass(root, "", "Hello", contents);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf("each"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'each'", "each", elt[0].getElementName());
    }
    
    public void testGroovyCategory() throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        String contents = "class MyCategory { static doNothing(Object o) { } }";
        env.addGroovyClass(root, "", "MyCategory", contents);
        
        String contents2 = "use(MyCategory) { doNothing() }";
        env.addGroovyClass(root, "", "Hello", contents2);
        incrementalBuild(projectPath);
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        IJavaElement[] elt = unit.codeSelect(contents2.lastIndexOf("doNothing"), 1);
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable 'doNothing'", "doNothing", elt[0].getElementName());
    }
}
