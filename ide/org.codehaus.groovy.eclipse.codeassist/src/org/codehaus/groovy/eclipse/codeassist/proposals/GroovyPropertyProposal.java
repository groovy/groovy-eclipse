/*
 * Copyright 2009-2024 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaFieldCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.viewers.StyledString;

public class GroovyPropertyProposal extends AbstractGroovyProposal {

    private final PropertyNode property;
    private final String contributor;

    public GroovyPropertyProposal(PropertyNode property) {
        this.property = property;
        this.contributor = "Groovy";
    }

    public GroovyPropertyProposal(PropertyNode property, String contributor) {
        this.property = property;
        this.contributor = contributor;
    }

    @Override
    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            return null;
        }
        CompletionProposal proposal = createProposal(context);
        return new GroovyJavaFieldCompletionProposal(proposal, createDisplayString(property), javaContext);
    }

    protected StyledString createDisplayString(PropertyNode property) {
        return new StyledString()
            .append(property.getName())
            .append(" : ")
            .append(ProposalUtils.createSimpleTypeName(property.getType()))
            .append(" - ")
            .append(ProposalUtils.createSimpleTypeName(property.getDeclaringClass()), StyledString.QUALIFIER_STYLER)
            .append(" (" + contributor + ")", StyledString.DECORATIONS_STYLER);
    }

    private CompletionProposal createProposal(ContentAssistContext context) {
        var proposal = (InternalCompletionProposal) CompletionProposal.create(CompletionProposal.FIELD_REF, context.completionLocation);
        proposal.setFlags(property.getModifiers() | (GroovyUtils.isDeprecated(property) ? Flags.AccDeprecated : 0));
        proposal.setName(property.getName().toCharArray());
        proposal.setCompletion(proposal.getName());
        proposal.setRelevance(computeRelevance(context));
        proposal.setSignature(ProposalUtils.createTypeSignature(property.getType()));
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(property.getDeclaringClass()));
        proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionEnd);
        return proposal;
    }
}
