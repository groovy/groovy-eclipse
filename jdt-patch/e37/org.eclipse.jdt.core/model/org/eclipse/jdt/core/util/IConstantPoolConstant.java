/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of constant pool constants as described in the JVM specifications.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IConstantPoolConstant {

	int CONSTANT_Class = 7;
	int CONSTANT_Fieldref = 9;
	int CONSTANT_Methodref = 10;
	int CONSTANT_InterfaceMethodref = 11;
	int CONSTANT_String = 8;
	int CONSTANT_Integer = 3;
	int CONSTANT_Float = 4;
	int CONSTANT_Long = 5;
	int CONSTANT_Double = 6;
	int CONSTANT_NameAndType = 12;
	int CONSTANT_Utf8 = 1;

	int CONSTANT_Methodref_SIZE = 5;
	int CONSTANT_Class_SIZE = 3;
	int CONSTANT_Double_SIZE = 9;
	int CONSTANT_Fieldref_SIZE = 5;
	int CONSTANT_Float_SIZE = 5;
	int CONSTANT_Integer_SIZE = 5;
	int CONSTANT_InterfaceMethodref_SIZE = 5;
	int CONSTANT_Long_SIZE = 9;
	int CONSTANT_String_SIZE = 3;
	int CONSTANT_Utf8_SIZE = 3;
	int CONSTANT_NameAndType_SIZE = 5;

}
