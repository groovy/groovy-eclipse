/*
 * Copyright 2009-2023 the original author or authors.
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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
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
        this(value.getName(), DSLContributionGroup.getTypeName(value.getType()));
        this.value = value;
    }

    public Parameter toParameter(ResolverCache resolver) {
        if (value == null) {
            String typeName = type;
            if (type.indexOf('@') != -1) {
                try { // process annotation(s) using groovy compiler
                    CompilerConfiguration conf = new CompilerConfiguration(resolver.module.getContext().getConfiguration());
                    conf.setPreviewFeatures(false);
                    conf.setScriptBaseClass(null);
                    conf.setTargetBytecode(CompilerConfiguration.DEFAULT.getTargetBytecode());

                    CompilationUnit unit = new CompilationUnit(conf, null, resolver.module.getContext().getClassLoader());
                    unit.addSource("Script" + java.util.UUID.randomUUID().toString().replace('-', '$'),
                        "import groovy.transform.stc.*\n" + "void meth(" + type + " " + name + ") {}");
                    unit.compile(Phases.CANONICALIZATION);

                    MethodNode meth = unit.getFirstClassNode().getMethods("meth").get(0);
                    return meth.getParameters()[0];
                } catch (Exception | LinkageError e) {
                    GroovyDSLCoreActivator.logException(e);
                }

                String i = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
                typeName = type.replaceAll("@(?:" + i + "\\.)*" + i + "(?:\\([^\\)]*\\))?\\s*", "");
            }
            value = new Parameter(resolver != null ? resolver.resolve(typeName) : ClassHelper.dynamicType(), name);
        }
        return value;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
