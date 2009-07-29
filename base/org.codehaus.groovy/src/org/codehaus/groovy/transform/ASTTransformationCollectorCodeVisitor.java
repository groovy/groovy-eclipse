/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.transform;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;

import groovy.lang.GroovyClassLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This visitor walks the AST tree and collects references to Annotations that
 * are annotated themselves by {@link GroovyASTTransformation}. Each such
 * annotation is added.
 * <p/>
 * This visitor is only intended to be executed once, during the
 * SEMANTIC_ANALYSIS phase of compilation.
 *
 * @author Danno Ferrin (shemnon)
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

    
    private String[] getTransformClasses(ClassNode cn) {
    	if (!cn.hasClass()) { 
        	List<AnnotationNode> annotations = cn.getAnnotations();
        	AnnotationNode transformAnnotation = null;
        	for (AnnotationNode anno: annotations) {
        		if (anno.getClassNode().getName().equals(GroovyASTTransformationClass.class.getName())){
                    transformAnnotation = anno; 
                    break; 
                }
            }  
        	if (transformAnnotation!=null) {
        		// will work so long as signature for the member 'value' is String[]
        		ListExpression expression = (ListExpression)transformAnnotation.getMember("value");
        		List<Expression> expressions = expression.getExpressions();
        		String[] values = new String[expressions.size()];
        		int e=0;
        		for (Expression expr: expressions) {
        			values[e++] = ((ConstantExpression)expr).getText();
        		}
        		return values;
        	}
        	return null;
    	} else {
    		// FIXASC (M2) check haven't broken transforms for 'vanilla' (outside of eclipse) execution of groovyc
	        Annotation transformClassAnnotation = getTransformClassAnnotation(cn);
	        if (transformClassAnnotation == null) {
	        	return null;
	        }
	        return getTransformClasses(transformClassAnnotation);
    	}
    }
    /**
     * If the annotation is annotated with {@link GroovyASTTransformation}
     * the annotation is added to <code>stageVisitors</code> at the appropriate processor visitor.
     *
     * @param node the node to process
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
        	String[] transformClasses = getTransformClasses(annotation.getClassNode());
//            Annotation transformClassAnnotation = getTransformClassAnnotation(annotation.getClassNode());
//            if (transformClassAnnotation == null) {
//                // skip if there is no such annotation
//                continue;
//            }
        	if (transformClasses ==null) {
        		continue;
        	}
            for (String transformClass : transformClasses) { //getTransformClasses(transformClassAnnotation)) {
                try {
                    Class klass = transformLoader.loadClass(transformClass, false, true, false);
                    if (ASTTransformation.class.isAssignableFrom(klass)) {
                        classNode.addTransform(klass, annotation);
                    } else {
                        source.getErrorCollector().addError(
                                new SimpleMessage(
                                        "Not an ASTTransformation: " + transformClass
                                        + " declared by " + annotation.getClassNode().getName(),
                                        source));
                    }
                } catch (ClassNotFoundException e) {
                    source.getErrorCollector().addErrorAndContinue(
                            new SimpleMessage(
                                    "Could not find class for Transformation Processor " + transformClass
                                    + " declared by " + annotation.getClassNode().getName(),
                                    source));
                }
            }
        }
    }

    private static Annotation getTransformClassAnnotation(ClassNode annotatedType) {
        if (!annotatedType.isResolved()) return null;
        // FIXASC (M3:ast) recognizing annotations - care about this code now? do we even come in here
        if (!annotatedType.hasClass()) { 
        	List<AnnotationNode> annotations = annotatedType.getAnnotations();
        	return null;
        }
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

    private String[] getTransformClasses(Annotation transformClassAnnotation) {
        try {
            Method valueMethod = transformClassAnnotation.getClass().getMethod("value");
            return (String[]) valueMethod.invoke(transformClassAnnotation);
        } catch (Exception e) {
            source.addException(e);
            return new String[0];
        }
    }
}
