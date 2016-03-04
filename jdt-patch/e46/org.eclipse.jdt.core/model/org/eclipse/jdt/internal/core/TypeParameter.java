/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 466279 - [hovering] IAE on hover when annotation-based null analysis is enabled
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;

public class TypeParameter extends SourceRefElement implements ITypeParameter {

	static final ITypeParameter[] NO_TYPE_PARAMETERS = new ITypeParameter[0];

	protected String name;

	public TypeParameter(JavaElement parent, String name) {
		super(parent);
		this.name = name;
	}

	public boolean equals(Object o) {
		if (!(o instanceof TypeParameter)) return false;
		return super.equals(o);
	}

	public String[] getBounds() throws JavaModelException {
		TypeParameterElementInfo info = (TypeParameterElementInfo) getElementInfo();
		return CharOperation.toStrings(info.bounds);
	}
	
	public String[] getBoundsSignatures() throws JavaModelException {
		
		String[] boundSignatures = null;
		TypeParameterElementInfo info = (TypeParameterElementInfo) this.getElementInfo();
		
		// For a binary type or method, the signature is already available from the .class file.
		// No need to construct again
		if (this.parent instanceof BinaryMember) {
			char[][] boundsSignatures = info.boundsSignatures;
			if (boundsSignatures == null || boundsSignatures.length == 0) {
				return CharOperation.NO_STRINGS;	
			}
			return CharOperation.toStrings(info.boundsSignatures);
		}
		
		char[][] bounds = info.bounds;
		if (bounds == null || bounds.length == 0) {
			return CharOperation.NO_STRINGS;
		}
	
		int boundsLength = bounds.length;
		boundSignatures = new String[boundsLength];
		for (int i = 0; i < boundsLength; i++) {
			boundSignatures[i] = new String(Signature.createCharArrayTypeSignature(bounds[i], false));
		}
		return boundSignatures;
	}
	
	public IMember getDeclaringMember() {
		return (IMember) getParent();
	}

	public String getElementName() {
		return this.name;
	}

	public int getElementType() {
		return TYPE_PARAMETER;
	}

	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_TYPE_PARAMETER;
	}
	
	public String getKey(boolean forceOpen) throws JavaModelException {
		StringBuilder buf = new StringBuilder();
		if (this.parent instanceof IType) {
			if (this.parent instanceof BinaryType)
				buf.append(((BinaryType) this.parent).getKey(forceOpen));
			else 
				buf.append(((IType) this.parent).getKey());
		} else if (this.parent instanceof IMember) {
			if (this.parent instanceof BinaryMember)
				buf.append(((BinaryMember) this.parent).getKey(forceOpen));
			else 
				buf.append(((IMethod) this.parent).getKey());
		}
		buf.append(":T"); //$NON-NLS-1$
		buf.append(this.name);
		buf.append(';');
		return buf.toString();
	}

	public ISourceRange getNameRange() throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			// ensure the class file's buffer is open so that source ranges are computed
			ClassFile classFile = (ClassFile)getClassFile();
			if (classFile != null) {
				classFile.getBuffer();
				return mapper.getNameRange(this);
			}
		}
		TypeParameterElementInfo info = (TypeParameterElementInfo) getElementInfo();
		return new SourceRange(info.nameStart, info.nameEnd - info.nameStart + 1);
	}

	/*
	 * @see ISourceReference
	 */
	public ISourceRange getSourceRange() throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			// ensure the class file's buffer is open so that source ranges are computed
			ClassFile classFile = (ClassFile)getClassFile();
			if (classFile != null) {
				classFile.getBuffer();
				return mapper.getSourceRange(this);
			}
		}
		return super.getSourceRange();
	}

	public IClassFile getClassFile() {
		return ((JavaElement)getParent()).getClassFile();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 3.7
	 */
	public ITypeRoot getTypeRoot() {
		return this.getDeclaringMember().getTypeRoot();
	}

	protected void toStringName(StringBuffer buffer) {
		buffer.append('<');
		buffer.append(getElementName());
		buffer.append('>');
	}
}
