/*******************************************************************************
 * Copyright (c) 2004, 2022 IBM Corporation and others.
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
 * AST node for a tag within a doc comment.
 * Tag elements nested within another tag element are called
 * inline doc tags.
 * <pre>
 * TagElement:
 *     [ <b>@</b> Identifier ] { DocElement }
 *     {tagProperty = tagValue}
 * DocElement:
 *     TextElement
 *     Name
 *     MethodRef
 *     MemberRef
 *     <b>{</b> TagElement <b>}</b>
 * </pre>
 *
 * @see Javadoc
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class TagElement extends AbstractTagElement {


	/**
	 * The "tagName" structural property of this node type (type: {@link String}).
	 */

	public static final SimplePropertyDescriptor TAG_NAME_PROPERTY =
			internalTagNamePropertyFactory(TagElement.class);
	/**
	 * The "fragments" structural property of this node type (element type: {@link IDocElement}).
	 */
	public static final ChildListPropertyDescriptor FRAGMENTS_PROPERTY =
			internalFragmentsPropertyFactory(TagElement.class);

	/**
	 * The "properties" structural property of this node type (element type: {@link TagProperty}).
	 * @since 3.30
	 */
	public static final ChildListPropertyDescriptor TAG_PROPERTIES_PROPERTY =
		new ChildListPropertyDescriptor(TagElement.class, "tagProperties", TagProperty.class, CYCLE_RISK); //$NON-NLS-1$



	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS;

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.30
	 */
	private static final List PROPERTY_DESCRIPTORS_18;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(TagElement.class, propertyList);
		addProperty(TAG_NAME_PROPERTY, propertyList);
		addProperty(FRAGMENTS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);

		propertyList = new ArrayList(4);
		createPropertyList(TagElement.class, propertyList);
		addProperty(TAG_NAME_PROPERTY, propertyList);
		addProperty(FRAGMENTS_PROPERTY, propertyList);
		addProperty(TAG_PROPERTIES_PROPERTY, propertyList);

		PROPERTY_DESCRIPTORS_18 = reapPropertyList(propertyList);
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
		if (DOMASTUtil.isJavaDocCodeSnippetSupported(apiLevel)) {
			return PROPERTY_DESCRIPTORS_18;
		}
		return PROPERTY_DESCRIPTORS;
	}
	@Override
	final ChildListPropertyDescriptor internalFragmentsPropertyFactory() {
		return FRAGMENTS_PROPERTY;
	}
	@Override
	final SimplePropertyDescriptor internalTagNamePropertyFactory() {
		return TAG_NAME_PROPERTY;
	}

	/**
	 * Standard doc tag name (value {@value}).
	 */
	public static final String TAG_AUTHOR = "@author"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * <p>
	 * Note that this tag first appeared in J2SE 5.
	 * </p>
	 * @since 3.30
	 */
	public static final String TAG_CODE = "@code"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_DEPRECATED = "@deprecated"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 */
	public static final String TAG_DOCROOT = "@docRoot"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_EXCEPTION = "@exception"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_HIDDEN = "@hidden"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_INDEX = "@index"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_INHERITDOC = "@inheritDoc"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_LINK = "@link"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_LINKPLAIN = "@linkplain"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * <p>
	 * Note that this tag first appeared in J2SE 5.
	 * </p>
	 * @since 3.30
	 */
	public static final String TAG_LITERAL = "@literal"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_PARAM = "@param"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_PROVIDES = "@provides"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_RETURN = "@return"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_SEE = "@see"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_SERIAL = "@serial"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_SERIALDATA= "@serialData"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_SERIALFIELD= "@serialField"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 */
	public static final String TAG_SINCE = "@since"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_SUMMARY = "@summary"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_THROWS = "@throws"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_USES = "@uses"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_VALUE= "@value"; //$NON-NLS-1$

	/**
	 * Standard doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_VERSION = "@version"; //$NON-NLS-1$

	/**
	 * Javadoc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_API_NOTE = "@apiNote"; //$NON-NLS-1$

	/**
	 * Javadoc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_IMPL_SPEC = "@implSpec"; //$NON-NLS-1$

	/**
	 * Javadoc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_IMPL_NOTE = "@implNote"; //$NON-NLS-1$

	/**
	 * Standard inline doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_SNIPPET = "@snippet"; //$NON-NLS-1$

	/**
	 * Standard snippet doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_HIGHLIGHT = "@highlight"; //$NON-NLS-1$

	/**
	 * Standard snippet doc tag name (value {@value}).
	 * @since 3.30
	 */
	public static final String TAG_REPLACE = "@replace"; //$NON-NLS-1$

	/**
	 * The list of doc elements (element type: {@link TagProperty}).
	 * Defaults to an empty list.
	 */
	private final ASTNode.NodeList tagProperties =
		new ASTNode.NodeList(TAG_PROPERTIES_PROPERTY);

	/**
	 * Creates a new AST node for a tag element owned by the given AST.
	 * The new node has no name and an empty list of fragments.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	TagElement(AST ast) {
		super(ast);
	}

	@Override
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}


	@Override
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == FRAGMENTS_PROPERTY) {
			return fragments();
		} else if (property == TAG_PROPERTIES_PROPERTY) {
			return tagProperties();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	@Override
	final int getNodeType0() {
		return TAG_ELEMENT;
	}

	@Override
	ASTNode clone0(AST target) {
		TagElement result = new TagElement(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setTagName(getTagName());
		result.fragments().addAll(ASTNode.copySubtrees(target, fragments()));
		if (DOMASTUtil.isJavaDocCodeSnippetSupported(target.apiLevel)) {
			result.tagProperties().addAll(ASTNode.copySubtrees(target, tagProperties()));
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
			acceptChildren(visitor, this.fragments);
			if (DOMASTUtil.isJavaDocCodeSnippetSupported(this.getAST().apiLevel)) {
				acceptChildren(visitor, this.tagProperties);
			}
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the live list of tag properties in this tag element.
	 *
	 * @return the live list of properties in this tag element
	 * (element type: {@link TagProperty})
	 * @exception UnsupportedOperationException if this operation is used less than JLS18
	 * @since 3.30
	 */
	public List tagProperties() {
		unsupportedBelow18();
		return this.tagProperties;
	}

	/**
	 * Returns the list of non dummy JavaDopRegions in this tag element.
	 *
	 * @return the list of non dummy JavaDopRegions in this tag element.
	 * (element type: {@link JavaDocRegion})
	 * @exception UnsupportedOperationException if this operation is used less than JLS18
	 * @since 3.30
	 */
	public List tagRegions() {
		unsupportedBelow18();
		List<JavaDocRegion> regions = new ArrayList<>();
		List<Object> frags = this.fragments();
		if ( frags != null) {
			for (Object fragment : frags) {
				if (fragment instanceof JavaDocRegion
						&& !((JavaDocRegion)fragment).isDummyRegion()) {
					regions.add((JavaDocRegion)fragment);
				}
			}
		}
		return regions;
	}

	/**
	 * Returns the list of non dummy JavaDocRegions containing this ASTNode and IDocElement.
	 *
	 * @return the list of non dummy JavaDocRegions containing this ASTNode and IDocElement.
	 * (element type: {@link JavaDocRegion})
	 * @exception UnsupportedOperationException if this operation is used less than JLS18
	 * @since 3.30
	 */
	public List tagRegionsContainingTextElement(ASTNode docElem) {
		unsupportedBelow18();
		List<JavaDocRegion> regions = new ArrayList<>();
		if (docElem == null || !(docElem instanceof IDocElement)) {
			return regions;
		} else {
			int textElemStart = docElem.getStartPosition();
			int textElemEnd = textElemStart + docElem.getLength();
			List<JavaDocRegion> javaDocRegions = this.tagRegions();
			for (JavaDocRegion region : javaDocRegions) {
				int regionStart = region.getStartPosition();
				int regionEnd = regionStart + region.getLength();
				if (regionStart <= textElemStart && regionEnd >= textElemEnd) {
					regions.add(region);
				}
			}
		}
		return regions;
	}

	/**
	 * Returns the list of non dummy JavaDocRegions starting at this ASTNode and IDocElement.
	 *
	 * @return the list of non dummy JavaDocRegions starting at this ASTNode and IDocElement.
	 * (element type: {@link JavaDocRegion})
	 * @exception UnsupportedOperationException if this operation is used less than JLS18
	 * @since 3.30
	 */
	public List tagRegionsStartingAtTextElement(ASTNode docElem) {
		unsupportedBelow18();
		List<JavaDocRegion> regions = new ArrayList<>();
		if (docElem == null || !(docElem instanceof AbstractTextElement)) {
			return regions;
		} else {
			AbstractTextElement textElem= (AbstractTextElement) docElem;
			List<JavaDocRegion> javaDocRegions = this.tagRegions();
			for (JavaDocRegion region : javaDocRegions) {
				if (!region.isDummyRegion()) {
					Object textObj= region.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_REGION_TEXT);
					if (textElem.equals(textObj)) {
						regions.add(region);
					}
				}
			}
		}
		return regions;
	}

	@Override
	int memSize() {
		return super.memSize();
	}

	@Override
	int treeSize() {
		return memSize() + (DOMASTUtil.isJavaDocCodeSnippetSupported(this.getAST().apiLevel)? this.tagProperties.listSize(): 0);
	}

}
