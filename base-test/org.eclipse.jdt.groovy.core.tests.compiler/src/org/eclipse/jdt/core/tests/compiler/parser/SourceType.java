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
	private int numberOfFields;
	private char[] source;
	SourceType parent;

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
	if (fields == null) {
		fields = new SourceField[4];
	}

	if (numberOfFields == fields.length) {
		System.arraycopy(
			fields, 
			0, 
			fields = new SourceField[numberOfFields * 2], 
			0, 
			numberOfFields); 
	}
	fields[numberOfFields++] = sourceField;
}
protected void addMemberType(SourceType sourceMemberType) {
	if(memberTypes == null) {
		memberTypes = new SourceType[4];
	}

	if(numberOfMemberTypes == memberTypes.length) {
		System.arraycopy(memberTypes, 0, memberTypes = new SourceType[numberOfMemberTypes * 2], 0, numberOfMemberTypes);
	}
	memberTypes[numberOfMemberTypes++] = sourceMemberType;
}
protected void addMethod(SourceMethod sourceMethod) {
	if (methods == null) {
		methods = new SourceMethod[4];
	}

	if (numberOfMethods == methods.length) {
		System.arraycopy(
			methods, 
			0, 
			methods = new SourceMethod[numberOfMethods * 2], 
			0, 
			numberOfMethods); 
	}
	methods[numberOfMethods++] = sourceMethod;
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
	return buffer.toString().trim();
}
public String getActualName() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(source, nameSourceStart, nameSourceEnd - nameSourceStart + 1);
	return buffer.toString();
}
public int getDeclarationSourceEnd() {
	return declarationEnd;
}
public int getDeclarationSourceStart() {
	return declarationStart;
}
public char[] getEnclosingTypeName() {
	return enclosingTypeName;
}
public SourceField[] getFields() {
	if (fields != null && fields.length != numberOfFields) {
		System.arraycopy(fields, 0, fields = new SourceField[numberOfFields], 0, numberOfFields);
	}
	return fields;
}
public char[] getFileName() {
	return fileName;
}
public char[][] getImports() {
	if (imports == null) return null;
	int importLength = imports.length;
	char[][] importNames = new char[importLength][];
	for (int i = 0, max = importLength; i < max; i++) {
		importNames[i] = imports[i].name;
	}
	return importNames;
}
public char[][] getInterfaceNames() {
	return interfaceNames;
}
public SourceType[] getMemberTypes() {
	if (memberTypes != null && memberTypes.length != numberOfMemberTypes) {
		System.arraycopy(
			memberTypes, 
			0, 
			memberTypes = new SourceType[numberOfMemberTypes], 
			0, 
			numberOfMemberTypes); 
	}
	return memberTypes;
}
public SourceMethod[] getMethods() {
	if (methods != null && methods.length != numberOfMethods) {
		System.arraycopy(methods, 0, methods = new SourceMethod[numberOfMethods], 0, numberOfMethods);
	}
	return methods;
}
public int getModifiers() {
	return modifiers;
}
public char[] getName() {
	return name;
}
public int getNameSourceEnd() {
	return nameSourceEnd;
}
public int getNameSourceStart() {
	return nameSourceStart;
}
public char[] getPackageName() {
	return packageName.name;
}
public char[] getQualifiedName() {
	if (qualifiedName == null) {
		StringBuffer temp = new StringBuffer();
		temp.append(packageName);
		temp.append('.');
		temp.append(name);
		qualifiedName = temp.toString().toCharArray();
	}
	return qualifiedName;
}
public char[] getSuperclassName() {
	return superclassName;
}
public boolean isBinaryType() {
	return false;
}
public boolean isClass() {
	return (modifiers & ClassFileConstants.AccInterface) == 0;
}
public boolean isInterface() {
	return (modifiers & ClassFileConstants.AccInterface) == ClassFileConstants.AccInterface;
}
public void setDeclarationSourceEnd(int position) {
	declarationEnd = position;
}
public void setDefaultConstructor(String s) {
	this.defaultConstructor = s;
}
public void setImports(SourceImport[] imports) {
	this.imports = imports;
}
public void setPackage(SourcePackage sourcePackage) {
	packageName = sourcePackage;
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
public String toString() {
	return toString(0);
}
public String toString(int tab) {

	StringBuffer buffer = new StringBuffer();
	if (packageName != null) {
		buffer.append(tabString(tab)).append(packageName);
	}
	if (imports != null) {
		for (int i = 0, max = imports.length; i < max; i++) {
			buffer.append(tabString(tab)).append(imports[i]);
		}
	}
	buffer.append(tabString(tab));
	String displayModifiers = displayModifiers();
	if (displayModifiers != null) {
		buffer.append(displayModifiers).append(" ");
	}
	buffer.append(isInterface() ? "interface " : "class ").append(name).append(" ");
	if (superclassName != null) {
		buffer.append("extends ").append(superclassName).append(" ");
	}
	if (interfaceNames != null) {
		buffer.append("implements ");
		for (int i = 0, max = interfaceNames.length; i < max; i++) {
			buffer.append(interfaceNames[i]).append(", ");
		}
	}
	buffer.append("{\n");
	if (memberTypes != null) {
		for (int i = 0, max = numberOfMemberTypes; i < max; i++) {
			buffer.append(memberTypes[i].toString(tab + 1)).append("\n");
		}
	}
	if (fields != null) {
		for (int i = 0, max = numberOfFields; i < max; i++) {
			buffer.append(fields[i].toString(tab + 1)).append("\n");
		}
	}
	if (defaultConstructor != null) {
			buffer.append(tabString(tab + 1)).append(defaultConstructor);		
	}
	if (methods != null) {
		for (int i = 0, max = numberOfMethods; i < max; i++) {
			buffer.append(methods[i].toString(tab + 1)).append("\n");
		}
	}
	buffer.append(tabString(tab)).append("}");
	return buffer.toString();
}
}
