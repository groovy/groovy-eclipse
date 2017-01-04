/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.IJavaElement;

/**
 * This class represents the recovered binding for a variable
 */
class RecoveredVariableBinding implements IVariableBinding {

	private VariableDeclaration variableDeclaration;
	private BindingResolver resolver;

	RecoveredVariableBinding(BindingResolver resolver, VariableDeclaration variableDeclaration) {
		this.resolver = resolver;
		this.variableDeclaration = variableDeclaration;
	}
	public Object getConstantValue() {
		return null;
	}

	public ITypeBinding getDeclaringClass() {
		ASTNode parent = this.variableDeclaration.getParent();
		while (parent != null && parent.getNodeType() != ASTNode.TYPE_DECLARATION) {
			parent = parent.getParent();
		}
		if (parent != null) {
			return ((TypeDeclaration) parent).resolveBinding();
		}
		return null;
	}

	public IMethodBinding getDeclaringMethod() {
		ASTNode parent = this.variableDeclaration.getParent();
		while (parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION) {
			parent = parent.getParent();
		}
		if (parent != null) {
			return ((MethodDeclaration) parent).resolveBinding();
		}
		return null;
	}

	public String getName() {
		return this.variableDeclaration.getName().getIdentifier();
	}

	public ITypeBinding getType() {
		return this.resolver.getTypeBinding(this.variableDeclaration);
	}

	public IVariableBinding getVariableDeclaration() {
		return this;
	}

	public int getVariableId() {
		return 0;
	}

	public boolean isEnumConstant() {
		return false;
	}

	public boolean isField() {
		return this.variableDeclaration.getParent() instanceof FieldDeclaration;
	}

	public boolean isParameter() {
		return this.variableDeclaration instanceof SingleVariableDeclaration;
	}

	public IAnnotationBinding[] getAnnotations() {
		return AnnotationBinding.NoAnnotations;
	}

	public IJavaElement getJavaElement() {
		return null;
	}

	public String getKey() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Recovered#"); //$NON-NLS-1$
		if (this.variableDeclaration != null) {
			buffer
				.append("variableDeclaration") //$NON-NLS-1$
				.append(this.variableDeclaration.getClass())
				.append(this.variableDeclaration.getName().getIdentifier())
				.append(this.variableDeclaration.getExtraDimensions());
		}
		return String.valueOf(buffer);
	}

	public int getKind() {
		return IBinding.VARIABLE;
	}

	public int getModifiers() {
		return 0;
	}

	public boolean isDeprecated() {
		return false;
	}

	public boolean isEqualTo(IBinding binding) {
		if (binding.isRecovered() && binding.getKind() == IBinding.VARIABLE) {
			return getKey().equals(binding.getKey());
		}
		return false;
	}

	public boolean isRecovered() {
		return true;
	}

	public boolean isSynthetic() {
		return false;
	}
	public boolean isEffectivelyFinal() {
		return false;
	}
}
