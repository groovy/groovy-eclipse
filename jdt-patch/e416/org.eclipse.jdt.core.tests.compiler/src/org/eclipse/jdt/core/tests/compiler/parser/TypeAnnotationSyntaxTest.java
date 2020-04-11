/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TypeAnnotationSyntaxTest extends AbstractSyntaxTreeTest {

	private static String  jsr308TestScratchArea = "c:\\Jsr308TestScratchArea";
	private static String referenceCompiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe";

	static {
//		TESTS_NAMES = new String [] { "test0137" };
	}
	public static Class testClass() {
		return TypeAnnotationSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	static final class LocationPrinterVisitor extends ASTVisitor {
		TypeReference enclosingReference;
		Map locations;

		public LocationPrinterVisitor() {
			this.locations = new HashMap();
		}

		public Map getLocations() {
			return this.locations;
		}
		public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
			this.enclosingReference = fieldDeclaration.type;
			return true;
		}
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			TypeReference returnType = methodDeclaration.returnType;
			if (returnType != null) {
				this.enclosingReference = returnType;
				returnType.traverse(this, scope);
			}
			if (methodDeclaration.thrownExceptions != null) {
				int thrownExceptionsLength = methodDeclaration.thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++) {
					TypeReference typeReference = methodDeclaration.thrownExceptions[i];
					this.enclosingReference = typeReference;
					typeReference.traverse(this, scope);
				}
			}
			return false;
		}
		public boolean visit(Argument argument, ClassScope scope) {
			this.enclosingReference = argument.type;
			return true;
		}
		public boolean visit(Argument argument, BlockScope scope) {
			this.enclosingReference = argument.type;
			return true;
		}
		public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, annotation));
			}
			return false;
		}
		public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, annotation));
			}
			return false;
		}
		public boolean visit(NormalAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, annotation));
			}
			return false;
		}
		public void storeLocations(Annotation annotation, int[] tab) {
			String key = String.valueOf(annotation);
			if (this.locations.get(key) != null) {
				return;
			}
			if (tab == null) {
				this.locations.put(key, null);
				return;
			}

			StringBuffer buffer = new StringBuffer("[");
			for (int i = 0, max = tab.length; i < max; i += 2) {
				if (i > 0) {
					buffer.append(", ");
				}
				switch (tab[i]) {
				case 0:
					buffer.append("ARRAY");
					break;
				case 1:
					buffer.append("INNER_TYPE");
					break;
				case 2:
					buffer.append("WILDCARD");
					break;
				case 3:
					buffer.append("TYPE_ARGUMENT(").append(tab[i+1]).append(')');
					break;
				}
			}
			buffer.append(']');
			this.locations.put(key, String.valueOf(buffer));
		}

		public boolean visit(ArrayTypeReference arrayReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
		public boolean visit(ParameterizedSingleTypeReference typeReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
		public boolean visit(SingleTypeReference typeReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
	}
public TypeAnnotationSyntaxTest(String testName){
	super(testName, referenceCompiler, jsr308TestScratchArea);
	if (referenceCompiler != null) {
		File f = new File(jsr308TestScratchArea);
		if (!f.exists()) {
			f.mkdir();
		}
		if (f.exists()) {
			try {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Marker.java")));
				w.write("@interface Marker {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Normal.java")));
				w.write("@interface Normal {\n\tint value() default 10;\n}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "SingleMember.java")));
				w.write("@interface SingleMember {\n\tint value() default 10;\n}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Positive.java")));
				w.write("@interface Positive {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Negative.java")));
				w.write("@interface Negative{}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Readonly.java")));
				w.write("@interface Readonly {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "NonNull.java")));
				w.write("@interface NonNull {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "HashMap.java")));
				w.write("class HashMap<X,Y> {\n class Iterator {}; \n}\n".toCharArray());
				w.close();
				CHECK_ALL |= CHECK_JAVAC_PARSER;
			} catch (IOException e) {
				// ignore
			}
		}
	}
}

static {
//	TESTS_NAMES = new String[] { "test0038", "test0039", "test0040a" };
//	TESTS_NUMBERS = new int[] { 133, 134, 135 };
	if (!(new File(referenceCompiler).exists())) {
		referenceCompiler = null;
		jsr308TestScratchArea = null;
	}
}
void traverse (File f) throws IOException {
	if (f.isDirectory()) {
		File [] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			traverse(files[i]);
		}
	} else {
		if (f.getName().endsWith(".java")) {
			System.out.println(f.getCanonicalPath());
			char [] contents = new char[(int) f.length()];
			FileInputStream fs = new FileInputStream(f);
			InputStreamReader isr = null;
			try {
				isr = new InputStreamReader(fs);
			} finally {
				if (isr != null) isr.close();
			}
			isr.read(contents);
			checkParse(contents, null, f.getCanonicalPath(), null);
		}
	}
}
public void _test000() throws IOException {
	traverse(new File("C:\\jsr308tests"));
}

public void test0001() throws IOException {
	String source = "@Marker class A extends String {}\n;" +
					"@Marker class B extends @Marker String {}\n" +
					"@Marker class C extends @Marker @SingleMember(0) String {}\n" +
					"@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}\n" +
					"@Marker class E extends String {}\n;";

	String expectedUnitToString =
		"@Marker class A extends String {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class B extends @Marker String {\n" +
		"  B() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class C extends @Marker @SingleMember(0) String {\n" +
		"  C() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {\n" +
		"  D() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class E extends String {\n" +
		"  E() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
}
public void test0002() throws IOException {
	String source = "class A extends String {}\n;" +
					"class B extends @Marker String {}\n" +
					"class C extends @Marker @SingleMember(0) String {}\n" +
					"class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}\n" +
					"class E extends String {}\n;";

	String expectedUnitToString =
		"class A extends String {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"class B extends @Marker String {\n" +
		"  B() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"class C extends @Marker @SingleMember(0) String {\n" +
		"  C() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {\n" +
		"  D() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"class E extends String {\n" +
		"  E() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0002", expectedUnitToString);
}
public void test0003() throws IOException {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString =
		"@Marker class A implements Comparable, @Marker Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0003", expectedUnitToString);
}
public void test0004() throws IOException {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker @SingleMember(0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString =
		"@Marker class A implements Comparable, @Marker @SingleMember(0) Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0004", expectedUnitToString);
}
public void test0005() throws IOException {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString =
		"@Marker class A implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0005", expectedUnitToString);
}
public void test0006() throws IOException {
	String source = "@Marker class A implements @Marker Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   @Marker Cloneable {\n" +
					"}\n";
	String expectedUnitToString =
		"@Marker class A implements @Marker Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, @Marker Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0006", expectedUnitToString);
}
public void test007() throws IOException {
	String source = "@Marker class A extends Object implements Comparable, " +
					"                   @Marker @SingleMember(10) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString =
		"@Marker class A extends Object implements Comparable, @Marker @SingleMember(10) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0007", expectedUnitToString);
}
public void test0008() throws IOException {
	String source = "@Marker class A extends @Marker Object implements Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString =
		"@Marker class A extends @Marker Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0008", expectedUnitToString);
}
public void test0009() throws IOException {
	String source = "@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, " +
	"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
	"                   Cloneable {\n" +
	"}\n";
	String expectedUnitToString =
		"@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0009", expectedUnitToString);
}
public void test0010() throws IOException {
	String source = "@Marker class A extends @Marker @SingleMember(0) @Normal(Value=0) Object implements Comparable, " +
	"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
	"                   Cloneable {\n" +
	"}\n";
	String expectedUnitToString =
		"@Marker class A extends @Marker @SingleMember(0) @Normal(Value = 0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0010", expectedUnitToString);
}
public void test0011() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String[] @Marker[][] s[] @SingleMember(0)[][] @Normal(Value = 0)[][];\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker String[] @Marker [][][] @SingleMember(0) [][] @Normal(Value = 0) [][] s;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0011", expectedUnitToString);
}
public void test0012() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static void main(String args[]) {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
		"  int[][] f;\n" +
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    @Readonly String @Nullable [] @NonNull [] s;\n" +
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0012", expectedUnitToString);
}
public void test0013() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static void main(String args[]) {\n" +
					"    @Readonly String s;\n" +
					"	 s = new @Readonly String @NonNull[] @Nullable[] { {\"Hello\"}, {\"World\"}} [0][0];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
		"  int[][] f;\n" +
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    @Readonly String s;\n" +
		"    s = new @Readonly String @NonNull [] @Nullable []{{\"Hello\"}, {\"World\"}}[0][0];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0013", expectedUnitToString);
}
public void test0014() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
		"  int[][] f;\n" +
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) {\n" +
		"    @Readonly String @Nullable [] @NonNull [] s;\n" +
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0014", expectedUnitToString);

}
public void test0015() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"@Marker public A () {}\n" +
					"}\n";
	String expectedUnitToString =
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
		"  int[][] f;\n" +
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" +
		"  float[][] p;\n" +
		"  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) {\n" +
		"    @Readonly String @Nullable [] @NonNull [] s;\n" +
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" +
		"  }\n" +
		"  public @Marker A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0015", expectedUnitToString);
}
// parameters
public void test0016() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0016", expectedUnitToString);
}
public void test0017() throws IOException  {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0017", expectedUnitToString);
}
public void test0018() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<String, Object>[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>[] @Normal(Value = 10) [][][] @SingleMember(10) [][] args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0018", expectedUnitToString);
}
public void test0019() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker [][] main(HashMap<String, Object>.Iterator[] @SingleMember(10) [][] args[] @Normal(Value = 10) [][])[] @Marker [][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>.Iterator[] @Normal(Value = 10) [][][] @SingleMember(10) [][] args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0019", expectedUnitToString);
}
// varargs annotation
public void test0020() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][] @Marker ... args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0020", expectedUnitToString);
}
public void test0021() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][] @Marker ... args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0021", expectedUnitToString);
}
public void test0022() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<Integer,String>[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>[] @SingleMember(10) [][] @Marker ... args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0022", expectedUnitToString);
}
public void test0023() throws IOException {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<Integer,String>.Iterator[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] {\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>.Iterator[] @SingleMember(10) [][] @Marker ... args) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0023", expectedUnitToString);
}
// local variables
public void test0024() throws IOException {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"public static void main(String args[]) {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int[][] f;\n" +
		"    @English String[] @NonNull [][] @Nullable [][] s;\n" +
		"    float[][] p;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0024", expectedUnitToString);
}
// type parameter
public void test0025() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> void foo() {\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"class A {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>void foo() {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0025", expectedUnitToString);
}
// Type
public void test0026() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int foo() {\n" +
					"    return 0;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> int bar() {\n" +
					"    return 0;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0026", null);
}
// Type
public void test0027() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String foo() {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> String bar () {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0027", null);
}
//Type
public void test0028() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object> foo() {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object> bar () {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0028", null);
}
// Type
public void test0029() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator foo() {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator bar () {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0029", null);
}
//Type
public void test0030() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator[] @NonEmpty[][] foo() {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator[] @NonEmpty[][] bar () {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0030", null);
}
//Type
public void test0031() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int[] @NonEmpty[][] foo() {\n" +
					"    return 0;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> int[] @NonEmpty[][] bar() {\n" +
					"    return 0;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0031", null);
}
// Type
public void test0032() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String[]@NonEmpty[][] foo() {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> String[]@NonEmpty[][] bar () {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0032", null);
}
//Type
public void test0033() throws IOException {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>[] @NonEmpty[][] foo() {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>[]@NonEmpty[][] bar () {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedError = "";
	checkParse(CHECK_PARSER, source.toCharArray(), expectedError, "test0033", null);
}
// Type0 field declaration.
public void test0034() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker int k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker int k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0034", expectedUnitToString);
}
//Type0 field declaration.
public void test0035() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker String k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0035", expectedUnitToString);
}
//Type0 field declaration.
public void test0036() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer> k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker HashMap<@Positive Integer, @Negative Integer> k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0036", expectedUnitToString);
}
//Type0 field declaration.
public void test0037() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0037", expectedUnitToString);
}
//Type0 field declaration.
public void test0038() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker int[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker int[] @NonEmpty [][] k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0038", expectedUnitToString);
}
//Type0 field declaration.
public void test0039() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String[] @NonEmpty[][]k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker String[] @NonEmpty [][] k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0039", expectedUnitToString);
}
//Type0 field declaration.
public void test0040() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0040", expectedUnitToString);
}
//Type0 field declaration.
public void test0041() throws IOException {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  int[][] f;\n" +
		"  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] k;\n" +
		"  float[][] p;\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0041", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0042() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker int foo() { return 0; }\n" +
					"    public int bar() { return 0; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int foo() {\n" +
		"    return 0;\n" +
		"  }\n" +
		"  public int bar() {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0042", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0043() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker String foo() { return null; }\n" +
					"    public String bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker String foo() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public String bar() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0043", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0044() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer> foo() { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>  bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker HashMap<@Positive Integer, @Negative Integer> foo() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public HashMap<@Positive Integer, @Negative Integer> bar() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0044", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0045() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>.Iterator  bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public HashMap<@Positive Integer, @Negative Integer>.Iterator bar() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0045", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0046() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker int[] foo() @NonEmpty[][] { return 0; }\n" +
					"    public int[] @NonEmpty[][] bar() { return 0; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker int[] @NonEmpty [][] foo() {\n" +
		"    return 0;\n" +
		"  }\n" +
		"  public int[] @NonEmpty [][] bar() {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0046", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0047() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker String[]  foo() @NonEmpty[][] { return null; }\n" +
					"    public String[] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker String[] @NonEmpty [][] foo() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public String[] @NonEmpty [][] bar() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0047", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0048() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>[] foo() @NonEmpty[][] { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer> [] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker HashMap<@Positive Integer, @Negative Integer> @NonEmpty [][][] foo() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] bar() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0048", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0049() throws IOException {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[]  foo() @NonEmpty[][] { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator @NonEmpty [][][] foo() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] bar() {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0049", expectedUnitToString);
}
//Type0 local variable declaration
public void test0050() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker int p;\n" +
					"        int q;\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker int p;\n" +
		"    int q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0050", expectedUnitToString);
}
//Type0 local variable declaration
public void test0051() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker String p;\n" +
					"        String q;\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker String p;\n" +
		"    String q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0051", expectedUnitToString);
}
//Type0 local variable declaration
public void test0052() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer> p;\n" +
					"        HashMap<@Positive Integer, @Negative Integer> q;\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker HashMap<@Positive Integer, @Negative Integer> p;\n" +
		"    HashMap<@Positive Integer, @Negative Integer> q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0052", expectedUnitToString);
}
//Type0 local variable declaration
public void test0053() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;\n" +
					"        HashMap<@Positive Integer, @Negative Integer>.Iterator q;\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;\n" +
		"    HashMap<@Positive Integer, @Negative Integer>.Iterator q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0053", expectedUnitToString);
}
//Type0 local variable declaration
public void test0054() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker int[] @NonNull[] p @NonEmpty[][];\n" +
					"        int[] @NonNull[] q @NonEmpty[][];\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker int[] @NonNull [] @NonEmpty [][] p;\n" +
		"    int[] @NonNull [] @NonEmpty [][] q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0054", expectedUnitToString);
}
//Type0 local variable declaration
public void test0055() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker String[] @NonNull[] p @NonEmpty[][];\n" +
					"        String[] @NonNull[] q @NonEmpty[][];\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker String[] @NonNull [] @NonEmpty [][] p;\n" +
		"    String[] @NonNull [] @NonEmpty [][] q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0055", expectedUnitToString);
}
//Type0 local variable declaration
public void test0056() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] p @NonEmpty[][];\n" +
					"        HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] q @NonEmpty[][];\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker HashMap<@Positive Integer, @Negative Integer> @NonEmpty [][][] @NonNull [] p;\n" +
		"    HashMap<@Positive Integer, @Negative Integer> @NonEmpty [][][] @NonNull [] q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0056", expectedUnitToString);
}
//Type0 local variable declaration
public void test0057() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] p @NonEmpty[][];\n" +
					"        HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] @NonEmpty[][] q;\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator @NonEmpty [][][] @NonNull [] p;\n" +
		"    HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull [] @NonEmpty [][] q;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0057", expectedUnitToString);
}
//Type0 foreach
public void test0058() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        String @NonNull[] @Marker[] s @Readonly[];\n" +
					"    	 for (@Readonly String @NonNull[] si @Marker[] : s) {}\n" +
					"    	 for (String @NonNull[] sii @Marker[] : s) {}\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    String @NonNull [] @Marker [] @Readonly [] s;\n" +
		"    for (@Readonly String @NonNull [] @Marker [] si : s) \n" +
		"      {\n" +
		"      }\n" +
		"    for (String @NonNull [] @Marker [] sii : s) \n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0058", expectedUnitToString);
}
//Type0 foreach
public void test0059() throws IOException {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        int @NonNull[] @Marker[] s @Readonly[];\n" +
					"    	 for (@Readonly int @NonNull[] si @Marker[] : s) {}\n" +
					"    	 for (int @NonNull[] sii @Marker[] : s) {}\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString =
		"public class A {\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    int @NonNull [] @Marker [] @Readonly [] s;\n" +
		"    for (@Readonly int @NonNull [] @Marker [] si : s) \n" +
		"      {\n" +
		"      }\n" +
		"    for (int @NonNull [] @Marker [] sii : s) \n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0059", expectedUnitToString);
}
// cast expression
public void test0060() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"int x;\n" +
					"x = (Integer)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value=0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly String[] @Normal(Value=0)[][] )\n" +
					"(@Readonly String[] @SingleMember(0)[][] )\n" +
					"(@Readonly String[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly int[] @Normal(Value=0)[][] )\n" +
					"(@Readonly int[] @SingleMember(0)[][] )\n" +
					"(@Readonly int[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>)\n" +
					"(@Readonly Object)\n" +
					"(@ReadOnly String)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly int) 10;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"public class Clazz {\n" +
		"  public Clazz() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int x;\n" +
		"    x = (Integer) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker [][]) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value = 0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker [][]) (@Readonly Object) (@Readonly String[] @Normal(Value = 0) [][]) (@Readonly String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (@Readonly Object) (@Readonly int[] @Normal(Value = 0) [][]) (@Readonly int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>) (@Readonly Object) (@ReadOnly String) (@Readonly Object) (@Readonly int) 10;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0060", expectedUnitToString);
}
//cast expression
public void test0061() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"int x;\n" +
					"x = (Integer)\n" +
					"(Object)\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )\n" +
					"(HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value=0)[][] )\n" +
					"(HashMap<Integer, @Negative Integer>[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly String[] @Normal(Value=0)[][] )\n" +
					"(String[] @SingleMember(0)[][] )\n" +
					"(@Readonly String[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly int[] @Normal(Value=0)[][] )\n" +
					"(int[] @SingleMember(0)[][] )\n" +
					"(@Readonly int[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator)\n" +
					"(Object)\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>)\n" +
					"(Object)\n" +
					"(@ReadOnly String)\n" +
					"(Object)\n" +
					"(@Readonly int) 10;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString =
		"public class Clazz {\n" +
		"  public Clazz() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int x;\n" +
		"    x = (Integer) (Object) (@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) (HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0) [][]) (@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker [][]) (Object) (@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value = 0) [][]) (HashMap<Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, Integer>[] @Marker [][]) (Object) (@Readonly String[] @Normal(Value = 0) [][]) (String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (Object) (@Readonly int[] @Normal(Value = 0) [][]) (int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (Object) (@Readonly HashMap<Integer, @Negative Integer>.Iterator) (Object) (@Readonly HashMap<@Positive Integer, Integer>) (Object) (@ReadOnly String) (Object) (@Readonly int) 10;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0061", expectedUnitToString);
}
// instanceof checks
public void test0062() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(Object o) {\n" +
					"if (o instanceof @Readonly String) {\n" +
					"} else if (o instanceof @Readonly int[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly String[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<?,?>[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<?,?>) {\n" +
					"} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) {\n" +
					"}\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class Clazz {\n" +
		"  public Clazz() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(Object o) {\n" +
		"    if ((o instanceof @Readonly String))\n" +
		"        {\n" +
		"        }\n" +
		"    else\n" +
		"        if ((o instanceof @Readonly int[] @NonEmpty [][]))\n" +
		"            {\n" +
		"            }\n" +
		"        else\n" +
		"            if ((o instanceof @Readonly String[] @NonEmpty [][]))\n" +
		"                {\n" +
		"                }\n" +
		"            else\n" +
		"                if ((o instanceof @Readonly HashMap<?, ?>[] @NonEmpty [][]))\n" +
		"                    {\n" +
		"                    }\n" +
		"                else\n" +
		"                    if ((o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][]))\n" +
		"                        {\n" +
		"                        }\n" +
		"                    else\n" +
		"                        if ((o instanceof @Readonly HashMap<?, ?>))\n" +
		"                            {\n" +
		"                            }\n" +
		"                        else\n" +
		"                            if ((o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator))\n" +
		"                                {\n" +
		"                                }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0062", expectedUnitToString);
}
// assorted unclassified
public void test0063() throws IOException {
	String source = "import java.util.HashMap;\n" +
					"import java.util.Map; \n" +
					"\n" +
					"public class Clazz <@A M extends @B String, @C N extends @D Comparable> extends\n" +
					"								@E Object implements @F Comparable <@G Object> {\n" +
					"	\n" +
					"  Clazz(char[] ...args) { \n" +
					"   }\n" +
					"   \n" +
					"  int @I[] f @J[], g, h[], i@K[];\n" +
					"  int @L[][]@M[] f2; \n" +
					"   \n" +
					"  Clazz (int @N[] @O... a) {}\n" +
					" int @R[]@S[] aa() {}\n" +
					" \n" +
					" int @T[]@U[]@V[] a () @W[]@X[]@Y[] { return null; }\n" +
					"   \n" +
					"  public void main(String @A[] @B ... args) throws @D Exception {\n" +
					"  	\n" +
					"       HashMap<@E String, @F String> b1;\n" +
					"      \n" +
					"     int b; b = (@G int) 10;\n" +
					"      \n" +
					"     char @H[]@I[] ch; ch = (@K char @L[]@M[])(@N char @O[]@P[]) null;\n" +
					"      \n" +
					"      int[] i; i = new @Q int @R[10];\n" +
					"       \n" +
					"      \n" +
					"   Integer w; w = new X<@S String, @T Integer>().get(new @U Integer(12));\n" +
					"    throw new @V Exception(\"test\");\n" +
					"    boolean c; c  = null instanceof @W String;\n" +
					"	} \n" +
					" public <@X X, @Y Y> void foo(X x, Y @Z... y) {  \n" +
					"	\n" +
					"}\n" +
					" \n" +
					" void foo(Map<? super @A Object, ? extends @B String> m){}\n" +
					" public int compareTo(Object arg0) {\n" +
					"     return 0;\n" +
					" }\n" +
					"\n" +
					"}\n" +
					"class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {\n" +
					"	\n" +
					"  public Integer get(Integer integer) {\n" +
					"       return null;\n" +
					"   }\n" +
					"}\n";


	String expectedUnitToString = "import java.util.HashMap;\n" +
								  "import java.util.Map;\n" +
								  "public class Clazz<@A M extends @B String, @C N extends @D Comparable> extends @E Object implements @F Comparable<@G Object> {\n" +
								  "  int @I [] @J [] f;\n" +
								  "  int @I [] g;\n" +
								  "  int @I [][] h;\n" +
								  "  int @I [] @K [] i;\n" +
								  "  int @L [][] @M [] f2;\n" +
								  "  Clazz(char[]... args) {\n" +
								  "    super();\n" +
								  "  }\n" +
								  "  Clazz(int @N [] @O ... a) {\n" +
								  "    super();\n" +
								  "  }\n" +
								  "  int @R [] @S [] aa() {\n" +
								  "  }\n" +
								  "  int @T [] @U [] @V [] @W [] @X [] @Y [] a() {\n" +
								  "    return null;\n" +
								  "  }\n" +
								  "  public void main(String @A [] @B ... args) throws @D Exception {\n" +
								  "    HashMap<@E String, @F String> b1;\n" +
								  "    int b;\n" +
								  "    b = (@G int) 10;\n" +
								  "    char @H [] @I [] ch;\n" +
								  "    ch = (@K char @L [] @M []) (@N char @O [] @P []) null;\n" +
								  "    int[] i;\n" +
								  "    i = new @Q int @R [10];\n" +
								  "    Integer w;\n" +
								  "    w = new X<@S String, @T Integer>().get(new @U Integer(12));\n" +
								  "    throw new @V Exception(\"test\");\n" +
								  "    boolean c;\n" +
								  "    c = (null instanceof @W String);\n" +
								  "  }\n" +
								  "  public <@X X, @Y Y>void foo(X x, Y @Z ... y) {\n" +
								  "  }\n" +
								  "  void foo(Map<? super @A Object, ? extends @B String> m) {\n" +
								  "  }\n" +
								  "  public int compareTo(Object arg0) {\n" +
								  "    return 0;\n" +
								  "  }\n" +
								  "}\n" +
								  "class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {\n" +
								  "  X() {\n" +
								  "    super();\n" +
								  "  }\n" +
								  "  public Integer get(Integer integer) {\n" +
								  "    return null;\n" +
								  "  }\n" +
								  "}\n";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0063", expectedUnitToString);
}
//assorted unclassified
public void test0064() throws IOException {
	String source = "class X<T extends @E Object & @F Comparable<? super T>> {}\n";
	String expectedUnitToString = "class X<T extends @E Object & @F Comparable<? super T>> {\n" +
								  "  X() {\n" +
								  "    super();\n" +
								  "  }\n" +
								  "}\n";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test064", expectedUnitToString);
}
//type class literal expression
public void test0066() throws IOException {
	String source = "public class X {\n" +
			"	<T extends Y<@A String @C[][]@B[]> & Cloneable> void foo(T t) {}\n" +
			"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <T extends Y<@A String @C [][] @B []> & Cloneable>void foo(T t) {\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0066", expectedUnitToString);
}
//check locations
public void test0067() throws IOException {
	String source =
		"public class X {\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"	@A Map<@B String, @C List<@D Object>> field2;\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @H String @E [] @F [] @G [] field;\n" +
		"  @A Map<@B String, @C List<@D Object>> field2;\n" +
		"  @A Map<@B String, @H String @E [] @F [] @G []> field3;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0067", expectedUnitToString);
}
//check locations
public void test0068() throws IOException {
	String source =
		"public class X {\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @H String @E [] @F [] @G [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0068", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0069() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B String, @H String> field3;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B String, @H String> field3;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0069", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 3, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@H"));
}
//check locations
public void test0070() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B String, @H String @E [] @F [] @G []> field3;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0070", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0071() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>> field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B String, @C List<@H String @E [][] @G []>> field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0071", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
}
//check locations
public void test0072() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>>[] @I[] @J[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B String, @C List<@H String @E [][] @G []>>[] @I [] @J [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0072", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", "[ARRAY]", locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
}
//check locations
public void test0073() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>> @I[][] @J[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B String, @C List<@H String @E [][] @G []>> @I [][] @J [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0073", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
}
//check locations
public void test0074() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@C List<@H String @E[][] @G[]>, String @B[] @D[]> @I[] @F[] @J[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@C List<@H String @E [][] @G []>, String @B [] @D []> @I [] @F [] @J [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0074", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 10, locations.size());
	assertEquals("Wrong location", null, locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), ARRAY]", locations.get("@D"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@B"));
}
//check locations
public void test0075() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@C List<@H String @E[][] @G[]>, @B List<String [] @D[]>> [] @I[] @F[] @J[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@C List<@H String @E [][] @G []>, @B List<String[] @D []>>[] @I [] @F [] @J [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0075", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 10, locations.size());
	assertEquals("Wrong location", "[ARRAY]", locations.get("@I"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@J"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY]", locations.get("@A"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0)]", locations.get("@C"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1)]", locations.get("@B"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY, ARRAY, TYPE_ARGUMENT(1), TYPE_ARGUMENT(0), ARRAY]", locations.get("@D"));
}
//check locations
public void test0076() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B String, @C List<@D Object>> field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B String, @C List<@D Object>> field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0076", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@D"));
}
//check locations
public void test0077() throws IOException {
	String source =
		"public class X {\n" +
		"	@H String @E[] @F[] @G[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @H String @E [] @F [] @G [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0077", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0078() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B Comparable<@C Object @D[] @E[] @F[]>, @G List<@H Document>> field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B Comparable<@C Object @D [] @E [] @F []>, @G List<@H Document>> field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0078", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@D"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@H"));
}
//check locations
public void test0079() throws IOException {
	String source =
		"public class X {\n" +
		"	@A java.util.Map<@B Comparable<@C Object @D[] @E[] @F[]>, @G List<@H Document>> field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A java.util.Map<@B Comparable<@C Object @D [] @E [] @F []>, @G List<@H Document>> field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0079", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 8, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY, ARRAY]", locations.get("@C"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0)]", locations.get("@D"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0), TYPE_ARGUMENT(0), ARRAY, ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), TYPE_ARGUMENT(0)]", locations.get("@H"));
}
//check locations
public void test0080() throws IOException {
	String source =
		"public class X {\n" +
		"	@B Map<? extends Z, ? extends @A Z> field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @B Map<? extends Z, ? extends @A Z> field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0080", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 2, locations.size());
	assertEquals("Wrong location", null, locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), WILDCARD]", locations.get("@A"));
}
//check locations
public void test0081() throws IOException {
	String source =
		"public class X {\n" +
		"	@H java.lang.String @E[] @F[] @G[] field;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @H java.lang.String @E [] @F [] @G [] field;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0081", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 4, locations.size());
	assertEquals("Wrong location", null, locations.get("@E"));
	assertEquals("Wrong location", "[ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
//check locations
public void test0082() throws IOException {
	String source =
		"public class X {\n" +
		"	@A Map<@B java.lang.String, @H java.lang.String @E[] @F[] @G[]> field3;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A Map<@B java.lang.String, @H java.lang.String @E [] @F [] @G []> field3;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	LocationPrinterVisitor visitor = new LocationPrinterVisitor();
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0082", expectedUnitToString, visitor);
	Map locations = visitor.getLocations();
	assertEquals("Wrong size", 6, locations.size());
	assertEquals("Wrong location", null, locations.get("@A"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(0)]", locations.get("@B"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1)]", locations.get("@E"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY]", locations.get("@F"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY]", locations.get("@G"));
	assertEquals("Wrong location", "[TYPE_ARGUMENT(1), ARRAY, ARRAY, ARRAY]", locations.get("@H"));
}
public void test0083() throws IOException {
	String source =
		"@Marker class A {}\n;" +
		"@Marker class B extends @Marker A {}\n" +
		"@Marker class C extends @Marker @SingleMember(0) A {}\n" +
		"@Marker class D extends @Marker @SingleMember(0) @Normal(value = 0) A {}\n" +
		"@Marker class E extends B {}\n;";

	String expectedUnitToString =
		"@Marker class A {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class B extends @Marker A {\n" +
		"  B() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class C extends @Marker @SingleMember(0) A {\n" +
		"  C() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class D extends @Marker @SingleMember(0) @Normal(value = 0) A {\n" +
		"  D() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class E extends B {\n" +
		"  E() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0083", expectedUnitToString);
}

// To test Parser.consumeAdditionalBound() with Type annotations
public void test0084() throws IOException {
	String source =
		"@Marker interface I<@Negative T> {}\n" +
		"@SingleMember(0) interface J<@Positive T> {}\n" +
		"@Marker class A implements I<@SingleMember(0) A>, J<@Marker A> {}\n" +
		"@Normal(value = 1) class X<E extends @Positive A & @Marker I<A> & @Marker @SingleMember(1) J<@Readonly A>>  {\n" +
		"}";
	String expectedUnitToString =
		"@Marker interface I<@Negative T> {\n" +
		"}\n" +
		"@SingleMember(0) interface J<@Positive T> {\n" +
		"}\n" +
		"@Marker class A implements I<@SingleMember(0) A>, J<@Marker A> {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Normal(value = 1) class X<E extends @Positive A & @Marker I<A> & @Marker @SingleMember(1) J<@Readonly A>> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0084", expectedUnitToString );
}

// To test Parser.consumeAdditionalBound() with Type annotations
public void test0085() throws IOException {
	String source =
		"import java.io.Serializable;\n" +
		"\n" +
		"@SingleMember(10) class X<T extends @Marker Serializable & @Normal(value = 10) Runnable, V extends @Marker T> {\n" +
		"	@Negative T t;\n" +
		"	@Marker X(@Readonly T t) {\n" +
		"		this.t = t;\n" +
		"	}\n" +
		"	void foo(@Marker X this) {\n" +
		"		(this == null ? t : t).run();\n" +
		"		((@Marker V) t).run();\n" +
		"	}\n" +
		"	public static void main(@Readonly String @Marker [] args) {\n" +
		"		new @Marker  X<@Marker A, @Negative A>(new @Marker A()).foo();\n" +
		"	}\n" +
		"}\n" +
		"@Marker class A implements @Marker Serializable, @SingleMember(1) Runnable {\n" +
		"	public void run() {\n" +
		"		System.out.print(\"AA\");\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"import java.io.Serializable;\n" +
		"@SingleMember(10) class X<T extends @Marker Serializable & @Normal(value = 10) Runnable, V extends @Marker T> {\n" +
		"  @Negative T t;\n" +
		"  @Marker X(@Readonly T t) {\n" +
		"    super();\n" +
		"    this.t = t;\n" +
		"  }\n" +
		"  void foo(@Marker X this) {\n" +
		"    ((this == null) ? t : t).run();\n" +
		"    ((@Marker V) t).run();\n" +
		"  }\n" +
		"  public static void main(@Readonly String @Marker [] args) {\n" +
		"    new @Marker X<@Marker A, @Negative A>(new @Marker A()).foo();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class A implements @Marker Serializable, @SingleMember(1) Runnable {\n" +
		"  A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void run() {\n" +
		"    System.out.print(\"AA\");\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0085", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0086() throws IOException {
	String source =
		"class X {\n" +
		"	@Marker X() {\n" +
		"		System.out.print(\"new X created\");\n" +
		"	}\n" +
		"  	void f() throws @Marker InstantiationException {\n" +
		"       X testX;\n" +
		"		testX = new @Readonly @Negative X();\n" +
		"		Double d;\n" +
		"		d = new @Marker @Positive Double(1.1);\n" +
		"     	throw new @Positive @Normal(value = 10) InstantiationException(\"test\");\n" +
		"   }\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  @Marker X() {\n" +
		"    super();\n" +
		"    System.out.print(\"new X created\");\n" +
		"  }\n" +
		"  void f() throws @Marker InstantiationException {\n" +
		"    X testX;\n" +
		"    testX = new @Readonly @Negative X();\n" +
		"    Double d;\n" +
		"    d = new @Marker @Positive Double(1.1);\n" +
		"    throw new @Positive @Normal(value = 10) InstantiationException(\"test\");\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0086", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0087() throws IOException {
	String source =
		"class X {\n" +
		"	@Marker X() {\n" +
		"		System.out.print(\"new X created\");\n" +
		"	}\n" +
		"	@Marker class Inner {\n" +
		"		@Normal(value = 10) Inner(){\n" +
		"			System.out.print(\"X.Inner created\");\n" +
		"		}\n" +
		"	}\n" +
		"	public String getString(){\n" +
		"		return \"hello\";\n" +
		"	}\n" +
		"  	void f(@Marker X this) {\n" +
		"       String testString;\n" +
		"		testString = new @Readonly @Negative X().getString();\n" +
		"		X.Inner testInner;\n" +
		"		testInner = new @Readonly X.Inner();\n" +
		"		int i;\n" +
		"		for(i = 0; i < 10; i++)\n" +
		"			System.out.print(\"test\");\n" +
		"   }\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  @Marker class Inner {\n" +
		"    @Normal(value = 10) Inner() {\n" +
		"      super();\n" +
		"      System.out.print(\"X.Inner created\");\n" +
		"    }\n" +
		"  }\n" +
		"  @Marker X() {\n" +
		"    super();\n" +
		"    System.out.print(\"new X created\");\n" +
		"  }\n" +
		"  public String getString() {\n" +
		"    return \"hello\";\n" +
		"  }\n" +
		"  void f(@Marker X this) {\n" +
		"    String testString;\n" +
		"    testString = new @Readonly @Negative X().getString();\n" +
		"    X.Inner testInner;\n" +
		"    testInner = new @Readonly X.Inner();\n" +
		"    int i;\n" +
		"    for (i = 0; (i < 10); i ++) \n" +
		"      System.out.print(\"test\");\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0087", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0088() throws IOException {
	String source =
		"import java.io.Serializable;\n" +
		"class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		new @Marker Serializable() {\n" +
		"		};\n" +
		"		new @Positive @Marker Serializable() {\n" +
		"			public long serialVersion;\n" +
		"		};\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.io.Serializable;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker Serializable() {\n" +
		"    };\n" +
		"    new @Positive @Marker Serializable() {\n" +
		"      public long serialVersion;\n" +
		"    };\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0088", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0089() throws IOException {
	String source =
		"import java.io.Serializable;\n" +
		"class X<T>{\n" +
		"	public void f() {\n" +
		"		X testX;\n" +
		"		testX = new @Marker @SingleMember(10) X<@Negative Integer>();\n" +
		"		System.out.print(\"object created\");\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.io.Serializable;\n" +
		"class X<T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void f() {\n" +
		"    X testX;\n" +
		"    testX = new @Marker @SingleMember(10) X<@Negative Integer>();\n" +
		"    System.out.print(\"object created\");\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0089", expectedUnitToString );
}

// To test Parser.classInstanceCreation() with type annotations
public void test0090() throws IOException {
	String source =
		"class X <@Marker T extends @Readonly String> {\n" +
		"    T foo(T t) {\n" +
		"        return t;\n" +
		"    }\n" +
		"    public static void main(String[] args) {\n" +
		"        new @Readonly X<String>().baz(\"SUCCESS\");\n" +	// Parser.classInstanceCreation called
		"    }\n" +
		"    void baz(final T t) {\n" +
		"        new @Readonly @Marker Object() {\n" +	// Parser.classInstanceCreation called
		"            void print() {\n" +
		"            }\n" +
		"        }.print();\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T extends @Readonly String> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  T foo(T t) {\n" +
		"    return t;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Readonly X<String>().baz(\"SUCCESS\");\n" +
		"  }\n" +
		"  void baz(final T t) {\n" +
		"    new @Readonly @Marker Object() {\n" +
		"  void print() {\n" +
		"  }\n" +
		"}.print();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0090", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0091() throws IOException {
	String source =
		"class X <@Marker T extends @Readonly String> {\n" +
		"    public static void main(String[] args) {\n" +
		"		int [] x1;\n" +
		"		x1 = new int @Marker @SingleMember(2) [] {-1, -2};\n" +
		"       Integer [][] x2;\n" +
		"		x2 = new @Positive Integer @Marker @SingleMember(3) [] @SingleMember(3) [] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T extends @Readonly String> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int[] x1;\n" +
		"    x1 = new int @Marker @SingleMember(2) []{(- 1), (- 2)};\n" +
		"    Integer[][] x2;\n" +
		"    x2 = new @Positive Integer @Marker @SingleMember(3) [] @SingleMember(3) []{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0091", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0092() throws IOException {
	String source =
		"class X {\n" +
		"	static class T {\n" +
		"		public @Readonly Object @Normal(value = 10) [] f() {\n" +
		"			return new @Readonly Object @Normal(value = 10) [] {this, T.this};\n" +
		"		}\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  static class T {\n" +
		"    T() {\n" +
		"      super();\n" +
		"    }\n" +
		"    public @Readonly Object @Normal(value = 10) [] f() {\n" +
		"      return new @Readonly Object @Normal(value = 10) []{this, T.this};\n" +
		"    }\n" +
		"  }\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0092", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0093() throws IOException {
	String source =
		"class X {\n" +
		"    public static void main(String[] args) {\n" +
		"        java.util.Arrays.asList(new @Readonly Object @SingleMember(1) [] {\"1\"});\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    java.util.Arrays.asList(new @Readonly Object @SingleMember(1) []{\"1\"});\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0093", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0094() throws IOException {
	String source =
		"class X {\n" +
		"	public boolean test() {\n" +
		"		String[] s;\n" +
		"		s = foo(new @Marker String @SingleMember(1) []{\"hello\"});\n" +
		"		return s != null;\n" +
		"	}\n" +
		"	public <@Marker F> F @SingleMember(1) [] foo(F[] f) {\n" +
		"		return f;\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean test() {\n" +
		"    String[] s;\n" +
		"    s = foo(new @Marker String @SingleMember(1) []{\"hello\"});\n" +
		"    return (s != null);\n" +
		"  }\n" +
		"  public <@Marker F>F @SingleMember(1) [] foo(F[] f) {\n" +
		"    return f;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0094", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithInitializer() with Type Annotations
public void test0095() throws IOException {
	String source =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"@Marker class Deejay {\n" +
		"	@Marker class Counter<@Marker T> {}\n" +
		"	public void f(String[] args) {\n" +
		"		Counter<@Positive Integer> songCounter;\n" +
		"		songCounter = new Counter<@Positive Integer>();\n" +
		"		Counter<@Readonly String> genre;\n" +
		"		genre = new Counter<@Readonly String>();\n" +
		"		List<@Marker Counter<?>> list1;\n" +
		"		list1 = Arrays.asList(new @Marker Counter<?> @Normal(value = 2) @Marker [] {songCounter, genre});\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"@Marker class Deejay {\n" +
		"  @Marker class Counter<@Marker T> {\n" +
		"    Counter() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  Deejay() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void f(String[] args) {\n" +
		"    Counter<@Positive Integer> songCounter;\n" +
		"    songCounter = new Counter<@Positive Integer>();\n" +
		"    Counter<@Readonly String> genre;\n" +
		"    genre = new Counter<@Readonly String>();\n" +
		"    List<@Marker Counter<?>> list1;\n" +
		"    list1 = Arrays.asList(new @Marker Counter<?> @Normal(value = 2) @Marker []{songCounter, genre});\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0095", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0096() throws IOException {
	String source =
		"class X <@Marker T extends @Readonly String> {\n" +
		"    public static void main(String[] args) {\n" +
		"		int [] x1;\n" +
		"		x1 = new int @Marker @SingleMember(10) [10];\n" +
		"       Integer [][] x2;\n" +
		"		x2 = new @Positive Integer @Marker [10] @Normal(value = 10) [10];\n" +
		"		char[][] tokens;\n" +
		"		tokens = new char @SingleMember(0) [0] @Normal(value = 10) @Marker [];\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T extends @Readonly String> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    int[] x1;\n" +
		"    x1 = new int @Marker @SingleMember(10) [10];\n" +
		"    Integer[][] x2;\n" +
		"    x2 = new @Positive Integer @Marker [10] @Normal(value = 10) [10];\n" +
		"    char[][] tokens;\n" +
		"    tokens = new char @SingleMember(0) [0] @Normal(value = 10) @Marker [];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0096", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0097() throws IOException {
	String source =
		"class X {\n" +
		"	public @Readonly Object @Normal(value = 10) [] f(@Marker X this) {\n" +
		"		return new @Readonly Object @Normal(value = 10) [10];\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public @Readonly Object @Normal(value = 10) [] f(@Marker X this) {\n" +
		"    return new @Readonly Object @Normal(value = 10) [10];\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0097", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0098() throws IOException {
	String source =
		"class X {\n" +
		"	public boolean test() {\n" +
		"		String[] s;\n" +
		"		s = foo(new @Marker String @SingleMember(1) [10]);\n" +
		"		return s != null;\n" +
		"	}\n" +
		"	public <@Marker F> F @SingleMember(1) [] foo(F[] f) {\n" +
		"		return f;\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean test() {\n" +
		"    String[] s;\n" +
		"    s = foo(new @Marker String @SingleMember(1) [10]);\n" +
		"    return (s != null);\n" +
		"  }\n" +
		"  public <@Marker F>F @SingleMember(1) [] foo(F[] f) {\n" +
		"    return f;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0098", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0099() throws IOException {
	String source =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"class X<@Marker T> {\n" +
		"	public void test() {\n" +
		"		List<@Marker X<?>> a;\n" +
		"		a = Arrays.asList(new @Marker X<?> @SingleMember(0) [0]);\n" +
		"		String @Marker [] @SingleMember(1) [] x;\n" +
		"		x = new @Readonly String @Normal(value = 5) [5] @SingleMember(1) [1];\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.util.Arrays;\n" +
		"import java.util.List;\n" +
		"class X<@Marker T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test() {\n" +
		"    List<@Marker X<?>> a;\n" +
		"    a = Arrays.asList(new @Marker X<?> @SingleMember(0) [0]);\n" +
		"    String @Marker [] @SingleMember(1) [] x;\n" +
		"    x = new @Readonly String @Normal(value = 5) [5] @SingleMember(1) [1];\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0099", expectedUnitToString );
}

// To test Parser.consumeArrayCreationExpressionWithoutInitializer() with Type Annotations
public void test0100() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X {\n" +
		"    public Integer[] getTypes() {\n" +
		"        List<@Positive Integer> list;\n" +
		"		 list = new ArrayList<@Positive Integer>();\n" +
		"        return list == null \n" +
		"            ? new @Positive Integer @SingleMember(0) [0] \n" +
		"            : list.toArray(new @Positive Integer @Marker [list.size()]);\n" +
		"    }\n" +
		"}";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public Integer[] getTypes() {\n" +
		"    List<@Positive Integer> list;\n" +
		"    list = new ArrayList<@Positive Integer>();\n" +
		"    return ((list == null) ? new @Positive Integer @SingleMember(0) [0] : list.toArray(new @Positive Integer @Marker [list.size()]));\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0100", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0101() throws IOException {
	String source =
		"import java.util.*;\n" +
		"\n" +
		"@Marker class X {\n" +
		"    Vector<Object> data;\n" +
		"    public void t() {\n" +
		"        Vector<@Readonly Object> v;\n" +
		" 		 v = (@Marker @SingleMember(0) Vector<@Readonly Object>) data.elementAt(0);\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"@Marker class X {\n" +
		"  Vector<Object> data;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void t() {\n" +
		"    Vector<@Readonly Object> v;\n" +
		"    v = (@Marker @SingleMember(0) Vector<@Readonly Object>) data.elementAt(0);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0101", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
// To test Parser.consumeClassHeaderExtends() with Type Annotations
public void test0102() throws IOException {
	String source =
		"class X<E> {\n" +
		"    X<@Readonly String> bar() {\n" +
		"    	return (@Marker AX<@Readonly String>) new X<@Readonly String>();\n" +
		"    }\n" +
		"    X<@Readonly String> bar(Object o) {\n" +
		"    	return (@Marker AX<@Readonly String>) o;\n" +
		"    }\n" +
		"    X<@Negative E> foo(Object o) {\n" +
		"    	return (@Marker @Normal(value = 10) AX<@Negative E>) o;\n" +
		"    }    \n" +
		"    X<E> baz(Object o) {\n" +
		"    	return (@Marker AX<E>) null;\n" +
		"    }\n" +
		"    X<String> baz2(BX bx) {\n" +
		"    	return (@Marker @SingleMember(10) X<String>) bx;\n" +
		"    }\n" +
		"}\n" +
		"@Normal(value = 1) class AX<@Marker F> extends @Marker X<@SingleMember(10)F> {}\n" +
		"@Normal(value = 2) class BX extends @Marker @SingleMember(1) AX<@Readonly String> {}\n";
	String expectedUnitToString =
		"class X<E> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  X<@Readonly String> bar() {\n" +
		"    return (@Marker AX<@Readonly String>) new X<@Readonly String>();\n" +
		"  }\n" +
		"  X<@Readonly String> bar(Object o) {\n" +
		"    return (@Marker AX<@Readonly String>) o;\n" +
		"  }\n" +
		"  X<@Negative E> foo(Object o) {\n" +
		"    return (@Marker @Normal(value = 10) AX<@Negative E>) o;\n" +
		"  }\n" +
		"  X<E> baz(Object o) {\n" +
		"    return (@Marker AX<E>) null;\n" +
		"  }\n" +
		"  X<String> baz2(BX bx) {\n" +
		"    return (@Marker @SingleMember(10) X<String>) bx;\n" +
		"  }\n" +
		"}\n" +
		"@Normal(value = 1) class AX<@Marker F> extends @Marker X<@SingleMember(10) F> {\n" +
		"  AX() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Normal(value = 2) class BX extends @Marker @SingleMember(1) AX<@Readonly String> {\n" +
		"  BX() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0102", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0103() throws IOException {
	String source =
		"import java.lang.reflect.Array;\n" +
		"@Marker class X<@Readonly T> {\n" +
		"	T @SingleMember(0) [] theArray;\n" +
		"	public X(Class<T> clazz) {\n" +
		"		theArray = (@Marker @SingleMember(0) T @Normal(value = 10) []) Array.newInstance(clazz, 10); // Compiler warning\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.lang.reflect.Array;\n" +
		"@Marker class X<@Readonly T> {\n" +
		"  T @SingleMember(0) [] theArray;\n" +
		"  public X(Class<T> clazz) {\n" +
		"    super();\n" +
		"    theArray = (@Marker @SingleMember(0) T @Normal(value = 10) []) Array.newInstance(clazz, 10);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0103", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithGenericsArray() with Type Annotations
public void test0104() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X {\n" +
		"    void method(Object o) {\n" +
		"		 if (o instanceof String[]){\n" +
		"			 String[] s;\n" +
		"			 s = (@Marker @Readonly String @Marker []) o;\n" +
		"		 }\n" +
		"        if (o instanceof @Readonly List<?>[]) {\n" +
		"            List<?>[] es;\n" +
		"			 es = (@Marker List<?> @SingleMember(0) []) o;\n" +
		"        }\n" +
		"    }\n" +
		"}";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void method(Object o) {\n" +
		"    if ((o instanceof String[]))\n" +
		"        {\n" +
		"          String[] s;\n" +
		"          s = (@Marker @Readonly String @Marker []) o;\n" +
		"        }\n" +
		"    if ((o instanceof @Readonly List<?>[]))\n" +
		"        {\n" +
		"          List<?>[] es;\n" +
		"          es = (@Marker List<?> @SingleMember(0) []) o;\n" +
		"        }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0104", expectedUnitToString );
}


// To test Parser.consumeCastExpressionWithPrimitiveType() with Type Annotations
public void test0105() throws IOException {
	String source =
		"import java.util.HashMap;\n" +
		"class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		HashMap<Byte, Byte> subst;\n" +
		"		subst = new HashMap<Byte, Byte>();\n" +
		"		subst.put((@Marker byte)1, (@Positive byte)1);\n" +
		"		if (1 + subst.get((@Positive @Normal(value = 10) byte)1) > 0.f) {\n" +
		"			System.out.println(\"SUCCESS\");\n" +
		"		}		\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"import java.util.HashMap;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    HashMap<Byte, Byte> subst;\n" +
		"    subst = new HashMap<Byte, Byte>();\n" +
		"    subst.put((@Marker byte) 1, (@Positive byte) 1);\n" +
		"    if (((1 + subst.get((@Positive @Normal(value = 10) byte) 1)) > 0.f))\n" +
		"        {\n" +
		"          System.out.println(\"SUCCESS\");\n" +
		"        }\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0105", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithPrimitiveType() with Type Annotations
public void test0106() throws IOException {
	String source =
		"class X{\n" +
		"	private float x, y, z;\n" +
		"	float magnitude () {\n" +
		"		return (@Marker @Positive float) Math.sqrt((x*x) + (y*y) + (z*z));\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  private float x;\n" +
		"  private float y;\n" +
		"  private float z;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  float magnitude() {\n" +
		"    return (@Marker @Positive float) Math.sqrt((((x * x) + (y * y)) + (z * z)));\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0106", expectedUnitToString );
}

// To test Parser.consumeCastExpressionWithQualifiedGenericsArray() with Type Annotations
// Javac version b76 crashes on type annotations on type arguments to parameterized classes
// in a qualified generic reference
public void test0107() throws IOException {
	String source =
		"class C1<T> {\n" +
		"	class C11 {	}\n" +
		"	@Marker class C12 {\n" +
		"		T t;\n" +
		"		C1<@Readonly T>.C11 m() {\n" +
		"			C1<@Readonly T>.C11[] ts;\n" +
		"			ts = (@Marker C1<@Readonly T>.C11[]) new @Marker C1<?>.C11 @Normal(value = 5) [5];\n" +
		"			return ts;\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class C1<T> {\n" +
		"  class C11 {\n" +
		"    C11() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  @Marker class C12 {\n" +
		"    T t;\n" +
		"    C12() {\n" +
		"      super();\n" +
		"    }\n" +
		"    C1<@Readonly T>.C11 m() {\n" +
		"      C1<@Readonly T>.C11[] ts;\n" +
		"      ts = (@Marker C1<@Readonly T>.C11[]) new @Marker C1<?>.C11 @Normal(value = 5) [5];\n" +
		"      return ts;\n" +
		"    }\n" +
		"  }\n" +
		"  C1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0107", expectedUnitToString );
}

// To test Parser.consumeFormalParameter() with Type Annotations
public void test0108() throws IOException {
	String source =
		"class X {\n" +
		"	int field;" +
		"	public void test(@Marker X x,@Positive int i){\n" +
		"		x.field = i;\n" +
		"	}\n" +
		"	public static void main(@Readonly String args @Normal(10) []){" +
		"		System.exit(0);\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X {\n" +
		"  int field;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test(@Marker X x, @Positive int i) {\n" +
		"    x.field = i;\n" +
		"  }\n" +
		"  public static void main(@Readonly String @Normal(10) [] args) {\n" +
		"    System.exit(0);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0108", expectedUnitToString );
}

// To test Parser.consumeFormalParameter() with Type Annotations
public void test0109() throws IOException {
	String source =
		"class X<@Marker T> {\n" +
		"	T field;" +
		"	public void test(@Marker @SingleMember(1) X<? extends @Marker Object> x,@Positive T i){\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@Marker T> {\n" +
		"  T field;\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test(@Marker @SingleMember(1) X<? extends @Marker Object> x, @Positive T i) {\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0109", expectedUnitToString );
}

// To test Parser.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments()
// with Type Annotations
// Javac b76 crashes with type annotations in qualified class instance creation expression
public void test0110() throws IOException {
	String source =
		"class X {\n" +
		"	class MX {\n" +
		"		@Marker <T> MX(T t){\n" +
		"			System.out.println(t);\n" +
		"		}\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		new @Marker @SingleMember(10) X().new <@Readonly String> @Marker MX(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
			"class X {\n" +
			"  class MX {\n" +
			"    @Marker <T>MX(T t) {\n" +
			"      super();\n" +
			"      System.out.println(t);\n" +
			"    }\n" +
			"  }\n" +
			"  X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    new @Marker @SingleMember(10) X().new <@Readonly String>@Marker MX(\"SUCCESS\");\n" +
			"  }\n" +
			"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0110", expectedUnitToString);
}

// To test Parser.consumeClassInstanceCreationExpressionWithTypeArguments()
// with Type Annotations
public void test0111() throws IOException {
	String source =
		"class X {\n" +
		"	public <T> X(T t){\n" +
		"		System.out.println(t);\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		new <@Readonly String> @Marker @SingleMember(0) X(\"SUCCESS\");\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
			"class X {\n" +
			"  public <T>X(T t) {\n" +
			"    super();\n" +
			"    System.out.println(t);\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    new <@Readonly String>@Marker @SingleMember(0) X(\"SUCCESS\");\n" +
			"  }\n" +
			"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0111", expectedUnitToString);
}

// To test Parser.consumeEnhancedForStatementHeaderInit() with Type Annotations
public void test0112() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X {\n" +
		"   List list() { return null; }\n" +
		"   void m2() { for (@SingleMember(10) Iterator<@Marker X> i = list().iterator(); i.hasNext();); }\n" +
		"	void m3() {\n" +
		"		Integer [] array;\n" +
		"		array = new Integer [] {1, 2, 3};\n" +
		"		List<List<X>> xList;\n" +
		"		xList = null;\n" +
		"		for(@Positive @SingleMember(10) Integer i: array) {}\n" +
		"		for(@Marker @Normal(value = 5) List<@Readonly X> x: xList) {}\n" +
		"	}" +
		"}\n";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  List list() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  void m2() {\n" +
		"    for (@SingleMember(10) Iterator<@Marker X> i = list().iterator();; i.hasNext(); ) \n" +
		"      ;\n" +
		"  }\n" +
		"  void m3() {\n" +
		"    Integer[] array;\n" +
		"    array = new Integer[]{1, 2, 3};\n" +
		"    List<List<X>> xList;\n" +
		"    xList = null;\n" +
		"    for (@Positive @SingleMember(10) Integer i : array) \n" +
		"      {\n" +
		"      }\n" +
		"    for (@Marker @Normal(value = 5) List<@Readonly X> x : xList) \n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_COMPLETION_PARSER & ~CHECK_SELECTION_PARSER & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0112", expectedUnitToString );
	expectedUnitToString =
		"import java.util.*;\n" +
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  List list() {\n" +
		"    return null;\n" +
		"  }\n" +
		"  void m2() {\n" +
		"    for (@SingleMember(10) Iterator<@Marker X> i;; i.hasNext(); ) \n" +
		"      ;\n" +
		"  }\n" +
		"  void m3() {\n" +
		"    Integer[] array;\n" +
		"    array = new Integer[]{1, 2, 3};\n" +
		"    List<List<X>> xList;\n" +
		"    xList = null;\n" +
		"    for (@Positive @SingleMember(10) Integer i : array) \n" +
		"      {\n" +
		"      }\n" +
		"    for (@Marker @Normal(value = 5) List<@Readonly X> x : xList) \n" +
		"      {\n" +
		"      }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_COMPLETION_PARSER & CHECK_SELECTION_PARSER, source.toCharArray(), null, "test0112", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0113() throws IOException {
	String source =
		"@Marker class X {\n" +
		"  void f(@Normal(value = 5) X this) {\n" +
		"    new @Marker @SingleMember(10) Object() {\n" +
		"      void foo(){\n" +
		"        System.out.println(\"test\");\n" +
		"      }\n" +
		"    }.foo();\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"@Marker class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  void f(@Normal(value = 5) X this) {\n" +
		"    new @Marker @SingleMember(10) Object() {\n" +
		"  void foo() {\n" +
		"    System.out.println(\"test\");\n" +
		"  }\n" +
		"}.foo();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test0113", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0114() throws IOException {
	String source =
		"class Toplevel2{\n" +
		"    public boolean foo(){\n" +
		"    Toplevel2 o;\n" +
		"	 o = new @Marker @Normal(value = 5) Toplevel2() { \n" +
		"              public boolean foo() {  return false; }  // no copy in fact\n" +
		"              };\n" +
		"    return o.foo();\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"class Toplevel2 {\n" +
		"  Toplevel2() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean foo() {\n" +
		"    Toplevel2 o;\n" +
		"    o = new @Marker @Normal(value = 5) Toplevel2() {\n" +
		"  public boolean foo() {\n" +
		"    return false;\n" +
		"  }\n" +
		"};\n" +
		"    return o.foo();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test0114", expectedUnitToString );
}

// To test Parser.consumeEnterAnonymousClassBody() with Type Annotations
public void test0115() throws IOException {
	String source =
		"class X <T> {\n" +
		"    T foo(T t) {\n" +
		"        System.out.println(t);\n" +
		"        return t;\n" +
		"    }\n" +
		"    public static void main(String @Normal(value =  5) [] args) {\n" +
		"        new @Marker X<@SingleMember(10) @Normal(value = 5) XY>() {\n" +
		"            void run() {\n" +
		"                foo(new @Marker XY());\n" +
		"            }\n" +
		"        }.run();\n" +
		"    }\n" +
		"}\n" +
		"@Marker class XY {\n" +
		"    public String toString() {\n" +
		"        return \"SUCCESS\";\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"class X<T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  T foo(T t) {\n" +
		"    System.out.println(t);\n" +
		"    return t;\n" +
		"  }\n" +
		"  public static void main(String @Normal(value = 5) [] args) {\n" +
		"    new @Marker X<@SingleMember(10) @Normal(value = 5) XY>() {\n" +
		"  void run() {\n" +
		"    foo(new @Marker XY());\n" +
		"  }\n" +
		"}.run();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class XY {\n" +
		"  XY() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public String toString() {\n" +
		"    return \"SUCCESS\";\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_DOCUMENT_ELEMENT_PARSER, source.toCharArray(), null, "test0115", expectedUnitToString );
}

// To test Parser.consumeInsideCastExpressionLL1() with Type Annotations
public void test0116() throws IOException {
	String source =
		"class X{\n" +
		"  public void test1(){\n" +
		"    throw (@Marker Error) null; \n" +
		"  }  \n" +
		"  public void test2(){\n" +
		"    String s;\n" +
		"	 s = (@Marker @SingleMember(10) String) null;\n" +
		"	 byte b;\n" +
		"	 b = 0;\n" +
		"	 Byte i;\n" +
		"	 i = (@Positive Byte) b;\n" +
		"  }  \n" +
		"  public void test3(java.io.Serializable name) {\n" +
		"     Object temp;\n" +
		"	  temp = (Object)name;\n" +
		"     System.out.println( (String)temp );\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"class X {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test1() {\n" +
		"    throw (@Marker Error) null;\n" +
		"  }\n" +
		"  public void test2() {\n" +
		"    String s;\n" +
		"    s = (@Marker @SingleMember(10) String) null;\n" +
		"    byte b;\n" +
		"    b = 0;\n" +
		"    Byte i;\n" +
		"    i = (@Positive Byte) b;\n" +
		"  }\n" +
		"  public void test3(java.io.Serializable name) {\n" +
		"    Object temp;\n" +
		"    temp = (Object) name;\n" +
		"    System.out.println((String) temp);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0116", expectedUnitToString );
}

// To test Parser.consumeInstanceOfExpression() with Type Annotations
public void test0117() throws IOException {
	String source =
		"import java.util.*;\n" +
		"class X <@NonNull T>{\n" +
		" 	public void test1(Object obj) {\n" +
		"   	if(obj instanceof @Marker @NonNull X) {\n" +
		"		 	X newX;\n" +
		"		 	newX = (@NonNull X) obj;\n" +
		"	 }\n" +
		"   }\n" +
		"	@NonNull T foo(@NonNull T t) {\n" +
		"       if (t instanceof @NonNull @Marker List<?> @Normal(value = 10) []) {\n" +
		"           List<?> @SingleMember (10) [] es;\n" +
		"			es = (@Marker List<?> @SingleMember(10) []) t;\n" +
		"       }\n" +
		"		if (t instanceof @Marker @Normal(value = 5) X<?>) {\n" +
		"			return t;\n" +
		"		}\n" +
		"		return t;\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"import java.util.*;\n" +
		"class X<@NonNull T> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void test1(Object obj) {\n" +
		"    if ((obj instanceof @Marker @NonNull X))\n" +
		"        {\n" +
		"          X newX;\n" +
		"          newX = (@NonNull X) obj;\n" +
		"        }\n" +
		"  }\n" +
		"  @NonNull T foo(@NonNull T t) {\n" +
		"    if ((t instanceof @NonNull @Marker List<?> @Normal(value = 10) []))\n" +
		"        {\n" +
		"          List<?> @SingleMember(10) [] es;\n" +
		"          es = (@Marker List<?> @SingleMember(10) []) t;\n" +
		"        }\n" +
		"    if ((t instanceof @Marker @Normal(value = 5) X<?>))\n" +
		"        {\n" +
		"          return t;\n" +
		"        }\n" +
		"    return t;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER , source.toCharArray(), null, "test0117", expectedUnitToString );
}

// To test Parser.consumeInstanceOfExpressionWithName() with Type Annotations
public void test0118() throws IOException {
	String source =
		"class Outer<E> {\n" +
		"  Inner inner;\n" +
		"  class Inner {\n" +
		"    E e;\n" +
		"    @NonNull E getOtherElement(Object other) {\n" +
		"      if (!(other instanceof @Marker @SingleMember(10) Outer<?>.Inner))\n" +
		"       throw new @Marker IllegalArgumentException(String.valueOf(other));\n" +
		"      Inner that;\n" +
		"	   that = (@Marker Inner) other;\n" +
		"      return that.e;\n" +
		"    }\n" +
		"  }\n" +
		"}";
	String expectedUnitToString =
		"class Outer<E> {\n" +
		"  class Inner {\n" +
		"    E e;\n" +
		"    Inner() {\n" +
		"      super();\n" +
		"    }\n" +
		"    @NonNull E getOtherElement(Object other) {\n" +
		"      if ((! (other instanceof @Marker @SingleMember(10) Outer<?>.Inner)))\n" +
		"          throw new @Marker IllegalArgumentException(String.valueOf(other));\n" +
		"      Inner that;\n" +
		"      that = (@Marker Inner) other;\n" +
		"      return that.e;\n" +
		"    }\n" +
		"  }\n" +
		"  Inner inner;\n" +
		"  Outer() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER , source.toCharArray(), null, "test0118", expectedUnitToString );
}

// To test Parser.consumeTypeArgument() with Type Annotations
public void test0119() throws IOException {
	String source =
		"class X<@SingleMember(1) Xp1 extends @Readonly String, @NonNull Xp2 extends @NonNull Comparable>  extends @Marker XS<@SingleMember(10) Xp2> {\n" +
		"\n" +
		"    public static void main(String @Marker [] args) {\n" +
		"        Integer w;\n" +
		"        w = new @Marker X<@Readonly @SingleMember(10) String,@Positive Integer>().get(new @Positive Integer(12));\n" +
		"        System.out.println(\"SUCCESS\");\n" +
		"	 }\n" +
		"    Xp2 get(@Marker X this, Xp2 t) {\n" +
		"        System.out.print(\"{X::get}\");\n" +
		"        return super.get(t);\n" +
		"    }\n" +
		"}\n" +
		"@Marker class XS <@NonNull XSp1> {\n" +
		"    XSp1 get(XSp1 t) {\n" +
		"		 @NonNull @SingleMember(10) Y.M mObject;\n" +
		"		 mObject = new @SingleMember(10) @NonNull Y.M();\n" +
		"        System.out.print(\"{XS::get}\");\n" +
		"        return t;\n" +
		"    }\n" +
		"}\n" +
		"class X2<T,E>{}\n" +
		"@Marker class Y extends @Marker X2<@NonNull Y.M, @NonNull @SingleMember(1) Y.N> {\n" +
		"	static class M{}\n" +
		"	static class N extends M{}\n" +
		"}\n";
	String expectedUnitToString =
		"class X<@SingleMember(1) Xp1 extends @Readonly String, @NonNull Xp2 extends @NonNull Comparable> extends @Marker XS<@SingleMember(10) Xp2> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String @Marker [] args) {\n" +
		"    Integer w;\n" +
		"    w = new @Marker X<@Readonly @SingleMember(10) String, @Positive Integer>().get(new @Positive Integer(12));\n" +
		"    System.out.println(\"SUCCESS\");\n" +
		"  }\n" +
		"  Xp2 get(@Marker X this, Xp2 t) {\n" +
		"    System.out.print(\"{X::get}\");\n" +
		"    return super.get(t);\n" +
		"  }\n" +
		"}\n" +
		"@Marker class XS<@NonNull XSp1> {\n" +
		"  XS() {\n" +
		"    super();\n" +
		"  }\n" +
		"  XSp1 get(XSp1 t) {\n" +
		"    @NonNull @SingleMember(10) Y.M mObject;\n" +
		"    mObject = new @SingleMember(10) @NonNull Y.M();\n" +
		"    System.out.print(\"{XS::get}\");\n" +
		"    return t;\n" +
		"  }\n" +
		"}\n" +
		"class X2<T, E> {\n" +
		"  X2() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class Y extends @Marker X2<@NonNull Y.M, @NonNull @SingleMember(1) Y.N> {\n" +
		"  static class M {\n" +
		"    M() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  static class N extends M {\n" +
		"    N() {\n" +
		"      super();\n" +
		"    }\n" +
		"  }\n" +
		"  Y() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0119", expectedUnitToString );
}

// To test Parser.consumeTypeArgument() with Type Annotations
public void test0120() throws IOException {
	String source =
		"class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" +
		"}\n" +
		"class Y {\n" +
		"	@Marker X<int @Marker [], short @SingleMember(1) [] @Marker [], long[] @NonNull [][], float[] @Marker [] @Normal(value = 5) [][], double[][]@Marker [] @SingleMember(10) [][], boolean[][][][][][], char[] @Marker [][][][][][], Object[][]@Marker [] @SingleMember(10) [] @Normal(value = 5) [][][][][]> x;\n" +
		"}\n";
	String expectedUnitToString =
		"class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"class Y {\n" +
		"  @Marker X<int @Marker [], short @SingleMember(1) [] @Marker [], long[] @NonNull [][], float[] @Marker [] @Normal(value = 5) [][], double[][] @Marker [] @SingleMember(10) [][], boolean[][][][][][], char[] @Marker [][][][][][], Object[][] @Marker [] @SingleMember(10) [] @Normal(value = 5) [][][][][]> x;\n" +
		"  Y() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0120", expectedUnitToString );
}

// To test Parser.consumeTypeArgumentReferenceType1() with Type Annotations
public void test0121() throws IOException {
	String source =
		"@Marker class X <@NonNull T> {\n" +
		"    protected T t;\n" +
		"    @Marker X(@NonNull T t) {\n" +
		"        this.t = t;\n" +
		"    }\n" +
		"    public static void main(String[] args) {\n" +
		"	  X<@Marker X<@Readonly @NonNull String>> xs;\n" +
		"	  xs = new @Marker X<@Marker X<@Readonly @NonNull String>>(new @Marker X<@Readonly @NonNull @SingleMember(10) String>(\"SUCCESS\"));\n" +
		"	  System.out.println(xs.t.t);\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class X<@NonNull T> {\n" +
		"  protected T t;\n" +
		"  @Marker X(@NonNull T t) {\n" +
		"    super();\n" +
		"    this.t = t;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    X<@Marker X<@Readonly @NonNull String>> xs;\n" +
		"    xs = new @Marker X<@Marker X<@Readonly @NonNull String>>(new @Marker X<@Readonly @NonNull @SingleMember(10) String>(\"SUCCESS\"));\n" +
		"    System.out.println(xs.t.t);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0121", expectedUnitToString );
}

// To test Parser.consumeTypeParameter1WithExtendsAndBounds() and Parser.consumeWildcardBoundsSuper() with
// Type Annotations
public void test0122() throws IOException {
	String source =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable<@Marker Foo1> {\n" +
		"	public int compareTo(Foo1 arg0) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n" +
		"class Foo1 {}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal (value = 5) Object & @Marker Comparable<? super @NonNull T>> {\n" +
		"    public static void main(String[] args) {\n" +
		"        new @Marker @SingleMember(10) X<@Marker Foo>();\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable<@Marker Foo1> {\n" +
		"  Foo() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public int compareTo(Foo1 arg0) {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n" +
		"class Foo1 {\n" +
		"  Foo1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal(value = 5) Object & @Marker Comparable<? super @NonNull T>> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker @SingleMember(10) X<@Marker Foo>();\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0122", expectedUnitToString );
}

// To test Parser.consumeTypeParameter1WithExtendsAndBounds() with Type Annotations
public void test0123() throws IOException {
	String source =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable {\n" +
		"	public int compareTo(Object arg0) {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}\n" +
		"class Foo1 {}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal (value = 5) Object & @Marker Comparable, @NonNull V extends @Readonly Object> {\n" +
		"    public static void main(String[] args) {\n" +
		"        new @Marker @SingleMember(10) X<@Marker Foo, @SingleMember(0) Foo1>();\n" +
		"		 Class <@NonNull Foo> c;\n" +
		"    }\n" +
		"}\n";
	String expectedUnitToString =
		"@Marker class Foo extends @Marker Foo1 implements @Marker @SingleMember(10) Comparable {\n" +
		"  Foo() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public int compareTo(Object arg0) {\n" +
		"    return 0;\n" +
		"  }\n" +
		"}\n" +
		"class Foo1 {\n" +
		"  Foo1() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n" +
		"@Marker class X<@NonNull T extends @NonNull @Normal(value = 5) Object & @Marker Comparable, @NonNull V extends @Readonly Object> {\n" +
		"  X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    new @Marker @SingleMember(10) X<@Marker Foo, @SingleMember(0) Foo1>();\n" +
		"    Class<@NonNull Foo> c;\n" +
		"  }\n" +
		"}\n";
	checkParse(source.toCharArray(), null, "test0123", expectedUnitToString );
}
//To test type annotations on static class member access in a declaration
public void test0125() throws IOException {
	String source =
		"public class X extends @A(\"Hello, World!\") Y<@B @C('(') String[] @D[]> {}";
	String expectedUnitToString =
		"public class X extends @A(\"Hello, World!\") Y<@B @C(\'(\') String[] @D []> {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0125", expectedUnitToString );
}
//To test type annotations on static class member access in a declaration
public void test0126() throws IOException {
	String source =
		"public class X {\n" +
		"	@A(\"Hello, World!\") @B @C('(') String@E[] @D[] f;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A(\"Hello, World!\") @B @C(\'(\') String @E [] @D [] f;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0126", expectedUnitToString );
}
//To test type annotations on static class member access in a declaration
public void test0127() throws IOException {
	String source =
		"public class X {\n" +
		"	@A(\"Hello, World!\") Y<@B @C('(') String[] @D[]> f;\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  @A(\"Hello, World!\") Y<@B @C(\'(\') String[] @D []> f;\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0127", expectedUnitToString );
}
//type class literal expression
public void test0128() throws IOException {
	String source =
	"public class X {\n" +
	"	public boolean foo(String s) {\n" +
	"		return (s instanceof @C('_') Object[]);\n" +
	"	}\n" +
	"	public Object foo1(String s) {\n" +
	"		return new @B(3) @A(\"new Object\") Object[] {};\n" +
	"	}\n" +
	"	public Class foo2(String s) {\n" +
	"		return null;\n" +
	"	}\n" +
	"	public Class foo3(String s) {\n" +
	"		return null;\n" +
	"	}\n" +
	"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public boolean foo(String s) {\n" +
		"    return (s instanceof @C(\'_\') Object[]);\n" +
		"  }\n" +
		"  public Object foo1(String s) {\n" +
		"    return new @B(3) @A(\"new Object\") Object[]{};\n" +
		"  }\n" +
		"  public Class foo2(String s) {\n" +
		"    return null;\n" +
		"  }\n" +
		"  public Class foo3(String s) {\n" +
		"    return null;\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0128", expectedUnitToString );
}
//instanceof checks
public void test0129() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void main(Object o) {\n" +
					"if (o instanceof @Readonly String) {\n" +
					"}\n" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class Clazz {\n" +
		"  public Clazz() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(Object o) {\n" +
		"    if ((o instanceof @Readonly String))\n" +
		"        {\n" +
		"        }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0129", expectedUnitToString);
}
//instanceof checks
public void test0130() throws IOException {
	String source = "public class Clazz {\n" +
					"public static void foo() {\n" +
					"	if (o instanceof @Readonly String[]) {}" +
					"}\n" +
					"}";
	String expectedUnitToString =
		"public class Clazz {\n" +
		"  public Clazz() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void foo() {\n" +
		"    if ((o instanceof @Readonly String[]))\n" +
		"        {\n" +
		"        }\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//cast
public void test0131() throws IOException {
	String source =
		"public class X {\n" +
		"	public void foo(Object o) {\n" +
		"		if (o instanceof String[][]) {\n" +
		"			String[][] tab = (@C('_') @B(3) String[] @A[]) o;\n" +
		"			System.out.println(tab.length);\n" +
		"		}\n" +
		"		System.out.println(o);\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo(Object o) {\n" +
		"    if ((o instanceof String[][]))\n" +
		"        {\n" +
		"          String[][] tab = (@C(\'_\') @B(3) String[] @A []) o;\n" +
		"          System.out.println(tab.length);\n" +
		"        }\n" +
		"    System.out.println(o);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//cast
public void test0132() throws IOException {
	String source =
		"public class X {\n" +
		"	public void foo(Object o) {\n" +
		"		if (o instanceof String[][]) {\n" +
		"			String[][] tab = (@C('_') @B(3) String@D[] @A[]) o;\n" +
		"			System.out.println(tab.length);\n" +
		"		}\n" +
		"		System.out.println(o);\n" +
		"	}\n" +
		"}";
	String expectedUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public void foo(Object o) {\n" +
		"    if ((o instanceof String[][]))\n" +
		"        {\n" +
		"          String[][] tab = (@C(\'_\') @B(3) String @D [] @A []) o;\n" +
		"          System.out.println(tab.length);\n" +
		"        }\n" +
		"    System.out.println(o);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//generic type arguments in a generic method invocation
public void test0133() throws IOException {
	String source =
		"public class X {\n" +
		"	static <T, U> T foo(T t, U u) {\n" +
		"		return t;\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		System.out.println(X.<@D() @A(value = \"hello\") String, @B X>foo(\"SUCCESS\", null));\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  static <T, U>T foo(T t, U u) {\n" +
		"    return t;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    System.out.println(X.<@D() @A(value = \"hello\") String, @B X>foo(\"SUCCESS\", null));\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//generic type arguments in a generic method invocation
public void test0134() throws IOException {
	String source =
		"public class X {\n" +
		"\n" +
		"	<T, U> T foo(T t, U u) {\n" +
		"		return t;\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		X x = new X();\n" +
		"		System.out.println(x.<@D() @A(value = \"hello\") String, @B X>foo(\"SUCCESS\", null));\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"public class X {\n" +
		"  public X() {\n" +
		"    super();\n" +
		"  }\n" +
		"  <T, U>T foo(T t, U u) {\n" +
		"    return t;\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    X x = new X();\n" +
		"    System.out.println(x.<@D() @A(value = \"hello\") String, @B X>foo(\"SUCCESS\", null));\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
//generic type arguments in a generic constructor invocation
public void test0135() throws IOException {
	String source =
		"public class X {\n" +
		"	<T, U> X(T t, U u) {\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		X x = new <@D() @A(value = \"hello\") String, @B X> X();\n" +
		"		System.out.println(x);\n" +
		"	}\n" +
		"}\n";
	String expectedUnitToString =
		"public class X {\n" +
		"  <T, U>X(T t, U u) {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] args) {\n" +
		"    X x = new <@D() @A(value = \"hello\") String, @B X>X();\n" +
		"    System.out.println(x);\n" +
		"  }\n" +
		"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=383600 -- Receiver annotation - new syntax.
public void test0136() throws IOException {
	String source =
			"public class X<T> {\n" +
			"  public class Y<K> {\n" +
			"    void foo(@Marker X<T> this) {\n" +
			"    }\n" +
			"    public class Z {\n" +
			"      Z(@D() @A(value = \"hello\") X<T>.Y<K> X.Y.this) {\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    new X<String>().new Y<Integer>().new Z();\n" +
			"  }\n" +
			"}\n";
	String expectedUnitToString =
			"public class X<T> {\n" +
			"  public class Y<K> {\n" +
			"    public class Z {\n" +
			"      Z(@D() @A(value = \"hello\") X<T>.Y<K> X.Y.this) {\n" +
			"        super();\n" +
			"      }\n" +
			"    }\n" +
			"    public Y() {\n" +
			"      super();\n" +
			"    }\n" +
			"    void foo(@Marker X<T> this) {\n" +
			"    }\n" +
			"  }\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    new X<String>().new Y<Integer>().new Z();\n" +
			"  }\n" +
			"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0130", expectedUnitToString);
}
// Support type annotations for wildcard
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388085
public void test0137() throws IOException {
	String source =
			"class X {\n" +
			"	public void main(Four<@Marker ? super String, @Marker ? extends Object> param) {\n" +
			"		One<@Marker ? extends Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String,@Marker ? extends Object>>>> one = null;\n" +
			"		Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String,@Marker ? extends Object>>> two = null;\n" +
			"		Three<@Marker ? extends Four<@Marker ? super String,@Marker ? extends Object>> three = null;\n" +
			"		Four<@Marker ? super String,@Marker ? extends Object> four = param;\n" +
			"	}\n" +
			"}\n" +
			"class One<R> {}\n" +
			"class Two<S> {}\n" +
			"class Three<T> {}\n" +
			"class Four<U, V> {}\n" +
			"@interface Marker {}";
	String expectedUnitToString =
			"class X {\n" +
			"  X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public void main(Four<@Marker ? super String, @Marker ? extends Object> param) {\n" +
			"    One<@Marker ? extends Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String, @Marker ? extends Object>>>> one = null;\n" +
			"    Two<@Marker ? extends Three<@Marker ? extends Four<@Marker ? super String, @Marker ? extends Object>>> two = null;\n" +
			"    Three<@Marker ? extends Four<@Marker ? super String, @Marker ? extends Object>> three = null;\n" +
			"    Four<@Marker ? super String, @Marker ? extends Object> four = param;\n" +
			"  }\n" +
			"}\n" +
			"class One<R> {\n" +
			"  One() {\n" +
			"    super();\n" +
			"  }\n" +
			"}\n" +
			"class Two<S> {\n" +
			"  Two() {\n" +
			"    super();\n" +
			"  }\n" +
			"}\n" +
			"class Three<T> {\n" +
			"  Three() {\n" +
			"    super();\n" +
			"  }\n" +
			"}\n" +
			"class Four<U, V> {\n" +
			"  Four() {\n" +
			"    super();\n" +
			"  }\n" +
			"}\n" +
			"@interface Marker {\n" +
			"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0137", expectedUnitToString);
}
public void test0138() throws IOException {
	String source =
			"import java.lang.annotation.Target;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [bar()] @Marker @Marker2 [];\n" +
			"		int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [X.bar2(2)] @Marker @Marker2 [];\n" +
			"	}\n" +
			"	public int bar() {\n" +
			"		return 2;\n" +
			"	}\n" +
			"	public static int bar2(int k) {\n" +
			"		return k;\n" +
			"	}\n" +
			"}\n" +
			"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"@interface Marker {}\n" +
			"@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"@interface Marker2 {}\n";
	String expectedUnitToString =
			"import java.lang.annotation.Target;\n" +
			"import static java.lang.annotation.ElementType.*;\n" +
			"public class X {\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"  public void foo() {\n" +
			"    int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [bar()] @Marker @Marker2 [];\n" +
			"    int @Marker [][][] j = new @Marker2 int @Marker @Marker2 [2] @Marker @Marker2 [X.bar2(2)] @Marker @Marker2 [];\n" +
			"  }\n" +
			"  public int bar() {\n" +
			"    return 2;\n" +
			"  }\n" +
			"  public static int bar2(int k) {\n" +
			"    return k;\n" +
			"  }\n" +
			"}\n" +
			"@Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {\n" +
			"}\n" +
			"@Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker2 {\n" +
			"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0137", expectedUnitToString);
}
// Support for annotations on ellipsis in lambda expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432574
public void test0139() throws IOException {
	String source =
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"public class X {\n" +
			"	FI fi = (String @T1[] @T1... x) -> {};\n" +
			"}\n" +
			"interface FI {\n" +
			"	void foo(String[]... x);\n" +
			"}\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface T1 {\n" +
			"}\n";
	String expectedUnitToString =
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"public class X {\n" +
			"  FI fi = (String @T1 [] @T1 ... x) ->   {\n" +
			"  };\n" +
			"  public X() {\n" +
			"    super();\n" +
			"  }\n" +
			"}\n" +
			"interface FI {\n" +
			"  void foo(String[]... x);\n" +
			"}\n" +
			"@Target(ElementType.TYPE_USE) @interface T1 {\n" +
			"}\n";
	checkParse(CHECK_PARSER, source.toCharArray(), null, "test0139", expectedUnitToString);
}
}
