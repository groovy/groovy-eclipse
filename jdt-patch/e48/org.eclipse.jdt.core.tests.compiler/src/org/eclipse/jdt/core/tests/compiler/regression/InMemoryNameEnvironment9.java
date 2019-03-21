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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

public class InMemoryNameEnvironment9 extends InMemoryNameEnvironment implements IModuleAwareNameEnvironment {
	
	Map<String, IModule> moduleMap = new HashMap<>();

	public InMemoryNameEnvironment9(String[] compilationUnits, Map<String, IModule> moduleMap, INameEnvironment[] classLibs) {
		super(compilationUnits, classLibs);
		this.moduleMap = moduleMap;
	}

	protected <T> T fromFirstClassLib(Function<IModuleAwareNameEnvironment, T> function) {
		for (int i = 0; i < this.classLibs.length; i++) {
			INameEnvironment env = this.classLibs[i];
			if (env instanceof IModuleAwareNameEnvironment) {
				T answer = function.apply(((IModuleAwareNameEnvironment) env));
				if (answer != null)
					return answer;
			}
		}
		return null;
	}
	
	protected <T> T[] collect(Function<IModuleAwareNameEnvironment, T[]> function, Function<Integer,T[]> arraySupplier) {
		Set<T> mods = new HashSet<>();
		for (int i = 0; i < this.classLibs.length; i++) {
			INameEnvironment env = this.classLibs[i];
			if (env instanceof IModuleAwareNameEnvironment) {
				T[] someMods = function.apply((IModuleAwareNameEnvironment) env); 
				if (someMods != null) {
					for (int j = 0; j < someMods.length; j++)
						mods.add(someMods[j]);
				}
			}
		}
		return mods.toArray(arraySupplier.apply(mods.size()));
	}

	@Override
	public NameEnvironmentAnswer findType(char[][] compoundName, char[] moduleName) {
		return fromFirstClassLib(env -> env.findType(compoundName, moduleName));
	}

	@Override
	public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName, char[] moduleName) {
		return fromFirstClassLib(env -> env.findType(typeName, packageName, moduleName));
	}

	@Override
	public char[][] getModulesDeclaringPackage(char[][] parentPackageName, char[] name, char[] moduleName) {
		return collect(env -> env.getModulesDeclaringPackage(parentPackageName, name, moduleName), char[][]::new);
	}

	@Override
	public boolean hasCompilationUnit(char[][] qualifiedPackageName, char[] moduleName, boolean checkCUs) {
		return Boolean.TRUE.equals(fromFirstClassLib(env -> env.hasCompilationUnit(qualifiedPackageName, moduleName, checkCUs) ? Boolean.TRUE : null));
	}

	@Override
	public IModule getModule(char[] moduleName) {
		IModule mod = this.moduleMap.get(String.valueOf(moduleName));
		if (mod != null)
			return mod;
		return fromFirstClassLib(env -> env.getModule(moduleName));
	}

	@Override
	public char[][] getAllAutomaticModules() {
		return collect(env -> env.getAllAutomaticModules(), char[][]::new);
	}

}
