/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler;

public class DefaultErrorHandlingPolicies {

/*
 * Accumulate all problems, then exit without proceeding.
 *
 * Typically, the #proceedWithProblems(Problem[]) should
 * show the problems.
 */
public static IErrorHandlingPolicy exitAfterAllProblems() {
	return new IErrorHandlingPolicy() {
		@Override
		public boolean stopOnFirstError() {
			return false;
		}
		@Override
		public boolean proceedOnErrors(){
			return false;
		}
		@Override
		public boolean ignoreAllErrors() {
			return false;
		}
		@Override
		public String toString() {
			return "ExitAfterAllProblems"; //$NON-NLS-1$
		}
	};
}
/*
 * Exit without proceeding on the first problem wich appears
 * to be an error.
 */
public static IErrorHandlingPolicy exitOnFirstError() {
	return new IErrorHandlingPolicy() {
		@Override
		public boolean stopOnFirstError() {
			return true;
		}
		@Override
		public boolean proceedOnErrors(){
			return false;
		}
		@Override
		public boolean ignoreAllErrors() {
			return false;
		}
		@Override
		public String toString() {
			return "ExitOnFirstError"; //$NON-NLS-1$
		}
	};
}
/*
 * Proceed on the first error met.
 */
public static IErrorHandlingPolicy proceedOnFirstError() {
	return new IErrorHandlingPolicy() {
		@Override
		public boolean stopOnFirstError() {
			return true;
		}
		@Override
		public boolean proceedOnErrors(){
			return true;
		}
		@Override
		public boolean ignoreAllErrors() {
			return false;
		}
		@Override
		public String toString() {
			return "ProceedOnFirstError"; //$NON-NLS-1$
		}
	};
}
/*
 * Accumulate all problems, then proceed with them.
 */
public static IErrorHandlingPolicy proceedWithAllProblems() {
	return new IErrorHandlingPolicy() {
		@Override
		public boolean stopOnFirstError() {
			return false;
		}
		@Override
		public boolean proceedOnErrors(){
			return true;
		}
		@Override
		public boolean ignoreAllErrors() {
			return false;
		}
		@Override
		public String toString() {
			return "ProceedWithAllProblems"; //$NON-NLS-1$
		}
	};
}
/*
 * Accumulate all problems, then proceed with them, but never report them.
 */
public static IErrorHandlingPolicy ignoreAllProblems() {
	return new IErrorHandlingPolicy() {
		@Override
		public boolean stopOnFirstError() {
			return false;
		}
		@Override
		public boolean proceedOnErrors(){
			return true;
		}
		@Override
		public boolean ignoreAllErrors() {
			return true;
		}
		@Override
		public String toString() {
			return "IgnoreAllProblems"; //$NON-NLS-1$
		}
	};
}
}
