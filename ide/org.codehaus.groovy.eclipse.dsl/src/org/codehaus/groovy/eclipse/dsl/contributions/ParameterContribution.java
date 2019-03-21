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
    private Parameter cachedParameter;

    public ParameterContribution(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public ParameterContribution(Parameter cachedParameter) {
        this.cachedParameter = cachedParameter;
        this.name = cachedParameter.getName();
        this.type = DSLContributionGroup.getTypeName(cachedParameter.getType());
    }

    public ParameterContribution(String name) {
        this.name = name;
        this.type = null;
    }

    public Parameter toParameter(ResolverCache resolver) {
        if (cachedParameter == null) {
            if (resolver != null) {
                cachedParameter = new Parameter(resolver.resolve(type), name);
            } else {
                cachedParameter = new Parameter(ClassHelper.DYNAMIC_TYPE, name);
            }
        }
        return cachedParameter;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
