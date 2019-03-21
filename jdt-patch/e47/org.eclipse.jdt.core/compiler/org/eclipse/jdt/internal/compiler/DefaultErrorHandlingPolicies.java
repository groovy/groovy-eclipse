/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
 *
 */
public static IErrorHandlingPolicy exitAfterAllProblems() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors(){
			return false;
		}
		public boolean ignoreAllErrors() {
			return false;
		}
	};
}
/*
 * Exit without proceeding on the first problem wich appears
 * to be an error.
 *
 */
public static IErrorHandlingPolicy exitOnFirstError() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return true;
		}
		public boolean proceedOnErrors(){
			return false;
		}
		public boolean ignoreAllErrors() {
			return false;
		}
	};
}
/*
 * Proceed on the first error met.
 *
 */
public static IErrorHandlingPolicy proceedOnFirstError() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return true;
		}
		public boolean proceedOnErrors(){
			return true;
		}
		public boolean ignoreAllErrors() {
			return false;
		}
	};
}
/*
 * Accumulate all problems, then proceed with them.
 *
 */
public static IErrorHandlingPolicy proceedWithAllProblems() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors(){
			return true;
		}
		public boolean ignoreAllErrors() {
			return false;
		}
	};
}
/*
 * Accumulate all problems, then proceed with them, but never report them.
 *
 */
public static IErrorHandlingPolicy ignoreAllProblems() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors(){
			return true;
		}
		public boolean ignoreAllErrors() {
			return true;
		}
	};
}
}
