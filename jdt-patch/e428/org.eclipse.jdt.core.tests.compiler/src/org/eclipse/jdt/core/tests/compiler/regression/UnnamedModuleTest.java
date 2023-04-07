/*******************************************************************************
 * Copyright (c) 2017 Till Brychcy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Till Brychcy - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class UnnamedModuleTest extends AbstractRegressionTest9 {

static {
//	TESTS_NAMES = new String[] { "testBugXXX" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public UnnamedModuleTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}

public static Class<UnnamedModuleTest> testClass() {
	return UnnamedModuleTest.class;
}

public void testBug522327() {
	runConformTest(
		new String[] {
			"nonmodular/ProblemWithThrowable.java",
			"package nonmodular;\n" +
			"\n" +
			"import java.io.IOException;\n" +
			"import java.sql.SQLException;\n" +
			"\n" +
			"public class ProblemWithThrowable {\n" +
			"    public void saveProperties() throws IOException {\n" +
			"    }\n" +
			"}\n" +
			"",
		}
	);
}

public void testBug522326() {
	runConformTest(
		new String[] {
			"nonmodular/ProblemWithNested.java",
			"package nonmodular;\n" +
			"\n" +
			"import java.sql.Connection;\n" +
			"import java.util.Map.Entry;\n" +
			"\n" +
			"public class ProblemWithNested {\n" +
			"}\n" +
			"",
		}
	);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/415
// Should compile successfully when the compilation unit contains a split package and
// the option 'OPTION_IgnoreUnnamedModuleForSplitPackage' is turned on.
public void testIgnoreUnnamedModule1() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ignore-unnamed-module-test.jar";
	String[] defaultLibs = getDefaultClassPaths();
	int len = defaultLibs.length;
	String[] libs = new String[len+1];
	System.arraycopy(defaultLibs, 0, libs, 0, len);
	libs[len] = path;
	Map<String, String> compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_IgnoreUnnamedModuleForSplitPackage, CompilerOptions.ENABLED);
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"import org.xml.sax.SAXException;\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		SAXException s;\n" +
				"	}\n" +
				"}"
			},
			libs,
			compilerOptions,
			"",
			"",
			"",
			JavacTestOptions.DEFAULT);
}
public void testIgnoreUnnamedModule2() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ignore-unnamed-module-test.jar";
	String[] defaultLibs = getDefaultClassPaths();
	int len = defaultLibs.length;
	String[] libs = new String[len+1];
	System.arraycopy(defaultLibs, 0, libs, 0, len);
	libs[len] = path;
	Map<String, String> compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_IgnoreUnnamedModuleForSplitPackage, CompilerOptions.ENABLED);
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo() {\n" +
				"		org.xml.sax.SAXException s;\n" +
				"	}\n" +
				"}"
			},
			libs,
			compilerOptions,
			"",
			"",
			"",
			JavacTestOptions.DEFAULT);
}
public void testIgnoreUnnamedModule3() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ignore-unnamed-module-test.jar";
	String[] defaultLibs = getDefaultClassPaths();
	int len = defaultLibs.length;
	String[] libs = new String[len+1];
	System.arraycopy(defaultLibs, 0, libs, 0, len);
	libs[len] = path;
	Map<String, String> compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_IgnoreUnnamedModuleForSplitPackage, CompilerOptions.ENABLED);
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"import static org.xml.sax.helpers.NamespaceSupport.XMLNS;\n" +
				"public class X {\n" +
				"	void foo() {\n" +
				"		String s = XMLNS;\n" +
				"	}\n" +
				"}"
			},
			libs,
			compilerOptions,
			"",
			"",
			"",
			JavacTestOptions.DEFAULT);
}
public void testIgnoreUnnamedModule4() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ignore-unnamed-module-test.jar";
	String[] defaultLibs = getDefaultClassPaths();
	int len = defaultLibs.length;
	String[] libs = new String[len+1];
	System.arraycopy(defaultLibs, 0, libs, 0, len);
	libs[len] = path;
	Map<String, String> compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_IgnoreUnnamedModuleForSplitPackage, CompilerOptions.ENABLED);
	this.runConformTest(
			true,
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo() {\n" +
				"		String s = org.xml.sax.helpers.NamespaceSupport.XMLNS;\n" +
				"	}\n" +
				"}"
			},
			libs,
			compilerOptions,
			"",
			"",
			"",
			JavacTestOptions.DEFAULT);
}
public void testConflictWithUnnamedModule() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "ignore-unnamed-module-test.jar";
	String[] defaultLibs = getDefaultClassPaths();
	int len = defaultLibs.length;
	String[] libs = new String[len+1];
	System.arraycopy(defaultLibs, 0, libs, 0, len);
	libs[len] = path;
	Runner runner = new Runner();
	runner.classLibraries = libs;
	runner.testFiles =
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo() {\n" +
				"		String s = org.xml.sax.helpers.NamespaceSupport.XMLNS;\n" +
				"	}\n" +
				"}"
			};
	runner.expectedErrorString =
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	String s = org.xml.sax.helpers.NamespaceSupport.XMLNS;\n" +
			"	           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"The package org.xml.sax.helpers is accessible from more than one module: <unnamed>, java.xml\n" +
			"----------\n";
	runner.runNegativeTest();
}
}
