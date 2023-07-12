/*******************************************************************************
 * Copyright (c) 2033 IBM Corporation and others.
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
 * Enhanced For statement AST node type with record pattern(added in JLS20 API).
 *
 * <pre>
 * EnhancedForStatement:
 *    <b>for</b> <b>(</b> RecordPattern <b>:</b> Expression <b>)</b>
 * 			Statement
 * </pre>
 *
 * @since 3.33
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
@SuppressWarnings("rawtypes")
public class EnhancedForWithRecordPattern extends Statement {

	/**
	 * The "pattern" structural property of this node type (child type: {@link RecordPattern}).
	 */
	public static final ChildPropertyDescriptor PATTERN_PROPERTY =
		new ChildPropertyDescriptor(EnhancedForWithRecordPattern.class, "pattern", RecordPattern.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "expression" structural property of this node type (child type: {@link Expression}).
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY =
		new ChildPropertyDescriptor(EnhancedForWithRecordPattern.class, "expression", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "body" structural property of this node type (child type: {@link Statement}).
	 */
	public static final ChildPropertyDescriptor BODY_PROPERTY =
		new ChildPropertyDescriptor(EnhancedForWithRecordPattern.class, "body", Statement.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List properyList = new ArrayList(4);
		createPropertyList(EnhancedForWithRecordPattern.class, properyList);
		addProperty(PATTERN_PROPERTY, properyList);
		addProperty(EXPRESSION_PROPERTY, properyList);
		addProperty(BODY_PROPERTY, properyList);
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
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * The record pattern; lazily initialized; defaults to a unspecified,
	 * legal node.
	 */
	private RecordPattern pattern = null;

	/**
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * The body statement; lazily initialized; defaults to an empty block
	 * statement.
	 */
	private Statement body = null;

	/**
	 * Creates a new AST node for an enchanced for statement owned by the
	 * given AST. By default, the record pattern and expression are unspecified
	 * but legal subtrees, and the body is an empty block.
	 *
	 * @param ast the AST that is to own this node
	 */
	EnhancedForWithRecordPattern(AST ast) {
		super(ast);
	    supportedOnlyIn20();
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == PATTERN_PROPERTY) {
			if (get) {
				return getPattern();
			} else {
				setPattern((RecordPattern) child);
				return null;
			}
		}
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
			} else {
				setExpression((Expression) child);
				return null;
			}
		}
		if (property == BODY_PROPERTY) {
			if (get) {
				return getBody();
			} else {
				setBody((Statement) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final int getNodeType0() {
		return ENHANCED_FOR_WITH_RECORD_PATTERN;
	}

	@Override
	ASTNode clone0(AST target) {
		EnhancedForWithRecordPattern result = new EnhancedForWithRecordPattern(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.copyLeadingComment(this);
		result.setPattern((RecordPattern) getPattern().clone(target));
		result.setExpression((Expression) getExpression().clone(target));
		result.setBody(
			(Statement) ASTNode.copySubtree(target, getBody()));
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
			acceptChild(visitor, getPattern());
			acceptChild(visitor, getExpression());
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the record pattern in this enhanced for statement.
	 *
	 * @return the pattern
	 */
	public RecordPattern getPattern() {
		if (this.pattern == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.pattern == null) {
					preLazyInit();
					this.pattern = this.ast.newRecordPattern();
					postLazyInit(this.pattern, PATTERN_PROPERTY);
				}
			}
		}
		return this.pattern;
	}

	/**
	 * Sets the record pattern in this enhanced for statement.
	 *
	 * @param pattern the new record pattern
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setPattern(RecordPattern pattern) {
		if (pattern == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.pattern;
		preReplaceChild(oldChild, pattern, PATTERN_PROPERTY);
		this.pattern = pattern;
		postReplaceChild(oldChild, pattern, PATTERN_PROPERTY);
	}

	/**
	 * Returns the expression of this enhanced for statement.
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
	 * Sets the expression of this enhanced for statement.
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
		ASTNode oldChild = this.expression;
		preReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
		this.expression = expression;
		postReplaceChild(oldChild, expression, EXPRESSION_PROPERTY);
	}

	/**
	 * Returns the body of this enchanced for statement.
	 *
	 * @return the body statement node
	 */
	public Statement getBody() {
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
	 * Sets the body of this enhanced for statement.
	 *
	 * @param statement the body statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setBody(Statement statement) {
		if (statement == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.body;
		preReplaceChild(oldChild, statement, BODY_PROPERTY);
		this.body = statement;
		postReplaceChild(oldChild, statement, BODY_PROPERTY);
	}

	@Override
	int memSize() {
		return super.memSize() + 3 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.pattern == null ? 0 : getPattern().treeSize())
			+ (this.expression == null ? 0 : getExpression().treeSize())
			+ (this.body == null ? 0 : getBody().treeSize());
	}
}
