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

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

public class DefaultCompilerFactory implements ICompilerFactory {

	@Override
	public Compiler newCompiler(INameEnvironment environment, IErrorHandlingPolicy policy,
			CompilerConfiguration compilerConfig, ICompilerRequestor requestor, IProblemFactory problemFactory) {
		return new Compiler(environment, policy, compilerConfig.compilerOptions(),
				requestor, problemFactory);
	}
}
