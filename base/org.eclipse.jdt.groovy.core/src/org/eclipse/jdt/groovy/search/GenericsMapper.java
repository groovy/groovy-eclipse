/*
 * Copyright 2009-2025 the original author or authors.
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
package org.eclipse.jdt.groovy.search;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Maps type parameters to resolved types.
 */
public class GenericsMapper {

    /**
     * Creates a mapper for a resolved type.
     * <p>
     * This is the public entry point for this class.
     *
     * @param resolvedType type that has type parameters already resolved
     */
    public static GenericsMapper gatherGenerics(final ClassNode resolvedType) {
        return gatherGenerics(resolvedType, resolvedType);
    }

    /**
     * Creates a mapper for a resolved type by tracing up the type hierarchy
     * until the declaring type is reached.
     * <p>
     * This is the public entry point for this class.
     *
     * @param resolvedType type that has type parameters already resolved
     * @param declaringType a type that is somewhere in {@code resolvedType}'s hierarchy used to find the target of the mapping
     */
    public static GenericsMapper gatherGenerics(final ClassNode resolvedType, final ClassNode declaringType) {
        if (resolvedType.isArray() && declaringType.isArray()) {
            return gatherGenerics(resolvedType.getComponentType(), declaringType.getComponentType());
        }

        GenericsMapper mapper = new GenericsMapper();

        if (declaringType.isGenericsPlaceHolder()) {
            Map<String, ClassNode> resolved = new TreeMap<>();
            resolved.put(declaringType.getUnresolvedName(), resolvedType);

            mapper.allGenerics.add(resolved);
            return mapper;
        }

        ClassNode rCandidate = resolvedType;
        ClassNode uCandidate = resolvedType.redirect();
        Iterator<ClassNode> rIterator = getTypeHierarchy(rCandidate, true);
        Iterator<ClassNode> uIterator = getTypeHierarchy(uCandidate, false);

        // travel up the hierarchy
        while (rIterator.hasNext() && uIterator.hasNext()) {
            rCandidate = rIterator.next();
            uCandidate = uIterator.next();

            Map<String, ClassNode> resolved = null;
            ClassNode oc = rCandidate.getNodeMetaData("outer.class");
            if (oc != null) { GenericsMapper gm = gatherGenerics(oc);
                if (gm.hasGenerics()) resolved = gm.allGenerics.getLast();
            }

            GenericsType[] rgts = GroovyUtils.getGenericsTypes(rCandidate);
            GenericsType[] ugts = GroovyUtils.getGenericsTypes(uCandidate);

            int n = ugts.length;
            if (n > 0 && rgts.length == 0) { // diamond or raw type
                rgts = new GenericsType[n];
                for (int i = 0; i < n; i += 1) {
                    rgts[i] = new GenericsType(Optional.ofNullable(ugts[i].getUpperBounds()).map(bounds -> bounds[0])
                        .orElse(ugts[i].getType().redirect()).getPlainNodeReference()); // GROOVY-10055, GROOVY-10166
                }
            }
            assert rgts.length == ugts.length;

            if (resolved == null) resolved = (n > 0 ? new TreeMap<>() : Collections.emptyMap());
            for (int i = 0; i < n; i += 1) {
                // now try to resolve the parameter in the context of the
                // most recently visited type; if it does not exist, then
                // default to the resovled type
                resolved.put(ugts[i].getName(), mapper.resolveParameter(rgts[i], 0));
            }
            mapper.allGenerics.add(resolved);

            // don't need to travel up the whole hierarchy; stop at the declaring class
            if (rCandidate.getName().equals(declaringType.getName())) {
                break;
            }
        }

        return mapper;
    }

    public static GenericsMapper gatherGenerics(final List<ClassNode> argumentTypes, final ClassNode delegateOrThisType, final MethodNode methodDeclaration, final GenericsType... methodGenerics) {
        // GOAL: resolve return type of something like "<T> Iterator<T> iterator(T[] array)"

        GenericsMapper mapper;
        if (methodDeclaration.isStatic()) {
            mapper = new GenericsMapper();
        } else { // inspect receiver type for generics
            mapper = gatherGenerics(delegateOrThisType, methodDeclaration.getDeclaringClass());
        }

        GenericsType[] ugts = GroovyUtils.getGenericsTypes(methodDeclaration);
        if (ugts.length > 0) {
            Map<String, ClassNode> resolved;
            // add method generics to the end
            if (mapper.allGenerics.isEmpty() ||
                    (resolved = mapper.allGenerics.removeLast()).isEmpty()) {
                resolved = new TreeMap<>();
            }
            mapper.allGenerics.add(resolved);

            // deal with name shadowing
            for (GenericsType ugt : ugts) {
                resolved.remove(ugt.getName());
            }

            // check for explicit type argument(s)
            if (methodGenerics != null && methodGenerics.length > 0) {
                for (int i = 0, n = Math.min(ugts.length, methodGenerics.length); i < n; i += 1) {
                    resolved.put(ugts[i].getName(), methodGenerics[i].getType());
                }
            } else if (argumentTypes != null && !argumentTypes.isEmpty()) {
                // try to resolve each by matching arguments to parameters
                Parameter[] parameters = methodDeclaration.getParameters();
                for (GenericsType ugt : ugts) {
                    tryResolveMethodT(ugt, resolved, parameters, argumentTypes);
                }
            }
        }

        return mapper;
    }

    /**
     * Takes type or type parameter and determines what its type should be based
     * on the type parameter resolution in the top level of the mapper.
     *
     * @param depth ensures that we don't cycle; bottom out after a chosen depth
     */
    public ClassNode resolveParameter(final GenericsType topGT, final int depth) { assert depth >= 0;
        if (allGenerics.isEmpty()) {
            if (!topGT.isWildcard()) {
                return topGT.getType();
            }
            if (topGT.getLowerBound() != null) {
                return topGT.getLowerBound();
            }
            if (topGT.getUpperBounds() != null) {
                return topGT.getUpperBounds()[0];
            }
            if (!topGT.getType().equals(VariableScope.OBJECT_CLASS_NODE)) {
                return topGT.getType(); // GROOVY-10671: non-Object erasure
            }
            return VariableScope.OBJECT_CLASS_NODE;
        }

        if (depth > 10) {
            // FIXADE This problem is believed fixed. If this conidtional is never reached, then we should be able to delete this section.
            Util.log(new Status(IStatus.WARNING, "org.eclipse.jdt.groovy.core", "GRECLIPSE-1040: prevent infinite recursion when resolving type parameters on generics type: " + topGT));
            return topGT.getType();
        }

        ClassNode theType = allGenerics.getLast().getOrDefault(topGT.getName(), topGT.getType());
        // recur for type parameters of the type: class Enum<E extends Enum<E>>
        if (theType.redirect().isUsingGenerics()) {
            theType = VariableScope.clone(theType);
            GenericsType[] genericsTypes = theType.getGenericsTypes();
            if (genericsTypes == null) genericsTypes = GenericsType.EMPTY_ARRAY;
            for (GenericsType genericsType : genericsTypes) {
                if (genericsType.getName().equals(topGT.getName())) {
                    continue; // avoid infinite loops -- not ideal but better than using a depth counter
                }
                genericsType.setType(allGenerics.getLast().getOrDefault(genericsType.getName(), resolveParameter(genericsType, depth + 1)));
                genericsType.setName(genericsType.getType().getName());
                genericsType.setUpperBounds(null);
                genericsType.setLowerBound(null);
            }

            ClassNode oc = theType.getOuterClass(); // non-static inner class may use outer class type var
            if (oc != null && oc.getGenericsTypes() != null && !Flags.isStatic(theType.getModifiers())) {
                oc = resolveParameter(new GenericsType(oc), depth + 1);
                theType.putNodeMetaData("outer.class", oc);
            }
        }
        return theType;
    }

    //--------------------------------------------------------------------------

    /** Keeps track of all type parameterization up the type hierarchy. */
    final Deque<Map<String, ClassNode>> allGenerics = new LinkedList<>();

    protected boolean hasGenerics() {
        return !allGenerics.isEmpty() && !allGenerics.getLast().isEmpty();
    }

    /**
     * Finds the type of a parameter name in the highest level of the type hierarchy currently analyzed.
     *
     * @param defaultType type to return if parameter name doesn't exist
     */
    protected ClassNode findParameter(final String parameterName, final ClassNode defaultType) {
        if (!allGenerics.isEmpty()) {
            ClassNode type = allGenerics.getLast().get(parameterName);
            if (type != null) {
                return type;
            }
        }
        return defaultType;
    }

    protected static Iterator<ClassNode> getTypeHierarchy(final ClassNode type, final boolean useResolved) {
        Set<ClassNode> hierarchy = new LinkedHashSet<>(); // keeps order
        VariableScope.createTypeHierarchy(type, hierarchy, useResolved);
        hierarchy.remove(VariableScope.OBJECT_CLASS_NODE);
        hierarchy.removeIf(cn -> cn.isInterface() &&
          cn.redirect().getGenericsTypes() == null);
        return hierarchy.iterator();
    }

    /**
     * @see org.codehaus.groovy.classgen.AsmClassGenerator#isVargs(Parameter[])
     * @see org.codehaus.jdt.groovy.internal.compiler.ast.GroovyCompilationUnitDeclaration.UnitPopulator#isVargs(Parameter[])
     */
    protected static boolean isVargs(final Parameter[] parameters) {
        if (parameters.length > 0) {
            Parameter last = parameters[parameters.length - 1];
            ClassNode type = last.getType();
            if (type.isArray()) {
                return true;
            }
        }
        return false;
    }

    protected static void readGenericsLinks(final Map<String, ClassNode> map, final ClassNode rt, final ClassNode ut) {
        if (rt == null || ut == null || rt == ut || GroovyUtils.getBaseType(ut).getGenericsTypes() == null) return;

        if (ut.isGenericsPlaceHolder()) {
            map.put(ut.getUnresolvedName(), rt);
        } else if (rt.isArray() && ut.isArray()) {
            readGenericsLinks(map, rt.getComponentType(), ut.getComponentType());
        } else if (rt.equals(ut) || !StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(rt, ut)) {
            readGenericsLinks(map, rt.getGenericsTypes(), ut.getGenericsTypes());
        } else {
            ClassNode superClass = ClassHelper.getNextSuperClass(ClassHelper.getWrapper(rt), ut);
            readGenericsLinks(map, StaticTypeCheckingSupport.getCorrectedClassNode(rt, superClass, true), ut);
        }
    }

    protected static void readGenericsLinks(final Map<String, ClassNode> map, final ClassNode[] rt, final ClassNode[] ut) {
        if (rt == null || ut == null || rt.length != ut.length) return;

        for (int i = 0, n = rt.length; i < n; i += 1) {
            ClassNode rti = rt[i];
            ClassNode uti = ut[i];
            if (uti.isGenericsPlaceHolder()) {
                map.put(uti.getUnresolvedName(), uti);
            } else if (uti.isUsingGenerics()) {
                readGenericsLinks(map, rti.getGenericsTypes(), uti.getGenericsTypes());
            }
        }
    }

    protected static void readGenericsLinks(final Map<String, ClassNode> map, final GenericsType[] rt, final GenericsType[] ut) {
        if (rt == null || ut == null || rt.length != ut.length) return;

        for (int i = 0, n = rt.length; i < n; i += 1) {
            GenericsType rti = rt[i];
            GenericsType uti = ut[i];
            if (uti.isPlaceholder()) {
                map.put(uti.getName(), rti.getType());
            } else if (uti.isWildcard()) {
                if (rti.isWildcard()) {
                    readGenericsLinks(map, rti.getLowerBound(), uti.getLowerBound());
                    readGenericsLinks(map, rti.getUpperBounds(), uti.getUpperBounds());
                } else {
                    ClassNode cu = rti.getType();
                    readGenericsLinks(map, cu, uti.getLowerBound());
                    ClassNode[] upperBounds = uti.getUpperBounds();
                    if (upperBounds != null) {
                        for (ClassNode cn : upperBounds) {
                            readGenericsLinks(map, cu, cn);
                        }
                    }
                }
            } else {
                readGenericsLinks(map, rti.getType(), uti.getType());
            }
        }
    }

    protected static void saveParameterType(final Map<String, ClassNode> map, final String key, ClassNode val, final boolean weak) {
        // special case 1: Arrays.asList(T...): List<T> -- each param has a chance to influence the LUB
        // special case 2: Collections.replaceAll(List<T>, T, T) -- list should dictate type unless it's dynamic
        // special case 3: Collections.checkedSet(Set<E>, Class<E>): Set<E> -- set type and class type should agree

        ClassNode old = map.remove(key); // if mapped type is Object, consider it malleable
        if (old != null && !old.equals(val) && !old.equals(VariableScope.OBJECT_CLASS_NODE) && weak) {
            val = /*WideningCategories.lowestUpperBound(*/old/*, val)*/;
        }
        map.put(key, GroovyUtils.getWrapperTypeIfPrimitive(val));
    }

    protected static void tryResolveMethodT(final GenericsType unresolved, final Map<String, ClassNode> resolved, final Parameter[] parameters, final List<ClassNode> argumentTypes) {
        for (int i = 0, n = isVargs(parameters) ? argumentTypes.size() : Math.min(argumentTypes.size(), parameters.length); i < n; i += 1) {
            ClassNode rbt = argumentTypes.get(i);
            ClassNode ubt = parameters[Math.min(i, parameters.length - 1)].getType();
            while (rbt.isArray() && ubt.isArray()) {
                rbt = rbt.getComponentType();
                ubt = ubt.getComponentType();
            }

            if (ubt.isGenericsPlaceHolder()) {
                // ubt could be "T" or "T extends CharSequence" and rbt could be "String" or whatever
                if (ubt.getUnresolvedName().equals(unresolved.getName()) && GroovyUtils.isAssignable(rbt, ubt)) {
                    saveParameterType(resolved, unresolved.getName(), rbt, true);
                }
            } else if (ubt.redirect().isUsingGenerics()) {
                // ubt could be "Foo<T,U>" and rbt could be "Foo<String,Object>" or "Closure<Object>"
                GenericsType[] ubt_gts = GroovyUtils.getGenericsTypes(ubt);
ubt_gts:        for (int j = 0; j < ubt_gts.length; j += 1) {
                    if (StaticTypeCheckingSupport.isUnboundedWildcard(ubt_gts[j])) continue;
                    ClassNode ubt_gt_t = GroovyUtils.getBaseType(ubt_gts[j].isWildcard() ? Optional.ofNullable(ubt_gts[j].getUpperBounds()).map(arr -> arr[0]).orElse(ubt_gts[j].getLowerBound()) : ubt_gts[j].getType());
                    // ubt_gt_t is "T" from "Foo<T>" or "Foo<T[]>" or "Foo<? super T>"; or "Bar<T>" from "Foo<Bar<T>>"
                    if (rbt.equals(ClassHelper.CLOSURE_TYPE) && !ubt.equals(ClassHelper.CLOSURE_TYPE)) {
                        if (!rbt.isUsingGenerics()) continue; // rbt is raw type
                        MethodNode sam = ClassHelper.findSAM(ubt);
                        if (sam != null) {
                            // read "T: A[]"
                            GenericsMapper um = gatherGenerics(ubt);
                            // read "T: String[]"
                            Map<String, ClassNode> rm = new HashMap<>();
                            readGenericsLinks(rm, rbt.getGenericsTypes()[0].getType(), sam.getReturnType());

                            for (Map.Entry<String, ClassNode> entry : rm.entrySet()) {
                                ClassNode ut = um.findParameter(entry.getKey(), null);
                                if (ut != null) {
                                    // find "A: String" from "A[]" and "String[]"
                                    Map<String, ClassNode> map = new HashMap<>();
                                    readGenericsLinks(map, entry.getValue(), ut);

                                    ClassNode rt = map.get(unresolved.getName());
                                    if (rt != null && GroovyUtils.isAssignable(rt, ubt_gt_t)) {
                                        saveParameterType(resolved, unresolved.getName(), rt, false);
                                        break ubt_gts;
                                    }
                                }
                            }
                        }
                    } else if (ubt_gt_t.isGenericsPlaceHolder() && ubt_gt_t.getUnresolvedName().equals(unresolved.getName())) {
                        // to resolve "T" follow "List<T> -> List<E>" then walk resolved type hierarchy to find "List<E>"
                        String key = GroovyUtils.getGenericsTypes(ubt.redirect())[j].getName();
                        GenericsMapper map = gatherGenerics(rbt, ubt);
                        ClassNode rt = map.findParameter(key, null);

                        if (rt != null && GroovyUtils.isAssignable(rt, ubt_gt_t)) {
                            saveParameterType(resolved, unresolved.getName(), rt, false);
                        }
                        break ubt_gts; // ugt resolved; no need to look at more
                    }
                }
            }
        }
    }
}
