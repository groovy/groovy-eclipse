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
package org.codehaus.groovy.eclipse.core.type;

import junit.framework.TestCase;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.types.impl.ClassLoaderMemberLookup;
import org.codehaus.groovy.eclipse.core.types.impl.MapSymbolTable;

public class TypeEvaluatorTests extends TestCase {
	MapSymbolTable symbolTable;
	IMemberLookup memberLookup;
	private TypeEvaluator eval;
	
	@Override
    protected void setUp() throws Exception {
		super.setUp();
		symbolTable = new MapSymbolTable();
		memberLookup = new ClassLoaderMemberLookup(TypeEvaluatorTests.class.getClassLoader());
		ITypeEvaluationContext context = new TypeEvaluationContextBuilder()
				.symbolTable(symbolTable).memberLookup(memberLookup)
				.classLoader(TypeEvaluatorTests.class.getClassLoader())
				.done();
		eval = new TypeEvaluator(context);
	}
	
	public void testEvalSimple() {
		EvalResult result = eval.evaluate("String");
		assertEquals("java.lang.String", result.getName());
		assertTrue(result.isClass());
	}
	
	public void testEvalStaticProperty() {
		EvalResult result = eval.evaluate("String.class");
		assertEquals("java.lang.String", result.getName());
		assertTrue(result.isClass());
	}
	
	public void testThis() {
		symbolTable.addVariable("this", "java.lang.Object");
		EvalResult result = eval.evaluate("this.toString()");
		assertEquals("java.lang.String", result.getName());
	}
	
	public void testEvalProperty1() {
		symbolTable.addVariable("str", "java.lang.String");
		assertEquals("[B", eval.evaluate("str.bytes").getName());
	}
	
	public void testEvalProperty2() {
		symbolTable.addVariable("str", "java.lang.String");
		assertEquals("java.lang.Class", eval.evaluate("str.bytes.class").getName());
	}
	
	public void testEvalMethod1() {
		symbolTable.addVariable("str", "java.lang.String");
		assertEquals("java.lang.String", eval.evaluate("str.toUpperCase()").getName());
	}
	
	public void testEvalMethod2() {
		symbolTable.addVariable("str", "java.lang.String");
		assertEquals("[B", eval.evaluate("str.toUpperCase().bytes").getName());
	}
	
	public void testEvalMethod3() {
		symbolTable.addVariable("f", "javax.swing.JFrame");
		assertEquals("java.awt.Rectangle", eval.evaluate("f.getBounds()").getName());
	}
	
	public void testEvalMethod4() {
		symbolTable.addVariable("f", "javax.swing.JFrame");
		assertEquals("double", eval.evaluate("f.getBounds().getHeight()").getName());
	}
	
	public void testEvalMethod5() {
		symbolTable.addVariable("str", "java.lang.String");
		assertEquals("int", eval.evaluate("str.toString().compareTo(\"someString\")").getName());
	}
	
	public void testDefaultType() {
		assertEquals("java.lang.Object", eval.evaluate("String.blah").getName());
	}
	
	public void testComment() {
		symbolTable.addVariable("str", "java.lang.String");
		assertEquals("java.lang.String", eval.evaluate("// blah.\nstr.toString()").getName());
	}
	
	public static void main(String[] args) throws Exception {
		TypeEvaluatorTests tests = new TypeEvaluatorTests();
		tests.setUp();
		tests.testEvalMethod3();
	}
}