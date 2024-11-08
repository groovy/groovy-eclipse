/*******************************************************************************
 * Copyright (c) 2014, 2016 IBM Corporation and others.
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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Deprecated18Test extends AbstractRegressionTest {
public Deprecated18Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
public void test412555() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Runnable r = () -> {\n" +
			"			Y.callMe();\n" +
			"		};\n" +
			"	}\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"	@Deprecated\n" +
			 "	public static void callMe() {}\n" +
			"}\n",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Y.callMe();\n" +
		"	  ^^^^^^^^\n" +
		"The method callMe() from the type Y is deprecated\n" +
		"----------\n",
		null,
		true,
		options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1370
// Deprecation warnings are not suppressed in lambdas of deprecated methods
public void testGH1370() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		false /* skipJavac */,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError,
		new String[] {
			"X.java",
			"""
			public class X {
			  @Deprecated
			  static void deprecatedMethod(Object o) {}
			}
			""",
			"Y.java",
			"""
			import java.util.List;
			public class Y {
			  @Deprecated
			  void callDeprecated() {
			    X.deprecatedMethod(null);                   // no warning
			    List.of().forEach(X::deprecatedMethod);     // no warning
			    List.of().forEach(o -> X.deprecatedMethod(o)); // warning
			  }
			  void callDeprecated2() {
  			    X.deprecatedMethod(null);
			    List.of().forEach(X::deprecatedMethod);
			    List.of().forEach(o -> X.deprecatedMethod(o));
			  }
			}
			""",
		},
		"""
		----------
		1. ERROR in Y.java (at line 10)
			X.deprecatedMethod(null);
			  ^^^^^^^^^^^^^^^^^^^^^^
		The method deprecatedMethod(Object) from the type X is deprecated
		----------
		2. ERROR in Y.java (at line 11)
			List.of().forEach(X::deprecatedMethod);
			                  ^^^^^^^^^^^^^^^^^^^
		The method deprecatedMethod(Object) from the type X is deprecated
		----------
		3. ERROR in Y.java (at line 12)
			List.of().forEach(o -> X.deprecatedMethod(o));
			                         ^^^^^^^^^^^^^^^^^^^
		The method deprecatedMethod(Object) from the type X is deprecated
		----------
		""",
		null,
		true,
		options);
}
public static Class testClass() {
	return Deprecated18Test.class;
}
}
