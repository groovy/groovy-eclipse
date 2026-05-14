/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

/**
 * A factory used to produce a compiler to compile the Java files.
 */
public interface ICompilerFactory {

	/**
	 * Create a new compiler using the given name environment and compiler options.
	 *
	 * @param environment - the type system environment used for resolving types and packages
	 * @param policy - the error handling policy
	 * @param compilerConfig - the configuration to control the compiler behavior
	 * @param requestor - the requestor to receive and persist compilation results
	 * @param problemFactory - the factory to create problem descriptors
	 * @param compilationProgress - the CompilationProgress to be used
	 * @return the new compiler instance
	 * @see CompilationProgress
	 */
	public Compiler newCompiler(INameEnvironment environment,
			IErrorHandlingPolicy policy,
			CompilerConfiguration compilerConfig,
			ICompilerRequestor requestor,
			IProblemFactory problemFactory,
			CompilationProgress compilationProgress
			);
}
