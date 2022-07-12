/*******************************************************************************
 * Copyright (c) 2011, 2020 GK Software SE and others.
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
 *     Nikolay Metchev (nikolaymetchev@gmail.com) - Contributions for
 *								bug 411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.IrritantSet;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ResourceLeakTests extends AbstractRegressionTest {

// well-known helper classes:
private static final String GUAVA_CLOSEABLES_JAVA = "com/google/common/io/Closeables.java";
private static final String GUAVA_CLOSEABLES_CONTENT = "package com.google.common.io;\n" +
	"public class Closeables {\n" +
	"    public static void closeQuietly(java.io.Closeable closeable) {}\n" +
	"    public static void close(java.io.Closeable closeable, boolean flag) {}\n" +
	"}\n";
private static final String APACHE_DBUTILS_JAVA = "org/apache/commons/dbutils/DbUtils.java";
private static final String APACHE_DBUTILS_CONTENT = "package org.apache.commons.dbutils;\n" +
	"import java.sql.*;\n" +
	"public class DbUtils {\n" +
	"    public static void close(Connection connection) {}\n" +
	"    public static void close(ResultSet resultSet) {}\n" +
	"    public static void close(Statement statement) {}\n" +
	"    public static void closeQuietly(Connection connection) {}\n" +
	"    public static void closeQuietly(ResultSet resultSet) {}\n" +
	"    public static void closeQuietly(Statement statement) {}\n" +
	"    public static void closeQuietly(Connection conn, Statement stmt, ResultSet rs) {}\n" +
	"}\n";

// one.util.streamex.StreamEx stub
private static final String STREAMEX_JAVA = "one/util/streamex/StreamEx.java";
private static final String STREAMEX_CONTENT = "package one.util.streamex;\n" +
	"import java.util.stream.*;\n" +
	"public abstract class StreamEx<T> implements Stream<T> {\n" +
	"    public static <T> StreamEx<T> create() { return null; }\n" +
	"}\n";

static {
//	TESTS_NAMES = new String[] { "testBug463320" };
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public ResourceLeakTests(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(ResourceLeakTests.class);
}

void runTestsExpectingErrorsOnlyIn17(String[] testFiles, String errorsIn17, Map options) {
	if (this.complianceLevel >= ClassFileConstants.JDK1_7)
		runLeakTest(testFiles, errorsIn17, options);
	else
		runConformTest(testFiles, "", null, true, null, options, null);
}

protected void runLeakTest(String[] testFiles, String expectedCompileError, Map options) {
	runNegativeTest(testFiles, expectedCompileError, null, true, options, null, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}

protected void runLeakWarningTest(String[] testFiles, String expectedCompileError, Map options) {
	runNegativeTest(testFiles, expectedCompileError, null, true, options, null, JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without ever closing it.
public void test056() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
// not invoking any methods on FileReader, try to avoid necessary call to superclass() in the compiler
//			"        char[] in = new char[50];\n" +
//			"        fileReader.read(in);\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		"Resource leak: 'fileReader' is never closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable and closes it but not protected by t-w-r nor regular try-finally
public void test056a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"		 fileReader.close();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		"Resource 'fileReader' should be managed by try-with-resource\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable and closes it properly in a finally block
public void test056b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        try {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } finally {\n" +
			"		     fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ioex) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable properly within try-with-resources.
public void test056c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        try (FileReader fileReader = new FileReader(file)) {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"		 }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ioex) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses two AutoCloseables (testing independent analysis)
//- one closeable may be unclosed at a conditional return
//- the other is only conditionally closed
public void test056d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean flag1, boolean flag2) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        char[] in = new char[50];\n" +
			"        FileReader fileReader1 = new FileReader(file);\n" +
			"        fileReader1.read(in);\n" +
			"        FileReader fileReader2 = new FileReader(file);\n" +
			"        fileReader2.read(in);\n" +
			"        if (flag1) {\n" +
			"            fileReader2.close();\n" +
			"            return;\n" +
			"        } else if (flag2) {\n" +
			"            fileReader2.close();\n" +
			"        }\n" +
			"        fileReader1.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(false, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 10)\n" +
		"	FileReader fileReader2 = new FileReader(file);\n" +
		"	           ^^^^^^^^^^^\n" +
		"Potential resource leak: 'fileReader2' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		"Resource leak: 'fileReader1' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses two AutoCloseables (testing independent analysis)
//- one closeable may be unclosed at a conditional return
//- the other is only conditionally closed
public void test056d_suppress() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // annotations used
	Map options = getCompilerOptions();
	enableAllWarningsForIrritants(options, IrritantSet.RESOURCE);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean flag1, boolean flag2) throws IOException {\n" +
			"        @SuppressWarnings(\"resource\") File file = new File(\"somefile\"); // unnecessary suppress\n" +
			"        char[] in = new char[50];\n" +
			"        FileReader fileReader1 = new FileReader(file);\n" +
			"        fileReader1.read(in);\n" +
			"        @SuppressWarnings(\"resource\") FileReader fileReader2 = new FileReader(file); // useful suppress\n" +
			"        fileReader2.read(in);\n" +
			"        if (flag1) {\n" +
			"            fileReader2.close();\n" +
			"            return; // not suppressed\n" +
			"        } else if (flag2) {\n" +
			"            fileReader2.close();\n" +
			"        }\n" +
			"        fileReader1.close();\n" +
			"    }\n" +
			"    @SuppressWarnings(\"resource\") // useful suppress\n" +
			"    void bar() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(false, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	@SuppressWarnings(\"resource\") File file = new File(\"somefile\"); // unnecessary suppress\n" +
		"	                  ^^^^^^^^^^\n" +
		"Unnecessary @SuppressWarnings(\"resource\")\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	return; // not suppressed\n" +
		"	^^^^^^^\n" +
		"Resource leak: 'fileReader1' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// one method returns an AutoCleasble, a second method uses this object without ever closing it.
public void test056e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    FileReader getReader(String filename) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        return fileReader;\n" + 		// don't complain here, pass responsibility to caller
			"    }\n" +
			"    void foo() throws IOException {\n" +
			"        FileReader reader = getReader(\"somefile\");\n" +
			"        char[] in = new char[50];\n" +
			"        reader.read(in);\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	FileReader reader = getReader(\"somefile\");\n" +
		"	           ^^^^^^\n" +
		"Potential resource leak: \'reader\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method explicitly closes its AutoCloseable rather than using t-w-r
public void test056f() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = null;\n" +
			"        try {\n" +
			"            fileReader = new FileReader(file);\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"        } finally {\n" +
			"            fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = null;\n" +
		"	           ^^^^^^^^^^\n" +
		"Resource 'fileReader' should be managed by try-with-resource\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned
public void test056g() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        fileReader = new FileReader(file);\n" +
			"        fileReader.read(in);\n" +
			"        fileReader.close();\n" +
			"        fileReader = null;\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	fileReader = new FileReader(file);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'fileReader' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned after null-assigned
public void test056g2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        fileReader = null;\n" +
			"        fileReader = new FileReader(file);\n" + // don't complain again, fileReader is null, so nothing can leak here
			"        fileReader.read(in);\n" +
			"        fileReader.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	fileReader = null;\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'fileReader' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// two AutoCloseables at different nesting levels (anonymous local type)
public void test056h() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        final File file = new File(\"somefile\");\n" +
			"        final FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        new Runnable() {\n public void run() {\n" +
			"            try {\n" +
			"                fileReader.close();\n" +
			"                FileReader localReader = new FileReader(file);\n" +
			"            } catch (IOException ex) { /* nop */ }\n" +
			"        }}.run();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	final FileReader fileReader = new FileReader(file);\n" +
		"	                 ^^^^^^^^^^\n" +
		"Potential resource leak: 'fileReader' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	FileReader localReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^^\n" +
		"Resource leak: 'localReader' is never closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean f1, boolean f2) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        if (f1) {\n" +
			"            FileReader fileReader = new FileReader(file); // err: not closed\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            while (true) {\n" +
			"                 FileReader loopReader = new FileReader(file); // don't warn, properly closed\n" +
			"                 loopReader.close();" +
			"                 break;\n" +
			"            }\n" +
			"        } else {\n" +
			"            FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" +
			"            if (f2)\n" +
			"                fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(true, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	FileReader fileReader = new FileReader(file); // err: not closed\n" +
		"	           ^^^^^^^^^^\n" +
		"Resource leak: 'fileReader' is never closed\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 16)\n" +
		"	FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" +
		"	           ^^^^^^^^^^\n" +
		"Potential resource leak: 'fileReader' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method - problems ignored
public void test056i_ignore() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean f1, boolean f2) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        if (f1) {\n" +
			"            FileReader fileReader = new FileReader(file); // err: not closed\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            while (true) {\n" +
			"                 FileReader loopReader = new FileReader(file); // don't warn, properly closed\n" +
			"                 loopReader.close();" +
			"                 break;\n" +
			"            }\n" +
			"        } else {\n" +
			"            FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" +
			"            if (f2)\n" +
			"                fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean f1, boolean f2) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        if (f1) {\n" +
			"            FileReader fileReader = new FileReader(file); // properly closed\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            while (true) {\n" +
			"                  fileReader.close();\n" +
			"                  FileReader loopReader = new FileReader(file); // don't warn, properly closed\n" +
			"                  loopReader.close();\n" +
			"                  break;\n" +
			"            }\n" +
			"        } else {\n" +
			"            FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" +
			"            if (f2)\n" +
			"                fileReader.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(true, true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 18)\n" +
		"	FileReader fileReader = new FileReader(file); // warn: not closed on all paths\n" +
		"	           ^^^^^^^^^^\n" +
		"Potential resource leak: 'fileReader' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        read(fileReader);\n" +
			"    }\n" +
			"    void read(FileReader reader) { }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		"Potential resource leak: 'fileReader' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056jconditional() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean b) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        synchronized (b ? this : new X()) {\n" +
			"            new ReadDelegator(fileReader);\n" +
			"        }\n" +
			"    }\n" +
			"    class ReadDelegator { ReadDelegator(FileReader reader) { } }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo(true);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		"Potential resource leak: 'fileReader' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// many locals, some are AutoCloseable.
// Unfortunately analysis cannot respect how exception exits may affect ra3 and rb3,
// doing so would create false positives.
public void test056k() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	String expectedProblems = this.complianceLevel < ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. ERROR in X.java (at line 15)\n" +
				"	ra2 = new FileReader(file);\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Resource leak: \'ra2\' is never closed\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 28)\n" +
				"	rb2 = new FileReader(file);\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Resource leak: \'rb2\' is never closed\n" +
				"----------\n"
			:
				"----------\n" +
				"1. ERROR in X.java (at line 12)\n" +
				"	FileReader ra1 = null, ra2 = null;\n" +
				"	           ^^^\n" +
				"Resource 'ra1' should be managed by try-with-resource\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 15)\n" +
				"	ra2 = new FileReader(file);\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Resource leak: 'ra2' is never closed\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 16)\n" +
				"	FileReader ra3 = new FileReader(file);\n" +
				"	           ^^^\n" +
				"Resource 'ra3' should be managed by try-with-resource\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 25)\n" +
				"	FileReader rb1 = null, rb2 = null;\n" +
				"	           ^^^\n" +
				"Resource 'rb1' should be managed by try-with-resource\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 28)\n" +
				"	rb2 = new FileReader(file);\n" +
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Resource leak: 'rb2' is never closed\n" +
				"----------\n" +
				"6. ERROR in X.java (at line 29)\n" +
				"	FileReader rb3 = new FileReader(file);\n" +
				"	           ^^^\n" +
				"Resource 'rb3' should be managed by try-with-resource\n" +
				"----------\n";
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        int i01, i02, i03, i04, i05, i06, i07, i08, i09,\n" +
			"            i11, i12, i13, i14, i15, i16, i17, i18, i19,\n" +
			"            i21, i22, i23, i24, i25, i26, i27, i28, i29,\n" +
			"            i31, i32, i33, i34, i35, i36, i37, i38, i39,\n" +
			"            i41, i42, i43, i44, i45, i46, i47, i48, i49;\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader ra1 = null, ra2 = null;\n" +
			"        try {\n" +
			"            ra1 = new FileReader(file);\n" +
			"            ra2 = new FileReader(file);\n" +
			"            FileReader ra3 = new FileReader(file);\n" +
			"            char[] in = new char[50];\n" +
			"            ra1.read(in);\n" +
			"            ra2.read(in);\n" +
			"            ra3.close();\n" +
			"        } finally {\n" +
			"            ra1.close();\n" +
			"        }\n" +
			"        int i51, i52, i53, i54, i55, i56, i57, i58, i59, i60;\n" + // beyond this point locals are analyzed using extraBits
			"        FileReader rb1 = null, rb2 = null;\n" +
			"        try {\n" +
			"            rb1 = new FileReader(file);\n" +
			"            rb2 = new FileReader(file);\n" +
			"            FileReader rb3 = new FileReader(file);\n" +
			"            char[] in = new char[50];\n" +
			"            rb1.read(in);\n" +
			"            rb2.read(in);\n" +
			"            rb3.close();\n" +
			"        } finally {\n" +
			"            rb1.close();\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		expectedProblems,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// various non-problems
public void test056l() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	String expectedProblems = this.complianceLevel >= ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	FileReader fileReader = getReader();\n" +
				"	           ^^^^^^^^^^\n" +
				"Resource 'fileReader' should be managed by try-with-resource\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 11)\n" +
				"	FileReader r3 = getReader();\n" +
				"	           ^^\n" +
				"Resource 'r3' should be managed by try-with-resource\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 24)\n" +
				"	FileReader r2 = new FileReader(new File(\"inexist\")); // only potential problem: ctor X below might close r2\n" +
				"	           ^^\n" +
				"Potential resource leak: 'r2' may not be closed\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 25)\n" +
				"	new X(r2).foo(new FileReader(new File(\"notthere\"))); // potential problem: foo may/may not close the new FileReader\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
				"----------\n"
			:
				"----------\n" +
				"1. ERROR in X.java (at line 24)\n" +
				"	FileReader r2 = new FileReader(new File(\"inexist\")); // only potential problem: ctor X below might close r2\n" +
				"	           ^^\n" +
				"Potential resource leak: 'r2' may not be closed\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 25)\n" +
				"	new X(r2).foo(new FileReader(new File(\"notthere\"))); // potential problem: foo may/may not close the new FileReader\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
				"----------\n";
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    X(FileReader r0) {}\n" + // don't complain against argument
			"    FileReader getReader() { return null; }\n" +
			"    void foo(FileReader r1) throws IOException {\n" +
			"        FileReader fileReader = getReader();\n" +
			"        if (fileReader == null)\n" +
			"            return;\n" + // don't complain, resource is actually null
			"        FileReader r3 = getReader();\n" +
			"        if (r3 == null)\n" +
			"            r3 = new FileReader(new File(\"absent\"));\n" + // don't complain, previous resource is actually null
			"        try {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            r1.read(in);\n" +
			"        } finally {\n" +
			"            fileReader.close();\n" +
			"            r3.close();\n" +  // the effect of this close() call might be spoiled by exception in fileReader.close() above, but we ignore exception exits in the analysis
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        FileReader r2 = new FileReader(new File(\"inexist\")); // only potential problem: ctor X below might close r2\n" +
			"        new X(r2).foo(new FileReader(new File(\"notthere\"))); // potential problem: foo may/may not close the new FileReader\n" +
			"    }\n" +
			"}\n"
		},
		expectedProblems,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested try with early exit
public void test056m() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() {\n" +
			"        File file = new File(\"somefile\");" +
			"        try {\n" +
			"            FileReader fileReader = new FileReader(file);\n" +
			"            try {\n" +
			"                char[] in = new char[50];\n" +
			"                if (fileReader.read(in)==0)\n" +
			"                    return;\n" +
			"            } finally {\n" +
			"		         fileReader.close();\n" +
			"            }\n" +
			"        } catch (IOException e) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested try should not interfere with earlier analysis.
public void test056n() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"import java.io.FileNotFoundException;\n" +
			"public class X {\n" +
			"    void foo(File someFile, char[] buf) throws IOException {\n" +
			"		FileReader fr1 = new FileReader(someFile);\n" +
			"		try {\n" +
			"			fr1.read(buf);\n" +
			"		} finally {\n" +
			"			fr1.close();\n" +
			"		}\n" +
			"		try {\n" +
			"			FileReader fr3 = new FileReader(someFile);\n" +
			"			try {\n" +
			"			} finally {\n" +
			"				fr3.close();\n" +
			"			}\n" +
			"		} catch (IOException e) {\n" +
			"		}\n" +
			"	 }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo(new File(\"missing\"), new char[100]);\n" +
			"        } catch (FileNotFoundException e) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// if close is guarded by null check this should still be recognized as definitely closed
public void test056o() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"import java.io.FileNotFoundException;\n" +
			"public class X {\n" +
			"    void foo(File someFile, char[] buf) throws IOException {\n" +
			"		FileReader fr1 = null;\n" +
			"		try {\n" +
			"           fr1 = new FileReader(someFile);" +
			"			fr1.read(buf);\n" +
			"		} finally {\n" +
			"			if (fr1 != null)\n" +
			"               try {\n" +
			"                   fr1.close();\n" +
			"               } catch (IOException e) { /*do nothing*/ }\n" +
			"		}\n" +
			"	 }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo(new File(\"missing\"), new char[100]);\n" +
			"        } catch (FileNotFoundException e) {\n" +
			"            System.out.println(\"caught\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"caught", /*output*/
		null/*classLibs*/,
		true/*shouldFlush*/,
		null/*vmargs*/,
		options,
		null/*requestor*/);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a method uses an AutoCloseable without ever closing it, type from a type variable
public void test056p() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // generics used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.Reader;\n" +
			"import java.io.IOException;\n" +
			"public abstract class X <T extends Reader> {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        T fileReader = newReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"    }\n" +
			"    abstract T newReader(File file) throws IOException;\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X<FileReader>() {\n" +
			"            FileReader newReader(File f) throws IOException { return new FileReader(f); }\n" +
			"        }.foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	T fileReader = newReader(file);\n" +
		"	  ^^^^^^^^^^\n" +
		"Potential resource leak: \'fileReader\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// closed in dead code
public void test056q() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"        char[] in = new char[50];\n" +
			"        fileReader.read(in);\n" +
			"        if (2*2 == 4)\n" +
			"        	return;\n" +
			"        fileReader.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		"Resource leak: 'fileReader' is never closed\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 10)\n" +
		"	if (2*2 == 4)\n" +
		"	    ^^^^^^^^\n" +
		"Comparing identical expressions\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 12)\n" +
		"	fileReader.close();\n" +
		"	^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// properly closed, dead code in between
public void test056r() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fr = new FileReader(file);\n" +
			"  		 Object b = null;\n" +
			"        fr.close();\n" +
			"        if (b != null) {\n" +
			"            fr = new FileReader(file);\n" +
			"            return;\n" +
			"        } else {\n" +
			"            System.out.print(42);\n" +
			"        }\n" +
			"        return;     // Should not complain about fr\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	if (b != null) {\n" +
		"            fr = new FileReader(file);\n" +
		"            return;\n" +
		"        } else {\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 13)\n" +
		"	} else {\n" +
		"            System.out.print(42);\n" +
		"        }\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Statement unnecessarily nested within else clause. The corresponding then clause does not complete normally\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource inside t-w-r is re-assigned, shouldn't even record an errorLocation
public void test056s() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        try (FileReader fileReader = new FileReader(file);) {\n" +
			"            char[] in = new char[50];\n" +
			"            fileReader.read(in);\n" +
			"            fileReader = new FileReader(file);  // debug here\n" +
			"            fileReader.read(in);\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	fileReader = new FileReader(file);  // debug here\n" +
		"	^^^^^^^^^^\n" +
		"The resource fileReader of a try-with-resources statement cannot be assigned\n" +
		"----------\n",
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource is closed, dead code follows
public void test056t() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo31() throws IOException {\n" +
			"        FileReader reader = new FileReader(\"file\"); //warning\n" +
			"        if (reader != null) {\n" +
			"            reader.close();\n" +
			"        } else {\n" +
			"            // nop\n" +
			"        }\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo31();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	} else {\n" +
		"            // nop\n" +
		"        }\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource is reassigned within t-w-r with different resource
// was initially broken due to https://bugs.eclipse.org/358827
public void test056u() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo() throws Exception {\n" +
			"        FileReader reader1 = new FileReader(\"file1\");\n" +
			"        FileReader reader2 = new FileReader(\"file2\");\n" +
			"        reader2 = reader1;// this disconnects reader 2\n" +
			"        try (FileReader reader3 = new FileReader(\"file3\")) {\n" +
			"            int ch;\n" +
			"            while ((ch = reader2.read()) != -1) {\n" +
			"                System.out.println(ch);\n" +
			"                reader1.read();\n" +
			"            }\n" +
			"            reader2 = reader1; // warning 1 regarding original reader1\n" + // this warning was missing
			"            reader2 = reader1; // warning 2 regarding original reader1\n" +
			"        } finally {\n" +
			"            if (reader2 != null) {\n" +
			"                reader2.close();\n" +
			"            } else {\n" +
			"                System.out.println();\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	FileReader reader2 = new FileReader(\"file2\");\n" +
		"	           ^^^^^^^\n" +
		"Resource leak: 'reader2' is never closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	reader2 = reader1; // warning 1 regarding original reader1\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader1' is not closed at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	reader2 = reader1; // warning 2 regarding original reader1\n" +
		"	^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader1' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// scope-related pbs reported in https://bugs.eclipse.org/349326#c70 and https://bugs.eclipse.org/349326#c82
public void test056v() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	String expectedProblems = this.complianceLevel >= ClassFileConstants.JDK1_7 ?
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	FileReader reader = new FileReader(\"file\");\n" +
				"	           ^^^^^^\n" +
				"Resource leak: 'reader' is never closed\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 19)\n" +
				"	FileReader reader111 = new FileReader(\"file2\");\n" +
				"	           ^^^^^^^^^\n" +
				"Resource 'reader111' should be managed by try-with-resource\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 42)\n" +
				"	return;\n" +
				"	^^^^^^^\n" +
				"Resource leak: 'reader2' is not closed at this location\n" +
				"----------\n"
			:
				"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	FileReader reader = new FileReader(\"file\");\n" +
				"	           ^^^^^^\n" +
				"Resource leak: 'reader' is never closed\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 42)\n" +
				"	return;\n" +
				"	^^^^^^^\n" +
				"Resource leak: 'reader2' is not closed at this location\n" +
				"----------\n";
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    boolean foo1() throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            int ch;\n" +
			"            while ((ch = reader.read()) != -1) {\n" +
			"                System.out.println(ch);\n" +
			"                reader.read();\n" +
			"            }\n" +
			"            if (ch > 10) {\n" +
			"                return true;\n" +
			"            }\n" +
			"            return false;\n" + // return while resource from enclosing scope remains unclosed
			"        } finally {\n" +
			"        }\n" +
			"    }\n" +
			"    void foo111() throws Exception {\n" +
			"        FileReader reader111 = new FileReader(\"file2\");\n" +
			"        try {\n" +
			"            int ch;\n" +
			"            while ((ch = reader111.read()) != -1) {\n" +
			"                System.out.println(ch);\n" +
			"                reader111.read();\n" +
			"            }\n" +
			"            return;\n" + // this shouldn't spoil the warning "should be managed with t-w-r"
			"        } finally {\n" +
			"            if (reader111 != null) {\n" +
			"                reader111.close();\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"    void foo2() throws Exception {\n" +
			"        FileReader reader2 = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            int ch;\n" +
			"            while ((ch = reader2.read()) != -1) {\n" +
			"                System.out.println(ch);\n" +
			"                reader2.read();\n" +
			"            }\n" +
			"            if (ch > 10) {\n" +
			"                return;\n" + // potential leak
			"            }\n" +
			"        } finally {\n" +
			"        }\n" +
			"        reader2.close();\n" + // due to this close we don't say "never closed"
			"    }\n" +
			"}\n"
		},
		expectedProblems,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// end of method is dead end, but before we have both a close() and an early return
public void test056w() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    boolean foo1() throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            int ch;\n" +
			"            while ((ch = reader.read()) != -1) {\n" +
			"                System.out.println(ch);\n" +
			"                reader.read();\n" +
			"            }\n" +
			"            if (ch > 10) {\n" +
			"				 reader.close();\n" +
			"                return true;\n" +
			"            }\n" +
			"            return false;\n" +
			"        } finally {\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	return false;\n" +
		"	^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// different early exits, if no close seen report as definitely unclosed
public void test056x() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo31(boolean b) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        if (b) {\n" +
			"            reader.close();\n" +
			"        } else {\n" +
			"            return; // warning\n" +
			"        }\n" +
			"    }\n" +
			"    void foo32(boolean b) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\"); // warn here\n" +
			"        return;\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	return; // warning\n" +
		"	^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	FileReader reader = new FileReader(\"file\"); // warn here\n" +
		"	           ^^^^^^\n" +
		"Resource leak: 'reader' is never closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested method passes the resource to outside code
public void test056y() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakWarningTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo31(boolean b) throws Exception {\n" +
			"        final FileReader reader31 = new FileReader(\"file\");\n" +
			"        new Runnable() {\n" +
			"            public void run() {\n" +
			"                foo18(reader31);\n" +
			"            }\n" +
			"        }.run();\n" +
			"    }\n" +
			"    void foo18(FileReader r18) {\n" +
			"        // could theoretically close r18;\n" +
			"    }\n" +
			"    abstract class ResourceProvider {\n" +
			"        abstract FileReader provide();" +
			"    }\n" +
			"    ResourceProvider provider;" +
			"    void foo23() throws Exception {\n" +
			"        final FileReader reader23 = new FileReader(\"file\");\n" +
			"        provider = new ResourceProvider() {\n" +
			"            public FileReader provide() {\n" +
			"                return reader23;\n" + // responsibility now lies at the caller of this method
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	final FileReader reader31 = new FileReader(\"file\");\n" +
		"	                 ^^^^^^^^\n" +
		"Potential resource leak: 'reader31' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource assigned to second local and is (potentially) closed on the latter
public void test056z() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo17() throws Exception {\n" +
			"        FileReader reader17 = new FileReader(\"file\");\n" +
			"        final FileReader readerCopy = reader17;\n" +
			"        readerCopy.close();\n" +
			"    }\n" +
			"    void foo17a() throws Exception {\n" +
			"        FileReader reader17a = new FileReader(\"file\");\n" +
			"        FileReader readerCopya;" +
			"		 readerCopya = reader17a;\n" +
			"        bar(readerCopya);\n" + // potentially closes
			"    }\n" +
			"    void bar(FileReader r) {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	FileReader reader17a = new FileReader(\"file\");\n" +
		"	           ^^^^^^^^^\n" +
		"Potential resource leak: 'reader17a' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// multiple early exists from nested scopes (always closed)
public void test056zz() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo16() throws Exception {\n" +
			"        FileReader reader16 = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            reader16.close();\n " +
			"            return;\n" +
			"        } catch (RuntimeException re) {\n" +
			"            return;\n" +
			"        } catch (Error e) {\n" +
			"            return;\n" +
			"        } finally {\n" +
			"            reader16.close();\n " +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	FileReader reader16 = new FileReader(\"file\");\n" +
		"	           ^^^^^^^^\n" +
		"Resource 'reader16' should be managed by try-with-resource\n" +
		"----------\n",
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// multiple early exists from nested scopes (never closed)
public void test056zzz() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo16() throws Exception {\n" +
			"        FileReader reader16 = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            return;\n" +
			"        } catch (RuntimeException re) {\n" +
			"            return;\n" +
			"        } catch (Error e) {\n" +
			"            return;\n" +
			"        } finally {\n" +
			"            System.out.println();\n " +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	FileReader reader16 = new FileReader(\"file\");\n" +
		"	           ^^^^^^^^\n" +
		"Resource leak: 'reader16' is never closed\n" +
		"----------\n",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// explicit throw is a true method exit here
public void test056throw1() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2(boolean a, boolean b, boolean c) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        if(a)\n" +
			"            throw new Exception();    //warning 1\n" +
			"        else if (b)\n" +
			"            reader.close();\n" +
			"        else if(c)\n" +
			"            throw new Exception();    //warning 2\n" +
			"        reader.close();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	throw new Exception();    //warning 1\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	throw new Exception();    //warning 2\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: 'reader' is not closed at this location\n" +
		"----------\n",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// close() within finally provides protection for throw
public void test056throw2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
					"public class X {\n" +
					"    void foo1() throws Exception {\n" +
					"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
					"        try {\n" +
					"            reader.read();\n" +
					"            return;\n" +
					"        } catch (Exception e) {\n" +
					"            throw new Exception();\n" +
					"        } finally {\n" +
					"            reader.close();\n" +
					"        }\n" +
					"    }\n" +
					"\n" +
					"    void foo2() throws Exception {\n" +
					"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
					"        try {\n" +
					"            reader.read();\n" +
					"            throw new Exception(); // should not warn here\n" +
					"        } catch (Exception e) {\n" +
					"            throw new Exception();\n" +
					"        } finally {\n" +
					"            reader.close();\n" +
					"        }\n" +
					"    }\n" +
					"\n" +
					"    void foo3() throws Exception {\n" +
					"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
					"        try {\n" +
					"            reader.read();\n" +
					"            throw new Exception();\n" +
					"        } finally {\n" +
					"            reader.close();\n" +
					"        }\n" +
					"    }\n" +
					"}\n"
			},
			"----------\n" +
			"1. ERROR in X.java (at line 4)\n" +
			"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
			"	           ^^^^^^\n" +
			"Resource 'reader' should be managed by try-with-resource\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 16)\n" +
			"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
			"	           ^^^^^^\n" +
			"Resource 'reader' should be managed by try-with-resource\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 28)\n" +
			"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
			"	           ^^^^^^\n" +
			"Resource 'reader' should be managed by try-with-resource\n" +
			"----------\n",
			options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// close() nested within finally provides protection for throw
public void test056throw3() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2x() throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
			"        try {\n" +
			"            reader.read();\n" +
			"            throw new Exception(); // should not warn here\n" +
			"        } catch (Exception e) {\n" +
			"            throw new Exception();\n" +
			"        } finally {\n" +
			"            if (reader != null)\n" +
			"                 try {\n" +
			"                     reader.close();\n" +
			"                 } catch (java.io.IOException io) {}\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	FileReader reader = new FileReader(\"file\"); // propose t-w-r\n" +
		"	           ^^^^^^\n" +
		"Resource 'reader' should be managed by try-with-resource\n" +
		"----------\n",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// additional boolean should shed doubt on whether we reach the close() call
public void test056throw4() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2x(boolean b) throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            reader.read();\n" +
			"            throw new Exception(); // should warn here\n" +
			"        } catch (Exception e) {\n" +
			"            throw new Exception(); // should warn here\n" +
			"        } finally {\n" +
			"            if (reader != null && b)\n" + // this condition is too strong to protect reader
			"                 try {\n" +
			"                     reader.close();\n" +
			"                 } catch (java.io.IOException io) {}\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	throw new Exception(); // should warn here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	throw new Exception(); // should warn here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n",
		options);
}
// Bug 359334 - Analysis for resource leak warnings does not consider exceptions as method exit points
// similar to test056throw3() but indirectly calling close(), so doubts remain.
public void test056throw5() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.FileReader;\n" +
			"public class X {\n" +
			"    void foo2x() throws Exception {\n" +
			"        FileReader reader = new FileReader(\"file\");\n" +
			"        try {\n" +
			"            reader.read();\n" +
			"            throw new Exception(); // should warn 'may not' here\n" +
			"        } catch (Exception e) {\n" +
			"            throw new Exception(); // should warn 'may not' here\n" +
			"        } finally {\n" +
			"            doClose(reader);\n" +
			"        }\n" +
			"    }\n" +
			"    void doClose(FileReader r) { try { r.close(); } catch (java.io.IOException ex) {}}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	throw new Exception(); // should warn \'may not\' here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	throw new Exception(); // should warn \'may not\' here\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: 'reader' may not be closed at this location\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// a resource wrapper is not closed but the underlying resource is
public void test061a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);\n" +
			"        BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
			"        System.out.println(bis.available());\n" +
			"        fileStream.close();\n" +
			"    }\n" +
			"    void inline() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileInputStream fileStream;\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream = new FileInputStream(file));\n" +
			"        System.out.println(bis.available());\n" +
			"        fileStream.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ex) {" +
			"            System.out.println(\"Got IO Exception\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"Got IO Exception",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable without OS resource is not closed
public void test061b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.StringReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        StringReader string  = new StringReader(\"content\");\n" +
			"        System.out.println(string.read());\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"99", // character 'c'
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is not closed but the underlying closeable is resource-free
public void test061c() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.BufferedReader;\n" +
			"import java.io.StringReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        StringReader input = new StringReader(\"content\");\n" +
			"        BufferedReader br = new BufferedReader(input);\n" +
			"        BufferedReader doubleWrap = new BufferedReader(br);\n" +
			"        System.out.println(br.read());\n" +
			"    }\n" +
			"    void inline() throws IOException {\n" +
			"        BufferedReader br = new BufferedReader(new StringReader(\"content\"));\n" +
			"        System.out.println(br.read());\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        new X().foo();\n" +
			"    }\n" +
			"}\n"
		},
		"99",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is not closed neither is the underlying resource
public void test061d() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);\n" +
			"        BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
			"        System.out.println(bis.available());\n" +
			"    }\n" +
			"    void inline() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));\n" +
			"        System.out.println(bis2.available());\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ex) {" +
			"            System.out.println(\"Got IO Exception\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
		"	                    ^^^^^^^^^^\n" +
		"Resource leak: \'doubleWrap\' is never closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));\n" +
		"	                    ^^^^\n" +
		"Resource leak: \'bis2\' is never closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is closed closing also the underlying resource
public void test061e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    FileInputStream fis;" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);\n" +
			"        BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
			"        System.out.println(bis.available());\n" +
			"        bis.close();\n" +
			"    }\n" +
			"    void inline() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        BufferedInputStream bis2 = new BufferedInputStream(fis = new FileInputStream(file));\n" + // field assignment
			"        System.out.println(bis2.available());\n" +
			"        bis2.close();\n" +
			"        FileInputStream fileStream  = null;\n" +
			"        BufferedInputStream bis3 = new BufferedInputStream(fileStream = new FileInputStream(file));\n" +
			"        System.out.println(bis3.available());\n" +
			"        bis3.close();\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().foo();\n" +
			"        } catch (IOException ex) {" +
			"            System.out.println(\"Got IO Exception\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"Got IO Exception",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is closed closing also the underlying resource - original test case
public void test061f() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	URL url = FileLocator.toFileURL(FileLocator.find(Platform.getBundle("org.eclipse.jdt.core.tests.compiler"), new Path("META-INF/MANIFEST.MF"), null));
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.InputStream;\n" +
			"import java.io.InputStreamReader;\n" +
			"import java.io.BufferedReader;\n" +
			"import java.io.IOException;\n" +
			"import java.net.URL;\n" +
			"public class X {\n" +
			"    boolean loadURL(final URL url) throws IOException {\n" +
			"        InputStream stream = null;\n" +
			"        BufferedReader reader = null;\n" +
			"        try {\n" +
			"            stream = url.openStream();\n" +
			"            reader = new BufferedReader(new InputStreamReader(stream));\n" +
			"            System.out.println(reader.readLine());\n" +
			"        } finally {\n" +
			"            try {\n" +
			"                if (reader != null)\n" +
			"                    reader.close();\n" +
			"            } catch (IOException x) {\n" +
			"            }\n" +
			"        }\n" +
			"        return false; // 'stream' may not be closed at this location\n" +
			"    }\n" +
			"    public static void main(String[] args) throws IOException {\n" +
			"        try {\n" +
			"            new X().loadURL(new URL(\""+url.toString()+"\"));\n" +
			"        } catch (IOException ex) {\n" +
			"            System.out.println(\"Got IO Exception\"+ex);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"Manifest-Version: 1.0",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is closed closing also the underlying resource - from a real-world example
public void test061f2() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.OutputStream;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.BufferedOutputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void zork() throws IOException {\n" +
			"		try {\n" +
			"			OutputStream os = null;\n" +
			"			try {\n" +
			"				os = new BufferedOutputStream(new FileOutputStream(\"somefile\"));\n" +
			"				String externalForm = \"externalPath\";\n" +
			"			} finally {\n" +
			"				if (os != null)\n" +
			"					os.close();\n" +
			"			}\n" +
			"		} catch (IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// a resource wrapper is sent to another method affecting also the underlying resource - from a real-world example
public void test061f3() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.FileNotFoundException;\n" +
			"import java.io.InputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"public class X {\n" +
			"    String loadProfile(File profileFile) {\n" +
			"		try {\n" +
			"			InputStream stream = new BufferedInputStream(new FileInputStream(profileFile));\n" +
			"			return loadProfile(stream);\n" +
			"		} catch (FileNotFoundException e) {\n" +
			"			//null\n" +
			"		}\n" +
			"		return null;\n" +
			"	}\n" +
			"	private String loadProfile(InputStream stream) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	return loadProfile(stream);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'stream\' may not be closed at this location\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// Different points in a resource chain are closed
public void test061g() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void closeMiddle() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);\n" +
			"        BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
			"        System.out.println(bis.available());\n" +
			"        bis.close();\n" +
			"    }\n" +
			"    void closeOuter() throws IOException {\n" +
			"        File file2 = new File(\"somefile\");\n" +
			"        FileInputStream fileStream2  = new FileInputStream(file2);\n" +
			"        BufferedInputStream bis2 = new BufferedInputStream(fileStream2);\n" +
			"        BufferedInputStream doubleWrap2 = new BufferedInputStream(bis2);\n" +
			"        System.out.println(bis2.available());\n" +
			"        doubleWrap2.close();\n" +
			"    }\n" +
			"    void neverClosed() throws IOException {\n" +
			"        File file3 = new File(\"somefile\");\n" +
			"        FileInputStream fileStream3  = new FileInputStream(file3);\n" +
			"        BufferedInputStream bis3 = new BufferedInputStream(fileStream3);\n" +
			"        BufferedInputStream doubleWrap3 = new BufferedInputStream(bis3);\n" +
			"        System.out.println(doubleWrap3.available());\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 26)\n" +
		"	BufferedInputStream doubleWrap3 = new BufferedInputStream(bis3);\n" +
		"	                    ^^^^^^^^^^^\n" +
		"Resource leak: \'doubleWrap3\' is never closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// Different points in a resource chain are potentially closed
public void test061h() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void closeMiddle(boolean b) throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);\n" +
			"        BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
			"        System.out.println(bis.available());\n" +
			"        if (b)\n" +
			"            bis.close();\n" +
			"    }\n" +
			"    void closeOuter(boolean b) throws IOException {\n" +
			"        File file2 = new File(\"somefile\");\n" +
			"        FileInputStream fileStream2  = new FileInputStream(file2);\n" +
			"        BufferedInputStream dummy;\n" +
			"        BufferedInputStream bis2 = (dummy = new BufferedInputStream(fileStream2));\n" +
			"        BufferedInputStream doubleWrap2 = new BufferedInputStream(bis2);\n" +
			"        System.out.println(bis2.available());\n" +
			"        if (b)\n" +
			"            doubleWrap2.close();\n" +
			"    }\n" +
			"    void potAndDef(boolean b) throws IOException {\n" +
			"        File file3 = new File(\"somefile\");\n" +
			"        FileInputStream fileStream3  = new FileInputStream(file3);\n" +
			"        BufferedInputStream bis3 = new BufferedInputStream(fileStream3);\n" +
			"        BufferedInputStream doubleWrap3 = new BufferedInputStream(bis3);\n" +
			"        System.out.println(doubleWrap3.available());\n" +
			"        if (b) bis3.close();\n" +
			"        fileStream3.close();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
		"	                    ^^^^^^^^^^\n" +
		"Potential resource leak: \'doubleWrap\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 20)\n" +
		"	BufferedInputStream doubleWrap2 = new BufferedInputStream(bis2);\n" +
		"	                    ^^^^^^^^^^^\n" +
		"Potential resource leak: \'doubleWrap2\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// local var is re-used for two levels of wrappers
public void test061i() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.InputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void closeMiddle() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        InputStream stream  = new FileInputStream(file);\n" +
			"        stream = new BufferedInputStream(stream);\n" +
			"        InputStream middle;\n" +
			"        stream = new BufferedInputStream(middle = stream);\n" +
			"        System.out.println(stream.available());\n" +
			"        middle.close();\n" +
			"    }\n" +
			"    void closeOuter() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        InputStream stream2  = new FileInputStream(file);\n" +
			"        stream2 = new BufferedInputStream(stream2);\n" +
			"        stream2 = new BufferedInputStream(stream2);\n" +
			"        System.out.println(stream2.available());\n" +
			"        stream2.close();\n" +
			"    }\n" +
			"    void neverClosed() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        InputStream stream3  = new FileInputStream(file);\n" +
			"        stream3 = new BufferedInputStream(stream3);\n" +
			"        stream3 = new BufferedInputStream(stream3);\n" +
			"        System.out.println(stream3.available());\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 26)\n" +
		"	InputStream stream3  = new FileInputStream(file);\n" +
		"	            ^^^^^^^\n" +
		"Resource leak: \'stream3\' is never closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// self-wrapping a method argument (caused NPE UnconditionalFlowInfo.markAsDefinitelyNull(..)).
public void test061j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.InputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(InputStream stream) throws IOException {\n" +
			"        stream = new BufferedInputStream(stream);\n" +
			"        System.out.println(stream.available());\n" +
			"        stream.close();\n" +
			"    }\n" +
			"    void boo(InputStream stream2) throws IOException {\n" +
			"        stream2 = new BufferedInputStream(stream2);\n" +
			"        System.out.println(stream2.available());\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a wrapper is created in a return statement
public void test061k() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    BufferedInputStream getReader(File file) throws IOException {\n" +
			"        FileInputStream stream = new FileInputStream(file);\n" +
			"        return new BufferedInputStream(stream);\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable is assigned to a field
public void test061l() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    BufferedInputStream stream;\n" +
			"    void foo(File file) throws IOException {\n" +
			"        FileInputStream s = new FileInputStream(file);\n" +
			"        stream = new BufferedInputStream(s);\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
// a closeable is assigned to a field - constructor vs. method
public void test061l2() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"xy/Leaks.java",
			"package xy;\n" +
			"\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public class Leaks {\n" +
			"    private FileInputStream fInput;\n" +
			"\n" +
			"    Leaks(String name) throws IOException {\n" +
			"        FileInputStream fileInputStream= new FileInputStream(name);\n" +
			"        fInput= fileInputStream;\n" + // warning silenced by field assignment
			"        Objects.hashCode(fInput);\n" +
			"        \n" +
			"        init(name);\n" +
			"    }\n" +
			"    \n" +
			"    Leaks() throws IOException {\n" +
			"        this(new FileInputStream(\"default\")); // potential problem\n" +
			"    }\n" +
			"    \n" +
			"    Leaks(FileInputStream fis) throws IOException {\n" +
			"        fInput= fis;\n" +
			"    }\n" +
			"    void init(String name) throws IOException {\n" +
			"        FileInputStream fileInputStream= new FileInputStream(name);\n" +
			"        fInput= fileInputStream;\n" + // warning silenced by field assignment
			"        Objects.hashCode(fInput);\n" +
			"    }\n" +
			"    \n" +
			"    public void dispose() throws IOException {\n" +
			"        fInput.close();\n" +
			"    }\n" +
			"}\n" +
			"class Objects {\n" + // mock java.util.Objects (@since 1.7).
			"    static int hashCode(Object o) { return 13; }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in xy\\Leaks.java (at line 18)\n" +
		"	this(new FileInputStream(\"default\")); // potential problem\n" +
		"	     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
// a closeable is not assigned to a field - constructor vs. method
public void test061l3() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"xy/Leaks.java",
			"package xy;\n" +
			"\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public class Leaks {\n" +
			"\n" +
			"    Leaks(String name) throws IOException {\n" +
			"        FileInputStream fileInputStream= new FileInputStream(name);\n" +
			"        Objects.hashCode(fileInputStream);\n" +
			"        \n" +
			"        init(name);\n" +
			"    }\n" +
			"    void init(String name) throws IOException {\n" +
			"        FileInputStream fileInputStream= new FileInputStream(name);\n" +
			"        Objects.hashCode(fileInputStream);\n" +
			"    }\n" +
			"}\n" +
			"class Objects {\n" + // mock java.util.Objects (@since 1.7).
			"    static int hashCode(Object o) { return 13; }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in xy\\Leaks.java (at line 9)\n" +
		"	FileInputStream fileInputStream= new FileInputStream(name);\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'fileInputStream\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in xy\\Leaks.java (at line 15)\n" +
		"	FileInputStream fileInputStream= new FileInputStream(name);\n" +
		"	                ^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'fileInputStream\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable is passed to another method in a return statement
// example constructed after org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository#getArtifact(..)
public void test061m() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.InputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    BufferedInputStream stream;\n" +
			"    BufferedInputStream foo(File file) throws IOException {\n" +
			"        FileInputStream s = new FileInputStream(file);\n" +
			"        return check(new BufferedInputStream(s));\n" +
			"    }\n" +
			"    BufferedInputStream foo2(FileInputStream s, File file) throws IOException {\n" +
			"        s = new FileInputStream(file);\n" +
			"        return check(s);\n" +
			"    }\n" +
			"    BufferedInputStream foo3(InputStream s) throws IOException {\n" +
			"        s = check(s);\n" +
			"        return check(s);\n" +
			"    }\n" +
			"    BufferedInputStream check(InputStream s) { return null; }\n" +
			"}\n"
		},
		// TODO: also these warnings *might* be avoidable by detecting check(s) as a wrapper creation??
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	return check(new BufferedInputStream(s));\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	return check(s);\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'s\' may not be closed at this location\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 18)\n" +
		"	return check(s);\n" +
		"	^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'s\' may not be closed at this location\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper does not wrap any provided resource
public void test061n() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.PrintWriter;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        PrintWriter writer = new PrintWriter(\"filename\");\n" +
			"        writer.write(1);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	PrintWriter writer = new PrintWriter(\"filename\");\n" +
		"	            ^^^^^^\n" +
		"Resource leak: \'writer\' is never closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is closed only in its local block, underlying resource may leak
public void test061o() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean bar) throws IOException {\n" +
			"        File file = new File(\"somefil\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);   \n" +
			"        if (bar) {\n" +
			"            BufferedInputStream doubleWrap = new BufferedInputStream(bis);\n" +
			"            doubleWrap.close();\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	BufferedInputStream bis = new BufferedInputStream(fileStream);   \n" +
		"	                    ^^^\n" +
		"Potential resource leak: \'bis\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is conditionally allocated but not closed - from a real-world example
public void test061f4() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.FileNotFoundException;\n" +
			"import java.io.InputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"public class X {\n" +
			"    	void foo(File location, String adviceFilePath) throws FileNotFoundException {\n" +
			"		InputStream stream = null;\n" +
			"		if (location.isDirectory()) {\n" +
			"			File adviceFile = new File(location, adviceFilePath);\n" +
			"			stream = new BufferedInputStream(new FileInputStream(adviceFile));\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	stream = new BufferedInputStream(new FileInputStream(adviceFile));\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'stream\' may not be closed\n" + // message could be stronger, but the enclosing if blurs the picture
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a t-w-r wraps an existing resource
public void test061p() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.PrintWriter;\n" +
			"import java.io.BufferedWriter;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        PrintWriter writer = new PrintWriter(\"filename\");\n" +
			"        try (BufferedWriter bw = new BufferedWriter(writer)) {\n" +
			"            bw.write(1);\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a t-w-r potentially wraps an existing resource
// DISABLED, fails because we currently don't include t-w-r managed resources in the analysis
public void _test061q() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.PrintWriter;\n" +
			"import java.io.BufferedWriter;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo(boolean b) throws IOException {\n" +
			"        PrintWriter writer = new PrintWriter(\"filename\");\n" +
			"        if (b)\n" +
			"            try (BufferedWriter bw = new BufferedWriter(writer)) {\n" +
			"                bw.write(1);\n" +
			"            }\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	PrintWriter writer = new PrintWriter(\\\"filename\\\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'writer\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// the inner from a wrapper is returned
public void test061r() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    FileInputStream foo() throws IOException {\n" +
			"        File file = new File(\"somefil\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);   \n" +
			"        return fileStream;\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a wrapper is forgotten, the inner is closed afterwards
public void test061s() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.File;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefil\");\n" +
			"        FileInputStream fileStream  = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(fileStream);\n" +
			"        bis = null;\n" +
			"        fileStream.close();\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is never assigned
public void test062a() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        new FileOutputStream(new File(\"C:\\temp\\foo.txt\")).write(1);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	new FileOutputStream(new File(\"C:\\temp\\foo.txt\")).write(1);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n",
		options);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a freshly allocated resource is immediately closed
public void test062b() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        new FileOutputStream(new File(\"C:\\temp\\foo.txt\")).close();\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is directly passed to another method
public void test062c() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        writeIt(new FileOutputStream(new File(\"C:\\temp\\foo.txt\")));\n" +
			"    }\n" +
			"    void writeIt(FileOutputStream fos) throws IOException {\n" +
			"        fos.write(1);\n" +
			"        fos.close();\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	writeIt(new FileOutputStream(new File(\"C:\\temp\\foo.txt\")));\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is not used
public void test062d() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        new FileOutputStream(new File(\"C:\\temp\\foo.txt\"));\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	new FileOutputStream(new File(\"C:\\temp\\foo.txt\"));\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n",
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is obtained from another method
public void test063a() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void read(File file) throws IOException {\n" +
			"        FileInputStream stream = new FileInputStream(file);\n" +
			"        BufferedInputStream bis = new BufferedInputStream(stream); // never since reassigned\n" +
			"        FileInputStream stream2 = new FileInputStream(file); // unsure since passed to method\n" +
			"        bis = getReader(stream2); // unsure since obtained from method\n" +
			"        bis.available();\n" +
			"    }\n" +
			"    BufferedInputStream getReader(FileInputStream stream) throws IOException {\n" +
			"        return new BufferedInputStream(stream);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	FileInputStream stream = new FileInputStream(file);\n" +
		"	                ^^^^^^\n" +
		"Resource leak: \'stream\' is never closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	FileInputStream stream2 = new FileInputStream(file); // unsure since passed to method\n" +
		"	                ^^^^^^^\n" +
		"Potential resource leak: \'stream2\' may not be closed\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 10)\n" +
		"	bis = getReader(stream2); // unsure since obtained from method\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'bis\' may not be closed\n" +
		"----------\n",
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is obtained from a field read
public void test063b() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    FileInputStream stream;\n" +
			"    void read() throws IOException {\n" +
			"        FileInputStream s = this.stream;\n" +
			"        BufferedInputStream bis = new BufferedInputStream(s); // don't complain since s is obtained from a field\n" +
			"        bis.available();\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is assigned to a field
public void test063c() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    BufferedInputStream stream;\n" +
			"    void read() throws IOException {\n" +
			"        FileInputStream s = new FileInputStream(\"somefile\");\n" +
			"        BufferedInputStream bis = new BufferedInputStream(s);\n" +
			"        this.stream = bis;\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a resource is obtained as a method argument and/or assigned with a cast
public void test063d() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runTestsExpectingErrorsOnlyIn17(
		new String[] {
			"X.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.BufferedInputStream;\n" +
			"import java.io.InputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo( InputStream input) throws IOException {\n" +
			"        FileInputStream input1  = (FileInputStream)input;\n" +
			"        System.out.println(input1.read());\n" +
			"        input.close();\n" + // don't propose t-w-r for argument
			"    }\n" +
			"    void foo() throws IOException {\n" +
			"        InputStream input = new FileInputStream(\"somefile\");\n" +
			"        FileInputStream input1  = (FileInputStream)input;\n" +
			"        System.out.println(input1.read());\n" +
			"        input.close();\n" + // do propose t-w-r, not from a method argument
			"    }\n" +
			"    void foo3( InputStream input, InputStream input2) throws IOException {\n" +
			"        FileInputStream input1  = (FileInputStream)input;\n" + // still don't claim because obtained from outside
			"        System.out.println(input1.read());\n" +
			"        BufferedInputStream bis = new BufferedInputStream(input2);\n" +
			"        System.out.println(bis.read());\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	InputStream input = new FileInputStream(\"somefile\");\n" +
		"	            ^^^^^\n" +
		"Resource \'input\' should be managed by try-with-resource\n" +
		"----------\n",
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a resource is obtained from a field read, then re-assigned
public void test063e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    FileInputStream input1;\n" +
			"    public void foo() throws IOException {\n" +
			"        FileInputStream input = input1;\n" +
			"        input = new FileInputStream(\"adfafd\");\n" +
			"        input.close();\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
// Bug 368709 - Endless loop in FakedTrackingVariable.markPassedToOutside
// original test case from jgit
public void testBug368709a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"import java.util.zip.*;\n" +
			"public class X {\n" +
			"  Object db, pack;\n" + // mock
			"  int objectOffset, headerLength, type, size;\n" +
			"  public ObjectStream openStream() throws MissingObjectException, IOException {\n" +
			"    WindowCursor wc = new WindowCursor(db);\n" +
			"    InputStream in;\n" +
			"    try\n" +
			"      {\n" +
			"        in = new PackInputStream(pack, (objectOffset + headerLength), wc);\n" +
			"      }\n" +
			"    catch (IOException packGone)\n" +
			"      {\n" +
			"        return wc.open(getObjectId(), type).openStream();\n" +
			"      }\n" +
			"    in = new BufferedInputStream(new InflaterInputStream(in, wc.inflater(), 8192), 8192);\n" +
			"    return new ObjectStream.Filter(type, size, in);\n" +
			"  }\n" +
			"  String getObjectId() { return \"\"; }\n" + // mock
			"}\n" +
			// mock:
			"class WindowCursor {\n" +
			"    WindowCursor(Object db) {}\n" +
			"    ObjectStream open(String id, int type) { return null; }\n" +
			"    Inflater inflater() { return null; }\n" +
			"}\n" +
			"class MissingObjectException extends Exception {\n" +
			"    public static final long serialVersionUID = 13L;\n" +
			"    MissingObjectException() { super();}\n" +
			"}\n" +
			"class PackInputStream extends InputStream {\n" +
			"    PackInputStream(Object pack, int offset, WindowCursor wc) throws IOException {}\n" +
			"    public int read() { return 0; }\n" +
			"}\n" +
			"class ObjectStream extends InputStream {\n" +
			"    static class Filter extends ObjectStream {\n" +
			"        Filter(int type, int size, InputStream in) { }\n" +
			"    }\n" +
			"    ObjectStream openStream() { return this; }\n" +
			"    public int read() { return 0; }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 15)\n" +
		"	return wc.open(getObjectId(), type).openStream();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 18)\n" +
		"	return new ObjectStream.Filter(type, size, in);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'in\' may not be closed at this location\n" +
		"----------\n",
		options);
}
// Bug 368709 - Endless loop in FakedTrackingVariable.markPassedToOutside
// minimal test case: constructing an indirect self-wrapper
public void testBug368709b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"import java.util.zip.*;\n" +
			"public class X {\n" +
			"  void doit() throws IOException {\n" +
			"    InputStream in = new FileInputStream(\"somefile\");\n" +
			"    in = new BufferedInputStream(new InflaterInputStream(in, inflater(), 8192), 8192);\n" +
			"    process(in);\n" +
			"  }\n" +
			"  Inflater inflater() { return null; }\n" +
			"  void process(InputStream is) { }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	InputStream in = new FileInputStream(\"somefile\");\n" +
		"	            ^^\n" +
		"Potential resource leak: \'in\' may not be closed\n" +
		"----------\n",
		options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 3
public void test064() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(new String[] {
		"Test064.java",
		"import java.io.*;\n" +
		"public class Test064 {\n" +
		"    void foo(File outfile) {\n" +
		"        OutputStream out= System.out;\n" +
		"        if (outfile != null) {\n" +
		"            try {\n" +
		"                out = new FileOutputStream(outfile);\n" +
		"            } catch (java.io.IOException e) {\n" +
		"                throw new RuntimeException(e);\n" +
		"            }\n" +
		"        }\n" +
		"        setOutput(out);\n" +
		"    }\n" +
		"    private void setOutput(OutputStream out) { }\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test064.java (at line 7)\n" +
	"	out = new FileOutputStream(outfile);\n" +
	"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Potential resource leak: \'out\' may not be closed\n" +
	"----------\n",
	options);
}
// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 10
// disabled, because basic null-analysis machinery doesn't support this pattern
// see also Bug 370424 - [compiler][null] throw-catch analysis for null flow could be more precise
public void _test065() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test065.java",
		"import java.io.*;\n" +
		"class MyException extends Exception{}\n" +
		"public class Test065 {\n" +
		"	void foo(String fileName) throws IOException, MyException {\n" +
		"		FileReader       fileRead   = new FileReader(fileName);\n" +
		"		BufferedReader   bufRead    = new BufferedReader(fileRead);\n" +
		"		LineNumberReader lineReader = new LineNumberReader(bufRead);\n" +
		"		try {\n" +
		"		while (lineReader.readLine() != null) {\n" +
		"			bufRead.close();\n" +
		"			callSome();  // only this can throw MyException\n" +
		"		}\n" +
		"		} catch (MyException e) {\n" +
		"			throw e;  // Pot. leak reported here\n" +
		"		}\n" +
		"		bufRead.close(); \n" +
		"	}\n" +
		"	private void callSome() throws MyException\n" +
		"	{\n" +
		"		\n" +
		"	}\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 11
public void test066() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test066.java",
		"import java.io.*;\n" +
		"class MyException extends Exception{}\n" +
		"public class Test066 {\n" +
		"    void countFileLines(String fileName) throws IOException {\n" +
		"		FileReader       fileRead   = new FileReader(fileName);\n" +
		"		BufferedReader   bufRead    = new BufferedReader(fileRead);\n" +
		"		LineNumberReader lineReader = new LineNumberReader(bufRead);\n" +
		"		while (lineReader.readLine() != null) {\n" +
		"			if (lineReader.markSupported())\n" +
		"               throw new IOException();\n" +
		"			bufRead.close();\n" +
		"		}\n" +
		"		bufRead.close();\n" +
		"	}\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test066.java (at line 10)\n" +
	"	throw new IOException();\n" +
	"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Potential resource leak: \'lineReader\' may not be closed at this location\n" +
	"----------\n",
	options);
}
// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 11 - variant with closing top-level resource
public void test066b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test066.java",
		"import java.io.*;\n" +
		"class MyException extends Exception{}\n" +
		"public class Test066 {\n" +
		"    void countFileLines(String fileName) throws IOException {\n" +
		"		FileReader       fileRead   = new FileReader(fileName);\n" +
		"		BufferedReader   bufRead    = new BufferedReader(fileRead);\n" +
		"		LineNumberReader lineReader = new LineNumberReader(bufRead);\n" +
		"		while (lineReader.readLine() != null) {\n" +
		"			if (lineReader.markSupported())\n" +
		"               throw new IOException();\n" +
		"			lineReader.close();\n" +
		"		}\n" +
		"		lineReader.close();\n" +
		"	}\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test066.java (at line 10)\n" +
	"	throw new IOException();\n" +
	"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
	"Potential resource leak: \'lineReader\' may not be closed at this location\n" +
	"----------\n",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 12
// Red herring (disabled): warning says "potential" because in the exception case no resource
// would actually be allocated.
public void _test067() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test067.java",
		"import java.io.*;\n" +
		"public class Test067 {\n" +
		"	public void comment12() throws IOException {\n" +
		"    	LineNumberReader o = null;\n" +
		"    	try {\n" +
		"    		o = new LineNumberReader(null);    		\n" +
		"    	} catch (NumberFormatException e) {    		\n" +
		"    	}\n" +
		"    }\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 12
public void test067b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test067.java",
		"import java.io.*;\n" +
		"public class Test067 {\n" +
		"	public void comment12b() throws IOException {\n" +
		"		LineNumberReader o = new LineNumberReader(null);\n" +
		"    	try {\n" +
		"    		o.close();\n" +
		"    	} catch (NumberFormatException e) {\n" +
		"    	}\n" +
		"    }\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 13
public void test068() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test068.java",
		"import java.io.*;\n" +
		"public class Test068 {\n" +
		"	class ProcessingStep extends OutputStream {\n" +
		"		public void write(int b) throws IOException {}\n" +
		"		public OutputStream getDestination() { return null; }\n" +
		"	}\n" +
		"	class ArtifactOutputStream  extends OutputStream {\n" +
		"		public void write(int b) throws IOException {}\n" +
		"	}" +
		"	ArtifactOutputStream comment13(OutputStream stream) {\n" +
		"		OutputStream current = stream;\n" +
		"		while (current instanceof ProcessingStep)\n" +
		"			current = ((ProcessingStep) current).getDestination();\n" +  // we previously saw a bogus warning here.
		"		if (current instanceof ArtifactOutputStream)\n" +
		"			return (ArtifactOutputStream) current;\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 16
public void test069() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // generics used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test069.java",
		"import java.io.*;\n" +
		"import java.util.Collection;\n" +
		"public class Test069 {\n" +
		"	class Profile {}\n" +
		"	class CoreException extends Exception {}\n" +
		"	void writeProfilesToStream(Collection<Profile> p, OutputStream s, String enc) {}\n" +
		"	CoreException createException(IOException ioex, String message) { return new CoreException(); }\n" +
		"	public void comment16(Collection<Profile> profiles, File file, String encoding) throws CoreException {\n" +
		"		final OutputStream stream;\n" +
		"		try {\n" +
		"			stream= new FileOutputStream(file);\n" +
		"			try {\n" +
		"				writeProfilesToStream(profiles, stream, encoding);\n" +
		"			} finally {\n" +
		"				try { stream.close(); } catch (IOException e) { /* ignore */ }\n" +
		"			}\n" +
		"		} catch (IOException e) {\n" +
		"			throw createException(e, \"message\"); // should not shout here\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer
public void test070() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test070.java",
		"import java.io.*;\n" +
		"public class Test070 {\n" +
		"    void storeInArray(String fileName) throws IOException {\n" +
		"		FileReader       fileRead   = new FileReader(fileName);\n" +
		"		closeThemAll(new FileReader[] { fileRead });\n" +
		"	}\n" +
		"   void closeThemAll(FileReader[] readers) { }\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test070.java (at line 4)\n" +
	"	FileReader       fileRead   = new FileReader(fileName);\n" +
	"	                 ^^^^^^^^\n" +
	"Potential resource leak: \'fileRead\' may not be closed\n" +
	"----------\n",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer
public void test071() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test071.java",
		"import java.io.*;\n" +
		"public class Test071 {\n" +
		"    class ReaderHolder {\n" +
		"		FileReader reader;\n" +
		"	}\n" +
		"	private FileReader getReader() {\n" +
		"		return null;\n" +
		"	}\n" +
		"	void invokeCompiler(ReaderHolder readerHolder, boolean flag) throws FileNotFoundException {\n" +
		"		FileReader reader = readerHolder.reader;\n" +
		"		if (reader == null)\n" +
		"			reader = getReader();\n" +
		"		try {\n" +
		"			return;\n" +
		"		} finally {\n" +
		"			try {\n" +
		"				if (flag)\n" +
		"					reader.close();\n" +
		"			} catch (IOException e) {\n" +
		"				// nop\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test071.java (at line 14)\n" +
	"	return;\n" +
	"	^^^^^^^\n" +
	"Potential resource leak: \'reader\' may not be closed at this location\n" +
	"----------\n",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer
// disabled because it would require correlation analysis between the tracking variable and its original
// need to pass to downstream: either (nonnull & open) or (null)
public void _test071b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test071b.java",
		"import java.io.*;\n" +
		"public class Test071b {\n" +
		"   private FileReader getReader() {\n" +
		"		return null;\n" +
		"	}\n" +
		"	void invokeCompiler(boolean flag) throws FileNotFoundException {\n" +
		"		FileReader reader = null;\n" +
		"		if (flag)\n" +
		"			reader = new FileReader(\"file\");\n" +
		"		if (reader == null)\n" +
		"			reader = getReader();\n" +
		"		try {\n" +
		"			return;\n" +
		"		} finally {\n" +
		"			try {\n" +
		"				if (flag)\n" +
		"					reader.close();\n" +
		"			} catch (IOException e) {\n" +
		"				// nop\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test071b.java (at line 13)\n" +
	"	return;\n" +
	"	^^^^^^^\n" +
	"Potential resource leak: \'reader\' may not be closed at this location\n" +
	"----------\n",
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// throw inside loop inside try - while closed in finally
public void test072() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test072.java",
		"import java.io.*;\n" +
		"public class Test072 {\n" +
		"   void readState(File file) {\n" +
		"		DataInputStream in = null;\n" +
		"		try {\n" +
		"			in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));\n" +
		"			int sizeOfFlags = in.readInt();\n" +
		"			for (int i = 0; i < sizeOfFlags; ++i) {\n" +
		"				String childPath = in.readUTF();\n" +
		"				if (childPath.length() == 0)\n" +
		"					throw new IOException();\n" +
		"			}\n" +
		"		}\n" +
		"		catch (IOException ioe) { /* nop */ }\n" +
		"		finally {\n" +
		"			if (in != null) {\n" +
		"				try {in.close();} catch (IOException ioe) {}\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// unspecific parameter is casted into a resource, yet need to mark as OWNED_BY_OUTSIDE
public void test073() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runConformTest(new String[] {
		"Test073.java",
		"import java.io.*;\n" +
		"public class Test073 {\n" +
		"   String getEncoding(Object reader) {\n" +
		"		if (reader instanceof FileReader) {\n" +
		"			final FileReader fr = (FileReader) reader;\n" +
		"			return fr.getEncoding();\n" +
		"		}\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	},
	"",
	null,
	true,
	null,
	options,
	null);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// status after nested try-finally
public void test074() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	runLeakTest(new String[] {
		"Test074.java",
		"import java.io.*;\n" +
		"public class Test074 {\n" +
		"   void foo() throws FileNotFoundException {\n" +
		"		FileOutputStream out = null;\n" +
		"		try {\n" +
		"			out = new FileOutputStream(\"outfile\");\n" +
		"		} finally {\n" +
		"			try {\n" +
		"				out.flush();\n" +
		"				out.close();\n" +
		"			} catch (IOException e) {\n" +
		"				e.printStackTrace();\n" +
		"			}\n" +
		"			out = null;\n" + // unclosed if exception occurred on flush()
		"		}\n" +
		"	}\n" +
		"}\n"
	},
	"----------\n" +
	"1. ERROR in Test074.java (at line 14)\n" +
	"	out = null;\n" +
	"	^^^^^^^^^^\n" +
	"Potential resource leak: \'out\' may not be closed at this location\n" +
	"----------\n",
	options);
}
// Bug 370639 - [compiler][resource] restore the default for resource leak warnings
// check that the default is warning
public void test075() {
	runLeakWarningTest(
		new String[] {
			"X.java",
			"import java.io.File;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"public class X {\n" +
			"    void foo() throws IOException {\n" +
			"        File file = new File(\"somefile\");\n" +
			"        FileReader fileReader = new FileReader(file);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in X.java (at line 7)\n" +
		"	FileReader fileReader = new FileReader(file);\n" +
		"	           ^^^^^^^^^^\n" +
		"Resource leak: 'fileReader' is never closed\n" +
		"----------\n",
		getCompilerOptions());
}
// Bug 385415 - Incorrect resource leak detection
public void testBug385415() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"    void foo() throws FileNotFoundException {\n" +
			"        FileReader fileReader = new FileReader(\"somefile\");\n" +
			"        try {\n" +
			"            fileReader.close();\n" +
			"        } catch (Exception e) {\n" +
			"            e.printStackTrace();\n" +
			"            return;\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 361073 - Avoid resource leak warning when the top level resource is closed explicitly
// test case from comment 7
// Duplicate of Bug 385415 - Incorrect resource leak detection
public void testBug361073c7() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"  public void test() {\n" +
			"    BufferedReader br = null;\n" +
			"    try {\n" +
			"        br = new BufferedReader(new FileReader(\"blah\"));\n" +
			"        String line = null;\n" +
			"        while ( (line = br.readLine()) != null ) {\n" +
			"            if ( line.startsWith(\"error\") )\n" +
			"                throw new Exception(\"error\"); //Resource leak: 'br' is not closed at this location\n" +
			"        }\n" +
			"    } catch (Throwable t) {\n" +
			"        t.printStackTrace();\n" +
			"    } finally {\n" +
			"        if ( br != null ) {\n" +
			"            try { br.close(); }\n" +
			"            catch (Throwable e) { br = null; }\n" +
			"        }\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 386534 - "Potential resource leak" false positive warning
// DISABLED
public void _testBug386534() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug.java",
			"import java.io.FileNotFoundException;\n" +
			"import java.io.IOException;\n" +
			"import java.io.OutputStream;\n" +
			"\n" +
			"public class Bug {\n" +
			"	private static final String DETAILS_FILE_NAME = null;\n" +
			"	private static final String LOG_TAG = null;\n" +
			"	private static Context sContext;\n" +
			"	static void saveDetails(byte[] detailsData) {\n" +
			"		OutputStream os = null;\n" +
			"		try {\n" +
			"			os = sContext.openFileOutput(DETAILS_FILE_NAME,\n" +
			"					Context.MODE_PRIVATE);\n" +
			"			os.write(detailsData);\n" +
			"		} catch (IOException e) {\n" +
			"			Log.w(LOG_TAG, \"Unable to save details\", e);\n" +
			"		} finally {\n" +
			"			if (os != null) {\n" +
			"				try {\n" +
			"					os.close();\n" +
			"				} catch (IOException ignored) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	static class Context {\n" +
			"		public static final String MODE_PRIVATE = null;\n" +
			"		public OutputStream openFileOutput(String detailsFileName,\n" +
			"				String modePrivate) throws FileNotFoundException{\n" +
			"			return null;\n" +
			"		}\n" +
			"	}\n" +
			"	static class Log {\n" +
			"		public static void w(String logTag, String string, IOException e) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// https://bugs.eclipse.org/388996 - [compiler][resource] Incorrect 'potential resource leak'
public void testBug388996() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug.java",
			"import java.io.*;\n" +
			"public class Bug {\n" +
			"	public void processRequest(ResponseContext responseContext) throws IOException {\n" +
			"		OutputStream bao = null;\n" +
			"\n" +
			"		try {\n" +
			"			HttpServletResponse response = responseContext.getResponse();\n" +
			"\n" +
			"			bao = response.getOutputStream(); // <<<<\n" +
			"		} finally {\n" +
			"			if(bao != null) {\n" +
			"				bao.close();\n" +
			"			}\n" +
			"		}\n" +
			"	}" +
			"}\n" +
			"class ResponseContext {\n" +
			"	public HttpServletResponse getResponse() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"class HttpServletResponse {\n" +
			"	public OutputStream getOutputStream() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// https://bugs.eclipse.org/386534 -  [compiler][resource] "Potential resource leak" false positive warning
public void testBug386534() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug386534.java",
			"import java.io.FileNotFoundException;\n" +
			"import java.io.IOException;\n" +
			"import java.io.OutputStream;\n" +
			"\n" +
			"public class Bug386534 {\n" +
			"	private static final String DETAILS_FILE_NAME = null;\n" +
			"	private static final String LOG_TAG = null;\n" +
			"	private static Context sContext;\n" +
			"	static void saveDetails(byte[] detailsData) {\n" +
			"		OutputStream os = null;\n" +
			"		try {\n" +
			"			os = sContext.openFileOutput(DETAILS_FILE_NAME,\n" +
			"					Context.MODE_PRIVATE);\n" +
			"			os.write(detailsData);\n" +
			"		} catch (IOException e) {\n" +
			"			Log.w(LOG_TAG, \"Unable to save details\", e);\n" +
			"		} finally {\n" +
			"			if (os != null) {\n" +
			"				try {\n" +
			"					os.close();\n" +
			"				} catch (IOException ignored) {\n" +
			"				}\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	static class Context {\n" +
			"		public static final String MODE_PRIVATE = null;\n" +
			"		public OutputStream openFileOutput(String detailsFileName,\n" +
			"				String modePrivate) throws FileNotFoundException{\n" +
			"			return null;\n" +
			"		}\n" +
			"	}\n" +
			"	static class Log {\n" +
			"		public static void w(String logTag, String string, IOException e) {\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

//https://bugs.eclipse.org/386534 -  [compiler][resource] "Potential resource leak" false positive warning
public void testBug394768() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug394768.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.InputStream;\n" +
			"\n" +
			"public class Bug394768 {\n" +
			"	public void readFile(String path) throws Exception {\n" +
			"		InputStream stream = null;\n" +
			"		File file = new File(path);\n" +
			"\n" +
			"		if (file.exists())\n" +
			"			stream = new FileInputStream(path);\n" +
			"		else\n" +
			"			stream = getClass().getClassLoader().getResourceAsStream(path);\n" +
			"\n" +
			"		if (stream == null)\n" +
			"			return;\n" +
			"\n" +
			"		try {\n" +
			"			// Use the opened stream here\n" +
			"			stream.read();\n" +
			"		} finally {\n" +
			"			stream.close();\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// https://bugs.eclipse.org/386534 -  [compiler][resource] "Potential resource leak" false positive warning
// variation: 2nd branch closes and nulls the newly acquired resource
public void testBug394768_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug394768.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.InputStream;\n" +
			"\n" +
			"public class Bug394768 {\n" +
			"	public void readFile(String path) throws Exception {\n" +
			"		InputStream stream = null;\n" +
			"		File file = new File(path);\n" +
			"\n" +
			"		if (file.exists()) {\n" +
			"			stream = new FileInputStream(path);\n" +
			"		} else {\n" +
			"			stream = getClass().getClassLoader().getResourceAsStream(path);" +
			"           stream.close();\n" +
			"           stream = null;\n" +
			"       }\n" +
			"\n" +
			"		if (stream == null)\n" +
			"			return;\n" +
			"\n" +
			"		try {\n" +
			"			// Use the opened stream here\n" +
			"			stream.read();\n" +
			"		} finally {\n" +
			"			stream.close();\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A resource is closed using various known close helpers
public void testBug381445_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			GUAVA_CLOSEABLES_JAVA,
			GUAVA_CLOSEABLES_CONTENT,
			"org/apache/commons/io/IOUtils.java",
			"package org.apache.commons.io;\n" +
			"public class IOUtils {\n" +
			"    public static void closeQuietly(java.io.Closeable closeable) {}\n" +
			"}\n",
			"Bug381445.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.InputStream;\n" +
			"\n" +
			"public class Bug381445 {\n" +
			"	public void readFile(String path) throws Exception {\n" +
			"		File file = new File(path);\n" +
			"		InputStream stream1 = new FileInputStream(path);\n" +
			"		InputStream stream2 = new FileInputStream(path);\n" +
			"		InputStream stream3 = new FileInputStream(path);\n" +
			"		InputStream stream4 = new FileInputStream(path);\n" +
			"		try {\n" +
			"			// Use the opened streams here\n" +
			"			stream1.read();\n" +
			"			stream2.read();\n" +
			"			stream3.read();\n" +
			"			stream4.read();\n" +
			"		} finally {\n" +
			"			com.google.common.io.Closeables.closeQuietly(stream1);\n" +
			"			com.google.common.io.Closeables.close(stream2, false);\n" +
			"			org.apache.commons.io.IOUtils.closeQuietly(stream3);\n" +
			"			Closeables.closeQuietly(stream4);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Closeables {\n" + // fake, should not be recognized
			"	public static void closeQuietly(java.io.Closeable closeable) {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug381445.java (at line 11)\n" +
		"	InputStream stream4 = new FileInputStream(path);\n" +
		"	            ^^^^^^^\n" +
		"Potential resource leak: \'stream4\' may not be closed\n" +
		"----------\n",
		options);
}

// Bug 405569 - Resource leak check false positive when using DbUtils.closeQuietly
// A resource is closed using more known close helpers
public void testBug381445_1b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // need AutoCloseable in apache's DbUtils
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			APACHE_DBUTILS_JAVA,
			APACHE_DBUTILS_CONTENT,
			"Bug381445.java",
			"import java.sql.*;\n" +
			"\n" +
			"public class Bug381445 {\n" +
			"	public void performQuery1(String url, String q1, String q2) throws Exception {\n" +
			"		Connection conn = DriverManager.getConnection(url);\n" +
			"		Statement stat = conn.createStatement();\n" +
			"		ResultSet rset = stat.executeQuery(q1);\n" +
			"		ResultSet rset2 = stat.executeQuery(q2);\n" +
			"		try {\n" +
			"			// empty\n" +
			"		} finally {\n" +
			"			org.apache.commons.dbutils.DbUtils.closeQuietly(conn);\n" +
			"			org.apache.commons.dbutils.DbUtils.close(stat);\n" +
			"			org.apache.commons.dbutils.DbUtils.closeQuietly(rset);\n" +
			"			Closeables.closeQuietly(rset2);\n" +
			"		}\n" +
			"	}\n" +
			"	public void performQuery2(String url, String q1, String q2) throws Exception {\n" +
			"		Connection conn = DriverManager.getConnection(url);\n" +
			"		Statement stat = conn.createStatement();\n" +
			"		ResultSet rset = stat.executeQuery(q1);\n" +
			"		try {\n" +
			"			// empty\n" +
			"		} finally {\n" +
			"			org.apache.commons.dbutils.DbUtils.closeQuietly(conn, stat, rset);\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"class Closeables {\n" + // fake, should not be recognized
			"	public static void closeQuietly(java.lang.AutoCloseable closeable) {}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug381445.java (at line 8)\n" +
		"	ResultSet rset2 = stat.executeQuery(q2);\n" +
		"	          ^^^^^\n" +
		"Potential resource leak: \'rset2\' may not be closed\n" +
		"----------\n",
		options);
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A resource is closed in different places of the flow
public void testBug381445_2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			GUAVA_CLOSEABLES_JAVA,
			GUAVA_CLOSEABLES_CONTENT,
			"Bug381445.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.InputStream;\n" +
			"import com.google.common.io.Closeables;\n" +
			"\n" +
			"public class Bug381445 {\n" +
			"	public void readFile(String path) throws Exception {\n" +
			"		File file = new File(path);\n" +
			"		InputStream stream1 = new FileInputStream(path);\n" +
			"		InputStream stream2 = new FileInputStream(path);\n" +
			"		InputStream stream3 = new FileInputStream(path);\n" +
			"		try {\n" +
			"			// Use the opened streams here\n" +
			"			stream1.read();\n" +
			"			Closeables.closeQuietly(stream1);\n" +
			"			stream2.read();\n" +
			"			if (path.length() > 2)\n" +
			"				Closeables.closeQuietly(stream2);\n" + // close inside if is too weak
			"			stream3.read();\n" +
			"		} finally {\n" +
			"		}\n" +
			"		Closeables.closeQuietly(stream3);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug381445.java (at line 10)\n" +
		"	InputStream stream2 = new FileInputStream(path);\n" +
		"	            ^^^^^^^\n" +
		"Potential resource leak: \'stream2\' may not be closed\n" +
		"----------\n",
		options);
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A close helper is referenced in various ways:
public void testBug381445_3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // using static import
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			GUAVA_CLOSEABLES_JAVA,
			GUAVA_CLOSEABLES_CONTENT,
			"Bug381445a.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.InputStream;\n" +
			"import static com.google.common.io.Closeables.closeQuietly;\n" +
			"\n" +
			"public class Bug381445a {\n" +
			"	public void readFile(String path) throws Exception {\n" +
			"		File file = new File(path);\n" +
			"		InputStream stream = new FileInputStream(path);\n" +
			"		try {\n" +
			"			// Use the opened stream here\n" +
			"			stream.read();\n" +
			"		} finally {\n" +
			"			closeQuietly(stream);\n" + // via static import
			"		}\n" +
			"	}\n" +
			"}\n",
			"Bug381445b.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.InputStream;\n" +
			"import com.google.common.io.Closeables;\n" +
			"\n" +
			"public class Bug381445b extends Closeables {\n" +
			"	public void readFile(String path) throws Exception {\n" +
			"		File file = new File(path);\n" +
			"		InputStream stream = new FileInputStream(path);\n" +
			"		try {\n" +
			"			// Use the opened streams here\n" +
			"			stream.read();\n" +
			"		} finally {\n" +
			"			closeQuietly(stream);\n" + // via super class
			"		}\n" +
			"	}\n" +
			"}\n",
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

// Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
// original test case
public void testBug395977() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"WriterTest.java",
			"import java.io.*;\n" +
			"\n" +
			"public class WriterTest implements Runnable\n" +
			"{\n" +
			"   private BufferedWriter m_Writer;\n" +
			"   \n" +
			"   public void run()\n" +
			"   {\n" +
			"      try\n" +
			"      {\n" +
			"         initializeWriter();\n" +
			"         \n" +
			"         m_Writer.write(\"string\");\n" +
			"         m_Writer.newLine();\n" +
			"         \n" +
			"         closeWriter();\n" +
			"      }\n" +
			"      catch (IOException ioe)\n" +
			"      {\n" +
			"         ioe.printStackTrace();\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private void initializeWriter()\n" +
			"      throws UnsupportedEncodingException, FileNotFoundException\n" +
			"   {\n" +
			"      m_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"))\n" +
			"      {\n" +
			"         /**\n" +
			"          * Writes an LF character on all platforms, to avoid constantly flipping the line terminator style.\n" +
			"          */\n" +
			"         public void newLine() throws IOException\n" +
			"         {\n" +
			"            write('\\n');\n" +
			"         }\n" +
			"      };\n" +
			"   }\n" +
			"   \n" +
			"   private void closeWriter()\n" +
			"      throws IOException\n" +
			"   {\n" +
			"      m_Writer.close();\n" +
			"   }\n" +
			"}"
		},
		"",
		null,
		true,
		null,
		options,
		null);
}

//Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
//variant with named local class - accept as a secure resource wrapper since no close method
public void testBug395977_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"WriterTest.java",
			"import java.io.*;\n" +
			"\n" +
			"public class WriterTest implements Runnable\n" +
			"{\n" +
			"   private BufferedWriter m_Writer;\n" +
			"   \n" +
			"   public void run()\n" +
			"   {\n" +
			"      try\n" +
			"      {\n" +
			"         initializeWriter();\n" +
			"         \n" +
			"         m_Writer.write(\"string\");\n" +
			"         m_Writer.newLine();\n" +
			"         \n" +
			"         closeWriter();\n" +
			"      }\n" +
			"      catch (IOException ioe)\n" +
			"      {\n" +
			"         ioe.printStackTrace();\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private void initializeWriter()\n" +
			"      throws UnsupportedEncodingException, FileNotFoundException\n" +
			"   {\n" +
			"      class MyBufferedWriter extends BufferedWriter\n" +
			"      {\n" +
			"         MyBufferedWriter(OutputStreamWriter writer) { super(writer); }\n" +
			"         /**\n" +
			"          * Writes an LF character on all platforms, to avoid constantly flipping the line terminator style.\n" +
			"          */\n" +
			"         public void newLine() throws IOException\n" +
			"         {\n" +
			"            write('\\n');\n" +
			"         }\n" +
			"      };" +
			"      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"));\n" +
			"   }\n" +
			"   \n" +
			"   private void closeWriter()\n" +
			"      throws IOException\n" +
			"   {\n" +
			"      m_Writer.close();\n" +
			"   }\n" +
			"}"
		},
		"",
		options);
}
//Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
//variant with named local class - don't accept as a secure resource wrapper since close() method exist
public void testBug395977_1a() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"WriterTest.java",
			"import java.io.*;\n" +
			"\n" +
			"public class WriterTest implements Runnable\n" +
			"{\n" +
			"   private BufferedWriter m_Writer;\n" +
			"   \n" +
			"   public void run()\n" +
			"   {\n" +
			"      try\n" +
			"      {\n" +
			"         initializeWriter();\n" +
			"         \n" +
			"         m_Writer.write(\"string\");\n" +
			"         m_Writer.newLine();\n" +
			"         \n" +
			"         closeWriter();\n" +
			"      }\n" +
			"      catch (IOException ioe)\n" +
			"      {\n" +
			"         ioe.printStackTrace();\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private void initializeWriter()\n" +
			"      throws UnsupportedEncodingException, FileNotFoundException\n" +
			"   {\n" +
			"      class MyBufferedWriter extends BufferedWriter\n" +
			"      {\n" +
			"         MyBufferedWriter(OutputStreamWriter writer) { super(writer); }\n" +
			"         /**\n" +
			"          * Writes an LF character on all platforms, to avoid constantly flipping the line terminator style.\n" +
			"          */\n" +
			"         public void newLine() throws IOException\n" +
			"         {\n" +
			"            write('\\n');\n" +
			"         }\n" +
			"         public void close() {}\n" +
			"      };" +
			"      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"));\n" +
			"   }\n" +
			"   \n" +
			"   private void closeWriter()\n" +
			"      throws IOException\n" +
			"   {\n" +
			"      m_Writer.close();\n" +
			"   }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in WriterTest.java (at line 38)\n" +
		"	};      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"));\n" +
		"	                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}

// Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
// anonymous class tries to "cheat" by overriding close()
public void testBug395977_2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"WriterTest.java",
			"import java.io.*;\n" +
			"\n" +
			"public class WriterTest implements Runnable\n" +
			"{\n" +
			"   private BufferedWriter m_Writer;\n" +
			"   \n" +
			"   public void run()\n" +
			"   {\n" +
			"      try\n" +
			"      {\n" +
			"         initializeWriter();\n" +
			"         \n" +
			"         m_Writer.write(\"string\");\n" +
			"         m_Writer.newLine();\n" +
			"         \n" +
			"         closeWriter();\n" +
			"      }\n" +
			"      catch (IOException ioe)\n" +
			"      {\n" +
			"         ioe.printStackTrace();\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private void initializeWriter()\n" +
			"      throws UnsupportedEncodingException, FileNotFoundException\n" +
			"   {\n" +
			"      m_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"))\n" +
			"      {\n" +
			"         public void close() { /* nop */}\n" +
			"      };\n" +
			"   }\n" +
			"   \n" +
			"   private void closeWriter()\n" +
			"      throws IOException\n" +
			"   {\n" +
			"      m_Writer.close();\n" +
			"   }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in WriterTest.java (at line 27)\n" +
		"	m_Writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"))\n" +
		"	                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}

// Bug 376053 - [compiler][resource] Strange potential resource leak problems
// include line number when reporting against <unassigned Closeable value>
public void testBug376053() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"Try.java",
			"package xy;\n" +
			"\n" +
			"import java.io.FileNotFoundException;\n" +
			"import java.io.PrintStream;\n" +
			"\n" +
			"public class Try {\n" +
			"    public static void main(String[] args) throws FileNotFoundException {\n" +
			"        System.setOut(new PrintStream(\"log.txt\"));\n" +
			"        \n" +
			"        if (Math.random() > .5) {\n" +
			"            return;\n" +
			"        }\n" +
			"        System.out.println(\"Hello World\");\n" +
			"        return;\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Try.java (at line 11)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value from line 8>\' may not be closed at this location\n" +
		"----------\n" +
		"2. ERROR in Try.java (at line 14)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value from line 8>\' may not be closed at this location\n" +
		"----------\n",
		options);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.io.*;\n" +
			"\n" +
			"class A {\n" +
			"  void a(boolean b) throws Exception {\n" +
			"    try(FileInputStream in = b ? new FileInputStream(\"a\") : null){}\n" +
			"  }\n" +
			"}"
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"import java.io.*;\n"+
			"class A {\n" +
			"  void a(boolean b) throws Exception {\n" +
			"    try(FileInputStream in = create(new FileInputStream(\"a\"))){}\n" +
			"  }\n" +
			"  FileInputStream create(FileInputStream ignored) throws IOException {\n" +
			"    return new FileInputStream(\"b\"); \n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 4)\n" +
		"	try(FileInputStream in = create(new FileInputStream(\"a\"))){}\n" +
		"	                                ^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: '<unassigned Closeable value>' may not be closed\n" +
		"----------\n",
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"import java.io.*;\n" +
			"class A {\n" +
			"	void m() throws IOException {\n" +
			"		try (FileInputStream a = new FileInputStream(\"A\") {{\n" +
			"				FileInputStream b = new FileInputStream(\"B\");\n" +
			"				b.hashCode();\n" +
			"			}}){\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 5)\n" +
		"	FileInputStream b = new FileInputStream(\"B\");\n" +
		"	                ^\n" +
		"Resource leak: 'b' is never closed\n" +
		"----------\n",
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.io.FileInputStream;\n" +
			"class A {\n" +
			"	void testB(boolean b) throws Exception {\n" +
			"		FileInputStream in = null;\n" +
			"		try {\n" +
			"			in = b ? new FileInputStream(\"a\") : null;\n" +
			"		} finally {\n" +
			"		in.close();\n" +
			"		}\n" +
			"	}\n" +
			"}"
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test5() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.io.FileInputStream;\n" +
			"class A {\n" +
			"  void testA(boolean b) throws Exception {\n" +
			"    FileInputStream in = b ? new FileInputStream(\"a\") : null;\n" +
			"    in.close();\n" +
			"  }\n" +
			"}"
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
public void testBug411098_test6() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.io.FileInputStream;\n" +
			"class A {\n" +
			"  void testA(boolean b) throws Exception {\n" +
			"    FileInputStream in = b ? new FileInputStream(\"a\") : new FileInputStream(\"b\");\n" +
			"    in.close();\n" +
			"  }\n" +
			"}"
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
// challenge nested resource allocations
public void testBug411098_test7() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.io.*;\n" +
			"class A {\n" +
			"  void testA(boolean b) throws Exception {\n" +
			"    BufferedReader in = b ? new BufferedReader(new FileReader(\"a\")) : new BufferedReader(new FileReader(\"b\"));\n" +
			"    in.close();\n" +
			"  }\n" +
			"}"
		},
		options
		);
}

// https://bugs.eclipse.org/411098 - [compiler][resource] Invalid Resource Leak Warning using ternary operator inside try-with-resource
// should report potential leak only.
public void testBug411098_comment19() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"import java.io.PrintWriter;\n" +
			"public class A {\n" +
			"	PrintWriter fWriter;\n" +
			"	void bug(boolean useField) {\n" +
			"		PrintWriter bug= useField ? fWriter : null;\n" +
			"		System.out.println(bug);\n" +
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 5)\n" +
		"	PrintWriter bug= useField ? fWriter : null;\n" +
		"	            ^^^\n" +
		"Potential resource leak: \'bug\' may not be closed\n" +
		"----------\n",
		options
		);
}
// normal java.util.stream.Stream doesn't hold on to any resources
public void testStream1() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.util.*;\n" +
			"import java.util.stream.Stream;\n" +
			"class A {\n" +
			"  long test(List<String> ss) {\n" +
			"    Stream<String> stream = ss.stream();\n" +
			"    return stream.count();\n" +
			"  }\n" +
			"}"
		},
		options
		);
}
// normal java.util.stream.IntStream doesn't hold on to any resources
public void testStream1_Int() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.util.stream.*;\n" +
			"class A {\n" +
			"    public void f(Stream<Object> s) {\n" +
			"        IntStream n = s.mapToInt(Object::hashCode);\n" +
			"        IntStream n2 = IntStream.range(23, 42);\n" +
			"        n.forEach(i -> System.out.println(i));\n" +
			"        n2.forEach(i -> System.out.println(i));\n" +
			"    }\n" +
			"}"
		},
		options
		);
}
// normal java.util.stream.{Double,Long}Stream doesn't hold on to any resources
public void testStream1_Double_Long() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.util.stream.*;\n" +
			"class A {\n" +
			"    public void f(Stream<Object> s) {\n" +
			"        DoubleStream n = s.mapToDouble(o -> 0.2);\n" +
			"        LongStream n2 = LongStream.range(23, 42);\n" +
			"        n.forEach(i -> System.out.println(i));\n" +
			"        n2.forEach(i -> System.out.println(i));\n" +
			"    }\n" +
			"}"
		},
		options
		);
}
// normal java.util.stream.{Double,Long}Stream doesn't hold on to any resources
public void testStreamEx_572707() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses JRE 8 API

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			STREAMEX_JAVA,
			STREAMEX_CONTENT,
			"Bug572707.java",
			"import one.util.streamex.*;\n" +
			"\n" +
			"public class Bug572707 {\n" +
			"	public void m() {\n" +
			"		System.out.println(StreamEx.create());\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
// Functions java.nio.file.Files.x() returning *Stream* do produce a resource needing closing
public void testStream2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"import java.util.stream.Stream;\n" +
			"import java.nio.file.*;\n" +
			"class A {\n" +
			"  long test(Path start, FileVisitOption... options) throws java.io.IOException {\n" +
			"    Stream<Path> stream = Files.walk(start, options);\n" +
			"    return stream.count();\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 5)\n" +
		"	Stream<Path> stream = Files.walk(start, options);\n" +
		"	             ^^^^^^\n" +
		"Resource leak: \'stream\' is never closed\n" +
		"----------\n",
		options
		);
}
// closeable, but Stream, but produced by Files.m, but only potentially closed:
public void testStream3() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"A.java",
			"import java.util.stream.Stream;\n" +
			"import java.nio.file.*;\n" +
			"class A {\n" +
			"  void test(Path file) throws java.io.IOException {\n" +
			"    Stream<String> lines = Files.lines(file);\n" +
			"    if (lines.count() > 0)" +
			"    	lines.close();\n" +
			"  }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in A.java (at line 5)\n" +
		"	Stream<String> lines = Files.lines(file);\n" +
		"	               ^^^^^\n" +
		"Potential resource leak: \'lines\' may not be closed\n" +
		"----------\n",
		options
		);
}
// special stream from Files.m is properly handled by t-w-r
public void testStream4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses JRE 8 API
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"A.java",
			"import java.util.stream.Stream;\n" +
			"import java.nio.file.*;\n" +
			"class A {\n" +
			"  void test(Path dir) throws java.io.IOException {\n" +
			"    try (Stream<Path> list = Files.list(dir)) {\n" +
			"    	list.forEach(child -> System.out.println(child));\n" +
			"    }\n" +
			"  }\n" +
			"}"
		},
		options
		);
}
public void testBug415790_ex2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses foreach
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"    public void example2() throws IOException {\n" +
			"        for (final File file : new File[] { new File(\"/\") }) {\n" +
			"            BufferedReader reader = null;\n" +
			"            try {\n" +
			"                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));\n" +
			"            }\n" +
			"            finally {\n" +
			"                try {\n" +
			"                    reader.close();\n" +
			"                }\n" +
			"                catch (IOException e) {\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"" +
			"}\n"
		},
		options);
}
public void testBug415790_ex4() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"    public void example2(File[] files) throws IOException {\n" +
			"        for (int i = 0; i < files.length; i++) {\n" +
			"            File file = files[i];\n" +
			"            BufferedReader reader = null;\n" +
			"            try {\n" +
			"                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));\n" +
			"            }\n" +
			"            finally {\n" +
			"                try {\n" +
			"                    reader.close();\n" +
			"                }\n" +
			"                catch (IOException e) {\n" +
			"                }\n" +
			"            }\n" +
			"        }\n" +
			"    }\n" +
			"" +
			"}\n"
		},
		options);
}
public void testBug371614_comment0() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"C.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStream;\n" +
			"\n" +
			"public class C {\n" +
			"	public static void main(String[] args) {\n" +
			"		FileInputStream fileInputStream= null;\n" +
			"		try {\n" +
			"			fileInputStream = new FileInputStream(args[0]);\n" +
			"			while (true) {\n" +
			"				if (fileInputStream.read() == -1) {\n" +
			"					System.out.println(\"done\");\n" +
			"// Resource leak: 'fileInputStream' is not closed at this location\n" +
			"					return;\n" +
			"				}\n" +
			"			}\n" +
			"		} catch (IOException e) {\n" +
			"			e.printStackTrace();\n" +
			"			return;\n" +
			"		} finally {\n" +
			"			closeStream(fileInputStream);\n" +
			"		}\n" +
			"	}\n" +
			"	\n" +
			"	private static void closeStream(InputStream stream) {\n" +
			"		if (stream != null) {\n" +
			"			try {\n" +
			"				stream.close();\n" +
			"			} catch (IOException e) {\n" +
			"				e.printStackTrace();\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
			"\n"
		},
		"----------\n" +
		"1. ERROR in C.java (at line 14)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		"Potential resource leak: \'fileInputStream\' may not be closed at this location\n" +
		"----------\n",
		options);
}
public void testBug371614_comment2() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"ResourceLeak.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStreamReader;\n" +
			"import java.io.Reader;\n" +
			"\n" +
			"public class ResourceLeak {\n" +
			"\n" +
			"  boolean check(final Reader r) throws IOException {\n" +
			"    final int i = r.read();\n" +
			"    return (i != -1);\n" +
			"  }\n" +
			"\n" +
			"  public void test1() throws IOException {\n" +
			"    try (Reader r = new InputStreamReader(System.in);) {\n" +
			"      while (check(r)) {\n" +
			"        if (check(r))\n" +
			"          throw new IOException(\"fail\");\n" +
			"        if (!check(r))\n" +
			"          throw new IOException(\"fail\");\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public void test2() throws IOException {\n" +
			"    try (Reader r = new InputStreamReader(new FileInputStream(\"test.txt\"));) {\n" +
			"      while (check(r)) {\n" +
			"        if (check(r))\n" +
			"          throw new IOException(\"fail\");\n" +
			"        if (!check(r))\n" +
			"          throw new IOException(\"fail\");\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		options);
}
public void testBug371614_comment8() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"import java.net.*;\n" +
			"public class X {\n" +
			"	Socket fSocket;\n" +
			"	void test() {\n" +
			"    try (InputStreamReader socketIn = new InputStreamReader(fSocket.getInputStream())) {\n" +
			"         while (true) {\n" +
			"             if (socketIn.read(new char[1024]) < 0)\n" +
			"                 throw new IOException(\"Error\");\n" +
			"         }           \n" +
			"     } catch (IOException e) {\n" +
			"     }" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug462371_orig() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"interface IFile {\n" +
			"	InputStream getContents();\n" +
			"	boolean exists();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static String getAnnotatedSignature(String typeName, IFile file, String selector, String originalSignature) {\n" +
			"		if (file.exists()) {\n" +
			"			try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()))) {\n" +
			"				reader.readLine();\n" +
			"				while (true) {\n" +
			"					String line = reader.readLine(); \n" +
			"					// selector:\n" +
			"					if (selector.equals(line)) {\n" +
			"						// original signature:\n" +
			"						line = reader.readLine();\n" +
			"						if (originalSignature.equals(\"\")) {\n" +
			"							// annotated signature:\n" +
			"							return reader.readLine();\n" +
			"						}\n" +
			"					}\n" +
			"					if (line == null)\n" +
			"						break;\n" +
			"				}\n" +
			"			} catch (IOException e) {\n" +
			"				return null;\n" +
			"			}\n" +
			"		}\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void _testBug462371_shouldWarn() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"interface IFile {\n" +
			"	InputStream getContents();\n" +
			"	boolean exists();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static String getAnnotatedSignature(String typeName, IFile file, String selector, String originalSignature) {\n" +
			"		if (file.exists()) {\n" +
			"			try  {\n" +
			"				BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents())); \n" +
			"				reader.readLine();\n" +
			"				while (true) {\n" +
			"					String line = reader.readLine(); \n" +
			"					// selector:\n" +
			"					if (selector.equals(line)) {\n" +
			"						// original signature:\n" +
			"						line = reader.readLine();\n" +
			"						if (originalSignature.equals(\"\")) {\n" +
			"							// annotated signature:\n" +
			"							return reader.readLine();\n" +
			"						}\n" +
			"					}\n" +
			"					if (line == null)\n" +
			"						break;\n" +
			"				}\n" +
			"			} catch (IOException e) {\n" +
			"				return null;\n" +
			"			}\n" +
			"		}\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in C.java (at line 14)\n" +
		"	return;\n" +
		"	^^^^^^^\n" +
		"Potential resource leak: \'fileInputStream\' may not be closed at this location\n" +
		"----------\n",
		options);
}
public void testBug421035() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Test.java",
			"import java.io.BufferedReader;\n" +
			"import java.io.FileNotFoundException;\n" +
			"import java.io.FileReader;\n" +
			"import java.io.IOException;\n" +
			"import java.io.Reader;\n" +
			"\n" +
			"public class Test {\n" +
			"  void test() throws FileNotFoundException {\n" +
			"    Reader a = (Reader)new BufferedReader(new FileReader(\"a\"));\n" +
			"    try {\n" +
			"		a.close();\n" +
			"	} catch (IOException e) {\n" +
			"		e.printStackTrace();\n" +
			"	}\n" +
			"  }\n" +
			"}\n"
		},
		options);
}
public void testBug444964() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug444964.java",
			"import java.io.*;\n" +
			"\n" +
			"public class Bug444964 {\n" +
			"  void wrong() {\n" +
			"    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {\n" +
			"      for (;;) {\n" +
			"        return;\n" +
			"      }\n" +
			"    } catch (Exception e) {\n" +
			"    }\n" +
			"  }\n" +
			"  void right() {\n" +
			"    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {\n" +
			"      while (true) {\n" +
			"        return;\n" +
			"      }\n" +
			"    } catch (Exception e) {\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"}\n"
		},
		options);
}
public void testBug397204() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"HostIdTest.java",
			"import java.io.*;\n" +
			"import java.net.InetAddress;\n" +
			"import java.net.NetworkInterface;\n" +
			"import java.util.Enumeration;\n" +
			"import java.util.Formatter;\n" +
			"import java.util.Locale;\n" +
			"\n" +
			"\n" +
			"public class HostIdTest {\n" +
			"\n" +
			"    public final void primaryNetworkInterface() throws IOException {\n" +
			"        System.out.println(InetAddress.getLocalHost());\n" +
			"        System.out.println(InetAddress.getLocalHost().getHostName());\n" +
			"        System.out.println(hostId());\n" +
			"    }\n" +
			"\n" +
			"    String hostId() throws IOException {\n" +
			"        try (StringWriter s = new StringWriter(); PrintWriter p = new PrintWriter(s)) {\n" +
			"            p.print(InetAddress.getLocalHost().getHostName());\n" +
			"            p.print('/');\n" +
			"            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();\n" +
			"            while (e.hasMoreElements()) {\n" +
			"                NetworkInterface i = e.nextElement();\n" +
			"                System.out.println(i);\n" +
			"                if (i.getHardwareAddress() == null || i.getHardwareAddress().length == 0)\n" +
			"                    continue;\n" +
			"                for (byte b : i.getHardwareAddress())\n" +
			"                    p.printf(\"%02x\", b);\n" +
			"                return s.toString();\n" +
			"            }\n" +
			"            throw new RuntimeException(\"Unable to determine Host ID\");\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public void otherHostId() throws Exception {\n" +
			"        InetAddress addr = InetAddress.getLocalHost();\n" +
			"        byte[] ipaddr = addr.getAddress();\n" +
			"        if (ipaddr.length == 4) {\n" +
			"            int hostid = ipaddr[1] << 24 | ipaddr[0] << 16 | ipaddr[3] << 8 | ipaddr[2];\n" +
			"            StringBuilder sb = new StringBuilder();\n" +
			"            try (Formatter formatter = new Formatter(sb, Locale.US)) {\n" +
			"                formatter.format(\"%08x\", hostid);\n" +
			"                System.out.println(sb.toString());\n" +
			"            }\n" +
			"        } else {\n" +
			"            throw new Exception(\"hostid for IPv6 addresses not implemented yet\");\n" +
			"        }\n" +
			"    }\n" +
			"    \n" +
			"}\n"
		},
		options);
}
public void testBug397204_comment4() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"HostIdTest.java",
			"import java.io.*;\n" +
			"\n" +
			"public class HostIdTest {\n" +
			"\n" +
			"  void simple() throws Exception {\n" +
			"    try (InputStream x = new ByteArrayInputStream(null)) {\n" +
			"      while (Math.abs(1) == 1)\n" +
			"        if (Math.abs(1) == 1)\n" +
			"            return;\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		options);
}
public void testBug433510() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // t-w-r used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Bug433510.java",
			"import java.io.*;\n" +
			"\n" +
			"public class Bug433510 {\n" +
			"\n" +
			"	void test() throws Exception {\n" +
			"		try (Reader r = new StringReader(\"Hello World!\")) {\n" +
			"			int c;\n" +
			"			while ((c = r.read()) != -1) {\n" +
			"				if (c == ' ')\n" +
			"					throw new IOException(\"Unexpected space\");\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug440282() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"ResourceLeakFalseNegative.java",
			"import java.io.FileInputStream;\n" +
			"import java.io.IOException;\n" +
			"import java.io.InputStreamReader;\n" +
			"\n" +
			"public final class ResourceLeakFalseNegative {\n" +
			"\n" +
			"  private static final class Foo implements AutoCloseable {\n" +
			"    final InputStreamReader reader;\n" +
			"\n" +
			"    Foo(final InputStreamReader reader) {\n" +
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
		"----------\n",
		options);
}
public void testBug390064() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // generics used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"Redundant.java",
			"public class Redundant\n" +
			"{\n" +
			"   private static class A<T> implements AutoCloseable\n" +
			"   {\n" +
			"      public void close()\n" +
			"      {\n" +
			"      }\n" +
			"   }\n" +
			"\n" +
			"   private static class B extends A<Object>\n" +
			"   {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   private static class C implements AutoCloseable\n" +
			"   {\n" +
			"      public void close()\n" +
			"      {\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private static class D extends C\n" +
			"   {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public static void main(String[] args)\n" +
			"   {\n" +
			"      new B();\n" +
			"      \n" +
			"      new D();\n" +
			"   }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Redundant.java (at line 29)\n" +
		"	new B();\n" +
		"	^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"2. ERROR in Redundant.java (at line 31)\n" +
		"	new D();\n" +
		"	^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n",
		options);
}
public void testBug396575() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"Bug396575.java",
			"import java.io.*;\n" +
			"\n" +
			"public class Bug396575 {\n" +
			"  void test1(File myFile) {\n" +
			"   OutputStream out = null;\n" +
			"   BufferedWriter bw = null;\n" +
			"   try {\n" +
			"       // code...\n" +
			"       out = new FileOutputStream(myFile);\n" +
			"       OutputStreamWriter writer = new OutputStreamWriter(out);\n" +
			"       bw = new BufferedWriter(writer);\n" +
			"       // more code...\n" +
			"   } catch (Exception e) {\n" +
			"       try {\n" +
			"           bw.close(); // WARN: potential null pointer access\n" +
			"       } catch (Exception ignored) {}\n" +
			"       return;  // WARN: resource leak - bw may not be closed\n" +
			"   }\n" +
			"  }\n" +
			"  \n" +
			"  void test2(File myFile) {\n" +
			"       BufferedWriter bw = null;\n" +
			"   try {\n" +
			"       // code...\n" +
			"                                                       // declare \"out\" here inside try-catch as a temp variable\n" +
			"       OutputStream out = new FileOutputStream(myFile); // WARN: out is never closed.\n" +
			"       OutputStreamWriter writer = new OutputStreamWriter(out);\n" +
			"       bw = new BufferedWriter(writer);\n" +
			"       // more code...\n" +
			"   } catch (Exception e) {\n" +
			"       try {\n" +
			"           bw.close(); // WARN: potential null pointer access\n" +
			"       } catch (Exception ignored) {}\n" +
			"       return;  // WARN: resource leak - bw may not be closed\n" +
			"   }\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in Bug396575.java (at line 11)\n" +
		"	bw = new BufferedWriter(writer);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'bw\' is never closed\n" +
		"----------\n" +
		"2. ERROR in Bug396575.java (at line 28)\n" +
		"	bw = new BufferedWriter(writer);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'bw\' is never closed\n" +
		"----------\n",
		options);
}
public void testBug473317() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // using diamond
	Map<String, String> compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_PB_SYNTHETIC_ACCESS_EMULATION, JavaCore.IGNORE);
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"AutoCloseableEnhancedForTest.java",
			"import java.util.Iterator;\n" +
			"\n" +
			"public class AutoCloseableEnhancedForTest\n" +
			"{\n" +
			"   private static class MyIterator<T> implements Iterator<T>\n" +
			"   {\n" +
			"      private T value;\n" +
			"      \n" +
			"      public MyIterator(T value)\n" +
			"      {\n" +
			"         this.value = value;\n" +
			"      }\n" +
			"      \n" +
			"      @Override\n" +
			"      public boolean hasNext()\n" +
			"      {\n" +
			"         return false;\n" +
			"      }\n" +
			"\n" +
			"      @Override\n" +
			"      public T next()\n" +
			"      {\n" +
			"         return value;\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private static class MyIterable<T> implements Iterable<T>, AutoCloseable\n" +
			"   {\n" +
			"      @Override\n" +
			"      public Iterator<T> iterator()\n" +
			"      {\n" +
			"         return new MyIterator<>(null);\n" +
			"      }\n" +
			"      \n" +
			"      @Override\n" +
			"      public void close() throws Exception\n" +
			"      {\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   public static void main(String[] args)\n" +
			"   {\n" +
			"      // Not flagged as \"never closed.\"\n" +
			"      for (Object value : new MyIterable<>())\n" +
			"      {\n" +
			"         System.out.println(String.valueOf(value));\n" +
			"         \n" +
			"         break;\n" +
			"      }\n" +
			"      \n" +
			"      // Flagged as \"never closed.\"\n" +
			"      MyIterable<Object> iterable = new MyIterable<>();\n" +
			"      \n" +
			"      for (Object value : iterable)\n" +
			"      {\n" +
			"         System.out.println(String.valueOf(value));\n" +
			"         \n" +
			"         break;\n" +
			"      }\n" +
			"   }\n" +
			"}\n"
		};
	runner.expectedCompilerLog =
		"----------\n" +
		"1. WARNING in AutoCloseableEnhancedForTest.java (at line 44)\n" +
		"	for (Object value : new MyIterable<>())\n" +
		"	                    ^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"2. WARNING in AutoCloseableEnhancedForTest.java (at line 52)\n" +
		"	MyIterable<Object> iterable = new MyIterable<>();\n" +
		"	                   ^^^^^^^^\n" +
		"Resource leak: \'iterable\' is never closed\n" +
		"----------\n";
	runner.customOptions = compilerOptions;
	runner.runWarningTest(); // javac warns about exception thrown from close() method
}
public void testBug541705() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses diamond
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Test.java",
		"import java.util.*;\n" +
		"import java.util.zip.*;\n" +
		"import java.io.*;\n" +
		"public class Test {\n" +
		"	private static HashMap<String, ZipFile> fgZipFileCache = new HashMap<>(5);\n" +
		"	public static void closeArchives() {\n" +
		"		synchronized (fgZipFileCache) {\n" +
		"			for (ZipFile file : fgZipFileCache.values()) {\n" +
		"				synchronized (file) {\n" +
		"					try {\n" +
		"						file.close();\n" +
		"					} catch (IOException e) {\n" +
		"						System.out.println(e);\n" +
		"					}\n" +
		"				}\n" +
		"			}\n" +
		"			fgZipFileCache.clear();\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};
	runner.runConformTest();
}
public void testBug541705b() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return; // variable used in t-w-r
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runner.testFiles = new String[] {
		"Test.java",
		"import java.util.*;\n" +
		"import java.util.zip.*;\n" +
		"import java.io.*;\n" +
		"public class Test {\n" +
		"	private static HashMap<String, ZipFile> fgZipFileCache = new HashMap<>(5);\n" +
		"	public static void closeArchives() {\n" +
		"		synchronized (fgZipFileCache) {\n" +
		"			for (ZipFile file : fgZipFileCache.values()) {\n" +
		"				synchronized (file) {\n" +
		"					try (file) {\n" +
		"					} catch (IOException e) {\n" +
		"						System.out.println(e);\n" +
		"					}\n" +
		"				}\n" +
		"			}\n" +
		"			fgZipFileCache.clear();\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	};
	runner.runConformTest();
}
public void testBug542707_001() {
	if (!checkPreviewAllowed()) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n"+
			"import java.io.IOException;\n"+
			"\n"+
			"public class X implements Closeable{\n"+
			"	public static int foo(int i) throws IOException {\n"+
			"		int k = 0;\n"+
			"		X x = null;\n"+
			"		try {\n"+
			"			x = new X();\n"+
			"			x  = switch (i) { \n"+
			"			  case 1  ->   {\n"+
			"				 yield x;\n"+
			"			  }\n"+
			"			  default -> x;\n"+
			"			};\n"+
			"		} finally {\n"+
			"			x.close();\n"+
			"		}\n"+
			"		return k ;\n"+
			"	}\n"+
			"\n"+
			"	public static void main(String[] args) {\n"+
			"		try {\n"+
			"			System.out.println(foo(3));\n"+
			"		} catch (IOException e) {\n"+
			"			// do nothing\n"+
			"		}\n"+
			"	}\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"		Zork();\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 31)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		options);
}
public void testBug542707_002() {
	if (this.complianceLevel < ClassFileConstants.JDK15) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n"+
			"import java.io.IOException;\n"+
			"\n"+
			"public class X implements Closeable{\n"+
			"	public static int foo(int i) throws IOException {\n"+
			"		int k = 0;\n"+
			"		X x = null;\n"+
			"		try {\n"+
			"			x = new X();\n"+
			"			x  = switch (i) { \n"+
			"			  case 1  ->   {\n"+
			"				 x = new X();\n"+
			"				 yield x;\n"+
			"			  }\n"+
			"			  default -> x;\n"+
			"			};\n"+
			"		} finally {\n"+
			"			x.close();\n"+
			"		}\n"+
			"		return k ;\n"+
			"	}\n"+
			"\n"+
			"	public static void main(String[] args) {\n"+
			"		try {\n"+
			"			System.out.println(foo(3));\n"+
			"		} catch (IOException e) {\n"+
			"			// do nothing\n"+
			"		}\n"+
			"	}\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"		Zork();\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	x = new X();\n" +
		"	^^^^^^^^^^^\n" +
		"Resource leak: \'x\' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 32)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		options);
}
public void testBug542707_003() {
	if (!checkPreviewAllowed()) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n"+
			"import java.io.IOException;\n"+
			"\n"+
			"public class X implements Closeable{\n"+
			"	public static int foo(int i) throws IOException {\n"+
			"		int k = 0;\n"+
			"		X x = null;\n"+
			"		try {\n"+
			"			x = new X();\n"+
			"			x  = switch (i) { \n"+
			"			  case 1  ->   {\n"+
			"				 yield new X();\n"+
			"			  }\n"+
			"			  default -> x;\n"+
			"			};\n"+
			"		} finally {\n"+
			"			x.close();\n"+
			"		}\n"+
			"		return k ;\n"+
			"	}\n"+
			"\n"+
			"	public static void main(String[] args) {\n"+
			"		try {\n"+
			"			System.out.println(foo(3));\n"+
			"		} catch (IOException e) {\n"+
			"			// do nothing\n"+
			"		}\n"+
			"	}\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"		Zork();\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	x  = switch (i) { \n" +
		"			  case 1  ->   {\n" +
		"				 yield new X();\n" +
		"			  }\n" +
		"			  default -> x;\n" +
		"			};\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'x\' is not closed at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 31)\n" +
		"	Zork();\n" +
		"	^^^^\n" +
		"The method Zork() is undefined for the type X\n" +
		"----------\n",
		options);
}
public void testBug486506() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses switch expression
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"LogMessage.java",
			"import java.util.stream.*;\n" +
			"import java.io.*;\n" +
			"import java.nio.file.*;\n" +
			"import java.nio.charset.*;\n" +
			"class LogMessage {\n" +
			"  LogMessage(Path path, String message) {}\n" +
			"  public static Stream<LogMessage> streamSingleLineLogMessages(Path path) {\n" +
			"    try {\n" +
			"        Stream<String> lineStream = Files.lines(path, StandardCharsets.ISO_8859_1);\n" +
			"        Stream<LogMessage> logMessageStream =\n" +
			"                lineStream.map(message -> new LogMessage(path, message));\n" +
			"        logMessageStream.onClose(lineStream::close);\n" +
			"        return logMessageStream;\n" +
			"    } catch (IOException e) {\n" +
			"        throw new RuntimeException(e);\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in LogMessage.java (at line 13)\n" +
		"	return logMessageStream;\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'lineStream\' may not be closed at this location\n" +
		"----------\n",
		options);
}
public void testBug463320() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"Try17.java",
			"import java.util.zip.*;\n" +
			"import java.io.*;\n" +
			"public class Try17 {\n" +
			"    void potential() throws IOException {\n" +
			"        String name= getZipFile().getName();\n" +
			"        System.out.println(name);\n" +
			"    }\n" +
			"    void definite() throws IOException {\n" +
			"        String name= new ZipFile(\"bla.jar\").getName();\n" +
			"        System.out.println(name);\n" +
			"    }\n" +
			"	 void withLocal() throws IOException {\n" +
			"		 ZipFile zipFile = getZipFile();\n" +
			"        String name= zipFile.getName();\n" +
			"        System.out.println(name);\n" +
			"	 }\n" +
			"\n" +
			"    ZipFile getZipFile() throws IOException {\n" +
			"        return new ZipFile(\"bla.jar\");\n" +
			"    }\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Try17.java (at line 5)\n" +
		"	String name= getZipFile().getName();\n" +
		"	             ^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in Try17.java (at line 9)\n" +
		"	String name= new ZipFile(\"bla.jar\").getName();\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"2. ERROR in Try17.java (at line 13)\n" +
		"	ZipFile zipFile = getZipFile();\n" +
		"	        ^^^^^^^\n" +
		"Potential resource leak: \'zipFile\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug463320_comment8() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // required version of java.nio.file.*
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"Try17.java",
			"import java.io.*;\n" +
			"import java.nio.file.*;\n" +
			"import java.net.*;\n" +
			"public class Try17 {\n" +
			"   public InputStream openInputStream(URI uri) {\n" +
			"		try {\n" +
			"			System.out.println(FileSystems.getFileSystem(uri));\n" +
			"			return Files.newInputStream(Paths.get(uri));\n" +
			"		} catch (FileSystemNotFoundException e) {\n" +
			"			throw new IllegalArgumentException(e);\n" +
			"		} catch (IOException e) {\n" +
			"			throw new IllegalStateException(e);\n" +
			"		}\n" +
			"	}\n" +
			"	public InputStream delegateGet(URI uri) {\n" +
			"		return openInputStream(uri);\n" + // no problem here!
			"	}\n" +
			"}"
		},
		"----------\n" +
		"1. ERROR in Try17.java (at line 7)\n" +
		"	System.out.println(FileSystems.getFileSystem(uri));\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug558574() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses varargs signatures

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	void m1() throws FileNotFoundException {\n" +
			"		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(\"/tmp/out\")));\n" +
			"		pw.printf(\"%d\", 42).close();\n" +
			"	}\n" +
			"	void m2(PrintWriter pw) throws FileNotFoundException {\n" +
			"		pw.printf(\"%d\", 42).append(\"end\").close();\n" +
			"	}\n" +
			"	void m3() throws FileNotFoundException {\n" +
			"		new PrintWriter(new OutputStreamWriter(new FileOutputStream(\"/tmp/out\")))\n" +
			"			.format(\"%d\", 42)\n" +
			"			.append(\"end\")\n" +
			"			.close();\n" +
			"	}\n" +
			"	void m4(PrintWriter pw) throws FileNotFoundException {\n" +
			"		pw.printf(\"%d\", 42).append(\"end\");\n" +
			"	}\n" +
			"}\n"
		},
		"",
		options);
}
public void testBug560460() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.*;\n" +
			"public class X {\n" +
			"	Scanner m(String source) {\n" +
			"		return new Scanner(source).useDelimiter(\"foobar\");\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug463320_comment19() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"Try17.java",
			"import java.util.zip.*;\n" +
			"import java.io.*;\n" +
			"public class Try17 {\n" +
			"	void withLocal() throws IOException {\n" +
			"		ZipFile zipFile = null;\n" +
			"		if (zipFile != null)" +
			"			zipFile = getZipFile();\n" + // not reachable
			"		String name= zipFile.getName();\n" +
			"		System.out.println(name);\n" +
			"	 }\n" +
			"\n" +
			"    ZipFile getZipFile() throws IOException {\n" +
			"        return new ZipFile(\"bla.jar\");\n" +
			"    }\n" +
			"}"
		},
		options);
}
public void testBug552521() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakTest(
		new String[] {
			"EclipseBug552521getChannel.java",
			"import java.io.File;\n" +
			"import java.io.FileInputStream;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.nio.channels.FileChannel;\n" +
			"\n" +
			"public class EclipseBug552521getChannel {\n" +
			"\n" +
			"	@SuppressWarnings(\"unused\")\n" +
			"	public void copyFile(final File srcFile, final File dstFile) throws Exception {\n" +
			"		/*\n" +
			"		 * TODO Eclipse Setting: Window/Preferences/Java/Compiler/Errors-Warnings/\n" +
			"		 * Resource not managed via try-with-resource = Ignore (default)\n" +
			"		 */\n" +
			"        try (\n" +
			"        		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"        		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 17
			"        		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok\n" +
			"        		)\n" +
			"        {\n" +
			"    		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"        }\n" +
			"\n" +
			"        if (srcFile.isFile()) { // \"if\" (resolved at runtime) -> Warning suppressed\n" +
			"            try (\n" +
			"            		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"            		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 28
			"            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!\n" +
			"            		)\n" +
			"            {\n" +
			"        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"            }\n" +
			"        } else { // \"else\" (resolved at runtime) -> Warning suppressed\n" +
			"            try (\n" +
			"            		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"            		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 38
			"            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!\n" +
			"            		)\n" +
			"            {\n" +
			"        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"            }\n" +
			"        }\n" +
			"\n" +
			"        if (true) { // Dummy \"if\" (= constant true) -> Warning\n" +
			"            try (\n" +
			"            		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"            		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 50
			"            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok\n" +
			"            		)\n" +
			"            {\n" +
			"        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"            }\n" +
			"        } else { // Dummy \"else\" (= constant false) -> Warning suppressed\n" +
			"            try (\n" +
			"            		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"            		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 60
			"            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!\n" +
			"            		)\n" +
			"            {\n" +
			"        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"            }\n" +
			"        }\n" +
			"\n" +
			"        if (false) { // Dummy \"if\" (= constant false) -> Warning suppressed\n" +
			"            try (\n" +
			"            		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"            		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 72
			"            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  FIXME Warning missing!\n" +
			"            		)\n" +
			"            {\n" +
			"        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"            }\n" +
			"        } else { // Dummy \"else\" (= constant true) -> Warning\n" +
			"            try (\n" +
			"            		final FileInputStream srcStream  = new FileInputStream (srcFile);\n" +
			"            		final FileChannel     srcChannel =                      srcStream.getChannel();\n" +
			"    				final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" + // line 82
			"            		//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok\n" +
			"            		)\n" +
			"            {\n" +
			"        		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"            }\n" +
			"        }\n" +
			"        /*\n" +
			"         * Following test-case differs from all the above as follows:\n" +
			"         * FileInputStream is unassigned, instead of FileOutputStream\n" +
			"         */\n" +
			"        try (\n" +
			"        		final FileChannel      srcChannel = new FileInputStream (srcFile) .getChannel();\n" + // line 94
			"        		//                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  TODO Warning ok\n" +
			"        		final FileOutputStream dstStream  = new FileOutputStream(srcFile);\n" +
			"				final FileChannel      dstChannel =                      dstStream.getChannel();\n" +
			"        		)\n" +
			"        {\n" +
			"    		srcChannel.transferTo(0, srcChannel.size(), dstChannel);\n" +
			"        }\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in EclipseBug552521getChannel.java (at line 17)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"2. ERROR in EclipseBug552521getChannel.java (at line 28)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"3. ERROR in EclipseBug552521getChannel.java (at line 38)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"4. ERROR in EclipseBug552521getChannel.java (at line 50)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"5. ERROR in EclipseBug552521getChannel.java (at line 60)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"6. ERROR in EclipseBug552521getChannel.java (at line 72)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"7. ERROR in EclipseBug552521getChannel.java (at line 82)\n" +
		"	final FileChannel     dstChannel = new FileOutputStream(dstFile) .getChannel();\n" +
		"	                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n" +
		"8. ERROR in EclipseBug552521getChannel.java (at line 94)\n" +
		"	final FileChannel      srcChannel = new FileInputStream (srcFile) .getChannel();\n" +
		"	                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Resource leak: \'<unassigned Closeable value>\' is never closed\n" +
		"----------\n",
		options);
}
public void testBug552521_comment14() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"import java.util.*;\n" +
			"public class X {\n" +
			"	List<String> process(InputStream is) throws IOException {\n" +
			"		is.close();\n" +
			"		return Collections.emptyList();\n" +
			"	}\n" +
			"	void test(String fileName) throws IOException {\n" +
			"		for (String string : process(new FileInputStream(fileName))) {\n" +
			"			System.out.println(string);\n" +
			"		}\n" +
			"	}\n" +
			"	void test2(String fileName) throws IOException {\n" +
			"		for (String string : process(new FileInputStream(fileName)))\n" +
			"			System.out.println(string);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	for (String string : process(new FileInputStream(fileName))) {\n" +
		"	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	for (String string : process(new FileInputStream(fileName)))\n" +
		"	                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug552521_comment14b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses foreach

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	boolean check(InputStream is) throws IOException {\n" +
			"		is.close();\n" +
			"		return true;\n" +
			"	}\n" +
			"	void test1(String fileName) throws IOException {\n" +
			"		while (check(new FileInputStream(fileName)))\n" +
			"			System.out.println(\"while\");\n" +
			"	}\n" +
			"	void test2(String fileName) throws IOException {\n" +
			"		do {\n" +
			"			System.out.println(\"while\");\n" +
			"		} while (check(new FileInputStream(fileName)));\n" +
			"	}\n" +
			"	void test3(String fileName) throws IOException {\n" +
			"		for (int i=0;check(new FileInputStream(fileName));i++)\n" +
			"			System.out.println(i);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	while (check(new FileInputStream(fileName)))\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	} while (check(new FileInputStream(fileName)));\n" +
		"	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 17)\n" +
		"	for (int i=0;check(new FileInputStream(fileName));i++)\n" +
		"	                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug519740() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"Snippet.java",
			"class Snippet {\n" +
			"  static void foo() throws Exception {\n" +
			"    try (java.util.Scanner scanner = new java.util.Scanner(new java.io.FileInputStream(\"abc\"))) {\n" +
			"      while (scanner.hasNext()) \n" +
			"        if (scanner.hasNextInt())\n" +
			"          throw new RuntimeException();  /* Potential resource leak: 'scanner' may not be closed at this location */\n" +
			"    }\n" +
			"  }\n" +
			"}\n"
		},
		options);
}
public void testBug552441() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses try-with-resources

	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"Test.java",
			"import java.io.BufferedOutputStream;\n" +
			"import java.io.FileOutputStream;\n" +
			"import java.io.IOException;\n" +
			"import java.io.OutputStream;\n" +
			"import java.util.concurrent.atomic.AtomicLong;\n" +
			"\n" +
			"public class Test {\n" +
			"    public static class CountingBufferedOutputStream extends BufferedOutputStream {\n" +
			"        private final AtomicLong bytesWritten;\n" +
			"\n" +
			"        public CountingBufferedOutputStream(OutputStream out, AtomicLong bytesWritten) throws IOException {\n" +
			"            super(out);\n" +
			"            this.bytesWritten = bytesWritten;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public void write(byte[] b) throws IOException {\n" +
			"            super.write(b);\n" +
			"            bytesWritten.addAndGet(b.length);\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public void write(byte[] b, int off, int len) throws IOException {\n" +
			"            super.write(b, off, len);\n" +
			"            bytesWritten.addAndGet(len);\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public synchronized void write(int b) throws IOException {\n" +
			"            super.write(b);\n" +
			"            bytesWritten.incrementAndGet();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public static void test(String[] args) throws IOException {\n" +
			"        AtomicLong uncompressedBytesOut = new AtomicLong();\n" +
			"        int val = 0;\n" +
			"        try (CountingBufferedOutputStream out = new CountingBufferedOutputStream(\n" +
			"                new FileOutputStream(\"outputfile\"), uncompressedBytesOut)) {\n" +
			"\n" +
			"            for (int i = 0; i < 1; i++) {\n" +
			"                if (val > 2) {\n" +
			"                    throw new RuntimeException(\"X\");\n" +
			"                }\n" +
			"            }\n" +
			"            if (val > 2) {\n" +
			"                throw new RuntimeException(\"Y\");\n" +
			"            }\n" +
			"            throw new RuntimeException(\"Z\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		options);
}
public void testBug400523() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);

	runConformTest(
		new String[] {
			"LeakWarning.java",
			"import java.sql.Connection;\n" +
			"import java.sql.PreparedStatement;\n" +
			"import java.sql.ResultSet;\n" +
			"import java.sql.SQLException;\n" +
			"\n" +
			"public class LeakWarning {\n" +
			"	String value = null;\n" +
			"	\n" +
			"    public void setValue(Connection conn)\n" +
			"	{        \n" +
			"        PreparedStatement stmt = null;\n" +
			"        ResultSet rs = null;\n" +
			"        try {            \n" +
			"            stmt = conn.prepareStatement(\"SELECT 'value'\");  /* marked as potential resource leak */\n" +
			"            rs = stmt.executeQuery();                        /* marked as potential resource leak */\n" +
			"            if (rs.next()) value = rs.getString(1);\n" +
			"        } catch(SQLException e) {\n" +
			"        }\n" +
			"        finally {\n" +
			"        	if (null != rs)   try { rs.close();   } catch (SQLException e) {} finally { rs = null;   }\n" +
			"        	if (null != stmt) try { stmt.close(); } catch (SQLException e) {} finally { stmt = null; }\n" +
			"        }\n" +
			"    }\n" +
			"    \n" +
			"    public void setValueReturn(Connection conn)\n" +
			"	{        \n" +
			"        PreparedStatement stmt = null;\n" +
			"        ResultSet rs = null;\n" +
			"        try {            \n" +
			"            stmt = conn.prepareStatement(\"SELECT 'value'\");\n" +
			"            rs = stmt.executeQuery();\n" +
			"            if (rs.next()) value = rs.getString(1);\n" +
			"        } catch(SQLException e) {\n" +
			"        }\n" +
			"        finally {\n" +
			"        	if (null != rs)   try { rs.close();   } catch (SQLException e) {} finally { rs = null;   }\n" +
			"        	if (null != stmt) try { stmt.close(); } catch (SQLException e) {} finally { stmt = null; }\n" +
			"        }\n" +
			"        return; /* no warning now */\n" +
			"    }\n" +
			"}\n"
		},
		options);
}
public void testBug527761() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"class BAOSWrapper extends java.io.ByteArrayOutputStream {}\n" +
			"public class X {\n" +
			"	public static void warningCauser() {\n" +
			"		BAOSWrapper baos = new BAOSWrapper();\n" +
			"		//WARNING HAS BEEN CAUSED\n" +
			"		baos.write(0);\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug527761_otherClose() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	String xSource =
			"public class X {\n" +
			"	public static void warningCauser() {\n" +
			"		BAOSWrapper<String> baos = new BAOSWrapper<String>();\n" +
			"		//WARNING HAS BEEN CAUSED\n" +
			"		baos.write(0);\n" +
			"	}\n" +
			"}\n";
	runConformTest(
		new String[] {
			"BAOSWrapper.java",
			"class BAOSWrapper<T> extends java.io.ByteArrayOutputStream {\n" +
			"	public void close(java.util.List<?> l) {}\n" + // not relevant, param challenges treatment of unresolved types
			"}\n",
			"X.java",
			xSource
		},
		options);
	// consume BAOSWrapper from .class:
	runConformTest(false,
			new String[] { "X.java", xSource },
			"", "", "", null);
}
public void testBug527761_neg() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"class BAOSWrapper extends java.io.ByteArrayOutputStream {\n" +
			"	public void close() {}\n" + // indicates that resource could be relevant
			"}\n" +
			"public class X {\n" +
			"	public static void warningCauser() {\n" +
			"		BAOSWrapper baos = new BAOSWrapper();\n" +
			"		//WARNING HAS BEEN CAUSED\n" +
			"		baos.write(0);\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	BAOSWrapper baos = new BAOSWrapper();\n" +
		"	            ^^^^\n" +
		"Resource leak: \'baos\' is never closed\n" +
		"----------\n",
		options);
}
// regression caused by Bug 527761
public void testBug558759() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses generics
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	String ySource =
		"public class Y {\n" +
		"	class YInner extends X<I> {}\n" +
		"}\n";
	runConformTest(
		new String[] {
			"I.java",
			"import java.io.Closeable;\n" +
			"public interface I extends Closeable {\n" +
			"	interface Location {}\n" +
			"	void m(Location l);\n" +
			"}\n",
			"X0.java",
			"public abstract class X0<T extends I> implements I {\n" +
			"	public void close() {}\n" +
			"}\n",
			"X.java",
			"public class X<T extends I> extends X0<T> implements I {\n" +
			"	public void m(Location l) {}\n" +
			"}\n",
			"Y.java",
			ySource
		},
		options);
	runConformTest(false,
			new String[] { "Y.java", ySource },
			"", "", "", null);
}
public void testBug559119() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses @Override
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runLeakWarningTest(
		new String[] {
			"Sequencer.java",
			"interface Sequencer extends AutoCloseable {\n" +
			"	void close(); // no exception\n" +
			"}\n",
			"SequencerControl.java",
			"public abstract class SequencerControl {\n" +
			"	public abstract Sequencer getSequencer();\n" +
			"	@Override\n" +
			"	public boolean equals(Object obj) {\n" +
			"		if (obj != null) {\n" +
			"			if (getClass().equals(obj.getClass())) {\n" +
			"				return ((SequencerControl)obj).getSequencer().equals(getSequencer());\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in SequencerControl.java (at line 7)\n" +
		"	return ((SequencerControl)obj).getSequencer().equals(getSequencer());\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n" +
		"2. WARNING in SequencerControl.java (at line 7)\n" +
		"	return ((SequencerControl)obj).getSequencer().equals(getSequencer());\n" +
		"	                                                     ^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug560610() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses enum
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"A.java",
			"import java.util.EnumSet;\n" +
			"public abstract class A<T> extends B<T> implements C<D> {\n" +
			"	void m(EnumSet<EN> en) {}\n" + // unResolvedMethods() when a is seen as a PTB causes bogus resolving of this method
			"}\n",
			"B.java",
			"public abstract class B<U> implements AutoCloseable {}\n", // this causes A to be seen as a resource requiring closer inspection
			"C.java",
			"public interface C<T> {}\n", // just so we can read D as a type argument during hierarchy connecting for A
			"D.java",
			"public abstract class D extends A<String> {}\n", // extends A causes searching A for a close method, A seen as a PTB
			"EN.java",
			"public enum EN {\n" + // when we find this via ahead-of-time resolveTypesFor("m()") we don't yet have a superclass
			"	One, Two;\n" +
			"}\n"
		},
		"",
		options);
}
public void testBug560671() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses t-w-r
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Scanner;\n" +
			"public class X {\n" +
			"	void m(String source) {\n" +
			"		try (Scanner s = new Scanner(source).useDelimiter(\"foobar\")) {\n" +
			"			System.out.println(s.next());\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug560671b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"import java.util.Scanner;\n" +
			"public class X {\n" +
			"	void m(String source) throws java.io.IOException {\n" +
			"		Scanner s = null;" +
			"		try {\n" +
			"			s = new Scanner(source).useDelimiter(\"foobar\");\n" +
			"			System.out.println(s.next());\n" +
			"		} finally {\n" +
			"			if (s != null) s.close();\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug561259() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" +
			"public class X {\n" +
			"	  protected String m(String charset) throws IOException\n" +
			"	  {\n" +
			"		InputStream contents = new FileInputStream(\"/tmp/f\");\n" +
			"	    BufferedReader reader = new BufferedReader(new InputStreamReader(contents, charset));\n" +
			"	    CharArrayWriter writer = new CharArrayWriter();\n" +
			"	    int c;\n" +
			"	    while ((c = reader.read()) != -1)\n" +
			"	    {\n" +
			"	      writer.write(c);\n" +
			"	    }\n" +
			"	    contents.close();\n" +
			"	    return writer.toString();\n" +
			"	  }\n" +
			"}\n"
		},
		options);
}
public void testBug560076() {
	runNegativeTest(
		new String[] {
			"org/sqlite/database/sqlite/SQLiteOpenHelper.java",
			"package org.sqlite.database.sqlite;\n" +
			"\n" +
			"public abstract class SQLiteOpenHelper {\n" +
			"    private void getDatabaseLocked(String name, SQLiteDatabase mDatabase) {\n" +
			"        SQLiteDatabase sQLiteDatabase4 = mDatabase;\n" +
			"        try {\n" +
			"            sQLiteDatabase4 = name == null ? null : openDatabase();\n" +
			"        } catch (Throwable e) {\n" +
			"            sQLiteDatabase4 = openDatabase();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    public static SQLiteDatabase openDatabase() {\n" +
			"    }\n" +
			"}\n" +
			"\n" +
			"final class SQLiteDatabase implements java.io.Closeable {\n" +
			"}\n"
		},
		"----------\n" +
		"1. WARNING in org\\sqlite\\database\\sqlite\\SQLiteOpenHelper.java (at line 4)\n" +
		"	private void getDatabaseLocked(String name, SQLiteDatabase mDatabase) {\n" +
		"	             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"The method getDatabaseLocked(String, SQLiteDatabase) from the type SQLiteOpenHelper is never used locally\n" +
		"----------\n" +
		"2. ERROR in org\\sqlite\\database\\sqlite\\SQLiteOpenHelper.java (at line 13)\n" +
		"	public static SQLiteDatabase openDatabase() {\n" +
		"	                             ^^^^^^^^^^^^^^\n" +
		"This method must return a result of type SQLiteDatabase\n" +
		"----------\n" +
		"3. ERROR in org\\sqlite\\database\\sqlite\\SQLiteOpenHelper.java (at line 17)\n" +
		"	final class SQLiteDatabase implements java.io.Closeable {\n" +
		"	            ^^^^^^^^^^^^^^\n" +
		"The type SQLiteDatabase must implement the inherited abstract method Closeable.close()\n" +
		"----------\n");
}
public void testBug499037_001_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             final Y y1 = new Y();\n" +
			"             try (y1) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"} \n"
		},
		"",
		options);
}
public void testBug499037_002_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}\n" +
			"public class X {\n" +
			"\n" +
			"        public void foo() throws IOException {\n" +
			"             Y y1 = new Y();\n" +
			"             try (y1; final Y y2 = new Y()) { \n" +
			"            	 //\n" +
			"             }\n" +
			"        } \n" +
			"        public static void main(String[] args) {\n" +
			"			System.out.println(\"Done\");\n" +
			"		}\n" +
			"} \n"
		},
		"",
		options);
}
public void testBug499037_003_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.Closeable;\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public class X { \n" +
			"    public void foo() throws IOException {\n" +
			"         Y y1 = new Y();\n" +
			"         try(y1) { \n" +
			"             return;\n" +
			"         }\n" +
			"    } \n" +
			"}  \n" +
			"\n" +
			"class Y implements Closeable {\n" +
			"		final int x = 10;\n" +
			"        @Override\n" +
			"        public void close() throws IOException {\n" +
			"                // nothing\n" +
			"        }\n" +
			"}"
		},
		"",
		options);
}
public void testBug499037_004_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" +
			"\n" +
			"class Z {\n" +
			"	final Y yz = new Y();\n" +
			"}\n" +
			"public class X extends Z {\n" +
			"	final Y y2 = new Y();\n" +
			"	\n" +
			"	public void foo() {\n" +
			"		try (super.yz; y2)  {\n" +
			"			System.out.println(\"In Try\");\n" +
			"		} catch (IOException e) {\n" +
			"			\n" +
			"		}finally { \n" +
			"		}\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		new X().foo();\n" +
			"	}\n" +
			"}\n" +
			"class Y implements AutoCloseable {\n" +
			"	@Override\n" +
			"	public void close() throws IOException {\n" +
			"		System.out.println(\"Closed\");\n" +
			"	} \n" +
			"}  \n"
		},
		"",
		options);
}
public void testBug499037_005_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n"+
			"\n"+
			"public class X {\n"+
			"	void test(boolean b) throws IOException {\n"+
			"		Y y = new Y();\n"+
			"		if (b) {\n"+
			"			try (y) {}\n"+
			"		}\n"+
			"	}\n"+
			"}\n"+
			"\n"+
			"class Y implements AutoCloseable {\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Y y = new Y();\n" +
		"	  ^\n" +
		"Potential resource leak: \'y\' may not be closed\n" +
		"----------\n",
		options);
}
// non-empty finally block - takes a different route
public void testBug499037_006_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n"+
			"\n"+
			"public class X {\n"+
			"	void test(boolean b) throws IOException {\n"+
			"		Y y = new Y();\n"+
			"		if (b) {\n"+
			"			try (y;Y y2 = new Y();) { \n"+
			"			} finally {\n"+
			"			  System.out.println(\"hello\");\n"+
			"			}\n"+
			"		}\n"+
			"	}\n"+
			"}\n"+
			"\n"+
			"class Y implements AutoCloseable {\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Y y = new Y();\n" +
		"	  ^\n" +
		"Potential resource leak: \'y\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug499037_007_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n"+
			"\n"+
			"public class X {\n"+
			"	void test(boolean b) throws IOException {\n"+
			"		Y y = new Y();\n"+
			"		if (b) {\n"+
			"			try (y) { \n"+
			"			    // nothing \n"+
			"			}\n"+
			"		}\n"+
			"		else {\n"+
			"			y.close();\n"+
			"		}\n"+
			"	}\n"+
			"}\n"+
			"\n"+
			"class Y implements AutoCloseable {\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"	}\n"+
			"}\n"
		},
		"",
		options);
}
public void testBug499037_008_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n"+
			"\n"+
			"public class X {\n"+
			"	void test(boolean b, Y yDash) throws IOException {\n"+
			"		Y y = new Y();\n"+
			"		if (b) {\n"+
			"			try (y; yDash) { \n"+
			"			    // nothing \n"+
			"			}\n"+
			"		}\n"+
			"		else {\n"+
			"		}\n"+
			"	}\n"+
			"}\n"+
			"\n"+
			"class Y implements AutoCloseable {\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	Y y = new Y();\n" +
		"	  ^\n" +
		"Potential resource leak: \'y\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug499037_009_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n"+
			"\n"+
			"public class X {\n"+
			"private void foo(Y y) {}\n" +
			"	void test(boolean b) throws IOException {\n"+
			"		Y y = new Y();\n"+
			"		if (b) {\n"+
			"			try (y) { \n"+
			"			    // nothing \n"+
			"			}\n"+
			"		}\n"+
			"		else {\n"+
			"			foo(y);\n"+
			"		}\n"+
			"	}\n"+
			"}\n"+
			"\n"+
			"class Y implements AutoCloseable {\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	Y y = new Y();\n" +
		"	  ^\n" +
		"Potential resource leak: \'y\' may not be closed\n" +
		"----------\n",
		options);
}
public void testBug499037_010_since_9() {
	if (this.complianceLevel < ClassFileConstants.JDK9) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportExplicitlyClosedAutoCloseable, CompilerOptions.ERROR);
	runLeakTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n"+
			"\n"+
			"public class X {\n"+
			"private Y foo(Y y) {return y;}\n" +
			"	void test(boolean b) throws IOException {\n"+
			"		Y y = new Y();\n"+
			"		Y yy = foo(y);\n"+
			"		if (b) {\n"+
			"			try (y;yy) { \n"+
			"			    // do nothing \n"+
			"			}\n"+
			"		}\n"+
			"		else {\n"+
			"			// do nothing\n"+
			"		}\n"+
			"	}\n"+
			"}\n"+
			"\n"+
			"class Y implements AutoCloseable {\n"+
			"	@Override\n"+
			"	public void close() throws IOException {\n"+
			"	}\n"+
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	Y y = new Y();\n" +
		"	  ^\n" +
		"Potential resource leak: \'y\' may not be closed\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 7)\n" +
		"	Y yy = foo(y);\n" +
		"	  ^^\n" +
		"Potential resource leak: \'yy\' may not be closed\n" +
		"----------\n",
		options);
}
}
