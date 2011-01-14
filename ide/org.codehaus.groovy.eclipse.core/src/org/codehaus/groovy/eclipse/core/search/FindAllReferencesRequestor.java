/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.search;

import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;

/**
 * Finds all references to a particular Declaration in a file
 *
 * @author andrew
 * @created Dec 31, 2010
 */
public class FindAllReferencesRequestor implements ITypeRequestor {

    private final AnnotatedNode declaration;

    private final Collection<ASTNode> references;

    public FindAllReferencesRequestor(AnnotatedNode declaration) {
        this.declaration = declaration;
        this.references = new ArrayList<ASTNode>(10);
    }

    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (node.getLength() == 0) {
            return VisitStatus.CONTINUE;
        }

        if (node instanceof AnnotatedNode) {
            ASTNode maybeDeclaration = result.declaration;
            if (maybeDeclaration == null) {
                return VisitStatus.CONTINUE;
            }
            if (maybeDeclaration instanceof ClassNode) {
                // sometimes generated methods and properties have a classnode
                // as the declaration.
                // we want to ignore these
                if (!(node instanceof ClassExpression || node instanceof ClassNode)) {
                    return VisitStatus.CONTINUE;
                }

                maybeDeclaration = ((ClassNode) maybeDeclaration).redirect();
            }
            if (maybeDeclaration == declaration) {
                references.add(node);
            }
        }
        return VisitStatus.CONTINUE;
    }

    public Collection<ASTNode> getReferences() {
        return references;
    }
}
