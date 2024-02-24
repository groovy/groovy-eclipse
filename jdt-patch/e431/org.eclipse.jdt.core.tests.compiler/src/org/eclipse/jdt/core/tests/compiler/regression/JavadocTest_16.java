/*******************************************************************************
 * Copyright (c) 2023 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocTest_16 extends JavadocTest {

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

public JavadocTest_16(String name) {
	super(name);
}

public static Class javadocTestClass() {
	return JavadocTest_16.class;
}

// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {

}

public static Test suite() {
	return buildMinimalComplianceTestSuite(javadocTestClass(), F_16);
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


public void testInlineReturn1() {
	if(this.complianceLevel < ClassFileConstants.JDK16) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
			public class X {
				/** {@return 42} */
				public int sample() {
					return 42;
				}
			}
			""",
		}
	);
}
public void testInlineReturn2() {
	if(this.complianceLevel < ClassFileConstants.JDK16) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
			public class X {
				/** {@return some lengthy description} */
				public int sample() {
					return 42;
				}
			}
			""",
		}
	);
}

public void testInlineReturn_broken1() {
	if(this.complianceLevel < ClassFileConstants.JDK16) {
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
			public class X {
				/** {@return with unbalanced brace{} */
				public int sample() {
					return 42;
				}
			}
			""",
		},

		"""
		----------
		1. ERROR in X.java (at line 2)
			/** {@return with unbalanced brace{} */
			    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		Javadoc: Missing closing brace for inline tag
		----------
		""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void testInlineReturn_broken2() {
	if(this.complianceLevel < ClassFileConstants.JDK16) {
		return;
	}
	this.runNegativeTest(
			new String[] {
					"X.java",
			"""
			public class X
				/** {@return with unbalanced brace}} */
				public int sample() {
					return 42;
				}
			}
			""",
			},

		"""
		----------
		1. ERROR in X.java (at line 1)
			public class X
			             ^
		Syntax error on token "X", { expected after this token
		----------
		""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
			);
}
}

