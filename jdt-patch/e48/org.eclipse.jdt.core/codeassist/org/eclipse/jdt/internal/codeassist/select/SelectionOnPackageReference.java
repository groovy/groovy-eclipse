/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 * reduce an package statement containing the assist identifier.
 * e.g.
 *
 *  package java.[start]io[end];
 *	class X {
 *    void foo() {
 *    }
 *  }
 *
 *	---> <SelectOnPackage:java.io>
 *		 class X {
 *         void foo() {
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class SelectionOnPackageReference extends ImportReference {
public SelectionOnPackageReference(char[][] tokens , long[] positions) {
	super(tokens, positions, false, ClassFileConstants.AccDefault);
}
public StringBuffer print(int tab, StringBuffer output, boolean withOnDemand) {
	printIndent(tab, output).append("<SelectOnPackage:"); //$NON-NLS-1$
	for (int i = 0; i < this.tokens.length; i++) {
		if (i > 0) output.append('.');
		output.append(this.tokens[i]);
	}
	return output.append('>');
}
}
