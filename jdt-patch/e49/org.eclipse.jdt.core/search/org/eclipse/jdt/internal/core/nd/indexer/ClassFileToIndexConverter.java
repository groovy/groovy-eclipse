/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.indexer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotationValuePair;
import org.eclipse.jdt.internal.core.nd.java.NdBinding;
import org.eclipse.jdt.internal.core.nd.java.NdComplexTypeSignature;
import org.eclipse.jdt.internal.core.nd.java.NdConstant;
import org.eclipse.jdt.internal.core.nd.java.NdConstantAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdConstantArray;
import org.eclipse.jdt.internal.core.nd.java.NdConstantClass;
import org.eclipse.jdt.internal.core.nd.java.NdConstantEnum;
import org.eclipse.jdt.internal.core.nd.java.NdMethod;
import org.eclipse.jdt.internal.core.nd.java.NdMethodParameter;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdTypeArgument;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.NdTypeInterface;
import org.eclipse.jdt.internal.core.nd.java.NdTypeParameter;
import org.eclipse.jdt.internal.core.nd.java.NdTypeSignature;
import org.eclipse.jdt.internal.core.nd.java.NdVariable;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;
import org.eclipse.jdt.internal.core.util.Util;

public final class ClassFileToIndexConverter {
	private static final char[] JAVA_LANG_OBJECT_FIELD_DESCRIPTOR = "Ljava/lang/Object;".toCharArray(); //$NON-NLS-1$
	private static final char[] INNER_TYPE_SEPARATOR = new char[] { '$' };
	private static final char[] FIELD_DESCRIPTOR_SUFFIX = new char[] { ';' };
	private static final char[] COMMA = new char[]{','};
	private static final char[][] EMPTY_CHAR_ARRAY_ARRAY = new char[0][];
	private static final boolean ENABLE_LOGGING = false;
	static final char[] EMPTY_CHAR_ARRAY = new char[0];
	private static final char[] PATH_SEPARATOR = new char[]{'/'};
	private static final char[] ARRAY_FIELD_DESCRIPTOR_PREFIX = new char[] { '[' };
	private NdResourceFile resource;
	private JavaIndex index;

	public ClassFileToIndexConverter(NdResourceFile resourceFile) {
		this.resource = resourceFile;
		this.index = JavaIndex.getIndex(resourceFile.getNd());
	}

	private Nd getNd() {
		return this.resource.getNd();
	}

	/**
	 * Create a type info from the given class file in a jar and adds it to the given list of infos.
	 *
	 * @throws CoreException
	 */
	protected static IBinaryType createInfoFromClassFileInJar(Openable classFile) throws CoreException {
		PackageFragment pkg = (PackageFragment) classFile.getParent();
		String classFilePath = Util.concatWith(pkg.names, classFile.getElementName(), '/');
		IBinaryType info = null;
		java.util.zip.ZipFile zipFile = null;
		try {
			zipFile = ((JarPackageFragmentRoot) pkg.getParent()).getJar();
			info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(zipFile, classFilePath);
		} catch (Exception e) {
			throw new CoreException(Package.createStatus("Unable to parse JAR file", e)); //$NON-NLS-1$
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
		}
		return info;
	}

	/**
	 * Adds a type to the index, given an input class file and a binary name. Note that the given binary name is 
	 * 
	 * @param binaryType an object used for parsing the .class file itself
	 * @param fieldDescriptor the name that is used to locate the class, computed from the .class file's name and location.
	 * In the event that the .class file has been moved, this may differ from the binary name stored in the .class file
	 * itself, which is why this is received as an argument rather than extracted from the .class file.
	 * @throws CoreException
	 */
	public NdType addType(IBinaryType binaryType, char[] fieldDescriptor, IProgressMonitor monitor) throws CoreException {
		char[] fieldDescriptorFromClass = JavaNames.binaryNameToFieldDescriptor(binaryType.getName());
		logInfo("adding binary type " + new String(fieldDescriptor)); //$NON-NLS-1$

		NdTypeId name = createTypeIdFromFieldDescriptor(fieldDescriptor);
		NdType type = name.findTypeByResourceAddress(this.resource.address);

		if (type == null) {
			type = new NdType(getNd(), this.resource);
		}

		IBinaryTypeAnnotation[] typeAnnotations = binaryType.getTypeAnnotations();
		if (typeAnnotations != null) {
			type.allocateTypeAnnotations(typeAnnotations.length);
			for (IBinaryTypeAnnotation typeAnnotation : typeAnnotations) {
				NdTypeAnnotation annotation = type.createTypeAnnotation();

				initTypeAnnotation(annotation, typeAnnotation);
			}
		}

		type.setTypeId(name);

		if (!CharArrayUtils.equals(fieldDescriptorFromClass, fieldDescriptor)) {
			type.setFieldDescriptorFromClass(fieldDescriptorFromClass);
		}

		char[][] interfaces = binaryType.getInterfaceNames();
		if (interfaces == null) {
			interfaces = EMPTY_CHAR_ARRAY_ARRAY;
		}

		if (binaryType.getGenericSignature() != null) {
			type.setFlag(NdType.FLG_GENERIC_SIGNATURE_PRESENT, true);
		}

		// Create the default generic signature if the .class file didn't supply one
		SignatureWrapper signatureWrapper = GenericSignatures.getGenericSignature(binaryType);

		type.setModifiers(binaryType.getModifiers());
		type.setDeclaringType(createTypeIdFromBinaryName(binaryType.getEnclosingTypeName()));

		readTypeParameters(type, signatureWrapper);

		char[] superclassFieldDescriptor;
		char[] superclassBinaryName = binaryType.getSuperclassName();
		if (superclassBinaryName == null) {
			superclassFieldDescriptor = JAVA_LANG_OBJECT_FIELD_DESCRIPTOR;
		} else {
			superclassFieldDescriptor = JavaNames.binaryNameToFieldDescriptor(superclassBinaryName);
		}
		type.setSuperclass(createTypeSignature(signatureWrapper, superclassFieldDescriptor));

		short interfaceIdx = 0;
		while (signatureWrapper.start < signatureWrapper.signature.length) {
			// Note that there may be more interfaces listed in the generic signature than in the interfaces list.
			// Although the VM spec doesn't discuss this case specifically, there are .class files in the wild with
			// this characteristic. In such cases, we take what's in the generic signature and discard what's in the
			// interfaces list.
			char[] interfaceSpec = interfaceIdx < interfaces.length ? interfaces[interfaceIdx] : EMPTY_CHAR_ARRAY;
			new NdTypeInterface(getNd(), type,
					createTypeSignature(signatureWrapper, JavaNames.binaryNameToFieldDescriptor(interfaceSpec)));
			interfaceIdx++;
		}

		IBinaryAnnotation[] annotations = binaryType.getAnnotations();
		attachAnnotations(type, annotations);

		type.setDeclaringMethod(binaryType.getEnclosingMethod());

		IBinaryField[] fields = binaryType.getFields();

		if (fields != null) {
			type.allocateVariables(fields.length);
			for (IBinaryField nextField : fields) {
				addField(type, nextField);
			}
		}

		IBinaryMethod[] methods = binaryType.getMethods();

		if (methods != null) {
			char[][] methodNames = new char[methods.length][];
			Integer[] sortedElementIndices = new Integer[methods.length];

			for (int idx = 0; idx < sortedElementIndices.length; idx++) {
				sortedElementIndices[idx] = idx;
				methodNames[idx] = getSelectorAndDescriptor(methods[idx]);
			}

			Arrays.sort(sortedElementIndices, (Integer i1, Integer i2) -> {
				return CharArrayUtils.compare(methodNames[i1], methodNames[i2]);
			});

			type.allocateMethods(methods.length);
			for (int idx = 0; idx < methods.length; idx++) {
				NdMethod newMethod = type.createMethod();
				int position = sortedElementIndices[idx];
				newMethod.setDeclarationPosition(position);
				newMethod.setMethodName(methodNames[position]);
				IBinaryMethod nextMethod = methods[position];
				addMethod(newMethod, nextMethod, binaryType);
			}
		}

		char[][][] missingTypeNames = binaryType.getMissingTypeNames();
		char[] missingTypeString = getMissingTypeString(missingTypeNames);

		type.setMissingTypeNames(missingTypeString);
		type.setSourceFileName(binaryType.sourceFileName());
		type.setAnonymous(binaryType.isAnonymous());
		type.setIsLocal(binaryType.isLocal());
		type.setIsMember(binaryType.isMember());
		type.setTagBits(binaryType.getTagBits());
		type.setSourceNameOverride(binaryType.getSourceName());

		return type;
	}

	private char[] getSelectorAndDescriptor(IBinaryMethod binaryMethod) {
		return CharArrayUtils.concat(binaryMethod.getSelector(), binaryMethod.getMethodDescriptor());
	}

	private static char[] getMissingTypeString(char[][][] missingTypeNames) {
		char[] missingTypeString = null;
		if (missingTypeNames != null) {
			CharArrayBuffer builder = new CharArrayBuffer();
			for (int typeIdx = 0; typeIdx < missingTypeNames.length; typeIdx++) {
				char[][] next = missingTypeNames[typeIdx];
				if (typeIdx != 0) {
					builder.append(COMMA);
				}
				if (next == null) {
					continue;
				}
				for (int segmentIdx = 0; segmentIdx < next.length; segmentIdx++) {
					char[] segment = next[segmentIdx];
					if (segment == null) {
						continue;
					}
					if (segmentIdx != 0) {
						builder.append(PATH_SEPARATOR);
					}
					builder.append(segment);
				}
			}
			missingTypeString = builder.getContents();
		}
		return missingTypeString;
	}

	private void attachAnnotations(NdMethod method, IBinaryAnnotation[] annotations) {
		if (annotations != null) {
			method.allocateAnnotations(annotations.length);
			for (IBinaryAnnotation next : annotations) {
				NdAnnotation annotation = method.createAnnotation();
				initAnnotation(annotation, next);
			}
		}
	}

	private void attachAnnotations(NdType type, IBinaryAnnotation[] annotations) {
		if (annotations != null) {
			type.allocateAnnotations(annotations.length);
			for (IBinaryAnnotation next : annotations) {
				NdAnnotation annotation = type.createAnnotation();
				initAnnotation(annotation, next);
			}
		}
	}

	private void attachAnnotations(NdVariable variable, IBinaryAnnotation[] annotations) {
		if (annotations != null) {
			variable.allocateAnnotations(annotations.length);
			for (IBinaryAnnotation next : annotations) {
				NdAnnotation annotation = variable.createAnnotation();
				initAnnotation(annotation, next);
			}
		}
	}

	private void attachAnnotations(NdMethodParameter variable, IBinaryAnnotation[] annotations) {
		if (annotations != null) {
			variable.allocateAnnotations(annotations.length);
			for (IBinaryAnnotation next : annotations) {
				NdAnnotation annotation = variable.createAnnotation();
				initAnnotation(annotation, next);
			}
		}
	}

	/**
	 * Adds the given method to the given type
	 *
	 * @throws CoreException
	 */
	private void addMethod(NdMethod method, IBinaryMethod next, IBinaryType binaryType)
			throws CoreException {
		int flags = 0;

		attachAnnotations(method, next.getAnnotations());

		if (next.getGenericSignature() != null) {
			flags |= NdMethod.FLG_GENERIC_SIGNATURE_PRESENT;
		}
		SignatureWrapper signature = GenericSignatures.getGenericSignature(next);
		SignatureWrapper descriptor = new SignatureWrapper(next.getMethodDescriptor());
		readTypeParameters(method, signature);

		IBinaryTypeAnnotation[] typeAnnotations = next.getTypeAnnotations();
		if (typeAnnotations != null) {
			method.allocateTypeAnnotations(typeAnnotations.length);
			for (IBinaryTypeAnnotation typeAnnotation : typeAnnotations) {
				NdTypeAnnotation annotation = method.createTypeAnnotation();

				initTypeAnnotation(annotation, typeAnnotation);
			}
		}

		skipChar(signature, '(');
		skipChar(descriptor, '(');

		List<char[]> parameterFieldDescriptors = new ArrayList<>();
		while (!descriptor.atEnd()) {
			if (descriptor.charAtStart() == ')') {
				skipChar(descriptor, ')');
				break;
			}
			parameterFieldDescriptors.add(readNextFieldDescriptor(descriptor));
		}

		char[][] parameterNames = next.getArgumentNames();
		int numArgumentsInGenericSignature = countMethodArguments(signature);
		int numCompilerDefinedParameters = Math.max(0,
				parameterFieldDescriptors.size() - numArgumentsInGenericSignature);
		
		boolean compilerDefinedParametersAreIncludedInSignature = (next.getGenericSignature() == null);

		// If there is no generic signature, then fall back to heuristics based on what we know about
		// where the java compiler likes to create compiler-defined arguments
		if (compilerDefinedParametersAreIncludedInSignature) {
			// Constructors for non-static member types get a compiler-defined first argument
			if (binaryType.isMember() 
					&& next.isConstructor() 
					&& ((binaryType.getModifiers() & Modifier.STATIC) == 0)
					&& parameterFieldDescriptors.size() > 0) {

				numCompilerDefinedParameters = 1;
			} else {
				numCompilerDefinedParameters = 0;
			}
		}

		int parameterNameIdx = 0;
		int annotatedParametersCount = next.getAnnotatedParametersCount();
		int namedParameterCount = parameterNames == null ? 0 : parameterNames.length;
		int estimatedParameterCount = Math.max(Math.max(Math.max(numArgumentsInGenericSignature, namedParameterCount),
				annotatedParametersCount), parameterFieldDescriptors.size());
		method.allocateParameters(estimatedParameterCount);

		short descriptorParameterIdx = 0;
		char[] binaryTypeName = binaryType.getName();
		while (!signature.atEnd()) {
			if (signature.charAtStart() == ')') {
				skipChar(signature, ')');
				break;
			}
			char[] nextFieldDescriptor = parameterFieldDescriptors.get(descriptorParameterIdx);
			/**
			 * True iff this a parameter which is part of the field descriptor but not the generic signature -- that is,
			 * it is a compiler-defined parameter.
			 */
			boolean isCompilerDefined = descriptorParameterIdx < numCompilerDefinedParameters;
			SignatureWrapper nextFieldSignature = signature;
			if (isCompilerDefined && !compilerDefinedParametersAreIncludedInSignature) {
				nextFieldSignature = new SignatureWrapper(nextFieldDescriptor);
			}
			NdMethodParameter parameter = method.createNewParameter();
			parameter.setType(createTypeSignature(nextFieldSignature, nextFieldDescriptor));

			parameter.setCompilerDefined(isCompilerDefined);

			if (descriptorParameterIdx < annotatedParametersCount) {
				IBinaryAnnotation[] parameterAnnotations = next.getParameterAnnotations(descriptorParameterIdx,
						binaryTypeName);

				attachAnnotations(parameter, parameterAnnotations);
			}
			if (!isCompilerDefined && namedParameterCount > parameterNameIdx) {
				parameter.setName(parameterNames[parameterNameIdx++]);
			}
			descriptorParameterIdx++;
		}

		skipChar(descriptor, ')');
		char[] nextFieldDescriptor = readNextFieldDescriptor(descriptor);
		method.setReturnType(createTypeSignature(signature, nextFieldDescriptor));

		boolean hasExceptionsInSignature = hasAnotherException(signature);
		char[][] exceptionTypes = next.getExceptionTypeNames();
		if (exceptionTypes == null) {
			exceptionTypes = CharArrayUtils.EMPTY_ARRAY_OF_CHAR_ARRAYS;
		}
		method.allocateExceptions(exceptionTypes.length);
		int throwsIdx = 0;
		if (hasExceptionsInSignature) {
			while (hasAnotherException(signature)) {
				signature.start++;
				method.createException(createTypeSignature(signature,
						JavaNames.binaryNameToFieldDescriptor(exceptionTypes[throwsIdx])));
				throwsIdx++;
			}
		} else if (exceptionTypes.length != 0) {
			for (;throwsIdx < exceptionTypes.length; throwsIdx++) {
				char[] fieldDescriptor = JavaNames.binaryNameToFieldDescriptor(exceptionTypes[throwsIdx]);
				SignatureWrapper convertedWrapper = new SignatureWrapper(fieldDescriptor);
				method.createException(createTypeSignature(convertedWrapper,
						JavaNames.binaryNameToFieldDescriptor(exceptionTypes[throwsIdx])));
			}
		}

		if (hasExceptionsInSignature) {
			flags |= NdMethod.FLG_THROWS_SIGNATURE_PRESENT;
		}

		Object defaultValue = next.getDefaultValue();
		if (defaultValue != null) {
			method.setDefaultValue(createConstantFromMixedType(defaultValue));
		}

		method.setModifiers(next.getModifiers());
		method.setTagBits(next.getTagBits());
		method.setFlags(flags);
	}

	private boolean hasAnotherException(SignatureWrapper signature) {
		return !signature.atEnd() && signature.charAtStart() == '^';
	}

	private void skipChar(SignatureWrapper signature, char toSkip) {
		if (signature.start < signature.signature.length && signature.charAtStart() == toSkip) {
			signature.start++;
		}
	}

	/**
	 * Adds the given field to the given type
	 */
	private void addField(NdType type, IBinaryField nextField) throws CoreException {
		NdVariable variable = type.createVariable();

		variable.setName(nextField.getName());

		if (nextField.getGenericSignature() != null) {
			variable.setVariableFlag(NdVariable.FLG_GENERIC_SIGNATURE_PRESENT);
		}

		attachAnnotations(variable, nextField.getAnnotations());

		variable.setConstant(NdConstant.create(getNd(), nextField.getConstant()));
		variable.setModifiers(nextField.getModifiers());
		SignatureWrapper nextTypeSignature = GenericSignatures.getGenericSignatureFor(nextField);

		IBinaryTypeAnnotation[] typeAnnotations = nextField.getTypeAnnotations();
		if (typeAnnotations != null) {
			variable.allocateTypeAnnotations(typeAnnotations.length);
			for (IBinaryTypeAnnotation next : typeAnnotations) {
				NdTypeAnnotation annotation = variable.createTypeAnnotation();
	
				initTypeAnnotation(annotation, next);
			}
		}
		variable.setType(createTypeSignature(nextTypeSignature, nextField.getTypeName()));
		variable.setTagBits(nextField.getTagBits());
	}

	private static class TypeParameter {
		public TypeParameter() {
		}
		public List<NdTypeSignature> bounds = new ArrayList<>();
		public char[] identifier = ClassFileToIndexConverter.EMPTY_CHAR_ARRAY;
		public boolean firstBoundIsClass;
	}

	/**
	 * Reads and attaches any generic type parameters at the current start position in the given wrapper. Sets
	 * wrapper.start to the character following the type parameters.
	 *
	 * @throws CoreException
	 */
	private void readTypeParameters(NdBinding type, SignatureWrapper wrapper)
			throws CoreException {
		char[] genericSignature = wrapper.signature;
		if (genericSignature.length == 0 || wrapper.charAtStart() != '<') {
			return;
		}

		List<TypeParameter> typeParameters = new ArrayList<>();

		int indexOfClosingBracket = wrapper.skipAngleContents(wrapper.start) - 1;
		wrapper.start++;
		TypeParameter parameter = null;
		while (wrapper.start < indexOfClosingBracket) {
			int colonPos = CharOperation.indexOf(':', genericSignature, wrapper.start, indexOfClosingBracket);

			if (colonPos > wrapper.start) {
				char[] identifier = CharOperation.subarray(genericSignature, wrapper.start, colonPos);
				parameter = new TypeParameter();
				typeParameters.add(parameter);
				parameter.identifier = identifier;
				wrapper.start = colonPos + 1;
				// The first bound is a class as long as it doesn't start with a double-colon
				parameter.firstBoundIsClass = (wrapper.charAtStart() != ':');
			}

			skipChar(wrapper, ':');

			NdTypeSignature boundSignature = createTypeSignature(wrapper, JAVA_LANG_OBJECT_FIELD_DESCRIPTOR);

			parameter.bounds.add(boundSignature);
		}

		type.allocateTypeParameters(typeParameters.size());
		for (TypeParameter param : typeParameters) {
			NdTypeParameter ndParam = type.createTypeParameter();
			ndParam.setIdentifier(param.identifier);
			ndParam.setFirstBoundIsClass(param.firstBoundIsClass);
			ndParam.allocateBounds(param.bounds.size());
			for (NdTypeSignature bound : param.bounds) {
				ndParam.createBound(bound);
			}
		}

		skipChar(wrapper, '>');
	}

	private char[] readNextFieldDescriptor(SignatureWrapper genericSignature) throws CoreException {
		int endPosition = findEndOfFieldDescriptor(genericSignature);

		char[] result = CharArrayUtils.subarray(genericSignature.signature, genericSignature.start, endPosition);
		genericSignature.start = endPosition;
		return result;
	}

	private int findEndOfFieldDescriptor(SignatureWrapper genericSignature) throws CoreException {
		char[] signature = genericSignature.signature;

		if (signature == null || signature.length == 0) {
			return genericSignature.start;
		}
		int current = genericSignature.start;
		while (current < signature.length) {
			char firstChar = signature[current];
			switch (firstChar) {
				case 'L':
				case 'T': {
					return CharArrayUtils.indexOf(';', signature, current, signature.length) + 1;
				}
				case '[': {
					current++;
					break;
				}
				case 'V':
				case 'B':
				case 'C':
				case 'D':
				case 'F':
				case 'I':
				case 'J':
				case 'S':
				case 'Z':
					return current + 1;
				default:
					throw new CoreException(Package.createStatus("Field descriptor starts with unknown character: " //$NON-NLS-1$
							+ genericSignature.toString()));
			}
		}
		return current;
	}

	/**
	 * Given a generic signature which is positioned at the open brace for method arguments, this returns the number of
	 * method arguments. The start position of the given signature is not modified.
	 */
	private int countMethodArguments(SignatureWrapper genericSignature) {
		SignatureWrapper lookaheadSignature = new SignatureWrapper(genericSignature.signature);
		lookaheadSignature.start = genericSignature.start;
		skipChar(lookaheadSignature, '(');
		int argumentCount = 0;
		while (!lookaheadSignature.atEnd() && !(lookaheadSignature.charAtStart() == ')')) {
			switch (lookaheadSignature.charAtStart()) {
				case 'T': {
					// Skip the 'T' prefix
					lookaheadSignature.nextWord();
					skipChar(lookaheadSignature, ';');
					argumentCount++;
					break;
				}
				case '[': {
					// Skip the '[' prefix
					lookaheadSignature.start++;
					break;
				}
				case 'V':
				case 'B':
				case 'C':
				case 'D':
				case 'F':
				case 'I':
				case 'J':
				case 'S':
				case 'Z':
					argumentCount++;
					lookaheadSignature.start++;
					break;
				case 'L':
					for (;;) {
						lookaheadSignature.nextWord();
						lookaheadSignature.start = lookaheadSignature.skipAngleContents(lookaheadSignature.start);
						char nextChar = lookaheadSignature.charAtStart();
						if (nextChar == ';') {
							break;
						}
						if (nextChar != '.') {
							throw new IllegalStateException(
									"Unknown char in generic signature " + lookaheadSignature.toString()); //$NON-NLS-1$
						}
					}
					skipChar(lookaheadSignature, ';');
					argumentCount++;
					break;
				default:
					throw new IllegalStateException("Generic signature starts with unknown character: " //$NON-NLS-1$
							+ lookaheadSignature.toString());
			}
		}
		return argumentCount;
	}

	/**
	 * Reads a type signature from the given {@link SignatureWrapper}, starting at the character pointed to by
	 * wrapper.start. On return, wrapper.start will point to the first character following the type signature. Returns
	 * null if given an empty signature or the signature for the void type.
	 *
	 * @param genericSignature
	 *            the generic signature to parse
	 * @param fieldDescriptorIfVariable
	 *            the field descriptor to use if the type is a type variable -- or null if unknown (the field descriptor
	 *            for java.lang.Object will be used)
	 * @throws CoreException
	 */
	private NdTypeSignature createTypeSignature(SignatureWrapper genericSignature, char[] fieldDescriptorIfVariable)
			throws CoreException {
		char[] signature = genericSignature.signature;

		if (signature == null || signature.length == 0) {
			return null;
		}

		char firstChar = genericSignature.charAtStart();
		switch (firstChar) {
			case 'T': {
				// Skip the 'T' prefix
				genericSignature.start++;
				NdComplexTypeSignature typeSignature = new NdComplexTypeSignature(getNd());
				char[] fieldDescriptor = fieldDescriptorIfVariable;
				if (fieldDescriptor == null) {
					fieldDescriptor = JAVA_LANG_OBJECT_FIELD_DESCRIPTOR;
				}
				typeSignature.setRawType(createTypeIdFromFieldDescriptor(fieldDescriptor));
				typeSignature.setVariableIdentifier(genericSignature.nextWord());
				// Skip the trailing semicolon
				skipChar(genericSignature, ';');
				return typeSignature;
			}
			case '[': {
				// Skip the '[' prefix
				genericSignature.start++;
				char[] nestedFieldDescriptor = null;
				if (fieldDescriptorIfVariable != null && fieldDescriptorIfVariable.length > 0
						&& fieldDescriptorIfVariable[0] == '[') {
					nestedFieldDescriptor = CharArrayUtils.subarray(fieldDescriptorIfVariable, 1);
				}
				// Determine the array argument type
				NdTypeSignature elementType = createTypeSignature(genericSignature, nestedFieldDescriptor);
				char[] computedFieldDescriptor = CharArrayUtils.concat(ARRAY_FIELD_DESCRIPTOR_PREFIX,
						elementType.getRawType().getFieldDescriptor().getChars());
				NdTypeId rawType = createTypeIdFromFieldDescriptor(computedFieldDescriptor);
				// We encode signatures as though they were a one-argument generic type whose element
				// type is the generic argument.
				NdComplexTypeSignature typeSignature = new NdComplexTypeSignature(getNd());
				typeSignature.setRawType(rawType);
				NdTypeArgument typeArgument = new NdTypeArgument(getNd(), typeSignature);
				typeArgument.setType(elementType);
				return typeSignature;
			}
			case 'V':
				genericSignature.start++;
				return null;
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S':
			case 'Z':
				genericSignature.start++;
				return createTypeIdFromFieldDescriptor(new char[] { firstChar });
			case 'L':
				return parseClassTypeSignature(null, genericSignature);
			case '+':
			case '-':
			case '*':
				throw new CoreException(Package.createStatus("Unexpected wildcard in top-level of generic signature: " //$NON-NLS-1$
						+ genericSignature.toString()));
			default:
				throw new CoreException(Package.createStatus("Generic signature starts with unknown character: " //$NON-NLS-1$
						+ genericSignature.toString()));
		}
	}

	/**
	 * Parses a ClassTypeSignature (as described in section 4.7.9.1 of the Java VM Specification Java SE 8 Edition). The
	 * read pointer should be located just after the identifier. The caller is expected to have already read the field
	 * descriptor for the type.
	 */
	private NdTypeSignature parseClassTypeSignature(NdComplexTypeSignature parentTypeOrNull, SignatureWrapper wrapper)
			throws CoreException {
		char[] identifier = wrapper.nextWord();
		char[] fieldDescriptor;

		if (parentTypeOrNull != null) {
			fieldDescriptor = CharArrayUtils.concat(
					parentTypeOrNull.getRawType().getFieldDescriptorWithoutTrailingSemicolon(),
					INNER_TYPE_SEPARATOR, identifier, FIELD_DESCRIPTOR_SUFFIX);
		} else {
			fieldDescriptor = CharArrayUtils.concat(identifier, FIELD_DESCRIPTOR_SUFFIX);
		}

		char[] genericSignature = wrapper.signature;
		boolean hasGenericArguments = (genericSignature.length > wrapper.start)
				&& genericSignature[wrapper.start] == '<';
		boolean isRawTypeWithNestedClass = genericSignature[wrapper.start] == '.';
		NdTypeId rawType = createTypeIdFromFieldDescriptor(fieldDescriptor);
		NdTypeSignature result = rawType;

		boolean checkForSemicolon = true;
		// Special optimization for signatures with no type annotations, no arrays, and no generic arguments that
		// are not an inner type of a class that can't use this optimization. Basically, if there would be no attributes
		// set on a NdComplexTypeSignature besides what it picks up from its raw type, we just use the raw type.
		if (hasGenericArguments || parentTypeOrNull != null || isRawTypeWithNestedClass) {
			NdComplexTypeSignature typeSignature = new NdComplexTypeSignature(getNd());
			typeSignature.setRawType(rawType);

			if (hasGenericArguments) {
				wrapper.start++;
				while (wrapper.start < genericSignature.length && (genericSignature[wrapper.start] != '>')) {
					NdTypeArgument typeArgument = new NdTypeArgument(getNd(), typeSignature);

					switch (genericSignature[wrapper.start]) {
						case '+': {
							typeArgument.setWildcard(NdTypeArgument.WILDCARD_SUPER);
							wrapper.start++;
							break;
						}
						case '-': {
							typeArgument.setWildcard(NdTypeArgument.WILDCARD_EXTENDS);
							wrapper.start++;
							break;
						}
						case '*': {
							typeArgument.setWildcard(NdTypeArgument.WILDCARD_QUESTION);
							wrapper.start++;
							continue;
						}
					}

					NdTypeSignature nextSignature = createTypeSignature(wrapper, null);
					typeArgument.setType(nextSignature);
				}

				skipChar(wrapper, '>');
			}
			result = typeSignature;

			if (parentTypeOrNull != null) {
				typeSignature.setGenericDeclaringType(parentTypeOrNull);
			}

			if (genericSignature[wrapper.start] == '.') {
				// Don't check for a semicolon if we hit this branch since the recursive call to parseClassTypeSignature
				// will do this
				checkForSemicolon = false;
				// Identifiers shouldn't start with '.'
				skipChar(wrapper, '.');
				result = parseClassTypeSignature(typeSignature, wrapper);
			}
		}

		if (checkForSemicolon) {
			skipChar(wrapper, ';');
		}

		return result;
	}

	private NdTypeId createTypeIdFromFieldDescriptor(char[] typeName) {
		if (typeName == null) {
			return null;
		}
		return this.index.createTypeId(typeName);
	}

	private void initTypeAnnotation(NdTypeAnnotation annotation, IBinaryTypeAnnotation next) {
		int[] typePath = next.getTypePath();
		if (typePath != null && typePath.length > 0) {
			byte[] bytePath = new byte[typePath.length];
			for (int idx = 0; idx < bytePath.length; idx++) {
				bytePath[idx] = (byte)typePath[idx];
			}
			annotation.setPath(bytePath);
		}
		int targetType = next.getTargetType();
		annotation.setTargetType(targetType);
		switch (targetType) {
			case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER:
			case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER:
				annotation.setTargetInfo(next.getTypeParameterIndex());
				break;				
			case AnnotationTargetTypeConstants.CLASS_EXTENDS:
				annotation.setTargetInfo(next.getSupertypeIndex());
				break;
			case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND:
			case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND:
				annotation.setTargetInfo((byte)next.getTypeParameterIndex(), (byte)next.getBoundIndex());
				break;
			case AnnotationTargetTypeConstants.FIELD:
			case AnnotationTargetTypeConstants.METHOD_RETURN:
			case AnnotationTargetTypeConstants.METHOD_RECEIVER:
				break;
			case AnnotationTargetTypeConstants.METHOD_FORMAL_PARAMETER :
				annotation.setTargetInfo(next.getMethodFormalParameterIndex());
				break;
			case AnnotationTargetTypeConstants.THROWS :
				annotation.setTargetInfo(next.getThrowsTypeIndex());
				break;

			default:
				throw new IllegalStateException("Target type not handled " + targetType); //$NON-NLS-1$
		}
		initAnnotation(annotation, next.getAnnotation());
	}

	private void initAnnotation(NdAnnotation annotation, IBinaryAnnotation next) {
		annotation.setType(createTypeIdFromBinaryName(next.getTypeName())); 

		IBinaryElementValuePair[] pairs = next.getElementValuePairs();

		if (pairs != null) {
			annotation.allocateValuePairs(pairs.length);
			for (IBinaryElementValuePair element : pairs) {
				NdAnnotationValuePair nextPair = annotation.createValuePair(element.getName());
				nextPair.setValue(createConstantFromMixedType(element.getValue()));
			}
		}
	}

	private void logInfo(String string) {
		if (ENABLE_LOGGING) {
			Package.logInfo(string);
		}
	}

	private NdTypeId createTypeIdFromBinaryName(char[] binaryName) {
		if (binaryName == null) {
			return null;
		}

		return this.index.createTypeId(JavaNames.binaryNameToFieldDescriptor(binaryName));
	}

	/**
	 *
	 * @param value
	 *            accepts all values returned from {@link IBinaryElementValuePair#getValue()}
	 */
	public NdConstant createConstantFromMixedType(Object value) {
		if (value instanceof Constant) {
			Constant constant = (Constant) value;

			return NdConstant.create(getNd(), constant);
		} else if (value instanceof ClassSignature) {
			ClassSignature signature = (ClassSignature) value;

			char[] binaryName = JavaNames.binaryNameToFieldDescriptor(signature.getTypeName());
			NdTypeSignature typeId = this.index.createTypeId(binaryName);
			return NdConstantClass.create(getNd(), typeId);
		} else if (value instanceof IBinaryAnnotation) {
			IBinaryAnnotation binaryAnnotation = (IBinaryAnnotation) value;

			NdConstantAnnotation constant = new NdConstantAnnotation(getNd());
			initAnnotation(constant.getValue(), binaryAnnotation);
			return constant;
		} else if (value instanceof Object[]) {
			NdConstantArray result = new NdConstantArray(getNd());
			Object[] array = (Object[]) value;

			for (Object next : array) {
				NdConstant nextConstant = createConstantFromMixedType(next);
				nextConstant.setParent(result);
			}
			return result;
		} else if (value instanceof EnumConstantSignature) {
			EnumConstantSignature signature = (EnumConstantSignature) value;

			NdConstantEnum result = NdConstantEnum.create(createTypeIdFromBinaryName(signature.getTypeName()),
					new String(signature.getEnumConstantName()));

			return result;
		}
		throw new IllegalStateException("Unknown constant type " + value.getClass().getName()); //$NON-NLS-1$
	}
}
