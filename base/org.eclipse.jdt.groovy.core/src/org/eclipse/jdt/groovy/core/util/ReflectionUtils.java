/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.groovy.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.Activator;

/**
 * Common functionality for accessing private fields and methods.
 */
public class ReflectionUtils {

    private ReflectionUtils() {}

    public static Class<?>[] getAllInterfaces(Class<?> clazz) {
        @SuppressWarnings("rawtypes")
        Set<Class> interfaces = new LinkedHashSet<Class>();

        do {
            Collections.addAll(interfaces, clazz.getInterfaces());
        }
        while ((clazz = clazz.getSuperclass()) != null);

        return interfaces.toArray(NO_TYPES);
    }

    public static <T> Constructor<T> getConstructor(Class<T> instanceType, Class<?>... parameterTypes) {
        try {
            Constructor<T> ctor = instanceType.getDeclaredConstructor(parameterTypes);
            ctor.setAccessible(true);
            return ctor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeConstructor(Constructor<T> ctor, Object... args) {
        try {
            return ctor.newInstance(args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static <T> T invokeConstructor(Class<T> instanceType, Class<?>[] parameterTypes, Object[] args) {
        try {
            return invokeConstructor(getConstructor(instanceType, parameterTypes), args);
        } catch (RuntimeException e) {
            log("Error executing private constructor for '" + instanceType.getName() + "' on class " + instanceType, e);
            return null;
        }
    }

    public static Object getPrivateField(Class<?> clazz, String fieldName, Object target) {
        String key = clazz.getCanonicalName() + fieldName;
        Field field = FIELDS.get(key);
        try {
            if (field == null) {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                FIELDS.put(key, field);
            }
            return field.get(target);
        } catch (Exception e) {
            log("Error getting private field '" + fieldName + "' on class " + clazz, e);
        }
        return null;
    }

    public static void setPrivateField(Class<?> clazz, String fieldName, Object target, Object newValue) {
        String key = clazz.getCanonicalName() + fieldName;
        Field field = FIELDS.get(key);
        try {
            if (field == null) {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                FIELDS.put(key, field);
            }
            field.set(target, newValue);
        } catch (Exception e) {
            log("Error setting private field '" + fieldName + "' on class " + clazz, e);
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
            log("Error executing private method '" + methodName + "' on class " + clazz, e);
            return null;
        }
    }

    public static <T> Object throwableExecutePrivateMethod(Class<? extends T> clazz, String methodName, Class<?>[] types, T target, Object[] args) throws Exception {
        // forget caching for now...
        Method method = clazz.getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    public static <T> Object throwableGetPrivateField(Class<? extends T> clazz, String fieldName, T target) throws Exception {
        String key = clazz.getCanonicalName() + fieldName;
        Field field = FIELDS.get(key);
        if (field == null) {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            FIELDS.put(key, field);
        }
        return field.get(target);
    }

    private static void log(String message, Throwable throwable) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, throwable));
    }

    private static final Object[] NO_ARGS = new Object[0];
    private static final Class<?>[] NO_TYPES = new Class[0];
    private static final Map<String, Field> FIELDS = new HashMap<String, Field>();
}
