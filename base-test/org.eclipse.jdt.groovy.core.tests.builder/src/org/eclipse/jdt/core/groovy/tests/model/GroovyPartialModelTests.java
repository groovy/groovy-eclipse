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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


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
    
    // tests that a static field's initializer is not erased during a reconcile
    public void testStaticFieldInitializerIsNotMoved1() throws Exception {
        findFieldInitializer("package p1\n"+
                "public class Hello {\n"+
                "  static aStatic = []\n" +
                "}\n", ListExpression.class);
    }
    
    // tests that a static field's initializer is not erased during a reconcile
    public void testStaticFieldInitializerIsNotMoved2() throws Exception {
        findFieldInitializer("package p1\n"+
                "public class Hello {\n"+
                "  static aStatic = {}\n" +
                "}\n", ClosureExpression.class);
    }
    // tests that a non-static field initializer is not erased during a reconcile
    public void testFieldInitializerIsNotMoved1() throws Exception {
        findFieldInitializer("package p1\n"+
                "public class Hello {\n"+
                "  def aStatic = []\n" +
                "}\n", ListExpression.class);
    }
    
    // tests that a non-static field initializer is not erased during a reconcile
    public void testFieldInitializerIsNotMoved2() throws Exception {
        findFieldInitializer("package p1\n"+
                "public class Hello {\n"+
                "  def aStatic = {}\n" +
                "}\n", ClosureExpression.class);
    }
    
    private Expression findFieldInitializer(String contents, Class<? extends Expression> expressionClass) throws JavaModelException {
        IProject project = createSimpleGroovyProject().getProject();
        env.addGroovyClass(project.getFullPath().append("src"), "p1", "Hello2", contents);
        IFile javaFile = getFile("Project/src/p1/Hello2.groovy");
        GroovyCompilationUnit unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(javaFile);
        ClassNode inClass = unit.getModuleNode().getClasses().get(0);
        FieldNode field = inClass.getField("aStatic");
        Expression initialExpression = field.getInitialExpression();
        assertNotNull(initialExpression);
        assertTrue(expressionClass.isInstance(initialExpression));
        
        checkClinitAndAllConstructors(initialExpression, inClass);
        return initialExpression;
    }
    
    
    private void checkClinitAndAllConstructors(Expression expr, ClassNode inClass) {
        for (ConstructorNode cons : inClass.getDeclaredConstructors()) {
            assertUnique(expr, cons);
        }
        assertUnique(expr, inClass.getMethod("<clinit>", new Parameter[0]));
    }
    
    // asserts that the given expression has not been copied into the constructor
    // and so makes sure that this expression appears only once in the AST
    private void assertUnique(Expression expr, MethodNode cons) {
        UniquenessVisitor visitor = new UniquenessVisitor(expr);
        visitor.visitMethod(cons);
    }
    // Only checks for ClosureExpressions and ListExpressions, but we can easily add more
    class UniquenessVisitor extends ClassCodeVisitorSupport {
        public UniquenessVisitor(Expression exprToCheck) {
            this.exprToCheck = exprToCheck;
        }

        private final Expression exprToCheck;
        
        

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
        
        @Override
        public void visitListExpression(ListExpression expression) {
            doCheck(expression);
            super.visitListExpression(expression);
        }
        
        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            doCheck(expression);
            super.visitClosureExpression(expression);
        }
        
        void doCheck(Expression expr) {
            assertFalse ("Expression appears twice.  Once in constructor and once in field initializer.\nExpr: " + expr, expr == exprToCheck);
        }
    }
}