/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.inference;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.groovy.search.AbstractSimplifiedTypeLookup;
import org.eclipse.jdt.groovy.search.ITypeLookup;
import org.eclipse.jdt.groovy.search.VariableScope;

/**
 * Type lookup for provided AST transforms including:
 * <ul>
 * <li>Singleton
 * <li>Delegate
 * </ul>
 *
 * FIXADE This class will no longer be needed when DSL support becomes
 * available.
 *
 * @author Andrew Eisenberg
 * @created Apr 12, 2010
 */
public class StandardASTTransformInference extends AbstractSimplifiedTypeLookup
        implements ITypeLookup {

    private Map<String, BuiltInASTTransform[]> annotationCache;

    public void initialize(GroovyCompilationUnit unit,
            VariableScope topLevelScope) {
        annotationCache = new HashMap<String, BuiltInASTTransform[]>();
    }

    @Override
    protected TypeAndDeclaration lookupTypeAndDeclaration(
            ClassNode declaringType, String name, VariableScope scope) {
        BuiltInASTTransform[] transforms = annotationCache.get(declaringType.getName());
        if (transforms == null) {
            transforms = BuiltInASTTransform.createAll(declaringType);
            annotationCache.put(declaringType.getName(), transforms);
        }
        return getType(transforms, name);
    }

    /**
     * @param transforms
     * @param name
     * @return
     */
    private TypeAndDeclaration getType(BuiltInASTTransform[] transforms,
            String name) {
        for (BuiltInASTTransform transform : transforms) {
            TypeAndDeclaration tAndD = transform.symbolToDeclaration(name);
            if (tAndD != null) {
                return tAndD;
            }
        }
        return null;
    }

}
