/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;

/**
 * Describes one entry in the classes table of the InnerClasses attribute.
 * See the inner class specification (The class file attribute "InnerClasses").
 */

public class InnerClassInfo extends ClassFileStruct implements IBinaryNestedType {
	int innerClassNameIndex = -1;
	int outerClassNameIndex = -1;
	int innerNameIndex = -1;
	private char[] innerClassName;
	private char[] outerClassName;
	private char[] innerName;
	private int accessFlags = -1;
	private boolean readInnerClassName = false;
	private boolean readOuterClassName = false;
	private boolean readInnerName = false;

public InnerClassInfo(byte classFileBytes[], int offsets[], int offset) {
	super(classFileBytes, offsets, offset);
	this.innerClassNameIndex = u2At(0);
	this.outerClassNameIndex = u2At(2);
	this.innerNameIndex = u2At(4);
}
/**
 * Answer the resolved name of the enclosing type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[]
 */
public char[] getEnclosingTypeName() {
	if (!readOuterClassName) {
		// read outer class name
		readOuterClassName = true;
		if (outerClassNameIndex != 0) {
			int utf8Offset = 
				constantPoolOffsets[u2At(
					constantPoolOffsets[outerClassNameIndex] - structOffset + 1)]
					- structOffset; 
			outerClassName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		}

	}
	return outerClassName;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * @return int
 */
public int getModifiers() {
	if (accessFlags == -1) {
		// read access flag
		accessFlags = u2At(6);
	}
	return accessFlags;
}
/**
 * Answer the resolved name of the member type in the
 * class file format as specified in section 4.2 of the Java 2 VM spec.
 *
 * For example, p1.p2.A.M is p1/p2/A$M.
 * @return char[]
 */
public char[] getName() {
	if (!readInnerClassName) {
		// read the inner class name
		readInnerClassName = true;
		if (innerClassNameIndex != 0) {
			int  classOffset = constantPoolOffsets[innerClassNameIndex] - structOffset;
			int utf8Offset = constantPoolOffsets[u2At(classOffset + 1)] - structOffset;
			innerClassName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		}
	}
	return innerClassName;
}
/**
 * Answer the source name of the member type.
 *
 * For example, p1.p2.A.M is M.
 * @return char[]
 */
public char[] getSourceName() {
	if (!this.readInnerName) {
		this.readInnerName = true;
		if (innerNameIndex != 0) {
			int utf8Offset = constantPoolOffsets[innerNameIndex] - structOffset;
			innerName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		}
	}
	return innerName;
}
/**
 * Answer the string representation of the receiver
 * @return java.lang.String
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	if (getName() != null) {
		buffer.append(getName());
	}
	buffer.append("\n"); //$NON-NLS-1$
	if (getEnclosingTypeName() != null) {
		buffer.append(getEnclosingTypeName());
	}
	buffer.append("\n"); //$NON-NLS-1$
	if (getSourceName() != null) {
		buffer.append(getSourceName());
	}
	return buffer.toString();   
}
/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
void initialize() {
	getModifiers();
	getName();
	getSourceName();
	getEnclosingTypeName();
	reset();
}
}
