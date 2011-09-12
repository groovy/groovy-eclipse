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
package org.codehaus.groovy.eclipse.dsl.contributions;

import groovy.lang.GroovyObjectSupport;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * 
 * @author andrew
 * @created Mar 4, 2011
 */
public class ContributionGroup extends GroovyObjectSupport implements IContributionGroup {
    
    protected final static String DEFAULT_PROVIDER = "User";
    
    protected final static int DEFAULT_RELEVANCE_MULTIPLIER = 11;
    
    protected List<IContributionElement> contributions = new ArrayList<IContributionElement>();
    
    
    
    // alternative way to add a method contribution
    public void addMethodContribution(String name, ParameterContribution[] params, String returnType, String declaringType,
            boolean isStatic, boolean useNamedArgs) {
        contributions.add(new MethodContributionElement(name, params, returnType, declaringType, isStatic, DEFAULT_PROVIDER, null, useNamedArgs, false, DEFAULT_RELEVANCE_MULTIPLIER));
    }

    // alternative way to add a property contribution
    public void addPropertyContribution(String name, String type, String declaringType, boolean isStatic) {
        contributions.add(new PropertyContributionElement(name, type, declaringType, isStatic, DEFAULT_PROVIDER, null, false, DEFAULT_RELEVANCE_MULTIPLIER));
    }

    public List<IContributionElement> getContributions(
            GroovyDSLDContext pattern, BindingSet matches) {
        // only need to match on current type.
        List<IContributionElement> currentContributions = new ArrayList<IContributionElement>();
        for (IContributionElement element : contributions) {
            if (pattern.matchesType(element.getDeclaringTypeName())) {
                currentContributions.add(element);
            }
        }
        return currentContributions;
    }


}
