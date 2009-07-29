/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Common functionality for Binary member handles.
 */
public abstract class BinaryMember extends NamedMember {
/*
 * Constructs a binary member.
 */
protected BinaryMember(JavaElement parent, String name) {
	super(parent, name);
}
/*
 * @see ISourceManipulation
 */
public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
protected IAnnotation[] getAnnotations(IBinaryAnnotation[] binaryAnnotations) {
	if (binaryAnnotations == null) 
		return Annotation.NO_ANNOTATIONS;
	int length = binaryAnnotations.length;
	IAnnotation[] annotations = new IAnnotation[length];
	for (int i = 0; i < length; i++) {
		annotations[i] = getAnnotation(binaryAnnotations[i]);
	}
	return annotations;
}
private IAnnotation getAnnotation(IBinaryAnnotation binaryAnnotation) {
	IBinaryElementValuePair[] binaryElementValuePairs = binaryAnnotation.getElementValuePairs();
	int pairsLength = binaryElementValuePairs.length;
	final IMemberValuePair[] members;
	if (pairsLength == 0) {
		members = Annotation.NO_MEMBER_VALUE_PAIRS;
	} else {
		members = new IMemberValuePair[pairsLength];
		for (int i = 0; i < pairsLength; i++) {
			IBinaryElementValuePair binaryElementValuePair = binaryElementValuePairs[i];
			MemberValuePair memberValuePair = new MemberValuePair(new String(binaryElementValuePair.getName()));
			memberValuePair.value = getMemberValue(memberValuePair, binaryElementValuePair.getValue());
			members[i] = memberValuePair;
		}
	}
	char[] typeName = org.eclipse.jdt.core.Signature.toCharArray(CharOperation.replaceOnCopy(binaryAnnotation.getTypeName(), '/', '.'));
	return new Annotation(this, new String(typeName)) {
		public IMemberValuePair[] getMemberValuePairs() throws JavaModelException {
			return members;
		}
	};
}
protected Object getMemberValue(MemberValuePair memberValuePair, Object binaryValue) {
	if (binaryValue instanceof Constant) {
		return Util.getAnnotationMemberValue(memberValuePair, (Constant) binaryValue);
	} else if (binaryValue instanceof IBinaryAnnotation) {
		memberValuePair.valueKind = IMemberValuePair.K_ANNOTATION;
		return getAnnotation((IBinaryAnnotation) binaryValue);
	} else if (binaryValue instanceof ClassSignature) {
		memberValuePair.valueKind = IMemberValuePair.K_CLASS;
		char[] className = Signature.toCharArray(CharOperation.replaceOnCopy(((ClassSignature) binaryValue).getTypeName(), '/', '.'));
		return new String(className);
	} else if (binaryValue instanceof EnumConstantSignature) {
		memberValuePair.valueKind = IMemberValuePair.K_QUALIFIED_NAME;
		EnumConstantSignature enumConstant = (EnumConstantSignature) binaryValue;
		char[] enumName = Signature.toCharArray(CharOperation.replaceOnCopy(enumConstant.getTypeName(), '/', '.'));
		char[] qualifiedName = CharOperation.concat(enumName, enumConstant.getEnumConstantName(), '.');
		return new String(qualifiedName);
	} else if (binaryValue instanceof Object[]) {
		memberValuePair.valueKind = -1; // modified below by the first call to getMemberValue(...)
		Object[] binaryValues = (Object[]) binaryValue;
		int length = binaryValues.length;
		Object[] values = new Object[length];
		for (int i = 0; i < length; i++) {
			int previousValueKind = memberValuePair.valueKind;
			Object value = getMemberValue(memberValuePair, binaryValues[i]);
			if (previousValueKind != -1 && memberValuePair.valueKind != previousValueKind) {
				// values are heterogeneous, value kind is thus unknown
				memberValuePair.valueKind = IMemberValuePair.K_UNKNOWN;
			}
			values[i] = value;
		}
		if (memberValuePair.valueKind == -1)
			memberValuePair.valueKind = IMemberValuePair.K_UNKNOWN;
		return values;
	} else {
		memberValuePair.valueKind = IMemberValuePair.K_UNKNOWN;
		return null;
	}
}
public String[] getCategories() throws JavaModelException {
	SourceMapper mapper= getSourceMapper();
	if (mapper != null) {
		// ensure the class file's buffer is open so that categories are computed
		((ClassFile)getClassFile()).getBuffer();
		
		if (mapper.categories != null) {
			String[] categories = (String[]) mapper.categories.get(this);
			if (categories != null)
				return categories;
		}
	}
	return CharOperation.NO_STRINGS;	
}
public String getKey() {
	try {
		return getKey(false/*don't open*/);
	} catch (JavaModelException e) {
		// happen only if force open is true
		return null;
	}
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#computeUniqueKey()
 */
public abstract String getKey(boolean forceOpen) throws JavaModelException;
/*
 * @see ISourceReference
 */
public ISourceRange getNameRange() throws JavaModelException {
	SourceMapper mapper= getSourceMapper();
	if (mapper != null) {
		// ensure the class file's buffer is open so that source ranges are computed
		((ClassFile)getClassFile()).getBuffer();
		
		return mapper.getNameRange(this);
	} else {
		return SourceMapper.UNKNOWN_RANGE;
	}
}
/*
 * @see ISourceReference
 */
public ISourceRange getSourceRange() throws JavaModelException {
	SourceMapper mapper= getSourceMapper();
	if (mapper != null) {
		// ensure the class file's buffer is open so that source ranges are computed
		((ClassFile)getClassFile()).getBuffer();

		return mapper.getSourceRange(this);
	} else {
		return SourceMapper.UNKNOWN_RANGE;
	}
}
/*
 * @see IMember
 */
public boolean isBinary() {
	return true;
}
/*
 * @see IJavaElement
 */
public boolean isStructureKnown() throws JavaModelException {
	return ((IJavaElement)getOpenableParent()).isStructureKnown();
}
/*
 * @see ISourceManipulation
 */
public void move(IJavaElement container, IJavaElement sibling, String rename, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/*
 * @see ISourceManipulation
 */
public void rename(String newName, boolean force, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
/*
 * Sets the contents of this element.
 * Throws an exception as this element is read only.
 */
public void setContents(String contents, IProgressMonitor monitor) throws JavaModelException {
	throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.READ_ONLY, this));
}
}
