/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added J2SE 1.5 support
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Represents a field declared in a type.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IField extends IMember, IAnnotatable {

/**
 * Returns the constant value associated with this field
 * or <code>null</code> if this field has none. The field needs to be static and final to have
 * a constant value.
 * Returns an instance of the wrapper type corresponding to the the type of the field.
 * <table border="1">
 * <tr>
 * <th>field type</th>
 * <th>wrapper type</th>
 * </tr>
 * <tr>
 * <td>int
 * </td>
 * <td>java.lang.Integer
 * </td>
 * </tr>
 * <tr>
 * <td>byte
 * </td>
 * <td>java.lang.Byte
 * </td>
 * </tr>
 * <tr>
 * <td>boolean
 * </td>
 * <td>java.lang.Boolean
 * </td>
 * </tr>
 * <tr>
 * <td>char
 * </td>
 * <td>java.lang.Character
 * </td>
 * </tr>
 * <tr>
 * <td>double
 * </td>
 * <td>java.lang.Double
 * </td>
 * </tr>
 * <tr>
 * <td>float
 * </td>
 * <td>java.lang.Float
 * </td>
 * </tr>
 * <tr>
 * <td>long
 * </td>
 * <td>java.lang.Long
 * </td>
 * </tr>
 * <tr>
 * <td>short
 * </td>
 * <td>java.lang.Short
 * </td>
 * </tr>
 * <tr>
 * <td>java.lang.String
 * </td>
 * <td>java.lang.String
 * </td>
 * </tr>
 * </table>
 *
 * @return  the constant value associated with this field or <code>null</code> if this field has none.
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
public Object getConstant() throws JavaModelException;
/**
 * Returns the simple name of this field.
 * @return the simple name of this field.
 */
String getElementName();
/**
 * Returns the binding key for this field only if the given field is {@link #isResolved() resolved}.
 * A binding key is a key that uniquely identifies this field. It allows access to generic info
 * for parameterized fields.
 *
 * <p>If the given field is not resolved, the returned key is simply the java element's key.
 * </p>
 * @return the binding key for this field
 * @see org.eclipse.jdt.core.dom.IBinding#getKey()
 * @see BindingKey
 * @see #isResolved()
 * @since 3.1
 */
String getKey();
/**
 * Returns the type signature of this field. For enum constants,
 * this returns the signature of the declaring enum class.
 * <p>
 * The type signature may be either unresolved (for source types)
 * or resolved (for binary types), and either basic (for basic types)
 * or rich (for parameterized types). See {@link Signature} for details.
 * </p>
 *
 * @return the type signature of this field
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 * @see Signature
 */
String getTypeSignature() throws JavaModelException;
/**
 * Returns whether this field represents an enum constant.
 *
 * @return whether this field represents an enum constant
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 * @since 3.1
 */
boolean isEnumConstant() throws JavaModelException;
/**
 * Returns whether this field represents a resolved field.
 * If a field is resolved, its key contains resolved information.
 *
 * @return whether this field represents a resolved field.
 * @since 3.1
 */
boolean isResolved();

}
