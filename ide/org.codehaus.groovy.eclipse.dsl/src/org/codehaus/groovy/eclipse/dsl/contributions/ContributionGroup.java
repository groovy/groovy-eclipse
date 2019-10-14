/*
 * Copyright 2009-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import groovy.lang.GroovyObjectSupport;

import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

public class ContributionGroup extends GroovyObjectSupport implements IContributionGroup {

    protected static final String DEFAULT_PROVIDER = "User";

    protected static final int DEFAULT_RELEVANCE_MULTIPLIER = 11;

    protected List<IContributionElement> contributions = new ArrayList<>();

    // alternative way to add a method contribution
    public void addMethodContribution(String name, ParameterContribution[] params, String returnType, String declaringType, boolean isStatic, boolean useNamedArgs) {
        contributions.add(new MethodContributionElement(name, params, returnType, declaringType, isStatic, DEFAULT_PROVIDER, null, useNamedArgs, false, DEFAULT_RELEVANCE_MULTIPLIER));
    }

    // alternative way to add a property contribution
    public void addPropertyContribution(String name, String type, String declaringType, boolean isStatic) {
        contributions.add(new PropertyContributionElement(name, type, declaringType, isStatic, DEFAULT_PROVIDER, null, false, DEFAULT_RELEVANCE_MULTIPLIER));
    }

    @Override
    public List<IContributionElement> getContributions(GroovyDSLDContext pattern, BindingSet matches) {
        // only need to match on current type.
        List<IContributionElement> currentContributions = new ArrayList<>();
        for (IContributionElement element : contributions) {
            if (pattern.matchesType(element.getDeclaringTypeName())) {
                currentContributions.add(element);
            }
        }
        return currentContributions;
    }
}
