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
import groovy.transform.AnnotationCollector;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.trait.TraitASTTransformation;
import org.codehaus.groovy.transform.trait.Traits;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This visitor walks the AST tree and collects references to Annotations that
 * are annotated themselves by {@link GroovyASTTransformation}. Each such
 * annotation is added.
 * <p>
 * This visitor is only intended to be executed once, during the
 * SEMANTIC_ANALYSIS phase of compilation.
 */
public class ASTTransformationCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private ClassNode classNode;
    private GroovyClassLoader transformLoader;

    public ASTTransformationCollectorCodeVisitor(SourceUnit source, GroovyClassLoader transformLoader) {
        this.source = source;
        this.transformLoader = transformLoader;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitClass(ClassNode klassNode) {
        ClassNode oldClass = classNode;
        classNode = klassNode;
        super.visitClass(classNode);
        classNode = oldClass;
    }

    /**
     * If the annotation is annotated with {@link GroovyASTTransformation}
     * the annotation is added to <code>stageVisitors</code> at the appropriate processor visitor.
     *
     * @param node the node to process
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);

        List<AnnotationNode> collected = new ArrayList<AnnotationNode>();
        for (Iterator<AnnotationNode> it = node.getAnnotations().iterator(); it.hasNext();) {
            AnnotationNode annotation = it.next();
            if (addCollectedAnnotations(collected, annotation, node)) it.remove();
        }
        node.getAnnotations().addAll(collected);
        
        for (AnnotationNode annotation : node.getAnnotations()) {
            /* GRECLIPSE edit
            Annotation transformClassAnnotation = getTransformClassAnnotation(annotation.getClassNode());
            if (transformClassAnnotation == null) {
                // skip if there is no such annotation
                continue;
            }
            addTransformsToClassNode(annotation, transformClassAnnotation);
            */
            String[] transformClassNames = getTransformClassNames(annotation.getClassNode());
            Class[] transformClasses = getTransformClasses(annotation.getClassNode());
            if (transformClassNames == null && transformClasses == null) {
                continue;
            }
            if (transformClassNames == null) {
                transformClassNames = NONE;
            }
            if (transformClasses == null) {
                transformClasses = NO_CLASSES;
            }
            addTransformsToClassNode(annotation, transformClassNames, transformClasses);
            // GRECLIPSE end
        }
    }
    
    private void assertStringConstant(Expression exp) {
        if (exp==null) return;
        if (!(exp instanceof ConstantExpression)) {
            source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(
                    "Expected a String constant.", exp.getLineNumber(), exp.getColumnNumber()), 
                    source));
        }
        ConstantExpression ce = (ConstantExpression) exp;
        if (!(ce.getValue() instanceof String)) {
            source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(
                    "Expected a String constant.", exp.getLineNumber(), exp.getColumnNumber()), 
                    source));
        }
    }
    
    private boolean addCollectedAnnotations(List<AnnotationNode> collected, AnnotationNode aliasNode, AnnotatedNode origin) {
        ClassNode classNode = aliasNode.getClassNode();
        boolean ret = false;
        for (AnnotationNode annotation : classNode.getAnnotations()) {
            if (annotation.getClassNode().getName().equals(AnnotationCollector.class.getName())) {
                Expression processorExp = annotation.getMember("processor");
                AnnotationCollectorTransform act = null;
                assertStringConstant(processorExp);
                if (processorExp!=null) {
                    String className = (String) ((ConstantExpression) processorExp).getValue();
                    Class klass = loadTransformClass(className, aliasNode);
                    if (klass!=null) {
                        try {
                            act = (AnnotationCollectorTransform) klass.newInstance();
                        } catch (InstantiationException e) {
                            source.getErrorCollector().addErrorAndContinue(new ExceptionMessage(e, true, source));
                        } catch (IllegalAccessException e) {
                            source.getErrorCollector().addErrorAndContinue(new ExceptionMessage(e, true, source));
                        }
                    }
                } else {
                    act = new AnnotationCollectorTransform();
                }
                if (act!=null) {
                    // GRECLIPSE edit -- retain reference to aliasNode
                    //collected.addAll(act.visit(annotation, aliasNode, origin, source));
                    List<AnnotationNode> visitResult = act.visit(annotation, aliasNode, origin, source);
                    for (AnnotationNode annotationNode : visitResult) {
                        annotationNode.getNodeMetaData("AnnotationCollector", x -> new ArrayList<>(1)).add(aliasNode);
                    }
                    aliasNode.setNodeMetaData("AnnotationCollectorTransform", visitResult);
                    collected.addAll(visitResult);
                    // GRECLIPSE end
                }
                ret = true;
            }
        }
        return ret;
    }

    /* GRECLIPSE edit
    private void addTransformsToClassNode(AnnotationNode annotation, Annotation transformClassAnnotation) {
        List<String> transformClassNames = getTransformClassNames(annotation, transformClassAnnotation);

        if(transformClassNames.isEmpty()) {
            source.getErrorCollector().addError(new SimpleMessage("@GroovyASTTransformationClass in " +
                    annotation.getClassNode().getName() + " does not specify any transform class names/classes", source));
        }

        for (String transformClass : transformClassNames) {
            Class klass = loadTransformClass(transformClass, annotation); 
            if (klass!=null) {
                verifyAndAddTransform(annotation, klass);
            }
        }
    }
    */

    private void addTransformsToClassNode(AnnotationNode annotation, String[] transformClassNames, Class[] transformClasses) {
        if (transformClassNames.length == 0 && transformClasses.length == 0) {
            source.getErrorCollector().addError(new SimpleMessage(
                    "@GroovyASTTransformationClass in " + annotation.getClassNode().getName() +
                    " does not specify any transform class names/classes", source));
        } else if (transformClassNames.length > 0 && transformClasses.length > 0) {
            source.getErrorCollector().addError(new SimpleMessage(
                    "@GroovyASTTransformationClass in " + annotation.getClassNode().getName() +
                    " should specify transforms by class names or by classes, not by both", source));
        }

        for (String transformClass : transformClassNames) {
            try {
                Class klass = transformLoader.loadClass(transformClass, false, true, false);
                verifyAndAddTransform(annotation, klass);
            } catch (ClassNotFoundException e) {
                source.getErrorCollector().addErrorAndContinue(new SimpleMessage(
                        "Could not find class for Transformation Processor "+ transformClass +
                        " declared by " + annotation.getClassNode().getName(), source));
            }
        }
        for (Class klass : transformClasses) {
            verifyAndAddTransform(annotation, klass);
        }
    }

    private String[] getTransformClassNames(Annotation transformClassAnnotation) {
        try {
            Method valueMethod = transformClassAnnotation.getClass().getMethod("value");
            return (String[]) valueMethod.invoke(transformClassAnnotation);
        } catch (Exception e) {
            source.addException(e);
            return NONE;
        }
    }

    private Class[] getTransformClasses(Annotation transformClassAnnotation) {
        try {
            Method classesMethod = transformClassAnnotation.getClass().getMethod("classes");
            return (Class[]) classesMethod.invoke(transformClassAnnotation);
        } catch (Exception e) {
            source.addException(e);
            return NO_CLASSES;
        }
    }
    // GRECLIPSE end

    private Class loadTransformClass(String transformClass, AnnotationNode annotation) {
        try {
            return transformLoader.loadClass(transformClass, false, true, false);
        } catch (ClassNotFoundException e) {
            source.getErrorCollector().addErrorAndContinue(
                    new SimpleMessage(
                            "Could not find class for Transformation Processor " + transformClass
                            + " declared by " + annotation.getClassNode().getName(),
                            source));
        }
        return null;
    }

    private void verifyAndAddTransform(AnnotationNode annotation, Class klass) {
        verifyClass(annotation, klass);
        verifyCompilePhase(annotation, klass);
        addTransform(annotation, klass);
    }

    private void verifyCompilePhase(AnnotationNode annotation, Class<?> klass) {
        GroovyASTTransformation transformationClass = klass.getAnnotation(GroovyASTTransformation.class);
        if (transformationClass != null)  {
            CompilePhase specifiedCompilePhase = transformationClass.phase();
            if (specifiedCompilePhase.getPhaseNumber() < CompilePhase.SEMANTIC_ANALYSIS.getPhaseNumber())  {
                source.getErrorCollector().addError(
                        new SimpleMessage(
                                annotation.getClassNode().getName() + " is defined to be run in compile phase " + specifiedCompilePhase + ". Local AST transformations must run in " + CompilePhase.SEMANTIC_ANALYSIS + " or later!",
                                source));
            }

        } else {
            source.getErrorCollector().addError(
                new SimpleMessage("AST transformation implementation classes must be annotated with " + GroovyASTTransformation.class.getName() + ". " + klass.getName() + " lacks this annotation.", source));
        }
    }

    private void verifyClass(AnnotationNode annotation, Class klass) {
        if (!ASTTransformation.class.isAssignableFrom(klass)) {
            source.getErrorCollector().addError(new SimpleMessage("Not an ASTTransformation: " +
                    klass.getName() + " declared by " + annotation.getClassNode().getName(), source));
        }
    }

    private void addTransform(AnnotationNode annotation, Class klass)  {
        boolean apply = !Traits.isTrait(classNode) || klass == TraitASTTransformation.class;
        if (apply) {
            classNode.addTransform(klass, annotation);
        }
    }

    private static Annotation getTransformClassAnnotation(ClassNode annotatedType) {
        if (!annotatedType.isResolved()) return null;

        for (Annotation ann : annotatedType.getTypeClass().getAnnotations()) {
            // because compiler clients are free to choose any GroovyClassLoader for
            // resolving ClassNodeS such as annotatedType, we have to compare by name,
            // and cannot cast the return value to GroovyASTTransformationClass
            if (ann.annotationType().getName().equals(GroovyASTTransformationClass.class.getName())){
                return ann;
            }
        }

        return null;
    }

    /* GRECLIPSE edit
    private List<String> getTransformClassNames(AnnotationNode annotation, Annotation transformClassAnnotation) {
        List<String> result = new ArrayList<String>();

        try {
            Method valueMethod = transformClassAnnotation.getClass().getMethod("value");
            String[] names = (String[]) valueMethod.invoke(transformClassAnnotation);
            result.addAll(Arrays.asList(names));

            Method classesMethod = transformClassAnnotation.getClass().getMethod("classes");
            Class[] classes = (Class[]) classesMethod.invoke(transformClassAnnotation);
            for (Class klass : classes) {
                result.add(klass.getName());
            }

            if(names.length > 0 && classes.length > 0) {
                source.getErrorCollector().addError(new SimpleMessage("@GroovyASTTransformationClass in " +
                        annotation.getClassNode().getName() +
                        " should specify transforms only by class names or by classes and not by both", source));
            }
        } catch (Exception e) {
            source.addException(e);
        }

        return result;
    }
    */

    // GRECLIPSE add
    private static final String[] NONE = new String[0];
    private static final Class[] NO_CLASSES = new Class[0];

    /**
     * For the supplied classnode, this method will check if there is an annotation on it of kind 'GroovyASTTransformationClass'.  If there is then
     * the 'value' member of that annotation will be retrieved and the value considered to be the class name of a transformation.
     *
     * @return null if no annotation found, otherwise a String[] of classnames - this will be size 0 if no value was specified
     */
    private String[] getTransformClassNames(ClassNode cn) {
        if (!cn.hasClass()) {
            List<AnnotationNode> annotations = cn.getAnnotations();
            AnnotationNode transformAnnotation = null;
            for (AnnotationNode anno: annotations) {
                if (anno.getClassNode().getName().equals(GroovyASTTransformationClass.class.getName())) {
                    transformAnnotation = anno;
                    break;
                }
            }
            if (transformAnnotation != null) {
                // will work so long as signature for the member 'value' is String[]
                Expression expr2 = transformAnnotation.getMember("value");
                String[] values = null;
                if (expr2 == null) {
                    return NONE;
                }
                if (expr2 instanceof ListExpression) {
                    ListExpression expression = (ListExpression) expr2;
                    List<Expression> expressions = expression.getExpressions();
                    values = new String[expressions.size()];
                    int e = 0;
                    for (Expression expr: expressions) {
                        values[e++] = ((ConstantExpression) expr).getText();
                    }
                } else if (expr2 instanceof ConstantExpression) {
                    values = new String[1];
                    values[0] = ((ConstantExpression) expr2).getText();
                } else {
                    throw new IllegalStateException("NYI: eclipse doesn't understand this kind of expression in an Ast transform definition: " + expr2 + " (class=" + expr2.getClass().getName() + ")");
                }
                return values;
            }
            return null;
        } else {
            // FIXASC check haven't broken transforms for 'vanilla' (outside of eclipse) execution of groovyc
            Annotation transformClassAnnotation = getTransformClassAnnotation(cn);
            if (transformClassAnnotation == null) {
                return null;
            }
            return getTransformClassNames(transformClassAnnotation);
        }
    }

    private Class[] getTransformClasses(ClassNode classNode) {
        if (!classNode.hasClass()) {
            List<AnnotationNode> annotations = classNode.getAnnotations();
            AnnotationNode transformAnnotation = null;
            for (AnnotationNode anno: annotations) {
                if (anno.getClassNode().getName().equals(GroovyASTTransformationClass.class.getName())) {
                    transformAnnotation = anno;
                    break;
                }
            }
            if (transformAnnotation != null) {
                Expression expr = transformAnnotation.getMember("classes");
                if (expr == null) {
                    return NO_CLASSES;
                }
                Class<?>[] values = NO_CLASSES;
                // Will need to extract the classnames
                if (expr instanceof ListExpression) {
                    List<Class<?>> loadedClasses = new ArrayList<Class<?>>();
                    ListExpression expression = (ListExpression) expr;
                    List<Expression> expressions = expression.getExpressions();
                    for (Expression oneExpr: expressions) {
                        String classname = ((ClassExpression) oneExpr).getType().getName();
                        try {
                            Class<?> clazz = Class.forName(classname, false, transformLoader);
                            loadedClasses.add(clazz);
                        } catch (ClassNotFoundException cnfe) {
                            source.getErrorCollector().addError(new SimpleMessage("Ast transform processing, cannot find " + classname, source));
                        }
                    }
                    if (!loadedClasses.isEmpty()) {
                        values = loadedClasses.toArray(new Class<?>[loadedClasses.size()]);
                    }
                    return values;
                }
                throw new RuntimeException("nyi implemented in eclipse: need to support: " + expr + " (class=" + expr.getClass() + ")");
            }
            return null;
        } else {
            Annotation transformClassAnnotation = getTransformClassAnnotation(classNode);
            if (transformClassAnnotation == null) {
                return null;
            }
            return getTransformClasses(transformClassAnnotation);
        }
    }
    // GRECLIPSE end
}
