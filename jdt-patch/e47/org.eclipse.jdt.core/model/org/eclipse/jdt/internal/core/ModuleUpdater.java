/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IModuleAwareNameEnvironment;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdateKind;
import org.eclipse.jdt.internal.compiler.env.IUpdatableModule.UpdatesByKind;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * An instance of this class collects <code>add-exports</code> and <code>add-reads</code> options from
 * a project's class path entries, and performs the corresponding updates when requested by the compiler.
 * <p>For <code>patch-module</code> and <code>limit-modules</code> see
 * org.eclipse.jdt.internal.core.builder.ModuleEntryProcessor.</p>
 */
public class ModuleUpdater {

	private JavaProject javaProoject;

	private Map<String,UpdatesByKind> moduleUpdates = new HashMap<>();

	public ModuleUpdater(JavaProject javaProject) {
		this.javaProoject = javaProject;
	}

	/**
	 * Detects any ADD_EXPORTS or ADD_READS classpath attributes, parses the value,
	 * and collects the resulting module updates.
	 * @param entry a classpath entry of the current project.
	 * @throws JavaModelException 
	 */
	public void computeModuleUpdates(IClasspathEntry entry) throws JavaModelException {
		for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
			String attributeName = attribute.getName();
			String values = attribute.getValue(); // the attributes considered here may have multiple values separated by ':'
			if (attributeName.equals(IClasspathAttribute.ADD_EXPORTS)) {
				for (String value : values.split(":")) { // format: <source-module>/<package>=<target-module>(,<target-module>)* //$NON-NLS-1$
					int slash = value.indexOf('/');
					int equals = value.indexOf('=');
					if (slash != -1 && equals != -1) {
						String modName = value.substring(0, slash);
						char[] packName = value.substring(slash+1, equals).toCharArray();
						char[][] targets = CharOperation.splitOn(',', value.substring(equals+1).toCharArray());
						addModuleUpdate(modName, new IUpdatableModule.AddExports(packName, targets), UpdateKind.PACKAGE);
					} else {
						Util.log(IStatus.WARNING, "Invalid argument to add-exports: "+value); //$NON-NLS-1$
					}
				}
			} else if (attributeName.equals(IClasspathAttribute.ADD_READS)) {
				for (String value : values.split(":")) { // format: <source-module>=<target-module> //$NON-NLS-1$
					int equals = value.indexOf('=');
					if (equals != -1) {
						String srcMod = value.substring(0, equals);
						char[] targetMod = value.substring(equals+1).toCharArray();
						addModuleUpdate(srcMod, new IUpdatableModule.AddReads(targetMod), UpdateKind.MODULE);
					} else {
						Util.log(IStatus.WARNING, "Invalid argument to add-reads: "+value); //$NON-NLS-1$
					}
				}
			} else if (attributeName.equals(IClasspathAttribute.MODULE_MAIN_CLASS)) {
				IModuleDescription module = this.javaProoject.getModuleDescription();
				if (module == null)
					throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
				addModuleUpdate(module.getElementName(), m -> m.setMainClassName(values.toCharArray()), UpdateKind.MODULE);
			}
		}
	}

	public void addModuleUpdate(String moduleName, Consumer<IUpdatableModule> update, UpdateKind kind) {
		UpdatesByKind updates = this.moduleUpdates.get(moduleName);
		if (updates == null) {
			this.moduleUpdates.put(moduleName, updates = new UpdatesByKind());
		}
		updates.getList(kind, true).add(update);
	}

	/**
	 * @see IModuleAwareNameEnvironment#applyModuleUpdates(IUpdatableModule, UpdateKind)
	 */
	public void applyModuleUpdates(IUpdatableModule compilerModule, UpdateKind kind) {
		char[] name = compilerModule.name();
		if (name != ModuleBinding.UNNAMED) { // can't update the unnamed module
			UpdatesByKind updates = this.moduleUpdates.get(String.valueOf(name));
			if (updates != null) {
				for (Consumer<IUpdatableModule> update : updates.getList(kind, false))
					update.accept(compilerModule);
			}
		}
	}
	public UpdatesByKind getUpdates(String moduleName) {
		return this.moduleUpdates.get(moduleName);
	}
}
