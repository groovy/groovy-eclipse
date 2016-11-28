/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.tests;

import java.util.List;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentFactory;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;

/**
 * @author Andrew Eisenberg
 * @created May 13, 2010
 */
public abstract class CheckerTestCase extends EclipseTestCase {

    private GroovySnippetCompiler compiler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compiler = new GroovySnippetCompiler(testProject.getGroovyProjectFacade());
    }

    @Override
    protected void tearDown() throws Exception {
        compiler.cleanup();
        super.tearDown();
    }

    protected IASTFragment getLastFragment(ModuleNode module) {
        Expression expr = getLastExpression(module);
        if (expr != null) {
            ASTFragmentFactory factory = new ASTFragmentFactory();
            return factory.createFragment(expr);
        } else {
            // won't get here because a fail() would have been called
            return null;
        }
    }

    protected Expression getLastExpression(ModuleNode module) {
        List<Statement> statements = module.getStatementBlock().getStatements();
        Statement last = statements.get(statements.size() - 1);
        if (last instanceof ReturnStatement) {
            return ((ReturnStatement) last).getExpression();
        } else if (last instanceof ExpressionStatement) {
            return ((ExpressionStatement) last).getExpression();
        } else {
            fail("Could not find expression in module");
        }
        // won't get here
        return null;
    }

    protected ModuleNode createModuleFromText(String text) {
        return compiler.compile(text);
    }
}
