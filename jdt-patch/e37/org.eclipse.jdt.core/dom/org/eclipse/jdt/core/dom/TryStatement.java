/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 * Try statement AST node type.
 *
 * <pre>
 * TryStatement:
 *     <b>try</b> Block
 *         { CatchClause }
 *         [ <b>finally</b> Block ]
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class TryStatement extends Statement {

	/**
	 * The "body" structural property of this node type (child type: {@link Block}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY =
		new ChildPropertyDescriptor(TryStatement.class, "body", Block.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "catchClauses" structural property of this node type (element type: {@link CatchClause}).
	 * @since 3.0
	 */
	public static final ChildListPropertyDescriptor CATCH_CLAUSES_PROPERTY =
		new ChildListPropertyDescriptor(TryStatement.class, "catchClauses", CatchClause.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "finally" structural property of this node type (child type: {@link Block}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor FINALLY_PROPERTY =
		new ChildPropertyDescriptor(TryStatement.class, "finally", Block.class, OPTIONAL, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(4);
		createPropertyList(TryStatement.class, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		addProperty(CATCH_CLAUSES_PROPERTY, propertyList);
		addProperty(FINALLY_PROPERTY, propertyList);
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
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * The body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * The catch clauses (element type: {@link CatchClause}).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList catchClauses =
		new ASTNode.NodeList(CATCH_CLAUSES_PROPERTY);

	/**
	 * The finally block, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Block optionalFinallyBody = null;


	/**
	 * Creates a new AST node for a try statement owned by the given
	 * AST. By default, the try statement has an empty block, no catch
	 * clauses, and no finally block.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	TryStatement(AST ast) {
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
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody((Block) child);
				return null;
			}
		}
		if (property == FINALLY_PROPERTY) {
			if (get) {
				return getFinally();
			} else {
				setFinally((Block) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == CATCH_CLAUSES_PROPERTY) {
			return catchClauses();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return TRY_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		TryStatement result = new TryStatement(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.copyLeadingComment(this);
		result.setBody((Block) getBody().clone(target));
		result.catchClauses().addAll(
			ASTNode.copySubtrees(target, catchClauses()));
		result.setFinally(
			(Block) ASTNode.copySubtree(target, getFinally()));
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
			acceptChild(visitor, getBody());
			acceptChildren(visitor, this.catchClauses);
			acceptChild(visitor, getFinally());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the body of this try statement.
	 *
	 * @return the try body
	 */
	public Block getBody() {
		if (this.body == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.body == null) {
					preLazyInit();
					this.body = new Block(this.ast);
					postLazyInit(this.body, BODY_PROPERTY);
				}
			}
		}
		return this.body;
	}

	/**
	 * Sets the body of this try statement.
	 *
	 * @param body the block node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setBody(Block body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.body;
		preReplaceChild(oldChild, body, BODY_PROPERTY);
		this.body = body;
		postReplaceChild(oldChild, body, BODY_PROPERTY);
	}

	/**
	 * Returns the live ordered list of catch clauses for this try statement.
	 *
	 * @return the live list of catch clauses
	 *    (element type: {@link CatchClause})
	 */
	public List catchClauses() {
		return this.catchClauses;
	}

	/**
	 * Returns the finally block of this try statement, or <code>null</code> if
	 * this try statement has <b>no</b> finally block.
	 *
	 * @return the finally block, or <code>null</code> if this try statement
	 *    has none
	 */
	public Block getFinally() {
		return this.optionalFinallyBody;
	}

	/**
	 * Sets or clears the finally block of this try statement.
	 *
	 * @param block the finally block node, or <code>null</code> if
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setFinally(Block block) {
		ASTNode oldChild = this.optionalFinallyBody;
		preReplaceChild(oldChild, block, FINALLY_PROPERTY);
		this.optionalFinallyBody = block;
		postReplaceChild(oldChild, block, FINALLY_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.body == null ? 0 : getBody().treeSize())
			+ this.catchClauses.listSize()
			+ (this.optionalFinallyBody == null ? 0 : getFinally().treeSize());
	}
}
