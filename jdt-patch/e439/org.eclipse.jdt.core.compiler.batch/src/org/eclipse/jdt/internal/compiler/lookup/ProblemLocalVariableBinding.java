/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.internal.compiler.lookup;

public class ProblemLocalVariableBinding extends LocalVariableBinding {
	private final int problemId;
	public LocalVariableBinding closestMatch;

public ProblemLocalVariableBinding(LocalVariableBinding closestMatch, int problemId) {
	super(closestMatch.name, closestMatch.type, closestMatch.modifiers, closestMatch.isParameter());
	this.problemId = problemId;
	this.closestMatch = closestMatch;
}

@Override
public final int problemId() {
	return this.problemId;
}
}
