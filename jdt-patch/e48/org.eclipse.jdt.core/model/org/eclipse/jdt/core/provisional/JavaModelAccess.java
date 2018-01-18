/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.provisional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IModule.IModuleReference;
import org.eclipse.jdt.internal.core.AbstractModule;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.ModuleUpdater;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;

/**
 * Provisional API for use by JDT/UI, which may possibly be removed in a future version.
 * See <a href="https://bugs.eclipse.org/522391">Bug 522391</a>. 
 */
public class JavaModelAccess {

	private static final String BLANK  = " "; //$NON-NLS-1$
	private static final String COMMA  = ","; //$NON-NLS-1$
	private static final String OPTION_START  = "--"; //$NON-NLS-1$
	private static final String ADD_MODULES   = "--add-modules "; //$NON-NLS-1$
	private static final String LIMIT_MODULES = "--limit-modules "; //$NON-NLS-1$

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

	/**
	 * Returns the module-related command line options that are needed at runtime as equivalents
	 * of those options specified by {@link IClasspathAttribute}s of the following names:
	 * <ul>
	 * <li>{@link IClasspathAttribute#ADD_EXPORTS}</li>
	 * <li>{@link IClasspathAttribute#ADD_READS}</li>
	 * <li>{@link IClasspathAttribute#LIMIT_MODULES}</li>
	 * </ul>
	 * <p>Note that the {@link IClasspathAttribute#LIMIT_MODULES} value may be split into
	 * an {@code --add-modules} part and a {@code --limit-modules} part.</p>
	 *
	 * @param project the project holding the main class to be launched
	 * @param systemLibrary the classpath entry of the given project which represents the JRE System Library
	 * @return module-related command line options suitable for running the application.
	 * @throws JavaModelException when access to the classpath or module description of the given project fails.
	 */
	public static String getModuleCLIOptions(IJavaProject project, IClasspathEntry systemLibrary) throws JavaModelException {
		StringBuilder buf = new StringBuilder();
		for (IClasspathEntry classpathEntry : project.getRawClasspath()) {
			for (IClasspathAttribute classpathAttribute : classpathEntry.getExtraAttributes()) {
				String optName = classpathAttribute.getName();
				switch (optName) {
					case IClasspathAttribute.ADD_EXPORTS:
					case IClasspathAttribute.ADD_READS:
						for (String value : classpathAttribute.getValue().split(COMMA)) {
							buf.append(OPTION_START).append(optName).append(BLANK).append(value).append(BLANK);
						}
						break;
					case IClasspathAttribute.LIMIT_MODULES:
						addLimitModules(buf, project, systemLibrary, classpathAttribute.getValue());
						break;
				}
			}
		}
		return buf.toString().trim();
	}

	private static void addLimitModules(StringBuilder buf, IJavaProject prj, IClasspathEntry systemLibrary, String value) throws JavaModelException {
		String[] modules = value.split(COMMA);
		boolean isUnnamed = prj.getModuleDescription() == null;
		if (isUnnamed) {
			Set<String> selected = new HashSet<>(Arrays.asList(modules));
			List<IPackageFragmentRoot> allSystemRoots = Arrays.asList(getUnfilteredPackageFragmentRoots(prj, systemLibrary));
			Set<String> defaultModules = new HashSet<>(JavaProject.defaultRootModules(allSystemRoots));

			Set<String> limit = new HashSet<>(defaultModules);
			if (limit.retainAll(selected)) { // limit = selected ∩ default  -- only add the option, if limit ⊂ default
				if (limit.isEmpty()) {
					throw new IllegalArgumentException("Cannot hide all modules, at least java.base is required"); //$NON-NLS-1$
				}
				buf.append(LIMIT_MODULES).append(joinedSortedList(limit)).append(BLANK);
			}
			
			Set<String> add = new HashSet<>(selected);
			add.removeAll(defaultModules);
			if (!add.isEmpty()) { // add = selected \ default
				buf.append(ADD_MODULES).append(joinedSortedList(add)).append(BLANK);
			}
		} else {
			Arrays.sort(modules);
			buf.append(LIMIT_MODULES).append(String.join(COMMA, modules)).append(BLANK);
		}
	}

	private static String joinedSortedList(Collection<String> list) {
		String[] limitArray = list.toArray(new String[list.size()]);
		Arrays.sort(limitArray);
		return String.join(COMMA, limitArray);
	}

	/**
	 * Returns the module names that are compiled in projects which are built with a non-empty classpath and are on the build path of <code>project</code>
	 * @param project the project whose build path is examined
	 * @return set of module names
	 * @throws JavaModelException when access to the classpath or module description of the given project fails.
	 */
	public static Set<String> determineModulesOfProjectsWithNonEmptyClasspath(IJavaProject project) throws JavaModelException {
		if(project instanceof JavaProject) {
			// this should always be true, as IJavaProject is @noimplement
			JavaProject javaProject = (JavaProject) project;
			return ModuleUpdater.determineModulesOfProjectsWithNonEmptyClasspath(javaProject, javaProject.getExpandedClasspath());
		} 
		return Collections.emptySet();
	}

	/**
	 * Test if a type is from a location marked as test code (from the perspective of the project where it is defined.) 
	 * @param type the type that is examined
	 * @return false, if the corresponding class path entry is found and is not marked as test, otherwise true
	 * @throws JavaModelException when access to the classpath entry corresponding to the given type fails.
	 */
	public static boolean isTestCode(IType type) throws JavaModelException {
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) type.getPackageFragment().getParent();
		if (packageFragmentRoot.getJavaProject() instanceof JavaProject) {
			JavaProject javaProject = (JavaProject) packageFragmentRoot.getJavaProject();
			IClasspathEntry entry = javaProject.getClasspathEntryFor(packageFragmentRoot.getPath());
			if (entry != null && !entry.isTest()) {
				return false;
			}
		}
		return true;
	}
}
