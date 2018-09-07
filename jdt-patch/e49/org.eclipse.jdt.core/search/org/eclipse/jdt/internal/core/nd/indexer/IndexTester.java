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

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

public class IndexTester {

	private static final class TypeAnnotationWrapper {
		private IBinaryTypeAnnotation annotation;

		public TypeAnnotationWrapper(IBinaryTypeAnnotation next) {
			this.annotation = next;
		}

		@Override
		public int hashCode() {
			int hashCode;
			int[] typePath = this.annotation.getTypePath();

			hashCode = Arrays.hashCode(typePath);
			hashCode = hashCode * 31 + this.annotation.getTargetType();
			hashCode = hashCode * 31 + this.annotation.getTypeParameterIndex();
			return hashCode;
		}

		@Override
		public String toString() {
			return this.annotation.toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != TypeAnnotationWrapper.class) {
				return false;
			}

			TypeAnnotationWrapper wrapper = (TypeAnnotationWrapper) obj;
			IBinaryTypeAnnotation otherAnnotation = wrapper.annotation;

			int[] typePath = this.annotation.getTypePath();
			int[] otherTypePath = otherAnnotation.getTypePath();

			if (!Arrays.equals(typePath, otherTypePath)) {
				return false;
			}

			if (this.annotation.getTargetType() != otherAnnotation.getTargetType()) {
				return false;
			}

			if (this.annotation.getBoundIndex() != otherAnnotation.getBoundIndex()) {
				return false;
			}

			if (this.annotation.getMethodFormalParameterIndex() != otherAnnotation.getMethodFormalParameterIndex()) {
				return false;
			}

			if (this.annotation.getSupertypeIndex() != otherAnnotation.getSupertypeIndex()) {
				return false;
			}

			if (this.annotation.getThrowsTypeIndex() != otherAnnotation.getThrowsTypeIndex()) {
				return false;
			}

			if (this.annotation.getTypeParameterIndex() != otherAnnotation.getTypeParameterIndex()) {
				return false;
			}

			return IndexTester.isEqual(this.annotation.getAnnotation(), otherAnnotation.getAnnotation());
		}
	}

	public static void testType(IBinaryType expected, IBinaryType actual) {
		String contextPrefix = safeString(actual.getName());

		IBinaryTypeAnnotation[] expectedTypeAnnotations = expected.getTypeAnnotations();
		IBinaryTypeAnnotation[] actualTypeAnnotations = actual.getTypeAnnotations();

		compareTypeAnnotations(contextPrefix, expectedTypeAnnotations, actualTypeAnnotations);

		IBinaryAnnotation[] expectedBinaryAnnotations = expected.getAnnotations();
		IBinaryAnnotation[] actualBinaryAnnotations = actual.getAnnotations();

		compareAnnotations(contextPrefix, expectedBinaryAnnotations, actualBinaryAnnotations);

		compareGenericSignatures(contextPrefix + ": The generic signature did not match", //$NON-NLS-1$
				expected.getGenericSignature(), actual.getGenericSignature());

		assertEquals(contextPrefix + ": The enclosing method name did not match", expected.getEnclosingMethod(), //$NON-NLS-1$
				actual.getEnclosingMethod());
		assertEquals(contextPrefix + ": The enclosing method name did not match", expected.getEnclosingTypeName(), //$NON-NLS-1$
				actual.getEnclosingTypeName());

		IBinaryField[] expectedFields = expected.getFields();
		IBinaryField[] actualFields = actual.getFields();

		if (expectedFields != actualFields) {
			if (expectedFields == null && actualFields != null) {
				throw new IllegalStateException(contextPrefix + "Expected fields was null -- actual fields were not"); //$NON-NLS-1$
			}
			if (expectedFields.length != actualFields.length) {
				throw new IllegalStateException(
						contextPrefix + "The expected and actual number of fields did not match"); //$NON-NLS-1$
			}

			for (int fieldIdx = 0; fieldIdx < actualFields.length; fieldIdx++) {
				compareFields(contextPrefix, expectedFields[fieldIdx], actualFields[fieldIdx]);
			}
		}

		// Commented this out because the "expected" values often appear to be invalid paths when the "actual"
		// ones are correct.
		assertEquals("The file name did not match", expected.getFileName(), actual.getFileName()); //$NON-NLS-1$
		assertEquals("The interface names did not match", expected.getInterfaceNames(), actual.getInterfaceNames()); //$NON-NLS-1$

		// Member types are not expected to match during indexing since the index uses discovered cross-references,
		// not the member types encoded in the .class file.
		// expected.getMemberTypes() != actual.getMemberTypes()

		IBinaryMethod[] expectedMethods = expected.getMethods();
		IBinaryMethod[] actualMethods = actual.getMethods();

		if (expectedMethods != actualMethods) {
			if (expectedMethods == null || actualMethods == null) {
				throw new IllegalStateException("One of the method arrays was null"); //$NON-NLS-1$
			}

			if (expectedMethods.length != actualMethods.length) {
				throw new IllegalStateException("The number of methods didn't match"); //$NON-NLS-1$
			}

			for (int i = 0; i < actualMethods.length; i++) {
				IBinaryMethod actualMethod = actualMethods[i];
				IBinaryMethod expectedMethod = expectedMethods[i];

				compareMethods(contextPrefix, expectedMethod, actualMethod);
			}
		}

		assertEquals("The missing type names did not match", expected.getMissingTypeNames(), //$NON-NLS-1$
				actual.getMissingTypeNames());
		assertEquals("The modifiers don't match", expected.getModifiers(), actual.getModifiers()); //$NON-NLS-1$
		assertEquals("The names don't match.", expected.getName(), actual.getName()); //$NON-NLS-1$
		assertEquals("The source name doesn't match", expected.getSourceName(), actual.getSourceName()); //$NON-NLS-1$
		assertEquals("The superclass name doesn't match", expected.getSuperclassName(), actual.getSuperclassName()); //$NON-NLS-1$
		assertEquals("The tag bits don't match.", expected.getTagBits(), actual.getTagBits()); //$NON-NLS-1$

		compareTypeAnnotations(contextPrefix, expected.getTypeAnnotations(), actual.getTypeAnnotations());
	}

	private static <T> void assertEquals(String message, T o1, T o2) {
		if (!isEqual(o1, o2)) {
			throw new IllegalStateException(message + ": expected = " + getString(o1) + ", actual = " + getString(o2)); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private static String getString(Object object) {
		if (object instanceof char[]) {
			char[] charArray = (char[]) object;

			return new String(charArray);
		}
		return object.toString();
	}

	static <T> boolean isEqual(T o1, T o2) {
		if (o1 == o2) {
			return true;
		}

		if (o1 == null || o2 == null) {
			return false;
		}

		if (o1 instanceof ClassSignature) {
			if (!(o2 instanceof ClassSignature)) {
				return false;
			}

			ClassSignature sig1 = (ClassSignature) o1;
			ClassSignature sig2 = (ClassSignature) o2;

			return Arrays.equals(sig1.getTypeName(), sig2.getTypeName());
		}

		if (o1 instanceof IBinaryAnnotation) {
			IBinaryAnnotation binaryAnnotation = (IBinaryAnnotation) o1;
			IBinaryAnnotation otherBinaryAnnotation = (IBinaryAnnotation) o2;
			IBinaryElementValuePair[] elementValuePairs = binaryAnnotation.getElementValuePairs();
			IBinaryElementValuePair[] otherElementValuePairs = otherBinaryAnnotation.getElementValuePairs();

			if (elementValuePairs.length != otherElementValuePairs.length) {
				return false;
			}

			for (int idx = 0; idx < elementValuePairs.length; idx++) {
				IBinaryElementValuePair next = elementValuePairs[idx];
				IBinaryElementValuePair otherNext = otherElementValuePairs[idx];

				char[] nextName = next.getName();
				char[] otherNextName = otherNext.getName();

				if (!Arrays.equals(nextName, otherNextName)) {
					return false;
				}

				if (!isEqual(next.getValue(), otherNext.getValue())) {
					return false;
				}
			}
			return true;
		}

		if (o1 instanceof IBinaryTypeAnnotation) {
			IBinaryTypeAnnotation binaryAnnotation = (IBinaryTypeAnnotation)o1;
			IBinaryTypeAnnotation otherBinaryAnnotation = (IBinaryTypeAnnotation)o2;

			return new TypeAnnotationWrapper(binaryAnnotation).equals(new TypeAnnotationWrapper(otherBinaryAnnotation));
		}

		if (o1 instanceof Constant) {
			if (!(o2 instanceof Constant)) {
				return false;
			}

			if (o1 instanceof DoubleConstant && o2 instanceof DoubleConstant) {
				DoubleConstant d1 = (DoubleConstant) o1;
				DoubleConstant d2 = (DoubleConstant) o2;

				if (Double.isNaN(d1.doubleValue()) && Double.isNaN(d2.doubleValue())) {
					return true;
				}
			}

			if (o1 instanceof FloatConstant && o2 instanceof FloatConstant) {
				FloatConstant d1 = (FloatConstant) o1;
				FloatConstant d2 = (FloatConstant) o2;

				if (Float.isNaN(d1.floatValue()) && Float.isNaN(d2.floatValue())) {
					return true;
				}
			}

			Constant const1 = (Constant) o1;
			Constant const2 = (Constant) o2;

			return const1.hasSameValue(const2);
		}

		if (o1 instanceof EnumConstantSignature) {
			if (!(o2 instanceof EnumConstantSignature)) {
				return false;
			}

			EnumConstantSignature enum1 = (EnumConstantSignature) o1;
			EnumConstantSignature enum2 = (EnumConstantSignature) o2;

			return Arrays.equals(enum1.getEnumConstantName(), enum2.getEnumConstantName())
					&& Arrays.equals(enum1.getTypeName(), enum2.getTypeName());
		}

		if (o1 instanceof char[]) {
			char[] c1 = (char[]) o1;
			char[] c2 = (char[]) o2;

			return CharArrayUtils.equals(c1, c2);
		}

		if (o1 instanceof char[][]) {
			char[][] c1 = (char[][]) o1;
			char[][] c2 = (char[][]) o2;

			return CharArrayUtils.equals(c1, c2);
		}

		if (o1 instanceof char[][][]) {
			char[][][] c1 = (char[][][]) o1;
			char[][][] c2 = (char[][][]) o2;

			if (c1.length != c2.length) {
				return false;
			}

			for (int i = 0; i < c1.length; i++) {
				if (!isEqual(c1[i], c2[i])) {
					return false;
				}
			}
			return true;
		}

		if (o1 instanceof Object[]) {
			Object[] a1 = (Object[]) o1;
			Object[] a2 = (Object[]) o2;

			if (a1.length != a2.length) {
				return false;
			}

			for (int idx = 0; idx < a1.length; idx++) {
				if (!isEqual(a1[idx], a2[idx])) {
					return false;
				}
			}
			return true;
		}

		return Objects.equals(o1, o2);
	}

	private static void compareMethods(String contextPrefix, IBinaryMethod expectedMethod, IBinaryMethod actualMethod) {
		contextPrefix = contextPrefix + "." + safeString(expectedMethod.getSelector()); //$NON-NLS-1$
		compareAnnotations(contextPrefix, expectedMethod.getAnnotations(), actualMethod.getAnnotations());

		assertEquals(contextPrefix + ": The argument names didn't match.", expectedMethod.getArgumentNames(), //$NON-NLS-1$
				actualMethod.getArgumentNames());

		assertEquals(contextPrefix + ": The default values didn't match.", expectedMethod.getDefaultValue(), //$NON-NLS-1$
				actualMethod.getDefaultValue());

		assertEquals(contextPrefix + ": The exception type names did not match.", //$NON-NLS-1$
				expectedMethod.getExceptionTypeNames(), actualMethod.getExceptionTypeNames());

		compareGenericSignatures(contextPrefix + ": The method's generic signature did not match", //$NON-NLS-1$
				expectedMethod.getGenericSignature(), actualMethod.getGenericSignature());

		assertEquals(contextPrefix + ": The method descriptors did not match.", expectedMethod.getMethodDescriptor(), //$NON-NLS-1$
				actualMethod.getMethodDescriptor());
		assertEquals(contextPrefix + ": The modifiers didn't match.", expectedMethod.getModifiers(), //$NON-NLS-1$
				actualMethod.getModifiers());

		char[] classFileName = "".toCharArray(); //$NON-NLS-1$
		int minAnnotatedParameters = Math.min(expectedMethod.getAnnotatedParametersCount(),
				actualMethod.getAnnotatedParametersCount());
		for (int idx = 0; idx < minAnnotatedParameters; idx++) {
			compareAnnotations(contextPrefix, expectedMethod.getParameterAnnotations(idx, classFileName),
					actualMethod.getParameterAnnotations(idx, classFileName));
		}
		for (int idx = minAnnotatedParameters; idx < expectedMethod.getAnnotatedParametersCount(); idx++) {
			compareAnnotations(contextPrefix, new IBinaryAnnotation[0],
					expectedMethod.getParameterAnnotations(idx, classFileName));
		}
		for (int idx = minAnnotatedParameters; idx < actualMethod.getAnnotatedParametersCount(); idx++) {
			compareAnnotations(contextPrefix, new IBinaryAnnotation[0],
					actualMethod.getParameterAnnotations(idx, classFileName));
		}

		assertEquals(contextPrefix + ": The selectors did not match", expectedMethod.getSelector(), //$NON-NLS-1$
				actualMethod.getSelector());
		assertEquals(contextPrefix + ": The tag bits did not match", expectedMethod.getTagBits(), //$NON-NLS-1$
				actualMethod.getTagBits());

		compareTypeAnnotations(contextPrefix, expectedMethod.getTypeAnnotations(), actualMethod.getTypeAnnotations());
	}

	/**
	 * The index always provides complete generic signatures whereas some or all of the generic signature is optional
	 * for class files, so the generic signatures are expected to differ in certain situations.
	 */
	private static void compareGenericSignatures(String message, char[] expected, char[] actual) {
		assertEquals(message, expected, actual);
	}

	private static void compareTypeAnnotations(String contextPrefix, IBinaryTypeAnnotation[] expectedTypeAnnotations,
			IBinaryTypeAnnotation[] actualTypeAnnotations) {
		if (expectedTypeAnnotations == null) {
			if (actualTypeAnnotations != null) {
				throw new IllegalStateException(contextPrefix + ": Expected null for the annotation list but found: " //$NON-NLS-1$
						+ actualTypeAnnotations.toString());
			}
			return;
		}

		assertEquals(contextPrefix + ": The expected and actual number of type annotations did not match", //$NON-NLS-1$
				expectedTypeAnnotations.length, actualTypeAnnotations.length);

		for (int idx = 0; idx < expectedTypeAnnotations.length; idx++) {
			assertEquals(contextPrefix + ": Type annotation number " + idx + " did not match", //$NON-NLS-1$//$NON-NLS-2$
					expectedTypeAnnotations[idx], actualTypeAnnotations[idx]);
		}
	}

	private static void compareAnnotations(String contextPrefix, IBinaryAnnotation[] expectedBinaryAnnotations,
			IBinaryAnnotation[] actualBinaryAnnotations) {
		if (expectedBinaryAnnotations == null || expectedBinaryAnnotations.length == 0) {
			if (actualBinaryAnnotations != null && actualBinaryAnnotations.length != 0) {
				throw new IllegalStateException(contextPrefix + ": Expected null for the binary annotations"); //$NON-NLS-1$
			} else {
				return;
			}
		}
		if (actualBinaryAnnotations == null) {
			throw new IllegalStateException(contextPrefix + ": Actual null for the binary annotations"); //$NON-NLS-1$
		}
		if (expectedBinaryAnnotations.length != actualBinaryAnnotations.length) {
			throw new IllegalStateException(
					contextPrefix + ": The expected and actual number of annotations differed. Expected: " //$NON-NLS-1$
							+ expectedBinaryAnnotations.length + ", actual: " + actualBinaryAnnotations.length); //$NON-NLS-1$
		}

		for (int idx = 0; idx < expectedBinaryAnnotations.length; idx++) {
			if (!isEqual(expectedBinaryAnnotations[idx], actualBinaryAnnotations[idx])) {
				throw new IllegalStateException(contextPrefix + ": An annotation had an unexpected value"); //$NON-NLS-1$
			}
		}
	}

	private static void compareFields(String contextPrefix, IBinaryField field1, IBinaryField field2) {
		contextPrefix = contextPrefix + "." + safeString(field1.getName()); //$NON-NLS-1$
		compareAnnotations(contextPrefix, field1.getAnnotations(), field2.getAnnotations());
		assertEquals(contextPrefix + ": Constants not equal", field1.getConstant(), field2.getConstant()); //$NON-NLS-1$
		compareGenericSignatures(contextPrefix + ": The generic signature did not match", field1.getGenericSignature(), //$NON-NLS-1$
				field2.getGenericSignature());
		assertEquals(contextPrefix + ": The modifiers did not match", field1.getModifiers(), field2.getModifiers()); //$NON-NLS-1$
		assertEquals(contextPrefix + ": The tag bits did not match", field1.getTagBits(), field2.getTagBits()); //$NON-NLS-1$
		assertEquals(contextPrefix + ": The names did not match", field1.getName(), field2.getName()); //$NON-NLS-1$

		compareTypeAnnotations(contextPrefix, field1.getTypeAnnotations(), field2.getTypeAnnotations());
		assertEquals(contextPrefix + ": The type names did not match", field1.getTypeName(), field2.getTypeName()); //$NON-NLS-1$
	}

	private static String safeString(char[] name) {
		if (name == null) {
			return "<unnamed>"; //$NON-NLS-1$
		}
		return new String(name);
	}

}
