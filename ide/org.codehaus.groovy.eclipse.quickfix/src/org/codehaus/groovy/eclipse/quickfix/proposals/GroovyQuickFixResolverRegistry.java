/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This instance registry is associated with a particular problem that requires
 * a quick fix. <br>
 * For a given problem, this registry will look up potential resolvers that can
 * provide a quick fix solution for the problem. There may be more than one
 * resolver that can handle the problem </br>
 */
public class GroovyQuickFixResolverRegistry {

    private QuickFixProblemContext problem;
    private Map<ProblemType, List<IQuickFixResolver>> registry;

    /**
     * @param problem
     *            the quick fix problem that should be used to look up possible
     *            resolvers that can generated a solution. Must not be null
     */
    public GroovyQuickFixResolverRegistry(QuickFixProblemContext problem) {
        this.problem = problem;
    }

    /**
     * @return the quick fix problem used to look up potential resolvers that
     *         can provide a quick fix for that problem
     */
    protected QuickFixProblemContext getQuickFixProblem() {
        return problem;
    }

    /**
     * @return get the list of quick fix resolvers that can handle a given
     *         problem. Null if none are found
     */
    public List<IQuickFixResolver> getQuickFixResolvers() {
        ProblemDescriptor descriptor = getQuickFixProblem().getProblemDescriptor();
        return descriptor != null ? getRegistry().get(descriptor.getType()) : null;
    }

    /**
     * Gets all the registered quick fix resolvers mapped to a particular
     * problem descriptor. Therefore a problem descriptor may be associated with
     * multiple resolvers.
     *
     * @return non-null registry of quick fix resolvers. May be empty.
     */
    protected Map<ProblemType, List<IQuickFixResolver>> getRegistry() {
        if (registry == null) {
            registry = new EnumMap<>(ProblemType.class);
            for (IQuickFixResolver resolver : getRegisteredResolvers(getQuickFixProblem())) {
                for (ProblemType type : resolver.getProblemTypes()) {
                    registry.computeIfAbsent(type, x -> new ArrayList<>())
                        .add(resolver);
                }
            }
        }
        return registry;
    }

    /**
     * Should never be null. Return empty array if none are registered.
     *
     * @param problem
     *            for which quick fix resolvers should be obtained.
     * @return non null list of resolvers for the given problem. Return empty if
     *         nothing is found
     */
    protected static IQuickFixResolver[] getRegisteredResolvers(QuickFixProblemContext problem) {
        return new IQuickFixResolver[] {
            // Convert to groovy
            new ConvertToGroovyFileResolver(problem),

            // Add missing Groovy imports
            new AddMissingGroovyImportsResolver(problem),

            // Add classpath container to project
            new AddGroovyRuntimeResolver(problem),

            // Add unimplemented abstract methods
            new AddUnimplementedResolver(problem),

            // Add class cast
            new AddClassCastResolver(problem),

            // Change modifier(s) of overridden method
            new ChangeOverriddenModifierResolver(problem),
        };
    }
}
