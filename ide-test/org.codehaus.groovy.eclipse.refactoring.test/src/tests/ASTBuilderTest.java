/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package tests;

import java.io.ByteArrayInputStream;

import org.codehaus.groovy.eclipse.core.compiler.GroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.GroovyCompilerConfigurationBuilder;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompiler;
import org.codehaus.groovy.eclipse.core.compiler.IGroovyCompilerConfiguration;
import org.codehaus.groovy.eclipse.refactoring.core.utils.GroovyCompilationReporter;

import junit.framework.TestCase;

public class ASTBuilderTest extends TestCase{
	
	private final static String GROOVY_CODE = "def a = new unknownClass()";
	
	private void generateAST(IGroovyCompilerConfiguration config){
		ByteArrayInputStream is = new ByteArrayInputStream(GROOVY_CODE.getBytes());
		GroovyCompilationReporter reporter = new GroovyCompilationReporter();
		IGroovyCompiler compiler = new GroovyCompiler();
		compiler.compile("", is, config, reporter);
		// Force to throw an Exception if build failed.
		reporter.moduleNode.hashCode();
	}
	
	public void testRegularASTGeneration(){
		IGroovyCompilerConfiguration config = new GroovyCompilerConfigurationBuilder().buildAST().done();
		try {
			generateAST(config);
			fail("Generation is supposed to fail in this case");
		} catch (NullPointerException e) {
		}
	}
	
	public void testASTGenerationWithOutClassResolution(){
		IGroovyCompilerConfiguration config = new GroovyCompilerConfigurationBuilder().buildAST().doNotResolveAST().done();
		try {
			generateAST(config);
		} catch (NullPointerException e) {
			fail("Generation is not supposed to fail in this case");
		}
	}

}
