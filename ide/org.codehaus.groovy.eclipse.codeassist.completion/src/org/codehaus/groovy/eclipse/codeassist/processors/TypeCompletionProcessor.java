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
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 *
 */
public class TypeCompletionProcessor extends AbstractGroovyCompletionProcessor {

    public TypeCompletionProcessor(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();
        String toSearch = context.completionExpression.startsWith("new ") ? context.completionExpression.substring(4) : context.completionExpression;
        if (shouldShowTypes(context, toSearch)) {
            return Collections.emptyList();
        }

        int expressionStart = findExpressionStart(context);
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
                context, getJavaContext(), expressionStart,
                context.completionEnd - expressionStart,
                getNameEnvironment().nameLookup, monitor);

        getNameEnvironment().findTypes(toSearch.toCharArray(), true, // all member
                                                            // types, should
                                                            // be false when
                                                            // in
                                                            // constructor
                true, // camel case match
                getSearchFor(), requestor, monitor);

        List<ICompletionProposal> typeProposals = requestor
                .processAcceptedTypes();
        return typeProposals;
    }

    /**
     * Don't show types if there is no previous text (except if in imports)
     * Also, don't show types if there is a '.'
     * @param context
     * @param toSearch
     * @return
     */
    private boolean shouldShowTypes(ContentAssistContext context,
            String toSearch) {
        return (toSearch.length() == 0 && context.location != ContentAssistLocation.IMPORT) ||
                context.fullCompletionExpression.contains(".");
    }

    /**
     * @param context
     * @return
     */
    private int findExpressionStart(ContentAssistContext context) {
        // remove "new"
        int completionLength;
        if (context.completionExpression.startsWith("new ")) {
            completionLength = context.completionExpression.substring("new ".length()).trim().length();
        } else {
            completionLength = context.completionExpression.length();
        }

        int expressionStart = context.completionLocation-completionLength;
        return expressionStart;
    }

    /**
     * @return
     */
    private int getSearchFor() {
        switch(getContext().location) {
            case EXTENDS:
                return IJavaSearchConstants.CLASS;
            case IMPLEMENTS:
                return IJavaSearchConstants.INTERFACE;
            case EXCEPTIONS:
                return IJavaSearchConstants.CLASS;
            default:
                return IJavaSearchConstants.TYPE;
        }
    }

}
