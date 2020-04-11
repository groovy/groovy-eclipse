/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import org.eclipse.jdt.internal.core.dom.util.DOMASTUtil;

/**
 * Instanceof expression AST node type.
 * <pre>
 * InstanceofExpression:
 *    Expression <b>instanceof</b> Type
 * </pre>
 *
 * @since 2.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class InstanceofExpression extends Expression {

	/**
	 * The "leftOperand" structural property of this node type (child type: {@link Expression}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor LEFT_OPERAND_PROPERTY =
		new ChildPropertyDescriptor(InstanceofExpression.class, "leftOperand", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "rightOperand" structural property of this node type (child type: {@link Type}).
	 * @since 3.0
	 */
	public static final ChildPropertyDescriptor RIGHT_OPERAND_PROPERTY =
		new ChildPropertyDescriptor(InstanceofExpression.class, "rightOperand", Type.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "patternVariable" structural property of this node type (child type: {@link SingleVariableDeclaration}) (added in JLS14 API).
	 * @noreference This property is not intended to be referenced by clients as it is a part of Java preview feature.
	 * @since 3.22
	 */
	public static final ChildPropertyDescriptor PATTERN_VARIABLE_PROPERTY =
		new ChildPropertyDescriptor(InstanceofExpression.class, "patternVariable", SingleVariableDeclaration.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
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
	 */
	private static final List PROPERTY_DESCRIPTORS_14;

	static {
		List properyList = new ArrayList(3);
		createPropertyList(InstanceofExpression.class, properyList);
		addProperty(LEFT_OPERAND_PROPERTY, properyList);
		addProperty(RIGHT_OPERAND_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(properyList);
		properyList = new ArrayList(4);
		createPropertyList(InstanceofExpression.class, properyList);
		addProperty(LEFT_OPERAND_PROPERTY, properyList);
		addProperty(RIGHT_OPERAND_PROPERTY, properyList);
		addProperty(PATTERN_VARIABLE_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_14 = reapPropertyList(properyList);
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
		return propertyDescriptors(apiLevel, false);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @param previewEnabled the previewEnabled flag
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.22
	 */
	public static List propertyDescriptors(int apiLevel, boolean previewEnabled) {
		if (DOMASTUtil.isInstanceofExpressionPatternSupported(apiLevel, previewEnabled)) {
			return PROPERTY_DESCRIPTORS_14;
		}
		return PROPERTY_DESCRIPTORS;
	}
	/**
	 * The left operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression leftOperand = null;

	/**
	 * The right operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple type.
	 */
	private Type rightOperand = null;

	/**
	 * The patternVariable declaration.
	 */
	private SingleVariableDeclaration patternVariable = null;

	/**
	 * Creates a new AST node for an instanceof expression owned by the given
	 * AST. By default, the node has unspecified (but legal) operator,
	 * left and right operands.
	 *
	 * @param ast the AST that is to own this node
	 */
	InstanceofExpression(AST ast) {
		super(ast);
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
		if (property == LEFT_OPERAND_PROPERTY) {
			if (get) {
				return getLeftOperand();
			} else {
				setLeftOperand((Expression) child);
				return null;
			}
		}
		if (property == RIGHT_OPERAND_PROPERTY) {
			if (get) {
				return getRightOperand();
			} else {
				setRightOperand((Type) child);
				return null;
			}
		}
		if (property == PATTERN_VARIABLE_PROPERTY) {
			if (get) {
				return getPatternVariable();
			} else {
				setPatternVariable((SingleVariableDeclaration) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final int getNodeType0() {
		return INSTANCEOF_EXPRESSION;
	}

	@Override
	ASTNode clone0(AST target) {
		InstanceofExpression result = new InstanceofExpression(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setLeftOperand((Expression) getLeftOperand().clone(target));
		result.setRightOperand((Type) getRightOperand().clone(target));
		if (DOMASTUtil.isInstanceofExpressionPatternSupported(target)) {
			result.setPatternVariable((SingleVariableDeclaration) getPatternVariable().clone(target));
		}
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
			acceptChild(visitor, getLeftOperand());
			acceptChild(visitor, getRightOperand());
			if (DOMASTUtil.isInstanceofExpressionPatternSupported(this.ast)) {
				acceptChild(visitor, getPatternVariable());
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the left operand of this instanceof expression.
	 *
	 * @return the left operand node
	 */
	public Expression getLeftOperand() {
		if (this.leftOperand  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.leftOperand == null) {
					preLazyInit();
					this.leftOperand= new SimpleName(this.ast);
					postLazyInit(this.leftOperand, LEFT_OPERAND_PROPERTY);
				}
			}
		}
		return this.leftOperand;
	}

	/**
	 * Sets the left operand of this instanceof expression.
	 *
	 * @param expression the left operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setLeftOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.leftOperand;
		preReplaceChild(oldChild, expression, LEFT_OPERAND_PROPERTY);
		this.leftOperand = expression;
		postReplaceChild(oldChild, expression, LEFT_OPERAND_PROPERTY);
	}

	/**
	 * Returns the right operand of this instanceof expression.
	 *
	 * @return the right operand node
	 */
	public Type getRightOperand() {
		if (this.rightOperand  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.rightOperand == null) {
					preLazyInit();
					this.rightOperand= new SimpleType(this.ast);
					postLazyInit(this.rightOperand, RIGHT_OPERAND_PROPERTY);
				}
			}
		}
		return this.rightOperand;
	}

	/**
	 * Sets the right operand of this instanceof expression.
	 *
	 * @param referenceType the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public void setRightOperand(Type referenceType) {
		if (referenceType == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.rightOperand;
		preReplaceChild(oldChild, referenceType, RIGHT_OPERAND_PROPERTY);
		this.rightOperand = referenceType;
		postReplaceChild(oldChild, referenceType, RIGHT_OPERAND_PROPERTY);
	}

	/**
	 * Returns the patternVariable of this instanceof expression.
	 *
	 * @return the patternVariable node
	 * @exception UnsupportedOperationException if this operation is used other than JLS14
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 * @nooverride This method is not intended to be re-implemented or extended by clients as it is a part of Java preview feature.
	 */
	public SingleVariableDeclaration getPatternVariable() {
		supportedOnlyIn14();
		unsupportedWithoutPreviewError();
		return this.patternVariable;
	}

	/**
	 * Sets the patternVariable of this instanceof expression.
	 *
	 * @param referencePatternVariable the right operand node
	 * @exception IllegalArgumentException if:
	 * @exception UnsupportedOperationException if this operation is used other than JLS14
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 * @nooverride This method is not intended to be re-implemented or extended by clients as it is a part of Java preview feature.
	 */
	public void setPatternVariable(SingleVariableDeclaration referencePatternVariable) {
		supportedOnlyIn14();
		unsupportedWithoutPreviewError();
		if (referencePatternVariable == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.patternVariable;
		preReplaceChild(oldChild, referencePatternVariable, PATTERN_VARIABLE_PROPERTY);
		this.patternVariable = referencePatternVariable;
		postReplaceChild(oldChild, referencePatternVariable, PATTERN_VARIABLE_PROPERTY);
	}
	@Override
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 3 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.leftOperand == null ? 0 : getLeftOperand().treeSize())
			+ (this.rightOperand == null ? 0 : getRightOperand().treeSize())
			+ (this.patternVariable == null ? 0 : getPatternVariable().treeSize());
	}
}
