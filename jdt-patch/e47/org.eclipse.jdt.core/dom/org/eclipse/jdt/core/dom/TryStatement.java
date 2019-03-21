/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
 * Try statement AST node type.
 * <pre>
 * TryStatement:
 *     <b>try</b> [ <b>(</b> Resources <b>)</b> ]
 *         Block
 *         [ { CatchClause } ]
 *         [ <b>finally</b> Block ]
 * </pre>
 *
 * <p>
 * Not all node arrangements will represent legal Java constructs. In particular,
 * at least one resource, catch clause, or finally block must be present.</p>
 * 
 * <p>A resource is either a {@link VariableDeclarationExpression} or (since JLS9) a {@link Name}.</p>
 * 
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TryStatement extends Statement {

	
	/**
	 * The "resources" structural property of this node type (element type: {@link VariableDeclarationExpression}) (added in JLS4 API).
	 * @deprecated In the JLS9 API, this property is replaced by {@link #RESOURCES2_PROPERTY}.
	 * @since 3.7.1
	 */
	public static final ChildListPropertyDescriptor RESOURCES_PROPERTY =
		new ChildListPropertyDescriptor(TryStatement.class, "resources", VariableDeclarationExpression.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "resources" structural property of this node type (element type: {@link Expression}) (added in JLS9 API).
	 * @since 3.14
	 */
	public static final ChildListPropertyDescriptor RESOURCES2_PROPERTY =
		new ChildListPropertyDescriptor(TryStatement.class, "resources", Expression.class, CYCLE_RISK); //$NON-NLS-1$

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
	
	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.7
	 */
	private static final List PROPERTY_DESCRIPTORS_4_0;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.14
	 */
	private static final List PROPERTY_DESCRIPTORS_9_0;

	static {
		List propertyList = new ArrayList(4);
		createPropertyList(TryStatement.class, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		addProperty(CATCH_CLAUSES_PROPERTY, propertyList);
		addProperty(FINALLY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);

		propertyList = new ArrayList(5);
		createPropertyList(TryStatement.class, propertyList);
		addProperty(RESOURCES_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		addProperty(CATCH_CLAUSES_PROPERTY, propertyList);
		addProperty(FINALLY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_4_0 = reapPropertyList(propertyList);

		propertyList = new ArrayList(5);
		createPropertyList(TryStatement.class, propertyList);
		addProperty(RESOURCES2_PROPERTY, propertyList);
		addProperty(BODY_PROPERTY, propertyList);
		addProperty(CATCH_CLAUSES_PROPERTY, propertyList);
		addProperty(FINALLY_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_9_0 = reapPropertyList(propertyList);
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
		switch (apiLevel) {
			case AST.JLS2_INTERNAL :
			case AST.JLS3_INTERNAL :
				return PROPERTY_DESCRIPTORS;
			case AST.JLS4_INTERNAL :
			case AST.JLS8_INTERNAL :
				return PROPERTY_DESCRIPTORS_4_0;
			default :
				return PROPERTY_DESCRIPTORS_9_0;
		}
	}

	/**
	 * The resource expressions (element type: {@link Expression}).
	 * Null in JLS2 and JLS3. Added in JLS4.
	 * In the deprecated JLS4 and JLS8 APIs, this used to be
	 * (element type: {@link VariableDeclarationExpression}).
	 * Defaults to an empty list (see constructor).
	 * @since 3.7
	 */
	private ASTNode.NodeList resources = null;

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
	 * AST. By default, the try statement has no resources, an empty block, no catch
	 * clauses, and no finally block.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	TryStatement(AST ast) {
		super(ast);
		if (ast.apiLevel >= AST.JLS9_INTERNAL) {
			this.resources = new ASTNode.NodeList(RESOURCES2_PROPERTY);
		} else if (ast.apiLevel >= AST.JLS4_INTERNAL) {
			this.resources = new ASTNode.NodeList(RESOURCES_PROPERTY);
		}
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
		if (property == RESOURCES_PROPERTY || property == RESOURCES2_PROPERTY) {
			return resources();
		}
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
		if (this.ast.apiLevel >= AST.JLS4_INTERNAL) {
			result.resources().addAll(
					ASTNode.copySubtrees(target, resources()));
		}
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
			if (this.ast.apiLevel >= AST.JLS4_INTERNAL) {
				acceptChildren(visitor, this.resources);
			}
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

	/**
	 * Returns the live ordered list of resources for this try statement (added in JLS4 API).
	 * 
	 * <p>A resource is either a {@link VariableDeclarationExpression} or (since JLS9) a {@link Name}.</p>
	 *
	 * @return the live list of resources (element type: {@link Expression}).
	 *    In the deprecated JLS4 and JLS8 APIs, this used to be
	 *    (element type: {@link VariableDeclarationExpression}).
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2 or JLS3 AST
	 * @since 3.7.1
	 */
	public List resources() {
		// more efficient than just calling unsupportedIn2_3() to check
		if (this.resources == null) {
			unsupportedIn2_3();
		}
		return this.resources;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 4 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.resources == null ? 0 : this.resources.listSize())
			+ (this.body == null ? 0 : getBody().treeSize())
			+ this.catchClauses.listSize()
			+ (this.optionalFinallyBody == null ? 0 : getFinally().treeSize());
	}
}
