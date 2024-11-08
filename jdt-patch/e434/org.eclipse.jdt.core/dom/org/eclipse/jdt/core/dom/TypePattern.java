/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation and others.
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
 * TypePattern pattern AST node type.
 *
 * <pre>
 * TypePattern:
 *      {@link VariableDeclaration}
 * </pre>
 *
 * @since 3.27
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings("rawtypes")
public class TypePattern extends Pattern {
	/**
	 * The "patternVariable" structural property of this node type (child type: {@link SingleVariableDeclaration}).
	 * @deprecated In the JLS22 API, this property is replaced by {@link VariableDeclaration}.
	 */
	public static final ChildPropertyDescriptor PATTERN_VARIABLE_PROPERTY =
			new ChildPropertyDescriptor(TypePattern.class, "patternVariable", SingleVariableDeclaration.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "patternVariable" structural property of this node type (child type: {@link VariableDeclaration}).
	 * @since 3.39
	 */
	public static final ChildPropertyDescriptor PATTERN_VARIABLE_PROPERTY2 =
			new ChildPropertyDescriptor(TypePattern.class, "patternVariable2", VariableDeclaration.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

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
	 * @since 3.38
	 */
	private static final List PROPERTY_DESCRIPTORS_2_0;

	static {
		List properyList = new ArrayList(3);
		createPropertyList(TypePattern.class, properyList);
		addProperty(PATTERN_VARIABLE_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(properyList);

		properyList = new ArrayList(3);
		createPropertyList(TypePattern.class, properyList);
		addProperty(PATTERN_VARIABLE_PROPERTY2, properyList);
		PROPERTY_DESCRIPTORS_2_0 = reapPropertyList(properyList);
	}

	@Override
	int getNodeType0() {
		return ASTNode.TYPE_PATTERN;
	}

	TypePattern(AST ast) {
		super(ast);
		supportedOnlyIn21();
	}

	/**
	 * Creates a new AST node for a type pattern node owned by the
	 * given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 * @param flag which indicates true for SingleVariableDeclaration | false for VariableDeclarationFragment
	 */
	TypePattern(AST ast, boolean flag) {
		super(ast);
		supportedOnlyIn21();
		if (ast.apiLevel >= AST.JLS22_INTERNAL) {
			if(flag) {
				this.patternVariable = new SingleVariableDeclaration(ast);
			} else {
				this.patternVariable = new VariableDeclarationFragment(ast);
			}
		} else {
			this.patternVariable = new SingleVariableDeclaration(ast);
		}
	}


	/**
	 * The pattern Variable list; <code>empty</code> for none;
	 */
	private volatile VariableDeclaration patternVariable;

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.38
	 */
	public static List propertyDescriptors(int apiLevel) {
		return null;
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
	 * @since 3.38
	 */
	public static List propertyDescriptors(int apiLevel, boolean previewEnabled) {
		if (DOMASTUtil.isPatternSupported(apiLevel, previewEnabled)) {
			if(apiLevel < AST.JLS22_INTERNAL) {
				return PROPERTY_DESCRIPTORS;
			} else {
				return PROPERTY_DESCRIPTORS_2_0;
			}
		}
		return null;
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
		if (property == PATTERN_VARIABLE_PROPERTY) {
			return getPatternVariable();
		} else if (property == PATTERN_VARIABLE_PROPERTY2) {
			return getPatternVariable2();
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/**
	 * Sets the pattern variable.
	 *
	 * @param patternVariable the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used other than JLS19
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @since 3.38
	 */
	public void setPatternVariable(SingleVariableDeclaration patternVariable) {
		supportedOnlyIn20();
		if (patternVariable == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.patternVariable;
		preReplaceChild(oldChild, patternVariable, PATTERN_VARIABLE_PROPERTY);
		this.patternVariable = patternVariable;
		postReplaceChild(oldChild, patternVariable, PATTERN_VARIABLE_PROPERTY);
	}

	/**
	 * Sets the pattern variable.
	 *
	 * @param patternVariable the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 * @exception UnsupportedOperationException if this operation is used other than JLS19
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @since 3.39
	 */
	public void setPatternVariable(VariableDeclaration patternVariable) {
		supportedOnlyIn20();
		if (patternVariable == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.patternVariable;
		preReplaceChild(oldChild, patternVariable, PATTERN_VARIABLE_PROPERTY2);
		this.patternVariable = patternVariable;
		postReplaceChild(oldChild, patternVariable, PATTERN_VARIABLE_PROPERTY2);
	}

	/**
	 * Returns the pattern variable of Types Pattern.
	 *
	 * @return the pattern variable
	 * @exception UnsupportedOperationException if this operation is used other than JLS19
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @since 3.38
	 */
	public SingleVariableDeclaration getPatternVariable() {
		supportedOnlyIn20();
		if (this.patternVariable  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.patternVariable == null) {
					preLazyInit();

					this.patternVariable = postLazyInit(new SingleVariableDeclaration(this.ast),
							PATTERN_VARIABLE_PROPERTY);
				}
			}
		}
		return (SingleVariableDeclaration) this.patternVariable;

	}

	/**
	 * Returns the pattern variable of Types Pattern.
	 *
	 * @return the pattern variable
	 * @exception UnsupportedOperationException if this operation is used other than JLS19
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @since 3.39
	 */
	public VariableDeclaration getPatternVariable2() {
		supportedOnlyIn20();
		if (this.patternVariable  == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.patternVariable == null) {
					preLazyInit();

					this.patternVariable = postLazyInit(new SingleVariableDeclaration(this.ast),
							PATTERN_VARIABLE_PROPERTY2);
				}
			}
		}
		return this.patternVariable;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		TypePattern result = new TypePattern(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setPatternVariable((VariableDeclaration) getPatternVariable().clone(target));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			if (this.ast.apiLevel < AST.JLS22_INTERNAL) {
				acceptChild(visitor, getPatternVariable());
			} else {
				acceptChild(visitor, getPatternVariable2());
			}
		}
		visitor.endVisit(this);

	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}

	@Override
	int treeSize() {
		return
				memSize()
				+ (this.patternVariable == null ? 0 : ((this.ast.apiLevel < AST.JLS22_INTERNAL) ? getPatternVariable().treeSize() : getPatternVariable2().treeSize()));
	}


}
