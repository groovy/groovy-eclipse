/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Klenk and others        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
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
