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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;

public abstract class FindASTPointcut<T extends ASTNode> extends FilteringPointcut<T> {

    private final Function<ClassNode, ? extends Collection<T>> exploder;
    private final Function<T, String> stringify;

    public FindASTPointcut(IStorage containerIdentifier, String pointcutName, Class<T> filterBy,
            Function<ClassNode, ? extends Collection<T>> exploder, Function<T, String> stringify) {
        super(containerIdentifier, pointcutName, filterBy);
        this.exploder = Objects.requireNonNull(exploder);
        this.stringify = Objects.requireNonNull(stringify);
    }

    @Override
    protected final Collection<T> explodeObject(Object object) {
        Collection<T> result = new ArrayList<>();
        explodeObject(object, result);
        return result;
    }

    protected void explodeObject(Object object, Collection<T> result) {
        if (filterBy.isInstance(object)) {
            @SuppressWarnings("unchecked")
            T node = (T) object;
            result.add(node);
        } else if (object instanceof ClassNode) {
            result.addAll(exploder.apply(
                GroovyUtils.getWrapperTypeIfPrimitive((ClassNode) object)));
        } else if (object instanceof Collection) {
            ((Collection<?>) object).forEach(item -> explodeObject(item, result));
        }
    }

    @Override
    protected T filterObject(T result, GroovyDSLDContext context, String firstArgAsString) {
        if (firstArgAsString == null || firstArgAsString.equals(stringify.apply(result))) {
            return result;
        }
        return null;
    }
}
