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
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.core.resources.IStorage;

/**
 * Matches if the input has a method with the supplied characteristics -- either
 * a name or another pointcut like {@code hasAnnotation}.
 */
public class FindMethodPointcut extends FindASTPointcut<MethodNode> {

    public FindMethodPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, MethodNode.class, ClassNode::getMethods, MethodNode::getName);
    }
}
