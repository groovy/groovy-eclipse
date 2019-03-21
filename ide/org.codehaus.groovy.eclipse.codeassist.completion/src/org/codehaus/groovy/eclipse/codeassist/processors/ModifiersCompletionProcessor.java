/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;

public class ModifiersCompletionProcessor extends AbstractGroovyCompletionProcessor {

    private static final String[] CLASS_MEMBER_KEYWORDS = {
        "abstract",
        "def",
        "final",
        "native",
      //"package", TODO: Replace with "@PackageScope"?
        "private",
        "protected",
        "public",
        "static",
        "synchronized",
        "transient",
        "void",
        "volatile",
    };

    public ModifiersCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        String completionExpression = getContext().completionExpression;
        List<ICompletionProposal> proposals = new ArrayList<>();
        for (String keyword : CLASS_MEMBER_KEYWORDS) {
            if (keyword.startsWith(completionExpression)) {
                proposals.add(createProposal(keyword, completionExpression, getContext().completionLocation));
            }
        }
        return proposals;
    }

    protected ICompletionProposal createProposal(String keyword, String completionExpression, int completionLocation) {
        int length = completionExpression.length();
        int offset = (completionLocation - length);
        int relevance = Relevance.LOWEST.getRelevance(5);
        StyledString displayString = Strings.markJavaElementLabelLTR(new StyledString(keyword));
        return new JavaCompletionProposal(keyword, offset, length, null, displayString, relevance);
    }
}
