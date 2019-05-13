/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.transform.sc;

import groovy.lang.Reference;
import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationMopWriter;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import groovyjarjarasm.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.ast.tools.GenericsUtils.addMethodGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.applyGenericsContextToPlaceHolders;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.extractSuperClassGenerics;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.COMPONENT_TYPE;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_BRIDGE_METHODS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_FIELDS_ACCESSORS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PRIVATE_FIELDS_MUTATORS;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;
import static groovyjarjarasm.asm.Opcodes.ACC_STATIC;
import static groovyjarjarasm.asm.Opcodes.ACC_SYNTHETIC;

/**
 * This visitor is responsible for amending the AST with static compilation metadata or transform the AST so that
 * a class or a method can be statically compiled. It may also throw errors specific to static compilation which
 * are not considered as an error at the type check pass. For example, usage of spread operator is not allowed
 * in statically compiled portions of code, while it may be statically checked.
 *
 * Static compilation relies on static type checking, which explains why this visitor extends the type checker
 * visitor.
 */
public class StaticCompilationVisitor extends StaticTypeCheckingVisitor {
    private static final ClassNode TYPECHECKED_CLASSNODE = ClassHelper.make(TypeChecked.class);
    private static final ClassNode COMPILESTATIC_CLASSNODE = ClassHelper.make(CompileStatic.class);
    private static final ClassNode[] TYPECHECKED_ANNOTATIONS = {TYPECHECKED_CLASSNODE, COMPILESTATIC_CLASSNODE};

    public static final ClassNode ARRAYLIST_CLASSNODE = ClassHelper.make(ArrayList.class);
    public static final MethodNode ARRAYLIST_CONSTRUCTOR;
    public static final MethodNode ARRAYLIST_ADD_METHOD = ARRAYLIST_CLASSNODE.getMethod("add", new Parameter[]{new Parameter(ClassHelper.OBJECT_TYPE, "o")});

    static {
        ARRAYLIST_CONSTRUCTOR = new ConstructorNode(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        ARRAYLIST_CONSTRUCTOR.setDeclaringClass(StaticCompilationVisitor.ARRAYLIST_CLASSNODE);
    }

    private final TypeChooser typeChooser = new StaticTypesTypeChooser();

    private ClassNode classNode;

    public StaticCompilationVisitor(final SourceUnit unit, final ClassNode node) {
        super(unit, node);
    }

    @Override
    protected ClassNode[] getTypeCheckingAnnotations() {
        return TYPECHECKED_ANNOTATIONS;
    }

    public static boolean isStaticallyCompiled(AnnotatedNode node) {
        if (node.getNodeMetaData(STATIC_COMPILE_NODE)!=null) return (Boolean)node.getNodeMetaData(STATIC_COMPILE_NODE);
        if (node instanceof MethodNode) {
            return isStaticallyCompiled(node.getDeclaringClass());
        }
        if (node instanceof InnerClassNode) {
            return isStaticallyCompiled(((InnerClassNode)node).getOuterClass());
        }
        return false;
    }

    private void addPrivateFieldAndMethodAccessors(ClassNode node) {
        addPrivateBridgeMethods(node);
        addPrivateFieldsAccessors(node);
        Iterator<InnerClassNode> it = node.getInnerClasses();
        while (it.hasNext()) {
            addPrivateFieldAndMethodAccessors(it.next());
        }
    }

    private void addDynamicOuterClassAccessorsCallback(final ClassNode outer) {
        if (outer != null && !isStaticallyCompiled(outer)
                && outer.getNodeMetaData(StaticCompilationMetadataKeys.DYNAMIC_OUTER_NODE_CALLBACK) == null) {
            outer.putNodeMetaData(StaticCompilationMetadataKeys.DYNAMIC_OUTER_NODE_CALLBACK, new CompilationUnit.PrimaryClassNodeOperation() {
                @Override
                public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                    if (classNode == outer) {
                        addPrivateBridgeMethods(classNode);
                        addPrivateFieldsAccessors(classNode);
                    }
                }
            });
        }
    }

    @Override
    public void visitClass(final ClassNode node) {
        boolean skip = shouldSkipClassNode(node);
        if (!skip && !anyMethodSkip(node)) {
            node.putNodeMetaData(MopWriter.Factory.class, StaticCompilationMopWriter.FACTORY);
        }
        ClassNode oldCN = classNode;
        classNode = node;
        Iterator<InnerClassNode> innerClasses = classNode.getInnerClasses();
        while (innerClasses.hasNext()) {
            InnerClassNode innerClassNode = innerClasses.next();
            boolean innerStaticCompile = !(skip || isSkippedInnerClass(innerClassNode));
            innerClassNode.putNodeMetaData(STATIC_COMPILE_NODE, innerStaticCompile);
            innerClassNode.putNodeMetaData(WriterControllerFactory.class, node.getNodeMetaData(WriterControllerFactory.class));
            if (innerStaticCompile && !anyMethodSkip(innerClassNode)) {
                innerClassNode.putNodeMetaData(MopWriter.Factory.class, StaticCompilationMopWriter.FACTORY);
            }
        }
        super.visitClass(node);
        addPrivateFieldAndMethodAccessors(node);
        if (isStaticallyCompiled(node)) addDynamicOuterClassAccessorsCallback(node.getOuterClass());
        classNode = oldCN;
    }

    private boolean anyMethodSkip(final ClassNode node) {
        for (MethodNode methodNode : node.getMethods()) {
            if (isSkipMode(methodNode)) return true;
        }
        return false;
    }

    /**
     * If we are in a constructor, that is static compiled, but in a class, that
     * is not, it may happen that init code from object initializers, fields
     * or properties is added into the constructor code. The backend assumes
     * a purely static constructor, so it may fail if it encounters dynamic
     * code here. Thus we make this kind of code fail
     */
    private void checkForConstructorWithCSButClassWithout(MethodNode node) {
        if (!(node instanceof ConstructorNode)) return;
        Object meta = node.getNodeMetaData(STATIC_COMPILE_NODE);
        if (!Boolean.TRUE.equals(meta)) return;
        ClassNode clz = typeCheckingContext.getEnclosingClassNode();
        meta = clz.getNodeMetaData(STATIC_COMPILE_NODE);
        if (Boolean.TRUE.equals(meta)) return;
        if (    clz.getObjectInitializerStatements().isEmpty() &&
                clz.getFields().isEmpty() &&
                clz.getProperties().isEmpty())
        {
            return;
        }

        addStaticTypeError("Cannot statically compile constructor implicitly including non static elements from object initializers, properties or fields.",node);
    }

    @Override
    public void visitMethod(final MethodNode node) {
        if (isSkipMode(node)) {
            node.putNodeMetaData(STATIC_COMPILE_NODE, false);
        }
        super.visitMethod(node);
        checkForConstructorWithCSButClassWithout(node);
        if (isStaticallyCompiled(node)) addDynamicOuterClassAccessorsCallback(node.getDeclaringClass());
    }

    /**
     * Adds special accessors and mutators for private fields so that inner classes can get/set them
     */
    private static void addPrivateFieldsAccessors(ClassNode node) {
        Set<ASTNode> accessedFields = (Set<ASTNode>) node.getNodeMetaData(StaticTypesMarker.PV_FIELDS_ACCESS);
        Set<ASTNode> mutatedFields = (Set<ASTNode>) node.getNodeMetaData(StaticTypesMarker.PV_FIELDS_MUTATION);
        if (accessedFields == null && mutatedFields == null) return;
        Map<String, MethodNode> privateFieldAccessors = (Map<String, MethodNode>) node.getNodeMetaData(PRIVATE_FIELDS_ACCESSORS);
        Map<String, MethodNode> privateFieldMutators = (Map<String, MethodNode>) node.getNodeMetaData(PRIVATE_FIELDS_MUTATORS);
        if (privateFieldAccessors != null || privateFieldMutators != null) {
            // already added
            return;
        }
        int acc = -1;
        privateFieldAccessors = accessedFields != null ? new HashMap<String, MethodNode>() : null;
        privateFieldMutators = mutatedFields != null ? new HashMap<String, MethodNode>() : null;
        final int access = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC;
        for (FieldNode fieldNode : node.getFields()) {
            boolean generateAccessor = accessedFields != null && accessedFields.contains(fieldNode);
            boolean generateMutator = mutatedFields != null && mutatedFields.contains(fieldNode);
            if (generateAccessor) {
                acc++;
                Parameter param = new Parameter(node.getPlainNodeReference(), "$that");
                Expression receiver = fieldNode.isStatic() ? new ClassExpression(node) : new VariableExpression(param);
                Statement stmt = new ExpressionStatement(new PropertyExpression(
                        receiver,
                        fieldNode.getName()
                ));
                MethodNode accessor = node.addMethod("pfaccess$" + acc, access, fieldNode.getOriginType(), new Parameter[]{param}, ClassNode.EMPTY_ARRAY, stmt);
                privateFieldAccessors.put(fieldNode.getName(), accessor);
            }

            if (generateMutator) {
                //increment acc if it hasn't been incremented in the current iteration
                if (!generateAccessor) acc++;
                Parameter param = new Parameter(node.getPlainNodeReference(), "$that");
                Expression receiver = fieldNode.isStatic() ? new ClassExpression(node) : new VariableExpression(param);
                Parameter value = new Parameter(fieldNode.getOriginType(), "$value");
                Statement stmt = GeneralUtils.assignS(
                        new PropertyExpression(receiver, fieldNode.getName()),
                        new VariableExpression(value)
                );
                MethodNode mutator = node.addMethod("pfaccess$0" + acc, access, fieldNode.getOriginType(), new Parameter[]{param, value}, ClassNode.EMPTY_ARRAY, stmt);
                privateFieldMutators.put(fieldNode.getName(), mutator);
            }
        }
        if (privateFieldAccessors != null) node.setNodeMetaData(PRIVATE_FIELDS_ACCESSORS, privateFieldAccessors);
        if (privateFieldMutators != null) node.setNodeMetaData(PRIVATE_FIELDS_MUTATORS, privateFieldMutators);
    }

    /**
     * This method is used to add "bridge" methods for private methods of an inner/outer
     * class, so that the outer class is capable of calling them. It does basically
     * the same job as access$000 like methods in Java.
     *
     * @param node an inner/outer class node for which to generate bridge methods
     */
    private static void addPrivateBridgeMethods(final ClassNode node) {
        Set<ASTNode> accessedMethods = (Set<ASTNode>) node.getNodeMetaData(StaticTypesMarker.PV_METHODS_ACCESS);
        if (accessedMethods==null) return;
        List<MethodNode> methods = new ArrayList<MethodNode>(node.getAllDeclaredMethods());
        methods.addAll(node.getDeclaredConstructors());
        Map<MethodNode, MethodNode> privateBridgeMethods = (Map<MethodNode, MethodNode>) node.getNodeMetaData(PRIVATE_BRIDGE_METHODS);
        if (privateBridgeMethods!=null) {
            // private bridge methods already added
            return;
        }
        privateBridgeMethods = new HashMap<MethodNode, MethodNode>();
        int i=-1;
        final int access = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC;
        for (MethodNode method : methods) {
            if (accessedMethods.contains(method)) {
                List<String> methodSpecificGenerics = methodSpecificGenerics(method);
                i++;
                ClassNode declaringClass = method.getDeclaringClass();
                Map<String,ClassNode> genericsSpec = createGenericsSpec(node);
                genericsSpec = addMethodGenerics(method, genericsSpec);
                extractSuperClassGenerics(node, declaringClass, genericsSpec);
                Parameter[] methodParameters = method.getParameters();
                Parameter[] newParams = new Parameter[methodParameters.length+1];
                for (int j = 1; j < newParams.length; j++) {
                    Parameter orig = methodParameters[j-1];
                    newParams[j] = new Parameter(
                            correctToGenericsSpecRecurse(genericsSpec, orig.getOriginType(), methodSpecificGenerics),
                            orig.getName()
                    );
                }
                Expression arguments;
                if (method.getParameters()==null || method.getParameters().length==0) {
                    arguments = ArgumentListExpression.EMPTY_ARGUMENTS;
                } else {
                    List<Expression> args = new LinkedList<Expression>();
                    for (Parameter parameter : methodParameters) {
                        args.add(new VariableExpression(parameter));
                    }
                    arguments = new ArgumentListExpression(args);
                }

                MethodNode bridge;
                if (method instanceof ConstructorNode) {
                    // create constructor with a nested class as the first parameter, creating one if necessary
                    ClassNode thatType = null;
                    Iterator<InnerClassNode> innerClasses = node.getInnerClasses();
                    if (innerClasses.hasNext()) {
                        thatType = innerClasses.next();
                    } else {
                        thatType = new InnerClassNode(node.redirect(), node.getName() + "$1", ACC_STATIC | ACC_SYNTHETIC, ClassHelper.OBJECT_TYPE);
                        node.getModule().addClass(thatType);
                    }
                    newParams[0] = new Parameter(thatType.getPlainNodeReference(), "$that");
                    Expression cce = new ConstructorCallExpression(ClassNode.THIS, arguments);
                    Statement body = new ExpressionStatement(cce);
                    bridge = node.addConstructor(ACC_SYNTHETIC, newParams, ClassNode.EMPTY_ARRAY, body);
                } else {
                    newParams[0] = new Parameter(node.getPlainNodeReference(), "$that");
                    Expression receiver = method.isStatic()?new ClassExpression(node):new VariableExpression(newParams[0]);
                    MethodCallExpression mce = new MethodCallExpression(receiver, method.getName(), arguments);
                    mce.setMethodTarget(method);

                    ExpressionStatement returnStatement = new ExpressionStatement(mce);
                    bridge = node.addMethod(
                            "access$"+i, access,
                            correctToGenericsSpecRecurse(genericsSpec, method.getReturnType(), methodSpecificGenerics),
                            newParams,
                            method.getExceptions(),
                            returnStatement);
                }
                GenericsType[] origGenericsTypes = method.getGenericsTypes();
                if (origGenericsTypes !=null) {
                    bridge.setGenericsTypes(applyGenericsContextToPlaceHolders(genericsSpec,origGenericsTypes));
                }
                privateBridgeMethods.put(method, bridge);
                bridge.addAnnotation(new AnnotationNode(COMPILESTATIC_CLASSNODE));
            }
        }
        if (!privateBridgeMethods.isEmpty()) {
            node.setNodeMetaData(PRIVATE_BRIDGE_METHODS, privateBridgeMethods);
        }
    }

    private static List<String> methodSpecificGenerics(final MethodNode method) {
        List<String> genericTypeTokens = new ArrayList<String>();
        GenericsType[] candidateGenericsTypes = method.getGenericsTypes();
        if (candidateGenericsTypes != null) {
            for (GenericsType gt : candidateGenericsTypes) {
                genericTypeTokens.add(gt.getName());
            }
        }
        return genericTypeTokens;
    }

    private static void memorizeInitialExpressions(final MethodNode node) {
        // add node metadata for default parameters because they are erased by the Verifier
        if (node.getParameters()!=null) {
            for (Parameter parameter : node.getParameters()) {
                parameter.putNodeMetaData(StaticTypesMarker.INITIAL_EXPRESSION, parameter.getInitialExpression());
            }
        }
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        super.visitMethodCallExpression(call);

        MethodNode target = (MethodNode) call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target!=null) {
            call.setMethodTarget(target);
            memorizeInitialExpressions(target);
        }

        if (call.getMethodTarget()==null && call.getLineNumber()>0) {
            addError("Target method for method call expression hasn't been set", call);
        }

    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);

        MethodNode target = (MethodNode) call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target==null && call.getLineNumber()>0) {
            addError("Target constructor for constructor call expression hasn't been set", call);
        } else {
            if (target==null) {
                // try to find a target
                ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
                List<Expression> expressions = argumentListExpression.getExpressions();
                ClassNode[] args = new ClassNode[expressions.size()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = typeChooser.resolveType(expressions.get(i), classNode);
                }
                MethodNode constructor = findMethodOrFail(call, call.isSuperCall() ? classNode.getSuperClass() : classNode, "<init>", args);
                call.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, constructor);
                target = constructor;
            }
        }
        if (target!=null) {
            memorizeInitialExpressions(target);
        }
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        super.visitForLoop(forLoop);
        Expression collectionExpression = forLoop.getCollectionExpression();
        if (!(collectionExpression instanceof ClosureListExpression)) {
            final ClassNode collectionType = getType(forLoop.getCollectionExpression());
            ClassNode componentType = inferLoopElementType(collectionType);
            forLoop.getVariable().setType(componentType);
            // GRECLIPSE edit -- preserve origin type for code select
            //forLoop.getVariable().setOriginType(componentType);
            // GRECLIPSE end
        }
    }

    @Override
    protected MethodNode findMethodOrFail(final Expression expr, final ClassNode receiver, final String name, final ClassNode... args) {
        MethodNode methodNode = super.findMethodOrFail(expr, receiver, name, args);
        if (expr instanceof BinaryExpression && methodNode!=null) {
            expr.putNodeMetaData(BINARY_EXP_TARGET, new Object[] {methodNode, name});
        }
        return methodNode;
    }

    @Override
    protected boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly, final ClassCodeVisitorSupport visitor) {
        Expression objectExpression = pexp.getObjectExpression();
        ClassNode objectExpressionType = getType(objectExpression);
        final Reference<ClassNode> rType = new Reference<ClassNode>(objectExpressionType);
        ClassCodeVisitorSupport receiverMemoizer = new ClassCodeVisitorSupport() {
            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            public void visitField(final FieldNode node) {
                if (visitor!=null) visitor.visitField(node);
                ClassNode declaringClass = node.getDeclaringClass();
                if (declaringClass!=null) {
                    if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(declaringClass, ClassHelper.LIST_TYPE)) {
                        boolean spread = declaringClass.getDeclaredField(node.getName()) != node;
                        pexp.setSpreadSafe(spread);
                    }
                    rType.set(declaringClass);
                }
            }

            public void visitMethod(final MethodNode node) {
                if (visitor!=null) visitor.visitMethod(node);
                ClassNode declaringClass = node.getDeclaringClass();
                if (declaringClass!=null){
                    if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(declaringClass, ClassHelper.LIST_TYPE)) {
                        List<MethodNode> properties = declaringClass.getDeclaredMethods(node.getName());
                        boolean spread = true;
                        for (MethodNode mn : properties) {
                            if (node==mn) {
                                spread = false;
                                break;
                            }
                        }
                        // it's no real property but a property of the component
                        pexp.setSpreadSafe(spread);
                    }
                    rType.set(declaringClass);
                }
            }

            @Override
            public void visitProperty(final PropertyNode node) {
                if (visitor!=null) visitor.visitProperty(node);
                ClassNode declaringClass = node.getDeclaringClass();
                if (declaringClass!=null) {
                    if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(declaringClass, ClassHelper.LIST_TYPE)) {
                        List<PropertyNode> properties = declaringClass.getProperties();
                        boolean spread = true;
                        for (PropertyNode propertyNode : properties) {
                            if (propertyNode==node) {
                                spread = false;
                                break;
                            }
                        }
                        // it's no real property but a property of the component
                        pexp.setSpreadSafe(spread);
                    }
                    rType.set(declaringClass);
                }
            }
        };
        boolean exists = super.existsProperty(pexp, checkForReadOnly, receiverMemoizer);
        if (exists) {
            if (objectExpression.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER)==null) {
                objectExpression.putNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER, rType.get());
            }
            if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(objectExpressionType, ClassHelper.LIST_TYPE)) {
                objectExpression.putNodeMetaData(COMPONENT_TYPE, inferComponentType(objectExpressionType, ClassHelper.int_TYPE));
            }
        }
        return exists;
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression pexp) {
        super.visitPropertyExpression(pexp);
        Object dynamic = pexp.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION);
        if (dynamic !=null) {
            pexp.getObjectExpression().putNodeMetaData(StaticCompilationMetadataKeys.RECEIVER_OF_DYNAMIC_PROPERTY, dynamic);
        }
    }
}
