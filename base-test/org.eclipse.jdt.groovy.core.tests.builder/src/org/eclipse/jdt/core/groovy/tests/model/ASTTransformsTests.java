/*
 * Copyright 2009-2020 the original author or authors.
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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.junit.Before;
import org.junit.Test;

public final class ASTTransformsTests extends BuilderTestSuite {

    @Before
    public void setUp() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);
    }

    @Test
    public void testDelegateAnnotationFromOtherField() throws Exception {
        createUnit("Other",
            "class Other {\n" +
            "  @Delegate Date me\n" +
            "  int compareTo(arg0) {}\n" +
            "}");

        GroovyCompilationUnit unit = createUnit("ThisUnit", "Other");
        env.fullBuild();
        expectingNoProblems();
        ClassNode clazz = getClassFromScript(unit);
        clazz.getFields();  // force lazy initialization
        FieldNode field = clazz.getField("me");
        assertAnnotation("groovy.lang.Delegate", field);
    }

    @Test
    public void testDelegateAnnotationFromOtherMethod() throws Exception {
        createUnit("Other",
            "class Other {\n" +
            "  @Delegate Date me\n" +
            "  @Newify int compareTo(Object that) {}\n" +
            "}");

        GroovyCompilationUnit unit = createUnit("ThisUnit", "Other");
        env.fullBuild();
        expectingNoProblems();

        MethodNode meth = getClassFromScript(unit).getMethods("compareTo").stream().filter(
            mn -> mn.getParameters()[0].getType().getName().equals("java.lang.Object")
        ).findFirst().get();
        assertAnnotation("groovy.lang.Newify", meth);
    }

    @Test
    public void testSingletonAnnotationFromOtherClass() throws Exception {
        createUnit("Other",
            "@Singleton class Other {}");

        GroovyCompilationUnit unit = createUnit("ThisUnit", "Other");
        env.fullBuild();
        expectingNoProblems();
        ClassNode clazz = getClassFromScript(unit);
        assertAnnotation("groovy.lang.Singleton", clazz);
    }

    //--------------------------------------------------------------------------

    private void assertAnnotation(String aName, AnnotatedNode node) {
        assertEquals("Expecting @" + aName + " but no annotations found.", 1, node.getAnnotations().size());
        assertEquals(aName, node.getAnnotations().get(0).getClassNode().getName());
    }

    private ClassNode getClassFromScript(GroovyCompilationUnit unit) {
        return ((ClassExpression) ((ReturnStatement) unit.getModuleNode().getStatementBlock().getStatements().get(0)).getExpression()).getType();
    }

    private GroovyCompilationUnit createUnit(String name, String contents) {
        IPath path = env.addGroovyClass(ResourcesPlugin.getWorkspace().getRoot().getProject("Project").getFolder("src").getFullPath(), name, contents);
        return (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(env.getWorkspace().getRoot().getFile(path));
    }
}
