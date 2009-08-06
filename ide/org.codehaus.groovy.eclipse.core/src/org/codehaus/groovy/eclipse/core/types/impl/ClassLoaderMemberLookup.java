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
package org.codehaus.groovy.eclipse.core.types.impl;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.util.List;

import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.JavaField;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Modifiers;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;

/**
 * Looks up members in classes accessible via a ClassLoader. All classes appear
 * as Java types, no type inferrence is possible. Projects with Groovy source
 * will be served by other lookups.
 * 
 * @author empovazan
 */
public class ClassLoaderMemberLookup extends AbstractMemberLookup implements
        IGroovyProjectAware {
    private final ClassLoader classLoader;

    private GroovyProjectFacade project;

    public ClassLoaderMemberLookup(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected List<Field> collectAllFields(final String type) {
        return collectAllFields(newList(new Field[0]), resolveClass(type));
    }

    private List<Field> collectAllFields(List<Field> result, Class cls) {
        if (cls == null) {
            return result;
        }

        // Collect super class methods first.
        Class superClass = cls.getSuperclass();
        if (superClass != null) {
            collectAllFields(result, superClass);
        }

        // Collect this classes methods last.
        java.lang.reflect.Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            result.add(TypeUtil.newField(fields[i]));
        }

        if (cls.isArray()) {
            int modifiers = Modifiers.ACC_PUBLIC;
            ClassType declaringClass = TypeUtil.newClassType(Object.class);
            String signature = "int";
            result.add(new JavaField(signature, modifiers, "length",
                    declaringClass));
        }
        return result;
    }

    @Override
    protected List<Method> collectAllMethods(String type) {
        if (project == null || project.getClassNodeForName(type) == null)
            return collectAllMethods(newList(new Method[0]), resolveClass(type));
        return newList(new Method[0]);
    }

    private List<Method> collectAllMethods(List<Method> result, Class cls) {
        if (cls == null) {
            return result;
        }

        // Collect super class methods first.
        Class superClass = cls.getSuperclass();
        if (superClass != null) {
            collectAllMethods(result, superClass);
        }

        // Collect interface methods next.
        Class[] interfaces = cls.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            collectAllMethods(result, interfaces[i]);
        }

        // Collect this classes methods last.
        java.lang.reflect.Method[] methods = cls.getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            result.add(TypeUtil.newMethod(methods[i]));
        }
        return result;
    }

    private Class resolveClass(String type) {
        // TODO: emp - what about primitives?
        try {
            return Class.forName(type, true, classLoader);
        } catch (ClassNotFoundException e) {
            // Must be a dynamic/inaccessible type.
            return null;
        } catch (LinkageError e) {
            return null;
        }
    }

    public void setGroovyProject(GroovyProjectFacade project) {
        this.project = project;
    }
}