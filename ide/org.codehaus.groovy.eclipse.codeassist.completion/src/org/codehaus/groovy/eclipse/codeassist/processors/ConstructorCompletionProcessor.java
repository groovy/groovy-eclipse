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

package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Dec 10, 2009
 */
public class ConstructorCompletionProcessor extends AbstractGroovyCompletionProcessor {

    public ConstructorCompletionProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        char[] constructorCompletionText = getCompletionText(context.fullCompletionExpression);
        if (constructorCompletionText == null) {
            return Collections.emptyList();
        }
        int completionExprStart = context.completionLocation
                - constructorCompletionText.length;
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
                context, getJavaContext(), completionExprStart,
                context.completionEnd - completionExprStart,
                getNameEnvironment().nameLookup, monitor);
        getNameEnvironment().findConstructorDeclarations(
                constructorCompletionText, true, requestor, monitor);
        List<ICompletionProposal> constructoryProposals = requestor
                .processAcceptedConstructors();

        return constructoryProposals;
    }

    /**
     * removes whitespace and the 'new ' prefix and does a fail-fast if a
     * non-java identifier is found
     *
     * @param fullCompletionExpression
     * @return
     */
    private char[] getCompletionText(String fullCompletionExpression) {
        List<Character> chars = new LinkedList<Character>();
        if (fullCompletionExpression == null) {
            return new char[0];
        }
        char[] fullArray = fullCompletionExpression.toCharArray();
        int newIndex = CharOperation.indexOf("new ".toCharArray(), fullArray,
                true) + 4;
        if (newIndex == -1) {
            return null;
        }
        for (int i = newIndex; i < fullArray.length; i++) {
            if (Character.isWhitespace(fullArray[i])) {
                continue;
            } else if (Character.isJavaIdentifierPart(fullArray[i]) || fullArray[i] == '.') {
                chars.add(fullArray[i]);
            } else {
                // fail fast if something odd is found like parens or brackets
                return null;
            }
        }
        char[] res = new char[chars.size()];
        int i = 0;
        for (Character c : chars) {
            res[i] = c.charValue();
            i++;
        }
        return res;
    }
}
