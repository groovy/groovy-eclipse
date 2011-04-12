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

import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.createDisplayString;
import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.getImage;

import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;

/**
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 *
 */
public class ModifiersCompletionProcessor extends AbstractGroovyCompletionProcessor {


    /* Others.  Not used
        "extends",
        "implements",
        "import",
        "interface",
        "throws",

     */

    // just the keywords that may be available in a class body
    private static String[] keywords =
    {
        "abstract",
        "def",
        "final",
        "native",
        "package",
        "private",
        "protected",
        "public",
        "static",
        "synchronized",
        "transient",
        "volatile",
        "void"
    };

    public ModifiersCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        String completionExpression = getContext().completionExpression;
        List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
        for (String keyword : keywords) {
            if (keyword.startsWith(completionExpression)) {
                proposals.add(createProposal(keyword, getContext()));
            }
        }
        return proposals;
    }

    /**
     * @param keyword
     * @param context
     * @return
     */
    private ICompletionProposal createProposal(String keyword,
            ContentAssistContext context) {
        InternalCompletionProposal proposal =  createProposal(CompletionProposal.KEYWORD, context.completionLocation);
        proposal.setName(keyword.toCharArray());
        proposal.setCompletion(keyword.toCharArray());
        proposal.setReplaceRange(context.completionLocation
                - context.completionExpression.length(), context.completionEnd);

        String completion= String.valueOf(proposal.getCompletion());
        int start= proposal.getReplaceStart();
        int length= context.completionExpression.length();
        StyledString label= createDisplayString(proposal);
        int relevance = Relevance.LOWEST.getRelevance(5);
        JavaCompletionProposal jcp = new JavaCompletionProposal(completion, start, length, null, label, relevance);
        jcp.setImage(getImage(proposal));
        return jcp;
    }
}
