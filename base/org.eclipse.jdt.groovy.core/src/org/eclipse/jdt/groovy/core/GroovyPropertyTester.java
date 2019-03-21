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
package org.eclipse.jdt.groovy.core;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;

public class GroovyPropertyTester extends PropertyTester {

    @Override
    public boolean test(Object receiver, String property, Object[] arguments, Object expectedValue) {
        if (receiver instanceof IAdaptable) {
            ModuleNode node = Adapters.adapt(receiver, ModuleNode.class);
            if (node != null) {
                switch (property) {
                case "isScript":
                    return !node.getStatementBlock().isEmpty();
                case "hasMain":
                    return node.getClasses().stream().flatMap(cn -> cn.getMethods("main").stream()).filter(MethodNode::isStatic).anyMatch(mn -> {
                        Parameter[] parameters = mn.getParameters();
                        if (parameters != null && parameters.length == 1) {
                            ClassNode type = parameters[0].getType();
                            return (type.equals(ClassHelper.OBJECT_TYPE) || (type.isArray() &&
                                    type.getComponentType().equals(ClassHelper.STRING_TYPE)));
                        }
                        return false;
                    });
                }
            }
        }
        return false;
    }
}
