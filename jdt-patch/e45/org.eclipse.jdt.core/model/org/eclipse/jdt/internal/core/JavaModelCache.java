/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jdt.internal.core.util.LRUCache;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * The cache of java elements to their respective info.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaModelCache {
	public static boolean VERBOSE = false;

	public static final int DEFAULT_PROJECT_SIZE = 5;  // average 25552 bytes per project.
	public static final int DEFAULT_ROOT_SIZE = 50; // average 2590 bytes per root -> maximum size : 25900*BASE_VALUE bytes
	public static final int DEFAULT_PKG_SIZE = 500; // average 1782 bytes per pkg -> maximum size : 178200*BASE_VALUE bytes
	public static final int DEFAULT_OPENABLE_SIZE = 250; // average 6629 bytes per openable (includes children) -> maximum size : 662900*BASE_VALUE bytes
	public static final int DEFAULT_CHILDREN_SIZE = 250*20; // average 20 children per openable
	public static final String RATIO_PROPERTY = "org.eclipse.jdt.core.javamodelcache.ratio"; //$NON-NLS-1$
	public static final String JAR_TYPE_RATIO_PROPERTY = "org.eclipse.jdt.core.javamodelcache.jartyperatio"; //$NON-NLS-1$
	
	public static final Object NON_EXISTING_JAR_TYPE_INFO = new Object();

	/*
	 * The memory ratio that should be applied to the above constants.
	 */
	protected double memoryRatio = -1;

	/**
	 * Active Java Model Info
	 */
	protected Object modelInfo;

	/**
	 * Cache of open projects.
	 */
	protected HashMap projectCache;

	/**
	 * Cache of open package fragment roots.
	 */
	protected ElementCache rootCache;

	/**
	 * Cache of open package fragments
	 */
	protected ElementCache pkgCache;

	/**
	 * Cache of open compilation unit and class files
	 */
	protected ElementCache openableCache;

	/**
	 * Cache of open children of openable Java Model Java elements
	 */
	protected Map childrenCache;

	/*
	 * Cache of open binary type (inside a jar) that have a non-open parent
	 */
	protected LRUCache jarTypeCache;

public JavaModelCache() {
	// set the size of the caches as a function of the maximum amount of memory available
	double ratio = getMemoryRatio();
	// adjust the size of the openable cache using the RATIO_PROPERTY property
	double openableRatio = getOpenableRatio();
	this.projectCache = new HashMap(DEFAULT_PROJECT_SIZE); // NB: Don't use a LRUCache for projects as they are constantly reopened (e.g. during delta processing)
	if (VERBOSE) {
		this.rootCache = new VerboseElementCache((int) (DEFAULT_ROOT_SIZE * ratio), "Root cache"); //$NON-NLS-1$
		this.pkgCache = new VerboseElementCache((int) (DEFAULT_PKG_SIZE * ratio), "Package cache"); //$NON-NLS-1$
		this.openableCache = new VerboseElementCache((int) (DEFAULT_OPENABLE_SIZE * ratio * openableRatio), "Openable cache"); //$NON-NLS-1$
	} else {
		this.rootCache = new ElementCache((int) (DEFAULT_ROOT_SIZE * ratio));
		this.pkgCache = new ElementCache((int) (DEFAULT_PKG_SIZE * ratio));
		this.openableCache = new ElementCache((int) (DEFAULT_OPENABLE_SIZE * ratio * openableRatio));
	}
	this.childrenCache = new HashMap((int) (DEFAULT_CHILDREN_SIZE * ratio * openableRatio));
	resetJarTypeCache();
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
public Object getInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
			return this.projectCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.get(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.get(element);
		case IJavaElement.TYPE:
			Object result = this.jarTypeCache.get(element);
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
			return (IJavaElement) this.rootCache.getKey(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return (IJavaElement) this.pkgCache.getKey(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return (IJavaElement) this.openableCache.getKey(element);
		case IJavaElement.TYPE:
			return element; // jarTypeCache or childrenCache are Hashtables and Hashtables don't support getKey(...)
		default:
			return element; // childrenCache is a Hashtable and Hashtables don't support getKey(...)
	}
}

protected double getMemoryRatio() {
	if ((int) this.memoryRatio == -1) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		// if max memory is infinite, set the ratio to 4d which corresponds to the 256MB that Eclipse defaults to
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=111299)
		this.memoryRatio = maxMemory == Long.MAX_VALUE ? 4d : ((double) maxMemory) / (64 * 0x100000); // 64MB is the base memory for most JVM
	}
	return this.memoryRatio;
}

/**
 *  Returns the info for this element without
 *  disturbing the cache ordering.
 */
protected Object peekAtInfo(IJavaElement element) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			return this.modelInfo;
		case IJavaElement.JAVA_PROJECT:
			return this.projectCache.get(element);
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			return this.rootCache.peek(element);
		case IJavaElement.PACKAGE_FRAGMENT:
			return this.pkgCache.peek(element);
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			return this.openableCache.peek(element);
		case IJavaElement.TYPE:
			Object result = this.jarTypeCache.peek(element);
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
protected void putInfo(IJavaElement element, Object info) {
	switch (element.getElementType()) {
		case IJavaElement.JAVA_MODEL:
			this.modelInfo = info;
			break;
		case IJavaElement.JAVA_PROJECT:
			this.projectCache.put(element, info);
			this.rootCache.ensureSpaceLimit(info, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.rootCache.put(element, info);
			this.pkgCache.ensureSpaceLimit(info, element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.put(element, info);
			this.openableCache.ensureSpaceLimit(info, element);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.put(element, info);
			break;
		default:
			this.childrenCache.put(element, info);
	}
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
			this.projectCache.remove(element);
			this.rootCache.resetSpaceLimit((int) (DEFAULT_ROOT_SIZE * getMemoryRatio()), element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			this.rootCache.remove(element);
			this.pkgCache.resetSpaceLimit((int) (DEFAULT_PKG_SIZE * getMemoryRatio()), element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			this.pkgCache.remove(element);
			this.openableCache.resetSpaceLimit((int) (DEFAULT_OPENABLE_SIZE * getMemoryRatio() * getOpenableRatio()), element);
			break;
		case IJavaElement.COMPILATION_UNIT:
		case IJavaElement.CLASS_FILE:
			this.openableCache.remove(element);
			break;
		default:
			this.childrenCache.remove(element);
	}
}
protected void resetJarTypeCache() {
	this.jarTypeCache = new LRUCache((int) (DEFAULT_OPENABLE_SIZE * getMemoryRatio() * getJarTypeRatio()));
}
protected void removeFromJarTypeCache(BinaryType type) {
	this.jarTypeCache.flush(type);
}
public String toString() {
	return toStringFillingRation(""); //$NON-NLS-1$
}
public String toStringFillingRation(String prefix) {
	StringBuffer buffer = new StringBuffer();
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
