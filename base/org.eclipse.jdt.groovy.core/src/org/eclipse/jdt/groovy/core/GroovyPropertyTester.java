/*
 * Copyright 2009-2024 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.util.GroovyUtils.getGroovyVersion;

import java.util.function.Predicate;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;

public class GroovyPropertyTester extends PropertyTester {

    private static final Predicate<MethodNode> JAVA_MAIN = (MethodNode mn) -> {
        return mn.isPublic() && mn.isStatic() && mn.isVoidMethod() && oneStringArray(mn.getParameters());
    };

    private static final Predicate<MethodNode> JEP_445_MAIN = (MethodNode mn) -> {
        return !mn.isPrivate() && (mn.isVoidMethod() || mn.getReturnType().equals(ClassHelper.OBJECT_TYPE)) &&
            (mn.getParameters().length == 0 || oneObject(mn.getParameters()) || oneStringArray(mn.getParameters()));
    };

    private static boolean oneObject(Parameter[] p) {
        return p.length == 1 && !p[0].getType().isGenericsPlaceHolder() && p[0].getType().equals(ClassHelper.OBJECT_TYPE);
    }

    private static boolean oneStringArray(Parameter[] p) {
        return p.length == 1 && p[0].getType().isArray() && p[0].getType().getComponentType().equals(ClassHelper.STRING_TYPE);
    }

    //--------------------------------------------------------------------------

    @Override
    public boolean test(Object receiver, String property, Object[] arguments, Object expectedValue) {
        if (receiver instanceof IAdaptable) {
            ModuleNode node = Adapters.adapt(receiver, ModuleNode.class);
            if (node != null && !Boolean.TRUE.equals(node.getNodeMetaData("ParseError"))) {
                switch (property) {
                case "hasMain":
                    return node.getClasses().stream().flatMap(cn -> cn.getDeclaredMethods("main").stream()).anyMatch(JAVA_MAIN);
                case "isScript":
                    return !node.getStatementBlock().isEmpty() || (!node.getClasses().isEmpty() &&
                        node.getClasses().get(0).getNameEnd() < 1 /* un-named */ && getGroovyVersion().getMajor() >= 5 &&
                        node.getClasses().get(0).getDeclaredMethods("main").stream().anyMatch(JEP_445_MAIN.and(JAVA_MAIN.negate())));
                }
            }
        }
        return false;
    }
}
