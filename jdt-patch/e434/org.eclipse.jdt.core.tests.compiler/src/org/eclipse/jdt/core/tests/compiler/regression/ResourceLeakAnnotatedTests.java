/*******************************************************************************
 * Copyright (c) 2024 GK Software SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ResourceLeakAnnotatedTests extends ResourceLeakTests {

static {
	TESTS_NAMES = null; // clear forgotten filter from super class
//	TESTS_NAMES = new String[] { "test056l" };
}

// marker field that influences the call to buildTestsList():
public static final int INHERITED_DEPTH = 1;

public ResourceLeakAnnotatedTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = new TestSuite(ResourceLeakAnnotatedTests.class.getName());
	buildMinimalComplianceTestSuite(FIRST_SUPPORTED_JAVA_VERSION, 1, suite, ResourceLeakAnnotatedTests.class);
	return suite;
}

@Override
protected Map<String, String> getCompilerOptions() {
	Map<String, String> options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_AnnotationBasedResourceAnalysis, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportInsufficientResourceManagement, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportIncompatibleOwningContract, CompilerOptions.ERROR);
	return options;
}

protected static final String OWNING_JAVA = "org/eclipse/jdt/annotation/Owning.java";
protected static final String OWNING_CONTENT =
	"""
	package org.eclipse.jdt.annotation;
	import java.lang.annotation.*;
	@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE})
	public @interface Owning {}
	""";
protected static final String NOTOWNING_JAVA = "org/eclipse/jdt/annotation/NotOwning.java";
protected static final String NOTOWNING_CONTENT =
	"""
	package org.eclipse.jdt.annotation;
	import java.lang.annotation.*;
	@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
	public @interface NotOwning {}
	""";

@Override
protected String potentialLeakOrCloseNotShown(String resourceName) {
	// some leaks are considered as definite when annotations are used.
	return "Mandatory close of resource '"+resourceName+"' has not been shown\n";
}
@Override
protected String potentialLeakOrCloseNotShownAtExit(String resourceName) {
	// some leaks are considered as definite when annotations are used - variant
	// at-exit is irrelevant for unassigned closeables:
	String suffix = resourceName.startsWith("<unassigned Closeable") ? "\n" : " at this location\n";
	return "Mandatory close of resource '"+resourceName+"' has not been shown"+suffix;
}
@Override
protected String potentialOrDefiniteLeak(String string) {
	return "Resource leak: '"+string+"' is never closed\n";
}
protected String fieldDeclPrefix() {
	return "@org.eclipse.jdt.annotation.Owning "; // intentionally no linebreak
}
/** Override to add annotation to some tests from the super class. */
@Override
protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
	testFiles = addAnnotationSources(testFiles);
	super.runConformTest(testFiles, expectedOutput, customOptions);
}
/** Override to add annotation to some tests from the super class. */
@Override
protected void runLeakTest(String[] testFiles, String expectedCompileError, Map options) {
	testFiles = addAnnotationSources(testFiles);
	super.runLeakTest(testFiles, expectedCompileError, options);
}
private void runLeakTestWithAnnotations(String[] testFiles, String expectedProblems, Map<String, String> options) {
	runLeakTestWithAnnotations(testFiles, expectedProblems, options, true);
}

private void runLeakTestWithAnnotations(String[] testFiles, String expectedProblems, Map<String,String> options, boolean shouldFlushOutputDirectory) {
	if (options == null)
		options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.INFO);
	if (options.get(CompilerOptions.OPTION_ReportInsufficientResourceManagement).equals(CompilerOptions.IGNORE))
		options.put(CompilerOptions.OPTION_ReportInsufficientResourceManagement, CompilerOptions.INFO);
	testFiles = addAnnotationSources(testFiles);
	runLeakTest(testFiles, expectedProblems, options, shouldFlushOutputDirectory);
}
private String[] addAnnotationSources(String[] testFiles) {
	int length = testFiles.length;
	System.arraycopy(testFiles, 0, testFiles = new String[length+4], 0, length);
	testFiles[length+0] = OWNING_JAVA;
	testFiles[length+1] = OWNING_CONTENT;
	testFiles[length+2] = NOTOWNING_JAVA;
	testFiles[length+3] = NOTOWNING_CONTENT;
	return testFiles;
}

@Override
protected String getTest056e_log() {
	// error is more precise due to default of @Owning on method return
	return	"""
		----------
		2. ERROR in X.java (at line 11)
			FileReader reader = getReader("somefile");
			           ^^^^^^
		Resource leak: 'reader' is never closed
		----------
		""";
}
@Override
protected String getTest056y_log() {
	return """
			----------
			1. ERROR in X.java (at line 4)
				final FileReader reader31 = new FileReader("file");
				                 ^^^^^^^^
			Mandatory close of resource 'reader31' has not been shown
			----------
			2. WARNING in X.java (at line 17)
				final FileReader reader23 = new FileReader("file");
				                 ^^^^^^^^
			Potential resource leak: 'reader23' may not be closed
			----------
			""";
}
@Override
protected String getTest061l2_log() {
	return """
		----------
		1. ERROR in xy\\Leaks.java (at line 10)
			FileInputStream fileInputStream= new FileInputStream(name);
			                ^^^^^^^^^^^^^^^
		Mandatory close of resource 'fileInputStream' has not been shown
		----------
		2. ERROR in xy\\Leaks.java (at line 18)
			this(new FileInputStream("default")); // potential problem
			     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Mandatory close of resource '<unassigned Closeable value>' has not been shown
		----------
		3. ERROR in xy\\Leaks.java (at line 25)
			FileInputStream fileInputStream= new FileInputStream(name);
			                ^^^^^^^^^^^^^^^
		Mandatory close of resource 'fileInputStream' has not been shown
		----------
		""";
}
@Override
protected String getTest063c_log() {
	return """
		----------
		1. ERROR in X.java (at line 8)
			BufferedInputStream bis = new BufferedInputStream(s);
			                    ^^^
		Mandatory close of resource 'bis' has not been shown
		----------
		""";
}

@Override
protected String getTestBug440282_log() {
	return
		"----------\n" +
		"1. ERROR in ResourceLeakFalseNegative.java (at line 36)\n" +
		"	final FileInputStream in = new FileInputStream(\"/dev/null\");\n" +
		"	                      ^^\n" +
		"Resource leak: 'in' is never closed\n" +
		"----------\n" +
		"2. ERROR in ResourceLeakFalseNegative.java (at line 39)\n" +
		"	return new Foo(reader).read();\n" +
		"	       ^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n";
}

public void testBug411098_comment19_annotated() {
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"import java.io.PrintWriter;\n" +
			"import org.eclipse.jdt.annotation.Owning;\n" +
			"public class A implements AutoCloseable {\n" +
			"	@Owning PrintWriter fWriter;\n" +
			"	boolean useField = false;\n" +
			"	public void close() {\n" +
			"		PrintWriter bug= useField ? fWriter : null;\n" +
			"		System.out.println(bug);\n" +
			"	}\n" +
			"}"
		},
		"""
		----------
		1. ERROR in A.java (at line 6)
			public void close() {
			            ^^^^^^^
		Mandatory close of resource 'this.fWriter' has not been shown
		----------
		""",
		null);
}

public void testBug440282_annotated() {
	runLeakTestWithAnnotations(
		new String[] {
			"ResourceLeakFalseNegative.java",
			"import java.io.*;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"\n" +
			"public final class ResourceLeakFalseNegative {\n" +
			"\n" +
			"  private static final class Foo implements AutoCloseable {\n" +
			"    @Owning final InputStreamReader reader;\n" +
			"\n" +
			"    Foo(@Owning final InputStreamReader reader) {\n" +
			"      this.reader = reader;\n" +
			"    }\n" +
			"    \n" +
			"    public int read() throws IOException {\n" +
			"      return reader.read();\n" +
			"    }\n" +
			"\n" +
			"    public void close() throws IOException {\n" +
			"      reader.close();\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  private static final class Bar {\n" +
			"    final int read;\n" +
			"\n" +
			"    Bar(final InputStreamReader reader) throws IOException {\n" +
			"      read = reader.read();\n" +
			"    }\n" +
			"    \n" +
			"    public int read() {\n" +
			"      return read;\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public final static int foo() throws IOException {\n" +
			"    final FileInputStream in = new FileInputStream(\"/dev/null\");\n" +
			"    final InputStreamReader reader = new InputStreamReader(in);\n" +
			"    try {\n" +
			"      return new Foo(reader).read();\n" +
			"    } finally {\n" +
			"      // even though Foo is not closed, no potential resource leak is reported.\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public final static int bar() throws IOException {\n" +
			"    final FileInputStream in = new FileInputStream(\"/dev/null\");\n" +
			"    final InputStreamReader reader = new InputStreamReader(in);\n" +
			"    try {\n" +
			"      final Bar bar = new Bar(reader);\n" +
			"      return bar.read();\n" +
			"    } finally {\n" +
			"      // Removing the close correctly reports potential resource leak as a warning,\n" +
			"      // because Bar does not implement AutoCloseable.\n" +
			"      reader.close();\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public static void main(String[] args) throws IOException {\n" +
			"    for (;;) {\n" +
			"      foo();\n" +
			"      bar();\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in ResourceLeakFalseNegative.java (at line 39)\n" +
		"	return new Foo(reader).read();\n" +
		"	       ^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"2. INFO in ResourceLeakFalseNegative.java (at line 46)\n" +
		"	final FileInputStream in = new FileInputStream(\"/dev/null\");\n" +
		"	                      ^^\n" +
		"Resource \'in\' should be managed by try-with-resource\n" +
		"----------\n" +
		"3. INFO in ResourceLeakFalseNegative.java (at line 47)\n" +
		"	final InputStreamReader reader = new InputStreamReader(in);\n" +
		"	                        ^^^^^^\n" +
		"Resource \'reader\' should be managed by try-with-resource\n" +
		"----------\n",
		null);
}

public void testOwningField_NOK1() {
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			import org.eclipse.jdt.annotation.NotOwning;
			public class A implements AutoCloseable {
				@Owning PrintWriter fWriter;
				PrintWriter fOther;
				boolean useField = false;
				A(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
					fWriter = writer1; // OK
					fOther = writer2; // not sufficient
				}
				public void close() {
					PrintWriter bug= useField ? fWriter : null;
					println(bug);
					new A(null, null).fWriter.close(); // closing fWriter of other instance is no good!
				}
				void println(@NotOwning AutoCloseable rc) { System.out.println(rc); }
			}
			"""
		},
		"""
		----------
		1. INFO in A.java (at line 6)
			PrintWriter fOther;
			            ^^^^^^
		It is recommended to mark resource fields as '@Owning' to ensure proper closing
		----------
		2. ERROR in A.java (at line 8)
			A(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
			                                                   ^^^^^^^
		Mandatory close of resource 'writer2' has not been shown
		----------
		3. ERROR in A.java (at line 12)
			public void close() {
			            ^^^^^^^
		Resource leak: 'this.fWriter' is never closed
		----------
		4. ERROR in A.java (at line 15)
			new A(null, null).fWriter.close(); // closing fWriter of other instance is no good!
			^^^^^^^^^^^^^^^^^
		Resource leak: '<unassigned Closeable value>' is never closed
		----------
		""",
		null);
}

public void testOwningField_NOK2() {
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			class ASuper implements AutoCloseable {
				public void close() { /* nothing */ }
			}
			public class A extends ASuper {
				@Owning PrintWriter fWriter;
				PrintWriter fOther;
				boolean useField = false;
				A(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
					fWriter = writer1; // OK
					fOther = writer2; // not sufficient
				}
			}
			"""
		},
		"""
		----------
		1. INFO in A.java (at line 7)
			@Owning PrintWriter fWriter;
			                    ^^^^^^^
		Class with resource fields tagged as '@Owning' should implement 'close()'
		----------
		2. INFO in A.java (at line 8)
			PrintWriter fOther;
			            ^^^^^^
		It is recommended to mark resource fields as '@Owning' to ensure proper closing
		----------
		3. ERROR in A.java (at line 10)
			A(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
			                                                   ^^^^^^^
		Mandatory close of resource 'writer2' has not been shown
		----------
		""",
		null);
}

public void testOwningField_binaryField_lazyInit() {
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			public abstract class A implements AutoCloseable {
				@Owning PrintWriter fWriter;
				PrintWriter fOther;
				boolean useField = false;
			}
			"""
		},
		"""
		----------
		1. INFO in A.java (at line 4)
			@Owning PrintWriter fWriter;
			                    ^^^^^^^
		Class with resource fields tagged as '@Owning' should implement 'close()'
		----------
		2. INFO in A.java (at line 5)
			PrintWriter fOther;
			            ^^^^^^
		It is recommended to mark resource fields as '@Owning' to ensure proper closing
		----------
		""",
		null);
	runLeakTestWithAnnotations(
		new String[] {
			"AImpl.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			public class AImpl extends A {
				void init(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
					fWriter = writer1; // OK
					fOther = writer2; // not sufficient
				}
				public void close() {
					// not closing any fields
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in AImpl.java (at line 4)
			void init(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
			                                                           ^^^^^^^
		Mandatory close of resource 'writer2' has not been shown
		----------
		2. ERROR in AImpl.java (at line 8)
			public void close() {
			            ^^^^^^^
		Resource leak: 'this.fWriter' is never closed
		----------
		""",
		null,
		false);
}

public void testOwningField_binaryField_lazyInit2() {
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			public class A implements AutoCloseable {
				@Owning PrintWriter fWriter;
				PrintWriter fOther;
				boolean useField = false;
				public void close() {
					if (fWriter != null)
						fWriter.close();
				}
			}
			"""
		},
		"""
		----------
		1. INFO in A.java (at line 5)
			PrintWriter fOther;
			            ^^^^^^
		It is recommended to mark resource fields as '@Owning' to ensure proper closing
		----------
		""",
		null);
	runLeakTestWithAnnotations(
		new String[] {
			"AImpl.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			public class AImpl extends A {
				boolean should = false;
				void init(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
					fWriter = writer1; // OK
					fOther = writer2; // not sufficient
				}
				public void close() {
					if (should)
						super.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in AImpl.java (at line 5)
			void init(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
			                                                           ^^^^^^^
		Mandatory close of resource 'writer2' has not been shown
		----------
		2. ERROR in AImpl.java (at line 9)
			public void close() {
			            ^^^^^^^
		Potential resource leak: 'this.fWriter' may not be closed
		----------
		""",
		null,
		false);
}

public void testOwningField_OK() {
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			import org.eclipse.jdt.annotation.NotOwning;
			public class A implements AutoCloseable {
				@Owning PrintWriter fWriter;
				@Owning PrintWriter fOther;
				boolean useField = false;
				A(@Owning PrintWriter writer1, @Owning PrintWriter writer2) {
					fWriter = writer1;
					this.fOther = writer2;
				}
				public void close() {
					fWriter.close();
					this.fOther.close();
				}
				void println(@NotOwning AutoCloseable rc) { System.out.println(rc); }
			}
			"""
		},
		"",
		null);
}

public void testSharedField() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportInsufficientResourceManagement, CompilerOptions.WARNING);
	runLeakTestWithAnnotations(
		new String[] {
			"A.java",
			"""
			import java.io.PrintWriter;
			import org.eclipse.jdt.annotation.Owning;
			import org.eclipse.jdt.annotation.NotOwning;
			public class A {
				@NotOwning PrintWriter fWriter;
				PrintWriter fOther;
				@Owning PrintWriter fProper;
				boolean useField = false;
				A(@Owning PrintWriter writer1, PrintWriter writer2, @Owning PrintWriter writer3) {
					fWriter = writer1;
					fOther = writer2;
					fProper = writer3;
				}
			}
			"""
		},
		"""
		----------
		1. WARNING in A.java (at line 6)
			PrintWriter fOther;
			            ^^^^^^
		It is recommended to mark resource fields as '@Owning' to ensure proper closing
		----------
		2. WARNING in A.java (at line 7)
			@Owning PrintWriter fProper;
			                    ^^^^^^^
		Class with resource fields tagged as '@Owning' should implement AutoCloseable
		----------
		3. ERROR in A.java (at line 9)
			A(@Owning PrintWriter writer1, PrintWriter writer2, @Owning PrintWriter writer3) {
			                      ^^^^^^^
		Resource leak: 'writer1' is never closed
		----------
		""",
		options
		);
}

public void testOwning_receiving_parameter() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // t-w-r with pre-declared local available since 9
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import org.eclipse.jdt.annotation.Owning;
			public class X {
				void nok(@Owning AutoCloseable c) {
					System.out.print(1);
				}
				void ok(@Owning AutoCloseable c) throws Exception {
					try (c) {
						System.out.print(2);
					};
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 3)
			void nok(@Owning AutoCloseable c) {
			                               ^
		Resource leak: 'c' is never closed
		----------
		""",
		null);
}

public void testOwning_sending() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // t-w-r with pre-declared local available since 9
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import org.eclipse.jdt.annotation.Owning;
			class Rc implements AutoCloseable {
				@Override public void close() {}
			}
			public class X {
				void consume(@Owning Rc rc1) {
					try (rc1) {
						System.out.print(2);
					};
				}
				void nok() {
					Rc rc2 = new Rc();
					System.out.print(rc2);
				}
				void unsafe(boolean f) {
					Rc rc3 = new Rc();
					if (f)
						consume(rc3);
				}
				void leakAtReturn(boolean f) {
					Rc rc4 = new Rc();
					if (f)
						return;
					consume(rc4);
				}
				void ok(boolean f) {
					Rc rc5 = new Rc();
					if (f)
						consume(rc5);
					else
						rc5.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 12)
			Rc rc2 = new Rc();
			   ^^^
		Mandatory close of resource 'rc2' has not been shown
		----------
		2. ERROR in X.java (at line 16)
			Rc rc3 = new Rc();
			   ^^^
		Potential resource leak: 'rc3' may not be closed
		----------
		3. ERROR in X.java (at line 23)
			return;
			^^^^^^^
		Resource leak: 'rc4' is not closed at this location
		----------
		""",
		null);
}
public void testOwning_sending_toBinary() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // t-w-r with pre-declared local available since 9
	runLeakTestWithAnnotations(
		new String[] {
			"p/Consume.java",
			"""
			package p;
			import org.eclipse.jdt.annotation.Owning;
			public class Consume {
				public void consume(@Owning AutoCloseable rc1) {
					try (rc1) {
						System.out.print(2);
					} catch (Exception e) {
						// nothing
					}
				}
			}
			"""
		},
		"",
		null);
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			class Rc implements AutoCloseable {
				@Override public void close() {}
			}
			public class X {
				p.Consume consumer;

				void unsafe(boolean f) {
					Rc rc3 = new Rc();
					if (f)
						consumer.consume(rc3);
				}
				void leakAtReturn(boolean f) {
					Rc rc4 = new Rc();
					if (f)
						return;
					consumer.consume(rc4);
				}
				void ok(boolean f) {
					Rc rc5 = new Rc();
					if (f)
						consumer.consume(rc5);
					else
						rc5.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 8)
			Rc rc3 = new Rc();
			   ^^^
		Potential resource leak: 'rc3' may not be closed
		----------
		2. ERROR in X.java (at line 15)
			return;
			^^^^^^^
		Resource leak: 'rc4' is not closed at this location
		----------
		""",
		null,
		false);
}

public void testOwning_receiving_from_call() {
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import org.eclipse.jdt.annotation.*;
			import java.io.*;
			public class X {
				void err() {
					AutoCloseable rc1 = provideOwned();
					if (rc1 == null) System.out.print("null");
				}
				void warn() {
					AutoCloseable rc2 = provideShared(); // not responsible
					if (rc2 == null) System.out.print("null");
				}
				void err_without_local() {
					if (provideOwned() == null) System.out.print("null");
				}
				String err_wrapped() {
					BufferedInputStream bis = new BufferedInputStream(provideOwned());
					return bis.toString();
				}
				void ok() throws Exception {
					try (AutoCloseable c = provideOwned()) {
						System.out.print(2);
					};
				}
				@NotOwning AutoCloseable provideShared() {
					return null;
				}
				@Owning InputStream provideOwned() {
					return null;
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 5)
			AutoCloseable rc1 = provideOwned();
			              ^^^
		Resource leak: 'rc1' is never closed
		----------
		2. ERROR in X.java (at line 13)
			if (provideOwned() == null) System.out.print("null");
			    ^^^^^^^^^^^^^^
		Resource leak: '<unassigned Closeable value>' is never closed
		----------
		3. ERROR in X.java (at line 16)
			BufferedInputStream bis = new BufferedInputStream(provideOwned());
			                    ^^^
		Resource leak: 'bis' is never closed
		----------
		""",
		null);

}
public void testOwning_receiving_from_binaryCall() {
	runLeakTestWithAnnotations(
		new String[] {
			"p/Produce.java",
			"""
			package p;
			import org.eclipse.jdt.annotation.*;
			import java.io.*;
			public class Produce {
				public @NotOwning AutoCloseable provideShared() {
					return null;
				}
				public @Owning InputStream provideOwned() {
					return null;
				}
			}
			"""
		},
		"",
		null);
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			public class X {
				p.Produce producer;
				void err() {
					AutoCloseable rc1 = producer.provideOwned();
					if (rc1 == null) System.out.print("null");
				}
				void warn() {
					AutoCloseable rc2 = producer.provideShared();
					if (rc2 == null) System.out.print("null");
				}
				void err_without_local() {
					if (producer.provideOwned() == null) System.out.print("null");
				}
				String err_wrapped() {
					BufferedInputStream bis = new BufferedInputStream(producer.provideOwned());
					return bis.toString();
				}
				void ok() throws Exception {
					try (AutoCloseable c = producer.provideOwned()) {
						System.out.print(2);
					};
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 5)
			AutoCloseable rc1 = producer.provideOwned();
			              ^^^
		Resource leak: 'rc1' is never closed
		----------
		2. ERROR in X.java (at line 13)
			if (producer.provideOwned() == null) System.out.print("null");
			    ^^^^^^^^^^^^^^^^^^^^^^^
		Resource leak: '<unassigned Closeable value>' is never closed
		----------
		3. ERROR in X.java (at line 16)
			BufferedInputStream bis = new BufferedInputStream(producer.provideOwned());
			                    ^^^
		Resource leak: 'bis' is never closed
		----------
		""",
		null,
		false);

}
public void testOwning_return() {
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			import org.eclipse.jdt.annotation.Owning;
			public class X {
				@Owning AutoCloseable ok(String fn) throws IOException {
					return new FileInputStream(fn);
				}
				@Owning AutoCloseable somePath(String fn) throws IOException {
					FileInputStream fis = new FileInputStream(fn);
					if (fn.length() > 1)
						return fis;
					return null;
				}
				@Owning AutoCloseable mixed(String fn) throws IOException {
					FileInputStream fis = new FileInputStream(fn);
					if (fn.length() > 1)
						return new FileInputStream(fn);
					return fis;
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 11)
			return null;
			^^^^^^^^^^^^
		Mandatory close of resource 'fis' has not been shown at this location
		----------
		2. ERROR in X.java (at line 16)
			return new FileInputStream(fn);
			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Mandatory close of resource 'fis' has not been shown at this location
		----------
		""",
		null);
}
public void testUnannotated_return() {
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			public class X {
				AutoCloseable varReturn(String fn) throws IOException {
					FileInputStream fis = new FileInputStream(fn);
					return fis;
				}
				AutoCloseable unassignedReturn(String fn) throws IOException {
					return new FileInputStream(fn);
				}
				AutoCloseable passThrough(AutoCloseable rc) {
					return rc; // silent, since caller did not delegate responsibility to us
				}
			}
			"""
		},
		"""
		----------
		1. INFO in X.java (at line 5)
			return fis;
			^^^^^^^^^^^
		Enclosing method should be tagged as '@Owning' to pass the responsibility for the returned resource to the caller
		----------
		2. INFO in X.java (at line 8)
			return new FileInputStream(fn);
			       ^^^^^^^^^^^^^^^^^^^^^^^
		Enclosing method should be tagged as '@Owning' to pass the responsibility for the returned resource to the caller
		----------
		""",
		null);
}
public void testNotOwning_return() {
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			import org.eclipse.jdt.annotation.*;
			public class X {
				@NotOwning AutoCloseable nok(String fn) throws IOException {
					return new FileInputStream(fn);
				}
				@NotOwning AutoCloseable somePath(String fn) throws IOException {
					FileInputStream fis = new FileInputStream(fn);
					if (fn.length() > 1)
						return fis;
					return null;
				}
				@NotOwning AutoCloseable mixed(String fn) throws IOException {
					FileInputStream fis = new FileInputStream(fn);
					if (fn.length() > 1)
						return new FileInputStream(fn);
					return fis;
				}
				@NotOwning AutoCloseable passThrough(@NotOwning AutoCloseable resource, boolean flag) {
					if (flag)
						return resource;
					return null;
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in X.java (at line 5)
			return new FileInputStream(fn);
			       ^^^^^^^^^^^^^^^^^^^^^^^
		Resource leak: '<unassigned Closeable value>' is never closed
		----------
		2. ERROR in X.java (at line 8)
			FileInputStream fis = new FileInputStream(fn);
			                ^^^
		Resource leak: 'fis' is never closed
		----------
		3. ERROR in X.java (at line 14)
			FileInputStream fis = new FileInputStream(fn);
			                ^^^
		Resource leak: 'fis' is never closed
		----------
		4. ERROR in X.java (at line 16)
			return new FileInputStream(fn);
			       ^^^^^^^^^^^^^^^^^^^^^^^
		Resource leak: '<unassigned Closeable value>' is never closed
		----------
		""",
		null);
}
public void testNotOwningCloseableClass() {
	runLeakTestWithAnnotations(
		new String[] {
			"p/A.java",
			"""
			package p;
			import org.eclipse.jdt.annotation.NotOwning;
			public @NotOwning class A implements AutoCloseable {
				public void close() { /* nothing */ }
			}
			""",
			"X.java",
			"""
			import p.A;
			public class X {
				void test() {
					new A();
				}
			}
			"""
		},
		"",
		null);
}
public void testNotOwningCloseableClass_binary() {
	runLeakTestWithAnnotations(
		new String[] {
			"p/A.java",
			"""
			package p;
			import org.eclipse.jdt.annotation.NotOwning;
			public @NotOwning class A implements AutoCloseable {
				public void close() { /* nothing */ }
			}
			"""
		},
		"",
		null);
	runLeakTestWithAnnotations(
			new String[] {
				"X.java",
				"""
				import p.A;
				public class X {
					void test() {
						new A();
					}
				}
				"""
			},
			"",
			null,
			false);
}
public void testInheritance() {
	runLeakTestWithAnnotations(
		new String[] {
			"Super.java",
			"""
			import org.eclipse.jdt.annotation.*;
			public class Super implements AutoCloseable {
				@Owning AutoCloseable f1;
				@Owning AutoCloseable f3;
				@Owning AutoCloseable ok1(@Owning AutoCloseable rc1, @NotOwning AutoCloseable rc2) {
					return rc1;
				}
				AutoCloseable nok1(@Owning AutoCloseable rc1, AutoCloseable rc2, @Owning AutoCloseable rc3) {
					this.f1 = rc1;
					this.f3 = rc3;
					return rc2;
				}
				public void close() throws Exception {
					this.f1.close();
					if (this.f3 != null)
						this.f3.close();
				}
			}
			""",
			"Sub.java",
			"""
			import org.eclipse.jdt.annotation.*;
			public class Sub extends Super {
				@Override @Owning AutoCloseable ok1(@Owning AutoCloseable rc1, @NotOwning AutoCloseable rc2) {
					return rc1;
				}
				@Override @Owning AutoCloseable nok1(AutoCloseable rc1, @NotOwning AutoCloseable rc2, @NotOwning AutoCloseable rc3) {
					return rc1;
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in Sub.java (at line 6)
			@Override @Owning AutoCloseable nok1(AutoCloseable rc1, @NotOwning AutoCloseable rc2, @NotOwning AutoCloseable rc3) {
			          ^^^^^^^
		Unsafe redefinition, super method is not tagged as '@Owning'
		----------
		2. ERROR in Sub.java (at line 6)
			@Override @Owning AutoCloseable nok1(AutoCloseable rc1, @NotOwning AutoCloseable rc2, @NotOwning AutoCloseable rc3) {
			                                                   ^^^
		Unsafe redefinition, super method tagged this parameter as '@Owning'
		----------
		3. ERROR in Sub.java (at line 6)
			@Override @Owning AutoCloseable nok1(AutoCloseable rc1, @NotOwning AutoCloseable rc2, @NotOwning AutoCloseable rc3) {
			                                                                                                               ^^^
		Unsafe redefinition, super method tagged this parameter as '@Owning'
		----------
		""",
		null);
}
public void testCustomWrapperResource() {
	runLeakTestWithAnnotations(
		new String[] {
			"p1/C.java",
			"""
			package p1;
			import org.eclipse.jdt.annotation.*;
			import java.io.*;
			public class C implements AutoCloseable {
				@Owning InputStream fInput;
				public C(@Owning InputStream input) {
					this.fInput = input;
				}
				public void close() throws Exception {
					this.fInput.close();
				}
				static void test1(@Owning InputStream input) {
					C c = new C(input);
				}
				static void test2(@Owning InputStream input) throws Exception {
					C c = new C(input);
					input.close(); // now C is resource-less
				}
				static void test3(String name) throws Exception {
					FileInputStream fis = new FileInputStream(name);
					C c = new C(fis);
					if (name == null)
						fis.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in p1\\C.java (at line 13)
			C c = new C(input);
			  ^
		Resource leak: 'c' is never closed
		----------
		2. INFO in p1\\C.java (at line 16)
			C c = new C(input);
			  ^
		Resource 'c' should be managed by try-with-resource
		----------
		3. ERROR in p1\\C.java (at line 21)
			C c = new C(fis);
			  ^
		Potential resource leak: 'c' may not be closed
		----------
		""",
		null);
}
public void testCustomWrapperResource_binary() {
	runLeakTestWithAnnotations(
		new String[] {
			"p1/C.java",
			"""
			package p1;
			import org.eclipse.jdt.annotation.*;
			import java.io.*;
			public class C implements AutoCloseable {
				@Owning InputStream fInput;
				public C(@Owning InputStream input) {
					this.fInput = input;
				}
				public void close() throws Exception {
					this.fInput.close();
				}
			}
			"""
		},
		"",
		null);
	// challenge C.class
	runLeakTestWithAnnotations(
		new String[] {
			"D.java",
			"""
			import java.io.*;
			import p1.C;
			class D {
				void test3(String name) throws Exception {
					FileInputStream fis = new FileInputStream(name);
					C c = new C(fis);
					if (name == null)
						fis.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in D.java (at line 6)
			C c = new C(fis);
			  ^
		Potential resource leak: 'c' may not be closed
		----------
		""",
		null,
		false);

}
public void testSubclassingWrapperResource() {
	runLeakTestWithAnnotations(
		new String[] {
			"p1/C.java",
			"""
			package p1;
			import org.eclipse.jdt.annotation.*;
			import java.io.*;
			public class C extends BufferedInputStream {
				public C(@Owning InputStream input) {
					super(input); // should not complain, super param is implicilty @Owning
				}
				static void test1(@Owning InputStream input) {
					C c = new C(input);
				}
				static void test2(@Owning InputStream input) throws Exception {
					C c = new C(input);
					input.close(); // now C is resource-less
				}
				static void test3(String name) throws Exception {
					FileInputStream fis = new FileInputStream(name);
					C c = new C(fis);
					if (name == null)
						fis.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in p1\\C.java (at line 9)
			C c = new C(input);
			  ^
		Resource leak: 'c' is never closed
		----------
		2. INFO in p1\\C.java (at line 12)
			C c = new C(input);
			  ^
		Resource 'c' should be managed by try-with-resource
		----------
		3. ERROR in p1\\C.java (at line 17)
			C c = new C(fis);
			  ^
		Potential resource leak: 'c' may not be closed
		----------
		""",
		null);
}
public void testWrappingTwoResources() {
	runLeakTestWithAnnotations(
		new String[] {
			"X.java",
			"""
			import java.io.*;
			import org.eclipse.jdt.annotation.*;
			public class X implements AutoCloseable {
				private final @Owning DataInputStream fDataIn;
				private final @Owning DataOutputStream fDataOut;
				public X(@Owning InputStream in, @Owning OutputStream out) {
					fDataIn = new DataInputStream(new BufferedInputStream(in));
					fDataOut = new DataOutputStream(new BufferedOutputStream(out));
				}
				public void close() throws IOException {
					fDataIn.close();
					fDataOut.close();
				}
			}
			"""
		},
		"",
		null);
}
public void testConsumingMethod_nok() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runLeakTestWithAnnotations(
		new String[] {
			"F.java",
			"""
			import org.eclipse.jdt.annotation.*;
			public class F implements AutoCloseable {
				@Owning AutoCloseable rc1;
				@Owning AutoCloseable rc2;
				public void close() throws Exception { consume(); }
				public void consume(@Owning F this) throws Exception {
					rc1.close();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in F.java (at line 6)
			public void consume(@Owning F this) throws Exception {
			            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Resource leak: 'this.rc2' is never closed
		----------
		""",
		null);
}
public void testConsumingMethodUse() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runLeakTestWithAnnotations(
		new String[] {
			"F.java",
			"""
			import org.eclipse.jdt.annotation.*;
			public class F implements AutoCloseable {
				public void close() {}
				public void consume(@Owning F this) {
				}
				static void test() {
					F f = new F();
					f.consume();
				}
			}
			"""
		},
		"""
		----------
		1. INFO in F.java (at line 7)
			F f = new F();
			  ^
		Resource 'f' should be managed by try-with-resource
		----------
		""",
		null);
}
public void testConsumingMethodUse_binary() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runLeakTestWithAnnotations(
			new String[] {
				"p1/F.java",
				"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class F implements AutoCloseable {
					public void close() {}
					public void consume(@Owning F this) {
					}
				}
				"""
			},
			"",
			null);
	runLeakTestWithAnnotations(
			new String[] {
				"Test.java",
				"""
				import p1.F;
				public class Test {
					static void test() {
						F f = new F();
						f.consume();
					}
				}
				"""
			},
			"""
			----------
			1. INFO in Test.java (at line 4)
				F f = new F();
				  ^
			Resource 'f' should be managed by try-with-resource
			----------
			""",
			null,
			false);
}
public void testGH2207_2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTestWithAnnotations(
		new String[] {
			"ResourceLeakTest.java",
			"""
			import org.eclipse.jdt.annotation.Owning;

			class RC implements AutoCloseable {
				public void close() {}
			}
			interface ResourceProducer {
				@Owning RC newResource();
			}
			public class ResourceLeakTest {

				public void test() {
					consumerOK(() -> new RC());
				}
				void consumerOK(ResourceProducer producer) {
					try (RC ac = producer.newResource()) {
						System.out.println(ac);
					}
				}
				void consumerNOK(ResourceProducer producer) {
					RC ac = producer.newResource();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in ResourceLeakTest.java (at line 20)
			RC ac = producer.newResource();
			   ^^
		Resource leak: \'ac\' is never closed
		----------
		""",
		options);
}
public void testGH2207_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTestWithAnnotations(
		new String[] {
			"ResourceLeakTest.java",
			"""
			import org.eclipse.jdt.annotation.NotOwning;

			class RC implements AutoCloseable {
				public void close() {}
			}
			interface ResourceProducer {
				@NotOwning RC newResource();
			}
			public class ResourceLeakTest {

				public void test(@NotOwning RC rcParm) {
					consumer(() -> new RC());
					consumer(() -> rcParm);
				}
				void consumer(ResourceProducer producer) {
					RC ac = producer.newResource();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in ResourceLeakTest.java (at line 12)
			consumer(() -> new RC());
			               ^^^^^^^^
		Resource leak: \'<unassigned Closeable value>\' is never closed
		----------
		""",
		options);
}
public void testGH2207_4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTestWithAnnotations(
		new String[] {
			"ResourceLeakTest.java",
			"""
			import org.eclipse.jdt.annotation.NotOwning;

			class RC implements AutoCloseable {
				public void close() {}
			}
			interface ResourceProducer {
				@NotOwning RC newResource();
			}
			public class ResourceLeakTest {

				public void test(@NotOwning RC rcParm) {
					consumer(() -> new RC());
					consumer(() -> rcParm);
				}
				void consumer(ResourceProducer producer) {
					RC ac = producer.newResource();
				}
			}
			"""
		},
		"""
		----------
		1. ERROR in ResourceLeakTest.java (at line 12)
			consumer(() -> new RC());
			               ^^^^^^^^
		Resource leak: \'<unassigned Closeable value>\' is never closed
		----------
		""",
		options);
}
public void testGH2161_staticBlock() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTestWithAnnotations(
		new String[] {
			"ClassWithStatics.java",
			"""
			import org.eclipse.jdt.annotation.*;
			class RC implements AutoCloseable {
				public void close() {}
			}
			public class ClassWithStatics {

				private static AutoCloseable f1;
				protected static @Owning AutoCloseable f2;
				public static @NotOwning AutoCloseable f3;
				static @SuppressWarnings("resource") @Owning AutoCloseable fSilent;

				static {
					f1 = new RC();
					System.out.print(f1); // avoid unused warning
					f2 = new RC();
					f3 = new RC();
					fSilent = new RC();
				}
			}
			"""
		},
		"""
		----------
		1. INFO in ClassWithStatics.java (at line 7)
			private static AutoCloseable f1;
			                             ^^
		It is not recommended to hold a resource in a static field
		----------
		2. INFO in ClassWithStatics.java (at line 8)
			protected static @Owning AutoCloseable f2;
			                                       ^^
		It is not recommended to hold a resource in a static field
		----------
		3. INFO in ClassWithStatics.java (at line 9)
			public static @NotOwning AutoCloseable f3;
			                                       ^^
		It is not recommended to hold a resource in a static field
		----------
		4. ERROR in ClassWithStatics.java (at line 13)
			f1 = new RC();
			     ^^^^^^^^
		Mandatory close of resource \'<unassigned Closeable value>\' has not been shown
		----------
		5. ERROR in ClassWithStatics.java (at line 16)
			f3 = new RC();
			     ^^^^^^^^
		Resource leak: \'<unassigned Closeable value>\' is never closed
		----------
		""",
		options);
}
public void testGH2161_initializers() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTestWithAnnotations(
		new String[] {
			"ClassWithStatics.java",
			"""
			import org.eclipse.jdt.annotation.*;
			import java.io.StringWriter;
			class RC implements AutoCloseable {
				public void close() {}
			}
			public class ClassWithStatics {

				private static AutoCloseable f1 = new RC();
				protected static @Owning AutoCloseable f2 = new RC();
				public static @NotOwning AutoCloseable f3 = new RC();
				static @SuppressWarnings("resource") @Owning AutoCloseable fSilent = new RC();
				static StringWriter sw = new StringWriter(); // no reason to complain: white listed

				static {
					System.out.print(f1); // avoid unused warning :)
				}
			}
			"""
		},
		"""
		----------
		1. INFO in ClassWithStatics.java (at line 8)
			private static AutoCloseable f1 = new RC();
			                             ^^
		It is not recommended to hold a resource in a static field
		----------
		2. INFO in ClassWithStatics.java (at line 9)
			protected static @Owning AutoCloseable f2 = new RC();
			                                       ^^
		It is not recommended to hold a resource in a static field
		----------
		3. INFO in ClassWithStatics.java (at line 10)
			public static @NotOwning AutoCloseable f3 = new RC();
			                                       ^^
		It is not recommended to hold a resource in a static field
		----------
		""",
		options);
}
public void testGH2635() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTestWithAnnotations(
		new String[] {
			"owning_test/OwningTest.java",
			"""
			package owning_test;

			import java.io.FileInputStream;
			import java.io.FileNotFoundException;
			import java.io.IOException;
			import java.io.InputStream;

			import org.eclipse.jdt.annotation.Owning;

			public class OwningTest implements AutoCloseable {

				@Owning
				private InputStream fileInputStream;

				@SuppressWarnings("unused")
				private NotOwningTest cacheUser;

				public void initialise() throws FileNotFoundException {
				  fileInputStream = new FileInputStream("test.txt");
				  cacheUser = new NotOwningTest(fileInputStream);
				}

				@Override
				public void close() throws IOException {
					fileInputStream.close();
				}

			}
			""",
			"owning_test/NotOwningTest.java",
			"""
			package owning_test;

			import java.io.InputStream;

			import org.eclipse.jdt.annotation.NotOwning;

			public class NotOwningTest {

				// If get a warning here - "It is recommended to mark resource fields as '@Owning' to ensure proper closing"
				@NotOwning
				private InputStream cacheInputStream;

				NotOwningTest(InputStream aCacheInputStream) {
					cacheInputStream = aCacheInputStream;
				}

			}
			"""
		},
		"",
		options);
}
}
