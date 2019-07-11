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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Ignore;
import org.junit.Test;

public final class GroovyPartialModelTests  extends GroovyTypeRootTestSuite {

    @Test
    public void testFieldInitializerIsAvailable1() throws Exception {
        findFieldInitializer(
            "package p\n" +
            "class C {\n" +
            "  public static aField = []\n" +
            "}\n",
            ListExpression.class);
    }

    @Test
    public void testFieldInitializerIsAvailable2() throws Exception {
        findFieldInitializer(
            "package p\n" +
            "class C {\n" +
            "  public static aField = { -> }\n" +
            "}\n",
            ClosureExpression.class);
    }

    @Test @Ignore
    public void testFieldInitializerIsAvailable3() throws Exception {
        findFieldInitializer(
            "package p\n" +
            "class C {\n" +
            "  public Object aField = []\n" +
            "}\n",
            ListExpression.class);
    }

    @Test @Ignore
    public void testFieldInitializerIsAvailable4() throws Exception {
        findFieldInitializer(
            "package p\n" +
            "class C {\n" +
            "  public Object aField = { -> }\n" +
            "}\n",
            ClosureExpression.class);
    }

    @Test
    public void testClosureReturner() throws Exception {
        IProject project = createSimpleGroovyProject().getProject();
        env.addGroovyClass(project.getFullPath().append("src"), "p1", "Hello2",
            //@formatter:off
            "class C { def aaa = { 123 } }");
            //@formatter:on
        IFile javaFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("Project/src/p1/Hello2.groovy"));
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(javaFile);
        ClassNode inClass = unit.getModuleNode().getClasses().get(0);
        FieldNode field = inClass.getField("aaa");
        Expression initialExpression = field.getInitialExpression();
        ClosureExpression cEx = (ClosureExpression) initialExpression;
        BlockStatement bSt = (BlockStatement) cEx.getCode();
        Statement st = bSt.getStatements().get(0);
        assertEquals("org.codehaus.groovy.ast.stmt.ReturnStatement", st.getClass().getName());
    }

    //--------------------------------------------------------------------------

    private Expression findFieldInitializer(String contents, Class<? extends Expression> expressionClass) throws Exception {
        IProject project = createSimpleGroovyProject().getProject();
        GroovyCompilationUnit unit = env.getUnit(env.addGroovyClass(project.getFullPath().append("src"), "p", "C", contents));

        ClassNode clazz = unit.getModuleNode().getClasses().get(0);
        FieldNode field = clazz.getField("aField");
        Expression initialExpression = field.getInitialExpression();
        assertNotNull(initialExpression);
        assertTrue(expressionClass.isInstance(initialExpression));
        checkClinitAndAllConstructors(initialExpression, clazz);
        return initialExpression;
    }

    private void checkClinitAndAllConstructors(Expression expr, ClassNode inClass) {
        for (ConstructorNode cons : inClass.getDeclaredConstructors()) {
            assertUnique(expr, cons);
        }
        MethodNode clinit = inClass.getMethod("<clinit>", Parameter.EMPTY_ARRAY);
        if (clinit != null) {
            assertUnique(expr, clinit);
        }
    }

    // asserts that the given expression has not been copied into the constructor
    // and so makes sure that this expression appears only once in the AST
    private static void assertUnique(Expression expr, MethodNode node) {
        GroovyClassVisitor visitor = new ClassCodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression e) {
                doCheck(e);
                super.visitClosureExpression(e);
            }

            @Override
            public void visitListExpression(ListExpression e) {
                doCheck(e);
                super.visitListExpression(e);
            }

            void doCheck(Expression e) {
                assertFalse("Expression appears twice.  Once in constructor and once in field initializer.\nExpr: " + e, e == expr);
            }
        };
        visitor.visitMethod(node);
    }
}
