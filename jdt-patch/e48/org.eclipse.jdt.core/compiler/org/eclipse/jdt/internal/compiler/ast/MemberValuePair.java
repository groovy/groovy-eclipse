// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								Bug 434570 - Generic type mismatch for parametrized class annotation attribute with inner class
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
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

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		output
			.append(this.name)
			.append(" = "); //$NON-NLS-1$
		this.value.print(0, output);
		return output;
	}

	// GROOVY add
	private static boolean isClass(TypeBinding requiredType) {
		if (requiredType.isArrayType()) {
			requiredType = requiredType.leafComponentType();
		}
		requiredType = requiredType.original();
		boolean isClass = requiredType.id == TypeIds.T_JavaLangClass;
		return isClass;
	}

	private static boolean isGroovy(Scope scope) {
		while (scope.parent != null) {
			scope = scope.parent;
		}
		return scope.getClass().getSimpleName().startsWith("Groovy"); //$NON-NLS-1$
	}

	private static Expression repairClassLiteralReference(Expression exp, BlockScope scope) {
		if (exp instanceof SingleNameReference) {
			if (exp.resolveType(scope) != null) {
				SingleNameReference ref = (SingleNameReference) exp;
				return new ClassLiteralAccess(ref.sourceEnd, new SingleTypeReference(ref.token, ((long) ref.sourceStart) << 32 | ref.sourceEnd));
			}
		} else if (exp instanceof QualifiedNameReference) {
			if (exp.resolveType(scope) != null) {
				QualifiedNameReference ref = (QualifiedNameReference) exp;
				return new ClassLiteralAccess(ref.sourceEnd, new QualifiedTypeReference(ref.tokens, ref.sourcePositions));
			}
		}
		return exp;
	}
	// GROOVY end

	public void resolveTypeExpecting(final BlockScope scope, final TypeBinding requiredType) {
		if (this.compilerElementPair != null) {
			return;
		}

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

		// GROOVY add -- handling for class literals that do not end in '.class'
		if (isClass(requiredType) && isGroovy(scope)) {
			if (this.value instanceof ArrayInitializer) {
				Expression[] values = ((ArrayInitializer) this.value).expressions;
				for (int i = 0, n = values.length; i < n; i += 1) {
					values[i] = repairClassLiteralReference(values[i], scope);
				}
			} else {
				this.value = repairClassLiteralReference(this.value, scope);
			}
		}
		// GROOVY end

		this.value.setExpectedType(requiredType); // needed in case of generic method invocation - looks suspect, generic method invocation here ???
		final TypeBinding valueType;
		if (this.value instanceof ArrayInitializer) {
			ArrayInitializer initializer = (ArrayInitializer) this.value;
			valueType = initializer.resolveTypeExpecting(scope, this.binding.returnType);
		} else if (this.value instanceof ArrayAllocationExpression) {
			scope.problemReporter().annotationValueMustBeArrayInitializer(this.binding.declaringClass, this.name, this.value);
			this.value.resolveType(scope);
			valueType = null; // no need to pursue
		} else {
			valueType = this.value.resolveType(scope);
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=248897
			ASTVisitor visitor = new ASTVisitor() {
				@Override
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

		final TypeBinding leafType = requiredType.leafComponentType();
		// the next check may need deferring:
		final boolean[] shouldExit = new boolean[1];
		Runnable check = new Runnable() {
			@Override
			public void run() {
				if (!(MemberValuePair.this.value.isConstantValueOfTypeAssignableToType(valueType, requiredType)
						|| valueType.isCompatibleWith(requiredType))) {
					if (!(requiredType.isArrayType()
							&& requiredType.dimensions() == 1
							&& (MemberValuePair.this.value.isConstantValueOfTypeAssignableToType(valueType, leafType)
									|| valueType.isCompatibleWith(leafType)))) {
						
						if (leafType.isAnnotationType() && !valueType.isAnnotationType()) {
							scope.problemReporter().annotationValueMustBeAnnotation(MemberValuePair.this.binding.declaringClass,
									MemberValuePair.this.name, MemberValuePair.this.value, leafType);
						} else {
							scope.problemReporter().typeMismatchError(valueType, requiredType, MemberValuePair.this.value, null);
						}
						shouldExit[0] = true; // TODO may allow to proceed to find more errors at once
					}
				} else {
					scope.compilationUnitScope().recordTypeConversion(requiredType.leafComponentType(), valueType.leafComponentType());
					MemberValuePair.this.value.computeConversion(scope, requiredType, valueType);
				}
			}
		};
		// ... now or later?
		if (!scope.deferCheck(check)) {
			check.run();
			if (shouldExit[0])
				return;
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

	@Override
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.value != null) {
				this.value.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.value != null) {
				this.value.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}
