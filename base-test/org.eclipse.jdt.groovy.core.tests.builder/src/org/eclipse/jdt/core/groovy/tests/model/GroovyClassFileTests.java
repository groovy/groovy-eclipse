/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.BinaryType;
import org.junit.Test;

/**
 * Tests that our support for class files with Groovy source attachments is working.
 */
public final class GroovyClassFileTests extends GroovyTypeRootTestSuite {

    @Test // a class file in a groovy project should not include the non-source children
    public void testClassFileHasNoNonSourceChildren() throws Exception {
        IProject project = createSimpleGroovyProject().getProject();
        env.addJar(project.getFullPath(), "lib/test-groovy-project.jar");
        IJavaProject javaProject = JavaCore.create(project);
        IType binaryType = javaProject.findType("AGroovyClass");
        ((BinaryType) binaryType).getSource();
        assertEquals("should have 2 children: prop1 and prop2...no getters or setters", 2, binaryType.getChildren().length);
        assertEquals("wrong property name", "prop1", binaryType.getChildren()[0].getElementName());
        assertEquals("wrong property name", "prop2", binaryType.getChildren()[1].getElementName());

        assertTrue("source range for prop1 should be valid", ((IMember) binaryType.getChildren()[0]).getSourceRange().getOffset() > 0);
        assertTrue("source range for prop2 should be valid", ((IMember) binaryType.getChildren()[1]).getSourceRange().getOffset() > 0);
    }

    @Test // a class file in a java project should include the non-source children
    public void testClassFileInJavaProjectHasNonSourceChildren() throws Exception {
        IProject project = createSimpleJavaProject().getProject();
        env.addJar(project.getFullPath(), "lib/test-groovy-project.jar");
        IJavaProject javaProject = JavaCore.create(project);
        IType binaryType = javaProject.findType("AGroovyClass");
        ((BinaryType) binaryType).getSource();

        // the value should be somewhere upwards of 39.
        assertTrue("should have many children: prop1 and prop2 and generated methods and fields", binaryType.getChildren().length > 2);

        assertTrue("source range for prop1 should be valid", binaryType.getField("prop1").getSourceRange().getOffset() > 0);
        assertTrue("source range for prop2 should be valid", binaryType.getField("prop2").getSourceRange().getOffset() > 0);
    }

    @Test
    public void testCodeSelectInClassFile() throws Exception {
        IProject project = createSimpleGroovyProject().getProject();
        env.addJar(project.getFullPath(), "lib/code-select/test-project-for-code-select.jar");
        env.addGroovyNature(project.getName());
        IJavaProject javaProject = JavaCore.create(project);
        IType binaryType = javaProject.findType("AGroovyClassForCodeSelect");
        IClassFile classFile = binaryType.getClassFile();
        String contents = classFile.getBuffer().getContents();

        // now select multiple locations in the file
        lookForProperties(classFile, contents, "prop1");
        lookForProperties(classFile, contents, "prop2");
    }

    private void lookForProperties(IClassFile classFile, String contents, String prop) throws Exception {
        int first = contents.indexOf(prop),
            second = contents.indexOf(prop, first + 1),
            third = contents.indexOf(prop, second + 1);
        IJavaElement[] found = classFile.codeSelect(first, prop.length());
        assertElementFound(prop, found);
        found = classFile.codeSelect(second, prop.length());
        assertElementFound(prop, found);
        found = classFile.codeSelect(third, prop.length());
        assertElementFound(prop, found);
    }

    private void assertElementFound(String prop, IJavaElement[] found) {
        assertEquals("Expected to find one element but didn't", 1, found.length);
        assertEquals("Expected to find a field but didn't", IJavaElement.FIELD, found[0].getElementType());
        assertEquals("Element found with wrong name", prop, found[0].getElementName());
    }
}
