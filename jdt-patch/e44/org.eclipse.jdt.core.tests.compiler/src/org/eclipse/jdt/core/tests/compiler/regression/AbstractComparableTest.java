/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 376590 - Private fields with @Inject are ignored by unused field validation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractComparableTest extends AbstractRegressionTest {

	protected static final String GOOGLE_INJECT_NAME = "com/google/inject/Inject.java";
	protected static final String GOOGLE_INJECT_CONTENT = 
		"package com.google.inject;\n" + 
		"import static java.lang.annotation.ElementType.*;\n" + 
		"import java.lang.annotation.Retention;\n" + 
		"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
		"import java.lang.annotation.Target;\n" +
		"@Target({ METHOD, CONSTRUCTOR, FIELD })\n" + 
		"@Retention(RUNTIME)\n" + 
		"public @interface Inject {\n" + 
		"\n" + 
		"  boolean optional() default false;\n" + 
		"}";

	protected static final String JAVAX_INJECT_NAME = "javax/inject/Inject.java";
	protected static final String JAVAX_INJECT_CONTENT = 
		"package javax.inject;\n" + 
		"import static java.lang.annotation.ElementType.*;\n" + 
		"import java.lang.annotation.Retention;\n" + 
		"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" + 
		"import java.lang.annotation.Target;\n" +
		"@Target({ METHOD, CONSTRUCTOR, FIELD })\n" + 
		"@Retention(RUNTIME)\n" + 
		"public @interface Inject {}\n";

	public static Test buildComparableTestSuite(Class evaluationTestClass) {
		Test suite = buildMinimalComplianceTestSuite(evaluationTestClass, F_1_5);
		TESTS_COUNTERS.put(evaluationTestClass.getName(), new Integer(suite.countTestCases()));
		return suite;
	}

	public AbstractComparableTest(String name) {
		super(name);
	}

	/*
	 * Toggle compiler in mode -1.5
	 */
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFinalParameterBound, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingDeprecatedAnnotation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
		return options;
	}
}
