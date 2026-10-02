/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 331649 - [compiler][null] consider null annotations for fields
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 458396 - NPE in CodeStream.invoke()
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public abstract class VariableBinding extends Binding {

	public int modifiers;
	public TypeBinding type;
	public char[] name;
	protected Constant constant;
	public int id; // for flow-analysis (position in flowInfo bit vector)

	public VariableBinding(char[] name, TypeBinding type, int modifiers, Constant constant) {
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
		this.constant = constant;
		if (type != null) {
			this.tagBits |= (type.tagBits & TagBits.HasMissingType);
		}
	}

	public Constant constant() {
		return this.constant;
	}

	/**
	 * Call this variant during resolve / analyse, so we can handle the case
	 * when a tentative lambda resolve triggers resolving of outside code.
	 */
	public Constant constant(Scope scope) {
		return constant();
	}

	@Override
	public abstract AnnotationBinding[] getAnnotations();

	public ReferenceBinding getDeclaringClass() {
		return null;
	}

	public final boolean isBlankFinal(){
		return (this.modifiers & ExtraCompilerModifiers.AccBlankFinal) != 0;
	}

	/* Answer true if the receiver is explicitly or implicitly final
	 * and cannot be changed. Resources on try and multi catch variables are
	 * marked as implicitly final.
	*/
	public final boolean isFinal() {
		return (this.modifiers & ClassFileConstants.AccFinal) != 0;
	}

	/* Answer true if the receiver is a static field
	*/
	public final boolean isStatic() {
		return (this.modifiers & ClassFileConstants.AccStatic) != 0;
	}

	public final boolean isEffectivelyFinal() {
		return (this.tagBits & TagBits.IsEffectivelyFinal) != 0;
	}

	/** Answer true if null annotations are enabled and this field is specified @NonNull */
	public boolean isNonNull() {
		return (this.tagBits & TagBits.AnnotationNonNull) != 0
				|| (this.type != null
					&& (this.type.tagBits & TagBits.AnnotationNonNull) != 0);
	}

	/** Answer true if null annotations are enabled and this field is specified @Nullable */
	public boolean isNullable() {
		return (this.tagBits & TagBits.AnnotationNullable) != 0
				|| (this.type != null
				&& (this.type.tagBits & TagBits.AnnotationNullable) != 0);
	}

	@Override
	public char[] readableName() {
		return this.name;
	}
	public void setConstant(Constant constant) {
		this.constant = constant;
	}
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder(10);
		ASTNode.printModifiers(this.modifiers & ~ExtraCompilerModifiers.AccOutOfFlowScope, output); // so pattern bindings don't show up as sealed (!)
		if ((this.modifiers & ExtraCompilerModifiers.AccUnresolved) != 0) {
			output.append("[unresolved] "); //$NON-NLS-1$
		}
		output.append(this.type != null ? this.type.debugName() : "<no type>"); //$NON-NLS-1$
		output.append(" "); //$NON-NLS-1$
		output.append((this.name != null) ? new String(this.name) : "<no name>"); //$NON-NLS-1$
		return output.toString();
	}

	public void clearEffectiveFinality(Scope scope, Expression node, boolean complain) {
		return;
	}

	/* Answer true if the receiver has public visibility
	*/
	public final boolean isPublic() {
		return (this.modifiers & ClassFileConstants.AccPublic) != 0;
	}

	/* Answer true if the receiver is deprecated
	*/
	public final boolean isDeprecated() {
		return (this.modifiers & ClassFileConstants.AccDeprecated) != 0;
	}

	/** Applicable only for {@link FieldBinding} and {@link RecordComponentBinding}. */
	public void fillInDefaultNonNullness(AbstractVariableDeclaration sourceField, Scope scope) {
		assert sourceField.getKind() != AbstractVariableDeclaration.LOCAL_VARIABLE;
		if (this.type == null || this.type.isBaseType())
			return;
		LookupEnvironment environment = scope.environment();
		if (environment.usesNullTypeAnnotations()) {
			if (!this.type.acceptsNonNullDefault())
				return;
			if ( (this.type.tagBits & TagBits.AnnotationNullMASK) == 0) {
				this.type = environment.createNonNullAnnotatedType(this.type);
			} else if ((this.type.tagBits & TagBits.AnnotationNonNull) != 0) {
				scope.problemReporter().nullAnnotationIsRedundant(sourceField);
			}
		} else {
			if ( (this.tagBits & TagBits.AnnotationNullMASK) == 0 ) {
				this.tagBits |= TagBits.AnnotationNonNull;
			} else if ((this.tagBits & TagBits.AnnotationNonNull) != 0) {
				scope.problemReporter().nullAnnotationIsRedundant(sourceField);
			}
		}
	}
}
