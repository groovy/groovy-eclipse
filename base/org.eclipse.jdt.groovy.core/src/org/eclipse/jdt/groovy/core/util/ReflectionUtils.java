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

package org.eclipse.jdt.groovy.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;

/**
 * @author Andrew Eisenberg
 * @created May 8, 2009
 * 
 *          common functionality for accessing private fields and methods
 */
public class ReflectionUtils {

	private static final Class[] NO_TYPES = new Class[0];
	private static final Object[] NO_ARGS = new Object[0];
	private static Map<String, Field> fieldMap = new HashMap<String, Field>();

	public static <T> Object getPrivateField(Class<T> clazz, String fieldName, Object target) {
		String key = clazz.getCanonicalName() + fieldName;
		Field field = fieldMap.get(key);
		try {
			if (field == null) {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				fieldMap.put(key, field);
			}
			return field.get(target);
		} catch (Exception e) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error getting private field '" + fieldName //$NON-NLS-1$
							+ "' on class " + clazz, e)); //$NON-NLS-1$
		}
		return null;
	}

	public static <T> void setPrivateField(Class<T> clazz, String fieldName, Object target, Object newValue) {
		String key = clazz.getCanonicalName() + fieldName;
		Field field = fieldMap.get(key);
		try {
			if (field == null) {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				fieldMap.put(key, field);
			}
			field.set(target, newValue);
		} catch (Exception e) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error setting private field '" + fieldName //$NON-NLS-1$
							+ "' on class " + clazz, e)); //$NON-NLS-1$
		}
	}

	public static <T> Object executeNoArgPrivateMethod(Class<T> clazz, String methodName, Object target) {
		return executePrivateMethod(clazz, methodName, NO_TYPES, target, NO_ARGS);
	}

	public static <T> Object executePrivateMethod(Class<T> clazz, String methodName, Class<?>[] types, Object target, Object[] args) {
		// forget caching for now...
		try {
			Method method = clazz.getDeclaredMethod(methodName, types);
			method.setAccessible(true);
			return method.invoke(target, args);
		} catch (Exception e) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error executing private method '" + methodName //$NON-NLS-1$
							+ "' on class " + clazz, e)); //$NON-NLS-1$
			return null;
		}
	}

	public static <T> Object throwableExecutePrivateMethod(Class<T> clazz, String methodName, Class<?>[] types, T target,
			Object[] args) throws Exception {
		// forget caching for now...
		Method method = clazz.getDeclaredMethod(methodName, types);
		method.setAccessible(true);
		return method.invoke(target, args);
	}

	public static <T> Object throwableGetPrivateField(Class<T> clazz, String fieldName, T target) throws Exception {
		String key = clazz.getCanonicalName() + fieldName;
		Field field = fieldMap.get(key);
		if (field == null) {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			fieldMap.put(key, field);
		}
		return field.get(target);
	}

	/**
	 * The signature for the {@link LocalVariable} constructor has changed between 3.6 and 3.7. Use this method to generate a
	 * {@link LocalVariable} regardless of which Eclipse version being used.
	 */
	public static LocalVariable createLocalVariable(IJavaElement parent, String varName, int start, String returnTypeSignature) {
		// 3.7 version - two extra trailing parameters:
		// LocalVariable var = new LocalVariable((JavaElement) unit.getType(
		// className).getChildren()[offsetInParent], matchedVarName,
		// declStart, declStart + matchedVarName.length(),
		// declStart, declStart + matchedVarName.length(),
		// Signature.SIG_INT, new Annotation[0],0,false);

		// LocalVariable localVariable = new LocalVariable((JavaElement) unit.getType(
		// className).getChildren()[offsetInParent], matchedVarName,
		// declStart, declStart + matchedVarName.length(),
		// declStart, declStart + matchedVarName.length(),
		// Signature.SIG_INT, new Annotation[0]);

		LocalVariable localVariable;
		try {
			// 3.6 variant
			Constructor<LocalVariable> cons = LocalVariable.class.getConstructor(JavaElement.class, String.class, int.class,
					int.class, int.class, int.class, String.class, Annotation[].class);
			localVariable = cons.newInstance(parent, varName, start, start + varName.length() - 1, start, start + varName.length()
					- 1, returnTypeSignature, new Annotation[0]);
			return localVariable;
		} catch (Exception e) {
			// 3.7 variant
			try {
				Constructor<LocalVariable> cons = LocalVariable.class.getConstructor(JavaElement.class, String.class, int.class,
						int.class, int.class, int.class, String.class, Annotation[].class, int.class, boolean.class);
				localVariable = cons.newInstance(parent, varName, start, start + varName.length() - 1, start,
						start + varName.length() - 1, returnTypeSignature, new Annotation[0], 0, false);
				return localVariable;
			} catch (Exception e1) {
				Activator.getDefault().getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error creating local variable'" + varName //$NON-NLS-1$
								+ "' in element " + parent.getHandleIdentifier(), e)); //$NON-NLS-1$
				return null;
			}
		}
	}

	/**
	 * Executes a constructor reflectively
	 */
	public static <T> T executePrivateConstructor(Class<T> clazz, Class<? extends Object>[] parameterTypes, Object[] args) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor.newInstance(args);
		} catch (Exception e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Error executing private constructor for '" + clazz.getName() //$NON-NLS-1$
									+ "' on class " + clazz, e)); //$NON-NLS-1$
			return null;
		}
	}
}
