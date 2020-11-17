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

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
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
    public TypeLookupResult resolveTypeParameterization(final ClassNode objExprType, final boolean isStatic) {
        if (declaringType != null && (declaration instanceof FieldNode || declaration instanceof PropertyNode ||
                                      declaration instanceof MethodNode && !(declaration instanceof ConstructorNode))) {
            ClassNode objectType = objExprType;
            if (objectType == null) {
                if (isGroovy || !isStatic) {
                    objectType = scope.getDelegateOrThis();
                } else {
                    objectType = declaringType;
                }
            }
            if (ClassHelper.isPrimitiveType(objectType)) {
                objectType = ClassHelper.getWrapper(objectType);
            }

            if (!(declaration instanceof MethodNode)) {
                GenericsMapper mapper = GenericsMapper.gatherGenerics(objectType, declaringType);
                ClassNode maybe = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(type));
                if (!maybe.toString(false).equals(type.toString(false))) {
                    TypeLookupResult result = new TypeLookupResult(maybe, declaringType, declaration, confidence, scope, extraDoc);
                    result.enclosingAnnotation = enclosingAnnotation;
                    result.isGroovy = isGroovy;
                    return result;
                }
            } else {
                MethodNode method = (MethodNode) declaration;
                if (!isStatic && method.getName().equals("getClass") && method.getParameters().length == 0) {
                    ClassNode classType = VariableScope.clone(method.getReturnType());
                    classType.getGenericsTypes()[0].setUpperBounds(new ClassNode[] {objectType});
                    return new TypeLookupResult(classType, method.getDeclaringClass(), method, confidence, scope, extraDoc);
                }
                if (!GroovyUtils.isUsingGenerics(method) || !(GenericsUtils.hasUnresolvedGenerics(method.getReturnType()) ||
                        GroovyUtils.getParameterTypes(method.getParameters()).stream().anyMatch(GenericsUtils::hasUnresolvedGenerics))) {
                    return this;
                }

                List<ClassNode> argumentTypes = new ArrayList<>();
                if (isGroovy) argumentTypes.add(objectType);
                GenericsMapper mapper;

                if (scope.getEnclosingNode() instanceof MethodPointerExpression) {
                    // apply type arguments from the object expression to the referenced method
                    mapper = GenericsMapper.gatherGenerics(argumentTypes, objectType, method);
                    method = VariableScope.resolveTypeParameterization(mapper, method);

                    // check for SAM-type coercion of the method pointer/reference
                    VariableScope.CallAndType cat = scope.getEnclosingMethodCallExpression();
                    if (cat != null && cat.declaration instanceof MethodNode && cat.call.getArguments() instanceof TupleExpression) {
                        int index = ((TupleExpression) cat.call.getArguments()).getExpressions().indexOf(scope.getEnclosingNode());
                        Parameter[] params = ((MethodNode) cat.declaration).getParameters();
                        if (index != -1 && params.length > 0) {
                            ClassNode targetType = params[Math.min(index, params.length - 1)].getType();
                            if (targetType.isArray() && index >= params.length - 1) {
                                targetType = objectType.getComponentType();
                            }
                            MethodNode sam = ClassHelper.findSAM(targetType);
                            if (sam != null) {
                                // use parameter types of SAM as "argument types" for referenced method to help resolve type parameters
                                mapper = GenericsMapper.gatherGenerics(GroovyUtils.getParameterTypes(sam.getParameters()), declaringType, method);
                                method = VariableScope.resolveTypeParameterization(mapper, method);

                                mapper = GenericsMapper.gatherGenerics(targetType);
                                method = VariableScope.resolveTypeParameterization(mapper, method);
                            }
                        }
                    } else if (testEnclosingAssignment(scope, rhs -> rhs == scope.getEnclosingNode())) {
                        // maybe the assign target type can help resolve type parameters of method pointer
                        ClassNode targetType = scope.getEnclosingAssignment().getLeftExpression().getType();

                        ClassNode returnType; // aligned with referenced method
                        if (targetType.equals(VariableScope.CLOSURE_CLASS_NODE)) {
                            returnType = Optional.of(targetType).map(ClassNode::getGenericsTypes)
                                .filter(Objects::nonNull).map(gts -> gts[0].getType()).orElse(VariableScope.OBJECT_CLASS_NODE);
                        } else {
                            returnType = Optional.ofNullable(ClassHelper.findSAM(targetType)).map(sam ->
                                VariableScope.resolveTypeParameterization(GenericsMapper.gatherGenerics(targetType), VariableScope.clone(sam.getReturnType()))
                            ).orElse(null);
                        }
                        if (returnType != null) {
                            mapper = GenericsMapper.gatherGenerics(singletonList(returnType), declaringType, returnTypeStub(method));
                            method = VariableScope.resolveTypeParameterization(mapper, method);
                        }
                    }
                } else {
                    if (scope.getMethodCallArgumentTypes() != null) argumentTypes.addAll(scope.getMethodCallArgumentTypes());
                    mapper = GenericsMapper.gatherGenerics(argumentTypes, objectType, method, scope.getMethodCallGenericsTypes());
                    method = VariableScope.resolveTypeParameterization(mapper, method);
                }
                if (method != declaration) {
                    TypeLookupResult result = new TypeLookupResult(method.getReturnType(), method.getDeclaringClass(), method, confidence, scope, extraDoc);
                    result.enclosingAnnotation = enclosingAnnotation;
                    result.isGroovy = isGroovy;
                    return result;
                }
            }
        }
        return this;
    }

    //--------------------------------------------------------------------------

    private static MethodNode returnTypeStub(final MethodNode node) {
        MethodNode stub = new MethodNode("", 0, VariableScope.VOID_CLASS_NODE, new Parameter[] {new Parameter(node.getReturnType(), "")}, null, null);
        stub.setDeclaringClass(node.getDeclaringClass());
        stub.setGenericsTypes(node.getGenericsTypes());
        return stub;
    }

    private static boolean testEnclosingAssignment(final VariableScope scope, final Predicate<Expression> rhsTest) {
        return Optional.ofNullable(scope.getEnclosingAssignment())
            .filter(bexp -> bexp instanceof DeclarationExpression)
            .map(BinaryExpression::getRightExpression).filter(rhsTest)
            .isPresent();
    }
}
