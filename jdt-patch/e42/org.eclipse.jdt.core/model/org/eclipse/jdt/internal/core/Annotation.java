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

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.core.util.Util;

public class Annotation extends SourceRefElement implements IAnnotation {

	public static final IAnnotation[] NO_ANNOTATIONS = new IAnnotation[0];
	public static final IMemberValuePair[] NO_MEMBER_VALUE_PAIRS = new IMemberValuePair[0];

	protected String name;
	// require to distinguish same annotations in different member value pairs
	protected String memberValuePairName;

	public Annotation(JavaElement parent, String name) {
		this(parent, name, null);
	}

	public Annotation(JavaElement parent, String name, String memberValuePairName) {
		super(parent);
		this.name = name;
		this.memberValuePairName = memberValuePairName;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Annotation)) {
			return false;
		}
		Annotation other = (Annotation) o;
		if (this.memberValuePairName == null) {
			if (other.memberValuePairName != null)
				return false;
		} else if (!this.memberValuePairName.equals(other.memberValuePairName)) {
			return false;
		}
		// name equality is checked as part of the super.equals(..)
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
		Object info = getElementInfo();
		if (info instanceof AnnotationInfo)
			return ((AnnotationInfo) info).members;
		IBinaryElementValuePair[] binaryAnnotations = ((IBinaryAnnotation) info).getElementValuePairs();
		int length = binaryAnnotations.length;
		IMemberValuePair[] result = new IMemberValuePair[length];
		for (int i = 0; i < length; i++) {
			IBinaryElementValuePair binaryAnnotation = binaryAnnotations[i];
			MemberValuePair memberValuePair = new MemberValuePair(new String(binaryAnnotation.getName()));
			memberValuePair.value = Util.getAnnotationMemberValue(this, memberValuePair, binaryAnnotation.getValue());
			result[i] = memberValuePair;
		}
		return result;
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
		Object info = getElementInfo();
		if (info instanceof AnnotationInfo) {
			AnnotationInfo annotationInfo = (AnnotationInfo) info;
			return new SourceRange(annotationInfo.nameStart, annotationInfo.nameEnd - annotationInfo.nameStart + 1);
		}
		return null;
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

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.memberValuePairName == null) ? 0 : this.memberValuePairName.hashCode());
		result = prime * result + this.name.hashCode();
		return result;
	}

	protected void toStringName(StringBuffer buffer) {
		buffer.append('@');
		buffer.append(getElementName());
	}
}
