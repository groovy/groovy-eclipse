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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * @see IField
 */

/* package */ class BinaryField extends BinaryMember implements IField {

/*
 * Constructs a handle to the field with the given name in the specified type.
 */
protected BinaryField(JavaElement parent, String name) {
	super(parent, name);
}
public boolean equals(Object o) {
	if (!(o instanceof BinaryField)) return false;
	return super.equals(o);
}
public IAnnotation[] getAnnotations() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	IBinaryAnnotation[] binaryAnnotations = info.getAnnotations();
	return getAnnotations(binaryAnnotations, info.getTagBits());
}
/*
 * @see IField
 */
public Object getConstant() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return convertConstant(info.getConstant());
}
/*
 * @see IMember
 */
public int getFlags() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	return info.getModifiers();
}
/*
 * @see IJavaElement
 */
public int getElementType() {
	return FIELD;
}
/*
 * @see JavaElement#getHandleMemento()
 */
protected char getHandleMementoDelimiter() {
	return JavaElement.JEM_FIELD;
}
public String getKey(boolean forceOpen) throws JavaModelException {
	return getKey(this, forceOpen);
}
/*
 * @see IField
 */
public String getTypeSignature() throws JavaModelException {
	IBinaryField info = (IBinaryField) getElementInfo();
	char[] genericSignature = info.getGenericSignature();
	if (genericSignature != null) {
		return new String(ClassFile.translatedName(genericSignature));
	}
	return new String(ClassFile.translatedName(info.getTypeName()));
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.IField#isEnumConstant()
 */public boolean isEnumConstant() throws JavaModelException {
	return Flags.isEnum(getFlags());
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.IField#isResolved()
 */
public boolean isResolved() {
	return false;
}
public JavaElement resolved(Binding binding) {
	SourceRefElement resolvedHandle = new ResolvedBinaryField(this.parent, this.name, new String(binding.computeUniqueKey()));
	resolvedHandle.occurrenceCount = this.occurrenceCount;
	return resolvedHandle;
}
/*
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info, boolean showResolvedInfo) {
	buffer.append(tabString(tab));
	if (info == null) {
		toStringName(buffer);
		buffer.append(" (not open)"); //$NON-NLS-1$
	} else if (info == NO_INFO) {
		toStringName(buffer);
	} else {
		try {
			buffer.append(Signature.toString(getTypeSignature()));
			buffer.append(" "); //$NON-NLS-1$
			toStringName(buffer);
		} catch (JavaModelException e) {
			buffer.append("<JavaModelException in toString of " + getElementName()); //$NON-NLS-1$
		}
	}
}
public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
	JavadocContents javadocContents = ((BinaryType) this.getDeclaringType()).getJavadocContents(monitor);
	if (javadocContents == null) return null;
	return javadocContents.getFieldDoc(this);
}
}
