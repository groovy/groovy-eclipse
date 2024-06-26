/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
 * Common interface for Groovy {@code ASTNode}s backed by a JDT binding.
 */
public interface JDTNode {

    int ANNOTATIONS_INITIALIZED = 0x0001;
    int INNER_TYPES_INITIALIZED = 0x0002;
    int PROPERTIES_INITIALIZED  = 0x0004;

    List<AnnotationNode> getAnnotations(ClassNode type);

    List<AnnotationNode> getAnnotations();

    JDTResolver getResolver();

    Binding getJdtBinding();

    boolean isDeprecated();
}
