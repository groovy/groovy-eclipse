/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class OpensStatement extends PackageVisibilityStatement {

	public OpensStatement(ImportReference pkgRef) {
		this(pkgRef, null);
	}
	public OpensStatement(ImportReference pkgRef, ModuleReference[] targets) {
		super(pkgRef, targets);
	}
	@Override
	protected int computeSeverity(int problemId) {
		return ProblemSeverities.Warning;
	}
	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output);
		output.append("opens "); //$NON-NLS-1$
		super.print(0, output);
		output.append(";"); //$NON-NLS-1$
		return output;
	}

}
