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
package org.codehaus.groovy.eclipse.codebrowsing.elements;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Variable;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryField;

/**
 * A resolved java element suitable for hovers. Includes extra Javadoc information to appear in the hover.
 */
public class GroovyResolvedBinaryField extends ResolvedBinaryField implements IGroovyResolvedElement {

    private final String extraDoc;
    private final ASTNode inferredElement;

    public GroovyResolvedBinaryField(JavaElement parent, String fieldName, String uniqueKey, String extraDoc, ASTNode inferredElement) {
        super(parent, fieldName, uniqueKey);
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
        } else {
            return inferredElement.getText();
        }
    }
}
