/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

/**
 * Represents the fully-qualified signature a method. Holds back-pointers to all the entities that refer to the name,
 * along with pointers to all methods that have this fully-qualified name. Note that this isn't the class declaration
 * itself. If there are multiple jar files containing a class of the same fully-qualified name, there may also be
 * multiple methods with the same method ID.
 */
public class NdMethodId extends NdNode {
	public static final FieldSearchKey<JavaIndex> METHOD_NAME;
	public static final FieldOneToMany<NdMethod> METHODS;
	public static final FieldOneToMany<NdType> DECLARED_TYPES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdMethodId> type;

	static {
		type = StructDef.create(NdMethodId.class, NdNode.type);
		METHOD_NAME = FieldSearchKey.create(type, JavaIndex.METHODS);
		METHODS = FieldOneToMany.create(type, NdMethod.METHOD_ID, 2);
		DECLARED_TYPES = FieldOneToMany.create(type, NdType.DECLARING_METHOD);

		type.useStandardRefCounting().done();
	}

	public NdMethodId(Nd nd, long address) {
		super(nd, address);
	}

	/**
	 *
	 * @param nd
	 * @param methodIdentifier a field descriptor for the method type followed by a "#" followed by a method selector
	 *  followed by method descriptor. For example, "Lorg/eclipse/MyClass#foo()Ljava/lang/Object;V"
	 */
	public NdMethodId(Nd nd, char[] methodIdentifier) {
		super(nd);

		METHOD_NAME.put(nd, this.address, methodIdentifier);
	}

	public List<NdType> getDeclaredTypes() {
		return DECLARED_TYPES.asList(getNd(), this.address);
	}

	/**
	 * Returns the field descriptor for the type (without a trailing ';') followed by a # followed by the method
	 * selector followed by the method descriptor. For example, "Lorg/eclipse/MyClass#foo()Ljava/lang/Object;V"
	 */
	public IString getMethodName() {
		return METHOD_NAME.get(getNd(), this.address);
	}

	public char[] getSelector() {
		char[] name = getMethodName().getChars();
		int selectorStart = CharArrayUtils.indexOf('#', name) + 1;
		int selectorEnd = CharArrayUtils.indexOf('(', name, selectorStart, name.length);
		if (selectorEnd == -1) {
			selectorEnd = name.length;
		}
		return CharArrayUtils.subarray(name, selectorStart, selectorEnd);
	}

	public boolean isConstructor() {
		return org.eclipse.jdt.internal.compiler.classfmt.JavaBinaryNames.isConstructor(getSelector());
	}

	public char[] getMethodDescriptor() {
		char[] name = getMethodName().getChars();
		int descriptorStart = CharArrayUtils.indexOf('(', name, 0, name.length);
		return CharArrayUtils.subarray(name, descriptorStart, name.length);
	}

	public boolean isClInit() {
		return org.eclipse.jdt.internal.compiler.classfmt.JavaBinaryNames.isClinit(getSelector());
	}

	public String toString() {
		try {
			return new String(getSelector());
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
