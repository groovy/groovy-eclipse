/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

public class SourcePackage {
	int declarationSourceStart;
	int declarationSourceEnd;
	char[] name;
	char[] source;
/**
 * @param declarationSourceStart int
 * @param declarationSourceEnd int
 * @param name char[]
 * @param source char[]
 */
public SourcePackage(
	int declarationSourceStart,
	int declarationSourceEnd,
	char[] name,
	char[] source) {

	this.declarationSourceStart = declarationSourceStart;
	this.declarationSourceEnd = declarationSourceEnd;
	this.name = name;
	this.source = source;
}
/**
 *
 * @return java.lang.String
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer
		.append(
			this.source,
			this.declarationSourceStart,
			this.declarationSourceEnd - this.declarationSourceStart + 1)
		.append("\n");
	return buffer.toString();
}
}
