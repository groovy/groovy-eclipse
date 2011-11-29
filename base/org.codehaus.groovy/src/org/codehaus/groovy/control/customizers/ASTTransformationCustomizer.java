/*
 * Copyright 2003-2011 the original author or authors.
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

package org.codehaus.groovy.control.customizers;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;

/**
 * This is a 'stub' class inserted into Groovy 1.7 bundle so that it is 'compilation and classload
 * compatible' with Groovy 1.8 bundle, with respect to external code referencing ASTTransformationCustomizer.
 * <p>
 * This is intended to *not* actually be used in Groovy 1.7 so any attempt to actually instantiate
 * an instance of this class or call methods in it will cause a IllegalStateException. 
 * <p>
 * This class was created by copying ASTTransformationCustomizer from Groovy 1.8 and replacing all public
 * API with a stub method that throws IllegalStateException.
 * 
 * @author Kris De Volder
 */
@SuppressWarnings("rawtypes")
public class ASTTransformationCustomizer extends CompilationCustomizer {

    public ASTTransformationCustomizer(final Class<? extends Annotation> transformationAnnotation) {
        super(null);
        throw new IllegalStateException("ASTTransformationCustomizer not supported in Groovy 1.7");
    }

    public ASTTransformationCustomizer(final ASTTransformation transformation) {
        super(null);
        throw new IllegalStateException("ASTTransformationCustomizer not supported in Groovy 1.7");
    }

	ASTTransformationCustomizer(final Map annotationParams, final Class<? extends Annotation> transformationAnnotation) {
        super(null);
        throw new IllegalStateException("ASTTransformationCustomizer not supported in Groovy 1.7");
    }

    ASTTransformationCustomizer(final Map annotationParams, final ASTTransformation transformation) {
        super(null);
        throw new IllegalStateException("ASTTransformationCustomizer not supported in Groovy 1.7");
    }

    public void setAnnotationParameters(Map<String,Object> params) {
        throw new IllegalStateException("ASTTransformationCustomizer not supported in Groovy 1.7");
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        throw new IllegalStateException("ASTTransformationCustomizer not supported in Groovy 1.7");
    }
}
