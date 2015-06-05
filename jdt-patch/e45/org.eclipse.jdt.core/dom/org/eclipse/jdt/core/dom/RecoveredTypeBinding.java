/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 429813 - [1.8][dom ast] IMethodBinding#getJavaElement() should return IMethod for lambda
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.PackageFragment;

/**
 * This class represents the recovered binding for a type
 */
@SuppressWarnings("rawtypes")
class RecoveredTypeBinding implements ITypeBinding {

	private VariableDeclaration variableDeclaration;
	private Type currentType;
	private BindingResolver resolver;
	private int dimensions;
	private RecoveredTypeBinding innerTypeBinding;
	private ITypeBinding[] typeArguments;
	private org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;

	RecoveredTypeBinding(BindingResolver resolver, VariableDeclaration variableDeclaration) {
		this.variableDeclaration = variableDeclaration;
		this.resolver = resolver;
		this.currentType = getType();
		this.dimensions = variableDeclaration.getExtraDimensions();
		if (this.currentType.isArrayType()) {
			this.dimensions += ((ArrayType) this.currentType).getDimensions();
		}
	}

	RecoveredTypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding typeBinding) {
		this.resolver = resolver;
		this.dimensions = typeBinding.dimensions();
		this.binding = typeBinding;
	}

	RecoveredTypeBinding(BindingResolver resolver, Type type) {
		this.currentType = type;
		this.resolver = resolver;
		this.dimensions = 0;
		if (type.isArrayType()) {
			this.dimensions += ((ArrayType) type).getDimensions();
		}
	}

	RecoveredTypeBinding(BindingResolver resolver, RecoveredTypeBinding typeBinding, int dimensions) {
		this.innerTypeBinding = typeBinding;
		this.dimensions = typeBinding.getDimensions() + dimensions;
		this.resolver = resolver;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#createArrayType(int)
	 */
	public ITypeBinding createArrayType(int dims) {
		return this.resolver.getTypeBinding(this, dims);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getBinaryName()
	 */
	public String getBinaryName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getBound()
	 */
	public ITypeBinding getBound() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getGenericTypeOfWildcardType()
	 */
	public ITypeBinding getGenericTypeOfWildcardType() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getRank()
	 */
	public int getRank() {
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getComponentType()
	 */
	public ITypeBinding getComponentType() {
		if (this.dimensions == 0) return null;
		return this.resolver.getTypeBinding(this, -1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaredFields()
	 */
	public IVariableBinding[] getDeclaredFields() {
		return TypeBinding.NO_VARIABLE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaredMethods()
	 */
	public IMethodBinding[] getDeclaredMethods() {
		return TypeBinding.NO_METHOD_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaredModifiers()
	 * @deprecated Use ITypeBinding#getModifiers() instead
	 */
	public int getDeclaredModifiers() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaredTypes()
	 */
	public ITypeBinding[] getDeclaredTypes() {
		return TypeBinding.NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaringMethod()
	 */
	public IMethodBinding getDeclaringMethod() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDeclaringMember()
	 */
	@Override
	public IBinding getDeclaringMember() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getDimensions()
	 */
	public int getDimensions() {
		return this.dimensions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getElementType()
	 */
	public ITypeBinding getElementType() {
		if (this.binding != null) {
			if (this.binding.isArrayType()) {
				ArrayBinding arrayBinding = (ArrayBinding) this.binding;
				return new RecoveredTypeBinding(this.resolver, arrayBinding.leafComponentType);
			} else {
				return new RecoveredTypeBinding(this.resolver, this.binding);
			}
		}
		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getElementType();
		}
		if (this.currentType!= null && this.currentType.isArrayType()) {
			return this.resolver.getTypeBinding(((ArrayType) this.currentType).getElementType());
		}
		if (this.variableDeclaration != null && this.variableDeclaration.getExtraDimensions() != 0) {
			return this.resolver.getTypeBinding(getType());
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getErasure()
	 */
	public ITypeBinding getErasure() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getFunctionalInterfaceMethod
	 */
	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getInterfaces()
	 */
	public ITypeBinding[] getInterfaces() {
		return TypeBinding.NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getModifiers()
	 */
	public int getModifiers() {
		return Modifier.NONE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getName()
	 */
	public String getName() {
		char[] brackets = new char[this.dimensions * 2];
		for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
			brackets[i] = ']';
			brackets[i - 1] = '[';
		}
		StringBuffer buffer = new StringBuffer(getInternalName());
		buffer.append(brackets);
		return String.valueOf(buffer);
	}

	private String getInternalName() {
		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getInternalName();
		}
		ReferenceBinding referenceBinding = getReferenceBinding();
		if (referenceBinding != null) {
			return new String(referenceBinding.compoundName[referenceBinding.compoundName.length - 1]);
		}
		return getTypeNameFrom(getType());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getPackage()
	 */
	public IPackageBinding getPackage() {
		if (this.binding != null) {
			switch (this.binding.kind()) {
				case Binding.BASE_TYPE :
				case Binding.ARRAY_TYPE :
				case Binding.TYPE_PARAMETER : // includes capture scenario
				case Binding.WILDCARD_TYPE :
				case Binding.INTERSECTION_TYPE:
					return null;
			}
			IPackageBinding packageBinding = this.resolver.getPackageBinding(this.binding.getPackage());
			if (packageBinding != null) return packageBinding;
		}
		if (this.innerTypeBinding != null && this.dimensions > 0) {
			return null;
		}
		CompilationUnitScope scope = this.resolver.scope();
		if (scope != null) {
			return this.resolver.getPackageBinding(scope.getCurrentPackage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getQualifiedName()
	 */
	public String getQualifiedName() {
		ReferenceBinding referenceBinding = getReferenceBinding();
		if (referenceBinding != null) {
			StringBuffer buffer = new StringBuffer();
			char[] brackets = new char[this.dimensions * 2];
			for (int i = this.dimensions * 2 - 1; i >= 0; i -= 2) {
				brackets[i] = ']';
				brackets[i - 1] = '[';
			}
			buffer.append(CharOperation.toString(referenceBinding.compoundName));
			buffer.append(brackets);
			return String.valueOf(buffer);
		} else {
			return getName();
		}
	}

	private ReferenceBinding getReferenceBinding() {
		if (this.binding != null) {
			if (this.binding.isArrayType()) {
				ArrayBinding arrayBinding = (ArrayBinding) this.binding;
				if (arrayBinding.leafComponentType instanceof ReferenceBinding) {
					return (ReferenceBinding) arrayBinding.leafComponentType;
				}
			} else if (this.binding instanceof ReferenceBinding) {
				return (ReferenceBinding) this.binding;
			}
		} else if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getReferenceBinding();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getSuperclass()
	 */
	public ITypeBinding getSuperclass() {
		if (getQualifiedName().equals("java.lang.Object")) {	//$NON-NLS-1$
			return null;
		}
		return this.resolver.resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeArguments()
	 */
	public ITypeBinding[] getTypeArguments() {
		if (this.binding != null) {
			return this.typeArguments = TypeBinding.NO_TYPE_BINDINGS;
		}
		if (this.typeArguments != null) {
			return this.typeArguments;
		}

		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.getTypeArguments();
		}

		if (this.currentType.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) this.currentType;
			List typeArgumentsList = parameterizedType.typeArguments();
			int size = typeArgumentsList.size();
			ITypeBinding[] temp = new ITypeBinding[size];
			for (int i = 0; i < size; i++) {
				ITypeBinding currentTypeBinding = ((Type) typeArgumentsList.get(i)).resolveBinding();
				if (currentTypeBinding == null) {
					return this.typeArguments = TypeBinding.NO_TYPE_BINDINGS;
				}
				temp[i] = currentTypeBinding;
			}
			return this.typeArguments = temp;
		}
		return this.typeArguments = TypeBinding.NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeBounds()
	 */
	public ITypeBinding[] getTypeBounds() {
		return TypeBinding.NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeDeclaration()
	 */
	public ITypeBinding getTypeDeclaration() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeParameters()
	 */
	public ITypeBinding[] getTypeParameters() {
		return TypeBinding.NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getWildcard()
	 */
	public ITypeBinding getWildcard() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isAnnotation()
	 */
	public boolean isAnnotation() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isAnonymous()
	 */
	public boolean isAnonymous() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isArray()
	 */
	public boolean isArray() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isAssignmentCompatible(org.eclipse.jdt.core.dom.ITypeBinding)
	 */
	public boolean isAssignmentCompatible(ITypeBinding typeBinding) {
		if ("java.lang.Object".equals(typeBinding.getQualifiedName())) { //$NON-NLS-1$
			return true;
		}
		// since recovered binding are not unique isEqualTo is required
		return isEqualTo(typeBinding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isCapture()
	 */
	public boolean isCapture() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isCastCompatible(org.eclipse.jdt.core.dom.ITypeBinding)
	 */
	public boolean isCastCompatible(ITypeBinding typeBinding) {
		if ("java.lang.Object".equals(typeBinding.getQualifiedName())) { //$NON-NLS-1$
			return true;
		}
		// since recovered binding are not unique isEqualTo is required
		return isEqualTo(typeBinding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isClass()
	 */
	public boolean isClass() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isEnum()
	 */
	public boolean isEnum() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isFromSource()
	 */
	public boolean isFromSource() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isGenericType()
	 */
	public boolean isGenericType() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isInterface()
	 */
	public boolean isInterface() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isLocal()
	 */
	public boolean isLocal() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isMember()
	 */
	public boolean isMember() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isNested()
	 */
	public boolean isNested() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isNullType()
	 */
	public boolean isNullType() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isParameterizedType()
	 */
	public boolean isParameterizedType() {
		if (this.innerTypeBinding != null) {
			return this.innerTypeBinding.isParameterizedType();
		}
		if (this.currentType != null) {
			return this.currentType.isParameterizedType();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isPrimitive()
	 */
	public boolean isPrimitive() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isRawType()
	 */
	public boolean isRawType() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isSubTypeCompatible(org.eclipse.jdt.core.dom.ITypeBinding)
	 */
	public boolean isSubTypeCompatible(ITypeBinding typeBinding) {
		if ("java.lang.Object".equals(typeBinding.getQualifiedName())) { //$NON-NLS-1$
			return true;
		}
		// since recovered binding are not unique isEqualTo is required
		return isEqualTo(typeBinding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isTopLevel()
	 */
	public boolean isTopLevel() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isTypeVariable()
	 */
	public boolean isTypeVariable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isUpperbound()
	 */
	public boolean isUpperbound() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isWildcardType()
	 */
	public boolean isWildcardType() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#getAnnotations()
	 */
	public IAnnotationBinding[] getAnnotations() {
		return AnnotationBinding.NoAnnotations;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		IPackageBinding packageBinding = getPackage();
		if (packageBinding != null) {
			final IJavaElement javaElement = packageBinding.getJavaElement();
			if (javaElement != null && javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				// best effort: we don't know if the recovered binding is a binary or source binding, so go with a simple source type
				return ((PackageFragment) javaElement).getCompilationUnit(getInternalName() + SuffixConstants.SUFFIX_STRING_java).getType(this.getName());
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#getKey()
	 */
	public String getKey() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Recovered#"); //$NON-NLS-1$
		if (this.innerTypeBinding != null) {
			buffer.append("innerTypeBinding") //$NON-NLS-1$
			      .append(this.innerTypeBinding.getKey());
		} else if (this.currentType != null) {
			buffer.append("currentType") //$NON-NLS-1$
			      .append(this.currentType.toString());
		} else if (this.binding != null) {
			buffer.append("typeBinding") //$NON-NLS-1$
				  .append(this.binding.computeUniqueKey());
		} else if (this.variableDeclaration != null) {
			buffer
				.append("variableDeclaration") //$NON-NLS-1$
				.append(this.variableDeclaration.getClass())
				.append(this.variableDeclaration.getName().getIdentifier())
				.append(this.variableDeclaration.getExtraDimensions());
		}
		buffer.append(getDimensions());
		if (this.typeArguments != null) {
			buffer.append('<');
			for (int i = 0, max = this.typeArguments.length; i < max; i++) {
				if (i != 0) {
					buffer.append(',');
				}
				buffer.append(this.typeArguments[i].getKey());
			}
			buffer.append('>');
		}
		return String.valueOf(buffer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.TYPE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#isEqualTo(org.eclipse.jdt.core.dom.IBinding)
	 */
	public boolean isEqualTo(IBinding other) {
		if (!other.isRecovered() || other.getKind() != IBinding.TYPE) return false;
		return getKey().equals(other.getKey());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#isRecovered()
	 */
	public boolean isRecovered() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	private String getTypeNameFrom(Type type) {
		if (type == null) return Util.EMPTY_STRING;
		switch(type.getNodeType0()) {
			case ASTNode.ARRAY_TYPE :
				ArrayType arrayType = (ArrayType) type;
				type = arrayType.getElementType();
				return getTypeNameFrom(type);
			case ASTNode.PARAMETERIZED_TYPE :
				ParameterizedType parameterizedType = (ParameterizedType) type;
				StringBuffer buffer = new StringBuffer(getTypeNameFrom(parameterizedType.getType()));
				ITypeBinding[] tArguments = getTypeArguments();
				final int typeArgumentsLength = tArguments.length;
				if (typeArgumentsLength != 0) {
					buffer.append('<');
					for (int i = 0; i < typeArgumentsLength; i++) {
						if (i > 0) {
							buffer.append(',');
						}
						buffer.append(tArguments[i].getName());
					}
					buffer.append('>');
				}
				return String.valueOf(buffer);
			case ASTNode.PRIMITIVE_TYPE :
				PrimitiveType primitiveType = (PrimitiveType) type;
				return primitiveType.getPrimitiveTypeCode().toString();
			case ASTNode.QUALIFIED_TYPE :
				QualifiedType qualifiedType = (QualifiedType) type;
				return qualifiedType.getName().getIdentifier();
			case ASTNode.NAME_QUALIFIED_TYPE :
				NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
				return nameQualifiedType.getName().getIdentifier();
			case ASTNode.SIMPLE_TYPE :
				SimpleType simpleType = (SimpleType) type;
				Name name = simpleType.getName();
				if (name.isQualifiedName()) {
					QualifiedName qualifiedName = (QualifiedName) name;
					return qualifiedName.getName().getIdentifier();
				}
				return ((SimpleName) name).getIdentifier();
		}
		return Util.EMPTY_STRING;
	}

	private Type getType() {
		if (this.currentType != null) {
			return this.currentType;
		}
		if (this.variableDeclaration == null) return null;
		switch(this.variableDeclaration.getNodeType()) {
			case ASTNode.SINGLE_VARIABLE_DECLARATION :
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) this.variableDeclaration;
				return singleVariableDeclaration.getType();
			default :
				// this is a variable declaration fragment
				ASTNode parent = this.variableDeclaration.getParent();
				switch(parent.getNodeType()) {
					case ASTNode.VARIABLE_DECLARATION_EXPRESSION :
						VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) parent;
						return variableDeclarationExpression.getType();
					case ASTNode.VARIABLE_DECLARATION_STATEMENT :
						VariableDeclarationStatement statement = (VariableDeclarationStatement) parent;
						return statement.getType();
					case ASTNode.FIELD_DECLARATION :
						FieldDeclaration fieldDeclaration  = (FieldDeclaration) parent;
						return fieldDeclaration.getType();
				}
		}
		return null; // should not happen
	}

	public IAnnotationBinding[] getTypeAnnotations() {
		return AnnotationBinding.NoAnnotations;
	}
}
