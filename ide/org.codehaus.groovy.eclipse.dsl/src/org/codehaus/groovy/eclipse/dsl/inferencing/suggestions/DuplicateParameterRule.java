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
 * Checks if a parameter name is already present in a list of existing
 * parameters
 * 
 * @author Nieraj Singh
 * @created 2011-09-12
 */
public class DuplicateParameterRule implements IValueCheckingRule {

    private List<MethodParameter> existingParameters;

    static final String ERROR = "Parameter with the same name already exists. Choose a distinct parameter name.";

    public DuplicateParameterRule(List<MethodParameter> existingParameters) {
        this.existingParameters = existingParameters;
    }

    public ValueStatus checkValidity(Object value) {
        if (!(value instanceof String) || existingParameters == null) {
            return ValueStatus.getErrorStatus(value);
        }

        String paramName = (String) value;
        ValueStatus status = ValueStatus.getValidStatus(value);
        for (MethodParameter existingParamter : existingParameters) {

            if (existingParamter.getName().equals(paramName)) {
                status = ValueStatus.getErrorStatus(value, ERROR);
                break;
            }
        }

        return status;
    }

}