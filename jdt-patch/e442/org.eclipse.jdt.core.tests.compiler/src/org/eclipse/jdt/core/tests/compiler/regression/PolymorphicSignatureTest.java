/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;
import org.eclipse.jdt.core.util.ClassFormatException;

@SuppressWarnings({ "rawtypes" })
@RunJavac
public class PolymorphicSignatureTest extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "testBug515863" };
	}
	public PolymorphicSignatureTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}
	public static Class testClass() {
		return PolymorphicSignatureTest.class;
	}

	public void test0001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.*;\n" +
				"public class X {\n" +
				"   public static void main(String[] args) throws Throwable{\n" +
				"      MethodType mt; MethodHandle mh; \n" +
				"      MethodHandles.Lookup lookup = MethodHandles.lookup();\n" +
				"      mt = MethodType.methodType(String.class, char.class, char.class);\n"+
				"      mh = lookup.findVirtual(String.class, \"replace\", mt);\n"+
				"      String s = (String) mh.invokeExact(\"daddy\",'d','n');\n"+
				"      System.out.println(s);\n"+
				"   }\n" +
				"}\n"
			},
			"nanny");
	}
	public void test0002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.invoke.MethodHandles.*; \n" +
				"import java.lang.invoke.MethodHandle;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) throws Throwable {\n" +
				"		MethodHandle mh = dropArguments(insertArguments(identity(int.class), 0, 42), 0, Object[].class);\n" +
				"		int value = (int)mh.invokeExact(new Object[0]);\n" +
				"		System.out.println(value);\n"+
				"	}\n" +
				"}"
			},
			"42");
	}
	public void testBug515863() {
		runConformTest(
			new String[] {
				"Test.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.Collections;\n" +
				"\n" +
				"public class Test {\n" +
				"	\n" +
				"	public void foo() throws Throwable {\n" +
				"		\n" +
				"		MethodHandle mh = null;\n" +
				"		mh.invoke(null);                           // works, no issues.\n" +
				"		mh.invoke(null, new ArrayList<>());        // Bug 501457 fixed this\n" +
				"		mh.invoke(null, Collections.emptyList());  // This triggers UOE\n" +
				"		\n" +
				"	}\n" +
				"}\n"
			});
	}
	public void testBug475996() {
		if (!isJRE9Plus)
			return; // VarHandle is @since 9
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.VarHandle;\n" +
				"public class X<T> {\n" +
				"	static class Token {}\n" +
				"	Token NIL = new Token();\n" +
				"	VarHandle RESULT;\n" +
				"	void call(T t) {\n" +
				"		RESULT.compareAndSet(this, null, (t==null) ? NIL : t);\n" +
				"	}\n" +
				"" +
				"}\n"
			});
	}
	public void testGH3651() throws ClassFormatException, IOException {
		Runner runner = new Runner();
		String source = """
				import java.lang.invoke.VarHandle;
				class VarHandleCast<V> {
				     VarHandle vh;
				     V method(Object obj) {
				         return (V)vh.getAndSet(this, obj);
				     }
				}
				""";
		runner.testFiles = new String[] { "VarHandleCast.java", source };
		runner.expectedCompilerLog = """
			----------
			1. WARNING in VarHandleCast.java (at line 5)
				return (V)vh.getAndSet(this, obj);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to V
			----------
			""";
		runner.javacTestOptions = JavacHasABug.JavacBug8343286;
		runner.runWarningTest();

		String expectedOutput = """
			  // Method descriptor #19 (Ljava/lang/Object;)Ljava/lang/Object;
			  // Signature: (Ljava/lang/Object;)TV;
			  // Stack: 3, Locals: 2
			  java.lang.Object method(java.lang.Object obj);
			     0  aload_0 [this]
			     1  getfield VarHandleCast.vh : java.lang.invoke.VarHandle [22]
			     4  aload_0 [this]
			     5  aload_1 [obj]
			     6  invokevirtual java.lang.invoke.VarHandle.getAndSet(VarHandleCast, java.lang.Object) : java.lang.Object [24]
			     9  areturn
			      Line numbers:
			        [pc: 0, line: 5]
			      Local variable table:
			        [pc: 0, pc: 10] local: this index: 0 type: VarHandleCast
			        [pc: 0, pc: 10] local: obj index: 1 type: java.lang.Object
			      Local variable type table:
			        [pc: 0, pc: 10] local: this index: 0 type: VarHandleCast<V>
			""";
		checkClassFile("VarHandleCast", source, expectedOutput);
	}
	public void testGH3651_noCheckcast() throws ClassFormatException, IOException {
		String source = """
				import java.lang.invoke.MethodHandle;
				import java.util.function.Function;

				public class CheckCast {
				    static Function<Object, String> createValueGetter(MethodHandle methodHandle) {
				        return a -> {
				            try {
				                return (String) methodHandle.invokeExact(a); // CHECKCAST String added
				            } catch (Throwable e) {
				                throw new IllegalStateException(e);
				            }
				        };
				    }
				}
				""";
		runConformTest(new String[] { "CheckCast.java", source });
		String expectedOutput = """
			  // Method descriptor #24 (Ljava/lang/invoke/MethodHandle;Ljava/lang/Object;)Ljava/lang/String;
			  // Stack: 3, Locals: 3
			  private static synthetic java.lang.String lambda$0(java.lang.invoke.MethodHandle arg0, java.lang.Object a);
			     0  aload_0 [arg0]
			     1  aload_1 [a]
			     2  invokevirtual java.lang.invoke.MethodHandle.invokeExact(java.lang.Object) : java.lang.String [25]
			     5  areturn
			     6  astore_2 [e]
			     7  new java.lang.IllegalStateException [31]
			    10  dup
			    11  aload_2 [e]
			    12  invokespecial java.lang.IllegalStateException(java.lang.Throwable) [33]
			    15  athrow
			      Exception Table:
			        [pc: 0, pc: 5] -> 6 when : java.lang.Throwable
			      Line numbers:
			        [pc: 0, line: 8]
			        [pc: 6, line: 9]
			        [pc: 7, line: 10]
			      Local variable table:
			        [pc: 0, pc: 16] local: a index: 1 type: java.lang.Object
			        [pc: 7, pc: 16] local: e index: 2 type: java.lang.Throwable
			      Stack map table: number of frames 1
			        [pc: 6, same_locals_1_stack_item, stack: {java.lang.Throwable}]
			""";
		checkClassFile("CheckCast", source, expectedOutput);
	}
}
