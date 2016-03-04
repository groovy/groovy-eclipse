/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.*;

/**
 * @see IPackageDeclaration
 */

public class PackageDeclaration extends SourceRefElement implements IPackageDeclaration {

	String name;

protected PackageDeclaration(CompilationUnit parent, String name) {
	super(parent);
	this.name = name;
}
public boolean equals(Object o) {
	if (!(o instanceof PackageDeclaration)) return false;
	return super.equals(o);
}
public String getElementName() {
	return this.name;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return PACKAGE_DECLARATION;
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_PACKAGEDECLARATION;
}
/**
 * @see IPackageDeclaration#getNameRange()
 */
public ISourceRange getNameRange() throws JavaModelException {
	AnnotatableInfo info = (AnnotatableInfo) getElementInfo();
	return info.getNameRange();
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
	if (checkOwner && cu.isPrimary()) return this;
	return cu.getPackageDeclaration(this.name);
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
	buffer.append(tabString(tab));
	buffer.append("package "); //$NON-NLS-1$
	toStringName(buffer);
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
}
