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
package org.codehaus.jdt.groovy.model;

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see org.eclipse.jdt.internal.corext.util.JavaModelUtil
 */
public class JavaCoreUtil {

    private JavaCoreUtil() {}

    public static IMethod findMethod(MethodNode methodNode, IType declaringType) {
        try {
            char[] methodName = methodNode.getName().toCharArray();
            String[] paramTypes = GroovyUtils.getParameterTypeSignatures(methodNode, declaringType.isBinary());
            return Util.findMethod(declaringType, methodName, paramTypes, methodNode instanceof ConstructorNode);
        } catch (JavaModelException e) {
            Util.log(e);
            return null;
        }
    }

    public static IMethod findMethod(MethodNode methodNode, IJavaElement referenceContext) {
        IType declaringType = findType(methodNode.getDeclaringClass().getName(), referenceContext);
        return findMethod(methodNode, declaringType);
    }

    public static IType findType(String fullyQualifiedName, IJavaElement referenceContext) {
        try {
            IJavaProject project = referenceContext.getJavaProject();
            return project.findType(fullyQualifiedName.replace('$', '.'), (IProgressMonitor) null);
        } catch (JavaModelException e) {
            Util.log(e);
            return null;
        }
    }
}
