/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class SelectionOnNameOfMemberValuePair extends MemberValuePair {

	public SelectionOnNameOfMemberValuePair(char[] token, int sourceStart, int sourceEnd, Expression value) {
		super(token, sourceStart, sourceEnd, value);
	}

	public StringBuffer print(int indent, StringBuffer output) {
		output.append("<SelectOnName:"); //$NON-NLS-1$
		output.append(this.name);
		output.append(">"); //$NON-NLS-1$
		return output;
	}

	public void resolveTypeExpecting(BlockScope scope, TypeBinding requiredType) {
		super.resolveTypeExpecting(scope, requiredType);

		if(this.binding != null) {
			throw new SelectionNodeFound(this.binding);
		}
		throw new SelectionNodeFound();
	}
}
