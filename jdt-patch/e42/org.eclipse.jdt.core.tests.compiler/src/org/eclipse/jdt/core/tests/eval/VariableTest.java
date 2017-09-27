/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.eval;

import junit.framework.Test;

import org.eclipse.jdt.internal.eval.GlobalVariable;
/**
 * Test the global variable evaluation.
 * This assumes that the EvaluationContext class and that the GlobalVariable class
 * are working correctly.
 */
public class VariableTest extends EvaluationTest {
/**
 * Creates a new EvaluationContextTest.
 */
public VariableTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
/**
 * Tests the individual evaluation of variables with expected values of all kind of types.
 */
public void testAllKindOfValues() {
	try {
		// Creates the variables
		GlobalVariable var1 = this.context.newVariable("int".toCharArray(), "var1".toCharArray(), "1".toCharArray());
		GlobalVariable var2 = this.context.newVariable("boolean".toCharArray(), "var2".toCharArray(), "true".toCharArray());
		GlobalVariable var3 = this.context.newVariable("char".toCharArray(), "var3".toCharArray(), "'c'".toCharArray());
		GlobalVariable var4 = this.context.newVariable("float".toCharArray(), "var4".toCharArray(), "(float)1.0".toCharArray());
		GlobalVariable var5 = this.context.newVariable("double".toCharArray(), "var5".toCharArray(), "1.0".toCharArray());
		GlobalVariable var6 = this.context.newVariable("short".toCharArray(), "var6".toCharArray(), "(short)1".toCharArray());
		GlobalVariable var7 = this.context.newVariable("long".toCharArray(), "var7".toCharArray(), "(long)1".toCharArray());
		GlobalVariable var8 = this.context.newVariable("String".toCharArray(), "var8".toCharArray(), "\"hello\"".toCharArray());
		GlobalVariable var9 = this.context.newVariable("Object".toCharArray(), "var9".toCharArray(), buildCharArray(new String[] {
			"new Object() {",
			"	public String toString() {",
			"		return \"an object\";",
			"	}",
			"}"}));
		GlobalVariable var10 = this.context.newVariable("Object".toCharArray(), "var10".toCharArray(), null);

		// Install them
		installVariables(10);

		// Get values one by one
		evaluateWithExpectedValue(var1, "1".toCharArray(), "int".toCharArray());
		evaluateWithExpectedValue(var2, "true".toCharArray(), "boolean".toCharArray());
		evaluateWithExpectedValue(var3, "c".toCharArray(), "char".toCharArray());
		evaluateWithExpectedValue(var4, "1.0".toCharArray(), "float".toCharArray());
		evaluateWithExpectedValue(var5, "1.0".toCharArray(), "double".toCharArray());
		evaluateWithExpectedValue(var6, "1".toCharArray(), "short".toCharArray());
		evaluateWithExpectedValue(var7, "1".toCharArray(), "long".toCharArray());
		evaluateWithExpectedValue(var8, "hello".toCharArray(), "java.lang.String".toCharArray());
		evaluateWithExpectedValue(var9, "an object".toCharArray(), "java.lang.Object".toCharArray());
		evaluateWithExpectedValue(var10, "null".toCharArray(), "java.lang.Object".toCharArray());
	} finally {
		// Clean up
		GlobalVariable[] vars = this.context.allVariables();
		for (int i = 0; i < vars.length; i++) {
			this.context.deleteVariable(vars[i]);
		}
	}
}
public static Class testClass() {
	return VariableTest.class;
}
/**
 * Tests the assignment of a global variable in a code snippet.
 */
public void testCodeSnippetVarAssign() {
	try {
		// Creates the variables
		GlobalVariable var1 = this.context.newVariable("int".toCharArray(), "var1".toCharArray(), null);
		GlobalVariable var2 = this.context.newVariable("boolean".toCharArray(), "var2".toCharArray(), null);
		GlobalVariable var3 = this.context.newVariable("char".toCharArray(), "var3".toCharArray(), null);
		GlobalVariable var4 = this.context.newVariable("float".toCharArray(), "var4".toCharArray(), null);
		GlobalVariable var5 = this.context.newVariable("double".toCharArray(), "var5".toCharArray(), null);
		GlobalVariable var6 = this.context.newVariable("short".toCharArray(), "var6".toCharArray(), null);
		GlobalVariable var7 = this.context.newVariable("long".toCharArray(), "var7".toCharArray(), null);
		GlobalVariable var8 = this.context.newVariable("String".toCharArray(), "var8".toCharArray(), null);
		GlobalVariable var9 = this.context.newVariable("Object".toCharArray(), "var9".toCharArray(), null);
		GlobalVariable var10 = this.context.newVariable("Object".toCharArray(), "var10".toCharArray(), null);

		// Install them
		installVariables(10);

		// Assign each of the variable and get its value
		evaluateWithExpectedDisplayString("var1 = 1;".toCharArray(), "1".toCharArray());
		evaluateWithExpectedValue(var1, "1".toCharArray(), "int".toCharArray());

		evaluateWithExpectedDisplayString("var2 = true;".toCharArray(), "true".toCharArray());
		evaluateWithExpectedValue(var2, "true".toCharArray(), "boolean".toCharArray());

		evaluateWithExpectedDisplayString("var3 = 'c';".toCharArray(), "c".toCharArray());
		evaluateWithExpectedValue(var3, "c".toCharArray(), "char".toCharArray());

		evaluateWithExpectedDisplayString("var4 = (float)1.0;".toCharArray(), "1.0".toCharArray());
		evaluateWithExpectedValue(var4, "1.0".toCharArray(), "float".toCharArray());

		evaluateWithExpectedDisplayString("var5 = 1.0;".toCharArray(), "1.0".toCharArray());
		evaluateWithExpectedValue(var5, "1.0".toCharArray(), "double".toCharArray());

		evaluateWithExpectedDisplayString("var6 = (short)1;".toCharArray(), "1".toCharArray());
		evaluateWithExpectedValue(var6, "1".toCharArray(), "short".toCharArray());

		evaluateWithExpectedDisplayString("var7 = (long)1;".toCharArray(), "1".toCharArray());
		evaluateWithExpectedValue(var7, "1".toCharArray(), "long".toCharArray());

		evaluateWithExpectedDisplayString("var8 = \"hello\";".toCharArray(), "hello".toCharArray());
		evaluateWithExpectedValue(var8, "hello".toCharArray(), "java.lang.String".toCharArray());

		evaluateWithExpectedDisplayString(buildCharArray(new String[] {
			"var9 = new Object() {",
			"	public String toString() {",
			"		return \"an object\";",
			"	}",
			"};"}), "an object".toCharArray());
		evaluateWithExpectedValue(var9, "an object".toCharArray(), "java.lang.Object".toCharArray());

		evaluateWithExpectedDisplayString("var10 = null;".toCharArray(), "null".toCharArray());
		evaluateWithExpectedValue(var10, "null".toCharArray(), "java.lang.Object".toCharArray());
	} finally {
		// Clean up
		GlobalVariable[] vars = this.context.allVariables();
		for (int i = 0; i < vars.length; i++) {
			this.context.deleteVariable(vars[i]);
		}
	}
}
/**
 * Tests the retrieval of a global variable from a code snippet.
 */
public void testCodeSnippetVarRetrieval() {
	try {
		// Creates the variables
		GlobalVariable var1 = this.context.newVariable("int".toCharArray(), "var1".toCharArray(), "1".toCharArray());
		GlobalVariable var2 = this.context.newVariable("boolean".toCharArray(), "var2".toCharArray(), "true".toCharArray());
		GlobalVariable var3 = this.context.newVariable("char".toCharArray(), "var3".toCharArray(), "'c'".toCharArray());
		GlobalVariable var4 = this.context.newVariable("float".toCharArray(), "var4".toCharArray(), "(float)1.0".toCharArray());
		GlobalVariable var5 = this.context.newVariable("double".toCharArray(), "var5".toCharArray(), "1.0".toCharArray());
		GlobalVariable var6 = this.context.newVariable("short".toCharArray(), "var6".toCharArray(), "(short)1".toCharArray());
		GlobalVariable var7 = this.context.newVariable("long".toCharArray(), "var7".toCharArray(), "(long)1".toCharArray());
		GlobalVariable var8 = this.context.newVariable("String".toCharArray(), "var8".toCharArray(), "\"hello\"".toCharArray());
		GlobalVariable var9 = this.context.newVariable("Object".toCharArray(), "var9".toCharArray(), buildCharArray(new String[] {
			"new Object() {",
			"	public String toString() {",
			"		return \"an object\";",
			"	}",
			"}"}));
		GlobalVariable var10 = this.context.newVariable("Object".toCharArray(), "var10".toCharArray(), null);

		// Install them
		installVariables(10);

		// Get values one by one
		evaluateWithExpectedValue(var1, "1".toCharArray(), "int".toCharArray());
		evaluateWithExpectedValue(var2, "true".toCharArray(), "boolean".toCharArray());
		evaluateWithExpectedValue(var3, "c".toCharArray(), "char".toCharArray());
		evaluateWithExpectedValue(var4, "1.0".toCharArray(), "float".toCharArray());
		evaluateWithExpectedValue(var5, "1.0".toCharArray(), "double".toCharArray());
		evaluateWithExpectedValue(var6, "1".toCharArray(), "short".toCharArray());
		evaluateWithExpectedValue(var7, "1".toCharArray(), "long".toCharArray());
		evaluateWithExpectedValue(var8, "hello".toCharArray(), "java.lang.String".toCharArray());
		evaluateWithExpectedValue(var9, "an object".toCharArray(), "java.lang.Object".toCharArray());
		evaluateWithExpectedValue(var10, "null".toCharArray(), "java.lang.Object".toCharArray());
	} finally {
		// Clean up
		GlobalVariable[] vars = this.context.allVariables();
		for (int i = 0; i < vars.length; i++) {
			this.context.deleteVariable(vars[i]);
		}
	}
}
/**
 * Tests variables that include one or more imports.
 */
public void testImports() {
	try {
		// import a package
		this.context.setImports(new char[][] {"java.io.*".toCharArray()});
		GlobalVariable file = this.context.newVariable("boolean".toCharArray(), "file".toCharArray(), "new File(\"!@#%\").exists()".toCharArray());
		installVariables(1);
		evaluateWithExpectedValue(file, "false".toCharArray(), "boolean".toCharArray());
		this.context.deleteVariable(file);

		// import a type
		this.context.setImports(new char[][] {"java.math.BigInteger".toCharArray()});
		GlobalVariable big = this.context.newVariable("BigInteger".toCharArray(), "big".toCharArray(), "new BigInteger(\"123456789012345678901234567890\")".toCharArray());
		installVariables(1);
		evaluateWithExpectedValue(big, "123456789012345678901234567890".toCharArray(), "java.math.BigInteger".toCharArray());
		this.context.deleteVariable(big);

		// import a type and a package
		this.context.setImports(new char[][] {"java.util.Enumeration".toCharArray(), "java.lang.reflect.*".toCharArray()});
		GlobalVariable fields = this.context.newVariable("Field[]".toCharArray(), "fields".toCharArray(), "Enumeration.class.getDeclaredFields()".toCharArray());
		installVariables(1);
		evaluateWithExpectedType("return fields;".toCharArray(), "[Ljava.lang.reflect.Field;".toCharArray());
		this.context.deleteVariable(fields);
	} finally {
		// clean up
		this.context.setImports(new char[0][]);
	}
}
/**
 * Tests the additions and deletion of variables, installing them each time.
 */
public void testSeveralVariableInstallations() {
	try {
		// Creates 6 variables
		GlobalVariable var1 = this.context.newVariable("int".toCharArray(), "var1".toCharArray(), "1".toCharArray());
		GlobalVariable var2 = this.context.newVariable("boolean".toCharArray(), "var2".toCharArray(), "true".toCharArray());
		GlobalVariable var3 = this.context.newVariable("char".toCharArray(), "var3".toCharArray(), "'c'".toCharArray());
		GlobalVariable var4 = this.context.newVariable("float".toCharArray(), "var4".toCharArray(), "(float)1.0".toCharArray());
		GlobalVariable var5 = this.context.newVariable("double".toCharArray(), "var5".toCharArray(), "1.0".toCharArray());
		GlobalVariable var6 = this.context.newVariable("short".toCharArray(), "var6".toCharArray(), "(short)1".toCharArray());

		// Install the variables
		installVariables(6);

		// Get their values
		evaluateWithExpectedValue(var1, "1".toCharArray(), "int".toCharArray());
		evaluateWithExpectedValue(var2, "true".toCharArray(), "boolean".toCharArray());
		evaluateWithExpectedValue(var3, "c".toCharArray(), "char".toCharArray());
		evaluateWithExpectedValue(var4, "1.0".toCharArray(), "float".toCharArray());
		evaluateWithExpectedValue(var5, "1.0".toCharArray(), "double".toCharArray());
		evaluateWithExpectedValue(var6, "1".toCharArray(), "short".toCharArray());

		// Delete 3 variables
		this.context.deleteVariable(var2);
		this.context.deleteVariable(var5);
		this.context.deleteVariable(var6);

		// Install the variables
		installVariables(3);

		// Get their values
		evaluateWithExpectedValue(var1, "1".toCharArray(), "int".toCharArray());
		evaluateWithExpectedValue(var3, "c".toCharArray(), "char".toCharArray());
		evaluateWithExpectedValue(var4, "1.0".toCharArray(), "float".toCharArray());

		// Add 4 more variables
		GlobalVariable var7 = this.context.newVariable("long".toCharArray(), "var7".toCharArray(), "(long)1".toCharArray());
		GlobalVariable var8 = this.context.newVariable("String".toCharArray(), "var8".toCharArray(), "\"hello\"".toCharArray());
		GlobalVariable var9 = this.context.newVariable("Object".toCharArray(), "var9".toCharArray(), buildCharArray(new String[] {
			"new Object() {",
			"	public String toString() {",
			"		return \"an object\";",
			"	}",
			"}"}));
		GlobalVariable var10 = this.context.newVariable("Object".toCharArray(), "var10".toCharArray(), null);

		// Install the variables
		installVariables(7);

		// Change value of a variable using a code snippet and move it
		evaluateWithExpectedValue("var3 = 'z'; return var3;".toCharArray(), "z".toCharArray(), "char".toCharArray());
		this.context.deleteVariable(var3);

		// Change the type of another variable to an incompatible type
		this.context.deleteVariable(var4);
		installVariables(5);

		// Recreate the variables
		var3 = this.context.newVariable(var3.getTypeName(), var3.getName(), var3.getInitializer());
		var4 = this.context.newVariable("java.net.URL".toCharArray(), "var4".toCharArray(), "new java.net.URL(\"http://www.ibm.com/index.html\")".toCharArray());
		installVariables(7);

		// Get their values
		evaluateWithExpectedValue(var1, "1".toCharArray(), "int".toCharArray());
		evaluateWithExpectedValue(var3, "c".toCharArray(), "char".toCharArray());
		evaluateWithExpectedValue(var4, "http://www.ibm.com/index.html".toCharArray(), "java.net.URL".toCharArray());
		evaluateWithExpectedValue(var7, "1".toCharArray(), "long".toCharArray());
		evaluateWithExpectedValue(var8, "hello".toCharArray(), "java.lang.String".toCharArray());
		evaluateWithExpectedValue(var9, "an object".toCharArray(), "java.lang.Object".toCharArray());
		evaluateWithExpectedValue(var10, "null".toCharArray(), "java.lang.Object".toCharArray());
	} finally {
		// Clean up
		GlobalVariable[] vars = this.context.allVariables();
		for (int i = 0; i < vars.length; i++) {
			this.context.deleteVariable(vars[i]);
		}
	}
}
}
