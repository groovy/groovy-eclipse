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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.control.AnnotationConstantsVisitor;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import groovyjarjarasm.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;

/**
 * A specialized Groovy AST visitor meant to perform additional verifications upon the
 * current AST. Currently it does checks on annotated nodes and annotations itself.
 * <p>
 * Current limitations:
 * - annotations on local variables are not supported
 */
public class ExtendedVerifier extends ClassCodeVisitorSupport {
    public static final String JVM_ERROR_MESSAGE = "Please make sure you are running on a JVM >= 1.5";

    private final SourceUnit source;
    private ClassNode currentClass;

    public ExtendedVerifier(SourceUnit sourceUnit) {
        this.source = sourceUnit;
    }

    @Override
    public void visitClass(ClassNode node) {
        AnnotationConstantsVisitor acv = new AnnotationConstantsVisitor();
        acv.visitClass(node, this.source);
        this.currentClass = node;
        if (node.isAnnotationDefinition()) {
            visitAnnotations(node, AnnotationNode.ANNOTATION_TARGET);
        } else {
            visitAnnotations(node, AnnotationNode.TYPE_TARGET);
        }
        PackageNode packageNode = node.getPackage();
        if (packageNode != null) {
            visitAnnotations(packageNode, AnnotationNode.PACKAGE_TARGET);
        }
        node.visitContents(this);
    }

    @Override
    public void visitField(FieldNode node) {
        visitAnnotations(node, AnnotationNode.FIELD_TARGET);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitAnnotations(expression, AnnotationNode.LOCAL_VARIABLE_TARGET);
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node, AnnotationNode.CONSTRUCTOR_TARGET);
    }

    @Override
    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node, AnnotationNode.METHOD_TARGET);
    }

    private void visitConstructorOrMethod(MethodNode node, int methodTarget) {
        visitAnnotations(node, methodTarget);
        for (Parameter parameter : node.getParameters()) {
            visitAnnotations(parameter, AnnotationNode.PARAMETER_TARGET);
        }

        if (this.currentClass.isAnnotationDefinition() && !node.isStaticConstructor()) {
            ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
            AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
            visitor.setReportClass(this.currentClass);
            visitor.checkReturnType(node.getReturnType(), node);
            if (node.getParameters().length > 0) {
                addError("Annotation members may not have parameters.", node.getParameters()[0]);
            }
            if (node.getExceptions().length > 0) {
                addError("Annotation members may not have a throws clause.", node.getExceptions()[0]);
            }
            ReturnStatement code = (ReturnStatement) node.getCode();
            if (code != null) {
                visitor.visitExpression(node.getName(), code.getExpression(), node.getReturnType());
                visitor.checkCircularReference(this.currentClass, node.getReturnType(), code.getExpression());
            }
            this.source.getErrorCollector().addCollectorContents(errorCollector);
        }
        Statement code = node.getCode();
        if (code != null) {
            code.visit(this);
        }
    }

    @Override
    public void visitProperty(PropertyNode node) {
    }

    private void visitAnnotations(AnnotatedNode node, int target) {
        if (node.getAnnotations().isEmpty()) {
            return;
        }
        this.currentClass.setAnnotated(true);
        if (!isAnnotationCompatible()) {
            addError("Annotations are not supported in the current runtime. " + JVM_ERROR_MESSAGE, node);
            return;
        }
        Map<String, List<AnnotationNode>> nonSourceAnnotations = new LinkedHashMap<String, List<AnnotationNode>>();
        for (AnnotationNode unvisited : node.getAnnotations()) {
            AnnotationNode visited = visitAnnotation0(unvisited);
            String name = visited.getClassNode().getName();
            // GRECLIPSE edit -- GROOVY-9095
            //if (visited.hasRuntimeRetention()) {
            if (!visited.hasSourceRetention()) {
            // GRECLIPSE end
                List<AnnotationNode> seen = nonSourceAnnotations.get(name);
                if (seen == null) {
                    seen = new ArrayList<AnnotationNode>();
                }
                seen.add(visited);
                nonSourceAnnotations.put(name, seen);
            }
            boolean isTargetAnnotation = name.equals("java.lang.annotation.Target");

            // Check if the annotation target is correct, unless it's the target annotating an annotation definition
            // defining on which target elements the annotation applies
            if (!isTargetAnnotation && !visited.isTargetAllowed(target)) {
                addError("Annotation @" + name + " is not allowed on element "
                        + AnnotationNode.targetToName(target), visited);
            }
            visitDeprecation(node, visited);
            visitOverride(node, visited);
        }
        checkForDuplicateAnnotations(nonSourceAnnotations);
    }

    private void checkForDuplicateAnnotations(Map<String, List<AnnotationNode>> nonSourceAnnotations) {
        for (Map.Entry<String, List<AnnotationNode>> next : nonSourceAnnotations.entrySet()) {
            if (next.getValue().size() > 1) {
                String repeatableName = null;
                AnnotationNode repeatee = next.getValue().get(0);
                List<AnnotationNode> repeateeAnnotations = repeatee.getClassNode().getAnnotations();
                for (AnnotationNode anno : repeateeAnnotations) {
                    ClassNode annoClassNode = anno.getClassNode();
                    if (annoClassNode.getName().equals("java.lang.annotation.Repeatable")) {
                        Expression value = anno.getMember("value");
                        if (value instanceof ClassExpression) {
                            ClassExpression ce = (ClassExpression) value;
                            if (ce.getType() != null && ce.getType().isAnnotationDefinition()) {
                                repeatableName = ce.getType().getName();
                            }
                        }
                        break;
                    }
                }
                /* GRECLIPSE edit -- GROOVY-9095
                if (repeatableName != null) {
                    addError("Annotation @" + next.getKey() + " has RUNTIME retention and " + next.getValue().size()
                            + " occurrences. Automatic repeated annotations are not supported in this version of Groovy. " +
                            "Consider using the explicit @" + repeatableName + " collector annotation instead.", next.getValue().get(1));
                } else {
                    addError("Annotation @" + next.getKey() + " has RUNTIME retention and " + next.getValue().size()
                            + " occurrences. Duplicate annotations not allowed.", next.getValue().get(1));
                }
                */
                String message = "Annotation @" + next.getKey() + " has " + (next.getValue().get(0).hasRuntimeRetention() ? "RUNTIME" : "CLASS") + " retention and " + next.getValue().size() + " occurrences.";
                message += (repeatableName == null ? " Duplicate annotations are not allowed." :
                    " Automatic repeated annotations are not supported in this version of Groovy." +
                        " Consider using the explicit @" + repeatableName + " collector annotation instead.");
                addError(message, next.getValue().get(1));
                // GRECLIPSE end
            }
        }
    }

    private static void visitDeprecation(AnnotatedNode node, AnnotationNode visited) {
        if (visited.getClassNode().isResolved() && visited.getClassNode().getName().equals("java.lang.Deprecated")) {
            if (node instanceof MethodNode) {
                MethodNode mn = (MethodNode) node;
                mn.setModifiers(mn.getModifiers() | Opcodes.ACC_DEPRECATED);
            } else if (node instanceof FieldNode) {
                FieldNode fn = (FieldNode) node;
                fn.setModifiers(fn.getModifiers() | Opcodes.ACC_DEPRECATED);
            } else if (node instanceof ClassNode) {
                ClassNode cn = (ClassNode) node;
                cn.setModifiers(cn.getModifiers() | Opcodes.ACC_DEPRECATED);
            }
        }
    }

    private void visitOverride(AnnotatedNode node, AnnotationNode visited) {
        ClassNode annotationClassNode = visited.getClassNode();
        if (annotationClassNode.isResolved() && annotationClassNode.getName().equals("java.lang.Override")) {
            if (node instanceof MethodNode && !Boolean.TRUE.equals(node.getNodeMetaData(Verifier.DEFAULT_PARAMETER_GENERATED))) {
                boolean override = false;
                MethodNode origMethod = (MethodNode) node;
                ClassNode cNode = origMethod.getDeclaringClass();
                if (origMethod.hasDefaultValue()) {
                    List<MethodNode> variants = cNode.getDeclaredMethods(origMethod.getName());
                    for (MethodNode m : variants) {
                        if (m.getAnnotations().contains(visited) && isOverrideMethod(m)) {
                            override = true;
                            break;
                        }
                    }
                } else {
                    override = isOverrideMethod(origMethod);
                }

                if (!override) {
                    addError("Method '" + origMethod.getName() + "' from class '" + cNode.getName() + "' does not override " +
                            "method from its superclass or interfaces but is annotated with @Override.", visited);
                }
            }
        }
    }

    private static boolean isOverrideMethod(MethodNode method) {
        ClassNode cNode = method.getDeclaringClass();
        ClassNode next = cNode;
        outer:
        while (next != null) {
            Map<String, ClassNode> genericsSpec = createGenericsSpec(next);
            MethodNode mn = correctToGenericsSpec(genericsSpec, method);
            if (next != cNode) {
                ClassNode correctedNext = correctToGenericsSpecRecurse(genericsSpec, next);
                MethodNode found = getDeclaredMethodCorrected(genericsSpec, mn, correctedNext);
                if (found != null) break;
            }
            List<ClassNode> ifaces = new ArrayList<ClassNode>(Arrays.asList(next.getInterfaces()));
            while (!ifaces.isEmpty()) {
                ClassNode origInterface = ifaces.remove(0);
                if (!origInterface.equals(ClassHelper.OBJECT_TYPE)) {
                    genericsSpec = createGenericsSpec(origInterface, genericsSpec);
                    ClassNode iNode = correctToGenericsSpecRecurse(genericsSpec, origInterface);
                    MethodNode found2 = getDeclaredMethodCorrected(genericsSpec, mn, iNode);
                    if (found2 != null) break outer;
                    Collections.addAll(ifaces, iNode.getInterfaces());
                }
            }
            ClassNode superClass = next.getUnresolvedSuperClass();
            if (superClass != null) {
                next = correctToGenericsSpecRecurse(genericsSpec, superClass);
            } else {
                next = null;
            }
        }
        return next != null;
    }

    private static MethodNode getDeclaredMethodCorrected(Map genericsSpec, MethodNode mn, ClassNode correctedNext) {
        for (MethodNode declared : correctedNext.getDeclaredMethods(mn.getName())) {
            MethodNode corrected = correctToGenericsSpec(genericsSpec, declared);
            if (ParameterUtils.parametersEqual(corrected.getParameters(), mn.getParameters())) {
                return corrected;
            }
        }
        return null;
    }

    /**
     * Resolve metadata and details of the annotation.
     *
     * @param unvisited the node to visit
     * @return the visited node
     */
    private AnnotationNode visitAnnotation0(AnnotationNode unvisited) {
        ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
        AnnotationNode visited = visitor.visit(unvisited);
        this.source.getErrorCollector().addCollectorContents(errorCollector);
        return visited;
    }

    /**
     * Check if the current runtime allows Annotation usage.
     *
     * @return true if running on a 1.5+ runtime
     */
    private boolean isAnnotationCompatible() {
        return CompilerConfiguration.isPostJDK5(this.source.getConfiguration().getTargetBytecode());
    }

    /* GRECLIPSE edit
    protected void addError(String msg, ASTNode expr) {
        this.source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(
                        new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), this.source)
        );
    }
    */

    @Override
    protected SourceUnit getSourceUnit() {
        return this.source;
    }

    /* GRECLIPSE edit
    public void visitGenericType(GenericsType genericsType) {
    }
    */
}
