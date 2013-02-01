/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
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
 * Type node for a qualified type (added in JLS3 API).
 * <pre>
 * QualifiedType:
 *    Type <b>.</b> SimpleName
 * </pre>
 * <p>
 * Not all node arrangements will represent legal Java constructs. In particular,
 * it is nonsense if the type is an array type or primitive type. The normal use
 * is when the type is a simple or parameterized type.
 * </p>
 * <p>
 * A type like "A.B" can be represented either of two ways:
 * <ol>
 * <li>
 * <code>QualifiedType(SimpleType(SimpleName("A")),SimpleName("B"))</code>
 * </li>
 * <li>
 * <code>SimpleType(QualifiedName(SimpleName("A"),SimpleName("B")))</code>
 * </li>
 * </ol>
 * The first form is preferred when "A" is known to be a type. However, a
 * parser cannot always determine this. Clients should be prepared to handle
 * either rather than make assumptions. (Note also that the first form
 * became possible as of JLS3; only the second form existed in JLS2 API.)
 * </p>
 *
 * @since 3.1
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class QualifiedType extends Type {
    /**
     * This index represents the position inside a parameterized qualified type.
     */
    int index;

	/**
	 * The "qualifier" structural property of this node type (child type: {@link Type}).
	 */
	public static final ChildPropertyDescriptor QUALIFIER_PROPERTY =
		new ChildPropertyDescriptor(QualifiedType.class, "qualifier", Type.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * The "name" structural property of this node type (child type: {@link SimpleName}).
	 */
	public static final ChildPropertyDescriptor NAME_PROPERTY =
		new ChildPropertyDescriptor(QualifiedType.class, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type:
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;

	static {
		List propertyList = new ArrayList(3);
		createPropertyList(QualifiedType.class, propertyList);
		addProperty(QUALIFIER_PROPERTY, propertyList);
		addProperty(NAME_PROPERTY, propertyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(propertyList);
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
		return PROPERTY_DESCRIPTORS;
	}

	/**
	 * The type node; lazily initialized; defaults to a type with
	 * an unspecfied, but legal, simple name.
	 */
	private Type qualifier = null;

	/**
	 * The name being qualified; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName name = null;

	/**
	 * Creates a new unparented node for a qualified type owned by the
	 * given AST. By default, an unspecified, but legal, qualifier and name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	QualifiedType(AST ast) {
		super(ast);
	    unsupportedIn2();
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
		if (property == QUALIFIER_PROPERTY) {
			if (get) {
				return getQualifier();
			} else {
				setQualifier((Type) child);
				return null;
			}
		}
		if (property == NAME_PROPERTY) {
			if (get) {
				return getName();
			} else {
				setName((SimpleName) child);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetChildProperty(property, get, child);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return QUALIFIED_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		QualifiedType result = new QualifiedType(target);
		result.setSourceRange(getStartPosition(), getLength());
		result.setQualifier((Type) ((ASTNode) getQualifier()).clone(target));
		result.setName((SimpleName) ((ASTNode) getName()).clone(target));
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
			acceptChild(visitor, getQualifier());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns the qualifier of this qualified type.
	 *
	 * @return the qualifier of this qualified type
	 */
	public Type getQualifier() {
		if (this.qualifier == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.qualifier == null) {
					preLazyInit();
					this.qualifier = new SimpleType(this.ast);
					postLazyInit(this.qualifier, QUALIFIER_PROPERTY);
				}
			}
		}
		return this.qualifier;
	}

	/**
	 * Sets the qualifier of this qualified type to the given type.
	 *
	 * @param type the new qualifier of this qualified type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setQualifier(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.qualifier;
		preReplaceChild(oldChild, type, QUALIFIER_PROPERTY);
		this.qualifier = type;
		postReplaceChild(oldChild, type, QUALIFIER_PROPERTY);
	}

	/**
	 * Returns the name part of this qualified type.
	 *
	 * @return the name being qualified
	 */
	public SimpleName getName() {
		if (this.name == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.name == null) {
					preLazyInit();
					this.name = new SimpleName(this.ast);
					postLazyInit(this.name, NAME_PROPERTY);
				}
			}
		}
		return this.name;
	}

	/**
	 * Sets the name part of this qualified type to the given simple name.
	 *
	 * @param name the identifier of this qualified name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public void setName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.name;
		preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.name = name;
		postReplaceChild(oldChild, name, NAME_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 3 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.qualifier == null ? 0 : getQualifier().treeSize())
			+ (this.name == null ? 0 : getName().treeSize());
	}
}

