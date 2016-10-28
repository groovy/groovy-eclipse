/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.jdt.groovy.internal.compiler.ast;

import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

/**
 * Wraps a JDT FieldBinding, representing it to groovy as a FieldNode. Translates annotations only when required.
 *
 * @author Andy Clement
 */
public class JDTFieldNode extends FieldNode implements JDTNode {

    private FieldBinding fieldBinding;
    private JDTResolver resolver;
    private int bits = 0;

    public JDTFieldNode(FieldBinding fieldBinding, JDTResolver resolver, String name, int modifiers, ClassNode type,
            JDTClassNode declaringType, Expression initializerExpression) {
        super(name, modifiers, type, declaringType, initializerExpression);
        this.resolver = resolver;
        this.fieldBinding = fieldBinding;
    }

    @Override
    public void addAnnotation(AnnotationNode value) {
        throw new IllegalStateException("JDTFieldNode is immutable");
    }

    @Override
    public void addAnnotations(List<AnnotationNode> annotations) {
        throw new IllegalStateException("JDTFieldNode is immutable");
    }

    @Override
    public List<AnnotationNode> getAnnotations() {
        ensureAnnotationsInitialized();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        ensureAnnotationsInitialized();
        return super.getAnnotations(type);
    }

    private void ensureAnnotationsInitialized() {
        if ((bits & ANNOTATIONS_INITIALIZED) == 0) {
            // If the backing declaring entity for the member is not a SourceTypeBinding then the
            // annotations will have already been discarded/lost
            AnnotationBinding[] annotationBindings = fieldBinding.getAnnotations();
            for (AnnotationBinding annotationBinding : annotationBindings) {
                super.addAnnotation(new JDTAnnotationNode(annotationBinding, this.resolver));
            }
            bits |= ANNOTATIONS_INITIALIZED;
        }
    }

    public FieldBinding getFieldBinding() {
        return fieldBinding;
    }

    public JDTResolver getResolver() {
        return resolver;
    }

    public Binding getJdtBinding() {
        return fieldBinding;
    }

    public boolean isDeprecated() {
        return fieldBinding.isDeprecated();
    }
}
