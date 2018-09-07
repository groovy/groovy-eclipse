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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldLong;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

public class NdType extends NdBinding {
	public static final FieldManyToOne<NdResourceFile> FILE;
	public static final FieldManyToOne<NdTypeId> TYPENAME;
	public static final FieldManyToOne<NdTypeSignature> SUPERCLASS;
	public static final FieldOneToMany<NdTypeInterface> INTERFACES;
	public static final FieldManyToOne<NdTypeId> DECLARING_TYPE;
	public static final FieldList<NdMethod> METHODS;
	public static final FieldList<NdTypeAnnotation> TYPE_ANNOTATIONS;
	public static final FieldList<NdAnnotation> ANNOTATIONS;
	public static final FieldList<NdVariable> VARIABLES;
	public static final FieldString MISSING_TYPE_NAMES;
	public static final FieldString SOURCE_FILE_NAME;
	public static final FieldString INNER_CLASS_SOURCE_NAME;
	public static final FieldByte FLAGS;
	public static final FieldLong TAG_BITS;
	public static final FieldString ENCLOSING_METHOD;
	/**
	 * Binary name that was recorded in the .class file if different from the binary
	 * name that was determined by the .class's name and location. This is only set for
	 * .class files that have been moved to the wrong folder.
	 */
	public static final FieldString FIELD_DESCRIPTOR_FROM_CLASS;

	@SuppressWarnings("hiding")
	public static final StructDef<NdType> type;

	static {
		type = StructDef.create(NdType.class, NdBinding.type);
		FILE = FieldManyToOne.createOwner(type, NdResourceFile.TYPES);
		TYPENAME = FieldManyToOne.create(type, NdTypeId.TYPES);
		DECLARING_TYPE = FieldManyToOne.create(type, NdTypeId.DECLARED_TYPES);
		INTERFACES = FieldOneToMany.create(type, NdTypeInterface.APPLIES_TO);
		SUPERCLASS = FieldManyToOne.create(type, NdTypeSignature.SUBCLASSES);
		METHODS = FieldList.create(type, NdMethod.type);
		TYPE_ANNOTATIONS = FieldList.create(type, NdTypeAnnotation.type);
		ANNOTATIONS = FieldList.create(type, NdAnnotation.type);
		VARIABLES = FieldList.create(type, NdVariable.type);
		MISSING_TYPE_NAMES = type.addString();
		SOURCE_FILE_NAME = type.addString();
		INNER_CLASS_SOURCE_NAME = type.addString();
		FLAGS = type.addByte();
		TAG_BITS = type.addLong();
		FIELD_DESCRIPTOR_FROM_CLASS = type.addString();
		ENCLOSING_METHOD = type.addString();
		type.done();
	}

	public static final byte FLG_TYPE_ANONYMOUS 	= 0x0001;
	public static final byte FLG_TYPE_LOCAL 		= 0x0002;
	public static final byte FLG_TYPE_MEMBER 		= 0x0004;
	public static final byte FLG_GENERIC_SIGNATURE_PRESENT = 0x0008;

	public NdType(Nd nd, long address) {
		super(nd, address);
	}

	public NdType(Nd nd, NdResourceFile resource) {
		super(nd);

		FILE.put(nd, this.address, resource);
	}

	public NdTypeId getTypeId() {
		return TYPENAME.get(getNd(), this.address);
	}

	public void setTypeId(NdTypeId typeId) {
		TYPENAME.put(getNd(), this.address, typeId);
	}

	public void setFile(NdResourceFile file) {
		FILE.put(getNd(), this.address, file);
	}

	public NdResourceFile getFile() {
		return FILE.get(getNd(), this.address);
	}

	/**
	 * Sets the source name for this type.
	 */
	public void setSourceNameOverride(char[] sourceName) {
		char[] oldSourceName = getSourceName();
		if (!CharArrayUtils.equals(oldSourceName, sourceName)) {
			INNER_CLASS_SOURCE_NAME.put(getNd(), this.address, sourceName);
		}
	}

	public IString getSourceNameOverride() {
		return INNER_CLASS_SOURCE_NAME.get(getNd(), this.address);
	}

	public long getResourceAddress() {
		return FILE.getAddress(getNd(), this.address);
	}

	public void setSuperclass(NdTypeSignature superclassTypeName) {
		SUPERCLASS.put(getNd(), this.address, superclassTypeName);
	}

	public NdTypeSignature getSuperclass() {
		return SUPERCLASS.get(getNd(), this.address);
	}

	public List<NdTypeInterface> getInterfaces() {
		return INTERFACES.asList(getNd(), this.address);
	}

	public NdResourceFile getResourceFile() {
		return FILE.get(getNd(), this.address);
	}

	/**
	 * @param createTypeIdFromBinaryName
	 */
	public void setDeclaringType(NdTypeId createTypeIdFromBinaryName) {
		DECLARING_TYPE.put(getNd(), this.address, createTypeIdFromBinaryName);
	}

	public NdTypeId getDeclaringType() {
		return DECLARING_TYPE.get(getNd(), this.address);
	}

	/**
	 * Sets the missing type names (if any) for this class. The names are encoded in a comma-separated list.
	 */
	public void setMissingTypeNames(char[] contents) {
		MISSING_TYPE_NAMES.put(getNd(), this.address, contents);
	}

	/**
	 * Returns the missing type names as a comma-separated list
	 */
	public IString getMissingTypeNames() {
		return MISSING_TYPE_NAMES.get(getNd(), this.address);
	}

	public void setSourceFileName(char[] sourceFileName) {
		SOURCE_FILE_NAME.put(getNd(), this.address, sourceFileName);
	}

	public IString getSourceFileName() {
		return SOURCE_FILE_NAME.get(getNd(), this.address);
	}

	public void setAnonymous(boolean anonymous) {
		setFlag(FLG_TYPE_ANONYMOUS, anonymous);
	}

	public void setIsLocal(boolean local) {
		setFlag(FLG_TYPE_LOCAL, local);
	}

	public void setIsMember(boolean member) {
		setFlag(FLG_TYPE_MEMBER, member);
	}

	public boolean isAnonymous() {
		return getFlag(FLG_TYPE_ANONYMOUS);
	}

	public boolean isLocal() {
		return getFlag(FLG_TYPE_LOCAL);
	}

	public boolean isMember() {
		return getFlag(FLG_TYPE_MEMBER);
	}

	public void setFlag(byte flagConstant, boolean value) {
		int oldFlags = FLAGS.get(getNd(), this.address);
		int newFlags =  ((oldFlags & ~flagConstant) | (value ? flagConstant : 0));
		FLAGS.put(getNd(), this.address, (byte)newFlags);
	}

	public boolean getFlag(byte flagConstant) {
		return (FLAGS.get(getNd(), this.address) & flagConstant) != 0;
	}

	public char[] getSourceName() {
		IString sourceName = getSourceNameOverride();
		if (sourceName.length() != 0) {
			return sourceName.getChars();
		}
		char[] simpleName = getTypeId().getSimpleNameCharArray();
		return JavaNames.simpleNameToSourceName(simpleName);
	}

	public List<NdVariable> getVariables() {
		return VARIABLES.asList(getNd(), this.address);
	}

	@Override
	public List<NdTypeParameter> getTypeParameters() {
		return TYPE_PARAMETERS.asList(getNd(), this.address);
	}

	public List<NdTypeAnnotation> getTypeAnnotations() {
		return TYPE_ANNOTATIONS.asList(getNd(), this.address);
	}

	public List<NdAnnotation> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}

	public NdAnnotation createAnnotation() {
		return ANNOTATIONS.append(getNd(), getAddress());
	}

	public void allocateAnnotations(int length) {
		ANNOTATIONS.allocate(getNd(), getAddress(), length);
	}

	/**
	 * Returns the list of methods, sorted by ascending method name (selector + descriptor).
	 */
	public List<NdMethod> getMethods() {
		return METHODS.asList(getNd(), this.address);
	}

	/**
	 * Returns the list of methods, in declaration order.
	 */
	public List<NdMethod> getMethodsInDeclarationOrder() {
		List<NdMethod> unsorted = getMethods();
		NdMethod[] sorted = new NdMethod[unsorted.size()];
		for (NdMethod next : unsorted) {
			int pos = next.getDeclarationPosition();

			if (pos < 0 || pos >= sorted.length) {
				throw getNd().describeProblem()
					.addProblemAddress(NdMethod.DECLARATION_POSITION, next.getAddress())
					.build("Method " + next.getMethodName().getString() + " reports invalid position of " + pos); //$NON-NLS-1$//$NON-NLS-2$
			}

			NdMethod oldMethodAtThisPosition = sorted[pos];
			if (oldMethodAtThisPosition != null) {
				throw getNd().describeProblem()
					.addProblemAddress(NdMethod.DECLARATION_POSITION, next.getAddress())
					.addProblemAddress(NdMethod.DECLARATION_POSITION, oldMethodAtThisPosition.getAddress())
					.build("Method " + oldMethodAtThisPosition.getMethodName().getString()  //$NON-NLS-1$
							+ " and method " + next.getMethodName().getString() + " both claim to be at position "  //$NON-NLS-1$//$NON-NLS-2$
							+ pos);
			}
			sorted[pos] = next;
		}

		return Arrays.asList(sorted);
	}

	@Override
	public String toString() {
		try {
			return "class " + new String(getSourceName()); //$NON-NLS-1$
		} catch (RuntimeException e) {
			return super.toString();
		}
	}

	public void setTagBits(long tagBits) {
		TAG_BITS.put(getNd(), this.address, tagBits);
	}

	public long getTagBits() {
		return TAG_BITS.get(getNd(), this.address);
	}

	public void setFieldDescriptorFromClass(char[] fieldDescriptorFromClass) {
		FIELD_DESCRIPTOR_FROM_CLASS.put(getNd(), this.address, fieldDescriptorFromClass);
	}

	/**
	 * Returns the field descriptor for this type, based on the binary type information stored in the
	 * .class file itself. Note that this may differ from the field descriptor of this type's typeId in
	 * the event that the .class file has been moved.
	 */
	public IString getFieldDescriptor() {
		IString descriptorFromClass = FIELD_DESCRIPTOR_FROM_CLASS.get(getNd(), this.address);
		if (descriptorFromClass.length() == 0) {
			return getTypeId().getFieldDescriptor();
		}
		return descriptorFromClass;
	}

	public NdTypeAnnotation createTypeAnnotation() {
		return TYPE_ANNOTATIONS.append(getNd(), getAddress());
	}

	public void allocateTypeAnnotations(int length) {
		TYPE_ANNOTATIONS.allocate(getNd(), getAddress(), length);
	}

	public NdVariable createVariable() {
		return VARIABLES.append(getNd(), getAddress());
	}

	public void allocateVariables(int length) {
		VARIABLES.allocate(getNd(), getAddress(), length);
	}

	public void allocateMethods(int length) {
		METHODS.allocate(getNd(), getAddress(), length);
	}

	public NdMethod createMethod() {
		return METHODS.append(getNd(), getAddress());
	}

	public void setDeclaringMethod(char[] enclosingMethod) {
		ENCLOSING_METHOD.put(getNd(), getAddress(), enclosingMethod);
	}

	public IString getDeclaringMethod() {
		return ENCLOSING_METHOD.get(getNd(), getAddress());
	}
}
