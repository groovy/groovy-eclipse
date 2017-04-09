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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.*;

/**
 * Handle for an import declaration. Info object is a ImportDeclarationElementInfo.
 * @see IImportDeclaration
 */

public class ImportDeclaration extends SourceRefElement implements IImportDeclaration {

	protected String name;
	protected boolean isOnDemand;

/**
 * Constructs an ImportDeclaration in the given import container
 * with the given name.
 */
protected ImportDeclaration(ImportContainer parent, String name, boolean isOnDemand) {
	super(parent);
	this.name = name;
	this.isOnDemand = isOnDemand;
}
public boolean equals(Object o) {
	if (!(o instanceof ImportDeclaration)) return false;
	return super.equals(o);
}
public String getElementName() {
	if (this.isOnDemand)
		return this.name + ".*"; //$NON-NLS-1$
	return this.name;
}
public String getNameWithoutStar() {
	return this.name;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return IMPORT_DECLARATION;
}
/**
 * @see org.eclipse.jdt.core.IImportDeclaration#getFlags()
 */
public int getFlags() throws JavaModelException {
	ImportDeclarationElementInfo info = (ImportDeclarationElementInfo)getElementInfo();
	return info.getModifiers();
}
/**
 * @see JavaElement#getHandleMemento(StringBuffer)
 * For import declarations, the handle delimiter is associated to the import container already
 */
protected void getHandleMemento(StringBuffer buff) {
	((JavaElement)getParent()).getHandleMemento(buff);
	escapeMementoName(buff, getElementName());
	if (this.occurrenceCount > 1) {
		buff.append(JEM_COUNT);
		buff.append(this.occurrenceCount);
	}
}
/**
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	// For import declarations, the handle delimiter is associated to the import container already
	Assert.isTrue(false, "Should not be called"); //$NON-NLS-1$
	return 0;
}
public ISourceRange getNameRange() throws JavaModelException {
	ImportDeclarationElementInfo info = (ImportDeclarationElementInfo) getElementInfo();
	return info.getNameRange();
}
/*
 * @see JavaElement#getPrimaryElement(boolean)
 */
public IJavaElement getPrimaryElement(boolean checkOwner) {
	CompilationUnit cu = (CompilationUnit)this.parent.getParent();
	if (checkOwner && cu.isPrimary()) return this;
	return cu.getImport(getElementName());
}
/**
 * Returns true if the import is on-demand (ends with ".*")
 */
public boolean isOnDemand() {
	return this.isOnDemand;
}
/**
 */
public String readableName() {

	return null;
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
	buffer.append(tabString(tab));
	buffer.append("import "); //$NON-NLS-1$
	toStringName(buffer);
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
}
