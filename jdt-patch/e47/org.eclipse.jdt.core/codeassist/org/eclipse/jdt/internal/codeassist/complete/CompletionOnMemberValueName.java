/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce annotation's attribute name containing the cursor.
 * e.g.
 *
 *	@Annot(attri[cursor]
 *	class X {
 *  }
 *
 *	---> @Annot(<CompletionOnAttributeName:attri>)
 *		 class X {
 *       }
 */
public class CompletionOnMemberValueName extends MemberValuePair {
	public CompletionOnMemberValueName(char[] token, int sourceStart, int sourceEnd) {
		super(token, sourceStart, sourceEnd, null);
	}

	public StringBuffer print(int indent, StringBuffer output) {
		output.append("<CompleteOnAttributeName:"); //$NON-NLS-1$
		output.append(this.name);
		output.append('>');
		return output;
	}
}
