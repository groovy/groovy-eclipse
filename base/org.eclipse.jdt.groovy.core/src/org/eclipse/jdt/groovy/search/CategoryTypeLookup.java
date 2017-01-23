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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;

/**
 * Looks up the type of an expression in the currently applicable categories.
 * Note that DefaultGroovyMethods are always considered to be an applicable
 * category.
 *
 * @author Andrew Eisenberg
 * @created Oct 25, 2009
 */
public class CategoryTypeLookup implements ITypeLookup {

    /**
     * Looks up method calls to see if they are declared in any current categories
     */
    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
        if (node instanceof VariableExpression || (node instanceof ConstantExpression &&
                ClassHelper.STRING_TYPE.equals(node.getType()) && node.getLength() <= node.getText().length())) {
            TypeLookupResult result;
            String simpleName = node.getText();
            ClassNode expectedType = objectExpressionType != null ? objectExpressionType : scope.getDelegateOrThis();
            ClassNode normalizedType = GroovyUtils.getWrapperTypeIfPrimitive(expectedType);
            String getterName = AccessorSupport.GETTER.createAccessorName(simpleName);
            String setterName = AccessorSupport.SETTER.createAccessorName(simpleName);

            // go through all categories and look for a method with the given name
            for (ClassNode category : scope.getCategoryNames()) {
                for (MethodNode method : category.getMethods(simpleName)) {
                    if ((result = tryMatch(method, scope, expectedType, normalizedType)) != null) {
                        return result;
                    }
                }
                // also check to see if an accessor variant is available
                if (getterName != null) {
                    for (MethodNode method : category.getMethods(getterName)) {
                        if (method.isStatic() && AccessorSupport.findAccessorKind(method, true) == AccessorSupport.GETTER) {
                            if ((result = tryMatch(method, scope, expectedType, normalizedType)) != null) {
                                return result;
                            }
                        }
                    }
                }
                if (setterName != null) {
                    for (MethodNode method : category.getMethods(setterName)) {
                        if (method.isStatic() && AccessorSupport.findAccessorKind(method, true) == AccessorSupport.SETTER) {
                            if ((result = tryMatch(method, scope, expectedType, normalizedType)) != null) {
                                return result;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private TypeLookupResult tryMatch(MethodNode method, VariableScope scope, ClassNode expectedType, ClassNode normalizedType) {
        Parameter[] params = method.getParameters();
        if (params != null && params.length > 0 && isAssignableFrom(normalizedType, params[0].getType())) {
            ClassNode declaringClass = method.getDeclaringClass();
            ClassNode returnType = SimpleTypeLookup.getTypeFromDeclaration(method, expectedType);
            TypeConfidence confidence = getConfidence(declaringClass);
            if (confidence == TypeConfidence.LOOSELY_INFERRED) {
                confidence = checkParameters(params, scope.getMethodCallArgumentTypes());
            }
            TypeLookupResult result = new TypeLookupResult(returnType, declaringClass, method, confidence, scope);
            result.isGroovy = true;
            return result;
        }
        return null;
    }

    /**
     * DGM and DGSM classes are loosely inferred so that other lookups can provide better solutions
     */
    private TypeConfidence getConfidence(ClassNode declaringClass) {
        boolean contains = VariableScope.ALL_DEFAULT_CATEGORIES.contains(declaringClass);
        return contains ? TypeConfidence.LOOSELY_INFERRED : TypeConfidence.INFERRED;
    }

    /**
     * Checks for exact match. If so then confidence is EXACT. In that case no need to find better solution.
     *
     * @param params declared method parameters
     * @param arguments actual method arguments
     * @return updated confidence
     */
    private TypeConfidence checkParameters(Parameter[] params, List<ClassNode> arguments) {
        if (arguments == null || arguments.size() == 0) {
            return TypeConfidence.LOOSELY_INFERRED;
        }
        if (params.length != arguments.size() + 1) {
            return TypeConfidence.LOOSELY_INFERRED;
        }
        for (int i = 0; i < arguments.size(); i++) {
            if (!arguments.get(i).equals(params[i + 1].getOriginType())) {
                return TypeConfidence.LOOSELY_INFERRED;
            }
        }
        return TypeConfidence.EXACT;
    }

    private void findAllSupers(ClassNode clazz, Set<String> allSupers) {
        String name = clazz.getName();
        if (!allSupers.contains(name)) {
            allSupers.add(name);
            if (clazz.getSuperClass() != null) {
                findAllSupers(clazz.getSuperClass(), allSupers);
            }
            if (clazz.getInterfaces() != null) {
                for (ClassNode superInterface : clazz.getInterfaces()) {
                    findAllSupers(superInterface, allSupers);
                }
            }
        }
    }

    /**
     * Can {@code source} be assigned to {@code target}?
     */
    private boolean isAssignableFrom(ClassNode source, ClassNode target) {
        if (source == null || target == null) {
            return false;
        }
        String sourceName = source.getName(), targetName = target.getName();
        Set<String> allSupers = new HashSet<String>();
        allSupers.add("java.lang.Object");
        if (sourceName.startsWith("["))
            allSupers.add("[Ljava.lang.Object;");
        findAllSupers(source, allSupers);
        for (String superName : allSupers) {
            if (targetName.equals(superName)) {
                return true;
            }
        }
        return false;
    }

    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        return null;
    }

    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        return null;
    }

    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        // do nothing
    }
}
