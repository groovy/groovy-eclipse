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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import groovyjarjarasm.asm.Opcodes;
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
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;
import org.eclipse.jdt.internal.compiler.lookup.LazilyResolvedMethodBinding;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Determines types using AST inspection.
 *
 * @author Andrew Eisenberg
 */
public class SimpleTypeLookup implements ITypeLookupExtension {

    protected GroovyCompilationUnit unit;

    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        this.unit = unit;
    }

    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
        return lookupType(node, scope, objectExpressionType, false);
    }

    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType, boolean isStaticObjectExpression) {
        TypeConfidence[] confidence = new TypeConfidence[] {TypeConfidence.EXACT};
        if (ClassHelper.isPrimitiveType(objectExpressionType)) {
            objectExpressionType = ClassHelper.getWrapper(objectExpressionType);
        }
        ClassNode declaringType = objectExpressionType != null ? objectExpressionType : findDeclaringType(node, scope, confidence);
        TypeLookupResult result = findType(node, declaringType, scope, confidence[0],
            isStaticObjectExpression || (objectExpressionType == null && scope.isStatic()), objectExpressionType == null);

        return result;
    }

    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return new TypeLookupResult(node.getType(), node.getDeclaringClass(), node, TypeConfidence.EXACT, scope);
    }

    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return new TypeLookupResult(node.getReturnType(), node.getDeclaringClass(), node, TypeConfidence.EXACT, scope);
    }

    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        ClassNode baseType = node.getClassNode();
        return new TypeLookupResult(baseType, baseType, baseType, TypeConfidence.EXACT, scope);
    }

    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        ClassNode baseType = node.getType();
        if (baseType != null) {
            return new TypeLookupResult(baseType, baseType, baseType, TypeConfidence.EXACT, scope);
        } else {
            // this is a * import
            return new TypeLookupResult(VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE, TypeConfidence.INFERRED, scope);
        }
    }

    /**
     * always return the passed in node, unless the declaration of an InnerClassNode
     */
    public TypeLookupResult lookupType(ClassNode node, VariableScope scope) {
        ClassNode resultType;
        if (node instanceof InnerClassNode && !node.isRedirectNode()) {
            resultType = node.getSuperClass();
            if (resultType.getName().equals(VariableScope.OBJECT_CLASS_NODE.getName()) && node.getInterfaces().length > 0) {
                resultType = node.getInterfaces()[0];
            }
        } else {
            resultType = node;
        }
        return new TypeLookupResult(resultType, resultType, node, TypeConfidence.EXACT, scope);
    }

    public TypeLookupResult lookupType(Parameter node, VariableScope scope) {
        // look up the type in the current scope to see if the type has
        // has been predetermined (eg- for loop variables)
        VariableInfo info = scope.lookupNameInCurrentScope(node.getName());
        ClassNode type;
        if (info != null) {
            type = info.type;
        } else {
            type = node.getType();
        }
        return new TypeLookupResult(type, scope.getEnclosingTypeDeclaration(), node /* should be methodnode? */, TypeConfidence.EXACT, scope);
    }

    public void lookupInBlock(BlockStatement node, VariableScope scope) {
    }

    //--------------------------------------------------------------------------

    protected ClassNode findDeclaringType(Expression node, VariableScope scope, TypeConfidence[] confidence) {
        if (node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
            return node.getType();

        } else if (node instanceof FieldExpression) {
            return ((FieldExpression) node).getField().getDeclaringClass();

        } else if (node instanceof StaticMethodCallExpression) {
            return ((StaticMethodCallExpression) node).getOwnerType();

        } else if (node instanceof ConstantExpression) {
            if (scope.isMethodCall()) {
                // method call with an implicit this
                return scope.getDelegateOrThis();
            }
        } else if (node instanceof VariableExpression) {
            Variable var = ((VariableExpression) node).getAccessedVariable();
            if (var instanceof DynamicVariable) {
                // search type hierarchy for declaration
                // first look in delegate and hierarchy and then go for this
                ASTNode declaration = null;

                ClassNode delegate = scope.getDelegate();
                if (delegate != null) {
                    declaration = findDeclaration(var.getName(), delegate, scope.containsInThisScope(var.getName()), false, scope.getMethodCallArgumentTypes());
                }

                ClassNode thiz = scope.getThis();
                if (declaration == null && thiz != null && (delegate == null || !thiz.equals(delegate))) {
                    declaration = findDeclaration(var.getName(), thiz, scope.containsInThisScope(var.getName()), false, scope.getMethodCallArgumentTypes());
                }

                ClassNode type;
                if (declaration == null) {
                    // this is a dynamic variable that doesn't seem to have a declaration
                    // it might be an unknown and a mistake, but it could also be declared by 'this'
                    type = thiz != null ? thiz : VariableScope.OBJECT_CLASS_NODE;
                } else {
                    type = getDeclaringTypeFromDeclaration(declaration, var.getType());
                }
                confidence[0] = TypeConfidence.findLessPrecise(confidence[0], TypeConfidence.INFERRED);
                return type;
            } else if (var instanceof FieldNode) {
                return ((FieldNode) var).getDeclaringClass();
            } else if (var instanceof PropertyNode) {
                return ((PropertyNode) var).getDeclaringClass();
            } else if (scope.isThisOrSuper((VariableExpression) node)) { // use 'node' because 'var' may be null
                // this or super expression, but it is not bound, probably because concrete ast was requested
                return scope.lookupName(((VariableExpression) node).getName()).declaringType;
            }
            // else local variable, no declaring type
        }
        return VariableScope.OBJECT_CLASS_NODE;
    }

    protected TypeLookupResult findType(Expression node, ClassNode declaringType, VariableScope scope,
            TypeConfidence confidence, boolean isStaticObjectExpression, boolean isPrimaryExpression) {

        if (node instanceof VariableExpression) {
            return findTypeForVariable((VariableExpression) node, scope, confidence, declaringType);
        }

        ClassNode nodeType = node.getType();
        if ((!isPrimaryExpression || scope.isMethodCall()) && node instanceof ConstantExpression) {
            return findTypeForNameWithKnownObjectExpression(node.getText(), nodeType, declaringType, scope, confidence,
                isStaticObjectExpression, isPrimaryExpression, (scope.getWormhole().remove("lhs") == node));
        }

        // no object expression, look at the kind of expression the following
        // expressions have a type that is constant no matter what their contents are
        if (node instanceof ConstantExpression) {
            // here, we know that since there is no object expression, this is not part
            // of a dotted anything, so we can safely assume that it is a quoted string or
            // some other constant
            ConstantExpression constExpr = (ConstantExpression) node;

            if (constExpr.isTrueExpression() || constExpr.isFalseExpression()) {
                return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);
            } else if (constExpr.isNullExpression()) {
                return new TypeLookupResult(VariableScope.VOID_CLASS_NODE, null, null, confidence, scope);
            } else if (constExpr.isEmptyStringExpression()) {
                return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);
            } else if (ClassHelper.isNumberType(nodeType) || nodeType == ClassHelper.BigDecimal_TYPE
                    || nodeType == ClassHelper.BigInteger_TYPE) {
                if (ClassHelper.isPrimitiveType(nodeType)) {
                    return new TypeLookupResult(ClassHelper.getWrapper(nodeType), null, null, confidence, scope);
                }
                return new TypeLookupResult(nodeType, null, null, confidence, scope);
            } else if (nodeType.equals(VariableScope.STRING_CLASS_NODE)) {
                // likely a proper quoted string constant
                return new TypeLookupResult(nodeType, null, node, confidence, scope);
            } else {
                return new TypeLookupResult(nodeType, null, null, TypeConfidence.UNKNOWN, scope);
            }

        } else if (node instanceof BooleanExpression || node instanceof NotExpression) {
            return new TypeLookupResult(VariableScope.BOOLEAN_CLASS_NODE, null, null, confidence, scope);

        } else if (node instanceof GStringExpression) {
            // note that we return String type here, not GString so that DGMs will apply
            return new TypeLookupResult(VariableScope.STRING_CLASS_NODE, null, null, confidence, scope);

        } else if (node instanceof BitwiseNegationExpression) {
            ClassNode type = ((BitwiseNegationExpression) node).getExpression().getType();
            if (type.getName().equals(VariableScope.STRING_CLASS_NODE.getName())) {
                return new TypeLookupResult(VariableScope.PATTERN_CLASS_NODE, null, null, confidence, scope);
            } else {
                return new TypeLookupResult(type, null, null, confidence, scope);
            }
        } else if (node instanceof ClassExpression) {
            // check for special case...a bit crude...determine if the actual reference is to Foo.class or to Foo
            if (isNodeDotClassReference(node, unit)) {
                return new TypeLookupResult(VariableScope.CLASS_CLASS_NODE, VariableScope.CLASS_CLASS_NODE, VariableScope.CLASS_CLASS_NODE, TypeConfidence.EXACT, scope);
            } else {
                return new TypeLookupResult(nodeType, declaringType, nodeType, confidence, scope);
            }
        } else if (node instanceof StaticMethodCallExpression) {
            String methodName = ((StaticMethodCallExpression) node).getMethod();
            ClassNode ownerType = ((StaticMethodCallExpression) node).getOwnerType();
            Expression methodArgs = ((StaticMethodCallExpression) node).getArguments();

            List<MethodNode> candidates = new LinkedList<MethodNode>();
            if (!ownerType.isInterface()) {
                candidates.addAll(ownerType.getMethods(methodName));
            } else {
                LinkedHashSet<ClassNode> faces = new LinkedHashSet<ClassNode>();
                VariableScope.findAllInterfaces(ownerType, faces, false);
                for (ClassNode face : faces) {
                    candidates.addAll(face.getMethods(methodName));
                }
            }
            for (Iterator<MethodNode> it = candidates.iterator(); it.hasNext();) {
                if (!it.next().isStatic()) it.remove();
            }

            if (!candidates.isEmpty()) {
                MethodNode closestMatch;
                if (methodArgs instanceof ArgumentListExpression) {
                    closestMatch = findMethodDeclaration0(candidates, GroovyUtils.getArgumentTypes((ArgumentListExpression) methodArgs));
                    confidence = TypeConfidence.INFERRED;
                } else {
                    closestMatch = candidates.get(0);
                    confidence = TypeConfidence.LOOSELY_INFERRED;
                }
                return new TypeLookupResult(closestMatch.getReturnType(), closestMatch.getDeclaringClass(), closestMatch, confidence, scope);
            }
        } else if (node instanceof ConstructorCallExpression) {
            ConstructorCallExpression constructorCall = (ConstructorCallExpression) node;
            if (constructorCall.isThisCall()) {
                MethodNode constructorDecl = scope.getEnclosingMethodDeclaration(); // watch for initializers but no constructor
                declaringType = constructorDecl != null ? constructorDecl.getDeclaringClass() : scope.getEnclosingTypeDeclaration();
            } else if (constructorCall.isSuperCall()) {
                declaringType = scope.getEnclosingMethodDeclaration().getDeclaringClass().getUnresolvedSuperClass();
            }

            // try to find best match if there is more than one constructor to choose from
            List<ConstructorNode> declaredConstructors = declaringType.getDeclaredConstructors();
            if (constructorCall.getArguments() instanceof ArgumentListExpression && declaredConstructors.size() > 1) {
                List<ConstructorNode> looseMatches = new ArrayList<ConstructorNode>();
                List<ClassNode> callTypes = GroovyUtils.getArgumentTypes((ArgumentListExpression) constructorCall.getArguments());
                for (ConstructorNode ctor : declaredConstructors) {
                    if (callTypes.size() == ctor.getParameters().length) {
                        if (isTypeCompatible(callTypes, ctor.getParameters()) == Boolean.TRUE) {
                            return new TypeLookupResult(nodeType, declaringType, ctor, TypeConfidence.EXACT, scope);
                        }
                        // argument types may not be fully resolved; at least the number of arguments matched
                        looseMatches.add(ctor);
                    }
                }
                if (!looseMatches.isEmpty()) {
                    declaredConstructors = looseMatches;
                }
            }

            ASTNode declaration = !declaredConstructors.isEmpty() ? declaredConstructors.get(0) : declaringType;
            return new TypeLookupResult(nodeType, declaringType, declaration, confidence, scope);
        }

        // if we get here, then we can't infer the type. Set to unknown if required.
        if (!(node instanceof TupleExpression) && nodeType.equals(VariableScope.OBJECT_CLASS_NODE)) {
            confidence = TypeConfidence.UNKNOWN;
        }

        // don't know
        return new TypeLookupResult(nodeType, declaringType, null, confidence, scope);
    }

    /**
     * Looks for a name within an object expression. It is either in the hierarchy, it is in the variable scope, or it is unknown.
     */
    protected TypeLookupResult findTypeForNameWithKnownObjectExpression(String name, ClassNode type, ClassNode declaringType,
            VariableScope scope, TypeConfidence confidence, boolean isStaticObjectExpression, boolean isPrimaryExpression, boolean isLhsExpression) {

        ClassNode realDeclaringType;
        VariableInfo varInfo;
        TypeConfidence origConfidence = confidence;
        ASTNode declaration = findDeclaration(name, declaringType, isLhsExpression, isStaticObjectExpression, scope.getMethodCallArgumentTypes());

        if (declaration == null && isPrimaryExpression) {
            ClassNode thiz = scope.getThis();
            if (thiz != null && !thiz.equals(declaringType)) {
                // probably in a closure where the delegate has changed
                declaration = findDeclaration(name, thiz, isLhsExpression, isStaticObjectExpression, scope.getMethodCallArgumentTypes());
            }
        }

        // GRECLIPSE-1079
        if (declaration == null && isStaticObjectExpression) {
            // we might have a reference to a property/method defined on java.lang.Class
            declaration = findDeclaration(name, VariableScope.CLASS_CLASS_NODE, isLhsExpression, isStaticObjectExpression, scope.getMethodCallArgumentTypes());
        }

        if (declaration != null) {
            type = getTypeFromDeclaration(declaration, declaringType);
            realDeclaringType = getDeclaringTypeFromDeclaration(declaration, declaringType);
        } else if ("this".equals(name)) {
            // Fix for 'this' as property of ClassName
            declaration = declaringType;
            type = declaringType;
            realDeclaringType = declaringType;
        } else if (isPrimaryExpression &&
            // make everything from the scopes available
                (varInfo = scope.lookupName(name)) != null) {

            // now try to find the declaration again
            type = varInfo.type;
            realDeclaringType = varInfo.declaringType;
            declaration = findDeclaration(name, realDeclaringType, isLhsExpression, isStaticObjectExpression, scope.getMethodCallArgumentTypes());
            if (declaration == null) {
                declaration = varInfo.declaringType;
            }
        } else if (name.equals("call")) {
            // assume that this is a synthetic call method for calling a closure
            realDeclaringType = VariableScope.CLOSURE_CLASS;
            declaration = realDeclaringType.getMethods("call").get(0);
        } else {
            realDeclaringType = declaringType;
            confidence = TypeConfidence.UNKNOWN;
        }

        // now check to see if the object expression is static, but the declaration is not
        if (declaration != null && !realDeclaringType.equals(VariableScope.CLASS_CLASS_NODE)) {
            if (declaration instanceof FieldNode) {
                if (isStaticObjectExpression && !((FieldNode) declaration).isStatic()) {
                    confidence = TypeConfidence.UNKNOWN;
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
                if (isStaticObjectExpression && !((MethodNode) declaration).isStatic()) {
                    confidence = TypeConfidence.UNKNOWN;
                }
            }
        }

        if (confidence == TypeConfidence.UNKNOWN && realDeclaringType.getName().equals(VariableScope.CLASS_CLASS_NODE.getName())) {
            // GRECLIPSE-1544
            // check the type parameter for this class node reference
            // likely a type coming in from STC
            GenericsType[] classTypeParams = realDeclaringType.getGenericsTypes();
            ClassNode typeParam = classTypeParams != null && classTypeParams.length == 1 ? classTypeParams[0].getType() : null;

            if (typeParam != null && !typeParam.getName().equals(VariableScope.CLASS_CLASS_NODE.getName()) &&
                    !typeParam.getName().equals(VariableScope.OBJECT_CLASS_NODE.getName())) {
                return findTypeForNameWithKnownObjectExpression(name, type, typeParam, scope, origConfidence,
                    isStaticObjectExpression, isPrimaryExpression, isLhsExpression);
            }
        }
        return new TypeLookupResult(type, realDeclaringType, declaration, confidence, scope);
    }

    protected TypeLookupResult findTypeForVariable(VariableExpression var, VariableScope scope, TypeConfidence confidence, ClassNode declaringType) {
        ASTNode decl = var;
        ClassNode type = var.getType();
        TypeConfidence newConfidence = confidence;
        Variable accessedVar = var.getAccessedVariable();
        VariableInfo variableInfo = scope.lookupName(var.getName());

        if (accessedVar instanceof ASTNode) {
            decl = (ASTNode) accessedVar;
            if (decl instanceof FieldNode ||
                decl instanceof MethodNode ||
                decl instanceof PropertyNode) {
                // use field/method/property info
                variableInfo = null;
                type = getTypeFromDeclaration(decl, ((AnnotatedNode) decl).getDeclaringClass());
            }
        } else if (accessedVar instanceof DynamicVariable) {
            // likely a reference to a field or method in a type in the hierarchy; find the declaration
            ASTNode candidate = findDeclaration(accessedVar.getName(), getMorePreciseType(declaringType, variableInfo), scope.containsInThisScope(accessedVar.getName()), false, scope.getMethodCallArgumentTypes());
            if (candidate != null) {
                decl = candidate;
                declaringType = getDeclaringTypeFromDeclaration(decl, variableInfo != null ? variableInfo.declaringType : VariableScope.OBJECT_CLASS_NODE);
            } else {
                newConfidence = TypeConfidence.UNKNOWN;
                // dynamic variables are not allowed outside of script mainline
                if (variableInfo != null && !scope.inScriptRunMethod()) variableInfo = null;
            }
            type = getTypeFromDeclaration(decl, declaringType);
        }

        if (variableInfo != null && !(decl instanceof MethodNode)) {
            type = variableInfo.type;
            if (scope.isThisOrSuper(var)) decl = type;
            declaringType = getMorePreciseType(declaringType, variableInfo);
            newConfidence = TypeConfidence.findLessPrecise(confidence, TypeConfidence.INFERRED);
        }

        return new TypeLookupResult(type, declaringType, decl, newConfidence, scope);
    }

    /**
     * Looks for the named member in the declaring type. Also searches super types. The result can be a field, method, or property.
     *
     * @param name the name of the field, method, constant or property to find
     * @param declaringType the type in which the named member's declaration resides
     * @param isLhsExpression {@code true} if named member is being assigned a value
     * @param isStaticExpression {@code true} if member is being accessed statically
     * @param methodCallArgumentTypes types of arguments to the associated method call (or {@code null} if not a method call)
     */
    protected ASTNode findDeclaration(String name, ClassNode declaringType, boolean isLhsExpression, boolean isStaticExpression, List<ClassNode> methodCallArgumentTypes) {
        if (declaringType.isArray()) {
            // only length exists on arrays
            if (name.equals("length")) {
                return createLengthField(declaringType);
            }
            // otherwise search on object
            return findDeclaration(name, VariableScope.OBJECT_CLASS_NODE, isLhsExpression, isStaticExpression, methodCallArgumentTypes);
        }

        if (methodCallArgumentTypes != null) {
            ASTNode method = findMethodDeclaration(name, declaringType, methodCallArgumentTypes);
            if (method != null) {
                return method;
            }
            // name may still map to something that is callable; keep looking
        }

        // look for canonical accessor method
        MethodNode accessor = AccessorSupport.findAccessorMethodForPropertyName(name, declaringType, false, !isLhsExpression ? READER : WRITER);
        if (accessor != null && !isSynthetic(accessor) && (accessor.isStatic() == isStaticExpression)) {
            return accessor;
        }

        LinkedHashSet<ClassNode> typeHierarchy = new LinkedHashSet<ClassNode>();
        VariableScope.createTypeHierarchy(declaringType, typeHierarchy, true);

        // look for property
        for (ClassNode type : typeHierarchy) {
            PropertyNode property = type.getProperty(name);
            if (property != null) {
                return property;
            }
        }

        // look for field
        FieldNode field = declaringType.getField(name);
        if (field != null) {
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
        if (accessor != null) {
            return accessor;
        }

        if (methodCallArgumentTypes == null) {
            // reference may be in method pointer or static import; look for method as last resort
            return findMethodDeclaration(name, declaringType, null);
        }

        return null;
    }

    /**
     * Finds a method with the given name in the declaring type. Will prioritize methods with the same number of arguments,
     * but if multiple methods exist with same name, then will return an arbitrary one.
     *
     * @param checkSuperInterfaces potentially look through super interfaces for a declaration to this method
     */
    protected MethodNode findMethodDeclaration(String name, ClassNode declaringType, List<ClassNode> methodCallArgumentTypes) {
        // if this is an interface, then we also need to check super interfaces;
        // super interface methods on an interface are not returned by getMethods(),
        // so must explicitly look for them -- do this first since findAllInterfaces
        // will return the current interface as well and this will avoid running this
        // method on the same interface twice
        if (declaringType.isInterface()) {
            LinkedHashSet<ClassNode> allInterfaces = new LinkedHashSet<ClassNode>();
            VariableScope.findAllInterfaces(declaringType, allInterfaces, true);

            MethodNode outerCandidate = null;
            interfacesSearch: for (ClassNode interf : allInterfaces) {
                MethodNode innerCandidate = null;
                List<MethodNode> candidates = interf.getMethods(name);
                if (!candidates.isEmpty()) {
                    innerCandidate = findMethodDeclaration0(candidates, methodCallArgumentTypes);
                    if (outerCandidate == null) {
                        outerCandidate = innerCandidate;
                    }
                }

                // should we try to find more precise match or stop here?
                if (innerCandidate != null && methodCallArgumentTypes != null) {
                    Parameter[] methodParameters = innerCandidate.getParameters();
                    if (methodCallArgumentTypes.isEmpty() && methodParameters.length == 0) {
                        return innerCandidate;
                    }
                    if (methodCallArgumentTypes.size() == methodParameters.length) {
                        outerCandidate = innerCandidate;

                        Boolean suitable = isTypeCompatible(methodCallArgumentTypes, methodParameters);
                        if (suitable == Boolean.FALSE) {
                            continue interfacesSearch;
                        }
                        if (suitable == Boolean.TRUE) {
                            return innerCandidate;
                        }
                    }
                }
            }
            return outerCandidate;
        }

        List<MethodNode> candidates = declaringType.getMethods(name);
        if (!candidates.isEmpty()) {
            return findMethodDeclaration0(candidates, methodCallArgumentTypes);
        }

        return null;
    }

    protected MethodNode findMethodDeclaration0(List<MethodNode> candidates, List<ClassNode> arguments) {
        // remember first entry in case exact match not found
        MethodNode closestMatch = candidates.get(0);
        if (arguments == null) {
            return closestMatch;
        }

        // prefer retrieving the method with the same number of args as specified in the parameter.
        // if none exists, or parameter is -1, then arbitrarily choose the first.
        for (Iterator<MethodNode> iterator = candidates.iterator(); iterator.hasNext();) {
            MethodNode maybeMethod = iterator.next();
            Parameter[] parameters = maybeMethod.getParameters();
            if (parameters.length == 0 && arguments.isEmpty()) {
                return maybeMethod.getOriginal();
            }
            if (parameters.length == arguments.size()) {
                Boolean suitable = isTypeCompatible(arguments, parameters);
                if (suitable == Boolean.TRUE) {
                    return maybeMethod.getOriginal();
                }
                if (suitable != Boolean.FALSE) {
                    closestMatch = maybeMethod.getOriginal();
                    continue; // don't remove
                }
            }
            iterator.remove();
        }
        return closestMatch;
    }

    //--------------------------------------------------------------------------
    // TODO: Can any of these be relocated for reuse?

    protected static final AccessorSupport[] READER = {AccessorSupport.GETTER, AccessorSupport.ISSER};
    protected static final AccessorSupport[] WRITER = {AccessorSupport.SETTER};

    protected static ASTNode createLengthField(ClassNode declaringType) {
        FieldNode lengthField = new FieldNode("length", Opcodes.ACC_PUBLIC, VariableScope.INTEGER_CLASS_NODE, declaringType, null);
        lengthField.setType(VariableScope.INTEGER_CLASS_NODE);
        lengthField.setDeclaringClass(declaringType);
        return lengthField;
    }

    protected static ClassNode getMorePreciseType(ClassNode declaringType, VariableInfo info) {
        ClassNode maybeDeclaringType = info != null ? info.declaringType : VariableScope.OBJECT_CLASS_NODE;
        if (maybeDeclaringType.equals(VariableScope.OBJECT_CLASS_NODE) && !VariableScope.OBJECT_CLASS_NODE.equals(declaringType)) {
            return declaringType;
        } else {
            return maybeDeclaringType;
        }
    }

    /**
     * @param declaration the declaration to look up
     * @param resolvedType the unredirected type that declares this declaration somewhere in its hierarchy
     * @return class node with generics replaced by actual types
     */
    protected static ClassNode getTypeFromDeclaration(ASTNode declaration, ClassNode resolvedType) {
        ClassNode typeOfDeclaration, declaringType = getDeclaringTypeFromDeclaration(declaration, resolvedType);
        if (declaration instanceof PropertyNode) {
            FieldNode field = ((PropertyNode) declaration).getField();
            if (field != null) {
                declaration = field;
            }
        }
        if (declaration instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode) declaration;
            typeOfDeclaration = fieldNode.getType();
            if (VariableScope.OBJECT_CLASS_NODE.equals(typeOfDeclaration)) {
                // check to see if we can do better by looking at the initializer of the field
                if (fieldNode.hasInitialExpression()) {
                    typeOfDeclaration = fieldNode.getInitialExpression().getType();
                }
            }
        } else if (declaration instanceof MethodNode) {
            typeOfDeclaration = ((MethodNode) declaration).getReturnType();
        } else if (declaration instanceof Expression) {
            typeOfDeclaration = ((Expression) declaration).getType();
        } else {
            typeOfDeclaration = VariableScope.OBJECT_CLASS_NODE;
        }

        // now try to resolve generics
        // travel up the hierarchy and look for more generics
        // also look for generics on methods...(not doing this yet...)
        GenericsMapper mapper = GenericsMapper.gatherGenerics(resolvedType, declaringType.redirect());
        ClassNode resolvedTypeOfDeclaration = VariableScope.resolveTypeParameterization(mapper, VariableScope.clone(typeOfDeclaration));
        return resolvedTypeOfDeclaration;
    }

    protected static ClassNode getDeclaringTypeFromDeclaration(ASTNode declaration, ClassNode resolvedTypeOfDeclaration) {
        ClassNode typeOfDeclaration;
        if (declaration instanceof FieldNode) {
            typeOfDeclaration = ((FieldNode) declaration).getDeclaringClass();
        } else if (declaration instanceof MethodNode) {
            typeOfDeclaration = ((MethodNode) declaration).getDeclaringClass();
        } else if (declaration instanceof PropertyNode) {
            typeOfDeclaration = ((PropertyNode) declaration).getDeclaringClass();
        } else {
            typeOfDeclaration = VariableScope.OBJECT_CLASS_NODE;
        }
        // don't necessarily use the typeOfDeclaration. the resolvedTypeOfDeclaration includes the types of generics
        // so if the names are the same, then used the resolved version
        if (typeOfDeclaration.getName().equals(resolvedTypeOfDeclaration.getName())) {
            return resolvedTypeOfDeclaration;
        } else {
            return typeOfDeclaration;
        }
    }

    /**
     * a little crude because will not find if there are spaces between '.' and 'class'
     */
    protected static boolean isNodeDotClassReference(Expression node, CompilationUnit unit) {
        int end = node.getEnd();
        int start = node.getStart();
        if (unit.exists()) { // will return false if unit was moved during inferencing operation
            char[] contents = unit.getContents();
            if (contents.length >= end) {
                char[] realText = new char[end - start];
                System.arraycopy(contents, start, realText, 0, end - start);
                String realTextStr = String.valueOf(realText).trim();
                return realTextStr.endsWith(".class") || realTextStr.endsWith(".class.");
            }
        }
        return false;
    }

    /**
     * Determines if the specified method node is synthetic (i.e. generated or
     * implicit in some sense).
     */
    protected static boolean isSynthetic(MethodNode method) {
        // TODO: What about 'method.getDeclaringClass().equals(ClassHelper.GROOVY_OBJECT_TYPE)'?
        return method.isSynthetic() || method.getDeclaringClass().equals(ClassHelper.CLOSURE_TYPE) ||
            (method instanceof JDTMethodNode && ((JDTMethodNode) method).getJdtBinding() instanceof LazilyResolvedMethodBinding);
    }

    /**
     * Determines if the given argument types are compatible with the declaration parameters.
     *
     * @return {@link Boolean#TRUE true} for exact match, {@code null} for fuzzy match, and {@link Boolean#FALSE false} for not a match
     */
    protected static Boolean isTypeCompatible(List<ClassNode> arguments, Parameter[] parameters) {
        // TODO: Add handling for variadic methods/constructors.
        // TODO: Can anything be learned from org.codehaus.groovy.ast.ClassNode.tryFindPossibleMethod(String, Expression)?

        Boolean result = Boolean.TRUE;
        for (int i = 0, n = parameters.length; i < n; i += 1) {
            ClassNode parameter = parameters[i].getType(), argument = arguments.get(i);

            // test parameter and argument for exact and fuzzy match

            if (!parameter.equals(argument) &&
                    !(argument == GroovyUtils.NULL_TYPE && !parameter.isPrimitive()) &&
                    !(argument.equals(ClassHelper.CLOSURE_TYPE) && ClassHelper.isSAMType(parameter))) {

                result = null; // not an exact match

                try { // this matches primitives more thoroughly, but getTypeClass can fail if class has not been loaded
                    if (!MetaClassHelper.isAssignableFrom(parameter.getTypeClass(), argument.getTypeClass())) {
                        result = Boolean.FALSE;
                        break;
                    }
                } catch (Throwable boo) {
                    if (parameter.isInterface()) {
                        if (!argument.declaresInterface(parameter)) {
                            result = Boolean.FALSE;
                            break;
                        }
                    } else {
                        // TODO 'null' literal argument should be correctly resolved
                        if (!argument.isDerivedFrom(parameter)) {
                            result = Boolean.FALSE;
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
}
