/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.compiler.env.IRecordComponent;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.DeduplicationUtil;

/**
 * Element info for <code>ClassFile</code> handles.
 */

class ClassFileInfo extends OpenableElementInfo implements SuffixConstants {
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

private void generateAnnotationsInfos(JavaElement member, IBinaryAnnotation[] binaryAnnotations, long tagBits, Map<IJavaElement, IElementInfo> newElements) {
	generateAnnotationsInfos(member, null, binaryAnnotations, tagBits, newElements);
}
/**
 * Creates the handles and infos for the annotations of the given binary member.
 * Adds new handles to the given vector.
 */
private void generateAnnotationsInfos(JavaElement member, char[] parameterName, IBinaryAnnotation[] binaryAnnotations, long tagBits, Map<IJavaElement, IElementInfo> newElements) {
	if (binaryAnnotations != null) {
		for (IBinaryAnnotation annotationInfo : binaryAnnotations) {
			generateAnnotationInfo(member, parameterName, newElements, annotationInfo, null);
		}
	}
	generateStandardAnnotationsInfos(member, parameterName, tagBits, newElements);
}
private void generateAnnotationInfo(JavaElement parent, Map<IJavaElement, IElementInfo> newElements, IBinaryAnnotation annotationInfo, String memberValuePairName) {
	generateAnnotationInfo(parent, null, newElements, annotationInfo, memberValuePairName);
}
private void generateAnnotationInfo(JavaElement parent, char[] parameterName, Map<IJavaElement, IElementInfo> newElements, IBinaryAnnotation annotationInfo, String memberValuePairName) {
	char[] typeName = org.eclipse.jdt.core.Signature.toCharArray(CharOperation.replaceOnCopy(annotationInfo.getTypeName(), '/', '.'));
	int occurrenceCount = 0;
	Annotation annotation;
	do {
		annotation = new Annotation(parent, new String(typeName), memberValuePairName, ++occurrenceCount);
	} while (newElements.containsKey(annotation));

	newElements.put(annotation, annotationInfo);
	IBinaryElementValuePair[] pairs = annotationInfo.getElementValuePairs();
	for (IBinaryElementValuePair pair : pairs) {
		Object value = pair.getValue();
		if (value instanceof IBinaryAnnotation) {
			generateAnnotationInfo(annotation, newElements, (IBinaryAnnotation) value, new String(pair.getName()));
		} else if (value instanceof Object[]) {
			// if the value is an array, it can have no more than 1 dimension - no need to recurse
			Object[] valueArray = (Object[]) value;
			for (Object nestedValue : valueArray) {
				if (nestedValue instanceof IBinaryAnnotation) {
					generateAnnotationInfo(annotation, newElements, (IBinaryAnnotation) nestedValue, new String(pair.getName()));
				}
			}
		}
	}
}
private void generateStandardAnnotationsInfos(JavaElement javaElement, char[] parameterName, long tagBits, Map<IJavaElement, IElementInfo> newElements) {
	if ((tagBits & TagBits.AllStandardAnnotationsMask) == 0)
		return;
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

private void generateStandardAnnotation(JavaElement javaElement, char[][] typeName, IMemberValuePair[] members, Map<IJavaElement, IElementInfo> newElements) {
	IAnnotation annotation = new Annotation(javaElement, new String(CharOperation.concatWith(typeName, '.')));
	AnnotationInfo annotationInfo = new AnnotationInfo();
	annotationInfo.members = members;
	newElements.put(annotation, annotationInfo);
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
				@Override
				public int getValueKind() {
					return IMemberValuePair.K_QUALIFIED_NAME;
				}
				@Override
				public Object getValue() {
					return value;
				}
				@Override
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
private void generateFieldInfos(IType type, IBinaryType typeInfo, Map<IJavaElement, IElementInfo> newElements, ArrayList<JavaElement> childrenHandles) {
	// Make the fields
	IBinaryField[] fields = typeInfo.getFields();
	if (fields == null) {
		return;
	}
	for (IBinaryField fieldInfo : fields) {
		// If the type is a record and this is an instance field, it can only be a record component
		// Filter out
		if (typeInfo.isRecord() && (fieldInfo.getModifiers() & ClassFileConstants.AccStatic) == 0)
			continue;
		BinaryField field = new BinaryField((JavaElement)type, DeduplicationUtil.toString(fieldInfo.getName()));
		newElements.put(field, fieldInfo);
		childrenHandles.add(field);
		generateAnnotationsInfos(field, fieldInfo.getAnnotations(), fieldInfo.getTagBits(), newElements);
	}
}
/**
 * Creates the handles and infos for the fields of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateRecordComponentInfos(IType type, IBinaryType typeInfo, Map<IJavaElement, IElementInfo> newElements, ArrayList<JavaElement> childrenHandles) {
	// Make the fields
	IRecordComponent[] components = typeInfo.getRecordComponents();
	if (components == null) {
		return;
	}
	for (IRecordComponent componentInfo : components) {
		BinaryField component = new BinaryField((JavaElement)type, DeduplicationUtil.toString(componentInfo.getName())) {
			@Override
			public boolean isRecordComponent() throws JavaModelException {
				return true;
			}
		};
		newElements.put(component, componentInfo);
		childrenHandles.add(component);
		generateAnnotationsInfos(component, componentInfo.getAnnotations(), componentInfo.getTagBits(), newElements);
	}
}
/**
 * Creates the handles for the inner types of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateInnerClassHandles(IType type, IBinaryType typeInfo, ArrayList<JavaElement> childrenHandles) {
	// Add inner types
	// If the current type is an inner type, innerClasses returns
	// an extra entry for the current type.  This entry must be removed.
	// Can also return an entry for the enclosing type of an inner type.
	IBinaryNestedType[] innerTypes = typeInfo.getMemberTypes();
	if (innerTypes != null) {
		IPackageFragment pkg = (IPackageFragment) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		for (IBinaryNestedType binaryType : innerTypes) {
			IClassFile parentClassFile= pkg.getClassFile(new String(ClassFile.unqualifiedName(binaryType.getName())) + SUFFIX_STRING_class);
			BinaryType innerType = new BinaryType((JavaElement) parentClassFile, DeduplicationUtil.intern(ClassFile.simpleName(binaryType.getName())));
			childrenHandles.add(innerType);
		}
	}
}
/**
 * Creates the handles and infos for the methods of the given binary type.
 * Adds new handles to the given vector.
 */
private void generateMethodInfos(IType type, IBinaryType typeInfo, Map<IJavaElement, IElementInfo> newElements, ArrayList<JavaElement> childrenHandles, ArrayList<ITypeParameter> typeParameterHandles) {
	IBinaryMethod[] methods = typeInfo.getMethods();
	if (methods == null) {
		return;
	}
	for (IBinaryMethod methodInfo : methods) {
		final boolean isConstructor = methodInfo.isConstructor();
		boolean isEnum = false;
		try {
			isEnum = type.isEnum();
		} catch (JavaModelException e) {
			// ignore
		}
		// TODO (jerome) filter out synthetic members
		//                        indexer should not index them as well
		// if ((methodInfo.getModifiers() & IConstants.AccSynthetic) != 0) continue; // skip synthetic
		boolean useGenericSignature = true;
		char[] signature = methodInfo.getGenericSignature();
		String[] pNames = null;
		if (signature == null) {
			useGenericSignature = false;
			signature = methodInfo.getMethodDescriptor();
			if (isEnum && isConstructor) {
				pNames = Signature.getParameterTypes(new String(signature));
				int length = pNames.length - 2;
				if (length >= 0) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=436347
					System.arraycopy(pNames, 2, pNames = new String[length], 0, length);
			}
		}
		String selector = new String(methodInfo.getSelector());
		if (isConstructor) {
			selector = type.getElementName();
		}
		try {
			if (!(isEnum && isConstructor && !useGenericSignature)) {
				pNames = Signature.getParameterTypes(new String(signature));
			}
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
		} catch (IllegalArgumentException | JavaModelException e) {
			// protect against malformed .class file (e.g. com/sun/crypto/provider/SunJCE_b.class has a 'a' generic signature)
			signature = methodInfo.getMethodDescriptor();
			pNames = Signature.getParameterTypes(new String(signature));
		}
		char[][] paramNames= new char[pNames.length][];
		for (int j= 0; j < pNames.length; j++) {
			paramNames[j]= pNames[j].toCharArray();
		}
		char[][] parameterTypes = ClassFile.translatedNames(paramNames);
		selector =  DeduplicationUtil.intern(selector);
		for (int j= 0; j < pNames.length; j++) {
			pNames[j]= DeduplicationUtil.toString(parameterTypes[j]);
		}
		int occurrenceCount = 0;
		BinaryMethod method;
		// ensure that 2 binary methods with the same signature but with different return types have different occurrence counts.
		// (case of bridge methods in 1.5)
		do {
			method = new BinaryMethod((JavaElement) type, selector, pNames, ++occurrenceCount);
		} while (newElements.containsKey(method));

		childrenHandles.add(method);
		newElements.put(method, methodInfo);

		int max = pNames.length;
		char[][] argumentNames = methodInfo.getArgumentNames();
		if (argumentNames == null || argumentNames.length < max) {
			argumentNames = new char[max][];
			for (int j = 0; j < max; j++) {
				argumentNames[j] = ("arg" + j).toCharArray(); //$NON-NLS-1$
			}
		}
		int startIndex = 0;
		try {
			if (isConstructor) {
				if (isEnum) {
					startIndex = 2;
				} else if (type.isMember()
						&& !Flags.isStatic(type.getFlags())) {
					startIndex = 1;
				}
			}
		} catch(JavaModelException e) {
			// ignore
		}
		for (int j = startIndex; j < max; j++) {
			IBinaryAnnotation[] parameterAnnotations = methodInfo.getParameterAnnotations(j - startIndex, typeInfo.getFileName());
			if (parameterAnnotations != null) {
				LocalVariable localVariable = new LocalVariable(
						method,
						DeduplicationUtil.toString(argumentNames[j]),
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
private void generateTypeParameterInfos(BinaryMember parent, char[] signature, Map<IJavaElement, IElementInfo> newElements, ArrayList<ITypeParameter> typeParameterHandles) {
	if (signature == null) return;
	char[][] typeParameterSignatures = Signature.getTypeParameters(signature);
	for (char[] typeParameterSignature : typeParameterSignatures) {
		char[] typeParameterName = Signature.getTypeVariable(typeParameterSignature);
		CharOperation.replace(typeParameterSignature, '/', '.');
		char[][] typeParameterBoundSignatures = Signature.getTypeParameterBounds(typeParameterSignature);
		int boundLength = typeParameterBoundSignatures.length;
		char[][] typeParameterBounds = new char[boundLength][];
		for (int j = 0; j < boundLength; j++) {
			typeParameterBounds[j] = Signature.toCharArray(typeParameterBoundSignatures[j]);
		}
		int occurrenceCount = 0;
		TypeParameter typeParameter;
		do {
			typeParameter = new TypeParameter(parent, new String(typeParameterName), ++occurrenceCount);
			// ensure that 2 binary methods with the same signature but with different return types have different occurence counts.
			// (case of bridge methods in 1.5)
		} while (newElements.containsKey(typeParameter));
		TypeParameterElementInfo info = new TypeParameterElementInfo();
		info.bounds = typeParameterBounds;
		info.boundsSignatures = typeParameterBoundSignatures;
		typeParameterHandles.add(typeParameter);
		newElements.put(typeParameter, info);
	}
}
/**
 * Creates the handles for <code>BinaryMember</code>s defined in this
 * <code>ClassFile</code> and adds them to the
 * <code>JavaModelManager</code>'s cache.
 */
protected void readBinaryChildren(ClassFile classFile, Map<IJavaElement, IElementInfo> newElements, IBinaryType typeInfo) {
	ArrayList<JavaElement> childrenHandles = new ArrayList<>();
	BinaryType type = (BinaryType) classFile.getType();
	ArrayList<ITypeParameter> typeParameterHandles = new ArrayList<>();
	if (typeInfo != null) { //may not be a valid class file
		generateAnnotationsInfos(type, typeInfo.getAnnotations(), typeInfo.getTagBits(), newElements);
		generateTypeParameterInfos(type, typeInfo.getGenericSignature(), newElements, typeParameterHandles);
		generateFieldInfos(type, typeInfo, newElements, childrenHandles);
		generateRecordComponentInfos(type, typeInfo, newElements, childrenHandles);
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
		for (JavaElement child : this.binaryChildren) {
			if (child instanceof BinaryType) {
				manager.removeInfoAndChildren(child.getParent());
			} else {
				manager.removeInfoAndChildren(child);
			}
		}
		this.binaryChildren = JavaElement.NO_ELEMENTS;
	}
	if (this.typeParameters != null) {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		for (ITypeParameter p : this.typeParameters) {
			TypeParameter typeParameter = (TypeParameter) p;
			manager.removeInfoAndChildren(typeParameter);
		}
		this.typeParameters = TypeParameter.NO_TYPE_PARAMETERS;
	}
}
}
