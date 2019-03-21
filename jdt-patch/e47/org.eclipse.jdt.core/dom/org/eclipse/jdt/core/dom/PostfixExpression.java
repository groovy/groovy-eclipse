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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Postfix expression AST node type.
 *
 * <pre>
 * PostfixExpression:
 *    Expression PostfixOperator
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PostfixExpression extends Expression {

	/**
 	 * Postfix operators (typesafe enumeration).
	 * <pre>
	 * PostfixOperator:
	 *    <b><code>++</code></b>  <code>INCREMENT</code>
	 *    <b><code>--</code></b>  <code>DECREMENT</code>
	 * </pre>
	 */
	public static class Operator {

		/**
		 * The token for the operator.
		 */
		private String token;

		/**
		 * Creates a new postfix operator with the given token.
		 * <p>
		 * Note: this constructor is private. The only instances
		 * ever created are the ones for the standard operators.
		 * </p>
		 *
		 * @param token the character sequence for the operator
		 */
		private Operator(String token) {
			this.token = token;
		}

		/**
		 * Returns the character sequence for the operator.
		 *
		 * @return the character sequence for the operator
		 */
		public String toString() {
			return this.token;
		}

		/** Postfix increment "++" operator. */
		public static final Operator INCREMENT = new Operator("++");//$NON-NLS-1$
		/** Postfix decrement "--" operator. */
		public static final Operator DECREMENT = new Operator("--");//$NON-NLS-1$

		/**
		 * Map from token to operator (key type: <code>String</code>;
		 * value type: <code>Operator</code>).
		 */
		private static final Map CODES;
		static {
			CODES = new HashMap(20);
			Operator[] ops = {
					INCREMENT,
					DECREMENT,
				};
			for (int i = 0; i < ops.length; i++) {
				CODES.put(ops[i].toString(), ops[i]);
			}
		}

		/**
		 * Returns the postfix operator corresponding to the given string,
		 * or <code>null</code> if none.
		 * <p>
		 * <code>toOperator</code> is the converse of <code>toString</code>:
		 * that is, <code>Operator.toOperator(op.toString()) == op</code> for
		 * all operators <code>op</code>.
		 * </p>
		 *
		 * @param token the character sequence for the operator
		 * @return the postfix operator, or <code>null</code> if none
		 */
		public static Operator toOperator(String token) {
			return (Operator) CODES.get(token);
		}
	}

	/**
	 * The "operator" structural property of this node type (type: {@link PostfixExpression.Operator}).
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor OPERATOR_PROPERTY =
		new SimplePropertyDescriptor(PostfixExpression.class, "operator", PostfixExpression.Operator.class, MANDATORY); //$NON-NLS-1$

	/**
	 * The "operand" structural property of this node type (child type: {@link Expression}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor OPERAND_PROPERTY =
		new ChildPropertyDescriptor(PostfixExpression.class, "operand", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(PostfixExpression.class, propertyList);
		addProperty(OPERAND_PROPERTY, propertyList);
		addProperty(OPERATOR_PROPERTY, propertyList);
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
	 * The operator; defaults to an unspecified postfix operator.
	 */
	private PostfixExpression.Operator operator =
		PostfixExpression.Operator.INCREMENT;

	/**
	 * The operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression operand = null;

	/**
	 * Creates a new AST node for an postfix expression owned by the given
	 * AST. By default, the node has unspecified (but legal) operator and
	 * operand.
	 *
	 * @param ast the AST that is to own this node
	 */
	PostfixExpression(AST ast) {
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
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == OPERATOR_PROPERTY) {
			if (get) {
				return getOperator();
			} else {
				setOperator((Operator) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == OPERAND_PROPERTY) {
			if (get) {
				return getOperand();
			} else {
				setOperand((Expression) child);
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
		return POSTFIX_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		PostfixExpression result = new PostfixExpression(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setOperator(getOperator());
		result.setOperand((Expression) getOperand().clone(target));
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
			acceptChild(visitor, getOperand());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the operator of this postfix expression.
	 *
	 * @return the operator
	 */
	public PostfixExpression.Operator getOperator() {
		return this.operator;
	}

	/**
	 * Sets the operator of this postfix expression.
	 *
	 * @param operator the operator
	 * @exception IllegalArgumentException if the argument is incorrect
	 */
	public void setOperator(PostfixExpression.Operator operator) {
		if (operator == null) {
			throw new IllegalArgumentException();
		}
		preValueChange(OPERATOR_PROPERTY);
		this.operator = operator;
		postValueChange(OPERATOR_PROPERTY);
	}

	/**
	 * Returns the operand of this postfix expression.
	 *
	 * @return the operand expression node
	 */
	public Expression getOperand() {
		if (this.operand  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.operand == null) {
					preLazyInit();
					this.operand= new SimpleName(this.ast);
					postLazyInit(this.operand, OPERAND_PROPERTY);
				}
			}
		}
		return this.operand;
	}

	/**
	 * Sets the operand of this postfix expression.
	 *
	 * @param expression the operand expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.operand;
		preReplaceChild(oldChild, expression, OPERAND_PROPERTY);
		this.operand = expression;
		postReplaceChild(oldChild, expression, OPERAND_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 2 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.operand == null ? 0 : getOperand().treeSize());
	}
}
