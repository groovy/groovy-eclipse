/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/

package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.ast.ExportsStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
/*
 * Completion node build by the parser in any case it was intending to
 * reduce an exports reference containing the cursor location.
 * e.g.
 *
 *	module myModule {
 *  exports packageo[cursor];
 *  }
 *
 *	module myModule {
 *	---> <CompleteOnExport:packageo>
 *  }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */

public class CompletionOnExportReference extends ExportsStatement {

	public CompletionOnExportReference(ImportReference ref) {
		super(ref, null);
	}
	@Override
	public StringBuffer print(int indent, StringBuffer output) {

		printIndent(indent, output).append("<CompleteOnExport:"); //$NON-NLS-1$
		output.append(this.pkgName);
		return output.append('>');
	}

}
