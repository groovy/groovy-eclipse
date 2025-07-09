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

import java.util.List;

/**
 * AST node for a tag within a doc comment.
 * Tag elements nested within another tag element are called
 * inline doc tags.
 * <pre>
 * TagElement:
 *     [ <b>@</b> Identifier ] { DocElement }
 *     {tagProperty = tagValue}
 * DocElement:
 *     TextElement
 *     Name
 *     MethodRef
 *     MemberRef
 *     <b>{</b> TagElement <b>}</b>
 * </pre>
 *
 * @see Javadoc
 * @since 3.30
 */

@SuppressWarnings("rawtypes")
public abstract class AbstractTagElement extends ASTNode implements IDocElement {

	/**
	 * The "tagName" structural property of this node type (type: {@link String}).
	 *
	 * @since 3.30
	 */
	public static final SimplePropertyDescriptor internalTagNamePropertyFactory(Class nodeClass) {
		return new SimplePropertyDescriptor(nodeClass, "tagName", String.class, OPTIONAL); //$NON-NLS-1$
 	}

	/**
	 * The "fragments" structural property of this node type (element type: {@link IDocElement}).
	 * @since 3.30
	 */

	static final ChildListPropertyDescriptor internalFragmentsPropertyFactory(Class nodeClass) {
		return new ChildListPropertyDescriptor(nodeClass, "fragments", IDocElement.class, CYCLE_RISK); //$NON-NLS-1$
	}


	/**
	 * The tag name, or null if none; defaults to null.
	 */
	String optionalTagName = null;

	/**
	 * The list of doc elements (element type: {@link IDocElement}).
	 * Defaults to an empty list.
	 */
	ASTNode.NodeList fragments = new ASTNode.NodeList(internalFragmentsPropertyFactory());


	/**
	 * Creates a new AST node for a tag element owned by the given AST.
	 * The new node has no name and an empty list of fragments.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	AbstractTagElement(AST ast) {
		super(ast);
		this.fragments = new ASTNode.NodeList(internalFragmentsPropertyFactory());
	}

	/**
	 * Returns structural property descriptor for the "fragments" property
	 * of this node (element type: {@link IDocElement}).
	 *
	 * @return the property descriptor
	 */
	abstract ChildListPropertyDescriptor internalFragmentsPropertyFactory();

	/**
	 * Returns structural property descriptor for the "tagName" property
	 * of this node (element type: {@link String}).
	 *
	 * @return the property descriptor
	 */
	abstract SimplePropertyDescriptor internalTagNamePropertyFactory();


	/**
	 * Returns structural property descriptor for the "name" property
	 * of this node (child type: {@link SimpleName}).
	 *
	 * @return the property descriptor
	 * @since 3.1
	 */
	public final SimplePropertyDescriptor getTagNameProperty() {
		return internalTagNamePropertyFactory();
	}

	@Override
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == internalTagNamePropertyFactory()) {
			if (get) {
				return getTagName();
			} else {
				setTagName((String) value);
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
	public String getTagName() {
		return this.optionalTagName;
	}

	/**
	 * Sets the tag name of this node to the given value.
	 * For top level doc tags such as parameter tags, the tag name
	 * includes the "@" character ("@param").
	 * For inline doc tags such as link tags, the tag name
	 * includes the "@" character ("@link").
	 * The tag name may also be <code>null</code>; this is used to
	 * represent the material at the start of a doc comment preceding
	 * the first explicit tag.
	 *
	 * @param tagName the tag name, or <code>null</code> if none
	 */
	public void setTagName(String tagName) {
		preValueChange(internalTagNamePropertyFactory());
		this.optionalTagName = tagName;
		postValueChange(internalTagNamePropertyFactory());
	}

	/**
	 * Returns the live list of fragments in this tag element.
	 * <p>
	 * The fragments cover everything following the tag name
	 * (or everything if there is no tag name), and generally omit
	 * embedded line breaks (and leading whitespace on new lines,
	 * including any leading "*"). {@link org.eclipse.jdt.core.dom.AbstractTagElement}
	 * nodes are used to represent tag elements (e.g., "@link")
	 * nested within this tag element.
	 * </p>
	 * <p>
	 * Here are some typical examples:
	 * <ul>
	 * <li>"@see Foo#bar()" - TagElement with tag name "@see";
	 * fragments() contains a single MethodRef node</li>
	 * <li>"@param args the program arguments" -
	 * TagElement with tag name "@param";
	 * 2 fragments: SimpleName ("args"), TextElement
	 * (" the program arguments")</li>
	 * <li>"@return See {&#64;link #foo foo} instead." -
	 * TagElement with tag name "@return";
	 * 3 fragments: TextElement ("See "),
	 * TagElement (for "&#64;link #foo foo"),
	 * TextElement (" instead.")</li>
	 * </ul>
	 * The use of Name, MethodRef, and MemberRef nodes within
	 * tag elements allows these fragments to be queried for
	 * binding information.
	 * <p>
	 * Adding and removing nodes from this list affects this node
	 * dynamically. The nodes in this list may be of various
	 * types, including {@link TextElement},
	 * {@link org.eclipse.jdt.core.dom.AbstractTagElement}, {@link Name},
	 * {@link MemberRef}, and {@link MethodRef}.
	 * Clients should assume that the list of types may grow in
	 * the future, and write their code to deal with unexpected
	 * nodes types. However, attempts to add a non-prescribed type
	 * of node will trigger an exception.
	 *
	 * @return the live list of doc elements in this tag element
	 * (element type: {@link IDocElement})
	 */
	public List fragments() {
		return this.fragments;
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
	 * of this node is of type {@link org.eclipse.jdt.core.dom.AbstractTagElement}.
	 * </p>
	 *
	 * @return <code>true</code> if this node is a nested tag element,
	 * and false if this node is either parented by a doc comment node
	 * ({@link Javadoc}), or is unparented
	 */
	public boolean isNested() {
		return (getParent() instanceof AbstractTagElement);
	}

	@Override
	int memSize() {
		int size = BASE_NODE_SIZE + 2 * 3 + stringSize(this.optionalTagName);
		return size;
	}

	@Override
	int treeSize() {
		return memSize() + this.fragments.listSize();
	}
}
