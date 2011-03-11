/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Abstract base class of all AST node types that declare a single local
 * variable.
 * <p>
 * <pre>
 * VariableDeclaration:
 *    SingleVariableDeclaration
 *    VariableDeclarationFragment
 * </pre>
 * </p>
 *
 * @see SingleVariableDeclaration
 * @see VariableDeclarationFragment
 * @since 2.0
 */
public abstract class VariableDeclaration extends ASTNode {

	/**
	 * Returns structural property descriptor for the "extraDimensions" property
	 * of this node (type: {@link Integer}).
	 *
	 * @return the property descriptor
	 * @since 3.1
	 */
	abstract SimplePropertyDescriptor internalExtraDimensionsProperty();

	/**
	 * Returns structural property descriptor for the "extraDimensions" property
	 * of this node (type: {@link Integer}).
	 *
	 * @return the property descriptor
	 * @since 3.1
	 */
	public final SimplePropertyDescriptor getExtraDimensionsProperty() {
		return internalExtraDimensionsProperty();
	}

	/**
	 * Returns structural property descriptor for the "initializer" property
	 * of this node (child type: {@link Expression}).
	 *
	 * @return the property descriptor
	 * @since 3.1
	 */
	abstract ChildPropertyDescriptor internalInitializerProperty();

	/**
	 * Returns structural property descriptor for the "initializer" property
	 * of this node (child type: {@link Expression}).
	 *
	 * @return the property descriptor
	 * @since 3.1
	 */
	public final ChildPropertyDescriptor getInitializerProperty() {
		return internalInitializerProperty();
	}

	/**
	 * Returns structural property descriptor for the "name" property
	 * of this node (child type: {@link SimpleName}).
	 *
	 * @return the property descriptor
	 * @since 3.1
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
	 * Creates a new AST node for a variable declaration owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	VariableDeclaration(AST ast) {
		super(ast);
	}

	/**
	 * Returns the name of the variable declared in this variable declaration.
	 *
	 * @return the variable name node
	 */
	public abstract SimpleName getName();

	/**
	 * Sets the name of the variable declared in this variable declaration
	 * to the given name.
	 *
	 * @param variableName the new variable name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */
	public abstract void setName(SimpleName variableName);

	/**
	 * Returns the number of extra array dimensions over and above the
	 * explicitly-specified type.
	 * <p>
	 * For example, <code>int x[][]</code> has a type of
	 * <code>int</code> and two extra array dimensions;
	 * <code>int[][] x</code> has a type of <code>int[][]</code>
	 * and zero extra array dimensions. The two constructs have different
	 * ASTs, even though there are really syntactic variants of the same
	 * variable declaration.
	 * </p>
	 *
	 * @return the number of extra array dimensions
	 * @since 2.1
	 */
	public abstract int getExtraDimensions();

	/**
	 * Sets the number of extra array dimensions over and above the
	 * explicitly-specified type.
	 * <p>
	 * For example, <code>int x[][]</code> has a type of
	 * <code>int</code> and two extra array dimensions;
	 * <code>int[][] x</code> has a type of <code>int[][]</code>
	 * and zero extra array dimensions. The two constructs have different
	 * ASTs, even though there are really syntactic variants of the same
	 * variable declaration.
	 * </p>
	 *
	 * @param dimensions the number of array dimensions
	 * @exception IllegalArgumentException if the number of dimensions is
	 *    negative
	 * @since 2.1
	 */
	public abstract void setExtraDimensions(int dimensions);

	/**
	 * Returns the initializer of this variable declaration, or
	 * <code>null</code> if there is none.
	 *
	 * @return the initializer expression node, or <code>null</code> if
	 *    there is none
	 */
	public abstract Expression getInitializer();

	/**
	 * Sets or clears the initializer of this variable declaration.
	 *
	 * @param initializer the initializer expression node, or <code>null</code>
	 *    if there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public abstract void setInitializer(Expression initializer);

	/**
	 * Resolves and returns the binding for the variable declared in this
	 * variable declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 */
	public IVariableBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveVariable(this);
	}
}
