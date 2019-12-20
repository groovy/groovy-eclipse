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
        private final Stack<BindingSet> matches = new Stack()

        BindingSet largestMatch
        Collection<?> largestMatchResult

        PointcutEvaluationRequestor(IPointcut toMatch, GroovyCompilationUnit unit) {
            this.toMatch = toMatch
            this.context = createContext(unit)
        }

        private static GroovyDSLDContext createContext(GroovyCompilationUnit unit) {
            ModuleNodeInfo info = unit.getModuleInfo(true)
            GroovyDSLDContext context = new GroovyDSLDContext(unit, info.module, info.resolver)
            context.resetBinding()
            return context
        }

        @Override
        VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
            if (result?.scope != null) {
                context.currentScope = result.scope
                context.targetType = result.type
                context.resetBinding()
                Collection<?> matchResult = toMatch.matches(context, result.type)
                BindingSet bindingSet = context.currentBinding
                matches.push(bindingSet)
                if (largestMatch == null || largestMatch.size() <= bindingSet.size()) {
                    largestMatch = bindingSet
                    largestMatchResult = matchResult
                }
            }
            return VisitStatus.CONTINUE
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
        assertSingleBinding(match, name)
    }

    private void assertAllBindings(BindingSet bindings, BindingResult... results) {
        if (results.length == 0) {
            Assert.assertEquals('Should not have found any bindings', 0, bindings.bindings.size())
            return
        }
        Assert.assertNotNull('Should have found some bindings.  Expected:\n' + Arrays.toString(results), bindings)

        for (result in results) {
            Collection<?> o = bindings.getBinding(result.bindingName)
            if (o == null) {
                Assert.fail('Expected binding "' + result.bindingName + '", but not found.\n' + 'Actual bindings:\n' + bindings.bindings)
            }
            assertSingleBinding(o, result.bindingToString)
        }
        Assert.assertEquals('Wrong number of bindings.  Expected Bindings: \n' + Arrays.toString(results) + '\nActualBindings:\n' + bindings.bindings, results.length, bindings.bindings.size())
    }

    private void assertSingleBinding(Collection<?> binding, String expected) {
        if (expected == null) {
            Assert.assertNull('Match should have been null', binding)
            return
        }
        Assert.assertNotNull('Match should not be null', binding)

        String[] tokens = expected.split(',')
        Assert.assertEquals("Unexpected number of bindings for ${ -> BindingSet.printCollection(binding)}", tokens.length, binding.size())

        binding.each { object ->
            String name = extractName(object)
            if (!tokens.any { String token ->
                String regex = token.trim().replace('(', '\\(').replace(')', '\\)')
                return name.matches(regex)
            }) {
                Assert.fail('Match result ' + name + ' not found in "' + expected + '"')
            }
        }
    }

    private String extractName(Object bindingElement) {
        if (bindingElement == null) {
            return null
        } else if (bindingElement instanceof ClassNode) {
            return bindingElement.name
        } else if (bindingElement instanceof FieldNode) {
            return bindingElement.declaringClass.name + '.' + bindingElement.name
        } else if (bindingElement instanceof MethodNode) {
            return bindingElement.declaringClass.name + '.' + bindingElement.name
        } else if (bindingElement instanceof AnnotationNode) {
            return '@' + bindingElement.classNode.name
        } else if (bindingElement instanceof MethodCallExpression) {
            return bindingElement.methodAsString + '()'
        } else if (bindingElement instanceof VariableExpression) {
            return 'Var: ' + bindingElement.name
        } else if (bindingElement instanceof Collection) {
            return bindingElement.collect { extractName(it) }.join(', ')
        } else {
            return bindingElement.toString()
        }
    }

    private Collection<?> evaluateForMatch(GroovyCompilationUnit unit, String pointcutText) {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText)
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit)
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit)
        visitor.visitCompilationUnit(requestor)
        return requestor.hasMatches() ? requestor.largestMatchResult : null
    }

    private BindingSet evaluateForBindings(GroovyCompilationUnit unit, String pointcutText) {
        IPointcut pc = new PointcutScriptExecutor().createPointcut(pointcutText)
        PointcutEvaluationRequestor requestor = new PointcutEvaluationRequestor(pc, unit)
        TypeInferencingVisitorWithRequestor visitor = new TypeInferencingVisitorFactory().createVisitor(unit)
        visitor.visitCompilationUnit(requestor)
        return requestor.hasMatches() ? requestor.largestMatch : null
    }

    //--------------------------------------------------------------------------

    @Test
    void testCurrentType1() {
        doTestOfLastMatch('2', 'currentType("java.lang.Integer")', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFields1() {
        doTestOfLastMatch('2', 'currentType(fields("value"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFields2() {
        doTestOfLastMatch('2', 'currentType(fields("notHere"))', null)
    }

    @Test
    void testCurrentTypeMethods1() {
        doTestOfLastMatch('2', 'currentType(methods("intValue"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeHasConstructor1() {
        doTestOfLastMatch('2', 'currentType(hasConstructor("int"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeHasConstructor2() {
        doTestOfLastMatch('2', 'currentType(hasConstructor("String"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeHasConstructor3() {
        doTestOfLastMatch('""', 'currentType(hasConstructor("char[],int,int"))', 'java.lang.String')
    }

    @Test
    void testCurrentTypeHasConstructor4() {
        doTestOfLastMatch('2', 'currentType(hasConstructor("Unknown"))', null)
    }

    @Test
    void testCurrentTypeHasConstructor5() {
        doTestOfLastMatch('2', 'currentType(hasConstructor(isPublic()))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeHasConstructor6() {
        doTestOfLastMatch('2', 'currentType(hasConstructor(isPrivate()))', null)
    }

    @Test
    void testCurrentTypeHasConstructor7() {
        doTestOfLastMatch('2', 'currentType(hasConstructor(hasArgument(type(int))))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods1() {
        doTestOfLastMatch('2', 'currentType(fields("value") & methods("intValue"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods2() {
        doTestOfLastMatch('2',
            'def left = fields("value")\n' +
            'def right = methods("intValue")\n' +
            'currentType(left & right)', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods3() {
        doTestOfLastMatch('2',
            'def left = { fields("value") }\n' +
            'def right = { methods("intValue") }\n' +
            'currentType(left() & right())', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods4() {
        doTestOfLastMatch('2', 'currentType(fields("notHere") & methods("intValue"))', null)
    }

    @Test
    void testCurrentTypeFieldsAndMethods5() {
        doTestOfLastMatch('2', 'currentType(methods("intValue") & fields("notHere"))', null)
    }

    @Test
    void testCurrentTypeFieldsAndMethods6() {
        doTestOfLastMatch('2',
            'def left = fields("notHere")\n' +
            'def right = methods("intValue")\n' +
            'currentType(left & right)', null)
    }

    @Test
    void testCurrentTypeFieldsAndMethods7() {
        doTestOfLastMatch('2', 'currentType(fields("notHere") | methods("intValue"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods8() {
        doTestOfLastMatch('2', 'currentType(methods("intValue") | fields("notHere"))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods9() {
        doTestOfLastMatch('2', 'currentType(subType("java.lang.Number")) | (currentType(methods("intValue") & fields("notHere")))', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods10() {
        doTestOfLastMatch('2', '(currentType(methods("intValue") & fields("notHere"))) | currentType(subType("java.lang.Number")) ', 'java.lang.Integer')
    }

    @Test
    void testCurrentTypeFieldsAndMethods11() {
        doTestOfLastMatch('2', 'currentType("java.lang.Number.NOPE") | (currentType(methods("intValue") & fields("notHere")))', null)
    }

    @Test
    void testCurrentTypeFieldsAndMethods12() {
        doTestOfLastMatch('2', 'currentType(subType("java.lang.Number")) & (currentType(methods("intValue") & fields("notHere")))', null)
    }

    @Test
    void testAnnotatedBy1() {
        addGroovySource('@Deprecated\nclass Foo {}', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(annotatedBy("java.lang.Deprecated"))', 'p.Foo')
    }

    @Test
    void testAnnotatedBy2() {
        addGroovySource('class Foo {\n@Deprecated def t }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(fields(annotatedBy("java.lang.Deprecated")))', 'p.Foo')
    }

    @Test
    void testAnnotatedBy3() {
        addGroovySource('class Foo {\n@Deprecated def t() { } }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(methods(annotatedBy("java.lang.Deprecated")))', 'p.Foo')
    }

    @Test
    void testAnnotatedBy4() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(annotatedBy("java.lang.Deprecated") & fields("f") )', 'p.Foo')
    }

    @Test
    void testAnnotatedBy5() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType(annotatedBy("java.lang.Deprecated") | fields("g") )', 'p.Foo')
    }

    @Test
    void testAnnotatedBy6() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType( fields("g") | annotatedBy("java.lang.Deprecated") )', 'p.Foo')
    }

    @Test
    void testAnnotatedBy7() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType( fields("g") & annotatedBy("java.lang.Deprecated") )', null)
    }

    @Test
    void testAnnotatedBy8() {
        addGroovySource('class Foo { \n @Deprecated def f\n @Deprecated def g() { } }', nextUnitName(), 'p')
        doTestOfLastMatch('Foo', 'currentType( fields( annotatedBy("java.lang.Deprecated") ) & methods( annotatedBy("java.lang.Deprecated") ) )', 'p.Foo')
    }

    @Test
    void testAnnotatedBy9() {
        addGroovySource('@interface Tag { String value(); }', 'Tag', 'a')
        addGroovySource('@interface Tags { Tag[] value(); }', 'Tags', 'a')
        addGroovySource('import a.*; @Tags([@Tag("one"), @Tag("two")]) class Bar { def baz() {} }', 'Bar', 'foo')
        doTestOfLastMatch('foo.Bar', 'currentType(annotatedBy("a.Tag"))', 'foo.Bar')
    }

    @Test
    void testAnnotatedBy9a() {
        addGroovySource('@interface Tag { String value(); }', 'Tag', 'a')
        addGroovySource('@interface Tags { Tag[] value(); }', 'Tags', 'a')
        addGroovySource('import a.*; @Tags(@Tag("one")) class Bar { def baz() {} }', 'Bar', 'foo')
        doTestOfLastMatch('foo.Bar', 'currentType(annotatedBy("a.Tag"))', 'foo.Bar')
    }

    @Test
    void testFileExtension1() {
        doTestOfLastMatch('2', 'fileExtension("groovy")', 'src/p/TestUnit_[0-9a-f]{32}.groovy')
    }

    @Test
    void testFileExtension2() {
        doTestOfLastMatch('2', 'fileExtension("invalid")', null)
    }

    @Test
    void testNature1() {
        doTestOfLastMatch('2', 'nature("org.eclipse.jdt.groovy.core.groovyNature")', 'org.eclipse.jdt.groovy.core.groovyNature')
    }

    @Test
    void testNature2Fail() {
        doTestOfLastMatch('2', 'nature("invalid")', null)
    }

    @Test
    void testPackageFolder1() {
        doTestOfLastMatch('p', '2', 'packageFolder("p")', 'p')
    }

    @Test
    void testPackageFolder2() {
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
            new BindingResult('c', 'src/p/TestUnit_[0-9a-f]{32}.groovy'))
    }

    @Test
    void testNamedBinding4() {
        doTestOfLastBindingSet('2', 'bind( b : nature("org.eclipse.jdt.groovy.core.groovyNature") ) & ' +
            'bind( c : fileExtension("groovy") )',
            new BindingResult('b', 'org.eclipse.jdt.groovy.core.groovyNature'),
            new BindingResult('c', 'src/p/TestUnit_[0-9a-f]{32}.groovy'))
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
            new BindingResult('c', 'src/p/TestUnit_[0-9a-f]{32}.groovy'))
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
            new BindingResult('c', 'src/p/TestUnit_[0-9a-f]{32}.groovy'))
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
    void testAnnotatedByBinding1() {
        addGroovySource('@Deprecated\nclass Foo {}', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind( b : annotatedBy("java.lang.Deprecated")))',
            new BindingResult('b', '@java.lang.Deprecated'))
    }

    @Test
    void testAnnotatedByBinding2() {
        addGroovySource('class Foo {\n@Deprecated def t }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind(b : fields(annotatedBy("java.lang.Deprecated"))))',
            new BindingResult('b', 'p.Foo.t'))
    }

    @Test
    void testAnnotatedByBinding3() {
        addGroovySource('class Foo {\n@Deprecated def t() { } }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind( b : methods(annotatedBy("java.lang.Deprecated"))))',
            new BindingResult('b', 'p.Foo.t'))
    }

    @Test
    void testAnnotatedByBinding4() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind ( b : annotatedBy("java.lang.Deprecated") & fields("f") ) )',
            new BindingResult('b', '@java.lang.Deprecated, p.Foo.f'))
    }

    @Test
    void testAnnotatedByBinding5() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType(bind ( b : annotatedBy("java.lang.Deprecated") | fields("f") ) )',
            new BindingResult('b', '@java.lang.Deprecated, p.Foo.f'))
    }

    @Test
    void testAnnotatedByBinding6() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( bind( b : fields("g")) | annotatedBy("java.lang.Deprecated") )')
    }

    @Test
    void testAnnotatedByBinding7Fail() {
        addGroovySource('@Deprecated\nclass Foo { \n def f }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( fields("g") & bind( b : annotatedBy("java.lang.Deprecated") ) )')
    }

    @Test
    void testAnnotatedByBinding8() {
        addGroovySource('class Foo { \n @Deprecated def f\n @Deprecated def g() { } }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( bind( b : fields( annotatedBy("java.lang.Deprecated") ) & methods( annotatedBy("java.lang.Deprecated") ) ) )',
            new BindingResult('b', 'p.Foo.f, p.Foo.g'))
    }

    @Test
    void testAnnotatedByBinding9() {
        addGroovySource('class Foo { \n @Deprecated def f\n @Deprecated def g() { } }', 'Foo', 'p')
        doTestOfLastBindingSet('Foo', 'currentType( fields( bind ( b : annotatedBy("java.lang.Deprecated") ) ) & methods( bind ( b : annotatedBy("java.lang.Deprecated") ) ) )',
            new BindingResult('b', '@java.lang.Deprecated, @java.lang.Deprecated'))
    }

    @Test
    void testNestedCalls1() {
        doTestOfLastBindingSet(
            'bar {\n' +
            '  foo {\n' +
            '    XXX\n' +
            '  }\n' +
            '}', 'bind( x: enclosingCall()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'foo(), bar()'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCalls2() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '  bar {\n' +
            '    XXX\n' +
            '  }\n' +
            '}', 'bind( x: enclosingCall()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'bar(), foo()'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCalls3() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '  foo {\n' +
            '    XXX\n' +
            '  }\n' +
            '}', 'bind( x: enclosingCall()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'foo(), foo()'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCallNames1() {
        doTestOfLastBindingSet(
            'bar {\n' +
            '  foo {\n' +
            '    XXX\n' +
            '  }\n' +
            '}', 'bind( x: enclosingCallName()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'foo, bar'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCallNames2() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '  bar {\n' +
            '    XXX\n' +
            '  }\n' +
            '}', 'bind( x: enclosingCallName()) & bind(y: currentIdentifier("XXX"))',
            new BindingResult('x', 'bar, foo'),
            new BindingResult('y', 'Var: XXX'))
    }

    @Test
    void testNestedCallsName3() {
        doTestOfLastBindingSet(
            'foo {\n' +
            '  foo {\n' +
            '    XXX\n' +
            '  }\n' +
            '}',
            'bind( x: enclosingCallName()) & bind(y: currentIdentifier("XXX"))',

            // since we are matching on names and there are 2 names that are the same, they get collapsed
            new BindingResult('x', 'foo'),
            new BindingResult('y', 'Var: XXX'))
    }
}
