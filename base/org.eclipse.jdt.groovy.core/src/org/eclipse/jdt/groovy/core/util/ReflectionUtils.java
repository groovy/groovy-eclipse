/*
 * Copyright 2009-2020 the original author or authors.
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.Activator;

/**
 * Common functionality for accessing private fields and methods.
 */
public class ReflectionUtils {

    private ReflectionUtils() {}

    public static Class<?>[] getAllInterfaces(final Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();

        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            Collections.addAll(interfaces, c.getInterfaces());
        }

        return interfaces.toArray(NO_TYPES);
    }

    public static <T> Constructor<T> getConstructor(final Class<T> instanceType, final Class<?>... parameterTypes) {
        try {
            Constructor<T> ctor = instanceType.getDeclaredConstructor(parameterTypes);
            if (!ctor.isAccessible()) ctor.setAccessible(true);
            return ctor;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeConstructor(final Constructor<T> ctor, final Object... args) {
        try {
            return ctor.newInstance(args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeConstructor(final Class<T> instanceType, final Class<?>[] parameterTypes, final Object[] args) {
        try {
            return invokeConstructor(getConstructor(instanceType, parameterTypes), args);
        } catch (RuntimeException e) {
            log("Error executing private constructor for '" + instanceType.getName() + "' on class " + instanceType, e);
            return null;
        }
    }

    public static <R, T> R getPrivateField(final Class<? extends T> clazz, final String fieldName, final T target) {
        try {
            return throwableGetPrivateField(clazz, fieldName, target);
        } catch (Exception e) {
            log("Error getting private field '" + fieldName + "' on class " + clazz, e);
            return null;
        }
    }

    public static <T> void setPrivateField(final Class<? extends T> clazz, final String fieldName, final T target, final Object value) {
        try {
            throwableSetPrivateField(clazz, fieldName, target, value);
        } catch (Exception e) {
            log("Error setting private field '" + fieldName + "' on class " + clazz, e);
        }
    }

    public static <R, T> R executePrivateMethod(final Class<? extends T> clazz, final String methodName, final T target) {
        return executePrivateMethod(clazz, methodName, NO_TYPES, target, NO_ARGS);
    }

    public static <R, T> R executePrivateMethod(Class<? extends T> clazz, String methodName, Class<?>[] paramTypes, T target, Object[] args) {
        try {
            Method method = getDeclaredMethod(clazz, methodName, paramTypes);

            @SuppressWarnings("unchecked")
            R result = (R) method.invoke(target, args);
            return result;
        } catch (Exception e) {
            log("Error executing private method '" + methodName + "' on class " + clazz, e);
            return null;
        }
    }

    public static <R, T> R throwableExecutePrivateMethod(final Class<? extends T> clazz, final String methodName, final Class<?>[] paramTypes, final T target, final Object[] args) throws Exception {
        Method method = getDeclaredMethod(clazz, methodName, paramTypes);

        @SuppressWarnings("unchecked")
        R result = (R) method.invoke(target, args);
        return result;
    }

    public static <R, T> R throwableGetPrivateField(final Class<? extends T> clazz, final String fieldName, final T target) throws Exception {
        Field field = getDeclaredField(clazz, fieldName);

        @SuppressWarnings("unchecked")
        R result = (R) field.get(target);
        return result;
    }

    public static <T> void throwableSetPrivateField(final Class<? extends T> clazz, final String fieldName, final T target, final Object value) throws Exception {
        Field field = getDeclaredField(clazz, fieldName);

        field.set(target, value);
    }

    //--------------------------------------------------------------------------

    private static Field getDeclaredField(final Class<?> clazz, final String field) throws Exception {
        try {
            return FIELDS.computeIfAbsent(clazz.getCanonicalName() + field, k -> {
                try {
                    Field f = clazz.getDeclaredField(field);
                    if (!f.isAccessible()) f.setAccessible(true);
                    return f;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getClass() == RuntimeException.class && e.getCause() != null) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private static Method getDeclaredMethod(final Class<?> clazz, final String method, final Class<?>... paramTypes) throws Exception {
        Method m = clazz.getDeclaredMethod(method, paramTypes);
        if (!m.isAccessible()) m.setAccessible(true);
        return m;
    }

    private static void log(final String message, final Throwable throwable) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, throwable));
    }

    private static final Object[] NO_ARGS = new Object[0];
    private static final Class<?>[] NO_TYPES = new Class[0];
    private static final Map<String, Field> FIELDS = new ConcurrentHashMap<>();
}
