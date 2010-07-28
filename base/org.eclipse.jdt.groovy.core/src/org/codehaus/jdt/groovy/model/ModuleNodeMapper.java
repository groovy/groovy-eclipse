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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;
import org.eclipse.jdt.internal.core.util.Util;

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
		sweepAndPurgeModuleNodes();
		infoToModuleMap.put(info, node);
	}

	synchronized ModuleNode get(PerWorkingCopyInfo info) {
		sweepAndPurgeModuleNodes();
		return infoToModuleMap.get(info);
	}

	synchronized ModuleNode remove(PerWorkingCopyInfo info) {
		sweepAndPurgeModuleNodes();
		return infoToModuleMap.remove(info);
	}

	/* synchronized */public static boolean isEmpty() {
		return INSTANCE.infoToModuleMap.isEmpty();
	}

	synchronized void sweepAndPurgeModuleNodes() {
		if (System.getProperty("groovy.eclipse.model.purge") == null) {
			return;
		}

		System.out.println("ModuleNodeMap.size(): " + infoToModuleMap.size());
		List<PerWorkingCopyInfo> toPurge = new ArrayList<PerWorkingCopyInfo>();
		for (PerWorkingCopyInfo info : infoToModuleMap.keySet()) {
			if (((Integer) ReflectionUtils.getPrivateField(PerWorkingCopyInfo.class, "useCount", info)).intValue() == 0) {
				System.out.println("Bad module node map entry: " + info.getWorkingCopy().getElementName());
				Util.log(new RuntimeException("Bad module node map entry: " + info.getWorkingCopy().getElementName()));
				toPurge.add(info);
			}
		}

		if (toPurge.size() > 0) {
			System.out.println("ModuleNodeMap: Purging old working copies.");
			for (PerWorkingCopyInfo info : toPurge) {
				infoToModuleMap.remove(info);
			}
		}
	}
}