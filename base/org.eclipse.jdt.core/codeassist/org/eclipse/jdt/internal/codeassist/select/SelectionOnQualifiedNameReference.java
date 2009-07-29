/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a qualified name reference containing the assist identifier.
 * e.g.
 *
 *	class X {
 *    Y y;
 *    void foo() {
 *      y.fred.[start]ba[end]
 *    }
 *  }
 *
 *	---> class X {
 *         Y y;
 *         void foo() {
 *           <SelectOnName:y.fred.ba>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnQualifiedNameReference extends QualifiedNameReference {

public SelectionOnQualifiedNameReference(char[][] previousIdentifiers, char[] selectionIdentifier, long[] positions) {
	super(
		CharOperation.arrayConcat(previousIdentifiers, selectionIdentifier),
		positions,
		(int) (positions[0] >>> 32),
		(int) positions[positions.length - 1]);
}
public StringBuffer printExpression(int indent, StringBuffer output) {

	output.append("<SelectOnName:"); //$NON-NLS-1$
	for (int i = 0, length = tokens.length; i < length; i++) {
		if (i > 0) output.append('.');
		output.append(tokens[i]);
	}
	return output.append('>'); 
}
public TypeBinding resolveType(BlockScope scope) {
	// it can be a package, type, member type, local variable or field
	binding = scope.getBinding(tokens, this);
	if (!binding.isValidBinding()) {
		if (binding instanceof ProblemFieldBinding) {
			// tolerate some error cases
			if (binding.problemId() == ProblemReasons.NotVisible
					|| binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
					|| binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
					|| binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext) {
				throw new SelectionNodeFound(binding);
			}
			scope.problemReporter().invalidField(this, (FieldBinding) binding);
		} else if (binding instanceof ProblemReferenceBinding || binding instanceof MissingTypeBinding) {
			// tolerate some error cases
			if (binding.problemId() == ProblemReasons.NotVisible){
				throw new SelectionNodeFound(binding);
			}
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else {
			scope.problemReporter().unresolvableReference(this, binding);
		}
		throw new SelectionNodeFound();
	}
	throw new SelectionNodeFound(binding);
}
}
