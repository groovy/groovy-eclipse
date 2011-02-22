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
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.JavaModelManager;
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

	private class ModuleNodeInfo {
		public ModuleNodeInfo(ModuleNode module, JDTResolver resolver) {
			this.module = module;
			this.resolver = resolver;
		}

		final ModuleNode module;
		final JDTResolver resolver;
	}

	private static final ModuleNodeMapper INSTANCE = new ModuleNodeMapper();

	static ModuleNodeMapper getInstance() {
		return INSTANCE;
	}

	private final Map<PerWorkingCopyInfo, ModuleNodeInfo> infoToModuleMap = new HashMap<PerWorkingCopyInfo, ModuleNodeInfo>();

	synchronized void store(PerWorkingCopyInfo info, ModuleNode module, JDTResolver resolver) {
		sweepAndPurgeModuleNodes();
		infoToModuleMap.put(info, new ModuleNodeInfo(module, shouldStoreResovler() ? resolver : null));
	}

	private final static boolean DSL_BUNDLE_INSTALLED;
	static {
		boolean result = false;
		try {
			result = Platform.getBundle("org.codehaus.groovy.eclipse.dsl") != null;
		} catch (Exception e) {
			Util.log(e);
		}
		DSL_BUNDLE_INSTALLED = result;
	}

	private boolean shouldStoreResovler() {
		return DSL_BUNDLE_INSTALLED;
	}

	synchronized ModuleNode get(PerWorkingCopyInfo info) {
		sweepAndPurgeModuleNodes();
		ModuleNodeInfo moduleNodeInfo = infoToModuleMap.get(info);
		return moduleNodeInfo != null ? moduleNodeInfo.module : null;
	}

	synchronized JDTResolver getResolver(PerWorkingCopyInfo info) {
		if (info == null) {
			return null;
		}
		sweepAndPurgeModuleNodes();
		ModuleNodeInfo moduleNodeInfo = infoToModuleMap.get(info);
		return moduleNodeInfo != null ? moduleNodeInfo.resolver : null;
	}

	synchronized ModuleNode remove(PerWorkingCopyInfo info) {
		sweepAndPurgeModuleNodes();
		ModuleNodeInfo removed = infoToModuleMap.remove(info);
		return removed != null ? removed.module : null;
	}

	/**
	 * Cache the module node if this is a working copy
	 * 
	 * @param perWorkingCopyInfo
	 * @param compilationUnitDeclaration
	 */
	synchronized protected void maybeCacheModuleNode(JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo,
			GroovyCompilationUnitDeclaration compilationUnitDeclaration) {
		if (perWorkingCopyInfo != null && compilationUnitDeclaration != null) {
			ModuleNode module = compilationUnitDeclaration.getModuleNode();

			// Store it for later
			if (module != null) {
				JDTResolver resolver;
				if (shouldStoreResovler()) {
					resolver = (JDTResolver) compilationUnitDeclaration.getCompilationUnit().getResolveVisitor();
				} else {
					resolver = null;
				}
				ModuleNodeMapper.getInstance().store(perWorkingCopyInfo, module, resolver);
			}
		}
	}

	public static boolean isEmpty() {
		return INSTANCE.infoToModuleMap.isEmpty();
	}

	// GRECLIPSE-804 check to see that the stored nodes are correct
	// provide info to stdout if not and purge any stale elements
	synchronized void sweepAndPurgeModuleNodes() {
		if (System.getProperty("groovy.eclipse.model.purge") == null) {
			return;
		}

		System.out.println("ModuleNodeMap.size(): " + infoToModuleMap.size());
		List<PerWorkingCopyInfo> toPurge = new ArrayList<PerWorkingCopyInfo>();
		for (PerWorkingCopyInfo info : infoToModuleMap.keySet()) {
			int useCount = ((Integer) ReflectionUtils.getPrivateField(PerWorkingCopyInfo.class, "useCount", info)).intValue();
			if (useCount <= 0) {
				String message = "Bad module node map entry: " + info.getWorkingCopy().getElementName();
				System.out.println(message);
				Util.log(new RuntimeException(message), message);
				toPurge.add(info);
			} else if (useCount > 1) {
				System.out.println(info.getWorkingCopy().getElementName() + " : useCount : " + useCount);
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