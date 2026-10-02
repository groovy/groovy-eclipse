/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * Describes a class that can build a completion engine.
 */
public interface ICompletionEngineProvider {

	/**
	 * Returns a new completion engine built with the given parameters.
	 *
	 * @param nameEnvironment the name environment
	 * @param requestor the search requestor
	 * @param settings the settings
	 * @param javaProject the java project that  completion will be invoked under
	 * @param owner the working copy owner of the
	 * @param monitor the progress monitor
	 * @return a completion engine built with the given parameters
	 */
	ICompletionEngine newCompletionEngine(
			SearchableEnvironment nameEnvironment,
			CompletionRequestor requestor,
			Map<String, String> settings,
			IJavaProject javaProject,
			WorkingCopyOwner owner,
			IProgressMonitor monitor);

}
