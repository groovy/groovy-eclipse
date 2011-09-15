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

import java.util.List;

/**
 * Generic disposable descriptor used to create concrete suggestions (either
 * property or method suggestion). Useful as a return value from a dialogue where only
 * one type is returned containing all dialogue field values.
 * 
 * @author Nieraj Singh
 * @created 2011-08-04
 */
public class SuggestionDescriptor {

    private boolean isStatic;

    private boolean isMethod = false;

    private String name;

    private String javaDoc;

    private boolean isActive;

    private String suggestionType;

    private String declaringTypeName;

    private boolean useArgumentNames;

    private List<MethodParameter> parameters;

    protected SuggestionDescriptor(String declaringTypeName, boolean isStatic, boolean isMethod, String name, String javaDoc,
            String suggestionType, boolean useArgumentNames, List<MethodParameter> parameters, boolean isActive) {
        this.isStatic = isStatic;
        this.isMethod = isMethod;
        this.name = name;
        this.javaDoc = javaDoc;
        this.suggestionType = suggestionType;
        this.isActive = isActive;
        this.declaringTypeName = declaringTypeName;
        this.useArgumentNames = useArgumentNames;
        this.parameters = parameters;
    }

    /**
     * Use only if descriptor is for a method
     */
    public SuggestionDescriptor(String declaringTypeName, boolean isStatic, String name, String javaDoc, String suggestionType,
            boolean useArgumentNames, List<MethodParameter> parameters, boolean isActive) {
        this(declaringTypeName, isStatic, true, name, javaDoc, suggestionType, useArgumentNames, parameters, isActive);
    }

    /**
     * Use only if descriptor is for a property
     */
    public SuggestionDescriptor(String declaringTypeName, boolean isStatic, String name, String javaDoc, String suggestionType,
            boolean isActive) {
        this(declaringTypeName, isStatic, false, name, javaDoc, suggestionType, false, null, isActive);
    }

    /**
     * Creates a descriptor with the values of the given suggestion.
     */
    public SuggestionDescriptor(IGroovySuggestion suggestion) {
        this(suggestion, suggestion.isActive());
    }

    /**
     * Creates a descriptor with the values of the given suggestion, but with
     * the specified state.
     */
    public SuggestionDescriptor(IGroovySuggestion suggestion, boolean isActive) {
        this.isStatic = suggestion.isStatic();
        this.isActive = isActive;
        this.name = suggestion.getName();
        this.javaDoc = suggestion.getJavaDoc();
        this.suggestionType = suggestion.getType();
        this.declaringTypeName = suggestion.getDeclaringType().getName();
        if (suggestion instanceof GroovyMethodSuggestion) {
            GroovyMethodSuggestion methodSuggestion = (GroovyMethodSuggestion) suggestion;
            this.useArgumentNames = methodSuggestion.useNamedArguments();
            this.parameters = methodSuggestion.getParameters();
            this.isMethod = true;
        }

    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public String getName() {
        return name;
    }

    public String getDeclaringTypeName() {
        return declaringTypeName;
    }

    public String getJavaDoc() {
        return javaDoc;
    }

    public String getSuggestionType() {
        return suggestionType;
    }

    public boolean isUseArgumentNames() {
        return useArgumentNames;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

}
