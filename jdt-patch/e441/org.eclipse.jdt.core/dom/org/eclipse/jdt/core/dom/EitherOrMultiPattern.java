/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.internal.core.dom.util.DOMASTUtil;

/**
 * EitherOrMultiPattern AST node type.
 *
 * <pre>
 * {@code
 * EitherOrMultiPattern:
 *      Pattern, Pattern, Pattern....
 * }
 * </pre>
 *
 * @since 3.37
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
@SuppressWarnings("rawtypes")
public class EitherOrMultiPattern extends Pattern {

	/**
	 * The "patterns" structural property of this node type (child type: {@link Pattern}).
	 */
	public static final ChildListPropertyDescriptor PATTERNS_PROPERTY =
			new ChildListPropertyDescriptor(EitherOrMultiPattern.class, "patterns", Pattern.class, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List properyList = new ArrayList(5);
		createPropertyList(EitherOrMultiPattern.class, properyList);
		addProperty(PATTERNS_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(properyList);
	}

	@Override
	int getNodeType0() {
		return ASTNode.EitherOr_MultiPattern;
	}

	EitherOrMultiPattern(AST ast) {
		super(ast);
		supportedOnlyIn21();
	}

	/**
	 * The patterns
	 * (element type: {@link Pattern}).
	 * Defaults to an empty list.
	 */
	private final ASTNode.NodeList patterns =
		new ASTNode.NodeList(PATTERNS_PROPERTY);

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS*</code> constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 * @noreference This method is not intended to be referenced by clients.
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
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static List propertyDescriptors(int apiLevel, boolean previewEnabled) {
		if (DOMASTUtil.isEitherOrMultiPatternSupported(apiLevel, previewEnabled)) {
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
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == PATTERNS_PROPERTY) {
			return patterns();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/**
	 * Returns the "alternatives" Pattern list.
	 *
	 * @return the live list of pattern nodes
	 *    (element type: {@link Pattern})
	 * @exception UnsupportedOperationException if this operation is used other than JLS21
	 * @exception UnsupportedOperationException if this expression is used with previewEnabled flag as false
	 * @noreference This method is not intended to be referenced by clients as it is a part of Java preview feature.
	 */
	@SuppressWarnings("unchecked")
	public List<Pattern> patterns() {
		supportedOnlyIn21();
		return this.patterns;
	}


	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		return matcher.match(this, other);
	}

	@Override
	ASTNode clone0(AST target) {
		EitherOrMultiPattern result = new EitherOrMultiPattern(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.patterns().addAll(ASTNode.copySubtrees(target, patterns()));
		return result;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChildren(visitor, this.patterns);
		}
		visitor.endVisit(this);
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4 ;
	}

	@Override
	int treeSize() {
		return memSize()+ this.patterns.listSize();
	}
}