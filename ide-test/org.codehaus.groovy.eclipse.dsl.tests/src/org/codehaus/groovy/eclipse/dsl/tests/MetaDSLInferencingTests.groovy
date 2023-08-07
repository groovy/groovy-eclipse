/*
 * Copyright 2009-2023 the original author or authors.
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
import org.eclipse.jdt.groovy.search.TypeLookupResult
import org.junit.Before
import org.junit.Test

final class MetaDSLInferencingTests extends DSLInferencingTestSuite {

    @Before
    void setUp() {
        addJavaSource('''\
            |import groovy.lang.*;
            |public interface IPointcut {
            |  void accept(@DelegatesTo(value=IPointcut.class, strategy=Closure.DELEGATE_FIRST) Closure block);
            |}
            |'''.stripMargin(), 'IPointcut', 'p')
        addPlainText(getTestResourceContents('DSLD_meta_script.dsld'), 'DSLD_meta_script.dsld')
        buildProject()
    }

    private GroovyCompilationUnit addDsldSource(String contents) {
        createCompilationUnitFrom(addPlainText(contents, 'test.dsld'))
    }

    private void assertDsldType(GroovyCompilationUnit unit, String expr) {
        String declType = inferType(unit, expr).declaringTypeName
        assert declType == 'p.IPointcut'
    }

    private void assertUnknown(GroovyCompilationUnit unit, String expr) {
        TypeLookupResult result = inferType(unit, expr).result
        assert result.confidence == TypeLookupResult.TypeConfidence.UNKNOWN
    }

    //--------------------------------------------------------------------------

    @Test
    void testMetaDSL1() {
        def unit = addDsldSource('assertVersion')

        assert inferType(unit, 'assertVersion').typeName == 'java.lang.Void'
    }

    @Test
    void testMetaDSL2() {
        def unit = addDsldSource('supportsVersion')

        assert inferType(unit, 'supportsVersion').typeName == 'java.lang.Boolean'
    }

    @Test
    void testMetaDSL3() {
        def unit = addDsldSource('currentType()')

        assert inferType(unit, 'currentType').typeName == 'p.IPointcut'
    }

    @Test
    void testMetaDSL4() {
        def unit = addDsldSource '''\
            |currentType().accept {
            |  method
            |  property
            |  wormhole
            |  delegatesTo
            |  setDelegateType('Foo')
            |}
            |'''.stripMargin()

        assertDsldType(unit, 'method')
        assertDsldType(unit, 'property')
        assertDsldType(unit, 'wormhole')
        assertDsldType(unit, 'delegatesTo')
        assertDsldType(unit, 'setDelegateType')
    }

    @Test
    void testMetaDSL5() {
        // object expression is not "this"
        def unit = addDsldSource '''\
            |currentType().accept {
            |  String x = 'x'
            |  x.method
            |  x.property
            |  x.wormhole
            |  x.delegatesTo
            |  x.setDelegateType('Foo')
            |}
            |'''.stripMargin()

        assertUnknown(unit, 'method')
        assertUnknown(unit, 'property')
        assertUnknown(unit, 'wormhole')
        assertUnknown(unit, 'delegatesTo')
        assertUnknown(unit, 'setDelegateType')
    }

    @Test
    void testMetaDSL6() {
        // unknown outside contribution block
        def unit = addDsldSource '''\
            |method
            |property
            |wormhole
            |delegatesTo
            |setDelegateType('Foo')
            |'''.stripMargin()

        assertUnknown(unit, 'method')
        assertUnknown(unit, 'property')
        assertUnknown(unit, 'wormhole')
        assertUnknown(unit, 'delegatesTo')
        assertUnknown(unit, 'setDelegateType')
    }

    @Test
    void testMetaDSL7() {
        // unknown within plain Groovy source
        def unit = addGroovySource '''\
            |currentType().accept {
            |  method
            |  property
            |  wormhole
            |  delegatesTo
            |  setDelegateType('Foo')
            |}
            |'''.stripMargin()

        assertUnknown(unit, 'currentType')
        assertUnknown(unit, 'accept')
        assertUnknown(unit, 'method')
        assertUnknown(unit, 'property')
        assertUnknown(unit, 'wormhole')
        assertUnknown(unit, 'delegatesTo')
        assertUnknown(unit, 'setDelegateType')
    }

    @Test
    void testMetaDSL8() {
        // local declarations take precedence over meta-DSL contributions
        def unit = addDsldSource '''\
            |import org.codehaus.groovy.ast.*
            |def name = null; name = 'foobar'
            |contribute(bind(methods: enclosingMethod())) {
            |  MethodNode mn = null
            |  for (MethodNode method : methods) {
            |    Parameter[] params = method.parameters
            |    // ... if (...) {
            |    mn = method
            |    // }
            |  }
            |  method(name:'other', params: params(mn))
            |}
            |'''.stripMargin()

        assert inferType(unit, 'name = ', 4).typeName == 'java.lang.String' // not IPointcut

        // "method" in "Parameter[] params = method.parameters"
        assert inferType(unit, 'method.', 6).typeName == 'org.codehaus.groovy.ast.MethodNode'

        // "method()" in "method(name:'other', params: params(mn))"
        assert inferType(unit, 'method').result.declaration instanceof org.codehaus.groovy.ast.MethodNode

        // "params" in "Parameter[] params = method.parameters"
        assert inferType(unit, 'params =', 6).typeName == 'org.codehaus.groovy.ast.Parameter[]'

        // "params:" in "method(name:'other', params: params(mn))"
        assert inferType(unit, 'params:', 6).typeName == 'java.lang.String'

        // "params()" in "method(name:'other', params: params(mn))"
        inferType(unit, 'params(', 'params'.length()).with {
            assert result.declaration instanceof org.codehaus.groovy.ast.MethodNode
            assert typeName == 'java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>'
        }
    }

    @Test
    void testMetaDSL9() {
        def unit = addDsldSource '''\
            |contribute(inClosure()) {
            |  delegateType = null
            |}
            |'''.stripMargin()

        assert inferType(unit, 'delegateType').result.declaration.name == 'setDelegateType'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/638
    void testBindings1() {
        def unit = addDsldSource '''\
            |bind(methods: enclosingMethod()).accept {
            |}
            |'''.stripMargin()

        inferType(unit, 'methods').with {
            assert typeName == 'java.lang.String'
            assert result.declaration instanceof ConstantExpression
        }
    }

    @Test
    void testBindings2() {
        def unit = addDsldSource '''\
            |bind(types: currentType()).accept {
            |  types
            |}
            |'''.stripMargin()

        assert inferType(unit, 'types').typeName == 'java.util.Collection'
    }

    @Test
    void testBindings3() {
        def unit = addDsldSource '''\
            |contribute(bind(types: currentType())) {
            |  types
            |}
            |'''.stripMargin()

        assert inferType(unit, 'types').typeName == 'java.util.Collection'
    }

    @Test
    void testBindings4() {
        def unit = addDsldSource '''\
            |contribute(currentType(annos: annotatedBy(Deprecated))) {
            |  annos
            |}
            |'''.stripMargin()

        assert inferType(unit, 'annos').typeName == 'java.util.Collection'
    }

    @Test
    void testRegisterPointcut1() {
        def unit = addDsldSource('registerPointcut')

        assert inferType(unit, 'registerPointcut').typeName == 'java.lang.Void'
    }

    @Test
    void testRegisterPointcut2() {
        def unit = addDsldSource '''\
            |registerPointcut('pointcutName') {
            |  log ""
            |}
            |'''.stripMargin()

        assert inferType(unit, 'log').typeName == 'java.lang.Object'
    }

    @Test
    void testRegisterPointcut3() {
        def unit = addDsldSource '''\
            |registerPointcut('pointcutName') {
            |  currentType
            |}
            |'''.stripMargin()

        assert inferType(unit, 'currentType').typeName == 'org.codehaus.groovy.ast.ClassNode'
    }
}
