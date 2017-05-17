/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.elements;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

/**
 * A resolved Java element suitable for hovers.  May include extra Javadoc
 * information to appear in the hover.
 */
public class GroovyResolvedSourceMethod extends ResolvedSourceMethod implements IGroovyResolvedElement {

    private final String extraDoc;
    private ASTNode inferredElement;

    public GroovyResolvedSourceMethod(JavaElement parent, String name, String[] parameterTypes, String uniqueKey, String extraDoc, ASTNode inferredElement) {
        super(parent, name, parameterTypes, uniqueKey);
        this.extraDoc = extraDoc;
        this.inferredElement = inferredElement;
    }

    public String getExtraDoc() {
        return extraDoc;
    }

    public ASTNode getInferredElement() {
        return inferredElement;
    }

    public String getInferredElementName() {
        if (inferredElement instanceof Variable) {
            return ((Variable) inferredElement).getName();
        } else if (inferredElement instanceof MethodNode) {
            return ((MethodNode) inferredElement).getName();
        } else if (inferredElement instanceof ClassNode) {
            return ((ClassNode) inferredElement).getName();
        }
        return inferredElement.getText();
    }

    @Override
    public Object getElementInfo() throws JavaModelException {
        try {
            return super.getElementInfo();
        } catch (JavaModelException jme) {
            if (!jme.getJavaModelStatus().isDoesNotExist() ||
                    !(inferredElement instanceof MethodNode)) {
                throw jme;
            }
            return new org.eclipse.jdt.internal.core.SourceMethodInfo() {{
                MethodNode method = (MethodNode) inferredElement;

                setReturnType(method.getReturnType().getNameWithoutPackage().toCharArray());
                setExceptionTypeNames(buildExceptionTypeNames(method.getExceptions()));
                setArgumentNames(buildArgumentNames(method.getParameters()));
                setNameSourceStart(method.getNameStart());
                setNameSourceEnd(method.getNameEnd());
                setSourceRangeStart(method.getStart());
                setSourceRangeEnd(method.getEnd());
                setFlags(method.getModifiers());
            }};
        }
    }

    private static char[][] buildArgumentNames(Parameter[] params) {
        final int n;
        if (params != null && (n = params.length) > 0) {
            char[][] names = new char[n][];
            for (int i = 0; i < n; i += 1) {
                names[i] = params[i].getName().toCharArray();
            }
            return names;
        }
        return CharOperation.NO_CHAR_CHAR;
    }

    private static char[][] buildExceptionTypeNames(ClassNode[] types) {
        final int n;
        if (types != null && (n = types.length) > 0) {
            char[][] names = new char[n][];
            for (int i = 0; i < n; i += 1) {
                names[i] = types[i].getNameWithoutPackage().toCharArray();
            }
            return names;
        }
        return CharOperation.NO_CHAR_CHAR;
    }
}
