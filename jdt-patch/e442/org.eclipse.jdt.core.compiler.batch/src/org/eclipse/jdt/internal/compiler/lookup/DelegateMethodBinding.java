/*
 * Copyright 2009-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;

public class DelegateMethodBinding extends MethodBinding {

	public final AbstractMethodDeclaration delegateMethod;

	public DelegateMethodBinding(int modifiers, ReferenceBinding declaringClass, AbstractMethodDeclaration delegateMethod) {
		super(modifiers, null, null, declaringClass);
		this.delegateMethod = delegateMethod;
	}

	public DelegateMethodBinding(int modifiers, char[] selector, ReferenceBinding declaringClass, AbstractMethodDeclaration delegateMethod) {
		super(modifiers, selector, null, null, null, declaringClass);
		this.delegateMethod = delegateMethod;
	}
}
