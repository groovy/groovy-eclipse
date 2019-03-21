/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests

import groovy.transform.ToString
import groovy.transform.TupleConstructor

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut
import org.codehaus.groovy.eclipse.dsl.tests.internal.PointcutScriptExecutor
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.groovy.search.ITypeRequestor
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorWithRequestor
import org.eclipse.jdt.groovy.search.TypeLookupResult
import org.junit.Assert
import org.junit.Test

final class PointcutEvaluationTests extends GroovyEclipseTestSuite {

    private static class PointcutEvaluationRequestor implements ITypeRequestor {
        private final IPointcut toMatch
        private final GroovyDSLDContext context
        private final Stack<BindingSet> matches = new Stack<BindingSet>()
        private BindingSet largestMatch = null
        private Collection<?> largestMatchResult = null

        PointcutEvaluationRequestor(IPointcut toMatch, GroovyCompilationUnit unit) {
            this.toMatch = toMatch
            this.context = createContext(unit)
        }

        private GroovyDSLDContext createContext(GroovyCompilationUnit unit) {
            ModuleNodeInfo info = unit.getModuleInfo(true)
            GroovyDSLDContext context = new GroovyDSLDContext(unit, info.module, info.resolver)
            context.resetBinding()
            return context
        }

        @Override
        VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
            if (result != null && result.scope != null) {
                context.setCurrentScope(result.scope)
                context.setTargetType(result.type)
                context.resetBinding()
                Collection<?> matchResult = toMatch.matches(context, result.type)
                BindingSet set = context.getCurrentBinding()
                matches.push(set)
                if (largestMatch == null || largestMatch.size() <= set.size()) {
                    largestMatch = set
                    largestMatchResult = matchResult
                }
            }
            return VisitStatus.CONTINUE
        }

        Collection getLargestMatchResult() {
            return largestMatchResult
        }

        BindingSet getLargestMatch() {
            return largestMatch
        }

        boolean hasMatches() {
            return !matches.isEmpty()
        }
    }

    @TupleConstructor @ToString(includeNames=true)
    private static class BindingResult {
        final String bindingName
        final String bindingToString
    }

    private void doTestOfLastBindingSet(String cuContents, String pointcutText, BindingResult... results) {
        doTestOfLastBindingSet('p', cuContents, pointcutText, results)
    }

    private void doTestOfLastBindingSet(String pkg, String cuContents, String pointcutText, BindingResult... results) {
        GroovyCompilationUnit unit = addGroovySource(cuContents, nextUnitName(), pkg)
        BindingSet bindings = evaluateForBindings(unit, pointcutText)
        assertAllBindings(bindings, results)
    }

    private void doTestOfLastMatch(String cuContents, String pointcutText, String name) {
        doTestOfLastMatch('p', cuContents, pointcutText, name)
    }

    private void doTestOfLastMatch(String pkg, String cuContents, String pointcutText, String name) {
        GroovyCompilationUnit unit = addGroovySource(cuContents, nextUnitName(), pkg)
        Collection<?> match = evaluateForMatch(unit, pointcutText)
        assertSingleBinding(name, match)
    }

    private void assertAllBindings(BindingSet bindings, BindingResult... results) {
        if (results.length == 0) {
            Assert.assertEquals('Should not have found any bindings', 0, bindings.getBindings().size())
            return
        }
        Assert.assertNotNull('Should have found some bindings.  Expected:\n' + Arrays.toString(results), bindings)

        for (BindingResult result : results) {
            Collection<?> o = bindings.getBinding(result.bindingName)
            if (o == null) {
                Assert.fail('Expected binding "' + result.bindingName + '", but not found.\n' + 'Actual bindings:\n' + bindings.getBindings())
            }
            assertSingleBinding(result.bindingToString, o)
        }
        Assert.assertEquals('Wrong number of bindings.  Expected Bindings: \n' + Arrays.toString(results) + '\nActualBindings:\n' + bindings.getBindings(), results.length, bindings.getBindings().size())
    }

    private void assertSingleBinding(String bindingToString, Collection<?> binding) {
        if (bindingToString == null) {
            Assert.assertNull('Match should have been null', binding)
            return
        }
        Assert.assertNotNull('Match should not be null', binding)

        String[] split = bindingToString.split(', ')
        Assert.assertEquals('Unexpected number of bindings for ' + BindingSet.printCollection(binding), split.length, binding.size())
        outer: for (Object object : binding) {
            String name = extractName(object)
            for (String token : split) {
                String regex = token.replace('(', '\\(').replace(')', '\\)')
                if (name.matches(regex)) {
                    continue outer
                }
            }
            Assert.fail('Expected binding ' + name + ' not found in ' + Arrays.toString(split))
        }
    }

    private String extractName(Object defaultBinding) {
        if (defaultBinding == null) {
            return null
        } else if (defaultBinding instanceof ClassNode) {
            return ((ClassNode) defaultBinding).getName()
        } else if (defaultBinding instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) defaultBinding
            return fieldNode.getDeclaringClass().getName() + '.' + fieldNode.getName()
        } else if (defaultBinding instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) defaultBinding
            return methodNode.getDeclaringClass().getName() + '.' + methodNode.getName()
        } else if (defaultBinding instanceof AnnotationNode) {
            AnnotationNode annotationNode = (AnnotationNode) defaultBinding
            return '@' + annotationNode.getClassNode().getName()
        } else if (defaultBinding instanceof MethodCallExpression) {
            return ((MethodCallExpression) defaultBinding).getMethodAsString() + '()'
        } else if (defaultBinding instanceof VariableExpression) {
            return 'Var: ' + ((VariableExpression) defaultBinding).getName()
        } else if (defaultBinding instanceof Collection) {
            StringBuilder sb = new StringBuilder()
            for (Object item : ((Collection<?>) defaultBinding)) {
                sb.append(extractName(item))
                sb.append(', ')
            }
            sb.replace(sb.length()-2, sb.length(), '')
            return sb.toString()
        } else {
            return defaultBinding.toString()
        }
    }

    private Collection<?> evaluateForMatch(GroovyCompilationUnit unit, String pointcutText) {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText)
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit)
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit)
        visitor.visitCompilationUnit(requestor)
        return requestor.hasMatches() ? requestor.getLargestMatchResult() : null
    }

    private BindingSet evaluateForBindings(GroovyCompilationUnit unit, String pointcutText) {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText)
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit)
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit)
        visitor.visitCompilationUnit(requestor)
        return requestor.hasMatches() ? requestor.getLargestMatch() : null
    }

    //--------------------------------------------------------------------------

    @Test
    void testEvaluateTypeMethodField1() {
        doTestOfLastMatch('2', 'currentType("java.lang.Integer")', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField2() {
        doTestOfLastMatch('2', 'currentType(methods("intValue"))', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField3() {
        doTestOfLastMatch('2', 'currentType(fields("value"))', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField4Fail() {
        doTestOfLastMatch('2', 'currentType(fields("notHere"))', null)
    }

    @Test
    void testEvaluateTypeMethodField5() {
        doTestOfLastMatch('2', 'currentType(fields("value") & methods("intValue"))', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField5b() {
        doTestOfLastMatch('2',
            'def left = fields("value")\n' +
            'def right = methods("intValue")\n' +
            'currentType(left & right)', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField5c() {
        doTestOfLastMatch('2',
            'def left = { fields("value") }\n' +
            'def right = { methods("intValue") }\n' +
            'currentType(left() & right())', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField6Fail_a() {
        doTestOfLastMatch('2', 'currentType(fields("notHere") & methods("intValue"))', null)
    }

    @Test
    void testEvaluateTypeMethodField6Fail_b() {
        doTestOfLastMatch('2', 'currentType(methods("intValue") & fields("notHere"))', null)
    }

    @Test
    void testEvaluateTypeMethodField6Fail_c() {
        doTestOfLastMatch('2',
            'def left = fields("notHere")\n' +
            'def right = methods("intValue")\n' +
            'currentType(left & right)', null)
    }

    @Test
    void testEvaluateTypeMethodField7a() {
        doTestOfLastMatch('2', 'currentType(fields("notHere") | methods("intValue"))', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField7b() {
        doTestOfLastMatch('2', 'currentType(methods("intValue") | fields("notHere"))', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField8() {
        doTestOfLastMatch('2', 'currentType(subType("java.lang.Number")) | (currentType(methods("intValue") & fields("notHere")))', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField8b() {
        doTestOfLastMatch('2', '(currentType(methods("intValue") & fields("notHere"))) | currentType(subType("java.lang.Number")) ', 'java.lang.Integer')
    }

    @Test
    void testEvaluateTypeMethodField9Fail_a() {
        doTestOfLastMatch('2', 'currentType("java.lang.Number.NOPE") | (currentType(methods("intValue") & fields("notHere")))', null)
    }

    @Test
    void testEvaluateTypeMethodField9Fail_b() {
        doTestOfLastMatch('2', 'currentType(subType("java.lang.Number")) & (currentType(methods("intValue") & fields("notHere")))', null)
    }

    @Test
    void testAnnotation1() {
        addGroovySource('@Deprecated\nclass Foo {}', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(annotatedBy("java.lang.Deprecated"))', 'p.Foo')
    }

    @Test
    void testAnnotation2() {
        addGroovySource('class Foo {\n@Deprecated def t }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(fields(annotatedBy("java.lang.Deprecated")))', 'p.Foo')
    }

    @Test
    void testAnnotation3() {
        addGroovySource('class Foo {\n@Deprecated def t() { } }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(methods(annotatedBy("java.lang.Deprecated")))', 'p.Foo')
    }

    @Test
    void testAnnotation4() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(annotatedBy("java.lang.Deprecated") & fields("f") )', 'p.Foo')
    }

    @Test
    void testAnnotation5() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(annotatedBy("java.lang.Deprecated") | fields("g") )', 'p.Foo')
    }

    @Test
    void testAnnotation6() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType( fields("g") | annotatedBy("java.lang.Deprecated") )', 'p.Foo')
    }

    @Test
    void testAnnotation7() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType( fields("g") & annotatedBy("java.lang.Deprecated") )', null)
    }

    @Test
    void testAnnotation8() {
        addGroovySource('class Foo { \n @Deprecated def f\n @Deprecated def g() { } }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType( fields( annotatedBy("java.lang.Deprecated") ) & methods( annotatedBy("java.lang.Deprecated") ) )', 'p.Foo')
    }

    @Test
    void testAnnotation9() {
        addGroovySource('@interface Tag { String value(); }', 'Tag', 'a')
        addGroovySource('@interface Tags { Tag[] value(); }', 'Tags', 'a')
        addGroovySource('import a.*; @Tags([@Tag("one"), @Tag("two")]) class Bar { def baz() {} }', 'Bar', 'foo')
        doTestOfLastMatch('foo.Bar', 'currentType(annotatedBy("a.Tag"))', 'foo.Bar')
    }

    @Test
    void testAnnotation9a() {
        addGroovySource('@interface Tag { String value(); }', 'Tag', 'a')
        addGroovySource('@interface Tags { Tag[] value(); }', 'Tags', 'a')
        addGroovySource('import a.*; @Tags(@Tag("one")) class Bar { def baz() {} }', 'Bar', 'foo')
        doTestOfLastMatch('foo.Bar', 'currentType(annotatedBy("a.Tag"))', 'foo.Bar')
    }

    @Test
    void testEvaluateFileExtension1() {
        doTestOfLastMatch('2', 'fileExtension("groovy")', 'src/p/TestUnit\\d+.groovy')
    }

    @Test
    void testEvaluateFileExtension2Fail() {
        doTestOfLastMatch('2', 'fileExtension("invalid")', null)
    }

    @Test
    void testEvaluateNature1() {
        doTestOfLastMatch('2', 'nature("org.eclipse.jdt.groovy.core.groovyNature")', 'org.eclipse.jdt.groovy.core.groovyNature')
    }

    @Test
    void testEvaluateNature2Fail() {
        doTestOfLastMatch('2', 'nature("invalid")', null)
    }

    @Test
    void testPackagePath() {
        doTestOfLastMatch('p', '2', 'packageFolder("p")', 'p')
    }

    @Test
    void testPackagePathFail() {
        doTestOfLastMatch('p', '2', 'packageFolder("invalid")', null)
    }

    @Test
    void testNamedBinding1() {
        doTestOfLastBindingSet('2', 'bind( b : nature("org.eclipse.jdt.groovy.core.groovyNature") )',
            new BindingResult('b', 'org.eclipse.jdt.groovy.core.groovyNature'))
    }

    @Test
    void testNamedBinding2() {
        doTestOfLastBindingSet('2', 'bind( c : bind( b : nature("org.eclipse.jdt.groovy.core.groovyNature") ) )',
            new BindingResult('b', 'org.eclipse.jdt.groovy.core.groovyNature'),
            new BindingResult('c', 'org.eclipse.jdt.groovy.core.groovyNature'))
    }

    @Test
    void testNamedBinding3() {
        doTestOfLastBindingSet('2', 'bind( b : nature("org.eclipse.jdt.groovy.core.groovyNature") ) | ' +
            'bind( c : fileExtension("groovy") )',
            new BindingResult('b', 'org.eclipse.jdt.groovy.core.groovyNature'),
            new BindingResult('c', 'src/p/TestUnit\\d+.groovy'))
    }

    @Test
    void testNamedBinding4() {
        doTestOfLastBindingSet('2', 'bind( b : nature("org.eclipse.jdt.groovy.core.groovyNature") ) & ' +
            'bind( c : fileExtension("groovy") )',
            new BindingResult('b', 'org.eclipse.jdt.groovy.core.groovyNature'),
            new BindingResult('c', 'src/p/TestUnit\\d+.groovy'))
    }

    @Test
    void testNamedBinding5() {
        doTestOfLastBindingSet('2', 'bind( b : nature("org.eclipse.jdt.groovy.core.groovyNature") ) | ' +
            'bind( c : fileExtension("invalid") )',
            new BindingResult('b', 'org.eclipse.jdt.groovy.core.groovyNature'))
    }

    @Test
    void testNamedBinding6() {
        doTestOfLastBindingSet('2', 'bind( b : nature("invalid") ) & ' +
            'bind( c : fileExtension("groovy") )')
    }

    @Test
    void testNamedBinding6a() {
        doTestOfLastBindingSet('2', 'bind( b : nature("invalid") ) | ' +
            'bind( c : fileExtension("groovy") )',
            new BindingResult('c', 'src/p/TestUnit\\d+.groovy'))
    }

    @Test
    void testTypesNamedBinding1() {
        doTestOfLastBindingSet('2', 'bind( b : currentType("java.lang.Integer") )',
            new BindingResult('b', 'java.lang.Integer'))
    }

    @Test
    void testTypesNamedBinding2() {
        doTestOfLastBindingSet('2', 'bind( b : currentType("java.lang.Integer") ) | ' +
            'bind( c : fileExtension("invalid") )',
            new BindingResult('b', 'java.lang.Integer'))
    }

    @Test
    void testTypesNamedBinding3() {
        doTestOfLastBindingSet('2',
            'bind( b : currentType("java.lang.Integer") ) | ' +
            'bind( c : fileExtension("groovy") )',
            new BindingResult('b', 'java.lang.Integer'),
            new BindingResult('c', 'src/p/TestUnit\\d+.groovy'))
    }

    @Test
    void testTypesNamedBinding4() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("value") ) )',
            new BindingResult('b', 'java.lang.Integer.value'))
    }

    @Test
    void testTypesNamedBinding4Fail() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("invalid") ) )')
    }

    @Test
    void testTypesNamedBinding5() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("value") ) ) | currentType(bind( b : methods("intValue") ) )',
            new BindingResult('b', 'java.lang.Integer.value, java.lang.Integer.intValue'))
    }

    @Test
    void testTypesNamedBinding6() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("value") ) | bind( b : methods("intValue") ) )',
            new BindingResult('b', 'java.lang.Integer.value, java.lang.Integer.intValue'))
    }

    @Test
    void testTypesNamedBinding7() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("value") | methods("intValue") ) )',
            new BindingResult('b', 'java.lang.Integer.value, java.lang.Integer.intValue'))
    }

    @Test
    void testTypesNamedBinding8() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("value") & methods("intValue") ) )',
            new BindingResult('b', 'java.lang.Integer.value, java.lang.Integer.intValue'))
    }

    @Test
    void testTypesNamedBinding9() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("invalid") | methods("intValue") ) )',
            new BindingResult('b', 'java.lang.Integer.intValue'))
    }

    @Test
    void testTypesNamedBinding10Fail() {
        doTestOfLastBindingSet('2', 'currentType(bind( b : fields("invalid") & methods("intValue") ) )')
    }

    @Test
    void testTypesNamedBinding11() {
        doTestOfLastBindingSet('2', 'currentType( bind( b : subType( Number) ) )',
            new BindingResult('b', 'java.lang.Number'))
    }

    @Test
    void testTypesNamedBinding12() {
        doTestOfLastBindingSet('2', 'bind( b : currentType( subType( Number) ) )',
            new BindingResult('b', 'java.lang.Integer'))
    }

    @Test
    void testTypesNamedBinding13() {
        addGroovySource('@Deprecated\nclass Foo {}\nclass Bar extends Foo { }', 'Bar', 'p')
        doTestOfLastBindingSet('Bar', 'bind( b : currentType( subType( annotatedBy(Deprecated)) ) )',
            new BindingResult('b', 'p.Bar'))
    }

    @Test
    void testTypesNamedBinding14() {
        addGroovySource('@Deprecated\nclass Foo { }\nclass Bar extends Foo { }', 'Bar', 'p')
        doTestOfLastBindingSet('Bar', 'currentType( bind( b : subType( annotatedBy(Deprecated)) ) )',
            new BindingResult('b', 'p.Foo'))
    }

    @Test
    void testTypesNamedBinding15() {
        addGroovySource('@Deprecated\nclass Foo { }\nclass Bar extends Foo { }', 'Bar', 'p')
        doTestOfLastBindingSet('Bar', 'currentType( subType( bind( b : annotatedBy(Deprecated)) ) )',
            new BindingResult('b', '@java.lang.Deprecated'))
    }

    @Test
    void testTypesNamedBinding16() {
        addGroovySource('@Deprecated\nclass Foo { }\nclass Bar extends Foo { }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'bind( b : currentType( subType( annotatedBy(Deprecated)) ) )',
            new BindingResult('b', 'p.Foo'))
    }

    @Test
    void testTypesNamedBinding17() {
        addGroovySource('@Deprecated\nclass Foo { }\nclass Bar extends Foo { }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'bind( b : subType( annotatedBy(Deprecated)) ) ',
            new BindingResult('b', 'p.Foo'))
    }

    @Test
    void testTypesNamedBinding18Fail() {
        doTestOfLastBindingSet('2', 'bind( b : currentType( subType( annotatedBy(Deprecated)) ) )')
    }

    @Test
    void testAnd1() {
        doTestOfLastMatch('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) & bind( d : methods("intValue")))))', 'java.lang.Integer')
        doTestOfLastBindingSet('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) & bind( d : methods("intValue")))))',
            new BindingResult('a', 'java.lang.Integer'),
            new BindingResult('b', 'java.lang.Integer.value, java.lang.Integer.intValue'),
            new BindingResult('c', 'java.lang.Integer.value'),
            new BindingResult('d', 'java.lang.Integer.intValue'))
    }

    @Test
    void testAnd2() {
        doTestOfLastMatch('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) & bind( d : methods("invalid")))))', null)
        doTestOfLastBindingSet('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) & bind( d : methods("invalid")))))',
            new BindingResult('c', 'java.lang.Integer.value'))
    }

    @Test
    void testOr1() {
        doTestOfLastMatch('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) | bind( d : methods("invalid")))))', 'java.lang.Integer')
        doTestOfLastBindingSet('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) | bind( d : methods("intValue")))))',
            new BindingResult('a', 'java.lang.Integer'),
            new BindingResult('b', 'java.lang.Integer.value, java.lang.Integer.intValue'),
            new BindingResult('c', 'java.lang.Integer.value'),
            new BindingResult('d', 'java.lang.Integer.intValue'))
    }

    @Test
    void testOr2() {
        doTestOfLastMatch('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) | bind( d : methods("invalid")))))', 'java.lang.Integer')
        doTestOfLastBindingSet('2', 'bind( a : currentType( bind( b : bind( c : fields ("value") ) | bind( d : methods("invalid")))))',
            new BindingResult('a', 'java.lang.Integer'),
            new BindingResult('b', 'java.lang.Integer.value'),
            new BindingResult('c', 'java.lang.Integer.value'))
    }

    @Test
    void testAnnotationBinding1() {
        addGroovySource('@Deprecated\nclass Foo {}', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind( b : annotatedBy("java.lang.Deprecated")))',
            new BindingResult('b', '@java.lang.Deprecated'))
    }

    @Test
    void testAnnotationBinding2() {
        addGroovySource('class Foo {\n@Deprecated def t }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind(b : fields(annotatedBy("java.lang.Deprecated"))))',
            new BindingResult('b', 'p.Foo.t'))
    }

    @Test
    void testAnnotationBinding3() {
        addGroovySource('class Foo {\n@Deprecated def t() { } }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind( b : methods(annotatedBy("java.lang.Deprecated"))))',
            new BindingResult('b', 'p.Foo.t'))
    }

    @Test
    void testAnnotationBinding4() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind ( b : annotatedBy("java.lang.Deprecated") & fields("f") ) )',
            new BindingResult('b', '@java.lang.Deprecated, p.Foo.f'))
    }

    @Test
    void testAnnotationBinding5() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind ( b : annotatedBy("java.lang.Deprecated") | fields("f") ) )',
            new BindingResult('b', '@java.lang.Deprecated, p.Foo.f'))
    }

    @Test
    void testAnnotationBinding6() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( bind( b : fields("g")) | annotatedBy("java.lang.Deprecated") )')
    }

    @Test
    void testAnnotationBinding7Fail() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( fields("g") & bind( b : annotatedBy("java.lang.Deprecated") ) )')
    }

    @Test
    void testAnnotationBinding8() {
        addGroovySource('class Foo { \n @Deprecated def f\n @Deprecated def g() { } }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( bind( b : fields( annotatedBy("java.lang.Deprecated") ) & methods( annotatedBy("java.lang.Deprecated") ) ) )',
            new BindingResult('b', 'p.Foo.f, p.Foo.g'))
    }

    @Test
    void testAnnotationBinding9() {
        addGroovySource('class Foo { \n @Deprecated def f\n @Deprecated def g() { } }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( fields( bind ( b : annotatedBy("java.lang.Deprecated") ) ) & methods( bind ( b : annotatedBy("java.lang.Deprecated") ) ) )',
            new BindingResult('b', '@java.lang.Deprecated, @java.lang.Deprecated'))
    }

    @Test
    void testNestedCalls1() {
        doTestOfLastBindingSet(
            'bar {\n' +
            '	foo {\n' +
            '		 XXX\n' +
            '	}\n' +
            '}', 'bind( x: enclosingCall()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'foo(), bar()'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCalls2() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '	bar {\n' +
            '		 XXX\n' +
            '	}\n' +
            '}', 'bind( x: enclosingCall()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'bar(), foo()'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCalls3() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '	foo {\n' +
            '		 XXX\n' +
            '	}\n' +
            '}', 'bind( x: enclosingCall()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'foo(), foo()'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCallNames1() {
        doTestOfLastBindingSet(
            'bar {\n' +
            '	foo {\n' +
            '		 XXX\n' +
            '	}\n' +
            '}', 'bind( x: enclosingCallName()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'foo, bar'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCallNames2() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '	bar {\n' +
            '		 XXX\n' +
            '	}\n' +
            '}', 'bind( x: enclosingCallName()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'bar, foo'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCallsName3() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '	foo {\n' +
            '		 XXX\n' +
            '	}\n' +
            '}',
            'bind( x: enclosingCallName()) & bind(y: currentIdentifier("XXX"))',

            // since we are matching on names and there are 2 names that are the same, they get collapsed
            new BindingResult('x', 'foo'),
            new BindingResult('y', 'Var: XXX'))
    }
}
