/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * Creation reference expression AST node type (added in JLS8 API).
 * 
 * <pre>
 * CreationReference:
 *     Type <b>::</b> 
 *         [ <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b> ]
 *         <b>new</b>
 * </pre>
 *
 * @since 3.10
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CreationReference extends MethodReference {

	/**
	 * The "type" structural property of this node type (child type: {@link Type}).
	 */
	public static final ChildPropertyDescriptor TYPE_PROPERTY =
		new ChildPropertyDescriptor(CreationReference.class, "type", Type.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "typeArguments" structural property of this node type (element type: {@link Type}).
	 */
	public static final ChildListPropertyDescriptor TYPE_ARGUMENTS_PROPERTY =
		internalTypeArgumentsFactory(CreationReference.class);

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS_8_0;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(CreationReference.class, propertyList);
		addProperty(TYPE_PROPERTY, propertyList);
		addProperty(TYPE_ARGUMENTS_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS_8_0 = reapPropertyList(propertyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 *
	 * @param apiLevel the API level; one of the AST.JLS* constants
	 * @return a list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor})
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS_8_0;
	}

	/**
	 * The type; lazily initialized; defaults to an unspecified type.
	 */
	private Type type = null;

	/**
	 * Creates a new AST node for an CreationReference declaration owned
	 * by the given AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	CreationReference(AST ast) {
		super(ast);
		unsupportedIn2_3_4();
	}

	/* (omit javadoc for this method)
	 * Method declared on MethodReference.
	 */
	final ChildListPropertyDescriptor internalTypeArgumentsProperty() {
		return TYPE_ARGUMENTS_PROPERTY;
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
	final ASTNode internalGetSetChildProperty(ChildPropertyDescriptor property, boolean get, ASTNode child) {
		if (property == TYPE_PROPERTY) {
			if (get) {
				return getType();
			} else {
				setType((Type) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalGetChildListProperty(ChildListPropertyDescriptor property) {
		if (property == TYPE_ARGUMENTS_PROPERTY) {
			return typeArguments();
		}
		// allow default implementation to flag the error
		return super.internalGetChildListProperty(property);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return CREATION_REFERENCE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		CreationReference result = new CreationReference(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setType((Type) ASTNode.copySubtree(target, getType()));
		result.typeArguments().addAll(ASTNode.copySubtrees(target, typeArguments()));
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getType());
			acceptChildren(visitor, this.typeArguments);
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the type of this creation reference expression.
	 *
	 * @return the type node
	 */
	public Type getType() {
		if (this.type == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.type == null) {
					preLazyInit();
					this.type = new SimpleType(this.ast);
					postLazyInit(this.type, TYPE_PROPERTY);
				}
			}
		}
		return this.type;
	}

	/**
	 * Sets the type of this creation reference expression.
	 *
	 * @param type the new type node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.type;
		preReplaceChild(oldChild, type, TYPE_PROPERTY);
		this.type = type;
		postReplaceChild(oldChild, type, TYPE_PROPERTY);
	}

	/**
	 * Returns the live ordered list of type arguments of this creation reference expression.
	 *
	 * @return the live list of type arguments
	 *    (element type: {@link Type})
	 */
	public List typeArguments() {
		return this.typeArguments;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 2 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.type == null ? 0 : getType().treeSize())
			+ (this.typeArguments == null ? 0 : this.typeArguments.listSize());
	}
}