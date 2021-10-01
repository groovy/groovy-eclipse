/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.eval.target;

import java.util.*;

/**
 * A code snippet class loader is a class loader that loads code snippet classes and global
 * variable classes.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CodeSnippetClassLoader extends ClassLoader {
	/**
	 * Whether the code snippet support classes should be given by the IDE
	 * or should be found on disk.
	 */
	static boolean DEVELOPMENT_MODE = false;

	Hashtable loadedClasses = new Hashtable();
/**
 * Asks the class loader that loaded this class to load the given class.
 * @throws ClassNotFoundException if it could not be loaded.
 */
private Class delegateLoadClass(String name) throws ClassNotFoundException {
	ClassLoader myLoader = getClass().getClassLoader();
	if (myLoader == null) {
		return Class.forName(name);
	}
	return myLoader.loadClass(name);
}
/**
 * Loads the given class. If the class is known to this runner, returns it.
 * If only  the class definition is known to this runner, makes it a class and returns it.
 * Otherwise delegates to the real class loader.
 */
@Override
protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
	if (DEVELOPMENT_MODE) {
		try {
			return delegateLoadClass(name);
		} catch (ClassNotFoundException e) {
			Class clazz = makeClass(name, resolve);
			if (clazz == null) {
				throw e;
			}
			return clazz;
		}
	}
	Class clazz = makeClass(name, resolve);
	if (clazz == null) {
		return delegateLoadClass(name);
	}
	return clazz;
}
/**
 * Loads the given class either from the stored class definition or from the system.
 * Returns the existing class if it has already been loaded.
 * Returns null if no class definition can be found.
 */
Class loadIfNeeded(String className) {
	Class clazz = null;
	if (!supportsHotCodeReplacement()) {
		clazz = findLoadedClass(className);
	}
	if (clazz == null) {
		try {
			clazz = loadClass(className, true);
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	return clazz;
}
/**
 * Makes the class definition known by this code snippet runner a real class and
 * returns it.
 * Returns null if there is no class definition.
 */
private Class makeClass(String name, boolean resolve) {
	Object o = this.loadedClasses.get(name);
	if (o == null) {
		return null;
	}
	if (o instanceof Class) {
		return (Class) o;
	}
	byte[] classDefinition = (byte[]) o;
	Class clazz = defineClass(null, classDefinition, 0, classDefinition.length);
	if (resolve) {
		resolveClass(clazz);
	}
	this.loadedClasses.put(name, clazz);
	return clazz;
}
/**
 * Stores the given class definition for the given class.
 */
void storeClassDefinition(String className, byte[] classDefinition) {
	Object clazz = this.loadedClasses.get(className);
	if (clazz == null || supportsHotCodeReplacement()) {
		this.loadedClasses.put(className, classDefinition);
	}
}
/**
 * Returns whether this class loader supports Hot Code Replacement.
 */
protected boolean supportsHotCodeReplacement() {
	return false;
}
}
