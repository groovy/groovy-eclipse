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
package org.eclipse.jdt.groovy.search;

import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.first;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.tail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * Looks up the type of an expression in the currently applicable categories.
 * <p>
 * Note: DefaultGroovyMethods are always considered to be an applicable category.
 */
public class CategoryTypeLookup implements ITypeLookup {

    @Override
    public TypeLookupResult lookupType(final Expression node, final VariableScope scope, final ClassNode objectExpressionType) {
        if (node instanceof VariableExpression || isCompatibleConstantExpression(node, scope, objectExpressionType)) {
            String simpleName = node.getText();
            ClassNode selfType = GroovyUtils.getWrapperTypeIfPrimitive(
                objectExpressionType != null ? objectExpressionType : scope.getDelegateOrThis());
            boolean isMethodPointer = (scope.getEnclosingNode() instanceof MethodPointerExpression);

            //
            List<MethodNode> candidates = new ArrayList<>();

            if (isMethodPointer || scope.isMethodCall()) {
                for (ClassNode category : scope.getCategoryNames()) {
                    for (MethodNode method : category.getMethods(simpleName)) {
                        if (isCompatibleCategoryMethod(method, selfType, scope)) {
                            candidates.add(method);
                        }
                    }
                }
            }
            if (!isMethodPointer) {
                for (AccessorSupport kind : AccessorSupport.values()) {
                    String methodName = kind.createAccessorName(simpleName);
                    if (methodName != null) {
                        for (ClassNode category : scope.getCategoryNames()) {
                            for (MethodNode method : category.getMethods(methodName)) {
                                if (kind.isAccessorKind(method, true) && isCompatibleCategoryMethod(method, selfType, scope) &&
                                        // GROOVY-5245: isPropName() methods cannot be used for bean-style property expressions
                                        (kind != AccessorSupport.ISSER || isDefaultGroovyMethod(method, scope) || isDefaultGroovyStaticMethod(method, scope))) {
                                    candidates.add(method);
                                }
                            }
                        }
                    }
                }
            }

            if (!candidates.isEmpty()) {
                int args = 1 + scope.getMethodCallNumberOfArguments();
                List<ClassNode> argumentTypes = new ArrayList<>(args);
                argumentTypes.add(selfType); // lhs of dot or implicit-this type
                if (args > 1) argumentTypes.addAll(scope.getMethodCallArgumentTypes());

                MethodNode method = selectBestMatch(candidates, argumentTypes, scope);
                if (method != null) {
                    ClassNode resolvedType = method.getReturnType();
                    // getAt(Object,String):Object supersedes getAt(Map<K,V>,Object):V when first param is String or GString; restore return type V for user experience
                    if ("getAt".equals(simpleName) && VariableScope.OBJECT_CLASS_NODE.equals(resolvedType) && isOrImplements(selfType, VariableScope.MAP_CLASS_NODE)) {
                        for (ClassNode face : selfType.getAllInterfaces()) {
                            if (face.equals(VariableScope.MAP_CLASS_NODE)) { // Map<K,V>
                                GenericsType[] generics = GroovyUtils.getGenericsTypes(face);
                                if (generics.length == 2) resolvedType = generics[1].getType();
                                break;
                            }
                        }
                    }

                    // must resolve generics here because TypeLookupResult uses declaring class (instead of self type)
                    if (org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.missesGenericsTypes(resolvedType)) {
                        GenericsMapper mapper = GenericsMapper.gatherGenerics(selfType, selfType.redirect());
                        resolvedType = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(resolvedType));
                    }

                    TypeLookupResult result = new TypeLookupResult(resolvedType, method.getDeclaringClass(), method,
                        isDefaultGroovyMethod(method, scope) ? TypeConfidence.LOOSELY_INFERRED : TypeConfidence.INFERRED, scope);
                    result.isGroovy = true; // enable semantic highlighting as Groovy method
                    return result;
                }
            }
        }
        return null;
    }

    private static boolean isCompatibleConstantExpression(final Expression node, final VariableScope scope, final ClassNode selfType) {
        if (node instanceof ConstantExpression && !scope.isTopLevel() && !VariableScope.VOID_CLASS_NODE.equals(selfType)) {
            org.codehaus.groovy.ast.ASTNode enclosingNode = scope.getEnclosingNode();
            if (!(enclosingNode instanceof AttributeExpression || (enclosingNode instanceof MethodPointerExpression &&
                                                                    VariableScope.CLASS_CLASS_NODE.equals(selfType)))) {
                return (VariableScope.STRING_CLASS_NODE.equals(node.getType()) && node.getLength() <= node.getText().length());
            }
        }
        return false;
    }

    private static boolean isCompatibleCategoryMethod(final MethodNode method, final ClassNode firstArgumentType, final VariableScope scope) {
        if (method.isStatic()) {
            Parameter[] paramters = method.getParameters();
            if (paramters != null && paramters.length > 0) {
                ClassNode parameterType = paramters[0].getType();
                if (VariableScope.CLASS_CLASS_NODE.equals(firstArgumentType) && isDefaultGroovyStaticMethod(method, scope)) {
                    parameterType = VariableScope.newClassClassNode(parameterType);
                }
                if (isSelfTypeCompatible(firstArgumentType, parameterType)) {
                    return !isDefaultGroovyMethod(method, scope) || !GroovyUtils.isDeprecated(method);
                }
            }
        }
        return false;
    }

    private static boolean isSelfTypeCompatible(final ClassNode source, final ClassNode target) {
        if (SimpleTypeLookup.isTypeCompatible(source, target) != Boolean.FALSE) {
            if (!(VariableScope.CLASS_CLASS_NODE.equals(source) && source.isUsingGenerics()) ||
                    VariableScope.OBJECT_CLASS_NODE.equals(target) || !target.isUsingGenerics()) {
                return true;
            } else {
                ClassNode sourceGT = source.getGenericsTypes()[0].getType();
                ClassNode targetGT = target.getGenericsTypes()[0].getType();
                if (SimpleTypeLookup.isTypeCompatible(sourceGT, targetGT) != Boolean.FALSE) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isDefaultGroovyMethod(final MethodNode method, final VariableScope scope) {
        return (VariableScope.DGM_CLASS_NODE.equals(method.getDeclaringClass()) || scope.isDefaultCategory(method.getDeclaringClass()));
    }

    private static boolean isDefaultGroovyStaticMethod(final MethodNode method, final VariableScope scope) {
        return (VariableScope.DGSM_CLASS_NODE.equals(method.getDeclaringClass()) || scope.isDefaultStaticCategory(method.getDeclaringClass()));
    }

    private static MethodNode selectBestMatch(final List<MethodNode> candidates, final List<ClassNode> argumentTypes, final VariableScope scope) {
        java.util.function.Function<MethodNode, List<ClassNode>> argsWithSelfTypeFix;
        if (!VariableScope.CLASS_CLASS_NODE.equals(argumentTypes.get(0))) {
            argsWithSelfTypeFix = x -> argumentTypes;
        } else {
            argsWithSelfTypeFix = m -> {
                if (isDefaultGroovyStaticMethod(m, scope)) {
                    List<ClassNode> adjusted = new ArrayList<>(argumentTypes);
                    adjusted.set(0, argumentTypes.get(0).getGenericsTypes()[0].getType());
                    return adjusted;
                }
                return argumentTypes;
            };
        }

        // Phase 1: find best self-type match for each set of parameters
        Map<String, MethodNode> m = new java.util.LinkedHashMap<>();
        for (MethodNode candidate : candidates) {
            StringBuilder signature = new StringBuilder();
            Parameter[] parameters = candidate.getParameters();
            for (int i = 1, n = parameters.length; i < n; i += 1) {
                signature.append(parameters[i].getType().getName()).append(',');
            }
            MethodNode previous = m.put(signature.toString(), candidate);
            if (previous != null) {
                long d1 = selfParameterDistance(argsWithSelfTypeFix.apply(previous), previous.getParameters());
                long d2 = selfParameterDistance(argsWithSelfTypeFix.apply(candidate), candidate.getParameters());

                if (d1 <= d2) m.put(signature.toString(), previous);
            }
        }

        if (scope.getEnclosingNode() instanceof MethodPointerExpression) {
            return first(m.values());
        }

        // Phase 2: find best set of parameters for given call arguments
        MethodNode method = null;
        for (MethodNode candidate : m.values()) {
            if (argumentTypes.size() == candidate.getParameters().length) {
                Boolean compatible = SimpleTypeLookup.isTypeCompatible(argumentTypes.subList(1, argumentTypes.size()), tail(candidate.getParameters()));
                if (compatible == Boolean.TRUE) { // exact match
                    method = candidate;
                    break;
                } else if (compatible != Boolean.FALSE) { // fuzzy match
                    if (method != null) {
                        long d1 = tailParameterDistance(argumentTypes, method.getParameters());
                        long d2 = tailParameterDistance(argumentTypes, candidate.getParameters());

                        if (d1 <= d2) continue; // stick with current selection
                    }
                    method = candidate;
                }
            }
        }

        return method;
    }

    private static long selfParameterDistance(final List<ClassNode> arguments, final Parameter[] parameters) {
        try {
            Class<?>[] a = {arguments.get(0).getTypeClass()};
            Class<?>[] p = {parameters[0].getType().getTypeClass()};
            // TODO: This can fail in a lot of cases; is there a better way to call it?
            return MetaClassHelper.calculateParameterDistance(a, new ParameterTypes(p));
        } catch (Throwable t) {
            return Long.MAX_VALUE - (VariableScope.isVoidOrObject(parameters[0].getType()) ? 0 : 1);
        }
    }

    private static long tailParameterDistance(final List<ClassNode> arguments, final Parameter[] parameters) {
        try {
            int n = arguments.size() - 1;
            Class<?>[] args = new Class[n];
            for (int i = 0; i < n; i += 1) {
                args[i] = arguments.get(i + 1).getTypeClass();
            }

            n = parameters.length - 1;
            Class<?>[] prms = new Class[n];
            for (int i = 0; i < n; i += 1) {
                prms[i] = parameters[i + 1].getType().getTypeClass();
            }

            // TODO: This can fail in a lot of cases; is there a better way to call it?
            return MetaClassHelper.calculateParameterDistance(args, new ParameterTypes(prms));
        } catch (Throwable t) {
            return Long.MAX_VALUE;
        }
    }
}
