/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * All of these tests should produce {@link ModuleNode}s with
 * {@code encounteredUnrecoverableError} set to {@code true}.
 */
final class UnrecoverableErrorTests extends GroovyEclipseTestSuite {

    private GroovySnippetCompiler compiler

    @Before
    void setUp() {
        compiler = getGroovySnippetCompiler()
    }

    @After
    void tearDown() {
        compiler.cleanup()
    }

    private ModuleNode compileScript(String script) {
        compiler.compile(script, 'Test')
    }

    @Test
    void testSomething() {
        ModuleNode result = compileScript '''\
            |package a
            |void method() {
            |  if (###) {
            |  }
            |}
            |'''.stripMargin()
        assert result.encounteredUnrecoverableError()
    }
}
