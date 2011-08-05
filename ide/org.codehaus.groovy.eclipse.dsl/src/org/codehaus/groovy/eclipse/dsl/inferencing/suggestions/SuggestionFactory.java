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

/**
 * 
 * @author Nieraj Singh
 * @created 2011-08-04
 */
public class SuggestionFactory {

    private SuggestionDescriptor descriptor;

    public SuggestionFactory(SuggestionDescriptor descriptor) {
        this.descriptor = descriptor;

    }

    public IGroovySuggestion createSuggestion(GroovySuggestionDeclaringType declaringType) {

        IGroovySuggestion suggestion = null;
        if (declaringType != null) {
            suggestion = descriptor.isMethod() ? new GroovyMethodSuggestion(declaringType, descriptor.getParameters(),
                    descriptor.isUseArgumentNames(), descriptor.getName(), descriptor.getSuggestionType(), descriptor.isStatic(),
                    descriptor.getJavaDoc(), descriptor.isActive()) :

            new GroovyPropertySuggestion(declaringType, descriptor.getName(), descriptor.getSuggestionType(),
                    descriptor.isStatic(), descriptor.getJavaDoc(), descriptor.isActive());

        }
        return suggestion;
    }

}
