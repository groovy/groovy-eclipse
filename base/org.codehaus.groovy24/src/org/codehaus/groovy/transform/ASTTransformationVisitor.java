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
package org.codehaus.groovy.transform;

import groovy.lang.GroovyClassLoader;
import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.ASTTransformationsContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the invocation of the ASTAnnotationTransformation
 * when it is encountered by a tree walk.  One instance of each exists
 * for each phase of the compilation it applies to.  Before invocation the
 * <p>
 * {@link org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor} will add a list
 * of annotations that this visitor should be concerned about.  All other
 * annotations are ignored, whether or not they are GroovyASTTransformation
 * annotated or not.
 * <p>
 * A Two-pass method is used. First all candidate annotations are added to a
 * list then the transformations are called on those collected annotations.
 * This is done to avoid concurrent modification exceptions during the AST tree
 * walk and allows the transformations to alter any portion of the AST tree.
 * Hence annotations that are added in this phase will not be processed as
 * transformations.  They will only be handled in later phases (and then only
 * if the type was in the AST prior to any AST transformations being run
 * against it).
 */
public final class ASTTransformationVisitor extends ClassCodeVisitorSupport {

    private final ASTTransformationsContext context;
    private final CompilePhase phase;
    private SourceUnit source;
    private List<ASTNode[]> targetNodes;
    private Map<ASTNode, List<ASTTransformation>> transforms;

    private ASTTransformationVisitor(final CompilePhase phase, final ASTTransformationsContext context) {
        this.phase = phase;
        this.context = context;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    // GRECLIPSE add
    private static String getTargetName(ASTNode node, ClassNode type) {
        if (node == type) {
            return "type: " + type.toString(false);
        }
        if (node instanceof FieldNode) {
            return "field: " + ((FieldNode) node).getName();
        }
        if (node instanceof MethodNode) {
            return "method: " + ((MethodNode) node).getName();
        }
        if (node instanceof PropertyNode) {
            return "property: " + ((PropertyNode) node).getName();
        }
        if (node instanceof DeclarationExpression) {
            DeclarationExpression expr = (DeclarationExpression) node;
            if (!expr.isMultipleAssignmentDeclaration()) {
                return "variable: " + expr.getVariableExpression().getName();
            } else {
                node = expr.getLeftExpression();
            }
        }
        return type.toString(false) + ":" + node;
    }
    // GRECLIPSE end

    /**
     * Main loop entry.
     * <p>
     * First, it delegates to the super visitClass so we can collect the
     * relevant annotations in an AST tree walk.
     * <p>
     * Second, it calls the visit method on the transformation for each relevant
     * annotation found.
     *
     * @param classNode the class to visit
     */
    public void visitClass(ClassNode classNode) {
        // only descend if we have annotations to look for
        Map<Class<? extends ASTTransformation>, Set<ASTNode>> baseTransforms = classNode.getTransforms(phase);
        if (!baseTransforms.isEmpty()) {
            final Map<Class<? extends ASTTransformation>, ASTTransformation> transformInstances = new HashMap<Class<? extends ASTTransformation>, ASTTransformation>();
            for (Class<? extends ASTTransformation> transformClass : baseTransforms.keySet()) {
                try {
                    transformInstances.put(transformClass, transformClass.newInstance());
                } catch (InstantiationException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could not instantiate Transformation Processor " + transformClass
                                    , //+ " declared by " + annotation.getClassNode().getName(),
                                    source));
                } catch (IllegalAccessException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could not instantiate Transformation Processor " + transformClass
                                    , //+ " declared by " + annotation.getClassNode().getName(),
                                    source));
                }
            }

            // invert the map, is now one to many
            transforms = new HashMap<ASTNode, List<ASTTransformation>>();
            for (Map.Entry<Class<? extends ASTTransformation>, Set<ASTNode>> entry : baseTransforms.entrySet()) {
                for (ASTNode node : entry.getValue()) {
                    List<ASTTransformation> list = transforms.get(node);
                    if (list == null)  {
                        list = new ArrayList<ASTTransformation>();
                        transforms.put(node, list);
                    }
                    list.add(transformInstances.get(entry.getKey()));
                }
            }

            targetNodes = new LinkedList<ASTNode[]>();

            // first pass, collect nodes
            super.visitClass(classNode);

            // second pass, call visit on all of the collected nodes
            for (ASTNode[] node : targetNodes) {
                for (ASTTransformation snt : transforms.get(node[0])) {
                    // GRECLIPSE add
                    try {
                        long t0 = System.nanoTime();
                        boolean okToSet = (source != null && source.getErrorCollector() != null);
                        try {
                            if (okToSet) {
                                source.getErrorCollector().transformActive = true;
                            }
                    // GRECLIPSE end
                    if (snt instanceof CompilationUnitAware) {
                        ((CompilationUnitAware)snt).setCompilationUnit(context.getCompilationUnit());
                    }
                    snt.visit(node, source);
                    // GRECLIPSE add
                        } finally {
                            if (okToSet) {
                                source.getErrorCollector().transformActive = false;
                            }
                        }
                        if (GroovyLogManager.manager.hasLoggers()) {
                            long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                            String sourceName = DefaultGroovyMethods.last(source.getName().split("/|\\\\"));
                            GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM, "Local transform " + snt.getClass().getName() + " applied to " + sourceName + "(" + getTargetName(node[1], classNode) + ") in " + millis + "ms");
                        }
                    } catch (NoClassDefFoundError ncdfe) {
                        String message = "Unable to run AST transform " + snt.getClass().getName() + ": missing class " + ncdfe.getMessage() +
                            ": Are you attempting to use groovy classes in an AST transform in the same project in which it is defined?";
                        source.addError(new SyntaxException(message, ncdfe, node[1].getLineNumber(), node[1].getColumnNumber()));
                    }
                    // GRECLIPSE end
                }
            }
        }
    }

    /**
     * Adds the annotation to the internal target list if a match is found.
     *
     * @param node the node to be processed
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (transforms.containsKey(annotation)) {
                targetNodes.add(new ASTNode[]{annotation, node});
            }
        }
    }

    public static void addPhaseOperations(final CompilationUnit compilationUnit) {
        final ASTTransformationsContext context = compilationUnit.getASTTransformationsContext();
        addGlobalTransforms(context);

        compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTTransformationCollectorCodeVisitor collector =
                    new ASTTransformationCollectorCodeVisitor(source, compilationUnit.getTransformLoader());
                collector.visitClass(classNode);
            }
        }, Phases.SEMANTIC_ANALYSIS);
        for (CompilePhase phase : CompilePhase.values()) {
            final ASTTransformationVisitor visitor = new ASTTransformationVisitor(phase, context);
            switch (phase) {
                case INITIALIZATION:
                case PARSING:
                case CONVERSION:
                    // with transform detection alone these phases are inaccessible, so don't add it
                    break;

                default:
                    compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
                        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                            visitor.source = source;
                            visitor.visitClass(classNode);
                        }
                    }, phase.getPhaseNumber());
                    break;

            }
        }
    }

    public static void addGlobalTransformsAfterGrab(ASTTransformationsContext context) {
        doAddGlobalTransforms(context, false);
    }

    public static void addGlobalTransforms(ASTTransformationsContext context) {
        doAddGlobalTransforms(context, true);
    }

    private static void doAddGlobalTransforms(ASTTransformationsContext context, boolean isFirstScan) {
        final CompilationUnit compilationUnit = context.getCompilationUnit();
        GroovyClassLoader transformLoader = compilationUnit.getTransformLoader();
        Map<String, URL> transformNames = new LinkedHashMap<String, URL>();
        try {
            Enumeration<URL> globalServices = transformLoader.getResources("META-INF/services/org.codehaus.groovy.transform.ASTTransformation");
            while (globalServices.hasMoreElements()) {
                URL service = globalServices.nextElement();
                BufferedReader svcIn = null;
                try {
                    svcIn = new BufferedReader(new InputStreamReader(service.openStream(), "UTF-8"));
                    String className;
                    try {
                        className = svcIn.readLine();
                    } catch (IOException ioe) {
                        compilationUnit.getErrorCollector().addError(new SimpleMessage(
                                "IOException reading the service definition at "
                                        + service.toExternalForm() + " because of exception " + ioe.toString(), null));
                        continue;
                    }
                    Set<String> disabledGlobalTransforms = compilationUnit.getConfiguration().getDisabledGlobalASTTransformations();
                    if (disabledGlobalTransforms == null) disabledGlobalTransforms = Collections.emptySet();
                    while (className != null) {
                        if (!className.startsWith("#") && className.length() > 0) {
                            if (!disabledGlobalTransforms.contains(className)) {
                                if (transformNames.containsKey(className)) {
                                    try {
                                        if (!service.toURI().equals(transformNames.get(className).toURI())) {
                                            compilationUnit.getErrorCollector().addWarning(
                                                    WarningMessage.POSSIBLE_ERRORS,
                                                    "The global transform for class " + className + " is defined in both "
                                                            + transformNames.get(className).toExternalForm()
                                                            + " and "
                                                            + service.toExternalForm()
                                                            + " - the former definition will be used and the latter ignored.",
                                                    null,
                                                    null);
                                        }
                                    } catch (URISyntaxException e) {
                                        compilationUnit.getErrorCollector().addWarning(
                                                WarningMessage.POSSIBLE_ERRORS,
                                                "Failed to parse URL as URI because of exception " + e.toString(),
                                                null,
                                                null);
                                    }
                                } else {
                                    // GRECLIPSE add
                                    if (compilationUnit.allowTransforms)
                                    // GRECLIPSE end
                                    transformNames.put(className, service);
                                }
                            }
                        }
                        try {
                            className = svcIn.readLine();
                        } catch (IOException ioe) {
                            compilationUnit.getErrorCollector().addError(new SimpleMessage(
                                    "IOException reading the service definition at "
                                            + service.toExternalForm() + " because of exception " + ioe.toString(), null));
                            continue;
                        }
                    }
                } finally {
                    if (svcIn != null)
                        svcIn.close();
                }
            }
        } catch (IOException e) {
            //FIXME the warning message will NPE with what I have :(
            compilationUnit.getErrorCollector().addError(new SimpleMessage(
                "IO Exception attempting to load global transforms:" + e.getMessage(),
                null));
        }
        try {
            Class.forName("java.lang.annotation.Annotation"); // test for 1.5 JVM
        } catch (Exception e) {
            // we failed, notify the user
            StringBuilder sb = new StringBuilder();
            sb.append("Global ASTTransformations are not enabled in retro builds of groovy.\n");
            sb.append("The following transformations will be ignored:");
            for (Map.Entry<String, URL> entry : transformNames.entrySet()) {
                sb.append('\t');
                sb.append(entry.getKey());
                sb.append('\n');
            }
            compilationUnit.getErrorCollector().addWarning(new WarningMessage(
                WarningMessage.POSSIBLE_ERRORS, sb.toString(), null, null));
            return;
        }

        // record the transforms found in the first scan, so that in the 2nd scan, phase operations
        // can be added for only for new transforms that have come in
        if (isFirstScan) {
            for (Map.Entry<String, URL> entry : transformNames.entrySet()) {
                context.getGlobalTransformNames().add(entry.getKey());
            }
            addPhaseOperationsForGlobalTransforms(context.getCompilationUnit(), transformNames, isFirstScan);
        } else {
            Iterator<Map.Entry<String, URL>> it = transformNames.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, URL> entry = it.next();
                if (!context.getGlobalTransformNames().add(entry.getKey())) {
                    // phase operations for this transform class have already been added before, so remove from current scan cycle
                    it.remove();
                }
            }
            addPhaseOperationsForGlobalTransforms(context.getCompilationUnit(), transformNames, isFirstScan);
        }
    }

    private static void addPhaseOperationsForGlobalTransforms(CompilationUnit compilationUnit,
            Map<String, URL> transformNames, boolean isFirstScan) {
        GroovyClassLoader transformLoader = compilationUnit.getTransformLoader();
        for (Map.Entry<String, URL> entry : transformNames.entrySet()) {
            try {
                Class gTransClass = transformLoader.loadClass(entry.getKey(), false, true, false);
                GroovyASTTransformation transformAnnotation = (GroovyASTTransformation) gTransClass.getAnnotation(GroovyASTTransformation.class);
                if (transformAnnotation == null) {
                    compilationUnit.getErrorCollector().addWarning(new WarningMessage(
                        WarningMessage.POSSIBLE_ERRORS,
                        "Transform Class " + entry.getKey() + " is specified as a global transform in " + entry.getValue().toExternalForm()
                        + " but it is not annotated by " + GroovyASTTransformation.class.getName()
                        + " the global transform associated with it may fail and cause the compilation to fail.",
                        null,
                        null));
                    continue;
                }
                if (ASTTransformation.class.isAssignableFrom(gTransClass)) {
                    // GRECLIPSE add
                    try {
                    // GRECLIPSE end
                    final ASTTransformation instance = (ASTTransformation)gTransClass.newInstance();
                    if (instance instanceof CompilationUnitAware) {
                        ((CompilationUnitAware)instance).setCompilationUnit(compilationUnit);
                    }
                    CompilationUnit.SourceUnitOperation suOp = new CompilationUnit.SourceUnitOperation() {
                        // GRECLIPSE add
                        private boolean isBuggered;
                        // GRECLIPSE end
                        public void call(SourceUnit source) throws CompilationFailedException {
                            // GRECLIPSE add
                            if (isBuggered) return;
                            try {
                                long t0 = System.nanoTime();
                                boolean okToSet = (source != null && source.getErrorCollector() != null);
                                try {
                                    if (okToSet) {
                                        source.getErrorCollector().transformActive = true;
                                    }
                            // GRECLIPSE end
                            instance.visit(new ASTNode[] {source.getAST()}, source);
                            // GRECLIPSE add
                                } finally {
                                    if (okToSet) {
                                        source.getErrorCollector().transformActive = false;
                                    }
                                }
                                if (GroovyLogManager.manager.hasLoggers()) {
                                    long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                                    String sourceName = DefaultGroovyMethods.last(source.getName().split("/|\\\\"));
                                    GroovyLogManager.manager.log(TraceCategory.AST_TRANSFORM, "Global transform " + instance.getClass().getName() + " applied to " + sourceName + " in " + millis + "ms");
                                }
                            } catch (NoClassDefFoundError ncdfe) {
                                // Suggests that the transform is written in Java but has dependencies on Groovy source
                                // within the same project - as this has yet to be compiled, we can't find the class.
                                source.addException(new GroovyException("Transform " + instance.getClass().getName() + " cannot be run", ncdfe));
                                // disable this visitor because it is BUGGERED
                                isBuggered = true;
                            }
                            // GRECLIPSE end
                        }
                    };
                    if (isFirstScan) {
                        compilationUnit.addPhaseOperation(suOp, transformAnnotation.phase().getPhaseNumber());
                    } else {
                        compilationUnit.addNewPhaseOperation(suOp, transformAnnotation.phase().getPhaseNumber());
                    }
                    // GRECLIPSE add
                    } catch (Throwable t) {
                        // unexpected problem with the transformation. Could be:
                        // - problem instantiating the transformation class
                        compilationUnit.getErrorCollector().addError(new SimpleMessage(
                            "Unexpected problem with AST transform: " + t.getMessage(), null));
                    }
                    // GRECLIPSE end
                } else {
                    compilationUnit.getErrorCollector().addError(new SimpleMessage(
                        "Transform Class " + entry.getKey() + " specified at "
                        + entry.getValue().toExternalForm() + " is not an ASTTransformation.", null));
                }
            } catch (Exception e) {
                compilationUnit.getErrorCollector().addError(new SimpleMessage(
                    "Could not instantiate global transform class " + entry.getKey() + " specified at "
                    + entry.getValue().toExternalForm() + "  because of exception " + e.toString(), null));
            }
        }
    }
}
