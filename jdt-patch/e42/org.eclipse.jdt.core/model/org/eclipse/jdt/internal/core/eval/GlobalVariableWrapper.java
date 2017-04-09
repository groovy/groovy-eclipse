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
package org.eclipse.jdt.internal.core.eval;

import org.eclipse.jdt.core.eval.IGlobalVariable;
import org.eclipse.jdt.internal.eval.GlobalVariable;

/**
 * A wrapper around the infrastructure global variable.
 */
class GlobalVariableWrapper implements IGlobalVariable {
	GlobalVariable variable;
/**
 * Creates a new wrapper around the given infrastructure global variable.
 */
GlobalVariableWrapper(GlobalVariable variable) {
	this.variable = variable;
}
/**
 * @see org.eclipse.jdt.core.eval.IGlobalVariable#getInitializer
 */
public String getInitializer() {
	char[] initializer = this.variable.getInitializer();
	if (initializer != null) {
		return new String(initializer);
	} else {
		return null;
	}
}
/**
 * @see org.eclipse.jdt.core.eval.IGlobalVariable#getName
 */
public String getName() {
	return new String(this.variable.getName());
}
/**
 * @see org.eclipse.jdt.core.eval.IGlobalVariable#getTypeName
 */
public String getTypeName() {
	return new String(this.variable.getTypeName());
}
}
