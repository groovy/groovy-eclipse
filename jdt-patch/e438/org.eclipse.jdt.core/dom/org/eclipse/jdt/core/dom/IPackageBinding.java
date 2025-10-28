/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
	@Override
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

	/**
	 * Returns the binding of the module associated with this package binding.
	 * @return the binding of the module associated with this package, or
	 * <code>null</code> if none
	 *
	 * @since 3.14
	 */
	public default IModuleBinding getModule() {
		return null;
	}
	/**
	 * Finds and returns the binding for the class or interface with the given
	 * name declared in this package.
	 * <p>
	 * For top-level classes and interfaces, the name is just the simple
	 * name of the class or interface. For member classes and interfaces, the
	 * name is prefixed by its outer class(es) with a dot separator.
	 * For example, for the following class:
	 *</p>
	 * <pre><code>
	 *    public class Outer {
	 *        public static class Inner {
	 *             public static class Inner2 {}
	 *        }
	 *    }
	 * </code></pre> the first inner class is referenced via:
	 * <code>"Outer.Inner"</code> and it's inner class can be accessed via
	 * <code>"Outer.Inner.Inner2"</code>.
	 *
	 * @param name the name of a class or interface
	 * @return the type binding for the class or interface with the
	 *   given name declared in this package, or <code>null</code>
	 *   if no such valid type can be found
	 * @since 3.44
	 */
	public ITypeBinding findTypeBinding(String name);
}
