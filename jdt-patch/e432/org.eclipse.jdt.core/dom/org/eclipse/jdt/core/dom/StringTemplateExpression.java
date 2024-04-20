/*******************************************************************************
 * Copyright (c) 2023, 2024 IBM Corporation and others.
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
 * StringTemplateExpression AST node type.
 *
 * <pre>
 * StringTemplateExpression:
 *    Expression <b>.</b> <b>&quot;</b> StringFragment [StringTemplateComponent {StringTemplateComponent}] <b>&quot;</b>
 * </pre>
 *
 * For a multi-line text block, corresponding delimiters are applicable instead of single quotes.
 *
 * @since 3.37
 * @noreference This class is not intended to be referenced by clients.
 */
@SuppressWarnings("rawtypes")
public class StringTemplateExpression extends Expression {

	/**
	 * A list of property descriptors (element type: {@link StructuralPropertyDescriptor}), or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	/**
	 * The "processor" structural property of this node type (child type: {@link Expression}).
	 *
	 * @since 3.37
	 */
	public static final ChildPropertyDescriptor TEMPLATE_PROCESSOR = new ChildPropertyDescriptor(
			StringTemplateExpression.class, "processor", Expression.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "firstFragment" structural property of this node type (child type: {@link StringFragment}).
	 */
	public static final ChildPropertyDescriptor FIRST_STRING_FRAGMENT = new ChildPropertyDescriptor(
			StringTemplateExpression.class, "firstFragment", StringFragment.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "components" structural property of this node type (child type: {@link StringTemplateComponent}).
	 */
	public static final ChildListPropertyDescriptor STRING_TEMPLATE_COMPONENTS = new ChildListPropertyDescriptor(
			StringTemplateExpression.class, "components", StringTemplateComponent.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "multiline" structural property of this node type (type: {@link Boolean}).
	 */
	public static final SimplePropertyDescriptor MULTI_LINE = new SimplePropertyDescriptor(
			StringTemplateExpression.class, "multiline", boolean.class, MANDATORY); //$NON-NLS-1$

	static {
		List propertyList = new ArrayList(2);
		createPropertyList(StringTemplateExpression.class, propertyList);
		addProperty(TEMPLATE_PROCESSOR, propertyList);
		addProperty(FIRST_STRING_FRAGMENT, propertyList);
		addProperty(MULTI_LINE, propertyList);
		addProperty(STRING_TEMPLATE_COMPONENTS, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	private volatile Expression processor;

	private volatile StringFragment firstFragment;

	/**
	 * <code>true</code> if this is part of a text block template, <code>false</code> otherwise. Defaults to false.
	 */
	private boolean isMultiline = false;

	/**
	 */
	private final ASTNode.NodeList components = new ASTNode.NodeList(STRING_TEMPLATE_COMPONENTS);

	StringTemplateExpression(AST ast) {
		super(ast);
		supportedOnlyIn22();
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public void setProcessor(Expression processor) {
		supportedOnlyIn22();
		if (processor == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.processor;
		preReplaceChild(oldChild, processor, TEMPLATE_PROCESSOR);
		this.processor = processor;
		postReplaceChild(oldChild, processor, TEMPLATE_PROCESSOR);
	}

	/**
	 * Returns the String template processor of this string template expression.
	 *
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public Expression getProcessor() {
		supportedOnlyIn22();
		if (this.processor == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.processor == null) {
					preLazyInit();
					this.processor = new SimpleName(this.ast);
					postLazyInit(this.processor, TEMPLATE_PROCESSOR);
				}
			}
		}
		return this.processor;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public void setFirstFragment(StringFragment firstFragment) {
		supportedOnlyIn22();
		if (firstFragment == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.firstFragment;
		preReplaceChild(oldChild, firstFragment, FIRST_STRING_FRAGMENT);
		this.firstFragment = firstFragment;
		postReplaceChild(oldChild, firstFragment, FIRST_STRING_FRAGMENT);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	public StringFragment getFirstFragment() {
		supportedOnlyIn22();
		if (this.firstFragment == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.firstFragment == null) {
					preLazyInit();
					this.firstFragment = new StringFragment(this.ast);
					postLazyInit(this.firstFragment, FIRST_STRING_FRAGMENT);
				}
			}
		}
		return this.firstFragment;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	@SuppressWarnings("unchecked")
	public List<StringTemplateComponent> components() {
		supportedOnlyIn22();
		return this.components;
	}

	/**
	 * Returns a list of structural property descriptors for this node type. Clients must not modify the result.
	 *
	 * @param apiLevel
	 *            the API level; one of the <code>AST.JLS*</code> constants
	 * @return a list of property descriptors (element type: {@link StructuralPropertyDescriptor})
	 * @since 3.24
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
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean value) {
		if (property == MULTI_LINE) {
			if (get) {
				return isMultiline();
			} else {
				setIsMultiline(value);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, value);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == TEMPLATE_PROCESSOR) {
			if (get) {
				return getProcessor();
			} else {
				setProcessor((Expression) child);
				return null;
			}
		}
		if (property == FIRST_STRING_FRAGMENT) {
			if (get) {
				return getFirstFragment();
			} else {
				setFirstFragment((StringFragment) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == STRING_TEMPLATE_COMPONENTS) {
			return components();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	public boolean isMultiline() {
		return this.isMultiline;
	}

	public void setIsMultiline(boolean isMultitine) {
		preValueChange(MULTI_LINE);
		this.isMultiline = isMultitine;
		postValueChange(MULTI_LINE);
	}

	@Override
	int getNodeType0() {
		return STRING_TEMPLATE_EXPRESSION;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		StringTemplateExpression result = new StringTemplateExpression(target);
		result.setProcessor((Expression) getProcessor().clone(target));
		result.setFirstFragment((StringFragment) getFirstFragment().clone(target));
		result.components().addAll(ASTNode.copySubtrees(target, components()));
		result.setSourceRange(getStartPosition(), getLength());
		result.setIsMultiline(isMultiline());
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getProcessor());
			acceptChild(visitor, getFirstFragment());
			acceptChildren(visitor, this.components);
		}
		visitor.endVisit(this);
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 4 * 4;
	}

	@Override
	int treeSize() {
		return memSize() + (this.processor == null ? 0 : getProcessor().treeSize())
				+ (this.firstFragment == null ? 0 : getFirstFragment().treeSize()) + this.components.listSize();
	}
}
