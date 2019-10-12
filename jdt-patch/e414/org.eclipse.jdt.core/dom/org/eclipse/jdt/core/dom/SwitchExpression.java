/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
 * Switch expression AST node type (added in JEP 325).
 * <pre>
 * SwitchExpression:
 *		<b>switch</b> <b>(</b> Expression <b>)</b>
 * 			<b>{</b> { SwitchCase | Statement } <b>}</b>
 * SwitchCase:
 *		<b>case</b> [ Expression { <b>,</b> Expression } ]  <b>{ : | ->}</b>
 *		<b>default</b> <b>{ : | ->}</b>
 * </pre>
 * <code>SwitchCase</code> nodes are treated as a kind of
 * <code>Statement</code>.
 *
 * @since 3.18
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SwitchExpression extends Expression {

	/**
	 * The "expression" structural property of this node type (child type: {@link Expression}).
	 * @since 3.16
	 */
	public static final ChildPropertyDescriptor EXPRESSION_PROPERTY =
		new ChildPropertyDescriptor(SwitchExpression.class, "expression", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "statements" structural property of this node type (element type: {@link Statement}).
	 * @since 3.16
	 */
	public static final ChildListPropertyDescriptor STATEMENTS_PROPERTY =
		new ChildListPropertyDescriptor(SwitchExpression.class, "statements", Statement.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(SwitchExpression.class, propertyList);
		addProperty(EXPRESSION_PROPERTY, propertyList);
		addProperty(STATEMENTS_PROPERTY, propertyList);
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
	 * @since 3.16
	 */
	public static List propertyDescriptors(int apiLevel) {
		return propertyDescriptors(apiLevel, false);
	}
	
		/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @param previewEnabled previewEnabled flag
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.20
	 */
	public static List propertyDescriptors(int apiLevel, boolean previewEnabled) {
		if (apiLevel == AST.JLS13_INTERNAL && previewEnabled) {
			return PROPERTY_DESCRIPTORS;
		}
		return null;
	}

	/**
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * The statements and SwitchCase nodes
	 * (element type: {@link Statement}).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList statements =
		new ASTNode.NodeList(STATEMENTS_PROPERTY);

	/**
	 * Creates a new unparented switch statement node owned by the given
	 * AST. By default, the swicth statement has an unspecified, but legal,
	 * expression, and an empty list of switch groups.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 * @exception UnsupportedOperationException if this operation is used other than JLS13
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 */
	SwitchExpression(AST ast) {
		super(ast);
		supportedOnlyIn13();
		unsupportedWithoutPreviewError();
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	@Override
	final List internalStructuralPropertiesForType(int apiLevel, boolean previewEnabled) {
		return propertyDescriptors(apiLevel, previewEnabled);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == EXPRESSION_PROPERTY) {
			if (get) {
				return getExpression();
			} else {
				setExpression((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == STATEMENTS_PROPERTY) {
			return statements();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final int getNodeType0() {
		return SWITCH_EXPRESSION;
	}

	@Override
	ASTNode clone0(AST target) {
		SwitchExpression result = new SwitchExpression(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setExpression((Expression) getExpression().clone(target));
		result.statements().addAll(ASTNode.copySubtrees(target, statements()));
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
			acceptChild(visitor, getExpression());
			acceptChildren(visitor, this.statements);
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the expression of this switch statement.
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
	 * Sets the expression of this switch statement.
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
	 * Returns the live ordered list of statements for this switch statement.
	 * Within this list, <code>SwitchCase</code> nodes mark the start of
	 * the switch groups.
	 *
	 * @return the live list of statement nodes
	 *    (element type: {@link Statement})
	 */
	public List statements() {
		return this.statements;
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.expression == null ? 0 : getExpression().treeSize())
			+ this.statements.listSize();
	}
}
