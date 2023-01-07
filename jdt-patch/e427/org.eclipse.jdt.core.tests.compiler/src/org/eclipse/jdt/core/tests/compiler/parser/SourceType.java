/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

public final class SourceType {
	private int modifiers;
	private int declarationStart;
	private int declarationEnd;
	private char[] fileName;
	private SourcePackage packageName;
	private SourceImport[] imports;
	private char[] enclosingTypeName;
	private char[] name;
	private int nameSourceStart;
	private int nameSourceEnd;
	private char[] superclassName;
	private char[][] interfaceNames;
	private SourceType[] memberTypes;
	private int numberOfMemberTypes;
	private SourceMethod[] methods;
	private int numberOfMethods;
	private SourceField[] fields;
	private SourceField[] recordComponents;
	private int numberOfFields;
	private int numberOfComponents;
	private char[] source;
	SourceType parent;
	char[][] typeParameterNames;
	char[][][] typeParameterBounds;

	// Buffering.
	private char[] qualifiedName;
	private String defaultConstructor;
public SourceType(
	char[] enclosingTypeName,
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclassName,
	char[][] interfaceNames,
	char[] source) {

	this.enclosingTypeName = enclosingTypeName;
	this.declarationStart = declarationStart;

	this.modifiers = modifiers;
	this.name = name;
	this.nameSourceStart = nameSourceStart;
	this.nameSourceEnd = nameSourceEnd;
	this.superclassName = superclassName;
	this.interfaceNames = interfaceNames;
	this.source = source;
}
protected void addField(SourceField sourceField) {
	if (this.fields == null) {
		this.fields = new SourceField[4];
	}

	if (this.numberOfFields == this.fields.length) {
		System.arraycopy(
			this.fields,
			0,
			this.fields = new SourceField[this.numberOfFields * 2],
			0,
			this.numberOfFields);
	}
	this.fields[this.numberOfFields++] = sourceField;
}
protected void addRecordComponent(SourceField comp) {
	if (this.recordComponents == null) {
		this.recordComponents = new SourceField[4];
	}
	if (this.numberOfComponents == this.recordComponents.length) {
		System.arraycopy(
			this.recordComponents,
			0,
			this.recordComponents = new SourceField[this.numberOfComponents * 2],
			0,
			this.numberOfComponents);
	}
	this.recordComponents[this.numberOfComponents++] = comp;
}
protected void addMemberType(SourceType sourceMemberType) {
	if(this.memberTypes == null) {
		this.memberTypes = new SourceType[4];
	}

	if(this.numberOfMemberTypes == this.memberTypes.length) {
		System.arraycopy(this.memberTypes, 0, this.memberTypes = new SourceType[this.numberOfMemberTypes * 2], 0, this.numberOfMemberTypes);
	}
	this.memberTypes[this.numberOfMemberTypes++] = sourceMemberType;
}
protected void addMethod(SourceMethod sourceMethod) {
	if (this.methods == null) {
		this.methods = new SourceMethod[4];
	}

	if (this.numberOfMethods == this.methods.length) {
		System.arraycopy(
			this.methods,
			0,
			this.methods = new SourceMethod[this.numberOfMethods * 2],
			0,
			this.numberOfMethods);
	}
	this.methods[this.numberOfMethods++] = sourceMethod;
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
	return buffer.toString().trim();
}
public String getActualName() {
	StringBuilder buffer = new StringBuilder();
	buffer.append(this.source, this.nameSourceStart, this.nameSourceEnd - this.nameSourceStart + 1);
	return buffer.toString();
}
public int getDeclarationSourceEnd() {
	return this.declarationEnd;
}
public int getDeclarationSourceStart() {
	return this.declarationStart;
}
public char[] getEnclosingTypeName() {
	return this.enclosingTypeName;
}
public SourceField[] getFields() {
	if (this.fields != null && this.fields.length != this.numberOfFields) {
		System.arraycopy(this.fields, 0, this.fields = new SourceField[this.numberOfFields], 0, this.numberOfFields);
	}
	return this.fields;
}
public char[] getFileName() {
	return this.fileName;
}
public char[][] getImports() {
	if (this.imports == null) return null;
	int importLength = this.imports.length;
	char[][] importNames = new char[importLength][];
	for (int i = 0, max = importLength; i < max; i++) {
		importNames[i] = this.imports[i].name;
	}
	return importNames;
}
public char[][] getInterfaceNames() {
	return this.interfaceNames;
}
public SourceType[] getMemberTypes() {
	if (this.memberTypes != null && this.memberTypes.length != this.numberOfMemberTypes) {
		System.arraycopy(
			this.memberTypes,
			0,
			this.memberTypes = new SourceType[this.numberOfMemberTypes],
			0,
			this.numberOfMemberTypes);
	}
	return this.memberTypes;
}
public SourceMethod[] getMethods() {
	if (this.methods != null && this.methods.length != this.numberOfMethods) {
		System.arraycopy(this.methods, 0, this.methods = new SourceMethod[this.numberOfMethods], 0, this.numberOfMethods);
	}
	return this.methods;
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
public char[] getPackageName() {
	return this.packageName.name;
}
public char[] getQualifiedName() {
	if (this.qualifiedName == null) {
		StringBuilder temp = new StringBuilder();
		temp.append(this.packageName);
		temp.append('.');
		temp.append(this.name);
		this.qualifiedName = temp.toString().toCharArray();
	}
	return this.qualifiedName;
}
public char[] getSuperclassName() {
	return this.superclassName;
}
public boolean isBinaryType() {
	return false;
}
public boolean isClass() {
	return (this.modifiers & ClassFileConstants.AccInterface) == 0;
}
public boolean isInterface() {
	return (this.modifiers & ClassFileConstants.AccInterface) == ClassFileConstants.AccInterface;
}
public void setDeclarationSourceEnd(int position) {
	this.declarationEnd = position;
}
public void setDefaultConstructor(String s) {
	this.defaultConstructor = s;
}
public void setImports(SourceImport[] imports) {
	this.imports = imports;
}
public void setPackage(SourcePackage sourcePackage) {
	this.packageName = sourcePackage;
}
public void setSuperclass(char[] superclassName) {
	this.superclassName = superclassName;
}
public void setSuperinterfaces(char[][] superinterfacesNames) {
	this.interfaceNames = superinterfacesNames;
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
	if (this.packageName != null) {
		buffer.append(tabString(tab)).append(this.packageName);
	}
	if (this.imports != null) {
		for (int i = 0, max = this.imports.length; i < max; i++) {
			buffer.append(tabString(tab)).append(this.imports[i]);
		}
	}
	buffer.append(tabString(tab));
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers).append(" ");
	}
	buffer.append(isInterface() ? "interface " : "class ").append(this.name).append(" ");
	if (this.superclassName != null) {
		buffer.append("extends ").append(this.superclassName).append(" ");
	}
	if (this.interfaceNames != null) {
		buffer.append("implements ");
		for (int i = 0, max = this.interfaceNames.length; i < max; i++) {
			buffer.append(this.interfaceNames[i]).append(", ");
		}
	}
	buffer.append("{\n");
	if (this.memberTypes != null) {
		for (int i = 0, max = this.numberOfMemberTypes; i < max; i++) {
			buffer.append(this.memberTypes[i].toString(tab + 1)).append("\n");
		}
	}
	if (this.fields != null) {
		for (int i = 0, max = this.numberOfFields; i < max; i++) {
			buffer.append(this.fields[i].toString(tab + 1)).append("\n");
		}
	}
	if (this.defaultConstructor != null) {
			buffer.append(tabString(tab + 1)).append(this.defaultConstructor);
	}
	if (this.methods != null) {
		for (int i = 0, max = this.numberOfMethods; i < max; i++) {
			buffer.append(this.methods[i].toString(tab + 1)).append("\n");
		}
	}
	buffer.append(tabString(tab)).append("}");
	return buffer.toString();
}
}
