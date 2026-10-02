/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
 * TagProperty pattern AST node type.
 *
 * <pre>
 * TagProperty:
 *      Name
 *      String Value
 *      Node Value
 * </pre>
 *
 * @since 3.30
 */

@SuppressWarnings("rawtypes")
public class TagProperty extends ASTNode implements IDocElement{

	TagProperty(AST ast) {
		super(ast);
		unsupportedBelow18();
	}

	/**
	 * The "name" structural property of this node type (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor NAME_PROPERTY  = new SimplePropertyDescriptor(TagProperty.class, "name", String.class, MANDATORY); //$NON-NLS-1$);

	/**
	 * The "string_value" structural property of this node type . (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor STRING_VALUE_PROPERTY  =
			new SimplePropertyDescriptor(TagProperty.class, "string_value", String.class, MANDATORY); //$NON-NLS-1$);

	/**
	 * The "node_value" structural property of this node type . (added in JEP 413).
	 */
	public static final ChildPropertyDescriptor NODE_VALUE_PROPERTY  =
			new ChildPropertyDescriptor(TagProperty.class, "node_value", ASTNode.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$);

	public static final String TAG_PROPERTY_SNIPPET_IS_VALID = "IsSnippetValid"; //$NON-NLS-1$

	public static final String TAG_PROPERTY_SNIPPET_ERROR = "SnippetError"; //$NON-NLS-1$

	/**
	 * @since 3.30
	 */
	public static final String TAG_PROPERTY_SNIPPET_ID =  "SnippetID"; //$NON-NLS-1$

	/**
	 * @since 3.30
	 */
	public static final String TAG_PROPERTY_SNIPPET_INLINE_TAG_COUNT = "SnippetInlineTagCount"; //$NON-NLS-1$

	/**
	 * @since 3.30
	 */
	public static final String TAG_PROPERTY_SNIPPET_REGION_TEXT = "SnippetRegionTextElement"; //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;



	static {
		List propertyList = new ArrayList(4);
		createPropertyList(TagProperty.class, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		addProperty(STRING_VALUE_PROPERTY, propertyList);
		addProperty(NODE_VALUE_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * The property name
	 */
	private String name = null;

	/**
	 * The property string value
	 */
	private String string_value = null;

	/**
	 * The property node value
	 */
	private ASTNode node_value = null;




	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}


	@Override
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object newValue) {
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((String)newValue);
				return null;
			}
		} else if (property == STRING_VALUE_PROPERTY) {
			if (get) {
				return getStringValue();
			} else {
				setStringValue((String)newValue);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, newValue);
	}

	@Override
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == NODE_VALUE_PROPERTY) {
			if (get) {
				return getNodeValue();
			} else {
				setNodeValue(child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	@Override
	int getNodeType0() {
		return TAG_PROPERTY;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		TagProperty result = new TagProperty(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setName(getName());
		result.setStringValue(getStringValue());
		result.setNodeValue(ASTNode.copySubtree(target, getNodeValue()));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);

	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4 + stringSize(this.name) + stringSize(this.string_value);
	}

	@Override
	int treeSize() {
		return memSize();
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
		if (DOMASTUtil.isJavaDocCodeSnippetSupported(apiLevel)) {
			return PROPERTY_DESCRIPTORS;
		}
		return null;
	}

	/**
	 * Returns the name of this tag property.
	 * @return the name
	 * @exception UnsupportedOperationException if this operation is used below than JLS18
	 */
	public String getName() {
		unsupportedBelow18();
		return this.name;
	}

	/**
	 * Returns the string value of this tag property.
	 * @return the string_value
	 * @exception UnsupportedOperationException if this operation is used below than JLS18
	 */
	public String getStringValue() {
		unsupportedBelow18();
		return this.string_value;
	}

	/**
	 * Returns the node value of this tag property.
	 * @return the node_value
	 * @exception UnsupportedOperationException if this operation is used below than JLS18
	 */
	public ASTNode getNodeValue() {
		unsupportedBelow18();
		return this.node_value;
	}

	/**
	 * Sets the name of this tag property.
	 *
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setName(String name) {
		unsupportedBelow18();
		preValueChange(NAME_PROPERTY);
		this.name = name;
		postValueChange(NAME_PROPERTY);
	}

	/**
	 * Sets the string value of this tag property.
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setStringValue(String value) {
		unsupportedBelow18();
		preValueChange(STRING_VALUE_PROPERTY);
		this.string_value = value;
		postValueChange(STRING_VALUE_PROPERTY);
	}

	/**
	 * Sets the node value of this tag property.
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setNodeValue(ASTNode value) {
		unsupportedBelow18();
		ASTNode oldChild = this.node_value;
		preReplaceChild(oldChild, value, NODE_VALUE_PROPERTY);
		this.node_value = value;
		postReplaceChild(oldChild, value, NODE_VALUE_PROPERTY);
	}

}
