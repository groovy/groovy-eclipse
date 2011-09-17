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

/**
 * 
 * @author Nieraj Singh
 * @created Apr 19, 2011
 */
public class GroovySuggestionDeclaringType implements IBaseGroovySuggestion {
    private List<IGroovySuggestion> suggestions;

    private String name;

    public GroovySuggestionDeclaringType(String name) {
        this.suggestions = new ArrayList<IGroovySuggestion>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Creates a new instance of the suggestion in the specified declaring type.
     * If a suggestion
     * already exists that matches the descriptor, null is returned
     * 
     * @param type
     * @param isActive whether the suggestion should be active in the declaring
     *            type
     * @return new created suggestion, or null if the suggestion already exists
     */
    public IGroovySuggestion createSuggestion(SuggestionDescriptor descriptor) {

        IGroovySuggestion suggestion = new SuggestionFactory(descriptor).createSuggestion(this);
        if (containsSuggestion(suggestion)) {
            return null;
        }
        suggestions.add(suggestion);
        return suggestion;
    }

    protected boolean containsSuggestion(IGroovySuggestion suggestion) {
        boolean isContained = false;
        if (suggestion instanceof GroovyPropertySuggestion) {
            String name = suggestion.getName();
            for (IGroovySuggestion existingSugg : suggestions) {
                if (existingSugg instanceof GroovyPropertySuggestion && existingSugg.getName().equals(name)) {
                    isContained = true;
                    break;
                }
            }
        } else if (suggestion instanceof GroovyMethodSuggestion) {
            String name = suggestion.getName();
            GroovyMethodSuggestion methodSuggestion = (GroovyMethodSuggestion) suggestion;
            for (IGroovySuggestion existingSugg : suggestions) {
                if (existingSugg instanceof GroovyMethodSuggestion && existingSugg.getName().equals(name)) {
                    GroovyMethodSuggestion existingMethodSuggestion = (GroovyMethodSuggestion) existingSugg;

                    List<MethodParameter> existingParameters = existingMethodSuggestion.getParameters();
                    List<MethodParameter> parameters = methodSuggestion.getParameters();
                    if (existingParameters != null) {
                        if (parameters != null && parameters.size() == existingParameters.size()) {
                            // make sure the types of each parameter is the
                            // same. names don't matter
                            boolean same = true;
                            for (int i = 0; i < parameters.size(); i++) {
                                String existingType = existingParameters.get(i).getType();
                                String type = parameters.get(i).getType();
                                if (type != null) {
                                    if (!type.equals(existingType)) {
                                        same = false;
                                        break;
                                    }
                                } else if (existingType != null) {
                                    same = false;
                                    break;
                                }
                            }
                            if (same) {
                                isContained = true;
                                break;
                            }
                        }
                    } else if (parameters == null) {
                        isContained = true;
                        break;
                    }
                }
            }
        }
        return isContained;
    }

    public IGroovySuggestion replaceSuggestion(SuggestionDescriptor descriptor, IGroovySuggestion suggestion) {

        if (suggestions.contains(suggestion)) {
            removeSuggestion(suggestion);

            IGroovySuggestion nwSuggestion = createSuggestion(descriptor);
            return nwSuggestion;
        }
        return null;

    }

    public boolean removeSuggestion(IGroovySuggestion suggestion) {
        return suggestions.remove(suggestion);
    }

    public List<IGroovySuggestion> getSuggestions() {
        return suggestions;
    }

    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }

}
