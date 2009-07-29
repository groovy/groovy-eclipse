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
package org.eclipse.jdt.internal.compiler.impl;

public class BooleanConstant extends Constant {

private boolean value;

private static final BooleanConstant TRUE = new BooleanConstant(true);
private static final BooleanConstant FALSE = new BooleanConstant(false);

public static BooleanConstant fromValue(boolean value) {
	return value ? BooleanConstant.TRUE : BooleanConstant.FALSE;
}
private BooleanConstant(boolean value) {
	this.value = value;
}

public boolean booleanValue() {
	return value;
}

public String stringValue() {
	//spec 15.17.11
	return String.valueOf(this.value);
}

public String toString(){
	return "(boolean)" + value ;  //$NON-NLS-1$
}

public int typeID() {
	return T_boolean;
}
}
