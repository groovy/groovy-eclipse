/*
 * Copyright 2009-2024 the original author or authors.
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
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.ClosureSignatureHint;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
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
    public       ClassNode receiverType;
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

    public TypeLookupResult(ClassNode type, ClassNode declaringType, ASTNode declaration, TypeLookupResult that) {
        this(type, declaringType, declaration, that.confidence, that.scope, that.extraDoc);
        this.enclosingAnnotation = that.enclosingAnnotation;
        this.enclosingAssignment = that.enclosingAssignment;
        this.receiverType = that.receiverType;
        this.isGroovy = that.isGroovy;
    }

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
            } else if (objectType.equals(VariableScope.CLASS_CLASS_NODE) &&
                   !declaringType.equals(VariableScope.CLASS_CLASS_NODE) && !isGroovy) {
                objectType = VariableScope.getFirstGenerics(objectType); // peel Class<>
            }

            if (!(declaration instanceof MethodNode)) {
                GenericsMapper mapper = GenericsMapper.gatherGenerics(objectType, declaringType);
                ClassNode maybe = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(type));
                if (!maybe.toString(false).equals(type.toString(false))) {
                    return new TypeLookupResult(maybe, declaringType, declaration, this);
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
                ClassNode targetType = null;
                GenericsMapper mapper;

                if (scope.getEnclosingNode() instanceof MethodPointerExpression) {
                    if (!isStatic && !method.isStatic()) {
                        // apply type arguments from the object expression to the referenced method
                        mapper = GenericsMapper.gatherGenerics(argumentTypes, objectType, method);
                        method = VariableScope.resolveTypeParameterization(mapper, method);
                    }

                    // check for Closure or SAM-type coercion of the method pointer/reference
                    VariableScope.CallAndType cat = scope.getEnclosingMethodCallExpression();
                    if (cat != null && cat.declaration instanceof MethodNode && cat.call.getArguments() instanceof TupleExpression) {
                        int index = ((TupleExpression) cat.call.getArguments()).getExpressions().indexOf(scope.getEnclosingNode());
                        Parameter[] params = ((MethodNode) cat.declaration).getParameters();
                        if (index != -1 && params.length > 0) {
                            targetType = params[Math.min(index, params.length - 1)].getType();
                            if (targetType.isArray() && index >= params.length - 1) {
                                targetType = objectType.getComponentType();
                            }

                            if (targetType.equals(VariableScope.CLOSURE_CLASS_NODE)) {
                                MethodNode source = method; // effectively final version
                                Parameter  target = params[Math.min(index, params.length - 1)];
                                method = findClosureSignature(target, source, scope.getEnclosingModuleNode().getContext(), cat.call).map(types -> {
                                    GenericsMapper gm = GenericsMapper.gatherGenerics(java.util.Arrays.asList(types), cat.declaringType, source);
                                    MethodNode mn = VariableScope.resolveTypeParameterization(gm, source);
                                    return mn;
                                }).orElse(method);
                            }
                        }
                    } else if (testEnclosingAssignment(scope, rhs -> rhs == scope.getEnclosingNode())) {
                        // maybe the assign target type can help resolve type parameters of method
                        targetType = scope.getEnclosingAssignment().getLeftExpression().getType();
                    }

                    if (targetType != null) {
                        if (targetType.equals(VariableScope.CLOSURE_CLASS_NODE)) {
                            ClassNode returnType = Optional.of(targetType).map(ClassNode::getGenericsTypes)
                                .filter(Objects::nonNull).map(gts -> gts[0].getType()).orElse(VariableScope.OBJECT_CLASS_NODE);

                            mapper = GenericsMapper.gatherGenerics(singletonList(returnType), declaringType, returnTypeStub(method));
                            method = VariableScope.resolveTypeParameterization(mapper, method);
                        } else {
                            if (ClassHelper.isSAMType(targetType)) {
                                ClassNode[] pt = GenericsUtils.parameterizeSAM(targetType).getV1();
                                if (isStatic && !method.isStatic()) { // GROOVY-10734, GROOVY-11259
                                    objectType = pt[0];  pt = Arrays.copyOfRange(pt, 1, pt.length);
                                }
                                // use parameter types of SAM as "argument types" for referenced method to help resolve type parameters
                                mapper = GenericsMapper.gatherGenerics(Arrays.asList(pt), objectType, method);
                                method = VariableScope.resolveTypeParameterization(mapper, method);

                                mapper = GenericsMapper.gatherGenerics(targetType);
                                method = VariableScope.resolveTypeParameterization(mapper, method);
                            }
                        }
                    }
                } else {
                    if (scope.getMethodCallArgumentTypes() != null) argumentTypes.addAll(scope.getMethodCallArgumentTypes());
                    mapper = GenericsMapper.gatherGenerics(argumentTypes, objectType, method, scope.getMethodCallGenericsTypes());
                    method = VariableScope.resolveTypeParameterization(mapper, method);

                    if (scope.getMethodCallGenericsTypes() == null && GenericsUtils.hasUnresolvedGenerics(method.getReturnType()) &&
                            (argumentTypes.size() == (isGroovy ? 1 : 0) || false/*return type placeholder(s) not in parameters*/) &&
                            testEnclosingAssignment(scope, rhs ->
                                (rhs instanceof StaticMethodCallExpression && rhs == scope.getCurrentNode()) ||
                                (rhs instanceof MethodCallExpression && ((MethodCallExpression) rhs).getMethod() == scope.getCurrentNode())
                            )) {
                        // maybe the assign target type can help resolve type parameters of method
                        targetType = scope.getEnclosingAssignment().getLeftExpression().getType();

                        mapper = GenericsMapper.gatherGenerics(singletonList(targetType), declaringType, returnTypeStub(method));
                        method = VariableScope.resolveTypeParameterization(mapper, method);
                    }
                }

                ClassNode returnType = method.getReturnType();
                // do not return method type parameters; "def <T> T m()" returns Object if "T" unknown
                if (method.getGenericsTypes() != null && scope.getMethodCallGenericsTypes() == null &&
                        GenericsUtils.hasUnresolvedGenerics(returnType) && (mapper = GenericsMapper.gatherGenerics(returnType)).hasGenerics()) {
                    returnType = VariableScope.resolveTypeParameterization(mapper.fillPlaceholders(method.getGenericsTypes()), VariableScope.clone(returnType.redirect()));
                }

                if (method != declaration || returnType != method.getReturnType()) {
                    return new TypeLookupResult(returnType, method.getDeclaringClass(), method, this);
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

    private static Optional<ClassNode[]> findClosureSignature(final Parameter target, final MethodNode origin, final SourceUnit unit, final MethodCall call) {
        return GroovyUtils.getAnnotations(target, VariableScope.CLOSURE_PARAMS.getName()).findFirst().map(cp -> {
            org.codehaus.groovy.control.CompilationUnit cu = ((EclipseSourceUnit) unit).resolver.compilationUnit;
            try {
                @SuppressWarnings("unchecked") Class<? extends ClosureSignatureHint> hint = (Class<? extends ClosureSignatureHint>) StaticTypeCheckingSupport.evaluateExpression(GeneralUtils.castX(VariableScope.CLASS_CLASS_NODE, cp.getMember("value")), unit.getConfiguration(), cu.getTransformLoader());
                String[] opts = (String[]) (cp.getMember("options") == null ? ClosureParams.class.getMethod("options").getDefaultValue() : StaticTypeCheckingSupport.evaluateExpression(GeneralUtils.castX(VariableScope.STRING_CLASS_NODE.makeArray(), cp.getMember("options")), unit.getConfiguration(), cu.getTransformLoader()));

                // determine closure param types from ClosureSignatureHint
                List<ClassNode[]> sigs = hint.newInstance().getClosureSignatures(origin, unit, cu, opts, (Expression) call);
                if (sigs != null) {
                    List<ClassNode> parameterTypes = GroovyUtils.getParameterTypes(origin.getParameters());
                    for (ClassNode[] sig : sigs) {
                        if (sig.length == parameterTypes.size() && range(0, sig.length).allMatch(i -> GroovyUtils.isAssignable(sig[i], parameterTypes.get(i)))) {
                            return sig;
                        }
                    }
                }
            } catch (Exception | LinkageError e) {
                org.eclipse.jdt.internal.core.util.Util.log(e, "Error processing @ClosureParams of " + call.getMethodAsString());
            }
            return null;
        });
    }
}
