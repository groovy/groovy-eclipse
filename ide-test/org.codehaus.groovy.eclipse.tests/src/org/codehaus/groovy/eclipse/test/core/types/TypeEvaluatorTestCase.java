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
package org.codehaus.groovy.eclipse.test.core.types;

import junit.framework.*;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.*;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;

/**
 * @author Heiko Boettger
 */
public class TypeEvaluatorTestCase extends EclipseTestCase {

    public void test_EvalutateStringExpression() {
        ITypeEvaluationContext context = new ITypeEvaluationContext() {
            public ClassLoader getClassLoader() {
                return null;
            }

            public String[] getImports() {
                return new String[0];
            }

            public Field lookupField(String type, String name,
                    boolean accessible, boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public Method lookupMethod(String type, String name,
                    String[] paramTypes, boolean accessible,
                    boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public Property lookupProperty(String type, String name,
                    boolean accessible, boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public GroovyDeclaration lookupSymbol(String name) {
                throw new UnsupportedOperationException();
            }

            public GroovyProjectFacade getProject() {
                return testProject.getGroovyProjectFacade();
            }

        };
        TypeEvaluator typeEvaluator = new TypeEvaluator(context);
        EvalResult result = typeEvaluator.evaluate("\"string\"");
        Assert.assertEquals("java.lang.String", result.getName());
    }

    public void test_EvalutateGStringExpression() {
        ITypeEvaluationContext context = new ITypeEvaluationContext() {
            public ClassLoader getClassLoader() {
                return null;
            }

            public String[] getImports() {
                return new String[0];
            }

            public Field lookupField(String type, String name,
                    boolean accessible, boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public Method lookupMethod(String type, String name,
                    String[] paramTypes, boolean accessible,
                    boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public Property lookupProperty(String type, String name,
                    boolean accessible, boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public GroovyDeclaration lookupSymbol(String name) {
                return new LocalVariable("AnyType", name);
            }
            public GroovyProjectFacade getProject() {
                return testProject.getGroovyProjectFacade();
            }

        };
        TypeEvaluator typeEvaluator = new TypeEvaluator(context);
        EvalResult result = typeEvaluator.evaluate("\"string${any}\"");
        Assert.assertEquals("groovy.lang.GString", result.getName());
    }

    public void test_EvalutateFieldExpression() {
        ITypeEvaluationContext context = new ITypeEvaluationContext() {
            public ClassLoader getClassLoader() {
                return null;
            }

            public String[] getImports() {
                return new String[0];
            }

            public Field lookupField(String type, String name,
                    boolean accessible, boolean staticAccess) {
                return new Field("int", Modifiers.ACC_PUBLIC, name,
                        new ClassType(type, Modifiers.ACC_PUBLIC, "String[]"));
            }

            public Method lookupMethod(String type, String name,
                    String[] paramTypes, boolean accessible,
                    boolean staticAccess) {
                throw new UnsupportedOperationException();
            }

            public Property lookupProperty(String type, String name,
                    boolean accessible, boolean staticAccess) {
                return null;
            }

            public GroovyDeclaration lookupSymbol(String name) {
                return new Field("java.lang.String[]", Modifiers.ACC_PUBLIC,
                        name, new ClassType("mypackage.MyClass",
                                Modifiers.ACC_PUBLIC, "MyClass"));
            }
            public GroovyProjectFacade getProject() {
                return testProject.getGroovyProjectFacade();
            }

        };
        TypeEvaluator typeEvaluator = new TypeEvaluator(context);
        EvalResult result = typeEvaluator.evaluate("array.length");
        Assert.assertEquals("int", result.getName());
    }
}
