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
package org.codehaus.jdt.groovy.ast;

import org.codehaus.groovy.ast.Parameter;

public interface MethodNodeWithNamedParams {

    Parameter[] getNamedParams();

    Parameter[] getOptionalParams();

    Parameter[] getPositionalParams();

    default Parameter[] getVisibleParams() {
        return concatParams(getPositionalParams(), getNamedParams(), null);
    }

    static Parameter[] concatParams(Parameter[] params, Parameter[] namedParams, Parameter[] optionalParams) {
        if (params == null) params = Parameter.EMPTY_ARRAY;
        if (namedParams == null) namedParams = Parameter.EMPTY_ARRAY;
        if (optionalParams == null) optionalParams = Parameter.EMPTY_ARRAY;

        Parameter[] allParams = new Parameter[params.length + namedParams.length + optionalParams.length];

        // https://github.com/groovy/groovy-eclipse/issues/613
        // named parameters first, followed by positional params (incl. trailing closure)
        System.arraycopy(namedParams, 0, allParams, 0, namedParams.length);
        System.arraycopy(optionalParams, 0, allParams, namedParams.length, optionalParams.length);
        System.arraycopy(params, 0, allParams, namedParams.length + optionalParams.length, params.length);

        return allParams;
    }
}
