/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests

import static org.eclipse.jdt.core.JavaCore.createCompilationUnitFrom

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.jdt.core.groovy.tests.search.InferencingTestSuite
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence
import org.junit.Before
import org.junit.Test

final class MetaDSLInferencingTests extends DSLInferencingTestSuite {

    @Before
    void setUp() {
        addJavaSource('public interface IPointcut { Object accept(@groovy.lang.DelegatesTo(IPointcut.class) groovy.lang.Closure<?> c); }', 'IPointcut', 'p')
        addPlainText(getTestResourceContents('DSLD_meta_script.dsld'), 'DSLD_meta_script.dsld')
        buildProject()
    }

    private GroovyCompilationUnit addDsldSource(String contents) {
        createCompilationUnitFrom(addPlainText(contents, 'test.dsld'))
    }

    private void assertDsldType(GroovyCompilationUnit unit, String expr) {
        int start = unit.source.lastIndexOf(expr), until = start + expr.length()
        String declType = InferencingTestSuite.doVisit(start, until, unit, false).declaringTypeName
        assert declType == 'p.IPointcut'
    }

    private void assertDsldUnknown(GroovyCompilationUnit unit, String expr) {
        int start = unit.source.lastIndexOf(expr), until = start + expr.length()
        TypeConfidence exprCnf = InferencingTestSuite.doVisit(start, until, unit, false).result.confidence
        assert exprCnf == TypeConfidence.UNKNOWN
    }

    private void assertUnknown(String contents, String expr) {
        assertUnknownConfidence(contents, contents.indexOf(expr), contents.indexOf(expr) + expr.length(), null)
    }

    //

    @Test
    void testSimpleDSL() {
        assertType('foo', 0, 3, 'java.lang.Object')
        createDsls('currentType().accept { property ( name: "foo", type: Date ) }')
        assertType('foo', 0, 3, 'java.util.Date')
        deleteDslFile(0)
        assertType('foo', 0, 3, 'java.lang.Object')
    }

    @Test
    void testMetaDSL1() {
        GroovyCompilationUnit unit = addDsldSource('currentType')
        InferencingTestSuite.assertType(unit, 0, 'currentType'.length(), 'p.IPointcut')
    }

    @Test
    void testMetaDSL2() {
        GroovyCompilationUnit unit = addDsldSource('registerPointcut')
        InferencingTestSuite.assertType(unit, 0, 'registerPointcut'.length(), 'java.lang.Void')
    }

    @Test
    void testMetaDSL3() {
        GroovyCompilationUnit unit = addDsldSource('supportsVersion')
        InferencingTestSuite.assertType(unit, 0, 'supportsVersion'.length(), 'java.lang.Void')
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
              String x
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

    @Test
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

    @Test // within plain Groovy source, these things are unknown
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

    @Test
    void testBindings() {
        GroovyCompilationUnit unit = addDsldSource('''\
            bind(b: currentType()).accept {
              b
            }
            '''.stripIndent())

        assertDsldType(unit, 'b')
    }
}
