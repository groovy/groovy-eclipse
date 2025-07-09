/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a qualified name reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    Y y;
 *    void foo() {
 *      y.fred.ba[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         Y y;
 *         void foo() {
 *           <CompleteOnName:y.fred.ba>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnQualifiedNameReference extends QualifiedNameReference implements CompletionNode {
	public char[] completionIdentifier;
	public boolean isInsideAnnotationAttribute;
public CompletionOnQualifiedNameReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions, boolean isInsideAnnotationAttribute) {
	super(previousIdentifiers, positions, (int) (positions[0] >>> 32), (int) positions[positions.length - 1]);
	this.completionIdentifier = completionIdentifier;
	this.isInsideAnnotationAttribute = isInsideAnnotationAttribute;
}
@Override
public StringBuilder printExpression(int indent, StringBuilder output) {

	output.append("<CompleteOnName:"); //$NON-NLS-1$
	for (char[] token : this.tokens) {
		output.append(token);
		output.append('.');
	}
	output.append(this.completionIdentifier).append('>');
	return output;
}
@Override
public TypeBinding resolveType(BlockScope scope) {
	// it can be a package, type, member type, local variable or field
	this.binding = scope.getBinding(this.tokens, this);
	if (!this.binding.isValidBinding()) {
		if (this.binding instanceof ProblemFieldBinding) {
			scope.problemReporter().invalidField(this, (FieldBinding) this.binding);
		} else if (this.binding instanceof ProblemReferenceBinding || this.binding instanceof MissingTypeBinding) {
			scope.problemReporter().invalidType(this, (TypeBinding) this.binding);
		} else {
			scope.problemReporter().unresolvableReference(this, this.binding);
		}

		if (this.binding.problemId() == ProblemReasons.NotFound) {
			throw new CompletionNodeFound(this, this.binding, scope);
		}

		throw new CompletionNodeFound();
	}

	return new CompletionNodeFound(this, this.binding, scope).throwOrDeferAndReturn(() -> {
		// probably not in the position to do useful resolution, just provide some binding
		// but perform minimal setup so downstream resolving doesn't throw exceptions:
		this.constant = Constant.NotAConstant;
		if ((this.bits & Binding.FIELD) != 0)
			this.binding = new ProblemFieldBinding(
					this.binding instanceof ReferenceBinding ? (ReferenceBinding) this.binding : null,
					this.completionIdentifier, ProblemReasons.NotFound);
		return this.resolvedType = new ProblemReferenceBinding(this.tokens, null, ProblemReasons.NotFound);
	});
}
}
