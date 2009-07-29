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

package org.codehaus.groovy.eclipse.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;

/**
 * @author Andrew Eisenberg
 * @created May 8, 2009
 *
 * common functionality for accessing private fields and methods
 * 
 * 
 */
public class ReflectionUtils {
    
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
            GroovyCore.logException("Error during reflective call.", e);
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
            GroovyCore.logException("Error during reflective call.", e);
        }
    }
    
    public static <T> Object executePrivateMethod(Class<T> clazz, String methodName, Class<?>[] types, Object target, Object[] args) {
        // forget caching for now...
        try {
            Method method = clazz.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            GroovyCore.logException("Error during reflective call.", e);
        }
        return null;
    }
    
}
