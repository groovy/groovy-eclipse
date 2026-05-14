/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 376590 - Private fields with @Inject are ignored by unused field validation
 *     Ulrich Grave <ulrich.grave@gmx.de> - Contributions for
 *                              bug 386692 - Missing "unused" warning on "autowired" fields
 *     Pierre-Yves B. <pyvesdev@gmail.com> - Contribution for
 *                              bug 542520 - [JUnit 5] Warning The method xxx from the type X is never used locally is shown when using MethodSource
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
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
		"package jakarta.inject;\n" +
		"import static java.lang.annotation.ElementType.*;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
		"import java.lang.annotation.Target;\n" +
		"@Target({ METHOD, CONSTRUCTOR, FIELD })\n" +
		"@Retention(RUNTIME)\n" +
		"public @interface Inject {}\n";

	protected static final String JAKARTA_INJECT_NAME = "jakarta/inject/Inject.java";
	protected static final String JAKARTA_INJECT_CONTENT =
		"package jakarta.inject;\n" +
		"import static java.lang.annotation.ElementType.*;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import static java.lang.annotation.RetentionPolicy.RUNTIME;\n" +
		"import java.lang.annotation.Target;\n" +
		"@Target({ METHOD, CONSTRUCTOR, FIELD })\n" +
		"@Retention(RUNTIME)\n" +
		"public @interface Inject {}\n";

	protected static final String SPRINGFRAMEWORK_AUTOWIRED_NAME = "org/springframework/beans/factory/annotation/Autowired.java";
	protected static final String SPRINGFRAMEWORK_AUTOWIRED_CONTENT =
		"package org.springframework.beans.factory.annotation;\n" +
		"import java.lang.annotation.Documented;\n" +
		"import java.lang.annotation.ElementType;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import java.lang.annotation.RetentionPolicy;\n" +
		"import java.lang.annotation.Target;\n" +
		"@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})\n" +
		"@Retention(RetentionPolicy.RUNTIME)\n" +
		"@Documented\n" +
		"public @interface Autowired {\n" +
		"\n" +
		"	boolean required() default true;\n" +
		"\n" +
		"}";

	protected static final String JUNIT_METHODSOURCE_NAME = "org/junit/jupiter/params/provider/MethodSource.java";
	protected static final String JUNIT_METHODSOURCE_CONTENT =
	    "package org.junit.jupiter.params.provider;\n" +
	    "import java.lang.annotation.ElementType;\n" +
	    "import java.lang.annotation.Retention;\n" +
	    "import java.lang.annotation.RetentionPolicy;\n" +
	    "import java.lang.annotation.Target;\n" +
	    "@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})\n" +
	    "@Retention(RetentionPolicy.RUNTIME)\n" +
	    "public @interface MethodSource {\n" +
	    "\n" +
	    "	String[] value() default \"\";\n" +
	    "\n" +
	    "}";

	public static Test buildComparableTestSuite(Class evaluationTestClass) {
		Test suite = buildMinimalComplianceTestSuite(evaluationTestClass, FIRST_SUPPORTED_JAVA_VERSION);
		TESTS_COUNTERS.put(evaluationTestClass.getName(), Integer.valueOf(suite.countTestCases()));
		return suite;
	}

	public AbstractComparableTest(String name) {
		super(name);
	}

	/*
	 * Toggle compiler in mode -1.5
	 */
	@Override
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

	protected String intersection(String... types) {
		// From JDK 12, Comparable gets two new super interfaces, namely Constable and ConstantDesc.
		// The idea is to append Comparable with &Constable&ConstantDesc automatically.
		if (isJRE12Plus) {
			int index = -1;
			for(int i = 0; i < types.length; i++) {
				if (types[i].startsWith("Comparable") && !types[i].endsWith("ConstantDesc")) {
					if ((types.length <= i+1) || !types[i+1].startsWith("CharSequence")) {
						index = i;
						break;
					}
				}
			}
			if (index >= 0) {
				index++;
				String[] temp = new String[types.length + 2];
				System.arraycopy(types, 0, temp, 0, index);
				temp[index] = "Constable";
				temp[index+1] = "ConstantDesc";
				if (index < types.length)
 					System.arraycopy(types, index, temp, index+2, types.length - index);
				types = temp;
			}
		}
		return String.join(" & ", types);
	}
}
