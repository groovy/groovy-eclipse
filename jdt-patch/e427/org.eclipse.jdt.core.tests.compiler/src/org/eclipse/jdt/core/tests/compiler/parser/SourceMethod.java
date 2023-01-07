/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;

public class SourceMethod implements ISourceMethod {
	private int modifiers;
	private int declarationStart;
	private int declarationEnd;
	private char[] returnTypeName;
	private char[] selector;
	private int nameSourceStart;
	private int nameSourceEnd;
	private char[][] argumentTypeNames;
	private char[][] argumentNames;
	private char[][] exceptionTypeNames;
	private char[] source;
	private String explicitConstructorCall;
	char[][] typeParameterNames;
	char[][][] typeParameterBounds;

public SourceMethod(
	int declarationStart,
	int modifiers,
	char[] returnTypeName,
	char[] selector,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] argumentTypeNames,
	char[][] argumentNames,
	char[][] exceptionTypeNames,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.returnTypeName = returnTypeName;
	this.selector = selector;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
	this.argumentTypeNames = argumentTypeNames;
	this.argumentNames = argumentNames;
	this.exceptionTypeNames = exceptionTypeNames;
	this.source = source;
}
public String displayModifiers() {
	StringBuilder buffer = new StringBuilder();

	if (this.modifiers == 0)
		return null;
	if ((this.modifiers & ClassFileConstants.AccPublic) != 0)
		buffer.append("public ");
	if ((this.modifiers & ClassFileConstants.AccProtected) != 0)
		buffer.append("protected ");
	if ((this.modifiers & ClassFileConstants.AccPrivate) != 0)
		buffer.append("private ");
	if ((this.modifiers & ClassFileConstants.AccFinal) != 0)
		buffer.append("final ");
	if ((this.modifiers & ClassFileConstants.AccStatic) != 0)
		buffer.append("static ");
	if ((this.modifiers & ClassFileConstants.AccAbstract) != 0)
		buffer.append("abstract ");
	if ((this.modifiers & ClassFileConstants.AccNative) != 0)
		buffer.append("native ");
	if ((this.modifiers & ClassFileConstants.AccSynchronized) != 0)
		buffer.append("synchronized ");
	if (buffer.toString().trim().equals(""))
		return null;
	return buffer.toString().trim();
}
public String getActualName() {
	StringBuilder buffer = new StringBuilder();
	buffer.append(this.source, this.nameSourceStart, this.nameSourceEnd - this.nameSourceStart + 1);
	return buffer.toString();
}
public char[][] getArgumentNames() {
	return this.argumentNames;
}
public char[][] getArgumentTypeNames() {
	return this.argumentTypeNames;
}
public int getDeclarationSourceEnd() {
	return this.declarationEnd;
}
public int getDeclarationSourceStart() {
	return this.declarationStart;
}
public char[][] getExceptionTypeNames() {
	return this.exceptionTypeNames;
}
public int getModifiers() {
	return this.modifiers;
}
public int getNameSourceEnd() {
	return this.nameSourceEnd;
}
public int getNameSourceStart() {
	return this.nameSourceStart;
}
public char[] getReturnTypeName() {
	return this.returnTypeName;
}
public char[] getSelector() {
	return this.selector;
}
public char[][][] getTypeParameterBounds() {
	return this.typeParameterBounds;
}
public char[][] getTypeParameterNames() {
	return this.typeParameterNames;
}
public boolean isConstructor() {
	return this.returnTypeName == null;
}
protected void setDeclarationSourceEnd(int position) {
	this.declarationEnd = position;
}
protected void setExplicitConstructorCall(String s) {
	this.explicitConstructorCall = s;
}
public String tabString(int tab) {
	/*slow code*/

	String s = "";
	for (int i = tab; i > 0; i--)
		s = s + "\t";
	return s;
}
@Override
public String toString() {
	return toString(0);
}
public String toString(int tab) {
	StringBuilder buffer = new StringBuilder();
	buffer.append(tabString(tab));
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers).append(" ");
	}
	if (this.returnTypeName != null) {
		buffer.append(this.returnTypeName).append(" ");
	}
	buffer.append(this.selector).append("(");
	if (this.argumentTypeNames != null) {
		for (int i = 0, max = this.argumentTypeNames.length; i < max; i++) {
			buffer.append(this.argumentTypeNames[i]).append(" ").append(
				this.argumentNames[i]).append(
				", ");
		}
	}
	buffer.append(") ");
	if (this.exceptionTypeNames != null) {
		buffer.append("throws ");
		for (int i = 0, max = this.exceptionTypeNames.length; i < max; i++) {
			buffer.append(this.exceptionTypeNames[i]).append(", ");
		}
	}
	if (this.explicitConstructorCall != null) {
		buffer.append("{\n").append(tabString(tab+1)).append(this.explicitConstructorCall).append(tabString(tab)).append("}");
	} else {
		buffer.append("{}");
	}
	return buffer.toString();
}
}
