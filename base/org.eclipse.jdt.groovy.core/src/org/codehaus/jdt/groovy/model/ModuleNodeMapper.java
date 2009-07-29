/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.jdt.groovy.model;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;

/**
 * @author Andrew Eisenberg
 * @created Jun 11, 2009
 * 
 *          This class stores module nodes for groovy compilation units This class is not meant to be accessed externally.
 * 
 *          One module node is stored per working copy of
 */
public class ModuleNodeMapper {

	private static final ModuleNodeMapper INSTANCE = new ModuleNodeMapper();

	static ModuleNodeMapper getInstance() {
		return INSTANCE;
	}

	private final Map<PerWorkingCopyInfo, ModuleNode> infoToModuleMap = new HashMap<PerWorkingCopyInfo, ModuleNode>();

	synchronized void store(PerWorkingCopyInfo info, ModuleNode node) {
		infoToModuleMap.put(info, node);
	}

	synchronized ModuleNode get(PerWorkingCopyInfo info) {
		return infoToModuleMap.get(info);
	}

	synchronized ModuleNode remove(PerWorkingCopyInfo info) {
		return infoToModuleMap.remove(info);
	}

	public static boolean isEmpty() {
		return INSTANCE.infoToModuleMap.isEmpty();
	}
}
