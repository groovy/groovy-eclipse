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
import org.codehaus.groovy.ast.FieldNode;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedSourceField;

/**
 * A resolved java element suitable for hovers. Includes extra Javadoc information to appear in the hover.
 */
public class GroovyResolvedSourceField extends ResolvedSourceField implements IGroovyResolvedElement {

    private final String extraDoc;
    private final ASTNode inferredElement;

    public GroovyResolvedSourceField(JavaElement parent, String name, String uniqueKey, String extraDoc, ASTNode inferredElement) {
        super(parent, name, uniqueKey);
        this.extraDoc = extraDoc;
        this.inferredElement = inferredElement;
    }

    @Override
    public String getExtraDoc() {
        return extraDoc;
    }

    @Override
    public ASTNode getInferredElement() {
        return inferredElement;
    }

    @Override
    public Object getElementInfo() throws JavaModelException {
        try {
            return super.getElementInfo();
        } catch (JavaModelException jme) {
            if (!jme.getJavaModelStatus().isDoesNotExist() || !(inferredElement instanceof FieldNode)) {
                throw jme;
            }
            // @formatter:off
            return new org.eclipse.jdt.internal.core.SourceFieldElementInfo() {{
                FieldNode field = (FieldNode) inferredElement;

                setTypeName(field.getType().toString(false).toCharArray());
                setNameSourceStart(field.getNameStart());
                setNameSourceEnd(field.getNameEnd());
                setSourceRangeStart(field.getStart());
                setSourceRangeEnd(field.getEnd());
                setFlags(field.getModifiers());
            }};
            // @formatter:on
        }
    }
}
