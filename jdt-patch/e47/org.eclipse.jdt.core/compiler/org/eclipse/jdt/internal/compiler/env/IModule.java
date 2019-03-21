/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import java.util.jar.Manifest;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public interface IModule {

	public static IModuleReference[] NO_MODULE_REFS = new IModuleReference[0];
	public static IPackageExport[] NO_EXPORTS = new IPackageExport[0];
	public static char[][] NO_USES = new char[0][];
	public static IService[] NO_PROVIDES = new IService[0];
	public static IModule[] NO_MODULES = new IModule[0];
	public static IPackageExport[] NO_OPENS = new IPackageExport[0];

	public String MODULE_INFO = "module-info"; //$NON-NLS-1$
	public String MODULE_INFO_JAVA = "module-info.java"; //$NON-NLS-1$
	public String MODULE_INFO_CLASS = "module-info.class"; //$NON-NLS-1$

	public char[] name();

	public IModuleReference[] requires();

	public IPackageExport[] exports();

	public char[][] uses();

	public IService[] provides();

	/*
	 * the opens package statement is very similar to package export statement, hence
	 * the same internal models are being used here.
	 */
	public IPackageExport[] opens();

	public interface IModuleReference {
		public char[] name();
		public default boolean isTransitive() {
			return (getModifiers() & ClassFileConstants.ACC_TRANSITIVE) != 0;
		}
		public int getModifiers();
		public default boolean isStatic() {
			return (getModifiers() & ClassFileConstants.ACC_STATIC_PHASE) != 0;
		}
	}

	public interface IPackageExport {
		public char[] name();
		public char[][] targets();
		public default boolean isQualified() {
			char[][] targets = targets();
			return targets != null && targets.length > 0;
		}
	}

	public interface IService {
		public char[] name();
		char[][] with();
	}
	
	public default void addReads(char[] modName) {
		// do nothing, would throwing an exception be better?
	}
	
	public default void addExports(IPackageExport[] exports) {
		// do nothing, would throwing an exception be better?
	}

	public default boolean isAutomatic() {
		return false;
	}
	public abstract boolean isOpen();


	public static IModule createAutomatic(char[] moduleName) {
		final class AutoModule implements IModule {
			char[] name;
			public AutoModule(char[] name) {
				this.name = name;
			}
			@Override
			public char[] name() {
				return this.name;
			}
			
			@Override
			public IModuleReference[] requires() {
				return IModule.NO_MODULE_REFS;
			}
			
			@Override
			public IPackageExport[] exports() {
				return IModule.NO_EXPORTS;
			}
			
			@Override
			public char[][] uses() {
				return IModule.NO_USES;
			}
			
			@Override
			public IService[] provides() {
				return IModule.NO_PROVIDES;
			}
			
			@Override
			public IPackageExport[] opens() {
				return NO_OPENS;
			}
			
			public boolean isAutomatic() {
				return true;
			}
			public boolean isOpen() {
				return false;
			}
		}
		return new AutoModule(moduleName);
	}

	public static IModule createAutomatic(String fileName, boolean isFile, Manifest manifest) {
		return createAutomatic(AutomaticModuleNaming.determineAutomaticModuleName(fileName, isFile, manifest));
	}
}
