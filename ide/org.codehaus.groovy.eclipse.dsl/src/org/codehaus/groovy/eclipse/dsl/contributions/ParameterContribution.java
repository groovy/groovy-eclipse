/*
 * Copyright 2009-2018 the original author or authors.
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

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
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
            if (type.indexOf('@') >= 0) {
                try {
                    List<ASTNode> nodes = new AstBuilder().buildFromString(CompilePhase.CANONICALIZATION, false,
                        "import groovy.transform.stc.*\n" + "void meth(" + type + " " + name + ") {}");
                    MethodNode meth = ((ClassNode) nodes.get(1)).getMethods("meth").get(0);
                    return meth.getParameters()[0];
                } catch (Exception e) {
                    GroovyDSLCoreActivator.logException(e);
                }
            }
            value = new Parameter(resolver != null ? resolver.resolve(type) : ClassHelper.DYNAMIC_TYPE, name);
        }
        return value;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
