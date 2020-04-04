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
package org.codehaus.jdt.groovy.model;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.util.Util;

public class GroovyProjectFacade {

    @Deprecated
    public GroovyProjectFacade(final IJavaElement element) {
        this(element.getJavaProject());
    }

    @Deprecated
    public GroovyProjectFacade(final IJavaProject project) {
        this.project = project;
    }

    public IJavaProject getProject() {
        return project;
    }

    private IJavaProject project;

    //--------------------------------------------------------------------------

    private static boolean hasAppropriateArgsForMain(final String[] params) {
        if (params == null || params.length != 1) {
            return false;
        }
        int array = Signature.getArrayCount(params[0]);
        String typeName;
        if (array == 1) {
            typeName = "String";
        } else if (array == 0) {
            typeName = "Object";
        } else {
            return false;
        }

        String sigNoArray = Signature.getElementType(params[0]);
        String name = Signature.getSignatureSimpleName(sigNoArray);
        String qual = Signature.getSignatureQualifier(sigNoArray);
        return (name.equals(typeName)) && (qual == null || qual.isEmpty() || "java.lang".equals(qual));
    }

    public static boolean hasGroovyMainMethod(final IType type) {
        try {
            for (IMethod method : type.getMethods()) {
                if (method.getElementName().equals("main") && Flags.isStatic(method.getFlags()) && // void or Object are valid return types
                    (method.getReturnType().equals("V") || method.getReturnType().endsWith("java.lang.Object;")) && hasAppropriateArgsForMain(method.getParameterTypes())) {

                    return true;
                }
            }
        } catch (JavaModelException e) {
            Util.log(e, "Exception searching for main method in " + type);
        }
        return false;
    }

    public static boolean isGroovyScript(final IType type) {
        ModuleNode module = Adapters.adapt(type.getCompilationUnit(), ModuleNode.class);
        if (module != null) {
            for (ClassNode classNode : module.getClasses()) {
                if (classNode.getNameWithoutPackage().equals(type.getElementName())) {
                    return classNode.isScript();
                }
            }
        }
        return false;
    }
}
