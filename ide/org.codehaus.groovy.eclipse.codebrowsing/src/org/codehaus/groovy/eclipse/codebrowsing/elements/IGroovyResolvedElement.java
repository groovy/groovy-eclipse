/*
 * Copyright 2009-2022 the original author or authors.
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
package org.codehaus.groovy.eclipse.codebrowsing.elements;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;

/**
 * Allows Groovy elements to provide customized Javadoc hovers.
 */
public interface IGroovyResolvedElement extends IJavaElement, ISourceReference {

    String getKey();

    String getExtraDoc();

    ASTNode getInferredElement();

    default String getInferredElementName() {
        ASTNode element = getInferredElement();
        if (element instanceof Variable) {
            return ((Variable) element).getName();
        } else if (element instanceof ClassNode) {
            return ((ClassNode) element).getName();
        } else if (element instanceof MethodNode) {
            MethodNode method = (MethodNode) element;
            return "<init>".equals(method.getName()) ? method.getDeclaringClass().getName() : method.getName();
        }
        return element.getText();
    }
}
