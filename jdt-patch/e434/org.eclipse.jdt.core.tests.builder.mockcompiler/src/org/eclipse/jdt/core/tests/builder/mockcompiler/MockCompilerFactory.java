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

package org.eclipse.jdt.core.tests.builder.mockcompiler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.CompilerConfiguration;
import org.eclipse.jdt.internal.compiler.ICompilerFactory;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class MockCompilerFactory implements ICompilerFactory {
	public static Set<Consumer<Compiler>> listeners = new HashSet<>();

	@Override
	public Compiler newCompiler(INameEnvironment environment, IErrorHandlingPolicy policy,
			CompilerConfiguration compilerConfig, ICompilerRequestor requestor, IProblemFactory problemFactory) {
		Compiler compiler = new MockCompiler(environment, policy, compilerConfig, requestor, problemFactory);
		for (Consumer<Compiler> listener : listeners) {
			listener.accept(compiler);
		}

		return compiler;
	}

	public static void addListener(Consumer<Compiler> listener) {
		listeners.add(listener);
	}

	public static void removeListener(Consumer<Compiler> listener) {
		listeners.remove(listener);
	}

	public static class MockCompiler extends org.eclipse.jdt.internal.compiler.Compiler {
		public CompilerConfiguration compilerConfig;

		public MockCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerConfiguration compilerConfig,
				ICompilerRequestor requestor, IProblemFactory problemFactory) {
			super(environment, policy, compilerConfig.compilerOptions(), requestor, problemFactory);
			this.compilerConfig = compilerConfig;
		}

		@Override
		public void compile(ICompilationUnit[] sourceUnits) {
			for (int i = 0; i < sourceUnits.length; i++) {
				ICompilationUnit in = sourceUnits[i];
				CompilationResult result = new CompilationResult(in, i, sourceUnits.length, Integer.MAX_VALUE);
				if (i == 0) {
					CategorizedProblem problem = new DefaultProblem(in.getFileName(),
							"Compilation error from MockCompiler",
							0,
							new String[0],
							ProblemSeverities.Error,
							0, 0, 0, 0);
					result.problems = new CategorizedProblem[] { problem };
					result.problemCount = result.problems.length;
				}

				this.requestor.acceptResult(result);
			}
		}
	}
}
