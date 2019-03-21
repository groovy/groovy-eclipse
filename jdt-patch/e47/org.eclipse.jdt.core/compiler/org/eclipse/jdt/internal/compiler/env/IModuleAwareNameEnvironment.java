/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import java.util.function.Predicate;

import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;

/**
 * A module aware name environment
 */
public interface IModuleAwareNameEnvironment extends INameEnvironment {

	/** Strategies for searching types & packages in classpath locations & modules. */
	enum LookupStrategy {
		/** Search a specific named module only. */
		Named {
			@Override
			public <T> boolean matchesWithName(T elem, Predicate<T> isNamed, Predicate<T> nameMatcher) {
				assert nameMatcher != null : "name match needs a nameMatcher"; //$NON-NLS-1$
				return isNamed.test(elem) && nameMatcher.test(elem);
			}
		},
		/** Search all named modules. */
		AnyNamed {
			@Override
			public <T> boolean matchesWithName(T elem, Predicate<T> isNamed, Predicate<T> nameMatcher) {
				return isNamed.test(elem);
			}
		},
		/** Search all locations, module or otherwise. */
		Any {
			@Override
			public <T> boolean matchesWithName(T elem, Predicate<T> isNamed, Predicate<T> nameMatcher) {
				return true;
			}
		},
		/** Search only the unnamed module. */
		Unnamed {
			@Override
			public <T> boolean matchesWithName(T elem, Predicate<T> isNamed, Predicate<T> nameMatcher) {
				return !isNamed.test(elem);
			}
		};
		/**
		 * Test whether the given element matches this lookup strategy.
		 * @param elem location being tests
		 * @param isNamed predicate to determine if 'elem' represents a named module
		 * @param nameMatcher predicate to test if 'elem' matches the expected module name
		 * @return true iff the given element matches this lookup strategy.
		 */
		public abstract <T> boolean matchesWithName(T elem, Predicate<T> isNamed, Predicate<T> nameMatcher);
		/**
		 * Test whether the given element matches this lookup strategy.
		 * @param elem location being tests
		 * @param isNamed predicate to determine if 'elem' represents a named module
		 * @return true iff the given element matches this lookup strategy.
		 */
		public <T> boolean matches(T elem, Predicate<T> isNamed) {
			return matchesWithName(elem, isNamed, t -> true);
		}
		
		/** Get the lookup strategy corresponding to the given module name. */
		public static LookupStrategy get(char[] moduleName) {
			if (moduleName == ModuleBinding.ANY)
				return Any;
			if (moduleName == ModuleBinding.ANY_NAMED)
				return AnyNamed;
			if (moduleName == ModuleBinding.UNNAMED)
				return Unnamed;
			return Named;
		}
		/** If 'moduleName' is none of the special names (ANY, ANY_NAMED, UNNAMED) return the string converted name, else {@code null}. */
		public static String getStringName(char[] moduleName) {
			switch (get(moduleName)) {
				case Named : return String.valueOf(moduleName);
				default: return null;
			}
		}
	}
	
	default NameEnvironmentAnswer findType(char[][] compoundTypeName) {
		return findType(compoundTypeName, ModuleBinding.ANY);
	}
	default NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
		return findType(typeName, packageName, ModuleBinding.ANY);
	}
	default boolean isPackage(char[][] parentPackageName, char[] packageName) {
		return getModulesDeclaringPackage(parentPackageName, packageName, ModuleBinding.ANY) != null;
	}

	NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName);
	/** Answer a type identified by the given names. moduleName may be one of the special names from ModuleBinding (ANY, ANY_NAMED, UNNAMED). */
	NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName);
	char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] name, char[] moduleName);
	
	/**
	 * Answer whether the given package (within the given module) contains any compilation unit.
	 * @param qualifiedPackageName
	 * @param checkCUs - if true, check contained Compilation Units for a matching package declaration
	 * @return true iff the package contains at least one compilation unit.
	 */
	boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs);
	
	/** Get the module with the given name, which must denote a named module. */
	IModule getModule(char[] moduleName);
	char[][] getAllAutomaticModules();

	/**
	 * Ask the name environment to perform any updates (add-exports or add-reads) to the given module.
	 * @param module the compiler representation of the module to updates
	 * @param kind selects what kind of updates should be performed
	 */
	default void applyModuleUpdates(IUpdatableModule module, IUpdatableModule.UpdateKind kind) { /* default: do nothing */ }
}
