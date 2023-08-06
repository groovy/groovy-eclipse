/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A wrapper around the JDT resolver that caches resolve requests.
 * Handles classes with type parameters.
 */
public class ResolverCache {

    private final Map<String, ClassNode> nameTypeCache;
    private final JDTResolver resolver;
    public final ModuleNode module;

    public ResolverCache(JDTResolver resolver, ModuleNode module) {
        this.nameTypeCache = new WeakHashMap<>();
        this.resolver = resolver;
        this.module = module;
    }

    /**
     * Resolves a class name to a ClassNode.  Using the fully qualified type name,
     * or the array type signature for arrays.  Can specify type parameters, also
     * using fully qualified names.
     */
    public ClassNode resolve(String name) {
        if (name == null || (name = name.trim()).isEmpty()) {
            return ClassHelper.dynamicType();
        }
        if ("void".equals(name) || "java.lang.Void".equals(name)) {
            return VariableScope.VOID_CLASS_NODE;
        }
        ClassNode type = nameTypeCache.get(name);
        if (type == null && resolver != null) {
            int typeParamEnd = name.lastIndexOf('>');
            int arrayStart = name.indexOf('[', typeParamEnd);
            String componentName;
            int arrayCount = 0;
            if (arrayStart > 0) {
                componentName = name.substring(0, arrayStart);
                arrayCount = calculateArrayCount(name, arrayStart);
            } else {
                componentName = name;
            }

            String erasureName = componentName;
            int typeParamStart = -1;
            if (typeParamEnd > 0) {
                typeParamStart = componentName.indexOf('<');
                if (typeParamStart > 0) {
                    erasureName = componentName.substring(0, typeParamStart);
                }
            }
            type = resolver.resolve(erasureName);
            if (type == null) {
                type = VariableScope.OBJECT_CLASS_NODE;
            }
            nameTypeCache.put(erasureName, type);

            // now recur down through the type parameters
            if (typeParamStart > 0 && type.isUsingGenerics()) {
                String[] typeParameterNames = componentName.substring(typeParamStart + 1, componentName.length() - 1).split("\\s*,\\s*");
                ClassNode[] typeParameterTypes = new ClassNode[typeParameterNames.length];
                for (int i = 0; i < typeParameterNames.length; i += 1) {
                    typeParameterTypes[i] = resolve(typeParameterNames[i].replaceFirst("^\\?\\s+(extends|super)\\s+", ""));
                }
                type = VariableScope.clone(type);
                GenericsType[] genericsTypes = type.getGenericsTypes();
                // need to be careful here...there may be too many or too few type parameters
                for (int i = 0; i < genericsTypes.length && i < typeParameterTypes.length; i += 1) {
                    if (typeParameterNames[i].startsWith("?")) {
                        genericsTypes[i] = GenericsUtils.buildWildcardType(typeParameterTypes[i]);
                    } else {
                        genericsTypes[i].setName(typeParameterTypes[i].getName());
                        genericsTypes[i].setType(typeParameterTypes[i]);
                        genericsTypes[i].setPlaceHolder(false);
                        genericsTypes[i].setUpperBounds(null);
                        genericsTypes[i].setLowerBound(null);
                        genericsTypes[i].setWildcard(false);
                    }
                    genericsTypes[i].setResolved(true);
                }
                nameTypeCache.put(componentName, type);
            }

            while (arrayCount > 0) {
                arrayCount -= 1;
                componentName += "[]";
                type = type.makeArray();
                nameTypeCache.put(componentName, type);
            }
        }
        return (name.indexOf('<') < 0 ? GenericsUtils.nonGeneric(type) : type);
    }

    private int calculateArrayCount(String qName, int arrayStart) {
        if (arrayStart < 0) {
            return 0;
        }
        int cnt = 1;
        while ((arrayStart = qName.indexOf('[', arrayStart + 1)) > 0) {
            cnt += 1;
        }
        return cnt;
    }
}
