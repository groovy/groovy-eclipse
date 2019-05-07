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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentFactory
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.junit.After
import org.junit.Before

abstract class CheckerTestSuite extends GroovyEclipseTestSuite {

    private GroovySnippetCompiler compiler

    @Before
    final void setUpCheckerTestCase() {
        compiler = groovySnippetCompiler
    }

    @After
    final void tearDownCheckerTestCase() {
        compiler.cleanup()
    }

    protected ModuleNode createModuleFromText(String text) {
        return compiler.compile(text)
    }

    protected IASTFragment getLastFragment(ModuleNode module) {
        Expression expr = getLastExpression(module)
        if (expr != null) {
            def factory = new ASTFragmentFactory()
            return factory.createFragment(expr)
        }
    }

    protected Expression getLastExpression(ModuleNode module) {
        Statement last = module.statementBlock.statements[-1]
        if (last instanceof ReturnStatement) {
            return last.expression
        } else if (last instanceof ExpressionStatement) {
            return last.expression
        }
        assert false : 'Could not find expression in module'
    }
}
