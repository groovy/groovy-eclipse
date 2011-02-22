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
package org.codehaus.groovy.eclipse.dsl.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTFieldNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.groovy.tests.search.AbstractGroovySearchTest;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;


/**
 * 
 * @author Andrew Eisenberg
 * @created Feb 11, 2011
 */
public class PointcutEvaluationTests extends AbstractGroovySearchTest {
    public static Test suite() {
        return new TestSuite(PointcutEvaluationTests.class);
    }

    class PointcutEvaluationRequestor implements ITypeRequestor {

        private final IPointcut toMatch;
        private final GroovyDSLDContext context;
        Stack<BindingSet> matches = new Stack<BindingSet>();
        BindingSet largestMatch = null;
        
        public PointcutEvaluationRequestor(IPointcut toMatch, GroovyCompilationUnit unit) throws CoreException {
            super();
            this.toMatch = toMatch;
            this.context = createContext(unit);
        }

        private GroovyDSLDContext createContext(GroovyCompilationUnit unit) throws CoreException {
            return new GroovyDSLDContext(unit);
        }

        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
                IJavaElement enclosingElement) {
            context.setCurrentScope(result.scope);
            context.setTargetType(result.type);
            BindingSet set = toMatch.matches(context);
            if (set != null) {
                matches.push(set);
                if (largestMatch == null || largestMatch.size() <= set.size()) {
                    largestMatch = set;
                }
            }
            return VisitStatus.CONTINUE;
        }
        
        BindingSet getLargestMatch() {
            return largestMatch;
        }

        boolean hasMatches() {
            return ! matches.isEmpty();
        }
        
    }

    class BindingResult {
        public BindingResult(String bindingName, Class<?> bindingType,
                String bindingToString) {
            super();
            this.bindingType = bindingType;
            this.bindingName = bindingName;
            this.bindingToString = bindingToString;
        }
        final Class<?> bindingType;
        final String bindingName;
        final String bindingToString;
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BindingResult [bindingType=");
            builder.append(bindingType);
            builder.append(", bindingName=");
            builder.append(bindingName);
            builder.append(", bindingToString=");
            builder.append(bindingToString);
            builder.append("]");
            return builder.toString();
        }
    }

    public PointcutEvaluationTests(String name) {
        super(name);
    }
    
    public void testEvaluateTypeMethodField1() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(\"java.lang.Integer\")", ClassNode.class, "java.lang.Integer");
    }

    public void testEvaluateTypeMethodField2() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findMethod(\"intValue\"))", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField3() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findField(\"value\"))", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField4Fail() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findField(\"notHere\"))", null, null);
    }
    
    public void testEvaluateTypeMethodField5() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findField(\"value\") & findMethod(\"intValue\"))", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField5b() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", 
                
                "def left = findField(\"value\")\n" +
                "def right = findMethod(\"intValue\")\n" +
                "currentType(left & right)", ClassNode.class, "java.lang.Integer");
    }

    public void testEvaluateTypeMethodField5c() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", 
                
                "def left = { findField(\"value\") }\n" +
                "def right = { findMethod(\"intValue\") }\n" +
                "currentType(left() & right())", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField6Fail_a() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findField(\"notHere\") & findMethod(\"intValue\"))", null, null);
    }

    public void testEvaluateTypeMethodField6Fail_b() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findMethod(\"intValue\") & findField(\"notHere\"))", null, null);
    }
    
    public void testEvaluateTypeMethodField6Fail_c() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", 
                "def left = findField(\"notHere\")\n" +
                "def right = findMethod(\"intValue\")\n" +
                "currentType(left & right)", null, null);
    }
    
    public void testEvaluateTypeMethodField7a() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findField(\"notHere\") | findMethod(\"intValue\"))", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField7b() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(findMethod(\"intValue\") | findField(\"notHere\"))", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField8() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(\"java.lang.Number\") | (currentType(findMethod(\"intValue\") & findField(\"notHere\")))", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField8b() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "(currentType(findMethod(\"intValue\") & findField(\"notHere\"))) | currentType(\"java.lang.Number\") ", ClassNode.class, "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField9Fail_a() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(\"java.lang.Number.NOPE\") | (currentType(findMethod(\"intValue\") & findField(\"notHere\")))", null, null);
    }
    
    public void testEvaluateTypeMethodField9Fail_b() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "currentType(\"java.lang.Number\") & (currentType(findMethod(\"intValue\") & findField(\"notHere\")))", null, null);
    }
    
    public void testAnnotation1() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo {}");
        doTestOfLastDefaultBinding("Foo", "currentType(annotatedBy(\"java.lang.Deprecated\"))", ClassNode.class, "p.Foo");
    }

    public void testAnnotation2() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t }");
        doTestOfLastDefaultBinding("Foo", "currentType(findField(annotatedBy(\"java.lang.Deprecated\")))", ClassNode.class, "p.Foo");
    }
    
    public void testAnnotation3() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t() { } }");
        doTestOfLastDefaultBinding("Foo", "currentType(findMethod(annotatedBy(\"java.lang.Deprecated\")))", ClassNode.class, "p.Foo");
    }
    
    public void testAnnotation4() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastDefaultBinding("Foo", "currentType(annotatedBy(\"java.lang.Deprecated\") & findField(\"f\") )", ClassNode.class, "p.Foo");
    }

    public void testAnnotation5() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastDefaultBinding("Foo", "currentType(annotatedBy(\"java.lang.Deprecated\") | findField(\"g\") )", ClassNode.class, "p.Foo");
    }
    
    public void testAnnotation6() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastDefaultBinding("Foo", "currentType( findField(\"g\") | annotatedBy(\"java.lang.Deprecated\") )", ClassNode.class, "p.Foo");
    }
    
    public void testAnnotation7Fail() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastDefaultBinding("Foo", "currentType( findField(\"g\") & annotatedBy(\"java.lang.Deprecated\") )", null, null);
    }
    
    public void testAnnotation8() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo { \n @Deprecated def f\n @Deprecated def g() { } }");
        doTestOfLastDefaultBinding("Foo", "currentType( findField( annotatedBy(\"java.lang.Deprecated\") ) & findMethod( annotatedBy(\"java.lang.Deprecated\") ) )", 
                ClassNode.class, "p.Foo");
    }
    
    
    public void testEvaluateFileExtension1() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "fileExtension(\"groovy\")", String.class, "src/p/Unit.groovy");
    }
    
    public void testEvaluateFileExtension2Fail() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "fileExtension(\"invalid\")", null, null);
    }
    
    public void testEvaluateNature1() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "nature(\"org.eclipse.jdt.groovy.core.groovyNature\")", String.class, "org.eclipse.jdt.groovy.core.groovyNature");
    }
    
    public void testEvaluateNature2Fail() throws Exception {
        doTestOfLastDefaultBinding("package p\n2", "nature(\"invalid\")", null, null);
    }
    
    public void testNamedBinding1() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") )", 
                new BindingResult(null, String.class, "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("b", String.class, "org.eclipse.jdt.groovy.core.groovyNature"));
    }
    
    public void testNamedBinding2() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( c : bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) )", 
                new BindingResult(null, String.class, "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("b", String.class, "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("c", String.class, "org.eclipse.jdt.groovy.core.groovyNature"));
    }
    
    public void testNamedBinding3() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) | " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult(null, ArrayList.class, "org.eclipse.jdt.groovy.core.groovyNature, src/p/Unit.groovy"),
                new BindingResult("b", String.class, "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("c", String.class, "src/p/Unit.groovy"));
    }
    
    public void testNamedBinding4() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) & " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult(null, ArrayList.class, "org.eclipse.jdt.groovy.core.groovyNature, src/p/Unit.groovy"),
                new BindingResult("b", String.class, "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("c", String.class, "src/p/Unit.groovy"));
    }
    
    public void testNamedBinding5() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) | " +
                "bind( c : fileExtension(\"invalid\") )", 
                new BindingResult(null, String.class, "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("b", String.class, "org.eclipse.jdt.groovy.core.groovyNature"));
    }
    
    public void testNamedBinding6Fail() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"invalid\") ) & " +
                "bind( c : fileExtension(\"groovy\") )");
    }

    
    public void testTypesNamedBinding1() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : currentType(\"java.lang.Integer\") )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", ClassNode.class, "java.lang.Integer"));
    }

    public void testTypesNamedBinding2() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : currentType(\"java.lang.Integer\") ) | " +
                "bind( c : fileExtension(\"invalid\") )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", ClassNode.class, "java.lang.Integer"));
    }
    
    public void testTypesNamedBinding3() throws Exception {
        doTestOfLastBindingSet("package p\n2", 
                "bind( b : currentType(\"java.lang.Integer\") ) | " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult(null, ArrayList.class, "java.lang.Integer, src/p/Unit.groovy"),
                new BindingResult("b", ClassNode.class, "java.lang.Integer"),
                new BindingResult("c", String.class, "src/p/Unit.groovy"));
    }
    
    public void testTypesNamedBinding4() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"value\") ) )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", FieldNode.class, "java.lang.Integer.value"));
    }

    public void testTypesNamedBinding4Fail() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"invalid\") ) )");
    }
    
    public void testTypesNamedBinding5() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"value\") ) ) | currentType(bind( b : findMethod(\"intValue\") ) )", 
                new BindingResult(null, ArrayList.class, "java.lang.Integer, java.lang.Integer"),
                new BindingResult("b", MethodNode.class, "java.lang.Integer.intValue"));
    }

    public void testTypesNamedBinding6() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"value\") ) | bind( b : findMethod(\"intValue\") ) )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", MethodNode.class, "java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding7() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"value\") | findMethod(\"intValue\") ) )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", ArrayList.class, "java.lang.Integer.value, java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding8() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"value\") & findMethod(\"intValue\") ) )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", ArrayList.class, "java.lang.Integer.value, java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding9() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"invalid\") | findMethod(\"intValue\") ) )", 
                new BindingResult(null, ClassNode.class, "java.lang.Integer"),
                new BindingResult("b", MethodNode.class, "java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding10Fail() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : findField(\"invalid\") & findMethod(\"intValue\") ) )");
    }
    
    
    public void testAnnotationBinding1() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo {}");
        doTestOfLastBindingSet("Foo", "currentType(bind( b : annotatedBy(\"java.lang.Deprecated\")))",
                new BindingResult(null, ClassNode.class, "p.Foo"),
                new BindingResult("b", ClassNode.class, "p.Foo"));
    }

    public void testAnnotationBinding2() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t }");
        doTestOfLastBindingSet("Foo", "currentType(bind(b : findField(annotatedBy(\"java.lang.Deprecated\"))))",
                new BindingResult(null, ClassNode.class, "p.Foo"),
                new BindingResult("b", JDTFieldNode.class, "p.Foo.t"));
    }
    
    public void testAnnotationBinding3() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t() { } }");
        doTestOfLastBindingSet("Foo", "currentType(bind( b : findMethod(annotatedBy(\"java.lang.Deprecated\"))))",
                new BindingResult(null, ClassNode.class, "p.Foo"),
                new BindingResult("b", JDTMethodNode.class, "p.Foo.t"));
    }
    
    public void testAnnotationBinding4() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType(bind ( b : annotatedBy(\"java.lang.Deprecated\") & findField(\"f\") ) )",
                new BindingResult(null, ClassNode.class, "p.Foo"),
                new BindingResult("b", ArrayList.class, "p.Foo, p.Foo.f"));
    }

    public void testAnnotationBinding5() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType(bind ( b : annotatedBy(\"java.lang.Deprecated\") | findField(\"f\") ) )",
                new BindingResult(null, ClassNode.class, "p.Foo"),
                new BindingResult("b", ArrayList.class, "p.Foo, p.Foo.f"));
    }
    
    public void testAnnotationBinding6() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType( bind( b : findField(\"g\")) | annotatedBy(\"java.lang.Deprecated\") )", 
                new BindingResult(null, ClassNode.class, "p.Foo"));
    }
    
    public void testAnnotationBinding7Fail() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType( findField(\"g\") & bind( b : annotatedBy(\"java.lang.Deprecated\") ) )");
    }
    
    public void testAnnotationBinding8() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo { \n @Deprecated def f\n @Deprecated def g() { } }");
        doTestOfLastBindingSet("Foo", "currentType( bind( b : findField( annotatedBy(\"java.lang.Deprecated\") ) & findMethod( annotatedBy(\"java.lang.Deprecated\") ) ) )", 
                new BindingResult(null, ClassNode.class, "p.Foo"),
                new BindingResult("b", ArrayList.class, "p.Foo.f, p.Foo.g"));
    }
    

    
    private void doTestOfLastBindingSet(String cuContents, String pointcutText, BindingResult... results) throws Exception {
        doTestOfLastBindingSet("p", cuContents, pointcutText, results);
    }
    private void doTestOfLastBindingSet(String pkg, String cuContents, String pointcutText, BindingResult... results) throws Exception {
        GroovyCompilationUnit unit = createUnit(pkg, "Unit", cuContents);
        BindingSet bindings = evaluateForBindings(unit, pointcutText);
        assertAllBindings(bindings, results);
    }
    

    private void doTestOfLastDefaultBinding(String cuContents, String pointcutText, Class<?> type, String name) throws Exception {
        doTestOfLastDefaultBinding("p", cuContents, pointcutText, type, name);
    }
    private void doTestOfLastDefaultBinding(String pkg, String cuContents, String pointcutText, Class<?> type, String name) throws Exception {
        GroovyCompilationUnit unit = createUnit(pkg, "Unit", cuContents);
        Object defaultBinding = evaluateForDefault(unit, pointcutText);
        assertSingleBinding(type, name, defaultBinding);
    }
    
    private void assertAllBindings(BindingSet bindings, BindingResult... results) {
        if (results.length == 0) {
            assertNull("Should not have found any bindings", bindings);
            return;
        }
        
        assertNotNull("Should have found some bindings.  Expected:\n" + Arrays.toString(results), bindings);
        
        for (BindingResult result : results) {
            Object o = bindings.getBinding(result.bindingName);
            if (o == null) {
                fail("Expected binding '" + result.bindingName + "' of type " + result.bindingType + ", but not found.\n" +
                		"Actual bindings:\n" + bindings.getBindings());
            }
            assertSingleBinding(result.bindingType, result.bindingToString, o);
        }
        assertEquals("Wrong number of bindings.  Expected Bindings: \n" + Arrays.toString(results) + 
                "\nActualBindings:\n" + bindings.getBindings(), results.length, bindings.getBindings().size());
    }
    
    private void assertSingleBinding(Class<?> type, String bindingToString, Object binding) {
        if (type == null) {
            assertNull("Default binding should have been null", binding);
        } else {
            assertNotNull("Binding should not be null.  Should be: " + type + " : name", binding);
            assertEquals("Wrong class for binding", type, binding.getClass());
            assertEquals("Wrong toString for binding", bindingToString, extractName(binding));
        }
    }

    private String extractName(Object defaultBinding) {
        if (defaultBinding == null) {
            return null;
        } else if (defaultBinding instanceof ClassNode) {
            return ((ClassNode) defaultBinding).getName();
        } else if (defaultBinding instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) defaultBinding;
            return fieldNode.getDeclaringClass().getName() + "." + fieldNode.getName();
        } else if (defaultBinding instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) defaultBinding;
            return methodNode.getDeclaringClass().getName() + "." + methodNode.getName();
        } else if (defaultBinding instanceof AnnotationNode) {
            AnnotationNode annotationNode = (AnnotationNode) defaultBinding;
            return "@" + annotationNode.getClassNode().getName();
        } else if (defaultBinding instanceof Collection<?>) {
            StringBuilder sb = new StringBuilder();
            for (Object item : ((Collection<?>) defaultBinding)) {
                sb.append(extractName(item));
                sb.append(", ");
            }
            sb.replace(sb.length()-2, sb.length(), "");
            return sb.toString();
        } else {
            return defaultBinding.toString();
        }
    }

    private Object evaluateForDefault(GroovyCompilationUnit unit, String pointcutText) throws CoreException {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText);
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.visitCompilationUnit(requestor);
        return requestor.hasMatches() ? requestor.getLargestMatch().getDefaultBinding() : null;
    }
    
    private BindingSet evaluateForBindings(GroovyCompilationUnit unit, String pointcutText) throws CoreException {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText);
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.visitCompilationUnit(requestor);
        return requestor.hasMatches() ? requestor.getLargestMatch() : null;
    }
    
}