/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	@Override
	public char[] computeUniqueKey() {
		return super.computeUniqueKey();
	}
}
