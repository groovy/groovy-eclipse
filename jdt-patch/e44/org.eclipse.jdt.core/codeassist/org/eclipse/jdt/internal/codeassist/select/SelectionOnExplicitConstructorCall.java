/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
 * reduce an explicit constructor call containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      Y.[start]super[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnExplicitConstructorCall:Y.super(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnExplicitConstructorCall extends ExplicitConstructorCall {

	public SelectionOnExplicitConstructorCall(int accessMode) {

		super(accessMode);
	}

	public StringBuffer printStatement(int tab, StringBuffer output) {

		printIndent(tab, output);
		output.append("<SelectOnExplicitConstructorCall:"); //$NON-NLS-1$
		if (this.qualification != null) this.qualification.printExpression(0, output).append('.');
		if (this.accessMode == This) {
			output.append("this("); //$NON-NLS-1$
		} else {
			output.append("super("); //$NON-NLS-1$
		}
		if (this.arguments != null) {
			for (int i = 0; i < this.arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.arguments[i].printExpression(0, output);
			}
		}
		return output.append(")>;"); //$NON-NLS-1$
	}

	public void resolve(BlockScope scope) {

		super.resolve(scope);

		// tolerate some error cases
		if (this.binding == null ||
				!(this.binding.isValidBinding() ||
					this.binding.problemId() == ProblemReasons.NotVisible))
			throw new SelectionNodeFound();
		else
			throw new SelectionNodeFound(this.binding);
	}
}
