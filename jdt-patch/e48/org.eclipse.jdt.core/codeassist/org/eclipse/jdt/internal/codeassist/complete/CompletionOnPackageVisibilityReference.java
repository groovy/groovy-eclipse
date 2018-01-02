/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.core.compiler.CharOperation;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an exports or an opens reference containing the cursor location.
 * e.g.
 *
 *	module myModule {
 *  exports packageo[cursor];
 *  opens packageo[cursor];

 *  }
 *
 *	module myModule {
 *	---> <CompleteOnPackageVisibilityReference:packageo>
 *  }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

public class CompletionOnPackageVisibilityReference extends CompletionOnImportReference {

	String pkgName;
	public CompletionOnPackageVisibilityReference(char[][] ident, long[] pos) {
		super(ident, pos, 0);
		this.pkgName = new String(CharOperation.concatWith(ident, '.'));
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output).append("<CompleteOnPackageVisibilityReference:"); //$NON-NLS-1$
		output.append(this.pkgName);
		return output.append('>');
	}

}
