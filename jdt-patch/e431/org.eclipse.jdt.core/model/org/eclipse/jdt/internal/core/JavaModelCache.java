/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Terry Parker <tparker@google.com> (Google Inc.)  https://bugs.eclipse.org/365499
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.compiler.env.IBinaryInfo;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.core.util.LRUCache;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * The cache of java elements to their respective info.
 */
public class JavaModelCache {
	public static boolean VERBOSE = false;
	public static boolean DEBUG_CACHE_INSERTIONS = false;

	public static final int DEFAULT_ROOT_SIZE = 2_000;
	public static final int DEFAULT_PKG_SIZE = 20_000;
	public static final int DEFAULT_OPENABLE_SIZE = 200_000;

	public static final String RATIO_PROPERTY = "org.eclipse.jdt.core.javamodelcache.ratio"; //$NON-NLS-1$
	public static final String JAR_TYPE_RATIO_PROPERTY = "org.eclipse.jdt.core.javamodelcache.jartyperatio"; //$NON-NLS-1$

	public static final IBinaryInfo NON_EXISTING_JAR_TYPE_INFO = new IBinaryInfo() {/*empty marker instance only*/};

	/*
	 * The memory ratio that should be applied to the above constants.
	 */
	protected double memoryRatio = -1;

	/**
	 * Active Java Model Info
	 */
	protected JavaElementInfo modelInfo;

	/**
	 * Cache of open projects.
	 */
	protected HashMap<IJavaProject, JavaElementInfo> projectCache;

	/**
	 * Cache of open package fragment roots.
	 */
	protected ElementCache<IPackageFragmentRoot> rootCache;

	/**
	 * Cache of open package fragments
	 */
	protected ElementCache<IPackageFragment> pkgCache;

	/**
	 * Cache of open compilation unit and class files
	 */
	protected ElementCache<ITypeRoot> openableCache;

	/**
	 * Cache of open children of openable Java Model Java elements
	 */
	protected Map<IJavaElement, IElementInfo> childrenCache;

	/**
	 * Cache of open binary type (inside a jar) that have a non-open parent
	 * Values are either instance of IBinaryType or Object (see {@link #NON_EXISTING_JAR_TYPE_INFO})
	 */
	protected LRUCache<IJavaElement, IElementInfo> jarTypeCache;

public JavaModelCache() {
	double openableRatio = getOpenableRatio();
	int rootCacheSize = sizeLimit(DEFAULT_ROOT_SIZE * openableRatio);
	int packageCacheeSize = sizeLimit(DEFAULT_PKG_SIZE * openableRatio);
	int openableCacheSize = sizeLimit(DEFAULT_OPENABLE_SIZE * openableRatio);

	// NB: Don't use a LRUCache for projects as they are
	// constantly reopened (e.g. during delta processing)
	this.projectCache = new HashMap<>();  // HashMap size adjusts automatically
	if (VERBOSE) {
		this.rootCache = new VerboseElementCache<>(rootCacheSize, "Root cache"); //$NON-NLS-1$
		this.pkgCache = new VerboseElementCache<>(packageCacheeSize, "Package cache"); //$NON-NLS-1$
		this.openableCache = new VerboseElementCache<>(openableCacheSize, "Openable cache"); //$NON-NLS-1$
	} else {
		this.rootCache = new ElementCache<>(rootCacheSize);
		this.pkgCache = new ElementCache<>(packageCacheeSize);
		this.openableCache = new ElementCache<>(openableCacheSize);
	}
	this.childrenCache = new HashMap<>(); // HashMap size adjusts automatically
	resetJarTypeCache();
}

private int sizeLimit(double d) {
	return (int) Double.min(Integer.MAX_VALUE/2,d);
}

private double getOpenableRatio() {
	return getRatioForProperty(RATIO_PROPERTY);
}

private double getJarTypeRatio() {
	return getRatioForProperty(JAR_TYPE_RATIO_PROPERTY);
}

private double getRatioForProperty(String propertyName) {
	String property = System.getProperty(propertyName);
	if (property != null) {
		try {
			return Double.parseDouble(property);
		} catch (NumberFormatException e) {
			// ignore
			Util.log(e, "Could not parse value for " + propertyName + ": " + property); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	return 1.0;
}

/**
 *  Returns the info for the element.
 */
public IElementInfo getInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
			return this.projectCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.get((IPackageFragmentRoot) element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.get((IPackageFragment) element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.get((ITypeRoot) element);
		case IJavaElement.TYPE:
			IElementInfo result = this.jarTypeCache.get(element);
			if (result != null)
				return result;
			else
				return this.childrenCache.get(element);
		default:
			return this.childrenCache.get(element);
	}
}

/*
 *  Returns the existing element that is equal to the given element if present in the cache.
 *  Returns the given element otherwise.
 */
public IJavaElement getExistingElement(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return element;
		case IJavaElement.JAVA_PROJECT:
			return element; // projectCache is a Hashtable and Hashtables don't support getKey(...)
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.getKey((IPackageFragmentRoot) element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.getKey((IPackageFragment) element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.getKey((ITypeRoot) element);
		case IJavaElement.TYPE:
			return element; // jarTypeCache or childrenCache are Hashtables and Hashtables don't support getKey(...)
		default:
			return element; // childrenCache is a Hashtable and Hashtables don't support getKey(...)
	}
}

/**
 *  Returns the info for this element without
 *  disturbing the cache ordering.
 */
protected IElementInfo peekAtInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
			return this.projectCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.peek((IPackageFragmentRoot) element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.peek((IPackageFragment) element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.peek((ITypeRoot) element);
		case IJavaElement.TYPE:
			IElementInfo result = this.jarTypeCache.peek(element);
			if (result != null)
				return result;
			else
				return this.childrenCache.get(element);
		default:
			return this.childrenCache.get(element);
	}
}

/**
 * Remember the info for the element.
 */
protected void putInfo(IJavaElement element, IElementInfo info) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = (JavaElementInfo) info;
			break;
		case IJavaElement.JAVA_PROJECT:
			this.projectCache.put((IJavaProject) element, (JavaElementInfo) info);
			this.rootCache.ensureSpaceLimit((JavaElementInfo) info, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.rootCache.put((IPackageFragmentRoot) element, (JavaElementInfo) info);
			this.pkgCache.ensureSpaceLimit((JavaElementInfo) info, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.put((IPackageFragment) element, (JavaElementInfo) info);
			this.openableCache.ensureSpaceLimit((JavaElementInfo) info, element);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.put((ITypeRoot) element, (JavaElementInfo) info);
			break;
		default:
			this.childrenCache.put(element, info);
			return; // don't trace children -- too many
	}
	if (DEBUG_CACHE_INSERTIONS) {
		JavaModelManager.trace(Thread.currentThread() + " cache putInfo " + getCacheType(element) + " " + element.getElementName() + ", " + info.getClass().getSimpleName());  //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}

static String getCacheType(IJavaElement element) {
	String elementType;
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			elementType = "JAVA_MODEL"; //$NON-NLS-1$
			break;
		case IJavaElement.JAVA_PROJECT:
			elementType = "JAVA_PROJECT"; //$NON-NLS-1$
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			elementType = "PACKAGE_FRAGMENT_ROOT"; //$NON-NLS-1$
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			elementType = "PACKAGE_FRAGMENT"; //$NON-NLS-1$
			break;
		case IJavaElement.CLASS_FILE:
			elementType = "CLASS_FILE"; //$NON-NLS-1$
			break;
		case IJavaElement.COMPILATION_UNIT:
			elementType = "COMPILATION_UNIT"; //$NON-NLS-1$
			break;
		default:
			elementType = "CHILD"; //$NON-NLS-1$
	}
	return elementType + ", " + element.getClass().getSimpleName(); //$NON-NLS-1$
}

/**
 * Removes the info of the element from the cache.
 */
protected void removeInfo(JavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = null;
			break;
		case IJavaElement.JAVA_PROJECT:
			this.projectCache.remove((IJavaProject)element);
			this.rootCache.resetSpaceLimit(element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.rootCache.remove((IPackageFragmentRoot) element);
			this.pkgCache.resetSpaceLimit(element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.remove((IPackageFragment) element);
			this.openableCache.resetSpaceLimit(element);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.remove((ITypeRoot) element);
			break;
		default:
			this.childrenCache.remove(element);
			return; // don't trace children -- too many
	}
	if (DEBUG_CACHE_INSERTIONS) {
		JavaModelManager.trace(Thread.currentThread() + " cache removeInfo " + getCacheType(element) + " " + element.getElementName());  //$NON-NLS-1$//$NON-NLS-2$
	}
}
protected void resetJarTypeCache() {
	int jarTypeCacheSize = sizeLimit(DEFAULT_OPENABLE_SIZE * getJarTypeRatio());
	this.jarTypeCache = new LRUCache<>(jarTypeCacheSize);
}
protected void removeFromJarTypeCache(BinaryType type) {
	this.jarTypeCache.flush(type);
}
@Override
public String toString() {
	return toStringFillingRation(""); //$NON-NLS-1$
}
public String toStringFillingRation(String prefix) {
	StringBuilder buffer = new StringBuilder();
	buffer.append(prefix);
	buffer.append("Project cache: "); //$NON-NLS-1$
	buffer.append(this.projectCache.size());
	buffer.append(" projects\n"); //$NON-NLS-1$
	buffer.append(prefix);
	buffer.append(this.rootCache.toStringFillingRation("Root cache")); //$NON-NLS-1$
	buffer.append('\n');
	buffer.append(prefix);
	buffer.append(this.pkgCache.toStringFillingRation("Package cache")); //$NON-NLS-1$
	buffer.append('\n');
	buffer.append(prefix);
	buffer.append(this.openableCache.toStringFillingRation("Openable cache")); //$NON-NLS-1$
	buffer.append('\n');
	buffer.append(prefix);
	buffer.append(this.jarTypeCache.toStringFillingRation("Jar type cache")); //$NON-NLS-1$
	buffer.append('\n');
	return buffer.toString();
}
}
