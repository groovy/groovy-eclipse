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
package org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.writer;

import java.util.List;

import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovyMethodSuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.GroovySuggestionDeclaringType;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.IGroovySuggestion;
import org.codehaus.groovy.eclipse.dsl.inferencing.suggestions.MethodParameter;

/**
 * 
 * @author Nieraj Singh
 * @created 2011-08-09
 */
public class TransformElementFactory {

    public TransformElement getSuggestionsElement(IGroovySuggestion suggestion) {
        if (suggestion == null) {
            return null;
        }

        GroovyMethodSuggestion methodSuggestion = suggestion instanceof GroovyMethodSuggestion ? (GroovyMethodSuggestion) suggestion
                : null;

        String elementName = methodSuggestion != null ? SuggestionElementStatics.METHOD : SuggestionElementStatics.PROPERTY;

        TransformElement suggestionsElement = new TransformElement(elementName, null);

        suggestionsElement.addProperty(SuggestionElementStatics.NAME_ATT, suggestion.getName());
        suggestionsElement.addProperty(SuggestionElementStatics.TYPE_ATT, suggestion.getType());
        suggestionsElement.addProperty(SuggestionElementStatics.IS_STATIC_ATT, suggestion.isStatic() + "");
        suggestionsElement.addProperty(SuggestionElementStatics.IS_ACTIVE, suggestion.isActive() + "");

        if (methodSuggestion != null) {
            TransformElement argumentsElement = new TransformElement(SuggestionElementStatics.PARAMETERS, null);
            suggestionsElement.addChild(argumentsElement);

            argumentsElement.addProperty(SuggestionElementStatics.USE_NAMED_ARGUMENTS_ATT, methodSuggestion.useNamedArguments()
                    + "");

            List<MethodParameter> parameters = methodSuggestion.getParameters();
            if (parameters != null) {
                for (MethodParameter parameter : parameters) {
                    TransformElement parameterElement = new TransformElement(SuggestionElementStatics.PARAMETER, null);
                    parameterElement.addProperty(SuggestionElementStatics.NAME_ATT, parameter.getName());
                    parameterElement.addProperty(SuggestionElementStatics.TYPE_ATT, parameter.getType());
                    argumentsElement.addChild(parameterElement);
                }
            }
        }

        // Add the Javadoc
        TransformElement javadocElement = new TransformElement(SuggestionElementStatics.DOC, suggestion.getJavaDoc());
        suggestionsElement.addChild(javadocElement);

        return suggestionsElement;
    }

    public TransformElement getRootElement() {
        return new TransformElement(SuggestionElementStatics.ROOT, null);
    }

    public TransformElement getDeclaringTypeWriterElement(GroovySuggestionDeclaringType declaringType) {
        if (declaringType == null) {
            return null;
        }
        TransformElement declaringTypeElement = new TransformElement(SuggestionElementStatics.DECLARING_TYPE, null);

        TransformElementProperty property = new TransformElementProperty(SuggestionElementStatics.TYPE_ATT, declaringType.getName());

        declaringTypeElement.addProperty(property);

        List<IGroovySuggestion> suggestions = declaringType.getSuggestions();

        for (IGroovySuggestion suggestion : suggestions) {
            TransformElement suggestionElement = getSuggestionsElement(suggestion);
            declaringTypeElement.addChild(suggestionElement);
        }

        return declaringTypeElement;

    } 
}
