/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceManipulation;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @see IInitializer
 */

public class Initializer extends Member implements IInitializer {

protected Initializer(JavaElement parent, int occurrenceCount) {
	super(parent, occurrenceCount);
}
@Override
public boolean equals(Object o) {
	if (!(o instanceof Initializer)) return false;
	return super.equals(o);
}

/**
 * @see IJavaElement
 */
@Override
public int getElementType() {
	return INITIALIZER;
}
/**
 * @see JavaElement#getHandleMemento(StringBuilder)
 */
@Override
protected void getHandleMemento(StringBuilder buff) {
	getParent().getHandleMemento(buff);
	buff.append(getHandleMementoDelimiter());
	buff.append(this.getOccurrenceCount());
}
/**
 * @see JavaElement#getHandleMemento()
 */
@Override
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_INITIALIZER;
}
@Override
public String readableName() {

	return ((JavaElement)getDeclaringType()).readableName();
}
/**
 * @see ISourceManipulation
 */
@Override
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, this));
}
/**
 * @see IMember
 */
@Override
public ISourceRange getNameRange() {
	return null;
}

@Override
public JavaElement getPrimaryElement(boolean checkOwner) {
	if (checkOwner) {
		CompilationUnit cu = (CompilationUnit)getAncestor(COMPILATION_UNIT);
		if (cu == null || cu.isPrimary()) return this;
	}
	IJavaElement primaryParent = this.getParent().getPrimaryElement(false);
	return (JavaElement) ((IType) primaryParent).getInitializer(this.getOccurrenceCount());
}
/**
 * for debugging only
 */
@Override
protected void toStringInfo(int tab, StringBuilder buffer, Object info, boolean showResolvedInfo) {
	buffer.append(tabString(tab));
	if (info == null) {
		buffer.append("<initializer #"); //$NON-NLS-1$
		buffer.append(this.getOccurrenceCount());
		buffer.append("> (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		buffer.append("<initializer #"); //$NON-NLS-1$
		buffer.append(this.getOccurrenceCount());
		buffer.append(">"); //$NON-NLS-1$
	} else {
		try {
			buffer.append("<"); //$NON-NLS-1$
			if (Flags.isStatic(getFlags())) {
				buffer.append("static "); //$NON-NLS-1$
			}
		buffer.append("initializer #"); //$NON-NLS-1$
		buffer.append(this.getOccurrenceCount());
		buffer.append(">"); //$NON-NLS-1$
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
}
