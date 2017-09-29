/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.eval;

import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.GlobalVariable;
import org.eclipse.jdt.internal.eval.InstallException;

public class SimpleVariableTest extends SimpleTest {
void evaluateVariable() throws TargetException, InstallException {
	startEvaluationContext();
	GlobalVariable var = getVariable();
	INameEnvironment env = getEnv();
	this.context.evaluateVariables(env, null, this.requestor, getProblemFactory());
	this.context.deleteVariable(var);
	stopEvaluationContext();
}
public GlobalVariable getVariable() {
	return this.context.newVariable(
		"int".toCharArray(),
		"var".toCharArray(),
		"1".toCharArray());
}
public static void main(String[] args) throws TargetException, InstallException {
	SimpleVariableTest test = new SimpleVariableTest();
	test.evaluateVariable();
}
}
