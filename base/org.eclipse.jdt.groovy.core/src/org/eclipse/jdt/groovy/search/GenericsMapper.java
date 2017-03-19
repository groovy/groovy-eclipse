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
package org.eclipse.jdt.groovy.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Maps type parameters to resolved types.
 */
public class GenericsMapper {

    /**
     * Creates a mapper for a particular resolved type tracing up the type hierarchy until the declaring type is reached.
     * This is the public entry point for this class.
     *
     * @param resolvedType unredirected type that has generic types already parameterized
     * @param declaringType a type that is somewhere in resolvedType's hierarchy used to find the target of the mapping
     */
    public static GenericsMapper gatherGenerics(ClassNode resolvedType, ClassNode declaringType) {
        GenericsMapper mapper = new GenericsMapper();

        ClassNode rcandidate = resolvedType;
        ClassNode ucandidate = resolvedType.redirect();

        LinkedHashSet<ClassNode> rHierarchy = new LinkedHashSet<ClassNode>();
        VariableScope.createTypeHierarchy(rcandidate, rHierarchy, true);
        Iterator<ClassNode> rIter = rHierarchy.iterator();
        LinkedHashSet<ClassNode> uHierarchy = new LinkedHashSet<ClassNode>();
        VariableScope.createTypeHierarchy(ucandidate, uHierarchy, false);
        Iterator<ClassNode> uIter = uHierarchy.iterator();

        // travel up the hierarchy
        while (rIter.hasNext() && uIter.hasNext()) {
            rcandidate = rIter.next();
            ucandidate = uIter.next();

            GenericsType[] rgts = getGenericsTypes(rcandidate);
            GenericsType[] ugts = getGenericsTypes(ucandidate);

            int n = Math.min(rgts.length, ugts.length);
            Map<String, ClassNode> resolved = (n <= 0) ? Collections.EMPTY_MAP : new TreeMap<String, ClassNode>();
            for (int i = 0; i < n; i += 1) {
                // now try to resolve the parameter in the context of the
                // most recently visited type. If it doesn't exist, then
                // default to the resovled type
                resolved.put(ugts[i].getName(), mapper.resolveParameter(rgts[i], 0));
            }
            mapper.allGenerics.add(resolved);

            // don't need to travel up the whole hierarchy; stop at the declaring class
            if (rcandidate.getName().equals(declaringType.getName())) {
                break;
            }
        }

        return mapper;
    }

    public static GenericsMapper gatherGenerics(List<ClassNode> argumentTypes, ClassNode delegateOrThisType, MethodNode methodDeclaration) {
        // GOAL: resolve return type of something like "static <T> Iterator<T> iterator(T[] array)"

        // inspect owner type for generics
        GenericsMapper mapper = gatherGenerics(delegateOrThisType, methodDeclaration.getDeclaringClass());

        // inspect parameters for generics
        GenericsType[] ugts = getGenericsTypes(methodDeclaration);
        if (ugts.length > 0 && argumentTypes != null) {
            Map<String, ClassNode> resolved;
            // add method generics to the end of the chain
            if (mapper.allGenerics.isEmpty() || (resolved = mapper.allGenerics.removeLast()).isEmpty()) {
                resolved = new TreeMap<String, ClassNode>();
            }
            mapper.allGenerics.add(resolved);

            // try to resolve each generics type by matching arguments to parameters
            for (GenericsType ugt : ugts) {
                for (int i = 0, n = Math.min(argumentTypes.size(), methodDeclaration.getParameters().length); i < n; i += 1) {
                    ClassNode rbt = GroovyUtils.getBaseType(argumentTypes.get(i));
                    ClassNode ubt = GroovyUtils.getBaseType(methodDeclaration.getParameters()[i].getType());

                    // rbt could be "String" and ubt could be "T"
                    if (ubt.isGenericsPlaceHolder() && ubt.getUnresolvedName().equals(ugt.getName())) {
                        saveParameterType(resolved, ugt.getName(), rbt);
                    } else {
                        // rbt could be "Foo<String, Object> and ubt could be "Foo<K, V>"
                        GenericsType[] ubt_gts = getGenericsTypes(ubt);
                        for (int j = 0; j < ubt_gts.length; j += 1) {
                            if (ubt_gts[j].getType().isGenericsPlaceHolder() && ubt_gts[0].getName().equals(ugt.getName())) {
                              //System.err.println(rbt.toString(false) + " --> " + ubt.toString(false));
                                // to resolve "T" follow "List<T> -> List<E>" then walk resolved type hierarchy to find "List<E>"
                                String key = getGenericsTypes(ubt.redirect())[j].getName();
                                GenericsMapper map = gatherGenerics(rbt, ubt.redirect());
                                ClassNode rt = map.findParameter(key, null);
                                if (rt != null) {
                                    saveParameterType(resolved, ugt.getName(), rt);
                                }
                                break;
                            }
                        }
                        // TODO: What about "Foo<Bar<T>>", "Foo<? extends T>", or "Foo<? super T>"?
                    }
                }
            }
        }

        return mapper;
    }

    /**
     * takes this type or type parameter and determines what its type should be based on the type parameter resolution in the top level of the mapper
     *
     * @param depth ensure that we don't recur forever, bottom out after a certain depth
     */
    public ClassNode resolveParameter(GenericsType topGT, int depth) {
        if (allGenerics.isEmpty()) {
            return topGT.getType();
        }

        if (depth > 10) {
            // don't recur forever
            // FIXADE This problem is believed fixed. If this conidtional is never reached, then we should be able to delete this section.
            Util.log(new Status(IStatus.WARNING, "org.eclipse.jdt.groovy.core", "GRECLIPSE-1040: prevent infinite recursion when resolving type parameters on generics type: " + topGT));
            return topGT.getType();
        }

        ClassNode origType = findParameter(topGT.getName(), topGT.getType());

        // now recur down all type parameters inside of this type
        // class Enum<E extends Enum<E>>
        if (origType.getGenericsTypes() != null) {
            origType = VariableScope.clone(origType);
            GenericsType[] genericsTypes = origType.getGenericsTypes();
            for (GenericsType genericsType : genericsTypes) {
                if (genericsType.getName().equals(topGT.getName())) {
                    // avoid infinite loops
                    // I still don't like this solution, but better than using a depth counter.
                    continue;
                }
                genericsType.setType(findParameter(genericsType.getName(), resolveParameter(genericsType, depth + 1)));
                genericsType.setLowerBound(null);
                genericsType.setUpperBounds(null);
                genericsType.setName(genericsType.getType().getName());
            }
        }
        return origType;
    }

    //--------------------------------------------------------------------------

    /** Keeps track of all type parameterization up the type hierarchy. */
    private final LinkedList<Map<String, ClassNode>> allGenerics = new LinkedList<Map<String, ClassNode>>();

    protected boolean hasGenerics() {
        return !allGenerics.isEmpty() && !allGenerics.getLast().isEmpty();
    }

    protected static GenericsType[] getGenericsTypes(ClassNode classNode) {
        GenericsType[] generics = GroovyUtils.getBaseType(classNode).getGenericsTypes();
        if (generics == null) return VariableScope.NO_GENERICS;
        return generics;
    }

    protected static GenericsType[] getGenericsTypes(MethodNode methodNode) {
        GenericsType[] generics = methodNode.getGenericsTypes();
        if (generics == null) return VariableScope.NO_GENERICS;
        return generics;
    }

    /**
     * finds the type of a parameter name in the highest level of the type hierarchy currently analyzed
     *
     * @param defaultType type to return if parameter name doesn't exist
     */
    protected ClassNode findParameter(String parameterName, ClassNode defaultType) {
        if (allGenerics.isEmpty()) {
            return defaultType;
        }
        ClassNode type = allGenerics.getLast().get(parameterName);
        if (type == null) {
            return defaultType;
        }
        return type;
    }

    protected static void saveParameterType(Map<String, ClassNode> map, String key, ClassNode val) {
        ClassNode old = map.remove(key);
        if (old != null && !old.equals(val) && !VariableScope.OBJECT_CLASS_NODE.equals(old) &&
                !VariableScope.OBJECT_CLASS_NODE.equals(val) && SimpleTypeLookup.isTypeCompatible(old, val) != Boolean.FALSE) {
            // find the LUB of val and value and save it to val
            System.err.println("Need to find LUB of " + val.toString(false) + " and " + old.toString(false));
            return;
        }
        map.put(key, val);
    }
}
