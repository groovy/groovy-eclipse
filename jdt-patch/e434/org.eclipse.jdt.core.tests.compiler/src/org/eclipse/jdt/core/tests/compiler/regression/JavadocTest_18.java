/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTest_18 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String reportJavadocDeprecation = null;
	String processAnnotations = null;

public JavadocTest_18(String name) {
	super(name);
}

public static Class javadocTestClass() {
	return JavadocTest_18.class;
}

// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {

}

public static Test suite() {
	return buildMinimalComplianceTestSuite(javadocTestClass(), F_18);
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
	if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
	}
	if (this.reportJavadocDeprecation != null) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportJavadocDeprecation);
	}
	if (this.reportMissingJavadocComments != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
		if (this.reportMissingJavadocCommentsVisibility != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		}
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocTags != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocDescription != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, this.reportMissingJavadocDescription);
	}
	if (this.processAnnotations != null) {
		options.put(CompilerOptions.OPTION_Process_Annotations, this.processAnnotations);
	}
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportDeprecation, this.reportDeprecation);
	options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	return options;
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.docCommentSupport = CompilerOptions.ENABLED;
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	this.reportDeprecation = CompilerOptions.ERROR;
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
}



public void test001() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet : public static void main(String... args) {\n"
				+ " *       for (var arg : args) {                 \n"
				+ " *           if (!arg.isBlank()) {\n"
				+ " *               System.out.println(arg);\n"
				+ " *           }\n"
				+ " *       }                                      \n"
				+ " *   }\n"
				+ " *   }\n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	* {@snippet : public static void main(String... args) {\n" +
				"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Snippet content should be on a new line\n" +
				"----------\n",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test002() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet : "
				+ " * public static void main(String... args) {\n"
				+ " *       for (var arg : args) {                 \n"
				+ " *           if (!arg.isBlank()) {\n"
				+ " *               System.out.println(arg);\n"
				+ " *           }\n"
				+ " *       }                                      \n"
				+ " *   }\n"
				+ " *   }\n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		null,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test003() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet:\n"
				+ " *abcd                  \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
				"1. ERROR in X.java (at line 2)\n" +
				"	* {@snippet:\n" +
				"	  ^^^^^^^^^^\n" +
				"Javadoc: Missing closing brace for inline tag\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 2)\n" +
				"	* {@snippet:\n" +
				"	    ^^^^^^^^\n" +
				"Javadoc: Space required after snippet tag\n" +
				"----------\n",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test004() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet :\n"
				+ " *abcd   }               \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		}
	);
}

public void test005() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet :\n"
				+ " * while(true){{{             \n"
				+ " * }             \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	* {@snippet :\n" +
		"	  ^^^^^^^^^^^\n" +
		"Javadoc: Missing closing brace for inline tag\n" +
		"----------\n"
	,
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test006() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet \n"
				+ " *              \n"
				+ " * }             \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	* }             \n" +
		"	   ^^^^^^^^^^^^^\n" +
		"Javadoc: Snippet is invalid due to missing colon\n" +
		"----------\n",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test007() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet \n"
				+ " *              \n"
				+ " * }             \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	* }             \n" +
				"	   ^^^^^^^^^^^^^\n" +
				"Javadoc: Snippet is invalid due to missing colon\n" +
				"----------\n",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void test008() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet : \n"
				+ " *     abc // @replace substring='a'  regex='a'       \n"
				+ " * }             \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
				"1. ERROR in X.java (at line 3)\n" +
				"	*     abc // @replace substring=\'a\'  regex=\'a\'       \n" +
				"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
				"Javadoc: Attribute regex and substring used simulataneously\n" +
				"----------\n",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test009() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet : \n"
				+ " *     abc // @highlight substring='a'  region='abcd'      \n"
				+ " *      //@end region='abc'        \n"
				+ " * }             \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	* }             \n" +
				"	   ^^^^^^^^^^^^^\n" +
				"Javadoc: Region in the snippet is not closed\n" +
				"----------\n",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test010() {
	if(this.complianceLevel < ClassFileConstants.JDK18) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
				" /**\n"
				+ " * {@snippet : \n"
				+ " *     abc // @highlight substring='a'  region='abcd'      \n"
				+ " *     abc // @highlight substring='a'  region='abcd'      \n"
				+ " *      //@end region='abcd'        \n"
				+ " *      //@end region='abcd'        \n"
				+ " * }             \n"
				+ " */\n"
				+ "public class X {\n"
				+ "}",
		},
		"----------\n" +
				"1. ERROR in X.java (at line 4)\n" +
				"	*     abc // @highlight substring=\'a\'  region=\'abcd\'      \n" +
				"	                                       ^^^^^^\n" +
				"Javadoc: Duplicate region\n" +
				"----------\n",

			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
}

