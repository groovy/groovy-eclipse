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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;

public class ErrorRecoveryTests extends EclipseTestCase {
	private GroovyCompiler compiler;

	private Reporter reporter;
	
	private IGroovyCompilerConfiguration config;

	@Override
    protected void setUp() throws Exception {
		super.setUp();
		compiler = new GroovyCompiler();
		reporter = new Reporter();
		GroovyRuntime.addGroovyRuntime(testProject.getProject());
		config = new GroovyCompilerConfigurationBuilder().classLoader(new GroovyProjectFacade(testProject.getJavaProject()).getProjectClassLoader())
			.buildCST()
			.errorRecovery()
			.resolveAST()
			.buildAST()
			.done();
	}
	
	public void compileScript(String script) {
		InputStream is = textToInputStream(script);
		compiler.compile("test", is, config, reporter);
	}

	public void testDotNothing1() {
		compileScript("s.");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testDotNothing2() {
		compileScript("s.a.");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testDotNothing3() {
		compileScript("s[10].");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testDotNothing4() {
		compileScript("s().");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testDotNothing5() {
		compileScript("s { it }.");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testDotNothing6() {
		compileScript("String s = 'hello'; s.");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testSpreadDotNothing() {
		compileScript("s*.");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	public void testOptionalDotNothing() {
		compileScript("s?.");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}

	public void testDotLBrace() {
		compileScript("String s = 'hello'; s.{");
		assertEquals(1, reporter.mapFileNameToErrorMessages.size());
		assertEquals(1, reporter.mapFileNameToCST.size());
		assertEquals(1, reporter.mapFileNameToAST.size());
	}
	
	private InputStream textToInputStream(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}
}
