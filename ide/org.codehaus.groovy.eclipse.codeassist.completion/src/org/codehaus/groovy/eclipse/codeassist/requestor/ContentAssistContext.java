/*
 * Copyright 2003-2009 the original author or authors.
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

package org.codehaus.groovy.eclipse.codeassist.requestor;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;

/**
 * @author Andrew Eisenberg
 * @created Nov 9, 2009
 *
 */
public class ContentAssistContext {

    public final int completionLocation;  // the caret location where completion occurs
    public final String completionExpression; // the phrase that is being completed.  not null, but might be empty.
    public final ASTNode completionNode;  // the ast node that provides the type being completed.  might be null if there is none
    public final ASTNode containingCodeBlock;  // the import, method, field, class. or code block that contains this completion request
    public final ContentAssistLocation location; // the location of this content assist
    public final GroovyCompilationUnit unit;
    public final AnnotatedNode containingDeclaration;  // the class, method or field containing the completion location

    public ContentAssistContext(int completionLocation,
            String completionExpression, ASTNode completionNode,
            ASTNode containingCodeBlock, ContentAssistLocation location,
            GroovyCompilationUnit unit, AnnotatedNode containingDeclaration) {
        super();
        this.completionLocation = completionLocation;
        this.completionExpression = completionExpression;
        this.completionNode = completionNode;
        this.containingCodeBlock = containingCodeBlock;
        this.location = location;
        this.unit = unit;
        this.containingDeclaration = containingDeclaration;
    }
}
