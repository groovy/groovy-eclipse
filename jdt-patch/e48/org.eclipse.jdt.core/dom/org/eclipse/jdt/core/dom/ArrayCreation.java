/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Array creation expression AST node type.
 * <pre>
 * ArrayCreation:
 *    <b>new</b> PrimitiveType <b>[</b> Expression <b>]</b> { <b>[</b> Expression <b>]</b> } { <b>[</b> <b>]</b> }
 *    <b>new</b> TypeName [ <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b> ]
 *        <b>[</b> Expression <b>]</b> { <b>[</b> Expression <b>]</b> } { <b>[</b> <b>]</b> }
 *    <b>new</b> PrimitiveType <b>[</b> <b>]</b> { <b>[</b> <b>]</b> } ArrayInitializer
 *    <b>new</b> TypeName [ <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b> ]
 *        <b>[</b> <b>]</b> { <b>[</b> <b>]</b> } ArrayInitializer
 * </pre>
 * <p>
 * The mapping from Java language syntax to AST nodes is as follows:
 * <ul>
 * <li>the type node is the array type of the creation expression,
 *   with one level of array per set of square brackets,</li>
 * <li>the dimension expressions are collected into the <code>dimensions</code>
 *   list.</li>
 * </ul>
 * </p>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ArrayCreation extends Expression {

	/**
	 * The "type" structural property of this node type (child type: {@link ArrayType}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY =
		new ChildPropertyDescriptor(ArrayCreation.class, "type", ArrayType.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "dimensions" structural property of this node type (element type: {@link Expression}).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor DIMENSIONS_PROPERTY =
		new ChildListPropertyDescriptor(ArrayCreation.class, "dimensions", Expression.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "initializer" structural property of this node type (child type: {@link ArrayInitializer}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor INITIALIZER_PROPERTY =
		new ChildPropertyDescriptor(ArrayCreation.class, "initializer", ArrayInitializer.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List properyList = new ArrayList(4);
		createPropertyList(ArrayCreation.class, properyList);
		addProperty(TYPE_PROPERTY, properyList);
		addProperty(DIMENSIONS_PROPERTY, properyList);
		addProperty(INITIALIZER_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * The array type; lazily initialized; defaults to a unspecified,
	 * legal array type.
	 */
	private ArrayType arrayType = null;

	/**
	 * The list of dimension expressions (element type:
	 * {@link Expression}). Defaults to an empty list.
	 */
	private ASTNode.NodeList dimensions =
		new ASTNode.NodeList(DIMENSIONS_PROPERTY);

	/**
	 * The optional array initializer, or <code>null</code> if none;
	 * defaults to none.
	 */
	private ArrayInitializer optionalInitializer = null;

	/**
	 * Creates a new AST node for an array creation expression owned by the
	 * given AST. By default, the array type is an unspecified 1-dimensional
	 * array, the list of dimensions is empty, and there is no array
	 * initializer.
	 *
	 * @param ast the AST that is to own this node
	 */
	ArrayCreation(AST ast) {
		super(ast);
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == INITIALIZER_PROPERTY) {
			if (get) {
				return getInitializer();
			} else {
				setInitializer((ArrayInitializer) child);
				return null;
			}
		}
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((ArrayType) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == DIMENSIONS_PROPERTY) {
			return dimensions();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final int getNodeType0() {
		return ARRAY_CREATION;
	}

	@Override
	ASTNode clone0(AST target) {
		ArrayCreation result = new ArrayCreation(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setType((ArrayType) getType().clone(target));
		result.dimensions().addAll(ASTNode.copySubtrees(target, dimensions()));
		result.setInitializer(
			(ArrayInitializer) ASTNode.copySubtree(target, getInitializer()));
		return result;
	}

	@Override
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getType());
			acceptChildren(visitor, this.dimensions);
			acceptChild(visitor, getInitializer());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the array type in this array creation expression.
	 *
	 * @return the array type
	 */
	public ArrayType getType() {
		if (this.arrayType == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.arrayType == null) {
					preLazyInit();
					this.arrayType = this.ast.newArrayType(
							this.ast.newPrimitiveType(PrimitiveType.INT));
					postLazyInit(this.arrayType, TYPE_PROPERTY);
				}
			}
		}
		return this.arrayType;
	}

	/**
	 * Sets the array type in this array creation expression.
	 *
	 * @param type the new array type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setType(ArrayType type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		// an ArrayCreation cannot occur inside a ArrayType - cycles not possible
		ASTNode oldChild = this.arrayType;
		preReplaceChild(oldChild, type, TYPE_PROPERTY);
		this.arrayType = type;
		postReplaceChild(oldChild, type, TYPE_PROPERTY);
	}

	/**
	 * Returns the live ordered list of dimension expressions in this array
	 * initializer.
	 *
	 * @return the live list of dimension expressions
	 *    (element type: {@link Expression})
	 */
	public List dimensions() {
		return this.dimensions;
	}

	/**
	 * Returns the array initializer of this array creation expression, or
	 * <code>null</code> if there is none.
	 *
	 * @return the array initializer node, or <code>null</code> if
	 *    there is none
	 */
	public ArrayInitializer getInitializer() {
		return this.optionalInitializer;
	}

	/**
	 * Sets or clears the array initializer of this array creation expression.
	 *
	 * @param initializer the array initializer node, or <code>null</code>
	 *    if there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setInitializer(ArrayInitializer initializer) {
		// an ArrayCreation may occur inside an ArrayInitializer
		// must check cycles
		ASTNode oldChild = this.optionalInitializer;
		preReplaceChild(oldChild, initializer, INITIALIZER_PROPERTY);
		this.optionalInitializer = initializer;
		postReplaceChild(oldChild, initializer, INITIALIZER_PROPERTY);
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}

	@Override
	int treeSize() {
		int size = memSize()
			+ (this.arrayType == null ? 0 : getType().treeSize())
			+ (this.optionalInitializer == null ? 0 : getInitializer().treeSize())
			+ this.dimensions.listSize();
		return size;
	}
}

