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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

public class TypeLookupResult {
    /**
     * Specifies the kind of match found for this type
     */
    public enum TypeConfidence {
        /**
         * Match is certain. E.g., the type is explicitly declared on a variable.
         */
        EXACT,

        /**
         * The type has been inferred from local or global context. E.g., by looking at assignment statements.
         */
        INFERRED,

        /**
         * The type has been inferred using less precise means. E.g., from an extending ITypeLookup.
         * <p>
         * Furthermore, a type confidence of this will not cause the inferencing engine to end its lookup. It will continue and try
         * to find a more confident type using other lookups.
         */
        LOOSELY_INFERRED,

        /**
         * This is an unknown reference
         */
        UNKNOWN;

        public static TypeConfidence findLessPrecise(TypeConfidence left, TypeConfidence right) {
            return left.isLessThan(right) ? left : right;
        }

        public boolean isLessThan(TypeConfidence that) {
            return this.ordinal() > that.ordinal();
        }

        public boolean isAtLeast(TypeConfidence that) {
            return this.ordinal() <= that.ordinal();
        }
    }

    public final TypeConfidence confidence;
    public final ClassNode declaringType;
    public final ClassNode type;

    /**
     * the type should be AnnotatedNode, but in Groovy 1.6.5, this type is not compatible with expression nodes
     */
    public final ASTNode declaration;
    public final VariableScope scope;

    /** Extra Javadoc that should appear in hovers. */
    public final String extraDoc;

    /** Found through Groovy category or extension. */
    public boolean isGroovy;

    public AnnotationNode enclosingAnnotation;

    /**
     * The assignment statement that encloses this expression, or null if there is none.
     */
    public BinaryExpression enclosingAssignment;

    /**
     * @param type the type of the expression being analyzed
     * @param declaringType the declaring type of the expression if the expression is a field, method, or type reference
     * @param declaration the declaration that this node refers to, or null if none (ie- the method, field, class, or property node)
     * @param confidence the confidence in this type assertion
     * @param scope the variable scope at this location
     */
    public TypeLookupResult(ClassNode type, ClassNode declaringType, ASTNode declaration, TypeConfidence confidence, VariableScope scope) {
        this(type, declaringType, declaration, confidence, scope, null);
    }

    /**
     * @param type the type of the expression being analyzed
     * @param declaringType the declaring type of the expression if the expression is a field, method, or type reference
     * @param declaration the declaration that this node refers to, or null if none (ie- the method, field, class, or property node)
     * @param confidence the confidence in this type assertion
     * @param scope the variable scope at this location
     * @param extraDoc extra javadoc to be shown in hovers
     */
    public TypeLookupResult(ClassNode type, ClassNode declaringType, ASTNode declaration, TypeConfidence confidence, VariableScope scope, String extraDoc) {
        this.confidence = confidence;
        this.type = type;
        this.declaringType = declaringType;
        this.declaration = declaration;
        this.scope = scope;
        this.extraDoc = extraDoc;
    }

    /**
     * Replaces type parameters with resolved types.
     */
    public TypeLookupResult resolveTypeParameterization(ClassNode objExprType, boolean isStatic) {
        if (declaringType != null && (declaration instanceof FieldNode || declaration instanceof PropertyNode ||
                                      declaration instanceof MethodNode && !(declaration instanceof ConstructorNode))) {
            ClassNode targetType = objExprType;
            if (targetType == null) {
                if (isGroovy || !isStatic) {
                    targetType = scope.getDelegateOrThis();
                } else {
                    targetType = declaringType;
                }
            }
            targetType = GroovyUtils.getWrapperTypeIfPrimitive(targetType);

            if (!(declaration instanceof MethodNode)) {
                GenericsMapper mapper = GenericsMapper.gatherGenerics(targetType, declaringType.redirect());
                ClassNode maybe = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(type));
                if (!maybe.toString(false).equals(type.toString(false))) {
                    TypeLookupResult result = new TypeLookupResult(maybe, declaringType, declaration, confidence, scope, extraDoc);
                    result.enclosingAnnotation = enclosingAnnotation;
                    result.enclosingAssignment = enclosingAssignment;
                    result.isGroovy = isGroovy;
                    return result;
                }
            } else {
                List<ClassNode> argumentTypes = scope.getMethodCallArgumentTypes();
                if (isGroovy) {
                    argumentTypes = new ArrayList<>();
                    argumentTypes.add(targetType);
                    if (scope.getMethodCallArgumentTypes() != null)
                        argumentTypes.addAll(scope.getMethodCallArgumentTypes());
                }

                MethodNode method = (MethodNode) declaration;
                if (!isStatic && method.getName().equals("getClass") && method.getParameters().length == 0) {
                    ClassNode classType = VariableScope.clone(method.getReturnType());
                    classType.getGenericsTypes()[0].setUpperBounds(new ClassNode[] {targetType});
                    return new TypeLookupResult(classType, method.getDeclaringClass(), method, confidence, scope, extraDoc);
                } else {
                    GenericsMapper mapper = GenericsMapper.gatherGenerics(argumentTypes, targetType, method, scope.getMethodCallGenericsTypes());
                    method = VariableScope.resolveTypeParameterization(mapper, method);
                    if (method != declaration) {
                        TypeLookupResult result = new TypeLookupResult(method.getReturnType(), method.getDeclaringClass(), method, confidence, scope, extraDoc);
                        result.enclosingAnnotation = enclosingAnnotation;
                        result.enclosingAssignment = enclosingAssignment;
                        result.isGroovy = isGroovy;
                        return result;
                    }
                }
            }
        }
        return this;
    }
}
