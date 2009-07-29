/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.test.ui

import org.codehaus.groovy.eclipse.ui.ArtifactCodeGenerator class ArtifactCodeGeneratorTest extends org.codehaus.groovy.eclipse.test.EclipseTestCase {
	ArtifactCodeGenerator generator
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		generator = new ArtifactCodeGenerator(testProject.getJavaProject())
	}
	
	/**
	 * Test the simplest usage
	 */
	final void testGenerateSimpleOutput() {		
		assertEquals(generator.addCode("dummy", false).toString(), "dummy");		
	}
	
	/**
	 * Tests if a method stub can be generated
	 */
	final void testGenerateMethodStub(){
		generator.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_NONE, 
				"public void main(String[] args){")
				.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_RIGHT, "//TODO")
				.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_LEFT, "}", false)
				
		String compare = "public void main(String[] args){"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"//TODO"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			"}"
			
		assertEquals(compare, generator.toString())
	}
	
	/**
	 * Test the indentation feature
	 */
	final void testIndentation(){
		generator.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_NONE, "layer1{")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_RIGHT, "layer2{")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_RIGHT, "layer3{")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_RIGHT, "layer4{")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_NONE, "}")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_LEFT, "}")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_LEFT, "}")
			.addCode(ArtifactCodeGenerator.IndentationDirection.INDENT_LEFT, "}")
			
		String compare = "layer1{"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"layer2{"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"layer3{"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"layer4{"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"}"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"}"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			CodeGeneration.getIndentation(testProject.getJavaProject())+
			"}"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())+
			"}"+
			CodeGeneration.getLineDelimiter(testProject.getJavaProject())
		assertEquals(compare, generator.toString())
			
	}
}