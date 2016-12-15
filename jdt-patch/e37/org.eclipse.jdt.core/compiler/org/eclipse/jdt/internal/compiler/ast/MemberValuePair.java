// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * MemberValuePair node
 */
public class MemberValuePair extends ASTNode {

	public char[] name;
	public Expression value;
	public MethodBinding binding;
	/**
	 *  The representation of this pair in the type system.
	 */
	public ElementValuePair compilerElementPair = null;

	public MemberValuePair(char[] token, int sourceStart, int sourceEnd, Expression value) {
		this.name = token;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.value = value;
		if (value instanceof ArrayInitializer) {
			value.bits |= IsAnnotationDefaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		output
			.append(this.name)
			.append(" = "); //$NON-NLS-1$
		this.value.print(0, output);
		return output;
	}

	// GROOVY add
	private boolean isClass(TypeBinding requiredType) {
		if (requiredType.isArrayType()) {
			requiredType = requiredType.leafComponentType();
		}
		requiredType = requiredType.original();
		boolean isClass = requiredType.id == TypeIds.T_JavaLangClass;
		return isClass;
	}

	private boolean isGroovy(Scope scope) {
		while (scope.parent != null) {
			scope = scope.parent;
		}
		return scope.getClass().getSimpleName().startsWith("Groovy"); //$NON-NLS-1$
	}

	private Expression repairClassLiteralReference(Expression exp, BlockScope scope, TypeBinding[] valueType) {
		TypeBinding vtb = null;
		if (exp instanceof SingleNameReference) {
			vtb = exp.resolveType(scope);
			SingleNameReference ref = (SingleNameReference) exp;
			if (vtb != null && Arrays.equals(ref.token, vtb.sourceName())) {
				return new ClassLiteralAccess(ref.sourceEnd, new SingleTypeReference(ref.token, ((long) ref.sourceStart) << 32 | ref.sourceEnd));
			}
		} else if (this.value instanceof QualifiedNameReference) {
			vtb = exp.resolveType(scope);
			QualifiedNameReference ref = (QualifiedNameReference) exp;
			if (vtb != null && Arrays.equals(ref.tokens[ref.tokens.length - 1], vtb.sourceName())) {
				return new ClassLiteralAccess(ref.sourceEnd, new QualifiedTypeReference(ref.tokens, ref.sourcePositions));
			}
		}
		if (valueType != null) {
			valueType[0] = vtb;
		}
		return exp;
	}
	// GROOVY end

	public void resolveTypeExpecting(BlockScope scope, TypeBinding requiredType) {

		if (this.value == null) {
			this.compilerElementPair = new ElementValuePair(this.name, this.value, this.binding);
			return;
		}
		if (requiredType == null) {
			// fault tolerance: keep resolving
			if (this.value instanceof ArrayInitializer) {
				this.value.resolveTypeExpecting(scope, null);
			} else {
				this.value.resolveType(scope);
			}
			this.compilerElementPair = new ElementValuePair(this.name, this.value, this.binding);
			return;
		}

		// GROOVY add - handling for class literals that do not end in '.class'
		TypeBinding[] vtb = null;
		if (isClass(requiredType) && isGroovy(scope)) {
			if (this.value instanceof ArrayInitializer) {
				Expression[] values = ((ArrayInitializer) this.value).expressions;
				for (int i = 0, n = values.length; i < n; i += 1) {
					values[i] = repairClassLiteralReference(values[i], scope, null);
				}
			} else {
				vtb = new TypeBinding[1]; // need resolved type if value is resolved but unchanged
				this.value = repairClassLiteralReference(this.value, scope, vtb);
			}
		}
		// GROOVY end

		this.value.setExpectedType(requiredType); // needed in case of generic method invocation
		TypeBinding valueType;
		if (this.value instanceof ArrayInitializer) {
			ArrayInitializer initializer = (ArrayInitializer) this.value;
			valueType = initializer.resolveTypeExpecting(scope, this.binding.returnType);
		} else if (this.value instanceof ArrayAllocationExpression) {
			scope.problemReporter().annotationValueMustBeArrayInitializer(this.binding.declaringClass, this.name, this.value);
			this.value.resolveType(scope);
			valueType = null; // no need to pursue
		} else {
			// GROOVY edit -- returns null if called 2x
			//valueType = this.value.resolveType(scope);
			valueType = (vtb != null && vtb[0] != null) ? vtb[0] : this.value.resolveType(scope);
			// GROOVY end
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=248897
			ASTVisitor visitor = new ASTVisitor() {
				public boolean visit(SingleNameReference reference, BlockScope scop) {
					if (reference.binding instanceof LocalVariableBinding) {
						((LocalVariableBinding) reference.binding).useFlag = LocalVariableBinding.USED;
					}
					return true;
				}
			};
			this.value.traverse(visitor, scope);
		}
		this.compilerElementPair = new ElementValuePair(this.name, this.value, this.binding);
		if (valueType == null)
			return;

		TypeBinding leafType = requiredType.leafComponentType();
		if (!(this.value.isConstantValueOfTypeAssignableToType(valueType, requiredType)
				|| valueType.isCompatibleWith(requiredType))) {

			if (!(requiredType.isArrayType()
					&& requiredType.dimensions() == 1
					&& (this.value.isConstantValueOfTypeAssignableToType(valueType, leafType)
							|| valueType.isCompatibleWith(leafType)))) {

				if (leafType.isAnnotationType() && !valueType.isAnnotationType()) {
					scope.problemReporter().annotationValueMustBeAnnotation(this.binding.declaringClass, this.name, this.value, leafType);
				} else {
					scope.problemReporter().typeMismatchError(valueType, requiredType, this.value, null);
				}
				return; // may allow to proceed to find more errors at once
			}
		} else {
			scope.compilationUnitScope().recordTypeConversion(requiredType.leafComponentType(), valueType.leafComponentType());
			this.value.computeConversion(scope, requiredType, valueType);
		}

		// annotation methods can only return base types, String, Class, enum type, annotation types and arrays of these
		checkAnnotationMethodType: {
			switch (leafType.erasure().id) {
				case T_byte :
				case T_short :
				case T_char :
				case T_int :
				case T_long :
				case T_float :
				case T_double :
				case T_boolean :
				case T_JavaLangString :
					if (this.value instanceof ArrayInitializer) {
						ArrayInitializer initializer = (ArrayInitializer) this.value;
						final Expression[] expressions = initializer.expressions;
						if (expressions != null) {
							for (int i =0, max = expressions.length; i < max; i++) {
								Expression expression = expressions[i];
								if (expression.resolvedType == null) continue; // fault-tolerance
								if (expression.constant == Constant.NotAConstant) {
									scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, expressions[i], false);
								}
							}
						}
					} else if (this.value.constant == Constant.NotAConstant) {
						if (valueType.isArrayType()) {
							scope.problemReporter().annotationValueMustBeArrayInitializer(this.binding.declaringClass, this.name, this.value);
						} else {
							scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, this.value, false);
						}
					}
					break checkAnnotationMethodType;
				case T_JavaLangClass :
					if (this.value instanceof ArrayInitializer) {
						ArrayInitializer initializer = (ArrayInitializer) this.value;
						final Expression[] expressions = initializer.expressions;
						if (expressions != null) {
							for (int i =0, max = expressions.length; i < max; i++) {
								Expression currentExpression = expressions[i];
								if (!(currentExpression instanceof ClassLiteralAccess)) {
									scope.problemReporter().annotationValueMustBeClassLiteral(this.binding.declaringClass, this.name, currentExpression);
								}
							}
						}
					} else if (!(this.value instanceof ClassLiteralAccess)) {
						scope.problemReporter().annotationValueMustBeClassLiteral(this.binding.declaringClass, this.name, this.value);
					}
					break checkAnnotationMethodType;
			}
			if (leafType.isEnum()) {
				if (this.value instanceof NullLiteral) {
					scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, this.value, true);
				} else if (this.value instanceof ArrayInitializer) {
					ArrayInitializer initializer = (ArrayInitializer) this.value;
					final Expression[] expressions = initializer.expressions;
					if (expressions != null) {
						for (int i =0, max = expressions.length; i < max; i++) {
							Expression currentExpression = expressions[i];
							if (currentExpression instanceof NullLiteral) {
								scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, currentExpression, true);
							} else if (currentExpression instanceof NameReference) {
								NameReference nameReference = (NameReference) currentExpression;
								final Binding nameReferenceBinding = nameReference.binding;
								if (nameReferenceBinding.kind() == Binding.FIELD) {
									FieldBinding fieldBinding = (FieldBinding) nameReferenceBinding;
									if (!fieldBinding.declaringClass.isEnum()) {
										scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, currentExpression, true);
									}
								}
							}
						}
					}
				} else if (this.value instanceof NameReference) {
					NameReference nameReference = (NameReference) this.value;
					final Binding nameReferenceBinding = nameReference.binding;
					if (nameReferenceBinding.kind() == Binding.FIELD) {
						FieldBinding fieldBinding = (FieldBinding) nameReferenceBinding;
						if (!fieldBinding.declaringClass.isEnum()) {
							if (!fieldBinding.type.isArrayType()) {
								scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, this.value, true);
							} else {
								scope.problemReporter().annotationValueMustBeArrayInitializer(this.binding.declaringClass, this.name, this.value);
							}
						}
					}
				}  else {
					scope.problemReporter().annotationValueMustBeConstant(this.binding.declaringClass, this.name, this.value, true);
				}
				break checkAnnotationMethodType;
			}
			if (leafType.isAnnotationType()) {
				if (!valueType.leafComponentType().isAnnotationType()) { // check annotation type and also reject null literal
					scope.problemReporter().annotationValueMustBeAnnotation(this.binding.declaringClass, this.name, this.value, leafType);
				} else if (this.value instanceof ArrayInitializer) {
					ArrayInitializer initializer = (ArrayInitializer) this.value;
					final Expression[] expressions = initializer.expressions;
					if (expressions != null) {
						for (int i =0, max = expressions.length; i < max; i++) {
							Expression currentExpression = expressions[i];
							if (currentExpression instanceof NullLiteral || !(currentExpression instanceof Annotation)) {
								scope.problemReporter().annotationValueMustBeAnnotation(this.binding.declaringClass, this.name, currentExpression, leafType);
							}
						}
					}
				} else if (!(this.value instanceof Annotation)) {
					scope.problemReporter().annotationValueMustBeAnnotation(this.binding.declaringClass, this.name, this.value, leafType);
				}
				break checkAnnotationMethodType;
			}
		}
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.value != null) {
				this.value.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
