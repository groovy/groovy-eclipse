/*******************************************************************************
 * Copyright (c) 2018 Jesper Steen Møller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.junit.Assert;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JEP286Test extends AbstractRegressionTest {

public static Class testClass() {
	return JEP286Test.class;
}
public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_10);
}

public JEP286Test(String testName){
	super(testName);
}
static {
//	TESTS_NAMES = new String[] { "test0018_project_variable_types" };
}
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_10);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_10);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_10);
	return options;
}
private static final Map<String, String> simpleTypeNames = new HashMap<>();
static {
	simpleTypeNames.put("String", "java.lang.String");
	simpleTypeNames.put("Object", "java.lang.Object");
	simpleTypeNames.put("Bar", "X.Bar");

	simpleTypeNames.put("AnonymousObjectSubclass", "new java.lang.Object(){}");
	simpleTypeNames.put("AnonymousRunnableSubclass", "new java.lang.Runnable(){}");
	simpleTypeNames.put("CollectionOfExtString", "Collection<? extends java.lang.String>");
	simpleTypeNames.put("CollectionOfSuperString", "Collection<? super java.lang.String>");
	simpleTypeNames.put("CollectionAny", "Collection<?>");
	simpleTypeNames.put("ComparableAny", "Comparable<?>");
	simpleTypeNames.put("CollectionExt_ComparableAny", "Collection<? extends Comparable<?>>");
	simpleTypeNames.put("CollectionSuperComparableAny", "Collection<? super Comparable<?>>");
	simpleTypeNames.put("IntLongFloat", "java.lang.Number&Comparable<?>");
	simpleTypeNames.put("ListTestAndSerializable", "List<? extends Z & java.io.Serializable>");
	simpleTypeNames.put("TestAndSerializable", "Z & java.io.Serializable");
}

static void assertInferredType(LocalDeclaration varDecl) {
	String varName = new String(varDecl.name);
	int underscoreIndex = varName.indexOf('_');
	Assert.assertNotEquals(-1, underscoreIndex);
	String typeNamePart = varName.substring(underscoreIndex+1);
	typeNamePart = typeNamePart.replaceAll("ARRAY", "[]"); // So that we assume that x_intARRAY is of type int[]
	String expectedTypeName = simpleTypeNames.getOrDefault(typeNamePart, typeNamePart);
	String actualTypeName = varDecl.binding.type.debugName();
	// System.out.println("For " + varName + ", we expect " + expectedTypeName + ", the type was: "
	// + actualTypeName + " - " + (expectedTypeName.equals(actualTypeName) ? "❤️" : "🤡"));
	Assert.assertEquals("Type of variable " + varName, expectedTypeName, actualTypeName);
}

// This visitor visits the 'testXxx' method in the visited classes, checking for expected types of local variables (using the debug name)
private final static class InferredTypeVerifier extends ASTVisitor {
		public int localsChecked = 0;
		public InferredTypeVerifier() { }

		@Override
		public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
			return false; // Don't check Foo itself
		}

		@Override
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			if (! new String(methodDeclaration.selector).startsWith("test")) return false;
			return super.visit(methodDeclaration, scope);
		}

		@Override
		public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
			assertInferredType(localDeclaration);
			this.localsChecked++;
			return super.visit(localDeclaration, scope);
		}
	}

public void test0001_local_variable_inference() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        var x = \"SUCCESS\";\n" +
				"        System.out.println(x);\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS");
}
public void test0002_inferred_for() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"		int sum = 0;\n" +	
				"		for(var n = 1; n <= 2; ++n) {\n" +
				"			sum += n;\n" +	
				"       }\n" +
				"		System.out.println(\"SUCCESS \" + sum);\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS 3");
}
public void test0003_inferred_enhanced_for() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"		int sum = 0;\n" +	
				"		for(var n : java.util.List.of(1, 2)) {\n" +
				"			sum += n;\n" +	
				"       }\n" +
				"		System.out.println(\"SUCCESS \" + sum);\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS 3");
}
public void test0004_try_with_resources() throws IOException {
	try(java.io.Writer w = new java.io.StringWriter()) {
		w.write("SUCCESS!\n");
		System.out.println(w.toString());
	}
	
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) throws Exception {\n" +
				"		try(var w = new java.io.StringWriter()) {\n" +	
				"			w.write(\"SUCCESS\\n\");" +
				"			System.out.println(w.toString());\n" +
				"       }\n" +
				"    }\n" +
				"}\n"
			},
			"SUCCESS");
}
public void test0005_no_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] argv) {\n" + 
				"		var a;\n" + 
				"		for(var b;;);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a;\n" +
			"	    ^\n" +
			"Cannot use 'var' on variable without initializer\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	for(var b;;);\n" +
			"	        ^\n" +
			"Cannot use 'var' on variable without initializer\n" +
			"----------\n");
}
public void test0006_multiple_declarators() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static void main(String[] argv) {\n" + 
				"		var a = 1, b = 2;\n" + 
				"		for(var c = 1, d = 20; c<d; c++);\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = 1, b = 2;\n" +
			"	           ^\n" +
			"'var' is not allowed in a compound declaration\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	for(var c = 1, d = 20; c<d; c++);\n" + 
			"	               ^\n" +
			"'var' is not allowed in a compound declaration\n" +
			"----------\n");
}
public void test0007_var_in_wrong_place() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	private var someField = 0;\n" +
				"	public var method() {\n" + 
				"		return null;\n" +
				"	}\n" + 
				"	public void main(var arg) {\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	private var someField = 0;\n" +
			"	        ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	public var method() {\n" + 
			"	       ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 6)\n" +
			"	public void main(var arg) {\n" + 
			"	                 ^^^\n" +
			"'var' is not allowed here\n" +
			"----------\n");
}
public void test0008_null_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String[] arg) {\n" +
				"		var notMuch = null;\n" +	
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var notMuch = null;\n" +
			"	    ^^^^^^^\n" +
			"Cannot infer type for local variable initialized to 'null'\n" +
			"----------\n");
}
public void test0008_void_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void foo() {\n" +
				"	}\n" +
				"\n" +
				"	public void baz() {\n" +
				"		var nothingHere = foo();\n" +	
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" + 
			"	var nothingHere = foo();\n" + 
			"	    ^^^^^^^^^^^\n" + 
			"Variable initializer is 'void' -- cannot infer variable type\n" + 
			"----------\n");
}
public void test0009_var_as_type_name() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public enum var { V, A, R };\n" +	
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public enum var { V, A, R };\n" +
			"	            ^^^\n" +
			"'var' is not a valid type name\n" +
			"----------\n");
}
public void test0010_array_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var myArray = { 1, 2, 3 };\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var myArray = { 1, 2, 3 };\n" +
			"	    ^^^^^^^\n" +
			"Array initializer needs an explicit target-type\n" +
			"----------\n");
}
public void test0011_array_type() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var myArray[] = new int[42];\n" +
				"		var[] moreArray = new int[1337];\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var myArray[] = new int[42];\n" +
			"	    ^^^^^^^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	var[] moreArray = new int[1337];\n" +
			"	      ^^^^^^^^^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}
public void test0012_self_reference() throws IOException {
	
	// BTW: This will give a VerifyError: int a = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : a)).call();
	// The cases are tested. a is a simple case, with plain usage in the same scope. b is used in a nested scope.
	// c and d are shadowed by the nested definitions.
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = 42 + a;\n" +
				"		var b = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : b)).call();\n" +
				"       var c = new java.util.concurrent.Callable<Integer>() {\n" + 
				"           public Integer call() {\n" + 
				"               int c = 42; return c;\n" + 
				"           }\n" + 
				"       }.call();" +
				"       var d = new java.util.concurrent.Callable<Integer>() {\n" +
				"           int d = 42;\n" +
				"           public Integer call() {\n" + 
				"               return d;\n" + 
				"           }\n" + 
				"       }.call();" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = 42 + a;\n" +
			"	    ^\n" +
			"Declaration using 'var' may not contain references to itself\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	var b = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : b)).call();\n" +
			"	    ^\n" +
			"Declaration using 'var' may not contain references to itself\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 7)\n" +
			"	int c = 42; return c;\n" +
			"	    ^\n" +
		    "The local variable c is hiding another local variable defined in an enclosing scope\n" +
		    	"----------\n"+
			"3. WARNING in X.java (at line 10)\n" +
			"	int d = 42;\n" +
			"	    ^\n" +
		    "The field new Callable<Integer>(){}.d is hiding another local variable defined in an enclosing scope\n" +
		    	"----------\n");
}
public void test0013_lambda() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = (int i) -> 42;\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = (int i) -> 42;\n" +
			"	    ^\n" +
			"Lambda expression needs an explicit target-type\n" +
			"----------\n");
}
public void test0014_method_reference() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = X::main;\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = X::main;\n" +
			"	    ^\n" +
			"Method reference needs an explicit target-type\n" +
			"----------\n");
}
public void test0015_complain_over_first_poly_encountered() throws Exception {
	
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public void main(String [] args) {\n" +
				"		var a = args.length > 1 ? X::main : (int i) -> 42;\n" +
				"	}\n" + 
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	var a = args.length > 1 ? X::main : (int i) -> 42;\n" +
			"	    ^\n" +
			"Method reference needs an explicit target-type\n" +
			"----------\n");
}
public void test0016_dont_capture_deep_poly_expressions() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String [] args) throws Exception {\n" +
				"		var z = ((java.util.concurrent.Callable<String>)(() -> \"SUCCESS\"));\n" + 		
				"		var x = args.length > 1 ? \"FAILURE\" : z.call();\n" +
				"		System.out.println(x);\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS");
}
//static <T extends List<? super E>, E extends List<? super Integer>> void doSomething(T[] e) {
//	e[0] = null;
//}

public void test0017_simple_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.List;\n" + 
				"\n" + 
				"public class X {\n" + 
				"    void test() {\n" + 
				"        var i_String = \"\";\n" + 
				"        for (var i2_String = \"\" ; ; ) { break; }\n" + 
				"        for (var i2_String : iterableOfString()) { break; }\n" + 
				"        for (var i2_String : arrayOfString()) { break; }\n" + 
				"        try (var i2_Bar = new Bar()) { } finally { }\n" + 
				"        try (var i2_Bar = new Bar(); var i3_Bar = new Bar()) { } finally { }\n" + 
				"    }\n" + 
				"\n" + 
				"    Iterable<String> iterableOfString() { return null; }\n" + 
				"    String[] arrayOfString() { return null; }\n" + 
				"\n" + 
				"    static class Bar implements AutoCloseable {\n" + 
				"        @Override\n" + 
				"        public void close() { }\n" + 
				"    }\n" + 
				"}\n" + 
				"\n"
			},
			typeVerifier);
	Assert.assertEquals(7, typeVerifier.localsChecked);
}
public void test0018_primitive_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"Y.java",
				"class Y {\n" + 
				"    boolean[] booleanArray = { true };\n" + 
				"    byte[] byteArray = { 1 };\n" + 
				"    char[] charArray = { 'c' };\n" + 
				"    short[] shortArray = { 42 };\n" + 
				"    int[] intArray = { 42 };\n" + 
				"    long[] longArray = { 42L };\n" + 
				"    float[] floatArray = { 0.1f };\n" + 
				"    double[] doubleArray = { 0.2d };\n" + 
				"\n" + 
				"    void testBuiltins() {\n" + 
				"        var z_boolean = false;\n" + 
				"        var b_byte = (byte)0xff;\n" + 
				"        var c_char = 'c';\n" + 
				"        var s_short = (short)42;\n" + 
				"        var i_int = 42;\n" + 
				"        var l_long = 42L;\n" + 
				"        var f_float = 0.25f;\n" + 
				"        var d_double = 0.35d;\n" + 
				"    }\n" + 
				"\n" + 
				"    void testBuiltinsForEach() {\n" + 
				"        for (var z_boolean : booleanArray) { System.out.print(\".\"); }\n" + 
				"        for (var b_byte : byteArray) { System.out.print(\".\"); }\n" + 
				"        for (var c_char : charArray) { System.out.print(\".\"); }\n" + 
				"        for (var s_short : shortArray) { System.out.print(\".\"); }\n" + 
				"        for (var i_int : intArray) { System.out.print(\".\"); }\n" + 
				"        for (var l_long : longArray) { System.out.print(\".\"); }\n" + 
				"        for (var f_float : floatArray) { System.out.print(\".\"); }\n" + 
				"        for (var d_double : doubleArray) { System.out.print(\".\"); }\n" + 
				"    }\n" + 
				"    void testBuiltinsArray() {\n" + 
				"        var z_booleanARRAY = booleanArray;\n" + 
				"        var b_byteARRAY = byteArray;\n" + 
				"        var c_charARRAY = charArray;\n" + 
				"        var s_shortARRAY = shortArray;\n" + 
				"        var i_intARRAY = intArray;\n" + 
				"        var l_longARRAY = longArray;\n" + 
				"        var f_floatARRAY = floatArray;\n" + 
				"        var d_doubleARRAY = doubleArray;\n" + 
				"    }\n" + 
				"\n" + 
				"}\n"
			},
			typeVerifier);
	Assert.assertEquals(24, typeVerifier.localsChecked);
}
public void test0018_project_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"Z.java",
				"import java.util.Collection;\n" + 
				"import java.util.List;\n" + 
				"import java.io.Serializable;\n" + 
				"\n" + 
				"class Z {\n" + 
				"\n" + 
				"    void testExtends() {\n" + 
				"        var l1_CollectionOfExtString = extendsString();\n" + 
				"        for (var l2_CollectionOfExtString = extendsString() ; ; ) { break; }\n" + 
				"        for (var l3_CollectionOfExtString : extendsStringArr()) { break; }\n" + 
				"        for (var l4_CollectionOfExtString : extendsCollectionIterable()) { break; }\n" + 
				"        for (var l5_String : extendsString()) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testExtendsFbound() { \n" + 
				"        var l1_CollectionExt_ComparableAny = extendsTBound();\n" + 
				"        for (var l2_CollectionExt_ComparableAny = extendsTBound() ; ; ) { break; }\n" + 
				"        for (var l3_CollectionExt_ComparableAny : extendsTBoundArray()) { break; }\n" + 
				"        for (var l3_CollectionExt_ComparableAny : extendsTBoundIter()) { break; }\n" + 
				"        for (var l4_ComparableAny : extendsTBound()) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testSuperTBound() {\n" + 
				"        var s_CollectionAny = superTBound();\n" + 
				"        for (var s2_CollectionAny = superTBound() ; ; ) { break; }\n" + 
				"        for (var s2_CollectionAny : superTBoundArray()) { break; }\n" + 
				"        for (var s2_CollectionAny : superTBoundIter()) { break; }\n" + 
				"        for (var s2_Object : superTBound()) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testCollectSuper() {\n" + 
				"        var s_CollectionOfSuperString = superString();\n" + 
				"        for (var s2_CollectionOfSuperString = superString() ; ; ) { break; }\n" + 
				"        for (var s2_CollectionOfSuperString : superStringArray()) { break; }\n" + 
				"        for (var s2_CollectionOfSuperString : superCollectionIterable()) { break; }\n" + 
				"        for (var s2_Object : superString()) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testUnbound() {\n" + 
				"        var s_CollectionAny = unboundedString();\n" + 
				"        for (var s2_CollectionAny = unboundedString() ; ; ) { break; }\n" + 
				"        for (var s2_CollectionAny : unboundedStringArray()) { break; }\n" + 
				"        for (var s2_CollectionAny : unboundedCollectionIterable()) { break; }\n" + 
				"        for (var s2_Object : unboundedString()) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testTypeOfAnAnonymousClass() {\n" + 
				"        var o_AnonymousObjectSubclass = new Object() { };\n" + 
				"        for (var s2_AnonymousObjectSubclass = new Object() { } ; ; ) { break; }\n" + 
				"        for (var s2_AnonymousObjectSubclass : arrayOf(new Object() { })) { break; }\n" + 
				"        for (var s2_AnonymousObjectSubclass : listOf(new Object() { })) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testTypeOfAnAnonymousInterface() {\n" + 
				"        var r_AnonymousRunnableSubclass = new Runnable() { public void run() { } };\n" + 
				"        for (var s2_AnonymousRunnableSubclass = new Runnable() { public void run() { } } ; ; ) { break; }\n" + 
				"        for (var s2_AnonymousRunnableSubclass : arrayOf(new Runnable() { public void run() { } })) { break; }\n" + 
				"        for (var s2_AnonymousRunnableSubclass : listOf(new Runnable() { public void run() { } })) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    void testTypeOfIntersectionType() {\n" + 
				"        var c_IntLongFloat = choose(1, 1L);\n" + 
				"        for (var s2_IntLongFloat = choose(1, 1L) ; ;) { break; }\n" + 
				"        for (var s2_IntLongFloat : arrayOf(choose(1, 1L))) { break; }\n" + 
				"        for (var s2_IntLongFloat : listOf(choose(1, 1L))) { break; }\n" + 
				"    }\n" + 
				"\n" + 
				"    public void testProjections() {\n" + 
				"        var inter_ListTestAndSerializable = getIntersections();\n" + 
				"        var r_TestAndSerializable = inter_ListTestAndSerializable.get(0);\n" + 
				"    }\n" + 
				"\n" + 
				"    Collection<? extends String> extendsString() { return null; }\n" + 
				"    Collection<? super String> superString() { return null; }\n" + 
				"    Collection<?> unboundedString() { return null; }\n" + 
				"\n" + 
				"    Collection<? extends String>[] extendsStringArr() { return null; }\n" + 
				"    Collection<? super String>[] superStringArray() { return null; }\n" + 
				"    Collection<?>[] unboundedStringArray() { return null; }\n" + 
				"\n" + 
				"    Iterable<? extends Collection<? extends String>> extendsCollectionIterable() { return null; }\n" + 
				"    Iterable<? extends Collection<? super String>> superCollectionIterable() { return null; }\n" + 
				"    Iterable<? extends Collection<?>> unboundedCollectionIterable() { return null; }\n" + 
				"\n" + 
				"    <TBound extends Comparable<TBound>> Collection<? extends TBound> extendsTBound() { return null; }\n" + 
				"    <TBound extends Comparable<TBound>> Collection<? super TBound> superTBound() { return null; }\n" + 
				"\n" + 
				"    <TBound extends Comparable<TBound>> Collection<? extends TBound>[] extendsTBoundArray() { return null; }\n" + 
				"    <TBound extends Comparable<TBound>> Collection<? super TBound>[] superTBoundArray() { return null; }\n" + 
				"\n" + 
				"    <TBound extends Comparable<TBound>> Iterable<? extends Collection<? extends TBound>> extendsTBoundIter() { return null; }\n" + 
				"    <TBound extends Comparable<TBound>> Iterable<? extends Collection<? super TBound>> superTBoundIter() { return null; }\n" + 
				"\n" + 
				"    <TBound> Collection<TBound> listOf(TBound b) { return null; }\n" + 
				"    <TBound> TBound[] arrayOf(TBound b) { return null; }\n" + 
				"\n" + 
				"    <TBound> TBound choose(TBound b1, TBound b2) { return b1; }\n" +
				"    <T extends Z & Serializable> List<? extends T> getIntersections() {\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}"
			},
			typeVerifier);
	Assert.assertEquals(39, typeVerifier.localsChecked);
}
public void testBug531832() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        for (var[] v : args) { }\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for (var[] v : args) { }\n" +
			"	           ^\n" +
			"'var' is not allowed as an element type of an array\n" +
			"----------\n");
}
public void testBug530879() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void foo() { }\n" +
				"    public static void main(String [] args) {\n" +
				"        for (var v : foo()) { }\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	for (var v : foo()) { }\n" +
			"	         ^\n" +
			"Variable initializer is 'void' -- cannot infer variable type\n" +
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	for (var v : foo()) { }\n" + 
			"	             ^^^^^\n" + 
			"Can only iterate over an array or an instance of java.lang.Iterable\n" + 
			"----------\n");
}
public void testBug530879a() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        for (var v : null) { }\n" +
				"    }\n" +
				"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	for (var v : null) { }\n" +
			"	         ^\n" +
			"Cannot infer type for local variable initialized to 'null'\n" +
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	for (var v : null) { }\n" + 
			"	             ^^^^\n" + 
			"Can only iterate over an array or an instance of java.lang.Iterable\n" + 
			"----------\n");
}
public void testBug532349() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo(Boolean p) {\n" + 
			"		Y<? super Boolean> y = new Y<>();\n" + 
			"		var v = y;\n" + 
			"		Y<? super Boolean> tmp = v;\n" + 
			"	}\n" + 
			"}\n" + 
			"class Y<T extends Boolean> {\n" + 
			"}"
		});
}
public void testBug532349a() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"public class X {\n" + 
			"	public static void foo(Boolean p) {\n" + 
			"		List<Y<? super Boolean>> l = new ArrayList<>();\n" + 
			"		var dlv = l;\n" + 
			"		for (var iv : dlv) {\n" + 
			"			Y<? super Boolean> id = iv;\n" + 
			"		}" +
			"	}\n" + 
			"}\n" + 
			"class Y<T extends Boolean> {}"
		});
}
public void testBug532349b() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	public static void foo(Boolean p) {\n" + 
			"		Y<? super Boolean> y = new Y<>();\n" + 
			"		try (var v = y) {\n" + 
			"			Y<? super Boolean> tmp = v;\n" +
			"		} catch (Exception e) { }\n" +
			"	}\n" + 
			"}\n" + 
			"class Y<T extends Boolean> implements AutoCloseable {\n" +
			"	@Override\n" + 
			"	public void close() throws Exception {}\n" +
			"}"
		});
}
public void testBug532351() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  public static void foo(Boolean p) {\n" + 
			"    Y<? super Number> y = new Y<Number>(); // Javac reports, ECJ accepts\n" + 
			"    var v = y;\n" + 
			"    Y<? super Number> tmp = v;\n" + 
			"  }\n" + 
			"  class Y<T extends Number> {\n" + 
			"  }\n" + 
			"}"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	Y<? super Number> y = new Y<Number>(); // Javac reports, ECJ accepts\n" +
		"	                      ^^^^^^^^^^^^^^^\n" +
		"No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).\n" +
		"----------\n");
}
public void testBug531025() {
	runNegativeTest(
		new String[] {
			"a/Ann.java",
			"package a;\n" +
			"public @interface Ann {}\n",
			"a/AnnM.java",
			"package a;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.METHOD)\n" +
			"public @interface AnnM {}\n",
			"a/AnnD.java",
			"package a;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.LOCAL_VARIABLE)\n" +
			"public @interface AnnD {}\n",
			"a/AnnT.java",
			"package a;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"public @interface AnnT {}\n",
			"a/AnnDT.java",
			"package a;\n" +
			"import java.lang.annotation.*;\n" +
			"@Target({ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})\n" +
			"public @interface AnnDT {}\n",
			"X.java",
			"import a.*;\n" +
			"import java.util.*;\n" +
			"public class X {\n" +
			"	void test(List<String> strings) {\n" +
			"		@Ann   var v  = strings;\n" +
			"		@AnnM  var vm = strings;\n" +
			"		@AnnD  var vd = strings;\n" +
			"		@AnnT  var vt = \"\";\n" +
			"		@AnnDT var vdt = this;\n" +
			"		for (@AnnD var fvd : strings) {}\n" +
			"		for (@AnnT var fvt : strings) {}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	@AnnM  var vm = strings;\n" + 
		"	^^^^^\n" + 
		"The annotation @AnnM is disallowed for this location\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	@AnnT  var vt = \"\";\n" + 
		"	^^^^^\n" + 
		"The annotation @AnnT is disallowed for this location\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 11)\n" + 
		"	for (@AnnT var fvt : strings) {}\n" + 
		"	     ^^^^^\n" + 
		"The annotation @AnnT is disallowed for this location\n" + 
		"----------\n");
}
public void testBug532349_001() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<? extends Number> y = new Y<>();\n" + 
			"		var v = y.t;\n" + 
			"		Integer dsbType0 = v;\n" + 
			"	}\n" + 
			"}\n" + 
			"class Y<T extends Integer> {\n" + 
			"	public T t;\n" + 
			"}"
		});
}
public void testBug532349_002() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<? extends I> y = new Y<>();\n" + 
			"		var v = y.t;\n" + 
			"		Integer dsbType0 = v;\n" + 
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"class Y<T extends Integer> {\n" + 
			"	public T t;\n" + 
			"}"
		});
}
public void testBug532349_003() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static void foo(Y<? extends I> y) {\n" + 
			"		var v = y.t;\n" + 
			"		Integer dsbType0 = v;\n" +
			"		I i = v;\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"class Y<T extends Integer> {\n" + 
			"	public T t;\n" + 
			"}"
		});
}
public void testBug532349_004() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<? extends Integer> y = new Y<>();\n" + 
			"		var v = y.t;\n" + 
			"		Integer dsbType0 = v;\n" +
			"		Serializable s = v;\n" +
			"	}\n" + 
			"}\n" + 
			"class Y<T extends Number&Serializable> {\n" + 
			"	public T t;\n" + 
			"}"
		});
}
public void testBug532349_005() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<?> y = new Y<>();\n" + 
			"		var v = y.t;\n" + 
			"		I i = v;\n" +
			"		Serializable s = v;\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"class Y<T extends I&Serializable> {\n" + 
			"	public T t;\n" + 
			"}"
		});
}
public void testBug532349_006() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<? extends I> y = new Y<>();\n" + 
			"		var v = y.t;\n" + 
			"		I i = v;\n" +
			"		Serializable s = v;\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"class Y<T extends Serializable> {\n" + 
			"	public T t;\n" + 
			"}",
		});
}
public void testBug532349_007() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Z<? extends I> z = new Z<>();\n" + 
			"		var v = z.t;\n" + 
			"		X x = v.t;\n" +
			"		v.doSomething();\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { void doSomething();}\n" +
			"class Z<T extends Y<?>> {\n" + 
			"	public T t;\n" + 
			"}\n" +
			"class Y<T extends X> {\n" + 
			"	public T t;\n" + 
			"}",
		});
}
public void testBug532349_008() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Z<? extends Y<? extends C>> z = new Z<>();\n" + 
			"		var v = z.t;\n" + 
			"		C c = v.t;\n" +
			"		v.doSomething();\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { void doSomething();}\n" +
			"class C extends X{ }\n" +
			"class Z<T extends I> {\n" + 
			"	public T t;\n" + 
			"}\n" +
			"class Y<T extends X> {\n" + 
			"	public T t;\n" + 
			"}",
		});
}
public void testBug532349_009() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<? super J> y = new Y<>();\n" + 
			"		var v = y.t;\n" + 
			"		I i = v;\n" +
			"		Serializable s = v;\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"interface J extends I{}" +
			"class Y<T extends I> {\n" + 
			"	public T t;\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	Serializable s = v;\n" + 
		"	                 ^\n" + 
		"Type mismatch: cannot convert from I to Serializable\n" + 
		"----------\n");
}
public void testBug532349_010() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.Serializable;\n" +
			"class X {\n" + 
			"	public static void foo(C<?> c) {\n" + 
			"		var v = c.t;\n" + 
			"		v = (I&Serializable) new D();\n" +
			"		v.doSomething();\n" +
			"	}\n" + 
			"}\n" + 
			"interface I { void doSomething();}\n" +
			"class C<T extends I&Serializable>{ T t;}\n" +
			"class D implements I, Serializable { public void doSomething() {} }\n"
		});
}
public void testBug532349_11() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	static <R extends D<? extends Y>> W<? extends R> boo() {\n" + 
			"		return null;\n" + 
			"	}\n" +
			"	public static void foo() {\n" + 
			"		var v = boo();\n" + 
			"		var var = v.t;\n" +
			"		Y y = var.r;\n" +
			"	}\n" + 
			"}\n" + 
			"class Y extends X { }\n" +
			"class D<R extends X>{ R r;}\n" +
			"class W<T extends D<?>> { T t; }\n"
		});
}
public void testBug532349_12() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	public static void foo(D<?> d) {\n" + 
			"		var v = d;\n" + 
			"		D<? extends Y> dy = v;\n" +
			"		D<? extends X> dx = v;\n" +
			"	}\n" + 
			"}\n" + 
			"class Y extends X{ }\n" +
			"class D<R extends Y>{ R r;}\n"
		});
}
public void testBug532349_13() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	public static void foo(D<Y<? extends Integer>> d) {\n" + 
			"		var v = d.r;\n" + 
			"		Y<? extends Number> yn = v;\n" +
			"		Y<? extends Integer> yi = v;\n" +
			"	}\n" + 
			"}\n" + 
			"class Y<T extends Integer>{ }\n" +
			"class D<R extends Y<? extends Number>>{ R r;}\n"
		});
}
public void testBug532349_14() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X {\n" +
			"	public static void foo(A<? super C> ac) {\n" + 
			"		C c = new C(100);\n" + 
			"		var c1 = ac;\n" + 
			"		A<? super C> a1 = c1;\n" + 
			"		A<? super C> a2 = new A<B>(new B());\n" +
			"		a2 = c1;\n" +
			"	}\n" + 
			"}\n" + 
			"class C<T> extends B{\n" + 
			"	T t;\n" + 
			"	C(T t) {\n" + 
			"		this.t = t;\n" + 
			"	}\n" + 
			"}\n" + 
			"class B { }\n" + 
			"class A<Q> {\n" + 
			"	A(Q e) {}\n" + 
			"}"
		});
}
public void testBug532349_15() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	    public static <T> A<T> m(T t) {\n" + 
			"        return new A(t);\n" + 
			"    }\n" + 
			"    public static <U extends I1<?>> A<? extends U> m2(A<? super U> u) {\n" + 
			"        return new A(u);\n" + 
			"    }\n" + 
			"    public static void main(String argv[]) {\n" + 
			"        A<?> checkValue1 = new C(10);\n" + 
			"        var varValue = m2(m(checkValue1));\n" + 
			"        if(!varValue.t.t.equals(10)) {\n" + 
			"            System.out.println(\"Error:\");\n" + 
			"        }\n" + 
			"        if(varValue.t.methodOnI1() != true) {\n" + 
			"            System.out.println(\"Error:\");\n" + 
			"        }\n" + 
			"    }" + 
			"}\n" + 
			"class A<E> {\n" + 
			"    E t;\n" + 
			"    A(E t) {\n" + 
			"        this.t = t;\n" + 
			"    }\n" + 
			"    A<E> u;\n" + 
			"    A (A<E> u) {\n" + 
			"        this(u.t);\n" + 
			"        this.u = u;\n" + 
			"    }\n" + 
			"}\n" + 
			"interface I1<E> {\n" + 
			"    default boolean methodOnI1() {\n" + 
			"        return true;\n" + 
			"    }\n" + 
			"}\n" +
			"class C<T> extends A implements I1 {\n" + 
			"    C(T t) {\n" + 
			"        super(t);\n" + 
			"    }\n" + 
			"}"
		}, "");
}
public void testBug532349_0016() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static void foo() {\n" + 
			"		Y<? extends I> yi = new Y<>();\n" +
			"		var vi = yi.t;\n" +
			"		Y<Integer> yj = new Y<>();\n" +
			"		vi = yj.t;\n" + 
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"class Y<T extends Number> {\n" + 
			"	public T t;\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	vi = yj.t;\n" + 
		"	     ^^^^\n" + 
		"Type mismatch: cannot convert from Integer to Number & I\n" + 
		"----------\n");
}
public void testBug532349_0017() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"class X {\n" + 
			"	public static <Q extends Number & I> void foo(Y<? super Q> y) {\n" + 
			"		var vy = y;\n" +
			"		Y<Integer> yi = new Y<>();\n" +
			"		vy = yi;\n" + 
			"	}\n" + 
			"}\n" + 
			"interface I { }\n" +
			"class Y<T extends Number> {\n" + 
			"	public T t;\n" + 
			"}"
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	vy = yi;\n" + 
		"	     ^^\n" + 
		"Type mismatch: cannot convert from Y<Integer> to Y<? super Q>\n" + 
		"----------\n");
}
}
