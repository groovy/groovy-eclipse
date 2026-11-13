/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce a message send containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      this.[start]bar[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnMessageSend:this.bar(1, 2)>
 *         }
 *       }
 */

import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnMessageSend extends MessageSend {

	@Override
	public StringBuilder printExpression(int indent, StringBuilder output) {

		output.append("<SelectOnMessageSend:"); //$NON-NLS-1$
		if (!this.receiver.isImplicitThis()) this.receiver.printExpression(0, output).append('.');
		output.append(this.selector).append('(');
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].printExpression(0, output);
			}
		}
		return output.append(")>"); //$NON-NLS-1$
	}

	@Override
	public TypeBinding resolveType(BlockScope scope) {

		TypeBinding type = super.resolveType(scope);
		if (type != null && type.isPolyType())
			return type; // wait for more inference/resolution

		// tolerate some error cases
		if(this.binding == null ||
					!(this.binding.isValidBinding() ||
						this.binding.problemId() == ProblemReasons.NotVisible
						|| this.binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
						|| this.binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
						|| this.binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext)) {
			throw new SelectionNodeFound();
		} else {
			throw new SelectionNodeFound(this.binding);
		}
	}
}
