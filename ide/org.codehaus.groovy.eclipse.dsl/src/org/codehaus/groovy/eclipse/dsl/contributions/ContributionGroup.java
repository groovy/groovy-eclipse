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
package org.codehaus.groovy.eclipse.dsl.contributions;

import static java.util.stream.Collectors.toList;

import static org.eclipse.jdt.core.Flags.AccStatic;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.GroovyObjectSupport;

import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

public class ContributionGroup extends GroovyObjectSupport implements IContributionGroup {

    protected static final String DEFAULT_PROVIDER = "User";

    protected static final int DEFAULT_RELEVANCE_MULTIPLIER = 11;

    protected List<IContributionElement> contributions = new ArrayList<>();

    /**
     * Alternative way to add a method contribution.
     */
    public void addMethodContribution(final String name, final ParameterContribution[] parameters, final String returnType,
                                            final String declaringType, final boolean isStatic, final boolean namedArgs) {
        int mult = DEFAULT_RELEVANCE_MULTIPLIER;
        contributions.add(new MethodContributionElement(name, parameters, returnType, declaringType, isStatic, DEFAULT_PROVIDER, null, namedArgs, false, mult));
    }

    /**
     * Alternative way to add a property contribution.
     */
    public void addPropertyContribution(final String name, final String type, final String declaringType, final boolean isStatic) {
        int mods = isStatic ? AccStatic : 0, mult = DEFAULT_RELEVANCE_MULTIPLIER;
        contributions.add(new PropertyContributionElement(name, type, declaringType, mods, DEFAULT_PROVIDER, null, false, mult));
    }

    @Override
    public List<IContributionElement> getContributions(final GroovyDSLDContext pattern, final BindingSet matches) {
        return contributions.stream().filter(element -> pattern.matchesType(element.getDeclaringTypeName())).collect(toList());
        // only need to match on current type           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
}
