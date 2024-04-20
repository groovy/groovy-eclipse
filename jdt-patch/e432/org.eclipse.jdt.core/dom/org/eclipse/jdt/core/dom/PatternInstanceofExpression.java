/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
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
 * PatternInstanceof expression AST node type.
 * <pre>
 * PatternInstanceofExpression:
 *    Expression <b>instanceof</b> Variable
 * PatternInstanceofExpression:
 *    Expression <b>instanceof</b> Pattern
 * </pre>
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class PatternInstanceofExpression extends Expression {

	/**
	 * The "leftOperand" structural property of this node type (child type: {@link Expression}).
	 */
	public static final ChildPropertyDescriptor LEFT_OPERAND_PROPERTY =
		new ChildPropertyDescriptor(PatternInstanceofExpression.class, "leftOperand", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "rightOperand" structural property of this node type (child type: {@link SingleVariableDeclaration}).
	 * @deprecated use pattern property instead from Java 20 onwards
	 */
	public static final ChildPropertyDescriptor RIGHT_OPERAND_PROPERTY =
		new ChildPropertyDescriptor(PatternInstanceofExpression.class, "rightOperand", SingleVariableDeclaration.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$
	/**
	 * The "pattern" structural property of this node type (child type: {@link Pattern}).
	 * @since 3.34
	 */
	public static final ChildPropertyDescriptor PATTERN_PROPERTY =
		new ChildPropertyDescriptor(PatternInstanceofExpression.class, "pattern", Pattern.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @deprecated use PROPERTY_DESCRIPTORS_20 from Java 20 onwards
	 */
	private static final List PROPERTY_DESCRIPTORS_16;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 *
	 * @since 3.33
	 */
	private static final List PROPERTY_DESCRIPTORS_20;

	static {
		List properyList = new ArrayList(3);
		createPropertyList(PatternInstanceofExpression.class, properyList);
		addProperty(LEFT_OPERAND_PROPERTY, properyList);
		addProperty(RIGHT_OPERAND_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_16 = reapPropertyList(properyList);

		properyList = new ArrayList(3);
		createPropertyList(PatternInstanceofExpression.class, properyList);
		addProperty(LEFT_OPERAND_PROPERTY, properyList);
		addProperty(PATTERN_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS_20 = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants

	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.27
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS_16;
	}
	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @param isPreview If Java language preview features are enabled or not
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.34
	 */
	public static List propertyDescriptors(int apiLevel, boolean isPreview) {
		if (apiLevel >= AST.JLS21_INTERNAL) {
			return PROPERTY_DESCRIPTORS_20;
		}
		return PROPERTY_DESCRIPTORS_16;
	}

	/**
	 * The left operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private volatile Expression leftOperand;

	/**
	 * The right operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple variable decalaration.
	 */
	private volatile SingleVariableDeclaration rightOperand;
	/**
	 * The right operand - a pattern, which could either be a TypePattern or
	 * a RecordPattern.
	 */
	private volatile Pattern pattern;


	/**
	 * Creates a new AST node for an instanceof expression owned by the given
	 * AST. By default, the node has unspecified (but legal) operator,
	 * left and right operands.
	 *
	 * @param ast the AST that is to own this node
	 */
	PatternInstanceofExpression(AST ast) {
		super(ast);
		unsupportedBelow16();
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel, boolean isPreview) {
		return propertyDescriptors(apiLevel, isPreview);
	}

	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
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
				setRightOperand((SingleVariableDeclaration) child);
				return null;
			}
		}
		if (property == PATTERN_PROPERTY) {
			if (get) {
				return getPattern();
			} else {
				setPattern((Pattern) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final int getNodeType0() {
		return PATTERN_INSTANCEOF_EXPRESSION;
	}

	@Override
	ASTNode clone0(AST target) {
		PatternInstanceofExpression result = new PatternInstanceofExpression(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setLeftOperand((Expression) getLeftOperand().clone(target));
		if (this.ast.apiLevel < AST.JLS20_INTERNAL) {
			result.setRightOperand((SingleVariableDeclaration) getRightOperand().clone(target));
		} else {
			result.setPattern((Pattern) getPattern().clone(target));
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
			if (this.ast.apiLevel >= AST.JLS21_INTERNAL) {
				acceptChild(visitor, getPattern());
			} else {
				acceptChild(visitor, getRightOperand());
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the left operand of this Patterninstanceof expression.
	 *
	 * @return the left operand node
	 * @since 3.27
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
	 * @since 3.27
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
	 * @since 3.27
	 * @deprecated
	 */
	public SingleVariableDeclaration getRightOperand() {
		if (this.rightOperand  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.rightOperand == null) {
					preLazyInit();
					this.rightOperand= new SingleVariableDeclaration(this.ast);
					postLazyInit(this.rightOperand, RIGHT_OPERAND_PROPERTY);
				}
			}
		}
		return this.rightOperand;
	}
	/**
	 * Returns the pattern of this instanceof expression.
	 *
	 * @return the pattern
	 * @since 3.34
	 */
	public Pattern getPattern() {
		supportedOnlyIn21();
		if (this.pattern  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.pattern == null) {
					preLazyInit();
					this.pattern = new TypePattern(this.ast);
					postLazyInit(this.pattern, PATTERN_PROPERTY);
				}
			}
		}
		return this.pattern;
	}
	/**
	 * Sets the right operand of this instanceof expression.
	 *
	 * @param referenceDeclaration the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 * @since 3.27
	 * @deprecated
	 */
	public void setRightOperand(SingleVariableDeclaration referenceDeclaration) {
		if (referenceDeclaration == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.rightOperand;
		preReplaceChild(oldChild, referenceDeclaration, RIGHT_OPERAND_PROPERTY);
		this.rightOperand = referenceDeclaration;
		postReplaceChild(oldChild, referenceDeclaration, RIGHT_OPERAND_PROPERTY);
	}
	/**
	 * Sets the right operand of this instanceof expression.
	 *
	 * @param pattern either the type or record pattern
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 * @since 3.34
	 */
	public void setPattern(Pattern pattern) {
		supportedOnlyIn20();
		if (pattern == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.pattern;
		preReplaceChild(oldChild, pattern, PATTERN_PROPERTY);
		this.pattern = pattern;
		postReplaceChild(oldChild, pattern, PATTERN_PROPERTY);
	}

	@Override
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 2 * 4;
	}

	@Override
	int treeSize() {
		return
			memSize()
			+ (this.leftOperand == null ? 0 : getLeftOperand().treeSize())
			+ ((this.ast.apiLevel < AST.JLS20_INTERNAL) ?
					(this.rightOperand == null ? 0 : getRightOperand().treeSize()) :
						(this.pattern == null ? 0 : getPattern().treeSize()));
	}
}
