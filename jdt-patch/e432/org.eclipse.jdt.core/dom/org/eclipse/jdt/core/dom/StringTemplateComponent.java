/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
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
 * StringTemplateComponent AST node type is a combination of a single embedded expression followed by a
 * <code>StringFragment</code>.
 *
 * <pre>
 * StringTemplateComponent:
 *  <b>\{</b> Expression <b>}</b> StringFragment
 * </pre>
 *
 * @since 3.37
 * @noreference This class is not intended to be referenced by clients.
 */
@SuppressWarnings("rawtypes")
public class StringTemplateComponent extends Expression {

	/**
	 * A list of property descriptors (element type: {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	/**
	 * The "expression" structural property of this node type (child type: {@link StringFragment}).
	 */
	public static final ChildPropertyDescriptor EMBEDDED_EXPRESSION_PROPERTY = new ChildPropertyDescriptor(
			StringTemplateComponent.class, "expression", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "fragment" structural property of this node type (child type: {@link StringFragment}).
	 */
	public static final ChildPropertyDescriptor STRING_FRAGMENT_PROPERTY = new ChildPropertyDescriptor(
			StringTemplateComponent.class, "fragment", StringFragment.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(StringTemplateComponent.class, propertyList);
		addProperty(EMBEDDED_EXPRESSION_PROPERTY, propertyList);
		addProperty(STRING_FRAGMENT_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	StringTemplateComponent(AST ast) {
		super(ast);
		supportedOnlyIn21();
	}

	/**
	 * The embedded expression.
	 */
	private volatile Expression expression;

	/**
	 * The string fragment that follows the embedded expression.
	 */
	private volatile StringFragment fragment;

	/**
	 * Returns a list of structural property descriptors for this node type. Clients must not modify the result.
	 *
	 * @param apiLevel
	 *            the API level; one of the <code>AST.JLS*</code> constants
	 *
	 * @return a list of property descriptors (element type: {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return null;
	}

	public static List propertyDescriptors(int apiLevel, boolean previewEnabled) {
		if (DOMASTUtil.isStringTemplateSupported(apiLevel, previewEnabled)) {
			return PROPERTY_DESCRIPTORS;
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
		if (property == STRING_FRAGMENT_PROPERTY) {
			if (get) {
				return getStringFragment();
			} else {
				setStringFragment((StringFragment) child);
				return null;
			}
		}
		if (property == EMBEDDED_EXPRESSION_PROPERTY) {
			if (get) {
				return getEmbeddedExpression();
			} else {
				setEmbeddedExpression((Expression) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public void setEmbeddedExpression(Expression processor) {
		supportedOnlyIn21();
		if (processor == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.expression;
		preReplaceChild(oldChild, processor, EMBEDDED_EXPRESSION_PROPERTY);
		this.expression = processor;
		postReplaceChild(oldChild, processor, EMBEDDED_EXPRESSION_PROPERTY);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public Expression getEmbeddedExpression() {
		supportedOnlyIn21();
		if (this.expression == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.expression == null) {
					preLazyInit();
					this.expression = new SimpleName(this.ast);
					postLazyInit(this.expression, EMBEDDED_EXPRESSION_PROPERTY);
				}
			}
		}
		return this.expression;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public void setStringFragment(StringFragment fragment) {
		supportedOnlyIn21();
		if (fragment == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.fragment;
		preReplaceChild(oldChild, fragment, STRING_FRAGMENT_PROPERTY);
		this.fragment = fragment;
		postReplaceChild(oldChild, fragment, STRING_FRAGMENT_PROPERTY);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public StringFragment getStringFragment() {
		supportedOnlyIn21();
		if (this.fragment == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.fragment == null) {
					preLazyInit();
					this.fragment = new StringFragment(this.ast);
					postLazyInit(this.fragment, STRING_FRAGMENT_PROPERTY);
				}
			}
		}
		return this.fragment;
	}

	@Override
	int getNodeType0() {
		return STRING_TEMPLATE_COMPONENT;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		StringTemplateComponent result = new StringTemplateComponent(target);
		result.setStringFragment((StringFragment) getStringFragment().clone(target));
		result.setEmbeddedExpression((Expression) getEmbeddedExpression().clone(target));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getStringFragment());
			acceptChild(visitor, getEmbeddedExpression());
		}
		visitor.endVisit(this);
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}

	@Override
	int treeSize() {
		return memSize() + (this.fragment == null ? 0 : getStringFragment().treeSize())
				+ (this.expression == null ? 0 : getEmbeddedExpression().treeSize());
	}
}
