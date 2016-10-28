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
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Common interface for Groovy ASTNodes backed by a JDT binding
 *
 * @author Andrew Eisenberg
 * @created Dec 13, 2010
 */
public interface JDTNode {
    int ANNOTATIONS_INITIALIZED = 0x0001;
    int PROPERTIES_INITIALIZED = 0x0002;

    JDTResolver getResolver();

    List<AnnotationNode> getAnnotations();

    List<AnnotationNode> getAnnotations(ClassNode type);

    Binding getJdtBinding();

    boolean isDeprecated();
}
