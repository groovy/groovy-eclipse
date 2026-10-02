/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * AST node for a text element within a doc comment.
 * <pre>
 * TextElement:
 *     Sequence of characters including a close comment delimiter <b>*</b><b>/</b>
 * </pre>
 *
 * @see Javadoc
 * @since 3.31
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public final class JavaDocTextElement extends AbstractTextElement {

	/**
	 * The "text" structural property of this node type (type: {@link String}).
	 */

	public static final SimplePropertyDescriptor TEXT_PROPERTY =
			internalTextPropertyFactory(JavaDocTextElement.class);

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(JavaDocTextElement.class, propertyList);
		addProperty(TEXT_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}


	@Override
	final SimplePropertyDescriptor internalTextPropertyFactory() {
		return TEXT_PROPERTY;
	}

	/**
	 * Creates a new AST node for a text element owned by the given AST.
	 * The new node has an empty text string.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	JavaDocTextElement(AST ast) {
		super(ast);
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}


	@Override
	final int getNodeType0() {
		return JAVADOC_TEXT_ELEMENT;
	}

	@Override
	ASTNode clone0(AST target) {
		JavaDocTextElement result = new JavaDocTextElement(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setText(getText());
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
	 * Sets the text of this node to the given value.
	 * <p>
	 * The text element typically includes leading and trailing
	 * whitespace that separates it from the immediately preceding
	 * or following elements.
	 * </p>
	 *
	 * @param text the text of this node
	 * @exception IllegalArgumentException if the text is null
	 * or contains a block comment closing delimiter
	 */
	@Override
	public void setText(String text) {
		super.setText(text);
		preValueChange(TEXT_PROPERTY);
		this.text = text;
		postValueChange(TEXT_PROPERTY);
	}

	@Override
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		if (this.text != Util.EMPTY_STRING) {
			// everything but our empty string costs
			size += stringSize(this.text);
		}
		return size;
	}

	@Override
	int treeSize() {
		return memSize();
	}
}

