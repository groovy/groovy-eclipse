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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
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
                    TypeLookupResult result = new TypeLookupResult(method.getReturnType(), method.getDeclaringClass(), method,
                        isDefaultGroovyMethod(method, scope) ? TypeConfidence.LOOSELY_INFERRED : TypeConfidence.INFERRED, scope);
                    result.isGroovy = true; // enable semantic highlighting as Groovy method
                    return result;
                }
            }
        }
        return null;
    }

    protected static boolean isCompatibleConstantExpression(final Expression node, final VariableScope scope, final ClassNode selfType) {
        if (node instanceof ConstantExpression && !scope.isTopLevel() && !VariableScope.VOID_CLASS_NODE.equals(selfType)) {
            org.codehaus.groovy.ast.ASTNode enclosingNode = scope.getEnclosingNode();
            if (!(enclosingNode instanceof AttributeExpression || (enclosingNode instanceof MethodPointerExpression &&
                                                                    VariableScope.CLASS_CLASS_NODE.equals(selfType)))) {
                return (VariableScope.STRING_CLASS_NODE.equals(node.getType()) && node.getLength() <= node.getText().length());
            }
        }
        return false;
    }

    protected static boolean isCompatibleCategoryMethod(final MethodNode method, final ClassNode firstArgumentType, final VariableScope scope) {
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

    protected static boolean isSelfTypeCompatible(final ClassNode source, final ClassNode target) {
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

    protected static boolean isDefaultGroovyMethod(final MethodNode method, final VariableScope scope) {
        return (VariableScope.DGM_CLASS_NODE.equals(method.getDeclaringClass()) || scope.isDefaultCategory(method.getDeclaringClass()));
    }

    protected static boolean isDefaultGroovyStaticMethod(final MethodNode method, final VariableScope scope) {
        return (VariableScope.DGSM_CLASS_NODE.equals(method.getDeclaringClass()) || scope.isDefaultStaticCategory(method.getDeclaringClass()));
    }

    protected static MethodNode selectBestMatch(final List<MethodNode> candidates, final List<ClassNode> argumentTypes, final VariableScope scope) {
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

        MethodNode method = null;
        for (MethodNode candidate : candidates) {
            if (argumentTypes.size() == candidate.getParameters().length) {
                Boolean compatible = SimpleTypeLookup.isTypeCompatible(argsWithSelfTypeFix.apply(candidate), candidate.getParameters());
                if (compatible == Boolean.TRUE) { // exact match
                    method = candidate;
                    break;
                } else if (compatible != Boolean.FALSE) { // fuzzy match
                    if (method != null) {
                        long d1 = calculateParameterDistance(argsWithSelfTypeFix.apply(method), method.getParameters());
                        long d2 = calculateParameterDistance(argsWithSelfTypeFix.apply(candidate), candidate.getParameters());

                        if (d1 <= d2) continue; // stick with current selection
                    }
                    method = candidate;
                }
            }
        }
        return method;
    }

    protected static long calculateParameterDistance(final List<ClassNode> arguments, final Parameter[] parameters) {
        try {
            if (arguments.size() == 1 && parameters.length == 1) {
                Class<?>[] args = {arguments.get(0).getTypeClass()};
                Class<?>[] prms = {parameters[0].getType().getTypeClass()};
                return MetaClassHelper.calculateParameterDistance(args, new ParameterTypes(prms));
            }

            // weight self type higher to prevent considering getAt(Map, Object)
            // and getAt(Object, String) equally for the arguments (Map, String)

            int n = 1 + arguments.size();
            Class<?>[] args = new Class[n];
            for (int i = 1; i < n; i += 1) {
                args[i] = arguments.get(i - 1).getTypeClass();
            }
            args[0] = args[1]; // repeat the self type for effect

            n = 1 + parameters.length;
            Class<?>[] prms = new Class[n];
            for (int i = 1; i < n; i += 1) {
                prms[i] = parameters[i - 1].getType().getTypeClass();
            }
            prms[0] = prms[1]; // repeat the self type for effect

            // TODO: This can fail in a lot of cases; is there a better way to call it?
            return MetaClassHelper.calculateParameterDistance(args, new ParameterTypes(prms));
        } catch (Throwable t) {
            return Long.MAX_VALUE - (VariableScope.isVoidOrObject(parameters[0].getType()) ? 0 : 1);
        }
    }
}
