/*
 * Copyright 2009-2018 the original author or authors.
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
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.util.Util;

public class ConstructorReferenceSearchRequestor implements ITypeRequestor {

    private final SearchRequestor requestor;
    private final SearchParticipant participant;
    private final String declaringQualifiedName;

    public ConstructorReferenceSearchRequestor(ConstructorPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;
        this.declaringQualifiedName = String.valueOf(CharOperation.concat(pattern.declaringQualification, pattern.declaringSimpleName, '.'));
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (node instanceof ConstructorCallExpression && node.getEnd() > 0) {
            ConstructorCallExpression call = (ConstructorCallExpression) node;

            // only match on type name, not method parameters (for now)
            if (result.declaringType.getName().replace('$', '.').equals(declaringQualifiedName)) {

                // must translate from synthetic source to binary if necessary
                if (enclosingElement.getOpenable() instanceof GroovyClassFileWorkingCopy) {
                    enclosingElement = ((GroovyClassFileWorkingCopy) enclosingElement.getOpenable()).convertToBinary(enclosingElement);
                }
                boolean isConstructor = true, isSynthetic = false, isSuperInvocation = call.isSuperCall(), isWithinComment = false;

                SearchMatch match = new MethodReferenceMatch(
                    enclosingElement, SearchMatch.A_ACCURATE, call.getNameStart(), call.getNameEnd() + 1 - call.getNameStart(),
                    isConstructor, isSynthetic, isSuperInvocation, isWithinComment, participant, enclosingElement.getResource());
                try {
                    requestor.acceptSearchMatch(match);
                } catch (CoreException e) {
                    Util.log(e, "Error reporting search match inside of " + enclosingElement + " in resource " + enclosingElement.getResource());
                }
            }
        }
        return VisitStatus.CONTINUE;
    }
}
