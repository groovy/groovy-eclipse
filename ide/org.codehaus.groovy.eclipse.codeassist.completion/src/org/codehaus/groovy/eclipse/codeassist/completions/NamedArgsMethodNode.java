/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.completions;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * A method node that knows which of its parameters are regular (aka positional),
 * named (required) and named (optional).
 */
public class NamedArgsMethodNode extends MethodNode {

    private final Parameter[] params, namedParams, optionalParams;

    public NamedArgsMethodNode(String name, int modifiers, ClassNode returnType, Parameter[] params, Parameter[] namedParams, Parameter[] optionalParams, ClassNode[] exceptions, Statement code) {
        super(name, modifiers, returnType, concatParams(params, namedParams, optionalParams), exceptions, code);
        this.params = params;
        this.namedParams = namedParams;
        this.optionalParams = optionalParams;
    }

    private static Parameter[] concatParams(Parameter[] params, Parameter[] namedParams, Parameter[] optionalParams) {
        if (params == null) params = Parameter.EMPTY_ARRAY;
        if (namedParams == null) namedParams = Parameter.EMPTY_ARRAY;
        if (optionalParams == null) optionalParams = Parameter.EMPTY_ARRAY;

        Parameter[] allParams = new Parameter[params.length + namedParams.length + optionalParams.length];
        System.arraycopy(params, 0, allParams, 0, params.length);
        System.arraycopy(namedParams, 0, allParams, params.length, namedParams.length);
        System.arraycopy(optionalParams, 0, allParams, params.length + namedParams.length, optionalParams.length);
        return allParams;
    }

    public Parameter[] getRegularParams() {
        return params;
    }

    public Parameter[] getNamedParams() {
        return namedParams;
    }

    public Parameter[] getOptionalParams() {
        return optionalParams;
    }

    public Parameter[] getVisibleParams() {
        return concatParams(getRegularParams(), getNamedParams(), null);
    }
}
