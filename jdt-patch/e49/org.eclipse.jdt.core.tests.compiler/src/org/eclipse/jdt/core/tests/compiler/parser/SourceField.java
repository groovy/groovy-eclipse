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
import org.eclipse.jdt.internal.compiler.env.ISourceField;

public class SourceField implements ISourceField {
	protected int modifiers;
	protected char[] typeName;
	protected char[] name;
	protected int declarationStart;
	protected int declarationEnd;
	protected int nameSourceStart;
	protected int nameSourceEnd;
	protected char[] source;
public SourceField(
	int declarationStart,
	int modifiers,
	char[] typeName,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] source) {

	this.declarationStart = declarationStart;
	this.modifiers = modifiers;
	this.typeName = typeName;
	this.name = name;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
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
	return buffer.toString();
}
public String getActualName() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(this.source, this.nameSourceStart, this.nameSourceEnd - this.nameSourceStart + 1);
	return buffer.toString();
}
public int getDeclarationSourceEnd() {
	return this.declarationEnd;
}
public int getDeclarationSourceStart() {
	return this.declarationStart;
}
public char[] getInitializationSource() {
	return null;
}
public int getModifiers() {
	return this.modifiers;
}
public char[] getName() {
	return this.name;
}
public int getNameSourceEnd() {
	return this.nameSourceEnd;
}
public int getNameSourceStart() {
	return this.nameSourceStart;
}
public char[] getTypeName() {
	return this.typeName;
}
protected void setDeclarationSourceEnd(int position) {
	this.declarationEnd = position;
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
		buffer.append(displayModifiers);
	}
	buffer.append(this.typeName).append(" ").append(this.name);
	buffer.append(";");
	return buffer.toString();
}
}
