/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * String fragment node is similar to StringLiteral and TextBlock, but does not include the delimiters.
 *
 * @since 3.37
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class StringFragment extends Expression {

	/**
	 * The "escapedValue" structural property of this node type (type: {@link String}).
	 *
	 * @since 3.37
	 */
	public static final SimplePropertyDescriptor ESCAPED_VALUE_PROPERTY = new SimplePropertyDescriptor(
			StringFragment.class, "escapedValue", String.class, MANDATORY); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(StringFragment.class, propertyList);
		addProperty(ESCAPED_VALUE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type. Clients must not modify the result.
	 *
	 * @param apiLevel
	 *            the API level; one of the <code>AST.JLS*</code> constants
	 *
	 * @return a list of property descriptors (element type: {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * The literal string not including the delimiters; defaults to the literal for the empty string.
	 */
	private String escapedValue = "";//$NON-NLS-1$

	/**
	 * Creates a new unparented string literal node owned by the given AST. By default, the string literal denotes the
	 * empty string.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast
	 *            the AST that is to own this node
	 */
	StringFragment(AST ast) {
		super(ast);
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == ESCAPED_VALUE_PROPERTY) {
			if (get) {
				return getEscapedValue();
			} else {
				setEscapedValue((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}

	@Override
	final int getNodeType0() {
		return STRING_FRAGMENT;
	}

	@Override
	ASTNode clone0(AST target) {
		StringFragment result = new StringFragment(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setEscapedValue(getEscapedValue());
		return result;
	}

	@Override
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	/**
	 * Returns the string value of this literal node to the given string fragment token. The token is the sequence of
	 * characters that would appear in the source program, excluding enclosing double quotes.
	 *
	 * @return the string fragment token, excluding enclosing double quotes
	 */
	public String getEscapedValue() {
		return this.escapedValue;
	}

	/**
	 * Sets the string value of this fragment node to the given string fragment token. The token is the sequence of
	 * characters that would appear in the source program, excluding enclosing double quotes but including embedded
	 * escapes. For example, the following String template <code> STR."Hello \{last}, \{first}!"</code> considered to
	 * have the following literl values
	 * <ul>
	 * <li><code>"Hello ")</code></li>
	 * <li><code>", "</code></li>
	 * <li><code>"!"</code></li>
	 * </ul>
	 *
	 * @param token
	 *            the string fragment token, excluding enclosing double quotes
	 * @exception IllegalArgumentException
	 *                if the argument is incorrect
	 */
	public void setEscapedValue(String token) {
		if (token == null) {
			throw new IllegalArgumentException("Token cannot be null"); //$NON-NLS-1$
		}
		preValueChange(ESCAPED_VALUE_PROPERTY);
		this.escapedValue = token;
		postValueChange(ESCAPED_VALUE_PROPERTY);
	}

	void internalSetEscapedValue(String token) {
		preValueChange(ESCAPED_VALUE_PROPERTY);
		this.escapedValue = token;
		postValueChange(ESCAPED_VALUE_PROPERTY);
	}

	@Override
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4 + stringSize(this.escapedValue);
		return size;
	}

	@Override
	int treeSize() {
		return memSize();
	}
}
