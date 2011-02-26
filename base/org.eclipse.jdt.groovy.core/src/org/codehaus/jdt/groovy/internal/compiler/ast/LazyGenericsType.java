/*******************************************************************************
 * Copyright (c) 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * A lazily initialized GenericsType info that gets its' state from a JDT entity.
 * 
 * @author Andy Clement
 * 
 */
@SuppressWarnings("restriction")
public class LazyGenericsType extends GenericsType {

	private boolean initialized = false;
	private TypeVariableBinding tvBinding;
	private JDTResolver resolver;

	public LazyGenericsType(TypeVariableBinding typeVariableBinding, JDTResolver jdtResolver) {
		this.tvBinding = typeVariableBinding;
		this.resolver = jdtResolver;
		this.name = new String(typeVariableBinding.sourceName);
		this.placeholder = true;
	}

	@Override
	public ClassNode getLowerBound() {
		ensureInitialized();
		return lowerBound;
	}

	@Override
	public ClassNode getType() {
		ensureInitialized();
		return type;
	}

	@Override
	public ClassNode[] getUpperBounds() {
		ensureInitialized();
		return upperBounds;
	}

	@Override
	public boolean isResolved() {
		// It is always resolved, but may not be initialized yet
		return true;
	}

	@Override
	public boolean isWildcard() {
		return false; // FIXASC (M3:optimize) This class is not yet used for wildcards, but it could be
	}

	@Override
	public void setLowerBound(ClassNode bound) {
		throw new ImmutableException();
	}

	@Override
	public void setName(String name) {
		throw new ImmutableException();
	}

	@Override
	public void setPlaceholder(boolean placeholder) {
		throw new ImmutableException();
	}

	@Override
	public void setResolved(boolean res) {
		throw new ImmutableException();
	}

	@Override
	public void setType(ClassNode type) {
		throw new ImmutableException();
	}

	@Override
	public void setUpperBounds(ClassNode[] bounds) {
		throw new ImmutableException();
	}

	@Override
	public void setWildcard(boolean wildcard) {
		throw new ImmutableException();
	}

	@Override
	public String toString() {
		if (!initialized) {
			return "LazyGenericsType instance (uninitialized) for " + tvBinding;
		} else {
			return super.toString();
		}
	}

	// @Override
	// public String toStructureString() {
	// ensureInitialized();
	// return super.toStructureString();
	// }

	// private ClassNode configureTypeVariableReference(TypeVariable tv) {
	// ClassNode cn = ClassHelper.makeWithoutCaching(tv.getName());
	// cn.setGenericsPlaceHolder(true);
	// ClassNode cn2 = ClassHelper.makeWithoutCaching(tv.getName());
	// cn2.setGenericsPlaceHolder(true);
	// GenericsType[] gts = new GenericsType[]{new GenericsType(cn2)};
	// cn.setGenericsTypes(gts);
	// cn.setRedirect(ClassHelper.OBJECT_TYPE);
	// return cn;
	// }

	private void ensureInitialized() {
		if (!initialized) {
			ClassNode cn = ClassHelper.makeWithoutCaching(name);
			cn.setGenericsPlaceHolder(true);
			ClassNode cn2 = ClassHelper.makeWithoutCaching(name);
			cn2.setGenericsPlaceHolder(true);
			GenericsType[] gts = new GenericsType[] { new GenericsType(cn2) };
			cn.setGenericsTypes(gts);
			cn.setRedirect(ClassHelper.OBJECT_TYPE);
			type = cn;
			if (tvBinding.firstBound == null) {
				type = ClassHelper.OBJECT_TYPE;
			} else {
				// type = ClassHelper.OBJECT_TYPE;

				// new ClassNode(Object.class);
				// resolver.convertToClassNode(tvBinding.firstBound);
				// GenericsType gt = new GenericsType();
				// gt.setName(name);
				// type.setGenericsTypes(new GenericsType[] { gt });
				// type.setRedirect(ClassHelper.OBJECT_TYPE);
				ClassNode firstBoundType = resolver.convertToClassNode(tvBinding.firstBound);
				TypeBinding[] otherUpperBounds = tvBinding.otherUpperBounds();
				if (otherUpperBounds.length == 0) {
					upperBounds = new ClassNode[] { firstBoundType };
				} else {
					ClassNode[] nodes = new ClassNode[1 + otherUpperBounds.length];
					int idx = 0;
					nodes[idx++] = firstBoundType;
					for (TypeBinding typeBinding : otherUpperBounds) {
						nodes[idx++] = resolver.convertToClassNode(typeBinding);
					}
					upperBounds = nodes;
				}
			}
			lowerBound = null;
			initialized = true;
		}
	}

}
