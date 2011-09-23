/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 * Abstract base class of all type AST node types. A type node represents a
 * reference to a primitive type (including void), to an array type, or to a
 * simple named type (or type variable), to a qualified type, to a
 * parameterized type, or to a wildcard type. Note that not all of these
 * are meaningful in all contexts; for example, a wildcard type is only
 * meaningful in the type argument position of a parameterized type.
 * <p>
 * <pre>
 * Type:
 *    PrimitiveType
 *    ArrayType
 *    SimpleType
 *    QualifiedType
 *    ParameterizedType
 *    WildcardType
 * PrimitiveType:
 *    <b>byte</b>
 *    <b>short</b>
 *    <b>char</b>
 *    <b>int</b>
 *    <b>long</b>
 *    <b>float</b>
 *    <b>double</b>
 *    <b>boolean</b>
 *    <b>void</b>
 * ArrayType:
 *    Type <b>[</b> <b>]</b>
 * SimpleType:
 *    TypeName
 * ParameterizedType:
 *    Type <b>&lt;</b> Type { <b>,</b> Type } <b>&gt;</b>
 * QualifiedType:
 *    Type <b>.</b> SimpleName
 * WildcardType:
 *    <b>?</b> [ ( <b>extends</b> | <b>super</b>) Type ]
 * </pre>
 * </p>
 *
 * @since 2.0
 */
public abstract class Type extends ASTNode {

	/**
	 * Creates a new AST node for a type owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	Type(AST ast) {
		super(ast);
	}

	/**
	 * Returns whether this type is a primitive type
	 * ({@link PrimitiveType}).
	 *
	 * @return <code>true</code> if this is a primitive type, and
	 *    <code>false</code> otherwise
	 */
	public final boolean isPrimitiveType() {
		return (this instanceof PrimitiveType);
	}

	/**
	 * Returns whether this type is a simple type
	 * ({@link SimpleType}).
	 *
	 * @return <code>true</code> if this is a simple type, and
	 *    <code>false</code> otherwise
	 */
	public final boolean isSimpleType() {
		return (this instanceof SimpleType);
	}

	/**
	 * Returns whether this type is an array type
	 * ({@link ArrayType}).
	 *
	 * @return <code>true</code> if this is an array type, and
	 *    <code>false</code> otherwise
	 */
	public final boolean isArrayType() {
		return (this instanceof ArrayType);
	}

	/**
	 * Returns whether this type is a parameterized type
	 * ({@link ParameterizedType}).
	 *
	 * @return <code>true</code> if this is a parameterized type, and
	 *    <code>false</code> otherwise
	 * @since 3.1
	 */
	public final boolean isParameterizedType() {
		return (this instanceof ParameterizedType);
	}

	/**
	 * Returns whether this type is a qualified type
	 * ({@link QualifiedType}).
	 * <p>
	 * Note that a type like "A.B" can be represented either of two ways:
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
	 * became possible as of JLS3; only the second form existed in the
	 * JLS2 API.)
	 * </p>
	 *
	 * @return <code>true</code> if this is a qualified type, and
	 *    <code>false</code> otherwise
	 * @since 3.1
	 */
	public final boolean isQualifiedType() {
		return (this instanceof QualifiedType);
	}

	/**
	 * Returns whether this type is a union type
	 * ({@link UnionType}).
	 *
	 * @return <code>true</code> if this is a union type, and
	 *    <code>false</code> otherwise
	 * @since 3.7.1
	 */
	public final boolean isUnionType() {
		return (this instanceof UnionType);
	}

	/**
	 * Returns whether this type is a wildcard type
	 * ({@link WildcardType}).
	 * <p>
	 * Note that a wildcard type is only meaningful as a
	 * type argument of a {@link ParameterizedType} node.
	 * </p>
	 *
	 * @return <code>true</code> if this is a wildcard type, and
	 *    <code>false</code> otherwise
	 * @since 3.1
	 */
	public final boolean isWildcardType() {
		return (this instanceof WildcardType);
	}

	/**
	 * Resolves and returns the binding for this type.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 *
	 * @return the type binding, or <code>null</code> if the binding cannot be
	 *    resolved
	 */
	public final ITypeBinding resolveBinding() {
		return this.ast.getBindingResolver().resolveType(this);
	}
}
