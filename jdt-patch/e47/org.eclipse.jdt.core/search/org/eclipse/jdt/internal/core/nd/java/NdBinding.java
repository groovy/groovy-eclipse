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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldInt;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Base class for bindings in the {@link Nd}.
 */
public abstract class NdBinding extends NdNode implements IAdaptable {
	public static final FieldInt MODIFIERS;
	public static final FieldOneToMany<NdTypeParameter> TYPE_PARAMETERS;
	public static final FieldManyToOne<NdResourceFile> FILE;
	public static final FieldOneToMany<NdVariable> VARIABLES;

	@SuppressWarnings("hiding")
	public static final StructDef<NdBinding> type;

	static {
		type = StructDef.create(NdBinding.class, NdNode.type);
		MODIFIERS = type.addInt();
		TYPE_PARAMETERS = FieldOneToMany.create(type, NdTypeParameter.PARENT);
		FILE = FieldManyToOne.createOwner(type, NdResourceFile.ALL_NODES);
		VARIABLES = FieldOneToMany.create(type, NdVariable.PARENT);
		type.done();
	}

	public NdBinding(Nd nd, long address) {
		super(nd, address);
	}

	public NdBinding(Nd nd, NdResourceFile resource) {
		super(nd);

		FILE.put(nd, this.address, resource);
	}

	public List<NdVariable> getVariables() {
		return VARIABLES.asList(getNd(), this.address);
	}

	/**
	 * Tests whether this binding has one of the flags defined in {@link Flags}
	 */
	public boolean hasModifier(int toTest) {
		return (MODIFIERS.get(getNd(), this.address) & toTest) != 0;
	}

	/**
	 * Sets the modifiers for this binding (defined in {@link Flags})
	 */
	public void setModifiers(int toSet) {
		MODIFIERS.put(getNd(), this.address, toSet);
	}

	public int getModifiers() {
		return MODIFIERS.get(getNd(), this.address);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isAssignableFrom(NdBinding.class))
			return this;

		return null;
	}

	public final int getBindingConstant() {
		return getNodeType();
	}

	public void setFile(NdResourceFile file) {
		FILE.put(getNd(), this.address, file);
	}

	public NdResourceFile getFile() {
		return FILE.get(getNd(), this.address);
	}

	public char[][] getTypeParameterSignatures() {
		List<NdTypeParameter> parameters = getTypeParameters();
		char[][] result = new char[parameters.size()][];

		int idx = 0;
		for (NdTypeParameter next : parameters) {
			char[] nextContents = getSignatureFor(next);
			result[idx] = nextContents;
			idx++;
		}
		return result;
	}

	private char[] getSignatureFor(NdTypeParameter next) {
		CharArrayBuffer nextArray = new CharArrayBuffer();
		next.getSignature(nextArray);
		char[] nextContents = nextArray.getContents();
		return nextContents;
	}

	public List<NdTypeParameter> getTypeParameters() {
		return TYPE_PARAMETERS.asList(getNd(), this.address);
	}
}
