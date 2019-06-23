/*
 * Copyright 2009-2019 the original author or authors.
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

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;
import static org.eclipse.jdt.groovy.core.util.GroovyUtils.implementsTrait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import groovy.lang.Closure;
import groovy.lang.GroovySystem;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.StatementMeta;
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
    public TypeLookupResult lookupType(final ImportNode node, final VariableScope scope) {
        ClassNode baseType = Optional.ofNullable(node.getType()).orElse(VariableScope.NULL_TYPE);
        return new TypeLookupResult(baseType, baseType, baseType, TypeConfidence.EXACT, scope);
    }

    /**
     * @return {@code node}, unless the declaration is an {@link InnerClassNode}
     */
    @Override
    public TypeLookupResult lookupType(final ClassNode node, final VariableScope scope) {
        ClassNode type;
        if (node.getOuterClass() != null && !node.isRedirectNode()) {
            type = node.getSuperClass();
            if (type.getName().equals(VariableScope.OBJECT_CLASS_NODE.getName()) && node.getInterfaces().length > 0) {
                type = node.getInterfaces()[0];
            }
        } else {
            type = node;
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
                    List<ClassNode> types = outer.getMethodCallArgumentTypes();
                    try { outer.setMethodCallArgumentTypes(scope.getMethodCallArgumentTypes());
                        return findTypeForVariable(new VariableExpression(new DynamicVariable(node.getText(), false)), outer, declaringType);
                    } finally {
                        outer.setMethodCallArgumentTypes(types);
                    }
                }

                // short-circuit if object expression is part of direct field access (aka AttributeExpression)
                if (scope.getEnclosingNode() instanceof AttributeExpression) {
                    ClassNode clazz = !isStaticObjectExpression ? declaringType : declaringType.getGenericsTypes()[0].getType();
                    FieldNode field = clazz.getDeclaredField(node.getText()); // don't search super types (see GROOVY-8167)
                    if (!isCompatible(field, isStaticObjectExpression)) {
                        return new TypeLookupResult(VariableScope.VOID_CLASS_NODE, null, null, TypeConfidence.UNKNOWN, scope);
                    } else {
                        boolean isPrivateSuperField = Flags.isPrivate(field.getModifiers()) && isSuperObjectExpression(scope);
                        return new TypeLookupResult(field.getType(), clazz, field, !isPrivateSuperField ? confidence : TypeConfidence.UNKNOWN, scope);
                    }
                }

                boolean isLhsExpression = (scope.getWormhole().remove("lhs") == node);
                return findTypeForNameWithKnownObjectExpression(node.getText(), nodeType, declaringType, scope, isLhsExpression, isStaticObjectExpression);
            }

            ConstantExpression cexp = (ConstantExpression) node;
            if (cexp.isNullExpression()) {
                return new TypeLookupResult(VariableScope.VOID_CLASS_NODE, null, null, confidence, scope);
            } else if (cexp.isTrueExpression() || cexp.isFalseExpression()) {
                return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);
            } else if (cexp.isEmptyStringExpression() || VariableScope.STRING_CLASS_NODE.equals(nodeType)) {
                return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, node, confidence, scope);
            } else if (ClassHelper.isNumberType(nodeType) || VariableScope.BIG_DECIMAL_CLASS.equals(nodeType) || VariableScope.BIG_INTEGER_CLASS.equals(nodeType)) {
                return new TypeLookupResult(GroovyUtils.getWrapperTypeIfPrimitive(nodeType), null, null, confidence, scope);
            } else {
                return new TypeLookupResult(nodeType, null, null, TypeConfidence.UNKNOWN, scope);
            }

        } else if (node instanceof BooleanExpression) {
            return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);

        } else if (node instanceof GStringExpression) {
            // return String not GString so that DGMs will apply
            return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);

        } else if (node instanceof BitwiseNegationExpression) {
            ClassNode type = ((BitwiseNegationExpression) node).getExpression().getType();
            // check for ~/.../ (a.k.a. Pattern literal)
            if (VariableScope.STRING_CLASS_NODE.equals(type)) {
                return new TypeLookupResult(VariableScope.PATTERN_CLASS_NODE, null, null, confidence, scope);
            }
            return new TypeLookupResult(type, null, null, confidence, scope);

        } else if (node instanceof ClosureExpression && VariableScope.isPlainClosure(nodeType)) {
            ClassNode returnType = node.getNodeMetaData("returnType");
            if (returnType != null && !VariableScope.isVoidOrObject(returnType))
                GroovyUtils.updateClosureWithInferredTypes(nodeType, returnType, ((ClosureExpression) node).getParameters());

        } else if (node instanceof ClassExpression) {
            ClassNode classType = VariableScope.newClassClassNode(node.getType());
            classType.setSourcePosition(node);

            return new TypeLookupResult(classType, null, node.getType(), TypeConfidence.EXACT, scope);

        } else if (node instanceof ConstructorCallExpression) {
            ConstructorCallExpression call = (ConstructorCallExpression) node;
            ClassNode resolvedDeclaringType = declaringType;
            if (call.isSpecialCall()) {
                nodeType = VariableScope.VOID_CLASS_NODE;
                resolvedDeclaringType = scope.getEnclosingMethodDeclaration().getDeclaringClass();
                if (call.isSuperCall()) resolvedDeclaringType = resolvedDeclaringType.getUnresolvedSuperClass(false);
            } else if (call.isUsingAnonymousInnerClass()) {
                resolvedDeclaringType = resolvedDeclaringType.getUnresolvedSuperClass(false); // nodeType is anon. inner
            }

            // try to find best match if there is more than one constructor to choose from
            List<ConstructorNode> declaredConstructors = resolvedDeclaringType.getDeclaredConstructors();
            if (declaredConstructors.size() > 1 && call.getArguments() instanceof ArgumentListExpression) {
                List<ClassNode> callTypes = scope.getMethodCallArgumentTypes();
                if (callTypes != null && callTypes.size() > 1) {
                    // non-static inner types may have extra argument for enclosing type
                    if (callTypes.get(0).equals(resolvedDeclaringType.getOuterClass()) && !Flags.isStatic(resolvedDeclaringType.getModifiers())) {
                        callTypes.remove(0);
                    }
                }

                List<ConstructorNode> looseMatches = new ArrayList<>();
                for (ConstructorNode ctor : declaredConstructors) {
                    if (callTypes != null && callTypes.size() == ctor.getParameters().length) {
                        if (Boolean.TRUE.equals(isTypeCompatible(callTypes, ctor.getParameters()))) {
                            return new TypeLookupResult(nodeType, resolvedDeclaringType, ctor, confidence, scope);
                        }
                        // argument types may not be fully resolved; at least the number of arguments matched
                        looseMatches.add(ctor);
                    }
                }
                if (!looseMatches.isEmpty()) {
                    declaredConstructors = looseMatches;
                }
            }

            ASTNode declaration = (!declaredConstructors.isEmpty() ? declaredConstructors.get(0) : resolvedDeclaringType);
            return new TypeLookupResult(nodeType, resolvedDeclaringType, declaration, confidence, scope);

        } else if (node instanceof StaticMethodCallExpression) {
            List<MethodNode> candidates = new LinkedList<>();
            java.util.function.BiConsumer<ClassNode, String> collector = (classNode, methodName) -> {
                if (classNode.isAbstract() || classNode.isInterface() || implementsTrait(classNode)) {
                    LinkedHashSet<ClassNode> abstractTypes = new LinkedHashSet<>();
                    VariableScope.findAllInterfaces(classNode, abstractTypes, false);
                    for (ClassNode abstractType : abstractTypes) {
                        candidates.addAll(abstractType.getMethods(methodName));
                    }
                } else {
                    candidates.addAll(classNode.getMethods(methodName));
                }
            };

            String methodName = ((StaticMethodCallExpression) node).getMethod();
            collector.accept(((StaticMethodCallExpression) node).getOwnerType(), methodName);

            // Workaround for https://issues.apache.org/jira/browse/GROOVY-7744:
            /*String alias = node.getNodeMetaData("static.import.alias");
            // deal with "import static a.B.C as Z" and "import static d.E.F as Z"
            for (ImportNode staticImport : scope.getEnclosingModuleNode().getStaticImports().values()) {
                if (staticImport.getAlias().equals(alias != null ? alias : methodName)) {
                    collector.accept(staticImport.getType(), staticImport.getFieldName());
                }
            }
            // deal with "import static a.B.C as Z" and "import static d.E.*" where type E has member Z
            for (ImportNode staticImport : scope.getEnclosingModuleNode().getStaticStarImports().values()) {
                collector.accept(staticImport.getType(), alias != null ? alias : methodName);
            }*/

            for (Iterator<MethodNode> it = candidates.iterator(); it.hasNext();) {
                if (!it.next().isStatic()) it.remove();
            }

            if (!candidates.isEmpty()) {
                MethodNode closestMatch;
                if (scope.isMethodCall()) {
                    closestMatch = findMethodDeclaration0(candidates, scope.getMethodCallArgumentTypes(), isStaticObjectExpression);
                    confidence = TypeConfidence.INFERRED;
                } else {
                    closestMatch = candidates.get(0);
                    confidence = TypeConfidence.LOOSELY_INFERRED;
                }

                return new TypeLookupResult(closestMatch.getReturnType(), closestMatch.getDeclaringClass(), closestMatch, confidence, scope);
            }
        }

        if (!(node instanceof TupleExpression) && VariableScope.OBJECT_CLASS_NODE.equals(nodeType)) {
            confidence = TypeConfidence.UNKNOWN;
        }

        return new TypeLookupResult(nodeType, declaringType, null, confidence, scope);
    }

    protected TypeLookupResult findTypeForNameWithKnownObjectExpression(final String name, final ClassNode type, final ClassNode declaringType, final VariableScope scope, final boolean isLhsExpression, final boolean isStaticObjectExpression) {

        TypeConfidence confidence = TypeConfidence.EXACT;
        boolean isFieldAccessDirect = (isThisObjectExpression(scope) ? scope.isFieldAccessDirect() : false);
        ASTNode declaration = findDeclaration(name, declaringType, isLhsExpression, isStaticObjectExpression, isFieldAccessDirect, scope.getMethodCallArgumentTypes());
        if (declaration instanceof MethodNode && scope.getEnclosingNode() instanceof PropertyExpression && !scope.isMethodCall() &&
                (!AccessorSupport.isGetter((MethodNode) declaration) || name.equals(((MethodNode) declaration).getName()))) {
            declaration = null; // property expression "foo.bar" does not resolve to "bar(...)" or "setBar(x)" w/o call args
        }

        ClassNode resolvedType = type, resolvedDeclaringType;
        if (declaration != null) {
            resolvedType = getTypeFromDeclaration(declaration);
            resolvedDeclaringType = getDeclaringTypeFromDeclaration(declaration, declaringType);
        } else if ("call".equals(name)) {
            // assume that this is a synthetic call method for calling a closure
            resolvedDeclaringType = VariableScope.CLOSURE_CLASS_NODE;
            declaration = resolvedDeclaringType.getMethods("call").get(0);
        } else if ("this".equals(name) && VariableScope.CLASS_CLASS_NODE.equals(declaringType)) {
            // "Type.this" (aka ClassExpression.ConstantExpression) within inner class
            declaration = resolvedType = resolvedDeclaringType = declaringType.getGenericsTypes()[0].getType();
        } else {
            resolvedDeclaringType = declaringType;
            confidence = TypeConfidence.UNKNOWN;
        }

        if (declaration != null) {
            if (!VariableScope.CLASS_CLASS_NODE.equals(resolvedDeclaringType) && !VariableScope.CLASS_CLASS_NODE.equals(resolvedType)) {
                // check to see if the object expression is static but the declaration is not -- and some other conditions
                if (declaration instanceof FieldNode) {
                    FieldNode field = (FieldNode) declaration;
                    if (isStaticObjectExpression && !field.isStatic()) {
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (Flags.isPrivate(field.getModifiers())) {
                        // "super.field" reference to private field yields MissingMethodException
                        if (isSuperObjectExpression(scope)) {
                            confidence = TypeConfidence.UNKNOWN;
                        // "this.field" reference to private field of super class yields MissingPropertyException
                        } else if (isThisObjectExpression(scope) && isNotThisOrOuterClass(declaringType, resolvedDeclaringType)) {
                            confidence = TypeConfidence.UNKNOWN;
                        }
                    }
                } else if (declaration instanceof PropertyNode) {
                    FieldNode underlyingField = ((PropertyNode) declaration).getField();
                    if (underlyingField != null) {
                        // prefer looking at the underlying field
                        if (isStaticObjectExpression && !underlyingField.isStatic()) {
                            confidence = TypeConfidence.UNKNOWN;
                        }
                    } else if (isStaticObjectExpression && !((PropertyNode) declaration).isStatic()) {
                        confidence = TypeConfidence.UNKNOWN;
                    }
                } else if (declaration instanceof MethodNode) {
                    MethodNode method = (MethodNode) declaration;
                    if (isStaticObjectExpression && !method.isStatic() && !isStaticReferenceToInstanceMethod(scope)) {
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (Flags.isPrivate(method.getModifiers()) && isThisObjectExpression(scope) && isNotThisOrOuterClass(declaringType, resolvedDeclaringType)) {
                        // "this.method()" reference to private method of super class yields MissingMethodException; "super.method()" is okay
                        confidence = TypeConfidence.UNKNOWN;
                    } else if (isLooseMatch(scope.getMethodCallArgumentTypes(), method.getParameters())) {
                        // if arguments and parameters are mismatched, a category method may make a better match
                        confidence = TypeConfidence.LOOSELY_INFERRED;
                    }
                }
            } else if (VariableScope.CLASS_CLASS_NODE.equals(resolvedDeclaringType) && declaration instanceof MethodNode) {
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

        // StatementAndExpressionCompletionProcessor circa line 390 has similar check for proposals
        if (confidence == TypeConfidence.UNKNOWN && VariableScope.CLASS_CLASS_NODE.equals(declaringType) && declaringType.isUsingGenerics()) {
            ClassNode typeParam = declaringType.getGenericsTypes()[0].getType();
            if (!VariableScope.CLASS_CLASS_NODE.equals(typeParam) && !VariableScope.OBJECT_CLASS_NODE.equals(typeParam)) {
                // GRECLIPSE-1544: "Type.staticMethod()" or "def type = Type.class; type.staticMethod()" or ".&" variations
                return findTypeForNameWithKnownObjectExpression(name, resolvedType, typeParam, scope, isLhsExpression, isStaticObjectExpression);
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
        boolean direct = (accessedVar instanceof AnnotatedNode &&
            resolvedDeclaringType.equals(((AnnotatedNode) accessedVar).getDeclaringClass()));

        if ((accessedVar instanceof FieldNode && !(direct && scope.isFieldAccessDirect())) ||
                (direct && resolveStrategy != Closure.OWNER_FIRST && resolveStrategy != Closure.OWNER_ONLY)) {
            // accessed variable was found using direct search; forget the reference
            accessedVar = new DynamicVariable(var.getName(), scope.isStatic());
        }

        if (accessedVar instanceof ASTNode) {
            decl = (ASTNode) accessedVar;
            if (decl instanceof FieldNode ||
                decl instanceof MethodNode ||
                decl instanceof PropertyNode) {

                if (decl instanceof PropertyNode) {
                    PropertyNode prop = (PropertyNode) decl; // check for pseudo-property
                    if (prop.isDynamicTyped() && prop.getField().hasNoRealSourcePosition()) {
                        Optional<MethodNode> accessor = findPropertyAccessorMethod(prop.getName(), resolvedDeclaringType,
                            (scope.getWormhole().get("lhs") == var), prop.isStatic(), scope.getMethodCallArgumentTypes());
                        decl = accessor.map(meth -> (ASTNode) meth).orElse(decl);
                    }
                }

                resolvedDeclaringType = ((AnnotatedNode) decl).getDeclaringClass();
                variableInfo = null; // use field/method/property
                type = getTypeFromDeclaration(decl);
            }
        } else if (accessedVar instanceof DynamicVariable) {
            resolvedDeclaringType = getMorePreciseType(resolvedDeclaringType, variableInfo);
            ASTNode candidate = findDeclarationForDynamicVariable(var, resolvedDeclaringType, scope, resolveStrategy);
            if (candidate != null && (!(candidate instanceof MethodNode) || scope.isMethodCall() ||
                    (AccessorSupport.isGetter((MethodNode) candidate) && !var.getName().equals(((MethodNode) candidate).getName())))) {
                if (candidate instanceof FieldNode) {
                    FieldNode field = (FieldNode) candidate;
                    ClassNode owner = field.getDeclaringClass();
                    if (field.getName().contains("__") && implementsTrait(owner)) {
                        candidate = findTraitField(field.getName(), owner).orElse(field);
                    } else if (Flags.isPrivate(field.getModifiers()) && isNotThisOrOuterClass(resolvedDeclaringType, field.getDeclaringClass())) {
                        confidence = TypeConfidence.UNKNOWN; // reference to private field of super class yields MissingPropertyException
                    }
                } else if (candidate instanceof MethodNode) {
                    // check for call "method(1,2,3)" matched to decl "method(int)"
                    List<ClassNode> argumentTypes = scope.getMethodCallArgumentTypes();
                    Parameter[] parameterNodes = ((MethodNode) candidate).getParameters();
                    if (argumentTypes != null && isLooseMatch(argumentTypes, parameterNodes)) {
                        confidence = TypeConfidence.LOOSELY_INFERRED;
                    }
                    if (Flags.isPrivate(((MethodNode) candidate).getModifiers()) && isNotThisOrOuterClass(resolvedDeclaringType, ((MethodNode) candidate).getDeclaringClass())) {
                        confidence = TypeConfidence.UNKNOWN; // reference to private method of super class yields MissingMethodException
                    }
                }
                decl = candidate;
                type = getTypeFromDeclaration(decl);
                resolvedDeclaringType = getDeclaringTypeFromDeclaration(decl, resolvedDeclaringType);
            } else {
                type = VariableScope.OBJECT_CLASS_NODE;
                confidence = TypeConfidence.UNKNOWN;
                // dynamic variables are not allowed outside of script mainline
                if (variableInfo != null && !scope.inScriptRunMethod()) variableInfo = null;
            }
        }

        if (variableInfo != null && (!(decl instanceof MethodNode) || VariableScope.CLOSURE_CLASS_NODE.equals(resolvedDeclaringType))) {
            type = variableInfo.type;
            confidence = TypeConfidence.INFERRED;
            if (VariableScope.isThisOrSuper(var)) decl = type;
            resolvedDeclaringType = getMorePreciseType(resolvedDeclaringType, variableInfo);
        }

        return new TypeLookupResult(type, resolvedDeclaringType, decl, confidence, scope);
    }

    protected ASTNode findDeclarationForDynamicVariable(final VariableExpression var, final ClassNode owner, final VariableScope scope, final int resolveStrategy) {
        ASTNode candidate = null;
        List<ClassNode> callArgs = scope.getMethodCallArgumentTypes();
        boolean isLhsExpr = (scope.getWormhole().remove("lhs") == var);

        if (resolveStrategy == Closure.DELEGATE_FIRST || resolveStrategy == Closure.DELEGATE_ONLY) {
            // TODO: If strategy is DELEGATE_ONLY and delegate is enclosing closure, do outer search.
            candidate = findDeclaration(var.getName(), scope.getDelegate(), isLhsExpr, false, false, callArgs);
        }
        if (candidate == null && resolveStrategy < Closure.DELEGATE_ONLY) {
            VariableScope outer = owner.getNodeMetaData("outer.scope");
            if (outer != null) { // owner is an enclosing closure
                if (isLhsExpr) scope.getWormhole().put("lhs", var);
                int enclosingResolveStrategy = outer.getEnclosingClosureResolveStrategy();
                candidate = findDeclarationForDynamicVariable(var, getBaseDeclaringType(outer.getOwner()), outer, enclosingResolveStrategy);
            } else {
                candidate = findDeclaration(var.getName(), owner, isLhsExpr, scope.isOwnerStatic(), scope.isFieldAccessDirect(), callArgs);
            }
            if (candidate == null && resolveStrategy < Closure.DELEGATE_FIRST && scope.getEnclosingClosure() != null) {
                candidate = findDeclaration(var.getName(), scope.getDelegate(), isLhsExpr, false, false, callArgs);
            }
            if (candidate == null && scope.getEnclosingClosure() == null && scope.getEnclosingMethodDeclaration() != null) {
                for (Parameter parameter : scope.getEnclosingMethodDeclaration().getParameters()) {
                    if (parameter.getName().equals(var.getName())) {
                        candidate = parameter; break;
                    }
                }
            }
        }
        if (candidate == null && resolveStrategy <= Closure.TO_SELF && (resolveStrategy > 0 || scope.getEnclosingClosure() != null)) {
            candidate = findDeclaration(var.getName(), VariableScope.CLOSURE_CLASS_NODE, isLhsExpr, false, false, callArgs);
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
     * @param directFieldAccess {@code false} if accessor methods may take precedence
     * @param methodCallArgumentTypes types of arguments to the associated method call
     *        (or {@code null} if not a method call)
     */
    protected ASTNode findDeclaration(final String name, final ClassNode declaringType, final boolean isLhsExpression, final boolean isStaticExpression, final boolean directFieldAccess, final List<ClassNode> methodCallArgumentTypes) {
        if (declaringType.isArray()) {
            // only length exists on arrays
            if ("length".equals(name)) {
                return createLengthField(declaringType);
            }
            // otherwise search on object
            return findDeclaration(name, VariableScope.OBJECT_CLASS_NODE, isLhsExpression, isStaticExpression, directFieldAccess, methodCallArgumentTypes);
        }

        if (!isLhsExpression && methodCallArgumentTypes != null) {
            MethodNode method = findMethodDeclaration(name, declaringType, methodCallArgumentTypes, isStaticExpression);
            if (isCompatible(method, isStaticExpression)) {
                return method;
            }
            // name may still map to something that is callable; keep looking
        }

        // look for canonical accessor method
        Optional<MethodNode> accessor = findPropertyAccessorMethod(name, declaringType, isLhsExpression, isStaticExpression, methodCallArgumentTypes);
        if (accessor.filter(it -> !isSynthetic(it) && !(directFieldAccess && declaringType.equals(it.getDeclaringClass()))).isPresent()) {
            return accessor.get();
        }

        LinkedHashSet<ClassNode> typeHierarchy = new LinkedHashSet<>();
        VariableScope.createTypeHierarchy(declaringType, typeHierarchy, true);

        // look for property
        for (ClassNode type : typeHierarchy) {
            PropertyNode property = type.getProperty(name);
            if (isCompatible(property, isStaticExpression)) {
                return property;
            }
        }

        // look for field
        FieldNode field = declaringType.getField(name);
        if (isCompatible(field, isStaticExpression)) {
            return field;
        }

        typeHierarchy.clear();
        VariableScope.findAllInterfaces(declaringType, typeHierarchy, true);

        // look for constant in interfaces
        for (ClassNode type : typeHierarchy) {
            if (type == declaringType) {
                continue;
            }
            field = type.getField(name);
            if (field != null && field.isFinal() && field.isStatic()) {
                return field;
            }
        }

        // look for static or synthetic accessor
        if (accessor.isPresent()) {
            return accessor.get();
        }

        // look for member in outer classes
        if (getBaseDeclaringType(declaringType).getOuterClass() != null) {
            // search only for static declarations if inner class is static
            boolean isStatic = (isStaticExpression || Flags.isStatic(declaringType.getModifiers()));
            ASTNode declaration = findDeclaration(name, getBaseDeclaringType(declaringType).getOuterClass(), isLhsExpression, isStatic, directFieldAccess, methodCallArgumentTypes);
            if (declaration != null) {
                return declaration;
            }
        }

        if (methodCallArgumentTypes == null) {
            // reference may be in method pointer or static import; look for method as last resort
            return findMethodDeclaration(name, declaringType, methodCallArgumentTypes, isStaticExpression);
        }

        return null;
    }

    /**
     * Finds a method with the given name in the declaring type. Prioritizes methods
     * with the same number of arguments, but if multiple methods exist with same name,
     * then will return an arbitrary one.
     */
    protected MethodNode findMethodDeclaration(final String name, final ClassNode declaringType, final List<ClassNode> argumentTypes, final boolean isStaticExpression) {
        // concrete types (without mixins/traits) return all declared methods from getMethods(String)
        if (!declaringType.isAbstract() && !declaringType.isInterface() && !implementsTrait(declaringType)) {
            List<MethodNode> candidates = declaringType.getMethods(name);
            if (!candidates.isEmpty()) {
                return findMethodDeclaration0(candidates, argumentTypes, isStaticExpression);
            }
            return null;
        }

        // abstract types may not return all methods from getMethods(String)
        LinkedHashSet<ClassNode> types = new LinkedHashSet<>();
        if (!declaringType.isInterface()) types.add(declaringType);
        VariableScope.findAllInterfaces(declaringType, types, true);
        if (!implementsTrait(declaringType))
            types.add(VariableScope.OBJECT_CLASS_NODE); // implicit super type

        MethodNode outerCandidate = null;
        for (ClassNode type : types) {
            MethodNode innerCandidate = null;
            List<MethodNode> candidates = getMethods(name, type);
            if (!candidates.isEmpty()) {
                innerCandidate = findMethodDeclaration0(candidates, argumentTypes, isStaticExpression);
                if (innerCandidate.getOriginal() == null) {
                    innerCandidate = null; // trait bridge
                }
                if (outerCandidate == null) {
                    outerCandidate = innerCandidate;
                }
            }
            if (innerCandidate != null && argumentTypes != null) {
                Parameter[] methodParameters = innerCandidate.getParameters();
                if (argumentTypes.isEmpty() && methodParameters.length == 0) {
                    return innerCandidate;
                }
                if (argumentTypes.size() == methodParameters.length) {
                    outerCandidate = innerCandidate;

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

    protected static MethodNode findMethodDeclaration0(final List<MethodNode> candidates, final List<ClassNode> argumentTypes, final boolean isStaticExpression) {
        int argumentCount = (argumentTypes == null ? -1 : argumentTypes.size());

        MethodNode closestMatch = null;
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
        if (closestMatch != null) {
            return closestMatch;
        }

        // prefer method with the same number of parameters as arguments
        if (argumentCount > 0) {
            for (MethodNode candidate : candidates) {
                if (argumentCount == candidate.getParameters().length) {
                    if (isCompatible(candidate, isStaticExpression)) {
                        return candidate;
                    }
                    closestMatch = candidate;
                }
            }
            if (closestMatch != null) {
                return closestMatch;
            }
        }

        if (isStaticExpression) {
            for (MethodNode candidate : candidates) {
                if (candidate.isStatic()) {
                    return candidate;
                }
            }
        }

        return candidates.get(0);
    }

    protected static Optional<MethodNode> findPropertyAccessorMethod(final String propertyName, final ClassNode declaringType, final boolean isLhsExpression, final boolean isStaticExpression, final List<ClassNode> methodCallArgumentTypes) {
        Stream<MethodNode> accessors = AccessorSupport.findAccessorMethodsForPropertyName(propertyName, declaringType, false, !isLhsExpression ? READER : WRITER);
        accessors = accessors.filter(accessor -> isCompatible(accessor, isStaticExpression));
        if (isLhsExpression) {
            // use methodCallArgumentTypes to select closer match
            accessors = accessors.sorted((m1, m2) -> (m1 == closer(m2, m1, methodCallArgumentTypes) ? -1 : +1));
        }
        return accessors.findFirst();
    }

    //--------------------------------------------------------------------------
    // TODO: Can any of these be relocated for reuse?

    protected static final AccessorSupport[] READER = {AccessorSupport.GETTER, AccessorSupport.ISSER};
    protected static final AccessorSupport[] WRITER = {AccessorSupport.SETTER};

    protected static MethodNode closer(final MethodNode next, final MethodNode last, final List<ClassNode> args) {
        if (last != null) {
            long d1 = CategoryTypeLookup.calculateParameterDistance(args, last.getParameters());
            long d2 = CategoryTypeLookup.calculateParameterDistance(args, next.getParameters());
            if (d1 < d2)
                return last;
        }
        return next;
    }

    protected static FieldNode createLengthField(final ClassNode declaringType) {
        FieldNode lengthField = new FieldNode("length", Flags.AccPublic, VariableScope.INTEGER_CLASS_NODE, declaringType, null);
        lengthField.setDeclaringClass(declaringType);
        return lengthField;
    }

    /**
     * Given {@code Class<T>} and {@code T} is not {@code ?}, {@code Class}, or
     * {@code Object} returns {@code T}; otherwise returns {@code declaringType}.
     */
    protected static ClassNode getBaseDeclaringType(final ClassNode declaringType) {
        if (VariableScope.CLASS_CLASS_NODE.equals(declaringType) && declaringType.isUsingGenerics()) {
            ClassNode typeParam = declaringType.getGenericsTypes()[0].getType();
            if (!VariableScope.CLASS_CLASS_NODE.equals(typeParam) &&
                !VariableScope.OBJECT_CLASS_NODE.equals(typeParam)) {

                return typeParam;
            }
        }
        return declaringType;
    }

    protected static List<MethodNode> getMethods(final String name, final ClassNode type) {
        List<MethodNode> methods = type.getMethods(name);
        List<MethodNode> traitMethods =
            type.redirect().getNodeMetaData("trait.methods");
        if (traitMethods != null) {
            methods = new ArrayList<>(methods);
            for (MethodNode method : traitMethods) {
                if (method.getName().equals(name)) {
                    methods.add(method);
                }
            }
        }
        return methods;
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
            if (field.isDynamicTyped() && field.hasInitialExpression()) {
                type = field.getInitialExpression().getType();
            }
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
        if (declaringType.equals(inferredDeclaringType)) {
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

    protected static Expression getObjectExpression(final VariableScope scope) {
        ASTNode node = scope.getEnclosingNode();
        if (node instanceof PropertyExpression) {
            return ((PropertyExpression) node).getObjectExpression();
        }
        if (node instanceof AttributeExpression) {
            return ((AttributeExpression) node).getObjectExpression();
        }
        if (node instanceof MethodCallExpression) {
            return ((MethodCallExpression) node).getObjectExpression();
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
            int majorVersion = Integer.parseInt(GroovySystem.getVersion().split("\\.")[0], 10);
            return (majorVersion >= 3);
        }
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
        // TODO: What about 'method.getDeclaringClass().equals(ClassHelper.GROOVY_OBJECT_TYPE)'?
        return method.isSynthetic() || method.getDeclaringClass().equals(VariableScope.CLOSURE_CLASS_NODE);
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
            } else if (i == (n - 1) && arguments.size() == parameters.length && parameter.isArray()) {
                // argument aligned with the last parameter (an array) may be a vararg
                if (!argument.isArray()) parameter = parameter.getComponentType();
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
            !(source == VariableScope.NULL_TYPE && !target.isPrimitive()) /*&&
            !(source.equals(VariableScope.CLOSURE_CLASS_NODE) && ClassHelper.isSAMType(target))*/) {
            // NOTE: Exact match of Closure to SAM Type creates tie for m(Closure) and m(Comparator)

            result = !GroovyUtils.isAssignable(source, target) ? Boolean.FALSE : null; // not an exact match
        }
        return result;
    }
}
