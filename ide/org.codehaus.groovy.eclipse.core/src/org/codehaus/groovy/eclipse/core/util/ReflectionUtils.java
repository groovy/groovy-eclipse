 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 *          common functionality for accessing private fields and methods
 *
 * @deprecated use {@link org.eclipse.jdt.groovy.core.util.ReflectionUtils}
 *             instead
 */
@Deprecated
public class ReflectionUtils {

    private static Map<String, Field> fieldMap = new HashMap<String, Field>();

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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
