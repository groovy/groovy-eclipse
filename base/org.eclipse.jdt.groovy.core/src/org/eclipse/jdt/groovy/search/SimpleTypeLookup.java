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

import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.EXACT;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.INFERRED;
import static org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence.UNKNOWN;
import static org.eclipse.jdt.groovy.search.VariableScope.NO_GENERICS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTMethodNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.TypeLookupResult.TypeConfidence;
import org.eclipse.jdt.groovy.search.VariableScope.VariableInfo;

/**
 * Determines types from the AST.
 *
 * @author Andrew Eisenberg
 */
public class SimpleTypeLookup implements ITypeLookupExtension {

    private GroovyCompilationUnit unit;

    public void initialize(GroovyCompilationUnit unit, VariableScope topLevelScope) {
        this.unit = unit;
    }

    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType) {
        return lookupType(node, scope, objectExpressionType, false);
    }

    public TypeLookupResult lookupType(Expression node, VariableScope scope, ClassNode objectExpressionType, boolean isStaticObjectExpression) {
        TypeConfidence[] confidence = new TypeConfidence[] {EXACT};
        if (ClassHelper.isPrimitiveType(objectExpressionType)) {
            objectExpressionType = ClassHelper.getWrapper(objectExpressionType);
        }
        ClassNode declaringType = objectExpressionType != null ? objectExpressionType : findDeclaringType(node, scope, confidence);
        TypeLookupResult result = findType(node, declaringType, scope, confidence[0],
            isStaticObjectExpression || (objectExpressionType == null && scope.isStatic()), objectExpressionType == null);

        return result;
    }

    public TypeLookupResult lookupType(FieldNode node, VariableScope scope) {
        return new TypeLookupResult(node.getType(), node.getDeclaringClass(), node, EXACT, scope);
    }

    public TypeLookupResult lookupType(MethodNode node, VariableScope scope) {
        return new TypeLookupResult(node.getReturnType(), node.getDeclaringClass(), node, EXACT, scope);
    }

    public TypeLookupResult lookupType(AnnotationNode node, VariableScope scope) {
        ClassNode baseType = node.getClassNode();
        return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
    }

    public TypeLookupResult lookupType(ImportNode node, VariableScope scope) {
        ClassNode baseType = node.getType();
        if (baseType != null) {
            return new TypeLookupResult(baseType, baseType, baseType, EXACT, scope);
        } else {
            // this is a * import
            return new TypeLookupResult(VariableScope.OBJECT_CLASS_NODE,
                VariableScope.OBJECT_CLASS_NODE, VariableScope.OBJECT_CLASS_NODE, INFERRED, scope);
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
        return new TypeLookupResult(resultType, resultType, node, EXACT, scope);
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
        return new TypeLookupResult(type, scope.getEnclosingTypeDeclaration(), node /* should be methodnode? */, EXACT, scope);
    }

    public void lookupInBlock(BlockStatement node, VariableScope scope) {
    }

    private ClassNode findDeclaringType(Expression node, VariableScope scope, TypeConfidence[] confidence) {
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
                    declaration = findDeclaration(var.getName(), delegate, scope.containsInThisScope(var.getName()), scope.getMethodCallArgumentTypes());
                }

                ClassNode thiz = scope.getThis();
                if (declaration == null && thiz != null && (delegate == null || !thiz.equals(delegate))) {
                    declaration = findDeclaration(var.getName(), thiz, scope.containsInThisScope(var.getName()), scope.getMethodCallArgumentTypes());
                }

                ClassNode type;
                if (declaration == null) {
                    // this is a dynamic variable that doesn't seem to have a declaration
                    // it might be an unknown and a mistake, but it could also be declared by 'this'
                    type = thiz != null ? thiz : VariableScope.OBJECT_CLASS_NODE;
                } else {
                    type = declaringTypeFromDeclaration(declaration, var.getType());
                }
                confidence[0] = TypeConfidence.findLessPrecise(confidence[0], INFERRED);
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

    private TypeLookupResult findType(Expression node, ClassNode declaringType, VariableScope scope,
            TypeConfidence confidence, boolean isStaticObjectExpression, boolean isPrimaryExpression) {

        if (node instanceof VariableExpression) {
            return findTypeForVariable((VariableExpression) node, scope, confidence, declaringType);
        }

        ClassNode nodeType = node.getType();
        if ((!isPrimaryExpression || scope.isMethodCall()) && node instanceof ConstantExpression) {
            return findTypeForNameWithKnownObjectExpression(node.getText(), nodeType, declaringType, scope, confidence,
                isStaticObjectExpression, isPrimaryExpression, (scope.getWormhole().remove("lhs") == node ? Boolean.TRUE : Boolean.FALSE));
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
                return new TypeLookupResult(nodeType, null, null, UNKNOWN, scope);
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
            if (nodeIsDotClassReference(node)) {
                return new TypeLookupResult(VariableScope.CLASS_CLASS_NODE, VariableScope.CLASS_CLASS_NODE,
                        VariableScope.CLASS_CLASS_NODE, TypeConfidence.EXACT, scope);
            } else {
                return new TypeLookupResult(nodeType, declaringType, nodeType, confidence, scope);
            }
        } else if (node instanceof StaticMethodCallExpression) {
            StaticMethodCallExpression expr = (StaticMethodCallExpression) node;
            List<MethodNode> methods = expr.getOwnerType().getMethods(expr.getMethod());
            if (methods.size() > 0) {
                MethodNode method = methods.get(0);
                return new TypeLookupResult(method.getReturnType(), method.getDeclaringClass(), method, confidence, scope);
            }
        } else if (node instanceof ConstructorCallExpression) {
            List<ConstructorNode> declaredConstructors = declaringType.getDeclaredConstructors();
            if (declaredConstructors != null && declaredConstructors.size() > 0) {
                // FIXADE we can do better here and at least match on number of arguments
                return new TypeLookupResult(nodeType, declaringType, declaredConstructors.get(0), confidence, scope);
            }
            return new TypeLookupResult(nodeType, declaringType, declaringType, confidence, scope);
        }

        // if we get here, then we can't infer the type. Set to unknown if required.
        if (!(node instanceof TupleExpression) && nodeType.equals(VariableScope.OBJECT_CLASS_NODE)) {
            confidence = UNKNOWN;
        }

        // don't know
        return new TypeLookupResult(nodeType, declaringType, null, confidence, scope);
    }

    /**
     * a little crude because will not find if there are spaces between '.' and 'class'
     */
    private boolean nodeIsDotClassReference(Expression node) {
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
     * Looks for a name within an object expression. It is either in the hierarchy, it is in the variable scope, or it is unknown.
     */
    private TypeLookupResult findTypeForNameWithKnownObjectExpression(String name, ClassNode type, ClassNode declaringType,
            VariableScope scope, TypeConfidence confidence, boolean isStaticObjectExpression, boolean isPrimaryExpression, boolean isLhsExpression) {

        ClassNode realDeclaringType;
        VariableInfo varInfo;
        TypeConfidence origConfidence = confidence;
        ASTNode declaration = findDeclaration(name, declaringType, isLhsExpression, scope.getMethodCallArgumentTypes());

        if (declaration == null && isPrimaryExpression) {
            ClassNode thiz = scope.getThis();
            if (thiz != null && !thiz.equals(declaringType)) {
                // probably in a closure where the delegate has changed
                declaration = findDeclaration(name, thiz, isLhsExpression, scope.getMethodCallArgumentTypes());
            }
        }

        // GRECLIPSE-1079
        if (declaration == null && isStaticObjectExpression) {
            // we might have a reference to a property/method defined on java.lang.Class
            declaration = findDeclaration(name, VariableScope.CLASS_CLASS_NODE, isLhsExpression, scope.getMethodCallArgumentTypes());
        }

        if (declaration != null) {
            type = typeFromDeclaration(declaration, declaringType);
            realDeclaringType = declaringTypeFromDeclaration(declaration, declaringType);
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
            declaration = findDeclaration(name, realDeclaringType, isLhsExpression, scope.getMethodCallArgumentTypes());
            if (declaration == null) {
                declaration = varInfo.declaringType;
            }
        } else if (name.equals("call")) {
            // assume that this is a synthetic call method for calling a closure
            realDeclaringType = VariableScope.CLOSURE_CLASS;
            declaration = realDeclaringType.getMethods("call").get(0);
        } else {
            realDeclaringType = declaringType;
            confidence = UNKNOWN;
        }

        // now check to see if the object expression is static, but the declaration is not
        if (declaration != null && !realDeclaringType.equals(VariableScope.CLASS_CLASS_NODE)) {
            if (declaration instanceof FieldNode) {
                if (isStaticObjectExpression && !((FieldNode) declaration).isStatic()) {
                    confidence = UNKNOWN;
                }
            } else if (declaration instanceof PropertyNode) {
                FieldNode underlyingField = ((PropertyNode) declaration).getField();
                if (underlyingField != null) {
                    // prefer looking at the underlying field
                    if (isStaticObjectExpression && !underlyingField.isStatic()) {
                        confidence = UNKNOWN;
                    }
                } else if (isStaticObjectExpression && !((PropertyNode) declaration).isStatic()) {
                    confidence = UNKNOWN;
                }
            } else if (declaration instanceof MethodNode) {
                if (isStaticObjectExpression && !((MethodNode) declaration).isStatic()) {
                    confidence = UNKNOWN;
                }
            }
        }

        if (confidence == UNKNOWN && realDeclaringType.getName().equals(VariableScope.CLASS_CLASS_NODE.getName())) {
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

    private TypeLookupResult findTypeForVariable(VariableExpression var, VariableScope scope, TypeConfidence confidence, ClassNode declaringType) {
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
                // use field/method/property type info
                variableInfo = null;
            }
        } else if (accessedVar instanceof DynamicVariable) {
            // this is likely a reference to a field or method in a type in the hierarchy find the declaration
            ASTNode maybeDeclaration = findDeclaration(accessedVar.getName(), getMorePreciseType(declaringType, variableInfo),
                scope.containsInThisScope(accessedVar.getName()), scope.getMethodCallArgumentTypes());
            if (maybeDeclaration != null) {
                decl = maybeDeclaration;
                // declaring type may have changed
                declaringType = declaringTypeFromDeclaration(decl, variableInfo != null ? variableInfo.declaringType : VariableScope.OBJECT_CLASS_NODE);
            } else {
                newConfidence = UNKNOWN;
            }
        } else {
            assert accessedVar == null;
        }

        if (variableInfo != null) {
            type = variableInfo.type;
            if (scope.isThisOrSuper(var)) decl = type;
            declaringType = getMorePreciseType(declaringType, variableInfo);
            newConfidence = TypeConfidence.findLessPrecise(confidence, INFERRED);
        } else if (accessedVar instanceof DynamicVariable) {
            type = typeFromDeclaration(decl, declaringType);
        }

        return new TypeLookupResult(type, declaringType, decl, newConfidence, scope);
    }

    private ClassNode getMorePreciseType(ClassNode declaringType, VariableInfo info) {
        ClassNode maybeDeclaringType = info != null ? info.declaringType : VariableScope.OBJECT_CLASS_NODE;
        if (maybeDeclaringType.equals(VariableScope.OBJECT_CLASS_NODE) && !VariableScope.OBJECT_CLASS_NODE.equals(declaringType)) {
            return declaringType;
        } else {
            return maybeDeclaringType;
        }
    }

    static ClassNode declaringTypeFromDeclaration(ASTNode declaration, ClassNode resolvedTypeOfDeclaration) {
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
     * @param declaration the declaration to look up
     * @param resolvedType the unredirected type that declares this declaration somewhere in its hierarchy
     * @return class node with generics replaced by actual types
     */
    static ClassNode typeFromDeclaration(ASTNode declaration, ClassNode resolvedType) {
        ClassNode typeOfDeclaration, declaringType = declaringTypeFromDeclaration(declaration, resolvedType);
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
        ClassNode resolvedTypeOfDeclaration = VariableScope.resolveTypeParameterization(mapper,
                VariableScope.clone(typeOfDeclaration));
        return resolvedTypeOfDeclaration;
    }

    protected GenericsType[] unresolvedGenericsForType(ClassNode unresolvedType) {
        ClassNode candidate = unresolvedType;
        GenericsType[] gts = candidate.getGenericsTypes();
        gts = gts == null ? NO_GENERICS : gts;
        List<GenericsType> allGs = new ArrayList<GenericsType>(2);
        while (candidate != null) {
            gts = candidate.getGenericsTypes();
            gts = gts == null ? NO_GENERICS : gts;
            for (GenericsType gt : gts) {
                allGs.add(gt);
            }
            candidate = candidate.getSuperClass();
        }
        return allGs.toArray(NO_GENERICS);
    }

    /**
     * Looks for the named member in the declaring type. Also searches super types. The result can be a field, method, or property.
     *
     * @param name the name of the field, method, constant or property to find
     * @param declaringType the type in which the named member's declaration resides
     * @param isLhsExpression {@code true} if named member is being assigned a value
     * @param methodCallArgumentTypes types of arguments to the associated method call (or {@code null} if not a method call)
     */
    private ASTNode findDeclaration(String name, ClassNode declaringType, boolean isLhsExpression, List<ClassNode> methodCallArgumentTypes) {
        if (declaringType.isArray()) {
            // only length exists on arrays
            if (name.equals("length")) {
                return createLengthField(declaringType);
            }
            // otherwise search on object
            return findDeclaration(name, VariableScope.OBJECT_CLASS_NODE, isLhsExpression, methodCallArgumentTypes);
        }

        if (methodCallArgumentTypes != null) {
            ASTNode method = findMethodDeclaration(name, declaringType, methodCallArgumentTypes, true);
            if (method != null) {
                return method;
            }
            // name may still map to something that is callable; keep looking
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

        // look for canonical getter or setter
        MethodNode accessor = AccessorSupport.findAccessorMethodForPropertyName(name, declaringType, false, !isLhsExpression ? READER : WRITER);
        if (accessor instanceof JDTMethodNode && !accessor.isStatic() && !accessor.isSynthetic()) {
            return accessor;
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
            return findMethodDeclaration(name, declaringType, null, true);
        }

        return null;
    }

    private static final AccessorSupport[] READER = {AccessorSupport.GETTER, AccessorSupport.ISSER};
    private static final AccessorSupport[] WRITER = {AccessorSupport.SETTER};

    /**
     * Finds a method with the given name in the declaring type. Will prioritize methods with the same number of arguments, but if
     * multiple methods exist with same name, then will return an arbitrary one.
     *
     * @param checkSuperInterfaces potentially look through super interfaces for a declaration to this method
     */
    private AnnotatedNode findMethodDeclaration(String name, ClassNode declaringType, List<ClassNode> methodCallArgumentTypes, boolean checkSuperInterfaces) {
        // if this is an interface, then we also need to check super interfaces
        // super interface methods on an interface are not returned by getMethods(), so must explicitly look for them
        // do this piece first since findAllInterfaces will return the current interface as well and this will avoid running this
        // method on the same interface twice.
        if (checkSuperInterfaces && declaringType.isInterface()) {
            LinkedHashSet<ClassNode> allInterfaces = new LinkedHashSet<ClassNode>();
            VariableScope.findAllInterfaces(declaringType, allInterfaces, true);
            AnnotatedNode candidate = null;
            interfacesSearch: for (ClassNode interf : allInterfaces) {
                AnnotatedNode methodDeclaration = findMethodDeclaration(name, interf, methodCallArgumentTypes, false);

                if (candidate == null) {
                    candidate = methodDeclaration;
                }

                // should we try to find more precise match or stop here?
                if (methodDeclaration != null && methodCallArgumentTypes != null) {
                    Parameter[] methodParameters = ((MethodNode) methodDeclaration).getParameters();
                    if (methodCallArgumentTypes.size() == 0 && methodParameters.length == 0) {
                        return methodDeclaration;
                    } else if (methodCallArgumentTypes.size() == methodParameters.length) {
                        candidate = methodDeclaration;
                        boolean exactMatchFound = true;
                        for (int i = 0; i < methodParameters.length; i++) {
                            if (!methodCallArgumentTypes.get(i).equals(methodParameters[i].getType())) {
                                exactMatchFound = false;
                            }
                            if (methodParameters[i].getType().isInterface()) {
                                if (!methodCallArgumentTypes.get(i).declaresInterface(methodParameters[i].getType())) {
                                    continue interfacesSearch;
                                }
                            } else {
                                if (!methodCallArgumentTypes.get(i).isDerivedFrom(methodParameters[i].getType())) {
                                    continue interfacesSearch;
                                }
                            }
                        }
                        if (exactMatchFound) {
                            return methodDeclaration;
                        }
                    }
                }
            }
            return candidate;
        }

        List<MethodNode> maybeMethods = declaringType.getMethods(name);
        if (maybeMethods != null && !maybeMethods.isEmpty()) {
            // Remember first entry in case exact match not found
            MethodNode closestMatch = maybeMethods.get(0);
            if (methodCallArgumentTypes == null) {
                return closestMatch;
            }

            // prefer retrieving the method with the same number of args as specified in the parameter.
            // if none exists, or parameter is -1, then arbitrarily choose the first.
            for (Iterator<MethodNode> iterator = maybeMethods.iterator(); iterator.hasNext();) {
                MethodNode maybeMethod = iterator.next();
                Parameter[] parameters = maybeMethod.getParameters();
                if ((parameters == null || parameters.length == 0) && methodCallArgumentTypes.isEmpty()) {
                    return maybeMethod.getOriginal();
                }
                if (parameters != null && parameters.length == methodCallArgumentTypes.size()) {
                    boolean found = true;
                    boolean exactMatchFound = true;
                    closestMatch = maybeMethod.getOriginal();
                    for (int i = 0; i < parameters.length; i++) {
                        if (!methodCallArgumentTypes.get(i).equals(parameters[i].getType())) {
                            exactMatchFound = false;
                        }
                        if (parameters[i].getType().isInterface()) {
                            if (!methodCallArgumentTypes.get(i).declaresInterface(parameters[i].getType())) {
                                found = false;
                                break;
                            }
                        } else {
                            // TODO 'null' literal argument should be correctly resolved
                            if (!methodCallArgumentTypes.get(i).isDerivedFrom(parameters[i].getType())) {
                                found = false;
                                break;
                            }
                        }
                    }
                    if (exactMatchFound) {
                        return closestMatch;
                    }
                    if (!found) {
                        iterator.remove();
                    }
                } else {
                    iterator.remove();
                }
            }
            return closestMatch;
        }
        return null;
    }

    private ASTNode createLengthField(ClassNode declaringType) {
        FieldNode lengthField = new FieldNode("length", Opcodes.ACC_PUBLIC, VariableScope.INTEGER_CLASS_NODE, declaringType, null);
        lengthField.setType(VariableScope.INTEGER_CLASS_NODE);
        lengthField.setDeclaringClass(declaringType);
        return lengthField;
    }
}
