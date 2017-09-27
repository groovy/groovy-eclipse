/*******************************************************************************
 * Copyright (c) 2016 Sven Strohschein and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sven Strohschein - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;

public class PackageBindingTest extends AbstractCompilerTest
{
	public PackageBindingTest(String name) {
		super(name);
	}

	/**
	 * This test checks if it is searched for packages before searching for types.
	 * The search for packages is much faster than searching for types, therefore it should get executed before searching for types.
	 * Commented since reverted to original behaviour as per bug 495598
	 */
	public void _test01() {
		NameEnvironmentDummy nameEnv = new NameEnvironmentDummy(true);

		PackageBinding packageBinding = new PackageBinding(new LookupEnvironment(null, new CompilerOptions(), null, nameEnv));
		Binding resultBinding = packageBinding.getTypeOrPackage("java.lang".toCharArray());
		assertNotNull(resultBinding);

		assertTrue(nameEnv.isPackageSearchExecuted);
		assertFalse(nameEnv.isTypeSearchExecuted);
	}

	/**
	 * This test checks if it is searched for types when no package was found.
	 * The test {@link #test01()} checks if the package search is executed before the type search.
	 * The search for packages is much faster than searching for types, therefore it should get executed before searching for types.
	 */
	public void test02() {
		NameEnvironmentDummy nameEnv = new NameEnvironmentDummy(false);

		PackageBinding packageBinding = new PackageBinding(new LookupEnvironment(null, new CompilerOptions(), null, nameEnv));
		Binding resultBinding = packageBinding.getTypeOrPackage("java.lang.String".toCharArray());
		assertNull(resultBinding); // (not implemented)

		assertTrue(nameEnv.isPackageSearchExecuted);
		assertTrue(nameEnv.isTypeSearchExecuted);
	}

	/**
	 * This test checks if {@link INameEnvironment#findType(char[], char[][])} is executed.
	 * INameEnvironment has no option to avoid the search for secondary types, therefore the search for secondary types is executed (when available).
	 */
	public void test03() {
		NameEnvironmentDummy nameEnv = new NameEnvironmentDummy(false);

		LookupEnvironment lookupEnv = new LookupEnvironment(null, new CompilerOptions(), null, nameEnv);
		PackageBinding packageBinding = lookupEnv.createPackage(new char[][]{"org/eclipse/jdt".toCharArray(), "org/eclipse/jdt/internal".toCharArray()});
		assertNotNull(packageBinding);

		assertTrue(nameEnv.isTypeSearchExecuted); //the method findType(char[], char[][]) should got executed (without an option to avoid the search for secondary types)
	}

	/**
	 * This test checks if types are searched on creating a package, but without the search for secondary types.
	 * It isn't necessary to search for secondary types when creating a package, because a package name can not collide with a secondary type.
	 * The search for secondary types should not get executed, because the search for secondary types is very expensive regarding performance
	 * (all classes of a package have to get loaded, parsed and analyzed).
	 */
	public void test04() {
		NameEnvironmentWithProgressDummy nameEnvWithProgress = new NameEnvironmentWithProgressDummy();

		LookupEnvironment lookupEnv = new LookupEnvironment(null, new CompilerOptions(), null, nameEnvWithProgress);
		PackageBinding packageBinding = lookupEnv.createPackage(new char[][]{"org/eclipse/jdt".toCharArray(), "org/eclipse/jdt/internal".toCharArray()});
		assertNotNull(packageBinding);

		assertTrue(nameEnvWithProgress.isTypeSearchExecutedWithSearchWithSecondaryTypes); //the method findType(char[], char[][], boolean) should got executed ...
		assertFalse(nameEnvWithProgress.isTypeSearchWithSearchWithSecondaryTypes); //... but without the search for secondary types
	}

	private class NameEnvironmentDummy implements INameEnvironment
	{
		private final boolean isPackage;
		boolean isPackageSearchExecuted;
		boolean isTypeSearchExecuted;

		NameEnvironmentDummy(boolean isPackage) {
			this.isPackage = isPackage;
		}

		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
			this.isTypeSearchExecuted = true;
			return null;
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
			this.isTypeSearchExecuted = true;
			return null;
		}

		@Override
		public boolean isPackage(char[][] parentPackageName, char[] packageName) {
			this.isPackageSearchExecuted = true;
			return this.isPackage;
		}

		@Override
		public void cleanup() {
		}
	}

	private class NameEnvironmentWithProgressDummy implements INameEnvironmentWithProgress
	{
		boolean isTypeSearchWithSearchWithSecondaryTypes;
		boolean isTypeSearchExecutedWithSearchWithSecondaryTypes;

		NameEnvironmentWithProgressDummy() {}

		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
			return null;
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
			return null;
		}

		@Override
		public boolean isPackage(char[][] parentPackageName, char[] packageName) {
			return false;
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void setMonitor(IProgressMonitor monitor) {
		}

		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, boolean searchWithSecondaryTypes) {
			this.isTypeSearchExecutedWithSearchWithSecondaryTypes = true;
			this.isTypeSearchWithSearchWithSecondaryTypes = searchWithSecondaryTypes;
			return null;
		}
	}
}