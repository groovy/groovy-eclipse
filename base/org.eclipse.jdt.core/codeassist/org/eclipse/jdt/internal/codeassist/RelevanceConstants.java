/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

public interface RelevanceConstants {

	int R_DEFAULT = 0;
	int R_INTERESTING = 5;
	int R_CASE = 10;
	int R_CAMEL_CASE = 5;
	int R_EXACT_NAME = 4;
	int R_EXPECTED_TYPE = 20;
	int R_EXACT_EXPECTED_TYPE = 30;
	int R_INTERFACE = 20;
	int R_CLASS = 20;
	int R_ENUM = 20;
	int R_ANNOTATION = 20;
	int R_EXCEPTION = 20;
	int R_ENUM_CONSTANT = 5;
	int R_ABSTRACT_METHOD = 20;
	int R_NON_STATIC = 11;
	int R_UNQUALIFIED = 3;
	int R_QUALIFIED = 2;
	int R_NAME_FIRST_PREFIX = 6;
	int R_NAME_PREFIX = 5;
	int R_NAME_FIRST_SUFFIX = 4;
	int R_NAME_SUFFIX = 3;
	int R_NAME_LESS_NEW_CHARACTERS = 15;
	int R_METHOD_OVERIDE = 3;
	int R_NON_RESTRICTED = 3;
	int R_TRUE_OR_FALSE = 1;
	int R_INLINE_TAG = 31;
	int R_VALUE_TAG = 31;
	int R_NON_INHERITED = 2;
	int R_NO_PROBLEMS = 1;
	int R_RESOLVED = 1;
	int R_TARGET = 5;
}
