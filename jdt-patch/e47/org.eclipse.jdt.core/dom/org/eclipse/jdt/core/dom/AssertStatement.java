/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Assert statement AST node type.
 *
 * <pre>
 * AssertStatement:
 *    <b>assert</b> Expression [ <b>:</b> Expression ] <b>;</b>
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class AssertStatement extends Statement {

	/**
	 * The "expression" structural property of this node type (child type: {@link Expression}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY =
		new ChildPropertyDescriptor(AssertStatement.class, "expression", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "message" structural property of this node type (child type: {@link Expression}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor MESSAGE_PROPERTY =
		new ChildPropertyDescriptor(AssertStatement.class, "message", Expression.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List properyList = new ArrayList(3);
		createPropertyList(AssertStatement.class, properyList);
		addProperty(EXPRESSION_PROPERTY, properyList);
		addProperty(MESSAGE_PROPERTY, properyList);
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
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * The message expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalMessageExpression = null;

	/**
	 * Creates a new unparented assert statement node owned by the given
	 * AST. By default, the assert statement has an unspecified, but legal,
	 * expression, and not message expression.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	AssertStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
			} else {
				setExpression((Expression) child);
				return null;
			}
		}
		if (property == MESSAGE_PROPERTY) {
			if (get) {
				return getMessage();
			} else {
				setMessage((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return ASSERT_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		AssertStatement result = new AssertStatement(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.copyLeadingComment(this);
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		result.setMessage(
			(Expression) ASTNode.copySubtree(target, getMessage()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getExpression());
			acceptChild(visitor, getMessage());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the first expression of this assert statement.
	 *
	 * @return the expression node
	 */
	public Expression getExpression() {
		if (this.expression == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.expression == null) {
					preLazyInit();
					this.expression = new SimpleName(this.ast);
					postLazyInit(this.expression, EXPRESSION_PROPERTY);
				}
			}
		}
		return this.expression;
	}

	/**
	 * Sets the first expression of this assert statement.
	 *
	 * @param expression the new expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an AssertStatement may occur inside an Expression - must check cycles
		ASTNode oldChild = this.expression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.expression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	/**
	 * Returns the message expression of this assert statement, or
	 * <code>null</code> if there is none.
	 *
	 * @return the message expression node, or <code>null</code> if there
	 *    is none
	 */
	public Expression getMessage() {
		return this.optionalMessageExpression;
	}

	/**
	 * Sets or clears the message expression of this assert statement.
	 *
	 * @param expression the message expression node, or <code>null</code> if
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setMessage(Expression expression) {
		// an AsertStatement may occur inside an Expression - must check cycles
		ASTNode oldChild = this.optionalMessageExpression;
		preReplaceChild(oldChild, expression, MESSAGE_PROPERTY);
		this.optionalMessageExpression = expression;
		postReplaceChild(oldChild, expression, MESSAGE_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 2 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.expression == null ? 0 : getExpression().treeSize())
			+ (this.optionalMessageExpression == null ? 0 : getMessage().treeSize());

	}
}

