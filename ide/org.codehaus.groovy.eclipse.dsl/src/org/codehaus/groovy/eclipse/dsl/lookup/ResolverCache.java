/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.lookup;

import java.util.Map;
import java.util.WeakHashMap;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * A wrapper around the JDT resolver that caches resolve requests.
 * Handles classes with type parameters.
 */
public class ResolverCache {

    private final Map<String, ClassNode> nameTypeCache;
    private final JDTResolver resolver;

    public ResolverCache(JDTResolver resolver, ModuleNode thisModule) {
        this.nameTypeCache = new WeakHashMap<String, ClassNode>();
        this.resolver = resolver;
    }

    /**
     * Resolves a class name to a ClassNode.  Using the fully qualified type name,
     * or the array type signature for arrays.  Can specify type parameters, also
     * using fully qualified names.
     */
    public ClassNode resolve(String qName) {
        if (qName == null || qName.length() == 0) {
            return ClassHelper.DYNAMIC_TYPE;
        }
        qName = qName.trim();
        if (qName.equals("java.lang.Void") || qName.equals("void")) {
            return VariableScope.VOID_CLASS_NODE;
        }
        ClassNode clazz = nameTypeCache.get(qName);
        int arrayCnt = 0;
        if (clazz == null && resolver != null) {
            int typeParamEnd = qName.lastIndexOf('>');
            int arrayStart = qName.indexOf('[', typeParamEnd);
            String componentName;
            if (arrayStart > 0) {
                componentName = qName.substring(0, arrayStart);
                arrayCnt = calculateArrayCount(qName, arrayStart);
            } else {
                componentName = qName;
            }

            String erasureName = componentName;
            int typeParamStart = -1;
            if (typeParamEnd > 0) {
                typeParamStart = componentName.indexOf('<');
                if (typeParamStart > 0) {
                    erasureName = componentName.substring(0, typeParamStart);
                }
            }
            clazz = resolver.resolve(erasureName);
            if (clazz == null) {
                clazz = VariableScope.OBJECT_CLASS_NODE;
            }
            nameTypeCache.put(erasureName, clazz);

            // now recur down through the type parameters
            if (typeParamStart > 0) {
                // only need to clone if generics are involved
                clazz = VariableScope.clone(clazz);

                String[] typeParameterNames = componentName.substring(typeParamStart + 1, componentName.length() - 1).split(",");
                ClassNode[] typeParameters = new ClassNode[typeParameterNames.length];
                for (int i = 0; i < typeParameterNames.length; i += 1) {
                    typeParameters[i] = resolve(typeParameterNames[i]);
                }
                clazz = VariableScope.clone(clazz);
                GenericsType[] genericsTypes = clazz.getGenericsTypes();
                if (genericsTypes != null) {
                    // need to be careful here...there may be too many or too few type parameters
                    for (int i = 0; i < genericsTypes.length && i < typeParameters.length; i += 1) {
                        genericsTypes[i].setResolved(true);
                        genericsTypes[i].setWildcard(false);
                        genericsTypes[i].setPlaceholder(false);
                        genericsTypes[i].setLowerBound(null);
                        genericsTypes[i].setUpperBounds(null);
                        genericsTypes[i].setType(typeParameters[i]);
                        genericsTypes[i].setName(typeParameters[i].getName());
                    }
                    nameTypeCache.put(componentName, clazz);
                }
            }
            while (arrayCnt > 0) {
                clazz = new ClassNode(clazz);
                componentName += "[]";
                nameTypeCache.put(componentName, clazz);
                arrayCnt -= 1;
            }
        }

        return clazz;
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
