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
package org.eclipse.jdt.internal.compiler.ast;

public class ExportsStatement extends PackageVisibilityStatement {

	public ExportsStatement(ImportReference pkgRef) {
		this(pkgRef, null);
	}
	public ExportsStatement(ImportReference pkgRef, ModuleReference[] targets) {
		super(pkgRef, targets);
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output);
		output.append("exports "); //$NON-NLS-1$
		super.print(0, output);
		output.append(";"); //$NON-NLS-1$
		return output;
	}

}
