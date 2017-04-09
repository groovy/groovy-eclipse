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
package org.eclipse.jdt.core.util;

/**
 * Description of a constant pool entry as described in the JVM specifications.
 * Its contents is initialized according to its kind.
 *
 * This interface may be implemented by clients.
 *
 * @since 2.0
 */
public interface IConstantPoolEntry {

	/**
	 * Returns the type of this entry.
	 *
	 * @return the type of this entry
	 */
	int getKind();

	/**
	 * Returns the name index for a CONSTANT_Class type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the name index for a CONSTANT_Class type entry
	 */
	int getClassInfoNameIndex();

	/**
	 * Returns the class index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the class index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry
	 */
	int getClassIndex();

	/**
	 * Returns the nameAndType index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the nameAndType index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry
	 */
	int getNameAndTypeIndex();

	/**
	 * Returns the string index for a CONSTANT_String type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the string index for a CONSTANT_String type entry
	 */
	int getStringIndex();

	/**
	 * Returns the string value for a CONSTANT_String type entry.
	 * Returns null otherwise.
	 *
	 * @return the string value for a CONSTANT_String type entry
	 */
	String getStringValue();

	/**
	 * Returns the integer value for a CONSTANT_Integer type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the integer value for a CONSTANT_Integer type entry
	 */
	int getIntegerValue();

	/**
	 * Returns the float value for a CONSTANT_Float type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the float value for a CONSTANT_Float type entry
	 */
	float getFloatValue();

	/**
	 * Returns the double value for a CONSTANT_Double type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the double value for a CONSTANT_Double type entry
	 */
	double getDoubleValue();

	/**
	 * Returns the long value for a CONSTANT_Long type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the long value for a CONSTANT_Long type entry
	 */
	long getLongValue();

	/**
	 * Returns the descriptor index for a CONSTANT_NameAndType type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the descriptor index for a CONSTANT_NameAndType type entry
	 */
	int getNameAndTypeInfoDescriptorIndex();

	/**
	 * Returns the name index for a CONSTANT_NameAndType type entry.
	 * The value is unspecified otherwise.
	 *
	 * @return the name index for a CONSTANT_NameAndType type entry
	 */
	int getNameAndTypeInfoNameIndex();

	/**
	 * Returns the class name for a CONSTANT_Class type entry.
	 * Returns null otherwise.
	 *
	 * @return the class name for a CONSTANT_Class type entry
	 */
	char[] getClassInfoName();

	/**
	 * Returns the class name for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * Returns null otherwise.
	 *
	 * @return the class name for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry
	 */
	char[] getClassName();

	/**
	 * Returns the field name for a CONSTANT_Fieldref type entry.
	 * Returns null otherwise.
	 *
	 * @return the field name for a CONSTANT_Fieldref type entry
	 */
	char[] getFieldName();

	/**
	 * Returns the field name for a CONSTANT_Methodref or CONSTANT_InterfaceMethodred
	 * type entry.
	 * Returns null otherwise.
	 *
	 * @return the field name for a CONSTANT_Methodref or CONSTANT_InterfaceMethodred
	 * type entry
	 */
	char[] getMethodName();

	/**
	 * Returns the field descriptor value for a CONSTANT_Fieldref type entry. This value
	 * is set only when decoding the CONSTANT_Fieldref entry.
	 * Returns null otherwise.
	 *
	 * @return the field descriptor value for a CONSTANT_Fieldref type entry. This value
	 * is set only when decoding the CONSTANT_Fieldref entry
	 */
	char[] getFieldDescriptor();

	/**
	 * Returns the method descriptor value for a CONSTANT_Methodref or
	 * CONSTANT_InterfaceMethodref type entry. This value is set only when decoding the
	 * CONSTANT_Methodref or CONSTANT_InterfaceMethodref entry.
	 * Returns null otherwise.
	 *
	 * @return the method descriptor value for a CONSTANT_Methodref or
	 * CONSTANT_InterfaceMethodref type entry. This value is set only when decoding the
	 * CONSTANT_Methodref or CONSTANT_InterfaceMethodref entry
	 */
	char[] getMethodDescriptor();

	/**
	 * Returns the utf8 value for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry.
	 * Returns null otherwise.
	 *
	 * @return the utf8 value for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry
	 */
	char[] getUtf8Value();

	/**
	 * Returns the utf8 length for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry.
	 * Returns null otherwise.
	 *
	 * @return the utf8 length for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry
	 */
	int getUtf8Length();
}
