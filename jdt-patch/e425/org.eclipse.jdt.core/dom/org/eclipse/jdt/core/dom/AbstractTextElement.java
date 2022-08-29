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

import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * AST node for a text element within a doc comment.
 * <pre>
 * AbstractTextElement:
 *     Sequence of characters not including a close comment delimiter <b>*</b><b>/</b>
 * </pre>
 *
 * @see TextElement
 * @see JavaDocTextElement
 * @since 3.31
 */

@SuppressWarnings("rawtypes")
public abstract class AbstractTextElement extends ASTNode implements IDocElement {

	/**
	 * The "text" structural property of this node type (type: {@link String}).
	 *
	 */
	public static final SimplePropertyDescriptor internalTextPropertyFactory(Class nodeClass) {
		return new SimplePropertyDescriptor(nodeClass, "text", String.class, MANDATORY); //$NON-NLS-1$
 	}


	/**
	 * The text element; defaults to the empty string.
	 */
	String text = Util.EMPTY_STRING;


	/**
	 * Creates a new AST node for a text element owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	AbstractTextElement(AST ast) {
		super(ast);
	}

	/**
	 * Returns structural property descriptor for the "text" property
	 * of this node (element type: {@link String}).
	 *
	 * @return the property descriptor
	 */
	abstract SimplePropertyDescriptor internalTextPropertyFactory();


	/**
	 * Returns structural property descriptor for the "text" property
	 * of this node (child type: {@link String}).
	 *
	 * @return the property descriptor
	 */
	public final SimplePropertyDescriptor getTextNameProperty() {
		return internalTextPropertyFactory();
	}

	@Override
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == internalTextPropertyFactory()) {
			if (get) {
				return getText();
			} else {
				setText((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}
	/**
	 * Returns this node's tag name, or <code>null</code> if none.
	 * For top level doc tags such as parameter tags, the tag name
     * includes the "@" character ("@param").
	 * For inline doc tags such as link tags, the tag name
     * includes the "@" character ("@link").
     * The tag name may also be <code>null</code>; this is used to
     * represent the material at the start of a doc comment preceding
     * the first explicit tag.
     *
	 * @return the tag name, or <code>null</code> if none
	 */
	public String getText() {
		return this.text;
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
	 */
	public void setText(String text) {
		if (text == null) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns whether this tag element is nested within another
	 * tag element. Nested tag elements appears enclosed in
	 * "{" and "}"; certain doc tags, including "@link" and
	 * "@linkplain" are only meaningful as nested tags.
	 * Top-level (i.e., non-nested) doc tags begin on a new line;
	 * certain doc tags, including "@param" and
	 * "@see" are only meaningful as top-level tags.
	 * <p>
	 * This convenience methods checks to see whether the parent
	 * of this node is of type {@link org.eclipse.jdt.core.dom.AbstractTextElement}.
	 * </p>
	 *
	 * @return <code>true</code> if this node is a nested tag element,
	 * and false if this node is either parented by a doc comment node
	 * ({@link Javadoc}), or is unparented
	 */
	public boolean isNested() {
		return (getParent() instanceof AbstractTextElement);
	}

	@Override
	int memSize() {
		int size = BASE_NODE_SIZE + 2 * 3 + stringSize(this.text);
		return size;
	}

	@Override
	int treeSize() {
		return memSize();
	}
}
