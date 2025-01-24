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
 * reduce a single name reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      ba[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnName:ba>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CompletionOnSingleNameReference extends SingleNameReference implements CompletionNode {

	public char[][] possibleKeywords;
	public boolean canBeExplicitConstructor;
	public boolean isInsideAnnotationAttribute;
	public boolean isPrecededByModifiers;

	public CompletionOnSingleNameReference(char[] source, long pos, boolean isInsideAnnotationAttribute) {
		this(source, pos, null, false, isInsideAnnotationAttribute);
	}

	public CompletionOnSingleNameReference(char[] source, long pos, char[][] possibleKeywords, boolean canBeExplicitConstructor, boolean isInsideAnnotationAttribute) {
		super(source, pos);
		this.possibleKeywords = possibleKeywords;
		this.canBeExplicitConstructor = canBeExplicitConstructor;
		this.isInsideAnnotationAttribute = isInsideAnnotationAttribute;
	}

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {

		output.append("<CompleteOnName:"); //$NON-NLS-1$
		return super.printExpression(0, output).append('>');
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {
		CompletionNodeFound exception;
		if(scope instanceof MethodScope) {
			exception = new CompletionNodeFound(this, scope, ((MethodScope)scope).insideTypeAnnotation);
		} else {
			exception = new CompletionNodeFound(this, scope);
		}
		return exception.throwOrDeferAndReturn(() -> {
			// probably not in the position to do useful resolution, just provide some bindings
			char[][] compoundName = new char[][] { this.token };
			this.binding = new ProblemBinding(compoundName, ProblemReasons.NotFound);
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		});
	}
}
