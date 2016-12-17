/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * A package binding represents a named or unnamed package.
 *
 * @since 2.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPackageBinding extends IBinding {

	/**
	 * Returns the name of the package represented by this binding. For named
	 * packages, this is the fully qualified package name (using "." for
	 * separators). For unnamed packages, this is an empty string.
	 *
	 * @return the name of the package represented by this binding, or
	 *    an empty string for an unnamed package
	 */
	public String getName();

	/**
	 * Returns whether this package is an unnamed package.
	 * See <em>The Java Language Specification</em> section 7.4.2 for details.
	 *
	 * @return <code>true</code> if this is an unnamed package, and
	 *    <code>false</code> otherwise
	 */
	public boolean isUnnamed();

	/**
	 * Returns the list of name component making up the name of the package
	 * represented by this binding. For example, for the package named
	 * "com.example.tool", this method returns {"com", "example", "tool"}.
	 * Returns the empty list for unnamed packages.
	 *
	 * @return the name of the package represented by this binding, or the
	 *    empty list for unnamed packages
	 */
	public String[] getNameComponents();

//	/**
//	 * Finds and returns the binding for the class or interface with the given
//	 * name declared in this package.
//	 * <p>
//	 * For top-level classes and interfaces, the name here is just the simple
//	 * name of the class or interface. For nested classes and interfaces, the
//	 * name is the VM class name (in other words, a name like
//	 * <code>"Outer$Inner"</code> as used to name the class file; see
//	 * <code>ITypeBinding.getName</code>).
//	 * </p>
//	 *
//	 * @param name the name of a class or interface
//	 * @return the type binding for the class or interface with the
//	 *   given name declared in this package, or <code>null</code>
//	 *   if there is no such type
//	 */
//	public ITypeBinding findTypeBinding(String name);
}
