/*
 * Copyright 2003-2010 the original author or authors.
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

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;

/**
 *
 * @author andrew
 * @created Jul 26, 2011
 */
public class CodeSelectLocalTest extends BrowsingTestCase {
    public CodeSelectLocalTest() {
        super(CodeSelectLocalTest.class.getName());
    }

    public void testLocalVar1() throws Exception {
        assertSelection("def xxx(xxx) { xxx }", "xxx");
    }

    public void testLocalVar2() throws Exception {
        assertSelection("def xxx(xxx) { \"${xxx}\" }", "xxx");
    }

    public void testLocalVar3() throws Exception {
        assertSelection("def xxx = { xxx -> \"${xxx}\" }", "xxx");
    }

    public void testLocalVar4() throws Exception {
        String contents = "def (xxx, yyy) = []\nxxx\nyyy";
        assertSelection(contents, "xxx");
        assertSelection(contents, "yyy");
    }

    void assertSelection(String contents, String varName) throws Exception {
        IPath projectPath = createGenericProject();
        IPath root = projectPath.append("src");
        env.addGroovyClass(root, "", "Hello", contents);
        env.incrementalBuild();
        GroovyCompilationUnit unit = getGroovyCompilationUnit(root, "Hello.groovy");
        assertTrue("Hello groovy unit should exist", unit.exists());
        IJavaElement[] elt = unit.codeSelect(contents.lastIndexOf(varName), varName.length());
        assertEquals("Should have found a selection", 1, elt.length);
        assertEquals("Should have found local variable", varName, elt[0].getElementName());
        assertTrue("Should exist", elt[0].exists());
    }
}
