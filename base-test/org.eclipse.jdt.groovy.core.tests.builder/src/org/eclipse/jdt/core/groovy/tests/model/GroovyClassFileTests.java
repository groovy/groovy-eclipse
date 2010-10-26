/*
 * Copyright 2003-2009 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.model;

import groovy.time.BaseDuration.From;
import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.BinaryType;


/**
 * Tests that our support for class files with Groovy source attachments is working
 * @author Andrew Eisenberg
 * @created Oct 25, 2010
 */
public class GroovyClassFileTests  extends AbstractGroovyTypeRootTests {
    public GroovyClassFileTests(String name) {
        super(name);
    }
    public static Test suite() {
        return buildTestSuite(GroovyClassFileTests.class);
    }
    
    // a class file in a groovy project should not include the non-source children
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
    
    // a class file in a java project should include the non-source children
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
    
}
