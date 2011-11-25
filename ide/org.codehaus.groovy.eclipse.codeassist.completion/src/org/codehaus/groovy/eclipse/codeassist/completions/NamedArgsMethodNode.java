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
package org.codehaus.groovy.eclipse.codeassist.completions;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * A method node that knows which of its parameters are named, optional, and
 * regular
 *
 * @author andrew
 * @created Sep 9, 2011
 */
public class NamedArgsMethodNode extends MethodNode {

    private static final Parameter[] NO_PARAMETERS = new Parameter[0];

    private final Parameter[] regularParams;

    private final Parameter[] namedParams;

    private final Parameter[] optionalParams;

    /**
     * A combination of the regular and named params
     * Lazily initialized
     */
    private Parameter[] visibleParams;

    public NamedArgsMethodNode(String name, int modifiers, ClassNode returnType, Parameter[] regularParams,
            Parameter[] namedParams, Parameter[] optionalParams, ClassNode[] exceptions, Statement code) {
        super(name, modifiers, returnType, concatParams(regularParams, namedParams, optionalParams), exceptions, code);
        this.regularParams = regularParams;
        this.namedParams = namedParams;
        this.optionalParams = optionalParams;
    }

    private static Parameter[] concatParams(Parameter[] regularParams, Parameter[] namedParams, Parameter[] optionalParams) {
        regularParams = regularParams == null ? NO_PARAMETERS : regularParams;
        namedParams = namedParams == null ? NO_PARAMETERS : namedParams;
        optionalParams = optionalParams == null ? NO_PARAMETERS : optionalParams;

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
            visibleParams = new Parameter[regularParams.length + namedParams.length];
            System.arraycopy(regularParams, 0, visibleParams, 0, regularParams.length);
            System.arraycopy(namedParams, 0, visibleParams, regularParams.length, namedParams.length);
        }
        return visibleParams;
    }
}
