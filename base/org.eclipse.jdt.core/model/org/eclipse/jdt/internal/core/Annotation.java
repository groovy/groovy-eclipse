/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;

public class Annotation extends SourceRefElement implements IAnnotation {

	public static final IAnnotation[] NO_ANNOTATIONS = new IAnnotation[0];
	public static final IMemberValuePair[] NO_MEMBER_VALUE_PAIRS = new IMemberValuePair[0];
	
	protected String name;
	
	public Annotation(JavaElement parent, String name) {
		super(parent);
		this.name = name;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Annotation)) return false;
		return super.equals(o);
	}

	public IMember getDeclaringMember() {
		return (IMember) getParent();
	}

	public String getElementName() {
		return this.name;
	}

	public int getElementType() {
		return ANNOTATION;
	}

	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_ANNOTATION;
	}
	
	public IMemberValuePair[] getMemberValuePairs() throws JavaModelException {
		AnnotationInfo info = (AnnotationInfo) getElementInfo();
		return info.members;
	}

	public ISourceRange getNameRange() throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			ClassFile classFile = (ClassFile)getClassFile();
			if (classFile != null) {
				// ensure the class file's buffer is open so that source ranges are computed
				classFile.getBuffer();
				return mapper.getNameRange(this);
			}
		}
		AnnotationInfo info = (AnnotationInfo) getElementInfo();
		return new SourceRange(info.nameStart, info.nameEnd - info.nameStart + 1);
	}

	/*
	 * @see ISourceReference
	 */
	public ISourceRange getSourceRange() throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			// ensure the class file's buffer is open so that source ranges are computed
			ClassFile classFile = (ClassFile)getClassFile();
			if (classFile != null) {
				classFile.getBuffer();
				return mapper.getSourceRange(this);
			}
		}
		return super.getSourceRange();
	}

	public IClassFile getClassFile() {
		return ((JavaElement)getParent()).getClassFile();
	}

	protected void toStringName(StringBuffer buffer) {
		buffer.append('@');
		buffer.append(getElementName());
	}
}
