/*
 * Copyright 2009-2022 the original author or authors.
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
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.unique;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.implementsTrait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import groovy.lang.Closure;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.StatementMeta;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

/**
 * Determines types using AST inspection.
 */
public class SimpleTypeLookup implements ITypeLookupExtension {

    protected GroovyCompilationUnit unit;

    @Override
    public void initialize(final GroovyCompilationUnit unit, final VariableScope topLevelScope) {
        this.unit = unit;
    }

    @Override
    public TypeLookupResult lookupType(final Expression node, final VariableScope scope, final ClassNode objectExpressionType, final boolean isStaticObjectExpression) {
        ClassNode declaringType = Optional.ofNullable(GroovyUtils.getWrapperTypeIfPrimitive(objectExpressionType)).orElseGet(() -> findDeclaringType(node, scope));
        boolean isPrimary = (objectExpressionType == null), isStatic = (isStaticObjectExpression || (isPrimary && scope.isStatic()));
        return findType(node, declaringType, scope, isPrimary, isStatic);
    }

    @Override
    public TypeLookupResult lookupType(final FieldNode node, final VariableScope scope) {
        return new TypeLookupResult(node.getType(), node.getDeclaringClass(), node, TypeConfidence.EXACT, scope);
    }

    @Override
    public TypeLookupResult lookupType(final MethodNode node, final VariableScope scope) {
        return new TypeLookupResult(node.getReturnType(), node.getDeclaringClass(), node, TypeConfidence.EXACT, scope);
    }

    @Override
    public TypeLookupResult lookupType(final ClassNode node, final VariableScope scope) {
        ClassNode type = node;
        if (node.getOuterClass() != null) {
            if (!node.isRedirectNode() && GroovyUtils.isAnonymous(node)) {
                // return extended/implemented type for anonymous inner class
                type = node.getUnresolvedSuperClass(false); if (type == VariableScope.OBJECT_CLASS_NODE) type = node.getInterfaces()[0];
            } else if (isTraitHelper(node)) {
                // return trait type for trait helper
                type = node.getOuterClass();
            }
        }
        return new TypeLookupResult(type, type, node, TypeConfidence.EXACT, scope);
    }

    @Override
    public TypeLookupResult lookupType(final Parameter node, final VariableScope scope) {
        // look up the name in the current scope to see if the type has been pre-determined (e.g. for loop variables)
        ClassNode type = Optional.ofNullable(scope.lookupNameInCurrentScope(node.getName())).map(info -> info.type).orElse(node.getType());
        return new TypeLookupResult(type, scope.getEnclosingTypeDeclaration(), node, TypeConfidence.EXACT, scope);
    }

    //--------------------------------------------------------------------------

    protected static ClassNode findDeclaringType(final Expression node, final VariableScope scope) {
        if (node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
            return node.getType();

        } else if (node instanceof FieldExpression) {
            return ((FieldExpression) node).getField().getDeclaringClass();

        } else if (node instanceof StaticMethodCallExpression) {
            return null; //((StaticMethodCallExpression) node).getOwnerType();

        } else if (node instanceof ConstantExpression && scope.isMethodCall()) {
            // method call without an object expression; requires same handling as a free variable
            ClassNode ownerType;
            if (scope.getEnclosingClosure() != null) {
                ownerType = getBaseDeclaringType(scope.getOwner());
            } else {
                ownerType = scope.getEnclosingTypeDeclaration();
            }
            return ownerType;

        } else if (node instanceof VariableExpression) {
            Variable var = ((VariableExpression) node).getAccessedVariable();
            if (var != null && !(var instanceof Parameter || var instanceof VariableExpression)) {
                ClassNode ownerType;
                if (scope.getEnclosingClosure() != null) {
                    ownerType = getBaseDeclaringType(scope.getOwner());
                } else {
                    ownerType = scope.getEnclosingTypeDeclaration();
                }
                return ownerType;
            }
            // else local variable
        }
        return VariableScope.OBJECT_CLASS_NODE;
    }

    protected TypeLookupResult findType(final Expression node, final ClassNode declaringType, final VariableScope scope, final boolean isPrimaryExpression, final boolean isStaticObjectExpression) {

        ClassNode nodeType = node.getType();
        TypeConfidence confidence = TypeConfidence.EXACT;

        MethodNode target; // use value from node metadata if it's available
        if (scope.isMethodCall() && (target = getMethodTarget(node)) != null) {
            return new TypeLookupResult(target.getReturnType(), target.getDeclaringClass(), target, confidence, scope);
        }

        if (node instanceof VariableExpression) {
            return findTypeForVariable((VariableExpression) node, scope, declaringType);

        } else if (node instanceof ConstantExpression) {
            if (isPrimaryExpression) {
                if (scope.isMethodCall()) { // handle method call without object expression like a free variable
                    VariableExpression call = new VariableExpression(new DynamicVariable(node.getText(), false));
                    TypeLookupResult result = findTypeForVariable(call, scope, declaringType);
                    if (isCompatible((AnnotatedNode) result.declaration, isStaticObjectExpression)) {
                        return result;
                    }
                    if (isStaticObjectExpression) { // might be reference to a method defined on java.lang.Class
                        return findTypeForVariable(call, scope, VariableScope.newClassClassNode(declaringType));
                    }
                }
            } else {
                // handle method or property with "owner" object expression like a free variable in the outer scope
                VariableScope outer = declaringType.getNodeMetaData("outer.scope");
                if (outer != null) {
                    try {
                        outer.setMethodCallArgumentTypes(scope.getMethodCallArgumentTypes());
                        return findTypeForVariable(new VariableExpression(new DynamicVariable(node.getText(), false)), outer, declaringType);
                    } finally {
                        outer.setMethodCallArgumentTypes(null);
                    }
                }

                // short-circuit if expression is direct field access (aka AttributeExpression)
                if (scope.getEnclosingNode() instanceof AttributeExpression) {
                    ClassNode clazz = !isStaticObjectExpression ? declaringType : declaringType.getGenericsTypes()[0].getType();
                    FieldNode field = null;
                    if (!isSuperObjectExpression(scope)) {
                        field = clazz.getDeclaredField(node.getText());
                        // no access checks; even private is allowed
                        clazz = clazz.getSuperClass();
                    }
                    while (field == null && clazz != null) {
                        field = clazz.getDeclaredField(node.getText());
                        if (field != null && (field.isPrivate() || (!field.isPublic() && !field.isProtected() &&
                                !Objects.equals(clazz.getPackage(), scope.getEnclosingTypeDeclaration().getPackage())))) {
                            field = null; // field is inaccessible; continue searching for accessible field
                        }
                        clazz = clazz.getSuperClass();
                    }

                    if (isCompatible(field, isStaticObjectExpression)) {
                        return new TypeLookupResult(field.getType(), field.getDeclaringClass(), field, confidence, scope);
                    } else {
                        return new TypeLookupResult(VariableScope.VOID_CLASS_NODE, null, null, TypeConfidence.UNKNOWN, scope);
                    }
                }

                if ("new".equals(node.getText()) && isStaticObjectExpression && isStaticReferenceToInstanceMethod(scope)) {
                    return new TypeLookupResult(declaringType.getGenericsTypes()[0].getType(), null, node, confidence, scope);
                }

                boolean isLhsExpression = (scope.getWormhole().remove("lhs") == node);
                return findTypeForNameWithKnownObjectExpression(node.getText(), nodeType, declaringType, scope, isLhsExpression, isStaticObjectExpression);
            }

            ConstantExpression cexp = (ConstantExpression) node;
            if (cexp.isNullExpression()) {
                return new TypeLookupResult(VariableScope.NULL_TYPE, null, null, confidence, scope);
            } else if (cexp.isTrueExpression() || cexp.isFalseExpression()) {
                return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);
            } else if (cexp.isEmptyStringExpression() || VariableScope.STRING_CLASS_NODE.equals(nodeType)) {
                return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, node, confidence, scope);
            } else if (ClassHelper.isNumberType(nodeType) || VariableScope.BIG_DECIMAL_CLASS.equals(nodeType) || VariableScope.BIG_INTEGER_CLASS.equals(nodeType)) {
                return new TypeLookupResult(GroovyUtils.getWrapperTypeIfPrimitive(nodeType), null, null, confidence, scope);
            } else {
                return new TypeLookupResult(nodeType, null, null, TypeConfidence.UNKNOWN, scope);
            }

        } else if (node instanceof ConstructorCallExpression) {
            ConstructorCallExpression call = (ConstructorCallExpression) node;
            ClassNode resolvedDeclaringType = declaringType;
            if (call.isSpecialCall()) {
                nodeType = VariableScope.VOID_CLASS_NODE;
                resolvedDeclaringType = scope.getEnclosingMethodDeclaration().getDeclaringClass();
                if (call.isSuperCall()) resolvedDeclaringType = resolvedDeclaringType.getUnresolvedSuperClass(false);
            } else if (call.isUsingAnonymousInnerClass()) {
                nodeType = lookupType(call.getType(), scope).type;
                // resolve declaring type of the referenced constructor
                resolvedDeclaringType = resolvedDeclaringType.getUnresolvedSuperClass(false);
            }

            ASTNode declaration;
            // try to find best match if there is more than one constructor to choose from
            List<ConstructorNode> declaredConstructors = resolvedDeclaringType.getDeclaredConstructors();
            if (declaredConstructors.size() > 1) {
                List<ClassNode> callTypes = scope.getMethodCallArgumentTypes();
                if (callTypes != null && !callTypes.isEmpty()) {
                    // non-static inner types have extra argument for instance of enclosing type
                    if (callTypes.get(0).equals(declaringType.getOuterClass()) &&
                            (call.isUsingAnonymousInnerClass() ? !scope.isStatic() : !Flags.isStatic(declaringType.getModifiers()))) {
                        callTypes.remove(0);
                    }
                }
                declaration = findMethodDeclaration0(declaredConstructors, callTypes, false);
            } else {
                declaration = (!declaredConstructors.isEmpty() ? declaredConstructors.get(0) : resolvedDeclaringType);
            }

            return new TypeLookupResult(nodeType, resolvedDeclaringType, declaration, confidence, scope);

        } else if (node instanceof StaticMethodCallExpression) {
            List<MethodNode> candidates = new ArrayList<>(12);
            String methodName = ((StaticMethodCallExpression) node).getMethod();
            ClassNode theType = ((StaticMethodCallExpression) node).getOwnerType();
            if (theType.isAbstract() || theType.isInterface() || implementsTrait(theType)) {
                Set<ClassNode> hierarchy = new LinkedHashSet<>();
                VariableScope.createTypeHierarchy(theType, hierarchy, false);
                for (ClassNode type : hierarchy) {
                    for (MethodNode candidate : type.getDeclaredMethods(methodName)) {
                        if (candidate.isStatic()) candidates.add(candidate);
                    }
                }
            } else {
                for (ClassNode type = theType; type != null && type != VariableScope.OBJECT_CLASS_NODE; type = type.getSuperClass()) {
                    for (MethodNode candidate : type.getDeclaredMethods(methodName)) {
                        if (candidate.isStatic()) candidates.add(candidate);
                    }
                }
            }

            MethodNode closestMatch = findMethodDeclaration0(candidates, scope.getMethodCallArgumentTypes(), true);
            return new TypeLookupResult(closestMatch.getReturnType(), closestMatch.getDeclaringClass(), closestMatch, TypeConfidence.INFERRED, scope);

        } else if (node instanceof ClosureExpression) {
            if (VariableScope.isPlainClosure(nodeType)) {
                ClassNode returnType = node.getNodeMetaData("returnType");
                if (returnType != null && !VariableScope.isVoidOrObject(returnType))
                    GroovyUtils.updateClosureWithInferredTypes(nodeType, returnType, ((ClosureExpression) node).getParameters());
            }
            return new TypeLookupResult(nodeType, null, node, confidence, scope);

        } else if (node instanceof BooleanExpression) {
            return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);

        } else if (node instanceof GStringExpression) {
            return new TypeLookupResult(VariableScope.GSTRING_CLASS_NODE, null, null, confidence, scope);

        } else if (node instanceof ArrayExpression || node instanceof CastExpression) {
            return new TypeLookupResult(nodeType, null, null, confidence, scope);

        } else if (node instanceof ClassExpression) {
            ClassNode classType = VariableScope.newClassClassNode(nodeType);
            return new TypeLookupResult(classType, null, nodeType, confidence, scope);

        } else if (node instanceof EmptyExpression) {
            return new TypeLookupResult(null, null, null, confidence, scope);

        } else if (node instanceof BitwiseNegationExpression) {
            ClassNode type = ((BitwiseNegationExpression) node).getExpression().getType();
            // check for ~/.../ (a.k.a. Pattern literal)
            if (VariableScope.STRING_CLASS_NODE.equals(type)) {
                type = VariableScope.PATTERN_CLASS_NODE;
            }
            return new TypeLookupResult(type, null, null, confidence, scope);
        }

        if (VariableScope.OBJECT_CLASS_NODE.equals(nodeType)) {
            confidence = TypeConfidence.UNKNOWN;
        }

        return new TypeLookupResult(nodeType, declaringType, null, confidence, scope);
    }

    protected TypeLookupResult findTypeForNameWithKnownObjectExpression(final String name, final ClassNode type, final ClassNode declaringType, final VariableScope scope, final boolean isLhsExpression, final boolean isStaticObjectExpression) {

        TypeConfidence confidence = TypeConfidence.EXACT;
        int fieldAccessPolicy = (!scope.isFieldAccessDirect() || !(isThisObjectExpression(scope) || isSuperObjectExpression(scope)) ? 0 : isThisObjectExpression(scope) ? 1 : 2);
        // GRECLIPSE-1544: "Type.staticMethod()" or "def type = Type.class; type.staticMethod()" or ".&" variations; StatementAndExpressionCompletionProcessor circa line 275 has similar check for proposals
        ClassNode declaring = isStaticObjectExpression && (!Traits.isTrait(getBaseDeclaringType(declaringType)) || "$static$self".equals(getObjectExpression(scope).getText())) ? getBaseDeclaringType(declaringType) : declaringType;
        ASTNode declaration = findDeclaration(name, declaring, isLhsExpression, isStaticObjectExpression, fieldAccessPolicy, scope.getEnclosingNode() instanceof MethodPointerExpression ? UNKNOWN_TYPES : scope.getMethodCallArgumentTypes());
        if (declaration instanceof MethodNode && scope.getEnclosingNode() instanceof PropertyExpression && !scope.isMethodCall() &&
                (!AccessorSupport.isGetter((MethodNode) declaration) || name.equals(((MethodNode) declaration).getName()))) {
            declaration = null; // property expression "foo.bar" does not resolve to "bar(...)" or "setBar(x)" w/o call args
        }

        if (declaration == null && declaring != declaringType && (
                !VariableScope.CLASS_CLASS_NODE.equals(declaringType) ||
                !(scope.getEnclosingNode() instanceof MethodPointerExpression) ||
                GroovyUtils.getGroovyVersion().getMajor() >= 4)) { // GROOVY-8633, GROOVY-10057
            // "Type.getPackage()" or "def type = Type.class; type.getPackage()" or "Type.&getPackage"
            return findTypeForNameWithKnownObjectExpression(name, type, declaringType, scope, isLhsExpression, false);
        }

        ClassNode resolvedType, resolvedDeclaringType;
        if (declaration != null) {
            resolvedType = getTypeFromDeclaration(declaration);
            resolvedDeclaringType = getDeclaringTypeFromDeclaration(declaration, declaringType);
        } else if ("call".equals(name)) {
            // assume that this is a synthetic call method for calling a closure
            resolvedType = VariableScope.OBJECT_CLASS_NODE;
            resolvedDeclaringType = VariableScope.CLOSURE_CLASS_NODE;
            declaration = resolvedDeclaringType.getMethods("call").get(0);
        } else if ("this".equals(name) && declaringType.equals(VariableScope.CLASS_CLASS_NODE)) {
            // "Type.this" (aka ClassExpression.ConstantExpression) within inner class
            declaration = resolvedType = resolvedDeclaringType = declaringType.getGenericsTypes()[0].getType();
        } else {
            resolvedType = VariableScope.OBJECT_CLASS_NODE;
            resolvedDeclaringType = declaringType;
            confidence = TypeConfidence.UNKNOWN;
        }

        if (declaration != null) {
            if (!resolvedDeclaringType.equals(VariableScope.CLASS_CLASS_NODE) && !resolvedDeclaringType.equals(VariableScope.OBJECT_CLASS_NODE)) {
                // check to see if the object expression is static but the declaration is not -- and some other conditions
                if (declaration instanceof FieldNode) {
                    FieldNode field = (FieldNode) declaration;
                    if (isStaticObjectExpression && !field.isStatic()) {
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (field.isPrivate()) {
                        // "super.field" reference to private field yields MissingPropertyException
                        if (isSuperObjectExpression(scope)) {
                            confidence = TypeConfidence.UNKNOWN;
                        // "this.field" reference to private field of super class yields MissingPropertyException
                        } else if (isThisObjectExpression(scope) && isNotThisOrOuterClass(declaring, resolvedDeclaringType)) {
                            confidence = TypeConfidence.UNKNOWN;
                        }
                    }
                } else if (declaration instanceof PropertyNode) {
                    PropertyNode property = (PropertyNode) declaration;
                    FieldNode underlyingField = property.getField(); // prefer looking at the underlying field
                    if (isStaticObjectExpression && !(underlyingField != null ? underlyingField.isStatic() : property.isStatic())) {
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (property.isSynthetic()) {
                        confidence = isLhsExpression || !(scope.isMethodCall() || scope.getEnclosingNode() instanceof MethodPointerExpression) ? TypeConfidence.INFERRED : TypeConfidence.LOOSELY_INFERRED;
                    }
                } else if (declaration instanceof MethodNode) {
                    MethodNode method = (MethodNode) declaration;
                    if (isStaticObjectExpression && !method.isStatic() && !isStaticReferenceToInstanceMethod(scope)) {
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (method.isPrivate()) {
                        // "super.method()" reference to private method yields MissingMethodException
                        if (isSuperObjectExpression(scope)) {
                            if (scope.getEnclosingNode() instanceof MethodPointerExpression || // GROOVY-8999
                                                GroovyUtils.getGroovyVersion().getMajor() > 3) // GROOVY-9851
                                confidence = TypeConfidence.UNKNOWN;
                        // "this.method()" reference to private method of super class yields MissingMethodException
                        } else if (isThisObjectExpression(scope) && isNotThisOrOuterClass(declaring, resolvedDeclaringType)) {
                            confidence = TypeConfidence.UNKNOWN;
                        }
                    } else if (method.getName().startsWith("is") && !name.startsWith("is") && !scope.isMethodCall() && isSuperObjectExpression(scope) && GroovyUtils.getGroovyVersion().getMajor() < 4) {
                        // GROOVY-1736, GROOVY-6097: "super.name" => "super.getName()" in AsmClassGenerator
                        String newName = "get" + MetaClassHelper.capitalize(name);
                        scope.setMethodCallArgumentTypes(Collections.emptyList());
                        return findTypeForNameWithKnownObjectExpression(newName, type, declaringType, scope, isLhsExpression, isStaticObjectExpression);
                    } else if (isLooseMatch(scope.getMethodCallArgumentTypes(), method.getParameters()) &&
                            !(isStaticObjectExpression && isStaticReferenceToUnambiguousMethod(scope, name, declaringType)) &&
                            !(AccessorSupport.isGetter(method) && !scope.isMethodCall() && scope.getEnclosingNode() instanceof PropertyExpression)) {
                        // if arguments and parameters are mismatched, a category method may make a better match
                        confidence = TypeConfidence.LOOSELY_INFERRED;
                    }
                    if (isTraitHelper(resolvedDeclaringType) && method.getOriginal() != method) {
                        resolvedDeclaringType = method.getOriginal().getDeclaringClass();
                        declaration = method.getOriginal(); // the trait method
                    }
                }
                // compound assignment (i.e., +=, &=, ?=, etc.) may involve separate declarations for read and write
                if (confidence.isAtLeast(TypeConfidence.INFERRED) && isLhsExpression && isCompoundAssignment(scope)) {
                    if (declaration instanceof MethodNode) {
                        confidence = TypeConfidence.LOOSELY_INFERRED; // setter for write; field or property or accessor for read
                    } else if (findPropertyAccessorMethod(name, declaringType, false, isStaticObjectExpression, null).filter(getter ->
                        !isSynthetic(getter) && !(fieldAccessPolicy != 0 && declaringType.equals(getter.getDeclaringClass()))
                    ).isPresent()) {
                        confidence = TypeConfidence.LOOSELY_INFERRED; // field or property for write; accessor for read
                    }
                }
            } else if (resolvedDeclaringType.equals(VariableScope.CLASS_CLASS_NODE) && declaration instanceof MethodNode) {
                // beware of matching Class methods too aggressively; Arrays.toString(Object[]) vs. Class.toString()
                MethodNode classMethod = (MethodNode) declaration;
                if (isStaticObjectExpression && !classMethod.isStatic()) {
                    List<ClassNode> argumentTypes = scope.getMethodCallArgumentTypes();
                    if (argumentTypes == null && !name.equals(classMethod.getName()) && !isLhsExpression) {
                        argumentTypes = Collections.EMPTY_LIST; // assume getter; Class has only one setter
                    }
                    if (isLooseMatch(argumentTypes, ((MethodNode) declaration).getParameters())) {
                        confidence = TypeConfidence.UNKNOWN;
                    }
                }
            }
        }

        return new TypeLookupResult(resolvedType, resolvedDeclaringType, declaration, confidence, scope);
    }

    protected TypeLookupResult findTypeForVariable(final VariableExpression var, final VariableScope scope, final ClassNode declaringType) {
        ASTNode decl = var;
        ClassNode type = var.getType();
        ClassNode resolvedDeclaringType = declaringType;
        TypeConfidence confidence = TypeConfidence.EXACT;
        Variable accessedVar = var.getAccessedVariable();
        VariableInfo variableInfo = scope.lookupName(var.getName());
        int resolveStrategy = scope.getEnclosingClosureResolveStrategy();
        boolean isAssignTarget = (scope.getWormhole().get("lhs") == var);
        boolean isDirectAccess = (accessedVar instanceof AnnotatedNode &&
            declaringType.equals(((AnnotatedNode) accessedVar).getDeclaringClass()));

        if ((accessedVar instanceof FieldNode && !(isDirectAccess && scope.isFieldAccessDirect())) ||
                (accessedVar instanceof PropertyNode && isOrImplements(declaringType,VariableScope.MAP_CLASS_NODE)) ||
                (isDirectAccess && resolveStrategy != Closure.OWNER_FIRST && resolveStrategy != Closure.OWNER_ONLY)) {
            // accessed variable was found using direct search; forget the reference
            accessedVar = new DynamicVariable(var.getName(), scope.isStatic());
        } else if (accessedVar instanceof Parameter && ((Parameter) accessedVar).getEnd() < 1 && var.getEnd() > 0 && // explicit reference to implicit parameter
                variableInfo != null && variableInfo.scopeNode instanceof ConstructorNode) {
            // could be field reference from pre- or post-condition block (incl. record compact constructor)
            accessedVar = ((MethodNode) variableInfo.scopeNode).getDeclaringClass().getField(var.getName());
        }

        if (accessedVar instanceof ASTNode) {
            decl = (ASTNode) accessedVar;
            if (decl instanceof FieldNode ||
                decl instanceof MethodNode ||
                decl instanceof PropertyNode) {

                if (decl instanceof PropertyNode) {
                    PropertyNode prop = (PropertyNode) decl; // check for pseudo-property
                    if (prop.isDynamicTyped() && prop.getField().hasNoRealSourcePosition()) {
                        Optional<MethodNode> accessor = findPropertyAccessorMethod(prop.getName(),
                            declaringType, isAssignTarget, prop.isStatic(), scope.getMethodCallArgumentTypes());
                        decl = accessor.map(meth -> (ASTNode) meth).orElse(decl);
                    }
                }

                type = getTypeFromDeclaration(decl);
                resolvedDeclaringType = ((AnnotatedNode) decl).getDeclaringClass();
                if (decl instanceof MethodNode || !((Variable) decl).isDynamicTyped()) variableInfo = null;
            }
        } else if (accessedVar instanceof DynamicVariable) {
            ASTNode candidate = findDeclarationForDynamicVariable(var, declaringType, scope, isAssignTarget, resolveStrategy);
            if (candidate != null && (!(candidate instanceof MethodNode) || scope.isMethodCall() ||
                    ((AccessorSupport.isGetter((MethodNode) candidate) || AccessorSupport.isSetter((MethodNode) candidate)) && !var.getName().equals(((MethodNode) candidate).getName())))) {
                ClassNode implicitThisType = VariableScope.CLOSURE_CLASS_NODE.equals(declaringType) ? scope.getEnclosingTypeDeclaration() : declaringType;
                if (candidate instanceof FieldNode) {
                    FieldNode field = (FieldNode) candidate;
                    ClassNode owner = field.getDeclaringClass();
                    if (field.getName().contains("__") && implementsTrait(owner)) {
                        candidate = findTraitField(field.getName(), owner).orElse(field);
                    } else if (field.isPrivate() && isNotThisOrOuterClass(implicitThisType, owner)) {
                        confidence = TypeConfidence.UNKNOWN; // reference to private field of super class yields MissingPropertyException
                    }
                } else if (candidate instanceof MethodNode) {
                    MethodNode method = (MethodNode) candidate;
                    // check for call "method(1,2,3)" matched to decl "method(int)"
                    List<ClassNode> argumentTypes = scope.getMethodCallArgumentTypes();
                    if (argumentTypes != null && isLooseMatch(argumentTypes, method.getParameters())) {
                        confidence = TypeConfidence.LOOSELY_INFERRED;
                    }
                    if (method.isPrivate() && isNotThisOrOuterClass(implicitThisType, method.getDeclaringClass())) {
                        confidence = TypeConfidence.UNKNOWN; // reference to private method of super class yields MissingMethodException
                    }
                } else if (candidate instanceof PropertyNode) {
                    if (((PropertyNode) candidate).isSynthetic()) {
                        confidence = isAssignTarget || !scope.isMethodCall() ? TypeConfidence.INFERRED : TypeConfidence.LOOSELY_INFERRED;
                    }
                }
                // compound assignment (i.e., +=, &=, ?=, etc.) may involve separate declarations for read and write
                if (confidence.isAtLeast(TypeConfidence.INFERRED) && isAssignTarget && isCompoundAssignment(scope) &&
                        (candidate instanceof MethodNode || !candidate.equals(findDeclarationForDynamicVariable(var, declaringType, scope, false, resolveStrategy)))) {
                    confidence = TypeConfidence.LOOSELY_INFERRED;
                }

                decl = candidate;
                type = getTypeFromDeclaration(decl);
                resolvedDeclaringType = getDeclaringTypeFromDeclaration(decl, declaringType);
                if (!VariableScope.CLOSURE_CLASS_NODE.equals(resolvedDeclaringType)) variableInfo = null;
            } else {
                type = VariableScope.OBJECT_CLASS_NODE;
                confidence = TypeConfidence.UNKNOWN;
                // dynamic variables are not allowed outside of script mainline
                if (variableInfo != null && !scope.inScriptRunMethod()) variableInfo = null;
            }
        }

        if (variableInfo != null) {
            type = Optional.ofNullable(variableInfo.type).orElseGet(ClassHelper::dynamicType);
            resolvedDeclaringType = getMorePreciseType(declaringType, variableInfo);
            if (VariableScope.isThisOrSuper(var)) decl = type;
            confidence = TypeConfidence.INFERRED;
        }

        return new TypeLookupResult(type, resolvedDeclaringType, decl, confidence, scope);
    }

    protected ASTNode findDeclarationForDynamicVariable(final VariableExpression var, final ClassNode owner, final VariableScope scope, final boolean isAssignTarget, final int resolveStrategy) {
        ASTNode candidate = null;
        List<ClassNode> callArgs = scope.getMethodCallArgumentTypes();

        if (resolveStrategy == Closure.DELEGATE_FIRST || resolveStrategy == Closure.DELEGATE_ONLY) {
            // TODO: If strategy is DELEGATE_ONLY and delegate is enclosing closure, do outer search.
            candidate = findDeclaration(var.getName(), scope.getDelegate(), isAssignTarget, false, 0, callArgs);
        }
        if (candidate == null && resolveStrategy < Closure.DELEGATE_ONLY) {
            VariableScope outer = owner.getNodeMetaData("outer.scope");
            if (outer != null) { // owner is an enclosing closure
                try {
                    outer.setMethodCallArgumentTypes(callArgs);
                    candidate = findDeclarationForDynamicVariable(var, getBaseDeclaringType(outer.getOwner()), outer, isAssignTarget, outer.getEnclosingClosureResolveStrategy());
                } finally {
                    outer.setMethodCallArgumentTypes(null);
                }
            } else {
                candidate = findDeclaration(var.getName(), owner, isAssignTarget, scope.isOwnerStatic(), scope.isFieldAccessDirect() ? 1 : 0, callArgs);
            }
            if (candidate == null && resolveStrategy < Closure.DELEGATE_FIRST && scope.getEnclosingClosure() != null) {
                candidate = findDeclaration(var.getName(), scope.getDelegate(), isAssignTarget, false, 0, callArgs);
            }
            if (candidate == null && scope.getEnclosingClosure() == null && scope.getEnclosingMethodDeclaration() != null) {
                for (Parameter parameter : scope.getEnclosingMethodDeclaration().getParameters()) {
                    if (parameter.getName().equals(var.getName())) {
                        candidate = parameter;
                        break;
                    }
                }
            }
        }
        if (candidate == null && resolveStrategy <= Closure.TO_SELF && (resolveStrategy > 0 || scope.getEnclosingClosure() != null)) {
            candidate = findDeclaration(var.getName(), VariableScope.CLOSURE_CLASS_NODE, isAssignTarget, false, 0, callArgs);
        }

        return candidate;
    }

    /**
     * Looks for the named member in the declaring type. Also searches super types.
     * The result can be a field, method, or property.
     *
     * @param name the name of the field, method, constant or property to find
     * @param declaringType the type in which the named member's declaration resides
     * @param isLhsExpression {@code true} if named member is being assigned a value
     * @param isStaticExpression {@code true} if member is being accessed statically
     * @param directFieldAccess {@code 1}: access fields from {@code declaringType} directly;
     *                          {@code 2}: access non-private fields from {@code declaringType} directly;
     *                          {@code 0}: prefer accessor methods over fields/properties of {@code declaringType}
     * @param methodCallArgumentTypes types of arguments to the associated method call (or {@code null} if not a method call)
     */
    protected ASTNode findDeclaration(final String name, final ClassNode declaringType, final boolean isLhsExpression, final boolean isStaticExpression, final int directFieldAccess, final List<ClassNode> methodCallArgumentTypes) {
        if (declaringType.isArray()) {
            // only length exists on arrays
            if ("length".equals(name)) {
                return createLengthField(declaringType);
            }
            // otherwise search on object
            return findDeclaration(name, VariableScope.OBJECT_CLASS_NODE, isLhsExpression, isStaticExpression, 0, methodCallArgumentTypes);
        }

        boolean isCallExpression = (!isLhsExpression && methodCallArgumentTypes != null);

        if (isCallExpression) {
            if (!isStaticExpression && declaringType.implementsInterface(ClassHelper.GROOVY_INTERCEPTABLE_TYPE)) {
                return declaringType.getMethod("invokeMethod", new Parameter[] {new Parameter(VariableScope.STRING_CLASS_NODE, "name"), new Parameter(VariableScope.OBJECT_CLASS_NODE, "args")});
            }

            MethodNode method = findMethodDeclaration(name, declaringType, methodCallArgumentTypes, isStaticExpression);
            if (isCompatible(method, isStaticExpression)) {
                return method;
            }
            // name may still map to something that is callable; keep looking
        }

        boolean dynamicProperty = (!isCallExpression && !isStaticExpression && isOrImplements(declaringType, VariableScope.MAP_CLASS_NODE));

        if (dynamicProperty && directFieldAccess == 0 && !isLhsExpression) { // GROOVY-5491
            return createDynamicProperty(name, getMapPropertyType(declaringType), declaringType, isStaticExpression);
        }

        // look for canonical accessor method
        Optional<MethodNode> accessor = findPropertyAccessorMethod(name, declaringType, isLhsExpression, isStaticExpression, methodCallArgumentTypes).filter(it -> !isSynthetic(it));
        boolean nonPrivateAccessor = accessor.filter(it -> !it.isPrivate() || declaringType.equals(it.getDeclaringClass())).isPresent();
        if (nonPrivateAccessor && directFieldAccess == 0) {
            return accessor.get();
        }

        Set<ClassNode> typeHierarchy = new LinkedHashSet<>();
        VariableScope.createTypeHierarchy(declaringType, typeHierarchy, true);

        // look for property
        for (ClassNode type : typeHierarchy) {
            PropertyNode property = type.getProperty(name);
            if (property == null && implementsTrait(declaringType) && Traits.isTrait(type)) {
                property = Optional.ofNullable(type.redirect().<List<PropertyNode>>getNodeMetaData("trait.properties"))
                    .flatMap(list -> list.stream().filter(prop -> prop.getName().equals(name)).findFirst()).orElse(null);
            }
            if (isCompatible(property, isStaticExpression) &&
                (!isLhsExpression || !Flags.isFinal(property.getModifiers())) && // GROOVY-8065
                (!(accessor.isPresent() || (dynamicProperty && !isLhsExpression)) || // GROOVY-5491
                    directFieldAccess == 1 && declaringType.equals(property.getDeclaringClass()))) {
                return property;
            }
            if (property != null) break;
        }

        // look for field
        FieldNode field = declaringType.getField(name);
        if (isCompatible(field, isStaticExpression) &&
                !(Flags.isSynthetic(field.getModifiers()) && field.getType().equals(ClassHelper.REFERENCE_TYPE)) &&
                // no indirect, non-private accessor (map get or put if no non-final public/protected field exists)
                (!(nonPrivateAccessor || (dynamicProperty && !(isLhsExpression && !field.isFinal() && (field.isPublic()||field.isProtected())))) ||
                    directFieldAccess >= 1 && declaringType.equals(field.getDeclaringClass()) && (directFieldAccess == 1 || !field.isPrivate()))) {
            return field;
        }

        if (dynamicProperty && !(isLhsExpression && nonPrivateAccessor)) { // GROOVY-5491
            return createDynamicProperty(name, getMapPropertyType(declaringType), declaringType, isStaticExpression);
        }

        if (accessor.isPresent()) {
            return accessor.get();
        }

        // look for constant in interfaces
        for (ClassNode type : typeHierarchy) {
            if (type.isInterface() && type != declaringType) {
                field = type.getDeclaredField(name);
                if (field != null && field.isFinal() && field.isStatic()) {
                    return field;
                }
            }
        }

        if (!declaringType.equals(VariableScope.CLASS_CLASS_NODE) && !declaringType.equals(VariableScope.OBJECT_CLASS_NODE) && !declaringType.equals(ClassHelper.SCRIPT_TYPE)) {
            Optional<MethodNode> mopMethod = findMetaObjectMethods(declaringType, isLhsExpression, isStaticExpression, methodCallArgumentTypes).filter(mm -> {
                if (isSynthetic(mm)) return false;
                Parameter[] p = mm.getParameters();
                if (mm.getName().startsWith("g")) {
                    return p.length == 1 && p[0].getType().equals(VariableScope.STRING_CLASS_NODE);
                } else if (!mm.getName().endsWith("yMissing")) {
                    return p.length == 2 && p[0].getType().equals(VariableScope.STRING_CLASS_NODE) && p[1].getType().equals(VariableScope.OBJECT_CLASS_NODE);
                } else {
                    return p.length == 1; // propertyMissing and $static_propertyMissing have relaxed requirements
                }
            }).findFirst();
            if (mopMethod.isPresent()) {
                return mopMethod.map(mm -> !isCallExpression ? createDynamicProperty(name, VariableScope.OBJECT_CLASS_NODE, declaringType, isStaticExpression) : mm).get();
            }
        }

        // look for member in outer classes
        for (ClassNode type = getBaseDeclaringType(declaringType); type != null; type = type.getSuperClass()) {
            if (type.getOuterClass() != null) {
                // search only for static declarations if inner class is static
                boolean isStatic = (isStaticExpression || Flags.isStatic(type.getModifiers()));
                ASTNode declaration = findDeclaration(name, type.getOuterClass(), isLhsExpression, isStatic, 0, methodCallArgumentTypes);
                if (declaration != null) {
                    return declaration;
                }
            }
        }

        if (!isCallExpression && !isStaticExpression && isOrImplements(declaringType, ClassHelper.METACLASS_TYPE)) {
            try {
                if ("constructor".equals(name)) {
                    return createDynamicProperty(name, ClassHelper.make(Class.forName("groovy.lang.ExpandoMetaClass$ExpandoMetaConstructor")), declaringType, false);
                } else if ("static".equals(name)) {
                    return createDynamicProperty(name, ClassHelper.make(Class.forName("groovy.lang.ExpandoMetaClass$ExpandoMetaProperty")), declaringType, false);
                }
            } catch (Exception ignore) {
            }
        }

        if (methodCallArgumentTypes == null || methodCallArgumentTypes == UNKNOWN_TYPES) {
            // reference may be in static import or method pointer; look for method as last resort
            return findMethodDeclaration(name, declaringType, methodCallArgumentTypes, isStaticExpression);
        }

        return null;
    }

    /**
     * Looks for a method with the given name in the declaring type that is
     * suitable for the given argument types and static/non-static context.
     */
    protected MethodNode findMethodDeclaration(final String name, final ClassNode declaringType, final List<ClassNode> argumentTypes, final boolean isStaticExpression) {
        Set<ClassNode> interfaces = new LinkedHashSet<>();
        VariableScope.findAllInterfaces(declaringType, interfaces, false);

        // concrete types (without mixins/traits) return all methods from getMethods(String), except interface default and transform generated methods
        if (!declaringType.isAbstract() && !declaringType.isInterface() && !implementsTrait(declaringType)) {
            List<MethodNode> candidates = getMethods(name, declaringType);
            for (ClassNode face : interfaces) {
                for (MethodNode method : face.getDeclaredMethods(name)) {
                    if (method.isDefault()) candidates.add(method);
                }
            }
            return candidates.isEmpty() ? null
                : findMethodDeclaration0(candidates, argumentTypes, isStaticExpression);
        }

        Set<ClassNode> types = new LinkedHashSet<>();
        types.add(declaringType);
        types.addAll(interfaces);
        if (!implementsTrait(declaringType))
            types.add(VariableScope.OBJECT_CLASS_NODE); // implicit super type

        MethodNode outerCandidate = null;
        for (ClassNode type : types) {
            MethodNode innerCandidate = null;
            List<MethodNode> candidates = getMethods(name, type);
            if (!candidates.isEmpty()) {
                innerCandidate = findMethodDeclaration0(candidates, argumentTypes, isStaticExpression);
                if (innerCandidate != null) {
                    if (isTraitBridge(innerCandidate)) {
                        continue;
                    }
                    if (outerCandidate == null) {
                        outerCandidate = innerCandidate;
                    }
                }
            }
            if (innerCandidate != null && argumentTypes != null) {
                Parameter[] methodParameters = innerCandidate.getParameters();
                if (argumentTypes.isEmpty() && methodParameters.length == 0) {
                    return innerCandidate;
                }
                if (argumentTypes.size() == methodParameters.length) {
                    outerCandidate = closer(innerCandidate, outerCandidate, argumentTypes);

                    Boolean suitable = isTypeCompatible(argumentTypes, methodParameters);
                    if (Boolean.FALSE.equals(suitable)) {
                        continue;
                    }
                    if (Boolean.TRUE.equals(suitable)) {
                        return innerCandidate;
                    }
                }
            }
        }
        return outerCandidate;
    }

    /** Sentinel value to indicate method pointer/reference expression. */
    private static final List<ClassNode> UNKNOWN_TYPES = new ArrayList<>();

    private static MethodNode findMethodDeclaration0(final List<? extends MethodNode> candidates, final List<ClassNode> argumentTypes, final boolean isStaticExpression) {
        if (argumentTypes == null || argumentTypes == UNKNOWN_TYPES) {
            for (MethodNode candidate : candidates) {
                if (isCompatible(candidate, isStaticExpression)) {
                    return candidate;
                }
            }
            return candidates.get(0);
        }

        MethodNode closestMatch = null;
        int argumentCount = argumentTypes.size();
        for (MethodNode candidate : candidates) {
            Parameter[] parameters = candidate.getParameters();
            if (parameters.length == 0) {
                if (argumentCount == 0) {
                    return candidate;
                }
                continue;
            }
            if (argumentCount == parameters.length || (argumentCount >= parameters.length - 1 && GenericsMapper.isVargs(parameters))) {
                Boolean suitable = isTypeCompatible(argumentTypes, parameters);
                if (Boolean.TRUE.equals(suitable)) {
                    return candidate;
                }
                if (!Boolean.FALSE.equals(suitable)) {
                    closestMatch = closer(candidate, closestMatch, argumentTypes);
                }
            }
        }
        if (closestMatch == null) {
            if (argumentCount == 0) { // "m()" is implicitly "m(null)" if only 1 method exists and it has 1 parameter and the parameter isn't primitive
                if (candidates.size() == 1 && candidates.get(0).getParameters().length == 1 && !ClassHelper.isPrimitiveType(candidates.get(0).getParameters()[0].getOriginType())) {
                    return candidates.get(0);
                }
            } else if (argumentTypes.stream().anyMatch(t -> ClassHelper.isPrimitiveType(ClassHelper.getUnwrapper(t)) || ClassHelper.OBJECT_TYPE.equals(t) || ClassHelper.STRING_TYPE.equals(t))) {
                // prefer method with the same number of parameters as arguments
                for (MethodNode candidate : candidates) {
                    Parameter[] parameters = candidate.getParameters();
                    if (argumentCount == parameters.length || (argumentCount >= parameters.length - 1 && GenericsMapper.isVargs(parameters))) {
                        if (isCompatible(candidate, isStaticExpression)) {
                            return candidate;
                        }
                        closestMatch = candidate;
                    }
                }
            }
        }
        return closestMatch;
    }

    private static Stream<MethodNode> findMetaObjectMethods(final ClassNode declaringType, final boolean isLhsExpression, final boolean isStaticExpression, final List<ClassNode> methodCallArgumentTypes) {
        Stream<String> names;
        if (isLhsExpression) {
            if (!isStaticExpression) {
                names = Stream.of("setProperty", "set");
            } else {
                names = Stream.empty();
            }
        } else if (methodCallArgumentTypes == null) {
            if (!isStaticExpression) {
                names = Stream.of("getProperty", "get", "propertyMissing");
            } else {
                names = Stream.of("$static_propertyMissing");
            }
        } else {
            if (!isStaticExpression) {
                names = Stream.of("methodMissing", "invokeMethod");
            } else {
                names = Stream.of("$static_methodMissing");
            }
        }
        return names.flatMap(name -> declaringType.getDeclaredMethods(name).stream()).filter(node -> node.isStatic() == node.getName().startsWith("$static"));
    }

    private static Optional<MethodNode> findPropertyAccessorMethod(final String propertyName, final ClassNode declaringType, final boolean isLhsExpression, final boolean isStaticExpression, final List<ClassNode> methodCallArgumentTypes) {
        Stream<MethodNode> accessors = AccessorSupport.findAccessorMethodsForPropertyName(propertyName, declaringType, false, !isLhsExpression ? READER : WRITER);
        accessors = accessors.filter(accessor -> isCompatible(accessor, isStaticExpression) && !isTraitBridge(accessor) &&
            (!accessor.isStatic() || !accessor.getDeclaringClass().isInterface())); // GROOVY-10592
        if (isLhsExpression) {
            // use methodCallArgumentTypes to select closer match
            accessors = accessors.sorted((m1, m2) -> (m1 == closer(m2, m1, methodCallArgumentTypes) ? -1 : +1));
        }
        return accessors.findFirst();
    }

    //--------------------------------------------------------------------------
    // TODO: Can any of these be relocated for reuse?

    protected static final AccessorSupport[] READER = {AccessorSupport.ISSER, AccessorSupport.GETTER};
    protected static final AccessorSupport[] WRITER = {AccessorSupport.SETTER};

    protected static MethodNode closer(final MethodNode next, final MethodNode last, final List<ClassNode> args) {
        if (last != null) {
            long d1 = calculateParameterDistance(args, last.getParameters());
            long d2 = calculateParameterDistance(args, next.getParameters());
            if (d1 <= d2)
                return last;
        }
        return next;
    }

    protected static long calculateParameterDistance(final List<ClassNode> arguments, final Parameter[] parameters) {
        try {
            int n = arguments.size();
            Class<?>[] args = new Class[n];
            for (int i = 0; i < n; i += 1) {
                args[i] = arguments.get(i).getTypeClass();
            }

            n = parameters.length;
            Class<?>[] prms = new Class[n];
            for (int i = 0; i < n; i += 1) {
                prms[i] = parameters[i].getType().getTypeClass();
            }

            // TODO: This can fail in a lot of cases; is there a better way to call it?
            return MetaClassHelper.calculateParameterDistance(args, new ParameterTypes(prms));
        } catch (Throwable t) {
            ClassNode pt = last(parameters).getType();
            if (pt.isArray()) pt = pt.getComponentType();
            return Long.MAX_VALUE - (VariableScope.isVoidOrObject(pt) ? 0 : 1);
        }
    }

    protected static PropertyNode createDynamicProperty(final String name, final ClassNode type, final ClassNode declaringType, final boolean staticProperty) {
        FieldNode fn = new FieldNode(name, Flags.AccPublic | (staticProperty ? Flags.AccStatic : 0), type, declaringType, null);
        fn.setDeclaringClass(declaringType);
        fn.setHasNoRealSourcePosition(true);
        fn.setSynthetic(true);

        PropertyNode pn = new PropertyNode(fn, fn.getModifiers(), null, null);
        pn.setDeclaringClass(declaringType);
        pn.setSynthetic(true);
        return pn;
    }

    protected static FieldNode createLengthField(final ClassNode declaringType) {
        FieldNode fn = new FieldNode("length", Flags.AccPublic, ClassHelper.int_TYPE, declaringType, null);
        fn.setDeclaringClass(declaringType);
        fn.setHasNoRealSourcePosition(true);
        return fn;
    }

    /**
     * Given {@code Class<T>} and {@code T} is not {@code ?}, returns {@code T};
     * otherwise returns {@code declaringType}.
     */
    protected static ClassNode getBaseDeclaringType(final ClassNode declaringType) {
        if (VariableScope.CLASS_CLASS_NODE.equals(declaringType) && declaringType.isUsingGenerics()) {
            GenericsType genericsType = declaringType.getGenericsTypes()[0];
            if (!genericsType.isWildcard()) return genericsType.getType();
        }
        return declaringType;
    }

    /**
     * @param mapType type that is or implements {@link java.util.Map}
     */
    protected static ClassNode getMapPropertyType(final ClassNode mapType) {
        ClassNode propertyType = VariableScope.OBJECT_CLASS_NODE;
        for (ClassNode face : GroovyUtils.getAllInterfaces(mapType)) {
            if (face.equals(VariableScope.MAP_CLASS_NODE)) { // Map<K,V>
                GenericsType[] generics = GroovyUtils.getGenericsTypes(face);
                if (generics.length == 2) propertyType = generics[1].getType();
                break;
            }
        }
        return propertyType;
    }

    protected static List<MethodNode> getMethods(final String name, final ClassNode type) {
        List<MethodNode> methods = type.getMethods(name);
        List<MethodNode> traitMethods = type.redirect().getNodeMetaData("trait.methods");
        if (traitMethods != null) {
            for (MethodNode method : traitMethods) {
                if (method.getName().equals(name)) {
                    methods.add(method);
                }
            }
        }
        methods.removeIf(m -> Flags.isSynthetic(m.getModifiers())); // GROOVY-8638

        return methods.size() <= 1 ? methods : unique(methods, Comparator.comparing(m -> {
            StringBuilder sb = new StringBuilder();
            for (Parameter p : m.getParameters()) {
                sb.append(p.getType().getName());
                sb.append(',');
            }
            return sb.toString();
        }));
    }

    /**
     * @return target of method call expression if available or {@code null}
     */
    protected static MethodNode getMethodTarget(final Expression expr) {
        if (expr instanceof MethodCallExpression) {
            MethodNode target = ((MethodCallExpression) expr).getMethodTarget();
            return target;
        } else {
            StatementMeta meta = expr.getNodeMetaData(StatementMeta.class);
            if (meta != null) {
                MethodNode target = ReflectionUtils.getPrivateField(StatementMeta.class, "target", meta);
                return target;
            }
        }
        // TODO: Is "((StaticMethodCallExpression) expr).getMetaMethod()" useful?
        return null;
    }

    protected static ClassNode getMorePreciseType(final ClassNode declaringType, final VariableInfo info) {
        ClassNode maybeDeclaringType = info != null ? info.declaringType : VariableScope.OBJECT_CLASS_NODE;
        if (maybeDeclaringType.equals(VariableScope.OBJECT_CLASS_NODE) && !VariableScope.OBJECT_CLASS_NODE.equals(declaringType)) {
            return declaringType;
        } else {
            return maybeDeclaringType;
        }
    }

    protected static ClassNode getTypeFromDeclaration(final ASTNode declaration) {
        ClassNode type;
        ASTNode decl = declaration;
        if (decl instanceof PropertyNode) {
            PropertyNode property = (PropertyNode) decl;
            if (property.getField() != null) {
                decl = property.getField();
            }
        }
        if (decl instanceof FieldNode) {
            FieldNode field = (FieldNode) decl;
            type = field.getType();
        } else if (decl instanceof ConstructorNode) {
            type = ((ConstructorNode) decl).getDeclaringClass();
        } else if (decl instanceof MethodNode) {
            type = ((MethodNode) decl).getReturnType();
        } else if (decl instanceof Expression) {
            type = ((Expression) decl).getType();
        } else {
            type = VariableScope.OBJECT_CLASS_NODE;
        }
        return type;
    }

    protected static ClassNode getDeclaringTypeFromDeclaration(final ASTNode declaration, final ClassNode inferredDeclaringType) {
        ClassNode declaringType;
        if (declaration instanceof FieldNode) {
            declaringType = ((FieldNode) declaration).getDeclaringClass();
        } else if (declaration instanceof MethodNode) {
            declaringType = ((MethodNode) declaration).getDeclaringClass();
        } else if (declaration instanceof PropertyNode) {
            declaringType = ((PropertyNode) declaration).getDeclaringClass();
        } else {
            declaringType = VariableScope.OBJECT_CLASS_NODE;
        }
        // retain inferredDeclaringType's generics if possible
        if (inferredDeclaringType.equals(declaringType)) {
            return inferredDeclaringType;
        } else {
            return declaringType;
        }
    }

    protected static Optional<FieldNode> findTraitField(final String name, final ClassNode type) {
        String[] parts = name.split("__");
        for (ClassNode face : type.getInterfaces()) {
            if (face.getName().equals(parts[0].replace('_', '.'))) {
                List<FieldNode> traitFields = face.redirect().getNodeMetaData("trait.fields");
                return Optional.ofNullable(traitFields).flatMap(fields ->
                    fields.stream().filter(f -> f.getName().equals(parts[1])).findFirst());
            }
        }
        return Optional.empty();
    }

    protected static boolean isCompoundAssignment(final VariableScope scope) {
        return scope.getEnclosingAssignmentOperator().filter(op -> op.getType() != Types.EQUALS).isPresent();
    }

    protected static Expression getObjectExpression(final VariableScope scope) {
        ASTNode node = scope.getEnclosingNode();
        if (node instanceof PropertyExpression) {
            return ((PropertyExpression) node).getObjectExpression();
        }
        if (node instanceof MethodCallExpression) {
            return ((MethodCallExpression) node).getObjectExpression();
        }
        if (node instanceof MethodPointerExpression) {
            return ((MethodPointerExpression) node).getExpression();
        }
        return null;
    }

    protected static boolean isThisObjectExpression(final VariableScope scope) {
        // TODO: Test for "Type.this.name" expressions for inner classes?
        Expression expr = getObjectExpression(scope);
        return (expr instanceof VariableExpression && ((VariableExpression) expr).isThisExpression());
    }

    protected static boolean isSuperObjectExpression(final VariableScope scope) {
        // TODO: Test for "Type.super.name" expressions for traits?
        Expression expr = getObjectExpression(scope);
        return (expr instanceof VariableExpression && ((VariableExpression) expr).isSuperExpression());
    }

    protected static boolean isStaticReferenceToInstanceMethod(final VariableScope scope) {
        if (scope.getEnclosingNode() instanceof MethodPointerExpression && scope.getCurrentNode() instanceof ConstantExpression) {
            // "Type.&instanceMethod" and "Type::instanceMethod" are supported starting in Groovy 3
            return (GroovyUtils.getGroovyVersion().getMajor() >= 3);
        }
        return false;
    }

    protected static boolean isStaticReferenceToUnambiguousMethod(final VariableScope scope, final String name, final ClassNode type) {
        if (scope.getEnclosingNode() instanceof ImportNode) { // import nodes can only refer to static methods of type
            long staticMethodCount = getMethods(name, type).stream().filter(meth -> isCompatible(meth, true)).count();
            return (staticMethodCount == 1);
        }
        // TODO: Add case for PropertyExpression, MethodCallExpression or MethodPointerExpression?
        return false;
    }

    protected static boolean isCompatible(final AnnotatedNode declaration, final boolean isStaticExpression) {
        if (declaration != null) {
            boolean isStatic = false;
            if (declaration instanceof FieldNode) {
                isStatic = ((FieldNode) declaration).isStatic();
            } else if (declaration instanceof MethodNode) {
                isStatic = ((MethodNode) declaration).isStatic();
            } else if (declaration instanceof PropertyNode) {
                isStatic = ((PropertyNode) declaration).isStatic();
            }
            if (!isStaticExpression || isStatic ||
                    VariableScope.CLASS_CLASS_NODE.equals(declaration.getDeclaringClass()) ||
                    VariableScope.OBJECT_CLASS_NODE.equals(declaration.getDeclaringClass())) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isNotThisOrOuterClass(final ClassNode thisType, final ClassNode declaringClass) {
        return (!thisType.equals(declaringClass) && !thisType.getOuterClasses().contains(declaringClass) && !(implementsTrait(thisType) && thisType.implementsInterface(declaringClass)));
    }

    /**
     * Determines if the specified method node is synthetic (i.e. generated or
     * implicit in some sense).
     */
    protected static boolean isSynthetic(final MethodNode method) {
        return method.isSynthetic() || method.getDeclaringClass().equals(VariableScope.CLOSURE_CLASS_NODE) ||
            GroovyUtils.getAnnotations(method, "groovy.transform.Generated").anyMatch(annotationNode -> true);
    }

    protected static boolean isTraitBridge(final MethodNode method) {
        return method.getAnnotations().stream().map(AnnotationNode::getClassNode).anyMatch(Traits.TRAITBRIDGE_CLASSNODE::equals);
    }

    protected static boolean isTraitHelper(final ClassNode candidate) {
        return Flags.isSynthetic(candidate.getModifiers()) && candidate.getName().endsWith("Helper") && candidate.getName().contains("$Trait$") && Traits.isTrait(candidate.getOuterClass());
    }

    /**
     * Supplements {@link #isTypeCompatible} by supporting unequal lengths and
     * tagging Closure -> SAM type as an inexact match.
     */
    protected static boolean isLooseMatch(final List<ClassNode> arguments, final Parameter[] parameters) {
        int argCount = (arguments == null ? -1 : arguments.size());
        if (argCount != parameters.length && !(GenericsMapper.isVargs(parameters) &&
                (argCount == parameters.length - 1 || argCount > parameters.length))) {
            return true;
        } else if (argCount > 0 && arguments.get(argCount - 1).equals(VariableScope.CLOSURE_CLASS_NODE)) {
            ClassNode lastType = GroovyUtils.getBaseType(parameters[parameters.length - 1].getType());
            if (!lastType.equals(VariableScope.CLOSURE_CLASS_NODE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given argument types are compatible with the declaration parameters.
     *
     * @return {@link Boolean#TRUE true} for exact match, {@code null} for loose match, and {@link Boolean#FALSE false} for not a match
     */
    protected static Boolean isTypeCompatible(final List<ClassNode> arguments, final Parameter[] parameters) {
        Boolean result = Boolean.TRUE;
        for (int i = 0, n = Math.max(arguments.size(), parameters.length); i < n; i += 1) {
            ClassNode parameter = parameters[Math.min(i, parameters.length - 1)].getType();
            ClassNode argument = i < arguments.size() ? arguments.get(i) : parameter;

            if (i >= parameters.length) {
                // argument that does not align with a parameter must be vararg
                assert parameter.isArray(); parameter = parameter.getComponentType();
            } else if (i == (parameters.length - 1) && arguments.size() >= parameters.length && parameter.isArray()) {
                // argument aligned with the last parameter (an array) may be a vararg
                if (!argument.isArray() || !GroovyUtils.isAssignable(argument, parameter)) parameter = parameter.getComponentType();
            }

            // test parameter and argument for exact and loose match
            Boolean partialResult = isTypeCompatible(argument, parameter);
            if (partialResult == null) {
                result = null; // loose
            } else if (!partialResult) {
                return Boolean.FALSE;
            }
        }

        if (arguments.size() != parameters.length || !arguments.isEmpty() &&
                last(arguments).isArray() != last(parameters).getType().isArray()) {
            return null;
        }
        return result;
    }

    // TODO: How much of this could/should be moved to GroovyUtils.isAssignable?
    protected static Boolean isTypeCompatible(final ClassNode source, final ClassNode target) {
        Boolean result = Boolean.TRUE;
        if (!target.equals(source) &&
            !(VariableScope.NULL_TYPE == source && !ClassHelper.isPrimitiveType(target))) {
            // NOTE: Exact match of Closure to SAM Type creates tie for m(Closure) and m(Comparator)
            result = !GroovyUtils.isAssignable(source, target) && !(VariableScope.CLOSURE_CLASS_NODE.equals(source) && ClassHelper.isSAMType(target)) ? Boolean.FALSE : null; // not an exact match
        }
        return result;
    }
}
