/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.provisional;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.core.AbstractModule;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;

/**
 * Provisional API for use by JDT/UI, which may possibly be removed in a future version.
 * See <a href="https://bugs.eclipse.org/522391">Bug 522391</a>. 
 */
public class JavaModelAccess {

	/**
	 * In a Java 9 project, a classpath entry can be filtered using a {@link IClasspathAttribute#LIMIT_MODULES} attribute,
	 * otherwise a default set of roots is used as defined in JEP 261.
	 * In both cases {@link IJavaProject#findPackageFragmentRoots(IClasspathEntry)} will not contain all roots physically
	 * present in the container.
	 * This provisional API can be used to bypass any filter and get really all roots to which the given entry is resolved.
	 * 
	 * @param javaProject the Java project to search in
	 * @param entry a classpath entry of the Java project
	 * @return the unfiltered array of package fragment roots to which the classpath entry resolves
	 */
	public static IPackageFragmentRoot[] getUnfilteredPackageFragmentRoots(IJavaProject javaProject, IClasspathEntry entry) {
		try {
			JavaProject internalProject = (JavaProject) javaProject; // cast should be safe since IJavaProject is @noimplement
			IClasspathEntry[] resolvedEntries = internalProject.resolveClasspath(new IClasspathEntry[]{ entry });
			return internalProject.computePackageFragmentRoots(resolvedEntries, false /* not exported roots */, false /* don't filter! */, null /* no reverse map */);
		} catch (JavaModelException e) {
			// according to comment in JavaProject.findPackageFragmentRoots() we assume that this is caused by the project no longer existing
			return new IPackageFragmentRoot[] {};
		}
	}

	/**
	 * Answer the names of all modules directly required from the given module.
	 * @param module the module whose "requires" directives are queried
	 * @return a non-null array of module names
	 */
	public static String[] getRequiredModules(IModuleDescription module) throws JavaModelException {
		IModuleReference[] references = ((AbstractModule) module).getRequiredModules();
		return Arrays.stream(references).map(ref -> String.valueOf(ref.name())).toArray(String[]::new);
	}
	
	/**
	 * Filter the given set of system roots by the rules for root modules from JEP 261.
	 * @param allSystemRoots all physically available system modules, represented by their package fragment roots
	 * @return the list of names of default root modules
	 */
	public static List<String> defaultRootModules(Iterable<IPackageFragmentRoot> allSystemRoots) {
		return JavaProject.defaultRootModules(allSystemRoots);
	}

	/**
	 * Returns the <code>IModuleDescription</code> that the given java element contains 
	 * when regarded as an automatic module. The element must be an <code>IPackageFragmentRoot</code>
	 * or an <code>IJavaProject</code>.
	 * 
	 * <p>The returned module descriptor has a name (<code>getElementName()</code>) following
	 * the specification of <code>java.lang.module.ModuleFinder.of(Path...)</code>, but it
	 * contains no other useful information.</p>
	 * 
	 * @return the <code>IModuleDescription</code> representing this java element as an automatic module,
	 * 		never <code>null</code>.
	 * @throws JavaModelException
	 * @throws IllegalArgumentException if the provided element is neither <code>IPackageFragmentRoot</code>
	 * 	nor <code>IJavaProject</code>
	 * @since 3.14
	 */
	public static IModuleDescription getAutomaticModuleDescription(IJavaElement element) throws JavaModelException, IllegalArgumentException {
		switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
				return ((JavaProject) element).getAutomaticModuleDescription();
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				return ((PackageFragmentRoot) element).getAutomaticModuleDescription();
			default:
				throw new IllegalArgumentException("Illegal kind of java element: "+element.getElementType()); //$NON-NLS-1$
		}
	}
}
