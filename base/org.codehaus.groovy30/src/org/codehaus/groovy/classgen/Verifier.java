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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.transform.Generated;
import groovy.transform.Internal;
import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.apache.groovy.util.BeanUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter.ClassNodeSkip;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;
import groovyjarjarasm.asm.Label;
import groovyjarjarasm.asm.MethodVisitor;
import groovyjarjarasm.asm.Opcodes;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.joining;
import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.markAsGenerated;
import static org.apache.groovy.ast.tools.ConstructorNodeUtils.getFirstIfSpecialConstructorCall;
import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getCodeAsBlock;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getPropertyName;
import static org.apache.groovy.ast.tools.MethodNodeUtils.methodDescriptorWithoutReturnType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.bytecodeX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInterfacesAndSuperInterfaces;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.addMethodGenerics;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.PropertyNodeUtils.adjustPropertyModifiersForMethod;

/**
 * Verifies the AST node and adds any default AST code before bytecode generation occurs.
 * <p>
 * Checks include:
 * <ul>
 *     <li>Methods with duplicate signatures</li>
 *     <li>Duplicate interfaces</li>
 *     <li>Reassigned final variables/parameters</li>
 *     <li>Uninitialized variables</li>
 *     <li>Bad code in object initializers or constructors</li>
 *     <li>Mismatches in modifiers or return types between implementations and interfaces/abstract classes</li>
 * </ul>
 *
 * Added code includes:
 * <ul>
 *     <li>Methods needed to implement GroovyObject</li>
 *     <li>Property accessor methods</li>
 *     <li>Covariant methods</li>
 *     <li>Additional methods/constructors as needed for default parameters</li>
 * </ul>
 */
public class Verifier implements GroovyClassVisitor, Opcodes {

    public static final String SWAP_INIT = "__$swapInit";
    public static final String STATIC_METACLASS_BOOL = "__$stMC";
    public static final String INITIAL_EXPRESSION = "INITIAL_EXPRESSION";
    public static final String DEFAULT_PARAMETER_GENERATED = "DEFAULT_PARAMETER_GENERATED";
    private static final Parameter[] SET_METACLASS_PARAMS = {new Parameter(ClassHelper.METACLASS_TYPE, "mc")};

    private static final ClassNode GENERATED_ANNOTATION = ClassHelper.make(Generated.class);
    private static final ClassNode INTERNAL_ANNOTATION  = ClassHelper.make(Internal .class);
    private static final ClassNode TRANSIENT_ANNOTATION = ClassHelper.make(Transient.class);

    // NOTE: timeStamp constants shouldn't belong to Verifier but kept here for binary compatibility
    public static final String __TIMESTAMP = "__timeStamp";
    public static final String __TIMESTAMP__ = "__timeStamp__239_neverHappen";

    private ClassNode classNode;
    private MethodNode methodNode;
    // GRECLIPSE add
    public boolean inlineStaticFieldInitializersIntoClinit = true;
    // GRECLIPSE end

    public ClassNode getClassNode() {
        return classNode;
    }

    protected void setClassNode(final ClassNode classNode) {
        this.classNode = classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    private static FieldNode getMetaClassField(final ClassNode node) {
        FieldNode metaClassField = node.getDeclaredField("metaClass");
        if (metaClassField != null) {
            ClassNode mcFieldType = metaClassField.getType();
            if (!mcFieldType.equals(ClassHelper.METACLASS_TYPE)) {
                throw new RuntimeParserException("The class " + node.getName() +
                        " cannot declare field 'metaClass' of type " + mcFieldType.getName() + " as it needs to be of " +
                        "the type " + ClassHelper.METACLASS_TYPE.getName() + " for internal groovy purposes", metaClassField);
            }
            return metaClassField;
        }
        ClassNode current = node;
        while (!ClassHelper.OBJECT_TYPE.equals(current)) {
            current = current.getSuperClass();
            if (current == null) break;
            metaClassField = current.getDeclaredField("metaClass");
            if (metaClassField == null) continue;
            if (isPrivate(metaClassField.getModifiers())) continue;
            return metaClassField;
        }
        return null;
    }

    private static FieldNode setMetaClassFieldIfNotExists(final ClassNode node, FieldNode metaClassField) {
        if (metaClassField == null) {
            String classInternalName = BytecodeHelper.getClassInternalName(node);
            metaClassField =
                    node.addField("metaClass", ACC_PRIVATE | ACC_SYNTHETIC | ACC_TRANSIENT, ClassHelper.METACLASS_TYPE,
                            bytecodeX(ClassHelper.METACLASS_TYPE, mv -> {
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "$getStaticMetaClass", "()Lgroovy/lang/MetaClass;", false);
                            })
                    );
            metaClassField.setSynthetic(true);
        }
        return metaClassField;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitClass(final ClassNode node) {
        this.classNode = node;

        checkForDuplicateInterfaces(node);

        if (node.isInterface() || Traits.isTrait(node)) {
            // interfaces have no constructors but this expects one,
            // so create a dummy but do not add it to the class node
            addInitialization(node, new ConstructorNode(0, null));
            node.visitContents(this);
            if (node.getNodeMetaData(ClassNodeSkip.class) == null) {
                node.setNodeMetaData(ClassNodeSkip.class, Boolean.TRUE);
            }
            return;
        }

        addDefaultParameterMethods(node);
        addDefaultParameterConstructors(node);

        String classInternalName = BytecodeHelper.getClassInternalName(node);

        addStaticMetaClassField(node, classInternalName);

        addFastPathHelperFieldsAndHelperMethod(node, classInternalName);
        if (!node.isDerivedFrom(ClassHelper.GROOVY_OBJECT_SUPPORT_TYPE))
            addGroovyObjectInterfaceAndMethods(node, classInternalName);

        addDefaultConstructor(node);

        addInitialization(node);
        checkReturnInObjectInitializer(node.getObjectInitializerStatements());
        node.getObjectInitializerStatements().clear();
        node.visitContents(this);
        checkForDuplicateMethods(node);
        checkForDuplicateConstructors(node);
        addCovariantMethods(node);
        checkFinalVariables(node);
    }

    private void checkFinalVariables(final ClassNode node) {
        GroovyClassVisitor visitor = new FinalVariableAnalyzer(null, getFinalVariablesCallback());
        visitor.visitClass(node);
    }

    protected FinalVariableAnalyzer.VariableNotFinalCallback getFinalVariablesCallback() {
        return new FinalVariableAnalyzer.VariableNotFinalCallback() {
            @Override
            public void variableNotFinal(Variable var, final Expression bexp) {
                if (var instanceof VariableExpression) {
                    var = ((VariableExpression) var).getAccessedVariable();
                }
                if (var instanceof VariableExpression && isFinal(var.getModifiers())) {
                    throw new RuntimeParserException("The variable [" + var.getName() + "] is declared final but is reassigned", bexp);
                }
                if (var instanceof Parameter && isFinal(var.getModifiers())) {
                    throw new RuntimeParserException("The parameter [" + var.getName() + "] is declared final but is reassigned", bexp);
                }
            }

            @Override
            public void variableNotAlwaysInitialized(final VariableExpression var) {
                if (isFinal(var.getAccessedVariable().getModifiers()))
                    throw new RuntimeParserException("The variable [" + var.getName() + "] may be uninitialized", var);
            }
        };
    }

    private static Set<ClassNode> getAllInterfaces(final ClassNode cn) {
        return getInterfacesAndSuperInterfaces(cn);
    }

    private static void checkForDuplicateInterfaces(final ClassNode cn) {
        ClassNode[] interfaces = cn.getInterfaces();
        int nInterfaces = interfaces.length;
        if (nInterfaces == 0) return;

        if (nInterfaces > 1) {
            List<String> interfaceNames = new ArrayList<>(nInterfaces);
            for (ClassNode in : interfaces) interfaceNames.add(in.getName());
            if (interfaceNames.size() != new HashSet<>(interfaceNames).size()) {
                throw new RuntimeParserException("Duplicate interfaces in implements list: " + interfaceNames, cn);
            }
        }

        // GROOVY-5106: check for same interface with different type argument(s)
        List< Set<ClassNode> > allInterfaces = new ArrayList<>(nInterfaces + 1);
        for (ClassNode in : interfaces) allInterfaces.add(getAllInterfaces(in));
        allInterfaces.add(getAllInterfaces(cn.getUnresolvedSuperClass()));
        if (nInterfaces == 1 && allInterfaces.get(1).isEmpty())
            return; // no peer interface(s) to verify

        for (int i = 0; i < nInterfaces; i += 1) {
            for (ClassNode in : allInterfaces.get(i)) {
                if (in.redirect().getGenericsTypes() != null) {
                    for (int j = i + 1; j < nInterfaces + 1; j += 1) {
                        Set<ClassNode> set = allInterfaces.get(j);
                        if (set.contains(in)) {
                            for (ClassNode t : set) { // find match and check generics
                                if (t.equals(in)) {
                                    String one = in.toString(false), two = t.toString(false);
                                    if (!one.equals(two)) {
                                        String warning = String.format(
                                                "The %s %s is implemented more than once with different arguments: %s and %s",
                                                (Traits.isTrait(in) ? "trait" : "interface"), in.getNameWithoutPackage(), one, two);
                                        Token token = new Token(0, "", cn.getLineNumber(), cn.getColumnNumber()); // ASTNode to CSTNode
                                        cn.getModule().getContext().getErrorCollector().addWarning(1, warning, token, cn.getModule().getContext());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void checkForDuplicateMethods(final ClassNode cn) {
        Set<String> descriptors = new HashSet<>();
        for (MethodNode mn : cn.getMethods()) {
            if (mn.isSynthetic()) continue;
            String mySig = methodDescriptorWithoutReturnType(mn);
            if (descriptors.contains(mySig)) {
                if (mn.isScriptBody() || mySig.equals(scriptBodySignatureWithoutReturnType(cn))) {
                    throw new RuntimeParserException("The method " + mn.getText() +
                            " is a duplicate of the one declared for this script's body code", sourceOf(mn));
                } else {
                    throw new RuntimeParserException("The method " + mn.getText() +
                            " duplicates another method of the same signature", sourceOf(mn));
                }
            }
            descriptors.add(mySig);
        }
    }

    private static void checkForDuplicateConstructors(final ClassNode cn) {
        Set<String> descriptors = new HashSet<>();
        for (ConstructorNode cons : cn.getDeclaredConstructors()) {
            String mySig = methodDescriptorWithoutReturnType(cons);
            if (descriptors.contains(mySig)) {
                throw new RuntimeParserException("The constructor " + cons.getText() +
                    " duplicates another constructor of the same signature", sourceOf(cons));
            }
            descriptors.add(mySig);
        }
    }

    private static String scriptBodySignatureWithoutReturnType(final ClassNode cn) {
        for (MethodNode mn : cn.getMethods()) {
            if (mn.isScriptBody()) return methodDescriptorWithoutReturnType(mn);
        }
        return null;
    }

    private static FieldNode checkFieldDoesNotExist(final ClassNode node, final String fieldName) {
        FieldNode ret = node.getDeclaredField(fieldName);
        if (ret != null) {
            if (isPublic(ret.getModifiers()) &&
                    ret.getType().redirect() == ClassHelper.boolean_TYPE) {
                return ret;
            }
            throw new RuntimeParserException("The class " + node.getName() +
                    " cannot declare field '" + fieldName + "' as this" +
                    " field is needed for internal groovy purposes", ret);
        }
        return null;
    }

    private static void addFastPathHelperFieldsAndHelperMethod(final ClassNode node, final String classInternalName) {
        if (Boolean.TRUE.equals(node.getNodeMetaData(ClassNodeSkip.class))) return;
        FieldNode stMCB = checkFieldDoesNotExist(node, STATIC_METACLASS_BOOL);
        if (stMCB == null) {
            stMCB = node.addField(
                    STATIC_METACLASS_BOOL,
                    ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC | ACC_TRANSIENT,
                    ClassHelper.boolean_TYPE, null);
            stMCB.setSynthetic(true);
        }
    }

    protected void addDefaultConstructor(final ClassNode node) {
        if (!node.getDeclaredConstructors().isEmpty()) return;

        ConstructorNode constructor = new ConstructorNode(ACC_PUBLIC, new BlockStatement());
        constructor.setHasNoRealSourcePosition(true);
        markAsGenerated(node, constructor);
        node.addConstructor(constructor);
    }

    private void addStaticMetaClassField(final ClassNode node, final String classInternalName) {
        String _staticClassInfoFieldName = "$staticClassInfo";
        while (node.getDeclaredField(_staticClassInfoFieldName) != null)
            _staticClassInfoFieldName = _staticClassInfoFieldName + "$";
        final String staticMetaClassFieldName = _staticClassInfoFieldName;

        FieldNode staticMetaClassField = node.addField(staticMetaClassFieldName, ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, ClassHelper.make(ClassInfo.class, false), null);
        staticMetaClassField.setSynthetic(true);
        ClassNode classNode = this.classNode;
        node.addSyntheticMethod(
                "$getStaticMetaClass",
                ACC_PROTECTED,
                ClassHelper.make(MetaClass.class),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                        if (BytecodeHelper.isClassLiteralPossible(node) || BytecodeHelper.isSameCompilationUnit(classNode, node)) {
                            BytecodeHelper.visitClassLiteral(mv, node);
                        } else {
                            mv.visitMethodInsn(INVOKESTATIC, classInternalName, "$get$$class$" + classInternalName.replace('/', '$'), "()Ljava/lang/Class;", false);
                        }
                        Label l1 = new Label();
                        mv.visitJumpInsn(IF_ACMPEQ, l1);

                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/ScriptBytecodeAdapter", "initMetaClass", "(Ljava/lang/Object;)Lgroovy/lang/MetaClass;", false);
                        mv.visitInsn(ARETURN);

                        mv.visitLabel(l1);

                        mv.visitFieldInsn(GETSTATIC, classInternalName, staticMetaClassFieldName, "Lorg/codehaus/groovy/reflection/ClassInfo;");
                        mv.visitVarInsn(ASTORE, 1);
                        mv.visitVarInsn(ALOAD, 1);
                        Label l0 = new Label();
                        mv.visitJumpInsn(IFNONNULL, l0);

                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/reflection/ClassInfo", "getClassInfo", "(Ljava/lang/Class;)Lorg/codehaus/groovy/reflection/ClassInfo;", false);
                        mv.visitInsn(DUP);
                        mv.visitVarInsn(ASTORE, 1);
                        mv.visitFieldInsn(PUTSTATIC, classInternalName, staticMetaClassFieldName, "Lorg/codehaus/groovy/reflection/ClassInfo;");

                        mv.visitLabel(l0);

                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/ClassInfo", "getMetaClass", "()Lgroovy/lang/MetaClass;", false);
                        mv.visitInsn(ARETURN);
                    }
                })
        );
    }

    protected void addGroovyObjectInterfaceAndMethods(final ClassNode node, final String classInternalName) {
        if (!node.isDerivedFromGroovyObject()) node.addInterface(ClassHelper.GROOVY_OBJECT_TYPE);
        FieldNode metaClassField = getMetaClassField(node);

        boolean shouldAnnotate = (classNode.getModule().getContext() != null);
        if (!node.hasMethod("getMetaClass", Parameter.EMPTY_ARRAY)) {
            metaClassField = setMetaClassFieldIfNotExists(node, metaClassField);
            Statement getMetaClassCode = new BytecodeSequence(new BytecodeInstruction() {
                @Override
                public void visit(final MethodVisitor mv) {
                    Label nullLabel = new Label();
                    /*
                     *  the code is:
                     *  if (this.metaClass==null) {
                     *      this.metaClass = this.$getStaticMetaClass()
                     *      return this.metaClass
                     *  } else {
                     *      return this.metaClass
                     *  }
                     *  with the optimization that the result of the
                     *  first this.metaClass is duped on the operand
                     *  stack and reused for the return in the else part
                     */
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                    mv.visitInsn(DUP);
                    mv.visitJumpInsn(IFNULL, nullLabel);
                    mv.visitInsn(ARETURN);

                    mv.visitLabel(nullLabel);
                    mv.visitInsn(POP);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKEVIRTUAL, classInternalName, "$getStaticMetaClass", "()Lgroovy/lang/MetaClass;", false);
                    mv.visitFieldInsn(PUTFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                    mv.visitInsn(ARETURN);
                }
            });

            MethodNode methodNode = addMethod(node, !shouldAnnotate, "getMetaClass", ACC_PUBLIC,
                    ClassHelper.METACLASS_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getMetaClassCode);
            if (shouldAnnotate) {
                methodNode.addAnnotation(new AnnotationNode(GENERATED_ANNOTATION));
                methodNode.addAnnotation(new AnnotationNode(INTERNAL_ANNOTATION ));
                methodNode.addAnnotation(new AnnotationNode(TRANSIENT_ANNOTATION));
            }
        }

        if (!node.hasMethod("setMetaClass", SET_METACLASS_PARAMS)) {
            metaClassField = setMetaClassFieldIfNotExists(node, metaClassField);
            Statement setMetaClassCode;
            if (isFinal(metaClassField.getModifiers())) {
                setMetaClassCode = stmt(ctorX(ClassHelper.make(IllegalArgumentException.class), constX("cannot set read-only meta class")));
            } else {
                setMetaClassCode = new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        /*
                         * the code is (meta class is stored in 1):
                         * this.metaClass = <1>
                         */
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitFieldInsn(PUTFIELD, classInternalName, "metaClass", "Lgroovy/lang/MetaClass;");
                        mv.visitInsn(RETURN);
                    }
                });
            }

            MethodNode methodNode = addMethod(node, !shouldAnnotate, "setMetaClass", ACC_PUBLIC,
                    ClassHelper.VOID_TYPE, SET_METACLASS_PARAMS, ClassNode.EMPTY_ARRAY, setMetaClassCode);
            if (shouldAnnotate) {
                methodNode.addAnnotation(new AnnotationNode(GENERATED_ANNOTATION));
                methodNode.addAnnotation(new AnnotationNode(INTERNAL_ANNOTATION ));
            }
        }
    }

    /**
     * Helper method to add a new method to a ClassNode.  Depending on the shouldBeSynthetic flag the
     * call will either be made to ClassNode.addSyntheticMethod() or ClassNode.addMethod(). If a non-synthetic method
     * is to be added the ACC_SYNTHETIC modifier is removed if it has been accidentally supplied.
     */
    protected MethodNode addMethod(final ClassNode node, final boolean shouldBeSynthetic, final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        if (shouldBeSynthetic) {
            return node.addSyntheticMethod(name, modifiers, returnType, parameters, exceptions, code);
        } else {
            return node.addMethod(name, modifiers & ~ACC_SYNTHETIC, returnType, parameters, exceptions, code);
        }
    }

    // for binary compatibility only, don't use or override this
    protected void addMethod$$bridge(final ClassNode node, final boolean shouldBeSynthetic, final String name, final int modifiers, final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        addMethod(node, shouldBeSynthetic, name, modifiers, returnType, parameters, exceptions, code);
    }

    @Deprecated
    protected void addTimeStamp(final ClassNode node) {
    }

    private static void checkReturnInObjectInitializer(final List<Statement> init) {
        GroovyCodeVisitor visitor = new CodeVisitorSupport() {
            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
                // return is OK in closures in object initializers
            }
            @Override
            public void visitReturnStatement(final ReturnStatement statement) {
                throw new RuntimeParserException("'return' is not allowed in object initializer", statement);
            }
        };
        for (Statement stmt : init) {
            stmt.visit(visitor);
        }
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        Statement stmt = node.getCode();
        if (stmt != null) {
            stmt.visit(new VerifierCodeVisitor(getClassNode()));
            // check for uninitialized-this references
            stmt.visit(new CodeVisitorSupport() {
                /* GRECLIPSE edit
                @Override
                public void visitClosureExpression(final ClosureExpression ce) {
                    boolean oldInClosure = inClosure;
                    inClosure = true;
                    super.visitClosureExpression(ce);
                    inClosure = oldInClosure;
                }

                @Override
                public void visitConstructorCallExpression(final ConstructorCallExpression cce) {
                    boolean oldIsSpecialConstructorCall = inSpecialConstructorCall;
                    inSpecialConstructorCall |= cce.isSpecialCall();
                    super.visitConstructorCallExpression(cce);
                    inSpecialConstructorCall = oldIsSpecialConstructorCall;
                }

                @Override
                public void visitMethodCallExpression(final MethodCallExpression mce) {
                    if (inSpecialConstructorCall && isThisObjectExpression(mce)) {
                        MethodNode methodTarget = mce.getMethodTarget();
                        if (methodTarget == null || !(methodTarget.isStatic() || classNode.getOuterClasses().contains(methodTarget.getDeclaringClass()))) {
                            if (!mce.isImplicitThis()) {
                                throw newVariableError(mce.getObjectExpression().getText(), mce.getObjectExpression());
                            } else {
                                throw newVariableError(mce.getMethodAsString(), mce.getMethod());
                            }
                        }
                        mce.getMethod().visit(this);
                        mce.getArguments().visit(this);
                    } else {
                        super.visitMethodCallExpression(mce);
                    }
                }

                @Override
                public void visitVariableExpression(final VariableExpression ve) {
                    // before this/super ctor call completes, only params and static or outer members are accessible
                    if (inSpecialConstructorCall && (ve.isThisExpression() || ve.isSuperExpression() || isNonStaticMemberAccess(ve))) {
                        throw newVariableError(ve.getName(), ve.getLineNumber() > 0 ? ve : node); // TODO: context for default argument
                    }
                }

                //

                private boolean inClosure, inSpecialConstructorCall;

                private boolean isNonStaticMemberAccess(final VariableExpression ve) {
                    Variable variable = ve.getAccessedVariable();
                    return !inClosure && variable != null && !isStatic(variable.getModifiers())
                        && !(variable instanceof DynamicVariable) && !(variable instanceof Parameter);
                }
                */
                private final Deque<ASTNode> nodes = new ArrayDeque<>();

                @Override
                public void visitArrayExpression(ArrayExpression ae) {
                    nodes.push(ae);
                    super.visitArrayExpression(ae);
                    nodes.pop();
                }

                @Override
                public void visitAttributeExpression(AttributeExpression ae) {
                    nodes.push(ae);
                    super.visitAttributeExpression(ae);
                    nodes.pop();
                }

                @Override
                public void visitBinaryExpression(BinaryExpression be) {
                    nodes.push(be);
                    super.visitBinaryExpression(be);
                    nodes.pop();
                }

                @Override
                public void visitBitwiseNegationExpression(BitwiseNegationExpression bne) {
                    nodes.push(bne);
                    super.visitBitwiseNegationExpression(bne);
                    nodes.pop();
                }

                @Override
                public void visitBooleanExpression(BooleanExpression be) {
                    nodes.push(be);
                    super.visitBooleanExpression(be);
                    nodes.pop();
                }

                @Override
                public void visitCastExpression(CastExpression ce) {
                    nodes.push(ce);
                    super.visitCastExpression(ce);
                    nodes.pop();
                }

                @Override
                public void visitClosureExpression(ClosureExpression ce) {
                    nodes.push(ce);
                    super.visitClosureExpression(ce);
                    nodes.pop();
                }

                @Override
                public void visitClosureListExpression(ClosureListExpression cle) {
                    nodes.push(cle);
                    super.visitClosureListExpression(cle);
                    nodes.pop();
                }

                @Override
                public void visitConstructorCallExpression(ConstructorCallExpression cce) {
                    nodes.push(cce);
                    super.visitConstructorCallExpression(cce);
                    nodes.pop();
                }

                @Override
                public void visitGStringExpression(GStringExpression gse) {
                    nodes.push(gse);
                    super.visitGStringExpression(gse);
                    nodes.pop();
                }

                @Override
                public void visitListExpression(ListExpression le) {
                    nodes.push(le);
                    super.visitListExpression(le);
                    nodes.pop();
                }

                @Override
                public void visitMapExpression(MapExpression me) {
                    nodes.push(me);
                    super.visitMapExpression(me);
                    nodes.pop();
                }

                @Override
                public void visitMapEntryExpression(MapEntryExpression mee) {
                    nodes.push(mee);
                    super.visitMapEntryExpression(mee);
                    nodes.pop();
                }

                @Override
                public void visitMethodCallExpression(MethodCallExpression mce) {
                    if (inSpecialConstructorCall() && isThisObjectExpression(mce)) {
                        boolean outerOrStaticMember = false;
                        if (mce.getMethodTarget() != null) {
                            outerOrStaticMember = mce.getMethodTarget().isStatic() || classNode.getOuterClasses().contains(mce.getMethodTarget().getDeclaringClass());
                        } else if (mce.isImplicitThis()) { // GROOVY-11352
                            outerOrStaticMember = classNode.getOuterClasses().stream().anyMatch(oc -> oc.hasPossibleStaticMethod(mce.getMethodAsString(), mce.getArguments()));
                        }
                        if (!outerOrStaticMember) {
                            if (mce.isImplicitThis()) {
                                throw newVariableError(mce.getMethodAsString(), mce.getMethod());
                            } else {
                                throw newVariableError(mce.getObjectExpression().getText(), mce.getObjectExpression());
                            }
                        }
                        nodes.push(mce);
                        mce.getMethod().visit(this);
                        mce.getArguments().visit(this);
                    } else {
                        nodes.push(mce);
                        super.visitMethodCallExpression(mce);
                    }
                    nodes.pop();
                }

                @Override
                public void visitMethodPointerExpression(MethodPointerExpression mpe) {
                    nodes.push(mpe);
                    super.visitMethodPointerExpression(mpe);
                    nodes.pop();
                }

                @Override
                public void visitNotExpression(NotExpression ne) {
                    nodes.push(ne);
                    super.visitNotExpression(ne);
                    nodes.pop();
                }

                @Override
                public void visitPrefixExpression(PrefixExpression pe) {
                    nodes.push(pe);
                    super.visitPrefixExpression(pe);
                    nodes.pop();
                }

                @Override
                public void visitPropertyExpression(PropertyExpression pe) {
                    nodes.push(pe);
                    super.visitPropertyExpression(pe);
                    nodes.pop();
                }

                @Override
                public void visitPostfixExpression(PostfixExpression pe) {
                    nodes.push(pe);
                    super.visitPostfixExpression(pe);
                    nodes.pop();
                }

                @Override
                public void visitTernaryExpression(TernaryExpression te) {
                    nodes.push(te);
                    super.visitTernaryExpression(te);
                    nodes.pop();
                }

                @Override
                public void visitTupleExpression(TupleExpression te) {
                    nodes.push(te);
                    super.visitTupleExpression(te);
                    nodes.pop();
                }

                @Override
                public void visitRangeExpression(RangeExpression re) {
                    nodes.push(re);
                    super.visitRangeExpression(re);
                    nodes.pop();
                }

                @Override
                public void visitSpreadExpression(SpreadExpression se) {
                    nodes.push(se);
                    super.visitSpreadExpression(se);
                    nodes.pop();
                }

                @Override
                public void visitSpreadMapExpression(SpreadMapExpression sme) {
                    nodes.push(sme);
                    super.visitSpreadMapExpression(sme);
                    nodes.pop();
                }

                @Override
                public void visitUnaryMinusExpression(UnaryMinusExpression ume) {
                    nodes.push(ume);
                    super.visitUnaryMinusExpression(ume);
                    nodes.pop();
                }

                @Override
                public void visitUnaryPlusExpression(UnaryPlusExpression upe) {
                    nodes.push(upe);
                    super.visitUnaryPlusExpression(upe);
                    nodes.pop();
                }

                @Override
                public void visitVariableExpression(VariableExpression ve) {
                    nodes.push(ve);
                    // before this/super ctor call completes, only params and static or outer members are accessible
                    if (inSpecialConstructorCall() && (ve.isThisExpression() || ve.isSuperExpression() || isNonStaticMemberAccess(ve))) {
                        throw newVariableError(ve.getName(), nodes.stream().filter(it -> it.getLineNumber() > 0).findFirst().orElse(ve));
                    }
                    nodes.pop();
                }

                //

                private boolean inClosure() {
                    return nodes.stream().anyMatch(it -> it instanceof ClosureExpression);
                }

                private boolean inSpecialConstructorCall() {
                    return nodes.stream().anyMatch(it -> it instanceof ConstructorCallExpression && ((ConstructorCallExpression) it).isSpecialCall());
                }

                private boolean isNonStaticMemberAccess(final VariableExpression ve) {
                    Variable variable = ve.getAccessedVariable();
                    return !inClosure() && variable != null && !isStatic(variable.getModifiers())
                        && !(variable instanceof DynamicVariable) && !(variable instanceof Parameter);
                }
                // GRECLIPSE end

                private boolean isThisObjectExpression(final MethodCallExpression mce) {
                    if (mce.isImplicitThis()) {
                        return true;
                    } else if (mce.getObjectExpression() instanceof VariableExpression) {
                        VariableExpression var = (VariableExpression) mce.getObjectExpression();
                        return var.isThisExpression() || var.isSuperExpression();
                    } else {
                        return false;
                    }
                }

                private GroovyRuntimeException newVariableError(final String name, final ASTNode node) {
                    RuntimeParserException rpe = new RuntimeParserException("Cannot reference '" + name +
                            "' before supertype constructor has been called. Possible causes:\n" +
                            "You attempted to access an instance field, method, or property.\n" +
                            "You attempted to construct a non-static inner class.", node);
                    rpe.setModule(getClassNode().getModule());
                    return rpe;
                }
            });
        }
    }

    @Override
    public void visitMethod(final MethodNode node) {
        // GROOVY-3712: if it's a MOP method, it's an error as they aren't supposed to exist before ACG is invoked
        if (MopWriter.isMopMethod(node.getName())) {
            throw new RuntimeParserException("Found unexpected MOP methods in the class node for " + classNode.getName() + "(" + node.getName() + ")", classNode);
        }

        adjustTypesIfPublicStaticMainMethod(node);
        this.methodNode = node;
        addReturnIfNeeded(node);

        Statement stmt = node.getCode();
        if (stmt != null) {
            stmt.visit(new VerifierCodeVisitor(getClassNode()));
        }
    }

    private static void adjustTypesIfPublicStaticMainMethod(final MethodNode node) { // GRECLIPSE edit
        if (node.isPublic() && node.isStatic() && node.getName().equals("main")) {
            Parameter[] params = node.getParameters();
            if (params.length == 1) {
                Parameter param = params[0];
                if (param.getType() == null || (param.getType().equals(ClassHelper.OBJECT_TYPE) && !param.getType().isGenericsPlaceHolder())) {
                    param.setType(ClassHelper.STRING_TYPE.makeArray());
                    if (ClassHelper.OBJECT_TYPE.equals(node.getReturnType())) {
                        node.setReturnType(ClassHelper.VOID_TYPE);
                    }
                }
            }
        }
    }

    protected void addReturnIfNeeded(final MethodNode node) {
        ReturnAdder adder = new ReturnAdder();
        adder.visitMethod(node);
    }

    @Override
    public void visitField(final FieldNode node) {
    }

    private boolean methodNeedsReplacement(final MethodNode m) {
        // no method found, we need to replace
        if (m == null) return true;
        // method is in current class, nothing to be done
        if (m.getDeclaringClass() == getClassNode()) return false;
        // do not overwrite final
        if (isFinal(m.getModifiers())) return false;
        return true;
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        String name = node.getName();
        FieldNode field = node.getField();

        String getterName = "get" + capitalize(name);
        String setterName = "set" + capitalize(name);

        int accessorModifiers = adjustPropertyModifiersForMethod(node);

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null && !node.isPrivate()) {
            MethodNode getter = classNode.getGetterMethod(getterName, !node.isStatic());
            if (getter == null && node.getType().equals(ClassHelper.boolean_TYPE)) {
                getter = classNode.getGetterMethod("is" + capitalize(name));
            }
            if (methodNeedsReplacement(getter)) {
                getterBlock = createGetterBlock(node, field);
            }
        }
        Statement setterBlock = node.getSetterBlock();
        if (setterBlock == null && !node.isPrivate() && !isFinal(accessorModifiers)) {
            boolean voidOnly = false; // accept setter with non-void return type
            MethodNode setter = classNode.getSetterMethod(setterName, voidOnly);
            if (methodNeedsReplacement(setter)) {
                setterBlock = createSetterBlock(node, field);
            }
        }

        int getterModifiers = accessorModifiers;
        // don't make static accessors final
        if (node.isStatic()) {
            getterModifiers &= ~ACC_FINAL;
        }
        if (getterBlock != null) {
            visitGetter(node, getterBlock, getterModifiers, getterName);

            if (node.getType().equals(ClassHelper.boolean_TYPE) || node.getType().equals(ClassHelper.Boolean_TYPE)) {
                String isserName = "is" + capitalize(name);
                MethodNode isser = classNode.getGetterMethod(isserName, !node.isStatic());
                if (methodNeedsReplacement(isser)) {
                    visitGetter(node, getterBlock, getterModifiers, isserName);
                }
            }
        }
        if (setterBlock != null) {
            Parameter[] setterParameterTypes = {new Parameter(node.getType(), "value")};
            MethodNode setter = new MethodNode(setterName, accessorModifiers, ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
            // GRECLIPSE add
            node.getField().getAnnotations().stream().filter(a -> a.getClassNode().getName().equals("java.lang.Deprecated")).forEach(setter::addAnnotation);
            // GRECLIPSE end
            setter.setSynthetic(true);
            addPropertyMethod(setter);
            visitMethod(setter);
        }
    }

    private void visitGetter(final PropertyNode node, final Statement getterBlock, final int getterModifiers, final String getterName) {
        MethodNode getter = new MethodNode(getterName, getterModifiers, node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
        // GRECLIPSE add
        node.getField().getAnnotations().stream().filter(a -> a.getClassNode().getName().equals("java.lang.Deprecated")).forEach(getter::addAnnotation);
        // GRECLIPSE end
        getter.setSynthetic(true);
        addPropertyMethod(getter);
        visitMethod(getter);
    }

    protected void addPropertyMethod(final MethodNode method) {
        classNode.addMethod(method);
        markAsGenerated(classNode, method);
        // GROOVY-4415 / GROOVY-4645: check that there's no abstract method which corresponds to this one
        String methodName = method.getName();
        Parameter[] parameters = method.getParameters();
        ClassNode methodReturnType = method.getReturnType();
        for (MethodNode node : classNode.getAbstractMethods()) {
            if (node.getName().equals(methodName)
                    && node.getDeclaringClass().equals(classNode)
                    && node.getParameters().length == parameters.length) {
                if (parameters.length == 1) { // setter
                    ClassNode abstractMethodParameterType = node.getParameters()[0].getType();
                    ClassNode methodParameterType = parameters[0].getType();
                    if (!methodParameterType.isDerivedFrom(abstractMethodParameterType)
                            && !methodParameterType.implementsInterface(abstractMethodParameterType)) {
                        continue;
                    }
                }
                ClassNode nodeReturnType = node.getReturnType();
                if (!methodReturnType.isDerivedFrom(nodeReturnType)
                        && !methodReturnType.implementsInterface(nodeReturnType)) {
                    continue;
                }
                // matching method, remove abstract status and use the same body
                node.setModifiers(node.getModifiers() ^ ACC_ABSTRACT);
                node.setCode(method.getCode());
            }
        }
    }

    @FunctionalInterface
    public interface DefaultArgsAction {
        void call(ArgumentListExpression arguments, Parameter[] parameters, MethodNode method);
    }

    /**
     * Creates a new method for each combination of default parameter expressions.
     */
    protected void addDefaultParameterMethods(final ClassNode type) {
        List<MethodNode> methods = new ArrayList<>(type.getMethods());
        addDefaultParameters(methods, (arguments, params, method) -> {
            BlockStatement block = new BlockStatement();

            MethodNode newMethod = new MethodNode(method.getName(), method.getModifiers() & ~ACC_ABSTRACT, method.getReturnType(), params, method.getExceptions(), block);

            MethodNode oldMethod = type.getDeclaredMethod(method.getName(), params);
            if (oldMethod != null) {
                throw new RuntimeParserException(
                        "The method with default parameters \"" + method.getTypeDescriptor() +
                                "\" defines a method \"" + newMethod.getTypeDescriptor() +
                                "\" that is already defined.",
                        sourceOf(method));
            }

            newMethod.addAnnotations(method.getAnnotations());
            newMethod.setGenericsTypes(method.getGenericsTypes());

            // GROOVY-5632, GROOVY-9151: check for references to parameters that have been removed
            GroovyCodeVisitor visitor = new CodeVisitorSupport() {
                private boolean inClosure;

                @Override
                public void visitClosureExpression(final ClosureExpression e) {
                    boolean saved = inClosure; inClosure = true;
                    super.visitClosureExpression(e);
                    inClosure = saved;
                }

                @Override
                public void visitVariableExpression(final VariableExpression e) {
                    if (e.getAccessedVariable() instanceof Parameter) {
                        Parameter p = (Parameter) e.getAccessedVariable();
                        if (!Arrays.asList(params).contains(p) && Arrays.asList(method.getParameters()).contains(p)) { // GROOVY-10602
                            VariableScope blockScope = block.getVariableScope();
                            VariableExpression localVariable = (VariableExpression) blockScope.getDeclaredVariable(p.getName());
                            if (localVariable == null) {
                                // create a variable declaration so that the name can be found in the new method
                                localVariable = localVarX(p.getName(), p.getType());
                                localVariable.setModifiers(p.getModifiers());
                                blockScope.putDeclaredVariable(localVariable);
                                localVariable.setInStaticContext(blockScope.isInStaticContext());
                                block.addStatement(declS(localVariable, p.getInitialExpression()));
                            }
                            if (!localVariable.isClosureSharedVariable()) {
                                localVariable.setClosureSharedVariable(inClosure);
                            }
                        }
                    }
                }
            };
            visitor.visitArgumentlistExpression(arguments);

            // if variable was created to capture an initial value expression, reference it in arguments as well
            for (ListIterator<Expression> it = arguments.getExpressions().listIterator(); it.hasNext(); ) {
                Expression argument = it.next();
                if (argument instanceof CastExpression) {
                    argument = ((CastExpression) argument).getExpression();
                }

                for (Parameter p : method.getParameters()) {
                    if (p.hasInitialExpression() && p.getInitialExpression() == argument) {
                        if (block.getVariableScope().getDeclaredVariable(p.getName()) != null) {
                            it.set(varX(p.getName()));
                        }
                        break;
                    }
                }
            }

            // delegate to original method using arguments derived from defaults
            MethodCallExpression call = callThisX(method.getName(), arguments);
            call.setMethodTarget(method);
            call.setImplicitThis(true);

            if (method.isVoidMethod()) {
                block.addStatement(new ExpressionStatement(call));
            } else {
                block.addStatement(new ReturnStatement(call));
            }

            // GROOVY-5681: set anon. inner enclosing method reference
            visitor = new CodeVisitorSupport() {
                @Override
                public void visitConstructorCallExpression(final ConstructorCallExpression call) {
                    if (call.isUsingAnonymousInnerClass()) {
                        call.getType().setEnclosingMethod(newMethod);
                    }
                    super.visitConstructorCallExpression(call);
                }
            };
            visitor.visitBlockStatement(block);

            addPropertyMethod(newMethod);
            // GRECLIPSE add
            newMethod.setOriginal(method);
            newMethod.setNameEnd(method.getNameEnd());
            newMethod.setNameStart(method.getNameStart());
            Integer value = method.getNodeMetaData("rparen.offset");
            if (value != null) newMethod.putNodeMetaData("rparen.offset", value);
            // GRECLIPSE end
            newMethod.putNodeMetaData(DEFAULT_PARAMETER_GENERATED, Boolean.TRUE);
        });
    }

    /**
     * Creates a new constructor for each combination of default parameter expressions.
     */
    protected void addDefaultParameterConstructors(final ClassNode type) {
        List<ConstructorNode> constructors = new ArrayList<>(type.getDeclaredConstructors());
        addDefaultParameters(constructors, (arguments, params, method) -> {
            // GROOVY-9151: check for references to parameters that have been removed
            for (ListIterator<Expression> it = arguments.getExpressions().listIterator(); it.hasNext(); ) {
                Expression argument = it.next();
                if (argument instanceof CastExpression) {
                    argument = ((CastExpression) argument).getExpression();
                }
                if (argument instanceof VariableExpression) {
                    VariableExpression v = (VariableExpression) argument;
                    if (v.getAccessedVariable() instanceof Parameter) {
                        Parameter p = (Parameter) v.getAccessedVariable();
                        if (p.hasInitialExpression() && !Arrays.asList(params).contains(p)
                                && p.getInitialExpression() instanceof ConstantExpression) {
                            // replace argument "(Type) param" with "(Type) <param's default>" for simple default value
                            it.set(castX(method.getParameters()[it.nextIndex() - 1].getType(), p.getInitialExpression()));
                        }
                    }
                }
            }
            GroovyCodeVisitor visitor = new CodeVisitorSupport() {
                @Override
                public void visitVariableExpression(final VariableExpression e) {
                    if (e.getAccessedVariable() instanceof Parameter) {
                        Parameter p = (Parameter) e.getAccessedVariable();
                        if (p.hasInitialExpression() && !Arrays.asList(params).contains(p)) {
                            String error = String.format(
                                    "The generated constructor \"%s(%s)\" references parameter '%s' which has been replaced by a default value expression.",
                                    type.getNameWithoutPackage(),
                                    Arrays.stream(params).map(Parameter::getType).map(ClassNodeUtils::formatTypeName).collect(joining(",")),
                                    p.getName());
                            throw new RuntimeParserException(error, sourceOf(method));
                        }
                    }
                }
            };
            visitor.visitArgumentlistExpression(arguments);

            // delegate to original constructor using arguments derived from defaults
            Statement code = new ExpressionStatement(new ConstructorCallExpression(ClassNode.THIS, arguments));
            addConstructor(params, (ConstructorNode) method, code, type);
        });
    }

    protected void addConstructor(final Parameter[] newParams, final ConstructorNode ctor, final Statement code, final ClassNode type) {
        ConstructorNode newConstructor = type.addConstructor(ctor.getModifiers(), newParams, ctor.getExceptions(), code);
        newConstructor.putNodeMetaData(DEFAULT_PARAMETER_GENERATED, Boolean.TRUE);
        markAsGenerated(type, newConstructor);
        // TODO: Copy annotations, etc.?
        // GRECLIPSE add
        newConstructor.setOriginal(ctor);
        newConstructor.setNameEnd(ctor.getNameEnd());
        newConstructor.setNameStart(ctor.getNameStart());
        Integer value = ctor.getNodeMetaData("rparen.offset");
        if (value != null) newConstructor.putNodeMetaData("rparen.offset", value);
        // GRECLIPSE end

        // set anon. inner enclosing method reference
        code.visit(new CodeVisitorSupport() {
            @Override
            public void visitConstructorCallExpression(final ConstructorCallExpression call) {
                if (call.isUsingAnonymousInnerClass()) {
                    call.getType().setEnclosingMethod(newConstructor);
                }
                super.visitConstructorCallExpression(call);
            }
        });
    }

    /**
     * Creates a new helper method for each combination of default parameter expressions.
     */
    protected void addDefaultParameters(final List<? extends MethodNode> methods, final DefaultArgsAction action) {
        for (MethodNode method : methods) {
            if (method.hasDefaultValue()) {
                addDefaultParameters(action, method);
            }
        }
    }

    protected void addDefaultParameters(final DefaultArgsAction action, final MethodNode method) {
        Parameter[] parameters = method.getParameters();
        long n = Arrays.stream(parameters).filter(Parameter::hasInitialExpression).count();

        for (int i = 1; i <= n; i += 1) {
            Parameter[] newParams = new Parameter[parameters.length - i];
            ArgumentListExpression arguments = new ArgumentListExpression();
            int index = 0;
            int j = 1;
            for (Parameter parameter : parameters) {
                if (parameter == null) {
                    throw new GroovyBugError("Parameter should not be null for method " + methodNode.getName());
                } else {
                    Expression e;
                    if (j > n - i && parameter.hasInitialExpression()) {
                        e = parameter.getInitialExpression();
                    } else {
                        newParams[index++] = parameter;
                        e = varX(parameter);
                    }

                    arguments.addExpression(castX(parameter.getType(), e));

                    if (parameter.hasInitialExpression()) j += 1;
                }
            }
            action.call(arguments, newParams, method);
        }

        for (Parameter parameter : parameters) {
            if (parameter.hasInitialExpression()) {
                // remove default expression and store it as node metadata
                parameter.putNodeMetaData(Verifier.INITIAL_EXPRESSION,
                        parameter.getInitialExpression());
                parameter.setInitialExpression(null);
            }
        }
    }

    protected void addClosureCode(InnerClassNode node) {
        // add a new invoke
    }

    protected void addInitialization(final ClassNode node) {
        boolean addSwapInit = moveOptimizedConstantsInitialization(node);

        for (ConstructorNode cn : node.getDeclaredConstructors()) {
            addInitialization(node, cn);
        }

        if (addSwapInit) {
            BytecodeSequence seq = new BytecodeSequence(new BytecodeInstruction() {
                @Override
                public void visit(MethodVisitor mv) {
                    mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(node), SWAP_INIT, "()V", false);
                }
            });

            List<Statement> swapCall = new ArrayList<>(1);
            swapCall.add(seq);
            node.addStaticInitializerStatements(swapCall, true);
        }
    }

    protected void addInitialization(final ClassNode node, final ConstructorNode constructorNode) {
        Statement firstStatement = constructorNode.getFirstStatement();

        // if some transformation decided to generate constructor then it probably knows best
        if (firstStatement instanceof BytecodeSequence) return;

        ConstructorCallExpression specialCtorCall = getFirstIfSpecialConstructorCall(firstStatement);

        // in case of this(...) let the other constructor initialize
        if (specialCtorCall != null && specialCtorCall.isThisCall()) return;

        boolean isEnum = node.isEnum();
        List<Statement> statements = new ArrayList<>();
        List<Statement> staticStatements = new ArrayList<>();
        List<Statement> initStmtsAfterEnumValuesInit = new ArrayList<>();

        if (!Traits.isTrait(node)) {
            Set<String> explicitStaticPropsInEnum = !isEnum
                    ? Collections.emptySet() : getExplicitStaticProperties(node);

            for (FieldNode fn : node.getFields()) {
                addFieldInitialization(statements, staticStatements, fn, isEnum,
                        initStmtsAfterEnumValuesInit, explicitStaticPropsInEnum);
            }
        }

        BlockStatement block = getCodeAsBlock(constructorNode);
        List<Statement> blockStatements = block.getStatements();
        if (!blockStatements.isEmpty()) {
            if (specialCtorCall != null) {
                blockStatements.remove(0);
                statements.add(0, firstStatement);
                // GROOVY-7686: place local variable references above super ctor call
                if (node instanceof InnerClassNode && ((InnerClassNode) node).isAnonymous()) {
                    extractVariableReferenceInitializers(statements).forEach(s -> statements.add(0, s));
                }
            }
            if (node instanceof InnerClassNode) {
                // GROOVY-4471: place this$0 init above other field init and super ctor call;
                // there can be field initializers that depend on method/property dispatching
                Statement initThis$0 = getImplicitThis$0Stmt(blockStatements);
                if (initThis$0 != null) {
                    statements.add(0, initThis$0);
                }
            }
            statements.addAll(node.getObjectInitializerStatements());
            statements.addAll(blockStatements);
        } else {
            statements.addAll(node.getObjectInitializerStatements());
        }

        BlockStatement newBlock = new BlockStatement(statements, block.getVariableScope());
        newBlock.setSourcePosition(block);
        constructorNode.setCode(newBlock);

        if (!staticStatements.isEmpty()) {
            if (isEnum) {
                // GROOVY-3161: initialization statements for explicitly declared static
                // fields inside an enum should come after enum values are initialized
                staticStatements.removeAll(initStmtsAfterEnumValuesInit);
                node.addStaticInitializerStatements(staticStatements, true);
                if (!initStmtsAfterEnumValuesInit.isEmpty()) {
                    node.positionStmtsAfterEnumInitStmts(initStmtsAfterEnumValuesInit);
                }
            } else {
                node.addStaticInitializerStatements(staticStatements, true);
            }
        }
    }

    private static Set<String> getExplicitStaticProperties(final ClassNode cn) {
        Set<String> staticProperties = new HashSet<>();
        for (PropertyNode pn : cn.getProperties()) {
            if (!pn.isSynthetic() && pn.getField().isStatic()) {
                staticProperties.add(pn.getField().getName());
            }
        }
        for (FieldNode fn : cn.getFields()) {
            if (!fn.isSynthetic() && fn.isStatic() && fn.getType() != cn) {
                staticProperties.add(fn.getName());
            }
        }
        return staticProperties;
    }

    /**
     * When InnerClassVisitor adds <code>this.this$0 = $p$n</code>, it adds it
     * as a BlockStatement having that ExpressionStatement.
     */
    private static Statement getImplicitThis$0Stmt(final List<Statement> statements) {
        for (Statement stmt : statements) {
            if (stmt instanceof BlockStatement) {
                List<Statement> stmts = ((BlockStatement) stmt).getStatements();
                for (Statement bstmt : stmts) {
                    if (bstmt instanceof ExpressionStatement) {
                        if (extractImplicitThis$0StmtFromExpression(stmts, bstmt)) return bstmt;
                    }
                }
            } else if (stmt instanceof ExpressionStatement) {
                if (extractImplicitThis$0StmtFromExpression(statements, stmt)) return stmt;
            }
        }
        return null;
    }

    private static boolean extractImplicitThis$0StmtFromExpression(final List<Statement> stmts, final Statement exprStmt) {
        Expression expr = ((ExpressionStatement) exprStmt).getExpression();
        if (expr instanceof BinaryExpression) {
            Expression lExpr = ((BinaryExpression) expr).getLeftExpression();
            if (lExpr instanceof FieldExpression) {
                if ("this$0".equals(((FieldExpression) lExpr).getFieldName())) {
                    stmts.remove(exprStmt); // remove from here and let the caller reposition it
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Statement> extractVariableReferenceInitializers(final List<Statement> statements) {
        List<Statement> localVariableReferences = new ArrayList<>();
        for (ListIterator<Statement> it = statements.listIterator(1); it.hasNext();) {
            // the first statement is the super constructor call  ^

            Statement stmt = it.next();
            if (stmt instanceof ExpressionStatement
                    && ((ExpressionStatement) stmt).getExpression() instanceof BinaryExpression) {
                BinaryExpression expr = (BinaryExpression) ((ExpressionStatement) stmt).getExpression();

                if (expr.getOperation().getType() == Types.ASSIGN
                        && expr.getLeftExpression() instanceof FieldExpression
                        && expr.getLeftExpression().getType().equals(ClassHelper.REFERENCE_TYPE)
                        && (((FieldExpression) expr.getLeftExpression()).getField().getModifiers() & Opcodes.ACC_SYNTHETIC) != 0
                        /* also could check if the right expression is a variable expression that references ctor parameter */) {
                    localVariableReferences.add(stmt);
                    it.remove();
                }
            }
        }
        return localVariableReferences;
    }

    // TODO: add generics to collections
    protected void addFieldInitialization(final List list, final List staticList, final FieldNode fieldNode, final boolean isEnumClassNode, final List initStmtsAfterEnumValuesInit, final Set explicitStaticPropsInEnum) {
        Expression expression = fieldNode.getInitialExpression();
        if (expression != null) {
            final FieldExpression fe = fieldX(fieldNode);
            if (fieldNode.getType().equals(ClassHelper.REFERENCE_TYPE) && ((fieldNode.getModifiers() & ACC_SYNTHETIC) != 0)) {
                fe.setUseReferenceDirectly(true);
            }
            Statement statement = stmt(binX(
                    fe,
                    Token.newSymbol(Types.ASSIGN, fieldNode.getLineNumber(), fieldNode.getColumnNumber()),
                    expression
            ));
            if (fieldNode.isStatic()) {
                // GRECLIPSE add
                // only conditionally 'move stuff around'
                if (inlineStaticFieldInitializersIntoClinit) {
                // GRECLIPSE end
                // GROOVY-3311: pre-defined constants added by compiler for numbers/characters should be
                // initialized first so that code dependent on them does not see their values as empty
                Expression initialValueExpression = fieldNode.getInitialValueExpression();
                Expression transformed = transformInlineConstants(initialValueExpression, fieldNode.getType());
                if (transformed instanceof ConstantExpression) {
                    ConstantExpression cexp = (ConstantExpression) transformed;
                    cexp = transformToPrimitiveConstantIfPossible(cexp);
                    if (fieldNode.isFinal() && ClassHelper.isStaticConstantInitializerType(cexp.getType()) && cexp.getType().equals(fieldNode.getType())) {
                        fieldNode.setInitialValueExpression(transformed);
                        return; // GROOVY-5150: primitive type constants will be initialized directly
                    }
                    staticList.add(0, statement);
                } else {
                    staticList.add(statement);
                }
                fieldNode.setInitialValueExpression(null); // to avoid double initialization in case of several constructors
                // GRECLIPSE add
                }
                // GRECLIPSE end
                /*
                 * If it is a statement for an explicitly declared static field inside an enum, store its
                 * reference. For enums, they need to be handled differently as such init statements should
                 * come after the enum values have been initialized inside <clinit> block. GROOVY-3161.
                 */
                if (isEnumClassNode && explicitStaticPropsInEnum.contains(fieldNode.getName())) {
                    initStmtsAfterEnumValuesInit.add(statement);
                }
            } else {
                list.add(statement);
            }
        }
    }

    /**
     * Capitalizes the start of the given bean property name.
     */
    public static String capitalize(final String name) {
        return BeanUtils.capitalize(name);
    }

    protected Statement createGetterBlock(final PropertyNode propertyNode, final FieldNode field) {
        String owner = BytecodeHelper.getClassInternalName(classNode);
        return new BytecodeSequence(new BytecodeInstruction() {
            @Override
            public void visit(final MethodVisitor mv) {
                if (field.isStatic()) {
                    mv.visitFieldInsn(GETSTATIC, owner, field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, owner, field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                }
                BytecodeHelper.doReturn(mv, field.getType());
            }
        });
    }

    protected Statement createSetterBlock(final PropertyNode propertyNode, final FieldNode field) {
        String owner = BytecodeHelper.getClassInternalName(classNode);
        return new BytecodeSequence(new BytecodeInstruction() {
            @Override
            public void visit(final MethodVisitor mv) {
                if (field.isStatic()) {
                    BytecodeHelper.load(mv, field.getType(), 0);
                    mv.visitFieldInsn(PUTSTATIC, owner, field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    BytecodeHelper.load(mv, field.getType(), 1);
                    mv.visitFieldInsn(PUTFIELD, owner, field.getName(), BytecodeHelper.getTypeDescription(field.getType()));
                }
                mv.visitInsn(RETURN);
            }
        });
    }

    public void visitGenericType(final GenericsType genericsType) {
    }

    public static Long getTimestampFromFieldName(final String fieldName) {
        if (fieldName.startsWith(__TIMESTAMP__)) {
            try {
                return Long.decode(fieldName.substring(__TIMESTAMP__.length()));
            } catch (NumberFormatException e) {
                return Long.MAX_VALUE;
            }
        }
        return null;
    }

    public static long getTimestamp(final Class<?> clazz) {
        if (clazz.getClassLoader() instanceof GroovyClassLoader.InnerLoader) {
            GroovyClassLoader.InnerLoader innerLoader = (GroovyClassLoader.InnerLoader) clazz.getClassLoader();
            return innerLoader.getTimeStamp();
        }

        for (Field field : clazz.getFields()) {
            if (isStatic(field.getModifiers())) {
                Long timestamp = getTimestampFromFieldName(field.getName());
                if (timestamp != null) {
                    return timestamp;
                }
            }
        }
        return Long.MAX_VALUE;
    }

    protected void addCovariantMethods(final ClassNode classNode) {
        Map<String, MethodNode> methodsToAdd = new HashMap<>();
        Map<String, ClassNode> genericsSpec = new HashMap<>();

        // unimplemented abstract methods from interfaces
        Map<String, MethodNode> abstractMethods = ClassNodeUtils.getDeclaredMethodsFromInterfaces(classNode);
        Map<String, MethodNode> allInterfaceMethods = new HashMap<>(abstractMethods);
        ClassNodeUtils.addDeclaredMethodsFromAllInterfaces(classNode, allInterfaceMethods);
        List<MethodNode> declaredMethods = new ArrayList<>(classNode.getMethods());
        // remove all static, private and package private methods
        for (Iterator<MethodNode> methodsIterator = declaredMethods.iterator(); methodsIterator.hasNext(); ) {
            MethodNode m = methodsIterator.next();
            abstractMethods.remove(m.getTypeDescriptor());
            if (m.isStatic() || !(m.isPublic() || m.isProtected())) {
                methodsIterator.remove();
            }
            MethodNode intfMethod = allInterfaceMethods.get(m.getTypeDescriptor());
            if (intfMethod != null && ((m.getModifiers() & ACC_SYNTHETIC) == 0)
                    && !m.isPublic() && !m.isStaticConstructor()) {
                throw new RuntimeParserException("The method " + m.getName() +
                        " should be public as it implements the corresponding method from interface " +
                        intfMethod.getDeclaringClass(), sourceOf(m));

            }
        }

        addCovariantMethods(classNode, declaredMethods, abstractMethods, methodsToAdd, genericsSpec);

        Map<String, MethodNode> declaredMethodsMap = new HashMap<>();
        if (!methodsToAdd.isEmpty()) {
            for (MethodNode mn : declaredMethods) {
                declaredMethodsMap.put(mn.getTypeDescriptor(), mn);
            }
        }

        for (Map.Entry<String, MethodNode> entry : methodsToAdd.entrySet()) {
            // we skip bridge methods implemented in current class already
            MethodNode mn = declaredMethodsMap.get(entry.getKey());
            if (mn == null || !mn.getDeclaringClass().equals(classNode)) {
                addPropertyMethod(entry.getValue());
            }
        }
    }

    private void addCovariantMethods(final ClassNode classNode, final List<MethodNode> declaredMethods, final Map<String, MethodNode> abstractMethods, final Map<String, MethodNode> methodsToAdd, final Map<String, ClassNode> oldGenericsSpec) {
        ClassNode sn = classNode.getUnresolvedSuperClass(false);
        if (sn != null) {
            Map<String, ClassNode> genericsSpec = createGenericsSpec(sn, oldGenericsSpec);
            List<MethodNode> classMethods = sn.getMethods();
            // original class causing bridge methods for methods in super class
            storeMissingCovariantMethods(declaredMethods, methodsToAdd, genericsSpec, classMethods);
            // super class causing bridge methods for abstract methods in original class
            if (!abstractMethods.isEmpty()) {
                for (MethodNode method : classMethods) {
                    if (method.isStatic()) continue;
                    storeMissingCovariantMethods(abstractMethods.values(), method, methodsToAdd, Collections.emptyMap(), true);
                }
            }

            addCovariantMethods(sn.redirect(), declaredMethods, abstractMethods, methodsToAdd, genericsSpec);
        }

        for (ClassNode anInterface : classNode.getInterfaces()) {
            List<MethodNode> interfacesMethods = anInterface.getMethods();
            Map<String, ClassNode> genericsSpec = createGenericsSpec(anInterface, oldGenericsSpec);
            storeMissingCovariantMethods(declaredMethods, methodsToAdd, genericsSpec, interfacesMethods);
            addCovariantMethods(anInterface, declaredMethods, abstractMethods, methodsToAdd, genericsSpec);
        }
    }

    private void storeMissingCovariantMethods(final List<MethodNode> declaredMethods, final Map<String, MethodNode> methodsToAdd, final Map<String, ClassNode> genericsSpec, final List<MethodNode> methodNodeList) {
        for (MethodNode method : declaredMethods) {
            if (method.isStatic()) continue;
            storeMissingCovariantMethods(methodNodeList, method, methodsToAdd, genericsSpec, false);
        }
    }

    private MethodNode getCovariantImplementation(final MethodNode oldMethod, final MethodNode overridingMethod, Map<String, ClassNode> genericsSpec, final boolean ignoreError) {
        if (!oldMethod.getName().equals(overridingMethod.getName())) return null;
        if ((overridingMethod.getModifiers() & ACC_BRIDGE) != 0) return null;
        if ((oldMethod.getModifiers() & ACC_BRIDGE) != 0) return null;
        if (oldMethod.isPrivate()) return null;

        if (oldMethod.getGenericsTypes() != null)
            genericsSpec = addMethodGenerics(oldMethod, genericsSpec);

        // parameters
        boolean equalParameters = equalParametersNormal(overridingMethod, oldMethod);
        if (!equalParameters && !equalParametersWithGenerics(overridingMethod, oldMethod, genericsSpec)) return null;

        // return type
        ClassNode nmr = overridingMethod.getReturnType();
        ClassNode omr = oldMethod.getReturnType();
        boolean equalReturnType = nmr.equals(omr);

        // TODO: ClassNode nmrCorrected = correctToGenericsSpec(..., nmr)?
        ClassNode omrCorrected = correctToGenericsSpec(genericsSpec, omr);
        if (!isAssignable(nmr, omrCorrected)) {
            if (ignoreError) return null;
            throw new RuntimeParserException(
                    "The return type of " +
                            overridingMethod.getTypeDescriptor() +
                            " in " + overridingMethod.getDeclaringClass().getName() +
                            " is incompatible with " + omrCorrected.getName() +
                            " in " + oldMethod.getDeclaringClass().getName(),
                    sourceOf(overridingMethod));
        }

        if (equalReturnType && equalParameters) return null;

        if (oldMethod.isFinal()) {
            throw new RuntimeParserException(
                    "Cannot override final method " +
                            oldMethod.getTypeDescriptor() +
                            " in " + oldMethod.getDeclaringClass().getName(),
                    sourceOf(overridingMethod));
        }
        if (oldMethod.isStatic() != overridingMethod.isStatic()) {
            throw new RuntimeParserException(
                    "Cannot override method " +
                            oldMethod.getTypeDescriptor() +
                            " in " + oldMethod.getDeclaringClass().getName() +
                            " with disparate static modifier",
                    sourceOf(overridingMethod));
        }
        if (!equalReturnType) {
            boolean oldM = ClassHelper.isPrimitiveType(omr);
            boolean newM = ClassHelper.isPrimitiveType(nmr);
            if (oldM || newM) {
                String message;
                if (oldM && newM) {
                    message = " with old and new method having different primitive return types";
                } else if (newM) {
                    message = " with new method having a primitive return type and old method not";
                } else /*oldM*/ {
                    message = " with old method having a primitive return type and new method not";
                }
                throw new RuntimeParserException(
                        "Cannot override method " +
                                oldMethod.getTypeDescriptor() +
                                " in " + oldMethod.getDeclaringClass().getName() +
                                message,
                        sourceOf(overridingMethod));
            }
            // GRECLIPSE add -- GROOVY-8955
            if (overridingMethod.isDynamicReturnType() && equalParameters) {
                overridingMethod.setReturnType(cleanType(omr));
                return null;
            }
            // GRECLIPSE end
        }

        // if we reach this point there is least one parameter or return type
        // that is different in its specified form, so create a bridge method
        String owner = BytecodeHelper.getClassInternalName(classNode);
        return new MethodNode(
                oldMethod.getName(),
                overridingMethod.getModifiers() | ACC_SYNTHETIC | ACC_BRIDGE,
                cleanType(omr),
                cleanParameters(oldMethod.getParameters()),
                oldMethod.getExceptions(),
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        Parameter[] para = oldMethod.getParameters();
                        Parameter[] goal = overridingMethod.getParameters();
                        int doubleSlotOffset = 0;
                        for (int i = 0, n = para.length; i < n; i += 1) {
                            ClassNode type = para[i].getType();
                            BytecodeHelper.load(mv, type, i + 1 + doubleSlotOffset);
                            if (type.redirect() == ClassHelper.double_TYPE
                                    || type.redirect() == ClassHelper.long_TYPE) {
                                doubleSlotOffset += 1;
                            }
                            if (!type.equals(goal[i].getType())) {
                                BytecodeHelper.doCast(mv, goal[i].getType());
                            }
                        }
                        mv.visitMethodInsn(INVOKEVIRTUAL, owner, overridingMethod.getName(), BytecodeHelper.getMethodDescriptor(nmr, overridingMethod.getParameters()), false);

                        BytecodeHelper.doReturn(mv, oldMethod.getReturnType());
                    }
                })
        );
    }

    private static boolean isAssignable(final ClassNode node, final ClassNode testNode) {
        if (node.isArray() && testNode.isArray()) {
            return isArrayAssignable(node.getComponentType(), testNode.getComponentType());
        }
        if (testNode.isInterface()) {
            if (node.equals(testNode) || node.implementsInterface(testNode)) return true;
        }
        return node.isDerivedFrom(testNode);
    }

    private static boolean isArrayAssignable(final ClassNode node, final ClassNode testNode) {
        if (node.isArray() && testNode.isArray()) {
            return isArrayAssignable(node.getComponentType(), testNode.getComponentType());
        }
        return isAssignable(node, testNode);
    }

    private static Parameter[] cleanParameters(final Parameter[] parameters) {
        return Arrays.stream(parameters).map(p -> param(cleanType(p.getType()), p.getName())).toArray(Parameter[]::new);
    }

    private static ClassNode cleanType(final ClassNode type) {
        if (type.isArray()) {
            return cleanType(type.getComponentType()).makeArray();
        }
        return type.redirect().getPlainNodeReference();
    }

    private void storeMissingCovariantMethods(final Iterable<MethodNode> methods, final MethodNode method, final Map<String, MethodNode> methodsToAdd, final Map<String, ClassNode> genericsSpec, final boolean ignoreError) {
        for (MethodNode toOverride : methods) {
            MethodNode bridgeMethod = getCovariantImplementation(toOverride, method, genericsSpec, ignoreError);
            if (bridgeMethod != null) {
                // GRECLIPSE add
                bridgeMethod.setOriginal(method.getOriginal());
                // GRECLIPSE end
                methodsToAdd.put(bridgeMethod.getTypeDescriptor(), bridgeMethod);
                return;
            }
        }
    }

    private static boolean equalParametersNormal(final MethodNode m1, final MethodNode m2) {
        Parameter[] p1 = m1.getParameters();
        Parameter[] p2 = m2.getParameters();
        if (p1.length != p2.length) return false;
        for (int i = 0, n = p2.length; i < n; i += 1) {
            ClassNode type = p2[i].getType();
            ClassNode parameterType = p1[i].getType();
            if (!parameterType.equals(type)) return false;
        }
        return true;
    }

    private static boolean equalParametersWithGenerics(final MethodNode m1, final MethodNode m2, final Map<String, ClassNode> genericsSpec) {
        Parameter[] p1 = m1.getParameters();
        Parameter[] p2 = m2.getParameters();
        if (p1.length != p2.length) return false;
        for (int i = 0, n = p2.length; i < n; i += 1) {
            ClassNode type = p2[i].getType();
            ClassNode genericsType = correctToGenericsSpec(genericsSpec, type);
            ClassNode parameterType = p1[i].getType();
            if (!parameterType.equals(genericsType)) return false;
        }
        return true;
    }

    private static boolean moveOptimizedConstantsInitialization(final ClassNode node) {
        if (node.isInterface() && !Traits.isTrait(node)) return false;

        String name = SWAP_INIT;
        int mods = ACC_STATIC | ACC_SYNTHETIC | ACC_PUBLIC;
        BlockStatement methodCode = block(new SwapInitStatement());

        boolean swapInitRequired = false;
        for (FieldNode fn : node.getFields()) {
            if (!fn.isStatic() || !fn.isSynthetic() || !fn.getName().startsWith("$const$")) continue;
            if (fn.getInitialExpression() == null) continue;
            final FieldExpression fe = fieldX(fn);
            if (fn.getType().equals(ClassHelper.REFERENCE_TYPE)) fe.setUseReferenceDirectly(true);
            ConstantExpression init = (ConstantExpression) fn.getInitialExpression();
            init = constX(init.getValue(), true);
            Statement statement = stmt(binX(
                    fe,
                    Token.newSymbol(Types.ASSIGN, fn.getLineNumber(), fn.getColumnNumber()),
                    init
            ));
            fn.setInitialValueExpression(null);
            methodCode.addStatement(statement);
            swapInitRequired = true;
        }

        if (swapInitRequired) {
            node.addSyntheticMethod(
                    name, mods, ClassHelper.VOID_TYPE,
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, methodCode);
        }

        return swapInitRequired;
    }

    private static ASTNode sourceOf(final MethodNode methodNode) {
        if (methodNode.getLineNumber() < 1) {
            ClassNode declaringClass = methodNode.getDeclaringClass();
            if (methodNode.isSynthetic()) {
                String propertyName = getPropertyName(methodNode);
                if (propertyName != null) {
                    PropertyNode propertyNode = declaringClass.getProperty(propertyName);
                    if (propertyNode != null && propertyNode.getLineNumber() > 0) {
                        return propertyNode;
                    }
                }
            }
            return declaringClass;
        }
        return methodNode;
    }

    /**
     * When constant expressions are created, the value is always wrapped to a non primitive type.
     * Some constant expressions are optimized to return primitive types, but not all primitives are
     * handled. This method guarantees to return a similar constant expression but with a primitive type
     * instead of a boxed type.
     * <p/>
     * Additionally, single char strings are converted to 'char' types.
     *
     * @param constantExpression a constant expression
     * @return the same instance of constant expression if the type is already primitive, or a primitive
     * constant if possible.
     */
    public static ConstantExpression transformToPrimitiveConstantIfPossible(final ConstantExpression constantExpression) {
        Object value = constantExpression.getValue();
        if (value == null) return constantExpression;
        ConstantExpression result;
        ClassNode type = constantExpression.getType();
        if (ClassHelper.isPrimitiveType(type)) return constantExpression;
        if (value instanceof String && ((String) value).length() == 1) {
            result = constX(((String) value).charAt(0));
            result.setType(ClassHelper.char_TYPE);
        } else {
            type = ClassHelper.getUnwrapper(type);
            result = constX(value, true);
            result.setType(type);
        }
        return result;
    }

    //--------------------------------------------------------------------------

    private static class SwapInitStatement extends BytecodeSequence {
        private WriterController controller;

        SwapInitStatement() {
            super(new SwapInitInstruction());
            ((SwapInitInstruction) getInstructions().get(0)).statement = this;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                controller = ((AsmClassGenerator) visitor).getController();
            }
            super.visit(visitor);
        }

        private static class SwapInitInstruction extends BytecodeInstruction {
            private SwapInitStatement statement;

            @Override
            public void visit(final MethodVisitor mv) {
                statement.controller.getCallSiteWriter().makeCallSiteArrayInitializer();
            }
        }
    }
}
