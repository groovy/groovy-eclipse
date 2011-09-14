/*
 * Copyright 2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.dsl.contributions.ContributionGroup;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionElement;
import org.codehaus.groovy.eclipse.dsl.contributions.MethodContributionElement;
import org.codehaus.groovy.eclipse.dsl.contributions.ParameterContribution;
import org.codehaus.groovy.eclipse.dsl.contributions.PropertyContributionElement;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.eclipse.core.resources.IFile;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-05-31
 */
public class SuggestionsContributionGroup extends ContributionGroup {

    private IFile file;

    public SuggestionsContributionGroup(IFile file) {
        this.file = file;
    }

    public List<IContributionElement> getContributions(GroovyDSLDContext pattern, BindingSet matches) {

        // only need to match on current type.
        List<IContributionElement> currentContributions = new ArrayList<IContributionElement>();

        // FIXNS: May not be optimal. To improve performance per
        // inferencing run (i.e. AST walk)
        // cache any supertypes check into the GroovyDSLDContext, preferably
        // a map with current subtype obtained
        // from the pattern as a key, and the list of ClassNodes in the AST
        // as a value. In addition,
        // cache the list of exact declaring types in the ProjectSuggestion
        // that matches a specific current subtype
        // in a map, so that the calculation does not have to be recomputed.
        // It is safe to cache anything in the GroovyDSLDContext and avoid
        // memory leaks as it gets purged
        // at the end of the AST walk. An AST walk happens every time a
        // change is made to a Groovy file.

        List<GroovySuggestionDeclaringType> superTypes = new DeclaringTypeSuperTypeMatcher(file.getProject())
                .getAllSuperTypes(pattern);
        if (superTypes != null) {
            for (GroovySuggestionDeclaringType declaringType : superTypes) {

                List<IGroovySuggestion> suggestions = declaringType.getSuggestions();
                if (suggestions != null) {
                    for (IGroovySuggestion suggestion : suggestions) {
                        if (suggestion.isActive()) {
                            if (suggestion instanceof GroovyPropertySuggestion) {
                                GroovyPropertySuggestion prop = (GroovyPropertySuggestion) suggestion;

                                currentContributions.add(new PropertyContributionElement(prop.getName(), prop.getType(), prop
                                        .getDeclaringType().getName(), prop.isStatic(), DEFAULT_PROVIDER, prop.getJavaDoc(), false,
                                        DEFAULT_RELEVANCE_MULTIPLIER));

                            } else if (suggestion instanceof GroovyMethodSuggestion) {

                                GroovyMethodSuggestion method = (GroovyMethodSuggestion) suggestion;
                                ParameterContribution[] paramContribution = null;

                                List<MethodParameter> parameters = method.getParameters();

                                if (parameters != null) {
                                    paramContribution = new ParameterContribution[method.getParameters().size()];
                                    int i = 0;
                                    for (MethodParameter parameter : parameters) {
                                        if (i < paramContribution.length) {
                                            paramContribution[i++] = new ParameterContribution(parameter.getName(),
                                                    parameter.getType());
                                        }
                                    }

                                }
                                currentContributions.add(new MethodContributionElement(method.getName(), paramContribution, method
                                        .getType(), method.getDeclaringType().getName(), method.isStatic(), DEFAULT_PROVIDER,
                                        method.getJavaDoc(), method.useNamedArguments(), false, DEFAULT_RELEVANCE_MULTIPLIER));
                            }
                        }

                    }
                }

            }
            return currentContributions;
        }

        return null;

    }
}
