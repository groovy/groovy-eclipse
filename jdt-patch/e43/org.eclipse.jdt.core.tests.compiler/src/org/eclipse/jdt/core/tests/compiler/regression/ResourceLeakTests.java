/*******************************************************************************
 * Copyright (c) 2011, 2012 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
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

public class ResourceLeakTests extends AbstractRegressionTest {

// well-known helper class:
private static final String GUAVA_CLOSEABLES_JAVA = "com/google/common/io/Closeables.java";
private static final String GUAVA_CLOSEABLES_CONTENT = "package com.google.common.io;\n" +
	"public class Closeables {\n" +
	"    public static void closeQuietly(java.io.Closeable closeable) {}\n" +
	"    public static void close(java.io.Closeable closeable, boolean flag) {}\n" +
	"}\n";

static {
//	TESTS_NAMES = new String[] { "testBug376053" };
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
		runNegativeTest(testFiles, errorsIn17, null, true, options);
	else
		runConformTest(testFiles, "", null, true, null, options, null);
}

// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without ever closing it.
public void test056() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses two AutoCloseables (testing independent analysis)
//- one closeable may be unclosed at a conditional return
//- the other is only conditionally closed
public void test056d_suppress() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // annotations used
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_SuppressOptionalErrors, CompilerOptions.ENABLED);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// one method returns an AutoCleasble, a second method uses this object without ever closing it.
public void test056e() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// an AutoCloseable local is re-assigned after null-assigned
public void test056g2() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// two AutoCloseables at different nesting levels (anonymous local type)
public void test056h() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// three AutoCloseables in different blocks of the same method
public void test056i() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056j() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// a method uses an AutoCloseable without closing it locally but passing as arg to another method
public void test056jconditional() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// closed in dead code
public void test056q() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_EXPLICITLY_CLOSED_AUTOCLOSEABLE, CompilerOptions.IGNORE);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// end of method is dead end, but before we have both a close() and an early return
public void test056w() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// different early exits, if no close seen report as definitely unclosed
public void test056x() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// nested method passes the resource to outside code
public void test056y() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.WARNING);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 349326 - [1.7] new warning for missing try-with-resources
// resource assigned to second local and is (potentially) closed on the latter
public void test056z() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_POTENTIALLY_UNCLOSED_CLOSEABLE, CompilerOptions.ERROR);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// Different points in a resource chain are closed
public void test061g() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// Bug 360908 - Avoid resource leak warning when the underlying/chained resource is closed explicitly
// Different points in a resource chain are potentially closed
public void test061h() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// local var is re-used for two levels of wrappers
public void test061i() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 361407 - Resource leak warning when resource is assigned to a field outside of constructor
// a closeable is not assigned to a field - constructor vs. method
public void test061l3() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a closeable is passed to another method in a return statement
// example constructed after org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository#getArtifact(..)
public void test061m() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper does not wrap any provided resource
public void test061n() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is closed only in its local block, underlying resource may leak
public void test061o() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 358903 - Filter practically unimportant resource leak warnings
// a resource wrapper is conditionally allocated but not closed - from a real-world example
public void test061f4() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 362331 - Resource leak not detected when closeable not assigned to variable
// a resource is not used
public void test062d() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}
// Bug 362332 - Only report potential leak when closeable not created in the local scope
// a wrapper is obtained from another method
public void test063a() throws IOException {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
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
	this.runNegativeTest(
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
		"1. ERROR in X.java (at line 18)\n" +
		"	return new ObjectStream.Filter(type, size, in);\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential resource leak: \'in\' may not be closed at this location\n" +
		"----------\n",
		null,
		true,
		options);
}
// Bug 368709 - Endless loop in FakedTrackingVariable.markPassedToOutside
// minimal test case: constructing an indirect self-wrapper
public void testBug368709b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(
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
		null,
		true,
		options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 3
public void test064() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	this.runNegativeTest(new String[] {
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
	null,
	true,
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
	this.runNegativeTest(new String[] {
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
	null,
	true,
	options);
}
// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// example from comment 11 - variant with closing top-level resource 
public void test066b() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runNegativeTest(new String[] {
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
	null,
	true,
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
	this.runNegativeTest(new String[] {
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
	null,
	true,
	options);
}

// Bug 368546 - [compiler][resource] Avoid remaining false positives found when compiling the Eclipse SDK
// referenced in array initializer 
public void test071() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);
	this.runNegativeTest(new String[] {
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
	null,
	true,
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
	this.runNegativeTest(new String[] {
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
	null,
	true,
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
	this.runNegativeTest(new String[] {
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
	null,
	true,
	options);
}
// Bug 370639 - [compiler][resource] restore the default for resource leak warnings
// check that the default is warning
public void test075() {
	this.runNegativeTest(
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
		"----------\n");
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
	runNegativeTest(
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
		null,
		true,
		options,
		null);	
}

// Bug 381445 - [compiler][resource] Can the resource leak check be made aware of Closeables.closeQuietly?
// A resource is closed in different places of the flow
public void testBug381445_2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runNegativeTest(
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
		null,
		true,
		options,
		null);	
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

// Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
// variant with named local class - don't accept as a secure resource wrapper
public void testBug395977_1() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runNegativeTest(
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
		"----------\n" + 
		"1. ERROR in WriterTest.java (at line 37)\n" + 
		"	};      m_Writer = new MyBufferedWriter(new OutputStreamWriter(new FileOutputStream(\"file\"), \"UTF-8\"));\n" + 
		"	                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Potential resource leak: \'<unassigned Closeable value>\' may not be closed\n" + 
		"----------\n",
		null,
		true,
		options);
}

// Bug 395977 - Resource leak warning behavior possibly incorrect for anonymous inner class
// anonymous class tries to "cheat" by overriding close()
public void testBug395977_2() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runNegativeTest(
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
		null,
		true,
		options);
}

// Bug 376053 - [compiler][resource] Strange potential resource leak problems
// include line number when reporting against <unassigned Closeable value>
public void testBug376053() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportPotentiallyUnclosedCloseable, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportUnclosedCloseable, CompilerOptions.ERROR);
	runNegativeTest(
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
		null,
		true,
		options);
}
}
