/*
 * Copyright 2011 the original author or authors.
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

import junit.framework.Test;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;


/**
 * @author Andrew Eisenberg
 * @created May 9, 2011
 */
public class GroovyPartialModelTests  extends AbstractGroovyTypeRootTests {
    public GroovyPartialModelTests(String name) {
        super(name);
    }
    public static Test suite() {
        return buildTestSuite(GroovyPartialModelTests.class);
    }
    
    // tests that a field's static initializer is not erased during a reconcile
    public void testClassFileHasNoNonSourceChildren() throws Exception {
        IProject project = createSimpleGroovyProject().getProject();
        env.addGroovyClass(project.getFullPath().append("src"), "p1", "Hello2",
                "package p1\n"+
                "public class Hello {\n"+
                "  static aStatic = []\n" +
                "}\n"
                );
            
        IFile javaFile = getFile("Project/src/p1/Hello2.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(javaFile);
        FieldNode field = unit.getModuleNode().getClasses().get(0).getField("aStatic");
        assertNotNull(field.getInitialExpression());
        assertTrue(field.getInitialExpression() instanceof ListExpression);
    }
    
}
