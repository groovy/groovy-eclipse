/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

/**
 * Element info for <code>ClassFile</code> handles.
 */

/* package */ class ClassFileInfo extends OpenableElementInfo implements SuffixConstants {
	/**
	 * The children of the <code>BinaryType</code> corresponding to our
	 * <code>ClassFile</code>. These are kept here because we don't have
	 * access to the <code>BinaryType</code> info (<code>ClassFileReader</code>).
	 */
	protected JavaElement[] binaryChildren = null;
	/*
	 * The type parameters in this class file.
	 */
	protected ITypeParameter[] typeParameters;

private void generateAnnotationsInfos(JavaElement member, IBinaryAnnotation[] binaryAnnotations, long tagBits, HashMap newElements) {
	generateAnnotationsInfos(member, null, binaryAnnotations, tagBits, newElements);
}
/**
 * Creates the handles and infos for the annotations of the given binary member.
 * Adds new handles to the given vector.
 */
private void generateAnnotationsInfos(JavaElement member, char[] parameterName, IBinaryAnnotation[] binaryAnnotations, long tagBits, HashMap newElements) {
	if (binaryAnnotations != null) {
		for (int i = 0, length = binaryAnnotations.length; i < length; i++) {
			IBinaryAnnotation annotationInfo = binaryAnnotations[i];
			generateAnnotationInfo(member, parameterName, newElements, annotationInfo, null);
		}
	}
	generateStandardAnnotationsInfos(member, parameterName, tagBits, newElements);
}
private void generateAnnotationInfo(JavaElement parent, HashMap newElements, IBinaryAnnotation annotationInfo, String memberValuePairName) {
	generateAnnotationInfo(parent, null, newElements, annotationInfo, memberValuePairName);
}
private void generateAnnotationInfo(JavaElement parent, char[] parameterName, HashMap newElements, IBinaryAnnotation annotationInfo, String memberValuePairName) {
	char[] typeName = org.eclipse.jdt.core.Signature.toCharArray(CharOperation.replaceOnCopy(annotationInfo.getTypeName(), '/', '.'));
	Annotation annotation = new Annotation(parent, new String(typeName), memberValuePairName);
	while (newElements.containsKey(annotation)) {
		annotation.occurrenceCount++;
	}
	newElements.put(annotation, annotationInfo);
	IBinaryElementValuePair[] pairs = annotationInfo.getElementValuePairs();
	for (int i = 0, length = pairs.length; i < length; i++) {
		Object value = pairs[i].getValue();
		if (value instanceof IBinaryAnnotation) {
			generateAnnotationInfo(annotation, newElements, (IBinaryAnnotation) value, new String(pairs[i].getName()));
		} else if (value instanceof Object[]) {
			// if the value is an array, it can have no more than 1 dimension - no need to recurse
			Object[] valueArray = (Object[]) value;
			for (int j = 0, valueArrayLength = valueArray.length; j < valueArrayLength; j++) {
				Object nestedValue = valueArray[j];
				if (nestedValue instanceof IBinaryAnnotation) {
					generateAnnotationInfo(annotation, newElements, (IBinaryAnnotation) nestedValue, new String(pairs[i].getName()));
				}
			}
		}
	}
}
private void generateStandardAnnotationsInfos(JavaElement javaElement, char[] parameterName, long tagBits, HashMap newElements) {
	if ((tagBits & TagBits.AllStandardAnnotationsMask) == 0)
		return;
	if ((tagBits & TagBits.AnnotationTargetMASK) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_ANNOTATION_TARGET, getTargetElementTypes(tagBits), newElements);
	}
	if ((tagBits & TagBits.AnnotationRetentionMASK) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_ANNOTATION_RETENTION, getRetentionPolicy(tagBits), newElements);
	}
	if ((tagBits & TagBits.AnnotationDeprecated) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_DEPRECATED, Annotation.NO_MEMBER_VALUE_PAIRS, newElements);
	}
	if ((tagBits & TagBits.AnnotationDocumented) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_ANNOTATION_DOCUMENTED, Annotation.NO_MEMBER_VALUE_PAIRS, newElements);
	}
	if ((tagBits & TagBits.AnnotationInherited) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_ANNOTATION_INHERITED, Annotation.NO_MEMBER_VALUE_PAIRS, newElements);
	}
	if ((tagBits & TagBits.AnnotationPolymorphicSignature) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_INVOKE_METHODHANDLE_$_POLYMORPHICSIGNATURE, Annotation.NO_MEMBER_VALUE_PAIRS, newElements);
	}
	if ((tagBits & TagBits.AnnotationSafeVarargs) != 0) {
		generateStandardAnnotation(javaElement, TypeConstants.JAVA_LANG_SAFEVARARGS, Annotation.NO_MEMBER_VALUE_PAIRS, newElements);
	}
	// note that JAVA_LANG_SUPPRESSWARNINGS and JAVA_LANG_OVERRIDE cannot appear in binaries
}

private void generateStandardAnnotation(JavaElement javaElement, char[][] typeName, IMemberValuePair[] members, HashMap newElements) {
	IAnnotation annotation = new Annotation(javaElement, new String(CharOperation.concatWith(typeName, '.')));
	AnnotationInfo annotationInfo = new AnnotationInfo();
	annotationInfo.members = members;
	newElements.put(annotation, annotationInfo);
}

private IMemberValuePair[] getTargetElementTypes(long tagBits) {
	ArrayList values = new ArrayList();
	String elementType = new String(CharOperation.concatWith(TypeConstants.JAVA_LANG_ANNOTATION_ELEMENTTYPE, '.')) + '.';
	if ((tagBits & TagBits.AnnotationForType) != 0) {
		values.add(elementType + new String(TypeConstants.TYPE));
	}
	if ((tagBits & TagBits.AnnotationForField) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_FIELD));
	}
	if ((tagBits & TagBits.AnnotationForMethod) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_METHOD));
	}
	if ((tagBits & TagBits.AnnotationForParameter) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_PARAMETER));
	}
	if ((tagBits & TagBits.AnnotationForConstructor) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_CONSTRUCTOR));
	}
	if ((tagBits & TagBits.AnnotationForLocalVariable) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_LOCAL_VARIABLE));
	}
	if ((tagBits & TagBits.AnnotationForAnnotationType) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_ANNOTATION_TYPE));
	}
	if ((tagBits & TagBits.AnnotationForPackage) != 0) {
		values.add(elementType + new String(TypeConstants.UPPER_PACKAGE));
	}
	final Object value;
	if (values.size() == 0) {
		if ((tagBits & TagBits.AnnotationTarget) != 0)
			value = CharOperation.NO_STRINGS;
		else
			return Annotation.NO_MEMBER_VALUE_PAIRS;
	} else if (values.size() == 1) {
		value = values.get(0);
	} else {
		value = values.toArray(new String[values.size()]);
	}
	return new IMemberValuePair[] {
		new IMemberValuePair() {
			public int getValueKind() {
				return IMemberValuePair.K_QUALIFIED_NAME;
			}
			public Object getValue() {
				return value;
			}
			public String getMemberName() {
				return new String(TypeConstants.VALUE);
			}
		}
	};
}

private IMemberValuePair[] getRetentionPolicy(long tagBits) {
	if ((tagBits & TagBits.AnnotationRetentionMASK) == 0)
		return Annotation.NO_MEMBER_VALUE_PAIRS;
	String retention = null;
	if ((tagBits & TagBits.AnnotationRuntimeRetention) == TagBits.AnnotationRuntimeRetention) {
		// TagBits.AnnotationRuntimeRetention combines both TagBits.AnnotationClassRetention & TagBits.AnnotationSourceRetention
		retention = new String(CharOperation.concatWith(TypeConstants.JAVA_LANG_ANNOTATION_RETENTIONPOLICY, '.')) + '.' + new String(TypeConstants.UPPER_RUNTIME);
	} else if ((tagBits & TagBits.AnnotationSourceRetention) != 0) {
		retention = new String(CharOperation.concatWith(TypeConstants.JAVA_LANG_ANNOTATION_RETENTIONPOLICY, '.')) + '.' + new String(TypeConstants.UPPER_SOURCE);
	} else {
		retention = new String(CharOperation.concatWith(TypeConstants.JAVA_LANG_ANNOTATION_RETENTIONPOLICY, '.')) + '.' + new String(TypeConstants.UPPER_CLASS);
	}
	final String value = retention;
	return 
		new IMemberValuePair[] {
			new IMemberValuePair() {
				public int getValueKind() {
					return IMemberValuePair.K_QUALIFIED_NAME;
				}
				public Object getValue() {
					return value;
				}
				public String getMemberName() {
					return new String(TypeConstants.VALUE);
				}
			}
		};
}

/**
 * Creates the handles and infos for the fields of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateFieldInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList childrenHandles) {
	// Make the fields
	IBinaryField[] fields = typeInfo.getFields();
	if (fields == null) {
		return;
	}
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	for (int i = 0, fieldCount = fields.length; i < fieldCount; i++) {
		IBinaryField fieldInfo = fields[i];
		BinaryField field = new BinaryField((JavaElement)type, manager.intern(new String(fieldInfo.getName())));
		newElements.put(field, fieldInfo);
		childrenHandles.add(field);
		generateAnnotationsInfos(field, fieldInfo.getAnnotations(), fieldInfo.getTagBits(), newElements);
	}
}
/**
 * Creates the handles for the inner types of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateInnerClassHandles(IType type, IBinaryType typeInfo, ArrayList childrenHandles) {
	// Add inner types
	// If the current type is an inner type, innerClasses returns
	// an extra entry for the current type.  This entry must be removed.
	// Can also return an entry for the enclosing type of an inner type.
	IBinaryNestedType[] innerTypes = typeInfo.getMemberTypes();
	if (innerTypes != null) {
		IPackageFragment pkg = (IPackageFragment) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		for (int i = 0, typeCount = innerTypes.length; i < typeCount; i++) {
			IBinaryNestedType binaryType = innerTypes[i];
			IClassFile parentClassFile= pkg.getClassFile(new String(ClassFile.unqualifiedName(binaryType.getName())) + SUFFIX_STRING_class);
			IType innerType = new BinaryType((JavaElement) parentClassFile, ClassFile.simpleName(binaryType.getName()));
			childrenHandles.add(innerType);
		}
	}
}
/**
 * Creates the handles and infos for the methods of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateMethodInfos(IType type, IBinaryType typeInfo, HashMap newElements, ArrayList childrenHandles, ArrayList typeParameterHandles) {
	IBinaryMethod[] methods = typeInfo.getMethods();
	if (methods == null) {
		return;
	}
	for (int i = 0, methodCount = methods.length; i < methodCount; i++) {
		IBinaryMethod methodInfo = methods[i];
		// TODO (jerome) filter out synthetic members
		//                        indexer should not index them as well
		// if ((methodInfo.getModifiers() & IConstants.AccSynthetic) != 0) continue; // skip synthetic
		boolean useGenericSignature = true;
		char[] signature = methodInfo.getGenericSignature();
		if (signature == null) {
			useGenericSignature = false;
			signature = methodInfo.getMethodDescriptor();
		}
		String selector = new String(methodInfo.getSelector());
		final boolean isConstructor = methodInfo.isConstructor();
		if (isConstructor) {
			selector = type.getElementName();
		}
		String[] pNames = null;
		try {
			pNames = Signature.getParameterTypes(new String(signature));
			if (isConstructor
					&& useGenericSignature
					&& type.isMember()
					&& !Flags.isStatic(type.getFlags())) {
				int length = pNames.length;
				System.arraycopy(pNames, 0, (pNames = new String[length + 1]), 1, length);
				char[] descriptor = methodInfo.getMethodDescriptor();
				final String[] parameterTypes = Signature.getParameterTypes(new String(descriptor));
				pNames[0] = parameterTypes[0];
			}
		} catch (IllegalArgumentException e) {
			// protect against malformed .class file (e.g. com/sun/crypto/provider/SunJCE_b.class has a 'a' generic signature)
			signature = methodInfo.getMethodDescriptor();
			pNames = Signature.getParameterTypes(new String(signature));
		} catch (JavaModelException e) {
			// protect against malformed .class file (e.g. com/sun/crypto/provider/SunJCE_b.class has a 'a' generic signature)
			signature = methodInfo.getMethodDescriptor();
			pNames = Signature.getParameterTypes(new String(signature));
		}
		char[][] paramNames= new char[pNames.length][];
		for (int j= 0; j < pNames.length; j++) {
			paramNames[j]= pNames[j].toCharArray();
		}
		char[][] parameterTypes = ClassFile.translatedNames(paramNames);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		selector =  manager.intern(selector);
		for (int j= 0; j < pNames.length; j++) {
			pNames[j]= manager.intern(new String(parameterTypes[j]));
		}
		BinaryMethod method = new BinaryMethod((JavaElement)type, selector, pNames);
		childrenHandles.add(method);

		// ensure that 2 binary methods with the same signature but with different return types have different occurrence counts.
		// (case of bridge methods in 1.5)
		while (newElements.containsKey(method))
			method.occurrenceCount++;

		newElements.put(method, methodInfo);

		int max = pNames.length;
		char[][] argumentNames = methodInfo.getArgumentNames();
		if (argumentNames == null || argumentNames.length < max) {
			argumentNames = new char[max][];
			for (int j = 0; j < max; j++) {
				argumentNames[j] = ("arg" + j).toCharArray(); //$NON-NLS-1$
			}
		}
		for (int j = 0; j < max; j++) {
			IBinaryAnnotation[] parameterAnnotations = methodInfo.getParameterAnnotations(j);
			if (parameterAnnotations != null) {
				LocalVariable localVariable = new LocalVariable(
						method,
						new String(argumentNames[j]),
						0,
						-1,
						0,
						-1,
						method.parameterTypes[j],
						null,
						-1,
						true);
				generateAnnotationsInfos(localVariable, argumentNames[j], parameterAnnotations, methodInfo.getTagBits(), newElements);
			}
		}
		generateTypeParameterInfos(method, signature, newElements, typeParameterHandles);
		generateAnnotationsInfos(method, methodInfo.getAnnotations(), methodInfo.getTagBits(), newElements);
		Object defaultValue = methodInfo.getDefaultValue();
		if (defaultValue instanceof IBinaryAnnotation) {
			generateAnnotationInfo(method, newElements, (IBinaryAnnotation) defaultValue, new String(methodInfo.getSelector()));
		}
	}
}
/**
 * Creates the handles and infos for the type parameter of the given binary member.
 * Adds new handles to the given vector.
 */
private void generateTypeParameterInfos(BinaryMember parent, char[] signature, HashMap newElements, ArrayList typeParameterHandles) {
	if (signature == null) return;
	char[][] typeParameterSignatures = Signature.getTypeParameters(signature);
	for (int i = 0, typeParameterCount = typeParameterSignatures.length; i < typeParameterCount; i++) {
		char[] typeParameterSignature = typeParameterSignatures[i];
		char[] typeParameterName = Signature.getTypeVariable(typeParameterSignature);
		CharOperation.replace(typeParameterSignature, '/', '.');
		char[][] typeParameterBoundSignatures = Signature.getTypeParameterBounds(typeParameterSignature);
		int boundLength = typeParameterBoundSignatures.length;
		char[][] typeParameterBounds = new char[boundLength][];
		for (int j = 0; j < boundLength; j++) {
			typeParameterBounds[j] = Signature.toCharArray(typeParameterBoundSignatures[j]);
		}
		TypeParameter typeParameter = new TypeParameter(parent, new String(typeParameterName));
		TypeParameterElementInfo info = new TypeParameterElementInfo();
		info.bounds = typeParameterBounds;
		info.boundsSignatures = typeParameterBoundSignatures;
		typeParameterHandles.add(typeParameter);

		// ensure that 2 binary methods with the same signature but with different return types have different occurence counts.
		// (case of bridge methods in 1.5)
		while (newElements.containsKey(typeParameter))
			typeParameter.occurrenceCount++;

		newElements.put(typeParameter, info);
	}
}
/**
 * Returns true iff the <code>readBinaryChildren</code> has already
 * been called.
 */
boolean hasReadBinaryChildren() {
	return this.binaryChildren != null;
}
/**
 * Creates the handles for <code>BinaryMember</code>s defined in this
 * <code>ClassFile</code> and adds them to the
 * <code>JavaModelManager</code>'s cache.
 */
protected void readBinaryChildren(ClassFile classFile, HashMap newElements, IBinaryType typeInfo) {
	ArrayList childrenHandles = new ArrayList();
	BinaryType type = (BinaryType) classFile.getType();
	ArrayList typeParameterHandles = new ArrayList();
	if (typeInfo != null) { //may not be a valid class file
		generateAnnotationsInfos(type, typeInfo.getAnnotations(), typeInfo.getTagBits(), newElements);
		generateTypeParameterInfos(type, typeInfo.getGenericSignature(), newElements, typeParameterHandles);
		generateFieldInfos(type, typeInfo, newElements, childrenHandles);
		generateMethodInfos(type, typeInfo, newElements, childrenHandles, typeParameterHandles);
		generateInnerClassHandles(type, typeInfo, childrenHandles); // Note inner class are separate openables that are not opened here: no need to pass in newElements
	}

	this.binaryChildren = new JavaElement[childrenHandles.size()];
	childrenHandles.toArray(this.binaryChildren);
	int typeParameterHandleSize = typeParameterHandles.size();
	if (typeParameterHandleSize == 0) {
		this.typeParameters = TypeParameter.NO_TYPE_PARAMETERS;
	} else {
		this.typeParameters = new ITypeParameter[typeParameterHandleSize];
		typeParameterHandles.toArray(this.typeParameters);
	}
}
/**
 * Removes the binary children handles and remove their infos from
 * the <code>JavaModelManager</code>'s cache.
 */
void removeBinaryChildren() throws JavaModelException {
	if (this.binaryChildren != null) {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		for (int i = 0; i <this.binaryChildren.length; i++) {
			JavaElement child = this.binaryChildren[i];
			if (child instanceof BinaryType) {
				manager.removeInfoAndChildren((JavaElement)child.getParent());
			} else {
				manager.removeInfoAndChildren(child);
			}
		}
		this.binaryChildren = JavaElement.NO_ELEMENTS;
	}
	if (this.typeParameters != null) {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		for (int i = 0; i <this.typeParameters.length; i++) {
			TypeParameter typeParameter = (TypeParameter) this.typeParameters[i];
			manager.removeInfoAndChildren(typeParameter);
		}
		this.typeParameters = TypeParameter.NO_TYPE_PARAMETERS;
	}
}
}
