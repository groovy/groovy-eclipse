/*******************************************************************************
 * Copyright (c) 2005, 2013 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *    IBM Corporation - implemented methods from IBinding
 *    IBM Corporation - renamed from ResolvedDefaultValuePair to DefaultValuePairBinding
 *    IBM Corporation - Fix for 328969
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.dom.BindingResolver;
import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * Member value pair which compose of default values.
 */
class DefaultValuePairBinding extends MemberValuePairBinding {

	private org.eclipse.jdt.internal.compiler.lookup.MethodBinding method;

	DefaultValuePairBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding binding, BindingResolver resolver) {
		super(null, resolver);
		this.method = binding;
		this.value = MemberValuePairBinding.buildDOMValue(binding.getDefaultValue(), resolver);
		if (binding.returnType != null && binding.returnType.isArrayType()) {
			// wrap into an array
			if (this.value == null) {
				this.value = new Object[0];
			} else if (!this.value.getClass().isArray()) {
				this.value = new Object[] { this.value };
			}
		}
	}

	public IMethodBinding getMethodBinding() {
		return this.bindingResolver.getMethodBinding(this.method);
	}

	public String getName() {
		return new String(this.method.selector);
	}

	public Object getValue() {
		return this.value;
	}

	public boolean isDefault() {
		return true;
	}

	public boolean isDeprecated() {
		return this.method.isDeprecated();
	}
}
