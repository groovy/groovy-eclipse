/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
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
package org.eclipse.jdt.internal.core.nd.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Corresponds roughly to a JavaTypeSignature, as described in section 4.7.9.1 of the Java VM spec version 4, with the
 * addition of annotations and backpointers to locations where the type is used.
 * <p>
 * Holds back-pointers to all the entities that refer to the name, along with pointers to all classes that have this
 * name. Note that this isn't the class declaration itself. The same index can hold multiple jar files, some of which
 * may contain classes with the same name. All classes that use this fully-qualified name point to the same
 * {@link NdTypeSignature}.
 * <p>
 * Other entities should refer to a type via its TypeId if there is any possiblity that the type may change based on the
 * classpath. It should refer to the type directly if there is no possibility for a type lookup. For example, nested
 * classes refer to their enclosing class directly since they live in the same file and there is no possibility for the
 * enclosing class to change based on the classpath. Classes refer to their base class via its TypeId since the parent
 * class might live in a different jar and need to be resolved on the classpath.
 */
public abstract class NdTypeSignature extends NdNode {
	public static final FieldOneToMany<NdType> SUBCLASSES;
	public static final FieldOneToMany<NdAnnotation> ANNOTATIONS_OF_THIS_TYPE;
	public static final FieldOneToMany<NdTypeInterface> IMPLEMENTATIONS;
	public static final FieldOneToMany<NdVariable> VARIABLES_OF_TYPE;
	public static final FieldOneToMany<NdConstantClass> USED_AS_CONSTANT;
	public static final FieldOneToMany<NdConstantEnum> USED_AS_ENUM_CONSTANT;
	public static final FieldOneToMany<NdTypeArgument> USED_AS_TYPE_ARGUMENT;
	public static final FieldOneToMany<NdTypeBound> USED_AS_TYPE_BOUND;
	public static final FieldOneToMany<NdMethodParameter> USED_AS_METHOD_ARGUMENT;
	public static final FieldOneToMany<NdMethodException> USED_AS_EXCEPTION;
	public static final FieldOneToMany<NdMethod> USED_AS_RETURN_TYPE;

	@SuppressWarnings("hiding")
	public static StructDef<NdTypeSignature> type;

	static {
		type = StructDef.createAbstract(NdTypeSignature.class, NdNode.type);
		SUBCLASSES = FieldOneToMany.create(type, NdType.SUPERCLASS);
		ANNOTATIONS_OF_THIS_TYPE = FieldOneToMany.create(type, NdAnnotation.ANNOTATION_TYPE);
		IMPLEMENTATIONS = FieldOneToMany.create(type, NdTypeInterface.IMPLEMENTS);
		VARIABLES_OF_TYPE = FieldOneToMany.create(type, NdVariable.TYPE);
		USED_AS_CONSTANT = FieldOneToMany.create(type, NdConstantClass.VALUE);
		USED_AS_ENUM_CONSTANT = FieldOneToMany.create(type, NdConstantEnum.ENUM_TYPE);
		USED_AS_TYPE_ARGUMENT = FieldOneToMany.create(type, NdTypeArgument.TYPE_SIGNATURE);
		USED_AS_TYPE_BOUND = FieldOneToMany.create(type, NdTypeBound.TYPE);
		USED_AS_METHOD_ARGUMENT = FieldOneToMany.create(type, NdMethodParameter.ARGUMENT_TYPE);
		USED_AS_EXCEPTION = FieldOneToMany.create(type, NdMethodException.EXCEPTION_TYPE);
		USED_AS_RETURN_TYPE = FieldOneToMany.create(type, NdMethod.RETURN_TYPE);
		type.useStandardRefCounting().done();
	}

	public NdTypeSignature(Nd nd, long address) {
		super(nd, address);
	}

	public NdTypeSignature(Nd nd) {
		super(nd);
	}

	public List<NdType> getSubclasses() {
		return SUBCLASSES.asList(getNd(), this.address);
	}

	public List<NdTypeInterface> getImplementations() {
		return IMPLEMENTATIONS.asList(getNd(), this.address);
	}

	/**
	 * Returns all subclasses (for classes) and implementations (for interfaces) of this type
	 */
	public List<NdType> getSubTypes() {
		List<NdType> result = new ArrayList<>();
		result.addAll(getSubclasses());

		for (NdTypeInterface next : getImplementations()) {
			result.add(next.getImplementation());
		}

		return result;
	}

	/**
	 * Returns the raw version of this type, if one exists. That is, the version of this type
	 * without any generic arguments or annotations, which the java runtime sees. Returns null
	 * of this signature doesn't have a raw type, for example if it is a type variable.
	 */
	public abstract NdTypeId getRawType();

	public final void getSignature(CharArrayBuffer result) {
		getSignature(result, true);
	}

	public abstract void getSignature(CharArrayBuffer result, boolean includeTrailingSemicolon);

	/**
	 * Returns true iff this is an array type signature (ie: that getArrayDimensionType() will return a non-null
	 * answer). Note that this only returns true for the type signature that holds the reference to the array dimension
	 * type. The raw type for that signature will return false, even though it has a field descriptor starting with '['.
	 * <p>
	 * In other words:
	 *
	 * <pre>
	 * NdVariable someVariable = getSomeVariableWithAnArrayType()
	 * System.out.println(someVariable.getType().isArrayType()); // true
	 * System.out.println(someVariable.getType().getRawType().isArrayType()); // false
	 * </pre>
	 */
	public abstract boolean isArrayType();

	public abstract boolean isTypeVariable();

	/**
	 * Returns the chain of declaring generic types. The first element in the chain is a top-level type and the
	 * receiver is the last element in the chain.
	 */
	public abstract List<NdTypeSignature> getDeclaringTypeChain();

	/**
	 * If the receiver is an array type, it returns the signature of the array's next dimension. Returns null if
	 * this is not an array type.
	 */
	public abstract NdTypeSignature getArrayDimensionType();

	/**
	 * Returns the type arguments for this type signature, if any. Returns the empty list if none.
	 */
	public abstract List<NdTypeArgument> getTypeArguments();

	@Override
	public String toString() {
		try {
			CharArrayBuffer result = new CharArrayBuffer();
			getSignature(result);
			return result.toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
