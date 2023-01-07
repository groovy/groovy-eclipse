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
 * JavaDocRegion pattern AST node type.
 *
 * <pre>
 * JavaDocRegion:
 *     [ TagElement { <b>,</b> TagElement } ]
 *     [ ASTNode { [TextElement] [JavaDocRegion] } ]
 *     validSnippet
 * </pre>
 *
 * @since 3.30
 */

@SuppressWarnings("rawtypes")
public class JavaDocRegion extends AbstractTagElement{

	JavaDocRegion(AST ast) {
		super(ast);
		unsupportedBelow18();
	}

	/**
	 * The "tagName" structural property of this node type (type: {@link String}).
	 */

	public static final SimplePropertyDescriptor TAG_NAME_PROPERTY =
			internalTagNamePropertyFactory(JavaDocRegion.class);
	/**
	 * The "fragments" structural property of this node type (element type: {@link IDocElement}).
	 * These are the containers which will have texts and other JavaDoc regions
	 */
	public static final ChildListPropertyDescriptor FRAGMENTS_PROPERTY =
			internalFragmentsPropertyFactory(JavaDocRegion.class);

	/**
	 * The "tags" structural property of this node type (child type: {@link TagElement}). (added in JEP 413).
	 * These are the decorators like link, highlight etc
	 */
	public static final ChildListPropertyDescriptor TAGS_PROPERTY  =
			new ChildListPropertyDescriptor(JavaDocRegion.class, "tags", TagElement.class, CYCLE_RISK); //$NON-NLS-1$);


	/**
	 * The "dummy regions" structural property of this node type (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor DUMMY_REGION_PROPERTY  = new SimplePropertyDescriptor(JavaDocRegion.class, "dummyRegion", boolean.class, MANDATORY); //$NON-NLS-1$);

	/**
	 * The "validSnippet" structural property of this node type (added in JEP 413).
	 */
	public static final SimplePropertyDescriptor VALID_SNIPPET_PROPERTY  = new SimplePropertyDescriptor(JavaDocRegion.class, "validSnippet", boolean.class, MANDATORY); //$NON-NLS-1$);

	static final String REGION_ENDED = "Region Ended"; //$NON-NLS-1$
	static final String REGION_TO_BE_ENDED = "Region To Be Ended"; //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(6);
		createPropertyList(JavaDocRegion.class, propertyList);
		addProperty(TAG_NAME_PROPERTY, propertyList);
		addProperty(FRAGMENTS_PROPERTY, propertyList);
		addProperty(TAGS_PROPERTY, propertyList);
		addProperty(DUMMY_REGION_PROPERTY, propertyList);
		addProperty(VALID_SNIPPET_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
	}

	/**
	 * The tags list; <code>empty</code> for none;
	 */
	private ASTNode.NodeList tags = new ASTNode.NodeList(TAGS_PROPERTY);

	/**
	 * The property dummyRegion
	 */
	private boolean dummyRegion = Boolean.TRUE;

	/**
	 * The property validSnippet
	 */
	private boolean validSnippet = Boolean.TRUE;




	@Override
	List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}

	@Override
	final boolean internalGetSetBooleanProperty(SimplePropertyDescriptor property, boolean get, boolean newValue) {
		if (property == DUMMY_REGION_PROPERTY) {
			if (get) {
				return isDummyRegion();
			} else {
				setDummyRegion(newValue);
				return false;
			}
		} else if (property == VALID_SNIPPET_PROPERTY) {
			if (get) {
				return isValidSnippet();
			} else {
				setValidSnippet(newValue);
				return false;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetBooleanProperty(property, get, newValue);
	}

	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == FRAGMENTS_PROPERTY) {
			return fragments();
		} else if (property == TAGS_PROPERTY) {
			return tags();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	int getNodeType0() {
		return JAVADOC_REGION;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@SuppressWarnings("unchecked")
	@Override
	ASTNode clone0(AST target) {
		JavaDocRegion result = new JavaDocRegion(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setTagName(getTagName());
		result.setDummyRegion(isDummyRegion());
		result.setValidSnippet(isValidSnippet());
		result.tags().addAll(
				ASTNode.copySubtrees(target, tags()));
		result.fragments().addAll(
				ASTNode.copySubtrees(target, fragments()));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);

	}

	@Override
	int memSize() {
		return super.memSize() + 3*4 ;
	}

	@Override
	int treeSize() {
		return memSize() ;
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
	 * Returns the list of tag elements in this region, or
	 * <code>empty</code> if there is none.
	 *
	 *  @return the list of tag element nodes
	 *    (element type: {@link TagElement})
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public List tags() {
		unsupportedBelow18();
		return this.tags;
	}

	/**
	 * Returns <code>true</code> is region is dummy else <code>false</code>.
	 * @return the dummyRegion
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public boolean isDummyRegion() {
		unsupportedBelow18();
		return this.dummyRegion;
	}

	/**
	 * Sets the value of dummyRegion property.
	 * @param dummyRegion
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setDummyRegion(boolean dummyRegion) {
		unsupportedBelow18();
		preValueChange(DUMMY_REGION_PROPERTY);
		this.dummyRegion = dummyRegion;
		postValueChange(DUMMY_REGION_PROPERTY);
	}

	/**
	 * Returns <code>true</code> if region has valid snippet else <code>false</code>.
	 * @return the validSnippet
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public boolean isValidSnippet() {
		unsupportedBelow18();
		return this.validSnippet;
	}

	/**
	 * Sets the value of validSnippet property.
	 * @param validSnippet
	 * @exception UnsupportedOperationException if this operation is used below JLS18
	 */
	public void setValidSnippet(boolean validSnippet) {
		unsupportedBelow18();
		preValueChange(VALID_SNIPPET_PROPERTY);
		this.validSnippet = validSnippet;
		postValueChange(VALID_SNIPPET_PROPERTY);
	}

	@Override
	ChildListPropertyDescriptor internalFragmentsPropertyFactory() {
		return FRAGMENTS_PROPERTY;
	}

	@Override
	SimplePropertyDescriptor internalTagNamePropertyFactory() {
		return TAG_NAME_PROPERTY;
	}

}
