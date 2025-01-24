/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Jesper Steen Moeller - Contributions for:
 *         Bug 407297: [1.8][compiler] Control generation of parameter names by option
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.tool;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Class used to handle options in the EclipseFileManager and the EclipseCompiler
 */
public final class Options {
	private static final Set<String> ZERO_ARGUMENT_OPTIONS;
	private static final Set<String> ONE_ARGUMENT_OPTIONS;
	private static final Set<String> FILE_MANAGER_OPTIONS;
	static {
		ZERO_ARGUMENT_OPTIONS = new HashSet<>();
		Options.ZERO_ARGUMENT_OPTIONS.add("-progress");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-proceedOnError");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-proceedOnError:Fatal");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-time");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-v");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-version");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-showversion");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-deprecation");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-help");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-?");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-help:warn");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-?:warn");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-noExit");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-verbose");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-referenceInfo");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-inlineJSR");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-g");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-g:none");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-warn:none");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-preserveAllLocals");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-enableJavadoc");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-Xemacs");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-X");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-O");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-1.3");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-1.4");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-1.5");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-5");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-5.0");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-1.6");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-6");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-6.0");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-1.7");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-7");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-7.0");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-1.8");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-8");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-8.0");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-proc:only");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-proc:none");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-XprintProcessorInfo");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-XprintRounds");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-parameters");//$NON-NLS-1$
		Options.ZERO_ARGUMENT_OPTIONS.add("-genericsignature");//$NON-NLS-1$

		FILE_MANAGER_OPTIONS = new HashSet<>();
		Options.FILE_MANAGER_OPTIONS.add("-bootclasspath");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-encoding");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-d");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-classpath");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-cp");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-sourcepath");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-extdirs");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-endorseddirs");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-s");//$NON-NLS-1$
		Options.FILE_MANAGER_OPTIONS.add("-processorpath");//$NON-NLS-1$

		ONE_ARGUMENT_OPTIONS = new HashSet<>();
		Options.ONE_ARGUMENT_OPTIONS.addAll(Options.FILE_MANAGER_OPTIONS);
		Options.ONE_ARGUMENT_OPTIONS.add("-log");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-repeat");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-maxProblems");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-source");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-target");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-processor");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-classNames");//$NON-NLS-1$
		Options.ONE_ARGUMENT_OPTIONS.add("-properties");//$NON-NLS-1$

	}
	public static int processOptionsFileManager(String option) {
		if (option == null) return -1;
		if (Options.FILE_MANAGER_OPTIONS.contains(option)) {
			return 1;
		}
		return -1;
	}

	public static int processOptions(String option) {
		if (option == null) return -1;
		if (Options.ZERO_ARGUMENT_OPTIONS.contains(option)) {
			return 0;
		}
		if (Options.ONE_ARGUMENT_OPTIONS.contains(option)) {
			return 1;
		}
		if (option.startsWith("-g")) { //$NON-NLS-1$
			int length = option.length();
			if (length > 3) {
				StringTokenizer tokenizer =
					new StringTokenizer(option.substring(3, option.length()), ",");//$NON-NLS-1$
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if ("vars".equals(token) || "lines".equals(token) || "source".equals(token)) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						continue;
					}
					return -1;
				}
				return 0;
			}
			return -1;
		}
		if (option.startsWith("-warn")) {//$NON-NLS-1$
			int length = option.length();
			if (length <= 6) {
				return -1;
			}
			int warnTokenStart;
			switch (option.charAt(6)) {
				case '+' :
					warnTokenStart = 7;
					break;
				case '-' :
					warnTokenStart = 7;
					break;
				default:
					warnTokenStart = 6;
			}

			StringTokenizer tokenizer =
				new StringTokenizer(option.substring(warnTokenStart, option.length()), ","); //$NON-NLS-1$
			int tokenCounter = 0;

			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				tokenCounter++;
				if (token.equals("allDeadCode")//$NON-NLS-1$
						|| token.equals("allDeprecation")//$NON-NLS-1$
						|| token.equals("allJavadoc")//$NON-NLS-1$
						|| token.equals("allOver-ann")//$NON-NLS-1$
						|| token.equals("assertIdentifier")//$NON-NLS-1$
						|| token.equals("boxing")//$NON-NLS-1$
						|| token.equals("charConcat")//$NON-NLS-1$
						|| token.equals("compareIdentical")//$NON-NLS-1$
						|| token.equals("conditionAssign")//$NON-NLS-1$
						|| token.equals("constructorName")//$NON-NLS-1$
						|| token.equals("deadCode")//$NON-NLS-1$
						|| token.equals("dep-ann")//$NON-NLS-1$
						|| token.equals("deprecation")//$NON-NLS-1$
						|| token.equals("discouraged")//$NON-NLS-1$
						|| token.equals("emptyBlock")//$NON-NLS-1$
						|| token.equals("enumIdentifier")//$NON-NLS-1$
						|| token.equals("enumSwitch")//$NON-NLS-1$
						|| token.equals("fallthrough")//$NON-NLS-1$
						|| token.equals("fieldHiding")//$NON-NLS-1$
						|| token.equals("finalBound")//$NON-NLS-1$
						|| token.equals("finally")//$NON-NLS-1$
						|| token.equals("forbidden")//$NON-NLS-1$
						|| token.equals("hashCode")//$NON-NLS-1$
						|| token.equals("hiding")//$NON-NLS-1$
						|| token.equals("includeAssertNull")//$NON-NLS-1$
						|| token.equals("incomplete-switch")//$NON-NLS-1$
						|| token.equals("indirectStatic")//$NON-NLS-1$
						|| token.equals("interfaceNonInherited")//$NON-NLS-1$
						|| token.equals("intfAnnotation")//$NON-NLS-1$
						|| token.equals("intfNonInherited")//$NON-NLS-1$
						|| token.equals("intfRedundant")//$NON-NLS-1$
						|| token.equals("javadoc")//$NON-NLS-1$
						|| token.equals("localHiding")//$NON-NLS-1$
						|| token.equals("maskedCatchBlock")//$NON-NLS-1$
						|| token.equals("maskedCatchBlocks")//$NON-NLS-1$
						|| token.equals("nls")//$NON-NLS-1$
						|| token.equals("noEffectAssign")//$NON-NLS-1$
						|| token.equals("noImplicitStringConversion")//$NON-NLS-1$
						|| token.equals("null")//$NON-NLS-1$
						|| token.equals("nullDereference")//$NON-NLS-1$
						|| token.equals("over-ann")//$NON-NLS-1$
						|| token.equals("packageDefaultMethod")//$NON-NLS-1$
						|| token.equals("paramAssign")//$NON-NLS-1$
						|| token.equals("pkgDefaultMethod")//$NON-NLS-1$
						|| token.equals("raw")//$NON-NLS-1$
						|| token.equals("semicolon")//$NON-NLS-1$
						|| token.equals("serial")//$NON-NLS-1$
						|| token.equals("specialParamHiding")//$NON-NLS-1$
						|| token.equals("static-access")//$NON-NLS-1$
						|| token.equals("staticReceiver")//$NON-NLS-1$
						|| token.equals("super")//$NON-NLS-1$
						|| token.equals("suppress")//$NON-NLS-1$
						|| token.equals("syncOverride")//$NON-NLS-1$
						|| token.equals("synthetic-access")//$NON-NLS-1$
						|| token.equals("syntheticAccess")//$NON-NLS-1$
						|| token.equals("typeHiding")//$NON-NLS-1$
						|| token.equals("unchecked")//$NON-NLS-1$
						|| token.equals("unnecessaryElse")//$NON-NLS-1$
						|| token.equals("unnecessaryOperator")//$NON-NLS-1$
						|| token.equals("unqualified-field-access")//$NON-NLS-1$
						|| token.equals("unqualifiedField")//$NON-NLS-1$
						|| token.equals("unsafe")//$NON-NLS-1$
						|| token.equals("unused")//$NON-NLS-1$
						|| token.equals("unusedArgument")//$NON-NLS-1$
						|| token.equals("unusedArguments")//$NON-NLS-1$
						|| token.equals("unusedImport")//$NON-NLS-1$
						|| token.equals("unusedImports")//$NON-NLS-1$
						|| token.equals("unusedLabel")//$NON-NLS-1$
						|| token.equals("unusedLocal")//$NON-NLS-1$
						|| token.equals("unusedLocals")//$NON-NLS-1$
						|| token.equals("unusedPrivate")//$NON-NLS-1$
						|| token.equals("unusedThrown")//$NON-NLS-1$
						|| token.equals("unusedTypeArgs")//$NON-NLS-1$
						|| token.equals("uselessTypeCheck")//$NON-NLS-1$
						|| token.equals("varargsCast")//$NON-NLS-1$
						|| token.equals("warningToken")) {//$NON-NLS-1$
					continue;
				} else if (token.equals("tasks")) {//$NON-NLS-1$
					String taskTags = "";//$NON-NLS-1$
					int start = token.indexOf('(');
					int end = token.indexOf(')');
					if (start >= 0 && end >= 0 && start < end){
						taskTags = token.substring(start+1, end).trim();
						taskTags = taskTags.replace('|',',');
					}
					if (taskTags.length() == 0){
						return -1;
					}
					continue;
				} else {
					return -1;
				}
			}
			if (tokenCounter == 0) {
				return -1;
			} else {
				return 0;
			}
		}
		if (option.startsWith("-nowarn")) {//$NON-NLS-1$
			switch (option.length()) {
				case 7:
					return 0;
				case 8:
					return -1;
				default:
					int foldersStart = option.indexOf('[') + 1;
					int foldersEnd = option.lastIndexOf(']');
					if (foldersStart <= 8 || foldersEnd == -1
							|| foldersStart > foldersEnd
							|| foldersEnd < option.length() - 1) {
						return -1;
					}
					String folders = option.substring(foldersStart, foldersEnd);
					if (folders.length() > 0) {
						return 0;
					} else {
						return -1;
					}
			}
		}
		if (option.startsWith("-J")//$NON-NLS-1$
				|| option.startsWith("-X")//$NON-NLS-1$
				|| option.startsWith("-A")) {//$NON-NLS-1$
			return 0;
		}
		return -1;
	}
}
