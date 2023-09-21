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
package org.eclipse.jdt.groovy.search;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.util.Util;

public class TypeInferencingVisitorFactory {

    /**
     * Create a new {@link TypeInferencingVisitorWithRequestor}
     *
     * @param possibleMatch corresponds to the compilation unit to be inferred
     * @return a fully configured {@link TypeInferencingVisitorWithRequestor}
     */
    public TypeInferencingVisitorWithRequestor createVisitor(PossibleMatch possibleMatch) {
        if (possibleMatch.openable instanceof GroovyCompilationUnit) {
            return createVisitor((GroovyCompilationUnit) possibleMatch.openable);
        } else if (possibleMatch.openable instanceof ClassFile) {
            return createVisitor(new GroovyClassFileWorkingCopy((ClassFile) possibleMatch.openable, null));
        } else {
            throw new IllegalStateException("Attempted to do a groovy visit on a non-groovy file: " + String.valueOf(possibleMatch.getFileName()));
        }
    }

    public TypeInferencingVisitorWithRequestor createVisitor(GroovyCompilationUnit compilationUnit) {
        List<ITypeLookup> lookups = new ArrayList<>();
        IProject project = compilationUnit.getJavaProject().getProject();
        try {
            lookups.addAll(TypeLookupRegistry.getRegistry().getLookupsFor(project));
        } catch (CoreException e) {
            Util.log(e, "Failed to retrieve ITypeLookup instances for project " + project.getName());
        }
        lookups.add(new CategoryTypeLookup());
        lookups.add(new SimpleTypeLookup()); // must be last!

        return new TypeInferencingVisitorWithRequestor(compilationUnit, lookups.toArray(new ITypeLookup[lookups.size()]));
    }
}
