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

import static org.eclipse.jdt.core.JavaCore.createCompilationUnitFrom

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence
import org.junit.Before
import org.junit.Test

final class MetaDSLInferencingTests extends DSLInferencingTestSuite {

    @Before
    void setUp() {
        addJavaSource('import groovy.lang.*; public interface IPointcut { void accept(@DelegatesTo(value=IPointcut.class, strategy=Closure.DELEGATE_FIRST) Closure c); }', 'IPointcut', 'p')
        addPlainText(getTestResourceContents('DSLD_meta_script.dsld'), 'DSLD_meta_script.dsld')
        buildProject()
    }

    private GroovyCompilationUnit addDsldSource(String contents) {
        createCompilationUnitFrom(addPlainText(contents, 'test.dsld'))
    }

    private void assertDsldType(GroovyCompilationUnit unit, String expr) {
        int start = unit.source.lastIndexOf(expr), until = start + expr.length()
        String declType = InferencingTestSuite.doVisit(start, until, unit).declaringTypeName
        assert declType == 'p.IPointcut'
    }

    private void assertDsldUnknown(GroovyCompilationUnit unit, String expr) {
        int start = unit.source.lastIndexOf(expr), until = start + expr.length()
        def result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.confidence == TypeConfidence.UNKNOWN
    }

    private void assertUnknown(String contents, String expr) {
        assertUnknownConfidence(contents, contents.indexOf(expr), contents.indexOf(expr) + expr.length(), null)
    }

    //

    @Test // https://github.com/groovy/groovy-eclipse/issues/638
    void testBindings0() {
        GroovyCompilationUnit unit = addDsldSource('''\
            bind(methods: enclosingMethod()).accept {
            }
            '''.stripIndent())

        int start = unit.source.indexOf('methods'), until = start + 'methods'.length()
        def result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.declaration instanceof ConstantExpression
        assert result.type.name == 'java.lang.String'
    }

    @Test
    void testBindings1() {
        GroovyCompilationUnit unit = addDsldSource('''\
            bind(types: currentType()).accept {
              types
            }
            '''.stripIndent())

        int offset = unit.source.lastIndexOf('types')
        InferencingTestSuite.assertType(unit, offset, offset + 'types'.length(), 'java.util.Collection')
    }

    @Test
    void testBindings2() {
        GroovyCompilationUnit unit = addDsldSource('''\
            contribute(bind(types: currentType())) {
              types
            }
            '''.stripIndent())

        int offset = unit.source.lastIndexOf('types')
        InferencingTestSuite.assertType(unit, offset, offset + 'types'.length(), 'java.util.Collection')
    }

    @Test
    void testBindings3() {
        GroovyCompilationUnit unit = addDsldSource('''\
            contribute(currentType(annos: annotatedBy(Deprecated))) {
              annos
            }
            '''.stripIndent())

        int offset = unit.source.lastIndexOf('annos')
        InferencingTestSuite.assertType(unit, offset, offset + 'annos'.length(), 'java.util.Collection')
    }

    @Test
    void testMetaDSL1() {
        GroovyCompilationUnit unit = addDsldSource('currentType')
        InferencingTestSuite.assertType(unit, 0, 'currentType'.length(), 'p.IPointcut')
    }

    @Test
    void testMetaDSL2() {
        GroovyCompilationUnit unit = addDsldSource('assertVersion')
        InferencingTestSuite.assertType(unit, 0, 'assertVersion'.length(), 'java.lang.Void')
    }

    @Test
    void testMetaDSL3() {
        GroovyCompilationUnit unit = addDsldSource('supportsVersion')
        InferencingTestSuite.assertType(unit, 0, 'supportsVersion'.length(), 'java.lang.Boolean')
    }

    @Test
    void testMetaDSL4() {
        GroovyCompilationUnit unit = addDsldSource('''\
            currentType().accept {
              method
              wormhole
              setDelegateType
              delegatesToUseNamedArgs
              delegatesTo
            }
            '''.stripIndent())

        assertDsldType(unit, 'method')
        assertDsldType(unit, 'wormhole')
        assertDsldType(unit, 'setDelegateType')
        assertDsldType(unit, 'delegatesToUseNamedArgs')
        assertDsldType(unit, 'delegatesTo')
    }

    @Test // object expression is not "this"
    void testMetaDSL5() {
        GroovyCompilationUnit unit = addDsldSource('''\
            currentType().accept {
              String x = 'x'
              x.method
              x.wormhole
              x.setDelegateType
              x.delegatesToUseNamedArgs
              x.delegatesTo
            }
            '''.stripIndent())

        assertDsldUnknown(unit, 'method')
        assertDsldUnknown(unit, 'wormhole')
        assertDsldUnknown(unit, 'setDelegateType')
        assertDsldUnknown(unit, 'delegatesToUseNamedArgs')
        assertDsldUnknown(unit, 'delegatesTo')
    }

    @Test // unknown outside contribution block
    void testMetaDSL6() {
        GroovyCompilationUnit unit = addDsldSource('''\
            method
            wormhole
            setDelegateType
            delegatesToUseNamedArgs
            delegatesTo
            '''.stripIndent())

        assertDsldUnknown(unit, 'method')
        assertDsldUnknown(unit, 'wormhole')
        assertDsldUnknown(unit, 'setDelegateType')
        assertDsldUnknown(unit, 'delegatesTo')
        assertDsldUnknown(unit, 'delegatesToUseNamedArgs')
    }

    @Test // unknown within plain Groovy source
    void testMetaDSL7() {
        String contents = '''\
            currentType().accept {
              method
              wormhole
              setDelegateType
              delegatesTo
              delegatesToUseNamedArgs
            }
            '''.stripIndent()

        assertUnknown(contents, 'currentType')
        assertUnknown(contents, 'accept')
        assertUnknown(contents, 'method')
        assertUnknown(contents, 'wormhole')
        assertUnknown(contents, 'setDelegateType')
        assertUnknown(contents, 'delegatesTo')
        assertUnknown(contents, 'delegatesToUseNamedArgs')
    }

    @Test // local declarations take precedence over meta-DSL contributions
    void testMetaDSL8() {
        GroovyCompilationUnit unit = addDsldSource('''\
            import org.codehaus.groovy.ast.*
            contribute(bind(methods: enclosingMethod())) {
              MethodNode mn = null
              for (MethodNode method : methods) {
                Parameter[] params = method.parameters
                // ... if (...) {
                mn = method
                // }
              }
              method(name:'other', params: params(mn))
            }
            '''.stripIndent())

        // "method" in "Parameter[] params = method.parameters"
        int start = unit.source.indexOf('method.'), until = start + 'method'.length()
        def result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.type.toString(false) == 'org.codehaus.groovy.ast.MethodNode'

        // "method()" in "method(name:'other', params: params(mn))"
        start = unit.source.lastIndexOf('method'); until = start + 'method'.length()
        result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.declaration instanceof org.codehaus.groovy.ast.MethodNode

        // "params" in "Parameter[] params = method.parameters"
        start = unit.source.indexOf('params'); until = start + 'params'.length()
        result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.type.toString(false) == 'org.codehaus.groovy.ast.Parameter[]'

        // "params:" in "method(name:'other', params: params(mn))"
        start = unit.source.indexOf('params:'); until = start + 'params'.length()
        result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.type.toString(false) == 'java.lang.String'

        // "params()" in "method(name:'other', params: params(mn))"
        start = unit.source.indexOf('params('); until = start + 'params'.length()
        result = InferencingTestSuite.doVisit(start, until, unit).result
        assert result.declaration instanceof org.codehaus.groovy.ast.MethodNode
        assert result.type.toString(false) == 'java.util.Map <java.lang.String, org.codehaus.groovy.ast.ClassNode>'
    }

    @Test
    void testRegisterPointcut1() {
        def dsld = addDsldSource('registerPointcut')
        InferencingTestSuite.assertType(dsld, 0, 'registerPointcut'.length(), 'java.lang.Void')
    }

    @Test
    void testRegisterPointcut2() {
        def dsld = addDsldSource('''\
            registerPointcut('pointcutName') {
                log ""
            }
            '''.stripIndent())
        def target = 'log'
        int offset = dsld.source.lastIndexOf(target)
        InferencingTestSuite.assertType(dsld, offset, offset + target.length(), 'java.lang.Object')
    }

    @Test
    void testRegisterPointcut3() {
        def dsld = addDsldSource('''\
            registerPointcut('pointcutName') {
                currentType
            }
            '''.stripIndent())
        def target = 'currentType'
        int offset = dsld.source.lastIndexOf(target)
        InferencingTestSuite.assertType(dsld, offset, offset + target.length(), 'org.codehaus.groovy.ast.ClassNode')
    }
}
