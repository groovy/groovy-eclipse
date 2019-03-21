/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.completions;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * A method node that knows which of its parameters are named, optional, and regular.
 */
public class NamedArgsMethodNode extends MethodNode {

    private final Parameter[] regularParams;

    private final Parameter[] namedParams;

    private final Parameter[] optionalParams;

    /**
     * A combination of the regular and named params.
     * Lazily initialized
     */
    private Parameter[] visibleParams;

    public NamedArgsMethodNode(String name, int modifiers, ClassNode returnType, Parameter[] regularParams, Parameter[] namedParams, Parameter[] optionalParams, ClassNode[] exceptions, Statement code) {
        super(name, modifiers, returnType, concatParams(regularParams, namedParams, optionalParams), exceptions, code);
        this.regularParams = regularParams;
        this.namedParams = namedParams;
        this.optionalParams = optionalParams;
    }

    private static Parameter[] concatParams(Parameter[] regularParams, Parameter[] namedParams, Parameter[] optionalParams) {
        regularParams = regularParams == null ? Parameter.EMPTY_ARRAY : regularParams;
        namedParams = namedParams == null ? Parameter.EMPTY_ARRAY : namedParams;
        optionalParams = optionalParams == null ? Parameter.EMPTY_ARRAY : optionalParams;

        Parameter[] allParams = new Parameter[regularParams.length + namedParams.length + optionalParams.length];
        System.arraycopy(regularParams, 0, allParams, 0, regularParams.length);
        System.arraycopy(namedParams, 0, allParams, regularParams.length, namedParams.length);
        System.arraycopy(optionalParams, 0, allParams, regularParams.length + namedParams.length, optionalParams.length);
        return allParams;
    }

    public Parameter[] getRegularParams() {
        return regularParams;
    }

    public Parameter[] getNamedParams() {
        return namedParams;
    }

    public Parameter[] getOptionalParams() {
        return optionalParams;
    }

    public Parameter[] getVisibleParams() {
        if (visibleParams == null) {
            visibleParams = concatParams(regularParams, namedParams, null);
        }
        return visibleParams;
    }
}
