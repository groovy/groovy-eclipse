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
package org.codehaus.groovy.eclipse.core.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

public class STCTypeLookup implements ITypeLookup {

    // enabled for Groovy 2.0 or greater
    private static final boolean isEnabled = (CompilerUtils.getActiveGroovyBundle().getVersion().getMajor() >= 2);

    @Override
    public TypeLookupResult lookupType(final Expression expr, final VariableScope scope, final ClassNode objectExpressionType) {
        if (isEnabled) {
            boolean isGroovy = false;
            ASTNode declaration = expr;
            ClassNode declaringType = objectExpressionType;
            TypeConfidence confidence = TypeConfidence.INFERRED;
            Object inferredType = expr.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            if (inferredType == null && expr instanceof VariableExpression) {
                inferredType = expr.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
            }

            if (inferredType instanceof ClassNode) {
                if (expr instanceof ClassExpression) {
                    declaration = expr.getType();
                } else if (expr instanceof FieldExpression) {
                    declaration = ((FieldExpression) expr).getField();
                    assert ((FieldNode) declaration).getDeclaringClass().equals(declaringType);
                } else if (expr instanceof VariableExpression) {
                    VariableExpression vexp = (VariableExpression) expr;
                    if (vexp.isThisExpression() || vexp.isSuperExpression()) {
                        declaration = (ClassNode) inferredType;
                    } else {
                        Object methodTarget = vexp.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                        if (methodTarget instanceof MethodNode) {
                            declaration = (MethodNode) methodTarget;
                            declaringType = ((MethodNode) methodTarget).getDeclaringClass();
                        } else {
                            Variable accessedVariable = vexp.getAccessedVariable();
                            if (accessedVariable instanceof AnnotatedNode && !(vexp.getEnd() > 0 && // explicit reference to
                                    accessedVariable instanceof Parameter && ((Parameter) accessedVariable).getEnd() < 1)) { // implicit parameter
                                declaration = (AnnotatedNode) accessedVariable;
                                declaringType = ((AnnotatedNode) declaration).getDeclaringClass();

                                if (VariableScope.isPlainClosure((ClassNode) inferredType)) {
                                    VariableInfo info = scope.lookupName(accessedVariable.getName());
                                    if (info != null && VariableScope.isParameterizedClosure(info.type)) {
                                        inferredType = info.type; // Closure --> Closure<String>
                                    }
                                } else if (GroovyUtils.isAnonymous((ClassNode) inferredType)) {
                                    ClassNode type = (ClassNode) inferredType;
                                    // return extended/implemented type for anonymous inner class
                                    if (type.getUnresolvedSuperClass(false) != VariableScope.OBJECT_CLASS_NODE) {
                                        type = type.getUnresolvedSuperClass(false);
                                    } else {
                                        type = type.getUnresolvedInterfaces()[0];
                                    }
                                    inferredType = type;
                                }
                            } else {
                                // defer to other type lookup impls
                                confidence = TypeConfidence.UNKNOWN;
                            }
                        }
                    }
                } else if (expr instanceof MethodCall) {
                    Object methodTarget = declaration.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                    if (methodTarget instanceof MethodNode) {
                        declaration = (MethodNode) methodTarget;
                        declaringType = ((MethodNode) methodTarget).getDeclaringClass();
                    }
                }
            } else if (expr instanceof ConstantExpression) {
                ASTNode enclosingNode = scope.getEnclosingNode(), methodTarget = null;
                if (enclosingNode instanceof PropertyExpression && ((PropertyExpression) enclosingNode).getProperty() == expr) {
                    methodTarget = enclosingNode.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                    if (methodTarget instanceof MethodNode && ((MethodNode) methodTarget).isSynthetic()) {
                        declaringType = ((MethodNode) methodTarget).getOriginal().getDeclaringClass();
                        inferredType = enclosingNode.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
                        declaration  = declaringType.getProperty(expr.getText());
                        if (declaration != null) methodTarget = null;
                    }
                } else if (enclosingNode instanceof MethodCallExpression && ((MethodCallExpression) enclosingNode).getMethod() == expr) {
                    methodTarget = enclosingNode.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                    if (methodTarget == null) methodTarget = getMopMethodTarget((MethodCallExpression) enclosingNode);
                    if (methodTarget == null) methodTarget = ((MethodCallExpression) enclosingNode).getMethodTarget();
                    //
                } else if (enclosingNode instanceof MethodPointerExpression && ((MethodPointerExpression) enclosingNode).getMethodName() == expr) {
                    List<MethodNode> methods = enclosingNode.getNodeMetaData(MethodNode.class);
                    if (methods != null) {
                        methodTarget = methods.get(0);
                        if (methods.size() > 1) confidence = TypeConfidence.LOOSELY_INFERRED;
                    }
                }

                if (methodTarget instanceof ExtensionMethodNode) {
                    methodTarget = ((ExtensionMethodNode) methodTarget).getExtensionMethodNode();
                    isGroovy = true;
                }

                if (methodTarget instanceof MethodNode) {
                    declaration = ((MethodNode) methodTarget).getOriginal();
                    declaringType = ((MethodNode) declaration).getDeclaringClass();
                    if (!(enclosingNode instanceof MethodPointerExpression)) // ignore Closure<Type>
                        inferredType = enclosingNode.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
                    if (inferredType == null) inferredType = ((MethodNode) methodTarget).getReturnType();
                }
            }

            if (inferredType instanceof ClassNode) {
                if (declaringType != null) declaringType = declaringType.getPlainNodeReference();
                // compound assignment (+=, ?=, etc.) may involve separate declarations for read and write
                if (confidence.isAtLeast(TypeConfidence.INFERRED) && isCompoundAssignTarget(expr, scope)) {
                    if (declaration instanceof MethodNode) {
                        confidence = TypeConfidence.LOOSELY_INFERRED; // setter for write; field or property or accessor for read
                    }
                }

                TypeLookupResult result = new TypeLookupResult((ClassNode) inferredType, declaringType, declaration, confidence, scope);
                result.isGroovy = isGroovy;
                return result;
            }
        }
        return null;
    }

    @Override
    public TypeLookupResult lookupType(final FieldNode  node, final VariableScope scope) {
        if (isEnabled) {
            Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            if (inferredType instanceof ClassNode) {
                return new TypeLookupResult((ClassNode) inferredType, node.getDeclaringClass(), node, TypeConfidence.INFERRED, scope);
            }
        }
        return null;
    }

    @Override
    public TypeLookupResult lookupType(final MethodNode node, final VariableScope scope) {
        if (isEnabled) {
            Object inferredType = node.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
            if (inferredType instanceof ClassNode) {
                return new TypeLookupResult((ClassNode) inferredType, node.getDeclaringClass(), node, TypeConfidence.INFERRED, scope);
            }
        }
        return null;
    }

    @Override
    public TypeLookupResult lookupType(final Parameter param, final VariableScope scope) {
        if (isEnabled) {
            Object inferredType = param.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            if (inferredType instanceof ClassNode) {
                return new TypeLookupResult((ClassNode) inferredType, scope.getEnclosingTypeDeclaration(), param, TypeConfidence.INFERRED, scope);
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------

    /**
     * Resolves {@code this.super$2$method()} to {@code super.method()}
     *
     * @see MethodCallExpressionTransformer.transformToMopSuperCall
     */
    private static MethodNode getMopMethodTarget(final MethodCallExpression call) {
        if (call.isImplicitThis() && MopWriter.isMopMethod(call.getMethodAsString()) &&
                call.getObjectExpression().getNodeMetaData(ClassCodeVisitorSupport.ORIGINAL_EXPRESSION) != null) {
            Matcher m = Pattern.compile("super\\$(\\d+)\\$(.+)").matcher(call.getMethodAsString());
            if (m.matches()) {
                int dist = Integer.parseInt(m.group(1));
                List<ClassNode> types = new ArrayList<>();
                for (ClassNode next = call.getMethodTarget().getDeclaringClass(); next != null; next = next.getSuperClass()) {
                    types.add(next);
                }

                String name = m.group(2);
                ClassNode type = types.get(types.size() - dist);
                return type.getMethod(name, call.getMethodTarget().getParameters());
            }
        }
        return null;
    }

    private static boolean isCompoundAssignTarget(final Expression expr, final VariableScope scope) {
        boolean isAssignTarget = (scope.getWormhole().get("lhs") == expr || expr.getNodeMetaData("rhsType") != null);
        return (isAssignTarget && scope.getEnclosingAssignmentOperator().filter(op -> op.getType() != Types.EQUALS).isPresent());
    }
}
