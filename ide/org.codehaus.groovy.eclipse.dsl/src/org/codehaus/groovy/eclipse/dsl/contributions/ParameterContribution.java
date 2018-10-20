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
package org.codehaus.groovy.eclipse.dsl.contributions;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.dsl.lookup.ResolverCache;

/**
 * A parameter of a method contribution.
 */
public class ParameterContribution {

    final String name;
    final String type;
    private Parameter value;

    public ParameterContribution(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public ParameterContribution(Parameter value) {
        this.value = value;
        this.name = value.getName();
        this.type = DSLContributionGroup.getTypeName(value.getType());
    }

    public Parameter toParameter(ResolverCache resolver) {
        if (value == null) {
            value = new Parameter(resolver != null ? resolver.resolve(type) : ClassHelper.DYNAMIC_TYPE, name);
        }
        return value;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
