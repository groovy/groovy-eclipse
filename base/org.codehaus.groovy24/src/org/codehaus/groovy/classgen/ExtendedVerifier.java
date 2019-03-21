/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
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
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.PreciseSyntaxException;
import org.codehaus.groovy.syntax.SyntaxException;
import groovyjarjarasm.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    private SourceUnit source;
    private ClassNode currentClass;

    public ExtendedVerifier(SourceUnit sourceUnit) {
        this.source = sourceUnit;
    }

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

    public void visitField(FieldNode node) {
        visitAnnotations(node, AnnotationNode.FIELD_TARGET);
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitAnnotations(expression, AnnotationNode.LOCAL_VARIABLE_TARGET);
    }

    public void visitConstructor(ConstructorNode node) {
        visitConstructorOrMethod(node, AnnotationNode.CONSTRUCTOR_TARGET);
    }

    public void visitMethod(MethodNode node) {
        visitConstructorOrMethod(node, AnnotationNode.METHOD_TARGET);
    }

    private void visitConstructorOrMethod(MethodNode node, int methodTarget) {
        visitAnnotations(node, methodTarget);
        for (int i = 0; i < node.getParameters().length; i++) {
            Parameter parameter = node.getParameters()[i];
            visitAnnotations(parameter, AnnotationNode.PARAMETER_TARGET);
        }

        if (this.currentClass.isAnnotationDefinition() && !node.isStaticConstructor()) {
            ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
            AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
            visitor.setReportClass(currentClass);
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
                visitor.checkCircularReference(currentClass, node.getReturnType(), code.getExpression());
            }
            this.source.getErrorCollector().addCollectorContents(errorCollector);
        }
        Statement code = node.getCode();
        if (code != null) {
            code.visit(this);
        }

    }

    public void visitProperty(PropertyNode node) {
    }

    protected void visitAnnotations(AnnotatedNode node, int target) {
        if (node.getAnnotations().isEmpty()) {
            return;
        }
        this.currentClass.setAnnotated(true);
        if (!isAnnotationCompatible()) {
            addError("Annotations are not supported in the current runtime. " + JVM_ERROR_MESSAGE, node);
            return;
        }
        Map<String, List<AnnotationNode>> runtimeAnnotations = new LinkedHashMap<String, List<AnnotationNode>>();
        for (AnnotationNode unvisited : node.getAnnotations()) {
            AnnotationNode visited = visitAnnotation0(unvisited);
            String name = visited.getClassNode().getName();
            if (visited.hasRuntimeRetention()) {
                List<AnnotationNode> seen = runtimeAnnotations.get(name);
                if (seen == null) {
                    seen = new ArrayList<AnnotationNode>();
                }
                seen.add(visited);
                runtimeAnnotations.put(name, seen);
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
        checkForDuplicateAnnotations(runtimeAnnotations);
    }

    private void checkForDuplicateAnnotations(Map<String, List<AnnotationNode>> runtimeAnnotations) {
        for (Map.Entry<String, List<AnnotationNode>> next : runtimeAnnotations.entrySet()) {
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
                // TODO: further checks: that repeatableName is valid and has RUNTIME retention?
                if (repeatableName != null) {
                    addError("Annotation @" + next.getKey() + " has RUNTIME retention and " + next.getValue().size()
                            + " occurrences. Automatic repeated annotations are not supported in this version of Groovy. " +
                            "Consider using the explicit @" + repeatableName + " collector annotation instead.", next.getValue().get(1));
                } else {
                    addError("Annotation @" + next.getKey() + " has RUNTIME retention and " + next.getValue().size()
                            + " occurrences. Duplicate annotations not allowed.", next.getValue().get(1));
                }
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

    // TODO GROOVY-5011 handle case of @Override on a property
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
            Map genericsSpec = createGenericsSpec(next);
            MethodNode mn = correctToGenericsSpec(genericsSpec, method);
            if (next != cNode) {
                ClassNode correctedNext = correctToGenericsSpecRecurse(genericsSpec, next);
                MethodNode found = getDeclaredMethodCorrected(genericsSpec, mn, correctedNext);
                if (found != null) break;
            }
            List<ClassNode> ifaces = new ArrayList<ClassNode>();
            ifaces.addAll(Arrays.asList(next.getInterfaces()));
            Map updatedGenericsSpec = new HashMap(genericsSpec);
            while (!ifaces.isEmpty()) {
                ClassNode origInterface = ifaces.remove(0);
                if (!origInterface.equals(ClassHelper.OBJECT_TYPE)) {
                    updatedGenericsSpec = createGenericsSpec(origInterface, updatedGenericsSpec);
                    ClassNode iNode = correctToGenericsSpecRecurse(updatedGenericsSpec, origInterface);
                    MethodNode found2 = getDeclaredMethodCorrected(updatedGenericsSpec, mn, iNode);
                    if (found2 != null) break outer;
                    ifaces.addAll(Arrays.asList(iNode.getInterfaces()));
                }
            }
            ClassNode superClass = next.getUnresolvedSuperClass();
            if (superClass != null) {
                next = correctToGenericsSpecRecurse(updatedGenericsSpec, superClass);
            } else {
                next = null;
            }
        }
        return next != null;
    }

    private static MethodNode getDeclaredMethodCorrected(Map genericsSpec, MethodNode mn, ClassNode correctedNext) {
        for (MethodNode orig : correctedNext.getDeclaredMethods(mn.getName())) {
            MethodNode method = correctToGenericsSpec(genericsSpec, orig);
            if (ParameterUtils.parametersEqual(method.getParameters(), mn.getParameters())) {
                return method;
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
    protected boolean isAnnotationCompatible() {
        return CompilerConfiguration.isPostJDK5(this.source.getConfiguration().getTargetBytecode());
    }

    protected void addError(String msg, ASTNode expr) {
        // GRECLIPSE add -- use new form of error message that has an end column
        if (expr instanceof AnnotationNode) {
            AnnotationNode aNode = (AnnotationNode) expr;
            this.source.getErrorCollector().addErrorAndContinue(
                    new SyntaxErrorMessage(
                            new PreciseSyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), aNode.getStart(), aNode.getEnd()), this.source)
            );
        } else
        // GRECLIPSE end
        this.source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(
                        new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), this.source)
        );
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    // TODO use it or lose it
    public void visitGenericType(GenericsType genericsType) {

    }
}
