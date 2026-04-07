/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.runtime;

public interface RuntimeConstants {
	public static final String SUPPORT_ZIP_FILE_NAME = "EvalTestsTarget.zip";
	public static final String CODE_SNIPPET_RUNNER_CLASS_NAME = "org.eclipse.jdt.core.tests.eval.target.CodeSnippetRunner";
	public static final String RUN_CODE_SNIPPET_METHOD = "runCodeSnippet";
	public static final String THE_RUNNER_FIELD = "theRunner";
	public static final String EVALPORT_ARG = "-evalport";
	public static final String CODESNIPPET_CLASSPATH_ARG = "-cscp";
	public static final String CODESNIPPET_BOOTPATH_ARG = "-csbp";
}
