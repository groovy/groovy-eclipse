 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.compiler;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.eclipse.jdt.internal.compiler.CompilationResult;

public class ErrorRecoveryTests extends EclipseTestCase {
	private GroovySnippetCompiler compiler;

	@Override
    protected void setUp() throws Exception {
		super.setUp();
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		compiler = new GroovySnippetCompiler(testProject.getGroovyProjectFacade());
	}

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        compiler.cleanup();
    }

	public CompilationResult compileScript(String script) {
	    long start = System.currentTimeMillis();
	    CompilationResult result = compiler.compileForErrors(script, "Test");
	    System.out.println("Time to compile: " + (System.currentTimeMillis() - start) + " ms");
		return result;
	}

	public void testDotNothing1() {
	    CompilationResult result = compileScript("s.");
	    assertEquals(1, result.getAllProblems().length);
	}

	public void testDotNothing2() {
		CompilationResult result = compileScript("s.a.");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testDotNothing3() {
		CompilationResult result = compileScript("s[10].");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testDotNothing4() {
		CompilationResult result = compileScript("s().");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testDotNothing5() {
		CompilationResult result = compileScript("s { it }.");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testDotNothing6() {
		CompilationResult result = compileScript("String s = 'hello'; s.");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testSpreadDotNothing() {
		CompilationResult result = compileScript("s*.");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testOptionalDotNothing() {
		CompilationResult result = compileScript("s?.");
        assertEquals(1, result.getAllProblems().length);
	}

	public void testDotLBrace() {
		CompilationResult result = compileScript("String s = 'hello'; s.{");
        assertEquals(1, result.getAllProblems().length);
	}
}
