// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Harry Terkelsen (het@google.com) - Bug 449262 - Allow the use of third-party Java formatters
 *     Gábor Kövesdán - Contribution for Bug 350000 - [content assist] Include non-prefix matches in auto-complete suggestions
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;

/**
 * JavaCore eclipse preferences initializer.
 * Initially done in JavaCore.initializeDefaultPreferences which was deprecated
 * with new eclipse preferences mechanism.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavaCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// If modified, also modify the method JavaModelManager#getDefaultOptionsNoInitialization()
		// and also consider updating org.eclipse.jdt.internal.compiler.batch.Main#initializeWarnings(String)
		// Get options names set
		HashSet optionNames = JavaModelManager.getJavaModelManager().optionNames;

		// Compiler settings
		Map defaultOptionsMap = new CompilerOptions().getMap(); // compiler defaults

		String testDefaults = System.getProperty("jdt.default.test.compliance"); //$NON-NLS-1$
		if (testDefaults != null) {
			defaultOptionsMap.put(JavaCore.COMPILER_SOURCE, testDefaults);
			defaultOptionsMap.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, testDefaults);
			defaultOptionsMap.put(JavaCore.COMPILER_COMPLIANCE, testDefaults);
		}

		// Override some compiler defaults
		defaultOptionsMap.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		defaultOptionsMap.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		defaultOptionsMap.put(JavaCore.COMPILER_TASK_TAGS, JavaCore.DEFAULT_TASK_TAGS);
		defaultOptionsMap.put(JavaCore.COMPILER_TASK_PRIORITIES, JavaCore.DEFAULT_TASK_PRIORITIES);
		defaultOptionsMap.put(JavaCore.COMPILER_TASK_CASE_SENSITIVE, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);

		// Builder settings
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT);
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, JavaCore.WARNING);
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, JavaCore.CLEAN);
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER, JavaCore.IGNORE);

		// JavaCore settings
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_ORDER, JavaCore.IGNORE);
		defaultOptionsMap.put(JavaCore.CORE_INCOMPLETE_CLASSPATH, JavaCore.ERROR);
		defaultOptionsMap.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.ERROR);
		defaultOptionsMap.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.IGNORE);
		defaultOptionsMap.put(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.ERROR);

		// encoding setting comes from resource plug-in
		optionNames.add(JavaCore.CORE_ENCODING);

		// Formatter settings
		Map codeFormatterOptionsMap = DefaultCodeFormatterConstants.getEclipseDefaultSettings(); // code formatter defaults
		for (Iterator iter = codeFormatterOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			defaultOptionsMap.put(optionName, entry.getValue());
			optionNames.add(optionName);
		}
		defaultOptionsMap.put(JavaCore.JAVA_FORMATTER, JavaCore.DEFAULT_JAVA_FORMATTER);

		// CodeAssist settings
		defaultOptionsMap.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_IMPLICIT_QUALIFICATION, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_FIELD_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_LOCAL_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_SUBSTRING_MATCH, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, JavaCore.ENABLED);
		defaultOptionsMap.put(ChunkCache.CHUNK_CACHE_SIZE_MB, Double.toString(ChunkCache.CHUNK_CACHE_SIZE_MB_DEFAULT));
		defaultOptionsMap.put(ChunkCache.CHUNK_CACHE_SIZE_PERCENT,
				Double.toString(ChunkCache.CHUNK_CACHE_SIZE_PERCENT_DEFAULT));
		defaultOptionsMap.put(JavaIndex.ENABLE_NEW_JAVA_INDEX,
				Boolean.toString(JavaIndex.ENABLE_NEW_JAVA_INDEX_DEFAULT));

		// Time out for parameter names
		defaultOptionsMap.put(JavaCore.TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC, "50"); //$NON-NLS-1$

		// Store default values to default preferences
	 	IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		for (Iterator iter = defaultOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			defaultPreferences.put(optionName, (String)entry.getValue());
			optionNames.add(optionName);
		}

		// GROOVY add
		optionNames.add(CompilerOptions.OPTIONG_GroovyFlags);
		optionNames.add(CompilerOptions.OPTIONG_BuildGroovyFiles);
		optionNames.add(CompilerOptions.OPTIONG_GroovyProjectName);
		optionNames.add(CompilerOptions.OPTIONG_GroovyCompilerConfigScript);
		// GROOVY end

		// Initialize deprecated options
		initializeDeprecatedOptions();
	}

	/**
	 * @deprecated As using deprecated options
	 */
	private void initializeDeprecatedOptions() {
		Map deprecatedOptions = JavaModelManager.getJavaModelManager().deprecatedOptions;
		deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_MEMBER,
			new String[] {
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_FIELD,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_METHOD,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PACKAGE,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_TYPE
			});
		deprecatedOptions.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION,
			new String[] {
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_FIELD,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_METHOD,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PACKAGE,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_TYPE,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_LOCAL_VARIABLE,
				DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_ANNOTATION_ON_PARAMETER
			});
	}
}
