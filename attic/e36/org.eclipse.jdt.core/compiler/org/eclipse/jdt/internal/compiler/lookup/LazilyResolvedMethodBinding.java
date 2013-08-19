/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement        - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * This represents a method binding whose parameter and return type we want to resolve lazily.  It is
 * currently intended for use representing the getX/setX and isX methods you would get for a groovy
 * property (it therefore only supports 0 or 1 parameter, it doesn't need to support more for now).
 * Upon finding the method is unresolved, the code in SourceTypeBinding.resolveTypesFor(MethodBinding)
 * would normally look for a method declaration from which to discover the parameter and return type
 * references.  There is no declaration corresponding to these property accessor bindings and so
 * there is an extra check in resolveTypesFor when the declaration is not found, if the binding
 * is determined to be a LazilyResolvedMethodBinding - we ask it for the parameter and return
 * types which it determines from the field binding for the property.  The key benefit here is that
 * the parameter/return type bindings are not chased down unless required.
 * 
 * @author Andy Clement
 */
public class LazilyResolvedMethodBinding extends MethodBinding {
	
	private boolean isGetter;
	private String propertyName;
	
	public LazilyResolvedMethodBinding(boolean isGetter, String propertyName, int modifiers, char[] selector, ReferenceBinding[] thrownExceptions, ReferenceBinding declaringClass) {
		super(modifiers| ExtraCompilerModifiers.AccUnresolved,selector,null,null,thrownExceptions,declaringClass);
		this.propertyName = propertyName;
		this.isGetter = isGetter;
	}

	/**
	 * Discover the type of the property and return that as the binding to use as the accessor method
	 * parameter/return type.
	 */
	private TypeBinding getTypeBinding() {
		FieldBinding fBinding = this.declaringClass.getField(this.propertyName.toCharArray(), false);
		if (fBinding != null && !(fBinding.type instanceof MissingTypeBinding)) {
			return fBinding.type;
		}
		return null;
	}

	public TypeBinding getParameterTypeBinding() {
		if (this.isGetter) {
			return null;
		} else {
			return getTypeBinding();
		}
	}
	
	public TypeBinding getReturnTypeBinding() {
		if (this.isGetter) {
			return getTypeBinding();
		} else {
			return TypeBinding.VOID;
		}
	}

}
