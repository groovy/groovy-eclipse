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
	StringBuffer buffer = new StringBuffer();

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
	StringBuffer buffer = new StringBuffer();
	buffer.append(source, nameSourceStart, nameSourceEnd - nameSourceStart + 1);
	return buffer.toString();
}
public char[][] getArgumentNames() {
	return argumentNames;
}
public char[][] getArgumentTypeNames() {
	return argumentTypeNames;
}
public int getDeclarationSourceEnd() {
	return declarationEnd;
}
public int getDeclarationSourceStart() {
	return declarationStart;
}
public char[][] getExceptionTypeNames() {
	return exceptionTypeNames;
}
public int getModifiers() {
	return modifiers;
}
public int getNameSourceEnd() {
	return nameSourceEnd;
}
public int getNameSourceStart() {
	return nameSourceStart;
}
public char[] getReturnTypeName() {
	return returnTypeName;
}
public char[] getSelector() {
	return selector;
}
public char[][][] getTypeParameterBounds() {
	return typeParameterBounds;
}
public char[][] getTypeParameterNames() {
	return typeParameterNames;	
}
public boolean isConstructor() {
	return returnTypeName == null;
}
protected void setDeclarationSourceEnd(int position) {
	declarationEnd = position;
}
protected void setExplicitConstructorCall(String s) {
	explicitConstructorCall = s;
}
public String tabString(int tab) {
	/*slow code*/

	String s = "";
	for (int i = tab; i > 0; i--)
		s = s + "\t";
	return s;
}
public String toString() {
	return toString(0);
}
public String toString(int tab) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(tabString(tab));
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers).append(" ");
	}
	if (returnTypeName != null) {
		buffer.append(returnTypeName).append(" ");
	}
	buffer.append(selector).append("(");
	if (argumentTypeNames != null) {
		for (int i = 0, max = argumentTypeNames.length; i < max; i++) {
			buffer.append(argumentTypeNames[i]).append(" ").append(
				argumentNames[i]).append(
				", "); 
		}
	}
	buffer.append(") ");
	if (exceptionTypeNames != null) {
		buffer.append("throws ");
		for (int i = 0, max = exceptionTypeNames.length; i < max; i++) {
			buffer.append(exceptionTypeNames[i]).append(", ");
		}
	}
	if (explicitConstructorCall != null) {
		buffer.append("{\n").append(tabString(tab+1)).append(explicitConstructorCall).append(tabString(tab)).append("}");
	} else {
		buffer.append("{}");
	}
	return buffer.toString();
}
}
