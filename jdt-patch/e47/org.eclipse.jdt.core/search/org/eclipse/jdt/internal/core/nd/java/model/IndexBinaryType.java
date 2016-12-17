/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.BinaryTypeFormatter;
import org.eclipse.jdt.internal.compiler.classfmt.ElementValuePairInfo;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotationValuePair;
import org.eclipse.jdt.internal.core.nd.java.NdConstant;
import org.eclipse.jdt.internal.core.nd.java.NdConstantAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdConstantArray;
import org.eclipse.jdt.internal.core.nd.java.NdConstantClass;
import org.eclipse.jdt.internal.core.nd.java.NdConstantEnum;
import org.eclipse.jdt.internal.core.nd.java.NdMethod;
import org.eclipse.jdt.internal.core.nd.java.NdMethodException;
import org.eclipse.jdt.internal.core.nd.java.NdMethodId;
import org.eclipse.jdt.internal.core.nd.java.NdMethodParameter;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.NdTypeInterface;
import org.eclipse.jdt.internal.core.nd.java.NdTypeParameter;
import org.eclipse.jdt.internal.core.nd.java.NdTypeSignature;
import org.eclipse.jdt.internal.core.nd.java.NdVariable;
import org.eclipse.jdt.internal.core.nd.java.TypeRef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Implementation of {@link IBinaryType} that reads all its content from the index
 */
public class IndexBinaryType implements IBinaryType {
	private final TypeRef typeRef;

	private boolean simpleAttributesInitialized;
	private char[] enclosingMethod;
	private char[] enclosingType;
	private char[] fileName;
	private char[] superclassName;
	private int modifiers;
	private boolean isAnonymous;
	private boolean isLocal;
	private boolean isMember;

	private long tagBits;

	private char[] binaryTypeName;

	private static final IBinaryAnnotation[] NO_ANNOTATIONS = new IBinaryAnnotation[0];
	private static final int[] NO_PATH = new int[0];

	public IndexBinaryType(TypeRef type, char[] indexPath) {
		this.typeRef = type;
		this.fileName = indexPath;
	}

	public boolean exists() {
		return this.typeRef.get() != null;
	}

	@Override
	public int getModifiers() {
		initSimpleAttributes();

		return this.modifiers;
	}

	@Override
	public boolean isBinaryType() {
		return true;
	}

	@Override
	public char[] getFileName() {
		return this.fileName;
	}

	@Override
	public IBinaryAnnotation[] getAnnotations() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return toAnnotationArray(this.typeRef.get().getAnnotations());
			} else {
				return NO_ANNOTATIONS;
			}
		}
	}

	private static IBinaryAnnotation[] toAnnotationArray(List<? extends NdAnnotation> annotations) {
		if (annotations.isEmpty()) {
			return NO_ANNOTATIONS;
		}
		IBinaryAnnotation[] result = new IBinaryAnnotation[annotations.size()];

		for (int idx = 0; idx < result.length; idx++) {
			result[idx] = createBinaryAnnotation(annotations.get(idx));
		}
		return result;
	}

	@Override
	public IBinaryTypeAnnotation[] getTypeAnnotations() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return createBinaryTypeAnnotations(type.getTypeAnnotations());
			}
		}
		return null;
	}

	@Override
	public char[] getEnclosingMethod() {
		initSimpleAttributes();

		return this.enclosingMethod;
	}

	@Override
	public char[] getEnclosingTypeName() {
		initSimpleAttributes();

		return this.enclosingType;
	}

	@Override
	public IBinaryField[] getFields() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdVariable> variables = type.getVariables();

				if (variables.isEmpty()) {
					return null;
				}

				IBinaryField[] result = new IBinaryField[variables.size()];
				for (int fieldIdx = 0; fieldIdx < variables.size(); fieldIdx++) {
					result[fieldIdx] = createBinaryField(variables.get(fieldIdx));
				}
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public char[] getGenericSignature() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				if (!type.getFlag(NdType.FLG_GENERIC_SIGNATURE_PRESENT)) {
					return null;
				}
				CharArrayBuffer buffer = new CharArrayBuffer();
				NdTypeParameter.getSignature(buffer, type.getTypeParameters());
				NdTypeSignature superclass = type.getSuperclass();
				if (superclass != null) {
					superclass.getSignature(buffer);
				}
				for (NdTypeInterface nextInterface : type.getInterfaces()) {
					nextInterface.getInterface().getSignature(buffer);
				}
				return buffer.getContents();
			} else {
				return null;
			}
		}
	}

	@Override
	public char[][] getInterfaceNames() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdTypeInterface> interfaces = type.getInterfaces();

				if (interfaces.isEmpty()) {
					return null;
				}

				char[][] result = new char[interfaces.size()][];
				for (int idx = 0; idx < interfaces.size(); idx++) {
					NdTypeSignature nextInterface = interfaces.get(idx).getInterface();

					result[idx] = nextInterface.getRawType().getBinaryName();
				}
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public IBinaryNestedType[] getMemberTypes() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdType> declaredTypes = type.getTypeId().getDeclaredTypes();
				if (declaredTypes.isEmpty()) {
					return null;
				}

				NdResourceFile resFile = type.getResourceFile();
				IString javaRoot = resFile.getPackageFragmentRoot();

				// Filter out all the declared types which are at different java roots (only keep the ones belonging
				// to the same .jar file or to another .class file in the same folder).
				List<IBinaryNestedType> result = new ArrayList<>();
				for (NdType next : declaredTypes) {
					NdResourceFile nextResFile = next.getResourceFile();

					if (nextResFile.getPackageFragmentRoot().compare(javaRoot, true) == 0) {
						result.add(createBinaryNestedType(next));
					}
				}
				return result.isEmpty() ? null : result.toArray(new IBinaryNestedType[result.size()]);
			} else {
				return null;
			}
		}
	}

	private IBinaryNestedType createBinaryNestedType(NdType next) {
		return new IndexBinaryNestedType(next.getTypeId().getBinaryName(), next.getDeclaringType().getBinaryName(),
				next.getModifiers());
	}

	@Override
	public IBinaryMethod[] getMethods() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdMethod> methods = type.getMethods();

				if (methods.isEmpty()) {
					return null;
				}

				IBinaryMethod[] result = new IBinaryMethod[methods.size()];
				for (int idx = 0; idx < result.length; idx++) {
					result[idx] = createBinaryMethod(methods.get(idx));
				}

				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public char[][][] getMissingTypeNames() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				IString string = type.getMissingTypeNames();
				if (string.length() == 0) {
					return null;
				}
				char[] missingTypeNames = string.getChars();
				char[][] paths = CharOperation.splitOn(',', missingTypeNames);
				char[][][] result = new char[paths.length][][];
				for (int idx = 0; idx < paths.length; idx++) {
					result[idx] = CharOperation.splitOn('/', paths[idx]);
				}
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public char[] getName() {
		initSimpleAttributes();

		return this.binaryTypeName;
	}

	@Override
	public char[] getSourceName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getSourceName();
			} else {
				return new char[0];
			}
		}
	}

	@Override
	public char[] getSuperclassName() {
		initSimpleAttributes();

		return this.superclassName;
	}

	@Override
	public long getTagBits() {
		initSimpleAttributes();

		return this.tagBits;
	}

	@Override
	public boolean isAnonymous() {
		initSimpleAttributes();

		return this.isAnonymous;
	}

	@Override
	public boolean isLocal() {
		initSimpleAttributes();

		return this.isLocal;
	}

	@Override
	public boolean isMember() {
		initSimpleAttributes();

		return this.isMember;
	}

	@Override
	public char[] sourceFileName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				char[] result = type.getSourceFileName().getChars();
				if (result.length == 0) {
					return null;
				}
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member,
			LookupEnvironment environment) {
		return walker;
	}

	private IBinaryMethod createBinaryMethod(NdMethod ndMethod) {
		NdMethodId methodId = ndMethod.getMethodId();

		return IndexBinaryMethod.create().setAnnotations(toAnnotationArray(ndMethod.getAnnotations()))
				.setModifiers(ndMethod.getModifiers()).setIsConstructor(methodId.isConstructor())
				.setArgumentNames(getArgumentNames(ndMethod)).setDefaultValue(unpackValue(ndMethod.getDefaultValue()))
				.setExceptionTypeNames(getExceptionTypeNames(ndMethod))
				.setGenericSignature(getGenericSignatureFor(ndMethod))
				.setMethodDescriptor(methodId.getMethodDescriptor())
				.setParameterAnnotations(getParameterAnnotations(ndMethod))
				.setSelector(ndMethod.getMethodId().getSelector()).setTagBits(ndMethod.getTagBits())
				.setIsClInit(methodId.isClInit()).setTypeAnnotations(createBinaryTypeAnnotations(ndMethod.getTypeAnnotations()));
	}

	private static IBinaryTypeAnnotation[] createBinaryTypeAnnotations(List<? extends NdTypeAnnotation> typeAnnotations) {
		if (typeAnnotations.isEmpty()) {
			return null;
		}
		IBinaryTypeAnnotation[] result = new IBinaryTypeAnnotation[typeAnnotations.size()];
		int idx = 0;
		for (NdTypeAnnotation next : typeAnnotations) {
			IBinaryAnnotation annotation = createBinaryAnnotation(next);
			int[] typePath = getTypePath(next.getTypePath());
			int info = 0;
			int info2 = 0;
			switch (next.getTargetType()) {
				case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER:
				case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER:
					info = next.getTargetInfoArg0();
					break;
				case AnnotationTargetTypeConstants.CLASS_EXTENDS:
					info = next.getTarget();
					break;
				case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND:
				case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND:
					info = next.getTargetInfoArg0();
					info2 = next.getTargetInfoArg1();
					break;
				case AnnotationTargetTypeConstants.FIELD:
				case AnnotationTargetTypeConstants.METHOD_RETURN:
				case AnnotationTargetTypeConstants.METHOD_RECEIVER:
					break;
				case AnnotationTargetTypeConstants.METHOD_FORMAL_PARAMETER :
					info = next.getTargetInfoArg0();
					break;
				case AnnotationTargetTypeConstants.THROWS :
					info = next.getTarget();
					break;

				default:
					throw new IllegalStateException("Target type not handled " + next.getTargetType()); //$NON-NLS-1$
			}
			result[idx++] = new IndexBinaryTypeAnnotation(next.getTargetType(), info, info2, typePath, annotation);
		}
		return result;
	}

	private static int[] getTypePath(byte[] typePath) {
		if (typePath.length == 0) {
			return NO_PATH;
		}
		int[] result = new int[typePath.length];
		for (int idx = 0; idx < typePath.length; idx++) {
			result[idx] = typePath[idx];
		}
		return result;
	}

	private static char[] getGenericSignatureFor(NdMethod method) {
		if (!method.hasAllFlags(NdMethod.FLG_GENERIC_SIGNATURE_PRESENT)) {
			return null;
		}
		CharArrayBuffer result = new CharArrayBuffer();
		method.getGenericSignature(result, method.hasAllFlags(NdMethod.FLG_THROWS_SIGNATURE_PRESENT));
		return result.getContents();
	}
	
	private char[][] getArgumentNames(NdMethod ndMethod) {
		// Unlike what its JavaDoc says, IBinaryType returns an empty array if no argument names are available, so
		// we replicate this weird undocumented corner case here.
		char[][] result = ndMethod.getParameterNames();
		int lastNonEmpty = -1;
		for (int idx = 0; idx < result.length; idx++) {
			if (result[idx] != null && result[idx].length != 0) {
				lastNonEmpty = idx;
			}
		}

		if (lastNonEmpty != result.length - 1) {
			char[][] newResult = new char[lastNonEmpty + 1][];
			System.arraycopy(result, 0, newResult, 0, lastNonEmpty + 1);
			return newResult;
		}
		return result;
	}

	private IBinaryAnnotation[][] getParameterAnnotations(NdMethod ndMethod) {
		List<NdMethodParameter> parameters = ndMethod.getMethodParameters();
		if (parameters.isEmpty()) {
			return null;
		}

		IBinaryAnnotation[][] result = new IBinaryAnnotation[parameters.size()][];
		for (int idx = 0; idx < parameters.size(); idx++) {
			NdMethodParameter next = parameters.get(idx);

			result[idx] = toAnnotationArray(next.getAnnotations());
		}

		// int newLength = result.length;
		// while (newLength > 0 && result[newLength - 1] == null) {
		// --newLength;
		// }
		//
		// if (newLength < result.length) {
		// if (newLength == 0) {
		// return null;
		// }
		// IBinaryAnnotation[][] newResult = new IBinaryAnnotation[newLength][];
		// System.arraycopy(result, 0, newResult, 0, newLength);
		// result = newResult;
		// }

		return result;
	}

	private char[][] getExceptionTypeNames(NdMethod ndMethod) {
		List<NdMethodException> exceptions = ndMethod.getExceptions();

		// Although the JavaDoc for IBinaryMethod says that the exception list will be null if empty,
		// the implementation in MethodInfo returns an empty array rather than null. We copy the
		// same behavior here in case something is relying on it. Uncomment the following if the "null"
		// version is deemed correct.

		// if (exceptions.isEmpty()) {
		// return null;
		// }

		char[][] result = new char[exceptions.size()][];
		for (int idx = 0; idx < exceptions.size(); idx++) {
			NdMethodException next = exceptions.get(idx);

			result[idx] = next.getExceptionType().getRawType().getBinaryName();
		}
		return result;
	}

	public static IBinaryField createBinaryField(NdVariable ndVariable) {
		char[] name = ndVariable.getName().getChars();
		Constant constant = null;
		NdConstant ndConstant = ndVariable.getConstant();
		if (ndConstant != null) {
			constant = ndConstant.getConstant();
		}
		if (constant == null) {
			constant = Constant.NotAConstant;
		}

		NdTypeSignature type = ndVariable.getType();

		IBinaryTypeAnnotation[] typeAnnotationArray = createBinaryTypeAnnotations(ndVariable.getTypeAnnotations());

		IBinaryAnnotation[] annotations = toAnnotationArray(ndVariable.getAnnotations());

		CharArrayBuffer signature = new CharArrayBuffer();
		if (ndVariable.hasVariableFlag(NdVariable.FLG_GENERIC_SIGNATURE_PRESENT)) {
			type.getSignature(signature);
		}

		long tagBits = ndVariable.getTagBits();
		return new IndexBinaryField(annotations, constant, signature.getContents(), ndVariable.getModifiers(), name,
				tagBits, typeAnnotationArray, type.getRawType().getFieldDescriptor().getChars());
	}

	public static IBinaryAnnotation createBinaryAnnotation(NdAnnotation ndAnnotation) {
		List<NdAnnotationValuePair> elementValuePairs = ndAnnotation.getElementValuePairs();

		final IBinaryElementValuePair[] resultingPair = new IBinaryElementValuePair[elementValuePairs.size()];

		for (int idx = 0; idx < elementValuePairs.size(); idx++) {
			NdAnnotationValuePair next = elementValuePairs.get(idx);

			resultingPair[idx] = new ElementValuePairInfo(next.getName().getChars(), unpackValue(next.getValue()));
		}

		final char[] binaryName = JavaNames.fieldDescriptorToBinaryName(
				ndAnnotation.getType().getRawType().getFieldDescriptor().getChars());

		return new IBinaryAnnotation() {
			@Override
			public char[] getTypeName() {
				return binaryName;
			}

			@Override
			public IBinaryElementValuePair[] getElementValuePairs() {
				return resultingPair;
			}

			@Override
			public String toString() {
				return BinaryTypeFormatter.annotationToString(this);
			}
		};
	}

	public void initSimpleAttributes() {
		if (!this.simpleAttributesInitialized) {
			this.simpleAttributesInitialized = true;

			try (IReader rl = this.typeRef.lock()) {
				NdType type = this.typeRef.get();
				if (type != null) {
					NdMethodId methodId = type.getDeclaringMethod();

					if (methodId != null) {
						char[] methodName = methodId.getMethodName().getChars();
						int startIdx = CharArrayUtils.lastIndexOf('#', methodName);
						this.enclosingMethod = CharArrayUtils.subarray(methodName, startIdx + 1);
						this.enclosingType = CharArrayUtils.subarray(methodName, 1, startIdx);
					} else {
						NdTypeId typeId = type.getDeclaringType();

						if (typeId != null) {
							this.enclosingType = typeId.getBinaryName();
						}
					}

					this.modifiers = type.getModifiers();
					this.isAnonymous = type.isAnonymous();
					this.isLocal = type.isLocal();
					this.isMember = type.isMember();
					this.tagBits = type.getTagBits();

					NdTypeSignature superclass = type.getSuperclass();
					if (superclass != null) {
						this.superclassName = superclass.getRawType().getBinaryName();
					} else {
						this.superclassName = null;
					}

					this.binaryTypeName = JavaNames.fieldDescriptorToBinaryName(type.getFieldDescriptor().getChars());
				} else {
					this.binaryTypeName = JavaNames.fieldDescriptorToBinaryName(this.typeRef.getFieldDescriptor());
				}
			}
		}
	}

	private static Object unpackValue(NdConstant value) {
		if (value == null) {
			return null;
		}
		if (value instanceof NdConstantAnnotation) {
			NdConstantAnnotation annotation = (NdConstantAnnotation) value;

			return createBinaryAnnotation(annotation.getValue());
		}
		if (value instanceof NdConstantArray) {
			NdConstantArray array = (NdConstantArray) value;

			List<NdConstant> arrayContents = array.getValue();

			Object[] result = new Object[arrayContents.size()];
			for (int idx = 0; idx < arrayContents.size(); idx++) {
				result[idx] = unpackValue(arrayContents.get(idx));
			}
			return result;
		}
		if (value instanceof NdConstantEnum) {
			NdConstantEnum ndConstantEnum = (NdConstantEnum) value;

			NdTypeSignature signature = ndConstantEnum.getType();

			return new EnumConstantSignature(signature.getRawType().getBinaryName(), ndConstantEnum.getValue());
		}
		if (value instanceof NdConstantClass) {
			NdConstantClass constant = (NdConstantClass) value;

			return new ClassSignature(constant.getValue().getRawType().getBinaryName());
		}

		return value.getConstant();
	}

	@Override
	public ExternalAnnotationStatus getExternalAnnotationStatus() {
		return ExternalAnnotationStatus.NOT_EEA_CONFIGURED;
	}
}
