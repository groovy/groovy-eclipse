/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
        Collection<?> largestMatchResult = null;
        
        public PointcutEvaluationRequestor(IPointcut toMatch, GroovyCompilationUnit unit) throws CoreException {
            super();
            this.toMatch = toMatch;
            this.context = createContext(unit);
        }

        private GroovyDSLDContext createContext(GroovyCompilationUnit unit) throws CoreException {
            GroovyDSLDContext context = new GroovyDSLDContext(unit);
            context.resetBinding();
            return context;
        }

        public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result,
                IJavaElement enclosingElement) {
            context.setCurrentScope(result.scope);
            context.setTargetType(result.type);
            Collection<?> matchResult = toMatch.matches(context, result.type);
            if (result != null) {
                BindingSet set = context.getCurrentBinding();
                matches.push(set);
                if (largestMatch == null || largestMatch.size() <= set.size()) {
                    largestMatch = set;
                    largestMatchResult = matchResult;
                }
            }
            return VisitStatus.CONTINUE;
        }
        
        Collection<?> getLargestMatchResult() {
            return largestMatchResult;
        }
        
        BindingSet getLargestMatch() {
            return largestMatch;
        }

        boolean hasMatches() {
            return ! matches.isEmpty();
        }
        
    }

    class BindingResult {
        public BindingResult(String bindingName, String bindingToString) {
            super();
            this.bindingName = bindingName;
            this.bindingToString = bindingToString;
        }
        final String bindingName;
        final String bindingToString;
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BindingResult [");
            builder.append("bindingName=");
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
        doTestOfLastMatch("package p\n2", "currentType(\"java.lang.Integer\")", "java.lang.Integer");
    }

    public void testEvaluateTypeMethodField2() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(methods(\"intValue\"))", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField3() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(fields(\"value\"))", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField4Fail() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(fields(\"notHere\"))", null);
    }
    
    public void testEvaluateTypeMethodField5() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(fields(\"value\") & methods(\"intValue\"))", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField5b() throws Exception {
        doTestOfLastMatch("package p\n2", 
                
                "def left = fields(\"value\")\n" +
                "def right = methods(\"intValue\")\n" +
                "currentType(left & right)", "java.lang.Integer");
    }

    public void testEvaluateTypeMethodField5c() throws Exception {
        doTestOfLastMatch("package p\n2", 
                
                "def left = { fields(\"value\") }\n" +
                "def right = { methods(\"intValue\") }\n" +
                "currentType(left() & right())", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField6Fail_a() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(fields(\"notHere\") & methods(\"intValue\"))", null);
    }

    public void testEvaluateTypeMethodField6Fail_b() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(methods(\"intValue\") & fields(\"notHere\"))", null);
    }
    
    public void testEvaluateTypeMethodField6Fail_c() throws Exception {
        doTestOfLastMatch("package p\n2", 
                "def left = fields(\"notHere\")\n" +
                "def right = methods(\"intValue\")\n" +
                "currentType(left & right)", null);
    }
    
    public void testEvaluateTypeMethodField7a() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(fields(\"notHere\") | methods(\"intValue\"))", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField7b() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(methods(\"intValue\") | fields(\"notHere\"))", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField8() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(subType(\"java.lang.Number\")) | (currentType(methods(\"intValue\") & fields(\"notHere\")))", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField8b() throws Exception {
        doTestOfLastMatch("package p\n2", "(currentType(methods(\"intValue\") & fields(\"notHere\"))) | currentType(subType(\"java.lang.Number\")) ", "java.lang.Integer");
    }
    
    public void testEvaluateTypeMethodField9Fail_a() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(\"java.lang.Number.NOPE\") | (currentType(methods(\"intValue\") & fields(\"notHere\")))", null);
    }
    
    public void testEvaluateTypeMethodField9Fail_b() throws Exception {
        doTestOfLastMatch("package p\n2", "currentType(subType(\"java.lang.Number\")) & (currentType(methods(\"intValue\") & fields(\"notHere\")))", null);
    }
    
    public void testAnnotation1() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo {}");
        doTestOfLastMatch("Foo", "currentType(annotatedBy(\"java.lang.Deprecated\"))", "p.Foo");
    }

    public void testAnnotation2() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t }");
        doTestOfLastMatch("Foo", "currentType(fields(annotatedBy(\"java.lang.Deprecated\")))", "p.Foo");
    }
    
    public void testAnnotation3() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t() { } }");
        doTestOfLastMatch("Foo", "currentType(methods(annotatedBy(\"java.lang.Deprecated\")))", "p.Foo");
    }
    
    public void testAnnotation4() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastMatch("Foo", "currentType(annotatedBy(\"java.lang.Deprecated\") & fields(\"f\") )", "p.Foo");
    }

    public void testAnnotation5() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastMatch("Foo", "currentType(annotatedBy(\"java.lang.Deprecated\") | fields(\"g\") )", "p.Foo");
    }
    
    public void testAnnotation6() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastMatch("Foo", "currentType( fields(\"g\") | annotatedBy(\"java.lang.Deprecated\") )", "p.Foo");
    }
    
    public void testAnnotation7Fail() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastMatch("Foo", "currentType( fields(\"g\") & annotatedBy(\"java.lang.Deprecated\") )", null);
    }
    
    public void testAnnotation8() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo { \n @Deprecated def f\n @Deprecated def g() { } }");
        doTestOfLastMatch("Foo", "currentType( fields( annotatedBy(\"java.lang.Deprecated\") ) & methods( annotatedBy(\"java.lang.Deprecated\") ) )", 
                "p.Foo");
    }
    
    
    public void testEvaluateFileExtension1() throws Exception {
        doTestOfLastMatch("package p\n2", "fileExtension(\"groovy\")", "src/p/Unit.groovy");
    }
    
    public void testEvaluateFileExtension2Fail() throws Exception {
        doTestOfLastMatch("package p\n2", "fileExtension(\"invalid\")", null);
    }
    
    public void testEvaluateNature1() throws Exception {
        doTestOfLastMatch("package p\n2", "nature(\"org.eclipse.jdt.groovy.core.groovyNature\")", "org.eclipse.jdt.groovy.core.groovyNature");
    }
    
    public void testEvaluateNature2Fail() throws Exception {
        doTestOfLastMatch("package p\n2", "nature(\"invalid\")", null);
    }
    
    public void testPackagePath() throws Exception {
        doTestOfLastMatch("p", "package p\n2", "packageFolder(\"p\")", "p");
    }
    
    public void testPackagePathFail() throws Exception {
        doTestOfLastMatch("p", "package p\n2", "packageFolder(\"invalid\")", null);
    }
    
    public void testNamedBinding1() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") )", 
                new BindingResult("b", "org.eclipse.jdt.groovy.core.groovyNature"));
    }
    
    public void testNamedBinding2() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( c : bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) )", 
                new BindingResult("b", "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("c", "org.eclipse.jdt.groovy.core.groovyNature"));
    }
    
    public void testNamedBinding3() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) | " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult("b", "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("c", "src/p/Unit.groovy"));
    }
    
    public void testNamedBinding4() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) & " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult("b", "org.eclipse.jdt.groovy.core.groovyNature"),
                new BindingResult("c", "src/p/Unit.groovy"));
    }
    
    public void testNamedBinding5() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"org.eclipse.jdt.groovy.core.groovyNature\") ) | " +
                "bind( c : fileExtension(\"invalid\") )", 
                new BindingResult("b", "org.eclipse.jdt.groovy.core.groovyNature"));
    }
    
    public void testNamedBinding6() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"invalid\") ) & " +
                "bind( c : fileExtension(\"groovy\") )");
    }

    public void testNamedBinding6a() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : nature(\"invalid\") ) | " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult("c", "src/p/Unit.groovy"));
    }

    
    public void testTypesNamedBinding1() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : currentType(\"java.lang.Integer\") )", 
                new BindingResult("b", "java.lang.Integer"));
    }

    public void testTypesNamedBinding2() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : currentType(\"java.lang.Integer\") ) | " +
                "bind( c : fileExtension(\"invalid\") )", 
                new BindingResult("b", "java.lang.Integer"));
    }
    
    public void testTypesNamedBinding3() throws Exception {
        doTestOfLastBindingSet("package p\n2", 
                "bind( b : currentType(\"java.lang.Integer\") ) | " +
                "bind( c : fileExtension(\"groovy\") )", 
                new BindingResult("b", "java.lang.Integer"),
                new BindingResult("c", "src/p/Unit.groovy"));
    }
    
    public void testTypesNamedBinding4() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"value\") ) )", 
                new BindingResult("b", "java.lang.Integer.value"));
    }

    public void testTypesNamedBinding4Fail() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"invalid\") ) )");
    }
    
    public void testTypesNamedBinding5() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"value\") ) ) | currentType(bind( b : methods(\"intValue\") ) )", 
                new BindingResult("b", "java.lang.Integer.value, java.lang.Integer.intValue"));
    }

    public void testTypesNamedBinding6() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"value\") ) | bind( b : methods(\"intValue\") ) )", 
                new BindingResult("b", "java.lang.Integer.value, java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding7() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"value\") | methods(\"intValue\") ) )", 
                new BindingResult("b", "java.lang.Integer.value, java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding8() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"value\") & methods(\"intValue\") ) )", 
                new BindingResult("b", "java.lang.Integer.value, java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding9() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"invalid\") | methods(\"intValue\") ) )", 
                new BindingResult("b", "java.lang.Integer.intValue"));
    }
    
    public void testTypesNamedBinding10Fail() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType(bind( b : fields(\"invalid\") & methods(\"intValue\") ) )");
    }
    

    public void testTypesNamedBinding11() throws Exception {
        doTestOfLastBindingSet("package p\n2", "currentType( bind( b : subType( Number) ) )", 
                new BindingResult("b", "java.lang.Number"));
    }

    public void testTypesNamedBinding12() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : currentType( subType( Number) ) )", 
                new BindingResult("b", "java.lang.Integer"));
    }
    
    public void testTypesNamedBinding13() throws Exception {
        createUnit("p", "Bar", "package p\n@Deprecated\nclass Foo {}\nclass Bar extends Foo { }");
        doTestOfLastBindingSet("package p\nBar", "bind( b : currentType( subType( annotatedBy(Deprecated)) ) )", 
                new BindingResult("b", "p.Bar"));
    }
    
    public void testTypesNamedBinding14() throws Exception {
        createUnit("p", "Bar", "package p\n@Deprecated\nclass Foo { }\nclass Bar extends Foo { }");
        doTestOfLastBindingSet("package p\nBar", "currentType( bind( b : subType( annotatedBy(Deprecated)) ) )", 
                new BindingResult("b", "p.Foo"));
    }
    
    public void testTypesNamedBinding15() throws Exception {
        createUnit("p", "Bar", "package p\n@Deprecated\nclass Foo { }\nclass Bar extends Foo { }");
        doTestOfLastBindingSet("package p\nBar", "currentType( subType( bind( b : annotatedBy(Deprecated)) ) )", 
                new BindingResult("b", "@java.lang.Deprecated"));
    }
    
    public void testTypesNamedBinding16() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { }\nclass Bar extends Foo { }");
        doTestOfLastBindingSet("package p\nFoo", "bind( b : currentType( subType( annotatedBy(Deprecated)) ) )", 
                new BindingResult("b", "p.Foo"));
    }
    
    public void testTypesNamedBinding17() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { }\nclass Bar extends Foo { }");
        doTestOfLastBindingSet("package p\nFoo", "bind( b : subType( annotatedBy(Deprecated)) ) ", 
                new BindingResult("b", "p.Foo"));
    }
    
    public void testTypesNamedBinding18Fail() throws Exception {
        doTestOfLastBindingSet("package p\n2", "bind( b : currentType( subType( annotatedBy(Deprecated)) ) )");
    }
    
    public void testAnd1() throws Exception {
        doTestOfLastMatch("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) & bind( d : methods(\"intValue\")))))", "java.lang.Integer");
        doTestOfLastBindingSet("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) & bind( d : methods(\"intValue\")))))",
                new BindingResult("a", "java.lang.Integer"),
                new BindingResult("b", "java.lang.Integer.value, java.lang.Integer.intValue"),
                new BindingResult("c", "java.lang.Integer.value"),
                new BindingResult("d", "java.lang.Integer.intValue"));
    }
    public void testAnd2() throws Exception {
        doTestOfLastMatch("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) & bind( d : methods(\"invalid\")))))", null);
        doTestOfLastBindingSet("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) & bind( d : methods(\"invalid\")))))",
                new BindingResult("c", "java.lang.Integer.value"));
    }
    
    public void testOr1() throws Exception {
        doTestOfLastMatch("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) | bind( d : methods(\"invalid\")))))", "java.lang.Integer");
        doTestOfLastBindingSet("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) | bind( d : methods(\"intValue\")))))",
                new BindingResult("a", "java.lang.Integer"),
                new BindingResult("b", "java.lang.Integer.value, java.lang.Integer.intValue"),
                new BindingResult("c", "java.lang.Integer.value"),
                new BindingResult("d", "java.lang.Integer.intValue"));
    }
    public void testOr2() throws Exception {
        doTestOfLastMatch("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) | bind( d : methods(\"invalid\")))))", "java.lang.Integer");
        doTestOfLastBindingSet("package p\n2", "bind( a : currentType( bind( b : bind( c : fields (\"value\") ) | bind( d : methods(\"invalid\")))))",
                new BindingResult("a", "java.lang.Integer"),
                new BindingResult("b", "java.lang.Integer.value"),
                new BindingResult("c", "java.lang.Integer.value"));
    }
    

    
    public void testAnnotationBinding1() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo {}");
        doTestOfLastBindingSet("Foo", "currentType(bind( b : annotatedBy(\"java.lang.Deprecated\")))",
                new BindingResult("b", "@java.lang.Deprecated"));
    }

    public void testAnnotationBinding2() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t }");
        doTestOfLastBindingSet("Foo", "currentType(bind(b : fields(annotatedBy(\"java.lang.Deprecated\"))))",
                new BindingResult("b", "p.Foo.t"));
    }
    
    public void testAnnotationBinding3() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo {\n@Deprecated def t() { } }");
        doTestOfLastBindingSet("Foo", "currentType(bind( b : methods(annotatedBy(\"java.lang.Deprecated\"))))",
                new BindingResult("b", "p.Foo.t"));
    }
    
    public void testAnnotationBinding4() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType(bind ( b : annotatedBy(\"java.lang.Deprecated\") & fields(\"f\") ) )",
                new BindingResult("b", "@java.lang.Deprecated, p.Foo.f"));
    }

    public void testAnnotationBinding5() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType(bind ( b : annotatedBy(\"java.lang.Deprecated\") | fields(\"f\") ) )",
                new BindingResult("b", "@java.lang.Deprecated, p.Foo.f"));
    }
    
    public void testAnnotationBinding6() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType( bind( b : fields(\"g\")) | annotatedBy(\"java.lang.Deprecated\") )");
    }
    
    public void testAnnotationBinding7Fail() throws Exception {
        createUnit("p", "Foo", "package p\n@Deprecated\nclass Foo { \n def f }");
        doTestOfLastBindingSet("Foo", "currentType( fields(\"g\") & bind( b : annotatedBy(\"java.lang.Deprecated\") ) )");
    }
    
    public void testAnnotationBinding8() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo { \n @Deprecated def f\n @Deprecated def g() { } }");
        doTestOfLastBindingSet("Foo", "currentType( bind( b : fields( annotatedBy(\"java.lang.Deprecated\") ) & methods( annotatedBy(\"java.lang.Deprecated\") ) ) )", 
                new BindingResult("b", "p.Foo.f, p.Foo.g"));
    }
    
    public void testAnnotationBinding9() throws Exception {
        createUnit("p", "Foo", "package p\nclass Foo { \n @Deprecated def f\n @Deprecated def g() { } }");
        doTestOfLastBindingSet("Foo", "currentType( fields( bind ( b : annotatedBy(\"java.lang.Deprecated\") ) ) & methods( bind ( b : annotatedBy(\"java.lang.Deprecated\") ) ) )", 
                new BindingResult("b", "@java.lang.Deprecated, @java.lang.Deprecated"));
    }
    

    
    private void doTestOfLastBindingSet(String cuContents, String pointcutText, BindingResult... results) throws Exception {
        doTestOfLastBindingSet("p", cuContents, pointcutText, results);
    }
    private void doTestOfLastBindingSet(String pkg, String cuContents, String pointcutText, BindingResult... results) throws Exception {
        GroovyCompilationUnit unit = createUnit(pkg, "Unit", cuContents);
        BindingSet bindings = evaluateForBindings(unit, pointcutText);
        assertAllBindings(bindings, results);
    }
    

    private void doTestOfLastMatch(String cuContents, String pointcutText, String name) throws Exception {
        doTestOfLastMatch("p", cuContents, pointcutText, name);
    }
    private void doTestOfLastMatch(String pkg, String cuContents, String pointcutText, String name) throws Exception {
        GroovyCompilationUnit unit = createUnit(pkg, "Unit", cuContents);
        Collection<?> match = evaluateForMatch(unit, pointcutText);
        assertSingleBinding(name, match);
    }
    
    private void assertAllBindings(BindingSet bindings, BindingResult... results) {
        if (results.length == 0) {
            assertEquals("Should not have found any bindings", 0, bindings.getBindings().size());
            return;
        }
        
        assertNotNull("Should have found some bindings.  Expected:\n" + Arrays.toString(results), bindings);
        
        for (BindingResult result : results) {
            Collection<?> o = bindings.getBinding(result.bindingName);
            if (o == null) {
                fail("Expected binding '" + result.bindingName + "', but not found.\n" +
                		"Actual bindings:\n" + bindings.getBindings());
            }
            assertSingleBinding(result.bindingToString, o);
        }
        assertEquals("Wrong number of bindings.  Expected Bindings: \n" + Arrays.toString(results) + 
                "\nActualBindings:\n" + bindings.getBindings(), results.length, bindings.getBindings().size());
    }
    
    private void assertSingleBinding(String bindingToString, Collection<?> binding) {
        if (bindingToString == null) {
            assertNull("Match should have been null", binding);
            return;
        } 
        
        assertNotNull("Match should not be null", binding);
        
        String[] split = bindingToString.split(", ");
        assertEquals("Unexpected number of bindings for " + binding, split.length, binding.size());
        List<String> asList = Arrays.asList(split);
        for (Object object : binding) {
            String name = extractName(object);
            assertTrue("Expected binding " + name + " not found in " + asList, asList.contains(name));
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

    private Collection<?> evaluateForMatch(GroovyCompilationUnit unit, String pointcutText) throws CoreException {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText);
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.visitCompilationUnit(requestor);
        return requestor.hasMatches() ? requestor.getLargestMatchResult() : null;
    }
    
    private BindingSet evaluateForBindings(GroovyCompilationUnit unit, String pointcutText) throws CoreException {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText);
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit);
        TypeInferencingVisitorWithRequestor visitor = factory.createVisitor(unit);
        visitor.visitCompilationUnit(requestor);
        return requestor.hasMatches() ? requestor.getLargestMatch() : null;
    }
    
}