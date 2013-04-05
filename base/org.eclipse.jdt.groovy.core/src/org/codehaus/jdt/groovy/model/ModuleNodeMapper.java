/*******************************************************************************
 * Copyright (c) 2009, 2011 SpringSource and others.
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
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
 *          One module node is stored per working copy of a unit
 */
public class ModuleNodeMapper {

	public static class ModuleNodeInfo {
		public ModuleNodeInfo(ModuleNode module, JDTResolver resolver) {
			this.module = module;
			this.resolver = resolver;
		}

		public final ModuleNode module;
		public final JDTResolver resolver;
	}

	private static final ModuleNodeMapper INSTANCE = new ModuleNodeMapper();

	static ModuleNodeMapper getInstance() {
		return INSTANCE;
	}

	private final ReentrantLock lock = new ReentrantLock(true);

	private final Map<PerWorkingCopyInfo, ModuleNodeInfo> infoToModuleMap = new HashMap<PerWorkingCopyInfo, ModuleNodeInfo>();

	void store(PerWorkingCopyInfo info, ModuleNode module, JDTResolver resolver) {
		lock.lock();
		try {
			sweepAndPurgeModuleNodes();
			infoToModuleMap.put(info, new ModuleNodeInfo(module, shouldStoreResovler() ? resolver : null));
		} finally {
			lock.unlock();
		}
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

	public static boolean shouldStoreResovler() {
		return DSL_BUNDLE_INSTALLED;
	}

	ModuleNode getModule(PerWorkingCopyInfo info) {
		lock.lock();
		try {
			ModuleNodeInfo moduleNodeInfo = get(info);
			return moduleNodeInfo != null ? moduleNodeInfo.module : null;
		} finally {
			lock.unlock();
		}
	}

	ModuleNodeInfo get(PerWorkingCopyInfo info) {
		lock.lock();
		try {
			sweepAndPurgeModuleNodes();
			return infoToModuleMap.get(info);
		} finally {
			lock.unlock();
		}
	}

	JDTResolver getResolver(PerWorkingCopyInfo info) {
		lock.lock();
		try {
			ModuleNodeInfo moduleNodeInfo = get(info);
			return moduleNodeInfo != null ? moduleNodeInfo.resolver : null;
		} finally {
			lock.unlock();
		}
	}

	ModuleNode remove(PerWorkingCopyInfo info) {
		lock.lock();
		try {
			sweepAndPurgeModuleNodes();
			ModuleNodeInfo removed = infoToModuleMap.remove(info);
			return removed != null ? removed.module : null;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Cache the module node if this is a working copy
	 * 
	 * @param perWorkingCopyInfo
	 * @param compilationUnitDeclaration
	 */
	protected void maybeCacheModuleNode(final JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo,
			final GroovyCompilationUnitDeclaration compilationUnitDeclaration) {

		if (lock.tryLock()) {
			try {
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
						store(perWorkingCopyInfo, module, resolver);
					}
				}
			} finally {
				lock.unlock();
			}
		} else {
			// lock grabbed by someone else. rerun this operation later
			new Job("Cache module node") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					maybeCacheModuleNode(perWorkingCopyInfo, compilationUnitDeclaration);
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	public static boolean isEmpty() {
		return INSTANCE.infoToModuleMap.isEmpty();
	}

	// GRECLIPSE-804 check to see that the stored nodes are correct
	// provide info to stdout if not and purge any stale elements
	void sweepAndPurgeModuleNodes() {
		lock.lock();
		try {
			if (System.getProperty("groovy.eclipse.model.purge") == null) {
				return;
			}

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
				for (PerWorkingCopyInfo info : toPurge) {
					infoToModuleMap.remove(info);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}
}