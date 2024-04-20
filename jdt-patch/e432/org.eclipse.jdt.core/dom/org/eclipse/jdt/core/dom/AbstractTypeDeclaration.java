/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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

/**
 * Abstract subclass for type declaration, enum declaration,
 * and annotation type declaration AST node types.
 * <pre>
 * AbstractTypeDeclaration:
 * 		TypeDeclaration
 * 		EnumDeclaration
 * 		AnnotationTypeDeclaration
 * </pre>
 *
 * @since 3.0
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractTypeDeclaration extends AbstractUnnamedTypeDeclaration {

	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal Java class identifier.
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	volatile SimpleName typeName;

	/**
	 * Returns structural property descriptor for the "name" property
	 * of this node (child type: {@link SimpleName}).
	 *
	 * @return the property descriptor
	 */
	abstract ChildPropertyDescriptor internalNameProperty();

	/**
	 * Returns structural property descriptor for the "name" property
	 * of this node (child type: {@link SimpleName}).
	 *
	 * @return the property descriptor
	 * @since 3.1
	 */
	public final ChildPropertyDescriptor getNameProperty() {
		return internalNameProperty();
	}

	/**
	 * Creates and returns a structural property descriptor for the
	 * "name" property declared on the given concrete node type (child type: {@link SimpleName}).
	 *
	 * @return the property descriptor
	 */
	static final ChildPropertyDescriptor internalNamePropertyFactory(Class nodeClass) {
		return new ChildPropertyDescriptor(nodeClass, "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK); //$NON-NLS-1$
	}

	/**
	 * Creates a new AST node for an abstract type declaration owned by the given
	 * AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be
	 * declared in the same package; clients are unable to declare
	 * additional subclasses.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	AbstractTypeDeclaration(AST ast) {
		super(ast);
	}

	/**
	 * Returns the name of the type declared in this type declaration.
	 *
	 * @return the type name node
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	public SimpleName getName() {
		if (this.typeName == null) {
			// lazy init must be thread-safe for readers
			synchronized (this) {
				if (this.typeName == null) {
					preLazyInit();
					this.typeName = new SimpleName(this.ast);
					postLazyInit(this.typeName, internalNameProperty());
				}
			}
		}
		return this.typeName;
	}

	/**
	 * Sets the name of the type declared in this type declaration to the
	 * given name.
	 *
	 * @param typeName the new type name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 * @since 2.0 (originally declared on {@link TypeDeclaration})
	 */
	public void setName(SimpleName typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		ChildPropertyDescriptor p = internalNameProperty();
		ASTNode oldChild = this.typeName;
		preReplaceChild(oldChild, typeName, p);
		this.typeName = typeName;
		postReplaceChild(oldChild, typeName, p);
	}

	/**
	 * Resolves and returns the binding for the type declared in this type
	 * declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 * @since 3.1 Declared in 3.0 on the individual subclasses.
	 */
	public final ITypeBinding resolveBinding() {
		return internalResolveBinding();
	}

	/**
	 * Resolves and returns the binding for the type declared in this type
	 * declaration. This method must be implemented by subclasses.
	 *
	 * @return the binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 */
	abstract ITypeBinding internalResolveBinding();

	@Override
	int memSize() {
		return super.memSize() + 4;
	}

}
