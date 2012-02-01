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
 * A method binding represents a method or constructor of a class or interface.
 * Method bindings usually correspond directly to method or
 * constructor declarations found in the source code.
 * However, in certain cases of references to a generic method,
 * the method binding may correspond to a copy of a generic method
 * declaration with substitutions for the method's type parameters
 * (for these, <code>getTypeArguments</code> returns a non-empty
 * list, and either <code>isParameterizedMethod</code> or
 * <code>isRawMethod</code> returns <code>true</code>).
 * And in certain cases of references to a method declared in a
 * generic type, the method binding may correspond to a copy of a
 * method declaration with substitutions for the type's type
 * parameters (for these, <code>getTypeArguments</code> returns
 * an empty list, and both <code>isParameterizedMethod</code> and
 * <code>isRawMethod</code> return <code>false</code>).
 *
 * @see ITypeBinding#getDeclaredMethods()
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMethodBinding extends IBinding {

	/**
	 * Returns whether this binding is for a constructor or a method.
	 *
	 * @return <code>true</code> if this is the binding for a constructor,
	 *    and <code>false</code> if this is the binding for a method
	 */
	public boolean isConstructor();

	/**
	 * Returns whether this binding is known to be a compiler-generated
	 * default constructor.
	 * <p>
	 * This method returns <code>false</code> for:
	 * <ul>
	 * <li>methods</li>
	 * <li>constructors with more than one parameter</li>
	 * <li>0-argument constructors where the binding information was obtained
	 * from a Java source file containing an explicit 0-argument constructor
	 * declaration</li>
	 * <li>0-argument constructors where the binding information was obtained
	 * from a Java class file (it is not possible to determine from a
	 * class file whether a 0-argument constructor was present in the source
	 * code versus generated automatically by a Java compiler)</li>
	 * </ul>
	 *
	 * @return <code>true</code> if this is known to be the binding for a
	 * compiler-generated default constructor, and <code>false</code>
	 * otherwise
	 * @since 3.0
	 */
	public boolean isDefaultConstructor();

	/**
	 * Returns the name of the method declared in this binding. The method name
	 * is always a simple identifier. The name of a constructor is always the
	 * same as the declared name of its declaring class.
	 *
	 * @return the name of this method, or the declared name of this
	 *   constructor's declaring class
	 */
	public String getName();

	/**
	 * Returns the type binding representing the class or interface
	 * that declares this method or constructor.
	 *
	 * @return the binding of the class or interface that declares this method
	 *    or constructor
	 */
	public ITypeBinding getDeclaringClass();

	/**
	 * Returns the resolved default value of an annotation type member,
	 * or <code>null</code> if the member has no default value, or if this
	 * is not the binding for an annotation type member.
	 * <p>
	 * Resolved values are represented as follows (same as for
	 * {@link IMemberValuePairBinding#getValue()}):
	 * <ul>
	 * <li>Primitive type - the equivalent boxed object</li>
	 * <li>java.lang.Class - the <code>ITypeBinding</code> for the class object</li>
	 * <li>java.lang.String - the string value itself</li>
	 * <li>enum type - the <code>IVariableBinding</code> for the enum constant</li>
	 * <li>annotation type - an <code>IAnnotationBinding</code></li>
	 * <li>array type - an <code>Object[]</code> whose elements are as per above
	 * (the language only allows single dimensional arrays in annotations)</li>
	 * </ul>
	 *
	 * @return the default value of this annotation type member, or <code>null</code>
	 * if none or not applicable
	 * @since 3.2
	 */
	public Object getDefaultValue();

	/**
	 * Returns the resolved annotations of a parameter of this method.
	 * The result returned is the same regardless of whether
	 * this is a parameterized method.
	 *
	 * @param paramIndex the index of the parameter of interest
	 * @return the resolved annotations of the <code>paramIndex</code>th parameter,
	 * or an empty list if there are none
	 * @throws ArrayIndexOutOfBoundsException if <code>paramIndex</code> is
	 * not a valid index
	 * @since 3.2
	 */
	public IAnnotationBinding[] getParameterAnnotations(int paramIndex);

	/**
	 * Returns a list of type bindings representing the formal parameter types,
	 * in declaration order, of this method or constructor. Returns an array of
	 * length 0 if this method or constructor does not takes any parameters.
	 * <p>
	 * Note that the binding for the last parameter type of a vararg method
	 * declaration like <code>void fun(Foo... args)</code> is always for
	 * an array type (i.e., <code>Foo[]</code>) reflecting the the way varargs
	 * get compiled. However, the type binding obtained directly from
	 * the <code>SingleVariableDeclaration</code> for the vararg parameter
	 * is always for the type as written; i.e., the type binding for
	 * <code>Foo</code>.
	 * </p>
	 * <p>
	 * Note: The result does not include synthetic parameters introduced by
	 * inner class emulation.
	 * </p>
	 *
	 * @return a (possibly empty) list of type bindings for the formal
	 *   parameters of this method or constructor
	 */
	public ITypeBinding[] getParameterTypes();

	/**
	 * Returns the binding for the return type of this method. Returns the
	 * special primitive <code>void</code> return type for constructors.
	 *
	 * @return the binding for the return type of this method, or the
	 *    <code>void</code> return type for constructors
	 */
	public ITypeBinding getReturnType();

	/**
	 * Returns a list of type bindings representing the types of the exceptions thrown
	 * by this method or constructor. Returns an array of length 0 if this method
	 * throws no exceptions. The resulting types are in no particular order.
	 *
	 * @return a list of type bindings for exceptions
	 *   thrown by this method or constructor
	 */
	public ITypeBinding[] getExceptionTypes();

	/**
	 * Returns the type parameters of this method or constructor binding.
	 * <p>
	 * Note that type parameters only occur on the binding of the
	 * declaring generic method. Type bindings corresponding to a raw or
	 * parameterized reference to a generic method do not carry type
	 * parameters (they instead have non-empty type arguments
	 * and non-trivial erasure).
	 * </p>
	 *
	 * @return the list of binding for the type variables for the type
	 * parameters of this method, or otherwise the empty list
	 * @see ITypeBinding#isTypeVariable()
	 * @since 3.1
	 */
	public ITypeBinding[] getTypeParameters();

	/**
	 * Returns whether this is the binding for an annotation type member.
	 *
	 * @return <code>true</code> iff this is the binding for an annotation type member
	 *         and <code>false</code> otherwise
	 * @since 3.2
	 */
	public boolean isAnnotationMember();

	/**
	 * Returns whether this method binding represents a declaration of
	 * a generic method.
	 * <p>
	 * Note that type parameters only occur on the binding of the
	 * declaring generic method; e.g., <code>public &lt;T&gt; T identity(T t);</code>.
	 * Method bindings corresponding to a raw or parameterized reference to a generic
	 * method do not carry type parameters (they instead have non-empty type arguments
	 * and non-trivial erasure).
	 * This method is fully equivalent to <code>getTypeParameters().length &gt; 0)</code>.
	 * </p>
	 * <p>
	 * Note that {@link #isGenericMethod()},
	 * {@link #isParameterizedMethod()},
	 * and {@link #isRawMethod()} are mutually exclusive.
	 * </p>
	 *
	 * @return <code>true</code> if this method binding represents a
	 * declaration of a generic method, and <code>false</code> otherwise
	 * @see #getTypeParameters()
	 * @since 3.1
	 */
	public boolean isGenericMethod();

	/**
	 * Returns whether this method binding represents an instance of
	 * a generic method corresponding to a parameterized method reference.
	 * <p>
	 * Note that {@link #isGenericMethod()},
	 * {@link #isParameterizedMethod()},
	 * and {@link #isRawMethod()} are mutually exclusive.
	 * </p>
	 *
	 * @return <code>true</code> if this method binding represents a
	 * an instance of a generic method corresponding to a parameterized
	 * method reference, and <code>false</code> otherwise
	 * @see #getMethodDeclaration()
	 * @see #getTypeArguments()
	 * @since 3.1
	 */
	public boolean isParameterizedMethod();

	/**
	 * Returns the type arguments of this generic method instance, or the
	 * empty list for other method bindings.
	 * <p>
	 * Note that type arguments only occur on a method binding that represents
	 * an instance of a generic method corresponding to a raw or parameterized
	 * reference to a generic method. Do not confuse these with type parameters
	 * which only occur on the method binding corresponding directly to the
	 * declaration of a generic method.
	 * </p>
	 *
	 * @return the list of type bindings for the type arguments used to
	 * instantiate the corrresponding generic method, or otherwise the empty list
	 * @see #getMethodDeclaration()
	 * @see #isParameterizedMethod()
	 * @see #isRawMethod()
	 * @since 3.1
	 */
	public ITypeBinding[] getTypeArguments();

	/**
	 * Returns the binding for the method declaration corresponding to this
	 * method binding.
	 * <ul>
	 * <li>For parameterized methods ({@link #isParameterizedMethod()})
	 * and raw methods ({@link #isRawMethod()}), this method returns the binding
	 * for the corresponding generic method.</li>
	 * <li>For references to the method {@link Object#getClass() Object.getClass()},
	 * returns the binding for the method declaration which is declared to return
	 * <code>Class&lt;?&gt;</code> or <code>Class&lt;? extends Object&gt;</code>. In the
	 * reference binding, the return type becomes
	 * <code>Class&lt;? extends </code><em>R</em><code>&gt;</code>, where <em>R</em>
	 * is the erasure of the static type of the receiver of the method invocation.</li>
	 * <li>For references to a signature polymorphic method from class MethodHandle,
	 * returns the declaration of the method. In the reference binding, the parameter types and
	 * the return type are determined by the concrete invocation context.</li>
	 * <li>For other method bindings, this returns the same binding.</li>
	 * </ul>
	 *
	 * @return the method binding
	 * @since 3.1
	 */
	public IMethodBinding getMethodDeclaration();

	/**
	 * Returns whether this method binding represents an instance of
	 * a generic method corresponding to a raw method reference.
	 * <p>
	 * Note that {@link #isGenericMethod()},
	 * {@link #isParameterizedMethod()},
	 * and {@link #isRawMethod()} are mutually exclusive.
	 * </p>
	 *
	 * @return <code>true</code> if this method binding represents a
	 * an instance of a generic method corresponding to a raw
	 * method reference, and <code>false</code> otherwise
	 * @see #getMethodDeclaration()
	 * @see #getTypeArguments()
	 * @since 3.1
	 */
	public boolean isRawMethod();

	/**
	 * Returns whether this method's signature is a subsignature of the given method as
	 * specified in section 8.4.2 of <em>The Java Language Specification, Third Edition</em> (JLS3).
	 *
	 * @return <code>true</code> if this method's signature is a subsignature of the given method
	 * @since 3.1
	 */
	public boolean isSubsignature(IMethodBinding otherMethod);

	/**
	 * Returns whether this is a variable arity method.
	 * <p>
	 * Note: Variable arity ("varargs") methods were added in JLS3.
	 * </p>
	 *
	 * @return <code>true</code> if this is a variable arity method,
	 *    and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isVarargs();

	/**
	 * Returns whether this method overrides the given method,
	 * as specified in section 8.4.8.1 of <em>The Java Language
	 * Specification, Third Edition</em> (JLS3).
	 *
	 * @param method the method that is possibly overriden
	 * @return <code>true</code> if this method overrides the given method,
	 * and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean overrides(IMethodBinding method);
}
