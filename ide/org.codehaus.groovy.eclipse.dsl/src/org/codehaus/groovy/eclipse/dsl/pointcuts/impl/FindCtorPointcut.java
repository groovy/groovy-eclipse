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

import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

/**
 * Matches if the input has a constructor with the supplied characteristics --
 * either a signature (comma-separated simple type names) or another pointcut
 * like {@code hasAnnotation}.
 */
public class FindCtorPointcut extends FindASTPointcut<ConstructorNode> {

    public FindCtorPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName, ConstructorNode.class, ClassNode::getDeclaredConstructors, ctor -> {
            List<ClassNode> paramTypes = GroovyUtils.getParameterTypes(ctor.getParameters());
            return paramTypes.stream().map(FindCtorPointcut::getSimpleTypeName)
                .map(name -> name.substring(name.lastIndexOf('$') + 1))
                .collect(Collectors.joining(","));
        });
    }

    private static String getSimpleTypeName(ClassNode type) {
        int dims = 0;
        while (type.isArray()) {
            dims += 1;
            type = type.getComponentType();
        }

        String name = type.getNameWithoutPackage();
        while (dims > 0) {
            name += "[]";
            dims -= 1;
        }
        return name;
    }
}
