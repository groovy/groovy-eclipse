/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * Looks up the type of an expression in the currently applicable categories.
 * Note: DefaultGroovyMethods are always considered to be an applicable category.
 */
public class CategoryTypeLookup implements ITypeLookup {

    @Override
    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
        if (node instanceof VariableExpression || isCompatibleConstantExpression(node, scope)) {
            String simpleName = node.getText();
            ClassNode expectedType = objectExpressionType;
            if (expectedType == null) expectedType = scope.getDelegateOrThis();
            ClassNode normalizedType = GroovyUtils.getWrapperTypeIfPrimitive(expectedType);

            //
            List<MethodNode> candidates = new ArrayList<>();

            for (ClassNode category : scope.getCategoryNames()) {
                for (MethodNode method : category.getMethods(simpleName)) {
                    if (isCompatibleCategoryMethod(method, normalizedType)) {
                        candidates.add(method);
                    }
                }
                String getterName = AccessorSupport.GETTER.createAccessorName(simpleName);
                if (getterName != null) {
                    for (MethodNode method : category.getMethods(getterName)) {
                        if (AccessorSupport.findAccessorKind(method, true) == AccessorSupport.GETTER &&
                                isCompatibleCategoryMethod(method, normalizedType)) {
                            candidates.add(method);
                        }
                    }
                }
                String setterName = AccessorSupport.SETTER.createAccessorName(simpleName);
                if (setterName != null) {
                    for (MethodNode method : category.getMethods(setterName)) {
                        if (AccessorSupport.findAccessorKind(method, true) == AccessorSupport.SETTER &&
                                isCompatibleCategoryMethod(method, normalizedType)) {
                            candidates.add(method);
                        }
                    }
                }
            }

            if (!candidates.isEmpty()) {
                int args = 1 + scope.getMethodCallNumberOfArguments();
                List<ClassNode> argumentTypes = new ArrayList<>(args);
                argumentTypes.add(normalizedType); // lhs of dot or delegate type
                if (args > 1) argumentTypes.addAll(scope.getMethodCallArgumentTypes());

                MethodNode method = selectBestMatch(candidates, argumentTypes);

                TypeLookupResult result = new TypeLookupResult(method.getReturnType(), method.getDeclaringClass(), method,
                        isDefaultGroovyMethod(method) ? TypeConfidence.LOOSELY_INFERRED : TypeConfidence.INFERRED, scope);
                result.isGroovy = true; // enable semantic highlighting as Groovy method
                return result;
            }
        }
        return null;
    }

    protected static boolean isCompatibleConstantExpression(Expression node, VariableScope scope) {
        if (node instanceof ConstantExpression) {
            org.codehaus.groovy.ast.ASTNode enclosingNode = scope.getEnclosingNode();
            if (!(enclosingNode instanceof AttributeExpression || enclosingNode instanceof MethodPointerExpression)) {
                return (VariableScope.STRING_CLASS_NODE.equals(node.getType()) && node.getLength() <= node.getText().length());
            }
        }
        return false;
    }

    protected static boolean isCompatibleCategoryMethod(MethodNode method, ClassNode firstArgumentType) {
        if (method.isStatic()) {
            Parameter[] paramters = method.getParameters();
            if (paramters != null && paramters.length > 0) {
                ClassNode parameterType = paramters[0].getType();
                if (VariableScope.CLASS_CLASS_NODE.equals(firstArgumentType) &&
                        VariableScope.DGSM_CLASS_NODE.equals(method.getDeclaringClass())) {
                    parameterType = VariableScope.newClassClassNode(parameterType);
                }
                if (isTypeCompatible(firstArgumentType, parameterType)) {
                    return !isDefaultGroovyMethod(method) || !GroovyUtils.isDeprecated(method);
                }
            }
        }
        return false;
    }

    protected static boolean isTypeCompatible(ClassNode source, ClassNode target) {
        if (SimpleTypeLookup.isTypeCompatible(source, target) != Boolean.FALSE) {
            if (!(VariableScope.CLASS_CLASS_NODE.equals(source) && source.isUsingGenerics()) ||
                    VariableScope.OBJECT_CLASS_NODE.equals(target) || !target.isUsingGenerics()) {
                return true;
            } else {
                source = source.getGenericsTypes()[0].getType();
                target = target.getGenericsTypes()[0].getType();
                if (SimpleTypeLookup.isTypeCompatible(source, target) != Boolean.FALSE) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static boolean isDefaultGroovyMethod(MethodNode method) {
        return VariableScope.ALL_DEFAULT_CATEGORIES.contains(method.getDeclaringClass());
    }

    /**
     * Selects the candidate that most closely matches the method call arguments.
     */
    protected static MethodNode selectBestMatch(List<MethodNode> candidates, List<ClassNode> argumentTypes) {
        MethodNode method = null;
        for (MethodNode candidate : candidates) {
            if (argumentTypes.size() == candidate.getParameters().length) {
                Boolean compatible = SimpleTypeLookup.isTypeCompatible(argumentTypes, candidate.getParameters());
                if (compatible == Boolean.TRUE) { // exact match
                    method = candidate;
                    break;
                } else if (compatible != Boolean.FALSE) { // fuzzy match
                    if (method != null) {
                        long d1 = calculateParameterDistance(argumentTypes, method.getParameters());
                        long d2 = calculateParameterDistance(argumentTypes, candidate.getParameters());

                        if (d1 <= d2) continue; // stick with current selection
                    }
                    method = candidate;
                } else if (method == null) {
                    method = candidate; // at least arguments line up with parameters
                }
            }
        }
        return method != null ? method : candidates.get(0);
    }

    protected static long calculateParameterDistance(List<ClassNode> arguments, Parameter[] parameters) {
        try {
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
            return Long.MAX_VALUE;
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return null;
    }

    @Override
    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }

    @Override
    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        // do nothing
    }
}
