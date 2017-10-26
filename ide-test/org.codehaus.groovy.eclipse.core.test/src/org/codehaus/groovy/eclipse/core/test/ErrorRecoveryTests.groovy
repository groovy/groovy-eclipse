/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetCompiler
import org.codehaus.groovy.eclipse.test.GroovyEclipseTestSuite
import org.eclipse.jdt.internal.compiler.CompilationResult
import org.junit.After
import org.junit.Before
import org.junit.Test

final class ErrorRecoveryTests extends GroovyEclipseTestSuite {

    private GroovySnippetCompiler compiler

    @Before
    void setUp() {
        compiler = getGroovySnippetCompiler()
    }

    @After
    void tearDown() {
        compiler.cleanup()
    }

    private CompilationResult compileScript(String script) {
        compiler.compileForErrors(script, nextUnitName())
    }

    @Test
    void testDotNothing1() {
        CompilationResult result = compileScript('s.')
        assert result.allProblems.length == 1
    }

    @Test
    void testDotNothing2() {
        CompilationResult result = compileScript('s.a.')
        assert result.allProblems.length == 1
    }

    @Test
    void testDotNothing3() {
        CompilationResult result = compileScript('s[10].')
        assert result.allProblems.length == 1
    }

    @Test
    void testDotNothing4() {
        CompilationResult result = compileScript('s().')
        assert result.allProblems.length == 1
    }

    @Test
    void testDotNothing5() {
        CompilationResult result = compileScript('s { it }.')
        assert result.allProblems.length == 1
    }

    @Test
    void testDotNothing6() {
        CompilationResult result = compileScript('String s = "hello"; s.')
        assert result.allProblems.length == 1
    }

    @Test
    void testSpreadDotNothing() {
        CompilationResult result = compileScript('s*.')
        assert result.allProblems.length == 1
    }

    @Test
    void testOptionalDotNothing() {
        CompilationResult result = compileScript('s?.')
        assert result.allProblems.length == 1
    }

    @Test
    void testDotLBrace() {
        CompilationResult result = compileScript('String s = "hello"; s.{')
        assert result.allProblems.length == 1
    }
}
